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

import co.phoenixlab.discord.api.entities.guild.DiscordPermissionSet;
import co.phoenixlab.discord.api.enums.DiscordPermission;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.EnumSet;

public class DiscordPermissionTypeAdapter extends TypeAdapter<DiscordPermissionSet> {

    @Override
    public void write(JsonWriter out, DiscordPermissionSet value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        long bits = DiscordPermission.toLong(value.getValue());
        out.value(bits);
    }

    @Override
    public DiscordPermissionSet read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return new DiscordPermissionSet(EnumSet.noneOf(DiscordPermission.class));
        }
        long bits = 0xFFFFFFFFL & in.nextInt();
        return new DiscordPermissionSet(DiscordPermission.fromLong(bits));
    }
}
