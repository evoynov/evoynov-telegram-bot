package ru.evoynov.telegram.bot.flow.command;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evoynov.telegram.bot.flow.EntryBot;
import ru.evoynov.telegram.bot.flow.command.registration.SendContactCommand;
import ru.evoynov.telegram.bot.flow.command.selector.SelectorActionCommand;
import ru.evoynov.telegram.bot.flow.stage.Stage;
import ru.evoynov.telegram.bot.storage.ChatState;

import java.util.*;
import java.util.stream.Collectors;

public interface Command {
    String COMMAND_PREFIX = "/";
    String COMMAND_PARAM_SEP = ":";

    Logger log = LoggerFactory.getLogger(Command.class);

    String getName();

    String getLabel();

    default List<Stage> getKnownStages() {
        return Collections.emptyList();
    }

    default boolean isApplicable(String textMessage, ChatState chatState) {
        return (COMMAND_PREFIX + getName()).equalsIgnoreCase(textMessage) && getKnownStages().contains(chatState.getCurrentStage());
    }

    default void acceptMessage(List<String> entries, ChatState chatState, EntryBot sender) throws TelegramApiException {}

    static void enterStage(Stage newStage, ChatState chatState, EntryBot sender) throws TelegramApiException {
        String suffix = null;
        switch (newStage) {
            case CHANGE_PROFILE_DETAILS:
                suffix = chatState.getFullName();
                break;
            case ENTER_CAR_NUMBER:
                if (chatState.getCarType() != null) {
                    suffix = chatState.getCarType().getLabel();
                }
                break;
        }

        int menuMessageId = drawSimpleInlineMenu(
                chatState,
                newStage.getMaxButtonsInRow(),
                newStage.getHeader()
                        + (suffix != null ? ": " + suffix : ""),
                extractMenu(newStage, sender.getKnownCommands()),
                sender
        );

        chatState.setCurrentStage(newStage);
        chatState.setMenuMessageId(menuMessageId);
    }

    default void enterSelector(ChatState chatState, EntryBot sender) throws TelegramApiException {
        int menuMessageId = drawComplexMenu(chatState.getSelectorState().getHeader(), getSelectorMenu(chatState), chatState, sender);

        chatState.setCurrentStage(Stage.SELECTOR);
        chatState.setMenuMessageId(menuMessageId);
    }

    default void reenterStage(ChatState chatState, EntryBot sender) throws TelegramApiException {
        enterStage(chatState.getCurrentStage(), chatState, sender);
    }

    default InlineKeyboardMarkup getSelectorMenu(ChatState state) {
        var selectorState = state.getSelectorState();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        selectorState
                .getCurrentPageItems()
                .forEach(t -> {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setCallbackData(
                            COMMAND_PREFIX +
                                    SelectorActionCommand.NAME +
                                    COMMAND_PARAM_SEP +
                                    SelectorActionCommand.Action.SELECT +
                                    COMMAND_PARAM_SEP +
                                    t.getLeft()
                    );
                    button.setText(t.getMiddle() + (t.getRight() ? "" : " >>"));
                    keyboard.add(List.of(button));
                });

        List<InlineKeyboardButton> actionsRow = new ArrayList<>();

        if (!selectorState.isRoot()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setCallbackData(COMMAND_PREFIX + SelectorActionCommand.NAME + COMMAND_PARAM_SEP + SelectorActionCommand.Action.UPPER);
            button.setText("\uD83D\uDD3C");
            actionsRow.add(button);
        }

        if (selectorState.hasPreviousPage()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setCallbackData(
                    COMMAND_PREFIX + SelectorActionCommand.NAME + COMMAND_PARAM_SEP + SelectorActionCommand.Action.PREV_PAGE
            );
            button.setText("⏪");
            actionsRow.add(button);
        }

        if (selectorState.hasNextPage()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setCallbackData(
                    COMMAND_PREFIX + SelectorActionCommand.NAME + COMMAND_PARAM_SEP + SelectorActionCommand.Action.NEXT_PAGE
            );
            button.setText("⏩");
            actionsRow.add(button);
        }

        keyboard.add(actionsRow);

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setCallbackData(COMMAND_PREFIX + AbortInputFlowCommand.NAME);
        button.setText("\uD83D\uDEAB Вернуться");
        backRow.add(button);

        keyboard.add(backRow);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);

        return inlineKeyboardMarkup;
    }

    static List<Pair<String, String>> extractMenu(Stage stage, List<Command> knownCommands) {
        List<Pair<String, String>> result = new ArrayList<>();
        Arrays
                .stream(stage.getButtons())
                .forEach(b -> {
                    Optional<Command> cmd = knownCommands.stream().filter(c -> Objects.nonNull(c.getName()) && c.getName().equals(b)).findAny();
                    if (cmd.isPresent()) {
                        result.add(Pair.of(cmd.get().getName(), cmd.get().getLabel()));
                    } else {
                        log.warn("Unable to find any command by name: " + b);
                    }
                });
        return result;
    }

    static int drawSimpleInlineMenu(
            ChatState state,
            int maxInRow,
            String menuHeader,
            List<Pair<String, String>> menuSource,
            EntryBot sender
    ) throws TelegramApiException {
        ReplyKeyboard replyKeyboard;

        if (menuSource.stream().anyMatch(p -> p.getKey().equals(SendContactCommand.NAME))) {
            ReplyKeyboardMarkup keyBoardMarkup = new ReplyKeyboardMarkup();

            List<KeyboardRow> keyboard = menuSource
                    .stream()
                    .map(p -> {
                        KeyboardButton button = new KeyboardButton();
                        if (p.getKey().equals(SendContactCommand.NAME)) {
                            button.setRequestContact(true);
                        }
                        button.setText(p.getValue());
                        KeyboardRow row = new KeyboardRow();
                        row.add(button);
                        return row;
                    })
                    .collect(Collectors.toList());

            keyBoardMarkup.setKeyboard(keyboard);
            keyBoardMarkup.setResizeKeyboard(true);

            replyKeyboard = keyBoardMarkup;
        } else {
            List<InlineKeyboardButton> buttons = menuSource
                    .stream()
                    .map(p -> {
                        InlineKeyboardButton button = new InlineKeyboardButton();
                        button.setCallbackData(COMMAND_PREFIX + p.getKey());
                        String label = p.getValue();
                        button.setText(label);
                        return button;
                    })
                    .collect(Collectors.toList());

            int size = buttons.size();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            for (int i = 0; i < size / maxInRow + (size % maxInRow > 0 ? 1 : 0); i++) {
                keyboard.add(buttons.subList(i * maxInRow, Math.min((i + 1) * maxInRow, size)));
            }

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(keyboard);
            replyKeyboard = inlineKeyboardMarkup;
        }

        if (state.canRedrawMenu() && replyKeyboard instanceof InlineKeyboardMarkup) {
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setText(menuHeader);
            editMessageText.setMessageId(state.getMenuMessageId());
            editMessageText.setChatId(state.getChatIdStr());
            editMessageText.enableMarkdown(true);
            sender.execute(editMessageText);

            EditMessageReplyMarkup editMenu = new EditMessageReplyMarkup();
            editMenu.setReplyMarkup((InlineKeyboardMarkup) replyKeyboard);
            editMenu.setMessageId(state.getMenuMessageId());
            editMenu.setChatId(state.getChatIdStr());
            sender.execute(editMenu);

            return state.getMenuMessageId();
        } else {
            SendMessage msg = new SendMessage(state.getChatIdStr(), menuHeader);
            msg.setReplyMarkup(replyKeyboard);
            msg.enableMarkdown(true);
            return sender.execute(msg).getMessageId();
        }
    }

    default void sendValidationMessage(ChatState state, String text, EntryBot sender) throws TelegramApiException {
        var sendMessage = new SendMessage(state.getChatIdStr(), text);
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyToMessageId(state.getLastReceivedMessageId());
        sender.send(sendMessage);
    }

    default void sendTextMessage(ChatState state, String text, EntryBot sender) {
        var sendMessage = new SendMessage(state.getChatIdStr(), text);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.enableMarkdown(true);
        try {
            sender.send(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        state.resetMenuMessageId();
    }

    default void sendPopupMessage(ChatState state, String text, boolean asAlert, EntryBot sender) throws TelegramApiException {
        AnswerCallbackQuery query = new AnswerCallbackQuery();
        query.setText(text);
        query.setCallbackQueryId(state.getLastCallbackId());
        query.setShowAlert(asAlert);
        sender.send(query);
    }

    default int drawComplexMenu(String header, InlineKeyboardMarkup inlineKeyboardMarkup, ChatState state, EntryBot sender)
            throws TelegramApiException {
        if (state.canRedrawMenu()) {
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setText(header);
            editMessageText.setMessageId(state.getMenuMessageId());
            editMessageText.setChatId(state.getChatIdStr());
            editMessageText.enableMarkdown(true);
            sender.execute(editMessageText);

            EditMessageReplyMarkup editMenu = new EditMessageReplyMarkup();
            editMenu.setReplyMarkup(inlineKeyboardMarkup);
            editMenu.setMessageId(state.getMenuMessageId());
            editMenu.setChatId(state.getChatIdStr());
            sender.execute(editMenu);

            return state.getMenuMessageId();
        } else {
            SendMessage msg = new SendMessage(state.getChatIdStr(), header);
            msg.setReplyMarkup(inlineKeyboardMarkup);
            msg.enableMarkdown(true);
            return sender.execute(msg).getMessageId();
        }
    }

    default String numberToEmoji(String numberStr) {
        if (StringUtils.isBlank(numberStr) || !numberStr.matches("[0-9]+")) {
            return numberStr;
        }
        var result = new StringBuilder();
        for (char c : numberStr.toCharArray()) {
            switch (c) {
                case '0':
                    result.append("0⃣");
                    break;
                case '1':
                    result.append("1⃣");
                    break;
                case '2':
                    result.append("2⃣");
                    break;
                case '3':
                    result.append("3⃣");
                    break;
                case '4':
                    result.append("4⃣");
                    break;
                case '5':
                    result.append("5⃣");
                    break;
                case '6':
                    result.append("6⃣");
                    break;
                case '7':
                    result.append("7⃣");
                    break;
                case '8':
                    result.append("8⃣");
                    break;
                case '9':
                    result.append("9⃣");
                    break;
                default:
                    break;
            }
        }
        return result.toString();
    }
}
