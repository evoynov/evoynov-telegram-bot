package ru.evoynov.telegram.bot.flow.stage;

import lombok.Getter;
import ru.evoynov.telegram.bot.flow.command.feedback.AnotherQuestionCommand;
import ru.evoynov.telegram.bot.flow.command.feedback.FeedbackMenuCommand;
import ru.evoynov.telegram.bot.flow.command.feedback.WrongShiftsFeedbackCommand;
import ru.evoynov.telegram.bot.flow.command.profile.ChangeFullName;
import ru.evoynov.telegram.bot.flow.command.registration.RegisterCommand;
import ru.evoynov.telegram.bot.flow.command.registration.SendContactCommand;
import ru.evoynov.telegram.bot.flow.command.AbortInputFlowCommand;
import ru.evoynov.telegram.bot.flow.command.HelpCommand;
import ru.evoynov.telegram.bot.flow.command.profile.UserProfileMenu;
import ru.evoynov.telegram.bot.flow.command.stat.ShowReward;
import ru.evoynov.telegram.bot.flow.command.stat.StartShowStatByCarNumber;

import static ru.evoynov.telegram.bot.flow.stage.InputType.CONTACT;
import static ru.evoynov.telegram.bot.flow.stage.InputType.NONE;
import static ru.evoynov.telegram.bot.flow.stage.InputType.TEXT;

public enum Stage {
    NOT_AUTHORIZED(
            "\uD83D\uDCF1 Вас приветствует бот *EVoynov*!",
            NONE,
            StartShowStatByCarNumber.NAME,
            RegisterCommand.NAME,
            HelpCommand.NAME
    ),
    REG_NUMBER(
            "Для доступа к полной версии регистрации нужен ваш контакт.\nНажмите кнопку \uD83D\uDCF1 *Мой контакт* для отправки",
            CONTACT,
            SendContactCommand.NAME,
            AbortInputFlowCommand.NAME
    ),
    REG_NAME("Как можно к вам обращаться? ⌨\n_(Фамилия Имя)_", TEXT, AbortInputFlowCommand.NAME),
    AUTH_MAIN_MENU(
            "\uD83D\uDCF1 Вас приветствует бот *EVoynov*!",
            NONE,
            UserProfileMenu.NAME,
            StartShowStatByCarNumber.NAME,
            ShowReward.NAME,
            HelpCommand.NAME,
            FeedbackMenuCommand.NAME
    ),
    FEEDBACK_SUBMENU(
            "❓ Задать вопрос",
            NONE,
            WrongShiftsFeedbackCommand.NAME,
            AnotherQuestionCommand.NAME,
            AbortInputFlowCommand.NAME
    ),
    FEEDBACK_WRONG_SHIFTS(
            "Введите фактическое число смен, выполненных Вами (целое число 0-99)",
            TEXT,
            AbortInputFlowCommand.NAME
    ),
    FEEDBACK_ANOTHER_QUESTION(
            "Введите ваш вопрос",
            TEXT,
            AbortInputFlowCommand.NAME
    ),
    ENTER_CAR_NUMBER("Введите номер техники", TEXT, AbortInputFlowCommand.NAME),

    CHANGE_PROFILE_DETAILS("Редактирование профиля",
            NONE,
            ChangeFullName.NAME,
            AbortInputFlowCommand.NAME),

    ENTER_FULL_NAME("Введите в формате <Фамилия Имя Отчество> новое значение",
            TEXT, AbortInputFlowCommand.NAME),

    SELECTOR(null, InputType.SELECTOR),

    ;

    @Getter
    private final String header;
    @Getter
    private final String[] buttons;
    @Getter
    private int maxButtonsInRow = 1;
    private final InputType waitInputType;

    Stage(String header, InputType waitInputType, String... buttons) {
        this.header = header;
        this.buttons = buttons;
        this.waitInputType = waitInputType;
    }

    Stage(String header, InputType waitInputType, int maxButtonsInRow, String... buttons) {
        this.header = header;
        this.buttons = buttons;
        this.maxButtonsInRow = maxButtonsInRow;
        this.waitInputType = waitInputType;
    }

    public InputType waitInputType() {
        return waitInputType;
    }
}
