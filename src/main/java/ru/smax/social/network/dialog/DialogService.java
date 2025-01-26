package ru.smax.social.network.dialog;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;

@Slf4j
@AllArgsConstructor
@Service
public class DialogService {

    private final DialogRepository dialogRepository;

    @PostConstruct
    public void init() {
        generateDialogs(false);
    }

    private void generateDialogs(boolean generateDialogs) {
        if (!generateDialogs) {
            return;
        }

        log.info("Started generating dialogs");
        int users = 1000;
        int dialogs = 10;
        List<Object[]> batchArgs = new ArrayList<>(users);
        for (int i = 1; i <= users; i++) {
            var from = i;
            for (int j = 0; j < dialogs; j++) {
                var to = from + j + 1;
                var id = "%d,%d".formatted(from, to);
                if (to > users) {
                    to = to - users;
                    id = "%d,%d".formatted(to, from);
                }
                batchArgs.add(new Object[]{id});
                log.info("Dialog: {}", id);
            }
        }
        dialogRepository.createDialogs(batchArgs);
        log.info("Finished generating dialogs");
    }

    public void createDialog(List<Integer> userIds) {
        String dialogId = toDialogId(userIds);
        try {
            dialogRepository.createDialog(dialogId);
        } catch (DuplicateKeyException e) {
            log.debug("Dialog already exists: '{}'", dialogId);
        }
    }

    @Transactional(readOnly = true)
    public DialogResponse findDialogMessages(List<Integer> userIds) {
        var dialogId = toDialogId(userIds);
        var messages = dialogRepository.findDialog(dialogId)
                                       .stream()
                                       .map(m -> new DialogResponse.MessageResponse(
                                               m.text(),
                                               m.author(),
                                               m.sentAt()
                                       ))
                                       .sorted(comparing(DialogResponse.MessageResponse::sentAt))
                                       .toList();
        return new DialogResponse(dialogId, messages);
    }

    public void sendMessage(Integer from, Integer to, String text) {
        var message = Message.builder()
                             .id(randomUUID())
                             .dialogId(toDialogId(List.of(from, to)))
                             .author(from)
                             .text(text)
                             .sentAt(LocalDateTime.now())
                             .build();
        dialogRepository.saveMessage(message);
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
}
