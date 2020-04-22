package com.songoda.epicspawners.listeners;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTCompound;
import com.songoda.core.nms.nbt.NBTCore;
import com.songoda.core.nms.nbt.NBTItem;
import com.songoda.core.nms.nbt.NBTObject;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.player.PlayerData;
import com.songoda.epicspawners.player.PlayerDataManager;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class EntityListeners implements Listener {

    private final EpicSpawners plugin;
    private com.songoda.ultimatestacker.UltimateStacker ultimateStacker = null;

    public EntityListeners(EpicSpawners plugin) {
        this.plugin = plugin;
        if (Bukkit.getPluginManager().isPluginEnabled("UltimateStacker")) {
            ultimateStacker = com.songoda.ultimatestacker.UltimateStacker.getInstance();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlow(EntityExplodeEvent event) {
        List<Block> destroyed = event.blockList();
        Iterator<Block> it = destroyed.iterator();
        List<Block> toCancel = new ArrayList<>();
        while (it.hasNext()) {
            Block block = it.next();
            if (block.getType() != (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER")))
                continue;

            Location spawnLocation = block.getLocation();

            Spawner spawner = plugin.getSpawnerManager().getSpawnerFromWorld(block.getLocation());

            if (Settings.SPAWNERS_DONT_EXPLODE.getBoolean())
                toCancel.add(block);
            else {

                String chance = "";
                if (event.getEntity() instanceof Creeper)
                    chance = Settings.EXPLOSION_DROP_CHANCE_TNT.getString();
                else if (event.getEntity() instanceof TNTPrimed)
                    chance = Settings.EXPLOSION_DROP_CHANCE_TNT.getString();
                int ch = Integer.parseInt(chance.replace("%", ""));
                double rand = Math.random() * 100;
                if (rand - ch < 0 || ch == 100) {
                    for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                        ItemStack item = stack.getSpawnerData().toItemStack(1, stack.getStackSize());
                        spawnLocation.getWorld().dropItemNaturally(spawnLocation.clone().add(.5, 0, .5), item);
                    }

                    spawner.destroy(plugin);
                }
            }

            Location nloc = spawnLocation.clone();
            nloc.add(.5, -.4, .5);
            List<Entity> near = (List<Entity>) nloc.getWorld().getNearbyEntities(nloc, 8, 8, 8);
            for (Entity ee : near) {
                if (ee.getLocation().getX() == nloc.getX() && ee.getLocation().getY() == nloc.getY() && ee.getLocation().getZ() == nloc.getZ()) {
                    ee.remove();
                }
            }

        }

        for (Block block : toCancel) {
            event.blockList().remove(block);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER) return;
        if (event.getEntity().hasMetadata("ES")) {
            SpawnerData spawnerData = plugin.getSpawnerManager().getSpawnerData(event.getEntity().getMetadata("ES").get(0).asString());
            if (!spawnerData.getEntityDroppedItems().isEmpty()) {
                event.getDrops().clear();
            }
            for (ItemStack itemStack : spawnerData.getEntityDroppedItems()) {
                event.getDrops().add(itemStack);
            }
        }
        if (event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        if (!player.hasPermission("epicspawners.Killcounter") || !Settings.MOB_KILLING_COUNT.getBoolean())
            return;

        if (!plugin.getSpawnManager().isNaturalSpawn(event.getEntity().getUniqueId()) && !Settings.COUNT_UNNATURAL_KILLS.getBoolean())
            return;


        if (!plugin.getSpawnerManager().getSpawnerData(event.getEntityType()).isActive()) return;

        SpawnerData spawnerData = plugin.getSpawnerManager().getSpawnerData(event.getEntityType());

        if (!spawnerData.isActive()) return;

        int amount = 1;

        if (ultimateStacker != null) {
            boolean killAll = ultimateStacker.getConfig().getBoolean("Entities.Kill Whole Stack On Death");
            if (ultimateStacker.getEntityStackManager().isStacked(event.getEntity().getUniqueId()) && killAll) {
                amount = ultimateStacker.getEntityStackManager().getStack(event.getEntity().getUniqueId()).getAmount();
            }
        }
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();

        boolean isPlayerData = playerDataManager.isPlayerData(player);
        PlayerData playerData = playerDataManager.getPlayerData(player);
        int amt = playerData.addKilledEntity(event.getEntityType(), amount);
        if (isPlayerData)
            plugin.getDataManager().updateEntityKill(player, event.getEntity().getType(), amt);
        else
            plugin.getDataManager().createEntityKill(player, event.getEntity().getType(), amt);
        int goal = Settings.KILL_GOAL.getInt();

        int customGoal = spawnerData.getKillGoal();
        if (customGoal != 0) goal = customGoal;

        if (Settings.ALERT_INTERVAL.getInt() != 0
                && amt % Settings.ALERT_INTERVAL.getInt() == 0
                && amt != goal) {
            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9))
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(plugin.getLocale().getMessage("event.goal.alert")
                        .processPlaceholder("goal", goal - amt)
                        .processPlaceholder("type", spawnerData.getIdentifyingName()).getMessage()));
            else
                player.sendTitle("", plugin.getLocale().getMessage("event.goal.alert")
                        .processPlaceholder("goal", goal - amt)
                        .processPlaceholder("type", spawnerData.getIdentifyingName()).getMessage());
        }

        if (amt >= goal) {
            ItemStack item = spawnerData.toItemStack();

            if (Settings.SPAWNERS_TO_INVENTORY.getBoolean() && player.getInventory().firstEmpty() != -1)
                player.getInventory().addItem(item);
            else
                event.getEntity().getLocation().getWorld().dropItemNaturally(event.getEntity().getLocation(), item);

            plugin.getPlayerDataManager().getPlayerData(player).removeEntity(event.getEntityType());
            plugin.getDataManager().deleteEntityKills(player, event.getEntityType());
            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9))
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(plugin.getLocale().getMessage("event.goal.reached")
                        .processPlaceholder("type", spawnerData.getIdentifyingName()).getMessage()));
            else
                player.sendTitle("", plugin.getLocale().getMessage("event.goal.alert")
                        .processPlaceholder("goal", goal - amt)
                        .processPlaceholder("type", spawnerData.getIdentifyingName()).getMessage());
        }
    }
}

