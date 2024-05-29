package ru.evoynov.telegram.bot.flow.command.stat;

import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Component;
import ru.evoynov.telegram.bot.flow.command.selector.SelectorState;
import ru.evoynov.telegram.bot.flow.command.selector.SelectorStateBuilder;
import ru.evoynov.telegram.bot.storage.CarType;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class CarTypeSelectorStateBuilder implements SelectorStateBuilder {

    public static String NAME = CarTypeSelectorStateBuilder.class.getSimpleName();

    private final ShowStatByCarNumber showStatByCarNumber;

    public CarTypeSelectorStateBuilder(ShowStatByCarNumber showStatByCarNumber) {
        this.showStatByCarNumber = showStatByCarNumber;
    }

    @Override
    public SelectorState build() {

        var selectorContent = Arrays.stream(CarType.values()).map(carType -> Triple.of(
                Integer.toString(carType.getId()),
                carType.getLabel(),
                (Object) Collections.emptyList()
        )).collect(Collectors.toList());

        return new SelectorState("Вид машины", selectorContent, 3, showStatByCarNumber, NAME);
    }
}
