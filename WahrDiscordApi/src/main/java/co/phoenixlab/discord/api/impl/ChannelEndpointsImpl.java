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

import co.phoenixlab.discord.api.endpoints.ChannelsEndpoint;
import co.phoenixlab.discord.api.endpoints.async.ChannelsEndpointAsync;
import co.phoenixlab.discord.api.entities.channel.Channel;
import co.phoenixlab.discord.api.entities.channel.DmChannel;
import co.phoenixlab.discord.api.entities.channel.GuildChannel;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.request.channel.CreateChannelRequest;
import co.phoenixlab.discord.api.request.channel.CreatePrivateChannelRequest;
import co.phoenixlab.discord.api.request.channel.ModifyChannelRequest;
import com.google.inject.Inject;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static com.mashape.unirest.http.HttpMethod.GET;

public class ChannelEndpointsImpl implements ChannelsEndpoint, ChannelsEndpointAsync {

    private static final String CHANNEL_ENDPOINT_BASE = "/channels/";

    @Inject
    private ScheduledExecutorService executorService;
    @Inject
    private EndpointsImpl endpoints;
    @Inject
    private WahrDiscordApiImpl api;

    @Override
    public GuildChannel createChannel(long guildId, CreateChannelRequest request) throws ApiException {
        return null;
    }

    @Override
    public DmChannel createPrivateChannel(long myUserId, CreatePrivateChannelRequest request) throws ApiException {
        return null;
    }

    @Override
    public GuildChannel editChannel(long channelId, ModifyChannelRequest request) throws ApiException {
        endpoints.validate(GET, CHANNEL_ENDPOINT_BASE, request);
        try {
            return endpoints.defaultPatch(channel(channelId), request, GuildChannel.class);
        } catch (ApiException apie) {
            //  rethrow
            throw apie;
        } catch (Exception e) {
            throw new ApiException(GET, CHANNEL_ENDPOINT_BASE, e);
        }
    }

    @Override
    public void deleteChannel(long channelId) throws ApiException {

    }

    @Override
    public GuildChannel[] getGuildChannels(long guildId) throws ApiException {
        return new GuildChannel[0];
    }

    @Override
    public void broadcastTyping(long channelId) throws ApiException {

    }

    @Override
    public Channel getChannel(long channelId) throws ApiException {
        try {
            return endpoints.defaultGet(channel(channelId), Channel.class);
        } catch (ApiException apie) {
            //  rethrow
            throw apie;
        } catch (Exception e) {
            throw new ApiException(GET, CHANNEL_ENDPOINT_BASE, e);
        }
    }

    @Override
    public Future<GuildChannel> createChannelAsync(long guildId, CreateChannelRequest request) throws ApiException {
        return null;
    }

    @Override
    public Future<DmChannel> createPrivateChannelAsync(long myUserId, CreatePrivateChannelRequest request) throws ApiException {
        return null;
    }

    @Override
    public Future<GuildChannel> editChannelAsync(long channelId, ModifyChannelRequest request) throws ApiException {
        return null;
    }

    @Override
    public Future<Void> deleteChannelAsync(long channelId) throws ApiException {
        return null;
    }

    @Override
    public Future<GuildChannel[]> getGuildChannelsAsync(long guildId) throws ApiException {
        return null;
    }

    @Override
    public Future<Void> broadcastTypingAsync(long channelId) throws ApiException {
        return null;
    }

    @Override
    public Future<Channel> getChannelAsync(long channelId) throws ApiException {
        return null;
    }

    private String channel(long id) {
        return CHANNEL_ENDPOINT_BASE + endpoints.snowflakeToString(id);
    }
}
