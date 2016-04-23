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

package co.phoenixlab.discord.api.request;

import co.phoenixlab.discord.api.enums.GatewayOP;
import co.phoenixlab.discord.api.request.channel.VoiceStateUpdateRequest;
import co.phoenixlab.discord.api.request.guild.GuildMembersRequest;
import co.phoenixlab.discord.api.request.guild.UpdateStatusRequest;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WSRequest {

    @SerializedName("op")
    private GatewayOP opCode;
    @SerializedName("d")
    private Object data;


    public static WSRequest payload(GatewayOP op, Object request) {
        return WSRequest.builder().
                opCode(op).
                data(request).
                build();
    }

    public static WSRequest heartbeat(int lastSeqNum) {
        return payload(GatewayOP.HEARTBEAT, lastSeqNum);
    }

    public static WSRequest identify(ConnectRequest request) {
        return payload(GatewayOP.IDENTIFY, request);
    }

    public static WSRequest statusUpdate(UpdateStatusRequest request) {
        return payload(GatewayOP.STATUS_UPDATE, request);
    }

    public static WSRequest voiceStateUpdate(VoiceStateUpdateRequest request) {
        return payload(GatewayOP.VOICE_STATE_UPDATE, request);
    }

    public WSRequest resume(GatewayResumeRequest request) {
        return payload(GatewayOP.RESUME, request);
    }

    public WSRequest requestGuildMembers(GuildMembersRequest request) {
        return payload(GatewayOP.REQUEST_GUILD_MEMBERS, request);
    }
}
