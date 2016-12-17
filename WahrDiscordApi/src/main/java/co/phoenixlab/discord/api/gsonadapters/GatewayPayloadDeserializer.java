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

package co.phoenixlab.discord.api.gsonadapters;

import co.phoenixlab.discord.api.entities.*;
import co.phoenixlab.discord.api.entities.channel.*;
import co.phoenixlab.discord.api.entities.channel.message.DeletedMessage;
import co.phoenixlab.discord.api.entities.channel.message.Message;
import co.phoenixlab.discord.api.entities.channel.message.MessageDeleteBulkUpdate;
import co.phoenixlab.discord.api.entities.channel.message.MessageReactionUpdate;
import co.phoenixlab.discord.api.entities.guild.*;
import co.phoenixlab.discord.api.entities.user.SelfUser;
import co.phoenixlab.discord.api.entities.user.UserSettings;
import co.phoenixlab.discord.api.enums.GatewayOP;
import co.phoenixlab.discord.api.enums.WebSocketMessageType;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;

public class GatewayPayloadDeserializer implements JsonDeserializer<GatewayPayload> {

    private Logger LOGGER = LoggerFactory.getLogger(GatewayPayloadDeserializer.class);

    @Override
    public GatewayPayload deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        //  Check for error
        JsonElement errorElement = obj.get("message");
        if (errorElement != null) {
            return GatewayPayload.builder().
                    errorMessage(errorElement.getAsString()).
                    build();
        }
        //  Grab the opcode so we know what to do
        JsonElement opElement = obj.get("op");
        if (opElement == null) {
            throw new JsonParseException("Object is missing required field \"op\"");
        }
        GatewayOP op = GatewayOP.fromInt(opElement.getAsInt());
        //  Decode based on opcode
        switch (op) {
            case DISPATCH:
                //  Event dispatch, decode the type and sequence so we can decode
                JsonElement typeElement = obj.get("t");
                if (typeElement == null) {
                    throw new JsonParseException("Object is missing required field \"t\"");
                }
                String typeStr = typeElement.getAsString();
                JsonElement seqElement = obj.get("s");
                if (seqElement == null) {
                    throw new JsonParseException("Object is missing required field \"s\"");
                }
                //  Figure out the type
                WebSocketMessageType type = WebSocketMessageType.fromString(typeStr);
                //  Decode the body based on the type
                Object body = deserializeBody(obj.get("d").getAsJsonObject(), typeStr, type, context);
                return GatewayPayload.builder().
                    opCode(op).
                    sequenceNumber(seqElement.getAsInt()).
                    type(type).
                    data(body).
                    build();
            case HELLO:
                //  Websocket HELLO
                JsonElement helloElement = obj.get("d");
                if (helloElement == null) {
                    throw new JsonParseException("Object is missing required field \"d\"");
                }
                //  Decode
                GatewayHello hello = context.deserialize(helloElement, GatewayHello.class);
                return GatewayPayload.builder().
                    opCode(op).
                    data(hello).
                    build();
            default:
                //  Other opcodes don't have data associated with them
                return GatewayPayload.builder().
                    opCode(op).
                    build();
        }
    }

    private Object deserializeBody(JsonObject element, String typestr,
                                   WebSocketMessageType type, JsonDeserializationContext ctx) {
        Class<?> clazz;
        switch (type) {
            case RESUMED:
                clazz = GatewayResumed.class;
                break;
            case CHANNEL_CREATE:
            case CHANNEL_DELETE:
                clazz = Channel.class;
                break;
            case CHANNEL_UPDATE:
                clazz = GuildChannel.class;
                break;
            case GUILD_BAN_ADD:
            case GUILD_BAN_REMOVE:
                clazz = GuildBanAddRemoveUpdate.class;
                break;
            case GUILD_CREATE:
            case GUILD_UPDATE:
                clazz = Guild.class;
                break;
            case GUILD_DELETE:
                clazz = UnavailableGuild.class;
                break;
            case GUILD_EMOJI_UPDATE:
                clazz = GuildEmojisUpdate.class;
                break;
            case GUILD_INTEGRATIONS_UPDATE:
                clazz = GuildIntegrationsUpdate.class;
                break;
            case GUILD_MEMBER_ADD:
            case GUILD_MEMBER_REMOVE:
                clazz = GuildMemberAddRemoveUpdate.class;
                break;
            case GUILD_MEMBER_UPDATE:

            case GUILD_MEMBERS_CHUNK:
                clazz = GuildMembersChunk.class;
                break;
            case GUILD_ROLE_CREATE:
            case GUILD_ROLE_DELETE:
                clazz = RoleUpdate.class;
                break;
            case GUILD_ROLE_UPDATE:
                clazz = RoleDelete.class;
                break;
            case MESSAGE_ACK:
                clazz = MessageAck.class;
                break;
            case MESSAGE_CREATE:
            case MESSAGE_UPDATE:
                clazz = Message.class;
                break;
            case MESSAGE_DELETE:
                clazz = DeletedMessage.class;
                break;
            case MESSAGE_DELETE_BULK:
                clazz = MessageDeleteBulkUpdate.class;
                break;
            case MESSAGE_REACTION_ADD:
            case MESSAGE_REACTION_REMOVE:
                clazz = MessageReactionUpdate.class;
                break;
            case PRESENCE_UPDATE:
                clazz = Presence.class;
                break;
            case READY:
                clazz = ReadyMessage.class;
                break;
            case TYPING_START:
                clazz = TypingStart.class;
                break;
            case USER_UPDATE:
                clazz = SelfUser.class;
                break;
            case VOICE_SERVER_UPDATE:
                clazz = VoiceServerUpdate.class;
                break;
            case VOICE_STATE_UPDATE:
                clazz = VoiceState.class;
                break;
            case USER_SETTINGS_UPDATE:
                clazz = UserSettings.class;
                break;
            default:
                LOGGER.warn("Unknown event \"" + typestr + "\"");
                clazz = Map.class;
                break;
        }
        try {
            return ctx.deserialize(element, clazz);
        } catch (JsonSyntaxException jse) {
            LOGGER.warn("Failed to deserialize element \"{}\" of type {}", element.toString(), clazz.getName());
            throw jse;
        }
    }

}
