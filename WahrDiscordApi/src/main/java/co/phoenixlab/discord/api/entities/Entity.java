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

package co.phoenixlab.discord.api.entities;


import com.google.common.primitives.Longs;

import java.util.Base64;

/**
 * A uniquely identifiable entity from Discord
 */
public interface Entity {

    long getId();

    default String getBase64EncodedId() {
        return encodeId(getId());
    }

    static String encodeId(long l) {
        return "$" + Base64.getUrlEncoder().encodeToString(Longs.toByteArray(l));
    }

    static long decodeId(String id) throws IllegalArgumentException {
        if (!id.startsWith("$")) {
            throw new IllegalArgumentException("Not a valid encoded ID token: " + id);
        }
        id = id.substring(1);
        return Longs.fromByteArray(Base64.getUrlDecoder().decode(id));
    }

}
