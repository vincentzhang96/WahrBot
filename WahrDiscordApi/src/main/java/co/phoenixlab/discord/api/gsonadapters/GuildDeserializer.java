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

import co.phoenixlab.discord.api.entities.guild.CreatedGuild;
import co.phoenixlab.discord.api.entities.guild.Guild;
import co.phoenixlab.discord.api.entities.guild.UnavailableGuild;
import co.phoenixlab.discord.api.entities.guild.UserGuild;
import com.google.gson.*;

import java.lang.reflect.Type;

public class GuildDeserializer implements JsonDeserializer<UnavailableGuild> {

    public static GsonBuilder register(GsonBuilder builder) {
        GuildDeserializer d = new GuildDeserializer();
        return builder.registerTypeAdapter(UnavailableGuild.class, d)
            .registerTypeAdapter(UserGuild.class, d)
            .registerTypeAdapter(Guild.class, d);
    }

    @Override
    public UnavailableGuild deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        JsonElement nameElement = obj.get("name");
        if (nameElement == null) {
            JsonElement membersElement = obj.get("members");
            if (membersElement != null) {
                return context.deserialize(obj, CreatedGuild.class);
            } else {
                JsonElement ownerElement = obj.get("owner");
                if (ownerElement != null) {
                    return context.deserialize(obj, UserGuild.class);
                } else {
                    return context.deserialize(obj, Guild.class);
                }
            }
        } else {
            JsonElement unavailableElement = obj.get("unavailable");
            if (unavailableElement != null && unavailableElement.getAsBoolean()) {
                return context.deserialize(obj, UnavailableGuild.class);
            } else {
                throw new JsonParseException("Not a valid guild object");
            }
        }
    }
}
