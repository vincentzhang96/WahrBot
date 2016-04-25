/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vincent Zhang/PhoenixLAB
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package co.phoenixlab.discord.api.impl;

import co.phoenixlab.discord.api.entities.GatewayPayload;
import co.phoenixlab.discord.api.entities.ReadyMessage;
import co.phoenixlab.discord.api.enums.GatewayOP;
import co.phoenixlab.discord.api.enums.WebSocketMessageType;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.exceptions.RateLimitExceededException;
import co.phoenixlab.discord.api.request.ConnectionProperties;
import co.phoenixlab.discord.api.request.GatewayConnectRequest;
import co.phoenixlab.discord.api.request.GatewayResumeRequest;
import co.phoenixlab.discord.api.request.WSRequest;
import co.phoenixlab.discord.api.util.RateLimiter;
import co.phoenixlab.discord.api.util.WahrDiscordApiUtils;
import com.codahale.metrics.Timer;
import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class WSClient {

    private static final Logger WS_LOGGER = LoggerFactory.getLogger(WSClient.class);
    private final URI serverURI;

    private Lock delegateLock;
    private WebsocketDelegate delegate;

    private final WahrDiscordApiImpl api;
    private final WahrDiscordApiImpl.Stats stats;

    private final Gson gson;

    private int webSocketProtocolVersion;
    private int largeThreshold;
    private boolean compress;
    private String operatingSystem;
    private String browser;
    private String referrer;
    private String referringDomain;

    private RateLimiter sendLimiter;
    private volatile int lastSequenceId;
    private WebSocketClient.WebSocketClientFactory factory;
    private ScheduledFuture<?> heartbeat;

    public WSClient(URI serverURI, WahrDiscordApiImpl api) {
        this.serverURI = serverURI;
        stats = api.getStats();
        this.api = api;
        gson = WahrDiscordApiUtils.createGson();
        webSocketProtocolVersion = 4;
        largeThreshold = 250;
        compress = false;
        operatingSystem = System.getProperty("os.name");
        browser = "Java";
        referrer = "";
        referringDomain = "";
        sendLimiter = new RateLimiter("gateway", TimeUnit.SECONDS.toMillis(60), 120);
        lastSequenceId = -1;
        delegateLock = new ReentrantLock();
    }

    public boolean connectBlocking() throws InterruptedException {
        try {
            delegateLock.lock();
            if (delegate != null) {
                delegate.close();
            }
            delegate = new WebsocketDelegate(serverURI, this);
            delegate.setWebSocketFactory(factory);
            return delegate.connectBlocking();
        } finally {
            delegateLock.unlock();
        }
    }

    void onOpen(ServerHandshake handshakedata) {
        Thread.currentThread().setName("WebSocketClient");
        WS_LOGGER.info("WebSocket connection opened");
        if (api.getSessionIdUnchecked() == null) {
            sendIdentify();
        } else {
            sendResume();
        }
    }

    private void sendIdentify() {
        GatewayConnectRequest request = GatewayConnectRequest.builder().
                token(api.getToken()).
                v(webSocketProtocolVersion).
                largeThreshold(largeThreshold).
                compress(compress).
                properties(ConnectionProperties.builder().
                        os(operatingSystem).
                        browser(browser).
                        referrer(referrer).
                        referringDomain(referringDomain).
                        build()).
                build();
        send(WSRequest.identify(request));
    }

    private void sendResume() {
        GatewayResumeRequest resumeRequest = GatewayResumeRequest.builder().
                seq(lastSequenceId).
                token(api.getToken()).
                sessionId(api.getSessionId()).
                build();
        send(WSRequest.resume(resumeRequest));
    }

    void onMessage(String message) {
        try(Timer.Context ctx = stats.webSocketMessageParsing.time()) {
            GatewayPayload msg = gson.fromJson(message, GatewayPayload.class);
            if (msg.getErrorMessage() != null) {
                onDiscordError(msg.getErrorMessage());
                return;
            }
            GatewayOP op = msg.getOpCode();
            if (op == GatewayOP.DISPATCH) {
                if (msg.getSequenceNumber() > lastSequenceId) {
                    lastSequenceId = msg.getSequenceNumber();
                } else {
                    WS_LOGGER.warn("Sequence ID went backwards! Had {}, got {}", lastSequenceId, msg.getSequenceNumber());
                }
                handleGatewayDispatch(msg);
            } else if (op == GatewayOP.RECONNECT) {
                handleGatewayReconnect();
            } else if (op == GatewayOP.INVALID_SESSION) {
                handleInvalidSession();
            } else {
                throw new ApiException(
                        String.format("Unsupported received message type \"%s\", server should NOT be sending this!\n" +
                                "Message body:\n%s",
                        op.name(), message));
            }
        } catch (Exception e) {
            stats.webSocketMessageErrors.mark();
            WS_LOGGER.warn("Exception while parsing message", e);
        }
    }

    private void handleGatewayDispatch(GatewayPayload payload) {
        WebSocketMessageType type = payload.getType();
        switch (type) {
            case READY:
                handleReadyMessage((ReadyMessage) payload.getData());
                break;
            //  TODO
        }
    }

    private void handleReadyMessage(ReadyMessage readyMessage) {
        //  Set up heartbeat
        killHeart();
        heartbeat = api.getExecutorService().scheduleAtFixedRate(this::heartbeat, 0,
                readyMessage.getHeartbeatInterval(), TimeUnit.MILLISECONDS);
        WS_LOGGER.debug("Beating heart every {} ms", readyMessage.getHeartbeatInterval());

        //  delegated to api impl
        api.handleReadyMessage(readyMessage);
    }

    private void handleGatewayReconnect() {

    }

    private void handleInvalidSession() {

    }

    private void heartbeat() {
        while(true) {
            try {
                send(WSRequest.heartbeat(lastSequenceId));
                return;
            } catch (RateLimitExceededException ignore) {
                //  keep trying - heartbeats are critical
            }
        }
    }

    private void killHeart() {
        if (heartbeat != null) {
            heartbeat.cancel(true);
        }
    }

    private void onDiscordError(String error) {
        stats.webSocketMessageErrors.mark();
        WS_LOGGER.warn("Discord error: {}", error);
    }

    void onClose(int code, String reason, boolean remote) {
        if (remote) {
            WS_LOGGER.warn("Websocket closed by server. Code {}: \"{}\"", code, reason);
        } else {
            WS_LOGGER.info("Websocket closed by client. Code {}: \"{}\"", code, reason);
        }
        killHeart();
    }

    void onError(Exception ex) {
        stats.webSocketErrors.mark();
        WS_LOGGER.warn("Websocket exception", ex);
        killHeart();
    }

    public void send(WSRequest request) throws NotYetConnectedException, RateLimitExceededException {
        try {
            delegateLock.lock();
            sendLimiter.mark();
            delegate.send(gson.toJson(request));
            stats.webSocketOutboundMessages.mark();
        } catch (RateLimitExceededException e) {
            stats.webSocketRateLimitHits.mark();
            WS_LOGGER.warn("Rate limit exceeded for send(String), request={} retry={}",
                    request.getOpCode(),
                    e.getRetryIn());
            throw e;
        } finally {
            delegateLock.unlock();
        }
    }

    public void setWebSocketFactory(WebSocketClient.WebSocketClientFactory factory) {
        this.factory = factory;
    }
}
