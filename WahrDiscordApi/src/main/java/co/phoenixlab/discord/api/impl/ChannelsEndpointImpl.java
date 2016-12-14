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
import co.phoenixlab.discord.api.entities.channel.message.Message;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.request.channel.CreateChannelRequest;
import co.phoenixlab.discord.api.request.channel.CreatePrivateChannelRequest;
import co.phoenixlab.discord.api.request.channel.EditChannelPermissionsRequest;
import co.phoenixlab.discord.api.request.channel.ModifyChannelRequest;
import com.google.inject.Inject;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static co.phoenixlab.discord.api.enums.ValidateRequestOption.REQUEST_MUST_BE_NULL;
import static co.phoenixlab.discord.api.util.SnowflakeUtils.snowflakeToString;

public class ChannelsEndpointImpl implements ChannelsEndpoint, ChannelsEndpointAsync {

    private static final String CHANNEL_ENDPOINT_BASE = "/channels/";
    private static final String CHANNEL_ENDPOINT = "/channels/{channel.id}";
    private static final String GUILD_CHANNEL_ENDPOINT = "/guilds/{guild.id}/channels";
    private static final String GUILD_CHANNEL_ENDPOINT_FMT = "/guilds/%s/channels";
    private static final String PRIVATE_CHANNEL_ENDPOINT = "/users/@me/channels";
    private static final String TYPING_ENDPOINT = "/channels/{channel.id}/";
    private static final String TYPING_ENDPOINT_FMT = "/channels/%s/typing";
    private static final String CHANNEL_PERMISSIONS_ENDPOINT = "/channels/{channel.id}/permissions/{overwrite.id}";
    private static final String CHANNEL_PERMISSIONS_ENDPOINT_FMT = "/channels/%1$s/permissions/%2$s";
    private static final String CHANNEL_PIN_MSG_ENDPOINT = "/channels/{channel.id}/pins/{message.id}";
    private static final String CHANNEL_PIN_MSG_ENDPOINT_FMT = "/channels/%1$s/pins/%2$s";
    private static final String CHANNEL_PINS_ENDPOINT = "/channels/{channel.id}/pins";
    private static final String CHANNEL_PINS_ENDPOINT_FMT = "/channels/%1$s/pins";

    @Inject
    private ScheduledExecutorService executorService;
    @Inject
    private EndpointsImpl endpoints;
    @Inject
    private WahrDiscordApiImpl api;

    @Override
    public GuildChannel createChannel(long guildId, CreateChannelRequest request) throws ApiException {
        return endpoints.performPost(channelFormatPath(guildId, GUILD_CHANNEL_ENDPOINT_FMT),
            request,
            GuildChannel.class,
            GUILD_CHANNEL_ENDPOINT
        );
    }

    @Override
    public DmChannel createPrivateChannel(CreatePrivateChannelRequest request) throws ApiException {
        return endpoints.performPost(PRIVATE_CHANNEL_ENDPOINT,
            request,
            DmChannel.class,
            PRIVATE_CHANNEL_ENDPOINT
        );
    }

    @Override
    public GuildChannel editChannel(long channelId, ModifyChannelRequest request) throws ApiException {
        return endpoints.performPatch(channelPath(channelId),
            request,
            GuildChannel.class,
            CHANNEL_ENDPOINT
        );
    }

    @Override
    public void deleteChannel(long channelId) throws ApiException {
        endpoints.performDelete(channelPath(channelId),
            Void.class,
            CHANNEL_ENDPOINT
        );
    }

    @Override
    public GuildChannel[] getGuildChannels(long guildId) throws ApiException {
        return endpoints.performGet(channelFormatPath(guildId, GUILD_CHANNEL_ENDPOINT_FMT),
            GuildChannel[].class,
            GUILD_CHANNEL_ENDPOINT
        );
    }

    @Override
    public void broadcastTyping(long channelId) throws ApiException {
        endpoints.performPost(channelFormatPath(channelId, TYPING_ENDPOINT_FMT),
            null,
            Void.class,
            TYPING_ENDPOINT,
            REQUEST_MUST_BE_NULL
        );
    }

    @Override
    public Channel getChannel(long channelId) throws ApiException {
        return endpoints.performGet(channelPath(channelId),
            Channel.class,
            CHANNEL_ENDPOINT
        );
    }

    @Override
    public void editChannelPermission(long channelId, long overwriteId, EditChannelPermissionsRequest request)
            throws ApiException {
        String url = String.format(CHANNEL_PERMISSIONS_ENDPOINT_FMT,
            snowflakeToString(channelId),
            snowflakeToString(overwriteId)
        );
        endpoints.performPut(url,
            request,
            Void.class,
            CHANNEL_PERMISSIONS_ENDPOINT
        );
    }

    @Override
    public void deleteChannelPermission(long channelId, long overwriteId) throws ApiException {
        String url = String.format(CHANNEL_PERMISSIONS_ENDPOINT_FMT,
            snowflakeToString(channelId),
            snowflakeToString(overwriteId)
        );
        endpoints.performDelete(url,
            Void.class,
            CHANNEL_PERMISSIONS_ENDPOINT
        );
    }

    @Override
    public Message[] getPinnedMessages(long channelId) throws ApiException {
        return endpoints.performGet(channelFormatPath(channelId, CHANNEL_PINS_ENDPOINT_FMT),
            Message[].class,
            CHANNEL_PINS_ENDPOINT
        );
    }

    @Override
    public void pinMessage(long channelId, long messageId) throws ApiException {
        String url = String.format(CHANNEL_PIN_MSG_ENDPOINT_FMT,
            snowflakeToString(channelId),
            snowflakeToString(messageId)
        );
        endpoints.performPut(url,
            null,
            Void.class,
            CHANNEL_PIN_MSG_ENDPOINT,
            REQUEST_MUST_BE_NULL
        );
    }

    @Override
    public void deletePinnedMessage(long channelId, long messageId) throws ApiException {
        String url = String.format(CHANNEL_PIN_MSG_ENDPOINT_FMT,
            snowflakeToString(channelId),
            snowflakeToString(messageId)
        );
        endpoints.performDelete(url,
            Void.class,
            CHANNEL_PIN_MSG_ENDPOINT
        );
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

    @Override
    public Future<Void> editChannelPermissionAsync(long channelId, long overwriteId,
                                                   EditChannelPermissionsRequest request)
            throws ApiException {
        return executorService.submit(() -> editChannelPermission(channelId, overwriteId, request), null);
    }

    @Override
    public Future<Void> deleteChannelPermissionAsync(long channelId, long overwriteId) throws ApiException {
        return executorService.submit(() -> deleteChannelPermission(channelId, overwriteId), null);
    }

    @Override
    public Future<Message[]> getPinnedMessagesAsync(long channelId) throws ApiException {
        return executorService.submit(() -> getPinnedMessages(channelId));
    }

    @Override
    public Future<Void> pinMessageAsync(long channelId, long messageId) throws ApiException {
        return executorService.submit(() -> pinMessage(channelId, messageId), null);
    }

    @Override
    public Future<Void> deletePinnedMessageAsync(long channelId, long messageId) throws ApiException {
        return executorService.submit(() -> deletePinnedMessage(channelId, messageId), null);
    }

    private String channelPath(long id) {
        return CHANNEL_ENDPOINT_BASE + snowflakeToString(id);
    }

    private String channelFormatPath(long id, String format) {
        return String.format(format, snowflakeToString(id));
    }

}
