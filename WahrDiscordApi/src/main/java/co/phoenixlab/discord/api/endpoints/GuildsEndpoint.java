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

package co.phoenixlab.discord.api.endpoints;

import co.phoenixlab.discord.api.entities.guild.Guild;
import co.phoenixlab.discord.api.entities.guild.UserGuild;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.request.guild.CreateGuildRequest;
import co.phoenixlab.discord.api.request.guild.EditGuildRequest;
import co.phoenixlab.discord.api.request.guild.GuildMembersRequest;

public interface GuildsEndpoint {

    Guild createGuild(CreateGuildRequest request)
            throws ApiException;

    Guild editGuild(long guildId, EditGuildRequest request)
            throws ApiException;

    void leaveGuild(long guildId)
            throws ApiException;

    Guild deleteGuild(long guildId)
            throws ApiException;

    Guild getGuild(long guildId)
            throws ApiException;

    UserGuild[] getGuilds()
            throws ApiException;

    void requestMembers(GuildMembersRequest request)
            throws ApiException;


}
