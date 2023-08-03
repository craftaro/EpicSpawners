package com.craftaro.epicspawners.listeners;

import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.core.lootables.loot.Drop;
import com.craftaro.core.lootables.loot.DropUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.player.PlayerData;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.player.PlayerDataManagerImpl;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.ultimatestacker.api.UltimateStackerApi;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EntityListeners implements Listener {
    private final EpicSpawners plugin;

    public EntityListeners(EpicSpawners plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlow(EntityExplodeEvent event) {
        List<Block> destroyed = event.blockList();
        Iterator<Block> it = destroyed.iterator();
        List<Block> toCancel = new ArrayList<>();
        while (it.hasNext()) {
            Block block = it.next();
            if (block.getType() != (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))) {
                continue;
            }

            Location spawnLocation = block.getLocation();

            PlacedSpawner spawner = this.plugin.getSpawnerManager().getSpawnerFromWorld(block.getLocation());

            if (Settings.SPAWNERS_DONT_EXPLODE.getBoolean()) {
                toCancel.add(block);
            } else {
                String chance = "";
                if (event.getEntity() instanceof Creeper) {
                    chance = Settings.EXPLOSION_DROP_CHANCE_CREEPER.getString();
                } else if (event.getEntity() instanceof TNTPrimed) {
                    chance = Settings.EXPLOSION_DROP_CHANCE_TNT.getString();
                }
                int ch = Integer.parseInt(chance.replace("%", ""));
                double rand = Math.random() * 100;
                if (rand - ch < 0 || ch == 100) {
                    for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                        ItemStack item = stack.getSpawnerData().getFirstTier().toItemStack(1, stack.getStackSize());
                        spawnLocation.getWorld().dropItemNaturally(spawnLocation.clone().add(.5, 0, .5), item);
                    }

                    spawner.destroy();
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER) {
            return;
        }

        if (event.getEntity().hasMetadata("ESData")) {
            List<MetadataValue> values = event.getEntity().getMetadata("ESData");
            List<MetadataValue> values2 = event.getEntity().getMetadata("ESTier");
            if (!values.isEmpty()) {
                SpawnerData spawnerData = this.plugin.getSpawnerManager().getSpawnerData(values.get(0).asString());
                SpawnerTier spawnerTier = spawnerData.getTier(values2.get(0).asString());
                if (this.plugin.getLootablesManager().getLootManager().getRegisteredLootables().containsKey(spawnerTier.getFullyIdentifyingName())) {
                    List<Drop> drops = this.plugin.getLootablesManager().getDrops(event.getEntity(), spawnerTier);

                    if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            && !event.getEntity().getWorld().getGameRuleValue(GameRule.DO_MOB_LOOT)) {
                        drops.clear();
                    }

                    DropUtils.processStackedDrop(event.getEntity(), drops, event);
                }
            }
        }
        if (event.getEntity().getKiller() == null) {
            return;
        }
        Player player = event.getEntity().getKiller();
        if (!player.hasPermission("epicspawners.Killcounter") || !Settings.MOB_KILLING_COUNT.getBoolean()) {
            return;
        }

        if (!this.plugin.getSpawnManager().isNaturalSpawn(event.getEntity().getUniqueId()) && !Settings.COUNT_UNNATURAL_KILLS.getBoolean()) {
            return;
        }

        if (!this.plugin.getSpawnerManager().getSpawnerData(event.getEntityType()).isActive()) {
            return;
        }

        SpawnerTier spawnerTier = this.plugin.getSpawnerManager().getSpawnerData(event.getEntityType()).getFirstTier();

        if (!spawnerTier.getSpawnerData().isActive()) {
            return;
        }

        int amount = 1;

        if (Bukkit.getPluginManager().isPluginEnabled("UltimateStacker")) {
            boolean killAll = UltimateStackerApi.getSettings().killWholeStackOnDeath();
            if (UltimateStackerApi.getEntityStackManager().isStackedEntity(event.getEntity()) && killAll) {
                amount = UltimateStackerApi.getEntityStackManager().getStackedEntity(event.getEntity().getUniqueId()).getAmount();
            }
        }
        PlayerDataManagerImpl playerDataManager = this.plugin.getPlayerDataManager();

        PlayerData playerData = playerDataManager.getPlayerData(player);
        int amt = playerData.addKilledEntity(event.getEntityType(), amount);
        playerData.save();
        int goal = Settings.KILL_DROP_GOAL.getInt();
        double chance = Settings.KILL_DROP_CHANCE.getDouble();

        int customGoal = spawnerTier.getSpawnerData().getKillDropGoal();
        if (customGoal != 0) {
            goal = customGoal;
        }

        double customChance = spawnerTier.getSpawnerData().getKillDropChance();
        if (customChance != 0) {
            chance = customChance;
        }

        if (Settings.ALERT_INTERVAL.getInt() != 0
                && amt % Settings.ALERT_INTERVAL.getInt() == 0
                && amt != goal) {
            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(this.plugin.getLocale().getMessage("event.goal.alert")
                        .processPlaceholder("goal", goal - amt)
                        .processPlaceholder("type", spawnerTier.getDisplayName()).getMessage()));
            } else {
                player.sendTitle("", this.plugin.getLocale().getMessage("event.goal.alert")
                        .processPlaceholder("goal", goal - amt)
                        .processPlaceholder("type", spawnerTier.getDisplayName()).getMessage());
            }
        }

        double rand = Math.random() * 100;
        boolean goalReached = amt >= goal;
        if (goalReached || rand - chance < 0 || chance == 100) {
            ItemStack item = spawnerTier.toItemStack();

            if (Settings.SPAWNERS_TO_INVENTORY.getBoolean() && player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(item);
            } else {
                event.getEntity().getLocation().getWorld().dropItemNaturally(event.getEntity().getLocation(), item);
            }

            if (goalReached) {
                this.plugin.getPlayerDataManager().getPlayerData(player).removeEntity(event.getEntityType());
                playerData.deleteEntityKills(event.getEntityType());
                playerData.save();
            }

            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(this.plugin.getLocale().getMessage("event.goal.reached")
                        .processPlaceholder("type", spawnerTier.getIdentifyingName()).getMessage()));
            }
        }
    }
}
