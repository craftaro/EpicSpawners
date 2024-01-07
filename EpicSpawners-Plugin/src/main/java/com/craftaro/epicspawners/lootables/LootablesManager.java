package com.craftaro.epicspawners.lootables;

import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.core.lootables.Lootables;
import com.craftaro.core.lootables.Modify;
import com.craftaro.core.lootables.loot.Drop;
import com.craftaro.core.lootables.loot.Loot;
import com.craftaro.core.lootables.loot.LootManager;
import com.craftaro.core.lootables.loot.Lootable;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LootablesManager {

    private final LootManager lootManager;

    private final String lootablesDir = EpicSpawners.getInstance().getDataFolder() + File.separator + "lootables";

    public LootablesManager() {
        Lootables lootables = new Lootables(this.lootablesDir);
        this.lootManager = new LootManager(lootables);
    }

    public List<Drop> getDrops(LivingEntity entity, SpawnerTier spawnerTier) {
        List<Drop> toDrop = new ArrayList<>();

        if (entity instanceof Ageable && !((Ageable) entity).isAdult()
                || !this.lootManager.getRegisteredLootables().containsKey(spawnerTier.getFullyIdentifyingName())) {
            return toDrop;
        }

        Lootable lootable = this.lootManager.getRegisteredLootables().get(spawnerTier.getFullyIdentifyingName());
        int looting = entity.getKiller() != null
                && entity.getKiller().getItemInHand().containsEnchantment(Enchantment.LOOT_BONUS_MOBS)
                ? entity.getKiller().getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)
                : 0;

        int rerollChance = looting / (looting + 1);

        for (Loot loot : lootable.getRegisteredLoot()) {
            toDrop.addAll(runLoot(entity, loot, rerollChance, looting));
        }

        return toDrop;
    }

    private List<Drop> runLoot(LivingEntity entity, Loot loot, int rerollChance, int looting) {
        Modify modify = null;
        if (entity instanceof Sheep) {
            modify = (Loot loot2) -> {
                XMaterial material = loot2.getMaterial();
                if (material.name().contains("WOOL") && ((Sheep) entity).getColor() != null) {
                    if (((Sheep) entity).isSheared()) {
                        return null;
                    }
                    if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
                        loot2.setMaterial(XMaterial.valueOf(((Sheep) entity).getColor() + "_WOOL"));
                    }

                }
                return loot2;
            };
        }

        EntityType killer = null;
        if (entity.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            Entity killerEntity = ((EntityDamageByEntityEvent) entity.getLastDamageCause()).getDamager();
            killer = killerEntity.getType();
            if (killerEntity instanceof Projectile) {
                Projectile projectile = (Projectile) killerEntity;
                if (projectile.getShooter() instanceof Entity) {
                    killer = ((Entity) projectile.getShooter()).getType();
                }
            }
        }
        return this.lootManager.runLoot(modify,
                entity.getFireTicks() > 0,
                entity instanceof Creeper && ((Creeper) entity).isPowered(),
                entity.getKiller() != null ? entity.getKiller().getItemInHand() : null,
                killer,
                loot,
                rerollChance,
                looting);
    }

    public LootManager getLootManager() {
        return this.lootManager;
    }
}
