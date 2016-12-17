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

import co.phoenixlab.discord.api.endpoints.MembersEndpoint;
import co.phoenixlab.discord.api.endpoints.async.MembersEndpointAsync;
import co.phoenixlab.discord.api.entities.guild.Member;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.request.guild.EditMemberRequest;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpMethod;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static co.phoenixlab.discord.api.enums.ValidateRequestOption.REQUEST_CAN_BE_NULL;
import static co.phoenixlab.discord.api.enums.ValidateRequestOption.REQUEST_MUST_BE_NULL;

public class MembersEndpointImpl implements MembersEndpoint, MembersEndpointAsync {

    private static final String MEMBER_ENDPOINT = "/guilds/{guild.id}/members/{user.id}";
    private static final String MEMBER_ENDPOINT_FMT = "/guilds/%1$s/members/%2$s";
    private static final String PRUNE_ENDPOINT = "/guilds/{guild.id}/prune";
    private static final String PRUNE_ENDPOINT_FMT = "/guilds/%1$s/prune";

    @Inject
    private ScheduledExecutorService executorService;
    @Inject
    private EndpointsImpl endpoints;
    @Inject
    private WahrDiscordApiImpl api;

    @Override
    public void editMember(long guildId, long userId, EditMemberRequest request) throws ApiException {
        endpoints.performPatch(formatGuildMember(guildId, userId, MEMBER_ENDPOINT_FMT),
            request,
            Void.class,
            MEMBER_ENDPOINT,
            REQUEST_CAN_BE_NULL
        );
    }

    @Override
    public void kickMember(long guildId, long userId) throws ApiException {
        endpoints.performDelete(formatGuildMember(guildId, userId, MEMBER_ENDPOINT_FMT),
            Void.class,
            MEMBER_ENDPOINT
        );
    }

    @Override
    public Member getMember(long guildId, long userId) throws ApiException {
        return endpoints.performGet(formatGuildMember(guildId, userId, MEMBER_ENDPOINT_FMT),
            Member.class,
            MEMBER_ENDPOINT
        );
    }

    @Override
    public int getMemberCountForPrune(long guildId, int days) throws ApiException {
        try {
            return (Integer) endpoints.performGet(formatGuild(guildId, PRUNE_ENDPOINT_FMT) + "?days=" + days,
                Map.class,
                PRUNE_ENDPOINT
            )
                .get("pruned");
        } catch (NullPointerException npe) {
            throw new ApiException(HttpMethod.GET, PRUNE_ENDPOINT, "Unable to parse response from server");
        }
    }

    @Override
    public int pruneMembers(long guildId, int days) throws ApiException {
        try {
            return (Integer) endpoints.performPost(formatGuild(guildId, PRUNE_ENDPOINT_FMT) + "?days=" + days,
                null,
                Map.class,
                PRUNE_ENDPOINT,
                REQUEST_MUST_BE_NULL
            )
                .get("pruned");
        } catch (NullPointerException npe) {
            throw new ApiException(HttpMethod.GET, PRUNE_ENDPOINT, "Unable to parse response from server");
        }
    }

    private String formatGuild(long guildId, String format) {
        return String.format(format, guildId);
    }

    private String formatGuildMember(long guildId, long userId, String format) {
        return String.format(format, guildId, userId);
    }

    @Override
    public Future<Void> editMemberAsync(long guildId, long userId, EditMemberRequest request) throws ApiException {
        return executorService.submit(() -> editMember(guildId, userId, request), null);
    }

    @Override
    public Future<Void> kickMemberAsync(long guildId, long userId) throws ApiException {
        return executorService.submit(() -> kickMember(guildId, userId), null);
    }

    @Override
    public Future<Member> getMemberAsync(long guildId, long userId) throws ApiException {
        return executorService.submit(() -> getMember(guildId, userId));
    }

    @Override
    public Future<Integer> getMemberCountForPruneAsync(long guildId, int days) throws ApiException {
        return executorService.submit(() -> getMemberCountForPrune(guildId, days));
    }

    @Override
    public Future<Integer> pruneMembersAsync(long guildId, int days) throws ApiException {
        return executorService.submit(() -> pruneMembers(guildId, days));
    }
}
