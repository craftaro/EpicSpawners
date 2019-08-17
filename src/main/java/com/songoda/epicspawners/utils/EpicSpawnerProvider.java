package com.songoda.epicspawners.utils;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class EpicSpawnerProvider implements ExternalSpawnerProvider {

    private final EpicSpawners instance;

    public EpicSpawnerProvider() {
        this.instance = EpicSpawners.getInstance();
    }

    @Override
    public String getName() {
        return this.instance.getDescription().getName();
    }

    @Override
    public ItemStack getSpawnerItem(EntityType entityType) {
        SpawnerData data = this.instance.getSpawnerManager().getSpawnerData(entityType);
        if (data != null)
            return data.toItemStack();

        return new ItemStack(Material.AIR); // This shouldn't ever happen unless the plugin is out of date
    }

    @Override
    public EntityType getSpawnerEntityType(ItemStack itemStack) {
        SpawnerData data = this.instance.getSpawnerManager().getSpawnerData(itemStack);
        if (data != null && !data.getEntities().isEmpty())
            return data.getEntities().get(0);

        return EntityType.UNKNOWN;
    }

}
