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

public class EndpointsImpl implements Endpoints {

    public static final String BASE_URL = "https://discordapp.com/api";
    private final WahrDiscordApiImpl apiImpl;

    EndpointsImpl(WahrDiscordApiImpl apiImpl) {
        this.apiImpl = apiImpl;
    }

    @Override
    public AuthenticationEndpoint auth() {
        return null;
    }

    @Override
    public AuthenticationEndpointAsync authAsync() {
        return null;
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
}
