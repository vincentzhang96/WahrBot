package co.phoenixlab.discord.api.impl;

import co.phoenixlab.discord.api.endpoints.MessagesEndpoint;
import co.phoenixlab.discord.api.endpoints.async.MessagesEndpointAsync;
import co.phoenixlab.discord.api.entities.channel.message.Message;
import co.phoenixlab.discord.api.entities.guild.Emoji;
import co.phoenixlab.discord.api.entities.user.HumanUser;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.exceptions.InvalidApiRequestException;
import co.phoenixlab.discord.api.exceptions.RequestRequiresBotStatusException;
import co.phoenixlab.discord.api.request.channel.message.BulkMessageDeleteRequest;
import co.phoenixlab.discord.api.request.channel.message.CreateMessageRequest;
import co.phoenixlab.discord.api.request.channel.message.EditMessageRequest;
import com.google.inject.Inject;

import java.util.StringJoiner;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static co.phoenixlab.discord.api.util.SnowflakeUtils.snowflakeToString;
import static co.phoenixlab.discord.api.util.WahrDiscordApiUtils.urlEncode;
import static com.mashape.unirest.http.HttpMethod.GET;
import static com.mashape.unirest.http.HttpMethod.POST;

public class MessagesEndpointImpl implements MessagesEndpoint, MessagesEndpointAsync {

    private static final String MESSAGES_ENDPOINT = "/channels/{channel.id}/messages";
    private static final String MESSAGES_ENDPOINT_FMT = "/channels/%s/messages";
    private static final String SPECIFIC_MESSAGE_ENDPOINT = "/channels/{channel.id}/{message.id}";
    private static final String SPECIFIC_MESSAGE_ENDPOINT_FMT = "/channels/%1$s/%2$s";
    private static final String MESSAGE_ACK_ENDPOINT = "/channels/{channel.id}/{message.id}/ack";
    private static final String MESSAGE_ACK_ENDPOINT_FMT = "/channels/%1$s/%2$s/ack";
    private static final String MESSAGE_BULK_DELETE_ENDPOINT = "/channels/{channel.id}/messages/bulk-delete";
    private static final String MESSAGE_BULK_DELETE_ENDPOINT_FMT = "/channels/%1$s/messages/bulk-delete";
    private static final String USER_REACTION_ENDPOINT =
        "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/{user.id}";
    private static final String USER_REACTION_ENDPOINT_FMT =
        "/channels/%1$s/messages/%2$s/reactions/%3$s/%4$s";
    private static final String MESSAGE_REACTION_ENDPOINT =
        "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}";
    private static final String MESSAGE_REACTION_ENDPOINT_FMT =
        "/channels/%1$s/messages/%2$s/reactions/%3$s";
    private static final String MESSAGE_REACTIONS_ENDPOINT =
        "/channels/{channel.id}/messages/{message.id}/reactions";
    private static final String MESSAGE_REACTIONS_ENDPOINT_FMT =
        "/channels/%1$s/messages/%2$s/reactions";
    private static final String SELF = "@me";


    @Inject
    private ScheduledExecutorService executorService;
    @Inject
    private EndpointsImpl endpoints;
    @Inject
    private WahrDiscordApiImpl api;

    @Override
    public Message[] getMessages(long channelId, long beforeMessageId, long afterMessageId,
                                 long aroundMessageId, int limit)
            throws ApiException {
        //  Either before, around, or after can be set, not more than one
        int count = 0;
        if (beforeMessageId != MESSAGE_ID_IGNORE_PARAM) {
            ++count;
        }
        if (aroundMessageId != MESSAGE_ID_IGNORE_PARAM) {
            ++count;
        }
        if (afterMessageId != MESSAGE_ID_IGNORE_PARAM) {
            ++count;
        }
        if (count > 1) {
            throw new InvalidApiRequestException(GET, MESSAGES_ENDPOINT,
                    "BeforeMessageId, AroundMessageId, and AfterMessageId are mutually exclusive.");
        }
        if (limit < LIMIT_MIN) {
            throw new InvalidApiRequestException(GET, MESSAGES_ENDPOINT,
                    String.format("Limit must be at least %d, got %d instead", LIMIT_MIN, limit));
        }
        if (limit > LIMIT_MAX) {
            throw new InvalidApiRequestException(GET, MESSAGES_ENDPOINT,
                    String.format("Limit must be at most %d, got %d instead", LIMIT_MAX, limit));
        }
        //  Add query parameters
        StringJoiner joiner = new StringJoiner("&", "?", "");
        joiner.setEmptyValue("");   //  Don't send prefix/suffix if we don't have any args
        if (beforeMessageId != MESSAGE_ID_IGNORE_PARAM) {
            joiner.add(String.format("before=%s", snowflakeToString(beforeMessageId)));
        } else if (afterMessageId != MESSAGE_ID_IGNORE_PARAM) {
            joiner.add(String.format("after=%s", snowflakeToString(afterMessageId)));
        } else if (aroundMessageId != MESSAGE_ID_IGNORE_PARAM) {
            joiner.add(String.format("around=%s", snowflakeToString(aroundMessageId)));
        }
        if (limit != LIMIT_DEFAULT) {
            joiner.add(String.format("limit=%d", limit));
        }
        return endpoints.performGet(messageFormatPath(channelId, MESSAGES_ENDPOINT_FMT) + joiner.toString(),
                Message[].class,
                MESSAGES_ENDPOINT);
    }

    @Override
    public Message getMessage(long channelId, long messageId)
            throws ApiException {
        if (!api.getSelf().isBot()) {
            throw new RequestRequiresBotStatusException(GET, SPECIFIC_MESSAGE_ENDPOINT);
        }
        return endpoints.performGet(messageFormatPath(channelId, messageId, SPECIFIC_MESSAGE_ENDPOINT_FMT),
            Message.class,
            SPECIFIC_MESSAGE_ENDPOINT);
    }

    @Override
    public Message sendMessage(long channelId, CreateMessageRequest request)
            throws ApiException {
        //  API enforces this automatically for us so we actually don't need to care
//        if (request.getEmbed() != null && !"rich".equalsIgnoreCase(request.getEmbed().getType())) {
//            throw new InvalidApiRequestException(POST, MESSAGES_ENDPOINT,
//                String.format("Embed must have type set to \"rich\", is set to \"%s\".", request.getEmbed().getType()));
//        }
        return endpoints.performPost(messageFormatPath(channelId, MESSAGES_ENDPOINT_FMT),
                request,
                Message.class,
                MESSAGES_ENDPOINT);
    }

    @Override
    public Message editMessage(long channelId, long messageId, EditMessageRequest request)
            throws ApiException {
        return endpoints.performPatch(messageFormatPath(channelId, messageId, SPECIFIC_MESSAGE_ENDPOINT_FMT),
                request,
                Message.class,
                SPECIFIC_MESSAGE_ENDPOINT);
    }

    @Override
    public void deleteMessage(long channelId, long messageId)
            throws ApiException {
        endpoints.performDelete(messageFormatPath(channelId, messageId, SPECIFIC_MESSAGE_ENDPOINT_FMT),
                Void.class,
                SPECIFIC_MESSAGE_ENDPOINT);
    }

    @Override
    public void bulkDeleteMessages(long channelId, BulkMessageDeleteRequest request)
            throws ApiException {
        if (!api.getSelf().isBot()) {
            throw new RequestRequiresBotStatusException(POST, MESSAGE_BULK_DELETE_ENDPOINT);
        }
        endpoints.performPost(channelFormatPath(channelId, MESSAGE_BULK_DELETE_ENDPOINT_FMT),
                request,
                Void.class,
                MESSAGE_BULK_DELETE_ENDPOINT);
    }

    @Override
    public void createReaction(long channelId, long messageId, Emoji emoji)
            throws ApiException {
        createReaction(channelId, messageId, emoji.getEmojiCode());
    }

    @Override
    public void createReaction(long channelId, long messageId, String emojiString)
            throws ApiException {
        String url = String.format(USER_REACTION_ENDPOINT_FMT,
            snowflakeToString(channelId),
            snowflakeToString(messageId),
            urlEncode(emojiString),
            SELF
        );
        endpoints.performPut(url,
            null,
            Void.class,
            USER_REACTION_ENDPOINT);
    }

    @Override
    public void deleteOwnReaction(long channelId, long messageId, Emoji emoji)
            throws ApiException {
        deleteOwnReaction(channelId, messageId, emoji.getEmojiCode());
    }

    @Override
    public void deleteOwnReaction(long channelId, long messageId, String emojiString)
            throws ApiException {
        String url = String.format(USER_REACTION_ENDPOINT_FMT,
            snowflakeToString(channelId),
            snowflakeToString(messageId),
            urlEncode(emojiString),
            SELF
        );
        endpoints.performDelete(url,
            Void.class,
            USER_REACTION_ENDPOINT);
    }

    @Override
    public void deleteUserReaction(long channelId, long messageId, Emoji emoji, long userId)
            throws ApiException {
        deleteUserReaction(channelId, messageId, emoji.getEmojiCode(), userId);
    }

    @Override
    public void deleteUserReaction(long channelId, long messageId, String emojiString, long userId)
            throws ApiException {
        String url = String.format(USER_REACTION_ENDPOINT_FMT,
            snowflakeToString(channelId),
            snowflakeToString(messageId),
            urlEncode(emojiString),
            snowflakeToString(userId)
        );
        endpoints.performDelete(url,
            Void.class,
            USER_REACTION_ENDPOINT);
    }

    @Override
    public HumanUser[] getUsersThatReacted(long channelId, long messageId, Emoji emoji)
            throws ApiException {
        return getUsersThatReacted(channelId, messageId, emoji.getEmojiCode());
    }

    @Override
    public HumanUser[] getUsersThatReacted(long channelId, long messageId, String emojiString)
            throws ApiException {
        String url = String.format(MESSAGE_REACTION_ENDPOINT_FMT,
            snowflakeToString(channelId),
            snowflakeToString(messageId),
            urlEncode(emojiString)
        );
        return endpoints.performGet(url,
            HumanUser[].class,
            MESSAGE_REACTION_ENDPOINT);
    }

    @Override
    public void deleteAllReactions(long channelId, long messageId)
            throws ApiException {
        String url = String.format(MESSAGE_REACTIONS_ENDPOINT_FMT,
            snowflakeToString(channelId),
            snowflakeToString(messageId)
        );
        endpoints.performDelete(url,
            Void.class,
            MESSAGE_REACTIONS_ENDPOINT);
    }

    @Override
    public void ackMessage(long channelId, long messageId)
            throws ApiException {
        if (api.getSelf().isBot()) {
            throw new InvalidApiRequestException(POST, MESSAGE_ACK_ENDPOINT, "Cannot ack as a bot");
        }
        endpoints.performPost(messageFormatPath(channelId, messageId, MESSAGE_ACK_ENDPOINT_FMT),
                null,
                Void.class,
                MESSAGE_ACK_ENDPOINT);
    }

    private String messageFormatPath(long channelId, long messageId, String format) {
        return String.format(format, snowflakeToString(channelId), snowflakeToString(messageId));
    }

    private String messageFormatPath(long messageId, String format) {
        return String.format(format, snowflakeToString(messageId));
    }

    private String channelFormatPath(long channelId, String format) {
        return String.format(format, snowflakeToString(channelId));
    }

    @Override
    public Future<Message[]> getMessagesAsync(long channelId, long beforeMessageId, long afterMessageId,
                                              long aroundMessageId, int limit)
            throws ApiException {
        return executorService.submit(() -> getMessages(channelId, beforeMessageId, afterMessageId,
                aroundMessageId, limit));
    }

    @Override
    public Future<Message> sendMessageAsync(long channelId, CreateMessageRequest request)
            throws ApiException {
        return executorService.submit(() -> sendMessage(channelId, request));
    }

    @Override
    public Future<Message> editMessageAsync(long channelId, long messageId, EditMessageRequest request)
            throws ApiException {
        return executorService.submit(() -> editMessage(channelId, messageId, request));
    }

    @Override
    public Future<Void> deleteMessageAsync(long channelId, long messageId)
            throws ApiException {
        return executorService.submit(() -> deleteMessage(channelId, messageId), null);
    }

    public Future<Void> bulkDeleteMessagesAsync(long channelId, BulkMessageDeleteRequest request)
            throws ApiException {
        return executorService.submit(() -> bulkDeleteMessages(channelId, request), null);
    }

    @Override
    public Future<Void> createReactionAsync(long channelId, long messageId, Emoji emoji)
            throws ApiException {
        return executorService.submit(() -> createReaction(channelId, messageId, emoji), null);
    }

    @Override
    public Future<Void> createReactionAsync(long channelId, long messageId, String emojiString)
            throws ApiException {
        return executorService.submit(() -> createReaction(channelId, messageId, emojiString), null);
    }

    @Override
    public Future<Void> deleteOwnReactionAsync(long channelId, long messageId, Emoji emoji)
            throws ApiException {
        return executorService.submit(() -> deleteOwnReaction(channelId, messageId, emoji), null);
    }

    @Override
    public Future<Void> deleteOwnReactionAsync(long channelId, long messageId, String emojiString)
            throws ApiException {
        return executorService.submit(() -> deleteOwnReaction(channelId, messageId, emojiString), null);
    }

    @Override
    public Future<Void> deleteUserReactionAsync(long channelId, long messageId, Emoji emoji, long userId)
            throws ApiException {
        return executorService.submit(() -> deleteUserReaction(channelId, messageId, emoji, userId), null);
    }

    @Override
    public Future<Void> deleteUserReactionAsync(long channelId, long messageId, String emojiString, long userId)
            throws ApiException {
        return executorService.submit(() -> deleteUserReaction(channelId, messageId, emojiString, userId), null);
    }

    @Override
    public Future<HumanUser[]> getUsersThatReactedAsync(long channelId, long messageId, Emoji emoji)
            throws ApiException {
        return executorService.submit(() -> getUsersThatReacted(channelId, messageId, emoji));
    }

    @Override
    public Future<HumanUser[]> getUsersThatReactedAsync(long channelId, long messageId, String emojiString)
            throws ApiException {
        return executorService.submit(() -> getUsersThatReacted(channelId, messageId, emojiString));
    }

    @Override
    public Future<Void> deleteAllReactionsAsync(long channelId, long messageId)
            throws ApiException {
        return executorService.submit(() -> deleteAllReactions(channelId, messageId), null);
    }


    @Override
    public Future<Void> ackMessageAsync(long channelId, long messageId)
            throws ApiException {
        return executorService.submit(() -> ackMessage(channelId, messageId), null);
    }

    @Override
    public Future<Message> getMessageAsync(long channelId, long messageId)
            throws ApiException {
        return executorService.submit(() -> getMessage(channelId, messageId));
    }
}
