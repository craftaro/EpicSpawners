package com.craftaro.epicspawners.gui;

import com.craftaro.core.database.DataManager;
import com.craftaro.core.gui.CustomizableGui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.third_party.com.cryptomorin.xseries.XSound;
import com.craftaro.third_party.org.apache.commons.lang3.math.NumberUtils;
import com.craftaro.third_party.org.apache.commons.text.WordUtils;
import com.craftaro.third_party.org.jooq.Record;
import com.craftaro.third_party.org.jooq.Result;
import com.craftaro.third_party.org.jooq.impl.DSL;
import com.craftaro.core.utils.ItemUtils;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.boost.types.BoostedSpawnerImpl;
import com.craftaro.epicspawners.settings.Settings;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Calendar;
import java.util.Date;

public class SpawnerBoostGui extends CustomizableGui {
    private final EpicSpawners plugin;
    private final PlacedSpawner spawner;
    private final Player player;
    private int amount = 1;

    public SpawnerBoostGui(EpicSpawners plugin, PlacedSpawner spawner, Player player) {
        super(plugin, "boost");
        setRows(3);
        this.plugin = plugin;
        this.spawner = spawner;
        this.player = player;
        setUp();
        paint();
    }

    private void setUp() {
        if (this.amount > Settings.MAX_PLAYER_BOOST.getInt()) {
            this.amount = Settings.MAX_PLAYER_BOOST.getInt();
            return;
        } else if (this.amount < 1) {
            this.amount = 1;
        }
        setTitle(this.plugin.getLocale().getMessage("interface.boost.title")
                .processPlaceholder("spawner", this.spawner.getFirstTier().getCompiledDisplayName())
                .processPlaceholder("amount", this.amount).getMessage());
    }

    public void paint() {
        reset();

        ItemStack glass1 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_1.getMaterial());
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial());
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial());

        setDefaultItem(glass1);

        mirrorFill("mirrorfill_1", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_2", 0, 1, true, true, glass2);
        mirrorFill("mirrorfill_3", 0, 2, true, true, glass3);
        mirrorFill("mirrorfill_4", 1, 0, false, true, glass2);
        mirrorFill("mirrorfill_5", 1, 1, false, true, glass3);

        setButton("boost5", 10, GuiUtils.createButtonItem(XMaterial.COAL, this.plugin.getLocale().getMessage("interface.boost.boostfor")
                                .processPlaceholder("amount", "5").getMessage(),
                        this.plugin.getLocale().getMessage("interface.boost.cost")
                                .processPlaceholder("cost", getBoostCost(5, this.amount)).getMessage()),
                event -> purchaseBoost(this.player, 5, this.amount));

        setButton("boost15", 12, GuiUtils.createButtonItem(XMaterial.IRON_INGOT, this.plugin.getLocale().getMessage("interface.boost.boostfor")
                                .processPlaceholder("amount", "15").getMessage(),
                        this.plugin.getLocale().getMessage("interface.boost.cost")
                                .processPlaceholder("cost", getBoostCost(15, this.amount)).getMessage()),
                event -> purchaseBoost(this.player, 15, this.amount));

        setButton("boost30", 14, GuiUtils.createButtonItem(XMaterial.DIAMOND, this.plugin.getLocale().getMessage("interface.boost.boostfor")
                                .processPlaceholder("amount", "30").getMessage(),
                        this.plugin.getLocale().getMessage("interface.boost.cost")
                                .processPlaceholder("cost", getBoostCost(30, this.amount)).getMessage()),
                event -> purchaseBoost(this.player, 30, this.amount));

        setButton("boost60", 16, GuiUtils.createButtonItem(XMaterial.EMERALD, this.plugin.getLocale().getMessage("interface.boost.boostfor")
                                .processPlaceholder("amount", "60").getMessage(),
                        this.plugin.getLocale().getMessage("interface.boost.cost")
                                .processPlaceholder("cost", getBoostCost(60, this.amount)).getMessage()),
                event -> purchaseBoost(this.player, 60, this.amount));

        setButton("back", 4, GuiUtils.createButtonItem(Settings.EXIT_ICON.getMaterial(),
                        this.plugin.getLocale().getMessage("general.nametag.back").getMessage()),
                event -> this.spawner.overview(this.player));

        if (this.amount != 1)
            setButton("minus1", 0, GuiUtils.createButtonItem(XMaterial.ARROW,
                    TextUtils.formatText("&6&l-1")), event -> {
                this.amount--;
                setUp();
                paint();
            });

        if (this.amount < Settings.MAX_PLAYER_BOOST.getInt())
            setButton("plus1", 8, GuiUtils.createButtonItem(ItemUtils.getCustomHead("1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b"),
                    TextUtils.formatText("&6&l+1")), event -> {
                this.amount++;
                setUp();
                paint();
            });
    }

    private void purchaseBoost(Player player, int time, int amt) {
        Location location = this.spawner.getLocation();
        player.closeInventory();
        EpicSpawners instance = this.plugin;

        String un = Settings.BOOST_COST.getString();

        String[] parts = un.split(":");

        String type = parts[0];
        String multi = parts[1];
        int cost = boostCost(multi, time, amt);
        if (type.equals("ECO")) {
            if (EconomyManager.isEnabled()) {
                if (EconomyManager.hasBalance(player, cost)) {
                    EconomyManager.withdrawBalance(player, cost);
                } else {
                    this.plugin.getLocale().getMessage("event.upgrade.cannotafford").sendPrefixedMessage(player);
                    return;
                }
            } else {
                player.sendMessage("Economy not enabled.");
                return;
            }
        } else if (type.equals("XP")) {
            if (player.getLevel() >= cost || player.getGameMode() == GameMode.CREATIVE) {
                if (player.getGameMode() != GameMode.CREATIVE || Settings.CHARGE_FOR_CREATIVE.getBoolean())
                    player.setLevel(player.getLevel() - cost);
            } else {
                this.plugin.getLocale().getMessage("event.upgrade.cannotafford").sendPrefixedMessage(player);
                return;
            }
        } else {
            ItemStack stack = XMaterial.valueOf(type).parseItem();
            if (player.getInventory().containsAtLeast(stack, cost)) {
                stack.setAmount(cost);
                player.getInventory().removeItem(stack);
            } else {
                this.plugin.getLocale().getMessage("event.upgrade.cannotafford").sendPrefixedMessage(player);
                return;
            }
        }
        Calendar c = Calendar.getInstance();
        Date currentDate = new Date();
        c.setTime(currentDate);
        c.add(Calendar.MINUTE, time);


        BoostedSpawnerImpl boost = new BoostedSpawnerImpl(location, amt, c.getTimeInMillis());
        instance.getBoostManager().addBoost(boost);
        DataManager dataManager = instance.getDataManager();
        dataManager.getAsyncPool().execute(() -> {
            dataManager.getDatabaseConnector().connectDSL(context -> {

                Result<Record> result = context.select().from(DSL.table(dataManager.getTablePrefix() + "boosted_spawners"))
                        .where(DSL.field("world").eq(boost.getLocation().getWorld().getName()))
                        .and(DSL.field("x").eq(boost.getLocation().getX()))
                        .and(DSL.field("y").eq(boost.getLocation().getY()))
                        .and(DSL.field("z").eq(boost.getLocation().getZ()))
                        .fetch();

                if (result.isEmpty()) {
                    context.insertInto(DSL.table(dataManager.getTablePrefix() + "boosted_spawners"))
                            .set(boost.serialize())
                            .execute();
                } else {
                    context.update(DSL.table(dataManager.getTablePrefix() + "boosted_spawners"))
                            .set(boost.serialize())
                            .where(DSL.field("world").eq(boost.getLocation().getWorld().getName()))
                            .and(DSL.field("x").eq(boost.getLocation().getX()))
                            .and(DSL.field("y").eq(boost.getLocation().getY()))
                            .and(DSL.field("z").eq(boost.getLocation().getZ()))
                            .execute();
                }
            });
        });
        this.plugin.getLocale().getMessage("event.boost.applied").sendPrefixedMessage(player);
        XSound.ENTITY_VILLAGER_YES.play(player, 1.0f, 1.0f);
    }

    public String getBoostCost(int time, int amount) {
        StringBuilder cost = new StringBuilder("&6&l");
        String[] parts = Settings.BOOST_COST.getString().split(":");

        String type = parts[0];
        String multi = parts[1];

        int co = boostCost(multi, time, amount);

        if (type.equals("ECO")) {
            cost.append('$').append(EconomyManager.formatEconomy(co));
        } else if (type.equals("XP")) {
            cost.append(co).append(" &7Levels");
        } else {
            cost.append(co).append(" &7").append(WordUtils.capitalizeFully(type));
            if (co != 1) {
                cost.append('s');
            }
        }

        return cost.toString();
    }

    public int boostCost(String multi, int time, int amt) {
        return (int) Math.ceil(NumberUtils.toDouble(multi, 1) * time * amt);
    }
}
