package com.songoda.epicspawners.command.commands;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.References;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.command.AbstractCommand;
import com.songoda.epicspawners.gui.GUISpawnerStats;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Map;

public class CommandSpawnerStats extends AbstractCommand {

    public CommandSpawnerStats() {
        super("spawnerstats", null, true);
    }

    @Override
    protected ReturnType runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        Player player = (Player) sender;

        if (instance.getPlayerActionManager().getPlayerAction(player).getEntityKills().size() == 0) {
            player.sendMessage(References.getPrefix() + instance.getLocale().getMessage("interface.spawnerstats.nokills"));
            return AbstractCommand.ReturnType.SUCCESS;
        }


        new GUISpawnerStats(instance, player);
        
        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.stats";
    }

    @Override
    public String getSyntax() {
        return "/SpawnerStats";
    }

    @Override
    public String getDescription() {
        return "This allows you to boost the amount of spawns that are got from placed spawners.";
    }
}
