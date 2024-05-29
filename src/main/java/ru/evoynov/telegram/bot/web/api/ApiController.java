package ru.evoynov.telegram.bot.web.api;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.evoynov.telegram.bot.event.ToggleUserProfileEvent;
import ru.evoynov.telegram.bot.storage.ChatStateStorage;

@RestController
@RequestMapping("/api/v1")
public class ApiController implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private ChatStateStorage chatStateStorage;

    public ApiController(ChatStateStorage chatStateStorage) {
        this.chatStateStorage = chatStateStorage;
    }

    @RequestMapping("/{toggle}/{chatId}")
    public ResponseEntity<Boolean> toggle(
            @PathVariable(value="toggle") String toggle,
            @PathVariable(value="chatId") Long chatId) {

        if (chatStateStorage.hasState(chatId)) {
            var chat = chatStateStorage.getState(chatId);
            if (chat.isApproved() && "reject".equals(toggle)) {
                chat.resetCarTypeSelection();
                chat.resetUserProfile();
                chat.resetMenuMessageId();
                chat.setApproved(false);
                applicationContext.publishEvent(new ToggleUserProfileEvent(this, chatId, false));
                return ResponseEntity.ok(true);
            }
            if ("approve".equals(toggle)) {
                chat.setApproved(true);
                if (chat.getPendingFullName() != null) {
                    chat.setFullName(chat.getPendingFullName());
                    chat.setPendingFullName(null);
                }
                applicationContext.publishEvent(new ToggleUserProfileEvent(this, chatId, true));
                return ResponseEntity.ok(true);
            }
        }
        return ResponseEntity.ok(false);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
