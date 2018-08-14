package com.songoda.epicspawners.command.commands;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSpawnerShop extends AbstractCommand {

    public CommandSpawnerShop() {
        super("spawnershop", null);
    }

    @Override
    protected boolean runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        if (!sender.hasPermission("epicspawners.openshop")) {
            sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
            return true;
        }
        Player p = (Player) sender;
        instance.getShop().open(p, 1);
        return false;
    }
}
