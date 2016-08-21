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

package co.phoenixlab.discord.api.endpoints.async;

import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.entities.channel.message.Message;
import co.phoenixlab.discord.api.request.channel.message.BulkMessageDeleteRequest;
import co.phoenixlab.discord.api.request.channel.message.EditMessageRequest;
import co.phoenixlab.discord.api.request.channel.message.CreateMessageRequest;

import java.util.concurrent.Future;

import static co.phoenixlab.discord.api.endpoints.MessagesEndpoint.LIMIT_DEFAULT;
import static co.phoenixlab.discord.api.endpoints.MessagesEndpoint.MESSAGE_ID_IGNORE_PARAM;

public interface MessagesEndpointAsync {

    Future<Message[]> getMessagesAsync(long channelId, long beforeMessageId, long afterMessageId,
                                       long aroundMessageId, int limit)
            throws ApiException;

    default Future<Message[]> getMessagesAsync(long channelId, long beforeMessageId, long afterMessageId, int limit)
            throws ApiException {
        return getMessagesAsync(channelId, beforeMessageId, afterMessageId, MESSAGE_ID_IGNORE_PARAM, limit);
    }

    default Future<Message[]> getMessagesBeforeAsync(long channelId, long beforeMessageId, int limit)
            throws ApiException {
        return getMessagesAsync(channelId, beforeMessageId, MESSAGE_ID_IGNORE_PARAM, limit);
    }

    default Future<Message[]> getMessagesBeforeAsync(long channelId, long beforeMessageId)
            throws ApiException {
        return getMessagesBeforeAsync(channelId, beforeMessageId, LIMIT_DEFAULT);
    }

    default Future<Message[]> getMessagesAfterAsync(long channelId, long afterMessageId, int limit)
            throws ApiException {
        return getMessagesAsync(channelId, MESSAGE_ID_IGNORE_PARAM, afterMessageId, limit);
    }

    default Future<Message[]> getMessagesAfterAsync(long channelId, long afterMessageId)
            throws ApiException {
        return getMessagesAfterAsync(channelId, afterMessageId, LIMIT_DEFAULT);
    }

    default Future<Message[]> getLatestMessagesAsync(long channelId, int limit)
            throws ApiException {
        return getMessagesAsync(channelId, MESSAGE_ID_IGNORE_PARAM, MESSAGE_ID_IGNORE_PARAM, limit);
    }

    default Future<Message[]> getLatestMessagesAsync(long channelId)
            throws ApiException {
        return getLatestMessagesAsync(channelId, LIMIT_DEFAULT);
    }

    default Future<Message[]> getMessagesAroundAsync(long channelId, long aroundMessageId, int limit)
            throws ApiException {
        return getMessagesAsync(channelId, MESSAGE_ID_IGNORE_PARAM, MESSAGE_ID_IGNORE_PARAM, aroundMessageId, limit);
    }

    default Future<Message[]> getMessagesAroundAsync(long channelId, long aroundMessageId)
            throws ApiException {
        return getMessagesAroundAsync(channelId, aroundMessageId, LIMIT_DEFAULT);
    }

    Future<Message> sendMessageAsync(long channelId, CreateMessageRequest request)
            throws ApiException;

    Future<Message> editMessageAsync(long channelId, long messageId, EditMessageRequest request)
            throws ApiException;

    Future<Void> deleteMessageAsync(long channelId, long messageId)
            throws ApiException;

    Future<Void> ackMessageAsync(long channelId, long messageId)
            throws ApiException;

    Future<Void> bulkDeleteMessagesAsync(long channelId, BulkMessageDeleteRequest request)
            throws ApiException;
}
