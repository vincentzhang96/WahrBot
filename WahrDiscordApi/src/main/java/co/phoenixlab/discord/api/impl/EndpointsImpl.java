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

import co.phoenixlab.discord.api.endpoints.*;
import co.phoenixlab.discord.api.endpoints.async.*;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.exceptions.InvalidTokenException;
import co.phoenixlab.discord.api.exceptions.RateLimitExceededException;
import com.codahale.metrics.Timer;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.apache.http.entity.ContentType;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class EndpointsImpl implements Endpoints {

    public static final String BASE_URL = "https://discordapp.com/api";
    public static final int HTTP_TOO_MANY_REQUESTS = 429;
    public static final int HTTP_OK = 200;
    public static final int HTTP_EMPTY = 204;
    public static final int HTTP_NOT_AUTHENTICATED = 401;
    @Inject
    private WahrDiscordApiImpl apiImpl;
    @Inject
    private LoginEndpointsImpl login;
    @Inject
    private Gson gson;
    @Inject
    private WahrDiscordApiImpl.Stats stats;

    private Validator validator;

    public EndpointsImpl() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Override
    public AuthenticationEndpoint auth() {
        return login;
    }

    @Override
    public AuthenticationEndpointAsync authAsync() {
        return login;
    }

    @Override
    public BansEndpoint bans() {
        return null;
    }

    @Override
    public BansEndpointAsync bansAsync() {
        return null;
    }

    @Override
    public ChannelsEndpoint channels() {
        return null;
    }

    @Override
    public ChannelsEndpointAsync channelsAsync() {
        return null;
    }

    @Override
    public GatewayEndpoint gateway() {
        return login;
    }

    @Override
    public GatewayEndpointAsync gatewayAsync() {
        return login;
    }

    @Override
    public GuildsEndpoint guilds() {
        return null;
    }

    @Override
    public GuildsEndpointAsync guildsAsync() {
        return null;
    }

    @Override
    public MembersEndpoint members() {
        return null;
    }

    @Override
    public MembersEndpointAsync membersAsync() {
        return null;
    }

    @Override
    public MessagesEndpoint messages() {
        return null;
    }

    @Override
    public MessagesEndpointAsync messagesAsync() {
        return null;
    }

    @Override
    public PermissionsEndpoint permissions() {
        return null;
    }

    @Override
    public PermissionsEndpointAsync permissionsAsync() {
        return null;
    }

    @Override
    public RolesEndpoint roles() {
        return null;
    }

    @Override
    public RolesEndpointAsync rolesAsync() {
        return null;
    }

    @Override
    public UsersEndpoint users() {
        return null;
    }

    @Override
    public UsersEndpointAsync usersAsync() {
        return null;
    }

    private void addDefaultHeaders(HttpRequest request) {
        request.header(HttpHeaders.USER_AGENT, apiImpl.getUserAgent());
        request.header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
    }

    private void addAuthHeader(HttpRequest request) {
        request.header(HttpHeaders.AUTHORIZATION, apiImpl.getToken());
    }

    HttpResponse<String> defaultPostUnauth(String path, String body) throws UnirestException {
        try (Timer.Context ctx = stats.httpPostTime.time()) {
            HttpRequestWithBody req = Unirest.post(BASE_URL + path);
            addDefaultHeaders(req);
            return logResponse(req.body(body).asString());
        }
    }

    <T> T defaultPostUnauth(String path, Object body, Class<T> type) throws UnirestException {
        try (Timer.Context ctx = stats.httpPostTime.time()) {
            HttpRequestWithBody req = Unirest.post(BASE_URL + path);
            addDefaultHeaders(req);
            HttpResponse<String> response = req.body(gson.toJson(body)).asString();
            logResponse(response);
            int status = response.getStatus();
            if (status == HTTP_TOO_MANY_REQUESTS) {
                long retryIn = Long.parseLong(response.getHeaders().getFirst("Retry-After"));
                throw new RateLimitExceededException(retryIn);
            }
            if (status == HTTP_EMPTY) {
                if (Void.class.equals(type)) {
                    return null;
                } else {
                    throw new UnirestException("Got HTTP 204: Expected a response body, got none");
                }
            }
            if (status != HTTP_OK) {
                throw new UnirestException("HTTP " + status + ": " + response.getStatusText());
            }
            return gson.fromJson(response.getBody(), type);
        }
    }

    HttpResponse<String> defaultPost(String path, String body) throws UnirestException {
        try (Timer.Context ctx = stats.httpPostTime.time()) {
            HttpRequestWithBody req = Unirest.post(BASE_URL + path);
            addDefaultHeaders(req);
            addAuthHeader(req);
            return logResponse(req.body(body).asString());
        }
    }

    <T> T defaultPost(String path, Object body, Class<T> type)
            throws UnirestException, InvalidTokenException {
        try (Timer.Context ctx = stats.httpPostTime.time()) {
            HttpRequestWithBody req = Unirest.post(BASE_URL + path);
            addDefaultHeaders(req);
            addAuthHeader(req);
            HttpResponse<String> response = req.body(gson.toJson(body)).asString();
            logResponse(response);
            int status = response.getStatus();
            if (status == HTTP_NOT_AUTHENTICATED) {
                throw new InvalidTokenException(HttpMethod.POST, path, "Bad token");
            }
            if (status == HTTP_TOO_MANY_REQUESTS) {
                long retryIn = Long.parseLong(response.getHeaders().getFirst("Retry-After"));
                throw new RateLimitExceededException(retryIn);
            }
            if (status == HTTP_EMPTY) {
                if (Void.class.equals(type)) {
                    return null;
                } else {
                    throw new UnirestException("Got HTTP 204: Expected a response body, got none");
                }
            }
            if (status != HTTP_OK) {
                throw new UnirestException("HTTP " + status + ": " + response.getStatusText());
            }
            return gson.fromJson(response.getBody(), type);
        }
    }

    HttpResponse<String> defaultPatch(String path, String body) throws UnirestException {
        try (Timer.Context ctx = stats.httpPostTime.time()) {
            HttpRequestWithBody req = Unirest.patch(BASE_URL + path);
            addDefaultHeaders(req);
            addAuthHeader(req);
            return logResponse(req.body(body).asString());
        }
    }

    <T> T defaultPatch(String path, Object body, Class<T> type)
            throws UnirestException, InvalidTokenException {
        try (Timer.Context ctx = stats.httpPostTime.time()) {
            HttpRequestWithBody req = Unirest.patch(BASE_URL + path);
            addDefaultHeaders(req);
            addAuthHeader(req);
            HttpResponse<String> response = req.body(gson.toJson(body)).asString();
            logResponse(response);
            int status = response.getStatus();
            if (status == HTTP_NOT_AUTHENTICATED) {
                throw new InvalidTokenException(HttpMethod.POST, path, "Bad token");
            }
            if (status == HTTP_TOO_MANY_REQUESTS) {
                long retryIn = Long.parseLong(response.getHeaders().getFirst("Retry-After"));
                throw new RateLimitExceededException(retryIn);
            }
            if (status == HTTP_EMPTY) {
                if (Void.class.equals(type)) {
                    return null;
                } else {
                    throw new UnirestException("Got HTTP 204: Expected a response body, got none");
                }
            }
            if (status != HTTP_OK) {
                throw new UnirestException("HTTP " + status + ": " + response.getStatusText());
            }
            return gson.fromJson(response.getBody(), type);
        }
    }


    HttpResponse<String> defaultGetUnauth(String path) throws UnirestException {
        try (Timer.Context ctx = stats.httpGetTime.time()) {
            HttpRequest req = Unirest.get(BASE_URL + path);
            addDefaultHeaders(req);
            return logResponse(req.asString());
        }
    }

    <T> T defaultGetUnauth(String path, Class<T> type)
            throws UnirestException, InvalidTokenException {
        try (Timer.Context ctx = stats.httpGetTime.time()) {
            HttpRequest req = Unirest.get(BASE_URL + path);
            addDefaultHeaders(req);
            HttpResponse<String> response = req.asString();
            logResponse(response);
            int status = response.getStatus();
            if (status == HTTP_TOO_MANY_REQUESTS) {
                long retryIn = Long.parseLong(response.getHeaders().getFirst("Retry-After"));
                throw new RateLimitExceededException(retryIn);
            }
            if (status == HTTP_EMPTY) {
                if (Void.class.equals(type)) {
                    return null;
                } else {
                    throw new UnirestException("Got HTTP 204: Expected a response body, got none");
                }
            }
            if (status != HTTP_OK) {
                throw new UnirestException("HTTP " + status + ": " + response.getStatusText());
            }
            return gson.fromJson(response.getBody(), type);
        }
    }

    HttpResponse<String> defaultGet(String path) throws UnirestException {
        try (Timer.Context ctx = stats.httpGetTime.time()) {
            HttpRequest req = Unirest.get(BASE_URL + path);
            addDefaultHeaders(req);
            addAuthHeader(req);
            return logResponse(req.asString());
        }
    }

    <T> T defaultGet(String path, Class<T> type)
            throws UnirestException, InvalidTokenException {
        try (Timer.Context ctx = stats.httpGetTime.time()) {
            HttpRequest req = Unirest.get(BASE_URL + path);
            addDefaultHeaders(req);
            addAuthHeader(req);
            HttpResponse<String> response = req.asString();
            logResponse(response);
            int status = response.getStatus();
            if (status == HTTP_NOT_AUTHENTICATED) {
                throw new InvalidTokenException(HttpMethod.GET, path, "Bad token");
            }
            if (status == HTTP_TOO_MANY_REQUESTS) {
                long retryIn = Long.parseLong(response.getHeaders().getFirst("Retry-After"));
                throw new RateLimitExceededException(retryIn);
            }
            if (status == HTTP_EMPTY) {
                if (Void.class.equals(type)) {
                    return null;
                } else {
                    throw new UnirestException("Got HTTP 204: Expected a response body, got none");
                }
            }
            if (status != HTTP_OK) {
                throw new UnirestException("HTTP " + status + ": " + response.getStatusText());
            }
            return gson.fromJson(response.getBody(), type);
        }
    }

    private <T> HttpResponse<T> logResponse(HttpResponse<T> response) {
        int status = response.getStatus();
        switch (status / 100) {
            case 2:
                stats.http2xxResp.mark();
                break;
            case 4:
                stats.http4xxErrors.mark();
                break;
            case 5:
                stats.http5xxErrors.mark();
                break;
            default:
                stats.httpOtherResp.mark();
                break;
        }
        return response;
    }

    private boolean inStatusRange(int range, int status) {
        return status >= range && status < range + 100;
    }

    void validate(Object o) throws ApiException {
        Set<ConstraintViolation<Object>> validate = validator.validate(o);
        if (!validate.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (ConstraintViolation<Object> violation : validate) {
                builder.append(String.format("Field invalid: %s",
                        violation.getMessage()));
            }
        }
    }

    String snowflakeToString(long snowflake) {
        return Long.toUnsignedString(snowflake);
    }
}
