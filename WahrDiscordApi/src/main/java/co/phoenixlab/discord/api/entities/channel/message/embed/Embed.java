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

package co.phoenixlab.discord.api.entities.channel.message.embed;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Embed {

    /**
     * Title of embed.
     */
    private String title;
    /**
     * Type of embed.
     */
    private String type;
    /**
     * Description of embed.
     */
    private String description;
    /**
     * URL of embed.
     */
    private String url;
    /**
     * Timestamp of embed content.
     */
    private Instant timestamp;
    /**
     * Color code of the embed.
     */
    private int color;
    /**
     * Footer information.
     */
    private EmbedFooter footer;
    /**
     * Image information.
     */
    private EmbedImage image;
    /**
     * Thumbnail information.
     */
    private EmbedThumbnail thumbnail;
    /**
     * Video information.
     */
    private EmbedVideo video;
    /**
     * Provider information.
     */
    private EmbedProvider provider;
    /**
     * Author information.
     */
    private EmbedAuthor author;
    /**
     * Fields information.
     */
    private EmbedField[] fields;

}
