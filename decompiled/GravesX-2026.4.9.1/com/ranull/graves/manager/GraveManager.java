package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.compatibility.CompatibilityInventoryView;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.data.ChunkData;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.data.HologramData;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.inventory.GraveList;
import com.ranull.graves.inventory.GraveMenu;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.InventoryUtil;
import com.ranull.graves.util.LocationUtil;
import com.ranull.graves.util.MaterialUtil;
import com.ranull.graves.util.StringUtil;
import dev.cwhead.GravesX.api.provider.GraveProvider;
import dev.cwhead.GravesX.api.provider.RegisterGraveProviders;
import dev.cwhead.GravesX.event.GraveAbandonedEvent;
import dev.cwhead.GravesX.event.GraveAutoLootEvent;
import dev.cwhead.GravesX.event.GraveExpiredEvent;
import dev.cwhead.GravesX.event.GraveProtectionExpiredEvent;
import dev.cwhead.GravesX.event.GraveTimeoutEvent;
import dev.cwhead.GravesX.exception.GravesXGraveProviderException;
import dev.cwhead.GravesX.exception.GravesXNullPointerException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import me.jay.GravesX.util.pluginsWithoutMavenReposOrUsefulApiDocsThatCauseBugs.ReflectSupportAE;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class GraveManager {
   private final Graves plugin;
   private final Set<UUID> knownGraves = Collections.newSetFromMap(new ConcurrentHashMap<>());

   public GraveManager(@NotNull Graves plugin) {
      this.plugin = plugin;
      plugin.getSchedulerManager().runTask(this::startGraveTimer);
   }

   private void startGraveTimer() {
      this.plugin.getSchedulerManager().runTaskTimer(() -> {
         try {
            this.checkAndUpdateGraves();
         } catch (Throwable var2) {
            this.plugin.debugMessage("Failed to tick grave task. Graves may not update this tick. (This is normal for servers that are having TPS loss.)", 2);
         }
      }, 20L, 20L);
   }

   private void checkAndUpdateGraves() {
      List<Grave> graveRemoveList = new ArrayList<>();
      List<EntityData> entityDataRemoveList = new ArrayList<>();
      List<BlockData> blockDataRemoveList = new ArrayList<>();
      this.processGraves(graveRemoveList);
      this.processChunks(entityDataRemoveList, blockDataRemoveList);
      this.processHologramDataTick(entityDataRemoveList);
      this.removeExpiredElements(graveRemoveList, entityDataRemoveList, blockDataRemoveList);
      if (!graveRemoveList.isEmpty()
         && this.plugin
            .getConfigManager()
            .getConfigSection("grave.check-missing-graves", graveRemoveList.getFirst())
            .getBoolean("grave.check-missing-graves", false)) {
         this.restoreMissingGraves();
         this.plugin.getHologramManager().purgeLingeringHolograms();
      }
   }

   private void processGraves(List<Grave> graveRemoveList) {
      Collection<Grave> graves = this.plugin.getCacheManager().getGraveMap().values();

      for (Grave grave : new ArrayList<>(graves)) {
         long remainingTime = grave.getTimeAliveRemaining();
         if (remainingTime != -1L && !grave.isAbandoned()) {
            this.plugin.debugMessage("Checking grave: " + grave.getUUID() + " with remaining time: " + formatMillis(remainingTime), 1);
            if (remainingTime <= 0L) {
               this.handleGraveTimeout(grave, graveRemoveList);
            } else if (grave.getProtection()) {
               long protectionRemaining = grave.getTimeProtectionRemaining();
               if (protectionRemaining != -1L && protectionRemaining <= 0L) {
                  this.toggleGraveProtection(grave);
               }
            }
         } else {
            grave.setTimeAliveRemaining(-1L);
         }
      }
   }

   public static String formatMillis(long millis) {
      long totalSeconds = millis / 1000L;
      if (totalSeconds < 0L) {
         return "Unlimited";
      } else {
         long hours = totalSeconds / 3600L;
         long minutes = totalSeconds % 3600L / 60L;
         long seconds = totalSeconds % 60L;
         StringBuilder sb = new StringBuilder();
         if (hours > 0L) {
            sb.append(hours).append("h ");
         }

         if (minutes > 0L) {
            sb.append(minutes).append("m ");
         }

         sb.append(seconds).append("s");
         return sb.toString().trim();
      }
   }

   private void handleGraveTimeout(Grave grave, List<Grave> graveRemoveList) {
      long remaining = grave.getTimeAliveRemaining();
      if (remaining != -1L) {
         this.plugin.debugMessage("GraveTimeout check for " + grave.getUUID(), 1);
         if (remaining <= 0L) {
            boolean dropOnTimeout = this.plugin.getConfigManager().getConfigSection("drop.timeout", grave).getBoolean("drop.timeout", true);
            boolean abandonEnabled = this.plugin.getConfigManager().getConfigSection("drop.abandon", grave).getBoolean("drop.abandon", false);
            GraveTimeoutEvent tevModern = new GraveTimeoutEvent(grave);
            this.plugin.getServer().getPluginManager().callEvent(tevModern);
            com.ranull.graves.event.GraveTimeoutEvent tevLegacy = new com.ranull.graves.event.GraveTimeoutEvent(grave);
            this.plugin.getServer().getPluginManager().callEvent(tevLegacy);
            if (!tevModern.isCancelled() && !tevModern.isAddon() && !tevLegacy.isCancelled() && !tevLegacy.isAddon()) {
               Location loc = tevModern.hasLocation() ? tevModern.getLocation() : tevLegacy.getLocation();
               if (loc != null && loc.getWorld() != null) {
                  Location anchor = loc.clone();
                  Runnable logic = () -> {
                     World world = loc.getWorld();
                     if (world == null) {
                        this.plugin.debugMessage("World became null for grave " + grave.getUUID(), 2);
                     } else if (!dropOnTimeout && abandonEnabled) {
                        boolean cancelled = this.handleGraveAbandoned(grave, loc);
                        if (cancelled) {
                           GraveExpiredEvent expired = new GraveExpiredEvent(grave);
                           this.plugin.getServer().getPluginManager().callEvent(expired);
                           if (!expired.isCancelled() && !expired.isAddon()) {
                              this.plugin.debugMessage("Expired: removing grave (drop handled by removeGrave): " + grave.getUUID(), 2);
                              this.sendPlayerMessage(grave, "message.timeout", loc);
                              synchronized (graveRemoveList) {
                                 graveRemoveList.add(grave);
                              }

                              this.removeGrave(grave);
                           } else {
                              this.plugin.debugMessage("Expired cancelled — grave lives forever: " + grave.getUUID(), 2);
                              grave.setTimeAliveRemaining(-1L);
                           }
                        }
                     } else if (dropOnTimeout) {
                        if (abandonEnabled) {
                           boolean cancelled = this.handleGraveAbandoned(grave, loc);
                           if (cancelled) {
                              this.plugin.debugMessage("Timeout: removing grave " + grave.getUUID(), 2);
                              this.dropGraveItems(loc, grave);
                              this.dropGraveExperience(loc, grave);
                              this.sendPlayerMessage(grave, "message.timeout", loc);
                              synchronized (graveRemoveList) {
                                 graveRemoveList.add(grave);
                              }

                              this.removeGrave(grave);
                           }
                        } else {
                           this.plugin.debugMessage("Timeout: removing grave (drop handled by removeGrave): " + grave.getUUID(), 2);
                           this.dropGraveItems(loc, grave);
                           this.dropGraveExperience(loc, grave);
                           this.sendPlayerMessage(grave, "message.timeout", loc);
                           synchronized (graveRemoveList) {
                              graveRemoveList.add(grave);
                           }

                           this.removeGrave(grave);
                        }
                     } else {
                        GraveExpiredEvent expired = new GraveExpiredEvent(grave);
                        this.plugin.getServer().getPluginManager().callEvent(expired);
                        if (!expired.isCancelled() && !expired.isAddon()) {
                           this.plugin.debugMessage("Expired: removing grave (drop handled by removeGrave): " + grave.getUUID(), 2);
                           this.sendPlayerMessage(grave, "message.timeout", loc);
                           synchronized (graveRemoveList) {
                              graveRemoveList.add(grave);
                           }

                           this.removeGrave(grave);
                        } else {
                           this.plugin.debugMessage("Expired cancelled — grave lives forever: " + grave.getUUID(), 2);
                           grave.setTimeAliveRemaining(-1L);
                        }
                     }
                  };
                  this.plugin.getChunkManager().ensureLoadedAndExecute(anchor, loc, true, true, logic);
               } else {
                  this.plugin.debugMessage("Invalid timeout location for " + grave.getUUID(), 2);
               }
            } else {
               this.plugin.debugMessage("GraveTimeoutEvent cancelled → infinite life for " + grave.getUUID(), 2);
               grave.setTimeAliveRemaining(-1L);
            }
         }
      }
   }

   private boolean handleGraveAbandoned(Grave grave, Location loc) {
      GraveAbandonedEvent aevModern = new GraveAbandonedEvent(grave);
      this.plugin.getServer().getPluginManager().callEvent(aevModern);
      com.ranull.graves.event.GraveAbandonedEvent aevLegacy = new com.ranull.graves.event.GraveAbandonedEvent(grave);
      this.plugin.getServer().getPluginManager().callEvent(aevLegacy);
      if (!aevModern.isCancelled() && !aevModern.isAddon() && !aevLegacy.isCancelled() && !aevLegacy.isAddon()) {
         Location abandonLoc = aevModern.hasLocation() ? aevModern.getLocation() : aevLegacy.getLocation();
         this.sendPlayerMessage(grave, "message.grave-abandoned", abandonLoc != null ? abandonLoc : loc);
         this.abandonGrave(grave);
         return false;
      } else {
         this.plugin.debugMessage("Abandon event cancelled for " + grave.getUUID(), 2);
         this.sendPlayerMessage(grave, "message.timeout", loc);
         return true;
      }
   }

   private void sendPlayerMessage(Grave grave, String messageKey, Location location) {
      if (grave.getOwnerType() == EntityType.PLAYER && grave.getOwnerUUID() != null) {
         Player player = this.plugin.getServer().getPlayer(grave.getOwnerUUID());
         if (player != null && player.isOnline()) {
            this.plugin.getEntityManager().sendMessage(messageKey, player, location, grave);
         }
      }
   }

   private void processChunks(List<EntityData> entityDataRemoveList, List<BlockData> blockDataRemoveList) {
      for (ChunkData chunkData : this.plugin.getCacheManager().getChunkMap().values()) {
         Location anchor = new Location(chunkData.getWorld(), chunkData.getX() << 4, 0.0, chunkData.getZ() << 4);
         if (this.plugin.getVersionManager().isFolia()) {
            this.plugin.getSchedulerManager().execute(anchor, () -> {
               if (chunkData.isLoaded()) {
                  this.processEntityData(chunkData, entityDataRemoveList, anchor);
                  this.processBlockData(chunkData, blockDataRemoveList);
               }
            });
         } else {
            if (!chunkData.isLoaded()) {
               return;
            }

            this.processEntityData(chunkData, entityDataRemoveList, anchor);
            this.processBlockData(chunkData, blockDataRemoveList);
         }
      }
   }

   private void processHologramDataTick(List<EntityData> entityDataRemoveList) {
      Collection<ChunkData> chunks = this.plugin.getCacheManager().getChunkMap().values();
      Iterator var3 = chunks.iterator();

      while (true) {
         Collection<EntityData> snapshot;
         while (true) {
            if (!var3.hasNext()) {
               return;
            }

            ChunkData chunkData = (ChunkData)var3.next();
            if (chunkData != null && chunkData.isLoaded()) {
               try {
                  snapshot = new ArrayList<>(chunkData.getEntityDataMap().values());
                  break;
               } catch (Throwable var10) {
               }
            }
         }

         for (EntityData entityData : snapshot) {
            if (entityData instanceof HologramData hologramData) {
               UUID graveId = hologramData.getUUIDGrave();
               if (graveId != null) {
                  if (!this.plugin.getCacheManager().getGraveMap().containsKey(graveId)) {
                     entityDataRemoveList.add(hologramData);
                  } else {
                     this.processHologramData(hologramData, entityDataRemoveList);
                  }
               }
            }
         }
      }
   }

   private void removeExpiredElements(List<Grave> graveRemoveList, List<EntityData> entityDataRemoveList, List<BlockData> blockDataRemoveList) {
      List<Grave> gravesSnapshot = new ArrayList<>(graveRemoveList);
      List<EntityData> entitySnapshot = new ArrayList<>(entityDataRemoveList);
      List<BlockData> blockSnapshot = new ArrayList<>(blockDataRemoveList);
      graveRemoveList.clear();
      entityDataRemoveList.clear();
      blockDataRemoveList.clear();

      for (Grave grave : gravesSnapshot) {
         this.plugin.debugMessage("Removing grave: " + grave.getUUID(), 2);
         Location graveLoc = null;

         try {
            graveLoc = grave.getLocationDeath();
         } catch (Throwable var13) {
         }

         if (graveLoc != null && graveLoc.getWorld() != null) {
            Location anchor = new Location(graveLoc.getWorld(), graveLoc.getX(), graveLoc.getY(), graveLoc.getZ());
            this.plugin.getSchedulerManager().execute(anchor, () -> this.removeGrave(grave));
         } else {
            this.plugin.getSchedulerManager().runTask(() -> this.removeGrave(grave));
         }
      }

      for (EntityData entityData : entitySnapshot) {
         if (entityData == null) {
            this.plugin.debugMessage("Attempted to remove null EntityData", 2);
         } else {
            Location entityLoc = null;

            try {
               entityLoc = entityData.getLocation();
            } catch (Throwable var12) {
            }

            if (entityLoc != null && entityLoc.getWorld() != null) {
               Location anchor = new Location(entityLoc.getWorld(), entityLoc.getX(), entityLoc.getY(), entityLoc.getZ());
               this.plugin.getSchedulerManager().execute(anchor, () -> this.removeEntityData(entityData));
            } else {
               this.plugin.getSchedulerManager().runTask(() -> this.removeEntityData(entityData));
            }
         }
      }

      for (BlockData blockData : blockSnapshot) {
         Location blockLoc = null;

         try {
            blockLoc = blockData.getLocation();
         } catch (Throwable var11) {
         }

         if (blockLoc != null && blockLoc.getWorld() != null) {
            Location anchor = new Location(blockLoc.getWorld(), blockLoc.getX(), blockLoc.getY(), blockLoc.getZ());
            this.plugin.getSchedulerManager().execute(anchor, () -> this.plugin.getBlockManager().removeBlock(blockData));
         } else {
            this.plugin.getSchedulerManager().runTask(() -> this.plugin.getBlockManager().removeBlock(blockData));
         }
      }

      if (this.plugin.getVersionManager().isFolia()) {
         this.plugin.getSchedulerManager().runTask(() -> this.plugin.getGUIManager().refreshMenusSafely());
      } else {
         this.plugin.getSchedulerManager().runTask(() -> this.plugin.getGUIManager().refreshMenus());
      }
   }

   private void processEntityData(ChunkData chunkData, List<EntityData> entityDataRemoveList, Location location) {
      try {
         Collection<EntityData> values = chunkData.getEntityDataMap().values();

         for (EntityData entityData : new ArrayList<>(values)) {
            if (entityData == null) {
               this.plugin
                  .debugMessage("Encountered null EntityData while processing chunk at coordinates: (" + chunkData.getX() + ", " + chunkData.getZ() + ").", 2);
            } else {
               UUID graveId = entityData.getUUIDGrave();
               boolean hasGrave = graveId != null && this.plugin.getCacheManager().getGraveMap().containsKey(graveId);
               if (!hasGrave) {
                  entityDataRemoveList.add(entityData);
               }
            }
         }
      } catch (ArrayIndexOutOfBoundsException var9) {
      }
   }

   private void processHologramData(HologramData hologramData, List<EntityData> entityDataRemoveList) {
      UUID graveUuid = hologramData.getUUIDGrave();
      if (graveUuid != null) {
         Grave grave = this.plugin.getCacheManager().getGraveMap().get(graveUuid);
         if (grave != null) {
            Entity target = this.plugin.getServer().getEntity(hologramData.getUUIDEntity());
            if (target == null) {
               this.plugin
                  .debugMessage(
                     "Failed to update target for "
                        + hologramData.getUUIDEntity()
                        + " for grave "
                        + grave.getUUID()
                        + ". Holograms will not update or may linger in the world.",
                     2
                  );
            } else {
               this.plugin
                  .getSchedulerManager()
                  .execute(
                     target,
                     () -> {
                        try {
                           if (!target.isValid()) {
                              synchronized (entityDataRemoveList) {
                                 entityDataRemoveList.add(hologramData);
                                 return;
                              }
                           }

                           Grave g = this.plugin.getCacheManager().getGraveMap().get(graveUuid);
                           if (g == null) {
                              synchronized (entityDataRemoveList) {
                                 entityDataRemoveList.add(hologramData);
                                 return;
                              }
                           }

                           Location death = g.getLocationDeath();
                           if (death == null || death.getWorld() == null) {
                              return;
                           }

                           double offsetX = this.plugin.getConfigManager().getConfigSection("hologram.offset.x", g).getDouble("hologram.offset.x");
                           double offsetY = this.plugin.getConfigManager().getConfigSection("hologram.offset.y", g).getDouble("hologram.offset.y");
                           double offsetZ = this.plugin.getConfigManager().getConfigSection("hologram.offset.z", g).getDouble("hologram.offset.z");
                           boolean marker = this.plugin.getConfigManager().getConfigSection("hologram.marker", g).getBoolean("hologram.marker");
                           double lineHeight = this.plugin
                              .getConfigManager()
                              .getConfigSection("hologram.height-line", g)
                              .getDouble("hologram.height-line", 0.28);
                           List<String> cfgLines = new ArrayList<>(
                              this.plugin.getConfigManager().getConfigSection("hologram.line", g).getStringList("hologram.line")
                           );
                           if (cfgLines.isEmpty()) {
                              return;
                           }

                           Location base = LocationUtil.roundLocation(death).add(offsetX + 0.5, offsetY + (marker ? 0.49 : -0.49), offsetZ + 0.5);
                           if (this.plugin.getVersionManager().isHasTextDisplays()) {
                              this.plugin.getTextDisplayManager().updateTextDisplay(target, cfgLines, lineHeight, base, g);
                              return;
                           }

                           List<String> lineListReversed = new ArrayList<>(cfgLines);
                           Collections.reverse(lineListReversed);
                           int lineIndex = hologramData.getLine();
                           if (lineIndex < 0 || lineIndex >= lineListReversed.size()) {
                              synchronized (entityDataRemoveList) {
                                 entityDataRemoveList.add(hologramData);
                                 return;
                              }
                           }

                           Location expectedLineLoc = base.clone().add(0.0, (lineIndex + 1) * lineHeight, 0.0);

                           try {
                              Location cur = target.getLocation();
                              if (cur.getWorld() != null && expectedLineLoc.getWorld() != null && cur.getWorld().equals(expectedLineLoc.getWorld())) {
                                 double dx = cur.getX() - expectedLineLoc.getX();
                                 double dy = cur.getY() - expectedLineLoc.getY();
                                 double dz = cur.getZ() - expectedLineLoc.getZ();
                                 if (dx * dx + dy * dy + dz * dz > 1.0E-4) {
                                    target.teleport(expectedLineLoc);
                                 }
                              }
                           } catch (Throwable var32) {
                              this.plugin
                                 .debugMessage(
                                    "Failed to get Target Location for grave "
                                       + g.getUUID()
                                       + ". Holograms will not update. \n"
                                       + Arrays.toString((Object[])var32.getStackTrace()),
                                    2
                                 );
                           }

                           String lineTextRaw = lineListReversed.get(lineIndex);
                           String parsed = StringUtil.parseString(lineTextRaw, expectedLineLoc, g, this.plugin);
                           if (this.plugin.getIntegrationManager().hasMiniMessage()) {
                              target.setCustomName(MiniMessage.parseString(parsed));
                           } else {
                              target.setCustomName(parsed);
                           }
                        } catch (Throwable var34) {
                           synchronized (entityDataRemoveList) {
                              entityDataRemoveList.add(hologramData);
                           }
                        }
                     }
                  );
            }
         }
      }
   }

   private void processBlockData(ChunkData chunkData, List<BlockData> blockDataRemoveList) {
      try {
         Collection<BlockData> values = chunkData.getBlockDataMap().values();

         for (BlockData blockData : new ArrayList<>(values)) {
            if (blockData != null) {
               Location loc = blockData.getLocation();
               if (loc != null && loc.getWorld() != null) {
                  UUID graveId = blockData.getGraveUUID();
                  boolean hasGrave = graveId != null && this.plugin.getCacheManager().getGraveMap().containsKey(graveId);
                  if (hasGrave) {
                     Grave grave = this.plugin.getCacheManager().getGraveMap().get(graveId);
                     this.graveParticle(loc, grave);
                  } else {
                     blockDataRemoveList.add(blockData);
                  }
               } else {
                  blockDataRemoveList.add(blockData);
               }
            }
         }
      } catch (ArrayIndexOutOfBoundsException var10) {
      }
   }

   public void unload() {
      for (Player player : this.plugin.getServer().getOnlinePlayers()) {
         InventoryView view = player.getOpenInventory();
         if (view != null) {
            Inventory topInventory = CompatibilityInventoryView.getTopInventory(view);
            if (topInventory != null) {
               InventoryHolder inventoryHolder = topInventory.getHolder();
               if (inventoryHolder != null) {
                  try {
                     if (inventoryHolder instanceof Grave || inventoryHolder instanceof GraveList || inventoryHolder instanceof GraveMenu) {
                        player.closeInventory();
                     }
                  } catch (Exception var7) {
                  }
               }
            }
         }
      }
   }

   public void toggleGraveProtection(Grave grave) {
      boolean currentProtection = grave.getProtection();
      long protectionRemaining = grave.getTimeProtectionRemaining();
      grave.setProtection(!currentProtection);
      this.plugin.getDataManager().updateGrave(grave, "protection", grave.getProtection() ? 1 : 0);
      if (protectionRemaining == -1L) {
         this.plugin.debugMessage("Grave " + grave.getUUID() + " has infinite protection, skipping protection remaining handling.", 2);
      } else {
         if (currentProtection) {
            GraveProtectionExpiredEvent modern = new GraveProtectionExpiredEvent(grave);
            this.plugin.getServer().getPluginManager().callEvent(modern);
            com.ranull.graves.event.GraveProtectionExpiredEvent legacy = new com.ranull.graves.event.GraveProtectionExpiredEvent(grave);
            this.plugin.getServer().getPluginManager().callEvent(legacy);
            boolean cancelled = modern.isCancelled() || legacy.isCancelled();
            boolean addon = modern.isAddon() || legacy.isAddon();
            if (cancelled && !addon) {
               grave.setProtection(true);
               this.plugin.debugMessage("GraveProtectionExpiredEvent called for grave: " + grave.getUUID(), 2);
               this.plugin.getDataManager().updateGrave(grave, "protection", 1);
               grave.setTimeProtection(-1L);
            } else if (!cancelled && !addon) {
               this.plugin.debugMessage("Grave protection expired for grave: " + grave.getUUID(), 1);
               this.plugin.getDataManager().updateGrave(grave, "protection", grave.getProtection() ? 1 : 0);
            }
         }
      }
   }

   public void abandonGrave(Grave grave) {
      grave.setAbandoned(true);
      if (this.plugin.getConfigManager().getConfigSection("drop.abandon-lose-experience", grave).getBoolean("drop.abandon-lose-experience", true)) {
         grave.setExperience(0);
      }

      grave.setTimeProtection(0L);
      grave.setTimeCreation(System.currentTimeMillis());
      grave.setTimeAlive(-1L);
      grave.setTimeAliveRemaining(-1L);
      grave.setOwnerName("Abandoned");
      grave.setOwnerDisplayName("Abandoned");
      grave.setOwnerTexture(
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTYxZjFmY2Q0MmY0OGNhNTFmOWRhN2M1NWI3MmYzNWE4MjZlNzViNmEwMjA0OGExZGVhNWQ3MTE5YmM5Y2Q2OSJ9fX0="
      );
      this.plugin.getDataManager().updateGrave(grave, "owner_name", grave.getOwnerName());
      this.plugin.getDataManager().updateGrave(grave, "experience", grave.getExperience());
      this.plugin.getDataManager().updateGrave(grave, "owner_name_display", grave.getOwnerDisplayName());
      this.plugin.getDataManager().updateGrave(grave, "is_abandoned", grave.isAbandoned() ? 1 : 0);
   }

   public void graveParticle(Location location, Grave grave) {
      if (this.plugin.getVersionManager().hasParticle()
         && location != null
         && location.getWorld() != null
         && this.plugin.getConfigManager().getConfigSection("particle.enabled", grave).getBoolean("particle.enabled")) {
         Particle particle = this.plugin.getVersionManager().getParticleForVersion("REDSTONE");
         String configuredType = this.plugin.getConfigManager().getConfigSection("particle.type", grave).getString("particle.type");
         if (configuredType != null && !configuredType.isEmpty()) {
            try {
               particle = this.plugin.getVersionManager().getParticleForVersion(configuredType);
            } catch (IllegalArgumentException var14) {
               this.plugin.debugMessage(configuredType + " is not a Particle ENUM", 1);
               this.plugin.getLogger().severe("The Particle ENUM/INSTANCE " + configuredType + " is not valid. Update \"particle.type\" in grave.yml");
               return;
            }
         }

         int count = this.plugin.getConfigManager().getConfigSection("particle.count", grave).getInt("particle.count");
         double offX = this.plugin.getConfigManager().getConfigSection("particle.offset.x", grave).getDouble("particle.offset.x");
         double offY = this.plugin.getConfigManager().getConfigSection("particle.offset.y", grave).getDouble("particle.offset.y");
         double offZ = this.plugin.getConfigManager().getConfigSection("particle.offset.z", grave).getDouble("particle.offset.z");
         Location anchor = location.clone().add(offX + 0.5, offY + 0.5, offZ + 0.5);
         if (anchor.getWorld() != null) {
            Particle finalParticle = particle;
            this.plugin
               .getSchedulerManager()
               .execute(
                  anchor,
                  () -> {
                     try {
                        World world = anchor.getWorld();
                        if (world == null) {
                           return;
                        }

                        String s0$ = finalParticle.name();
                        switch (s0$) {
                           case "BLOCK":
                           case "BLOCK_CRUMBLE":
                           case "BLOCK_MARKER":
                           case "DUST_PILLAR":
                           case "FALLING_DUST":
                              BlockData gravesBD = this.plugin.getParticleManager().getGravesBlockData(grave, anchor);

                              try {
                                 world.spawnParticle(finalParticle, anchor, count, gravesBD);
                              } catch (IllegalArgumentException var18) {
                                 org.bukkit.block.data.BlockData bukkitBD = this.plugin.getParticleManager().toBukkitBlockData(gravesBD);
                                 if (bukkitBD != null) {
                                    world.spawnParticle(finalParticle, anchor, count, bukkitBD);
                                 } else {
                                    world.spawnParticle(finalParticle, anchor, count);
                                 }
                              }
                              break;
                           case "DUST":
                           case "REDSTONE": {
                              int sizeInt = this.plugin.getConfigManager().getConfigSection("particle.dust-size", grave).getInt("particle.dust-size");
                              float size = sizeInt;
                              Color color = this.plugin
                                 .getParticleManager()
                                 .safeColor(
                                    this.plugin.getConfigManager().getConfigSection("particle.dust-color", grave).getString("particle.dust-color", "RED"),
                                    Color.RED
                                 );

                              try {
                                 world.spawnParticle(finalParticle, anchor, count, new DustOptions(color, size));
                              } catch (IllegalArgumentException var16) {
                                 world.spawnParticle(finalParticle, anchor, count, 1);
                              }
                              break;
                           }
                           case "DUST_COLOR_TRANSITION": {
                              float size = (float)this.plugin
                                 .getConfigManager()
                                 .getConfigSection("particle.dust-size", grave)
                                 .getDouble("particle.dust-size", 1.0);
                              Color from = this.plugin
                                 .getParticleManager()
                                 .safeColor(
                                    this.plugin
                                       .getConfigManager()
                                       .getConfigSection("particle.dust.from-color", grave)
                                       .getString("particle.dust.from-color", "WHITE"),
                                    Color.WHITE
                                 );
                              Color to = this.plugin
                                 .getParticleManager()
                                 .safeColor(
                                    this.plugin.getConfigManager().getConfigSection("particle.dust.to-color", grave).getString("particle.dust.to-color", "RED"),
                                    Color.RED
                                 );
                              DustTransition transition = new DustTransition(from, to, size);
                              world.spawnParticle(finalParticle, anchor, count, transition);
                              break;
                           }
                           case "ENTITY_EFFECT":
                           case "TINTED_LEAVES":
                              Color tint = this.plugin
                                 .getParticleManager()
                                 .safeColor(
                                    this.plugin.getConfigManager().getConfigSection("particle.color", grave).getString("particle.color", "WHITE"), Color.WHITE
                                 );

                              try {
                                 world.spawnParticle(finalParticle, anchor, count, tint);
                              } catch (IllegalArgumentException var15) {
                                 world.spawnParticle(finalParticle, anchor, count);
                              }
                              break;
                           case "ITEM":
                              ItemStack stack = this.plugin.getParticleManager().parseItemStack(grave);
                              if (stack != null) {
                                 world.spawnParticle(finalParticle, anchor, count, stack);
                              } else {
                                 world.spawnParticle(finalParticle, anchor, count);
                              }
                              break;
                           case "SCULK_CHARGE":
                              float charge = (float)this.plugin
                                 .getConfigManager()
                                 .getConfigSection("particle.sculk-charge", grave)
                                 .getDouble("particle.sculk-charge", 1.0);

                              try {
                                 world.spawnParticle(finalParticle, anchor, count, charge);
                              } catch (IllegalArgumentException var14x) {
                                 world.spawnParticle(finalParticle, anchor, count);
                              }
                              break;
                           case "SHRIEK":
                              int delay = this.plugin.getConfigManager().getConfigSection("particle.shriek-delay", grave).getInt("particle.shriek-delay", 1);

                              try {
                                 world.spawnParticle(finalParticle, anchor, count, delay);
                              } catch (IllegalArgumentException var13x) {
                                 world.spawnParticle(finalParticle, anchor, count, 1);
                              }
                              break;
                           case "TRAIL":
                              Object trail = this.plugin.getParticleManager().parseTrailData(this.plugin, grave);
                              if (trail != null) {
                                 try {
                                    world.spawnParticle(finalParticle, anchor, count, trail);
                                    break;
                                 } catch (IllegalArgumentException var17) {
                                    this.plugin.debugMessage("TRAIL data not supported: " + var17.getMessage(), 2);
                                 }
                              }

                              world.spawnParticle(finalParticle, anchor, count);
                              break;
                           case "VIBRATION":
                              String mode = this.plugin
                                 .getConfigManager()
                                 .getConfigSection("particle.vibration.mode", grave)
                                 .getString("particle.vibration.mode", "single")
                                 .toLowerCase(Locale.ROOT);
                              if ("bounce".equals(mode)) {
                                 this.plugin.getParticleManager().spawnVibrationBounce(this.plugin, anchor, grave, finalParticle, count);
                              } else {
                                 Vibration vib = this.plugin.getParticleManager().buildVibrationSingle(this.plugin, anchor, grave);
                                 if (vib != null) {
                                    world.spawnParticle(finalParticle, anchor, count, vib);
                                 } else {
                                    world.spawnParticle(finalParticle, anchor, count);
                                 }
                              }
                              break;
                           default:
                              try {
                                 world.spawnParticle(finalParticle, anchor, count);
                              } catch (IllegalArgumentException var12x) {
                                 world.spawnParticle(finalParticle, anchor, count, 0.0, 0.0, 0.0, 0.0);
                              }
                        }
                     } catch (Throwable var19) {
                        this.plugin.debugMessage("Particle spawn failed for " + finalParticle + ": " + var19.getMessage(), 2);
                     }
                  }
               );
         }
      }
   }

   public void removeOldestGrave(LivingEntity livingEntity) {
      if (livingEntity != null) {
         Grave toDel = this.plugin.getCacheManager().getOldestGrave(livingEntity.getUniqueId());
         if (toDel != null) {
            Location deathLoc = toDel.getLocationDeath();
            if (deathLoc != null && deathLoc.getWorld() != null) {
               this.plugin.getChunkManager().ensureLoadedAndExecute(deathLoc, deathLoc, true, false, () -> {
                  if (this.plugin.getConfigManager().getConfigSection("drop.timeout", toDel).getBoolean("drop.timeout", true)) {
                     this.dropGraveItems(deathLoc, toDel);
                     this.dropGraveExperience(deathLoc, toDel);
                  }

                  this.removeGrave(toDel);
               });
            } else {
               this.removeGrave(toDel);
            }
         }
      }
   }

   public void dropAllFromGrave(Grave grave) {
      Location loc = grave.getLocationDeath();
      World w = loc.getWorld();
      List<ItemStack> items = grave.getInventoryItemStack();
      if (items != null) {
         for (ItemStack is : items) {
            if (is != null && is.getType() != Material.AIR && is.getAmount() > 0) {
               w.dropItemNaturally(loc, is.clone());
            }
         }

         try {
            items.clear();
         } catch (Throwable var9) {
         }
      }

      Map<EquipmentSlot, ItemStack> equip = grave.getEquipmentMap();
      if (equip != null) {
         for (ItemStack isx : equip.values()) {
            if (isx != null && isx.getType() != Material.AIR && isx.getAmount() > 0) {
               w.dropItemNaturally(loc, isx.clone());
            }
         }

         try {
            equip.clear();
         } catch (Throwable var8) {
         }
      }
   }

   public void removeGrave(Grave grave) {
      if (grave != null) {
         this.plugin.debugMessage("Starting removal of grave: " + grave.getUUID(), 1);
         Location anchor = null;

         try {
            anchor = grave.getLocationDeath();
         } catch (Throwable var5) {
         }

         Location finalAnchor = anchor;
         Runnable work = () -> {
            try {
               try {
                  this.closeGrave(grave);
               } catch (Throwable var27) {
               }

               String providerId = null;

               try {
                  providerId = grave.getProviderId();
               } catch (Throwable var26) {
               }

               boolean removedByProvider = false;
               if (providerId != null && !providerId.isBlank()) {
                  GraveProvider matched = RegisterGraveProviders.getById(providerId);
                  if (matched != null) {
                     try {
                        this.removeWithProvider(finalAnchor, grave, matched);
                        removedByProvider = true;
                     } catch (Throwable var25) {
                        this.plugin
                           .getLogger()
                           .warning("Provider removal failed for provider_id='" + providerId + "' grave=" + grave.getUUID() + ": " + var25.getMessage());
                     }
                  } else {
                     this.plugin
                        .getLogger()
                        .warning(
                           "provider_id='"
                              + providerId
                              + "' set for grave "
                              + grave.getUUID()
                              + " but no matching GraveProvider is registered. Skipping provider removal."
                        );
                  }
               }

               try {
                  if (this.plugin.getIntegrationManager().hasMultiPaper()) {
                     this.plugin.getIntegrationManager().getMultiPaper().notifyGraveRemoval(grave);
                  }
               } catch (Throwable var24) {
               }

               if (!removedByProvider) {
                  try {
                     if (this.plugin.getIntegrationManager().hasFurnitureLib()) {
                        this.plugin.getIntegrationManager().getFurnitureLib().removeFurniture(grave);
                     }
                  } catch (Throwable var23) {
                  }

                  try {
                     if (this.plugin.getIntegrationManager().hasFurnitureEngine()) {
                        this.plugin.getLogger().warning("You have FurnitureEngine enabled.");
                        this.plugin.getIntegrationManager().getFurnitureEngine().removeFurniture(grave);
                     }
                  } catch (Throwable var22) {
                  }

                  try {
                     if (this.plugin.getIntegrationManager().hasItemsAdder()) {
                        this.plugin.getIntegrationManager().getItemsAdder().removeFurniture(grave);
                     }
                  } catch (Throwable var21) {
                  }

                  try {
                     if (this.plugin.getIntegrationManager().hasOraxen()) {
                        this.plugin.getIntegrationManager().getOraxen().removeFurniture(grave);
                     }
                  } catch (Throwable var20) {
                  }

                  try {
                     if (this.plugin.getIntegrationManager().hasNexo()) {
                        this.plugin.getIntegrationManager().getNexo().removeFurniture(grave);
                     }
                  } catch (Throwable var19) {
                  }

                  try {
                     if (this.plugin.getIntegrationManager().hasPlayerNPC()) {
                        this.plugin.getIntegrationManager().getPlayerNPC().removeCorpse(grave);
                     }
                  } catch (Throwable var18) {
                  }

                  try {
                     if (this.plugin.getIntegrationManager().hasMannequins()) {
                        this.plugin.getIntegrationManager().getMannequins().removeCorpse(grave);
                     }
                  } catch (Throwable var17) {
                  }

                  try {
                     if (this.plugin.getIntegrationManager().hasFancyNpcs()) {
                        this.plugin.getIntegrationManager().getFancyNpcs().removeCorpse(grave);
                     }
                  } catch (Throwable var16) {
                  }
               }

               try {
                  this.plugin.getBlockManager().removeBlock(grave);
               } catch (Throwable var15) {
               }

               try {
                  this.plugin.getHologramManager().removeHologram(grave);
               } catch (Throwable var14) {
               }

               try {
                  this.plugin.getEntityManager().removeEntity(grave);
               } catch (Throwable var13) {
               }

               try {
                  if (finalAnchor != null && finalAnchor.getWorld() != null) {
                     String matName = this.plugin.getConfigManager().getConfigSection("block.material", grave).getString("block.material", "PLAYER_HEAD");
                     Material configured = null;

                     try {
                        configured = Material.valueOf(matName);
                     } catch (Throwable var11) {
                     }

                     if (configured != null && finalAnchor.getBlock().getType() == configured) {
                        finalAnchor.getBlock().setType(Material.AIR);
                     }
                  }
               } catch (Throwable var12) {
               }

               try {
                  this.plugin.getDataManager().removeGrave(grave);
               } catch (Throwable var10) {
                  this.plugin.getLogger().warning("DataManager removeGrave failed for " + grave.getUUID() + ": " + var10.getMessage());
               }

               try {
                  this.plugin.getCacheManager().getGraveMap().remove(grave.getUUID());
                  this.plugin.debugMessage("Grave " + grave.getUUID() + " removed from cache", 1);
               } catch (Throwable var9) {
               }

               try {
                  if (this.plugin.getConfigManager().getConfigSection("grave.check-missing-graves", grave).getBoolean("grave.check-missing-graves", false)) {
                     this.knownGraves.remove(grave.getUUID());
                  }
               } catch (Throwable var8) {
               }
            } catch (Throwable var28) {
               this.plugin.getLogger().warning("Error while removing grave " + grave.getUUID() + ": " + var28.getMessage());
            }
         };
         if (anchor != null && anchor.getWorld() != null) {
            this.plugin.getSchedulerManager().execute(anchor, work);
         } else {
            this.plugin.getSchedulerManager().runTask(work);
         }
      }
   }

   private void removeWithProvider(Location anchor, Grave grave, GraveProvider p) {
      try {
         p.remove(grave);
         boolean stillPlaced = false;

         try {
            stillPlaced = p.isPlaced(grave);
         } catch (Throwable var6) {
            this.plugin
               .getLogger()
               .warning("[CustomGraveProvider " + p.id() + " (order=" + p.order() + ")] isPlaced() threw after remove(): " + var6.getMessage());
            return;
         }

         if (!stillPlaced) {
            this.plugin.debugMessage("[CustomGraveProvider " + p.id() + " (order=" + p.order() + ")] removed successfully.", 1);
            return;
         }

         this.plugin
            .debugMessage(
               "[CustomGraveProvider "
                  + p.id()
                  + " (order="
                  + p.order()
                  + ")] remove() returned without error, but isPlaced=true at "
                  + anchor
                  + ". Assuming removal is pending or isPlaced() is conservative.",
               2
            );
      } catch (Throwable var7) {
         this.plugin.getLogger().warning("[CustomGraveProvider " + p.id() + " (order=" + p.order() + ")] remove() failed: " + var7.getMessage());
         this.plugin.logStackTrace(var7);
      }
   }

   public void removeEntityData(EntityData entityData) {
      if (entityData != null && entityData.getType() != null) {
         Location anchor = null;

         try {
            anchor = entityData.getLocation();
         } catch (Throwable var4) {
         }

         Runnable work = () -> {
            switch (entityData.getType()) {
               case FURNITURELIB:
                  this.plugin.getIntegrationManager().getFurnitureLib().removeEntityData(entityData);
                  break;
               case FURNITUREENGINE:
                  this.plugin.getIntegrationManager().getFurnitureEngine().removeEntityData(entityData);
                  break;
               case ITEMSADDER:
                  this.plugin.getIntegrationManager().getItemsAdder().removeEntityData(entityData);
                  break;
               case ORAXEN:
                  this.plugin.getIntegrationManager().getOraxen().removeEntityData(entityData);
                  break;
               case NEXO:
                  this.plugin.getIntegrationManager().getNexo().removeEntityData(entityData);
                  break;
               case PLAYERNPC:
                  this.plugin.getIntegrationManager().getPlayerNPC().removeEntityData(entityData);
                  break;
               case CRAFTENGINE:
                  this.plugin.getIntegrationManager().getCraftEngine().removeFurniture(entityData);
                  break;
               case CUSTOM:
                  UUID graveId = null;

                  try {
                     graveId = entityData.getUUIDGrave();
                  } catch (Throwable var9) {
                  }

                  String providerId = null;
                  if (graveId != null) {
                     Grave g = this.plugin.getCacheManager().getGraveMap().get(graveId);
                     if (g != null) {
                        try {
                           providerId = g.getProviderId();
                        } catch (Throwable var8) {
                        }
                     }
                  }

                  if (providerId != null && !providerId.isBlank()) {
                     GraveProvider p = RegisterGraveProviders.getById(providerId);
                     if (p != null) {
                        try {
                           if (p.supports(entityData) && p.removeEntityData(entityData)) {
                              return;
                           }
                        } catch (Throwable var7) {
                           this.plugin.getLogger().warning("[CustomGraveProvider " + providerId + "] removeEntityData() failed: " + var7.getMessage());
                           this.plugin.logStackTrace(var7);
                        }
                     }
                  } else {
                     List<GraveProvider> providers = RegisterGraveProviders.getAll();
                     if (!providers.isEmpty()) {
                        for (GraveProvider p : providers) {
                           if (p == null) {
                           }
                        }
                     }
                  }
            }

            Grave grave = this.plugin.getCacheManager().getGraveMap().get(entityData.getUUIDGrave());
            this.plugin.getHologramManager().removeHologram(grave);
         };
         if (anchor != null && anchor.getWorld() != null) {
            this.plugin.getSchedulerManager().execute(anchor, work);
         } else {
            this.plugin.getSchedulerManager().runTask(work);
         }
      } else {
         this.plugin.debugMessage("Attempted to remove null entity data. This is not a bug", 3);
      }
   }

   public void closeGrave(Grave grave) {
      Inventory inv = grave.getInventory();
      if (inv != null) {
         List<HumanEntity> inventoryViewers = inv.getViewers();

         for (HumanEntity humanEntity : new ArrayList<>(inventoryViewers)) {
            try {
               inv.getViewers().remove(humanEntity);
            } catch (Throwable var10) {
            }

            try {
               humanEntity.closeInventory();
            } catch (Throwable var9) {
            }

            this.plugin.debugMessage("Closing grave inventory " + grave.getUUID() + " on viewer " + humanEntity.getName(), 1);
         }
      }

      for (Player player : this.plugin.getServer().getOnlinePlayers()) {
         InventoryView view = player.getOpenInventory();
         if (view != null) {
            Inventory topInventory = CompatibilityInventoryView.getTopInventory(view);
            if (topInventory != null
               && topInventory.getHolder() instanceof GraveMenu graveMenu
               && graveMenu.getGrave() != null
               && graveMenu.getGrave().getUUID().equals(grave.getUUID())) {
               player.closeInventory();
               this.plugin.debugMessage("Closing grave inventory " + grave.getUUID() + " on viewer " + player.getName(), 1);
            }
         }
      }
   }

   public Grave createGrave(Entity entity, List<ItemStack> itemStackList) {
      return this.createGrave(entity, itemStackList, this.plugin.getConfigManager().getPermissionList(entity));
   }

   public Grave createGrave(Entity entity, List<ItemStack> itemStackList, List<String> permissionList) {
      Grave grave = new Grave(UUID.randomUUID());
      String entityName = this.plugin.getEntityManager().getEntityName(entity);
      UUID ownerUUID = entity.getUniqueId();
      String ownerName = entityName;
      String displayName = entity.getCustomName();
      if (entity instanceof Player player) {
         displayName = player.getDisplayName();
         if (this.plugin.getIntegrationManager().hasFloodgate()) {
            ownerUUID = this.plugin.getIntegrationManager().getFloodgate().getCorrectUniqueId(player);
            ownerName = this.plugin.getIntegrationManager().getFloodgate().getCorrectUsername(player);
         } else {
            ownerName = player.getName();
         }
      }

      grave.setOwnerType(entity.getType());
      grave.setOwnerName(ownerName);
      grave.setOwnerNameDisplay(displayName);
      grave.setOwnerUUID(ownerUUID);
      Location baseLocation = entity.getLocation().clone();
      String rawTitle = this.plugin.getConfigManager().getConfigSection("gui.grave.title", entity, permissionList).getString("gui.grave.title");
      String parsedTitle = StringUtil.parseString(rawTitle, entity, baseLocation, grave, this.plugin);
      String storageModeStr = this.plugin.getConfigManager().getConfigSection("storage.mode", entity, permissionList).getString("storage.mode");
      this.plugin.getSchedulerManager().execute(baseLocation, () -> {
         try {
            grave.setInventory(this.createGraveInventory(grave, baseLocation, itemStackList, parsedTitle, this.getStorageMode(storageModeStr)));
         } catch (Throwable var7x) {
            this.plugin.getLogger().warning("Failed to create grave inventory for " + grave.getUUID() + ": " + var7x.getMessage());
            this.plugin.logStackTrace(var7x);
         }
      });
      this.plugin.debugMessage("Creating grave " + grave.getUUID() + " for entity " + entityName, 1);
      return grave;
   }

   private void restoreMissingGraves() {
      Map<UUID, Grave> graveMap = this.plugin.getCacheManager().getGraveMap();
      if (graveMap != null && !graveMap.isEmpty()) {
         Iterator var2 = graveMap.values().iterator();

         while (true) {
            Grave grave;
            UUID id;
            Location loc;
            while (true) {
               if (!var2.hasNext()) {
                  return;
               }

               grave = (Grave)var2.next();
               if (grave != null) {
                  id = grave.getUUID();
                  if (!this.knownGraves.contains(id)) {
                     try {
                        loc = grave.getLocationDeath();
                        break;
                     } catch (Throwable var10) {
                        this.plugin.debugMessage("Failed to get location for grave " + id + ": " + var10.getMessage(), 2);
                     }
                  }
               }
            }

            if (loc != null && loc.getWorld() != null) {
               boolean placed;
               try {
                  placed = this.isGravePlaced(grave);
               } catch (Throwable var9) {
                  this.plugin.debugMessage("isGravePlaced threw for grave " + id + ": " + var9.getMessage(), 2);
                  placed = false;
               }

               if (!placed) {
                  this.plugin.debugMessage("Grave " + id + " missing from world. Scheduling placement.", 1);
                  this.plugin.getSchedulerManager().execute(loc, () -> {
                     try {
                        if (this.isGravePlaced(grave)) {
                           return;
                        }

                        this.plugin.getGraveManager().placeGrave(loc, grave);
                        this.knownGraves.add(id);
                     } catch (Throwable var5x) {
                        this.plugin.getLogger().warning("Failed to place grave " + id + ": " + var5x.getMessage());
                        this.plugin.logStackTrace(var5x);
                     }
                  });
               } else {
                  this.knownGraves.add(id);
               }
            } else {
               this.plugin.debugMessage("Cannot restore grave " + id + ": invalid location.", 2);
            }
         }
      }
   }

   public boolean isGravePlaced(Grave grave) {
      if (grave == null) {
         return false;
      } else {
         UUID id = grave.getUUID();
         if (this.knownGraves.contains(id)) {
            return true;
         } else {
            Location location = grave.getLocationDeath();
            if (location != null && location.getWorld() != null) {
               try {
                  String pid = null;

                  try {
                     pid = grave.getProviderId();
                  } catch (Throwable var9) {
                  }

                  if (pid != null && !pid.isBlank()) {
                     GraveProvider p = RegisterGraveProviders.getById(pid);
                     if (p != null) {
                        try {
                           if (p.isPlaced(grave)) {
                              this.knownGraves.add(id);
                              return true;
                           }
                        } catch (Throwable var8) {
                        }
                     }
                  }
               } catch (Throwable var10) {
               }

               try {
                  IntegrationManager im = this.plugin.getIntegrationManager();
                  if (im.hasFurnitureLib() && im.getFurnitureLib().hasFurniture(grave)) {
                     this.knownGraves.add(id);
                     return true;
                  }

                  if (im.hasFurnitureEngine() && im.getFurnitureEngine().hasFurniture(grave)) {
                     this.knownGraves.add(id);
                     return true;
                  }

                  if (im.hasFancyNpcs() && im.getFancyNpcs().hasCorpse(grave)) {
                     this.knownGraves.add(id);
                     return true;
                  }

                  if (im.hasItemsAdder() && (im.getItemsAdder().hasBlock(grave) || im.getItemsAdder().hasFurniture(grave))) {
                     this.knownGraves.add(id);
                     return true;
                  }

                  if (im.hasOraxen() && (im.getOraxen().hasBlock(grave) || im.getOraxen().hasFurniture(grave))) {
                     this.knownGraves.add(id);
                     return true;
                  }

                  if (im.hasNexo() && (im.getNexo().hasBlock(grave) || im.getNexo().hasFurniture(grave))) {
                     this.knownGraves.add(id);
                     return true;
                  }

                  if (im.hasCraftEngine() && (im.getCraftEngine().hasBlock(grave) || im.getCraftEngine().hasFurniture(grave))) {
                     this.knownGraves.add(id);
                     return true;
                  }
               } catch (Throwable var11) {
                  this.plugin.getLogger().warning("Integration check failed in isGravePlaced: " + var11.getMessage());
               }

               CompletableFuture<Boolean> result = new CompletableFuture<>();
               this.plugin.getSchedulerManager().execute(location, () -> {
                  try {
                     Collection<Entity> nearby = location.getWorld().getNearbyEntities(location, 0.49, 0.49, 0.49);
                     if (!nearby.isEmpty()) {
                        result.complete(true);
                        return;
                     }

                     Block block = location.getBlock();
                     if (this.isHeadBlock(block)) {
                        this.plugin.getBlockManager().createBlock(location, grave);
                     }

                     result.complete(false);
                  } catch (Throwable var7x) {
                     this.plugin.debugMessage("isGravePlaced region work failed for " + id + " → treating as placed. Reason: " + var7x, 2);
                     result.complete(true);
                  }
               });

               try {
                  boolean placed = result.get(100L, TimeUnit.MILLISECONDS);
                  if (placed) {
                     this.knownGraves.add(id);
                  }

                  return placed;
               } catch (Exception var7) {
                  this.plugin.debugMessage("isGravePlaced timed out or failed for " + id + " → assuming placed.", 2);
                  return true;
               }
            } else {
               return false;
            }
         }
      }
   }

   private boolean isHeadBlock(Block block) {
      String n = block.getType().name();
      return "PLAYER_HEAD".equals(n) || "PLAYER_WALL_HEAD".equals(n) || "SKULL".equals(n) || "LEGACY_SKULL".equals(n);
   }

   public Grave.StorageMode getStorageMode(String string) {
      if (string != null && !string.isEmpty()) {
         try {
            Grave.StorageMode storageMode = Grave.StorageMode.valueOf(string.toUpperCase(Locale.ROOT));
            return storageMode == Grave.StorageMode.CHESTSORT && !this.plugin.getIntegrationManager().hasChestSort() ? Grave.StorageMode.COMPACT : storageMode;
         } catch (IllegalArgumentException var3) {
            return Grave.StorageMode.EXACT;
         }
      } else {
         return Grave.StorageMode.EXACT;
      }
   }

   public void placeGrave(Location location, Grave grave) {
      if (location != null && location.getWorld() != null && grave != null) {
         Location anchor = location.clone();
         this.plugin.getSchedulerManager().execute(anchor, () -> {
            try {
               List<GraveProvider> providers = new ArrayList<>(RegisterGraveProviders.getAll());
               if (!providers.isEmpty()) {
                  providers.sort(Comparator.comparingInt(GraveProvider::order));
                  String preferredId = null;

                  try {
                     preferredId = grave.getProviderId();
                  } catch (Throwable var7) {
                  }

                  if (preferredId != null && !preferredId.isBlank()) {
                     GraveProvider preferred = RegisterGraveProviders.getById(preferredId);
                     if (preferred != null && this.tryPlaceWithProvider(anchor, grave, preferred)) {
                        return;
                     }
                  }

                  for (GraveProvider p : providers) {
                     if (p != null && (preferredId == null || !preferredId.equalsIgnoreCase(p.id())) && this.tryPlaceWithProvider(anchor, grave, p)) {
                        return;
                     }
                  }
               }

               grave.setProviderId(null);
               this.plugin.getDataManager().updateGrave(grave, "provider_id", null);
               this.createGraveIntegrations(anchor, grave);
            } catch (Throwable var8) {
               this.plugin.getLogger().warning("Failed to place grave " + grave.getUUID() + ": " + var8.getMessage());
               this.plugin.logStackTrace(var8);
            }
         });
      }
   }

   private boolean tryPlaceWithProvider(Location anchor, Grave grave, GraveProvider p) {
      boolean hologramCreated = false;
      boolean headPlaced = false;
      Material headMat = null;

      try {
         if (p.shouldUseGraveHead()) {
            String matName = this.plugin.getConfigManager().getConfigSection("block.material", grave).getString("block.material", "PLAYER_HEAD");

            try {
               headMat = Material.valueOf(matName);
            } catch (Throwable var15) {
            }

            if (headMat == null) {
               try {
                  headMat = Material.valueOf("PLAYER_HEAD");
               } catch (Throwable var14) {
               }
            }

            this.createGraveBlock(anchor, grave, p.shouldBlockBeReplaceable());
            headPlaced = headMat != null;
            this.plugin.getHologramManager().createHologram(anchor, grave);
            hologramCreated = true;
         } else {
            this.plugin.getHologramManager().createHologram(anchor, grave);
            hologramCreated = true;
         }

         p.place(anchor, grave);

         boolean placed;
         try {
            placed = p.isPlaced(grave);
         } catch (Throwable var13) {
            placed = false;
            GravesXGraveProviderException wrapped = GravesXGraveProviderException.forProvider(p, anchor, grave, "isPlaced() threw after place()", var13);
            this.plugin.getLogger().log(Level.WARNING, wrapped.getMessage(), (Throwable)wrapped);
         }

         if (!placed) {
            this.plugin
               .debugMessage(
                  "[CustomGraveProvider "
                     + p.id()
                     + " (order="
                     + p.order()
                     + ")] place() returned without error, but isPlaced=false at "
                     + anchor
                     + ". Assuming success. Provider should implement a reliable isPlaced().",
                  2
               );
         } else {
            this.plugin.debugMessage("[CustomGraveProvider " + p.id() + " (order=" + p.order() + ")] placed successfully.", 1);
         }

         grave.setProviderId(p.id());
         this.plugin.getDataManager().updateGrave(grave, "provider_id", p.id());
         return true;
      } catch (Throwable var16) {
         GravesXGraveProviderException wrapped = var16 instanceof GravesXGraveProviderException
            ? (GravesXGraveProviderException)var16
            : GravesXGraveProviderException.forProvider(p, anchor, grave, var16);
         this.plugin.getLogger().severe("Failed to place grave from GraveProvider " + p.id() + " (order=" + p.order() + "): " + wrapped.getMessage());
         this.plugin.logStackTrace((Exception)wrapped);

         try {
            if (hologramCreated) {
               this.plugin.getHologramManager().removeHologram(grave);
            }
         } catch (Throwable var12) {
         }

         try {
            if (headPlaced && anchor.getBlock().getType() == headMat) {
               anchor.getBlock().setType(Material.AIR);
            }
         } catch (Throwable var11) {
         }

         try {
            this.plugin.getBlockManager().removeBlock(grave);
         } catch (Throwable var10) {
         }

         return false;
      }
   }

   public void createGraveBlock(@NotNull Location location, @NotNull Grave grave) {
      this.createGraveBlock(location, grave, false);
   }

   public void createGraveBlock(@NotNull Location location, @NotNull Grave grave, boolean storeReplacementData) {
      try {
         this.plugin.getBlockManager().createBlock(location, grave, storeReplacementData);
      } catch (Throwable var6) {
         String world = location.getWorld() != null ? location.getWorld().getName() : null;
         this.plugin
            .getLogger()
            .warning(
               "Failed to create grave block for "
                  + grave.getUUID()
                  + " at "
                  + world
                  + ", "
                  + location.getBlockX()
                  + "x, "
                  + location.getBlockY()
                  + "y, "
                  + location.getBlockZ()
                  + "z: "
                  + var6.getMessage()
            );
         this.plugin.logStackTrace(var6);
      }
   }

   private void createGraveIntegrations(@NotNull Location anchor, @NotNull Grave grave) {
      if (this.plugin.getIntegrationManager().hasFurnitureLib()
         && this.plugin.getConfigManager().getConfigSection("furniturelib.enabled", grave).getBoolean("furniturelib.enabled", true)) {
         this.plugin.debugMessage("Creating FurnitureLib furniture grave for " + grave.getUUID(), 1);
         this.createGraveBlock(anchor, grave, true);
         this.plugin.getHologramManager().createHologram(anchor, grave);
         this.plugin.getIntegrationManager().getFurnitureLib().createFurniture(anchor, grave);
      } else if (this.plugin.getIntegrationManager().hasFurnitureEngine()
         && this.plugin.getConfigManager().getConfigSection("furnitureengine.enabled", grave).getBoolean("furnitureengine.enabled", true)) {
         this.plugin.debugMessage("Creating FurnitureEngine furniture grave for " + grave.getUUID(), 1);
         this.plugin.getHologramManager().createHologram(anchor, grave);
         this.plugin.getIntegrationManager().getFurnitureEngine().createFurniture(anchor, grave);
      } else if (this.plugin.getIntegrationManager().hasItemsAdder()
         && this.plugin.getConfigManager().getConfigSection("itemsadder.furniture.enabled", grave).getBoolean("itemsadder.furniture.enabled", true)) {
         this.plugin.debugMessage("Creating ItemsAdder furniture grave for " + grave.getUUID(), 1);
         this.createGraveBlock(anchor, grave, true);
         this.plugin.getHologramManager().createHologram(anchor, grave);
         this.plugin.getIntegrationManager().getItemsAdder().createFurniture(anchor, grave);
      } else if (this.plugin.getIntegrationManager().hasItemsAdder()
         && this.plugin.getConfigManager().getConfigSection("itemsadder.block.enabled", grave).getBoolean("itemsadder.block.enabled", true)) {
         this.plugin.debugMessage("Creating ItemsAdder block grave for " + grave.getUUID(), 1);
         this.createGraveBlock(anchor, grave);
         this.plugin.getHologramManager().createHologram(anchor, grave);
         this.plugin.getIntegrationManager().getItemsAdder().createBlock(anchor, grave);
      } else if (this.plugin.getIntegrationManager().hasOraxen()
         && this.plugin.getConfigManager().getConfigSection("oraxen.furniture.enabled", grave).getBoolean("oraxen.furniture.enabled", true)) {
         this.plugin.debugMessage("Creating Oraxen furniture grave for " + grave.getUUID(), 1);
         this.createGraveBlock(anchor, grave, true);
         this.plugin.getHologramManager().createHologram(anchor, grave);
         this.plugin.getIntegrationManager().getOraxen().createFurniture(anchor, grave);
      } else if (this.plugin.getIntegrationManager().hasOraxen()
         && this.plugin.getConfigManager().getConfigSection("oraxen.block.enabled", grave).getBoolean("oraxen.block.enabled", true)) {
         this.plugin.debugMessage("Creating Oraxen block grave for " + grave.getUUID(), 1);
         this.createGraveBlock(anchor, grave);
         this.plugin.getHologramManager().createHologram(anchor, grave);
         this.plugin.getIntegrationManager().getOraxen().createBlock(anchor, grave);
      } else if (this.plugin.getIntegrationManager().hasNexo()
         && this.plugin.getConfigManager().getConfigSection("nexo.furniture.enabled", grave).getBoolean("nexo.furniture.enabled", true)) {
         this.plugin.debugMessage("Creating Nexo furniture grave for " + grave.getUUID(), 1);
         this.createGraveBlock(anchor, grave, true);
         this.plugin.getHologramManager().createHologram(anchor, grave);
         this.plugin.getIntegrationManager().getNexo().createFurniture(anchor, grave);
      } else if (this.plugin.getIntegrationManager().hasNexo()
         && this.plugin.getConfigManager().getConfigSection("nexo.block.enabled", grave).getBoolean("nexo.block.enabled", true)) {
         this.plugin.debugMessage("Creating Nexo block grave for " + grave.getUUID(), 1);
         this.createGraveBlock(anchor, grave);
         this.plugin.getHologramManager().createHologram(anchor, grave);
         this.plugin.getIntegrationManager().getNexo().createBlock(anchor, grave);
      } else if (this.plugin.getIntegrationManager().hasCraftEngine()
         && this.plugin.getConfigManager().getConfigSection("craftengine.furniture.enabled", grave).getBoolean("craftengine.furniture.enabled", true)) {
         this.plugin.debugMessage("Creating CraftEngine furniture grave for " + grave.getUUID(), 1);
         this.createGraveBlock(anchor, grave, true);
         this.plugin.getHologramManager().createHologram(anchor, grave);
         this.plugin.getIntegrationManager().getCraftEngine().createFurniture(anchor, grave);
      } else if (this.plugin.getIntegrationManager().hasCraftEngine()
         && this.plugin.getConfigManager().getConfigSection("craftengine.block.enabled", grave).getBoolean("craftengine.block.enabled", true)) {
         this.plugin.debugMessage("Creating CraftEngine block grave for " + grave.getUUID(), 1);
         this.createGraveBlock(anchor, grave);
         this.plugin.getHologramManager().createHologram(anchor, grave);
         this.plugin.getIntegrationManager().getCraftEngine().createBlock(anchor, grave);
      } else if (this.plugin.getIntegrationManager().hasPlayerNPC()
         && this.plugin.getConfigManager().getConfigSection("playernpc.corpse.enabled", grave).getBoolean("playernpc.corpse.enabled", true)) {
         this.plugin.debugMessage("Creating PlayerNPC corpse grave for " + grave.getUUID(), 1);
         this.plugin.getHologramManager().createHologram(anchor, grave);
         this.plugin.getIntegrationManager().getPlayerNPC().createCorpse(anchor, grave);
      } else if (this.plugin.getIntegrationManager().hasMannequins()
         && this.plugin.getConfigManager().getConfigSection("mannequins.corpse.enabled", grave).getBoolean("mannequins.corpse.enabled", true)) {
         this.plugin.debugMessage("Creating Mannequins corpse grave for " + grave.getUUID(), 1);
         this.plugin.getIntegrationManager().getMannequins().createCorpse(grave.getUUID(), grave.getLocationDeath(), grave);
         this.plugin.getHologramManager().createHologram(anchor, grave);
      } else if (this.plugin.getIntegrationManager().hasFancyNpcs()
         && this.plugin.getConfigManager().getConfigSection("fancynpcs.corpse.enabled", grave).getBoolean("fancynpcs.corpse.enabled", true)) {
         if (!this.plugin.getIntegrationManager().hasFloodgate()) {
            this.plugin.debugMessage("Creating FancyNPCs corpse grave for " + grave.getUUID() + " (no Floodgate)", 1);
            this.plugin.getHologramManager().createHologram(anchor, grave);
            this.plugin.getIntegrationManager().getFancyNpcs().createCorpse(grave.getUUID(), grave.getLocationDeath(), grave);
         } else {
            try {
               if (this.plugin.getIntegrationManager().getFloodgate().isFloodgateId(grave.getOwnerUUID())) {
                  this.plugin.debugMessage("Creating FancyNPCs Bedrock-compatible corpse grave for " + grave.getUUID(), 1);
                  this.plugin.getHologramManager().createHologram(anchor, grave);
                  this.plugin.getIntegrationManager().getFancyNpcs().createBedrockcompatCorpse(grave.getUUID(), grave.getLocationDeath(), grave);
               } else {
                  this.plugin.debugMessage("Creating FancyNPCs corpse grave for " + grave.getUUID() + " (Java player)", 1);
                  this.plugin.getHologramManager().createHologram(anchor, grave);
                  this.plugin.getIntegrationManager().getFancyNpcs().createCorpse(grave.getUUID(), grave.getLocationDeath(), grave);
               }
            } catch (Throwable var4) {
               this.plugin.debugMessage("FancyNPCs Floodgate check failed; falling back to standard corpse for " + grave.getUUID(), 1);
               this.plugin.getHologramManager().createHologram(anchor, grave);
               this.plugin.getIntegrationManager().getFancyNpcs().createCorpse(grave.getUUID(), grave.getLocationDeath(), grave);
            }
         }
      } else if (this.plugin.getConfigManager().getConfigSection("armor-stand.enabled", grave).getBoolean("armor-stand.enabled", false)) {
         this.plugin.debugMessage("Creating ArmorStand grave for " + grave.getUUID(), 1);
         this.createGraveBlock(anchor, grave, true);
         this.plugin.getHologramManager().createHologram(anchor, grave);
         this.plugin.getEntityManager().createArmorStand(anchor, grave);
      } else if (this.plugin.getConfigManager().getConfigSection("item-frame.enabled", grave).getBoolean("item-frame.enabled", false)) {
         this.plugin.debugMessage("Creating ItemFrame grave for " + grave.getUUID(), 1);
         this.createGraveBlock(anchor, grave, true);
         this.plugin.getHologramManager().createHologram(anchor, grave);
         this.plugin.getEntityManager().createItemFrame(anchor, grave);
      } else {
         this.plugin.debugMessage("Creating Block grave for " + grave.getUUID(), 1);
         this.createGraveBlock(anchor, grave);
         this.plugin.getHologramManager().createHologram(anchor, grave);
      }
   }

   public Inventory getGraveInventory(
      Grave grave, LivingEntity livingEntity, List<ItemStack> graveItemStackList, List<ItemStack> removedItemStackList, List<String> permissionList
   ) {
      String title;
      if (this.plugin.getIntegrationManager().hasMiniMessage()) {
         String newTitle = StringUtil.parseString(
            this.plugin.getConfigManager().getConfigSection("gui.grave.title", grave).getString("gui.grave.title"),
            livingEntity,
            grave.getLocationDeath(),
            grave,
            this.plugin
         );
         title = MiniMessage.parseString(newTitle);
      } else {
         title = StringUtil.parseString(
            this.plugin.getConfigManager().getConfigSection("gui.grave.title", grave).getString("gui.grave.title"),
            livingEntity,
            grave.getLocationDeath(),
            grave,
            this.plugin
         );
      }

      Grave.StorageMode storageMode = this.getStorageMode(this.plugin.getConfigManager().getConfigSection("storage.mode", grave).getString("storage.mode"));
      return this.plugin.getGraveManager().createGraveInventory(grave, grave.getLocationDeath(), graveItemStackList, title, storageMode);
   }

   public Inventory createGraveInventory(
      InventoryHolder inventoryHolder, Location location, List<ItemStack> itemStackList, String title, Grave.StorageMode storageMode
   ) {
      if (storageMode != Grave.StorageMode.COMPACT && storageMode != Grave.StorageMode.CHESTSORT) {
         if (storageMode == Grave.StorageMode.EXACT) {
            if (this.plugin.getVersionManager().hasEnchantmentCurse()) {
               itemStackList.removeIf(itemStackx -> itemStackx != null && itemStackx.containsEnchantment(Enchantment.VANISHING_CURSE));
            }

            ItemStack itemStackAir = new ItemStack(Material.AIR);
            Inventory inventory = this.plugin.getServer().createInventory(inventoryHolder, InventoryUtil.getInventorySize(itemStackList.size()), title);
            int counter = 0;

            for (ItemStack itemStack : itemStackList) {
               if (counter < inventory.getSize()) {
                  inventory.setItem(counter, itemStack != null ? itemStack : itemStackAir);
               } else if (itemStack != null && location != null && location.getWorld() != null) {
                  location.getWorld().dropItem(location, itemStack);
               }

               counter++;
            }

            return inventory;
         } else {
            return null;
         }
      } else {
         Inventory tempInventory = this.plugin.getServer().createInventory(null, 54);
         int counter = 0;

         for (ItemStack itemStack : itemStackList) {
            if (this.getItemStacksSize(tempInventory.getContents()) < tempInventory.getSize()) {
               if (itemStack != null && !MaterialUtil.isAir(itemStack.getType())) {
                  tempInventory.addItem(new ItemStack[]{itemStack});
                  counter++;
               }
            } else if (itemStack != null && location != null && location.getWorld() != null) {
               location.getWorld().dropItem(location, itemStack);
            }
         }

         counter = 0;

         for (ItemStack itemStackx : tempInventory.getContents()) {
            if (itemStackx != null) {
               counter++;
            }
         }

         Inventory inventory = this.plugin.getServer().createInventory(inventoryHolder, InventoryUtil.getInventorySize(counter), title);

         for (ItemStack itemStackxx : tempInventory.getContents()) {
            if (itemStackxx != null && location != null && location.getWorld() != null) {
               inventory.addItem(new ItemStack[]{itemStackxx}).forEach((key, value) -> location.getWorld().dropItem(location, value));
            }
         }

         if (storageMode == Grave.StorageMode.CHESTSORT && this.plugin.getIntegrationManager().hasChestSort()) {
            this.plugin.getIntegrationManager().getChestSort().sortInventory(inventory);
         }

         return inventory;
      }
   }

   public int getItemStacksSize(ItemStack[] itemStacks) {
      if (itemStacks == null) {
         return 0;
      } else {
         int counter = 0;

         for (ItemStack itemStack : itemStacks) {
            if (itemStack != null) {
               counter++;
            }
         }

         return counter;
      }
   }

   public void breakGrave(Grave grave) {
      this.breakGrave(grave.getLocationDeath(), grave);
   }

   public void breakGrave(Location location, Grave grave) {
      if (grave != null) {
         if (location != null && location.getWorld() != null) {
            Location anchor = location.clone();
            this.plugin
               .getSchedulerManager()
               .execute(
                  anchor,
                  () -> {
                     try {
                        this.dropGraveItems(anchor, grave);
                        this.dropGraveExperience(anchor, grave);
                        if (grave.getLocationDeath().getBlock().getType()
                           == Material.valueOf(
                              this.plugin.getConfigManager().getConfigSection("block.material", grave).getString("block.material", "PLAYER_HEAD")
                           )) {
                           grave.getLocationDeath().getBlock().setType(Material.valueOf("AIR"));
                        }

                        this.removeGrave(grave);
                        this.plugin.debugMessage("Grave " + grave.getUUID() + " broken", 1);
                     } catch (Throwable var4) {
                        this.plugin.debugMessage("Grave " + grave.getUUID() + " failed to break due to an error", 2);
                        this.plugin.getLogger().severe("Error while dropping items/XP for grave " + grave.getUUID() + ": " + var4.getMessage());
                        this.plugin.logStackTrace(var4);
                     }
                  }
               );
         } else {
            this.removeGrave(grave);
            this.plugin.debugMessage("Grave " + grave.getUUID() + " broken (no valid location/world for drops)", 1);
         }
      }
   }

   public void dropGraveItems(Location location, Grave grave) {
      if (grave != null && location != null && location.getWorld() != null) {
         Inventory inv = grave.getInventory();
         Location anchor = location.clone();
         this.plugin.getSchedulerManager().execute(anchor, () -> {
            try {
               World world = anchor.getWorld();
               if (world == null) {
                  return;
               }

               for (ItemStack itemStack : inv.getContents()) {
                  if (itemStack != null) {
                     world.dropItemNaturally(anchor, itemStack);
                  }
               }

               inv.clear();
            } catch (Throwable var9) {
               this.plugin.debugMessage("dropGraveItems failed for " + grave.getUUID() + ": " + var9.getMessage(), 2);
               this.plugin.logStackTrace(var9);
            }
         });
      }
   }

   public void giveGraveExperience(Player player, Grave grave) {
      if (player != null && grave != null) {
         int xp = grave.getExperience();
         if (xp > 0) {
            Location anchor = player.getLocation();
            if (anchor.getWorld() == null) {
               this.plugin.getSchedulerManager().runTask(() -> {
                  if (player.isOnline()) {
                     player.giveExp(xp);
                     grave.setExperience(0);
                     this.plugin.getEntityManager().playWorldSound("ENTITY_EXPERIENCE_ORB_PICKUP", player);
                  }
               });
            } else {
               this.plugin.getSchedulerManager().execute(anchor, () -> {
                  if (player.isOnline()) {
                     player.giveExp(xp);
                     grave.setExperience(0);
                     this.plugin.getEntityManager().playWorldSound("ENTITY_EXPERIENCE_ORB_PICKUP", player);
                  }
               });
            }
         }
      }
   }

   public void dropGraveExperience(Location location, Grave grave) {
      if (grave != null && location != null && location.getWorld() != null) {
         int xp = grave.getExperience();
         if (xp > 0) {
            Location anchor = location.clone();
            this.plugin.getSchedulerManager().execute(anchor, () -> {
               try {
                  World world = anchor.getWorld();
                  if (world == null) {
                     throw new GravesXNullPointerException("World returned null for grave " + grave.getUUID());
                  }

                  ExperienceOrb experienceOrb = (ExperienceOrb)world.spawnEntity(anchor, EntityType.EXPERIENCE_ORB);
                  experienceOrb.setExperience(xp);
                  this.plugin.debugMessage("Dropping experience for grave " + grave.getUUID() + " in the amount of " + grave.getExperience(), 1);
                  grave.setExperience(0);
               } catch (Throwable var6) {
                  this.plugin.getLogger().severe("Unable to drop grave experience in the total of " + grave.getExperience() + ": " + var6.getMessage());
                  this.plugin.logStackTrace(var6);
               }
            });
         }
      }
   }

   public List<Grave> getGraveList(Player player) {
      return this.getGraveList(player.getUniqueId());
   }

   public List<Grave> getGraveList(OfflinePlayer player) {
      return this.getGraveList(player.getUniqueId());
   }

   public List<Grave> getGraveList(Entity entity) {
      return this.getGraveList(entity.getUniqueId());
   }

   public List<Grave> getGraveList(UUID uuid) {
      List<Grave> graveList = new ArrayList<>();
      if (uuid == null) {
         return graveList;
      } else {
         this.plugin.getCacheManager().getGraveMap().forEach((key, value) -> {
            if (value != null && !value.isAbandoned() && value.getOwnerUUID() != null && value.getOwnerUUID().equals(uuid)) {
               graveList.add(value);
            }
         });
         return graveList;
      }
   }

   public int getGraveCount(Entity entity) {
      return this.getGraveList(entity).size();
   }

   public boolean openGrave(Entity entity, Location location, Grave grave) {
      this.openGrave(entity, location, grave, false);
      return false;
   }

   public boolean openGrave(Entity entity, Location location, Grave grave, boolean preview) {
      if (entity instanceof Player player) {
         Location anchor = location != null ? location : player.getLocation();
         if (anchor.getWorld() == null) {
            return false;
         } else {
            UUID graveUUID = grave.getUUID();
            UUID viewerUUID = player.getUniqueId();
            UUID ownerUUID = grave.getOwnerUUID();
            boolean isOwner = ownerUUID != null && ownerUUID.equals(viewerUUID);
            this.plugin.getSchedulerManager().execute(anchor, () -> this.plugin.getEntityManager().swingMainHand(player));
            if (this.plugin.getEntityManager().canOpenGrave(player, grave)) {
               this.plugin
                  .getSchedulerManager()
                  .execute(
                     anchor,
                     () -> {
                        this.cleanupCompasses(player, grave);
                        if (this.plugin.getConfigManager().getConfigSection("drop.auto-loot.enabled", grave).getBoolean("drop.auto-loot.enabled", true)
                           && player.isSneaking()
                           && this.plugin.getPermissionManager().hasGrantedPermission("graves.autoloot", player.getPlayer())) {
                           if (player.getGameMode() == GameMode.SPECTATOR
                              && !this.plugin.getPermissionManager().hasGrantedPermission("graves.spectator.bypass", player.getPlayer())) {
                              return;
                           }

                           if (!this.plugin.getCacheManager().startViewingGrave(graveUUID, viewerUUID)) {
                              if (!isOwner) {
                                 this.plugin.getEntityManager().sendMessage("message.grave-in-use", player, location, grave);
                                 this.plugin.getEntityManager().playWorldSound("sound.protection", location, grave);
                                 return;
                              }

                              this.closeGrave(grave);
                              if (!this.plugin.getCacheManager().startViewingGrave(graveUUID, viewerUUID)) {
                                 this.plugin.getEntityManager().sendMessage("message.grave-in-use", player, location, grave);
                                 this.plugin.getEntityManager().playWorldSound("sound.protection", location, grave);
                                 return;
                              }
                           }

                           try {
                              GraveAutoLootEvent modern = new GraveAutoLootEvent(player, location, grave);
                              this.plugin.getServer().getPluginManager().callEvent(modern);
                              com.ranull.graves.event.GraveAutoLootEvent legacy = new com.ranull.graves.event.GraveAutoLootEvent(player, location, grave);
                              this.plugin.getServer().getPluginManager().callEvent(legacy);
                              if (!modern.isCancelled() || !modern.isAddon() || !legacy.isCancelled() || !legacy.isAddon()) {
                                 this.autoLootGrave(player, location, grave);
                                 if (this.plugin.getIntegrationManager().hasNoteBlockAPI()) {
                                    if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForPlayer(player)) {
                                       this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForPlayer(player);
                                    }

                                    if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForAllPlayers()) {
                                       this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForAllPlayers();
                                    }
                                 }
                              }
                           } finally {
                              this.plugin.getCacheManager().stopViewingGrave(graveUUID, viewerUUID);
                           }
                        } else if (this.plugin.getPermissionManager().hasGrantedPermission("graves.open", player.getPlayer())) {
                           if (!preview && !this.plugin.getCacheManager().startViewingGrave(graveUUID, viewerUUID)) {
                              if (!isOwner) {
                                 this.plugin.getEntityManager().sendMessage("message.grave-in-use", player, location, grave);
                                 this.plugin.getEntityManager().playWorldSound("sound.protection", location, grave);
                                 return;
                              }

                              this.closeGrave(grave);
                              if (!this.plugin.getCacheManager().startViewingGrave(graveUUID, viewerUUID)) {
                                 this.plugin.getEntityManager().sendMessage("message.grave-in-use", player, location, grave);
                                 this.plugin.getEntityManager().playWorldSound("sound.protection", location, grave);
                                 return;
                              }
                           }

                           if (this.plugin.getConfigManager().getConfigSection("grave.preview", grave).getBoolean("grave.preview", false)) {
                              grave.setGravePreview(preview);
                           } else {
                              grave.setGravePreview(false);
                           }

                           player.openInventory(grave.getInventory());
                           this.plugin.getEntityManager().runCommands("event.command.open", player, location, grave);
                           this.plugin.getEntityManager().playWorldSound("sound.open", location, grave);
                        }
                     }
                  );
               return true;
            } else {
               this.plugin.getSchedulerManager().execute(anchor, () -> {
                  boolean protPreview = this.plugin.getConfigManager().getConfigSection("protection.preview", grave).getBoolean("protection.preview", false);
                  boolean gravePreview = this.plugin.getConfigManager().getConfigSection("grave.preview", grave).getBoolean("grave.preview", false);
                  if (protPreview && gravePreview) {
                     grave.setGravePreview(preview);
                     player.openInventory(grave.getInventory());
                     this.plugin.getEntityManager().runCommands("event.command.open", player, location, grave);
                     this.plugin.getEntityManager().playWorldSound("sound.open", location, grave);
                  } else if (protPreview) {
                     grave.setGravePreview(false);
                     player.openInventory(grave.getInventory());
                     this.plugin.getEntityManager().runCommands("event.command.open", player, location, grave);
                     this.plugin.getEntityManager().playWorldSound("sound.open", location, grave);
                  } else {
                     this.plugin.getEntityManager().sendMessage("message.protection", player, location, grave);
                     this.plugin.getEntityManager().playWorldSound("sound.protection", location, grave);
                  }
               });
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public void cleanupCompasses(Player player, Grave grave) {
      if (player != null && grave != null) {
         Map<ItemStack, UUID> compasses = this.plugin.getEntityManager().getCompassesFromInventory(player);
         if (!compasses.isEmpty()) {
            for (Entry<ItemStack, UUID> entry : compasses.entrySet()) {
               UUID target = entry.getValue();
               if (target != null && target.equals(grave.getUUID())) {
                  try {
                     player.getInventory().remove(entry.getKey());
                  } catch (Throwable var8) {
                  }
               }
            }
         }
      }
   }

   public List<Location> getGraveLocationList(Location baseLocation, Grave grave) {
      if (baseLocation != null && grave != null) {
         List<Location> locationList = new ArrayList<>(this.plugin.getBlockManager().getBlockList(grave));
         Map<Double, Location> locationMap = new HashMap<>();
         List<Location> otherWorldLocationList = new ArrayList<>();
         if (baseLocation.getWorld() != null) {
            if (!locationList.contains(grave.getLocationDeath())) {
               locationList.add(grave.getLocationDeath());
            }

            for (Location location : locationList) {
               if (location != null) {
                  if (location.getWorld() != null && baseLocation.getWorld().equals(location.getWorld())) {
                     locationMap.put(location.distanceSquared(baseLocation), location);
                  } else {
                     otherWorldLocationList.add(location);
                  }
               }
            }

            locationList = new ArrayList<>(new TreeMap<>(locationMap).values());
            locationList.addAll(otherWorldLocationList);
         }

         return locationList;
      } else {
         return new ArrayList<>();
      }
   }

   public Location getGraveLocation(Location location, Grave grave) {
      if (location != null && grave != null) {
         List<Location> locationList = this.plugin.getGraveManager().getGraveLocationList(location, grave);
         return !locationList.isEmpty() ? locationList.get(0) : null;
      } else {
         return null;
      }
   }

   public void autoLootGrave(Entity entity, Location location, Grave grave) {
      if (entity instanceof Player player) {
         Location anchor = location != null ? location : player.getLocation();
         if (anchor.getWorld() != null) {
            this.plugin
               .getSchedulerManager()
               .execute(
                  anchor,
                  () -> {
                     Grave.StorageMode storageMode = this.getStorageMode(
                        this.plugin.getConfigManager().getConfigSection("storage.mode", grave).getString("storage.mode")
                     );
                     if (storageMode == Grave.StorageMode.EXACT) {
                        List<ItemStack> itemStackListLeftOver = new ArrayList<>();
                        int counter = 0;
                        int inventorySize = player.getInventory().getSize();

                        for (ItemStack itemStack : grave.getInventory().getContents()) {
                           if (itemStack != null) {
                              if (player.getInventory().getItem(counter) == null) {
                                 if (counter < inventorySize) {
                                    player.getInventory().setItem(counter, itemStack);
                                    grave.getInventory().remove(itemStack);
                                    if (counter == 17 && this.plugin.getVersionManager().hasSecondHand()) {
                                       player.getInventory().setItem(17, itemStack);
                                       grave.getInventory().remove(itemStack);
                                    }

                                    if (counter == 39 && InventoryUtil.isHelmet(itemStack)
                                       || counter == 38 && InventoryUtil.isChestplate(itemStack)
                                       || counter == 37 && InventoryUtil.isLeggings(itemStack)
                                       || counter == 36 && InventoryUtil.isBoots(itemStack)) {
                                       InventoryUtil.playArmorEquipSound(player, itemStack);
                                    }
                                 } else {
                                    itemStackListLeftOver.add(itemStack);
                                 }
                              } else {
                                 itemStackListLeftOver.add(itemStack);
                              }
                           }

                           counter++;
                        }

                        grave.getInventory().clear();

                        for (ItemStack itemStack : itemStackListLeftOver) {
                           for (Entry<Integer, ItemStack> itemStackEntry : player.getInventory().addItem(new ItemStack[]{itemStack}).entrySet()) {
                              grave.getInventory()
                                 .addItem(new ItemStack[]{itemStackEntry.getValue()})
                                 .forEach((key, value) -> player.getWorld().dropItem(player.getLocation(), value));
                           }
                        }
                     } else {
                        InventoryUtil.equipArmor(grave.getInventory(), player);
                        InventoryUtil.equipItems(grave.getInventory(), player);
                     }

                     if (this.plugin.getIntegrationManager().hasBagOfGold()) {
                        this.syncBagOfGoldBalanceAfterAutoloot(player);
                     }

                     player.updateInventory();
                     this.plugin.getDataManager().updateGrave(grave, "inventory", InventoryUtil.inventoryToString(grave.getInventory(), this.plugin));
                     this.plugin.getEntityManager().runCommands("event.command.open", player, location, grave);
                     if (grave.getItemAmount() <= 0) {
                        this.plugin.getEntityManager().runCommands("event.command.loot", player, location, grave);
                        this.plugin.getEntityManager().sendMessage("message.looted", player, location, grave);
                        this.plugin.getEntityManager().playWorldSound("sound.close", location, grave);
                        this.plugin.getEntityManager().spawnZombie(location, player, player, grave);
                        this.giveGraveExperience(player, grave);
                        this.playEffect("effect.loot", location, grave);
                        this.removeGrave(grave);
                        this.closeGrave(grave);
                        this.plugin.debugMessage("Grave " + grave.getUUID() + " autolooted by " + player.getName(), 1);
                     } else {
                        this.plugin.getEntityManager().playWorldSound("sound.open", location, grave);
                     }
                  }
               );
         }
      }
   }

   private void syncBagOfGoldBalanceAfterAutoloot(@NotNull Player player) {
      Plugin bagOfGold = this.plugin.getServer().getPluginManager().getPlugin("BagOfGold");
      if (bagOfGold != null && bagOfGold.isEnabled()) {
         try {
            Method getInstance = bagOfGold.getClass().getMethod("getInstance");
            Object instance = getInstance.invoke(null);
            if (instance == null) {
               return;
            }

            Method getRewardManager = instance.getClass().getMethod("getRewardManager");
            Object rewardManager = getRewardManager.invoke(instance);
            if (rewardManager == null) {
               return;
            }

            Method adjustToInventory = rewardManager.getClass().getMethod("adjustPlayerBalanceToAmounOfMoneyInInventory", Player.class);
            adjustToInventory.invoke(rewardManager, player);
         } catch (Throwable var8) {
            this.plugin.debugMessage("Failed to sync BagOfGold balance after grave auto-loot: " + var8.getMessage(), 1);
         }
      }
   }

   public boolean isNearGrave(Location location) {
      return this.isNearGrave(location, null, null);
   }

   public boolean isNearGrave(Location location, Player player) {
      return this.isNearGrave(location, player, null);
   }

   public boolean isNearGrave(Location location, Block block) {
      return this.isNearGrave(location, null, block);
   }

   public boolean isNearGrave(Location location, Player player, Block block) {
      if (location != null && location.getWorld() != null) {
         try {
            Collection<Grave> graves = this.plugin.getCacheManager().getGraveMap().values();

            for (Grave grave : new ArrayList<>(graves)) {
               if (grave != null) {
                  Location base = player != null ? player.getLocation() : (block != null ? block.getLocation() : location);
                  Location graveLocation = this.plugin.getGraveManager().getGraveLocation(base, grave);
                  if (graveLocation != null
                     && graveLocation.getWorld() != null
                     && graveLocation.getWorld().equals(location.getWorld())
                     && graveLocation.getBlockX() == location.getBlockX()
                     && graveLocation.getBlockY() == location.getBlockY()
                     && graveLocation.getBlockZ() == location.getBlockZ()) {
                     return true;
                  }
               }
            }
         } catch (Throwable var9) {
         }

         return false;
      } else {
         return false;
      }
   }

   @Deprecated(
      since = "2026.4.9.1"
   )
   public String getDamageReason(DamageCause damageCause, Grave grave) {
      List<String> reasons = this.plugin.getConfigManager().getConfigSection("message.death-reason", grave).getStringList("message.death-reason");
      String causeName = damageCause.name();

      for (String reason : reasons) {
         if (reason.startsWith(causeName + ":")) {
            return reason.split(":", 2)[1].trim();
         }
      }

      return StringUtil.format(causeName);
   }

   public String getDamageReason(Grave grave) {
      List<String> reasons = this.plugin.getConfigManager().getConfigSection("message.death-reason", grave).getStringList("message.death-reason");
      String causeName = grave.getDeathCause();

      for (String reason : reasons) {
         if (reason.startsWith(causeName + ":")) {
            return reason.split(":", 2)[1].trim();
         }
      }

      return StringUtil.format(causeName);
   }

   public void playEffect(String string, Location location) {
      this.playEffect(string, location, null);
   }

   public void playEffect(String string, Location location, Grave grave) {
      this.playEffect(string, location, 0, grave);
   }

   public void playEffect(String string, Location location, int data, Grave grave) {
      if (location != null && location.getWorld() != null) {
         String effectName = string;
         if (grave != null) {
            effectName = this.plugin.getConfigManager().getConfigSection(string, grave).getString(string);
         }

         if (effectName != null && !effectName.isEmpty()) {
            String resolved = effectName;
            this.plugin.getSchedulerManager().execute(location, () -> {
               World world = location.getWorld();
               if (world != null) {
                  try {
                     Effect effect = Effect.valueOf(resolved.toUpperCase(Locale.ROOT));
                     world.playEffect(location, effect, data);
                  } catch (IllegalArgumentException var6x) {
                     this.plugin.debugMessage(resolved.toUpperCase(Locale.ROOT) + " is not an Effect ENUM", 1);
                  }
               }
            });
         }
      }
   }

   public boolean shouldIgnoreItemStack(ItemStack itemStack, Entity entity, List<String> permissionList) {
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         if (Bukkit.getPluginManager().isPluginEnabled("AdvancedEnchantments")) {
            if (ReflectSupportAE.isSoulbound(itemStack)) {
               entity.getWorld().dropItem(entity.getLocation(), itemStack);
               return true;
            }

            if (ReflectSupportAE.hasWhitScroll(itemStack)) {
               return true;
            }
         }

         if (this.plugin
            .getConfigManager()
            .getConfigSection("ignore.item.material", entity, permissionList)
            .getStringList("ignore.item.material")
            .contains(itemStack.getType().name())) {
            return true;
         } else {
            if (itemStack.hasItemMeta()) {
               ItemMeta itemMeta = itemStack.getItemMeta();
               if (itemMeta != null) {
                  if (itemMeta.hasDisplayName()) {
                     for (String s : this.plugin
                        .getConfigManager()
                        .getConfigSection("ignore.item.name", entity, permissionList)
                        .getStringList("ignore.item.name")) {
                        if (!s.isEmpty() && itemMeta.getDisplayName().equals(StringUtil.parseString(s, this.plugin))) {
                           return true;
                        }
                     }

                     for (String sx : this.plugin
                        .getConfigManager()
                        .getConfigSection("ignore.item.name-contains", entity, permissionList)
                        .getStringList("ignore.item.name-contains")) {
                        if (!sx.isEmpty() && itemMeta.getDisplayName().contains(StringUtil.parseString(sx, this.plugin))) {
                           return true;
                        }
                     }
                  }

                  if (itemMeta.hasLore() && itemMeta.getLore() != null) {
                     List<String> loreLines = itemMeta.getLore();

                     for (String sxx : this.plugin
                        .getConfigManager()
                        .getConfigSection("ignore.item.lore", entity, permissionList)
                        .getStringList("ignore.item.lore")) {
                        if (!sxx.isEmpty()) {
                           String target = StringUtil.parseString(sxx, this.plugin);

                           for (String lore : loreLines) {
                              if (lore.equals(target)) {
                                 return true;
                              }
                           }
                        }
                     }

                     for (String sxxx : this.plugin
                        .getConfigManager()
                        .getConfigSection("ignore.item.lore-contains", entity, permissionList)
                        .getStringList("ignore.item.lore-contains")) {
                        if (!sxxx.isEmpty()) {
                           String parsed = StringUtil.parseString(sxxx, this.plugin);
                           String[] parts = parsed.split("\\n", -1);
                           if (parts.length == 1) {
                              String needle = parts[0];

                              for (String lorex : loreLines) {
                                 if (lorex != null && !needle.isEmpty() && lorex.contains(needle)) {
                                    return true;
                                 }
                              }
                           } else {
                              int window = parts.length;

                              for (int start = 0; start <= loreLines.size() - window; start++) {
                                 boolean allMatch = true;

                                 for (int i = 0; i < window; i++) {
                                    String part = parts[i];
                                    String lorexx = loreLines.get(start + i);
                                    if (lorexx == null || part != null && !part.isEmpty() && !lorexx.contains(part)) {
                                       allMatch = false;
                                       break;
                                    }
                                 }

                                 if (allMatch) {
                                    return true;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            List<String> enchantList = this.plugin
               .getConfigManager()
               .getConfigSection("ignore.item.enchantment", entity, permissionList)
               .getStringList("ignore.item.enchantment");
            List<String> enchantContainsList = this.plugin
               .getConfigManager()
               .getConfigSection("ignore.item.enchantment-contains", entity, permissionList)
               .getStringList("ignore.item.enchantment-contains");

            for (Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {
               Enchantment enchantment = entry.getKey();
               int level = entry.getValue();
               String fullKey = enchantment.getKey().toString().toLowerCase(Locale.ROOT);
               String shortKey = enchantment.getKey().getKey().toLowerCase(Locale.ROOT);

               for (String configKeyRaw : enchantList) {
                  String configKey = StringUtil.parseString(configKeyRaw, this.plugin).toLowerCase(Locale.ROOT);
                  if (configKey.contains("@")) {
                     String[] parts = configKey.split("@");
                     if (parts.length == 2) {
                        String key = parts[0];
                        String lvl = parts[1];
                        if ((key.equals(fullKey) || key.equals(shortKey)) && String.valueOf(level).equals(lvl)) {
                           return true;
                        }
                     }
                  }
               }

               for (String containsKeyRaw : enchantContainsList) {
                  String containsKey = StringUtil.parseString(containsKeyRaw, this.plugin).toLowerCase(Locale.ROOT);
                  if (!containsKey.isEmpty() && (fullKey.contains(containsKey) || shortKey.contains(containsKey))) {
                     return true;
                  }
               }
            }

            List<String> nsKeys = this.plugin
               .getConfigManager()
               .getConfigSection("ignore.item.namespacedkeys", entity, permissionList)
               .getStringList("ignore.item.namespacedkeys");
            if (this.namespacedKeys(itemStack, nsKeys)) {
               return true;
            } else {
               List<String> nsKeysContains = this.plugin
                  .getConfigManager()
                  .getConfigSection("ignore.item.namespacedkeys-contains", entity, permissionList)
                  .getStringList("ignore.item.namespacedkeys-contains");
               if (this.namespacedKeysContains(itemStack, nsKeysContains)) {
                  return true;
               } else {
                  List<String> craftEngineKeys = this.plugin
                     .getConfigManager()
                     .getConfigSection("ignore.item.craftengine", entity, permissionList)
                     .getStringList("ignore.item.craftengine");
                  if (this.craftEngineItemId(itemStack, craftEngineKeys, false)) {
                     return true;
                  } else {
                     List<String> craftEngineKeysContains = this.plugin
                        .getConfigManager()
                        .getConfigSection("ignore.item.craftengine-contains", entity, permissionList)
                        .getStringList("ignore.item.craftengine-contains");
                     return this.craftEngineItemId(itemStack, craftEngineKeysContains, true);
                  }
               }
            }
         }
      } else {
         return true;
      }
   }

   public List<Grave> getAllGraves() {
      Map<UUID, Grave> graveMap = this.plugin.getCacheManager().getGraveMap();
      if (graveMap != null && !graveMap.isEmpty()) {
         List<Grave> snapshot = new ArrayList<>(graveMap.values());
         snapshot.removeIf(Objects::isNull);
         return snapshot;
      } else {
         return Collections.emptyList();
      }
   }

   public boolean isGraveAbandoned(Grave grave) {
      return grave.isAbandoned();
   }

   private boolean namespacedKeys(ItemStack itemStack, List<String> keysToMatch) {
      if (itemStack != null && keysToMatch != null && !keysToMatch.isEmpty()) {
         Set<String> haystack = this.extractNamespacedKeys(itemStack);
         if (haystack.isEmpty()) {
            return false;
         } else {
            for (String raw : keysToMatch) {
               String k = StringUtil.parseString(raw, this.plugin);
               if (k != null) {
                  k = k.trim();
                  if (!k.isEmpty()) {
                     k = k.toLowerCase(Locale.ROOT);
                     if (haystack.contains(k)) {
                        return true;
                     }
                  }
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private boolean namespacedKeysContains(ItemStack itemStack, List<String> patterns) {
      if (itemStack != null && patterns != null && !patterns.isEmpty()) {
         Set<String> haystack = this.extractNamespacedKeys(itemStack);
         if (haystack.isEmpty()) {
            return false;
         } else {
            for (String raw : patterns) {
               String p = StringUtil.parseString(raw, this.plugin);
               if (p != null) {
                  p = p.trim();
                  if (!p.isEmpty()) {
                     p = p.toLowerCase(Locale.ROOT);

                     for (String key : haystack) {
                        if (key.contains(p)) {
                           return true;
                        }
                     }
                  }
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private boolean craftEngineItemId(ItemStack itemStack, List<String> patterns, boolean contains) {
      if (itemStack != null && patterns != null && !patterns.isEmpty() && this.plugin.getIntegrationManager().hasCraftEngine()) {
         String id = this.plugin.getIntegrationManager().getCraftEngine().getCustomItemId(itemStack);
         if (id != null && !id.isBlank()) {
            String fullId = id.toLowerCase(Locale.ROOT);
            String shortId = fullId.contains(":") ? fullId.substring(fullId.indexOf(58) + 1) : fullId;
            Set<String> exactHaystack = Set.of(fullId, shortId);

            for (String raw : patterns) {
               String parsed = StringUtil.parseString(raw, this.plugin);
               if (parsed != null && !parsed.isBlank()) {
                  String needle = parsed.toLowerCase(Locale.ROOT).trim();
                  if (contains) {
                     if (fullId.contains(needle) || shortId.contains(needle)) {
                        return true;
                     }
                  } else if (exactHaystack.contains(needle)) {
                     return true;
                  }
               }
            }

            return false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private Set<String> extractNamespacedKeys(ItemStack itemStack) {
      Set<String> out = new HashSet<>();
      if (itemStack == null) {
         return out;
      } else {
         BiConsumer<NamespacedKey, Set<String>> addKey = (kx, set) -> {
            if (kx != null) {
               set.add(kx.toString().toLowerCase(Locale.ROOT));
               set.add(kx.getKey().toLowerCase(Locale.ROOT));
            }
         };

         for (Enchantment ench : itemStack.getEnchantments().keySet()) {
            NamespacedKey k = ench.getKey();
            if (!k.getKey().isEmpty()) {
               addKey.accept(k, out);
            }
         }

         ItemMeta meta = itemStack.getItemMeta();
         if (meta instanceof EnchantmentStorageMeta esm) {
            for (Enchantment enchx : esm.getStoredEnchants().keySet()) {
               NamespacedKey k = enchx.getKey();
               if (!k.getKey().isEmpty()) {
                  addKey.accept(k, out);
               }
            }
         }

         if (meta != null) {
            try {
               PersistentDataContainer pdc = meta.getPersistentDataContainer();

               for (NamespacedKey k : pdc.getKeys()) {
                  addKey.accept(k, out);
               }
            } catch (NoClassDefFoundError var9) {
            }
         }

         return out;
      }
   }
}
