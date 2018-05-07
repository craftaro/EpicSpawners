package com.songoda.epicspawners.listeners;

import com.songoda.arconix.api.utils.Serializer;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.entity.EPlayer;
import com.songoda.epicspawners.spawners.Spawner;
import com.songoda.epicspawners.spawners.SpawnerBreakEvent;
import com.songoda.epicspawners.spawners.SpawnerPlaceEvent;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Created by songoda on 2/25/2017.
 */
public class BlockListeners implements Listener {

    private final EpicSpawners instance;

    public BlockListeners(EpicSpawners instance) {
        this.instance = instance;

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
        try {
            if (doLiquidRepel(e.getBlock(), false)) e.setCancelled(true);
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    private boolean doLiquidRepel(Block block, boolean from) {
        int radius = instance.getConfig().getInt("Main.Spawner Repel Liquid Radius");
        if (radius == 0) return false;
        if (!from) radius++;
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();
        for (int fx = -radius; fx <= radius; fx++) {
            for (int fy = -radius; fy <= radius; fy++) {
                for (int fz = -radius; fz <= radius; fz++) {
                    Block b2 = block.getWorld().getBlockAt(bx + fx, by + fy, bz + fz);

                    if (from) {
                        if ((b2.getType().equals(Material.STATIONARY_LAVA) || b2.getType().equals(Material.LAVA))
                                || (b2.getType().equals(Material.STATIONARY_WATER) || b2.getType().equals(Material.WATER))) {
                            b2.setType(Material.AIR);
                        }
                    } else {
                        if (b2.getType().equals(Material.MOB_SPAWNER)) {
                            return true;
                        }
                    }

                }
            }
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent e) {
        try {
            Player p = e.getPlayer();
            Block b = e.getBlockPlaced();

            if (b.getType().equals(Material.MOB_SPAWNER)) doLiquidRepel(b, true);

            if (Methods.isOffhand(e)) {
                if (p.getInventory().getItemInOffHand().getType() == Material.MOB_SPAWNER)
                    e.setCancelled(true);
                return;
            }

            ItemStack inh = p.getItemInHand();
            ItemMeta im = inh.getItemMeta();

            if (im == null || b.getType() != Material.MOB_SPAWNER || im.getDisplayName() == null)
                return;

            String type = instance.getApi().getIType(inh);
            FileConfiguration dataFile = instance.dataFile.getConfig();

            if (!p.hasPermission("epicspawners.place." + type) && !p.hasPermission("epicspawners.place." + Methods.getTypeFromString(type)) && !p.hasPermission("epicspawners.place.*")) {
                e.setCancelled(true);
                return;
            }

            SpawnerPlaceEvent event = new SpawnerPlaceEvent(b.getLocation(), p, type);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            if (doForceCombine(p, b, dataFile, type)) {
                e.setCancelled(true);
                return;
            }

            Spawner eSpawner = new Spawner(b);
            boolean isCustom = false;
            if (!type.equals("OMNI")) {
                try {
                    eSpawner.setSpawner(EntityType.valueOf(type));
                } catch (Exception ex) {
                    isCustom = true;
                    eSpawner.setSpawner(EntityType.valueOf("DROPPED_ITEM"));
                }
            }
            int multi = 0;
            if (im.hasDisplayName()) {
                if (instance.blacklist.isBlacklisted(p, true)) {
                    e.setCancelled(true);
                } else {
                    multi = instance.getApi().getIMulti(inh);
                    eSpawner.updateDelay();
                    dataFile.set("data.spawner." + Arconix.pl().getApi().serialize().serializeLocation(b), multi);
                    if (!type.equals("OMNI") && isCustom) {
                        dataFile.set("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(b) + ".type", type);
                    }
                    dataFile.set("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(b) + ".player", p.getUniqueId().toString());
                    instance.getApi().saveCustomSpawner(inh, b);
                }
            }
            if (instance.getConfig().getBoolean("Main.Alerts On Place And Break") &&
                    (!instance.blacklist.isBlacklisted(p, false) || !im.hasDisplayName())) {
                p.sendMessage(instance.getLocale().getMessage("event.block.place", Methods.compileName(type, multi, true)));
            }
            instance.getApi().updateDisplayItem(type, b.getLocation());
            if (multi <= 1) {
                dataFile.set("data.spawner." + Arconix.pl().getApi().serialize().serializeLocation(b), 1);
                if (!type.equals("OMNI") && isCustom) {
                    dataFile.set("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(b) + ".type", type);
                }
                dataFile.set("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(b) + ".player", p.getUniqueId().toString());

                if (!p.isOp() && instance.spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(type) + ".Pickup-cost") != 0 && eSpawner.canCharge()) {
                    int cost = instance.spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(type) + ".Pickup-cost");
                    p.sendMessage(instance.getLocale().getMessage("event.block.chargeplace", Arconix.pl().getApi().format().formatEconomy(cost)));
                    instance.freePickup.put(p, b.getLocation());
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> instance.freePickup.remove(p), 1200L);
                }

            }
            instance.holo.processChange(e.getBlock());
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    public boolean doForceCombine(Player p, Block b, FileConfiguration dataFile, String type) {
        if (instance.getConfig().getInt("Main.Force Combine Radius") == 0 || !dataFile.contains("data.spawner")) {
            return false;
        }
        ConfigurationSection cs = dataFile.getConfigurationSection("data.spawner");
        for (String key : cs.getKeys(false)) {
            if (Arconix.pl().getApi().serialize().unserializeLocation(key).getWorld() == null
                    || !Arconix.pl().getApi().serialize().unserializeLocation(key).getWorld().equals(b.getLocation().getWorld())
                    || Arconix.pl().getApi().serialize().unserializeLocation(key).distance(b.getLocation()) >= instance.getConfig().getInt("Main.Force Combine Radius")
                    || p.getItemInHand().getItemMeta().getDisplayName() == null
                    || instance.getApi().isOmniBlock(Arconix.pl().getApi().serialize().unserializeLocation(key))) {
                continue;
            }
            Spawner eSpawner = new Spawner(Arconix.pl().getApi().serialize().unserializeLocation(key).getBlock());

            String name = eSpawner.getSpawner() == null || eSpawner.getSpawner().getSpawnedType().name().equals("DROPPED_ITEM")
                    ? EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + key + ".type")
                    : eSpawner.getSpawner().getSpawnedType().name();

            if (name == null || !name.equals(type))
                continue;

            if (instance.getConfig().getBoolean("settings.serializer"))
                p.sendMessage(instance.getLocale().getMessage("event.block.forcedeny"));
            else if (eSpawner.processCombine(p, p.getItemInHand(), null))
                p.sendMessage(instance.getLocale().getMessage("event.block.mergedistance"));
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    //Leave this on high or WorldGuard will not work...
    public void onBlockBreak(BlockBreakEvent e) {
        try {
            Block b = e.getBlock();
            if (b.getType() != Material.MOB_SPAWNER) {
                return;
            }
            Spawner eSpawner = new Spawner(b);
            Player p = e.getPlayer();
            e.setExpToDrop(0);

            if (!eSpawner.canBreak()) {
                return;
            }

            FileConfiguration dataFile = instance.dataFile.getConfig();
            FileConfiguration config = instance.getConfig();

            Serializer serializer = Arconix.pl().getApi().serialize();

            String type = Methods.getType(eSpawner.getSpawner().getSpawnedType());

            if (dataFile.contains("data.spawnerstats." + serializer.serializeLocation(eSpawner.getSpawner().getBlock()) + ".type")
                    && !dataFile.getString("data.spawnerstats." + serializer.serializeLocation(eSpawner.getSpawner().getBlock()) + ".type").equals("OMNI"))
                type = dataFile.getString("data.spawnerstats." + serializer.serializeLocation(eSpawner.getSpawner().getBlock()) + ".type");

            int multi = eSpawner.getMulti();
            int omulti = multi;

            int newMulti = multi - 1;
            if (instance.getApi().isOmniBlock(b.getLocation())) {
                type = "Omni";
                if (omulti > 2) {
                    multi = instance.getApi().convertFromList(dataFile.getStringList("data.spawnerstats." + serializer.serializeLocation(b) + ".entities")).size();
                } else {
                    String old = dataFile.getStringList("data.spawnerstats." + serializer.serializeLocation(b) + ".entities").get(0);
                    newMulti = Integer.parseInt(old.split("-")[1]);
                }
            }

            if (config.getBoolean("Spawner Drops.Only Drop Stacked Spawners") ||
                    p.isSneaking() && config.getBoolean("Main.Sneak To Receive A Stacked Spawner")) {
                if (!instance.getApi().isOmniBlock(b.getLocation()))
                    newMulti = 0;
            }
            if (newMulti > 0) {
                e.setCancelled(true);
            }

            SpawnerBreakEvent event = new SpawnerBreakEvent(b.getLocation(), p);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            if ((p.getItemInHand().getItemMeta() == null || !p.getItemInHand().getItemMeta().hasEnchant(Enchantment.SILK_TOUCH))
                    && !p.hasPermission("epicspawners.no-silk-drop")) {
                eSpawner.downgradeFinal(p, newMulti, omulti, type);
                return;
            }
            if (!config.getBoolean("Spawner Drops.Drop On SilkTouch")) {
                return;
            }
            if (!p.isOp()
                    && !instance.freePickup.containsValue(b.getLocation())
                    && instance.spawnerFile.getConfig().getInt("Entities." + type + ".Pickup-cost") != 0) {
                int cost = instance.spawnerFile.getConfig().getInt("Entities." + type + ".Pickup-cost");
                if (newMulti == 0)
                    cost = cost * multi;
                if (!instance.pickup.containsKey(p)) {
                    p.sendMessage(instance.getLocale().getMessage("event.block.chargebreak", Arconix.pl().getApi().format().formatEconomy(cost)));
                    instance.pickup.put(p, true);
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> instance.pickup.remove(p), 300L);
                    e.setCancelled(true);
                    return;
                } else if (eSpawner.canCharge()
                        && instance.spawnerFile.getConfig().getInt("Entities." + type + ".Pickup-cost") != 0
                        && instance.pickup.containsKey(p)
                        && instance.getServer().getPluginManager().getPlugin("Vault") != null) {
                    RegisteredServiceProvider<Economy> rsp = instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                    net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                    if (econ.has(p, cost)) {
                        econ.withdrawPlayer(p, cost);
                    } else {
                        p.sendMessage(instance.getLocale().getMessage("event.block.cannotbreak"));
                        e.setCancelled(true);
                        return;
                    }
                } else {
                    e.setCancelled(true);
                    return;
                }
            }

            instance.pickup.remove(p);

            int ch = Integer.parseInt(config.getString(dataFile.contains("data.spawner." + serializer.serializeLocation(b))
                    ? "Spawner Drops.Chance On Placed Silktouch" : "Spawner Drops.Chance On Natural Silktouch").replace("%", ""));

            double rand = Math.random() * 100;

            if (!p.isSneaking() && !config.getBoolean("Spawner Drops.Only Drop Stacked Spawners") || p.isSneaking() && !config.getBoolean("Main.Sneak To Receive A Stacked Spawner")) {
                multi = 1;
            }
            if (rand - ch < 0 || ch == 100) {
                if (p.hasPermission("epicspawners.silkdrop." + type) || p.hasPermission("epicspawners.silkdrop.*")) {
                    new EPlayer(p).dropSpawner(b.getLocation(), multi, type);
                }
            }
            eSpawner.downgradeFinal(p, newMulti, omulti, type);
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> instance.holo.processChange(e.getBlock()), 10L);
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}
