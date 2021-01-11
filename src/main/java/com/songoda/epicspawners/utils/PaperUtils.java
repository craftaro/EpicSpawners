package com.songoda.epicspawners.utils;

public class PaperUtils {

    private static boolean paper = false;
    static {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            paper = true;
        } catch (ClassNotFoundException e) {}
    }

    public static boolean isPaper() {
        return paper;
    }
}
