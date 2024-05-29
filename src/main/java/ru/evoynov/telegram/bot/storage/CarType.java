package ru.evoynov.telegram.bot.storage;

import lombok.Getter;

public enum CarType {
    CABRIO(1,"Кабриолет"),
    ROADSTER(2,"Родстер"),
    GRAN_COUPE(3,"Гран Купе"),
    CROSSOVER(4,"Кроссовер"),

    ;

    @Getter
    private final int id;

    @Getter
    private final String label;

    CarType(int id, String label) {
        this.id = id;
        this.label = label;
    }
}
