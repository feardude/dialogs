package ru.smax.social.network.dialog;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record Message(
        String dialogId,

        @JsonProperty("author")
        int author,

        @JsonProperty("text")
        String text,

        @JsonProperty("sent_at")
        long sentAt
) implements Serializable {
}
