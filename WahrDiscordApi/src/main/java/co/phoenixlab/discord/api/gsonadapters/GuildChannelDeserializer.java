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

package co.phoenixlab.discord.api.gsonadapters;

import co.phoenixlab.discord.api.entities.channel.GuildChannel;
import co.phoenixlab.discord.api.entities.channel.GuildTextChannel;
import co.phoenixlab.discord.api.entities.channel.GuildVoiceChannel;
import co.phoenixlab.discord.api.enums.ChannelType;
import com.google.gson.*;

import java.lang.reflect.Type;

public class GuildChannelDeserializer implements JsonDeserializer<GuildChannel> {

    @Override
    public GuildChannel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        JsonElement typeElement = obj.get("type");
        if (typeElement == null) {
            throw new JsonParseException("Object is missing required field \"type\"");
        }
        ChannelType type = context.deserialize(typeElement, ChannelType.class);
        switch(type) {
            case TEXT:
                return context.deserialize(obj, GuildTextChannel.class);
            case VOICE:
                return context.deserialize(obj, GuildVoiceChannel.class);
            default:
                throw new JsonParseException("Unknown channel type: " + typeElement.toString());
        }
    }
}
