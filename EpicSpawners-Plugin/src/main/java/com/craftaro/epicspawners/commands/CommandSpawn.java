package com.craftaro.epicspawners.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.world.SSpawner;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandSpawn extends AbstractCommand {
    private final EpicSpawners plugin;

    public CommandSpawn(EpicSpawners plugin) {
        super(CommandType.PLAYER_ONLY, "spawn");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        Block block = player.getTargetBlock(null, 200);

        if (XMaterial.matchXMaterial(block.getType().name()).get() != XMaterial.SPAWNER) {
            this.plugin.getLocale().newMessage("&cThat is not a spawner...").sendPrefixedMessage(player);
            return ReturnType.FAILURE;
        }

        PlacedSpawner spawner = this.plugin.getSpawnerManager().getSpawnerFromWorld(block.getLocation());
        if (spawner == null) {
            //it is a vanilla spawner
            CreatureSpawner vanillaSpawner = (CreatureSpawner) block.getState();
            SSpawner creatureSpawner = new SSpawner(block.getLocation());
            creatureSpawner.spawn(vanillaSpawner.getSpawnCount(), vanillaSpawner.getSpawnedType());
        } else {
            //it is an epic spawner
            spawner.spawn();
        }
        this.plugin.getLocale().newMessage("&aSpawning successful.").sendPrefixedMessage(player);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.admin.spawn";
    }

    @Override
    public String getSyntax() {
        return "spawn";
    }

    @Override
    public String getDescription() {
        return "Force the spawner you are looking at to spawn so long as the spawn conditions are met.";
    }
}
