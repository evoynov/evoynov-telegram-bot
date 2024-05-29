package ru.evoynov.telegram.bot.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ToggleUserProfileEvent extends ApplicationEvent {

    private final long chatId;

    private final boolean enabled;

    public ToggleUserProfileEvent(Object source, long chatId, boolean enabled) {
        super(source);
        this.chatId = chatId;
        this.enabled = enabled;
    }
}
