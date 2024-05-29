package ru.evoynov.telegram.bot.flow.command;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evoynov.telegram.bot.flow.EntryBot;
import ru.evoynov.telegram.bot.flow.command.registration.SendContactCommand;
import ru.evoynov.telegram.bot.flow.stage.Stage;
import ru.evoynov.telegram.bot.storage.ChatState;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserInputCommand implements Command {


    public static final String NAME = "###userinput$$$";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isApplicable(String textMessage, ChatState chatState) {
        return getName().equals(textMessage);
    }

    @Override
    public void acceptMessage(List<String> entries, ChatState chatState, EntryBot sender) throws TelegramApiException {
        chatState.resetMenuMessageId();

        String entry = entries.isEmpty() ? null : entries.get(0);
        switch (chatState.getCurrentStage()) {
            case REG_NUMBER:
                String phoneNumber = normalizePhoneNumber(entry);
                if (phoneNumber != null) {
                    chatState.setPhone(phoneNumber);
                    sendTextMessage(chatState, "Номер принят", sender);
                    Command.enterStage(Stage.REG_NAME, chatState, sender);
                } else {
                    sendValidationMessage(
                            chatState,
                            "Ввести номер телефона можно только через кнопку " + (new SendContactCommand().getLabel()) + ". Попробуйте еще раз",
                            sender
                    );
                    reenterStage(chatState, sender);
                }
                return;
            case FEEDBACK_WRONG_SHIFTS:
                entry = Objects.requireNonNullElse(entry, "");
                if (entry.matches("[0-9]{1,2}")) {
                    int shiftsCount = Integer.parseInt(entry);

                    // TODO: регистрация заявки

                    sendTextMessage(chatState,
                            "Спасибо, ваша заявка принята! Проверьте количество смен завтра", sender);
                    Command.enterStage(Stage.AUTH_MAIN_MENU, chatState, sender);
                } else {
                    sendValidationMessage(
                            chatState, "Введено недопустимое значение. Ожидается целое число от 0 до 99 в виде текстового сообщения без других символов",
                            sender
                    );
                }
                return;
            case FEEDBACK_ANOTHER_QUESTION:
                if (StringUtils.isBlank(entry)) {
                    sendValidationMessage(
                            chatState, "Введено недопустимое значение. Ожидается ваш вопрос в виде текстового сообщения",
                            sender
                    );
                    return;
                }

                // TODO: регистрация заявки

                sendTextMessage(chatState,
                        "Спасибо, ваша заявка принята!", sender);
                Command.enterStage(Stage.AUTH_MAIN_MENU, chatState, sender);
                return;
            case REG_NAME:
            case ENTER_FULL_NAME:
                if (Objects.requireNonNull(entry).matches("[А-Яа-яё-]{2,}( +[А-Яа-яё-]{2,})+")) {
                    String name = StringUtils.normalizeSpace(entry);
                    String prevName = chatState.getFullName();
                    chatState.setPendingFullName(name);

                    if (prevName == null) {
                        log.info("Имя пользователя: " + name +
                                "\nПри регистрации пользователь указал номер телефона: " + chatState.getPhone() +
                                "\nПодтвердить учетную запись: http://localhost:8080/api/v1/approve/" + chatState.getChatId() + "\n" +
                                "Отклонить учетную запись: http://localhost:8080/api/v1/reject/" + chatState.getChatId());
                    } else {
                        log.info("Изменение полного имени: " + prevName + " -> " + name,
                                "Прежнее имя пользователя: " + prevName +
                                "\nПри регистрации пользователь указал номер телефона: " + chatState.getPhone() +
                                "\nПодтвердить учетную запись: http://localhost:8080/api/v1/approve/" + chatState.getChatId() + "\n" +
                                "Отклонить учетную запись: http://localhost:8080/api/v1/reject/" + chatState.getChatId());
                    }

                    Command.enterStage(chatState.isApproved() ?
                            Stage.AUTH_MAIN_MENU : Stage.NOT_AUTHORIZED, chatState, sender);
                } else {
                    sendValidationMessage(chatState, "Неверный формат имени. Попробуйте еще раз", sender);
                    reenterStage(chatState, sender);
                }
                return;
            case ENTER_CAR_NUMBER:
                if (entry != null && entry.matches("\\d+")) {
                    String carNumber = StringUtils.normalizeSpace(entry);

                    sendTextMessage(chatState,
                            "Вы счастливый обладатель: " + chatState.getCarType().getLabel(), sender);

                    chatState.resetCarTypeSelection();
                    Command.enterStage(chatState.isApproved() ? Stage.AUTH_MAIN_MENU : Stage.NOT_AUTHORIZED, chatState, sender);
                } else {
                    sendValidationMessage(chatState, "Неверный формат номера техники. Попробуйте еще раз", sender);
                    reenterStage(chatState, sender);
                }
                return;

            default:
                sendValidationMessage(chatState, "Неожиданная команда \uD83E\uDD37\u200D♂️", sender);
        }
    }

    private String normalizePhoneNumber(String number) {
        if (StringUtils.isBlank(number)) {
            return null;
        }
        char[] arr = number.toCharArray();
        StringBuilder result = new StringBuilder();
        for (char c : arr) {
            if (c >= '0' && c <= '9') {
                result.append(c);
            }
        }
        if (result.length() == 11) {
            return "7" + result.substring(1);
        }
        if (result.length() < 10) {
            return null;
        }
        return result.toString();
    }
}
