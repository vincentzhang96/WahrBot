package co.phoenixlab.discord.api.impl;

import co.phoenixlab.discord.api.endpoints.MessagesEndpoint;
import co.phoenixlab.discord.api.endpoints.async.MessagesEndpointAsync;
import co.phoenixlab.discord.api.entities.channel.message.Message;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.request.channel.message.BulkMessageDeleteRequest;
import co.phoenixlab.discord.api.request.channel.message.CreateMessageRequest;
import co.phoenixlab.discord.api.request.channel.message.EditMessageRequest;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpMethod;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static com.mashape.unirest.http.HttpMethod.*;

public class MessagesEndpointImpl implements MessagesEndpoint, MessagesEndpointAsync {



    private static final String MESSAGE_ACK_ENDPOINT = "/channels/{channel.id}/{message.id}/ack";
    private static final String MESSAGE_ACK_ENDPOINT_FMT = "/channels/%1$s/%2$s/ack";

    @Inject
    private ScheduledExecutorService executorService;
    @Inject
    private EndpointsImpl endpoints;
    @Inject
    private WahrDiscordApiImpl api;

    @Override
    public Message[] getMessages(long channelId, long beforeMessageId, long afterMessageId, int limit)
            throws ApiException {
        return new Message[0];
    }

    @Override
    public Message sendMessage(long channelId, CreateMessageRequest request)
            throws ApiException {
        return null;
    }

    @Override
    public Message editMessage(long channelId, long messageId, EditMessageRequest request)
            throws ApiException {
        return null;
    }

    @Override
    public void deleteMessage(long channelId, long messageId)
            throws ApiException {

    }

    @Override
    public void bulkDeleteMessages(long channelId, BulkMessageDeleteRequest request)
            throws ApiException {

    }

    @Override
    public void ackMessage(long channelId, long messageId)
            throws ApiException {
        if (api.getSelf().isBot()) {
            throw new ApiException(POST, MESSAGE_ACK_ENDPOINT, "Cannot ack as a bot");
        }
        try {
            endpoints.defaultPost(messageFormatPath(channelId, messageId, MESSAGE_ACK_ENDPOINT_FMT), null, Void.class);
        } catch (ApiException apie) {
            //  rethrow
            throw apie;
        } catch (Exception e) {
            throw new ApiException(POST, MESSAGE_ACK_ENDPOINT, e);
        }
    }

    private String messageFormatPath(long channelId, long messageId, String format) {
        return String.format(format, endpoints.snowflakeToString(channelId), endpoints.snowflakeToString(messageId));
    }

    private String messageFormatPath(long messageId, String format) {
        return String.format(format, endpoints.snowflakeToString(messageId));
    }

    @Override
    public Future<Message[]> getMessagesAsync(long channelId, long beforeMessageId, long afterMessageId, int limit)
            throws ApiException {
        return executorService.submit(() -> getMessages(channelId, beforeMessageId, afterMessageId, limit));
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
