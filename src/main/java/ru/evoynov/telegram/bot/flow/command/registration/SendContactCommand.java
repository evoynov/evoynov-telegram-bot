package ru.evoynov.telegram.bot.flow.command.registration;

import org.springframework.stereotype.Component;
import ru.evoynov.telegram.bot.flow.command.Command;

@Component
public class SendContactCommand implements Command {

    public static final String NAME = "sendcontact";

    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "\uD83D\uDCF1 Мой контакт";
    }
}
