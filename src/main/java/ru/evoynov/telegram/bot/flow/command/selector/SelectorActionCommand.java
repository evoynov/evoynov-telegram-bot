package ru.evoynov.telegram.bot.flow.command.selector;


import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evoynov.telegram.bot.flow.EntryBot;
import ru.evoynov.telegram.bot.flow.command.Command;
import ru.evoynov.telegram.bot.flow.stage.Stage;
import ru.evoynov.telegram.bot.storage.ChatState;

import java.util.List;

import static ru.evoynov.telegram.bot.flow.command.selector.SelectorActionCommand.Action.*;

@Component
public class SelectorActionCommand implements Command {

    public static final String NAME = "selectoraction";

    public static class Action {

        public static final String SELECT = "select";
        public static final String NEXT_PAGE = "nextpage";
        public static final String PREV_PAGE = "prevpage";
        public static final String UPPER = "upper";
    }

    @Override
    public String getName() {
        return NAME;
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
        if (entries.isEmpty()) {
            sendTextMessage(chatState, "Некорректная команда для селектора", sender);
            Command.enterStage(Stage.AUTH_MAIN_MENU, chatState, sender);
            return;
        }
        var selectorState = chatState.getSelectorState();
        if (selectorState == null) {
            sendTextMessage(chatState, "Селектор не задан", sender);
            Command.enterStage(Stage.AUTH_MAIN_MENU, chatState, sender);
            return;
        }

        String action = entries.get(0);
        switch (action) {
            case SELECT:
                if (entries.size() != 2 || StringUtils.isBlank(entries.get(1))) {
                    sendTextMessage(chatState, "Не передан идентификатор выбранного элемента", sender);
                    Command.enterStage(Stage.AUTH_MAIN_MENU, chatState, sender);
                }
                String selectedId = entries.get(1);
                if (selectorState.hasChildren(selectedId)) {
                    selectorState.toChild(selectedId);
                    enterSelector(chatState, sender);
                } else {
                    chatState.resetMenuMessageId();
                    selectorState.setSelectedId(selectedId);
                    selectorState.getCommandForSelection().acceptMessage(entries, chatState, sender);
                }
                break;
            case NEXT_PAGE:
                if (!selectorState.hasNextPage()) {
                    // ignore
                } else {
                    selectorState.toNextPage();
                    enterSelector(chatState, sender);
                }
                break;
            case PREV_PAGE:
                if (!selectorState.hasPreviousPage()) {
                    // ignore
                } else {
                    selectorState.toPreviousPage();
                    enterSelector(chatState, sender);
                }
                break;
            case UPPER:
                if (selectorState.isRoot()) {
                    // ignore
                } else {
                    selectorState.toParent();
                    enterSelector(chatState, sender);
                }
                break;
            default:
                sendTextMessage(chatState, "Неизвестная команда для селектора", sender);
                Command.enterStage(Stage.AUTH_MAIN_MENU, chatState, sender);
        }
    }
}
