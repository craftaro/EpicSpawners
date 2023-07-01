package com.craftaro.epicspawners.gui;

import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.particles.ParticleDensity;
import com.craftaro.epicspawners.api.particles.ParticleEffect;
import com.craftaro.epicspawners.api.particles.ParticleType;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class EditorParticleGui extends Gui {

    private final EpicSpawners plugin;
    private final Gui back;
    private final SpawnerTier spawnerTier;

    public EditorParticleGui(EpicSpawners plugin, Gui back, SpawnerTier spawnerTier) {
        super(5);
        this.plugin = plugin;
        this.back = back;
        this.spawnerTier = spawnerTier;

        setTitle(spawnerTier.getGuiTitle());
        setOnClose(event -> plugin.getSpawnerManager().saveSpawnerDataToFile());
        setDefaultItem(GuiUtils.getBorderItem(Settings.GLASS_TYPE_1.getMaterial().getItem()));

        paint();
    }

    public void paint() {
        reset();

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(CompatibleMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(CompatibleMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        setItem(1, glass2);
        setItem(2, glass3);
        setItem(9, glass2);

        setItem(6, glass3);
        setItem(7, glass2);
        setItem(8, glass2);
        setItem(17, glass2);

        setItem(27, glass2);

        setItem(35, glass2);
        setItem(36, glass2);
        setItem(37, glass2);
        setItem(38, glass3);

        setItem(42, glass3);
        setItem(43, glass2);
        setItem(44, glass2);

        setButton(0, GuiUtils.createButtonItem(CompatibleMaterial.OAK_DOOR,
                plugin.getLocale().getMessage("general.nametag.back").getMessage()),
                (event) -> guiManager.showGUI(event.player, back));

        setButton(20, GuiUtils.createButtonItem(CompatibleMaterial.ENDER_PEARL, TextUtils.formatText("&5&lParticle Types",
                "&7Entity Spawn Particle: &a" + spawnerTier.getEntitySpawnParticle().name(),
                "&cLeft-Click to change.",
                "&7Spawner Spawn Particle: &a" + spawnerTier.getSpawnerSpawnParticle().name(),
                "&cMiddle-Click to change.",
                "&7Effect Particle: &a" + spawnerTier.getSpawnEffectParticle().name(),
                "&cRight-Click to change.")), event -> {

            ClickType type = event.clickType;
            ParticleType currentParticleType;
            if (type == ClickType.LEFT) {
                currentParticleType = spawnerTier.getEntitySpawnParticle();
            } else if (type == ClickType.RIGHT) {
                currentParticleType = spawnerTier.getSpawnEffectParticle();
            } else {
                currentParticleType = spawnerTier.getSpawnerSpawnParticle();
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
                spawnerTier.setEntitySpawnParticle(currentParticleType);
            } else if (type == ClickType.RIGHT) {
                spawnerTier.setSpawnEffectParticle(currentParticleType);
            } else {
                spawnerTier.setSpawnerSpawnParticle(currentParticleType);
            }
        }).setOnClose(event -> paint());

        setButton(22, GuiUtils.createButtonItem(CompatibleMaterial.FIREWORK_ROCKET, TextUtils.formatText("&6&lSpawner Effect",
                "&7Particle Effect: &a" + spawnerTier.getParticleEffect().name(),
                "&cLeft-Click to change.",
                "&7Particle Effect For BoostedImpl Only: &a" + spawnerTier.isParticleEffectBoostedOnly(),
                "&cRight-Click to change.")), event -> {
            ParticleEffect currentParticleEffect = spawnerTier.getParticleEffect();
            ClickType type = event.clickType;
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
                spawnerTier.setParticleEffect(currentParticleEffect);
            } else if (type == ClickType.RIGHT) {
                spawnerTier.setParticleEffectBoostedOnly(!spawnerTier.isParticleEffectBoostedOnly());
            }
        }).setOnClose(event -> paint());

        setButton(24, GuiUtils.createButtonItem(CompatibleMaterial.COMPARATOR, TextUtils.formatText("&6&lPerformance",
                "&7Currently: &a" + spawnerTier.getParticleDensity().name() + " &cClick to change.")),
                event -> {
                    ParticleDensity currentParticleDensity = spawnerTier.getParticleDensity();

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
                    spawnerTier.setParticleDensity(currentParticleDensity);
                }).setOnClose(event -> paint());

    }
}
