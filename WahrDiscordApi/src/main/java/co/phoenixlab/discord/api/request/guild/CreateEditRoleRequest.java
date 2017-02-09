package co.phoenixlab.discord.api.request.guild;

import co.phoenixlab.discord.api.entities.guild.DiscordPermissionSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateEditRoleRequest {

    private String name;

    private DiscordPermissionSet permissions;

    private int color;

    private Boolean hoist;

    private Boolean mentionable;

}
