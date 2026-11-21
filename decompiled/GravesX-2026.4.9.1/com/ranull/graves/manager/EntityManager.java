package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.BlockFaceUtil;
import com.ranull.graves.util.LocationUtil;
import com.ranull.graves.util.MaterialUtil;
import com.ranull.graves.util.ReflectionUtil;
import com.ranull.graves.util.StringUtil;
import com.ranull.graves.util.UUIDUtil;
import dev.cwhead.GravesX.compatibility.CompatibilityParticleEnum;
import dev.cwhead.GravesX.compatibility.CompatibilitySoundEnum;
import dev.cwhead.GravesX.compatibility.CompatibilityTeleport;
import dev.cwhead.GravesX.event.GraveAbandonedEvent;
import dev.cwhead.GravesX.event.GraveAutoLootEvent;
import dev.cwhead.GravesX.event.GraveCompassAddEvent;
import dev.cwhead.GravesX.event.GraveCompassUseEvent;
import dev.cwhead.GravesX.event.GraveParticleEvent;
import dev.cwhead.GravesX.event.GravePostTeleportEvent;
import dev.cwhead.GravesX.event.GravePreTeleportEvent;
import dev.cwhead.GravesX.event.GraveProtectionCreateEvent;
import dev.cwhead.GravesX.event.GraveTeleportEvent;
import dev.cwhead.GravesX.event.GraveVirtualOpenEvent;
import dev.cwhead.GravesX.event.GraveZombieSpawnEvent;
import dev.cwhead.GravesX.graveutils.GraveProtectionRole;
import dev.cwhead.GravesX.manager.SchedulerManager;
import dev.cwhead.GravesX.util.CustomModelDataUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;

public class EntityManager extends EntityDataManager {
   private final Graves plugin;
   private final GraveProtectionRole roles;

   public EntityManager(Graves plugin) {
      super(plugin);
      this.plugin = plugin;
      this.roles = new GraveProtectionRole(plugin);
   }

   public void swingMainHand(Player player) {
      if (this.plugin.getVersionManager().hasSwingHand()) {
         player.swingMainHand();
      } else {
         ReflectionUtil.swingMainHand(player);
      }
   }

   public ItemStack createGraveCompass(Player player, Location location, Grave grave) {
      GraveCompassAddEvent modern = new GraveCompassAddEvent(player, grave);
      this.plugin.getServer().getPluginManager().callEvent(modern);
      com.ranull.graves.event.GraveCompassAddEvent legacy = new com.ranull.graves.event.GraveCompassAddEvent(player, grave);
      this.plugin.getServer().getPluginManager().callEvent(legacy);
      if (modern.isCancelled() && modern.isAddon() && legacy.isCancelled() && legacy.isAddon()) {
         return null;
      } else if (!this.plugin.getVersionManager().hasPersistentData()) {
         return null;
      } else {
         Material material = Material.COMPASS;
         if (this.plugin.getConfigManager().getConfigSection("compass.recovery", grave).getBoolean("compass.recovery")) {
            try {
               material = Material.valueOf("RECOVERY_COMPASS");
            } catch (IllegalArgumentException var16) {
            }
         }

         ItemStack itemStack = new ItemStack(material);
         ItemMeta itemMeta = itemStack.getItemMeta();
         if (itemMeta != null) {
            if (this.plugin.getVersionManager().hasCompassMeta() && itemMeta instanceof CompassMeta compassMeta) {
               compassMeta.setLodestoneTracked(false);
               compassMeta.setLodestone(location);
            } else if (itemStack.getType().name().equals("RECOVERY_COMPASS")) {
               try {
                  player.setLastDeathLocation(location);
               } catch (NoSuchMethodError var15) {
               }
            }

            List<String> loreList = new ArrayList<>();
            int customModelData = this.plugin.getConfigManager().getConfigSection("compass.model-data", grave).getInt("compass.model-data", -1);
            CustomModelDataUtil.applyCustomModelData(itemMeta, customModelData);
            if (this.plugin.getConfigManager().getConfigSection("compass.glow", grave).getBoolean("compass.glow")) {
               Enchantment enchantment = this.plugin.getVersionManager().getEnchantmentForVersion("DURABILITY");
               itemMeta.addEnchant(enchantment, 1, true);
               itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
            }

            if (this.plugin.getIntegrationManager().hasMiniMessage()) {
               String compass_name = StringUtil.parseString(
                  "&f" + this.plugin.getConfigManager().getConfigSection("compass.name", grave).getString("compass.name"), grave, this.plugin
               );
               itemMeta.setDisplayName(MiniMessage.parseString(compass_name));
               itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "graveUUID"), PersistentDataType.STRING, grave.getUUID().toString());

               for (String string : this.plugin.getConfigManager().getConfigSection("compass.lore", grave).getStringList("compass.lore")) {
                  String compass_lore = StringUtil.parseString("&7" + string, location, grave, this.plugin);
                  loreList.add(MiniMessage.parseString(compass_lore));
               }
            } else {
               itemMeta.setDisplayName(
                  ChatColor.WHITE
                     + StringUtil.parseString(
                        this.plugin.getConfigManager().getConfigSection("compass.name", grave).getString("compass.name"), grave, this.plugin
                     )
               );
               itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "graveUUID"), PersistentDataType.STRING, grave.getUUID().toString());

               for (String string : this.plugin.getConfigManager().getConfigSection("compass.lore", grave).getStringList("compass.lore")) {
                  loreList.add(ChatColor.GRAY + StringUtil.parseString(string, location, grave, this.plugin));
               }
            }

            itemMeta.setLore(loreList);
            itemStack.setItemMeta(itemMeta);
         }

         return itemStack;
      }
   }

   public Map<ItemStack, UUID> getCompassesFromInventory(HumanEntity player) {
      Map<ItemStack, UUID> itemStackUUIDMap = new HashMap<>();
      if (this.plugin.getVersionManager().hasPersistentData()) {
         for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack != null) {
               UUID uuid = this.getGraveUUIDFromItemStack(itemStack);
               if (uuid != null) {
                  itemStackUUIDMap.put(itemStack, uuid);
               }
            }
         }
      }

      return itemStackUUIDMap;
   }

   public UUID getGraveUUIDFromItemStack(ItemStack itemStack) {
      return this.plugin.getVersionManager().hasPersistentData()
            && itemStack != null
            && itemStack.getItemMeta() != null
            && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "graveUUID"), PersistentDataType.STRING)
         ? UUIDUtil.getUUID(
            (String)itemStack.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(this.plugin, "graveUUID"), PersistentDataType.STRING)
         )
         : null;
   }

   public void teleportEntity(Entity entity, Location location, Grave grave) {
      GravePreTeleportEvent modernPre = new GravePreTeleportEvent(grave, entity);
      this.plugin.getServer().getPluginManager().callEvent(modernPre);
      com.ranull.graves.event.GravePreTeleportEvent legacy = new com.ranull.graves.event.GravePreTeleportEvent(grave, entity);
      this.plugin.getServer().getPluginManager().callEvent(legacy);
      if (!modernPre.isCancelled() && !modernPre.isAddon() && !legacy.isCancelled() && !legacy.isAddon()) {
         Location base = LocationUtil.roundLocation(location).clone();
         BlockFace face = BlockFaceUtil.getYawBlockFace(grave.getYaw());
         boolean faceRelative = this.plugin.getConfigManager().getConfigSection("teleport.face-relative", grave).getBoolean("teleport.face-relative", true);
         Location currentLocation = entity.getLocation().clone();
         float preservedYaw = currentLocation.getYaw();
         float preservedPitch = currentLocation.getPitch();
         Location target = base.clone().getBlock().getRelative(face).getRelative(face).getLocation().add(0.5, 0.0, 0.5);
         if (this.plugin.getLocationManager().isLocationSafePlayer(target)) {
            if (faceRelative) {
               target.setYaw(BlockFaceUtil.getBlockFaceYaw(face.getOppositeFace()));
               target.setPitch(20.0F);
            } else {
               target.setYaw(preservedYaw);
               target.setPitch(preservedPitch);
            }
         } else {
            Location safe = this.plugin.getLocationManager().getSafeTeleportLocation(entity, base.clone().add(0.0, 1.0, 0.0), grave, this.plugin);
            if (safe != null) {
               target = safe.add(0.5, 0.0, 0.5);
               if (faceRelative) {
                  target.setYaw(BlockFaceUtil.getBlockFaceYaw(face));
                  target.setPitch(90.0F);
               } else {
                  target.setYaw(preservedYaw);
                  target.setPitch(preservedPitch);
               }
            } else {
               target = null;
            }
         }

         long delaySeconds = this.plugin.getConfigManager().getConfigSection("teleport.delay", grave).getLong("teleport.delay");
         if (target != null && target.getWorld() != null) {
            if (entity instanceof Player player) {
               Location initialLocation = player.getLocation().clone();
               GraveTeleportEvent modern = new GraveTeleportEvent(grave, player);
               this.plugin.getServer().getPluginManager().callEvent(modern);
               com.ranull.graves.event.GraveTeleportEvent legacyTeleport = new com.ranull.graves.event.GraveTeleportEvent(grave, player);
               this.plugin.getServer().getPluginManager().callEvent(legacyTeleport);
               if (!modern.isCancelled() && !modern.isAddon() && !legacyTeleport.isCancelled() && !legacyTeleport.isAddon()) {
                  boolean bypass = this.plugin.getPermissionManager().hasGrantedPermission("graves.teleport.delay-bypass", player);
                  if (!bypass && delaySeconds > 0L) {
                     Location finalTarget = target.clone();
                     BossBar bossBar;
                     if (this.plugin.getIntegrationManager().hasMiniMessage()) {
                        String mmText = StringUtil.parseString(
                           this.plugin.getConfigManager().getConfigSection("message.teleport-waiting", grave).getString("message.teleport-waiting"),
                           base,
                           grave,
                           this.plugin
                        );
                        bossBar = this.plugin.getServer().createBossBar(MiniMessage.parseString(mmText), BarColor.RED, BarStyle.SOLID, new BarFlag[0]);
                     } else {
                        bossBar = this.plugin
                           .getServer()
                           .createBossBar(
                              StringUtil.parseString(
                                 this.plugin.getConfigManager().getConfigSection("message.teleport-waiting", grave).getString("message.teleport-waiting"),
                                 base,
                                 grave,
                                 this.plugin
                              ),
                              BarColor.RED,
                              BarStyle.SOLID,
                              new BarFlag[0]
                           );
                     }

                     bossBar.addPlayer(player);
                     int[] secondsLeft = new int[]{(int)Math.min(2147483647L, Math.max(0L, delaySeconds))};
                     Object[] cancelRef = new Object[1];
                     Runnable tick = () -> {
                        if (!player.isOnline()) {
                           bossBar.removeAll();
                           this.plugin.getEntityManager().sendMessage("message.teleport-cancelled", player, player.getLocation(), grave);
                           if (cancelRef[0] != null) {
                              ((Runnable)cancelRef[0]).run();
                           }
                        } else {
                           Location now = player.getLocation();
                           boolean moved = this.plugin.getConfig().getBoolean("teleport.strict")
                              ? !now.equals(initialLocation)
                              : !now.getBlock().equals(initialLocation.getBlock());
                           if (moved) {
                              bossBar.removeAll();
                              this.plugin.getEntityManager().sendMessage("message.teleport-cancelled", player, now, grave);
                              if (cancelRef[0] != null) {
                                 ((Runnable)cancelRef[0]).run();
                              }
                           } else {
                              if (secondsLeft[0] > 0) {
                                 double progress = (double)secondsLeft[0] / Math.max(1L, delaySeconds);
                                 bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
                                 secondsLeft[0]--;
                              } else {
                                 Location from = player.getLocation().clone();
                                 CompatibilityTeleport.teleportSafely(player, finalTarget, this.plugin).thenAccept(ok -> this.executeRegion(player, () -> {
                                    try {
                                       if (ok) {
                                          Location actualTo = player.getLocation().clone();
                                          this.firePostTeleportAndRollbackIfCancelled(grave, player, from, actualTo);
                                          Location playerLoc = player.getLocation();
                                          boolean cancelTeleport = false;
                                          World playerWorld = playerLoc.getWorld();
                                          World fromWorld = from.getWorld();
                                          if (playerWorld != null && playerWorld.equals(fromWorld)) {
                                             cancelTeleport = playerLoc.distanceSquared(from) > 0.01;
                                          }

                                          if (cancelTeleport) {
                                             this.plugin.getEntityManager().sendMessage("message.teleport-cancelled", player, from, grave);
                                          } else {
                                             this.plugin.getEntityManager().sendMessage("message.teleport", player, actualTo, grave);
                                             this.plugin.getEntityManager().playPlayerSound("sound.teleport", player, actualTo, grave);
                                          }
                                       } else {
                                          this.plugin.getEntityManager().sendMessage("message.teleport-cancelled", player, player.getLocation(), grave);
                                       }
                                    } finally {
                                       bossBar.removeAll();
                                       if (cancelRef[0] != null) {
                                          ((Runnable)cancelRef[0]).run();
                                       }
                                    }
                                 }));
                              }
                           }
                        }
                     };
                     SchedulerManager.Task task = this.plugin.getSchedulerManager().returnable().runTaskTimer(() -> this.executeRegion(player, tick), 0L, 20L);
                     cancelRef[0] = task::cancel;
                  } else {
                     Location finalTarget = target.clone();
                     boolean strict = this.plugin.getConfig().getBoolean("teleport.strict");
                     boolean samePos = strict
                        ? player.getLocation().equals(initialLocation)
                        : player.getLocation().getBlock().equals(initialLocation.getBlock());
                     if (samePos) {
                        CompatibilityTeleport.teleportSafely(player, finalTarget, this.plugin).thenAccept(ok -> this.executeRegion(player, () -> {
                           if (ok) {
                              Location from = initialLocation.clone();
                              Location actualTo = player.getLocation().clone();
                              this.firePostTeleportAndRollbackIfCancelled(grave, player, from, actualTo);

                              try {
                                 if (player.getLocation().getWorld() != null && player.getLocation().distanceSquared(from) < 0.01) {
                                    this.plugin.getEntityManager().sendMessage("message.teleport-cancelled", player, from, grave);
                                 } else {
                                    this.plugin.getEntityManager().sendMessage("message.teleport", player, actualTo, grave);
                                    this.plugin.getEntityManager().playPlayerSound("sound.teleport", player, actualTo, grave);
                                 }
                              } catch (IllegalArgumentException var8x) {
                                 this.plugin.getEntityManager().sendMessage("message.teleport", player, actualTo, grave);
                                 this.plugin.getEntityManager().playPlayerSound("sound.teleport", player, actualTo, grave);
                              }
                           } else {
                              this.plugin.getEntityManager().sendMessage("message.teleport-cancelled", player, player.getLocation(), grave);
                           }
                        }));
                     } else {
                        this.plugin.getEntityManager().sendMessage("message.teleport-cancelled", player, player.getLocation(), grave);
                     }
                  }
               }
            } else {
               GraveTeleportEvent modern = new GraveTeleportEvent(grave, entity);
               this.plugin.getServer().getPluginManager().callEvent(modern);
               com.ranull.graves.event.GraveTeleportEvent legacyTeleport = new com.ranull.graves.event.GraveTeleportEvent(grave, entity);
               this.plugin.getServer().getPluginManager().callEvent(legacyTeleport);
               if (!modern.isCancelled() && !modern.isAddon() && !legacyTeleport.isCancelled() && !legacyTeleport.isAddon()) {
                  Location finalTarget = target.clone();
                  Runnable doTeleport = () -> this.executeRegion(entity, () -> {
                     if (entity.isValid()) {
                        Location from = entity.getLocation().clone();
                        boolean ok = entity.teleport(finalTarget);
                        if (ok) {
                           Location actualTo = entity.getLocation().clone();
                           this.firePostTeleportAndRollbackIfCancelled(grave, entity, from, actualTo);
                           if (entity.isValid() && entity.getLocation().getWorld() != null && entity.getLocation().distanceSquared(from) < 0.01) {
                              this.plugin.getEntityManager().sendMessage("message.teleport-cancelled", entity, from, grave);
                           } else {
                              this.plugin.getEntityManager().sendMessage("message.teleport", entity, actualTo, grave);
                           }
                        } else {
                           this.plugin.getEntityManager().sendMessage("message.teleport-cancelled", entity, entity.getLocation(), grave);
                        }
                     }
                  });
                  if (delaySeconds > 0L) {
                     this.plugin.getSchedulerManager().runTaskLater(doTeleport, delaySeconds * 20L);
                  } else {
                     doTeleport.run();
                  }
               }
            }
         } else {
            this.plugin.getEntityManager().sendMessage("message.teleport-failure", entity, base, grave);
         }
      }
   }

   private void firePostTeleportAndRollbackIfCancelled(Grave grave, Entity entity, Location from, Location to) {
      GravePostTeleportEvent post = new GravePostTeleportEvent(grave, entity, from, to);
      this.plugin.getServer().getPluginManager().callEvent(post);
      if (post.isCancelled()) {
         if (entity instanceof Player player) {
            CompatibilityTeleport.teleportSafely(player, from, this.plugin);
         } else if (entity.isValid()) {
            entity.teleport(from);
         }
      }
   }

   public void playWorldSound(String string, Player player) {
      this.playWorldSound(string, player.getLocation(), null);
   }

   public void playWorldSound(String string, Player player, Grave grave) {
      this.playWorldSound(string, player.getLocation(), grave);
   }

   public void playWorldSound(String string, Location location, Grave grave) {
      this.playWorldSound(string, location, grave != null ? grave.getOwnerType() : null, grave != null ? grave.getPermissionList() : null, 1.0F, 1.0F);
   }

   public void playWorldSound(String string, Location location, EntityType entityType, List<String> permissionList, float volume, float pitch) {
      if (location.getWorld() != null) {
         string = this.plugin.getConfigManager().getConfigSection(string, entityType, permissionList).getString(string);
         if (string != null && !string.isEmpty()) {
            try {
               Location locCopy = location.clone();
               this.executeRegion(locCopy, () -> {
                  World w = locCopy.getWorld();
                  if (w != null) {
                     w.playSound(locCopy, CompatibilitySoundEnum.valueOf(string.toUpperCase()), volume, pitch);
                  }
               });
            } catch (IllegalArgumentException var9) {
               this.plugin.debugMessage(string.toUpperCase() + " is not a Sound ENUM", 1);
            }
         }
      }
   }

   public void playPlayerSound(String string, Entity entity, Grave grave) {
      this.playPlayerSound(string, entity, entity.getLocation(), grave.getPermissionList(), 1.0F, 1.0F);
   }

   public void playPlayerSound(String string, Entity entity, Location location, Grave grave) {
      this.playPlayerSound(string, entity, location, grave.getPermissionList(), 1.0F, 1.0F);
   }

   public void playPlayerSound(String string, Entity entity, List<String> permissionList) {
      this.playPlayerSound(string, entity, entity.getLocation(), permissionList, 1.0F, 1.0F);
   }

   public void playPlayerSound(String string, Entity entity, Location location, List<String> permissionList) {
      this.playPlayerSound(string, entity, location, permissionList, 1.0F, 1.0F);
   }

   public void playPlayerSound(String string, Entity entity, Location location, List<String> permissionList, float volume, float pitch) {
      if (entity instanceof Player player) {
         string = this.plugin.getConfigManager().getConfigSection(string, entity, permissionList).getString(string);
         if (string != null && !string.isEmpty()) {
            try {
               Location locCopy = location.clone();
               this.executeRegion(player, () -> player.playSound(locCopy, CompatibilitySoundEnum.valueOf(string.toUpperCase()), volume, pitch));
            } catch (IllegalArgumentException var10) {
               this.plugin.debugMessage(string.toUpperCase() + " is not a Sound ENUM", 1);
            }
         }
      }
   }

   public void sendMessage(String string, CommandSender commandSender) {
      if (commandSender instanceof Player player) {
         this.sendMessage(string, player, player.getLocation(), null, this.plugin.getConfigManager().getPermissionList(player));
      }
   }

   public void sendMessage(String string, Entity entity) {
      this.sendMessage(string, entity, entity.getLocation(), null, this.plugin.getConfigManager().getPermissionList(entity));
   }

   public void sendMessage(String string, Entity entity, List<String> permissionList) {
      this.sendMessage(string, entity, entity.getLocation(), null, permissionList);
   }

   public void sendMessage(String string, Entity entity, Location location, List<String> permissionList) {
      this.sendMessage(string, entity, location, null, permissionList);
   }

   public void sendMessage(String string, Entity entity, Location location, Grave grave) {
      this.sendMessage(string, entity, location, grave, null);
   }

   public void sendMessage(String string, Entity entity, String name, Location location, List<String> permissionList) {
      this.sendMessage(string, entity, name, location, null, permissionList);
   }

   private void sendMessage(String string, Entity entity, Location location, Grave grave, List<String> permissionList) {
      this.sendMessage(string, entity, this.getEntityName(entity), location, grave, permissionList);
   }

   private void sendMessage(String string, Entity entity, String name, Location location, Grave grave, List<String> permissionList) {
      if (entity instanceof Player player) {
         if (grave != null) {
            if (StringUtil.isStringEmpty(this.plugin.getConfigManager().getConfigSection(string, grave).getString(string))) {
               this.plugin.debugMessage(string + " was either null or empty. Not sending message out.", 2);
               return;
            }

            string = this.plugin.getConfigManager().getConfigSection(string, grave).getString(string);
         } else {
            if (StringUtil.isStringEmpty(this.plugin.getConfigManager().getConfigSection(string, entity.getType(), permissionList).getString(string))) {
               this.plugin.debugMessage(string + " was either null or empty. Not sending message out.", 2);
               return;
            }

            string = this.plugin.getConfigManager().getConfigSection(string, entity.getType(), permissionList).getString(string);
         }

         String prefix = this.plugin.getConfigManager().getConfigSection("message.prefix", entity.getType(), permissionList).getString("message.prefix");
         if (string != null && !string.isEmpty()) {
            if (prefix != null && !prefix.isEmpty()) {
               if (this.plugin.getIntegrationManager().hasMiniMessage()) {
                  string = prefix + "<white>" + string;
               } else {
                  string = prefix + string;
               }
            } else {
               string = this.plugin.getIntegrationManager().hasMiniMessage() ? "<white>" + string : "&r" + string;
            }

            String message = StringUtil.parseString(string, entity, name, location, grave, this.plugin);
            this.plugin.debugMessage("Message found for " + string + " in grave.yml. Sending message to " + entity.getName() + ".", 2);
            if (this.plugin.getIntegrationManager().hasMiniMessage()) {
               MiniMessage.sendMessage(player, message);
            } else {
               player.sendMessage(message);
            }
         } else {
            this.plugin.debugMessage("Original string " + string + " is empty, no message sent.", 2);
         }
      }
   }

   public void runCommands(String string, Entity entity, Location location, Grave grave) {
      this.runCommands(string, entity, null, location, grave);
   }

   public void runCommands(String string, String name, Location location, Grave grave) {
      this.runCommands(string, null, name, location, grave);
   }

   private void runCommands(String string, Entity entity, String name, Location location, Grave grave) {
      for (String command : this.plugin.getConfigManager().getConfigSection(string, grave).getStringList(string)) {
         if (command != null && !command.isEmpty()) {
            this.runConsoleCommand(StringUtil.parseString(command, entity, name, location, grave, this.plugin));
         }
      }
   }

   private void runConsoleCommand(String string) {
      if (string != null && !string.isEmpty()) {
         ServerCommandEvent serverCommandEvent = new ServerCommandEvent(this.plugin.getServer().getConsoleSender(), string);
         this.plugin.getServer().getPluginManager().callEvent(serverCommandEvent);
         if (this.plugin.getVersionManager().is_v1_7() || this.plugin.getVersionManager().is_v1_8() || !serverCommandEvent.isCancelled()) {
            this.plugin
               .getSchedulerManager()
               .callSyncMethod(() -> this.plugin.getServer().dispatchCommand(serverCommandEvent.getSender(), serverCommandEvent.getCommand()));
            this.plugin.debugMessage("Running console command " + string, 1);
         }
      }
   }

   public boolean runFunction(Entity entity, String function) {
      return this.runFunction(entity, function, null);
   }

   public boolean runFunction(Entity entity, String function, Grave grave) {
      String var4 = function.toLowerCase();
      switch (var4) {
         case "list":
            UUID targetUUID = grave.getOwnerUUID();
            if (targetUUID == null) {
               targetUUID = entity.getUniqueId();
            }

            this.plugin.getGUIManager().openGraveList(entity, targetUUID);
            return true;
         case "menu":
            if (entity instanceof Player player && !this.plugin.getPermissionManager().hasGrantedPermission("graves.gui", player.getPlayer())) {
               this.plugin.getEntityManager().sendMessage("message.permission-denied", entity, entity.getLocation(), grave);
               return false;
            }

            this.plugin.getGUIManager().openGraveMenu(entity, grave);
            return true;
         case "teleport":
         case "teleportation":
            if (entity instanceof Player playerxxx && !this.plugin.getPermissionManager().hasGrantedPermission("graves.teleport", playerxxx.getPlayer())) {
               this.plugin.getEntityManager().sendMessage("message.permission-denied", entity, entity.getLocation(), grave);
               return false;
            }

            if ((
                  !this.plugin.getConfigManager().getConfigSection("teleport.enabled", grave).getBoolean("teleport.enabled")
                     || !this.plugin.getPermissionManager().hasGrantedPermission("graves.teleport", ((Player)entity).getPlayer())
                        && (
                           !this.plugin.getConfigManager().getConfigSection("teleport.enabled", grave).getBoolean("teleport.enabled")
                              || !this.plugin
                                 .getPermissionManager()
                                 .hasGrantedPermission("graves.teleport.world." + grave.getLocationDeath().getWorld().getName(), ((Player)entity).getPlayer())
                        )
                        && !this.plugin.getPermissionManager().hasGrantedPermission("graves.bypass", ((Player)entity).getPlayer())
               )
               && !this.plugin.getPermissionManager().hasGrantedPermission("graves.teleport.bypass", ((Player)entity).getPlayer())) {
               this.plugin.getEntityManager().sendMessage("message.teleport-disabled", entity, entity.getLocation(), grave);
            } else {
               boolean isBypassOther = this.plugin.getPermissionManager().hasGrantedPermission("graves.bypass", ((Player)entity).getPlayer())
                     && !grave.getOwnerUUID().equals(entity.getUniqueId())
                  || this.plugin.getPermissionManager().hasGrantedPermission("graves.teleport.bypass", ((Player)entity).getPlayer())
                     && !grave.getOwnerUUID().equals(entity.getUniqueId());
               if (isBypassOther) {
                  this.executeRegion(
                     entity, () -> entity.teleport(this.plugin.getGraveManager().getGraveLocation(grave.getLocationDeath().add(1.0, 0.0, 1.0), grave))
                  );
               } else {
                  this.plugin
                     .getEntityManager()
                     .teleportEntity(entity, this.plugin.getGraveManager().getGraveLocationList(entity.getLocation(), grave).get(0), grave);
               }
            }

            return true;
         case "protect":
         case "protection":
            if (entity instanceof Player playerxxx && !this.plugin.getPermissionManager().hasGrantedPermission("graves.protection", playerxxx.getPlayer())) {
               this.plugin.getEntityManager().sendMessage("message.permission-denied", entity, entity.getLocation(), grave);
               return false;
            }

            GraveProtectionCreateEvent modern = new GraveProtectionCreateEvent(entity, grave);
            this.plugin.getServer().getPluginManager().callEvent(modern);
            com.ranull.graves.event.GraveProtectionCreateEvent legacy = new com.ranull.graves.event.GraveProtectionCreateEvent(entity, grave);
            this.plugin.getServer().getPluginManager().callEvent(legacy);
            if ((!modern.isCancelled() || !modern.isAddon() || !legacy.isCancelled() || !legacy.isAddon())
               && (grave.getTimeProtectionRemaining() > 0L || grave.getTimeProtectionRemaining() < 0L)) {
               this.plugin.getGraveManager().toggleGraveProtection(grave);
               this.playPlayerSound("sound.protection-change", entity, grave);
               this.plugin.getGUIManager().openGraveMenu(entity, grave, false);
            }

            return true;
         case "abandoned":
            GraveAbandonedEvent modern = new GraveAbandonedEvent(grave);
            this.plugin.getServer().getPluginManager().callEvent(modern);
            com.ranull.graves.event.GraveAbandonedEvent legacy = new com.ranull.graves.event.GraveAbandonedEvent(grave);
            this.plugin.getServer().getPluginManager().callEvent(legacy);
            if (!modern.isCancelled() || !modern.isAddon() || !legacy.isCancelled() || !legacy.isAddon()) {
               Location location = this.plugin.getGraveManager().getGraveLocation(entity.getLocation(), grave);
               if (location != null) {
                  this.playPlayerSound("sound.abandoned", entity, grave);
                  this.plugin.getEntityManager().sendMessage("message.abandoned", entity, location, grave);
                  this.plugin.getGraveManager().abandonGrave(grave);
               }
            }

            return true;
         case "distance":
            Player playerxx = entity instanceof Player px ? px : null;
            if (playerxx == null) {
               return false;
            }

            GraveCompassUseEvent modern = new GraveCompassUseEvent(playerxx, grave);
            this.plugin.getServer().getPluginManager().callEvent(modern);
            com.ranull.graves.event.GraveCompassUseEvent legacy = new com.ranull.graves.event.GraveCompassUseEvent(playerxx, grave);
            this.plugin.getServer().getPluginManager().callEvent(legacy);
            if (!modern.isCancelled() || !modern.isAddon() || !legacy.isCancelled() || !legacy.isAddon()) {
               Location location = this.plugin.getGraveManager().getGraveLocation(entity.getLocation(), grave);
               if (location != null) {
                  if (entity.getWorld().equals(location.getWorld())) {
                     this.plugin.getEntityManager().sendMessage("message.distance", entity, location, grave);
                  } else {
                     this.plugin.getEntityManager().sendMessage("message.distance-world", entity, location, grave);
                  }
               }
            }

            return true;
         case "open":
         case "loot":
         case "virtual":
            if (entity.getLocation().getWorld() == grave.getLocationDeath().getWorld()) {
               if (entity instanceof Player playerxx && !this.plugin.getPermissionManager().hasGrantedPermission("graves.open", playerxx.getPlayer())) {
                  this.plugin.getEntityManager().sendMessage("message.permission-denied", entity, entity.getLocation(), grave);
                  return false;
               }

               double configMaxDistance = this.plugin.getConfigManager().getConfigSection("virtual.distance", grave).getDouble("virtual.distance");
               Location graveLocation = this.plugin.getGraveManager().getGraveLocation(entity.getLocation(), grave);
               if (graveLocation == null) {
                  graveLocation = grave.getLocationDeath();
               }

               if (graveLocation == null || graveLocation.getWorld() == null) {
                  this.plugin.getGraveManager().openGrave(entity, entity.getLocation(), grave);
                  return true;
               }

               if (graveLocation.getWorld() != entity.getLocation().getWorld()) {
                  this.plugin.getEntityManager().sendMessage("message.distance-virtual", entity, entity.getLocation(), grave);
                  return true;
               }

               double actualDistance = entity.getLocation().distance(graveLocation);
               LivingEntity livingEntity = entity instanceof LivingEntity le ? le : null;
               GraveVirtualOpenEvent virtualEvent = new GraveVirtualOpenEvent(
                  grave, entity, entity.getLocation(), null, null, livingEntity, null, actualDistance, configMaxDistance
               );
               this.plugin.getServer().getPluginManager().callEvent(virtualEvent);
               if (virtualEvent.isCancelled()) {
                  return true;
               }

               double effectiveMaxDistance = virtualEvent.getMaxDistance();
               if (!(effectiveMaxDistance < 0.0) && !(actualDistance <= effectiveMaxDistance)) {
                  this.plugin.getEntityManager().sendMessage("message.distance-virtual", entity, graveLocation, grave);
               } else {
                  this.plugin.getGraveManager().openGrave(entity, entity.getLocation(), grave);
               }
            } else {
               this.plugin.getEntityManager().sendMessage("message.distance-virtual", entity, entity.getLocation(), grave);
            }

            return true;
         case "autoloot":
            if (!this.plugin.getConfigManager().getConfigSection("drop.auto-loot.enabled", grave).getBoolean("drop.auto-loot.enabled", true)) {
               return false;
            } else {
               Location loc = entity.getLocation();
               if (entity instanceof Player playerx) {
                  if (!this.plugin.getPermissionManager().hasGrantedPermission("graves.autoloot", playerx.getPlayer())) {
                     this.plugin.getEntityManager().sendMessage("message.permission-denied", entity, entity.getLocation(), grave);
                     return false;
                  }

                  if (playerx.getGameMode() == GameMode.SPECTATOR
                     && !this.plugin.getPermissionManager().hasGrantedPermission("graves.spectator.bypass", playerx.getPlayer())) {
                     return false;
                  }
               }

               GraveAutoLootEvent modern = new GraveAutoLootEvent(entity, loc, grave);
               this.plugin.getServer().getPluginManager().callEvent(modern);
               com.ranull.graves.event.GraveAutoLootEvent legacy = new com.ranull.graves.event.GraveAutoLootEvent(entity, loc, grave);
               this.plugin.getServer().getPluginManager().callEvent(legacy);
               if (!modern.isCancelled() || !modern.isAddon() || !legacy.isCancelled() || !legacy.isAddon()) {
                  this.plugin.getGraveManager().autoLootGrave(entity, loc, grave);
                  if (this.plugin.getIntegrationManager().hasNoteBlockAPI()) {
                     Player playerx = (Player)entity;
                     if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForPlayer(playerx)) {
                        this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForPlayer(playerx);
                     }

                     if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForAllPlayers()) {
                        this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForAllPlayers();
                     }
                  }
               }

               return true;
            }
         case "particle":
         case "particles":
            if (this.plugin.getConfigManager().getConfigSection("compass.particles.enabled", grave).getBoolean("compass.particles.enabled")) {
               Player player = entity instanceof Player p ? p : null;
               if (player == null) {
                  return false;
               }

               GraveParticleEvent modern = new GraveParticleEvent(player, grave);
               this.plugin.getServer().getPluginManager().callEvent(modern);
               com.ranull.graves.event.GraveParticleEvent legacy = new com.ranull.graves.event.GraveParticleEvent(player, grave);
               this.plugin.getServer().getPluginManager().callEvent(legacy);
               if (!modern.isCancelled() || !modern.isAddon() || !legacy.isCancelled() || !legacy.isAddon()) {
                  Location location = this.plugin.getGraveManager().getGraveLocation(entity.getLocation(), grave);
                  if (location != null && entity.getLocation().getWorld() == grave.getLocationDeath().getWorld()) {
                     this.plugin
                        .getParticleManager()
                        .startCompassParticleTrail(
                           entity.getLocation(),
                           grave.getLocationDeath(),
                           CompatibilityParticleEnum.valueOf(
                              Objects.requireNonNull(
                                    this.plugin
                                       .getConfigManager()
                                       .getConfigSection("compass.particles.particle", grave)
                                       .getString("compass.particles.particle")
                                 )
                                 .toUpperCase()
                           ),
                           this.plugin.getConfigManager().getConfigSection("compass.particles.count", grave).getInt("compass.particles.count", 5),
                           this.plugin.getConfigManager().getConfigSection("compass.particles.speed", grave).getDouble("compass.particles.speed", 0.3),
                           this.plugin.getConfigManager().getConfigSection("compass.particles.duration", grave).getInt("compass.particles.duration"),
                           entity.getUniqueId()
                        );
                  }
               }
            }

            return true;
         case "preview":
         case "sneekpeak":
            Location location = this.plugin.getGraveManager().getGraveLocation(entity.getLocation(), grave);
            if (location != null
               && entity.getLocation().getWorld() == grave.getLocationDeath().getWorld()
               && this.plugin.getConfigManager().getConfigSection("grave.preview", grave).getBoolean("grave.preview")) {
               this.plugin.getGraveManager().openGrave(entity, entity.getLocation(), grave, true);
            }

            return true;
         default:
            return false;
      }
   }

   public boolean canOpenGrave(Player player, Grave grave) {
      if (this.plugin.getPermissionManager().hasGrantedPermission("graves.bypass", player.getPlayer())) {
         return true;
      } else {
         UUID playerId = this.plugin.getIntegrationManager().hasFloodgate()
            ? this.plugin.getIntegrationManager().getFloodgate().getNormalizedUUID(player)
            : player.getUniqueId();
         if (grave.getProtection() && grave.getOwnerUUID() != null) {
            long timeRemaining = grave.getTimeProtectionRemaining();
            if (timeRemaining == 0L) {
               return true;
            } else {
               GraveProtectionRole.GraveRole role = this.roles.getRole(grave, playerId);
               if (role == GraveProtectionRole.GraveRole.OWNER) {
                  return this.plugin.getConfigManager().getConfigSection("protection.open.owner", grave).getBoolean("protection.open.owner");
               } else if (role == GraveProtectionRole.GraveRole.KILLER) {
                  return this.plugin.getConfigManager().getConfigSection("protection.open.killer", grave).getBoolean("protection.open.killer");
               } else if (role == GraveProtectionRole.GraveRole.OTHER) {
                  return this.plugin.getConfigManager().getConfigSection("protection.open.other", grave).getBoolean("protection.open.other");
               } else if (role == GraveProtectionRole.GraveRole.MISSING_OWNER) {
                  return true;
               } else {
                  return role == GraveProtectionRole.GraveRole.MISSING_OTHER ? false : false;
               }
            }
         } else {
            return true;
         }
      }
   }

   public void spawnZombie(Location location, Entity entity, LivingEntity targetEntity, Grave grave) {
      if (this.plugin.getConfigManager().getConfigSection("zombie.spawn-owner", grave).getBoolean("zombie.spawn-owner")
            && grave.getOwnerUUID().equals(entity.getUniqueId())
         || this.plugin.getConfigManager().getConfigSection("zombie.spawn-other", grave).getBoolean("zombie.spawn-other")
            && !grave.getOwnerUUID().equals(entity.getUniqueId())) {
         this.spawnZombie(location, targetEntity, grave);
      }
   }

   public void spawnZombie(Location location, Grave grave) {
      this.spawnZombie(location, null, grave);
   }

   private void spawnZombie(Location location, LivingEntity targetEntity, Grave grave) {
      if (location != null && location.getWorld() != null && grave.getOwnerType() == EntityType.PLAYER) {
         GraveZombieSpawnEvent modern = new GraveZombieSpawnEvent(location, targetEntity, grave);
         this.plugin.getServer().getPluginManager().callEvent(modern);
         com.ranull.graves.event.GraveZombieSpawnEvent legacy = new com.ranull.graves.event.GraveZombieSpawnEvent(location, targetEntity, grave);
         this.plugin.getServer().getPluginManager().callEvent(legacy);
         if (!modern.isCancelled() && !modern.isAddon() && !legacy.isCancelled() && !legacy.isAddon()) {
            Location locCopy = location.clone();
            this.executeRegion(
               locCopy,
               () -> {
                  try {
                     World world = locCopy.getWorld();
                     if (world != null) {
                        try {
                           world.playSound(locCopy, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("BLOCK_BELL_USE")), 1.0F, 1.0F);
                        } catch (Exception var17) {
                           try {
                              world.playSound(locCopy, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("BELL")), 1.0F, 1.0F);
                           } catch (Exception var16) {
                           }
                        }

                        try {
                           this.plugin.getSchedulerManager().runTaskLater(locCopy, () -> {
                              try {
                                 World w2 = locCopy.getWorld();
                                 if (w2 == null) {
                                    return;
                                 }

                                 try {
                                    w2.playSound(locCopy, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("BLOCK_BELL_USE")), 1.0F, 0.75F);
                                 } catch (Exception var5x) {
                                    try {
                                       w2.playSound(locCopy, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("BELL")), 1.0F, 0.75F);
                                    } catch (Exception var4x) {
                                    }
                                 }
                              } catch (Exception var6x) {
                              }
                           }, 20L);
                        } catch (Throwable var15) {
                        }
                     }
                  } catch (Exception var18) {
                  }

                  String zombieType = this.plugin.getConfigManager().getConfigSection("zombie.type", grave).getString("zombie.type", "ZOMBIE").toUpperCase();
                  EntityType entityType = EntityType.ZOMBIE;

                  try {
                     entityType = EntityType.valueOf(zombieType);
                  } catch (IllegalArgumentException var14) {
                     this.plugin.debugMessage(zombieType + " is not a EntityType ENUM", 1);
                  }

                  if ("ZOMBIE".equals(entityType.name()) && MaterialUtil.isWater(locCopy.getBlock().getType())) {
                     try {
                        entityType = EntityType.valueOf("DROWNED");
                     } catch (IllegalArgumentException var13) {
                     }
                  }

                  Entity entity = locCopy.getWorld().spawnEntity(locCopy, entityType);
                  if (entity instanceof LivingEntity livingEntity) {
                     if (livingEntity.getEquipment() != null) {
                        if (this.plugin.getConfigManager().getConfigSection("zombie.owner-head", grave).getBoolean("zombie.owner-head")) {
                           livingEntity.getEquipment().setHelmet(this.plugin.getCompatibility().getSkullItemStack(grave, this.plugin));
                        }

                        livingEntity.getEquipment().setChestplate(null);
                        livingEntity.getEquipment().setLeggings(null);
                        livingEntity.getEquipment().setBoots(null);
                     }

                     livingEntity.setMetadata("GravesX", new FixedMetadataValue(this.plugin, true));
                     double zombieHealth = this.plugin.getConfigManager().getConfigSection("zombie.health", grave).getDouble("zombie.health");
                     if (zombieHealth >= 0.5) {
                        livingEntity.setMaxHealth(zombieHealth);
                        livingEntity.setHealth(zombieHealth);
                     }

                     if (!this.plugin.getConfigManager().getConfigSection("zombie.pickup", grave).getBoolean("zombie.pickup")) {
                        livingEntity.setCanPickupItems(false);
                     }

                     String zombieName = StringUtil.parseString(
                        this.plugin.getConfigManager().getConfigSection("zombie.name", grave).getString("zombie.name"), locCopy, grave, this.plugin
                     );
                     if (!zombieName.isEmpty()) {
                        if (this.plugin.getIntegrationManager().hasMiniMessage()) {
                           livingEntity.setCustomName(MiniMessage.parseString(zombieName));
                        } else {
                           livingEntity.setCustomName(zombieName);
                        }
                     }

                     this.setDataByte(livingEntity, "graveZombie");
                     this.setDataString(livingEntity, "graveUUID", grave.getUUID().toString());
                     this.setDataString(livingEntity, "graveEntityType", grave.getOwnerType().name());
                     this.runCommands("event.command.zombiespawn", targetEntity, locCopy, grave);
                     if (grave.getPermissionList() != null && !grave.getPermissionList().isEmpty()) {
                        this.setDataString(livingEntity, "gravePermissionList", String.join("|", grave.getPermissionList()));
                     }

                     if (livingEntity instanceof Mob mob
                        && !targetEntity.isInvulnerable()
                        && !(targetEntity instanceof Player p && p.getGameMode() == GameMode.CREATIVE)) {
                        mob.setTarget(targetEntity);
                     }

                     if (livingEntity instanceof Zombie zombie && zombie.isBaby()) {
                        zombie.setBaby(false);
                     }
                  }

                  this.plugin.debugMessage("Zombie type " + this.getEntityName(entity) + " spawned for grave " + grave.getUUID(), 1);
               }
            );
         }
      }
   }

   public void createArmorStand(Location location, Grave grave) {
      if (!this.plugin.getVersionManager().is_v1_7()
         && this.plugin.getConfigManager().getConfigSection("armor-stand.enabled", grave).getBoolean("armor-stand.enabled")) {
         double offsetX = this.plugin.getConfigManager().getConfigSection("armor-stand.offset.x", grave).getDouble("armor-stand.offset.x");
         double offsetY = this.plugin.getConfigManager().getConfigSection("armor-stand.offset.y", grave).getDouble("armor-stand.offset.y");
         double offsetZ = this.plugin.getConfigManager().getConfigSection("armor-stand.offset.z", grave).getDouble("armor-stand.offset.z");
         Location loc = LocationUtil.roundLocation(location).add(offsetX + 0.5, offsetY, offsetZ + 0.5);
         loc.setYaw(grave.getYaw());
         loc.setPitch(grave.getPitch());
         if (loc.getWorld() != null) {
            this.executeRegion(
               loc,
               () -> {
                  Material material = Material.matchMaterial(
                     this.plugin.getConfigManager().getConfigSection("armor-stand.material", grave).getString("armor-stand.material", "AIR")
                  );
                  if (material != null && !MaterialUtil.isAir(material)) {
                     ItemStack itemStack = new ItemStack(material, 1);
                     ItemMeta itemMeta = itemStack.getItemMeta();
                     int customModelData = this.plugin
                        .getConfigManager()
                        .getConfigSection("armor-stand.model-data", grave)
                        .getInt("armor-stand.model-data", -1);
                     if (itemMeta != null) {
                        CustomModelDataUtil.applyCustomModelData(itemMeta, customModelData);
                        itemStack.setItemMeta(itemMeta);
                        loc.getBlock().setType(Material.AIR);
                        ArmorStand armorStand = (ArmorStand)loc.getWorld().spawn(loc, ArmorStand.class);
                        this.createEntityData(loc, armorStand.getUniqueId(), grave.getUUID(), EntityData.Type.ARMOR_STAND);
                        boolean marker = this.plugin.getConfigManager().getConfigSection("armor-stand.marker", grave).getBoolean("armor-stand.marker");
                        if (!this.plugin.getVersionManager().is_v1_7()) {
                           try {
                              armorStand.setMarker(marker);
                           } catch (NoSuchMethodError var12) {
                           }
                        }

                        if (!this.plugin.getVersionManager().is_v1_7() && !this.plugin.getVersionManager().is_v1_8()) {
                           armorStand.setInvulnerable(true);
                        }

                        if (this.plugin.getVersionManager().hasScoreboardTags()) {
                           armorStand.getScoreboardTags().add("graveArmorStand");
                           armorStand.getScoreboardTags().add("graveArmorStandUUID:" + grave.getUUID());
                        }

                        armorStand.setVisible(false);
                        armorStand.setGravity(false);
                        armorStand.setCustomNameVisible(false);
                        armorStand.setSmall(this.plugin.getConfigManager().getConfigSection("armor-stand.small", grave).getBoolean("armor-stand.small"));
                        if (armorStand.getEquipment() != null) {
                           EquipmentSlot equipmentSlot = EquipmentSlot.HEAD;

                           try {
                              equipmentSlot = EquipmentSlot.valueOf(
                                 this.plugin.getConfigManager().getConfigSection("armor-stand.slot", grave).getString("armor-stand.slot", "HEAD")
                              );
                           } catch (IllegalArgumentException var11) {
                           }

                           armorStand.getEquipment().setItem(equipmentSlot, itemStack);
                        }
                     }
                  }
               }
            );
         }
      }
   }

   public void createItemFrame(Location location, Grave grave) {
      if (this.plugin.getConfigManager().getConfigSection("item-frame.enabled", grave).getBoolean("item-frame.enabled")) {
         double offsetX = this.plugin.getConfigManager().getConfigSection("item-frame.offset.x", grave).getDouble("item-frame.offset.x");
         double offsetY = this.plugin.getConfigManager().getConfigSection("item-frame.offset.y", grave).getDouble("item-frame.offset.y");
         double offsetZ = this.plugin.getConfigManager().getConfigSection("item-frame.offset.z", grave).getDouble("item-frame.offset.z");
         Location loc = LocationUtil.roundLocation(location).add(offsetX + 0.5, offsetY, offsetZ + 0.5);
         loc.setYaw(grave.getYaw());
         loc.setPitch(grave.getPitch());
         if (loc.getWorld() != null) {
            this.executeRegion(
               loc,
               () -> {
                  Material material = Material.matchMaterial(
                     this.plugin.getConfigManager().getConfigSection("item-frame.material", grave).getString("item-frame.material", "AIR")
                  );
                  if (material != null && !MaterialUtil.isAir(material)) {
                     ItemStack itemStack = new ItemStack(material, 1);
                     ItemMeta itemMeta = itemStack.getItemMeta();
                     int customModelData = this.plugin.getConfigManager().getConfigSection("item-frame.model-data", grave).getInt("item-frame.model-data", -1);
                     if (itemMeta != null) {
                        CustomModelDataUtil.applyCustomModelData(itemMeta, customModelData);
                        itemStack.setItemMeta(itemMeta);
                        loc.getBlock().setType(Material.AIR);
                        ItemFrame itemFrame = (ItemFrame)loc.getWorld().spawn(loc, ItemFrame.class);
                        itemFrame.setFacingDirection(BlockFace.UP);
                        itemFrame.setRotation(BlockFaceUtil.getBlockFaceRotation(BlockFaceUtil.getYawBlockFace(loc.getYaw())));
                        itemFrame.setVisible(false);
                        itemFrame.setGravity(false);
                        itemFrame.setCustomNameVisible(false);
                        itemFrame.setItem(itemStack);
                        if (!this.plugin.getVersionManager().is_v1_7() && !this.plugin.getVersionManager().is_v1_8()) {
                           itemFrame.setInvulnerable(true);
                        }

                        if (this.plugin.getVersionManager().hasScoreboardTags()) {
                           itemFrame.getScoreboardTags().add("graveItemFrame");
                           itemFrame.getScoreboardTags().add("graveItemFrameUUID:" + grave.getUUID());
                        }

                        this.createEntityData(loc, itemFrame.getUniqueId(), grave.getUUID(), EntityData.Type.ITEM_FRAME);
                     }
                  }
               }
            );
         }
      }
   }

   public void removeEntity(Grave grave) {
      this.removeEntity(this.getEntityDataMap(this.getLoadedEntityDataList(grave)));
   }

   public void removeEntity(Map<EntityData, Entity> entityDataMap) {
      List<EntityData> entityDataList = new ArrayList<>();

      for (Entry<EntityData, Entity> entry : entityDataMap.entrySet()) {
         if (entry.getKey().getType() == EntityData.Type.ARMOR_STAND
            || entry.getKey().getType() == EntityData.Type.ITEM_FRAME
            || entry.getKey().getType() == EntityData.Type.HOLOGRAM) {
            Entity e = entry.getValue();
            this.executeRegion(e, e::remove);
            entityDataList.add(entry.getKey());
         }
      }

      this.plugin.getDataManager().removeEntityData(entityDataList);
   }

   public Map<EquipmentSlot, ItemStack> getEquipmentMap(LivingEntity livingEntity, Grave grave) {
      Map<EquipmentSlot, ItemStack> equipmentSlotItemStackMap = new HashMap<>();
      if (livingEntity.getEquipment() != null) {
         EntityEquipment entityEquipment = livingEntity.getEquipment();
         if (entityEquipment.getHelmet() != null && grave.getInventory().contains(entityEquipment.getHelmet())) {
            equipmentSlotItemStackMap.put(EquipmentSlot.HEAD, entityEquipment.getHelmet());
         }

         if (entityEquipment.getChestplate() != null && grave.getInventory().contains(entityEquipment.getChestplate())) {
            equipmentSlotItemStackMap.put(EquipmentSlot.CHEST, entityEquipment.getChestplate());
         }

         if (entityEquipment.getLeggings() != null && grave.getInventory().contains(entityEquipment.getLeggings())) {
            equipmentSlotItemStackMap.put(EquipmentSlot.LEGS, entityEquipment.getLeggings());
         }

         if (entityEquipment.getBoots() != null && grave.getInventory().contains(entityEquipment.getBoots())) {
            equipmentSlotItemStackMap.put(EquipmentSlot.FEET, entityEquipment.getBoots());
         }

         if (this.plugin.getVersionManager().hasSecondHand()) {
            if (entityEquipment.getItemInMainHand().getType() != Material.AIR && grave.getInventory().contains(entityEquipment.getItemInMainHand())) {
               equipmentSlotItemStackMap.put(EquipmentSlot.HAND, entityEquipment.getItemInMainHand());
            }

            if (entityEquipment.getItemInOffHand().getType() != Material.AIR && grave.getInventory().contains(entityEquipment.getItemInOffHand())) {
               equipmentSlotItemStackMap.put(EquipmentSlot.OFF_HAND, entityEquipment.getItemInOffHand());
            }
         } else if (entityEquipment.getItemInHand().getType() != Material.AIR && grave.getInventory().contains(entityEquipment.getItemInHand())) {
            equipmentSlotItemStackMap.put(EquipmentSlot.HAND, entityEquipment.getItemInHand());
         }
      }

      return equipmentSlotItemStackMap;
   }

   public String getEntityName(Entity entity) {
      if (entity != null) {
         if (entity instanceof Player player) {
            return player.getName();
         } else {
            return !this.plugin.getVersionManager().is_v1_7() ? entity.getName() : StringUtil.format(entity.getType().toString());
         }
      } else {
         return "null";
      }
   }

   public boolean hasDataString(Entity entity, String string) {
      return this.plugin.getVersionManager().hasPersistentData()
         ? entity.getPersistentDataContainer().has(new NamespacedKey(this.plugin, string), PersistentDataType.STRING)
         : entity.hasMetadata(string);
   }

   public boolean hasDataByte(Entity entity, String string) {
      return this.plugin.getVersionManager().hasPersistentData()
         ? entity.getPersistentDataContainer().has(new NamespacedKey(this.plugin, string), PersistentDataType.BYTE)
         : entity.hasMetadata(string);
   }

   public String getDataString(Entity entity, String key) {
      return this.plugin.getVersionManager().hasPersistentData()
            && entity.getPersistentDataContainer().has(new NamespacedKey(this.plugin, key), PersistentDataType.STRING)
         ? (String)entity.getPersistentDataContainer().get(new NamespacedKey(this.plugin, key), PersistentDataType.STRING)
         : entity.getMetadata(key).toString();
   }

   public void setDataString(Entity entity, String key, String string) {
      if (this.plugin.getVersionManager().hasPersistentData()) {
         entity.getPersistentDataContainer().set(new NamespacedKey(this.plugin, key), PersistentDataType.STRING, string);
      } else {
         entity.setMetadata(key, new FixedMetadataValue(this.plugin, string));
      }
   }

   public void setDataByte(Entity entity, String key) {
      if (this.plugin.getVersionManager().hasPersistentData()) {
         entity.getPersistentDataContainer().set(new NamespacedKey(this.plugin, key), PersistentDataType.BYTE, (byte)1);
      } else {
         entity.setMetadata(key, new FixedMetadataValue(this.plugin, (byte)1));
      }
   }

   public Grave getGraveFromEntityData(Entity entity) {
      if (this.plugin.getVersionManager().hasPersistentData()
         && entity.getPersistentDataContainer().has(new NamespacedKey(this.plugin, "graveUUID"), PersistentDataType.STRING)) {
         return this.plugin
            .getCacheManager()
            .getGraveMap()
            .get(UUIDUtil.getUUID((String)entity.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "graveUUID"), PersistentDataType.STRING)));
      } else {
         if (entity.hasMetadata("graveUUID")) {
            List<MetadataValue> metadataValue = entity.getMetadata("graveUUID");
            if (!metadataValue.isEmpty()) {
               return this.plugin.getCacheManager().getGraveMap().get(UUIDUtil.getUUID(metadataValue.get(0).asString()));
            }
         }

         return null;
      }
   }

   private void executeRegion(Location loc, Runnable task) {
      SchedulerManager sched = this.plugin.getSchedulerManager();
      if (sched != null) {
         sched.execute(loc, task);
      } else {
         this.plugin.getSchedulerManager().runTask(task);
      }
   }

   private void executeRegion(Entity entity, Runnable task) {
      SchedulerManager sched = this.plugin.getSchedulerManager();
      if (sched != null) {
         sched.execute(entity, task);
      } else {
         this.plugin.getSchedulerManager().runTask(task);
      }
   }
}
