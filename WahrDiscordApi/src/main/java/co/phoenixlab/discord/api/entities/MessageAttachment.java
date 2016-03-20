package co.phoenixlab.discord.api.entities;

import lombok.Getter;

@Getter
public class MessageAttachment implements Entity {

    private long id;
    private String filename;
    private String url;
    private String proxyUrl;
    private int size;

    public MessageAttachment() {
    }
}
