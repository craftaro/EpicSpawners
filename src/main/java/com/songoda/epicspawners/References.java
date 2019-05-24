package com.songoda.epicspawners;

public class References {

    public static String getPrefix() {
        return EpicSpawners.getInstance().getLocale().getMessage("general.nametag.prefix") + " ";
    }
}