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

package co.phoenixlab.discord.api.entities.channel;

import co.phoenixlab.discord.api.entities.guild.PermissionOverwrite;
import co.phoenixlab.discord.api.entities.guild.Guild;
import co.phoenixlab.discord.api.enums.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class GuildChannel extends Channel {

    /**
     * The ID of the {@link Guild} that this channel belongs to.
     */
    private long guildId;
    /**
     * An array of {@link PermissionOverwrite} overwrites.
     */
    private PermissionOverwrite[] permissionOverwrites;
    /**
     * The ordering position of the channel.
     */
    private int position;
    /**
     * The type of channel. Will be {@code text} for {@link GuildTextChannel}s and {@code voice} for
     * {@link GuildVoiceChannel}s.
     */
    private ChannelType type;

}
