package com.songoda.epicspawners.gui;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.particles.ParticleDensity;
import com.songoda.epicspawners.api.particles.ParticleEffect;
import com.songoda.epicspawners.api.particles.ParticleType;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class GUIEditorParticle extends AbstractGUI {

    private final EpicSpawnersPlugin plugin;
    private final AbstractGUI back;
    private SpawnerData spawnerData;

    public GUIEditorParticle(EpicSpawnersPlugin plugin, AbstractGUI abstractGUI, SpawnerData spawnerData, Player player) {
        super(player);
        this.plugin = plugin;
        this.back = abstractGUI;
        this.spawnerData = spawnerData;

        init(Methods.compileName(spawnerData, 1, false) + " &8Particle &8Settings.", 45);
    }

    @Override
    public void constructGUI() {
        inventory.clear();
        resetClickables();
        registerClickables();

        int num = 0;
        while (num != 45) {
            inventory.setItem(num, Methods.getGlass());
            num++;
        }

        createButton(0, Methods.addTexture(new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3),
                "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"),
                plugin.getLocale().getMessage("general.nametag.back"));

        inventory.setItem(1, Methods.getBackgroundGlass(true));
        inventory.setItem(2, Methods.getBackgroundGlass(false));
        inventory.setItem(9, Methods.getBackgroundGlass(true));

        inventory.setItem(6, Methods.getBackgroundGlass(false));
        inventory.setItem(7, Methods.getBackgroundGlass(true));
        inventory.setItem(8, Methods.getBackgroundGlass(true));
        inventory.setItem(17, Methods.getBackgroundGlass(true));

        inventory.setItem(27, Methods.getBackgroundGlass(true));

        inventory.setItem(35, Methods.getBackgroundGlass(true));
        inventory.setItem(36, Methods.getBackgroundGlass(true));
        inventory.setItem(37, Methods.getBackgroundGlass(true));
        inventory.setItem(38, Methods.getBackgroundGlass(false));

        inventory.setItem(42, Methods.getBackgroundGlass(false));
        inventory.setItem(43, Methods.getBackgroundGlass(true));
        inventory.setItem(44, Methods.getBackgroundGlass(true));

        createButton(20, Material.ENDER_PEARL, "&5&lParticle Types",
                "&7Entity Spawn Particle: &a" + spawnerData.getEntitySpawnParticle().name(),
                "&cLeft-Click to change.",
                "&7Spawner Spawn Particle: &a" + spawnerData.getSpawnerSpawnParticle().name(),
                "&cMiddle-Click to change.",
                "&7Effect Particle: &a" + spawnerData.getSpawnEffectParticle().name(),
                "&cRight-Click to change.");

        createButton(22, Material.FIREWORK_ROCKET, "&6&lSpawner Effect",
                "&7Particle Effect: &a" + spawnerData.getParticleEffect().name(),
                "&cLeft-Click to change.",
                "&7Particle Effect For Boosted Only: &a" + spawnerData.isParticleEffectBoostedOnly(),
                "&cRight-Click to change.");

        createButton(24, Material.COMPARATOR, "&6&lPerformance",
                "&7Currently: &a" + spawnerData.getParticleDensity().name() + " &cClick to change.");

    }

    @Override
    protected void registerClickables() {

        registerClickable(20, (player, inventory, cursor, slot, type) -> {
            ParticleType currentParticleType;
            if (type == ClickType.LEFT) {
                currentParticleType = spawnerData.getEntitySpawnParticle();
            } else if (type == ClickType.RIGHT) {
                currentParticleType = spawnerData.getSpawnEffectParticle();
            } else {
                currentParticleType = spawnerData.getSpawnerSpawnParticle();
            }

            boolean next = false;
            for (ParticleType particleType : ParticleType.values()) {
                if (currentParticleType == particleType) {
                    next = true;
                } else if (next) {
                    currentParticleType = particleType;
                    next = false;
                }
            }
            if (next) {
                currentParticleType = ParticleType.values()[0];
            }


            if (type == ClickType.LEFT) {
                spawnerData.setEntitySpawnParticle(currentParticleType);
            } else if (type == ClickType.RIGHT) {
                spawnerData.setSpawnEffectParticle(currentParticleType);
            } else {
                spawnerData.setSpawnerSpawnParticle(currentParticleType);
            }

            constructGUI();
        });

        registerClickable(22, (player, inventory, cursor, slot, type) -> {
            ParticleEffect currentParticleEffect = spawnerData.getParticleEffect();

            if (type == ClickType.LEFT) {
                boolean next = false;
                for (ParticleEffect particleEffect : ParticleEffect.values()) {
                    if (currentParticleEffect == particleEffect) {
                        next = true;
                    } else if (next) {
                        currentParticleEffect = particleEffect;
                        next = false;
                    }
                }
                if (next) {
                    currentParticleEffect = ParticleEffect.values()[0];
                }
                spawnerData.setParticleEffect(currentParticleEffect);
            } else if (type == ClickType.RIGHT) {
                if (!spawnerData.isParticleEffectBoostedOnly())
                    spawnerData.setParticleEffectBoostedOnly(true);
                else
                    spawnerData.setParticleEffectBoostedOnly(false);
            }

            constructGUI();
        });


        registerClickable(24, (player, inventory, cursor, slot, type) -> {
            ParticleDensity currentParticleDensity = spawnerData.getParticleDensity();

            boolean next = false;
            for (ParticleDensity particleDensity : ParticleDensity.values()) {
                if (currentParticleDensity == particleDensity) {
                    next = true;
                } else if (next) {
                    currentParticleDensity = particleDensity;
                    next = false;
                }
            }
            if (next) {
                currentParticleDensity = ParticleDensity.values()[0];
            }
            spawnerData.setParticleDensity(currentParticleDensity);

            constructGUI();
        });

        registerClickable(0, (player, inventory, cursor, slot, type) -> {
            back.init(back.getSetTitle(), back.getInventory().getSize());
            back.constructGUI();
        });
    }

    @Override
    protected void registerOnCloses() {

    }
}
