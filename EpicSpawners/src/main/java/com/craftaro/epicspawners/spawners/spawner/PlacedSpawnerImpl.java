package com.craftaro.epicspawners.spawners.spawner;

import com.craftaro.core.compatibility.CompatibleHand;
import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.compatibility.CompatibleParticleHandler;
import com.craftaro.core.compatibility.CompatibleSound;
import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.core.database.Data;
import com.craftaro.core.database.SerializedLocation;
import com.craftaro.core.nms.world.SpawnedEntity;
import com.craftaro.core.utils.PlayerUtils;
import com.craftaro.core.world.SSpawner;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.boosts.types.Boosted;
import com.craftaro.epicspawners.api.events.SpawnerChangeEvent;
import com.craftaro.epicspawners.api.events.SpawnerDropEvent;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.boost.types.BoostedPlayerImpl;
import com.craftaro.epicspawners.boost.types.BoostedSpawnerImpl;
import com.craftaro.epicspawners.gui.SpawnerTiersGui;
import com.craftaro.epicspawners.api.particles.ParticleType;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.api.spawners.condition.SpawnCondition;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
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
    private int id;

    //Holds the different types of spawners contained by this creatureSpawner.
    private final Deque<SpawnerStack> spawnerStacks = new ArrayDeque<>();
    private String omniState = null;
    private int spawnCount;
    private UUID placedBy = null;
    private CreatureSpawner creatureSpawner = null;
    private Location location;
    private SSpawner sSpawner;

    public PlacedSpawnerImpl(Location location) {
        this.location = location;
        this.sSpawner = new SSpawner(this.location);
    }

    @Override
    public boolean spawn() {
        if (getFirstStack().getCurrentTier() == null) return false;

        EpicSpawners instance = EpicSpawners.getInstance();

        displaySpawnParticles();

        if (!isRedstonePowered()) return false;

        for (SpawnerStack stack : getSpawnerStacks())
            stack.getCurrentTier().spawn(this, stack);

        //ToDo: This is bad.
        if (getFirstTier().getSpawnLimit() != -1 && spawnCount * spawnerStacks.size() > getFirstTier().getSpawnLimit()) {
            location.getBlock().setType(Material.AIR);

            CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.LAVA,
                    location.clone().add(.5, .5, .5), 5, 0, 0, 0, 5);
            location.getWorld().playSound(location, ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                    ? Sound.ENTITY_GENERIC_EXPLODE : Sound.valueOf("EXPLODE"), 10, 10);

            instance.getSpawnerManager().removeSpawnerFromWorld(this);
            EpicSpawners.getInstance().getDataManager().deleteSpawner(this);
            instance.clearHologram(this);
            return true;
        }

        updateDelay();

        return true;
    }

    @Override
    public void displaySpawnParticles() {
        Location particleLocation = location.clone();
        particleLocation.add(.5, .5, .5);
        for (SpawnerStack spawnerStack : spawnerStacks) {
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
    public int spawn(int amountToSpawn, String particle, Set<CompatibleMaterial> canSpawnOn, SpawnedEntity spawned, EntityType... types) {
        return sSpawner.spawn(amountToSpawn, particle, canSpawnOn, spawned, types);
    }

    @Override
    public SpawnerStack addSpawnerStack(SpawnerStack spawnerStack) {
        this.spawnerStacks.push(spawnerStack);
        return spawnerStack;
    }


    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public int getX() {
        return location.getBlockX();
    }

    @Override
    public int getY() {
        return location.getBlockY();
    }

    @Override
    public int getZ() {
        return location.getBlockZ();
    }

    @Override
    public World getWorld() {
        return location.getWorld();
    }

    @Override
    public CreatureSpawner getCreatureSpawner() {
        if (!getWorld().isChunkLoaded(getX() >> 4, getZ() >> 4))
            return null;
        if (creatureSpawner == null) {
            if (location.getBlock().getType() != CompatibleMaterial.SPAWNER.getMaterial()) {
                EpicSpawners.getInstance().getSpawnerManager().removeSpawnerFromWorld(this);
                EpicSpawners.getInstance().getDataManager().deleteSpawner(this);
                return null;
            }
            this.creatureSpawner = (CreatureSpawner) location.getBlock().getState();
        }
        return creatureSpawner;
    }

    @Override
    public SpawnerStack getFirstStack() {
        if (spawnerStacks.size() == 0) return null;
        return spawnerStacks.getFirst();
    }

    @Override
    public int getStackSize() {
        int multi = 0;
        for (SpawnerStack stack : spawnerStacks) {
            multi += stack.getStackSize();
        }
        return multi;
    }

    @Override
    public boolean checkConditions() {
        for (SpawnerStack stack : spawnerStacks) {
            SpawnerTier tier = stack.getCurrentTier();
            if (tier == null) continue;
            for (SpawnCondition spawnCondition : tier.getConditions())
                if (!spawnCondition.isMet(this)) return false;
        }
        return true;
    }

    @Override
    public boolean isRedstonePowered() {
        return (!location.getBlock().isBlockPowered()
                && !location.getBlock().isBlockIndirectlyPowered())
                || !Settings.REDSTONE_ACTIVATE.getBoolean();
    }

    @Override
    public void overview(Player player) {
        EpicSpawners plugin = EpicSpawners.getInstance();
        if (!player.hasPermission("epicspawners.overview")
                || (getPlacedBy() == null && Settings.DISABLE_NATURAL_SPAWNERS.getBoolean())) return;
        SpawnerTiersGui.openTiers(plugin, player, this);
    }

    @Override
    public boolean unstack(Player player, CompatibleHand hand) {
        EpicSpawners instance = EpicSpawners.getInstance();
        SpawnerStack stack = getFirstStack();

        int stackSize = 1;

        if (player.isSneaking() && Settings.SNEAK_FOR_STACK.getBoolean()
                || Settings.ONLY_DROP_STACKED.getBoolean()) {
            stackSize = stack.getStackSize();
        }

        if (Settings.SOUNDS_ENABLED.getBoolean()) {
            player.playSound(player.getLocation(), CompatibleSound.ENTITY_ARROW_HIT_PLAYER.getSound(), 0.6F, 15.0F);
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
            } else if (!Settings.ONLY_DROP_PLACED.getBoolean() || placedBy != null) {
                int ch = Integer.parseInt((placedBy != null
                        ? Settings.SILKTOUCH_PLACED_SPAWNER_DROP_CHANCE.getString() : Settings.SILKTOUCH_NATURAL_SPAWNER_DROP_CHANCE.getString()).replace("%", ""));

                double rand = Math.random() * 100;

                if ((rand - ch < 0 || ch == 100) && ch != 0) {
                    SpawnerDropEvent placeEvent = new SpawnerDropEvent(player, this);
                    Bukkit.getPluginManager().callEvent(placeEvent);
                    if (placeEvent.isCancelled()) {
                        return false;
                    }

                    if (Settings.SPAWNERS_TO_INVENTORY.getBoolean() && player.getInventory().firstEmpty() != -1)
                        player.getInventory().addItem(item);
                    else
                        location.getWorld().dropItemNaturally(location.clone().add(.5, 0, .5), item);
                }
            }
        }

        if (stack.getStackSize() > 1 && stackSize == 1) {
            stack.setStackSize(stack.getStackSize() - 1);
            EpicSpawners.getInstance().getDataManager().updateSpawnerStack(stack);
            return true;
        }

        spawnerStacks.remove(stack);
        EpicSpawners.getInstance().getDataManager().deleteSpawnerStack(stack);

        if (spawnerStacks.size() != 0) return true;

        location.getBlock().setType(Material.AIR);
        EpicSpawners.getInstance().getSpawnerManager().removeSpawnerFromWorld(this);
        EpicSpawners.getInstance().getDataManager().deleteSpawner(this);
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
                && (!Settings.OMNI_SPAWNERS.getBoolean() || !player.hasPermission("epicspawners.omni")))
            return false;

        SpawnerChangeEvent event = new SpawnerChangeEvent(player, this, currentStackSize + amount, currentStackSize);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        if ((getStackSize() + amount) > max) {
            PlayerUtils.giveItem(player, tier.toItemStack(1, (getStackSize() + amount) - max));
            amount = max - currentStackSize;
        }

        for (SpawnerStack stack : spawnerStacks) {
            if (!stack.getCurrentTier().equals(tier)) continue;
            stack.setStackSize(stack.getStackSize() + amount);
            plugin.getDataManager().updateSpawnerStack(stack);
            upgradeEffects(player, tier, true);

            if (player.getGameMode() != GameMode.CREATIVE || Settings.CHARGE_FOR_CREATIVE.getBoolean())
                hand.takeItem(player);

            return true;
        }

        SpawnerStack stack = new SpawnerStackImpl(this, tier, amount);
        addSpawnerStack(stack);
        plugin.getDataManager().createSpawnerStack(stack);

        if (player.getGameMode() != GameMode.CREATIVE || Settings.CHARGE_FOR_CREATIVE.getBoolean())
            hand.takeItem(player);

        return true;
    }

    @Override
    public SpawnerTier getFirstTier() {
        return spawnerStacks.getFirst().getCurrentTier();
    }

    @Override
    public void upgradeEffects(Player player, SpawnerTier tier, boolean stacked) {
        EpicSpawners plugin = EpicSpawners.getInstance();
        int currentStackSize = getStackSize();

        if (stacked)
            if (getStackSize() != Settings.SPAWNERS_MAX.getInt())
                plugin.getLocale().getMessage("event.upgrade.success")
                        .processPlaceholder("size", currentStackSize).sendPrefixedMessage(player);
            else
                plugin.getLocale().getMessage("event.upgrade.successmaxed")
                        .processPlaceholder("size", currentStackSize).sendPrefixedMessage(player);
        else
            plugin.getLocale().getMessage("event.tierup.success").processPlaceholder("tier", tier.getCompiledDisplayName());

        Location loc = location.clone();
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
            player.playSound(player.getLocation(), CompatibleSound.ENTITY_PLAYER_LEVELUP.getSound(), 0.6F, 15.0F);
        } else {
            player.playSound(player.getLocation(), CompatibleSound.ENTITY_PLAYER_LEVELUP.getSound(), 2F, 25.0F);
            player.playSound(player.getLocation(), CompatibleSound.BLOCK_NOTE_BLOCK_CHIME.getSound(), 2F, 25.0F);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.playSound(player.getLocation(), CompatibleSound.BLOCK_NOTE_BLOCK_CHIME.getSound(), 1.2F, 35.0F), 5L);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.playSound(player.getLocation(), CompatibleSound.BLOCK_NOTE_BLOCK_CHIME.getSound(), 1.8F, 35.0F), 10L);
        }
    }

    @Override
    public List<Boosted> getBoosts() {
        EpicSpawners instance = EpicSpawners.getInstance();
        Set<Boosted> boosts = instance.getBoostManager().getBoosts();

        List<Boosted> found = new ArrayList<>();
        for (Boosted boost : new ArrayList<>(boosts)) {
            if (boost instanceof BoostedPlayerImpl && placedBy == null) continue;
            if (System.currentTimeMillis() >= boost.getEndTime()) {
                instance.getBoostManager().removeBoost(boost);
                instance.getDataManager().deleteBoost(boost);
                continue;
            }

            if (boost instanceof BoostedSpawnerImpl) {
                if (!location.equals(((BoostedSpawnerImpl) boost).getLocation())) continue;
            } else if (boost instanceof BoostedPlayerImpl) {
                if (!placedBy.equals(((BoostedPlayerImpl) boost).getPlayer().getUniqueId())) continue;
            }
            found.add(boost);
        }
        return found;
    }

    @Override
    public int updateDelay() {
        int max = 0;
        int min = 0;
        for (SpawnerStack stack : spawnerStacks) {
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

        if (getStackSize() == 0) return 0;

        int delay = (int) (Math.random() * (max - min)) + min + extraTicks;

        getCreatureSpawner().setDelay(delay);
        getCreatureSpawner().update();

        return delay;
    }

    @Override
    public String getIdentifyingName() {
        String name = spawnerStacks.getFirst().getSpawnerData().getIdentifyingName();

        if (spawnerStacks.size() > 1)
            name = EpicSpawners.getInstance().getSpawnerManager().getSpawnerData("omni").getIdentifyingName();

        return name;
    }

    @Override
    public List<SpawnerStack> getSpawnerStacks() {
        return new LinkedList<>(spawnerStacks);
    }

    @Override
    public void replaceStacks(List<SpawnerStack> stacks) {
        spawnerStacks.clear();
        spawnerStacks.addAll(stacks);
    }

    @Override
    public OfflinePlayer getPlacedBy() {
        if (placedBy == null) return null;
        return Bukkit.getOfflinePlayer(placedBy);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("spawn_count", spawnCount);
        map.put("placed_by", placedBy.toString());
        map.putAll(SerializedLocation.of(location));
        return map;
    }

    @Override
    public Data deserialize(Map<String, Object> map) {
        id = (int) map.get("id");
        spawnCount = (int) map.get("spawn_count");
        placedBy = UUID.fromString((String) map.get("placed_by"));
        location = SerializedLocation.of(map);
        return this;
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
        return spawnCount;
    }

    @Override
    public void setSpawnCount(int spawnCount) {
        this.spawnCount = spawnCount;
    }

    @Override
    public String getOmniState() {
        return omniState;
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
        plugin.getDataManager().deleteSpawner(this);
        plugin.clearHologram(this);
    }

    @Override
    public boolean isValid() {
        return !spawnerStacks.isEmpty()
                && getFirstStack() != null
                && getFirstStack().getCurrentTier() != null
                && getFirstStack().getSpawnerData() != null;
    }

    @Override
    public boolean merge(SpawnerStack toMerge, SpawnerTier oldTier) {
        EpicSpawners plugin = EpicSpawners.getInstance();
        boolean modified = false;
        for (SpawnerStack stack : getSpawnerStacks()) {
            if (stack == toMerge
                    || !stack.getCurrentTier().equals(toMerge.getCurrentTier())) continue;
            stack.setStackSize(toMerge.getStackSize() + stack.getStackSize());
            spawnerStacks.remove(toMerge);
            plugin.getDataManager().deleteSpawnerStack(toMerge, oldTier);
            plugin.getDataManager().updateSpawnerStack(stack);
            modified = true;
            break;
        }
        plugin.updateHologram(this);
        return modified;
    }

    @Override
    public String getHologramId() {
        return "EpicSpawners-" + uniqueHologramId;
    }

    @Override
    public int hashCode() {
        int result = 31 * (location == null ? 0 : location.hashCode());
        result = 31 * result + (placedBy == null ? 0 : placedBy.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PlacedSpawnerImpl)) return false;

        PlacedSpawnerImpl other = (PlacedSpawnerImpl) obj;
        return Objects.equals(location, other.location) && Objects.equals(placedBy, other.placedBy);
    }

    @Override
    public String toString() {
        return "Spawner:{"
                + "Owner:\"" + placedBy + "\","
                + "Location:{"
                + "World:\"" + location.getWorld().getName() + "\","
                + "X:" + location.getBlockX() + ","
                + "Y:" + location.getBlockY() + ","
                + "Z:" + location.getBlockZ()
                + "},"
                + "StackCount:" + spawnerStacks.size()
                + "}";
    }
}
