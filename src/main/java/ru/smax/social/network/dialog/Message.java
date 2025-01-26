package ru.smax.social.network.dialog;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record Message(
        UUID id,
        String dialogId,
        int author,
        String text,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
        LocalDateTime sentAt
) implements Serializable {
}
