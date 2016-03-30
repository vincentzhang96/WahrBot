package co.phoenixlab.discord.api.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User implements Entity {

    private long id;
    private String username;
    private short discriminator;
    private String avatar;
    private boolean bot;

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return Entity.areEqual(this, o);
    }

    @Override
    public int hashCode() {
        return Entity.hash(this);
    }
}
