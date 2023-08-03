package com.craftaro.epicspawners.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.gui.SpawnerShopGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandSpawnerShop extends AbstractCommand {
    private final EpicSpawners plugin;

    public CommandSpawnerShop(EpicSpawners plugin) {
        super(CommandType.PLAYER_ONLY, "spawnershop");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        this.plugin.getGuiManager().showGUI((Player) sender, new SpawnerShopGui(this.plugin, (Player) sender));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.openshop";
    }

    @Override
    public String getSyntax() {
        return "/SpawnerShop";
    }

    @Override
    public String getDescription() {
        return "Opens the spawner shop.";
    }
}
