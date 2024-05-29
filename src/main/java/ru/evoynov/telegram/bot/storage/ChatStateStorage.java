package ru.evoynov.telegram.bot.storage;

import org.springframework.stereotype.Component;
import ru.evoynov.telegram.bot.flow.stage.Stage;

@Component
public class ChatStateStorage {
    private ChatStateMap states;

    public boolean hasState(Long chatId) {
        if (states == null) {
            states = ChatStateMap.restore();
        }
        return states.containsKey(chatId);
    }

    public ChatState getState(Long chatId) {
        if (states == null) {
            states = ChatStateMap.restore();
        }
        ChatState state = states.get(chatId);
        if (state == null) {
            state = new ChatState(chatId, states);
            state.setCurrentStage(Stage.NOT_AUTHORIZED);
            states.put(chatId, state);
        }
        return state;
    }
}