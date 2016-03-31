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

import co.phoenixlab.discord.api.endpoints.AuthenticationEndpoint;
import co.phoenixlab.discord.api.endpoints.GatewayEndpoint;
import co.phoenixlab.discord.api.endpoints.async.AuthenticationEndpointAsync;
import co.phoenixlab.discord.api.endpoints.async.GatewayEndpointAsync;
import co.phoenixlab.discord.api.entities.TokenResponse;
import co.phoenixlab.discord.api.entities.WebsocketEndpointResponse;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.request.EmailPasswordLoginRequest;
import co.phoenixlab.discord.api.request.LogoutRequest;
import com.google.inject.Inject;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static com.mashape.unirest.http.HttpMethod.GET;
import static com.mashape.unirest.http.HttpMethod.POST;

public class LoginEndpointsImpl
        implements AuthenticationEndpoint, AuthenticationEndpointAsync,
        GatewayEndpoint, GatewayEndpointAsync {

    private static final String LOGIN_ENDPOINT = "/auth/login";
    private static final String LOGOUT_ENDPOINT = "/auth/login";
    private static final String GATEWAY_ENDPOINT = "/gateway";

    @Inject
    private ScheduledExecutorService executorService;
    @Inject
    private EndpointsImpl endpoints;

    @Override
    public TokenResponse logIn(EmailPasswordLoginRequest request) throws ApiException {
        try {
            return endpoints.defaultPostUnauth(LOGIN_ENDPOINT, request, TokenResponse.class);
        } catch (ApiException apie) {
            //  rethrow
            throw apie;
        } catch (Exception e) {
            throw new ApiException(POST, LOGIN_ENDPOINT, e);
        }
    }

    @Override
    public void logOut(LogoutRequest request) throws ApiException {
        try {
            endpoints.defaultPostUnauth(LOGOUT_ENDPOINT, request, Void.class);
        } catch (ApiException apie) {
            //  rethrow
            throw apie;
        } catch (Exception e) {
            throw new ApiException(POST, LOGOUT_ENDPOINT, e);
        }
    }

    @Override
    public Future<TokenResponse> logInAsync(EmailPasswordLoginRequest request) throws ApiException {
        return executorService.submit(() -> logIn(request));
    }

    @Override
    public Future<Void> logOutAsync(LogoutRequest request) throws ApiException {
        return executorService.submit(() -> logOut(request), null);
    }

    @Override
    public WebsocketEndpointResponse getGateway() throws ApiException {
        try {
            WebsocketEndpointResponse ret = endpoints.
                    defaultGet(GATEWAY_ENDPOINT, WebsocketEndpointResponse.class);
            if (ret == null || ret.getUrl() == null) {
                throw new ApiException(GET, GATEWAY_ENDPOINT, "Received invalid response");
            }
            return ret;
        } catch (ApiException apie) {
            //  rethrow
            throw apie;
        } catch (Exception e) {
            throw new ApiException(GET, GATEWAY_ENDPOINT, e);
        }
    }

    @Override
    public Future<WebsocketEndpointResponse> getGatewayAsync() throws ApiException {
        return executorService.submit(this::getGateway);
    }
}
