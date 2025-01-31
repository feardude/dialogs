package ru.smax.social.network.dialog;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;
import static java.util.Comparator.comparing;

@Slf4j
@AllArgsConstructor
@Service
public class DialogService {

    private final DialogRepository dialogRepository;

    @PostConstruct
    public void init() {
        generateDialogs(false);
    }

    private void generateDialogs(boolean generateMessages) {
        if (!generateMessages) {
            return;
        }

        log.info("Started generating messages");
        var random = new Random();
        int users = 1000;
        int dialogs = 100;
        int messages = 10;
        for (int i = 1; i <= users; i++) {
            generateDialogsWithMessages(dialogs, i, users, messages, random);
        }
        log.info("Finished generating messages");
    }

    private void generateDialogsWithMessages(int dialogs, int from, int users, int messages, Random random) {
        log.info("Generating dialogs with messages: from={}", from);
        for (int j = 0; j < dialogs; j++) {
            var to = from + j + 1;
            var id = "%d,%d".formatted(from, to);
            if (to > users) {
                to = to - users;
                id = "%d,%d".formatted(to, from);
            }
            for (int x = 0; x < messages; x++) {
                var msg = Message.builder()
                                 .dialogId(id)
                                 .author(random.nextInt(2) == 0 ? from : to)
                                 .text(UUID.randomUUID().toString())
                                 .sentAt(LocalDateTime.now().toInstant(UTC).toEpochMilli())
                                 .build();
                dialogRepository.sendMessage(msg);
            }
        }
        log.info("Generated {} dialogs, {} messages", dialogs, dialogs * messages);
    }

    public DialogResponse findDialogMessages(List<Integer> userIds) {
        var dialogId = toDialogId(userIds);
        var dialog = execWithRetry(() -> dialogRepository.findDialog(dialogId));
        var messages = dialog.stream()
                             .map(m -> new DialogResponse.MessageResponse(
                                     m.text(),
                                     m.author(),
                                     LocalDateTime.ofInstant(Instant.ofEpochMilli(m.sentAt()), UTC)
                             ))
                             .sorted(comparing(DialogResponse.MessageResponse::sentAt))
                             .toList();
        return new DialogResponse(dialogId, messages);
    }

    public void sendMessage(Integer from, Integer to, String text) {
        var message = Message.builder()
                             .dialogId(toDialogId(List.of(from, to)))
                             .author(from)
                             .text(text)
                             .sentAt(LocalDateTime.now().toInstant(UTC).toEpochMilli())
                             .build();
        dialogRepository.sendMessage(message);
        log.debug("Saved message: {}", message);
    }

    private String toDialogId(List<Integer> userIds) {
        String dialogId = userIds.stream()
                                 .sorted()
                                 .map(Object::toString)
                                 .collect(Collectors.joining(","));
        log.debug("User ids: {}, dialog id: '{}'", userIds, dialogId);
        return dialogId;
    }

    private <T> T execWithRetry(Supplier<T> action) {
        int retries = 5;
        while (retries > 0) {
            try {
                return action.get();
            } catch (Exception e) {
                retries--;
                if (retries == 0) {
                    throw e;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
        }
        throw new RuntimeException("Operation failed after retries");
    }
}
