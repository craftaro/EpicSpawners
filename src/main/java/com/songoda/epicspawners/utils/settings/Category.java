package com.songoda.epicspawners.utils.settings;

public enum Category {

    SPAWNER_BOOSTING("These are settings regarding the boosting of spawners."),
    SPAWNER_DROPS("These are settings regarding spawner drops."),
    MAIN("General settings and options."),
    FORMULA("These are settings relating to leveling up formulas."),
    INTERFACES("These settings allow you to alter the way interfaces look.",
            "They are used in GUI's to make patterns, change them up then open up a",
            "GUI to see how it works."),
    SYSTEM("System related settings.");

    private String[] comments;


    Category(String... comments) {
        this.comments = comments;
    }

    public String[] getComments() {
        return comments;
    }
}