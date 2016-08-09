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
import co.phoenixlab.discord.api.exceptions.InvalidApiRequestException;
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
import java.util.StringJoiner;

public class EndpointsImpl implements Endpoints {

    /**
     * The base URL for the Discord API
     */
    public static final String BASE_URL = "https://discordapp.com/api";

    /**
     * HTTP status code for throttling/rate limiting
     */
    public static final int HTTP_TOO_MANY_REQUESTS = 429;

    /**
     * HTTP status code for OK
     */
    public static final int HTTP_OK = 200;

    /**
     * HTTP status code for OK - No Content
     */
    public static final int HTTP_EMPTY = 204;

    /**
     * HTTP status code for unauthenticated
     */
    public static final int HTTP_NOT_AUTHENTICATED = 401;

    /**
     * The HTTP header "Retry-After"
     */
    public static final String HTTP_RETRY_AFTER_HEADER = "Retry-After";

    /**
     * API client implementation
     */
    @Inject
    private WahrDiscordApiImpl apiImpl;

    /**
     * Login endpoints implementation
     */
    @Inject
    private LoginEndpointsImpl login;

    /**
     * GSON instance to use by endpoint implementations
     */
    @Inject
    private Gson gson;

    /**
     * Discord API implementation metrics
     */
    @Inject
    private WahrDiscordApiImpl.Stats stats;

    /**
     * Validator instances for validating request parameters
     */
    private Validator validator;

    /**
     * Constructs a new endpoints implementation
     */
    public EndpointsImpl() {
        //  Create the validator we're using to validate request parameters - default settings are fine
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

    /**
     * Adds the default headers to the in-construction HttpRequest, specifically the user-agent and content-type (JSON)
     * @param request The in-construction HttpRequeset
     */
    private void addDefaultHeaders(HttpRequest request) {
        request.header(HttpHeaders.USER_AGENT, apiImpl.getUserAgent());
        request.header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
    }

    /**
     * Adds the authorization header to the in-construction HttpRequest
     * @param request The in-construction HttpRequest
     */
    private void addAuthHeader(HttpRequest request) {
        request.header(HttpHeaders.AUTHORIZATION, apiImpl.getToken());
    }

    /**
     * Performs a basic POST request without authentication, taking a string body and returning a string result.
     * @param path The URL path relative to the API {@link #BASE_URL}
     * @param body The body to send in the request, or null for no body
     * @return An HttpResponse that resolves to a string value
     * @throws UnirestException If there was an HTTP exception
     */
    HttpResponse<String> defaultPostUnauth(String path, String body) throws UnirestException {
        try (Timer.Context ctx = stats.httpPostTime.time()) {
            HttpRequestWithBody req = Unirest.post(BASE_URL + path);
            addDefaultHeaders(req);
            //  Skip adding the auth headers
            //  Log the response for metrics (passthrough)
            //  Only add body if present
            if (body != null) {
                return logResponse(req.body(body).asString());
            } else {
                return logResponse(req.asString());
            }
        }
    }

    /**
     * Performs a POST request without authentication, taking an Object body (serialized to JSON) and returning a typed
     * result (deserialized from JSON). For endpoints that respond with 204 No Content, the return class should be
     * {@code Void.class}. For making POST requests without a body, pass in {@code null} as the body.
     * @param path The URL path relative to the API {@link #BASE_URL}
     * @param body The body to send in the request, or null for no body
     * @param type The class to deserialize the result to, or {@code Void.class} for endpoints that are expected to
     *             return HTTP 204 No Content
     * @param <T> Type parameter for the return value type
     * @return The deserialized response object, or {@code null} if the return type is Void
     * @throws UnirestException If there was an HTTP exception
     */
    <T> T defaultPostUnauth(String path, Object body, Class<T> type) throws UnirestException {
        try (Timer.Context ctx = stats.httpPostTime.time()) {
            //  Construct the request
            HttpRequestWithBody req = Unirest.post(BASE_URL + path);
            addDefaultHeaders(req);
            //  Skip adding the auth header since it's an unauthenticated call
            //  Only serialize if we have a body to send
            HttpResponse<String> response;
            if (body != null) {
                response = req.body(gson.toJson(body)).
                        asString();
            } else {
                response = req.asString();
            }
            //  Log for metrics
            logResponse(response);
            //  Check for error conditions
            int status = response.getStatus();
            if (status == HTTP_TOO_MANY_REQUESTS) {
                //  We hit the rate limit, throw an exception to let the caller know
                long retryIn = Long.parseLong(response.getHeaders().getFirst(HTTP_RETRY_AFTER_HEADER));
                throw new RateLimitExceededException(retryIn);
            }
            if (status == HTTP_EMPTY) {
                //  We expect the response to be empty if we expected Void back
                //  Otherwise, this is an unexpected situation
                if (Void.class.equals(type)) {
                    return null;
                } else {
                    throw new UnirestException("Got HTTP 204: Expected a response body, got none");
                }
            }
            if (status != HTTP_OK) {
                //  Other errors
                throw new UnirestException("HTTP " + status + ": " + response.getStatusText());
            }
            return gson.fromJson(response.getBody(), type);
        }
    }

    /**
     * Performs a basic POST request, taking a string body and returning a string result.
     * @param path The URL path relative to the API {@link #BASE_URL}
     * @param body The body to send in the request, or null for no body
     * @return An HttpResponse that resolves to a string value
     * @throws UnirestException If there was an HTTP exception
     */
    HttpResponse<String> defaultPost(String path, String body) throws UnirestException {
        try (Timer.Context ctx = stats.httpPostTime.time()) {
            HttpRequestWithBody req = Unirest.post(BASE_URL + path);
            addDefaultHeaders(req);
            addAuthHeader(req);
            //  Log the response for metrics (passthrough)
            //  Only add body if present
            if (body != null) {
                return logResponse(req.body(body).asString());
            } else {
                return logResponse(req.asString());
            }
        }
    }

    /**
     * Performs a POST request, taking an Object body (serialized to JSON) and returning a typed
     * result (deserialized from JSON). For endpoints that respond with 204 No Content, the return class should be
     * {@code Void.class}. For making POST requests without a body, pass in {@code null} as the body.
     * @param path The URL path relative to the API {@link #BASE_URL}
     * @param body The body to send in the request, or null for no body
     * @param type The class to deserialize the result to, or {@code Void.class} for endpoints that are expected to
     *             return HTTP 204 No Content
     * @param <T> Type parameter for the return value type
     * @return The deserialized response object, or {@code null} if the return type is Void
     * @throws UnirestException If there was an HTTP exception
     */
    <T> T defaultPost(String path, Object body, Class<T> type)
            throws UnirestException, InvalidTokenException {
        try (Timer.Context ctx = stats.httpPostTime.time()) {
            HttpRequestWithBody req = Unirest.post(BASE_URL + path);
            addDefaultHeaders(req);
            addAuthHeader(req);
            //  Only serialize if we have a body to send
            HttpResponse<String> response;
            if (body != null) {
                response = req.body(gson.toJson(body)).
                        asString();
            } else {
                response = req.asString();
            }
            //  Log for metrics
            logResponse(response);
            //  Check for error conditions
            int status = response.getStatus();
            if (status == HTTP_NOT_AUTHENTICATED) {
                //  Authentication failed
                throw new InvalidTokenException(HttpMethod.POST, path, "Bad token");
            }
            if (status == HTTP_TOO_MANY_REQUESTS) {
                //  We hit the rate limit, throw an exception to let the caller know
                long retryIn = Long.parseLong(response.getHeaders().getFirst(HTTP_RETRY_AFTER_HEADER));
                throw new RateLimitExceededException(retryIn);
            }
            if (status == HTTP_EMPTY) {
                //  We expect the response to be empty if we expected Void back
                //  Otherwise, this is an unexpected situation
                if (Void.class.equals(type)) {
                    return null;
                } else {
                    throw new UnirestException("Got HTTP 204: Expected a response body, got none");
                }
            }
            if (status != HTTP_OK) {
                //  Other errors
                throw new UnirestException("HTTP " + status + ": " + response.getStatusText());
            }
            return gson.fromJson(response.getBody(), type);
        }
    }

    /**
     * Performs a basic PATCH request, taking a string body and returning a string result.
     * @param path The URL path relative to the API {@link #BASE_URL}
     * @param body The body to send in the request, or null for no body
     * @return An HttpResponse that resolves to a string value
     * @throws UnirestException If there was an HTTP exception
     */
    HttpResponse<String> defaultPatch(String path, String body) throws UnirestException {
        try (Timer.Context ctx = stats.httpPostTime.time()) {
            HttpRequestWithBody req = Unirest.patch(BASE_URL + path);
            addDefaultHeaders(req);
            addAuthHeader(req);
            //  Log the response for metrics (passthrough)
            //  Only add body if present
            if (body != null) {
                return logResponse(req.body(body).asString());
            } else {
                return logResponse(req.asString());
            }
        }
    }

    /**
     * Performs a PATCH request, taking an Object body (serialized to JSON) and returning a typed
     * result (deserialized from JSON). For endpoints that respond with 204 No Content, the return class should be
     * {@code Void.class}. For making PATCH requests without a body, pass in {@code null} as the body.
     * @param path The URL path relative to the API {@link #BASE_URL}
     * @param body The body to send in the request, or null for no body
     * @param type The class to deserialize the result to, or {@code Void.class} for endpoints that are expected to
     *             return HTTP 204 No Content
     * @param <T> Type parameter for the return value type
     * @return The deserialized response object, or {@code null} if the return type is Void
     * @throws UnirestException If there was an HTTP exception
     */
    <T> T defaultPatch(String path, Object body, Class<T> type)
            throws UnirestException, InvalidTokenException {
        try (Timer.Context ctx = stats.httpPostTime.time()) {
            HttpRequestWithBody req = Unirest.patch(BASE_URL + path);
            addDefaultHeaders(req);
            addAuthHeader(req);
            //  Only serialize if we have a body to send
            HttpResponse<String> response;
            if (body != null) {
                response = req.body(gson.toJson(body)).
                        asString();
            } else {
                response = req.asString();
            }
            //  Log for metrics
            logResponse(response);
            //  Check for error conditions
            int status = response.getStatus();
            if (status == HTTP_NOT_AUTHENTICATED) {
                //  Authentication failed
                throw new InvalidTokenException(HttpMethod.POST, path, "Bad token");
            }
            if (status == HTTP_TOO_MANY_REQUESTS) {
                //  We hit the rate limit, throw an exception to let the caller know
                long retryIn = Long.parseLong(response.getHeaders().getFirst(HTTP_RETRY_AFTER_HEADER));
                throw new RateLimitExceededException(retryIn);
            }
            if (status == HTTP_EMPTY) {
                //  We expect the response to be empty if we expected Void back
                //  Otherwise, this is an unexpected situation
                if (Void.class.equals(type)) {
                    return null;
                } else {
                    throw new UnirestException("Got HTTP 204: Expected a response body, got none");
                }
            }
            if (status != HTTP_OK) {
                //  Other errors
                throw new UnirestException("HTTP " + status + ": " + response.getStatusText());
            }
            return gson.fromJson(response.getBody(), type);
        }
    }

    /**
     * Performs a basic GET request without authentication, returning a string result.
     * @param path The URL path relative to the API {@link #BASE_URL}
     * @return An HttpResponse that resolves to a string value
     * @throws UnirestException If there was an HTTP exception
     */
    HttpResponse<String> defaultGetUnauth(String path) throws UnirestException {
        try (Timer.Context ctx = stats.httpGetTime.time()) {
            HttpRequest req = Unirest.get(BASE_URL + path);
            addDefaultHeaders(req);
            return logResponse(req.asString());
        }
    }

    /**
     * Performs a GET request without authentication, returning a typed result (deserialized from JSON). For endpoints
     * that respond with 204 No Content, the return class should be {@code Void.class}.
     * @param path The URL path relative to the API {@link #BASE_URL}
     * @param type The class to deserialize the result to, or {@code Void.class} for endpoints that are expected to
     *             return HTTP 204 No Content
     * @param <T> Type parameter for the return value type
     * @return The deserialized response object, or {@code null} if the return type is Void
     * @throws UnirestException If there was an HTTP exception
     */
    <T> T defaultGetUnauth(String path, Class<T> type)
            throws UnirestException, InvalidTokenException {
        try (Timer.Context ctx = stats.httpGetTime.time()) {
            HttpRequest req = Unirest.get(BASE_URL + path);
            addDefaultHeaders(req);
            HttpResponse<String> response = req.asString();
            logResponse(response);
            int status = response.getStatus();
            if (status == HTTP_TOO_MANY_REQUESTS) {
                long retryIn = Long.parseLong(response.getHeaders().getFirst(HTTP_RETRY_AFTER_HEADER));
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

    /**
     * Performs a basic GET request, returning a string result.
     * @param path The URL path relative to the API {@link #BASE_URL}
     * @return An HttpResponse that resolves to a string value
     * @throws UnirestException If there was an HTTP exception
     */
    HttpResponse<String> defaultGet(String path) throws UnirestException {
        try (Timer.Context ctx = stats.httpGetTime.time()) {
            HttpRequest req = Unirest.get(BASE_URL + path);
            addDefaultHeaders(req);
            addAuthHeader(req);
            return logResponse(req.asString());
        }
    }

    /**
     * Performs a GET request, returning a typed result (deserialized from JSON). For endpoints
     * that respond with 204 No Content, the return class should be {@code Void.class}.
     * @param path The URL path relative to the API {@link #BASE_URL}
     * @param type The class to deserialize the result to, or {@code Void.class} for endpoints that are expected to
     *             return HTTP 204 No Content
     * @param <T> Type parameter for the return value type
     * @return The deserialized response object, or {@code null} if the return type is Void
     * @throws UnirestException If there was an HTTP exception
     */
    <T> T defaultGet(String path, Class<T> type)
            throws UnirestException, InvalidTokenException {
        try (Timer.Context ctx = stats.httpGetTime.time()) {
            HttpRequest req = Unirest.get(BASE_URL + path);
            addDefaultHeaders(req);
            addAuthHeader(req);
            HttpResponse<String> response = req.asString();
            //  Log for metrics
            logResponse(response);
            //  Check for error conditions
            int status = response.getStatus();
            if (status == HTTP_NOT_AUTHENTICATED) {
                //  Authentication failed
                throw new InvalidTokenException(HttpMethod.POST, path, "Bad token");
            }
            if (status == HTTP_TOO_MANY_REQUESTS) {
                //  We hit the rate limit, throw an exception to let the caller know
                long retryIn = Long.parseLong(response.getHeaders().getFirst(HTTP_RETRY_AFTER_HEADER));
                throw new RateLimitExceededException(retryIn);
            }
            if (status == HTTP_EMPTY) {
                //  We expect the response to be empty if we expected Void back
                //  Otherwise, this is an unexpected situation
                if (Void.class.equals(type)) {
                    return null;
                } else {
                    throw new UnirestException("Got HTTP 204: Expected a response body, got none");
                }
            }
            if (status != HTTP_OK) {
                //  Other errors
                throw new UnirestException("HTTP " + status + ": " + response.getStatusText());
            }
            return gson.fromJson(response.getBody(), type);
        }
    }

    /**
     * Performs a basic DELETE request, returning a string result.
     * @param path The URL path relative to the API {@link #BASE_URL}
     * @return An HttpResponse that resolves to a string value
     * @throws UnirestException If there was an HTTP exception
     */
    HttpResponse<String> defaultDelete(String path) throws UnirestException {
        HttpRequest req = Unirest.delete(path);
        addDefaultHeaders(req);
        addAuthHeader(req);
        return logResponse(req.asString());
    }

    /**
     * Performs a DELETE request, returning a typed result (deserialized from JSON). For endpoints
     * that respond with 204 No Content, the return class should be {@code Void.class}.
     * @param path The URL path relative to the API {@link #BASE_URL}
     * @param type The class to deserialize the result to, or {@code Void.class} for endpoints that are expected to
     *             return HTTP 204 No Content
     * @param <T> Type parameter for the return value type
     * @return The deserialized response object, or {@code null} if the return type is Void
     * @throws UnirestException If there was an HTTP exception
     */
    <T> T defaultDelete(String path, Class<T> type)
        throws UnirestException, InvalidTokenException {
        try (Timer.Context ctx = stats.httpGetTime.time()) {
            HttpRequest req = Unirest.delete(BASE_URL + path);
            addDefaultHeaders(req);
            addAuthHeader(req);
            HttpResponse<String> response = req.asString();
            //  Log for metrics
            logResponse(response);
            //  Check for error conditions
            int status = response.getStatus();
            if (status == HTTP_NOT_AUTHENTICATED) {
                //  Authentication failed
                throw new InvalidTokenException(HttpMethod.POST, path, "Bad token");
            }
            if (status == HTTP_TOO_MANY_REQUESTS) {
                //  We hit the rate limit, throw an exception to let the caller know
                long retryIn = Long.parseLong(response.getHeaders().getFirst(HTTP_RETRY_AFTER_HEADER));
                throw new RateLimitExceededException(retryIn);
            }
            if (status == HTTP_EMPTY) {
                //  We expect the response to be empty if we expected Void back
                //  Otherwise, this is an unexpected situation
                if (Void.class.equals(type)) {
                    return null;
                } else {
                    throw new UnirestException("Got HTTP 204: Expected a response body, got none");
                }
            }
            if (status != HTTP_OK) {
                //  Other errors
                throw new UnirestException("HTTP " + status + ": " + response.getStatusText());
            }
            return gson.fromJson(response.getBody(), type);
        }
    }

    /**
     * Logs an HTTP response status code for metrics
     * @param response The HTTP request response
     * @param <T> The result type of the HTTP request response (ignored)
     * @return the given HTTP request response (pass-through)
     */
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

    /**
     * Checks whether or not a given status is within an HTTP status code range
     * @param range The HTTP status range, such as 200 for all 2XX statuses
     * @param status The HTTP status to test
     * @return true if status is a member of the given HTTP status range, false otherwise
     */
    private boolean inStatusRange(int range, int status) {
        return status >= range && status < range + 100;
    }

    /**
     * Performs validation on the given object according to its validation rules, throwing an exception if it fails
     * @param o The object to validate, annotated with validation constraints
     * @throws ApiException If the validation fails
     */
    void validate(Object o) throws ApiException {
        Set<ConstraintViolation<Object>> validate = validator.validate(o);
        if (!validate.isEmpty()) {
            throw new InvalidApiRequestException(getViolations(validate));
        }
    }

    /**
     * Performs validation on the given object according to its validation rules, throwing an exception containing
     * the HTTP method and endpoint if it fails
     * @param method The HTTP method being invoked
     * @param endpoint The endpoint being invoked
     * @param o The object to validate, annotated with validation constraints
     * @throws ApiException If the validation fails
     */
    void validate(HttpMethod method, String endpoint, Object o) throws ApiException {
        Set<ConstraintViolation<Object>> validate = validator.validate(o);
        if (!validate.isEmpty()) {
            throw new InvalidApiRequestException(method, endpoint, getViolations(validate));
        }
    }

    /**
     * Gets a string containing a list of validation violations, if any
     * @param validate The object to validate
     * @return A string containing a list of violations, or emtpy string if there are none
     */
    private String getViolations(Set<ConstraintViolation<Object>> validate) {
        StringJoiner joiner = new StringJoiner(", ");
        for (ConstraintViolation<Object> violation : validate) {
            joiner.add(String.format("Field %s invalid: %s",
                    violation.getPropertyPath(),
                    violation.getMessage()));
        }
        return joiner.toString();
    }

    /**
     * Converts a snowflake int64 into the proper string representation for Discord (unsigned decimal)
     * @param snowflake The snowflake ID to convert
     * @return The snowflake in string format
     */
    String snowflakeToString(long snowflake) {
        return Long.toUnsignedString(snowflake);
    }
}
