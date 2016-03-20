package co.phoenixlab.discord.api.entities;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ReceivedMessage implements Entity {

    private long id;
    private User author;
    private long channelId;
    private String nonce;
    private String content;
    private MessageAttachment[] attachments;
    private Instant timestamp;
    private Instant editedTimeestamp;
    private boolean mentionEveryone;
    private User[] mentions;

    public ReceivedMessage() {
    }
}
