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

    long MESSAGE_ID_IGNORE_PARAM = 0;
    int LIMIT_DEFAULT = 50;
    int LIMIT_MIN = 1;
    int LIMIT_MAX = 100;

    Message[] getMessages(long channelId, long beforeMessageId, long afterMessageId,
                          long aroundMessageId, int limit)
            throws ApiException;

    default Message[] getMessages(long channelId, long beforeMessageId, long afterMessageId, int limit)
            throws ApiException {
        return getMessages(channelId, beforeMessageId, afterMessageId, MESSAGE_ID_IGNORE_PARAM, limit);
    }

    default Message[] getMessagesBefore(long channelId, long beforeMessageId, int limit)
            throws ApiException {
        return getMessages(channelId, beforeMessageId, MESSAGE_ID_IGNORE_PARAM, limit);
    }

    default Message[] getMessagesBefore(long channelId, long beforeMessageId)
            throws ApiException {
        return getMessagesBefore(channelId, beforeMessageId, LIMIT_DEFAULT);
    }

    default Message[] getMessagesAfter(long channelId, long afterMessageId, int limit)
            throws ApiException {
        return getMessages(channelId, MESSAGE_ID_IGNORE_PARAM, afterMessageId, limit);
    }

    default Message[] getMessagesAfter(long channelId, long afterMessageId)
            throws ApiException {
        return getMessagesAfter(channelId, afterMessageId, LIMIT_DEFAULT);
    }

    default Message[] getLatestMessages(long channelId, int limit)
            throws ApiException {
        return getMessages(channelId, MESSAGE_ID_IGNORE_PARAM, MESSAGE_ID_IGNORE_PARAM, limit);
    }

    default Message[] getLatestMessages(long channelId)
            throws ApiException {
        return getLatestMessages(channelId, LIMIT_DEFAULT);
    }

    default Message[] getMessagesAround(long channelId, long aroundMessageId, int limit)
            throws ApiException {
        return getMessages(channelId, MESSAGE_ID_IGNORE_PARAM, MESSAGE_ID_IGNORE_PARAM, aroundMessageId, limit);
    }

    default Message[] getMessagesAround(long channelId, long aroundMessageId)
            throws ApiException {
        return getMessagesAround(channelId, aroundMessageId, LIMIT_DEFAULT);
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
