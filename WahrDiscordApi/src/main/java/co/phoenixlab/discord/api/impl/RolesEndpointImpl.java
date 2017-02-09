package co.phoenixlab.discord.api.impl;

import co.phoenixlab.discord.api.endpoints.RolesEndpoint;
import co.phoenixlab.discord.api.endpoints.async.RolesEndpointAsync;
import co.phoenixlab.discord.api.entities.guild.Guild;
import co.phoenixlab.discord.api.entities.guild.Role;
import co.phoenixlab.discord.api.exceptions.ApiException;
import co.phoenixlab.discord.api.request.guild.CreateEditRoleRequest;
import co.phoenixlab.discord.api.request.guild.RoleReorderRequestEntry;
import co.phoenixlab.discord.api.util.SnowflakeUtils;
import com.google.inject.Inject;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static co.phoenixlab.discord.api.util.SnowflakeUtils.snowflakeToString;

public class RolesEndpointImpl implements RolesEndpoint, RolesEndpointAsync {

    private static final String ROLES_ENDPOINT = "/guilds/{guild.id}/roles";
    private static final String ROLES_ENDPOINT_FMT = "/guilds/%1$s/roles";
    private static final String ROLE_ENDPOINT = "/guilds/{guild.id}/roles/{role.id}";
    private static final String ROLE_ENDPOINT_FMT = "/guilds/%1$s/roles/%2$s";

    @Inject
    private ScheduledExecutorService executorService;
    @Inject
    private EndpointsImpl endpoints;
    @Inject
    private WahrDiscordApiImpl api;

    @Override
    public Role createRole(long guildId, CreateEditRoleRequest request) throws ApiException {
        return endpoints.performPost(formatRole(guildId, ROLES_ENDPOINT_FMT),
                request,
                Role.class,
                ROLES_ENDPOINT
        );
    }

    @Override
    public Role editRole(long guildId, long roleId, CreateEditRoleRequest request) throws ApiException {
        return endpoints.performPatch(formatRole(guildId, roleId, ROLE_ENDPOINT_FMT),
                request,
                Role.class,
                ROLE_ENDPOINT
        );
    }

    @Override
    public Role[] reorderRoles(long guildId, RoleReorderRequestEntry[] request) throws ApiException {
        return endpoints.performPost(formatRole(guildId, ROLES_ENDPOINT_FMT),
                request,
                Role[].class,
                ROLES_ENDPOINT
        );
    }

    @Override
    public void deleteRole(long guildId, long roleId) throws ApiException {
        endpoints.performDelete(formatRole(guildId, roleId, ROLE_ENDPOINT_FMT),
                Void.class,
                ROLE_ENDPOINT
        );
    }

    @Override
    public Role[] getRoles(long guildId) throws ApiException {
        return endpoints.performGet(formatRole(guildId, ROLES_ENDPOINT_FMT),
                Role[].class,
                ROLES_ENDPOINT
        );
    }

    @Override
    public Future<Role> createRoleAsync(long guildId, CreateEditRoleRequest request) throws ApiException {
        return executorService.submit(() -> createRole(guildId, request));
    }

    @Override
    public Future<Role> editRoleAsync(long guildId, long roleId, CreateEditRoleRequest request) throws ApiException {
        return executorService.submit(() -> editRole(guildId, roleId, request));
    }

    @Override
    public Future<Role[]> reorderRolesAsync(long guildId, RoleReorderRequestEntry[] request) throws ApiException {
        return executorService.submit(() -> reorderRoles(guildId, request));
    }

    @Override
    public Future<Void> deleteRoleAsync(long guildId, long roleId) throws ApiException {
        return executorService.submit(() -> deleteRole(guildId, roleId), null);
    }

    @Override
    public Future<Role[]> getRolesAsync(long guildId) throws ApiException {
        return executorService.submit(() -> getRoles(guildId));
    }

    private static String formatRole(long guildId, String format) {
        return String.format(format, snowflakeToString(guildId));
    }

    private static String formatRole(long guildId, long roleId, String format) {
        return String.format(format, snowflakeToString(guildId), snowflakeToString(roleId));
    }
}
