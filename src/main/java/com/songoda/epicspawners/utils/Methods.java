package com.songoda.epicspawners.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by songoda on 2/24/2017.
 */
@SuppressWarnings("deprecation")
public class Methods {

    private static Map<String, Location> serializeCache = new HashMap<>();

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
        String[] parts = EpicSpawners.getInstance().getConfig().getString("Spawner Boosting.Item Charged For A Boost").split(":");

        String type = parts[0];
        String multi = parts[1];

        int co = boostCost(multi, time, amount);

        if (!type.equals("ECO") && !type.equals("XP")) {
            cost.append(co).append(" &7").append(WordUtils.capitalizeFully(type));
            if (co != 1) {
                cost.append('s');
            }
        } else if (type.equals("ECO")) {
            cost.append('$').append(formatEconomy(co));
        } else if (type.equals("XP")) {
            cost.append(co).append(" &7Levels");
        }

        return cost.toString();
    }

    public static int boostCost(String multi, int time, int amt) {
        return (int) Math.ceil(NumberUtils.toDouble(multi, 1) * time * amt);
    }

    public static String compileName(SpawnerData data, int multi, boolean full) {
        EpicSpawners plugin = EpicSpawners.getInstance();

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
            info += convertToInvisibleString(uuidStr + ":" + multiStr + ":");
        }

        return info + formatText(nameFormat).trim();
    }

    /**
     * Formats the specified double into the Economy format specified in the Arconix config.
     *
     * @param amt The double to format.
     * @return The economy formatted double.
     */
    public static String formatEconomy(double amt) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        return formatter.format(amt);
    }

    public static ItemStack getGlass() {
        EpicSpawners instance = EpicSpawners.getInstance();
        return Methods.getGlass(instance.getConfig().getBoolean("Interfaces.Replace Glass Type 1 With Rainbow Glass"), instance.getConfig().getInt("Interfaces.Glass Type 1"));
    }

    public static ItemStack getBackgroundGlass(boolean type) {
        EpicSpawners instance = EpicSpawners.getInstance();
        if (type)
            return getGlass(false, instance.getConfig().getInt("Interfaces.Glass Type 2"));
        else
            return getGlass(false, instance.getConfig().getInt("Interfaces.Glass Type 3"));
    }

    private static ItemStack getGlass(Boolean rainbow, int type) {
        int randomNum = 1 + (int) (Math.random() * 6);
        ItemStack glass;
        if (rainbow) {
            glass = new ItemStack(EpicSpawners.getInstance().isServerVersionAtLeast(ServerVersion.V1_13) ?
                    Material.LEGACY_STAINED_GLASS_PANE : Material.valueOf("STAINED_GLASS_PANE"), 1, (short) randomNum);
        } else {
            glass = new ItemStack(EpicSpawners.getInstance().isServerVersionAtLeast(ServerVersion.V1_13) ?
                    Material.LEGACY_STAINED_GLASS_PANE : Material.valueOf("STAINED_GLASS_PANE"), 1, (short) type);
        }
        ItemMeta glassmeta = glass.getItemMeta();
        glassmeta.setDisplayName("§l");
        glass.setItemMeta(glassmeta);
        return glass;
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

    public static String formatText(String text) {
        if (text == null || text.equals(""))
            return "";
        return formatText(text, false);
    }

    public static String formatText(String text, boolean cap) {
        if (text == null || text.equals(""))
            return "";
        if (cap)
            text = text.substring(0, 1).toUpperCase() + text.substring(1);
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String convertToInvisibleString(String s) {
        if (s == null || s.equals(""))
            return "";
        StringBuilder hidden = new StringBuilder();
        for (char c : s.toCharArray()) hidden.append(ChatColor.COLOR_CHAR + "").append(c);
        return hidden.toString();
    }

    public static ItemStack addTexture(ItemStack item, String headURL) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", new Object[]{headURL}).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));

        Field profileField;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Serializes the location of the block specified.
     *
     * @param b The block whose location is to be saved.
     * @return The serialized data.
     */
    public static String serializeLocation(Block b) {
        if (b == null)
            return "";
        return serializeLocation(b.getLocation());
    }

    /**
     * Serializes the location specified.
     *
     * @param location The location that is to be saved.
     * @return The serialized data.
     */
    public static String serializeLocation(Location location) {
        if (location == null)
            return "";
        String w = location.getWorld().getName();
        double x = location.getBlockX();
        double y = location.getBlockY();
        double z = location.getBlockZ();
        String str = w + ":" + x + ":" + y + ":" + z;
        str = str.replace(".0", "").replace("/", "");
        return str;
    }

    /**
     * Deserializes a location from the string.
     *
     * @param str The string to parse.
     * @return The location that was serialized in the string.
     */
    public static Location unserializeLocation(String str) {
        if (str == null || str.equals(""))
            return null;
        if (serializeCache.containsKey(str)) {
            return serializeCache.get(str).clone();
        }
        String cacheKey = str;
        str = str.replace("y:", ":").replace("z:", ":").replace("w:", "").replace("x:", ":").replace("/", ".");
        List<String> args = Arrays.asList(str.split("\\s*:\\s*"));

        World world = Bukkit.getWorld(args.get(0));
        double x = Double.parseDouble(args.get(1)), y = Double.parseDouble(args.get(2)), z = Double.parseDouble(args.get(3));
        Location location = new Location(world, x, y, z, 0, 0);
        serializeCache.put(cacheKey, location.clone());
        return location;
    }

    public static boolean isInt(String number) {
        if (number == null || number.equals(""))
            return false;
        try {
            Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Determines if the provided string is a valid number (int, double, float, or otherwise).
     *
     * @param s The string to check.
     * @return <code>true</code> if the string is numeric, otherwise <code>false</code>
     */
    public static boolean isNumeric(String s) {
        if (s == null || s.equals(""))
            return false;
        return s.matches("[-+]?\\d*\\.?\\d+");
    }


    public static void removeFromInventory(Inventory inventory, ItemStack item) {
        int amt = item.getAmount();
        ItemStack[] items = inventory.getContents();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].getType() == item.getType() && items[i].getDurability() == item.getDurability()) {
                if (items[i].getAmount() > amt) {
                    items[i].setAmount(items[i].getAmount() - amt);
                    break;
                } else if (items[i].getAmount() == amt) {
                    items[i] = null;
                    break;
                } else {
                    amt -= items[i].getAmount();
                    items[i] = null;
                }
            }
        }
        inventory.setContents(items);
    }

    public static int getAmountInInventory(Inventory inv, ItemStack item) {
        ItemStack[] items = inv.getContents();
        int has = 0;
        for (ItemStack itm : items) {
            if ((itm != null) && (itm.getType() == item.getType()) && (itm.getAmount() > 0) && itm.getDurability() == item.getDurability()) {
                has += itm.getAmount();
            }
        }
        return has;
    }

    public static String[] splitStringEvery(String s, int interval) {
        int arrayLength = (int) Math.ceil(((s.length() / (double) interval)));
        String[] result = new String[arrayLength];

        int j = 0;
        int lastIndex = result.length - 1;
        for (int i = 0; i < lastIndex; i++) {
            result[i] = s.substring(j, j + interval);
            j += interval;
        } //Add the last bit
        result[lastIndex] = s.substring(j);

        return result;
    }

    /**
     * Makes the specified Unix Epoch time human readable as per the format settings in the Arconix config.
     *
     * @param time The time to convert.
     * @return A human readable string representing to specified time.
     */
    public static String makeReadable(Long time) {
        if (time == null)
            return "";
        return String.format("%d hour(s), %d min(s), %d sec(s)", TimeUnit.MILLISECONDS.toHours(time), TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)), TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
    }


    public static class Tuple<key, value> {
        public final key x;
        public final value y;
        public Tuple(key x, value y) {
            this.x = x;
            this.y = y;
        }

        public key getKey() {
            return this.x;
        }

        public value getValue() {
            return this.y;
        }
    }
}