package ru.evoynov.telegram.bot.flow.command.stat;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evoynov.telegram.bot.flow.EntryBot;
import ru.evoynov.telegram.bot.flow.command.Command;
import ru.evoynov.telegram.bot.flow.stage.Stage;
import ru.evoynov.telegram.bot.storage.ChatState;

import java.util.List;

@Component
public class ShowReward implements Command {

    public static final String NAME = "showreward";

    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "💰Премия";
    }

    @Override
    public List<Stage> getKnownStages() {
        return List.of(Stage.AUTH_MAIN_MENU);
    }

    @Override
    public void acceptMessage(List<String> entries, ChatState chatState, EntryBot sender) throws TelegramApiException {
        sendTextMessage(chatState, "Вы заработали 100500 бобриков", sender);
        Command.enterStage(Stage.AUTH_MAIN_MENU, chatState, sender);
    }
}
