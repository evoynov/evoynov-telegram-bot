package ru.evoynov.telegram.bot.flow;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evoynov.telegram.bot.event.ToggleUserProfileEvent;
import ru.evoynov.telegram.bot.flow.command.AbortInputFlowCommand;
import ru.evoynov.telegram.bot.flow.command.Command;
import ru.evoynov.telegram.bot.flow.command.UserInputCommand;
import ru.evoynov.telegram.bot.flow.stage.Stage;
import ru.evoynov.telegram.bot.storage.ChatState;
import ru.evoynov.telegram.bot.storage.ChatStateStorage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static ru.evoynov.telegram.bot.flow.command.Command.COMMAND_PARAM_SEP;
import static ru.evoynov.telegram.bot.flow.command.Command.COMMAND_PREFIX;

@Component
@Slf4j
public class EntryBot extends TelegramLongPollingBot {
    private final String botName;

    @Getter
    private final List<Command> knownCommands;

    private final ChatStateStorage chatStateStorage;

    public EntryBot(
            @Value("${bot.token}") String botToken,
            @Value("${bot.name}") String botName,
            List<Command> knownCommands,
            ChatStateStorage chatStateStorage) {
        super(botToken);
        this.botName = botName;
        this.knownCommands = knownCommands;
        this.chatStateStorage = chatStateStorage;
    }

    @Override
    public String getBotUsername() {
        return "@" + botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String command = null;
        List<String> entries = new ArrayList<>();
        Long chatId;
        ChatState state;
        log.debug("Received new update: " + update);
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            state = chatStateStorage.getState(chatId);

            String msg = update.getMessage().getText();
            switch (state.getCurrentStage().waitInputType()) {
                case CONTACT:
                    if (update.getMessage().getContact() != null) {
                        Contact contact = update.getMessage().getContact();
                        entries.add(contact.getPhoneNumber());
                    } else {
                        entries.add("Number sent not via sending contact. Skip");
                    }
                    command = UserInputCommand.NAME;
                    break;
                case TEXT:
                case NONE:
                default:
                    if (StringUtils.isNotBlank(msg)) {
                        if (msg.startsWith(COMMAND_PREFIX)) {
                            command = msg;
                        } else if (msg.equals(new AbortInputFlowCommand().getLabel())) {
                            command = COMMAND_PREFIX + AbortInputFlowCommand.NAME;
                        } else {
                            command = UserInputCommand.NAME;
                            entries.add(msg);
                        }
                    }
                    break;
            }
        } else if (update.hasCallbackQuery() && StringUtils.isNotBlank(update.getCallbackQuery().getData())) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            state = chatStateStorage.getState(chatId);

            String[] requestItems = update.getCallbackQuery().getData().split(COMMAND_PARAM_SEP);
            int start = 0;
            if (requestItems[0].startsWith(COMMAND_PREFIX)) {
                command = requestItems[0];
                start = 1;
            } else {
                command = UserInputCommand.NAME;
            }
            IntStream.range(start, requestItems.length).forEach(i -> entries.add(requestItems[i]));
        } else {
            log.warn("Unknown case: " + update);
            return;
        }

        if (chatId == null) {
            log.warn("Received message without chat id, ignore it: " + update);
            return;
        }

        state.addUpdate(update);

        if (StringUtils.isNotBlank(command)) {
            final String cmd = command;
            Optional<Command> knownCommand = knownCommands.stream().filter(c -> c.isApplicable(cmd, state)).findAny();
            if (knownCommand.isPresent()) {
                try {
                    knownCommand.get().acceptMessage(entries, state, this);
                    return;
                } catch (Exception e) {
                    log.error("Command <" + knownCommand.get().getName() + "> failed to accept update, chatId is " + chatId, e);
                    try {
                        state.resetMenuMessageId();
                        Command.enterStage(state.isApproved() ? Stage.AUTH_MAIN_MENU : Stage.NOT_AUTHORIZED, state, this);
                        return;
                    } catch (TelegramApiException ignored) {}
                }
            }
        }
        try {
            state.resetMenuMessageId();
            Command.enterStage(state.isApproved() ? Stage.AUTH_MAIN_MENU : Stage.NOT_AUTHORIZED, state, this);
        } catch (TelegramApiException ignored) {}
    }

    public final <T extends Serializable, Method extends BotApiMethod<T>> void send(Method method) throws TelegramApiException {
        sendApiMethod(method);
    }

    @EventListener
    public void toggleUserProfile(ToggleUserProfileEvent event) {
        var state = chatStateStorage.getState(event.getChatId());
        try {
            state.resetMenuMessageId();

            var sendMessage = new SendMessage(state.getChatIdStr(),
                    event.isEnabled() ? "Ваша учетная запись подтверждена, теперь вам доступен просмотр индивидуальной премии" : "Ваша учетная запись отклонена, попробуйте снова");
            sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
            sendMessage.enableMarkdown(true);
            this.send(sendMessage);

            Command.enterStage(state.isApproved() ? Stage.AUTH_MAIN_MENU : Stage.NOT_AUTHORIZED, state, this);
        } catch (TelegramApiException ignored) {}
    }
}
