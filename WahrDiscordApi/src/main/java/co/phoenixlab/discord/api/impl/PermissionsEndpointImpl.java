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

package co.phoenixlab.discord.api.impl;

import co.phoenixlab.discord.api.endpoints.PermissionsEndpoint;
import co.phoenixlab.discord.api.endpoints.async.PermissionsEndpointAsync;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.request.channel.EditChannelPermissionsRequest;
import com.google.inject.Inject;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static co.phoenixlab.discord.api.util.SnowflakeUtils.snowflakeToString;

public class PermissionsEndpointImpl implements PermissionsEndpoint, PermissionsEndpointAsync {

    private static final String CHANNEL_PERMISSIONS_ENDPOINT = "/channels/{channel.id}/permissions/{overwrite.id}";
    private static final String CHANNEL_PERMISSIONS_ENDPOINT_FMT = "/channels/%1$s/permissions/%2$s";

    @Inject
    private ScheduledExecutorService executorService;
    @Inject
    private EndpointsImpl endpoints;
    @Inject
    private WahrDiscordApiImpl api;

    @Override
    public void editChannelPermission(long channelId, EditChannelPermissionsRequest request)
        throws ApiException {
        String url = String.format(CHANNEL_PERMISSIONS_ENDPOINT_FMT,
            snowflakeToString(channelId),
            snowflakeToString(request.getId())
        );
        endpoints.performPut(url,
            request,
            Void.class,
            CHANNEL_PERMISSIONS_ENDPOINT
        );
    }

    @Override
    public void deleteChannelPermission(long channelId, long overwriteId)
        throws ApiException {
        String url = String.format(CHANNEL_PERMISSIONS_ENDPOINT_FMT,
            snowflakeToString(channelId),
            snowflakeToString(overwriteId)
        );
        endpoints.performDelete(url,
            Void.class,
            CHANNEL_PERMISSIONS_ENDPOINT
        );
    }

    @Override
    public Future<Void> editChannelPermissionAsync(long channelId, EditChannelPermissionsRequest request)
        throws ApiException {
        return executorService.submit(() -> editChannelPermission(channelId, request), null);
    }

    @Override
    public Future<Void> deleteChannelPermissionAsync(long channelId, long overwriteId)
        throws ApiException {
        return executorService.submit(() -> deleteChannelPermission(channelId, overwriteId), null);
    }

}
