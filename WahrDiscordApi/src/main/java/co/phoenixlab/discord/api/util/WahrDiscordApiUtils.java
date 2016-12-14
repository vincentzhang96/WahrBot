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

import co.phoenixlab.discord.api.entities.GatewayPayload;
import co.phoenixlab.discord.api.entities.channel.Channel;
import co.phoenixlab.discord.api.entities.channel.GuildChannel;
import co.phoenixlab.discord.api.entities.user.BasicUser;
import co.phoenixlab.discord.api.gsonadapters.*;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;

public class WahrDiscordApiUtils {

    private WahrDiscordApiUtils() {

    }

    public static Gson createGson() {
        return new GsonBuilder().
                setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).
                setLongSerializationPolicy(LongSerializationPolicy.STRING).
                registerTypeAdapter(GatewayPayload.class, new GatewayPayloadDeserializer()).
                registerTypeAdapter(Instant.class, new InstantTypeAdapter()).
                registerTypeAdapter(Channel.class, new ChannelDeserializer()).
                registerTypeAdapter(GuildChannel.class, new GuildChannelDeserializer()).
                registerTypeAdapter(BasicUser.class, new BasicUserDeserializer()).
                create();
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Should never happen, Java always supports UTF-8 encoding");
        }
    }

}
