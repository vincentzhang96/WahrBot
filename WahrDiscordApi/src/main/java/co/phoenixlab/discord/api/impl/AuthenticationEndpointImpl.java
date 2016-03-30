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
import co.phoenixlab.discord.api.endpoints.async.AuthenticationEndpointAsync;
import co.phoenixlab.discord.api.entities.TokenResponse;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.request.EmailPasswordLoginRequest;
import co.phoenixlab.discord.api.request.LogoutRequest;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static co.phoenixlab.discord.api.impl.EndpointsImpl.*;

public class AuthenticationEndpointImpl implements AuthenticationEndpoint, AuthenticationEndpointAsync {

    private static final String LOGIN_ENDPOINT = "/auth/login";
    private static final String LOGOUT_ENDPOINT = "/auth/login";

    @Inject
    private ScheduledExecutorService executorService;
    @Inject
    private EndpointsImpl endpoints;
    @Inject
    private Gson gson;

    @Override
    public TokenResponse logIn(EmailPasswordLoginRequest request) throws ApiException {
        try {
            HttpResponse<String> response =
                    endpoints.defaultPostUnauth(BASE_URL + LOGIN_ENDPOINT, gson.toJson(request));
            return gson.fromJson(response.getBody(), TokenResponse.class);
        } catch (Exception e) {
            throw new ApiException(LOGIN_ENDPOINT, e);
        }
    }

    @Override
    public void logOut(LogoutRequest request) throws ApiException {
        try {
            HttpResponse<String> response =
                    endpoints.defaultPostUnauth(BASE_URL + LOGOUT_ENDPOINT, gson.toJson(request));
        } catch (Exception e) {
            throw new ApiException(LOGOUT_ENDPOINT, e);
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
}
