package co.phoenixlab.discord.api.entities;

import lombok.Data;

@Data
public class SentMessage {

    private String content;
    private String[] attachments;
    private boolean tts;

    public SentMessage() {
    }
}
