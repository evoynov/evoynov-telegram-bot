package ru.evoynov.telegram.bot.flow.command;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evoynov.telegram.bot.flow.EntryBot;
import ru.evoynov.telegram.bot.flow.stage.Stage;
import ru.evoynov.telegram.bot.storage.ChatState;

import java.util.List;

@Component
@Log4j2
public class HelpCommand implements Command {
    public static final String NAME = "showhelp";

    @Autowired
    private ResourceLoader resLoad;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "ℹ️ Справка";
    }

    @Override
    public List<Stage> getKnownStages() {
        return List.of(
                Stage.AUTH_MAIN_MENU,
                Stage.NOT_AUTHORIZED
        );
    }

    @Override
    public void acceptMessage(List<String> entries, ChatState chatState, EntryBot sender) throws TelegramApiException {
        String regFlowInfo = chatState.isApproved() ? "" :
                """
                        *Авторизация в сервисе производится в два этапа:*
                        1. Вы нажимаете "Авторизация"
                        2. Сервис регистрирует Ваш номер, Фамилию и Имя, позднее поступит входящий звонок или смс, чтобы подтвердить, что "Вы это Вы".
                        Важно: оба пункта обязательны, так как информация по Вашей заработной плате должна быть защищена от доступа других лиц!

                        """;

        sendTextMessage(chatState, regFlowInfo + "*За справочной информацией обращаться:*\n" +
                "По расчёту итоговых сумм, расчёту сделки - отдел ЗП", sender);

        Command.enterStage(chatState.getCurrentStage(), chatState, sender);
    }
}
