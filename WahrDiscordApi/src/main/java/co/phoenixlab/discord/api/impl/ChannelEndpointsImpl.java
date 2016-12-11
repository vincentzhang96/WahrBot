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
import co.phoenixlab.discord.api.endpoints.ChannelsEndpoint;
import co.phoenixlab.discord.api.endpoints.async.ChannelsEndpointAsync;
import co.phoenixlab.discord.api.entities.channel.Channel;
import co.phoenixlab.discord.api.entities.channel.DmChannel;
import co.phoenixlab.discord.api.entities.channel.GuildChannel;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.request.channel.CreateChannelRequest;
import co.phoenixlab.discord.api.request.channel.CreatePrivateChannelRequest;
import co.phoenixlab.discord.api.request.channel.ModifyChannelRequest;
import co.phoenixlab.discord.api.util.OkFailMeter;
import co.phoenixlab.discord.api.util.SnowflakeUtils;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.UnaryOperator;

import static co.phoenixlab.discord.api.util.SnowflakeUtils.*;
import static com.codahale.metrics.MetricRegistry.name;
import static com.mashape.unirest.http.HttpMethod.*;

public class ChannelEndpointsImpl implements ChannelsEndpoint, ChannelsEndpointAsync {

    private static final String CHANNEL_ENDPOINT_BASE = "/channels/";
    private static final String GUILD_CHANNEL_ENDPOINT = "/guilds/{guild.id}/channels";
    private static final String GUILD_CHANNEL_ENDPOINT_FMT = "/guilds/%s/channels";
    private static final String PRIVATE_CHANNEL_ENDPOINT = "/users/@me/channels";
    private static final String TYPING_ENDPOINT = "/channels/{channel.id}/";
    private static final String TYPING_ENDPOINT_FMT = "/channels/%s/typing";

    @Inject
    private ScheduledExecutorService executorService;
    @Inject
    private EndpointsImpl endpoints;
    @Inject
    private WahrDiscordApiImpl api;
    @Inject
    private Stats stats;

    @Override
    public GuildChannel createChannel(long guildId, CreateChannelRequest request) throws ApiException {
        endpoints.validate(POST, GUILD_CHANNEL_ENDPOINT, request);
        try {
            GuildChannel ret = endpoints.defaultPost(channelFormatPath(guildId, GUILD_CHANNEL_ENDPOINT_FMT),
                    request, GuildChannel.class);
            stats.createChannel.ok();
            return ret;
        } catch (ApiException apie) {
            stats.createChannel.fail();
            //  rethrow
            throw apie;
        } catch (Exception e) {
            stats.createChannel.fail();
            throw new ApiException(POST, GUILD_CHANNEL_ENDPOINT, e);
        }
    }

    @Override
    public DmChannel createPrivateChannel(CreatePrivateChannelRequest request) throws ApiException {
        endpoints.validate(POST, PRIVATE_CHANNEL_ENDPOINT, request);
        try {
            DmChannel ret = endpoints.defaultPost(PRIVATE_CHANNEL_ENDPOINT, request, DmChannel.class);
            stats.createPrivateChannel.ok();
            return ret;
        } catch (ApiException apie) {
            stats.createPrivateChannel.fail();
            //  rethrow
            throw apie;
        } catch (Exception e) {
            stats.createPrivateChannel.fail();
            throw new ApiException(POST, PRIVATE_CHANNEL_ENDPOINT, e);
        }
    }

    @Override
    public GuildChannel editChannel(long channelId, ModifyChannelRequest request) throws ApiException {
        endpoints.validate(PATCH, CHANNEL_ENDPOINT_BASE, request);
        try {
            GuildChannel ret = endpoints.defaultPatch(channelPath(channelId), request, GuildChannel.class);
            stats.editChannel.ok();
            return ret;
        } catch (ApiException apie) {
            stats.editChannel.fail();
            //  rethrow
            throw apie;
        } catch (Exception e) {
            stats.editChannel.fail();
            throw new ApiException(PATCH, CHANNEL_ENDPOINT_BASE, e);
        }
    }

    @Override
    public void deleteChannel(long channelId) throws ApiException {
        try {
            endpoints.defaultDelete(channelPath(channelId), Void.class);
            stats.deleteChannel.ok();
        } catch (ApiException apie) {
            stats.deleteChannel.fail();
            //  rethrow
            throw apie;
        } catch (Exception e) {
            stats.deleteChannel.fail();
            throw new ApiException(DELETE, CHANNEL_ENDPOINT_BASE, e);
        }
    }

    @Override
    public GuildChannel[] getGuildChannels(long guildId) throws ApiException {
        try {
            GuildChannel[] ret = endpoints.defaultGet(channelFormatPath(guildId, GUILD_CHANNEL_ENDPOINT_FMT),
                    GuildChannel[].class);
            stats.getGuildChannels.ok();
            return ret;
        } catch (ApiException apie) {
            stats.getGuildChannels.fail();
            //  rethrow
            throw apie;
        } catch (Exception e) {
            stats.getGuildChannels.fail();
            throw new ApiException(GET, GUILD_CHANNEL_ENDPOINT, e);
        }
    }

    @Override
    public void broadcastTyping(long channelId) throws ApiException {
        try {
            endpoints.defaultPost(channelFormatPath(channelId, TYPING_ENDPOINT_FMT), null, Void.class);
            stats.broadcastTyping.ok();
        } catch (ApiException apie) {
            stats.broadcastTyping.fail();
            //  rethrow
            throw apie;
        } catch (Exception e) {
            stats.broadcastTyping.fail();
            throw new ApiException(POST, TYPING_ENDPOINT, e);
        }
    }

    @Override
    public Channel getChannel(long channelId) throws ApiException {
        try {
            Channel ret = endpoints.defaultGet(channelPath(channelId), Channel.class);
            stats.getChannel.ok();
            return ret;
        } catch (ApiException apie) {
            stats.getChannel.fail();
            //  rethrow
            throw apie;
        } catch (Exception e) {
            stats.getChannel.fail();
            throw new ApiException(GET, CHANNEL_ENDPOINT_BASE, e);
        }
    }

    @Override
    public Future<GuildChannel> createChannelAsync(long guildId, CreateChannelRequest request) throws ApiException {
        return executorService.submit(() -> createChannel(guildId, request));
    }

    @Override
    public Future<DmChannel> createPrivateChannelAsync(CreatePrivateChannelRequest request) throws ApiException {
        return executorService.submit(() -> createPrivateChannel(request));
    }

    @Override
    public Future<GuildChannel> editChannelAsync(long channelId, ModifyChannelRequest request) throws ApiException {
        return executorService.submit(() -> editChannel(channelId, request));
    }

    @Override
    public Future<Void> deleteChannelAsync(long channelId) throws ApiException {
        return executorService.submit(() -> deleteChannel(channelId), null);
    }

    @Override
    public Future<GuildChannel[]> getGuildChannelsAsync(long guildId) throws ApiException {
        return executorService.submit(() -> getGuildChannels(guildId));
    }

    @Override
    public Future<Void> broadcastTypingAsync(long channelId) throws ApiException {
        return executorService.submit(() -> broadcastTyping(channelId), null);
    }

    @Override
    public Future<Channel> getChannelAsync(long channelId) throws ApiException {
        return executorService.submit(() -> getChannel(channelId));
    }

    private String channelPath(long id) {
        return CHANNEL_ENDPOINT_BASE + snowflakeToString(id);
    }

    private String channelFormatPath(long id, String format) {
        return String.format(format, snowflakeToString(id));
    }

    static class Stats {

        final OkFailMeter createChannel;
        final OkFailMeter createPrivateChannel;
        final OkFailMeter editChannel;
        final OkFailMeter deleteChannel;
        final OkFailMeter getGuildChannels;
        final OkFailMeter broadcastTyping;
        final OkFailMeter getChannel;

        @Inject
        Stats(MetricRegistry metrics, WahrDiscordApi api) {
            String instanceId = api.getInstanceId();
            Class<EndpointsImpl> baseClass = EndpointsImpl.class;
            createChannel = meter((s) -> name(baseClass, "channel", "create", s, instanceId), metrics);
            createPrivateChannel = meter((s) -> name(baseClass, "channel", "createPrivate", s, instanceId), metrics);
            editChannel = meter((s) -> name(baseClass, "channel", "edit", s, instanceId), metrics);
            deleteChannel = meter((s) -> name(baseClass, "channel", "delete", s, instanceId), metrics);
            getGuildChannels = meter((s) -> name(baseClass, "channel", "getGuildChannels", s, instanceId), metrics);
            broadcastTyping = meter((s) -> name(baseClass, "channel", "broadcastTyping", s, instanceId), metrics);
            getChannel = meter((s) -> name(baseClass, "channel", "get", s, instanceId), metrics);
        }

        private final OkFailMeter meter(UnaryOperator<String> builder, MetricRegistry metrics) {
            return new OkFailMeter(builder, metrics);
        }
    }
}
