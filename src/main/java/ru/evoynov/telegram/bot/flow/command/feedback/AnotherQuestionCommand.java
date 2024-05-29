package ru.evoynov.telegram.bot.flow.command.feedback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evoynov.telegram.bot.flow.EntryBot;
import ru.evoynov.telegram.bot.flow.command.Command;
import ru.evoynov.telegram.bot.flow.stage.Stage;
import ru.evoynov.telegram.bot.storage.ChatState;

import java.util.List;

@Component
public class AnotherQuestionCommand  implements Command {

    public static final String NAME = "anotherquestion";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "❔ Другой вопрос";
    }

    @Override
    public List<Stage> getKnownStages() {
        return List.of(
                Stage.FEEDBACK_SUBMENU
        );
    }

    @Override
    public void acceptMessage(List<String> entries, ChatState chatState, EntryBot sender) throws TelegramApiException {
        chatState.resetMenuMessageId();
        Command.enterStage(Stage.FEEDBACK_ANOTHER_QUESTION, chatState, sender);
    }
}