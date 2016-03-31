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
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.apache.http.entity.ContentType;

public class EndpointsImpl implements Endpoints {

    public static final String BASE_URL = "https://discordapp.com/api";
    @Inject
    private WahrDiscordApiImpl apiImpl;
    @Inject
    private LoginEndpointsImpl login;
    @Inject
    private Gson gson;

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

    HttpResponse<String> defaultPostUnauth(String url, String body) throws UnirestException {
        HttpRequestWithBody req = Unirest.post(url);
        addDefaultHeaders(req);
        return req.body(body).asString();
    }

    HttpResponse<String> defaultPost(String url, String body) throws UnirestException {
        HttpRequestWithBody req = Unirest.post(url);
        addDefaultHeaders(req);
        addAuthHeader(req);
        return req.body(body).asString();
    }

    HttpResponse<String> defaultGetUnauth(String url) throws UnirestException {
        HttpRequest req = Unirest.get(url);
        addDefaultHeaders(req);
        return req.asString();
    }

    HttpResponse<String> defaultGet(String url) throws UnirestException {
        HttpRequest req = Unirest.get(url);
        addDefaultHeaders(req);
        addAuthHeader(req);
        return req.asString();
    }
}
