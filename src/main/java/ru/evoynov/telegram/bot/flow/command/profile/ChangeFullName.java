package ru.evoynov.telegram.bot.flow.command.profile;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evoynov.telegram.bot.flow.EntryBot;
import ru.evoynov.telegram.bot.flow.command.Command;
import ru.evoynov.telegram.bot.flow.stage.Stage;
import ru.evoynov.telegram.bot.storage.ChatState;

import java.util.List;

@Component
public class ChangeFullName implements Command {

    public static final String NAME = "changefullname";

    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "✏️ Исправить имя";
    }

    @Override
    public List<Stage> getKnownStages() {
        return List.of(Stage.CHANGE_PROFILE_DETAILS);
    }

    @Override
    public void acceptMessage(List<String> entries, ChatState chatState, EntryBot sender) throws TelegramApiException {
        chatState.resetMenuMessageId();
        Command.enterStage(Stage.ENTER_FULL_NAME, chatState, sender);
    }
}