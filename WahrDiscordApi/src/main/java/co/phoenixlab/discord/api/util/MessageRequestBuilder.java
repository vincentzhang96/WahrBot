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

package co.phoenixlab.discord.api.util;

import co.phoenixlab.discord.api.entities.channel.GuildChannel;
import co.phoenixlab.discord.api.entities.guild.Emoji;
import co.phoenixlab.discord.api.entities.guild.Role;
import co.phoenixlab.discord.api.entities.user.BasicUser;
import co.phoenixlab.discord.api.request.channel.message.CreateMessageRequest;

import java.util.Arrays;
import java.util.EnumSet;

public class MessageRequestBuilder {

    private StringBuilder builder;
    private String nonce;
    private boolean tts;

    public MessageRequestBuilder() {
        builder = new StringBuilder();
        nonce = null;
        tts = false;
    }

    public MessageRequestBuilder text(String text) {
        builder.append(text);
        return this;
    }

    public MessageRequestBuilder newline() {
        builder.append('\n');
        return this;
    }

    public MessageRequestBuilder italicText(String text) {
        return formattedText(text, Format.ITALICS);
    }

    public MessageRequestBuilder boldText(String text) {
        return formattedText(text, Format.BOLD);
    }

    public MessageRequestBuilder underlineText(String text) {
        return formattedText(text, Format.UNDERLINE);
    }

    public MessageRequestBuilder strikethroughText(String text) {
        return formattedText(text, Format.STRIKETHROUGH);
    }

    public MessageRequestBuilder inlineCode(String code) {
        return formattedText(code, Format.INLINE_CODE);
    }

    public MessageRequestBuilder codeBlock(String code) {
        return formattedText(code, Format.CODE_BLOCK);
    }

    public MessageRequestBuilder codeBlock(String code, String language) {
        return formattedText(language + "\n" + code, Format.CODE_BLOCK);
    }

    public MessageRequestBuilder noPreviewUrl(String url) {
        return formattedText(url, Format.NO_PREVIEW);
    }

    public MessageRequestBuilder formattedText(String text, Format... formats) {
        EnumSet<Format> fmts;
        if (formats.length == 0) {
            fmts = EnumSet.noneOf(Format.class);
        } else {
            fmts = EnumSet.copyOf(Arrays.asList(formats));
        }
        Format[] sorted = fmts.toArray(new Format[fmts.size()]);
        Arrays.sort(sorted);
        for (int i = 0; i < sorted.length; i++) {
            builder.append(sorted[i].tag);
        }
        builder.append(text);
        for (int i = sorted.length - 1; i >= 0; i--) {
            builder.append(sorted[i].endTag);
        }
        return this;
    }

    public MessageRequestBuilder mentionUser(BasicUser user) {
        builder.append(String.format("<@%s>", user.getIdAsString()));
        return this;
    }

    public MessageRequestBuilder mentionNickname(BasicUser user) {
        builder.append(String.format("<@!%s>", user.getIdAsString()));
        return this;
    }

    public MessageRequestBuilder mentionChannel(GuildChannel channel) {
        builder.append(String.format("<#%s>", channel.getIdAsString()));
        return this;
    }

    public MessageRequestBuilder mentionRole(Role role) {
        builder.append(String.format("<@&%s>", role.getIdAsString()));
        return this;
    }

    public MessageRequestBuilder customEmoji(Emoji emoji) {
        builder.append(String.format("<:%s:%s>", emoji.getName(), emoji.getIdAsString()));
        return this;
    }

    public MessageRequestBuilder tts() {
        this.tts = true;
        return this;
    }

    public MessageRequestBuilder nonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    public String toString() {
        return builder.toString();
    }

    public String buildString() {
        return toString();
    }

    public CreateMessageRequest buildRequest() {
        return CreateMessageRequest.builder()
            .content(toString())
            .nonce(nonce)
            .tts(tts)
            .build();
    }

    public enum Format {
        ITALICS("*"),
        BOLD("**"),
        UNDERLINE("__"),
        STRIKETHROUGH("~~"),
        INLINE_CODE("`"),
        CODE_BLOCK("```"),
        NO_PREVIEW("<", ">");

        private final String tag;
        private final String endTag;

        Format(String tag) {
            this.tag = tag;
            this.endTag = tag;
        }

        Format(String tag, String endTag) {
            this.tag = tag;
            this.endTag = endTag;
        }
    }

}
