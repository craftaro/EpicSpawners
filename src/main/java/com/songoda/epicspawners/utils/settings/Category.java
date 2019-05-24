package com.songoda.epicspawners.utils.settings;

public enum Category {

    ECONOMY("Settings regarding economy.",
            "Only one economy option can be used at a time. If you enable more than",
            "one of these the first one will be used."),
    SPAWNER_BOOSTING("These are settings regarding the boosting of spawners."),
    SPAWNER_DROPS("These are settings regarding spawner drops."),
    ENTITY("These are settings regarding entities."),
    MAIN("General settings and options."),
    MAIN_EQUATIONS("These are settings relating to equations."),
    INTERFACES("These settings allow you to alter the way interfaces look.",
            "They are used in GUI's to make patterns, change them up then open up a",
            "GUI to see how it works."),
    DATABASE("Settings regarding the Database."),
    SYSTEM("System related settings.");

    private String[] comments;


    Category(String... comments) {
        this.comments = comments;
    }

    public String[] getComments() {
        return comments;
    }
}