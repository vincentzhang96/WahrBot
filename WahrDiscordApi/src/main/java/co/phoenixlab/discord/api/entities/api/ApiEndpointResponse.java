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

import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.exceptions.RateLimitExceededException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiEndpointResponse<T> {

    private enum ResponseType {
        SUCCESS(false),
        API_ERROR(true),
        RATE_LIMITED(true),
        GENERAL_EXCEPTION(true);

        private boolean isBad;

        ResponseType(boolean isBad) {
            this.isBad = isBad;
        }

        public boolean isBad() {
            return isBad;
        }
    };

    private ResponseType type;
    private ApiErrorResponse errorResponse;
    private RateLimitHeaders rateLimitHeaders;
    private RateLimitedResponse rateLimitedResponse;
    private Throwable exception;
    private T response;

    public static <T> ApiEndpointResponse<T> success(T t, RateLimitHeaders rateLimitHeaders) {
        ApiEndpointResponse<T> ret = new ApiEndpointResponse<>();
        ret.type = ResponseType.SUCCESS;
        ret.rateLimitHeaders = rateLimitHeaders;
        ret.response = t;
        return ret;
    }

    public static <T> ApiEndpointResponse<T> failedWithApiError(ApiErrorResponse errorResponse,
                                                                RateLimitHeaders rateLimitHeaders) {
        ApiEndpointResponse<T> ret = new ApiEndpointResponse<>();
        ret.type = ResponseType.API_ERROR;
        ret.errorResponse = errorResponse;
        ret.rateLimitHeaders = rateLimitHeaders;
        return ret;
    }

    public static <T> ApiEndpointResponse<T> failedWithRateLimited(RateLimitedResponse rateLimitedResponse,
                                                                   RateLimitHeaders rateLimitHeaders) {
        ApiEndpointResponse<T> ret = new ApiEndpointResponse<>();
        ret.type = ResponseType.RATE_LIMITED;
        ret.rateLimitedResponse = rateLimitedResponse;
        ret.rateLimitHeaders = rateLimitHeaders;
        return ret;
    }

    public static <T> ApiEndpointResponse<T> failedWithException(Throwable throwable) {
        ApiEndpointResponse<T> ret = new ApiEndpointResponse<>();
        ret.type = ResponseType.GENERAL_EXCEPTION;
        ret.exception = throwable;
        return ret;
    }

    public ApiEndpointResponse<T> onSuccess(Runnable onSuccess) {
        return onSuccess(consumeNone(onSuccess));
    }

    public ApiEndpointResponse<T> onSuccess(Consumer<T> onSuccess) {
        return onSuccess(consumeFirst(onSuccess));
    }

    public ApiEndpointResponse<T> onSuccess(BiConsumer<T, ApiEndpointResponse<T>> onSuccess) {
        if (type == ResponseType.SUCCESS) {
            onSuccess.accept(response, this);
        }
        return this;
    }

    public ApiEndpointResponse<T> onApiError(Runnable onApiError) {
        return onApiError(consumeNone(onApiError));
    }

    public ApiEndpointResponse<T> onApiError(Consumer<ApiErrorResponse> onApiError) {
        return onApiError(consumeFirst(onApiError));
    }

    public ApiEndpointResponse<T> onApiError(BiConsumer<ApiErrorResponse, ApiEndpointResponse<T>> onApiError) {
        if (type == ResponseType.API_ERROR) {
            onApiError.accept(errorResponse, this);
        }
        return this;
    }

    public ApiEndpointResponse<T> onRateLimited(Runnable onRateLimited) {
        return onRateLimited(consumeNone(onRateLimited));
    }

    public ApiEndpointResponse<T> onRateLimited(Consumer<RateLimitedResponse> onRateLimited) {
        return onRateLimited(consumeFirst(onRateLimited));
    }

    public ApiEndpointResponse<T> onRateLimited(BiConsumer<RateLimitedResponse, ApiEndpointResponse<T>> onRateLimited) {
        if (type == ResponseType.RATE_LIMITED) {
            onRateLimited.accept(rateLimitedResponse, this);
        }
        return this;
    }

    public ApiEndpointResponse<T> onGeneralException(Runnable onGeneralException) {
        return onGeneralException(consumeNone(onGeneralException));
    }

    public ApiEndpointResponse<T> onGeneralException(Consumer<Throwable> onGeneralException) {
        return onGeneralException(consumeFirst(onGeneralException));
    }

    public ApiEndpointResponse<T> onGeneralException(BiConsumer<Throwable, ApiEndpointResponse<T>> onGeneralException) {
        if (type == ResponseType.GENERAL_EXCEPTION) {
            onGeneralException.accept(exception, this);
        }
        return this;
    }

    public Optional<T> getResponse() {
        return Optional.ofNullable(response);
    }

    public RateLimitHeaders getRateLimitHeaders() {
        return rateLimitHeaders;
    }

    public Optional<RateLimitedResponse> getRateLimitedResponse() {
        return Optional.ofNullable(rateLimitedResponse);
    }

    public Optional<ApiErrorResponse> getApiErrorResponse() {
        return Optional.ofNullable(errorResponse);
    }

    public Optional<Throwable> getException() {
        return Optional.ofNullable(exception);
    }

    public T getOrThrow() throws ApiException {
        switch (type) {
            case SUCCESS:
                return response;
            case GENERAL_EXCEPTION:
                if (exception instanceof ApiException) {
                    throw (ApiException) exception;
                } else {
                    throw new ApiException(exception);
                }
            case RATE_LIMITED:
                throw new RateLimitExceededException(rateLimitHeaders.getRetryIn());
            case API_ERROR:
                throw new ApiException(String.format("%d: %s", errorResponse.getCode(), errorResponse.getMessage()));
        }
        throw new RuntimeException("This should be unreachable");
    }


    private static <T, U> BiConsumer<T, U> consumeFirst(Consumer<T> consumer) {
        return (t, u) -> consumer.accept(t);
    }

    private static <T, U> BiConsumer<T, U> consumeSecond(Consumer<U> consumer) {
        return (t, u) -> consumer.accept(u);
    }

    private static <T, U> BiConsumer<T, U> consumeNone(Runnable runnable) {
        return (t, u) -> runnable.run();
    }

}
