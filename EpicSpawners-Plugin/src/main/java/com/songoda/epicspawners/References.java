package com.songoda.epicspawners;

public class References {

    private String prefix;

    public References() {
        prefix = EpicSpawnersPlugin.getInstance().getLocale().getMessage("general.nametag.prefix") + " ";
    }

    public String getPrefix() {
        return this.prefix;
    }
}