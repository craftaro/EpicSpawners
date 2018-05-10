package com.songoda.epicspawners.handlers;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.boost.BoostData;
import com.songoda.epicspawners.boost.BoostType;
import com.songoda.epicspawners.spawners.object.Spawner;
import com.songoda.epicspawners.spawners.object.SpawnerData;
import com.songoda.epicspawners.spawners.object.SpawnerStack;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Created by songoda on 2/24/2017.
 */
@SuppressWarnings({"ConstantConditions", "Duplicates"})
public class CommandHandler implements CommandExecutor {

    private EpicSpawners instance;

    public CommandHandler(EpicSpawners instance) {
        this.instance = instance;
    }

    public void help(CommandSender sender, int page) {
        try {
            sender.sendMessage("");
            int of = 3;
            if (!sender.hasPermission("epicspawners.admin")) {
                of = 1;
            }

            sender.sendMessage(Arconix.pl().getApi().format().formatText("&7Page: &a" + page + " of " + of + " ======================"));
            if (page == 1) {
                sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&7" + instance.getDescription().getVersion() + " Created by &5&l&oBrianna"));
                sender.sendMessage(Arconix.pl().getApi().format().formatText(" &8- &aes help &7Displays this page."));
                if (sender.hasPermission("epicspawners.admin")) {
                    sender.sendMessage(Arconix.pl().getApi().format().formatText(" &8- &aes editor &7Opens the spawner editor."));
                }
                sender.sendMessage(Arconix.pl().getApi().format().formatText(" &8- &aspawnershop &7Opens the spawner shop."));
                sender.sendMessage(Arconix.pl().getApi().format().formatText(" &8- &aspawnerstats &7Allows a player to overview their current EpicSpawners stats and see how many kills they have left to get a specific spawner drop."));
            } else if (page == 2 && sender.hasPermission("epicspawners.admin")) {
                sender.sendMessage(Arconix.pl().getApi().format().formatText(" &8- &aes convertOverview <type> &7Changes the entity for the spawner you are looking at."));
                sender.sendMessage(Arconix.pl().getApi().format().formatText(" &8- &aes give [player/all] [spawnertype/random] [multiplier] [amount] &7Gives an operator the ability to spawn a spawner of his or her choice."));
                sender.sendMessage(Arconix.pl().getApi().format().formatText(" &8- &aes setshop <type> &7Assigns a spawner shop to the block you are looking at."));
            } else if (page == 3 && sender.hasPermission("epicspawners.admin")) {
                sender.sendMessage(Arconix.pl().getApi().format().formatText(" &8- &aes settings &7Edit the EpicSpawners Settings."));
                sender.sendMessage(Arconix.pl().getApi().format().formatText(" &8- &aes boost <p:player, f:faction, t:town, i:islandOwner> <amount> [m:minute, h:hour, d:day, y:year] &7This allows you to boost the amount of spawns that are got from placed spawners."));
                sender.sendMessage(Arconix.pl().getApi().format().formatText(" &8- &aes removeboosts <p:player, f:faction, t:town, i:islandOwner> &7This allows you to remove boosts."));
                sender.sendMessage(Arconix.pl().getApi().format().formatText(" &8- &aes removeshop &7Unassigns a spawner shop to the block you are looking at."));
            } else {
                sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "That page does not exist!"));
            }
            sender.sendMessage("");
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        try {
            if (cmd.getName().equalsIgnoreCase("EpicSpawners")) {
                if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                    if (args.length == 2) {
                        help(sender, Integer.parseInt(args[1]));
                    } else {
                        help(sender, 1);
                    }
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("epicspawners.admin")) {
                        sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
                    } else {
                        instance.reload();
                        sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&8Configuration and Language files reloaded."));
                    }
                } else if (args[0].equalsIgnoreCase("change")) {
                    if (!sender.hasPermission("epicspawners.admin") && !sender.hasPermission("epicspawners.change.*") && !sender.hasPermission("epicspawners.change." + args[1].toUpperCase())) {
                        sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
                    } else {
                        Player p = (Player) sender;
                        Block b = p.getTargetBlock(null, 200);

                        if (b.getType().equals(Material.MOB_SPAWNER)) {
                            Spawner spawner = instance.getSpawnerManager().getSpawnerFromWorld(b.getLocation());

                            try {
                                SpawnerStack stack = new SpawnerStack(instance.getSpawnerManager().getSpawnerType(Methods.restoreType(args[1]).toLowerCase()), spawner.getSpawnerMultiplier());
                                spawner.clearSpawnerStacks();
                                spawner.addSpawnerType(stack);
                                spawner.getSpawnerStacks();
                                try {
                                    spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf(args[1].toUpperCase()));
                                } catch (Exception ex) {
                                    spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf("DROPPED_ITEM"));
                                }
                                spawner.getCreatureSpawner().update();
                                instance.getHologramHandler().processChange(b);
                                sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&7Successfully changed this spawner to &6" + args[1] + "&7."));
                            } catch (Exception ee) {
                                sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&7That entity does not exist."));
                            }
                        } else {
                            sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&cThis is not a spawner."));
                        }
                    }
                } else if (args[0].equalsIgnoreCase("editor")) {
                    if (!sender.hasPermission("epicspawners.admin")) {
                        sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
                    } else {
                        instance.getSpawnerEditor().openSpawnerSelector((Player) sender, 1);
                    }
                } else if (args[0].equalsIgnoreCase("removeboosts")) {
                    if (!sender.hasPermission("epicspawners.admin")) {
                        sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
                    } else {
                        if (args.length == 2) {
                            if (instance.dataFile.getConfig().contains("data.boosts")) {
                                if (args[1].contains("p:") || args[1].contains("player:") ||
                                        args[1].contains("f:") || args[1].contains("faction:") ||
                                        args[1].contains("t:") || args[1].contains("town:") ||
                                        args[1].contains("i:") || args[1].contains("island:")) {
                                    String[] arr = (args[1]).split(":");

                                    String type = "";
                                    String att = "";

                                    if (arr[0].equalsIgnoreCase("p") || arr[0].equalsIgnoreCase("player")) {
                                        if (Bukkit.getOfflinePlayer(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&cThat player does not exist..."));
                                        } else {
                                            type = "player";
                                            att = Bukkit.getOfflinePlayer(arr[1]).getUniqueId().toString();
                                        }
                                    } else if (arr[0].equalsIgnoreCase("f") || arr[0].equalsIgnoreCase("faction")) {
                                        if (instance.getHookHandler().getFactionId(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&cThat faction does not exist..."));
                                            return true;
                                        } else {
                                            type = "faction";
                                            att = instance.getHookHandler().getFactionId(arr[1]);
                                        }
                                    } else if (arr[0].equalsIgnoreCase("t") || arr[0].equalsIgnoreCase("town")) {
                                        if (instance.getHookHandler().getFactionId(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&cThat town does not exist..."));
                                            return true;
                                        } else {
                                            type = "town";
                                            att = instance.getHookHandler().getTownId(arr[1]);
                                        }
                                    } else if (arr[0].equalsIgnoreCase("i") || arr[0].equalsIgnoreCase("island")) {
                                        if (instance.getHookHandler().getFactionId(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&cThat island does not exist..."));
                                            return true;
                                        } else {
                                            type = "island";
                                            att = instance.getHookHandler().getIslandId(arr[1]);
                                        }
                                    }

                                    int removes = instance.getApi().removeBoosts(type, att);

                                    if (removes == 0) {
                                        sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&cNo boosts were found matching your search."));
                                    } else {
                                        if (removes == 1)
                                            sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&6" + removes + " &7boost was removed."));
                                        else
                                            sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&6" + removes + " &7boosts were removed."));
                                    }
                                } else {
                                    sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&6" + args[1] + " &7this is incorrect"));
                                }
                            } else {
                                sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&cThe boost database is currently empty."));
                            }
                        } else {
                            sender.sendMessage(instance.references.getPrefix() + Arconix.pl().getApi().format().formatText("&7Syntax error..."));
                        }
                    }
                } else if (args[0].equalsIgnoreCase("boost")) {
                    if (!sender.hasPermission("epicspawners.admin")) {
                        sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
                    } else {
                        if (args.length >= 3) {
                            if (args[1].contains("p:") || args[1].contains("player:") ||
                                    args[1].contains("f:") || args[1].contains("faction:") ||
                                    args[1].contains("t:") || args[1].contains("town:") ||
                                    args[1].contains("i:") || args[1].contains("island:")) {
                                String[] arr = (args[1]).split(":");
                                if (!Arconix.pl().getApi().doMath().isNumeric(args[2])) {
                                    sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&6" + args[2] + " &7is not a number..."));
                                } else {

                                    Calendar c = Calendar.getInstance();
                                    Date currentDate = new Date();
                                    c.setTime(currentDate);

                                    String response = " &6" + arr[1] + "&7 has been given a spawner boost of &6" + args[2];

                                    if (args.length > 3) {
                                        if (args[3].contains("m:")) {
                                            String[] arr2 = (args[3]).split(":");
                                            c.add(Calendar.MINUTE, Integer.parseInt(arr2[1]));
                                            response += " &7for &6" + arr2[1] + " minutes&7.";
                                        } else if (args[3].contains("h:")) {
                                            String[] arr2 = (args[3]).split(":");
                                            c.add(Calendar.HOUR, Integer.parseInt(arr2[1]));
                                            response += " &7for &6" + arr2[1] + " hours&7.";
                                        } else if (args[3].contains("d:")) {
                                            String[] arr2 = (args[3]).split(":");
                                            c.add(Calendar.HOUR, Integer.parseInt(arr2[1]) * 24);
                                            response += " &7for &6" + arr2[1] + " days&7.";
                                        } else if (args[3].contains("y:")) {
                                            String[] arr2 = (args[3]).split(":");
                                            c.add(Calendar.YEAR, Integer.parseInt(arr2[1]));
                                            response += " &7for &6" + arr2[1] + " years&7.";
                                        } else {
                                            sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&7" + args[3] + " &7is invalid."));
                                            return true;
                                        }
                                    } else {
                                        c.add(Calendar.YEAR, 10);
                                        response += "&6.";
                                    }
                                    String uuid = UUID.randomUUID().toString();

                                    String start = "&7";

                                    BoostType boostType = null;

                                    Object boostObject = null;

                                    if (arr[0].equalsIgnoreCase("p") || arr[0].equalsIgnoreCase("player")) {
                                        if (Bukkit.getOfflinePlayer(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&cThat player does not exist..."));
                                        } else {
                                            start += "The player";
                                            boostType = BoostType.PLAYER;
                                            boostObject = Bukkit.getOfflinePlayer(arr[1]).getUniqueId().toString();
                                        }
                                    } else if (arr[0].equalsIgnoreCase("f") || arr[0].equalsIgnoreCase("faction")) {
                                        if (instance.getHookHandler().getFactionId(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&cThat faction does not exist..."));
                                            return true;
                                        } else {
                                            start += "The faction";
                                            boostType = BoostType.FACTION;
                                            boostObject = instance.getHookHandler().getFactionId(arr[1]);
                                        }
                                    } else if (arr[0].equalsIgnoreCase("t") || arr[0].equalsIgnoreCase("town")) {
                                        if (instance.getHookHandler().getTownId(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&cThat town does not exist..."));
                                            return true;
                                        } else {
                                            start += "The town";
                                            boostType = BoostType.TOWN;
                                            boostObject = instance.getHookHandler().getTownId(arr[1]);
                                        }
                                    } else if (arr[0].equalsIgnoreCase("i") || arr[0].equalsIgnoreCase("island")) {
                                        if (instance.getHookHandler().getIslandId(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&cThat island does not exist..."));
                                            return true;
                                        } else {
                                            start += "The island";
                                            boostType = BoostType.ISLAND;
                                            boostObject = instance.getHookHandler().getIslandId(arr[1]);
                                        }
                                    }

                                    if (boostType == null || boostObject == null) {
                                        sender.sendMessage("Syntax error.");
                                        return true;
                                    }

                                    BoostData boostData = new BoostData(boostType, Integer.parseInt(args[2]), c.getTime().getTime(), boostObject);
                                    instance.getBoostManager().addBoostToSpawner(boostData);
                                    sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + start + response));
                                }
                            } else {
                                sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&6" + args[1] + " &7this is incorrect"));
                            }
                        } else {
                            sender.sendMessage(instance.references.getPrefix() + Arconix.pl().getApi().format().formatText("&7Syntax error..."));
                        }
                    }
                } else if (args[0].equalsIgnoreCase("settings")) {
                    if (!sender.hasPermission("epicspawners.admin")) {
                        sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
                    } else {
                        Player p = (Player) sender;
                        instance.getSettingsManager().openSettingsManager(p);
                    }
                } else if (args[0].equalsIgnoreCase("setshop")) {
                    if (args.length >= 2) {
                        Player p = (Player) sender;
                        if (!sender.hasPermission("epicspawners.admin")) {
                            sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
                        } else {

                            String type = null;
                            ConfigurationSection css = instance.spawnerFile.getConfig().getConfigurationSection("Entities");
                            for (String key : css.getKeys(false)) {
                                String input = args[1].toUpperCase().replace("_", "").replace(" ", "");
                                String compare = key.toUpperCase().replace("_", "").replace(" ", "");
                                if (input.equals(compare))
                                    type = key;
                            }

                            if (type == null) {
                                sender.sendMessage(instance.references.getPrefix() + Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&7The entity type &6" + args[1] + " &7does not exist. Try one of these:"));
                                StringBuilder list = new StringBuilder();
                                for (final EntityType value : EntityType.values()) {
                                    if (value.isSpawnable() && value.isAlive()) {
                                        list.append(value.toString()).append("&7, &6");
                                    }
                                }
                                sender.sendMessage(Arconix.pl().getApi().format().formatText("&6" + list));
                            } else {
                                Entity ent = null;
                                if (Arconix.pl().getApi().getPlayer(p).getTarget() != null) {
                                    if (ent instanceof ItemFrame) {
                                        ent = Arconix.pl().getApi().getPlayer(p).getTarget();
                                    }
                                }
                                if (ent != null) {
                                    instance.dataFile.getConfig().set("data.entityshop." + ent.getUniqueId().toString(), type);
                                    sender.sendMessage(instance.references.getPrefix() + Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&aShop setup successfully."));
                                } else {
                                    if (p.getTargetBlock(null, 200) != null) {
                                        Block b = p.getTargetBlock(null, 200);
                                        String loc = Arconix.pl().getApi().serialize().serializeLocation(b);
                                        instance.dataFile.getConfig().set("data.blockshop." + loc, args[1]);
                                        sender.sendMessage(instance.references.getPrefix() + Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&aShop setup successfully."));
                                    }
                                }
                            }
                        }
                    }
                } else if (args[0].equalsIgnoreCase("removeshop")) {
                    if (!sender.hasPermission("epicspawners.admin")) {
                        sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
                    } else {
                        Player p = (Player) sender;
                        Entity ent = null;
                        if (Arconix.pl().getApi().getPlayer(p).getTarget() != null &&
                                ent.getType() == EntityType.ITEM_FRAME) {
                            ent = Arconix.pl().getApi().getPlayer(p).getTarget();
                        }
                        if (ent != null) {
                            instance.dataFile.getConfig().set("data.entityshop." + ent.getUniqueId().toString(), null);
                            return true;
                        }
                        Block b = p.getTargetBlock(null, 200);
                        String loc = Arconix.pl().getApi().serialize().serializeLocation(b);
                        instance.dataFile.getConfig().set("data.blockshop." + loc, null);
                        sender.sendMessage(instance.references.getPrefix() + Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&aShop removed successfully."));
                    }
                } else if (args[0].equalsIgnoreCase("shop")) {
                    if (!sender.hasPermission("epicspawners.openshop")) {
                        sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
                        return true;
                    }
                    Player p = (Player) sender;
                    instance.getShop().open(p, 1);
                } else if (args[0].equalsIgnoreCase("give")) {
                    if (args.length <= 3 && args.length != 6) {
                        sender.sendMessage(instance.references.getPrefix() + Arconix.pl().getApi().format().formatText("&7Syntax error..."));
                        return true;
                    }
                    if (!sender.hasPermission("epicspawners.admin")) {
                        sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
                        return true;
                    }
                    if (Bukkit.getPlayerExact(args[1]) == null && !args[1].toLowerCase().equals("all")) {
                        sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&cThat username does not exist, or the user is not online!"));
                        return true;
                    }
                    int multi = 0;

                    String type = null;
                    for (SpawnerData spawnerData : instance.getSpawnerManager().getSpawnerTypes().values()) {
                        String input = args[2].toUpperCase().replace("_", "").replace(" ", "");
                        String compare = spawnerData.getName().toUpperCase().replace("_", "").replace(" ", "");
                        if (input.equals(compare))
                            type = spawnerData.getName();
                    }

                    if (type == null && !args[2].equalsIgnoreCase("random")) {
                        sender.sendMessage(instance.references.getPrefix() + Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&7The entity type &6" + args[2] + " &7does not exist. Try one of these:"));
                        StringBuilder list = new StringBuilder();

                        for (SpawnerData spawnerData : instance.getSpawnerManager().getSpawnerTypes().values()) {
                            list.append(spawnerData.getName().toUpperCase().replace(" ", "_")).append("&7, &6");
                        }
                        sender.sendMessage(Arconix.pl().getApi().format().formatText("&6" + list));
                    } else {
                        if (args[2].equalsIgnoreCase("random")) {
                            List<String> list = new ArrayList<>();
                            for (SpawnerData spawnerData : instance.getSpawnerManager().getSpawnerTypes().values()) {
                                if (spawnerData.isActive()) {
                                    list.add(spawnerData.getName().toUpperCase().replace(" ", "_"));
                                }
                            }
                            Random rand = new Random();
                            int n = rand.nextInt(list.size() - 1);
                            type = list.get(n);
                        }
                        if (args.length == 4) {
                            if (!Arconix.pl().getApi().doMath().isNumeric(args[3])) {
                                sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&6" + args[3] + "&7 is not a number."));
                                return true;
                            }
                            int amt = Integer.parseInt(args[3]);
                            ItemStack spawnerItem = instance.api.newSpawnerItem(type, 0, Integer.parseInt(args[3]));
                            if (args[1].toLowerCase().equals("all")) {
                                for (Player pl : Bukkit.getOnlinePlayers()) {
                                    pl.getInventory().addItem(spawnerItem);
                                    pl.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + instance.getLocale().getMessage("command.give.success", amt, Methods.compileName(type, multi, false))));
                                }
                            } else {
                                Player pl = Bukkit.getPlayerExact(args[1]);
                                pl.getInventory().addItem(spawnerItem);
                                pl.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + instance.getLocale().getMessage("command.give.success", amt, Methods.compileName(type, multi, false))));

                            }
                        } else {
                            if (!Arconix.pl().getApi().doMath().isNumeric(args[3])) {
                                sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&6" + args[3] + "&7 is not a number."));
                                return true;
                            }
                            if (!Arconix.pl().getApi().doMath().isNumeric(args[3])) {
                                sender.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&6" + args[4] + "&7 is not a number."));
                                return true;
                            }
                            int amt = Integer.parseInt(args[3]);
                            multi = Integer.parseInt(args[4]);
                            ItemStack spawnerItem = instance.api.newSpawnerItem(type, multi, amt);
                            if (args[1].toLowerCase().equals("all")) {
                                for (Player pl : Bukkit.getOnlinePlayers()) {
                                    pl.getInventory().addItem(spawnerItem);
                                    pl.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + instance.getLocale().getMessage("command.give.success", amt, Methods.compileName(type, multi, false))));
                                }
                            } else {
                                Player pl = Bukkit.getPlayerExact(args[1]);
                                pl.getInventory().addItem(spawnerItem);
                                pl.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + instance.getLocale().getMessage("command.give.success", amt, Methods.compileName(type, multi, false))));

                            }
                        }
                    }
                }
            } else if (cmd.getName().equalsIgnoreCase("SpawnerShop")) {
                if (!sender.hasPermission("epicspawners.openshop")) {
                    sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
                } else {
                    Player p = (Player) sender;
                    instance.getShop().open(p, 1);
                }
            } else if (cmd.getName().equalsIgnoreCase("SpawnerStats")) {
                if (!sender.hasPermission("epicspawners.stats")) {
                    sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
                    return true;
                }

                Player p = (Player) sender;

                int size = 0;

                for (Map.Entry<EntityType, Integer> entry : instance.getPlayerActionManager().getPlayerAction(p).getEntityKills().entrySet()) {
                    if (instance.getSpawnerManager().getSpawnerType(Methods.getType(entry.getKey())).getKillGoal() != 0) {
                        size++;
                    }
                }

                String title = instance.getLocale().getMessage("interface.spawnerstats.title");

                Inventory i = Bukkit.createInventory(null, 54, title);
                if (size <= 9) {
                    i = Bukkit.createInventory(null, 18, title);
                } else if (size <= 9) {
                    i = Bukkit.createInventory(null, 27, title);
                } else if (size <= 18) {
                    i = Bukkit.createInventory(null, 36, title);
                } else if (size <= 27) {
                    i = Bukkit.createInventory(null, 45, title);
                }

                int num = 0;
                while (num != 9) {
                    i.setItem(num, Methods.getGlass());
                    num++;
                }
                ItemStack exit = new ItemStack(Material.valueOf(instance.getConfig().getString("Interfaces.Exit Icon")), 1);
                ItemMeta exitmeta = exit.getItemMeta();
                exitmeta.setDisplayName(instance.getLocale().getMessage("general.nametag.exit"));
                exit.setItemMeta(exitmeta);
                i.setItem(8, exit);

                short place = 9;
                p.sendMessage("");


                if (instance.getPlayerActionManager().getPlayerAction(p).getEntityKills().size() == 0) {
                    p.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("interface.spawnerstats.nokills"));
                    return true;
                }

                p.sendMessage(instance.references.getPrefix());
                p.sendMessage(instance.getLocale().getMessage("interface.spawnerstats.prefix"));
                for (Map.Entry<EntityType, Integer> entry : instance.getPlayerActionManager().getPlayerAction(p).getEntityKills().entrySet()) {
                    int goal = instance.getConfig().getInt("Spawner Drops.Kills Needed for Drop");

                    String type = Methods.getType(entry.getKey());

                    int customGoal = instance.getSpawnerManager().getSpawnerType(type).getKillGoal();
                    if (customGoal != 0) goal = customGoal;

                    ItemStack it = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);

                    ItemStack item = instance.getHeads().addTexture(it, Methods.restoreType(type));

                    ItemMeta itemmeta = item.getItemMeta();
                    ArrayList<String> lore = new ArrayList<>();
                    itemmeta.setLore(lore);
                    itemmeta.setDisplayName(Arconix.pl().getApi().format().formatText("&6" + type + "&7: &e" + entry.getValue() + "&7/&e" + goal));
                    item.setItemMeta(itemmeta);
                    i.setItem(place, item);

                    place++;
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7- &6" + type + "&7: &e" + entry.getValue() + "&7/&e" + goal));
                }
                p.sendMessage(instance.getLocale().getMessage("interface.spawnerstats.ongoal"));

                p.sendMessage("");

                p.openInventory(i);

            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return true;
    }
}
