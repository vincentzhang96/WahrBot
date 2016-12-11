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

public class SnowflakeUtils {

    public static final String CODEX = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final int BASE = CODEX.length();

    private SnowflakeUtils() {}

    public static long decodeSnowflake(String encoded) {
        if (!encoded.startsWith("$")) {
            throw new IllegalArgumentException("Not a valid encoded snowflake: " + encoded);
        }
        encoded = encoded.substring(1);
        return parse(encoded);
    }

    public static String encodeSnowflake(long snowflake) {
        return "$" + encode(snowflake);
    }

    private static long parse(String r62) {
        char[] chars = r62.toCharArray();
        int length = chars.length;
        long ret = 0;
        //  Read the number from right to left (smallest place to largest)
        //  WARNING: NO UNDER/OVERFLOW CHECKING IS DONE
        int lenLessOne = length - 1;
        for (int i = lenLessOne; i >= 0; i--) {
            long digit = charToDigit(chars[i]);
            int placeValue = lenLessOne - i;
            long addnum = digit * (long) Math.pow(BASE, placeValue);
            ret += addnum;
        }
        return ret;
    }

    private static String encode(long l) {
        long accum = l;
        StringBuilder builder = new StringBuilder();
        long remainder;
        while (Long.compareUnsigned(accum, 0) > 0) {
            long last = accum;
            accum = accum / BASE;
            remainder = last - (accum * BASE);
            builder.append(digitToChar(Math.abs((int) remainder)));
        }
        String ret = builder.reverse().toString();
        //  Strip leading zeros
        for (int i = 0; i < ret.length(); i++) {
            if (ret.charAt(i) != '0') {
                ret = ret.substring(i);
                break;
            }
        }
        return ret;
    }

    private static char digitToChar(int i) {
        return CODEX.charAt(i);
    }

    private static int charToDigit(char c) {
        return CODEX.indexOf(c);
    }

    /**
     * Converts a snowflake int64 into the proper string representation for Discord (unsigned decimal)
     * @param snowflake The snowflake ID to convert
     * @return The snowflake in string format
     */
    public static String snowflakeToString(long snowflake) {
        return Long.toUnsignedString(snowflake);
    }

}
