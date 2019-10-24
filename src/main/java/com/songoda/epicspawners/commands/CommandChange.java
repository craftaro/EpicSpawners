package com.songoda.epicspawners.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.utils.Methods;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CommandChange extends AbstractCommand {

    private final EpicSpawners plugin;

    public CommandChange(EpicSpawners plugin) {
        super(true, "change");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 1) return ReturnType.SYNTAX_ERROR;
        if (!sender.hasPermission("epicspawners.admin") && !sender.hasPermission("epicspawners.change.*") && !sender.hasPermission("epicspawners.change." + args[0].toUpperCase())) {
            plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }
        Player player = (Player) sender;
        Block block = player.getTargetBlock(null, 200);

        if (block.getType() != CompatibleMaterial.SPAWNER.getMaterial()) {
            plugin.getLocale().newMessage("&cThis is not a spawner.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        Spawner spawner = plugin.getSpawnerManager().getSpawnerFromWorld(block.getLocation());

        SpawnerData data = null;
        for (SpawnerData spawnerData : plugin.getSpawnerManager().getAllSpawnerData()) {
            String input = args[0].toUpperCase().replace("_", "").replace(" ", "");
            String compare = spawnerData.getIdentifyingName().toUpperCase().replace("_", "").replace(" ", "");
            if (input.equals(compare))
                data = spawnerData;
        }

        if (data == null) {
            player.sendMessage("This type does not exist.");
            return ReturnType.FAILURE;
        }

        try {
            SpawnerStack stack = new SpawnerStack(data, spawner.getSpawnerDataCount());
            spawner.clearSpawnerStacks();
            spawner.addSpawnerStack(stack);
            spawner.getSpawnerStacks();
            try {
                spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf(args[0].toUpperCase()));
            } catch (Exception ex) {
                spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf("PIG"));
            }
            spawner.getCreatureSpawner().update();
            plugin.updateHologram(spawner);
            plugin.getLocale().newMessage("&7Successfully changed this spawner to &6" + args[0] + "&7.")
                    .sendPrefixedMessage(sender);
            return ReturnType.SUCCESS;
        } catch (Exception ee) {
            plugin.getLocale().newMessage("&7That entity does not exist.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1) {
            return Methods.convertToList(plugin.getSpawnerManager().getAllSpawnerData(), (spawnerData) -> spawnerData.getIdentifyingName().replace(" ", "_"));
        }
        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "change <Type>";
    }

    @Override
    public String getDescription() {
        return "Changes the entity for the spawner you are looking at.";
    }
}
