package com.songoda.epicspawners.Events;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Spawners.Spawner;
import com.songoda.epicspawners.Spawners.SpawnerItem;
import com.songoda.epicspawners.Utils.Debugger;
import com.songoda.epicspawners.Utils.Methods;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class InventoryListeners implements Listener {

    private EpicSpawners instance;

    public InventoryListeners(EpicSpawners instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        try {
            if (e.getInventory() == null) return;
            Inventory inv = e.getInventory();
            Player p = (Player) e.getWhoClicked();
            if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Allow Stacking Spawners In Survival Inventories")
                    && p.getOpenInventory().getType().equals(InventoryType.CRAFTING)) {
                if ((inv == null)
                        && (e.getCursor() == null)
                        && (e.getCurrentItem() == null)) {
                    return;
                }
                ItemStack c = e.getCursor();
                ItemStack item = e.getCurrentItem();
                if (!e.isRightClick() && item.getType() != Material.MOB_SPAWNER) {
                    return;
                }
                if (c.getType() == Material.MOB_SPAWNER) {
                    if (item.getAmount() != 1 || c.getAmount() != 1) {
                        p.sendMessage(instance.getLocale().getMessage("event.combine.onlyone"));
                        return;
                    }
                    if (item.getAmount() == 1 && c.getAmount() - item.getAmount() == 1 || c.getAmount() == 1) {
                        Spawner eSpawner = new Spawner();
                        if (eSpawner.processCombine(p, item, c)) {
                            e.setCurrentItem(new ItemStack(Material.AIR));
                        }
                    }
                } else if (c.getType() == Material.AIR) {
                    if (item.getAmount() != 1) {
                        return;
                    }
                    if (EpicSpawners.getInstance().getApi().getType(item).equals("OMNI")) {
                        List<ItemStack> items = EpicSpawners.getInstance().getApi().removeOmni(item);
                        e.setCurrentItem(items.get(0));
                        e.setCursor(items.get(1));
                    } else {
                        List<ItemStack> items = EpicSpawners.getInstance().getApi().removeSpawner(item);
                        if (items.size() == 2) {
                            e.setCurrentItem(items.get(1));
                            e.setCursor(items.get(0));
                        }
                    }
                }
            }


            if (EpicSpawners.getInstance().inShow.containsKey(e.getWhoClicked())) {
                e.setCancelled(true);
                int amt = e.getInventory().getItem(22).getAmount();
                if (e.getSlot() == 0) {
                    int page = EpicSpawners.getInstance().page.get(p);
                    EpicSpawners.getInstance().shop.open(p, page);
                } else if (e.getSlot() == 8) {
                    p.closeInventory();
                } else if (e.getSlot() == 19) {
                    if (amt != 1)
                        amt = 1;
                    EpicSpawners.getInstance().shop.show(EpicSpawners.getInstance().inShow.get(p), amt, p);
                } else if (e.getSlot() == 29) {
                    if ((amt - 10) <= 64 && (amt - 10) >= 1)
                        amt = amt - 10;
                    EpicSpawners.getInstance().shop.show(EpicSpawners.getInstance().inShow.get(p), amt, p);
                } else if (e.getSlot() == 11) {
                    if ((amt - 1) <= 64 && (amt - 1) >= 1)
                        amt = amt - 1;
                    EpicSpawners.getInstance().shop.show(EpicSpawners.getInstance().inShow.get(p), amt, p);
                } else if (e.getSlot() == 15) {
                    if ((amt + 1) <= 64 && (amt + 1) >= 1)
                        amt = amt + 1;
                    EpicSpawners.getInstance().shop.show(EpicSpawners.getInstance().inShow.get(p), amt, p);
                } else if (e.getSlot() == 33) {
                    if ((amt + 10) <= 64 && (amt + 10) >= 1)
                        amt = amt + 10;
                    EpicSpawners.getInstance().shop.show(EpicSpawners.getInstance().inShow.get(p), amt, p);
                } else if (e.getSlot() == 25) {
                    if (amt != 64)
                        amt = 64;
                    EpicSpawners.getInstance().shop.show(EpicSpawners.getInstance().inShow.get(p), amt, p);
                } else if (e.getSlot() == 40) {
                    EpicSpawners.getInstance().shop.confirm(p, amt);
                    p.closeInventory();
                }
            } else if (EpicSpawners.getInstance().boosting.contains(p)) {
                e.setCancelled(true);
                Spawner eSpawner = new Spawner(EpicSpawners.getInstance().lastSpawner.get(p));
                if (e.getSlot() == 8) {
                    EpicSpawners.getInstance().boostAmt.put(p, EpicSpawners.getInstance().boostAmt.get(p) + 1);
                    eSpawner.playerBoost(p);
                } else if (e.getSlot() == 0) {
                    EpicSpawners.getInstance().boostAmt.put(p, EpicSpawners.getInstance().boostAmt.get(p) - 1);
                    eSpawner.playerBoost(p);
                } else if (e.getSlot() == 10) {
                    eSpawner.purchaseBoost(p, 5);
                } else if (e.getSlot() == 12) {
                    eSpawner.purchaseBoost(p, 15);
                } else if (e.getSlot() == 14) {
                    eSpawner.purchaseBoost(p, 30);
                } else if (e.getSlot() == 16) {
                    eSpawner.purchaseBoost(p, 60);
                }
            } else if (e.getInventory().getTitle().equals(instance.getLocale().getMessage("interface.convert.title"))) {
                e.setCancelled(true);
                ItemStack clicked = e.getCurrentItem();
                Spawner eSpawner = new Spawner(EpicSpawners.getInstance().spawnerLoc.get(p));

                int page = EpicSpawners.getInstance().page.get(p);

                if (e.getInventory().getType() == InventoryType.CHEST) {
                    if (e.getSlot() == 8) {
                        p.closeInventory();
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back"))) {
                        if (page != 1) {
                            eSpawner.change(p, page - 1);
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.next"))) {
                        eSpawner.change(p, page + 1);
                    } else if (clicked.getType() == Material.SKULL_ITEM || clicked.getType() == Material.MOB_SPAWNER) {
                        eSpawner.convert(EpicSpawners.getInstance().getApi().getIType(clicked), p);
                    }
                }
            } else if (EpicSpawners.getInstance().spawnerLoc.containsKey(e.getWhoClicked())) {
                e.setCancelled(true);
                Spawner eSpawner = new Spawner(EpicSpawners.getInstance().spawnerLoc.get(p));

                if (EpicSpawners.getInstance().spawnerFile.getConfig().getBoolean("Entities." + eSpawner.spawnedType + ".Upgradable")) {
                    if (e.getSlot() == 11) {
                        if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Upgrade With XP")) {
                            if (!e.getCurrentItem().getItemMeta().getDisplayName().equals("§l")) {
                                eSpawner.upgrade(p, "XP");
                            }
                            p.closeInventory();
                        }
                    } else if (e.getSlot() == 8) {
                        int page = 2;
                        if (EpicSpawners.getInstance().infPage.containsKey(p))
                            page = EpicSpawners.getInstance().infPage.get(p) + 1;
                        EpicSpawners.getInstance().infPage.put(p, page);
                        eSpawner.overView(p, page);
                    } else if (e.getSlot() == 13) {
                        if (e.getClick().isRightClick()) {
                            if (eSpawner.getBoost() == 0) {
                                eSpawner.playerBoost(p);
                            }
                        } else if (e.getClick().isLeftClick()) {
                            boolean omni = false;
                            if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawnerstats." + eSpawner.locationStr + ".type")) {
                                if (EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + eSpawner.locationStr + ".type").equals("OMNI")) {
                                    List<SpawnerItem> list = EpicSpawners.getInstance().getApi().convertFromList(EpicSpawners.getInstance().dataFile.getConfig().getStringList("data.spawnerstats." + eSpawner.locationStr + ".entities"));
                                    omni = true;
                                }
                            }
                            if (p.hasPermission("epicspawners.convert") && !omni) {
                                eSpawner.change(p, 1);
                            }
                        }
                    } else if (e.getSlot() == 15) {
                        if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Upgrade With Economy")) {
                            if (!e.getCurrentItem().getItemMeta().getDisplayName().equals("§l")) {
                                eSpawner.upgrade(p, "ECO");
                                p.closeInventory();
                            }
                        }
                    }
                }
            } else if (EpicSpawners.getInstance().editing.containsKey(p)) {
                if (!EpicSpawners.getInstance().subediting.containsKey(p)) {
                    e.setCancelled(true);
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back")))
                        EpicSpawners.getInstance().editor.open(p, EpicSpawners.getInstance().page.get(p));
                    else if (e.getSlot() == 11) {
                        if (!e.getClick().isLeftClick() && !e.getClick().isRightClick()) {
                            String name = EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p));
                            EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(name) + ".Display-Item", p.getItemInHand().getType().toString());
                            p.sendMessage(Arconix.pl().getApi().format().formatText(EpicSpawners.getInstance().references.getPrefix() + "&7Display Item for &6" + name + " &7set to &6" + p.getItemInHand().getType().toString() + "&7."));
                            EpicSpawners.getInstance().editor.overview(p, EpicSpawners.getInstance().editing.get(p));
                        } else if (e.getClick().isLeftClick()) {
                            EpicSpawners.getInstance().editor.editSpawnerName(p);
                        }
                    } else if (e.getSlot() == 25)
                        EpicSpawners.getInstance().editor.editor(p, "Entity");
                    else if (e.getSlot() == 33)
                        EpicSpawners.getInstance().editor.editor(p, "Block");
                    else if (e.getSlot() == 28) {

                        boolean right = e.isRightClick();
                        for (final EntityType val : EntityType.values()) {
                            if (val.isSpawnable() && val.isAlive()) {
                                if (val.name().equals(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p)))) {
                                    right = false;
                                }
                            }
                        }
                        if (!right) {
                            if (EpicSpawners.getInstance().spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".Allowed"))
                                EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".Allowed", false);
                            else
                                EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".Allowed", true);
                            EpicSpawners.getInstance().editor.overview(p, EpicSpawners.getInstance().editing.get(p));
                        } else {
                            EpicSpawners.getInstance().editor.destroy(p);
                        }
                    } else if (e.getSlot() == 23) {
                        EpicSpawners.getInstance().editor.basicSettings(p);
                    } else if (e.getSlot() == 30) {
                        EpicSpawners.getInstance().editor.save(p);
                        EpicSpawners.getInstance().editor.overview(p, EpicSpawners.getInstance().editing.get(p));
                    } else if (e.getSlot() == 41)
                        EpicSpawners.getInstance().editor.editor(p, "Item");
                    else if (e.getSlot() == 43)
                        EpicSpawners.getInstance().editor.editor(p, "Command");
                } else {
                    if (EpicSpawners.getInstance().subediting.get(p).equals("basic")) {
                        if (e.getInventory().equals(p.getOpenInventory().getTopInventory())) {
                            if (e.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back")))
                                EpicSpawners.getInstance().editor.overview(p, EpicSpawners.getInstance().editing.get(p));
                            else if (e.getSlot() == 13) {
                                if (EpicSpawners.getInstance().spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".Upgradable"))
                                    EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".Upgradable", false);
                                else
                                    EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".Upgradable", true);
                                EpicSpawners.getInstance().editor.basicSettings(p);
                            } else if (e.getSlot() == 19) {
                                EpicSpawners.getInstance().editor.alterSetting(p, "Shop-Price");
                            } else if (e.getSlot() == 20) {
                                if (EpicSpawners.getInstance().spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".In-Shop"))
                                    EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".In-Shop", false);
                                else
                                    EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".In-Shop", true);
                                EpicSpawners.getInstance().editor.basicSettings(p);
                            } else if (e.getSlot() == 22) {
                                if (EpicSpawners.getInstance().spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".Spawn-On-Fire"))
                                    EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".Spawn-On-Fire", false);
                                else
                                    EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".Spawn-On-Fire", true);
                                EpicSpawners.getInstance().editor.basicSettings(p);
                            } else if (e.getSlot() == 24) {
                                EpicSpawners.getInstance().editor.alterSetting(p, "Custom-ECO-Cost");
                            } else if (e.getSlot() == 25) {
                                EpicSpawners.getInstance().editor.alterSetting(p, "Custom-XP-Cost");
                            } else if (e.getSlot() == 30) {
                                EpicSpawners.getInstance().editor.alterSetting(p, "CustomGoal");
                            } else if (e.getSlot() == 32) {
                                EpicSpawners.getInstance().editor.alterSetting(p, "Pickup-cost");
                            }
                            e.setCancelled(true);
                        }
                    } else {
                        if (e.getInventory().equals(p.getOpenInventory().getTopInventory())) {
                            if ((e.getSlot() < 10 || e.getSlot() > 25) || e.getSlot() == 17 || e.getSlot() == 18) {
                                e.setCancelled(true);
                                if (e.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back")))
                                    EpicSpawners.getInstance().editor.overview(p, EpicSpawners.getInstance().editing.get(p));
                                else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Arconix.pl().getApi().format().formatText("&6Add Command")))
                                    EpicSpawners.getInstance().editor.createCommand(p);
                                else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Arconix.pl().getApi().format().formatText("&6Add Entity")))
                                    EpicSpawners.getInstance().editor.addEntityInit(p);
                                else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Arconix.pl().getApi().format().formatText("&aSave")))
                                    EpicSpawners.getInstance().editor.saveInstance(p, EpicSpawners.getInstance().editor.getItems(p));
                                else if (e.getSlot() == 40)
                                    EpicSpawners.getInstance().editor.editChatInit(p);
                                else if (e.getSlot() == 49)
                                    EpicSpawners.getInstance().editor.editSpawnLimit(p);
                            }
                        }
                    }
                }
            } else if (e.getInventory().getTitle().equals("Spawner Editor")) {
                e.setCancelled(true);
                int page = EpicSpawners.getInstance().page.get(p);
                if (e.getSlot() == 8) {
                    p.closeInventory();
                } else if ((e.getCurrentItem().getType().equals(Material.SKULL_ITEM) || e.getCurrentItem().getType().equals(Material.MOB_SPAWNER)) &&
                        !e.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.next")) &&
                        !e.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back"))) {
                    String idd = e.getCurrentItem().getItemMeta().getLore().get(1);
                    idd = idd.replace("§", "");
                    int id = Integer.parseInt(idd);
                    //if (e.getClick().isLeftClick())
                    EpicSpawners.getInstance().editor.overview(p, id);
                } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Arconix.pl().getApi().format().formatText("&9&lNew Spawner"))) {
                    EpicSpawners.getInstance().editor.overview(p, 0);
                    EpicSpawners.getInstance().editor.save(p);
                    EpicSpawners.getInstance().editor.open(p, 1);
                } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back"))) {
                    if (page != 1) {
                        EpicSpawners.getInstance().editor.open(p, page - 1);
                    }
                } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back"))) {
                    EpicSpawners.getInstance().editor.open(p, page + 1);
                }
            } else if (e.getInventory().getTitle().equals(instance.getLocale().getMessage("interface.spawnerstats.title"))) {
                e.setCancelled(true);
                if (e.getSlot() == 8) {
                    p.closeInventory();
                }
            } else if (e.getInventory().getTitle().equals(instance.getLocale().getMessage("interface.shop.title"))) {
                e.setCancelled(true);
                ItemStack clicked = e.getCurrentItem();

                int page = EpicSpawners.getInstance().page.get(p);

                if (e.getInventory().getType() == InventoryType.CHEST) {
                    if (e.getSlot() == 8) {
                        p.closeInventory();
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.back"))) {
                        if (page != 1) {
                            EpicSpawners.getInstance().shop.open(p, page - 1);
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(instance.getLocale().getMessage("general.nametag.next"))) {
                        EpicSpawners.getInstance().shop.open(p, page + 1);
                    } else if (e.getSlot() >= 10 && e.getSlot() <= (e.getInventory().getSize() - 10) && e.getSlot() != 17 && e.getSlot() != (e.getInventory().getSize() - 18))
                        EpicSpawners.getInstance().shop.show(EpicSpawners.getInstance().getApi().getIType(clicked), 1, p);
                }
            }
            if (e.getSlot() != 64537) {
                if (e.getInventory().getType() == InventoryType.ANVIL) {
                    if (e.getAction() != InventoryAction.NOTHING) {
                        if (e.getCurrentItem().getType() != Material.AIR) {
                            ItemStack item = e.getCurrentItem();
                            if (item.getType() == Material.MOB_SPAWNER) {
                                e.setCancelled(true);
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
            if (EpicSpawners.getInstance().boosting.contains(p) || EpicSpawners.getInstance().inShow.containsKey(p) || EpicSpawners.getInstance().spawnerLoc.containsKey(p)) {
                EpicSpawners.getInstance().boosting.remove(p);
                EpicSpawners.getInstance().inShow.remove(p);
                EpicSpawners.getInstance().spawnerLoc.remove(p);
                Bukkit.getScheduler().runTaskLater(EpicSpawners.getInstance(), () -> {
                    if (!p.getOpenInventory().getTopInventory().getType().equals(InventoryType.CHEST))
                        p.closeInventory();
                }, 1L);
            }
            if (!p.getOpenInventory().getTopInventory().getTitle().contains("Editing"))
                EpicSpawners.getInstance().editing.remove(p);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}
