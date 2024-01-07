package com.craftaro.epicspawners.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandChange extends AbstractCommand {
    private final EpicSpawners plugin;

    public CommandChange(EpicSpawners plugin) {
        super(CommandType.PLAYER_ONLY, "change");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 1) {
            return ReturnType.SYNTAX_ERROR;
        }
        if (!sender.hasPermission("epicspawners.admin") && !sender.hasPermission("epicspawners.change.*") && !sender.hasPermission("epicspawners.change." + args[0].toUpperCase())) {
            plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }
        Player player = (Player) sender;
        Block block = player.getTargetBlock(null, 200);

        if (block.getType() != XMaterial.SPAWNER.parseMaterial()) {
            plugin.getLocale().newMessage("&cThis is not a spawner.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        PlacedSpawner spawner = plugin.getSpawnerManager().getSpawnerFromWorld(block.getLocation());

        if (spawner.getSpawnerStacks().size() > 1) {
            plugin.getLocale().newMessage("&cYou cannot convert an omni spawner...").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        SpawnerData data = null;
        for (SpawnerData spawnerData : plugin.getSpawnerManager().getAllSpawnerData()) {
            String input = args[0].toUpperCase().replace("_", "").replace(" ", "");
            String compare = spawnerData.getIdentifyingName().toUpperCase().replace("_", "").replace(" ", "");
            if (input.equals(compare)) {
                data = spawnerData;
            }
        }

        if (data == null) {
            player.sendMessage("This type does not exist.");
            return ReturnType.FAILURE;
        }

        spawner.getFirstStack().convert(data, player, sender.hasPermission("com/craftaro/epicspawners"));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1) {
            return plugin.getSpawnerManager().getAllSpawnerData().stream()
                    .map(spawnerData -> spawnerData.getIdentifyingName().replace(" ", "_"))
                    .collect(Collectors.toList());
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
