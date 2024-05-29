package ru.evoynov.telegram.bot.flow.command.stat;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evoynov.telegram.bot.flow.EntryBot;
import ru.evoynov.telegram.bot.flow.command.Command;
import ru.evoynov.telegram.bot.flow.stage.Stage;
import ru.evoynov.telegram.bot.storage.CarType;
import ru.evoynov.telegram.bot.storage.ChatState;

import java.util.List;

@Component
public class ShowStatByCarNumber implements Command {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getLabel() {
        throw new IllegalStateException();
    }

    @Override
    public List<Stage> getKnownStages() {
        return List.of(Stage.SELECTOR);
    }

    @Override
    public void acceptMessage(List<String> entries, ChatState chatState, EntryBot sender) throws TelegramApiException {
        String selectedId = chatState.getSelectorState().getSelectedId();

        for (var carType : CarType.values()) {
            if (Integer.toString(carType.getId()).equals(selectedId)) {
                chatState.setCarType(carType);
                Command.enterStage(Stage.ENTER_CAR_NUMBER, chatState, sender);
                return;
            }
        }

        Command.enterStage(chatState.isApproved() ? Stage.AUTH_MAIN_MENU : Stage.NOT_AUTHORIZED, chatState, sender);
    }
}
