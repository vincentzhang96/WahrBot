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

public class ApiException extends RuntimeException {

    public ApiException() {
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String endpoint, String message) {
        super(createMessage(endpoint, message));
    }

    public ApiException(String endpoint, String message, Throwable throwable) {
        super(createMessage(endpoint, message), throwable);
    }

    public ApiException(String endpoint, Throwable throwable) {
        super(endpoint, throwable);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(HttpMethod method, String endpoint) {
        super(createMessage(method, endpoint));
    }

    public ApiException(HttpMethod method, String endpoint, String message) {
        super(createMessage(method, endpoint, message));
    }

    public ApiException(HttpMethod method, String endpoint, Throwable throwable) {
        super(createMessage(method, endpoint), throwable);
    }

    public ApiException(HttpMethod method, String endpoint, String message, Throwable throwable) {
        super(createMessage(method, endpoint, message), throwable);
    }

    private static String createMessage(HttpMethod method, String endpoint) {
        return method.name() + " " + endpoint;
    }

    private static String createMessage(HttpMethod method, String endpoint, String message) {
        return method.name() + " " + endpoint + " - " + message;
    }

    private static String createMessage(String endpoint, String message) {
        return endpoint + " - " + message;
    }
}
