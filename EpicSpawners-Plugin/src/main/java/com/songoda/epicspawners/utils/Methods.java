package com.songoda.epicspawners.utils;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

/**
 * Created by songoda on 2/24/2017.
 */
@SuppressWarnings("deprecation")
public class Methods {

    public static void takeItem(Player player, int amount) {
        if (player.getGameMode() == GameMode.CREATIVE) return;

        ItemStack item = player.getInventory().getItemInHand();
        if (item == null) return;

        int result = item.getAmount() - amount;
        item.setAmount(result);

        player.setItemInHand(result > 0 ? item : null);
    }

    public static String getBoostCost(int time, int amount) {
        StringBuilder cost = new StringBuilder("&6&l");
        String[] parts = EpicSpawnersPlugin.getInstance().getConfig().getString("Spawner Boosting.Item Charged For A Boost").split(":");

        String type = parts[0];
        String multi = parts[1];

        int co = boostCost(multi, time, amount);

        if (!type.equals("ECO") && !type.equals("XP")) {
            cost.append(co).append(" &7").append(WordUtils.capitalizeFully(type));
            if (co != 1) {
                cost.append('s');
            }
        } else if (type.equals("ECO")) {
            cost.append('$').append(TextComponent.formatEconomy(co));
        } else if (type.equals("XP")) {
            cost.append(co).append(" &7Levels");
        }

        return cost.toString();
    }

    public static int boostCost(String multi, int time, int amt) {
        return (int) Math.ceil(NumberUtils.toDouble(multi, 1) * time * amt);
    }

    public static String compileName(SpawnerData data, int multi, boolean full) {
        try {
            EpicSpawnersPlugin plugin = EpicSpawnersPlugin.getInstance();

            String nameFormat = plugin.getConfig().getString("Main.Spawner Name Format");
            String displayName = data.getDisplayName();

            nameFormat = nameFormat.replace("{TYPE}", displayName);

            if ((multi > 1 || plugin.getConfig().getBoolean("Main.Display Level In Spawner Title If Level 1") || plugin.getConfig().getBoolean("Main.Named Spawners Tiers")) && multi >= 0) {
                if (plugin.getConfig().getBoolean("Main.Named Spawners Tiers") && plugin.getConfig().getStringList("Main.Tier Names").size() >= multi) {
                    nameFormat = nameFormat.replace("{AMT}", plugin.getConfig().getStringList("Main.Tier Names").get(multi - 1));
                } else {
                    nameFormat = nameFormat.replace("{AMT}", Integer.toString(multi));
                }
                nameFormat = nameFormat.replace("[", "").replace("]", "");
            } else {
                nameFormat = nameFormat.replaceAll("\\[.*?]", "");
            }


            StringBuilder hidden = new StringBuilder();
            for (char c : String.valueOf(multi).toCharArray()) hidden.append(";").append(c);
            String multiStr = hidden.toString();

            hidden = new StringBuilder();
            for (char c : String.valueOf(data.getUUID()).toCharArray()) hidden.append(";").append(c);
            String uuidStr = hidden.toString();

            String info = "";
            if (full) {
                info += TextComponent.convertToInvisibleString(uuidStr + ":" + multiStr + ":");
            }

            return info + TextComponent.formatText(nameFormat).trim();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public static ItemStack getGlass() {
        try {
            FileConfiguration config = EpicSpawnersPlugin.getInstance().getConfig();
            return Arconix.pl().getApi().getGUI().getGlass(config.getBoolean("Interfaces.Replace Glass Type 1 With Rainbow Glass"), config.getInt("Interfaces.Glass Type 1"));
        } catch (Exception e) {
            Debugger.runReport(e);
        }

        return null;
    }

    public static ItemStack getBackgroundGlass(boolean type) {
        try {
            EpicSpawnersPlugin plugin = EpicSpawnersPlugin.getInstance();
            return Arconix.pl().getApi().getGUI().getGlass(false, plugin.getConfig().getInt("Interfaces.Glass Type " + (type ? 2 : 3)));
        } catch (Exception e) {
            Debugger.runReport(e);
        }

        return null;
    }

    public static String getTypeFromString(String type) {
        return (type != null) ? ChatColor.stripColor(WordUtils.capitalizeFully(type.replace("_", " "))) : null;
    }

    public static String restoreType(String type) {
        return (type != null) ? type.replace(" ", "_").toUpperCase() : null;
    }

    public static boolean isAir(Material type) {
        return type == Material.AIR || type.name().contains("PRESSURE");
    }

}