package co.phoenixlab.discord.api.impl;

import co.phoenixlab.discord.api.endpoints.MessagesEndpoint;
import co.phoenixlab.discord.api.endpoints.async.MessagesEndpointAsync;
import co.phoenixlab.discord.api.entities.channel.message.Message;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.exceptions.InvalidApiRequestException;
import co.phoenixlab.discord.api.request.channel.message.BulkMessageDeleteRequest;
import co.phoenixlab.discord.api.request.channel.message.CreateMessageRequest;
import co.phoenixlab.discord.api.request.channel.message.EditMessageRequest;
import com.google.inject.Inject;

import java.util.StringJoiner;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static com.mashape.unirest.http.HttpMethod.GET;
import static com.mashape.unirest.http.HttpMethod.POST;

public class MessagesEndpointImpl implements MessagesEndpoint, MessagesEndpointAsync {

    private static final String MESSAGES_ENDPOINT = "/channels/{channel.id}/messages";
    private static final String MESSAGES_ENDPOINT_FMT = "/channels/%s/messages";
    private static final String SPECIFIC_MESSAGE_ENDPOINT = "/channels/{channel.id}/{message.id}";
    private static final String SPECIFIC_MESSAGE_ENDPOINT_FMT = "/channels/%1$s/%2$s";
    private static final String MESSAGE_ACK_ENDPOINT = "/channels/{channel.id}/{message.id}/ack";
    private static final String MESSAGE_ACK_ENDPOINT_FMT = "/channels/%1$s/%2$s/ack";

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
            joiner.add(String.format("before=%s", endpoints.snowflakeToString(beforeMessageId)));
        } else if (afterMessageId != MESSAGE_ID_IGNORE_PARAM) {
            joiner.add(String.format("after=%s", endpoints.snowflakeToString(afterMessageId)));
        } else if (aroundMessageId != MESSAGE_ID_IGNORE_PARAM) {
            joiner.add(String.format("around=%s", endpoints.snowflakeToString(aroundMessageId)));
        }
        if (limit != LIMIT_DEFAULT) {
            joiner.add(String.format("limit=%d", limit));
        }
        return performGet(messageFormatPath(channelId, MESSAGES_ENDPOINT_FMT) + joiner.toString(),
                Message[].class,
                MESSAGES_ENDPOINT);
    }

    @Override
    public Message sendMessage(long channelId, CreateMessageRequest request)
            throws ApiException {
        return performPost(messageFormatPath(channelId, MESSAGES_ENDPOINT_FMT),
                request,
                Message.class,
                MESSAGES_ENDPOINT);
    }

    @Override
    public Message editMessage(long channelId, long messageId, EditMessageRequest request)
            throws ApiException {
        return performPatch(messageFormatPath(channelId, messageId, SPECIFIC_MESSAGE_ENDPOINT_FMT),
                request,
                Message.class,
                SPECIFIC_MESSAGE_ENDPOINT);
    }

    @Override
    public void deleteMessage(long channelId, long messageId)
            throws ApiException {
        performDelete(messageFormatPath(channelId, messageId, SPECIFIC_MESSAGE_ENDPOINT_FMT),
                Void.class,
                SPECIFIC_MESSAGE_ENDPOINT);
    }

    @Override
    public void bulkDeleteMessages(long channelId, BulkMessageDeleteRequest request)
            throws ApiException {

    }

    @Override
    public void ackMessage(long channelId, long messageId)
            throws ApiException {
        if (api.getSelf().isBot()) {
            throw new InvalidApiRequestException(POST, MESSAGE_ACK_ENDPOINT, "Cannot ack as a bot");
        }
        performPost(messageFormatPath(channelId, messageId, MESSAGE_ACK_ENDPOINT_FMT),
                null,
                Void.class,
                MESSAGE_ACK_ENDPOINT);
    }

    private <T> T performPost(String path, Object body, Class<T> clazz, String endpoint)
            throws ApiException {
        try {
            return endpoints.defaultPost(path, body, clazz);
        } catch (ApiException apie) {
            //  rethrow
            throw apie;
        } catch (Exception e) {
            throw new ApiException(POST, endpoint, e);
        }
    }

    private <T> T performGet(String path, Class<T> clazz, String endpoint)
            throws ApiException {
        try {
            return endpoints.defaultGet(path, clazz);
        } catch (ApiException apie) {
            //  rethrow
            throw apie;
        } catch (Exception e) {
            throw new ApiException(GET, endpoint, e);
        }
    }

    private <T> T performPatch(String path, Object body, Class<T> clazz, String endpoint)
            throws ApiException {
        try {
            return endpoints.defaultPatch(path, body, clazz);
        } catch (ApiException apie) {
            //  rethrow
            throw apie;
        } catch (Exception e) {
            throw new ApiException(POST, endpoint, e);
        }
    }

    private <T> T performDelete(String path, Class<T> clazz, String endpoint)
            throws ApiException {
        try {
            return endpoints.defaultDelete(path, clazz);
        } catch (ApiException apie) {
            //  rethrow
            throw apie;
        } catch (Exception e) {
            throw new ApiException(POST, endpoint, e);
        }
    }

    private String messageFormatPath(long channelId, long messageId, String format) {
        return String.format(format, endpoints.snowflakeToString(channelId), endpoints.snowflakeToString(messageId));
    }

    private String messageFormatPath(long messageId, String format) {
        return String.format(format, endpoints.snowflakeToString(messageId));
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
    public Future<Void> ackMessageAsync(long channelId, long messageId)
            throws ApiException {
        return executorService.submit(() -> ackMessage(channelId, messageId), null);
    }
}
