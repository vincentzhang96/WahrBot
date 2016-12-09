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

package co.phoenixlab.discord.api.exceptions;

import com.mashape.unirest.http.HttpMethod;

public class RequestRequiresBotStatusException extends InvalidApiRequestException {

    public RequestRequiresBotStatusException() {
    }

    public RequestRequiresBotStatusException(Throwable cause) {
        super(cause);
    }

    public RequestRequiresBotStatusException(String endpoint, String message) {
        super(endpoint, message);
    }

    public RequestRequiresBotStatusException(String endpoint, String message, Throwable throwable) {
        super(endpoint, message, throwable);
    }

    public RequestRequiresBotStatusException(String endpoint, Throwable throwable) {
        super(endpoint, throwable);
    }

    public RequestRequiresBotStatusException(String message) {
        super(message);
    }

    public RequestRequiresBotStatusException(HttpMethod method, String endpoint) {
        super(method, endpoint);
    }

    public RequestRequiresBotStatusException(HttpMethod method, String endpoint, String message) {
        super(method, endpoint, message);
    }

    public RequestRequiresBotStatusException(HttpMethod method, String endpoint, String message, Throwable throwable) {
        super(method, endpoint, message, throwable);
    }

    public RequestRequiresBotStatusException(HttpMethod method, String endpoint, Throwable throwable) {
        super(method, endpoint, throwable);
    }
}
