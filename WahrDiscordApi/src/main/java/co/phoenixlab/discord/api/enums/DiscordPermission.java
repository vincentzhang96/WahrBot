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

package co.phoenixlab.discord.api.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum DiscordPermission {
    GEN_CREATE_INSTANT_INVITE(0),
    GEN_KICK_MEMBERS(1),
    GEN_BAN_MEMBERS(2),
    GEN_MANAGE_ROLES(3),
    GEN_MANAGE_PERMISSIONS(4),
    GEN_MANAGE_CHANNELS(5),
    GEN_MANAGE_SERVER(6),
    CHAT_READ_MESSAGES(10),
    CHAT_SEND_MESSAGES(11),
    CHAT_SEND_TTS_MESSAGES(12),
    CHAT_MANAGE_MESSAGES(13),
    CHAT_EMBED_LINKS(14),
    CHAT_ATTACH_FILES(15),
    CHAT_READ_MESSAGE_HISTORY(16),
    CHAT_MENTION_EVERYONE(17),
    VOICE_CONNECT(20),
    VOICE_SPEAK(21),
    VOICE_MUTE_MEMBERS(22),
    VOICE_DEAFEN_MEMBERS(23),
    VOICE_MOVE_MEMBERS(24),
    VOICE_USE_VOICE_ACTIVITY_DETECTION(25);

    private final int bitNum;

    DiscordPermission(int bitNum) {
        this.bitNum = bitNum;
    }

    public int getBitNum() {
        return bitNum;
    }

    public long toLong() {
        return 1L << bitNum;
    }

    public boolean test(long l) {
        return (l & toLong()) != 0;
    }

    public static EnumSet<DiscordPermission> fromLong(long l) {
        return Arrays.stream(values()).
                filter(p -> p.test(l)).
                collect(Collectors.toCollection(() -> EnumSet.noneOf(DiscordPermission.class)));
    }

    public static long toLong(Set<DiscordPermission> set) {
        long accum = 0L;
        for (DiscordPermission permission : set) {
            accum |= permission.toLong();
        }
        return accum;
    }
}
