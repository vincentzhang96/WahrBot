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


import co.phoenixlab.discord.api.util.SnowflakeUtils;

import java.util.Objects;

/**
 * A uniquely identifiable entity from Discord
 */
public interface Entity {

    long getId();

    default String getIdAsString() {
        return Long.toUnsignedString(getId());
    }

    default String getBase64EncodedId() {
        return encodeId(getId());
    }

    static String encodeId(long l) {
        return SnowflakeUtils.encodeSnowflake(l);
    }

    static long decodeId(String id) throws IllegalArgumentException {
        return SnowflakeUtils.decodeSnowflake(id);
    }

    /**
     * Checks if two objects are entities and the entities are of the same type
     * and have the same ID.
     * @param a The first entity to check for equality. Can be null.
     * @param b The second entity to check for equality. Can be null.
     * @return True iff both objects are entities, same kind of entity, and have the same ID.
     */
    static boolean areEqual(Object a, Object b) {
        return a instanceof Entity && b instanceof Entity &&
                (a == b || a.getClass() == b.getClass() &&
                        ((Entity) a).getId() == ((Entity) b).getId());
    }

    static int hash(Entity a) {
        if (a == null) {
            return 0;
        }
        return Objects.hash(a.getId());
    }

}
