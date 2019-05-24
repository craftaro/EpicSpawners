package com.songoda.epicspawners.utils.settings;

public enum Category {

    ANTI_GRINDER("These are settings regarding anti hacks & grinders."),
    MAIN("General settings and options."),
    FORMULA("These are settings relating to leveling up formulas."),
    INTERFACES("These settings allow you to alter the way interfaces look.",
            "They are used in GUI's to make paterns, change them up then open up a",
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