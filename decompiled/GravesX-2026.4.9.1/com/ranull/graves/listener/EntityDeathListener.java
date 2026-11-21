package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.compatibility.CompatibilityInventoryView;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.ExperienceUtil;
import com.ranull.graves.util.LocationUtil;
import dev.cwhead.GravesX.event.GraveBlockPlaceEvent;
import dev.cwhead.GravesX.event.GraveCreateEvent;
import dev.cwhead.GravesX.event.GraveObituaryAddEvent;
import dev.cwhead.GravesX.event.GravePlayerHeadDropEvent;
import dev.cwhead.GravesX.event.GravePostCreateEvent;
import dev.cwhead.GravesX.event.GravePreCreateEvent;
import dev.cwhead.GravesX.event.GraveProtectionCreateEvent;
import dev.cwhead.GravesX.util.SkinTextureUtil_post_1_21_9;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import me.jay.GravesX.util.SkinSignatureUtil;
import me.jay.GravesX.util.SkinTextureUtil;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class EntityDeathListener implements Listener {
   private final Graves plugin;

   public EntityDeathListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onEntityDeath(EntityDeathEvent event) {
      LivingEntity livingEntity = event.getEntity();
      boolean isPlayer = livingEntity instanceof Player;
      Player player = isPlayer ? (Player)livingEntity : null;
      PlayerDeathEvent pde = event instanceof PlayerDeathEvent ? (PlayerDeathEvent)event : null;
      if (!this.isInvalidMohistDeath(event)) {
         String entityName = this.plugin.getEntityManager().getEntityName(livingEntity);
         if (!this.isInvalidGraveZombie(event, livingEntity, entityName)) {
            Location location = LocationUtil.roundLocation(livingEntity.getLocation());
            if (location == null) {
               this.plugin.debugMessage("Grave not created for " + entityName + " because the location couldn't be determined.", 2);
            } else {
               List<String> permissionList = isPlayer ? this.plugin.getConfigManager().getPermissionList(livingEntity) : null;
               if (!isPlayer && livingEntity instanceof Zombie zombie && this.isConfiguredZombieType(zombie) && this.hasGravesXMetadata(zombie)) {
                  this.removePlayerSkullFromDrops(zombie, event);
               }

               if (this.isEnabledGrave(livingEntity, permissionList, entityName)) {
                  List<String> worldList = this.plugin.getConfigManager().getConfigSection("world", livingEntity, permissionList).getStringList("world");
                  if (this.isValidWorld(worldList, livingEntity, entityName)) {
                     if (this.isValidDamageCause(livingEntity, permissionList, entityName)) {
                        if (isPlayer || !this.isInvalidCreatureSpawn(livingEntity, null, entityName)) {
                           if (isPlayer) {
                              if (this.handlePlayerDeath(player, entityName)) {
                                 return;
                              }

                              if (pde != null && this.isKeepInventory(pde, entityName)) {
                                 boolean bypass = this.plugin.getPermissionManager().hasGrantedPermission("graves.keepinventory.bypass", player.getPlayer());
                                 if (!bypass) {
                                    this.plugin
                                       .debugMessage(
                                          "Grave not created for "
                                             + entityName
                                             + " because they had keep inventory. You can set a user to have the bypass permission.",
                                          2
                                       );
                                    return;
                                 }

                                 try {
                                    pde.setKeepInventory(false);
                                 } catch (NoSuchMethodError var19) {
                                 }

                                 this.plugin
                                    .debugMessage("Grave creation proceeding for " + entityName + "; forcing keep inventory off to avoid duplication.", 1);
                              } else if (pde != null) {
                                 pde.setKeepInventory(false);
                              }

                              if (pde != null && event.getDrops().isEmpty()) {
                                 this.plugin.debugMessage("Grave not created for " + entityName + " because they had an empty inventory", 2);
                                 return;
                              }
                           }

                           if (this.hasValidToken(livingEntity, permissionList, entityName, event.getDrops())) {
                              if (isPlayer) {
                                 int serverMax = this.plugin.getConfigManager().getConfigSection("grave.max", livingEntity, permissionList).getInt("grave.max");
                                 int permsMax = this.getMaxGravesPermission(player);
                                 int applicableMax = permsMax > 0 ? permsMax : serverMax;
                                 if (this.plugin.getGraveManager().getGraveList(livingEntity).size() >= applicableMax) {
                                    if (this.plugin.getPermissionManager().hasGrantedPermission("graves.max.replace", player)
                                       && this.plugin
                                          .getConfigManager()
                                          .getConfigSection("grave.replace-oldest", livingEntity, permissionList)
                                          .getBoolean("grave.replace-oldest")) {
                                       this.plugin.getGraveManager().removeOldestGrave(livingEntity);
                                       this.plugin
                                          .getEntityManager()
                                          .sendMessage("message.grave-oldest-replaced", livingEntity, livingEntity.getLocation(), permissionList);
                                       this.plugin.debugMessage("Grave replaced oldest for " + entityName + " because they reached maximum graves", 2);
                                    } else {
                                       if (!this.plugin.getPermissionManager().hasGrantedPermission("graves.max.bypass", player)) {
                                          this.plugin.getEntityManager().sendMessage("message.max", livingEntity, livingEntity.getLocation(), permissionList);
                                          this.plugin.debugMessage("Grave not created for " + entityName + " because they reached maximum graves.", 2);
                                          boolean keepInvOnMax = this.plugin
                                             .getConfigManager()
                                             .getConfigSection("placement.failure-keep-inventory", livingEntity, permissionList)
                                             .getBoolean("placement.failure-keep-inventory");
                                          if (keepInvOnMax && pde != null) {
                                             try {
                                                pde.setKeepLevel(true);
                                                pde.setKeepInventory(true);
                                                event.getDrops().clear();
                                                this.plugin
                                                   .getEntityManager()
                                                   .sendMessage("message.failure-keep-inventory", livingEntity, location, permissionList);
                                             } catch (NoSuchMethodError var18) {
                                             }
                                          }

                                          return;
                                       }

                                       this.plugin.debugMessage("Grave created for " + entityName + " even though they reached the maximum graves cap", 2);
                                    }
                                 }
                              }

                              List<ItemStack> ignoredItemStackList = new ArrayList<>();
                              List<ItemStack> graveItemStackList = this.getGraveItemStackList(event, livingEntity, permissionList, ignoredItemStackList);
                              if (graveItemStackList.isEmpty()) {
                                 this.plugin.debugMessage("Grave not created for " + entityName + " because they had no drops", 2);
                              } else {
                                 List<ItemStack> removedItemStackList = this.getRemovedItemStacks(livingEntity);
                                 if (isPlayer && pde != null && location.getWorld() != null) {
                                    InventoryView view = player.getOpenInventory();
                                    if (view != null) {
                                       Inventory top = CompatibilityInventoryView.getTopInventory(view);
                                       if (top != null && (top.getType() == InventoryType.WORKBENCH || top.getType() == InventoryType.CRAFTING)) {
                                          ItemStack[] contents = top.getContents();

                                          for (int i = 1; i < contents.length; i++) {
                                             ItemStack item = contents[i];
                                             if (item != null && item.getType() != Material.AIR) {
                                                location.getWorld().dropItemNaturally(location, item);
                                                top.setItem(i, null);
                                             }
                                          }
                                       }
                                    }
                                 }

                                 event.getDrops().clear();
                                 this.createGrave(
                                    event,
                                    livingEntity,
                                    entityName,
                                    permissionList,
                                    removedItemStackList,
                                    graveItemStackList,
                                    ignoredItemStackList,
                                    location,
                                    pde,
                                    player
                                 );
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private int getMaxGravesPermission(Player player) {
      int maxGraves = 0;

      for (int i = 0; i <= 10; i++) {
         if (this.plugin.getPermissionManager().hasGrantedPermission("grave.max.limit.unlimited", player)) {
            return Integer.MAX_VALUE;
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("grave.max.limit." + i, player)) {
            maxGraves = i;
         }
      }

      return maxGraves;
   }

   private boolean isConfiguredZombieType(Zombie zombie) {
      String configuredZombieType = this.plugin.getConfig().getString("zombie.type", "ZOMBIE").toUpperCase();
      return zombie.getType() == EntityType.valueOf(configuredZombieType);
   }

   private void removePlayerSkullFromDrops(LivingEntity entity, EntityDeathEvent event) {
      ItemStack helmet = entity.getEquipment() != null ? entity.getEquipment().getHelmet() : null;
      if (helmet != null && helmet.getType() == Material.PLAYER_HEAD) {
         event.getDrops().removeIf(i -> i != null && i.getType() == Material.PLAYER_HEAD);
      }
   }

   private boolean hasGravesXMetadata(Zombie zombie) {
      for (MetadataValue v : zombie.getMetadata("GravesX")) {
         if (v.asBoolean()) {
            return true;
         }
      }

      return false;
   }

   private List<ItemStack> getRemovedItemStacks(LivingEntity livingEntity) {
      List<ItemStack> list = new ArrayList<>();
      Map<UUID, List<ItemStack>> map = this.plugin.getCacheManager().getRemovedItemStackMap();
      if (map.containsKey(livingEntity.getUniqueId())) {
         list.addAll(map.get(livingEntity.getUniqueId()));
         map.remove(livingEntity.getUniqueId());
      }

      return list;
   }

   private boolean isInvalidMohistDeath(EntityDeathEvent event) {
      if (event.getEntityType() == EntityType.PLAYER && !(event instanceof PlayerDeathEvent)) {
         event.setDroppedExp(0);
         return true;
      } else {
         return false;
      }
   }

   private boolean isInvalidGraveZombie(EntityDeathEvent event, LivingEntity livingEntity, String entityName) {
      if (this.plugin.getEntityManager().hasDataByte(livingEntity, "graveZombie")) {
         EntityType type = this.plugin.getEntityManager().hasDataString(livingEntity, "graveEntityType")
            ? EntityType.valueOf(this.plugin.getEntityManager().getDataString(livingEntity, "graveEntityType"))
            : EntityType.PLAYER;
         List<String> perms = this.plugin.getEntityManager().hasDataString(livingEntity, "gravePermissionList")
            ? Arrays.asList(this.plugin.getEntityManager().getDataString(livingEntity, "gravePermissionList").split("\\|"))
            : null;
         if (!this.plugin.getConfigManager().getConfigSection("zombie.drop", type, perms).getBoolean("zombie.drop")) {
            event.setDroppedExp(0);
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean handlePlayerDeath(Player player, String entityName) {
      Plugin essentials = this.plugin.getServer().getPluginManager().getPlugin("Essentials");
      if (!this.plugin.getPermissionManager().hasGrantedPermission("graves.place", player)) {
         this.plugin.debugMessage("Grave not created for " + entityName + " because they don't have permission to place graves", 2);
         return true;
      } else {
         if (essentials != null && essentials.isEnabled() && this.plugin.getPermissionManager().hasGrantedPermission("essentials.keepinv", player)) {
            this.plugin.debugMessage(entityName + " has essentials.keepinv", 2);
         }

         return false;
      }
   }

   private boolean isEnabledGrave(LivingEntity livingEntity, List<String> permissionList, String entityName) {
      if (!this.plugin.getConfigManager().getConfigSection("grave.enabled", livingEntity, permissionList).getBoolean("grave.enabled")) {
         if (livingEntity instanceof Player) {
            this.plugin.debugMessage("Grave not created for " + entityName + " because they have graves disabled", 2);
         }

         return false;
      } else {
         return true;
      }
   }

   private boolean isKeepInventory(PlayerDeathEvent event, String entityName) {
      try {
         if (event.getKeepInventory() && !this.plugin.getPermissionManager().hasGrantedPermission("graves.keepinventory.bypass", event.getEntity().getPlayer())
            )
          {
            this.plugin.debugMessage("Grave not created for " + entityName + " because they had keep inventory", 2);
            return true;
         }
      } catch (NoSuchMethodError var4) {
      }

      return false;
   }

   private boolean isInvalidCreatureSpawn(LivingEntity livingEntity, List<String> permissionList, String entityName) {
      if (livingEntity instanceof Creature) {
         List<String> reasons = this.plugin.getConfigManager().getConfigSection("spawn.reason", livingEntity, permissionList).getStringList("spawn.reason");
         if (this.plugin.getEntityManager().hasDataString(livingEntity, "spawnReason")
            && !reasons.contains("ALL")
            && !reasons.contains(this.plugin.getEntityManager().getDataString(livingEntity, "spawnReason"))) {
            this.plugin.debugMessage("Grave not created for " + entityName + " because they had an invalid spawn reason", 2);
            return true;
         }
      }

      return false;
   }

   private boolean isValidWorld(List<String> worldList, LivingEntity livingEntity, String entityName) {
      if (!worldList.contains("ALL") && !worldList.contains(livingEntity.getWorld().getName())) {
         this.plugin.debugMessage("Grave not created for " + entityName + " because they are not in a valid world", 2);
         return false;
      } else {
         return true;
      }
   }

   private boolean isValidDamageCause(LivingEntity livingEntity, List<String> permissionList, String entityName) {
      if (livingEntity.getLastDamageCause() != null) {
         DamageCause cause = livingEntity.getLastDamageCause().getCause();
         List<String> allowed = this.plugin.getConfigManager().getConfigSection("death.reason", livingEntity, permissionList).getStringList("death.reason");
         boolean ok = allowed.contains("ALL")
            || allowed.contains(cause.name())
            || cause == DamageCause.ENTITY_ATTACK
               && (
                  livingEntity.getKiller() != null
                        && this.plugin.getConfigManager().getConfigSection("death.player", livingEntity, permissionList).getBoolean("death.player")
                     || livingEntity.getKiller() == null
                        && this.plugin.getConfigManager().getConfigSection("death.entity", livingEntity, permissionList).getBoolean("death.entity")
               )
            || cause != DamageCause.ENTITY_ATTACK
               && this.plugin.getConfigManager().getConfigSection("death.environmental", livingEntity, permissionList).getBoolean("death.environmental");
         if (!ok) {
            this.plugin.debugMessage("Grave not created for " + entityName + " because they died to an invalid damage cause", 2);
            return false;
         }
      }

      return true;
   }

   @Deprecated(
      forRemoval = true,
      since = "4.9.10.10"
   )
   private boolean hasValidToken(LivingEntity livingEntity, List<String> permissionList, String entityName, List<ItemStack> drops) {
      if (this.plugin.getVersionManager().hasPersistentData()
         && this.plugin.getConfigManager().getConfigSection("token.enabled", livingEntity, permissionList).getBoolean("token.enabled")) {
         String name = this.plugin.getConfigManager().getConfigSection("token.name", livingEntity).getString("token.name", "basic");
         if (this.plugin.getConfig().isConfigurationSection("settings.token." + name)) {
            ItemStack token = this.plugin.getRecipeManager().getGraveTokenFromPlayer(name, drops);
            if (token == null) {
               this.plugin.getEntityManager().sendMessage("message.no-token", livingEntity, livingEntity.getLocation(), permissionList);
               this.plugin.debugMessage("Grave not created for " + entityName + " because they did not have a grave token", 2);
               return false;
            }

            token.setAmount(token.getAmount() - 1);
         }
      }

      return true;
   }

   private List<ItemStack> getGraveItemStackList(
      EntityDeathEvent event, LivingEntity livingEntity, List<String> permissionList, List<ItemStack> ignoredItemStackList
   ) {
      try {
         boolean exactMode = event.getEntity() instanceof Player player
            && this.plugin.getGraveManager().getStorageMode(this.plugin.getConfigManager().getConfigSection("storage.mode", player).getString("storage.mode"))
               == Grave.StorageMode.EXACT;
         if (exactMode) {
            Player playerx = (Player)event.getEntity();
            List<ItemStack> remainingDrops = new ArrayList<>();

            for (ItemStack drop : event.getDrops()) {
               if (drop != null && drop.getType() != Material.AIR) {
                  remainingDrops.add(drop.clone());
               }
            }

            List<ItemStack> slots = new ArrayList<>(Arrays.asList(playerx.getInventory().getContents()));
            ListIterator<ItemStack> it = slots.listIterator();

            while (it.hasNext()) {
               ItemStack invItem = it.next();
               if (invItem != null && invItem.getType() != Material.AIR) {
                  if (this.plugin.getVersionManager().hasEnchantmentCurse() && invItem.containsEnchantment(Enchantment.BINDING_CURSE)) {
                     ItemStack boundItem = invItem.clone();
                     int slot = it.previousIndex();
                     it.set(null);
                     consumeFromDropsBySimilarity(remainingDrops, invItem);
                     this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        if (player.isOnline()) {
                           player.getInventory().setItem(slot, boundItem);
                           player.updateInventory();
                        }
                     });
                  } else if (this.plugin.getVersionManager().hasEnchantmentCurse() && invItem.containsEnchantment(Enchantment.VANISHING_CURSE)) {
                     it.set(null);
                  } else {
                     UUID graveId = this.plugin.getEntityManager().getGraveUUIDFromItemStack(invItem);
                     if (graveId != null) {
                        if (this.plugin.getConfigManager().getConfigSection("compass.destroy", livingEntity, permissionList).getBoolean("compass.destroy")) {
                           it.set(null);
                        } else if (this.plugin.getConfigManager().getConfigSection("compass.ignore", livingEntity, permissionList).getBoolean("compass.ignore")
                           )
                         {
                           if (ignoredItemStackList != null) {
                              ignoredItemStackList.add(invItem);
                           }

                           it.set(null);
                        }
                     } else if (this.plugin.getGraveManager().shouldIgnoreItemStack(invItem, livingEntity, permissionList)) {
                        if (ignoredItemStackList != null) {
                           ignoredItemStackList.add(invItem);
                        }

                        it.set(null);
                     } else {
                        int consumed = consumeFromDropsBySimilarity(remainingDrops, invItem);
                        if (consumed <= 0) {
                           it.set(null);
                        } else {
                           ItemStack stored = invItem.clone();
                           stored.setAmount(consumed);
                           it.set(stored);
                        }
                     }
                  }
               }
            }

            event.getDrops().clear();

            for (ItemStack left : remainingDrops) {
               if (left != null && left.getType() != Material.AIR && left.getAmount() > 0) {
                  event.getDrops().add(left);
               }
            }

            if (this.plugin.getIntegrationManager().hasBagOfGold()) {
               this.plugin.getIntegrationManager().getBagOfGold().splitPhysicalMoney(livingEntity, permissionList, slots, ignoredItemStackList, true);
            }

            return slots;
         } else {
            List<ItemStack> graveList = new ArrayList<>();
            ListIterator<ItemStack> dropIt = event.getDrops().listIterator();

            label174:
            while (dropIt.hasNext()) {
               ItemStack item = dropIt.next();
               if (item != null && item.getType() != Material.AIR) {
                  if (this.plugin.getVersionManager().hasEnchantmentCurse()) {
                     if (item.containsEnchantment(Enchantment.VANISHING_CURSE)) {
                        dropIt.remove();
                        continue;
                     }

                     if (item.containsEnchantment(Enchantment.BINDING_CURSE) && livingEntity instanceof Player playerx) {
                        ItemStack boundItem = item.clone();
                        int slot = -1;
                        ItemStack[] contents = playerx.getInventory().getContents();
                        int i = 0;

                        while (true) {
                           if (i < contents.length) {
                              ItemStack slotItem = contents[i];
                              if (slotItem == null || slotItem.getType() == Material.AIR || !slotItem.isSimilar(item)) {
                                 i++;
                                 continue;
                              }

                              slot = i;
                           }

                           dropIt.remove();
                           if (slot >= 0) {
                              i = slot;
                              this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                                 player.getInventory().setItem(i, boundItem);
                                 player.updateInventory();
                              });
                           }
                           continue label174;
                        }
                     }
                  }

                  UUID graveId = this.plugin.getEntityManager().getGraveUUIDFromItemStack(item);
                  if (graveId != null) {
                     if (this.plugin.getConfigManager().getConfigSection("compass.destroy", livingEntity, permissionList).getBoolean("compass.destroy")) {
                        dropIt.remove();
                     } else if (!this.plugin.getConfigManager().getConfigSection("compass.ignore", livingEntity, permissionList).getBoolean("compass.ignore")) {
                        graveList.add(item);
                        dropIt.remove();
                     } else {
                        if (ignoredItemStackList != null) {
                           ignoredItemStackList.add(item);
                        }

                        dropIt.remove();
                     }
                  } else if (!this.plugin.getGraveManager().shouldIgnoreItemStack(item, livingEntity, permissionList)) {
                     graveList.add(item);
                     dropIt.remove();
                  } else {
                     if (ignoredItemStackList != null) {
                        ignoredItemStackList.add(item);
                     }

                     dropIt.remove();
                  }
               }
            }

            if (this.plugin.getIntegrationManager().hasBagOfGold()) {
               this.plugin.getIntegrationManager().getBagOfGold().splitPhysicalMoney(livingEntity, permissionList, graveList, ignoredItemStackList, false);
            }

            return graveList;
         }
      } catch (ArrayIndexOutOfBoundsException var15) {
         return new ArrayList<>();
      }
   }

   private static int consumeFromDropsBySimilarity(List<ItemStack> remainingDrops, ItemStack target) {
      int need = target.getAmount();
      int consumed = 0;
      ListIterator<ItemStack> it = remainingDrops.listIterator();

      while (it.hasNext() && need > 0) {
         ItemStack drop = it.next();
         if (drop == null || drop.getType() == Material.AIR) {
            it.remove();
         } else if (target.isSimilar(drop)) {
            int take = Math.min(need, drop.getAmount());
            consumed += take;
            need -= take;
            int left = drop.getAmount() - take;
            if (left <= 0) {
               it.remove();
            } else {
               drop.setAmount(left);
               it.set(drop);
            }
         }
      }

      return consumed;
   }

   private void createGrave(
      EntityDeathEvent event,
      LivingEntity livingEntity,
      String entityName,
      List<String> permissionList,
      List<ItemStack> removedItemStackList,
      List<ItemStack> graveItemStackList,
      List<ItemStack> ignoredItemStackList,
      Location location,
      PlayerDeathEvent pde,
      Player player
   ) {
      List<Block> ignoredBlockList = new ArrayList<>();
      Grave grave = new Grave(UUID.randomUUID());
      GraveCreateEvent modern = new GraveCreateEvent(livingEntity, grave, graveItemStackList, ignoredItemStackList, ignoredBlockList);
      if (ignoredItemStackList != null && !ignoredItemStackList.isEmpty()) {
         modern.setIgnoredItems(ignoredItemStackList);
      }

      modern.setIgnoredBlocks(ignoredBlockList);
      this.setupGrave(modern, grave, livingEntity, entityName, permissionList);
      this.setGraveExperience(modern, grave, event, livingEntity, pde);
      this.setupGraveKiller(modern, grave, livingEntity);
      this.setupGraveProtection(modern, livingEntity, grave);
      if (this.plugin.getConfigManager().getConfigSection("placement.safe-location", grave).getBoolean("placement.safe-location", true)) {
         Location safeLocation = this.plugin.getLocationManager().getSafeGraveLocation(livingEntity, location, grave);
         Location target = safeLocation != null ? safeLocation : location;
         event.setDroppedExp(0);
         if (this.plugin.getLocationManager().hasCachedGraveAt(target)) {
            Location newLoc = this.plugin.getLocationManager().getNewLocationIfCachedGraveExists(livingEntity, target, grave);
            if (newLoc != null) {
               target = newLoc;
            }
         }

         modern.setDeathLocation(LocationUtil.roundLocation(target));
      } else {
         event.setDroppedExp(0);
         Location target = location;
         if (this.plugin.getLocationManager().hasCachedGraveAt(location)) {
            Location newLoc = this.plugin.getLocationManager().getNewLocationIfCachedGraveExists(livingEntity, location, grave);
            if (newLoc != null) {
               target = newLoc;
            }
         }

         modern.setDeathLocation(LocationUtil.roundLocation(target));
      }

      GravePreCreateEvent pre = new GravePreCreateEvent(livingEntity, grave, graveItemStackList, ignoredItemStackList, ignoredBlockList);
      pre.setGraveUUID(grave.getUUID());
      pre.setDeathLocation(modern.getLocationDeath());
      pre.setOwnerType(modern.getOwnerType());
      pre.setOwnerName(modern.getOwnerName());
      pre.setOwnerNameDisplay(modern.getOwnerNameDisplay());
      pre.setOwnerUUID(modern.getOwnerUUID());
      pre.setPermissionList(modern.getPermissionList());
      pre.setYaw(modern.getYaw());
      pre.setPitch(modern.getPitch());
      pre.setTimeAlive(modern.getTimeAlive());
      pre.setOwnerTexture(modern.getOwnerTexture());
      pre.setOwnerTextureSignature(modern.getOwnerTextureSignature());
      pre.setExperience(modern.getExperience());
      pre.setKillerType(modern.getKillerType());
      pre.setKillerName(modern.getKillerName());
      pre.setKillerNameDisplay(modern.getKillerNameDisplay());
      pre.setKillerUUID(modern.getKillerUUID());
      pre.setProtection(modern.getProtection());
      pre.setTimeProtection(modern.getTimeProtection());
      pre.setDeathCause(modern.getDeathCause());
      this.plugin.getServer().getPluginManager().callEvent(pre);
      if (!pre.isAddon()) {
         if (pre.isCancelled()) {
            List<ItemStack> toReturn = new ArrayList<>();
            Set<ItemStack> identitySet = Collections.newSetFromMap(new IdentityHashMap<>());
            if (removedItemStackList != null) {
               for (ItemStack item : removedItemStackList) {
                  if (item != null && item.getType() != Material.AIR && identitySet.add(item)) {
                     toReturn.add(item);
                  }
               }
            }

            if (graveItemStackList != null) {
               for (ItemStack itemx : graveItemStackList) {
                  if (itemx != null && itemx.getType() != Material.AIR && identitySet.add(itemx)) {
                     toReturn.add(itemx);
                  }
               }
            }

            if (ignoredItemStackList != null) {
               for (ItemStack itemxx : ignoredItemStackList) {
                  if (itemxx != null && itemxx.getType() != Material.AIR && identitySet.add(itemxx)) {
                     toReturn.add(itemxx);
                  }
               }
            }

            if (!toReturn.isEmpty()) {
               event.getDrops().addAll(toReturn);
            }
         } else {
            UUID preUUID = pre.getGraveUUID();
            if (preUUID != null && !preUUID.equals(grave.getUUID())) {
               grave = new Grave(preUUID);
               modern = new GraveCreateEvent(livingEntity, grave, graveItemStackList, ignoredItemStackList, ignoredBlockList);
               if (ignoredItemStackList != null && !ignoredItemStackList.isEmpty()) {
                  modern.setIgnoredItems(ignoredItemStackList);
               }

               modern.setIgnoredBlocks(ignoredBlockList);
            }

            modern.setDeathLocation(pre.getLocationDeath());
            modern.setOwnerType(pre.getOwnerType());
            modern.setOwnerName(pre.getOwnerName());
            modern.setOwnerNameDisplay(pre.getOwnerNameDisplay());
            modern.setOwnerUUID(pre.getOwnerUUID());
            modern.setPermissionList(pre.getPermissionList().isEmpty() ? null : new ArrayList<>(pre.getPermissionList()));
            modern.setYaw(pre.getYaw());
            modern.setPitch(pre.getPitch());
            modern.setTimeAlive(pre.getTimeAlive());
            modern.setOwnerTexture(pre.getOwnerTexture());
            modern.setOwnerTextureSignature(pre.getOwnerTextureSignature());
            modern.setExperience(pre.getExperience());
            modern.setKillerType(pre.getKillerType());
            modern.setKillerName(pre.getKillerName());
            modern.setKillerNameDisplay(pre.getKillerNameDisplay());
            modern.setKillerUUID(pre.getKillerUUID());
            modern.setProtection(pre.getProtection());
            modern.setTimeProtection(pre.getTimeProtection());
            this.plugin.getServer().getPluginManager().callEvent(modern);
            com.ranull.graves.event.GraveCreateEvent legacy = new com.ranull.graves.event.GraveCreateEvent(
               livingEntity, grave, ignoredItemStackList, ignoredBlockList
            );
            this.plugin.getServer().getPluginManager().callEvent(legacy);
            if (graveItemStackList != null) {
               graveItemStackList.clear();
               graveItemStackList.addAll(modern.getGraveItemStackList());
            } else if (!modern.getGraveItemStackList().isEmpty()) {
               graveItemStackList = new ArrayList<>(modern.getGraveItemStackList());
            }

            List<ItemStack> effectiveIgnoredItems = new ArrayList<>();
            Set<ItemStack> itemsIdentity = Collections.newSetFromMap(new IdentityHashMap<>());
            if (ignoredItemStackList != null) {
               for (ItemStack itemxxx : ignoredItemStackList) {
                  if (itemxxx != null && itemsIdentity.add(itemxxx)) {
                     effectiveIgnoredItems.add(itemxxx);
                  }
               }
            }

            for (ItemStack itemxxxx : modern.getIgnoredItems()) {
               if (itemxxxx != null && itemsIdentity.add(itemxxxx)) {
                  effectiveIgnoredItems.add(itemxxxx);
               }
            }

            for (ItemStack itemxxxxx : legacy.getIgnoredItems()) {
               if (itemxxxxx != null && itemsIdentity.add(itemxxxxx)) {
                  effectiveIgnoredItems.add(itemxxxxx);
               }
            }

            List<Block> effectiveIgnoredBlocks = new ArrayList<>();
            Set<Block> blocksIdentity = Collections.newSetFromMap(new IdentityHashMap<>());

            for (Block block : ignoredBlockList) {
               if (block != null && blocksIdentity.add(block)) {
                  effectiveIgnoredBlocks.add(block);
               }
            }

            for (Block blockx : modern.getIgnoredBlocks()) {
               if (blockx != null && blocksIdentity.add(blockx)) {
                  effectiveIgnoredBlocks.add(blockx);
               }
            }

            for (Block blockxx : legacy.getIgnoredBlocks()) {
               if (blockxx != null && blocksIdentity.add(blockxx)) {
                  effectiveIgnoredBlocks.add(blockxx);
               }
            }

            boolean cancelled = pre.isCancelled() || modern.isCancelled() || legacy.isCancelled();
            boolean addon = pre.isAddon() || modern.isAddon() || legacy.isAddon();
            if (addon) {
               this.plugin
                  .debugMessage("GraveCreateEvent is handled by addon. Developers should handle it here. If not, this will do absolutely nothing at all.", 1);
            } else if (!cancelled) {
               grave.setOwnerType(modern.getOwnerType());
               grave.setOwnerName(modern.getOwnerName());
               grave.setOwnerNameDisplay(modern.getOwnerNameDisplay());
               grave.setOwnerUUID(modern.getOwnerUUID());
               grave.setPermissionList(modern.getPermissionList());
               grave.setYaw(modern.getYaw());
               grave.setPitch(modern.getPitch());
               grave.setTimeAlive(modern.getTimeAlive());
               grave.setOwnerTexture(modern.getOwnerTexture());
               grave.setOwnerTextureSignature(modern.getOwnerTextureSignature());
               grave.setExperience(modern.getExperience());
               grave.setKillerName(modern.getKillerName());
               grave.setKillerNameDisplay(modern.getKillerNameDisplay());
               grave.setKillerUUID(modern.getKillerUUID());
               grave.setKillerType(modern.getKillerType());
               grave.setProtection(modern.getProtection());
               grave.setTimeProtection(modern.getTimeProtection());
               grave.setLocationDeath(modern.getLocationDeath());
               Location deathLoc = Objects.requireNonNull(modern.getLocationDeath());
               World world = deathLoc.getWorld();
               boolean allowNetherRoof = this.plugin.getConfigManager().getConfigSection("placement.nether-roof", grave).getBoolean("placement.nether-roof");
               if ((world == null || !(deathLoc.getY() < world.getMinHeight()))
                  && (world == null || !(deathLoc.getY() > world.getMaxHeight()))
                  && (
                     allowNetherRoof
                        || world == null
                        || world.getEnvironment() != Environment.NETHER
                        || !this.plugin.getSafeLocationManager().isAboveNetherRoof(grave.getLocationDeath(), grave)
                  )) {
                  List<String> effectivePerms = (List<String>)(modern.getPermissionList().isEmpty()
                     ? permissionList
                     : new ArrayList<>(modern.getPermissionList()));
                  Location placedLocation = this.placeGrave(
                     event, grave, graveItemStackList, removedItemStackList, deathLoc, livingEntity, effectivePerms, player
                  );
                  this.plugin.getServer().getPluginManager().callEvent(new GravePostCreateEvent(livingEntity, grave, placedLocation));
                  if (!effectiveIgnoredItems.isEmpty() || !effectiveIgnoredBlocks.isEmpty()) {
                     this.dropIgnored(livingEntity, deathLoc, event, effectiveIgnoredItems, effectiveIgnoredBlocks);
                  }
               } else {
                  this.handleFailedGravePlacement(event, grave, deathLoc, livingEntity, removedItemStackList, graveItemStackList);
               }
            } else {
               List<ItemStack> toReturnx = new ArrayList<>();
               Set<ItemStack> identitySetx = Collections.newSetFromMap(new IdentityHashMap<>());
               if (graveItemStackList != null) {
                  for (ItemStack itemxxxxxx : graveItemStackList) {
                     if (itemxxxxxx != null && itemxxxxxx.getType() != Material.AIR && identitySetx.add(itemxxxxxx)) {
                        toReturnx.add(itemxxxxxx);
                     }
                  }
               }

               for (ItemStack itemxxxxxxx : effectiveIgnoredItems) {
                  if (itemxxxxxxx != null && itemxxxxxxx.getType() != Material.AIR && identitySetx.add(itemxxxxxxx)) {
                     toReturnx.add(itemxxxxxxx);
                  }
               }

               if (!toReturnx.isEmpty()) {
                  event.getDrops().addAll(toReturnx);
               }

               if (!effectiveIgnoredBlocks.isEmpty()) {
                  Location dropLoc = location != null ? location : livingEntity.getLocation();
                  World world = dropLoc.getWorld();
                  if (world != null) {
                     for (Block blockxxx : effectiveIgnoredBlocks) {
                        if (blockxxx != null) {
                           Location blockLoc = blockxxx.getLocation();

                           for (ItemStack drop : blockxxx.getDrops()) {
                              if (drop != null && drop.getType() != Material.AIR) {
                                 blockxxx.getWorld().dropItemNaturally(blockLoc, drop);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void setupGrave(GraveCreateEvent graveCreateEvent, Grave grave, LivingEntity livingEntity, String entityName, List<String> permissionList) {
      graveCreateEvent.setOwnerType(livingEntity.getType());
      graveCreateEvent.setOwnerName(entityName);
      graveCreateEvent.setOwnerNameDisplay(livingEntity instanceof Player p ? p.getDisplayName() : entityName);
      graveCreateEvent.setOwnerUUID(livingEntity.getUniqueId());
      graveCreateEvent.setPermissionList(permissionList);
      graveCreateEvent.setYaw(livingEntity.getLocation().getYaw());
      graveCreateEvent.setPitch(livingEntity.getLocation().getPitch());
      graveCreateEvent.setTimeAlive(this.plugin.getConfigManager().getConfigSection("grave.time", grave).getInt("grave.time") * 1000L);
      if (!this.plugin.getVersionManager().is_v1_7()) {
         if (this.plugin.getVersionManager().isPost1_21_9()) {
            graveCreateEvent.setOwnerTexture(SkinTextureUtil_post_1_21_9.getTexture(livingEntity));
         } else {
            graveCreateEvent.setOwnerTexture(SkinTextureUtil.getTexture(livingEntity));
         }

         graveCreateEvent.setOwnerTextureSignature(SkinSignatureUtil.getSignature(livingEntity));
      }

      EntityDamageEvent last = livingEntity.getLastDamageCause();
      DamageCause cause = last != null ? last.getCause() : null;
      grave.setDeathCause(cause != null ? cause.name() : null);
   }

   private void setGraveExperience(GraveCreateEvent graveCreateEvent, Grave grave, EntityDeathEvent event, LivingEntity livingEntity, PlayerDeathEvent pde) {
      float pct = (float)this.plugin.getConfigManager().getConfigSection("experience.store", grave).getDouble("experience.store");
      boolean storeExp = this.plugin.getConfigManager().getConfigSection("experience.store-in-grave", grave).getBoolean("experience.store-in-grave", true);
      this.plugin.debugMessage("Experience Percentage for " + grave.getUUID() + ": " + pct, 2);
      int vanillaDrop = event.getDroppedExp();
      event.setDroppedExp(0);
      if (livingEntity instanceof Player p) {
         Plugin essentials = this.plugin.getServer().getPluginManager().getPlugin("Essentials");
         if (!storeExp) {
            if (essentials != null && essentials.isEnabled() && this.plugin.getPermissionManager().hasGrantedPermission("essentials.keepxp", p.getPlayer())) {
               graveCreateEvent.setExperience(0);

               String playerDisplay;
               try {
                  playerDisplay = p.getPlayer().getDisplayName();
               } catch (NullPointerException var21) {
                  try {
                     playerDisplay = p.getPlayer().getName();
                  } catch (NullPointerException var20) {
                     playerDisplay = "Unknown";
                  }
               }

               pde.setNewExp(event.getDroppedExp());
               graveCreateEvent.setExperience(0);
               pde.setKeepLevel(true);
               this.plugin
                  .debugMessage("Set Dropped Experience not applied to " + grave.getUUID() + " because " + playerDisplay + " has essentials.keepxp.", 2);
            } else if (pde.getKeepLevel() && !this.plugin.getPermissionManager().hasGrantedPermission("graves.keepinventory.bypass", p.getPlayer())) {
               graveCreateEvent.setExperience(0);

               String playerDisplay;
               try {
                  playerDisplay = p.getPlayer().getDisplayName();
               } catch (NullPointerException var19) {
                  try {
                     playerDisplay = p.getPlayer().getName();
                  } catch (NullPointerException var18) {
                     playerDisplay = "Unknown";
                  }
               }

               pde.setNewExp(event.getDroppedExp());
               graveCreateEvent.setExperience(0);
               pde.setKeepLevel(true);
               this.plugin.debugMessage("Set Dropped Experience not applied to " + grave.getUUID() + " because " + playerDisplay + " has keep experience.", 2);
            } else if (pct >= 0.0F && this.plugin.getPermissionManager().hasGrantedPermission("graves.experience", p.getPlayer())) {
               int total = ExperienceUtil.getPlayerExperience(p);
               int stored = ExperienceUtil.getDropPercent(total, pct);
               graveCreateEvent.setExperience(0);
               pde.setDroppedExp(stored);
               this.plugin.debugMessage("Set Dropped Experience for player grave " + grave.getUUID() + ": " + stored, 1);
               pde.setKeepLevel(false);
            } else {
               graveCreateEvent.setExperience(0);
               pde.setDroppedExp(vanillaDrop);
               pde.setKeepLevel(false);
               this.plugin.debugMessage("Set Dropped Experience for player grave " + grave.getUUID() + ": " + vanillaDrop, 1);
            }
         } else if (essentials != null && essentials.isEnabled() && this.plugin.getPermissionManager().hasGrantedPermission("essentials.keepxp", p.getPlayer())
            )
          {
            graveCreateEvent.setExperience(0);

            String playerDisplay;
            try {
               playerDisplay = p.getPlayer().getDisplayName();
            } catch (NullPointerException var17) {
               try {
                  playerDisplay = p.getPlayer().getName();
               } catch (NullPointerException var16) {
                  playerDisplay = "Unknown";
               }
            }

            pde.setNewExp(event.getDroppedExp());
            graveCreateEvent.setExperience(0);
            pde.setKeepLevel(true);
            this.plugin.debugMessage("Set Grave Experience not applied to " + grave.getUUID() + " because " + playerDisplay + " has essentials.keepxp.", 2);
         } else if (pde.getKeepLevel() && !this.plugin.getPermissionManager().hasGrantedPermission("graves.keepinventory.bypass", p.getPlayer())) {
            graveCreateEvent.setExperience(0);

            String playerDisplay;
            try {
               playerDisplay = p.getPlayer().getDisplayName();
            } catch (NullPointerException var15) {
               try {
                  playerDisplay = p.getPlayer().getName();
               } catch (NullPointerException var14) {
                  playerDisplay = "Unknown";
               }
            }

            pde.setNewExp(event.getDroppedExp());
            graveCreateEvent.setExperience(0);
            pde.setKeepLevel(true);
            this.plugin.debugMessage("Set Grave Experience not applied to " + grave.getUUID() + " because " + playerDisplay + " has keep experience.", 2);
         } else if (pct >= 0.0F && this.plugin.getPermissionManager().hasGrantedPermission("graves.experience", p.getPlayer())) {
            int total = ExperienceUtil.getPlayerExperience(p);
            int stored = ExperienceUtil.getDropPercent(total, pct);
            graveCreateEvent.setExperience(stored);
            this.plugin.debugMessage("Set Grave Experience for player grave " + grave.getUUID() + ": " + stored, 1);
            pde.setKeepLevel(false);
         } else {
            graveCreateEvent.setExperience(vanillaDrop);
            pde.setKeepLevel(false);
            this.plugin.debugMessage("Set Grave Experience for player grave " + grave.getUUID() + ": " + vanillaDrop, 1);
         }
      } else if (!storeExp) {
         if (pct >= 0.0F) {
            int stored = ExperienceUtil.getDropPercent(vanillaDrop, pct);
            graveCreateEvent.setExperience(0);
            pde.setDroppedExp(stored);
            this.plugin.debugMessage("Set Dropped Experience for non player grave " + grave.getUUID() + ": " + stored, 1);
         } else {
            graveCreateEvent.setExperience(0);
            pde.setDroppedExp(vanillaDrop);
            this.plugin.debugMessage("Set Dropped Experience for default grave " + grave.getUUID() + ": " + vanillaDrop, 1);
         }
      } else if (pct >= 0.0F) {
         int stored = ExperienceUtil.getDropPercent(vanillaDrop, pct);
         graveCreateEvent.setExperience(stored);
         this.plugin.debugMessage("Set Grave Experience for non player grave " + grave.getUUID() + ": " + stored, 1);
      } else {
         graveCreateEvent.setExperience(vanillaDrop);
         this.plugin.debugMessage("Set Grave Experience for default grave " + grave.getUUID() + ": " + vanillaDrop, 1);
      }
   }

   private void setupGraveKiller(GraveCreateEvent graveCreateEvent, Grave grave, LivingEntity livingEntity) {
      if (livingEntity.getKiller() != null) {
         graveCreateEvent.setKillerType(EntityType.PLAYER);
         graveCreateEvent.setKillerName(livingEntity.getKiller().getName());
         graveCreateEvent.setKillerNameDisplay(livingEntity.getKiller().getDisplayName());
         graveCreateEvent.setKillerUUID(livingEntity.getKiller().getUniqueId());
      } else if (livingEntity.getLastDamageCause() != null) {
         EntityDamageEvent e = livingEntity.getLastDamageCause();
         if (e.getCause() == DamageCause.ENTITY_ATTACK && e instanceof EntityDamageByEntityEvent by) {
            graveCreateEvent.setKillerUUID(by.getDamager().getUniqueId());
            graveCreateEvent.setKillerType(by.getDamager().getType());
            graveCreateEvent.setKillerName(this.plugin.getEntityManager().getEntityName(by.getDamager()));
         } else {
            graveCreateEvent.setKillerUUID(null);
            graveCreateEvent.setKillerType(null);
            graveCreateEvent.setKillerName(this.plugin.getGraveManager().getDamageReason(grave));
         }

         graveCreateEvent.setKillerNameDisplay(graveCreateEvent.getKillerName());
      }
   }

   private void setupGraveProtection(GraveCreateEvent graveCreateEvent, LivingEntity livingEntity, Grave grave) {
      if (this.plugin.getConfigManager().getConfigSection("protection.enabled", grave).getBoolean("protection.enabled")) {
         GraveProtectionCreateEvent modern = new GraveProtectionCreateEvent(livingEntity, grave);
         this.plugin.getServer().getPluginManager().callEvent(modern);
         com.ranull.graves.event.GraveProtectionCreateEvent legacy = new com.ranull.graves.event.GraveProtectionCreateEvent(livingEntity, grave);
         this.plugin.getServer().getPluginManager().callEvent(legacy);
         if (!modern.isCancelled() && !modern.isAddon() && !legacy.isCancelled() && !legacy.isAddon()) {
            graveCreateEvent.setProtection(true);
            graveCreateEvent.setTimeProtection(this.plugin.getConfigManager().getConfigSection("protection.time", grave).getInt("protection.time") * 1000L);
         }
      }
   }

   private Location placeGrave(
      EntityDeathEvent event,
      Grave grave,
      List<ItemStack> graveItemStackList,
      List<ItemStack> removedItemStackList,
      Location location,
      LivingEntity livingEntity,
      List<String> permissionList,
      Player player
   ) {
      Map<Location, BlockData.BlockType> locationMap = new HashMap<>();
      grave.getLocationDeath().setYaw(grave.getYaw());
      grave.getLocationDeath().setPitch(grave.getPitch());
      locationMap.put(grave.getLocationDeath(), BlockData.BlockType.DEATH);
      this.setupObituary(grave, graveItemStackList, livingEntity, location);
      this.setupSkull(grave, graveItemStackList, livingEntity, location);
      grave.setInventory(this.plugin.getGraveManager().getGraveInventory(grave, livingEntity, graveItemStackList, removedItemStackList, permissionList));
      grave.setEquipmentMap(
         (Map<EquipmentSlot, ItemStack>)(!this.plugin.getVersionManager().is_v1_7()
            ? this.plugin.getEntityManager().getEquipmentMap(livingEntity, grave)
            : new HashMap<>())
      );
      if (!locationMap.isEmpty()) {
         this.notifyGraveCreation(event, grave, locationMap, livingEntity, permissionList, player, location, removedItemStackList, graveItemStackList);
         return location;
      } else {
         this.handleFailedGravePlacement(event, grave, location, livingEntity, removedItemStackList, graveItemStackList);
         return null;
      }
   }

   private void setupObituary(Grave grave, List<ItemStack> graveItemStackList, LivingEntity livingEntity, Location location) {
      if (this.plugin.getConfigManager().getConfigSection("obituary.enabled", grave).getBoolean("obituary.enabled")) {
         double pct = this.plugin.getConfigManager().getConfigSection("obituary.percent", grave).getDouble("obituary.percent");
         boolean drop = this.plugin.getConfigManager().getConfigSection("obituary.drop", grave).getBoolean("obituary.drop");
         if (Math.random() <= pct) {
            GraveObituaryAddEvent modern = new GraveObituaryAddEvent(grave, location, livingEntity);
            this.plugin.getServer().getPluginManager().callEvent(modern);
            com.ranull.graves.event.GraveObituaryAddEvent legacy = new com.ranull.graves.event.GraveObituaryAddEvent(grave, location, livingEntity);
            this.plugin.getServer().getPluginManager().callEvent(legacy);
            if (!modern.isCancelled() && !modern.isAddon() && !legacy.isCancelled() && !legacy.isAddon()) {
               if (drop) {
                  if (location.getWorld() != null) {
                     location.getWorld().dropItemNaturally(location, this.plugin.getItemStackManager().getGraveObituary(grave));
                     this.plugin
                        .debugMessage(
                           "Obituary dropped at location x: " + location.getBlockX() + " y: " + location.getBlockY() + " z: " + location.getBlockZ() + ".", 2
                        );
                  } else {
                     Block b = location.getBlock();
                     this.plugin
                        .debugMessage(
                           "World not found. Obituary added to "
                              + grave.getOwnerName()
                              + "'s Grave at location x: "
                              + b.getX()
                              + " y: "
                              + b.getY()
                              + " z: "
                              + b.getZ()
                              + ".",
                           2
                        );
                     graveItemStackList.add(this.plugin.getItemStackManager().getGraveObituary(grave));
                  }
               } else {
                  Block b = location.getBlock();
                  this.plugin
                     .debugMessage(
                        "Obituary added to " + grave.getOwnerName() + "'s Grave at location x: " + b.getX() + " y: " + b.getY() + " z: " + b.getZ() + ".", 2
                     );
                  graveItemStackList.add(this.plugin.getItemStackManager().getGraveObituary(grave));
               }
            }
         }
      }
   }

   private void setupSkull(Grave grave, List<ItemStack> graveItemStackList, LivingEntity livingEntity, Location location) {
      if (this.plugin.getConfigManager().getConfigSection("head.enabled", grave).getBoolean("head.enabled")
         && Math.random() < this.plugin.getConfigManager().getConfigSection("head.percent", grave).getDouble("head.percent")
         && grave.getOwnerTexture() != null
         && grave.getOwnerTextureSignature() != null) {
         boolean drop = this.plugin.getConfigManager().getConfigSection("head.drop", grave).getBoolean("head.drop");
         GravePlayerHeadDropEvent modern = new GravePlayerHeadDropEvent(grave, location, livingEntity);
         this.plugin.getServer().getPluginManager().callEvent(modern);
         com.ranull.graves.event.GravePlayerHeadDropEvent legacy = new com.ranull.graves.event.GravePlayerHeadDropEvent(grave, location, livingEntity);
         this.plugin.getServer().getPluginManager().callEvent(legacy);
         if (!modern.isCancelled() && !modern.isAddon() && !legacy.isCancelled() && !legacy.isAddon()) {
            if (drop) {
               if (location.getWorld() != null) {
                  ItemStack headItem = this.plugin.getItemStackManager().getGraveHead(grave);
                  location.getWorld().dropItemNaturally(location, headItem);
                  this.plugin
                     .debugMessage(
                        "Player Head dropped at location x: " + location.getBlockX() + ", y: " + location.getBlockY() + ", z: " + location.getBlockZ() + ".", 2
                     );
               } else {
                  Location graveLoc = grave.getLocationDeath();
                  this.plugin
                     .debugMessage(
                        "World not found. Player Head added to "
                           + livingEntity.getName()
                           + "'s grave at location x: "
                           + graveLoc.getBlockX()
                           + ", y: "
                           + graveLoc.getBlockY()
                           + ", z: "
                           + graveLoc.getBlockZ()
                           + ".",
                        2
                     );
                  graveItemStackList.add(this.plugin.getItemStackManager().getGraveHead(grave));
               }
            } else {
               Location graveLoc = grave.getLocationDeath();
               this.plugin
                  .debugMessage(
                     "Player Head added to "
                        + livingEntity.getName()
                        + "'s grave at location x: "
                        + graveLoc.getBlockX()
                        + ", y: "
                        + graveLoc.getBlockY()
                        + ", z: "
                        + graveLoc.getBlockZ()
                        + ".",
                     2
                  );
               graveItemStackList.add(this.plugin.getItemStackManager().getGraveHead(grave));
            }
         }
      }
   }

   private void notifyGraveCreation(
      EntityDeathEvent event,
      Grave grave,
      Map<Location, BlockData.BlockType> locationMap,
      LivingEntity livingEntity,
      List<String> permissionList,
      Player player,
      Location fallbackLocation,
      List<ItemStack> removedItemStackList,
      List<ItemStack> graveItemStackList
   ) {
      this.plugin.getEntityManager().sendMessage("message.death", livingEntity, grave.getLocationDeath(), grave);
      this.plugin.getEntityManager().runCommands("event.command.create", livingEntity, grave.getLocationDeath(), grave);
      this.plugin.getDataManager().addGrave(grave);
      if (player != null && this.plugin.getConfigManager().getConfigSection("grave.smite-death-location", grave).getBoolean("grave.smite-death-location", true)
         )
       {
         World world = player.getWorld();
         if (world != null) {
            switch (world.getEnvironment()) {
               case NORMAL:
                  if (this.plugin
                     .getConfigManager()
                     .getConfigSection("grave.actually-smite-death-location", grave)
                     .getBoolean("grave.actually-smite-death-location", false)) {
                     world.strikeLightning(player.getLocation());
                  } else {
                     world.strikeLightningEffect(player.getLocation());
                  }
                  break;
               case CUSTOM:
                  Boolean weatherCycle = (Boolean)world.getGameRuleValue(Objects.requireNonNull(GameRule.getByName("do_weather_cycle")));
                  if (Boolean.TRUE.equals(weatherCycle)) {
                     if (this.plugin
                        .getConfigManager()
                        .getConfigSection("grave.actually-smite-death-location", grave)
                        .getBoolean("grave.actually-smite-death-location", false)) {
                        world.strikeLightning(player.getLocation());
                     } else {
                        world.strikeLightningEffect(player.getLocation());
                     }
                  }
            }
         }
      }

      if (player != null
         && this.plugin.getConfigManager().getConfigSection("noteblockapi.enabled", grave).getBoolean("noteblockapi.enabled")
         && this.plugin.getIntegrationManager().hasNoteBlockAPI()) {
         String cause = event.getEntity().getLastDamageCause() != null ? event.getEntity().getLastDamageCause().getCause().name() : "UNKNOWN";
         String nbsSound = null;

         for (String s : this.plugin.getConfigManager().getConfigSection("noteblockapi.death-causes", grave).getStringList("noteblockapi.death-causes")) {
            String[] parts = s.split(": ");
            if (parts.length == 2 && parts[0].equalsIgnoreCase(cause)) {
               nbsSound = parts[1].trim();
               break;
            }
         }

         if (nbsSound == null) {
            nbsSound = this.plugin.getConfigManager().getConfigSection("noteblockapi.nbs-sound", grave).getString("noteblockapi.nbs-sound");
         }

         if (this.plugin.getConfigManager().getConfigSection("noteblockapi.play-locally", grave).getBoolean("noteblockapi.play-locally")) {
            this.plugin.getIntegrationManager().getNoteBlockAPI().playSongForPlayer(player, nbsSound);
         } else {
            this.plugin.getIntegrationManager().getNoteBlockAPI().playSongForAllPlayers(nbsSound);
         }
      } else if (player != null) {
         this.plugin.getEntityManager().playPlayerSound("sound.grave-create", player, grave);
      }

      if (this.plugin.getIntegrationManager().hasMultiPaper()) {
         this.plugin.getIntegrationManager().getMultiPaper().notifyGraveCreation(grave);
      }

      boolean placed = this.placeGraveBlocks(grave, locationMap, livingEntity);
      if (!placed) {
         this.handleFailedGravePlacement(event, grave, fallbackLocation, livingEntity, removedItemStackList, graveItemStackList);
      }
   }

   private boolean placeGraveBlocks(Grave grave, Map<Location, BlockData.BlockType> locationMap, LivingEntity livingEntity) {
      boolean placed = false;

      for (Entry<Location, BlockData.BlockType> entry : locationMap.entrySet()) {
         Location base = entry.getKey().clone();
         int dx = 0;
         int dy = 0;
         int dz = 0;
         if (entry.getValue() == BlockData.BlockType.NORMAL) {
            dx = this.plugin.getConfigManager().getConfigSection("placement.offset.x", grave).getInt("placement.offset.x");
            dy = this.plugin.getConfigManager().getConfigSection("placement.offset.y", grave).getInt("placement.offset.y");
            dz = this.plugin.getConfigManager().getConfigSection("placement.offset.z", grave).getInt("placement.offset.z");
         }

         Location loc = base.add(dx, dy, dz);
         GraveBlockPlaceEvent modern = new GraveBlockPlaceEvent(grave, loc, entry.getValue(), entry.getKey().getBlock(), livingEntity);
         this.plugin.getServer().getPluginManager().callEvent(modern);
         com.ranull.graves.event.GraveBlockPlaceEvent legacy = new com.ranull.graves.event.GraveBlockPlaceEvent(
            grave, loc, entry.getValue(), entry.getKey().getBlock(), livingEntity
         );
         this.plugin.getServer().getPluginManager().callEvent(legacy);
         if (!modern.isCancelled() && !modern.isAddon() && !legacy.isCancelled() && !legacy.isAddon()) {
            Location effectiveLoc = modern.hasLocation() ? modern.getLocation() : (legacy.hasLocation() ? legacy.getLocation() : loc);
            this.plugin.getGraveManager().placeGrave(effectiveLoc, grave);
            this.plugin.getEntityManager().sendMessage("message.block", livingEntity, effectiveLoc, grave);
            this.plugin.getEntityManager().runCommands("event.command.block", livingEntity, effectiveLoc, grave);
            placed = true;
         }
      }

      return placed;
   }

   private void handleFailedGravePlacement(
      EntityDeathEvent event,
      Grave grave,
      Location location,
      LivingEntity livingEntity,
      List<ItemStack> removedItemStackList,
      List<ItemStack> graveItemStackList
   ) {
      if (event instanceof PlayerDeathEvent pde
         && this.plugin.getConfigManager().getConfigSection("placement.failure-keep-inventory", grave).getBoolean("placement.failure-keep-inventory")) {
         try {
            pde.setKeepLevel(true);
            pde.setKeepInventory(true);
            this.plugin.getEntityManager().sendMessage("message.failure-keep-inventory", livingEntity, location, grave);
         } catch (NoSuchMethodError var9) {
         }
      } else {
         this.plugin.getEntityManager().sendMessage("message.failure", livingEntity, location, grave);
         if (removedItemStackList != null) {
            event.getDrops().addAll(removedItemStackList);
         }

         if (graveItemStackList != null) {
            event.getDrops().addAll(graveItemStackList);
         }
      }
   }

   private void dropIgnored(
      LivingEntity livingEntity, Location fallbackLocation, EntityDeathEvent event, List<ItemStack> ignoredItems, List<Block> ignoredBlocks
   ) {
      if (ignoredItems != null && !ignoredItems.isEmpty() || ignoredBlocks != null && !ignoredBlocks.isEmpty()) {
         Location dropLoc = fallbackLocation != null ? fallbackLocation : livingEntity.getLocation();
         World world = dropLoc.getWorld();
         if (world != null) {
            if (ignoredItems != null) {
               for (ItemStack item : ignoredItems) {
                  if (item != null && item.getType() != Material.AIR) {
                     world.dropItemNaturally(dropLoc, item.clone());
                  }
               }
            }

            if (ignoredBlocks != null) {
               for (Block block : ignoredBlocks) {
                  if (block != null) {
                     Location blockLoc = block.getLocation();

                     for (ItemStack drop : block.getDrops()) {
                        if (drop != null && drop.getType() != Material.AIR) {
                           block.getWorld().dropItemNaturally(blockLoc, drop);
                        }
                     }
                  }
               }
            }
         } else if (ignoredItems != null && !ignoredItems.isEmpty()) {
            event.getDrops().addAll(ignoredItems);
         }
      }
   }
}
