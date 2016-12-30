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

package co.phoenixlab.discord.api.entities.api;

import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
public class RateLimitHeaders {

    /**
     * In how many seconds can the request be retried.
     */
    private int retryIn;

    /**
     * If the rate limit headers returned are of the global rate limit.
     */
    private boolean global;

    /**
     * The number of requests that can be made.
     */
    private int limit;

    /**
     * The number of remaining requests that can be made.
     */
    private int remaining;

    /**
     * Time at which the rate limit resets
     */
    private Instant reset;

    public static RateLimitHeaders fromUnirestResponse(HttpResponse<?> response) {
        Headers h = response.getHeaders();
        RateLimitHeaders ret = new RateLimitHeaders();
        ret.global = h.containsKey("X-RateLimit-Global");
        ret.retryIn = Integer.parseInt(h.getFirst("Retry-After"));
        ret.limit = Integer.parseInt(h.getFirst("X-RateLimit-Limit"));
        ret.remaining = Integer.parseInt(h.getFirst("X-RateLimit-Remaining"));
        long epochTimeSec = Long.parseLong(h.getFirst("X-RateLimit-Reset"));
        ret.reset = Instant.ofEpochSecond(epochTimeSec);
        return ret;
    }

    /**
     * @return {@link #retryIn}
     */
    public int getRetryIn() {
        return retryIn;
    }

    /**
     * @return {@link #global}
     */
    public boolean isGlobal() {
        return global;
    }

    /**
     * @return {@link #limit}
     */
    public int getLimit() {
        return limit;
    }

    /**
     * @return {@link #remaining}
     */
    public int getRemaining() {
        return remaining;
    }

    /**
     * @return {@link #reset}
     */
    public Instant getReset() {
        return reset;
    }
}
