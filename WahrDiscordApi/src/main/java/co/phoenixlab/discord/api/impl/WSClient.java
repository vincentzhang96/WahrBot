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
import co.phoenixlab.discord.api.request.ConnectRequest;
import co.phoenixlab.discord.api.request.ConnectionProperties;
import co.phoenixlab.discord.api.util.WahrDiscordApiUtils;
import com.codahale.metrics.Timer;
import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

class WSClient extends WebSocketClient {

    private static final Logger WS_LOGGER = LoggerFactory.getLogger(WSClient.class);

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

    public WSClient(URI serverURI, WahrDiscordApiImpl api) {
        super(serverURI);
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
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Thread.currentThread().setName("WebSocketClient");
        WS_LOGGER.info("WebSocket connection opened");
        ConnectRequest request = ConnectRequest.builder().
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
        send(gson.toJson(GatewayPayload.identify(request)));
    }

    @Override
    public void onMessage(String message) {
        try(Timer.Context ctx = stats.webSocketMessageParsing.time()) {
            GatewayPayload msg = gson.fromJson(message, GatewayPayload.class);
            if (msg.getErrorMessage() != null) {
                onDiscordError(msg.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            stats.webSocketMessageErrors.mark();
            WS_LOGGER.warn("Exception while parsing message", e);
        }
    }

    private void onDiscordError(String error) {
        stats.webSocketMessageErrors.mark();
        WS_LOGGER.warn("Discord error: {}", error);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (remote) {
            WS_LOGGER.warn("Websocket closed by server. Code {}: \"{}\"", code, reason);
        } else {
            WS_LOGGER.info("Websocket closed by client. Code {}: \"{}\"", code, reason);
        }
    }

    @Override
    public void onError(Exception ex) {
        stats.webSocketErrors.mark();
        WS_LOGGER.warn("Websocket exception", ex);
    }

}
