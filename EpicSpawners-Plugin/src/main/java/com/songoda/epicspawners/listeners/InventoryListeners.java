package com.songoda.epicspawners.listeners;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.particles.ParticleDensity;
import com.songoda.epicspawners.api.particles.ParticleEffect;
import com.songoda.epicspawners.api.particles.ParticleType;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.player.MenuType;
import com.songoda.epicspawners.player.PlayerData;
import com.songoda.epicspawners.spawners.editor.EditingData;
import com.songoda.epicspawners.spawners.editor.EditingMenu;
import com.songoda.epicspawners.spawners.spawner.ESpawner;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

/**
 * Created by songoda on 2/25/2017.
 */
public class InventoryListeners implements Listener {

    private EpicSpawnersPlugin instance;

    public InventoryListeners(EpicSpawnersPlugin instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            if (event.getInventory() == null || event.getCurrentItem() == null) return;
            Player player = (Player) event.getWhoClicked();
            PlayerData playerData = instance.getPlayerActionManager().getPlayerAction(player);

            if (playerData.getInMenu() == MenuType.SHOP) {
                event.setCancelled(true);
                int amt = event.getInventory().getItem(22).getAmount();
                if (event.getSlot() == 0) {
                    int page = playerData.getCurrentPage();
                    instance.getShop().open(player, page);
                } else if (event.getSlot() == 8) {
                    player.closeInventory();
                } else if (event.getSlot() == 19) {
                    if (amt != 1)
                        amt = 1;
                    instance.getShop().show(amt, player);
                } else if (event.getSlot() == 29) {
                    if ((amt - 10) <= 64 && (amt - 10) >= 1)
                        amt = amt - 10;
                    instance.getShop().show(amt, player);
                } else if (event.getSlot() == 11) {
                    if ((amt - 1) <= 64 && (amt - 1) >= 1)
                        amt = amt - 1;
                    instance.getShop().show(amt, player);
                } else if (event.getSlot() == 15) {
                    if ((amt + 1) <= 64 && (amt + 1) >= 1)
                        amt = amt + 1;
                    instance.getShop().show(amt, player);
                } else if (event.getSlot() == 33) {
                    if ((amt + 10) <= 64 && (amt + 10) >= 1)
                        amt = amt + 10;
                    instance.getShop().show(amt, player);
                } else if (event.getSlot() == 25) {
                    if (amt != 64)
                        amt = 64;
                    instance.getShop().show(amt, player);
                } else if (event.getSlot() == 40) {
                    instance.getShop().confirm(player, amt);
                    player.closeInventory();
                }
            } else if (instance.getPlayerActionManager().getPlayerAction(player).getInMenu() == MenuType.PLAYERBOOST) {
                event.setCancelled(true);
                ESpawner spawner = instance.getPlayerActionManager().getPlayerAction(player).getLastSpawner();
                if (event.getSlot() == 8) {
                    spawner.addBoostAmt(player, spawner.getBoostAmt(player) + 1);
                    spawner.playerBoost(player);
                } else if (event.getSlot() == 0) {
                    spawner.addBoostAmt(player, spawner.getBoostAmt(player) - 1);
                    spawner.playerBoost(player);
                } else if (event.getSlot() == 10) {
                    spawner.purchaseBoost(player, 5);
                } else if (event.getSlot() == 12) {
                    spawner.purchaseBoost(player, 15);
                } else if (event.getSlot() == 14) {
                    spawner.purchaseBoost(player, 30);
                } else if (event.getSlot() == 16) {
                    spawner.purchaseBoost(player, 60);
                }
            } else if (instance.getPlayerActionManager().getPlayerAction(player).getInMenu() == MenuType.CONVERT) {
                event.setCancelled(true);
                ItemStack clicked = event.getCurrentItem();
                ESpawner spawner = instance.getPlayerActionManager().getPlayerAction(player).getLastSpawner();

                int page = playerData.getCurrentPage();

                if (event.getInventory().getType() == InventoryType.CHEST) {
                    if (event.getSlot() == 8) {
                        player.closeInventory();
                    } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back"))) {
                        if (page != 1) {
                            spawner.convertOverview(player, page - 1);
                        }
                    } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.next"))) {
                        spawner.convertOverview(player, page + 1);
                    } else if (clicked.getType() == Material.PLAYER_HEAD) {
                        spawner.convert(instance.getSpawnerDataFromItem(clicked), player);
                    }
                }
            } else if (instance.getSpawnerEditor().getEditingData(player).getMenu() != EditingMenu.NOT_IN) {

                if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;

                EditingData editingData = instance.getSpawnerEditor().getEditingData(player);
                EditingMenu editingMenu = editingData.getMenu();
                if (editingMenu == EditingMenu.OVERVIEW) {
                    event.setCancelled(true);
                    if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back")))
                        instance.getSpawnerEditor().openSpawnerSelector(player, playerData.getCurrentPage());
                    else if (event.getSlot() == 11) {
                        if (!event.getClick().isLeftClick() && !event.getClick().isRightClick()) {
                            SpawnerData spawnerData = instance.getSpawnerEditor().getType(editingData.getSpawnerSlot());
                            spawnerData.setDisplayItem(Material.valueOf(player.getInventory().getItemInMainHand().getType().toString()));
                            player.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&7Display Item for &6" + spawnerData.getIdentifyingName() + " &7set to &6" + player.getInventory().getItemInMainHand().getType().toString() + "&7."));
                            instance.getSpawnerEditor().overview(player, editingData.getSpawnerSlot());
                        } else if (event.getClick().isLeftClick()) {
                            instance.getSpawnerEditor().editSpawnerName(player);
                        }
                    } else if (event.getSlot() == 24)
                        instance.getSpawnerEditor().editor(player, EditingMenu.DROPS);
                    else if (event.getSlot() == 25)
                        instance.getSpawnerEditor().editor(player, EditingMenu.ENTITY);
                    else if (event.getSlot() == 32)
                        instance.getSpawnerEditor().editor(player, EditingMenu.BLOCK);
                    else if (event.getSlot() == 34)
                        instance.getSpawnerEditor().particleEditor(player);
                    else if (event.getSlot() == 29) {
                        boolean right = event.isRightClick();
                        for (final EntityType val : EntityType.values()) {
                            if (val.isSpawnable() && val.isAlive()) {
                                if (val.name().equals(Methods.restoreType(instance.getSpawnerEditor().getType(editingData.getSpawnerSlot()).getIdentifyingName()))) {
                                    right = false;
                                }
                            }
                        }
                        if (!right) {
                            SpawnerData spawnerData = instance.getSpawnerEditor().getType(editingData.getSpawnerSlot());
                            if (spawnerData.isActive())
                                spawnerData.setActive(false);
                            else
                                spawnerData.setActive(true);
                            instance.getSpawnerEditor().overview(player, editingData.getSpawnerSlot());
                        } else {
                            instance.getSpawnerEditor().destroy(player);
                        }
                    } else if (event.getSlot() == 23) {
                        instance.getSpawnerEditor().basicSettings(player);
                    } else if (event.getSlot() == 41)
                        instance.getSpawnerEditor().editor(player, EditingMenu.ITEM);
                    else if (event.getSlot() == 43)
                        instance.getSpawnerEditor().editor(player, EditingMenu.COMMAND);
                } else if (editingMenu == EditingMenu.PARTICLE) {
                    SpawnerData spawnerData = instance.getSpawnerEditor().getType(editingData.getSpawnerSlot());
                    event.setCancelled(true);

                    if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back")))
                        instance.getSpawnerEditor().overview(player, editingData.getSpawnerSlot());
                    else if (event.getSlot() == 20) {
                        ParticleType currentParticleType;
                        if (event.isLeftClick()) {
                            currentParticleType = spawnerData.getEntitySpawnParticle();
                        } else if (event.isRightClick()) {
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


                        if (event.isLeftClick()) {
                            spawnerData.setEntitySpawnParticle(currentParticleType);
                        } else if (event.isRightClick()) {
                            spawnerData.setSpawnEffectParticle(currentParticleType);
                        } else {
                            spawnerData.setSpawnerSpawnParticle(currentParticleType);
                        }

                        instance.getSpawnerEditor().particleEditor(player);
                    } else if (event.getSlot() == 22) {
                        ParticleEffect currentParticleEffect = spawnerData.getParticleEffect();

                        if (event.isLeftClick()) {
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
                        } else if (event.isRightClick()) {
                            if (!spawnerData.isParticleEffectBoostedOnly())
                                spawnerData.setParticleEffectBoostedOnly(true);
                            else
                                spawnerData.setParticleEffectBoostedOnly(false);
                        }

                        instance.getSpawnerEditor().particleEditor(player);
                    } else if (event.getSlot() == 24) {
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

                        instance.getSpawnerEditor().particleEditor(player);
                    }

                } else if (editingMenu == EditingMenu.GENERAL) {
                    if (event.getInventory().equals(player.getOpenInventory().getTopInventory())) {
                        SpawnerData spawnerData = instance.getSpawnerEditor().getType(editingData.getSpawnerSlot());
                        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back")))
                            instance.getSpawnerEditor().overview(player, editingData.getSpawnerSlot());
                        else if (event.getSlot() == 13) {
                            if (spawnerData.isUpgradeable())
                                spawnerData.setUpgradeable(false);
                            else
                                spawnerData.setUpgradeable(true);
                            instance.getSpawnerEditor().basicSettings(player);
                        } else if (event.getSlot() == 19) {
                            instance.getSpawnerEditor().alterSetting(player, ChatListeners.EditingType.SHOP_PRICE);
                        } else if (event.getSlot() == 20) {
                            if (spawnerData.isInShop())
                                spawnerData.setInShop(false);
                            else
                                spawnerData.setInShop(true);
                            instance.getSpawnerEditor().basicSettings(player);
                        } else if (event.getSlot() == 22) {
                            if (spawnerData.isSpawnOnFire())
                                spawnerData.setSpawnOnFire(false);
                            else
                                spawnerData.setSpawnOnFire(true);
                            instance.getSpawnerEditor().basicSettings(player);
                        } else if (event.getSlot() == 24) {
                            instance.getSpawnerEditor().alterSetting(player, ChatListeners.EditingType.CUSTOM_ECO_COST);
                        } else if (event.getSlot() == 25) {
                            instance.getSpawnerEditor().alterSetting(player, ChatListeners.EditingType.CUSTOM_XP_COST);
                        } else if (event.getSlot() == 30) {
                            instance.getSpawnerEditor().alterSetting(player, ChatListeners.EditingType.CUSTOM_GOAL);
                        } else if (event.getSlot() == 32) {
                            instance.getSpawnerEditor().alterSetting(player, ChatListeners.EditingType.PICKUP_COST);
                        } else if (event.getSlot() == 40) {
                            instance.getSpawnerEditor().alterSetting(player, ChatListeners.EditingType.TICK_RATE);
                        }
                        event.setCancelled(true);
                    }
                } else if (editingMenu != EditingMenu.SPAWNER_SELECTOR) {
                    if (event.getInventory().equals(player.getOpenInventory().getTopInventory())) {
                        if ((event.getSlot() < 10 || event.getSlot() > 25) || event.getSlot() == 17 || event.getSlot() == 18) {
                            event.setCancelled(true);
                            if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back")))
                                instance.getSpawnerEditor().overview(player, editingData.getSpawnerSlot());
                            else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(TextComponent.formatText("&6Add Command")))
                                instance.getSpawnerEditor().createCommand(player);
                            else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(TextComponent.formatText("&6Add entity")))
                                instance.getSpawnerEditor().addEntityInit(player);
                            else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(TextComponent.formatText("&aSave")))
                                instance.getSpawnerEditor().save(player, instance.getSpawnerEditor().getItems(player));
                            else if (event.getSlot() == 49)
                                instance.getSpawnerEditor().editSpawnLimit(player);
                        }
                    }
                }
            } else if (event.getInventory().getTitle().equals("Spawner Editor")) {
                event.setCancelled(true);
                int page = playerData.getCurrentPage();
                if (event.getSlot() == 8) {
                    player.closeInventory();
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(TextComponent.formatText("&9&lNew Spawner"))) {
                    instance.getSpawnerEditor().getEditingData(player).setNewId(instance.getSpawnerManager().getAllSpawnerData().size() - 1);
                    instance.getSpawnerEditor().overview(player, 0);
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back"))) {
                    if (page != 1) {
                        instance.getSpawnerEditor().openSpawnerSelector(player, page - 1);
                    }
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.next"))) {
                    instance.getSpawnerEditor().openSpawnerSelector(player, page + 1);
                } else if (!event.getCurrentItem().getType().name().contains("GLASS_PANE")) {
                    String idd = event.getCurrentItem().getItemMeta().getLore().get(1);
                    idd = idd.replace("§", "").replace(";", "");
                    int id = Integer.parseInt(idd);
                    //if (e.getClick().isLeftClick())
                    instance.getSpawnerEditor().overview(player, id);
                }
            } else if (event.getInventory().getTitle().equals(instance.getLocale().getMessage("interface.spawnerstats.title"))) {
                event.setCancelled(true);
                if (event.getSlot() == 8) {
                    player.closeInventory();
                }
            } else if (event.getInventory().getTitle().equals(instance.getLocale().getMessage("interface.shop.title"))) {
                event.setCancelled(true);
                ItemStack clicked = event.getCurrentItem();

                int page = playerData.getCurrentPage();

                if (event.getInventory().getType() == InventoryType.CHEST) {
                    if (event.getSlot() == 8) {
                        player.closeInventory();
                    } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back"))) {
                        if (page != 1) {
                            instance.getShop().open(player, page - 1);
                        }
                    } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.next"))) {
                        instance.getShop().open(player, page + 1);
                    } else if (event.getSlot() >= 10 && event.getSlot() <= (event.getInventory().getSize() - 10) && event.getSlot() != 17 && event.getSlot() != (event.getInventory().getSize() - 18)) {
                        playerData.setLastData(instance.getSpawnerDataFromItem(clicked));
                        instance.getShop().show(1, player);
                    }
                }
            }
            if (event.getSlot() != 64537) {
                if (event.getInventory().getType() == InventoryType.ANVIL) {
                    if (event.getAction() != InventoryAction.NOTHING) {
                        if (event.getCurrentItem().getType() != Material.AIR) {
                            ItemStack item = event.getCurrentItem();
                            if (item.getType() == Material.SPAWNER) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent event) {
        try {
            final Player p = (Player) event.getPlayer();

            PlayerData playerData = instance.getPlayerActionManager().getPlayerAction(p);


            if (playerData.getInMenu() == MenuType.SHOP) {
                Bukkit.getScheduler().runTaskLater(instance, () -> {
                    if (!p.getOpenInventory().getTopInventory().getType().equals(InventoryType.CHEST))
                        p.closeInventory();
                }, 1L);
            }

            playerData.setInMenu(MenuType.NOT_IN);

            instance.getSpawnerEditor().getEditingData(p).setMenu(EditingMenu.NOT_IN);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}
