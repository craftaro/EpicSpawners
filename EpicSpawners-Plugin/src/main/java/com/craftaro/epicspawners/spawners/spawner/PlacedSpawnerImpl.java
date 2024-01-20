package com.craftaro.epicspawners.spawners.spawner;

import com.craftaro.core.compatibility.CompatibleHand;
import com.craftaro.core.compatibility.CompatibleParticleHandler;
import com.craftaro.core.database.Data;
import com.craftaro.core.database.DataManager;
import com.craftaro.core.database.SerializedLocation;
import com.craftaro.core.nms.world.SpawnedEntity;
import com.craftaro.epicspawners.utils.CoreProtectLogger;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.third_party.com.cryptomorin.xseries.XSound;
import com.craftaro.third_party.org.jooq.impl.DSL;
import com.craftaro.core.utils.PlayerUtils;
import com.craftaro.core.world.SSpawner;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.boosts.types.Boosted;
import com.craftaro.epicspawners.api.events.SpawnerChangeEvent;
import com.craftaro.epicspawners.api.events.SpawnerDropEvent;
import com.craftaro.epicspawners.api.particles.ParticleType;
import com.craftaro.epicspawners.api.spawners.condition.SpawnCondition;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.boost.types.BoostedPlayerImpl;
import com.craftaro.epicspawners.boost.types.BoostedSpawnerImpl;
import com.craftaro.epicspawners.gui.SpawnerTiersGui;
import com.craftaro.epicspawners.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PlacedSpawnerImpl implements PlacedSpawner {
    // This is the unique identifier for this hologram.
    // It is reset on every plugin load.
    // Used for holograms.
    private final UUID uniqueHologramId = UUID.randomUUID();

    // Id for database use.
    private int id = -1;

    //Holds the different types of spawners contained by this creatureSpawner.
    private final Deque<SpawnerStack> spawnerStacks = new ArrayDeque<>();
    private String omniState = null;
    private int spawnCount;
    private UUID placedBy = null;
    private CreatureSpawner creatureSpawner = null;
    private Location location;
    private SSpawner sSpawner;

    /**
     * Default constructor used for database loading.
     */
    public PlacedSpawnerImpl() {
    }

    /**
     * Constructor used for database loading.
     *
     * @param id         The id of the spawner
     * @param spawnCount The spawn count of the spawner
     * @param placedBy   The player who placed the spawner
     * @param location   The location of the spawner
     */
    public PlacedSpawnerImpl(int id, int spawnCount, UUID placedBy, Location location) {
        this.id = id;
        this.spawnCount = spawnCount;
        this.placedBy = placedBy;
        this.location = location;
        this.sSpawner = new SSpawner(this.location);
        DataManager dataManager = EpicSpawners.getInstance().getDataManager();
        dataManager.getDatabaseConnector().connectDSL(dslContext -> {
            List<SpawnerStack> spawnerStacks = dataManager.loadBatch(SpawnerStackImpl.class, "spawner_stacks", DSL.field("spawner_id").eq(this.id));
            for (SpawnerStack stack : spawnerStacks) {
                SpawnerStackImpl spawnerStackImpl = (SpawnerStackImpl) stack;
                spawnerStackImpl.setSpawner(this);
                this.spawnerStacks.push(spawnerStackImpl);
            }
        });
    }

    /**
     * Constructor used for creating new spawners.
     *
     * @param location The location of the spawner
     */
    public PlacedSpawnerImpl(Location location) {
        this.location = location;
        this.sSpawner = new SSpawner(this.location);
    }

    @Override
    public boolean spawn() {
        if (getFirstStack().getCurrentTier() == null) {
            return false;
        }

        EpicSpawners instance = EpicSpawners.getInstance();

        displaySpawnParticles();

        if (!isRedstonePowered()) {
            return false;
        }

        for (SpawnerStack stack : getSpawnerStacks()) {
            stack.getCurrentTier().spawn(this, stack);
        }

        //ToDo: This is bad.
        if (getFirstTier().getSpawnLimit() != -1 && this.spawnCount * this.spawnerStacks.size() > getFirstTier().getSpawnLimit()) {
            this.location.getBlock().setType(Material.AIR);

            CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.LAVA,
                    this.location.clone().add(.5, .5, .5), 5, 0, 0, 0, 5);
            XSound.ENTITY_GENERIC_EXPLODE.play(this.location, 10, 10);

            instance.getSpawnerManager().removeSpawnerFromWorld(this);
            EpicSpawners.getInstance().getDataManager().delete(this);
            instance.clearHologram(this);
            return true;
        }

        updateDelay();

        return true;
    }

    @Override
    public void displaySpawnParticles() {
        Location particleLocation = this.location.clone();
        particleLocation.add(.5, .5, .5);
        for (SpawnerStack spawnerStack : this.spawnerStacks) {
            SpawnerTier spawnerTier = spawnerStack.getCurrentTier();
            ParticleType particleType = spawnerTier.getSpawnerSpawnParticle();
            if (particleType != ParticleType.NONE) {
                float x = (float) (0 + (Math.random() * .8));
                float y = (float) (0 + (Math.random() * .8));
                float z = (float) (0 + (Math.random() * .8));
                CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.getParticle(particleType.getEffect()),
                        particleLocation, 0, x, y, z, 0);
            }
        }
    }

    @Override
    public int spawn(int amountToSpawn, String particle, Set<XMaterial> canSpawnOn, SpawnedEntity spawned, EntityType... types) {
        return this.sSpawner.spawn(amountToSpawn, particle, canSpawnOn, spawned, types);
    }

    @Override
    public SpawnerStack addSpawnerStack(SpawnerStack spawnerStack) {
        this.spawnerStacks.push(spawnerStack);
        return spawnerStack;
    }


    @Override
    public Location getLocation() {
        return this.location.clone();
    }

    @Override
    public int getX() {
        return this.location.getBlockX();
    }

    @Override
    public int getY() {
        return this.location.getBlockY();
    }

    @Override
    public int getZ() {
        return this.location.getBlockZ();
    }

    @Override
    public World getWorld() {
        return this.location.getWorld();
    }

    @Override
    public CreatureSpawner getCreatureSpawner() {
        if (!getWorld().isChunkLoaded(getX() >> 4, getZ() >> 4)) {
            return null;
        }
        if (this.creatureSpawner == null) {
            if (this.location.getBlock().getType() != XMaterial.SPAWNER.parseMaterial()) {
                EpicSpawners.getInstance().getSpawnerManager().removeSpawnerFromWorld(this);
                EpicSpawners.getInstance().getDataManager().delete(this);
                return null;
            }
            this.creatureSpawner = (CreatureSpawner) this.location.getBlock().getState();
        }
        return this.creatureSpawner;
    }

    @Override
    public SpawnerStack getFirstStack() {
        if (this.spawnerStacks.isEmpty()) {
            CreatureSpawner creatureSpawner = getCreatureSpawner();
            return new SpawnerStackImpl(this, EpicSpawners.getInstance().getSpawnerManager().getSpawnerData(creatureSpawner.getSpawnedType().name()).getFirstTier(), 1);
        }
        return this.spawnerStacks.getFirst();
    }

    @Override
    public int getStackSize() {
        int multi = 0;
        for (SpawnerStack stack : this.spawnerStacks) {
            multi += stack.getStackSize();
        }
        return multi;
    }

    @Override
    public boolean checkConditions() {
        for (SpawnerStack stack : this.spawnerStacks) {
            SpawnerTier tier = stack.getCurrentTier();
            if (tier == null) {
                continue;
            }
            for (SpawnCondition spawnCondition : tier.getConditions()) {
                if (!spawnCondition.isMet(this)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isRedstonePowered() {
        return (!this.location.getBlock().isBlockPowered()
                && !this.location.getBlock().isBlockIndirectlyPowered())
                || !Settings.REDSTONE_ACTIVATE.getBoolean();
    }

    @Override
    public void overview(Player player) {
        EpicSpawners plugin = EpicSpawners.getInstance();
        if (!player.hasPermission("epicspawners.overview")
                || (getPlacedBy() == null && Settings.DISABLE_NATURAL_SPAWNERS.getBoolean())) {
            return;
        }
        SpawnerTiersGui.openTiers(plugin, player, this);
    }

    @Override
    public boolean unstack(Player player, CompatibleHand hand) {
        EpicSpawners instance = EpicSpawners.getInstance();
        SpawnerStack stack = getFirstStack();
        if (stack == null || stack.getSpawner().getId() == -1) {
            return false; //Not a stack
        }

        int stackSize = 1;

        if (player.isSneaking() && Settings.SNEAK_FOR_STACK.getBoolean()
                || Settings.ONLY_DROP_STACKED.getBoolean()) {
            stackSize = stack.getStackSize();
        }

        if (Settings.SOUNDS_ENABLED.getBoolean()) {
            XSound.ENTITY_ARROW_HIT_PLAYER.play(player, 0.6F, 15.0F);
        }
        ItemStack item = stack.getCurrentTier().toItemStack(1, stackSize);


        ItemStack inHand = hand.getItem(player);
        if (Settings.SILKTOUCH_SPAWNERS.getBoolean()
                && inHand.hasItemMeta()
                && inHand.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)
                && inHand.getEnchantmentLevel(Enchantment.SILK_TOUCH) >= Settings.SILKTOUCH_MIN_LEVEL.getInt()
                && player.hasPermission("epicspawners.silkdrop." + stack.getSpawnerData().getIdentifyingName().replace(' ', '_'))
                || player.hasPermission("epicspawners.no-silk-drop")) {
            if (Settings.SPAWNERS_TO_INVENTORY.getBoolean()) {
                SpawnerDropEvent placeEvent = new SpawnerDropEvent(player, this);
                Bukkit.getPluginManager().callEvent(placeEvent);
                if (placeEvent.isCancelled()) {
                    return false;
                }

                Collection<ItemStack> leftOver = player.getInventory().addItem(item).values();
                for (ItemStack itemStack : leftOver) {
                    player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                }
            } else if (!Settings.ONLY_DROP_PLACED.getBoolean() || this.placedBy != null) {
                int ch = Integer.parseInt((this.placedBy != null
                        ? Settings.SILKTOUCH_PLACED_SPAWNER_DROP_CHANCE.getString() : Settings.SILKTOUCH_NATURAL_SPAWNER_DROP_CHANCE.getString()).replace("%", ""));

                double rand = Math.random() * 100;

                if ((rand - ch < 0 || ch == 100) && ch != 0) {
                    SpawnerDropEvent placeEvent = new SpawnerDropEvent(player, this);
                    Bukkit.getPluginManager().callEvent(placeEvent);
                    if (placeEvent.isCancelled()) {
                        return false;
                    }

                    if (Settings.SPAWNERS_TO_INVENTORY.getBoolean() && player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(item);
                    } else {
                        this.location.getWorld().dropItemNaturally(this.location.clone().add(.5, 0, .5), item);
                    }
                }
            }
        }

        DataManager dataManager = EpicSpawners.getInstance().getDataManager();
        if (stack.getStackSize() > 1 && stackSize == 1) {
            stack.setStackSize(stack.getStackSize() - 1);
            dataManager.getAsyncPool().execute(() -> {
                dataManager.getDatabaseConnector().connectDSL(dslContext -> {
                    int deleted = dslContext.deleteFrom(DSL.table(dataManager.getTablePrefix()+"spawner_stacks"))
                            .where(DSL.field("spawner_id").eq(this.getId()))
                            .and(DSL.field("data_type").eq(stack.getSpawnerData().getIdentifyingName()))
                            .and(DSL.field("amount").eq(stack.getStackSize()+1))
                            .and(DSL.field("tier").eq(stack.getCurrentTier().getIdentifyingName()))
                            .execute();
                });
            });
            EpicSpawners.getInstance().getDataManager().save(stack, "spawner_id", this.id);
            return true;
        }

        this.spawnerStacks.remove(stack);

        dataManager.getAsyncPool().execute(() -> {
            dataManager.getDatabaseConnector().connectDSL(dslContext -> {
                dslContext.deleteFrom(DSL.table(dataManager.getTablePrefix()+"spawner_stacks"))
                        .where(DSL.field("spawner_id").eq(this.getId()))
                        .and(DSL.field("data_type").eq(stack.getSpawnerData().getIdentifyingName()))
                        .and(DSL.field("amount").eq(stack.getStackSize()))
                        .and(DSL.field("tier").eq(stack.getCurrentTier().getIdentifyingName()))
                        .execute();
            });
        });

        if (this.spawnerStacks.size() != 0) {
            return true;
        }

        this.location.getBlock().setType(Material.AIR);
        EpicSpawners.getInstance().getSpawnerManager().removeSpawnerFromWorld(this);
        EpicSpawners.getInstance().getDataManager().delete(this);
        instance.clearHologram(this);
        return true;
    }

    @Override
    public boolean stack(Player player, SpawnerTier tier, int amount, CompatibleHand hand) {
        EpicSpawners plugin = EpicSpawners.getInstance();

        int max = Settings.SPAWNERS_MAX.getInt();
        int currentStackSize = getStackSize();

        if (max <= 0 || currentStackSize == max) {
            plugin.getLocale().getMessage("event.upgrade.maxed").sendPrefixedMessage(player);
            return false;
        }

        if (tier != getFirstTier()
                && (!Settings.OMNI_SPAWNERS.getBoolean() || !player.hasPermission("epicspawners.omni"))) {
            return false;
        }

        SpawnerChangeEvent event = new SpawnerChangeEvent(player, this, currentStackSize + amount, currentStackSize);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }


        if ((getStackSize() + amount) > max) {
            PlayerUtils.giveItem(player, tier.toItemStack(1, (getStackSize() + amount) - max));
            amount = max - currentStackSize;
        }

        for (SpawnerStack spawnerStack : this.spawnerStacks) {
            if (!spawnerStack.getCurrentTier().equals(tier)) {
                continue;
            }
            spawnerStack.setStackSize(spawnerStack.getStackSize() + amount);
            //plugin.getDataManager().saveSync(spawnerStack, "spawner_id", spawnerStack.getSpawner().getId());
            // Not sure why this is not working. Maybe no key defined?
            DataManager dataManager = EpicSpawners.getInstance().getDataManager();
            dataManager.getDatabaseConnector().connectDSL(context -> {

                int modified = context.update(DSL.table(dataManager.getTablePrefix() + "spawner_stacks"))
                        .set(DSL.field("amount"), spawnerStack.getStackSize())
                        .where(DSL.field("spawner_id").eq(spawnerStack.getSpawner().getId()))
                        .and(DSL.field("data_type").eq(spawnerStack.getSpawnerData().getIdentifyingName()))
                        .and(DSL.field("tier").eq(spawnerStack.getCurrentTier().getIdentifyingName()))
                        .execute();
                if (modified == 0) {
                    context.insertInto(DSL.table(dataManager.getTablePrefix() + "spawner_stacks"))
                            .set(spawnerStack.serialize())
                            .execute();
                }
            });


            upgradeEffects(player, tier, true);

            if (player.getGameMode() != GameMode.CREATIVE || Settings.CHARGE_FOR_CREATIVE.getBoolean()) {
                hand.takeItem(player);
            }
            return true;
        }

        SpawnerStack stack = new SpawnerStackImpl(this, tier, amount);
        addSpawnerStack(stack);
        plugin.getDataManager().save(stack, "spawner_id", stack.getSpawner().getId());

        if (player.getGameMode() != GameMode.CREATIVE || Settings.CHARGE_FOR_CREATIVE.getBoolean()) {
            hand.takeItem(player);
        }

        return true;
    }

    @Override
    public SpawnerTier getFirstTier() {
        return this.spawnerStacks.getFirst().getCurrentTier();
    }

    @Override
    public void upgradeEffects(Player player, SpawnerTier tier, boolean stacked) {
        EpicSpawners plugin = EpicSpawners.getInstance();
        int currentStackSize = getStackSize();

        if (stacked) {
            if (getStackSize() != Settings.SPAWNERS_MAX.getInt()) {
                plugin.getLocale().getMessage("event.upgrade.success")
                        .processPlaceholder("size", currentStackSize).sendPrefixedMessage(player);
            } else {
                plugin.getLocale().getMessage("event.upgrade.successmaxed")
                        .processPlaceholder("size", currentStackSize).sendPrefixedMessage(player);
            }
        } else {
            plugin.getLocale().getMessage("event.tierup.success").processPlaceholder("tier", tier.getCompiledDisplayName());
        }

        Location loc = this.location.clone();
        loc.setX(loc.getX() + .5);
        loc.setY(loc.getY() + .5);
        loc.setZ(loc.getZ() + .5);

        CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.getParticle(Settings.UPGRADE_PARTICLE_TYPE.getString()),
                loc, 100, .5, .5, .5);

        plugin.updateHologram(this);
        plugin.getAppearanceTask().updateDisplayItem(this, getFirstTier());


        if (!Settings.SOUNDS_ENABLED.getBoolean()) {
            return;
        }
        if (currentStackSize != Settings.SPAWNERS_MAX.getInt()) {
            XSound.ENTITY_PLAYER_LEVELUP.play(player, 0.6F, 15.0F);
        } else {
            XSound.ENTITY_PLAYER_LEVELUP.play(player, 2.0F, 25.0F);
            XSound.BLOCK_NOTE_BLOCK_CHIME.play(player, 2.0F, 25.0F);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> XSound.BLOCK_NOTE_BLOCK_CHIME.play(player, 1.2F, 35.0F), 5L);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> XSound.BLOCK_NOTE_BLOCK_CHIME.play(player, 1.8F, 35.0F), 10L);
        }
    }

    @Override
    public List<Boosted> getBoosts() {
        EpicSpawners instance = EpicSpawners.getInstance();
        Set<Boosted> boosts = instance.getBoostManager().getBoosts();

        List<Boosted> found = new ArrayList<>();
        for (Boosted boost : new ArrayList<>(boosts)) {
            if (boost instanceof BoostedPlayerImpl && this.placedBy == null) {
                continue;
            }
            BoostedSpawnerImpl boostedSpawner = (BoostedSpawnerImpl) boost;
            if (System.currentTimeMillis() >= boost.getEndTime()) {
                instance.getBoostManager().removeBoost(boost);
                instance.getDataManager().getAsyncPool().execute(() -> {
                    instance.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
                        dslContext.deleteFrom(DSL.table(instance.getDataManager().getTablePrefix() + "boosted_spawners"))
                                .where(DSL.field("world").eq(boostedSpawner.getLocation().getWorld().getName()))
                                .and(DSL.field("x").eq(boostedSpawner.getLocation().getBlockX()))
                                .and(DSL.field("y").eq(boostedSpawner.getLocation().getBlockY()))
                                .and(DSL.field("z").eq(boostedSpawner.getLocation().getBlockZ()))
                                .execute();
                    });
                });
                continue;
            }

            if (boost instanceof BoostedSpawnerImpl) {
                if (!this.location.equals(((BoostedSpawnerImpl) boost).getLocation())) {
                    continue;
                }
            } else if (boost instanceof BoostedPlayerImpl) {
                if (!this.placedBy.equals(((BoostedPlayerImpl) boost).getPlayer().getUniqueId())) {
                    continue;
                }
            }
            found.add(boost);
        }
        return found;
    }

    @Override
    public int updateDelay() {
        int max = 0;
        int min = 0;
        for (SpawnerStack stack : this.spawnerStacks) {
            String tickRate = stack.getCurrentTier().getTickRate();

            String[] tick = tickRate.contains(":") ? tickRate.split(":") : new String[]{tickRate, tickRate};

            int tickMin = Integer.parseInt(tick[1]);
            int tickMax = Integer.parseInt(tick[0]);
            if (max == 0 && min == 0) {
                max = tickMax;
                min = tickMin;
                continue;
            }
            if ((max + min) < (tickMax + tickMin)) {
                max = tickMax;
                min = tickMin;
            }
        }
        int extraTicks = Settings.EXTRA_SPAWN_TICKS.getInt();

        if (getStackSize() == 0) {
            return 0;
        }

        int delay = (int) (Math.random() * (max - min)) + min + extraTicks;

        getCreatureSpawner().setDelay(delay);
        getCreatureSpawner().update();

        return delay;
    }

    @Override
    public String getIdentifyingName() {
        String name = this.spawnerStacks.getFirst().getSpawnerData().getIdentifyingName();

        if (this.spawnerStacks.size() > 1) {
            name = EpicSpawners.getInstance().getSpawnerManager().getSpawnerData("omni").getIdentifyingName();
        }

        return name;
    }

    @Override
    public List<SpawnerStack> getSpawnerStacks() {
        return new LinkedList<>(this.spawnerStacks);
    }

    @Override
    public void replaceStacks(List<SpawnerStack> stacks) {
        this.spawnerStacks.clear();
        this.spawnerStacks.addAll(stacks);
    }

    @Override
    public OfflinePlayer getPlacedBy() {
        if (this.placedBy == null) {
            return null;
        }
        return Bukkit.getOfflinePlayer(this.placedBy);
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.id);
        map.put("spawn_count", this.spawnCount);
        map.put("placed_by", this.placedBy.toString());
        map.putAll(SerializedLocation.of(this.location));
        return map;
    }

    @Override
    public Data deserialize(Map<String, Object> map) {
        int id = (int) map.get("id");
        int spawnCount = (int) map.get("spawn_count");
        String placedByString = (String) map.get("placed_by");
        UUID placedBy = placedByString != null ? UUID.fromString(placedByString) : null;
        Location location = SerializedLocation.of(map);
        return new PlacedSpawnerImpl(id, spawnCount, placedBy, location);
    }

    @Override
    public String getTableName() {
        return "placed_spawners";
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void setPlacedBy(Player placedBy) {
        this.placedBy = placedBy.getUniqueId();
    }

    @Override
    public void setPlacedBy(UUID placedBy) {
        this.placedBy = placedBy;
    }

    @Override
    public int getSpawnCount() {
        return this.spawnCount;
    }

    @Override
    public void setSpawnCount(int spawnCount) {
        this.spawnCount = spawnCount;
    }

    @Override
    public String getOmniState() {
        return this.omniState;
    }

    @Override
    public void setOmniState(String omniState) {
        this.omniState = omniState;
    }

    @Override
    public void destroy() {
        EpicSpawners plugin = EpicSpawners.getInstance();
        plugin.getAppearanceTask().removeDisplayItem(this);
        plugin.getSpawnerManager().removeSpawnerFromWorld(this);
        plugin.getDataManager().delete(this);
        plugin.clearHologram(this);
    }

    @Override
    public boolean isValid() {
        return !this.spawnerStacks.isEmpty()
                && getFirstStack() != null
                && getFirstStack().getCurrentTier() != null
                && getFirstStack().getSpawnerData() != null;
    }

    @Override
    public boolean merge(SpawnerStack toMerge, SpawnerTier oldTier) {
        EpicSpawners plugin = EpicSpawners.getInstance();
        boolean modified = false;
        String tablePrefix = plugin.getDataManager().getTablePrefix();

        for (SpawnerStack stack : getSpawnerStacks()) {
            if (stack == toMerge
                    || !stack.getCurrentTier().equals(toMerge.getCurrentTier())) {
                continue;
            }
            stack.setStackSize(toMerge.getStackSize() + stack.getStackSize());
            this.spawnerStacks.remove(toMerge);
            plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
                //Delete the old stack
                dslContext.update(DSL.table(tablePrefix + "spawner_stacks"))
                        .set(toMerge.serialize())
                        .where(DSL.field("spawner_id").eq(getId()))
                        .and(DSL.field("data_type").eq(oldTier.getSpawnerData().getIdentifyingName()))
                        .and(DSL.field("tier").eq(oldTier.getIdentifyingName()))
                        .and(DSL.field("amount").eq(toMerge.getStackSize()))
                        .execute();
            });
            plugin.getDataManager().save(stack, "spawner_id", getId());
            modified = true;
            break;
        }

        plugin.updateHologram(this);
        return modified;
    }

    @Override
    public String getHologramId() {
        return "EpicSpawners-" + this.uniqueHologramId;
    }

    @Override
    public int hashCode() {
        int result = 31 * (this.location == null ? 0 : this.location.hashCode());
        result = 31 * result + (this.placedBy == null ? 0 : this.placedBy.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PlacedSpawnerImpl)) {
            return false;
        }

        PlacedSpawnerImpl other = (PlacedSpawnerImpl) obj;
        return Objects.equals(this.location, other.location) && Objects.equals(this.placedBy, other.placedBy);
    }

    @Override
    public String toString() {
        return "Spawner:{"
                + "Owner:\"" + this.placedBy + "\","
                + "Location:{"
                + "World:\"" + this.location.getWorld().getName() + "\","
                + "X:" + this.location.getBlockX() + ","
                + "Y:" + this.location.getBlockY() + ","
                + "Z:" + this.location.getBlockZ()
                + "},"
                + "StackCount:" + this.spawnerStacks.size()
                + "}";
    }
}
