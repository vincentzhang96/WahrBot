package co.phoenixlab.discord.api.entities;

import lombok.Getter;

@Getter
public class User implements Entity {

    private long id;
    private String username;
    private short discriminator;
    private String avatar;
    private boolean isBot;

    public User() {
    }

}
