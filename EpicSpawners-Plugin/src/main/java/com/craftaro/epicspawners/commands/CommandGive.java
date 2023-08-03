package com.craftaro.epicspawners.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.core.utils.NumberUtils;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.settings.Settings;
import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CommandGive extends AbstractCommand {
    private final EpicSpawners plugin;

    public CommandGive(EpicSpawners plugin) {
        super(CommandType.CONSOLE_OK, "give");
        this.plugin = plugin;
    }

    @Override
    protected AbstractCommand.ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length < 2 || args.length > 5) {
            return ReturnType.SYNTAX_ERROR;
        }

        if (Bukkit.getPlayerExact(args[0]) == null && !args[0].toLowerCase().equals("all")) {
            plugin.getLocale().newMessage("&cThat username does not exist, or the user is not online!").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        SpawnerData data = null;
        for (SpawnerData spawnerData : plugin.getSpawnerManager().getAllSpawnerData()) {
            String input = args[1].toUpperCase().replace("_", "").replace(" ", "");
            String compare = spawnerData.getIdentifyingName().toUpperCase().replace("_", "").replace(" ", "");
            if (input.equals(compare)) {
                data = spawnerData;
            }
        }

        if (data == null && !args[1].equalsIgnoreCase("random")) {
            plugin.getLocale().newMessage("&7The entity Type &6" + args[1] + " &7does not exist. Try one of these:").sendPrefixedMessage(sender);
            StringBuilder list = new StringBuilder();

            for (SpawnerData spawnerData : plugin.getSpawnerManager().getAllSpawnerData()) {
                list.append(spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_")).append("&7, &6");
            }

            sender.sendMessage(TextUtils.formatText("&6" + list));
            return ReturnType.FAILURE;
        }

        if (args[1].equalsIgnoreCase("random")) {
            Collection<SpawnerData> list = plugin.getSpawnerManager().getAllEnabledSpawnerData();
            Random rand = new Random();
            data = Iterables.get(list, rand.nextInt(list.size()));
        }

        if (args.length == 2) {
            giveSpawner(args[0], data.getFirstTier(), 1, 1);
            return ReturnType.SUCCESS;
        }

        SpawnerTier tier = data.getFirstTier();
        SpawnerTier foundTier = data.getTier(args[2].trim());
        if (foundTier != null) {
            tier = foundTier;
        }

        if (args.length == 3) {
            giveSpawner(args[0], tier, 1, 1);
            return ReturnType.SUCCESS;
        }

        int amount;
        int stackSize = 1;

        if (!NumberUtils.isInt(args[3])) {
            plugin.getLocale().newMessage("&6" + args[3] + "&7 is not a number.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        } else {
            amount = Integer.parseInt(args[3]);
        }

        if (args.length > 4) {
            if (!NumberUtils.isInt(args[4])) {
                plugin.getLocale().newMessage("&6" + args[4] + "&7 is not a number.").sendPrefixedMessage(sender);
                return ReturnType.FAILURE;
            } else {
                stackSize = Integer.parseInt(args[4]);
            }
        }

        if (stackSize > Settings.SPAWNERS_MAX.getInt()) {
            plugin.getLocale().newMessage("&7The stack size &6" + stackSize + "&7 is above this spawner types maximum.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        giveSpawner(args[0], tier, stackSize, amount);
        return ReturnType.SUCCESS;
    }

    private void giveSpawner(String who, SpawnerTier tier, int stackSize, int amt) {
        ItemStack spawnerItem = tier.toItemStack(amt, Math.max(1, stackSize));
        if (who.toLowerCase().equals("all")) {
            for (Player pl : Bukkit.getOnlinePlayers()) {
                pl.getInventory().addItem(spawnerItem);
                plugin.getLocale().getMessage("command.give.success").processPlaceholder("amount", amt).processPlaceholder("type", tier.getCompiledDisplayName(false, stackSize)).sendPrefixedMessage(pl);
            }
        } else {
            Player pl = Bukkit.getPlayerExact(who);
            pl.getInventory().addItem(spawnerItem);
            plugin.getLocale().getMessage("command.give.success").processPlaceholder("amount", amt).processPlaceholder("type", tier.getCompiledDisplayName(false, stackSize)).sendPrefixedMessage(pl);

        }
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1) {
            List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            players.add("All");
            return players;
        } else if (args.length == 2) {
            List<String> spawners = plugin.getSpawnerManager().getAllSpawnerData().stream()
                    .map(spawnerData -> spawnerData.getIdentifyingName().replace(" ", "_"))
                    .collect(Collectors.toList());
            spawners.add("random");
            return spawners;
        } else if (args.length == 3) {
            SpawnerData data = null;
            for (SpawnerData spawnerData : plugin.getSpawnerManager().getAllSpawnerData()) {
                String input = args[1].toUpperCase().replace("_", "").replace(" ", "");
                String compare = spawnerData.getIdentifyingName().toUpperCase().replace("_", "").replace(" ", "");
                if (input.equals(compare)) {
                    data = spawnerData;
                }
            }

            if (data == null) {
                return Collections.emptyList();
            }
            return data.getTiers().stream()
                    .map(spawnerTier -> spawnerTier.getIdentifyingName().replace(" ", "_"))
                    .collect(Collectors.toList());
        } else if (args.length == 4) {
            int max = Settings.SPAWNERS_MAX.getInt();
            List<String> values;

            if (max <= 0) {
                values = new ArrayList<>(1);
                values.add(String.valueOf(1));
            } else {
                values = new ArrayList<>(max);
                for (int i = 1; i <= max; i++) {
                    values.add(String.valueOf(i));
                }
            }

            return values;
        } else if (args.length == 5) {
            return Arrays.asList("1", "2", "3", "4", "5");
        }
        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.admin.give";
    }

    @Override
    public String getSyntax() {
        return "give [player/all] [spawnertype/random] [tier] [amount] [stack-size]";
    }

    @Override
    public String getDescription() {
        return "Gives an operator the ability to spawn a spawner of his or her choice.";
    }
}
