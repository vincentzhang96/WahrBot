package co.phoenixlab.discord.api.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Attachment implements Entity {

    /**
     * Attachment ID.
     */
    private long id;
    /**
     * Name of the file attached.
     */
    private String filename;
    /**
     * Size of file, in bytes.
     */
    private int size;
    /**
     * Source URL of file.
     */
    private String url;
    /**
     * A proxied URL of file.
     */
    private String proxyUrl;
    /**
     * Height of file (if image, 0 otherwise).
     */
    private int height;
    /**
     * Width of file (if image, 0 otherwise).
     */
    private int width;

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
