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

package co.phoenixlab.discord.api.endpoints;

import co.phoenixlab.discord.api.entities.channel.message.Message;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.request.channel.message.BulkMessageDeleteRequest;
import co.phoenixlab.discord.api.request.channel.message.EditMessageRequest;
import co.phoenixlab.discord.api.request.channel.message.CreateMessageRequest;

public interface MessagesEndpoint {

    long IGNORE = 0;
    int DEFAULT_LIMIT = 50;

    Message[] getMessages(long channelId, long beforeMessageId, long afterMessageId, int limit)
            throws ApiException;

    default Message[] getMessagesBefore(long channelId, long beforeMessageId, int limit)
            throws ApiException {
        return getMessages(channelId, beforeMessageId, IGNORE, limit);
    }

    default Message[] getMessagesBefore(long channelId, long beforeMessageId)
            throws ApiException {
        return getMessagesBefore(channelId, beforeMessageId, DEFAULT_LIMIT);
    }

    default Message[] getMessagesAfter(long channelId, long afterMessageId, int limit)
            throws ApiException {
        return getMessages(channelId, IGNORE, afterMessageId, limit);
    }

    default Message[] getMessagesAfter(long channelId, long afterMessageId)
            throws ApiException {
        return getMessagesAfter(channelId, afterMessageId, DEFAULT_LIMIT);
    }

    default Message[] getLatestMessages(long channelId, int limit)
            throws ApiException {
        return getMessages(channelId, IGNORE, IGNORE, limit);
    }

    default Message[] getLatestMessages(long channelId)
            throws ApiException {
        return getLatestMessages(channelId, DEFAULT_LIMIT);
    }

    Message sendMessage(long channelId, CreateMessageRequest request)
            throws ApiException;

    Message editMessage(long channelId, long messageId, EditMessageRequest request)
            throws ApiException;

    void deleteMessage(long channelId, long messageId)
            throws ApiException;

    void ackMessage(long channelId, long messageId)
            throws ApiException;

    void bulkDeleteMessages(long channelId, BulkMessageDeleteRequest request)
            throws ApiException;
}
