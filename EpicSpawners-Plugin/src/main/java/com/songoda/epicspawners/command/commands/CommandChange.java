package com.songoda.epicspawners.command.commands;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.References;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.command.AbstractCommand;
import com.songoda.epicspawners.spawners.spawner.ESpawnerStack;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class CommandChange extends AbstractCommand {

    public CommandChange(AbstractCommand abstractCommand) {
        super("change", abstractCommand, true);
    }

    @Override
    protected ReturnType runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        if (args.length != 2) return ReturnType.SYNTAX_ERROR;
        if (!sender.hasPermission("epicspawners.admin") && !sender.hasPermission("epicspawners.change.*") && !sender.hasPermission("epicspawners.change." + args[1].toUpperCase())) {
            sender.sendMessage(References.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
            return ReturnType.FAILURE;
        }
        Player player = (Player) sender;
        Block block = player.getTargetBlock(null, 200);

        if (block.getType() != Material.SPAWNER) {
            sender.sendMessage(Methods.formatText(References.getPrefix() + "&cThis is not a spawner."));
            return ReturnType.FAILURE;
        }

        Spawner spawner = instance.getSpawnerManager().getSpawnerFromWorld(block.getLocation());

        SpawnerData data = null;
        for (SpawnerData spawnerData : instance.getSpawnerManager().getAllSpawnerData()) {
            String input = args[1].toUpperCase().replace("_", "").replace(" ", "");
            String compare = spawnerData.getIdentifyingName().toUpperCase().replace("_", "").replace(" ", "");
            if (input.equals(compare))
                data = spawnerData;
        }

        if (data == null) {
            player.sendMessage("This type does not exist.");
            return ReturnType.FAILURE;
        }

        try {
            SpawnerStack stack = new ESpawnerStack(data, spawner.getSpawnerDataCount());
            spawner.clearSpawnerStacks();
            spawner.addSpawnerStack(stack);
            spawner.getSpawnerStacks();
            try {
                spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf(args[1].toUpperCase()));
            } catch (Exception ex) {
                spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf("PIG"));
            }
            spawner.getCreatureSpawner().update();
            if (instance.getHologram() != null)
                instance.getHologram().processChange(block);
            sender.sendMessage(Methods.formatText(References.getPrefix() + "&7Successfully changed this spawner to &6" + args[1] + "&7."));
            return ReturnType.SUCCESS;
        } catch (Exception ee) {
            sender.sendMessage(Methods.formatText(References.getPrefix() + "&7That entity does not exist."));
            return ReturnType.FAILURE;
        }
    }

    @Override
    public String getPermissionNode() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/es change <Type>";
    }

    @Override
    public String getDescription() {
        return "Changes the entity for the spawner you are looking at.";
    }
}
