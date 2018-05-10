package com.songoda.epicspawners.listeners;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.particles.ParticleAmount;
import com.songoda.epicspawners.particles.ParticleEffect;
import com.songoda.epicspawners.particles.ParticleType;
import com.songoda.epicspawners.player.MenuType;
import com.songoda.epicspawners.player.PlayerData;
import com.songoda.epicspawners.spawners.editor.EditingData;
import com.songoda.epicspawners.spawners.editor.EditingMenu;
import com.songoda.epicspawners.spawners.object.Spawner;
import com.songoda.epicspawners.spawners.object.SpawnerData;
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

    private EpicSpawners instance;

    public InventoryListeners(EpicSpawners instance) {
        this.instance = instance;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            if (event.getInventory() == null) return;
            Player player = (Player) event.getWhoClicked();
            if (instance.inShow.containsKey(event.getWhoClicked())) {
                event.setCancelled(true);
                int amt = event.getInventory().getItem(22).getAmount();
                if (event.getSlot() == 0) {
                    int page = instance.page.get(player);
                    instance.getShop().open(player, page);
                } else if (event.getSlot() == 8) {
                    player.closeInventory();
                } else if (event.getSlot() == 19) {
                    if (amt != 1)
                        amt = 1;
                    instance.getShop().show(instance.inShow.get(player), amt, player);
                } else if (event.getSlot() == 29) {
                    if ((amt - 10) <= 64 && (amt - 10) >= 1)
                        amt = amt - 10;
                    instance.getShop().show(instance.inShow.get(player), amt, player);
                } else if (event.getSlot() == 11) {
                    if ((amt - 1) <= 64 && (amt - 1) >= 1)
                        amt = amt - 1;
                    instance.getShop().show(instance.inShow.get(player), amt, player);
                } else if (event.getSlot() == 15) {
                    if ((amt + 1) <= 64 && (amt + 1) >= 1)
                        amt = amt + 1;
                    instance.getShop().show(instance.inShow.get(player), amt, player);
                } else if (event.getSlot() == 33) {
                    if ((amt + 10) <= 64 && (amt + 10) >= 1)
                        amt = amt + 10;
                    instance.getShop().show(instance.inShow.get(player), amt, player);
                } else if (event.getSlot() == 25) {
                    if (amt != 64)
                        amt = 64;
                    instance.getShop().show(instance.inShow.get(player), amt, player);
                } else if (event.getSlot() == 40) {
                    instance.getShop().confirm(player, amt);
                    player.closeInventory();
                }
            } else if (instance.getPlayerActionManager().getPlayerAction(player).getInMenu() == MenuType.PLAYERBOOST) {
                event.setCancelled(true);
                Spawner spawner = instance.getPlayerActionManager().getPlayerAction(player).getLastSpawner();
                if (event.getSlot() == 8) {
                    instance.boostAmt.put(player, instance.boostAmt.get(player) + 1);
                    spawner.playerBoost(player);
                } else if (event.getSlot() == 0) {
                    instance.boostAmt.put(player, instance.boostAmt.get(player) - 1);
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
                Spawner spawner = instance.getPlayerActionManager().getPlayerAction(player).getLastSpawner();

                int page = instance.page.get(player);

                if (event.getInventory().getType() == InventoryType.CHEST) {
                    if (event.getSlot() == 8) {
                        player.closeInventory();
                    } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back"))) {
                        if (page != 1) {
                            spawner.convertOverview(player, page - 1);
                        }
                    } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.next"))) {
                        spawner.convertOverview(player, page + 1);
                    } else if (clicked.getType() == Material.SKULL_ITEM || clicked.getType() == Material.MOB_SPAWNER) {
                        spawner.convert(instance.getApi().getIType(clicked), player);
                    }
                }
            } else if (instance.getPlayerActionManager().getPlayerAction(player).getInMenu() == MenuType.OVERVIEW) {
                event.setCancelled(true);
                PlayerData playerData = instance.getPlayerActionManager().getPlayerAction(player);
                Spawner spawner = playerData.getLastSpawner();
                if (spawner.getFirstStack().getSpawnerData().isUpgradeable()) {
                    if (event.getSlot() == 11) {
                        if (instance.getConfig().getBoolean("Main.Upgrade With XP")) {
                            if (event.getCurrentItem().getItemMeta().getDisplayName() != "§l") {
                                spawner.upgrade(player, "XP");
                            }
                            player.closeInventory();
                        }
                    } else if (event.getSlot() == 8) {
                        int page = playerData.getInfoPage() + 1;
                        playerData.setInfoPage(page);
                        spawner.overview(player, page);
                    } else if (event.getSlot() == 13) {
                        if (event.getClick().isRightClick()) {
                            if (spawner.getBoost() == 0) {
                                spawner.playerBoost(player);
                            }
                        } else if (event.getClick().isLeftClick()) {
                            if (player.hasPermission("epicspawners.convert") && spawner.getSpawnerStacks().size() == 1) {
                                spawner.convertOverview(player, 1);
                            }
                        }
                    } else if (event.getSlot() == 15) {
                        if (instance.getConfig().getBoolean("Main.Upgrade With Economy")) {
                            if (!event.getCurrentItem().getItemMeta().getDisplayName().equals("§l")) {
                                spawner.upgrade(player, "ECO");
                                player.closeInventory();
                            }
                        }
                    }
                }
            } else if (instance.getSpawnerEditor().getEditingData(player).getMenu() != EditingMenu.NOTIN) {
                EditingData editingData = instance.getSpawnerEditor().getEditingData(player);
                EditingMenu editingMenu = editingData.getMenu();
                if (editingMenu == EditingMenu.OVERVIEW) {
                    event.setCancelled(true);
                    if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back")))
                        instance.getSpawnerEditor().openSpawnerSelector(player, instance.page.get(player));
                    else if (event.getSlot() == 11) {
                        if (!event.getClick().isLeftClick() && !event.getClick().isRightClick()) {
                            SpawnerData spawnerData = instance.getSpawnerEditor().getType(editingData.getSpawnerSlot());
                            spawnerData.setDisplayItem(Material.valueOf(player.getItemInHand().getType().toString()));
                            player.sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&7Display Item for &6" + spawnerData.getName() + " &7set to &6" + player.getItemInHand().getType().toString() + "&7."));
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
                                if (val.name().equals(Methods.restoreType(instance.getSpawnerEditor().getType(editingData.getSpawnerSlot()).getName()))) {
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

                    if (event.getSlot() == 20) {
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
                        ParticleAmount currentParticleAmount = spawnerData.getParticleAmount();

                        boolean next = false;
                        for (ParticleAmount particleAmount : ParticleAmount.values()) {
                            if (currentParticleAmount == particleAmount) {
                                next = true;
                            } else if (next) {
                                currentParticleAmount = particleAmount;
                                next = false;
                            }
                        }
                        if (next) {
                            currentParticleAmount = ParticleAmount.values()[0];
                        }
                        spawnerData.setParticleAmount(currentParticleAmount);

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
                            instance.getSpawnerEditor().alterSetting(player, "Shop-Price");
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
                            instance.getSpawnerEditor().alterSetting(player, "Custom-ECO-Cost");
                        } else if (event.getSlot() == 25) {
                            instance.getSpawnerEditor().alterSetting(player, "Custom-XP-Cost");
                        } else if (event.getSlot() == 30) {
                            instance.getSpawnerEditor().alterSetting(player, "CustomGoal");
                        } else if (event.getSlot() == 32) {
                            instance.getSpawnerEditor().alterSetting(player, "Pickup-cost");
                        } else if (event.getSlot() == 40) {
                            instance.getSpawnerEditor().alterSetting(player, "Tick-Rate");
                        }
                        event.setCancelled(true);
                    }
                } else if (editingMenu != EditingMenu.SPAWNERSELECTOR) {
                    if (event.getInventory().equals(player.getOpenInventory().getTopInventory())) {
                        if ((event.getSlot() < 10 || event.getSlot() > 25) || event.getSlot() == 17 || event.getSlot() == 18) {
                            event.setCancelled(true);
                            if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back")))
                                instance.getSpawnerEditor().overview(player, editingData.getSpawnerSlot());
                            else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(Arconix.pl().getApi().format().formatText("&6Add Command")))
                                instance.getSpawnerEditor().createCommand(player);
                            else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(Arconix.pl().getApi().format().formatText("&6Add entity")))
                                instance.getSpawnerEditor().addEntityInit(player);
                            else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(Arconix.pl().getApi().format().formatText("&aSave")))
                                instance.getSpawnerEditor().save(player, instance.getSpawnerEditor().getItems(player));
                            else if (event.getSlot() == 49)
                                instance.getSpawnerEditor().editSpawnLimit(player);
                        }
                    }
                }
            } else if (event.getInventory().getTitle().equals("Spawner Editor")) {
                event.setCancelled(true);
                int page = instance.page.get(player);
                if (event.getSlot() == 8) {
                    player.closeInventory();
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(Arconix.pl().getApi().format().formatText("&9&lNew Spawner"))) {
                    instance.getSpawnerEditor().overview(player, 0);
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back"))) {
                    if (page != 1) {
                        instance.getSpawnerEditor().openSpawnerSelector(player, page - 1);
                    }
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.next"))) {
                    instance.getSpawnerEditor().openSpawnerSelector(player, page + 1);
                } else if (event.getCurrentItem().getType() != Material.STAINED_GLASS_PANE) {
                    String idd = event.getCurrentItem().getItemMeta().getLore().get(1);
                    idd = idd.replace("§", "");
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

                int page = instance.page.get(player);

                if (event.getInventory().getType() == InventoryType.CHEST) {
                    if (event.getSlot() == 8) {
                        player.closeInventory();
                    } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back"))) {
                        if (page != 1) {
                            instance.getShop().open(player, page - 1);
                        }
                    } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.next"))) {
                        instance.getShop().open(player, page + 1);
                    } else if (event.getSlot() >= 10 && event.getSlot() <= (event.getInventory().getSize() - 10) && event.getSlot() != 17 && event.getSlot() != (event.getInventory().getSize() - 18))
                        instance.getShop().show(instance.getSpawnerManager().getSpawnerType(Methods.getTypeFromString(instance.getApi().getIType(clicked))), 1, player);
                }
            }
            if (event.getSlot() != 64537) {
                if (event.getInventory().getType() == InventoryType.ANVIL) {
                    if (event.getAction() != InventoryAction.NOTHING) {
                        if (event.getCurrentItem().getType() != Material.AIR) {
                            ItemStack item = event.getCurrentItem();
                            if (item.getType() == Material.MOB_SPAWNER) {
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

            instance.getPlayerActionManager().getPlayerAction(p).setInMenu(MenuType.NOTIN);

            if (instance.inShow.containsKey(p)) {
                instance.inShow.remove(p);
                Bukkit.getScheduler().runTaskLater(instance, () -> {
                    if (!p.getOpenInventory().getTopInventory().getType().equals(InventoryType.CHEST))
                        p.closeInventory();
                }, 1L);
            }
            instance.getSpawnerEditor().getEditingData(p).setMenu(EditingMenu.NOTIN);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}
