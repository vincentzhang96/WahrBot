package co.phoenixlab.discord.api.request.channel.message;

import co.phoenixlab.discord.api.util.SnowflakeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BulkMessageDeleteRequest {

    @Size(min = 2, max = 100)
    private String[] messages;

    static class Builder {

        private ArrayList<String> pending;

        public Builder() {
            pending = new ArrayList<>();
        }

        public Builder messages(String[] msgIds) {
            pending.addAll(Arrays.asList(msgIds));
            return this;
        }

        public Builder addMessage(String msgId) {
            pending.add(msgId);
            return this;
        }

        public Builder messages(long[] msgIds) {
            pending.addAll(Arrays.stream(msgIds).
                mapToObj(SnowflakeUtils::snowflakeToString).
                collect(Collectors.toList()));
            return this;
        }

        public Builder addMessage(long msgId) {
            pending.add(SnowflakeUtils.snowflakeToString(msgId));
            return this;
        }

        public BulkMessageDeleteRequest build() {
            return new BulkMessageDeleteRequest(pending.toArray(new String[pending.size()]));
        }
    }
}
