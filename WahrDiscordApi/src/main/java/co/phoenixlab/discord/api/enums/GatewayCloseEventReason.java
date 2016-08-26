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

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Optional;

public enum GatewayCloseEventReason {
    UNKNOWN_ERROR(4000),
    UNKNOWN_OPCODE(4001),
    DECODE_ERROR(4002),
    NOT_AUTHENTICATED(4003),
    AUTHENTICATION_FAILED(4004),
    ALREADY_AUTHENTICATED(4005),
    INVALID_SEQUENCE_NUMBER(4007),
    RATE_LIMITED(4008),
    SESSION_TIMEOUT(4009),
    INVALID_SHARD(4010),
    UNKNOWN_CODE(-1);

    private int code;

    GatewayCloseEventReason(int code) {
        this.code = code;
    }

    private static final TIntObjectHashMap<GatewayCloseEventReason> cache;

    static {
        cache = new TIntObjectHashMap<>();
        for (GatewayCloseEventReason gatewayCloseEventReason : values()) {
            cache.put(gatewayCloseEventReason.code, gatewayCloseEventReason);
        }
    }

    public int getCode() {
        return code;
    }

    public static GatewayCloseEventReason fromCode(int code) {
        return Optional.ofNullable(cache.get(code)).
            orElse(UNKNOWN_CODE);
    }

}
