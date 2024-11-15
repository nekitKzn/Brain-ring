package com.nekitvp.brain;


public enum Team {

    RED("Красная команда", "#ad3333", "КРАСНОЙ", "КРАСНАЯ"),

    GREEN("Зеленая команда", "#4f6b34", "ЗЕЛЕНОЙ", "ЗЕЛЕНАЯ");

    private final String name;
    private final String color;
    private final String textForFail;
    private final String textForAnswer;


    Team(String name, String color, String textForFail, String textForAnswer) {
        this.name = name;
        this.color = color;
        this.textForFail = textForFail;
        this.textForAnswer = textForAnswer;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public String getTextForFail() {
        return textForFail;
    }

    public String getTextForAnswer() {
        return textForAnswer;
    }
}
