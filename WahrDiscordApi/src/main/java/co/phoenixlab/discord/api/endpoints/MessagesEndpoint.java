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

import co.phoenixlab.discord.api.request.EditMessageRequest;
import co.phoenixlab.discord.api.request.PostMessageRequest;
import sun.plugin2.message.Message;

public interface MessagesEndpoint {

    Message[] getMessages(long channelId, long beforeMessageId, long afterMessageId, int limit)
            throws ApiException;

    Message[] getMessagesBefore(long channelId, long beforeMessageId, int limit)
            throws ApiException;

    Message[] getMessagesBefore(long channelId, long beforeMessageId)
            throws ApiException;

    Message[] getMessagesAfter(long channelId, long afterMessageId, int limit)
            throws ApiException;

    Message[] getMessagesAfter(long channelId, long afterMessageId)
            throws ApiException;

    Message[] getLatestMessages(long channelId, int limit)
            throws ApiException;

    Message[] getLatestMessages(long channelId)
            throws ApiException;

    Message sendMessage(long channelId, PostMessageRequest request)
            throws ApiException;

    Message editMessage(long channelId, long messageId, EditMessageRequest request)
            throws ApiException;

    void deleteMessage(long channelId, long messageId)
            throws ApiException;

    void ackMessage(long channelId, long messageId)
            throws ApiException;
}
