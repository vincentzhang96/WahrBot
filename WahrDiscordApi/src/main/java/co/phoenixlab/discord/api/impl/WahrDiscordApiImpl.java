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
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.entities.SelfUser;
import co.phoenixlab.discord.api.entities.TokenResponse;
import co.phoenixlab.discord.api.enums.ApiClientState;
import co.phoenixlab.discord.api.enums.ApiClientTrigger;
import co.phoenixlab.discord.api.exceptions.NotReadyException;
import co.phoenixlab.discord.api.request.EmailPasswordLoginRequest;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.eventbus.SubscriberExceptionContext;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static co.phoenixlab.discord.api.enums.ApiClientState.*;
import static co.phoenixlab.discord.api.enums.ApiClientTrigger.*;
import static com.codahale.metrics.MetricRegistry.name;

public class WahrDiscordApiImpl implements WahrDiscordApi {

    private static final Logger API_LOGGER = LoggerFactory.getLogger(WahrDiscordApiImpl.class);

    @Getter
    private final MetricRegistry metrics;
    private final Stats stats;
    private volatile String sessionId;
    @Getter
    private volatile String token;
    private SelfUser self;
    private WSClient websocketClient;
    private final EndpointsImpl endpoints;
    @Getter
    private final AsyncEventBus eventBus;
    @Getter
    private final ScheduledExecutorService executorService;

    private final StateMachine<ApiClientState, ApiClientTrigger> stateMachine;


    public WahrDiscordApiImpl() {
        this(null);
    }

    public WahrDiscordApiImpl(String token) {
        this.metrics = new MetricRegistry();
        this.stats = new Stats();
        this.token = token;
        this.executorService = Executors.newScheduledThreadPool(2);
        this.stateMachine = buildStateMachine();
        this.eventBus = new AsyncEventBus(executorService, this::handleEventBusException);
        this.endpoints = new EndpointsImpl(this);

        this.eventBus.register(this);
    }

    private StateMachine<ApiClientState, ApiClientTrigger> buildStateMachine() {
        StateMachineConfig<ApiClientState, ApiClientTrigger> config = new StateMachineConfig<>();
        config.configure(DISCONNECTED).
                permit(CONNECT, CONNECTING).
                ignore(DISCONNECT);
        config.configure(CONNECTING).
                permit(CONNECT_FAIL, DISCONNECTED).
                permit(CONNECT_OK, CONNECTED);
        config.configure(CONNECTED).
                permit(LOAD, LOADING).
                permit(DISCONNECT, DISCONNECTED);
        config.configure(LOADING).
                substateOf(CONNECTED).
                permit(LOAD_OK, READY).
                permit(DISCONNECT, DISCONNECTED);
        config.configure(READY).
                substateOf(CONNECTED).
                permit(DISCONNECT, DISCONNECTED);
        return new StateMachine<>(DISCONNECTED, config);
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
        //  TODO
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

    @Subscribe
    @AllowConcurrentEvents
    public void countEvent(Object object) {
        stats.eventBusEvents.mark();
    }

    class Stats {
        final Meter eventBusExceptions;
        final Meter eventBusEvents;

        Stats() {
            eventBusExceptions = metrics.meter(name(WahrDiscordApiImpl.class, "events", "errors", "uncaught"));
            eventBusEvents = metrics.meter(name(WahrDiscordApiImpl.class, "events"));

        }

    }
}
