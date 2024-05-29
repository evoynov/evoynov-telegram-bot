package ru.evoynov.telegram.bot.flow.command.stat;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evoynov.telegram.bot.flow.EntryBot;
import ru.evoynov.telegram.bot.flow.command.Command;
import ru.evoynov.telegram.bot.flow.stage.Stage;
import ru.evoynov.telegram.bot.storage.ChatState;

import java.util.List;

@Component
public class StartShowStatByCarNumber implements Command {

    private final CarTypeSelectorStateBuilder carTypeSelectorStateBuilder;

    public static final String NAME = "showstat";

    public StartShowStatByCarNumber(CarTypeSelectorStateBuilder carTypeSelectorStateBuilder) {
        this.carTypeSelectorStateBuilder = carTypeSelectorStateBuilder;
    }

    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "📊Рейтинг техники";
    }

    @Override
    public List<Stage> getKnownStages() {
        return List.of(Stage.AUTH_MAIN_MENU, Stage.NOT_AUTHORIZED);
    }

    @Override
    public void acceptMessage(List<String> entries, ChatState chatState, EntryBot sender) throws TelegramApiException {
        chatState.resetMenuMessageId();
        chatState.resetCarTypeSelection();
        chatState.setSelectorState(carTypeSelectorStateBuilder.build());

        enterSelector(chatState, sender);
    }
}
