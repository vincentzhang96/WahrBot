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
import co.phoenixlab.discord.api.entities.ReceivedMessage;
import co.phoenixlab.discord.api.request.EditMessageRequest;
import co.phoenixlab.discord.api.request.PostMessageRequest;

import java.util.concurrent.Future;

import static co.phoenixlab.discord.api.endpoints.MessagesEndpoint.DEFAULT_LIMIT;
import static co.phoenixlab.discord.api.endpoints.MessagesEndpoint.IGNORE;

public interface MessagesEndpointAsync {

    Future<ReceivedMessage[]> getMessages(long channelId, long beforeMessageId, long afterMessageId, int limit)
            throws ApiException;

    default Future<ReceivedMessage[]> getMessagesBefore(long channelId, long beforeMessageId, int limit)
            throws ApiException {
        return getMessages(channelId, beforeMessageId, IGNORE, limit);
    }

    default Future<ReceivedMessage[]> getMessagesBefore(long channelId, long beforeMessageId)
            throws ApiException {
        return getMessagesBefore(channelId, beforeMessageId, DEFAULT_LIMIT);
    }

    default Future<ReceivedMessage[]> getMessagesAfter(long channelId, long afterMessageId, int limit)
            throws ApiException {
        return getMessages(channelId, IGNORE, afterMessageId, limit);
    }

    default Future<ReceivedMessage[]> getMessagesAfter(long channelId, long afterMessageId)
            throws ApiException {
        return getMessagesAfter(channelId, afterMessageId, DEFAULT_LIMIT);
    }

    default Future<ReceivedMessage[]> getLatestMessages(long channelId, int limit)
            throws ApiException {
        return getMessages(channelId, IGNORE, IGNORE, limit);
    }

    default Future<ReceivedMessage[]> getLatestMessages(long channelId)
            throws ApiException {
        return getLatestMessages(channelId, DEFAULT_LIMIT);
    }

    Future<ReceivedMessage> sendMessage(long channelId, PostMessageRequest request)
            throws ApiException;

    Future<ReceivedMessage> editMessage(long channelId, long messageId, EditMessageRequest request)
            throws ApiException;

    Future<Void> deleteMessage(long channelId, long messageId)
            throws ApiException;

    Future<Void> ackMessage(long channelId, long messageId)
            throws ApiException;

}
