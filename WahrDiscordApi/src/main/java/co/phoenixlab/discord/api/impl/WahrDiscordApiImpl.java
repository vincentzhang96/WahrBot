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

import co.phoenixlab.discord.api.WahrDiscordApi;
import co.phoenixlab.discord.api.entities.ReadyMessage;
import co.phoenixlab.discord.api.entities.user.SelfUser;
import co.phoenixlab.discord.api.entities.TokenResponse;
import co.phoenixlab.discord.api.entities.WebsocketEndpointResponse;
import co.phoenixlab.discord.api.enums.ApiClientState;
import co.phoenixlab.discord.api.enums.ApiClientTrigger;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.exceptions.InvalidTokenException;
import co.phoenixlab.discord.api.exceptions.NotReadyException;
import co.phoenixlab.discord.api.request.EmailPasswordLoginRequest;
import co.phoenixlab.discord.api.util.WahrDiscordApiUtils;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.github.oxo42.stateless4j.triggers.TriggerWithParameters1;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.gson.Gson;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.Getter;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static co.phoenixlab.discord.api.enums.ApiClientState.*;
import static co.phoenixlab.discord.api.enums.ApiClientTrigger.*;
import static com.codahale.metrics.MetricRegistry.name;

public class WahrDiscordApiImpl implements WahrDiscordApi {

    private static final Logger API_LOGGER = LoggerFactory.getLogger(WahrDiscordApiImpl.class);

    @Getter
    private final String instanceId;
    @Getter
    private final MetricRegistry metrics;
    private final Injector injector;
    private final Stats stats;
    private volatile String sessionId;
    @Getter
    private final String userAgent;
    @Getter
    private volatile String token;
    private SelfUser self;
    private WSClient webSocketClient;
    private final EndpointsImpl endpoints;
    @Getter
    private final AsyncEventBus eventBus;
    @Getter
    private final ScheduledExecutorService executorService;

    private final StateMachine<ApiClientState, ApiClientTrigger> stateMachine;

    private Timer.Context readyContext;

    public WahrDiscordApiImpl(String userAgent, String instanceId) {
        this(instanceId, userAgent, null);
    }

    public WahrDiscordApiImpl(String instanceId, String userAgent, String token) {
        this.instanceId = instanceId;
        this.userAgent = userAgent;
        this.metrics = new MetricRegistry();
        this.token = token;
        this.executorService = Executors.newScheduledThreadPool(2);
        this.stateMachine = buildStateMachine();
        this.eventBus = new AsyncEventBus(executorService, this::handleEventBusException);
        this.injector = Guice.createInjector(this::configureInjector);
        this.stats = injector.getInstance(Stats.class);
        this.endpoints = injector.getInstance(EndpointsImpl.class);
        this.eventBus.register(this);
    }

    private StateMachine<ApiClientState, ApiClientTrigger> buildStateMachine() {
        StateMachineConfig<ApiClientState, ApiClientTrigger> config = new StateMachineConfig<>();
        config.configure(DISCONNECTED).
                onEntry(this::onDisconnected).
                permit(CONNECT, CONNECTING).
                ignore(DISCONNECT);
        config.configure(CONNECTING).
                onEntry(this::onConnecting).
                permit(CONNECT_FAIL, DISCONNECTED).
                permit(CONNECT_OK, CONNECTED);
        config.configure(CONNECTED).
                onEntry(this::onConnected).
                permit(LOAD, LOADING).
                permit(DISCONNECT, DISCONNECTED);
        config.configure(LOADING).
                substateOf(CONNECTED).
                onEntry(this::onLoading).
                permit(LOAD_OK, READY).
                permit(DISCONNECT, DISCONNECTED).
                ignore(LOAD);
        config.configure(READY).
                substateOf(CONNECTED).
                onEntry(this::onReady).
                permit(DISCONNECT, DISCONNECTED).
                ignore(LOAD);
        StateMachine<ApiClientState, ApiClientTrigger> ret = new StateMachine<>(DISCONNECTED, config);
        ret.onUnhandledTrigger((state, trigger) -> {
            throw new ApiException("Failed to handle trigger",
                    new IllegalStateException("Invalid trigger " + trigger + " for current state " + state));
        });
        return ret;
    }

    private void configureInjector(Binder binder) {
        binder.bind(ScheduledExecutorService.class).
                toInstance(executorService);
        binder.bind(WahrDiscordApi.class).
                toInstance(this);
        binder.bind(WahrDiscordApiImpl.class).
                toInstance(this);
        binder.bind(Gson.class).
                toProvider(WahrDiscordApiUtils::createGson);
        binder.bind(Stats.class).
                toProvider(this::getStats);
        binder.bind(MetricRegistry.class).
                toProvider(this::getMetrics);
    }

    private void onDisconnected() {
        API_LOGGER.info("Disconnected");
        if (readyContext != null) {
            readyContext.stop();
            readyContext = null;
        }
    }

    private void onConnecting() {
        API_LOGGER.info("Attempting to connect using token \"{}\"", token);
        try (Timer.Context ctx = stats.connectAttempts.time()) {
            try {
                WebsocketEndpointResponse response = endpoints.gateway().getGateway();
                initWSClient(response.getUrl());
            } catch (InvalidTokenException ite) {
                API_LOGGER.warn("Unable to get websocket gateway.", ite);
                stats.connectFails.mark();
                stateMachine.fire(CONNECT_FAIL);
                return;
            } catch (ApiException e) {
                API_LOGGER.warn("Unable to get websocket gateway. Is Discord's API down?", e);
                stats.connectFails.mark();
                stateMachine.fire(CONNECT_FAIL);
                return;
            }
            try {
                if (!webSocketClient.connectBlocking()) {
                    API_LOGGER.warn("Failed to connect to websocket gateway.");
                    stats.connectFails.mark();
                    stateMachine.fire(CONNECT_FAIL);
                    return;
                }
            } catch (InterruptedException e) {
                API_LOGGER.warn("Connection was interrupted, canceling");
                stateMachine.fire(CONNECT_FAIL);
                return;
            }
        }
        stateMachine.fire(CONNECT_OK);
    }

    private void initWSClient(URI uri) {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, null, null);
            API_LOGGER.debug("Using websocket gateway \"{}\"", uri);
            webSocketClient = new WSClient(uri, this);
            webSocketClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(context));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Platform does not support TLS");
        } catch (KeyManagementException kme) {
            throw new RuntimeException("Could not set up key management", kme);
        }
    }

    private void onConnected() {
        API_LOGGER.info("Connected to websocket gateway");
        readyContext = stats.timeToReady.time();
        stateMachine.fire(LOAD);
    }

    private void onLoading() {
        API_LOGGER.info("Waiting for READY");
    }

    private void onReady() {
        API_LOGGER.info("Websocket connection is ready");
        if (readyContext != null) {
            readyContext.stop();
            readyContext = null;
        }
    }

    void handleReadyMessage(ReadyMessage message) {
        sessionId = message.getSessionId();
        API_LOGGER.debug("Using sessionId {}", sessionId);
        //  TODO rest

        stateMachine.fire(LOAD_OK);
    }

    private void handleEventBusException(Throwable throwable, SubscriberExceptionContext context) {
        stats.eventBusExceptions.mark();
        API_LOGGER.warn("Exception while handling event when calling {}.\nEventData: {}",
                context.getEvent(), context.getSubscriberMethod().toGenericString());
        API_LOGGER.warn("Exception was:", throwable);
    }

    @Override
    public EndpointsImpl getEndpoints() throws NotReadyException {
        if (!isReady()) {
            throw new NotReadyException();
        }
        return endpoints;
    }

    @Deprecated
    @Override
    public TokenResponse getAndSetToken(String email, String password)
            throws ApiException {
        API_LOGGER.warn("======== WARNING!!!! ========\n" +
                "This method (getToken(String email, String password)) is DEPRECATED and should not be used!\n" +
                "Bot should be using a verified BOT account and use a BOT token to WahrDiscordApiImpl(String token).\n" +
                "If login fails, it's probably because the account is a BOT account");
        API_LOGGER.debug("Logging in as {}", email);
        TokenResponse response = endpoints.auth().logIn(EmailPasswordLoginRequest.builder().
                email(email).
                password(password).
                build());
        token = response.getToken();
        return response;
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public Future<TokenResponse> getAndSetTokenAsync(String email, String password) throws ApiException {
        return executorService.submit(() -> getAndSetToken(email, password));
    }

    @Override
    public String getSessionId() throws NotReadyException {
        if (!isReady()) {
            throw new NotReadyException();
        }
        return sessionId;
    }

    String getSessionIdUnchecked() {
        return sessionId;
    }

    @Override
    public SelfUser getSelf() throws NotReadyException {
        if (!isReady()) {
            throw new NotReadyException();
        }
        return self;
    }

    @Override
    public void open() throws ApiException {
        if (token == null) {
            throw new ApiException("No token set, please provide a token");
        }
        stateMachine.fire(CONNECT);
    }

    @Override
    public void close() {
        stateMachine.fire(DISCONNECT);
    }

    @Override
    public boolean isConnected() {
        return stateMachine.isInState(CONNECTED);
    }

    @Override
    public boolean isReady() {
        return stateMachine.isInState(READY);
    }

    @Override
    public ApiClientState getState() {
        return stateMachine.getState();
    }

    WSClient getWebSocketClient() {
        return webSocketClient;
    }

    @Subscribe
    @AllowConcurrentEvents
    public void countEvent(Object object) {
        stats.eventBusEvents.mark();
    }

    Stats getStats() {
        return stats;
    }

    static class Stats {
        final Meter eventBusExceptions;
        final Meter eventBusEvents;

        final Timer connectAttempts;
        final Meter connectFails;
        final Timer timeToReady;

        final Meter webSocketErrors;
        final Timer webSocketMessageParsing;
        final Meter webSocketMessageErrors;
        final Timer webSocketMessageDispatching;
        final Meter webSocketRateLimitHits;
        final Meter webSocketOutboundMessages;
        final Timer httpPostTime;
        final Timer httpPatchTime;
        final Timer httpGetTime;
        final Timer httpDeleteTime;
        final Meter http2xxResp;
        final Meter http4xxErrors;
        final Meter http5xxErrors;
        final Meter httpOtherResp;

        @Inject
        Stats(MetricRegistry metrics, WahrDiscordApi apiClient) {
            String instanceId = apiClient.getInstanceId();
            eventBusExceptions = metrics.meter(name(WahrDiscordApiImpl.class, instanceId,
                    "bus", "events", "errors", "uncaught"));
            eventBusEvents = metrics.meter(name(WahrDiscordApiImpl.class, instanceId, "bus", "events"));
            connectAttempts = metrics.timer(name(WahrDiscordApi.class, instanceId, "connection", "attempts"));
            connectFails = metrics.meter(name(WahrDiscordApi.class, instanceId, "connection", "failures"));
            timeToReady = metrics.timer(name(WahrDiscordApi.class, instanceId, "connection", "timetoready"));
            webSocketErrors = metrics.meter(name(WSClient.class, instanceId, "errors", "general"));
            webSocketMessageErrors = metrics.meter(name(WSClient.class, instanceId, "errors", "messages"));
            webSocketMessageParsing = metrics.timer(name(WSClient.class, instanceId, "messages", "parsing"));
            webSocketMessageDispatching = metrics.timer(name(WSClient.class, instanceId, "messages", "dispatching"));
            webSocketRateLimitHits = metrics.meter(name(WSClient.class, instanceId, "ratelimit", "overruns"));
            webSocketOutboundMessages = metrics.meter(name(WSClient.class, instanceId, "messages", "outbound"));
            httpPostTime = metrics.timer(name(WahrDiscordApiImpl.class, instanceId, "http", "post"));
            httpPatchTime = metrics.timer(name(WahrDiscordApiImpl.class, instanceId, "http", "patch"));
            httpGetTime = metrics.timer(name(WahrDiscordApiImpl.class, instanceId, "http", "get"));
            httpDeleteTime = metrics.timer(name(WahrDiscordApiImpl.class, instanceId, "http", "delete"));
            http2xxResp = metrics.meter(name(WahrDiscordApiImpl.class, instanceId, "http", "response", "2XX"));
            http4xxErrors = metrics.meter(name(WahrDiscordApiImpl.class, instanceId, "http", "response", "4XX"));
            http5xxErrors = metrics.meter(name(WahrDiscordApiImpl.class, instanceId, "http", "response", "5XX"));
            httpOtherResp = metrics.meter(name(WahrDiscordApiImpl.class, instanceId, "http", "response", "other"));
        }

    }
}
