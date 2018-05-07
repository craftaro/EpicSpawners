package com.songoda.epicspawners;

public class References {

    private String prefix;

    public References() {
        prefix = EpicSpawners.getInstance().getLocale().getMessage("general.nametag.prefix") + " ";
    }

    public String getPrefix() {
        return this.prefix;
    }
}
