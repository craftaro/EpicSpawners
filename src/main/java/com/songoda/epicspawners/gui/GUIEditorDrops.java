package com.songoda.epicspawners.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import com.songoda.lootables.gui.GuiLootableEditor;
import com.songoda.lootables.loot.LootManager;
import com.songoda.lootables.loot.Lootable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GUIEditorDrops extends Gui {

    private final EpicSpawners plugin;
    private final SpawnerData spawnerData;


    public GUIEditorDrops(EpicSpawners plugin, SpawnerData spawnerData, AbstractGUI back) {
        super(3);
        this.plugin = plugin;
        this.spawnerData = spawnerData;

        ItemStack glass1 = GuiUtils.getBorderItem(Methods.getGlass());
        ItemStack glass2 = GuiUtils.getBorderItem(Methods.getBackgroundGlass(true));
        ItemStack glass3 = GuiUtils.getBorderItem(Methods.getBackgroundGlass(false));

        setDefaultItem(glass1);

        GuiUtils.mirrorFill(this, 0, 0, true, true, glass2);
        GuiUtils.mirrorFill(this, 0, 1, true, true, glass2);
        GuiUtils.mirrorFill(this, 0, 2, true, true, glass3);
        GuiUtils.mirrorFill(this, 1, 0, false, true, glass2);
        GuiUtils.mirrorFill(this, 1, 1, false, true, glass3);

        setTitle(TextUtils.formatText(spawnerData.getCompiledDisplayName() + " &8Drops."));

        setButton(8, GuiUtils.createButtonItem(Methods.addTexture(new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3),
                "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"),
                plugin.getLocale().getMessage("general.nametag.back").getMessage()),
                (event) -> {
                    back.init(back.getSetTitle(), back.getInventory().getSize());
                    back.constructGUI();
                });

        paint();
    }

    public void paint() {
        LootManager lootManager = plugin.getLootablesManager().getLootManager();

        setButton(1, 3, GuiUtils.createButtonItem(CompatibleMaterial.BARRIER, "Remove & Reset Custom Drops"),
                (event) -> {
                    lootManager.removeLootable(spawnerData.getIdentifyingName());
                    paint();
                });

        boolean dropExists = lootManager.getRegisteredLootables().containsKey(spawnerData.getIdentifyingName());

        setButton(1, 5, GuiUtils.createButtonItem(CompatibleMaterial.LIME_DYE, dropExists ? "Edit Drops" : "Create & Enable Custom Drops"),
                (event) -> {
                    if (!dropExists)
                        lootManager.addLootable(new Lootable(spawnerData.getIdentifyingName()));
                    Lootable lootable = lootManager.getRegisteredLootables().get(spawnerData.getIdentifyingName());
                    plugin.getGuiManager().showGUI(event.player,
                            new GuiLootableEditor(plugin.getLootablesManager().getLootManager(), lootable, this));
                });
    }
}
