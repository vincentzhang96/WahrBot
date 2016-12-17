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

import co.phoenixlab.discord.api.endpoints.GuildsEndpoint;
import co.phoenixlab.discord.api.endpoints.async.GuildsEndpointAsync;
import co.phoenixlab.discord.api.entities.guild.Guild;
import co.phoenixlab.discord.api.entities.guild.UserGuild;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.exceptions.RequestDeniedToBotsException;
import co.phoenixlab.discord.api.request.WSRequest;
import co.phoenixlab.discord.api.request.guild.CreateGuildRequest;
import co.phoenixlab.discord.api.request.guild.EditGuildRequest;
import co.phoenixlab.discord.api.request.guild.GuildMembersRequest;
import com.google.inject.Inject;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static co.phoenixlab.discord.api.enums.ValidateRequestOption.REQUEST_CAN_BE_NULL;
import static com.mashape.unirest.http.HttpMethod.POST;

public class GuildsEndpointImpl implements GuildsEndpoint, GuildsEndpointAsync {

    private static final String GUILDS_ENDPOINT = "/guilds";
    private static final String GUILD_ENDPOINT = "/guilds/{guild.id}";
    private static final String GUILD_ENDPOINT_FMT = "/guilds/%1$s";
    private static final String USER_GUILDS_ENDPOINT = "/users/@me/guilds";
    private static final String USER_GUILD_ENDPOINT = "/users/@me/guilds/{guild.id}";
    private static final String USER_GUILD_ENDPOINT_FMT = "/users/@me/guilds/%1$s";

    @Inject
    private ScheduledExecutorService executorService;
    @Inject
    private EndpointsImpl endpoints;
    @Inject
    private WahrDiscordApiImpl api;

    @Override
    public Guild createGuild(CreateGuildRequest request) throws ApiException {
        if (api.getSelf().isBot()) {
            throw new RequestDeniedToBotsException(POST, GUILDS_ENDPOINT);
        }
        return endpoints.performPost(GUILDS_ENDPOINT,
            request,
            Guild.class,
            GUILDS_ENDPOINT
        );
    }

    @Override
    public Guild editGuild(long guildId, EditGuildRequest request) throws ApiException {
        return endpoints.performPatch(formatGuild(guildId, GUILD_ENDPOINT_FMT),
            request,
            Guild.class,
            GUILD_ENDPOINT,
            REQUEST_CAN_BE_NULL
        );
    }

    @Override
    public void leaveGuild(long guildId) throws ApiException {
        endpoints.performDelete(formatGuild(guildId, USER_GUILD_ENDPOINT_FMT),
            Void.class,
            USER_GUILD_ENDPOINT
        );
    }

    @Override
    public Guild deleteGuild(long guildId) throws ApiException {
        return endpoints.performDelete(formatGuild(guildId, GUILD_ENDPOINT_FMT),
            Guild.class,
            GUILD_ENDPOINT
        );
    }

    @Override
    public Guild getGuild(long guildId) throws ApiException {
        return endpoints.performGet(formatGuild(guildId, GUILD_ENDPOINT_FMT),
            Guild.class,
            GUILD_ENDPOINT
        );
    }

    @Override
    public UserGuild[] getGuilds() throws ApiException {
        return endpoints.performGet(USER_GUILDS_ENDPOINT,
            UserGuild[].class,
            USER_GUILDS_ENDPOINT
        );
    }

    @Override
    public void requestMembers(GuildMembersRequest request) throws ApiException {
        endpoints.validate(request);
        api.getWebSocketClient().send(WSRequest.requestGuildMembers(request));
    }


    private String formatGuild(long guildId, String format) {
        return String.format(format, guildId);
    }

    @Override
    public Future<Guild> createGuildAsync(CreateGuildRequest request) throws ApiException {
        return executorService.submit(() -> createGuild(request));
    }

    @Override
    public Future<Guild> editGuildAsync(long guildId, EditGuildRequest request) throws ApiException {
        return executorService.submit(() -> editGuild(guildId, request));
    }

    @Override
    public Future<Void> leaveGuildAsync(long guildId) throws ApiException {
        return executorService.submit(() -> leaveGuild(guildId), null);
    }

    @Override
    public Future<Guild> deleteGuildAsync(long guildId) throws ApiException {
        return executorService.submit(() -> deleteGuild(guildId));
    }

    @Override
    public Future<Guild> getGuildAsync(long guildId) throws ApiException {
        return executorService.submit(() -> getGuild(guildId));
    }

    @Override
    public Future<UserGuild[]> getGuildsAsync() throws ApiException {
        return executorService.submit(this::getGuilds);
    }

    @Override
    public Future<Void> requestMembersAsync(GuildMembersRequest request) throws ApiException {
        return executorService.submit(() -> requestMembers(request), null);
    }
}
