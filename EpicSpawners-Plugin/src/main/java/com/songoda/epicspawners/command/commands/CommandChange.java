package com.songoda.epicspawners.command.commands;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.command.AbstractCommand;
import com.songoda.epicspawners.spawners.spawner.ESpawnerStack;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class CommandChange extends AbstractCommand {

    public CommandChange(AbstractCommand abstractCommand) {
        super("change", null, abstractCommand);
    }

    @Override
    protected boolean runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command.");
            return true;
        }

        if (!sender.hasPermission("epicspawners.admin") && !sender.hasPermission("epicspawners.change.*") && !sender.hasPermission("epicspawners.change." + args[1].toUpperCase())) {
            sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
        } else {
            Player p = (Player) sender;
            Block b = p.getTargetBlock(null, 200);

            if (b.getType().equals(Material.SPAWNER)) {
                Spawner spawner = instance.getSpawnerManager().getSpawnerFromWorld(b.getLocation());

                SpawnerData data = null;
                for (SpawnerData spawnerData : instance.getSpawnerManager().getAllSpawnerData()) {
                    String input = args[1].toUpperCase().replace("_", "").replace(" ", "");
                    String compare = spawnerData.getIdentifyingName().toUpperCase().replace("_", "").replace(" ", "");
                    if (input.equals(compare))
                        data = spawnerData;
                }

                if (data == null) {
                    p.sendMessage("This type does not exist.");
                    return true;
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
                    instance.getHologramHandler().processChange(b);
                    sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&7Successfully changed this spawner to &6" + args[1] + "&7."));
                } catch (Exception ee) {
                    sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&7That entity does not exist."));
                }
            } else {
                sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&cThis is not a spawner."));
            }
        }
        return true;
    }
}
