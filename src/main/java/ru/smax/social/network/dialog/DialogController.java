package ru.smax.social.network.dialog;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.SocketException;
import java.util.List;

@Slf4j
@AllArgsConstructor
@RequestMapping("/dialog")
@RestController
public class DialogController {
    private final DialogService dialogService;

    @GetMapping("/{from}/{to}")
    public DialogResponse getDialog(@PathVariable Integer from,
                                    @PathVariable Integer to) {
        log.debug("Requested dialog for users {}, {}", from, to);
        return dialogService.findDialogMessages(List.of(from, to));
    }

    @PostMapping("/{from}/{to}")
    public void sendMessage(@PathVariable Integer from,
                            @PathVariable Integer to,
                            @RequestBody SendMessageRequest request) {
        log.debug("Requested send message from {} to {}: {}", from, to, request);
        dialogService.sendMessage(from, to, request.text);
    }

    public record SendMessageRequest(
            String text
    ) {
    }
}
