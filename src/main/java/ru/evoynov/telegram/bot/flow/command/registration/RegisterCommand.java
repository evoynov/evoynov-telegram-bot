package ru.evoynov.telegram.bot.flow.command.registration;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evoynov.telegram.bot.flow.EntryBot;
import ru.evoynov.telegram.bot.flow.command.Command;
import ru.evoynov.telegram.bot.flow.stage.Stage;
import ru.evoynov.telegram.bot.storage.ChatState;

import java.util.List;

@Component
public class RegisterCommand implements Command {

    public static final String NAME = "register";

    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "\uD83D\uDD11 Авторизация";
    }

    @Override
    public List<Stage> getKnownStages() {
        return List.of(Stage.NOT_AUTHORIZED);
    }

    @Override
    public void acceptMessage(List<String> entries, ChatState chatState, EntryBot sender) throws TelegramApiException {
        chatState.resetMenuMessageId();
        Command.enterStage(Stage.REG_NUMBER, chatState, sender);
    }
}
