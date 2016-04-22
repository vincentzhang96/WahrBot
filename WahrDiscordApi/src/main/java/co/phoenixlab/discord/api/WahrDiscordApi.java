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

package co.phoenixlab.discord.api;

import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.entities.user.SelfUser;
import co.phoenixlab.discord.api.entities.TokenResponse;
import co.phoenixlab.discord.api.enums.ApiClientState;
import co.phoenixlab.discord.api.exceptions.NotReadyException;
import co.phoenixlab.discord.api.impl.EndpointsImpl;
import com.codahale.metrics.MetricRegistry;
import com.google.common.eventbus.AsyncEventBus;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

public interface WahrDiscordApi {

    EndpointsImpl getEndpoints()
            throws NotReadyException;

    @Deprecated
    TokenResponse getAndSetToken(String email, String password)
            throws ApiException;

    @Deprecated
    Future<TokenResponse> getAndSetTokenAsync(String email, String password)
            throws ApiException;

    String getToken();

    String getSessionId()
            throws NotReadyException;

    AsyncEventBus getEventBus();

    ScheduledExecutorService getExecutorService();

    SelfUser getSelf()
            throws NotReadyException;

    MetricRegistry getMetrics();

    void open()
            throws ApiException;

    void close();

    boolean isConnected();

    boolean isReady();

    ApiClientState getState();

}
