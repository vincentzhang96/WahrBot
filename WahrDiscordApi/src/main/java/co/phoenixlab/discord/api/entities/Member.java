package co.phoenixlab.discord.api.entities;

import lombok.Getter;

import java.time.Instant;

@Getter
public class Member implements Entity {

    private User user;
    private String[] roles;
    private Instant joinedAt;

    public Member() {
    }

    @Override
    public long getId() {
        return user.getId();
    }
}
