package ru.evoynov.telegram.bot.flow.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evoynov.telegram.bot.flow.EntryBot;
import ru.evoynov.telegram.bot.flow.stage.Stage;
import ru.evoynov.telegram.bot.storage.ChatState;

import java.util.List;

@Component
public class AbortInputFlowCommand implements Command {

    public static final String NAME = "abortinputflow";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "\uD83D\uDEAB Вернуться";
    }

    @Override
    public List<Stage> getKnownStages() {
        return List.of(
                Stage.REG_NUMBER,
                Stage.REG_NAME,
                Stage.ENTER_CAR_NUMBER,
                Stage.ENTER_FULL_NAME,
                Stage.CHANGE_PROFILE_DETAILS
        );
    }

    @Override
    public void acceptMessage(List<String> entries, ChatState chatState, EntryBot sender) throws TelegramApiException {
        switch (chatState.getCurrentStage()) {
            case Stage.REG_NUMBER:
            case Stage.REG_NAME:
                chatState.resetMenuMessageId();
                chatState.resetUserProfile();
                Command.enterStage(Stage.NOT_AUTHORIZED, chatState, sender);
                break;
            case Stage.ENTER_CAR_NUMBER:
            case Stage.ENTER_FULL_NAME:
            case Stage.CHANGE_PROFILE_DETAILS:
                chatState.resetMenuMessageId();
                Command.enterStage(Stage.AUTH_MAIN_MENU, chatState, sender);
                break;
        }
    }
}