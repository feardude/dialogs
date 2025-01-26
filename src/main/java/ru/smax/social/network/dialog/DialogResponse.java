package ru.smax.social.network.dialog;

import java.time.LocalDateTime;
import java.util.List;

public record DialogResponse(
        String id,
        List<MessageResponse> messages
) {
    public record MessageResponse(
            String text,
            int author,
            LocalDateTime sentAt
    ) {
    }
}
