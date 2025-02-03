package ru.smax.social.network.dialog;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RequestMapping("/api/internal/v1/dialog")
@RestController
public class DialogController {
    private final DialogService dialogService;

    @GetMapping("/{from}/{to}")
    public DialogResponse getDialog(@PathVariable Integer from,
                                    @PathVariable Integer to) {
        log.debug("Requested dialog for users {}, {}", from, to);
        return dialogService.findDialogMessages(List.of(from, to));
    }

    @PostMapping
    public void sendMessage(@RequestBody SendMessageRequest request) {
        log.debug("Requested send message: {}", request);
        dialogService.sendMessage(request.from, request.to, request.text);
    }

    public record SendMessageRequest(
            Integer from,
            Integer to,
            String text
    ) {
    }
}
