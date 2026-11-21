package dev.cwhead.GravesX.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.data.HologramData;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.manager.EntityDataManager;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.LocationUtil;
import com.ranull.graves.util.StringUtil;
import dev.cwhead.GravesX.keys.GraveHologramKeys;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class ArmorStandManager extends EntityDataManager {
   private final Graves plugin;

   public ArmorStandManager(Graves plugin) {
      super(plugin);
      this.plugin = plugin;
   }

   public void createHologram(Location location, Grave grave) {
      if (!this.plugin.getVersionManager().is_v1_7()) {
         if (this.plugin.getConfigManager().getConfigSection("hologram.enabled", grave).getBoolean("hologram.enabled")) {
            double offsetX = this.plugin.getConfigManager().getConfigSection("hologram.offset.x", grave).getDouble("hologram.offset.x");
            double offsetY = this.plugin.getConfigManager().getConfigSection("hologram.offset.y", grave).getDouble("hologram.offset.y");
            double offsetZ = this.plugin.getConfigManager().getConfigSection("hologram.offset.z", grave).getDouble("hologram.offset.z");
            boolean marker = this.plugin.getConfigManager().getConfigSection("hologram.marker", grave).getBoolean("hologram.marker");
            Location base = LocationUtil.roundLocation(location).add(offsetX + 0.5, offsetY + (marker ? 0.49 : -0.49), offsetZ + 0.5);
            List<String> lineList = new ArrayList<>(this.plugin.getConfigManager().getConfigSection("hologram.line", grave).getStringList("hologram.line"));
            double lineHeight = this.plugin.getConfigManager().getConfigSection("hologram.height-line", grave).getDouble("hologram.height-line");
            int lineNumber = 0;
            Collections.reverse(lineList);

            for (String line : lineList) {
               Location lineLoc = base.clone().add(0.0, (lineNumber + 1) * lineHeight, 0.0);
               int finalLineNumber = lineNumber;
               this.executeRegion(lineLoc, () -> {
                  if (lineLoc.getWorld() != null) {
                     ArmorStand armorStand = (ArmorStand)lineLoc.getWorld().spawn(lineLoc, ArmorStand.class);
                     armorStand.setVisible(false);
                     armorStand.setGravity(false);
                     armorStand.setCustomNameVisible(true);
                     armorStand.setSmall(true);
                     String locKey = this.toLocKey(grave.getLocationDeath());
                     if (this.plugin.getVersionManager().hasPersistentData()) {
                        PersistentDataContainer pdc = armorStand.getPersistentDataContainer();
                        pdc.set(GraveHologramKeys.GRAVE_UUID, PersistentDataType.STRING, grave.getUUID().toString());
                        pdc.set(GraveHologramKeys.GRAVE_LOCATION, PersistentDataType.STRING, locKey);
                     }

                     String parsed = StringUtil.parseString(line, lineLoc, grave, this.plugin);
                     if (this.plugin.getIntegrationManager().hasMiniMessage()) {
                        armorStand.setCustomName(MiniMessage.parseString(parsed));
                     } else {
                        armorStand.setCustomName(parsed);
                     }

                     if (!this.plugin.getVersionManager().is_v1_7()) {
                        try {
                           armorStand.setMarker(marker);
                        } catch (NoSuchMethodError var10x) {
                        }
                     }

                     if (!this.plugin.getVersionManager().is_v1_7() && !this.plugin.getVersionManager().is_v1_8()) {
                        armorStand.setInvulnerable(true);
                     }

                     if (this.plugin.getVersionManager().hasScoreboardTags()) {
                        armorStand.getScoreboardTags().add("graveHologram");
                        armorStand.getScoreboardTags().add("graveHologramGraveUUID:" + grave.getUUID());
                        armorStand.getScoreboardTags().add("graveHologramGraveLocation:" + locKey);
                     }

                     HologramData hologramData = new HologramData(lineLoc, armorStand.getUniqueId(), grave.getUUID(), finalLineNumber);
                     this.plugin.getDataManager().addHologramData(hologramData);
                     if (this.plugin.getIntegrationManager().hasMultiPaper()) {
                        this.plugin.getIntegrationManager().getMultiPaper().notifyHologramCreation(hologramData);
                     }
                  }
               });
               lineNumber++;
            }
         }
      }
   }

   public void removeHologram(Grave grave) {
      if (grave == null) {
         this.plugin.debugMessage("[Holograms] removeHologram(grave) skipped: grave is null", 1);
      } else if (grave.getUUID() == null) {
         this.plugin.debugMessage("[Holograms] removeHologram(grave) skipped: grave UUID is null", 1);
      } else {
         this.plugin.debugMessage("[Holograms] removeHologram(grave=" + grave.getUUID() + ") resolving cached ArmorStand holograms", 1);
         List<HologramData> hologramDataList = this.getCachedHologramData(grave);
         this.plugin
            .debugMessage("[Holograms] removeHologram(grave=" + grave.getUUID() + ") resolved " + hologramDataList.size() + " cached ArmorStand hologram(s)", 1);
         if (hologramDataList.isEmpty()) {
            this.plugin.debugMessage("[Holograms] No cached ArmorStand holograms found for grave=" + grave.getUUID(), 2);
         } else {
            this.removeResolvedHolograms(hologramDataList);
         }
      }
   }

   @Deprecated(
      forRemoval = true
   )
   public void removeHologram(EntityData entityData) {
      String message = "ArmorStandManager#removeHologram(EntityData) is no longer supported. Use removeHologram(Grave) instead.";
      this.plugin.getLogger().severe(message + " entity=" + (entityData != null ? entityData.getUUIDEntity() : "null"));
      this.plugin.debugMessage("[Holograms] " + message, 1);
      throw new UnsupportedOperationException(message);
   }

   @Deprecated(
      forRemoval = true
   )
   public void removeHologram(Map<EntityData, Entity> entityDataMap) {
      String message = "ArmorStandManager#removeHologram(Map<EntityData, Entity>) is no longer supported. Use removeHologram(Grave) instead.";
      this.plugin.getLogger().severe(message + " size=" + (entityDataMap != null ? entityDataMap.size() : 0));
      this.plugin.debugMessage("[Holograms] " + message, 1);
      throw new UnsupportedOperationException(message);
   }

   private List<HologramData> getCachedHologramData(Grave grave) {
      List<HologramData> hologramDataList = new ArrayList<>();
      if (grave != null && grave.getUUID() != null) {
         for (EntityData entityData : this.plugin.getCacheManager().getEntityMap().values()) {
            if (entityData instanceof HologramData hologramData
               && grave.getUUID().equals(hologramData.getUUIDGrave())
               && hologramData.getBackend() == HologramData.Backend.ARMOR_STAND) {
               hologramDataList.add(hologramData);
            }
         }

         return hologramDataList;
      } else {
         return hologramDataList;
      }
   }

   private void removeResolvedHolograms(List<? extends EntityData> hologramDataList) {
      if (hologramDataList != null && !hologramDataList.isEmpty()) {
         this.plugin.debugMessage("[Holograms] removeResolvedHolograms count=" + hologramDataList.size(), 1);
         Map<EntityData, Entity> entityDataMap = this.getEntityDataMap(new ArrayList<>(hologramDataList));
         this.plugin
            .debugMessage(
               "[Holograms] Resolved " + entityDataMap.size() + " live entity reference(s) from " + hologramDataList.size() + " hologram data entrie(s)", 1
            );
         List<EntityData> removableEntityData = new ArrayList<>();

         for (EntityData data : hologramDataList) {
            if (data == null) {
               this.plugin.debugMessage("[Holograms] Skipping null EntityData entry during removal", 2);
            } else if (data instanceof HologramData hologramData) {
               if (hologramData.getBackend() != HologramData.Backend.ARMOR_STAND) {
                  this.plugin
                     .debugMessage("[Holograms] Skipping non-ArmorStand hologram uuid=" + data.getUUIDEntity() + ", backend=" + hologramData.getBackend(), 2);
               } else {
                  removableEntityData.add(data);
                  Entity finalEntity = entityDataMap.get(data);
                  Location finalGraveLocation = data.getLocation();
                  UUID finalGraveUUID = data.getUUIDGrave();
                  this.plugin
                     .debugMessage("[Holograms] Scheduling ArmorStand hologram removal for entity=" + data.getUUIDEntity() + ", grave=" + finalGraveUUID, 1);
                  Runnable remover = () -> {
                     int removedCount = 0;

                     try {
                        if (finalEntity != null && finalEntity.isValid()) {
                           finalEntity.remove();
                           removedCount++;
                           this.plugin.debugMessage("[Holograms] Removed direct ArmorStand entity=" + finalEntity.getUniqueId(), 2);
                        }
                     } catch (Throwable var14) {
                        this.plugin.debugMessage("[Holograms] Failed direct entity removal for uuid=" + data.getUUIDEntity() + ": " + var14.getMessage(), 1);
                     }

                     if (finalGraveLocation != null && finalGraveLocation.getWorld() != null) {
                        try {
                           for (Entity e : finalGraveLocation.getWorld().getNearbyEntities(finalGraveLocation, 2.0, 2.0, 2.0)) {
                              if (e instanceof ArmorStand && e.isValid()) {
                                 boolean remove = false;

                                 try {
                                    if (this.plugin.getVersionManager().hasPersistentData()) {
                                       PersistentDataContainer pdc = e.getPersistentDataContainer();
                                       String storedUuid = (String)pdc.get(GraveHologramKeys.GRAVE_UUID, PersistentDataType.STRING);
                                       if (finalGraveUUID != null && storedUuid != null && storedUuid.equals(finalGraveUUID.toString())) {
                                          remove = true;
                                       }
                                    }
                                 } catch (Throwable var13) {
                                 }

                                 try {
                                    if (!remove && this.plugin.getVersionManager().hasScoreboardTags()) {
                                       remove = e.getScoreboardTags().contains("graveHologramGraveUUID:" + finalGraveUUID);
                                    }
                                 } catch (Throwable var12) {
                                 }

                                 if (remove) {
                                    e.remove();
                                    removedCount++;
                                    this.plugin
                                       .debugMessage("[Holograms] Removed matched ArmorStand entity=" + e.getUniqueId() + " for grave=" + finalGraveUUID, 2);
                                 }
                              }
                           }

                           this.plugin
                              .debugMessage("[Holograms] Removal sweep finished for hologram entity=" + data.getUUIDEntity() + ", removed=" + removedCount, 2);
                        } catch (Throwable var15) {
                           this.plugin
                              .getLogger()
                              .severe(
                                 "Failed removing holograms at world: "
                                    + finalGraveLocation.getWorld().getName()
                                    + ", x: "
                                    + finalGraveLocation.getBlockX()
                                    + ", y: "
                                    + finalGraveLocation.getBlockY()
                                    + ", z: "
                                    + finalGraveLocation.getBlockZ()
                              );
                           this.plugin.logStackTrace(var15);
                        }
                     } else {
                        this.plugin
                           .debugMessage("[Holograms] No valid location world for hologram entity=" + data.getUUIDEntity() + ", skipping nearby sweep", 2);
                     }
                  };
                  if (finalEntity != null) {
                     this.plugin.debugMessage("[Holograms] Executing removal on entity region for entity=" + finalEntity.getUniqueId(), 2);
                     this.executeRegion(finalEntity, remover);
                  } else if (finalGraveLocation != null && finalGraveLocation.getWorld() != null) {
                     this.plugin.debugMessage("[Holograms] Executing removal on location region for hologram entity=" + data.getUUIDEntity(), 2);
                     this.executeRegion(finalGraveLocation, remover);
                  } else {
                     this.plugin.debugMessage("[Holograms] Executing fallback main-thread removal for hologram entity=" + data.getUUIDEntity(), 2);
                     this.plugin.getServer().getScheduler().runTask(this.plugin, remover);
                  }
               }
            } else {
               this.plugin.debugMessage("[Holograms] Skipping non-hologram EntityData uuid=" + data.getUUIDEntity(), 2);
            }
         }

         if (!removableEntityData.isEmpty()) {
            this.plugin.debugMessage("[Holograms] Removing " + removableEntityData.size() + " hologram entity-data entrie(s) from DataManager records", 1);
            this.plugin.getDataManager().removeEntityData(removableEntityData);
         } else {
            this.plugin.debugMessage("[Holograms] No ArmorStand hologram entity-data entries qualified for DB/cache removal", 2);
         }
      } else {
         this.plugin.debugMessage("[Holograms] removeResolvedHolograms skipped: no hologram data", 2);
      }
   }

   public void purgeLingeringHolograms() {
      this.plugin.debugMessage("[Cleanup] Starting ArmorStand hologram purge across all worlds", 1);

      for (World world : this.plugin.getServer().getWorlds()) {
         this.plugin.debugMessage("[Cleanup] Scanning world " + world.getName() + " for lingering ArmorStand holograms", 2);

         for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
            this.executeRegion(stand, () -> {
               boolean hasHoloTag = false;

               try {
                  if (this.plugin.getVersionManager().hasScoreboardTags()) {
                     hasHoloTag = stand.getScoreboardTags().contains("graveHologram");
                  }
               } catch (Throwable var11) {
               }

               boolean hasPdc = false;

               try {
                  if (this.plugin.getVersionManager().hasPersistentData()) {
                     PersistentDataContainer pdc = stand.getPersistentDataContainer();
                     hasPdc = pdc.has(GraveHologramKeys.GRAVE_UUID, PersistentDataType.STRING);
                  }
               } catch (Throwable var10) {
               }

               if (hasHoloTag || hasPdc) {
                  UUID graveUUID = null;

                  try {
                     if (this.plugin.getVersionManager().hasPersistentData()) {
                        PersistentDataContainer pdc = stand.getPersistentDataContainer();
                        String raw = (String)pdc.get(GraveHologramKeys.GRAVE_UUID, PersistentDataType.STRING);
                        if (raw != null) {
                           graveUUID = UUID.fromString(raw);
                        }
                     }
                  } catch (Throwable var9) {
                  }

                  if (graveUUID == null) {
                     try {
                        graveUUID = this.extractGraveUUIDFromStand(stand);
                     } catch (Throwable var8) {
                     }
                  }

                  if (graveUUID == null) {
                     stand.remove();
                     this.plugin.debugMessage("[Cleanup] Removed hologram missing grave UUID entity=" + stand.getUniqueId(), 2);
                  } else {
                     Grave grave = null;

                     try {
                        grave = this.hasGrave(graveUUID);
                     } catch (Throwable var7) {
                     }

                     if (grave == null) {
                        stand.remove();
                        this.plugin.debugMessage("[Cleanup] Removed hologram for missing grave " + graveUUID, 2);
                     } else {
                        boolean hologramExists = this.isHologramExists(graveUUID);
                        if (!hologramExists) {
                           stand.remove();
                           this.plugin.debugMessage("[Cleanup] Removed orphaned hologram entity=" + stand.getUniqueId() + " grave=" + graveUUID, 2);
                        } else {
                           if (!this.isCachedHologramEntity(graveUUID, stand.getUniqueId())) {
                              stand.remove();
                              this.plugin.debugMessage("[Cleanup] Removed stale hologram entity=" + stand.getUniqueId() + " grave=" + graveUUID, 2);
                           }
                        }
                     }
                  }
               }
            });
         }
      }

      this.plugin.debugMessage("[Cleanup] Finished ArmorStand hologram purge", 1);
   }

   public void purgeWorldArmorStandHolograms(Grave grave) {
      if (grave != null && grave.getUUID() != null) {
         UUID targetGraveUUID = grave.getUUID();
         this.plugin.debugMessage("[Holograms] Starting targeted ArmorStand world purge for grave=" + targetGraveUUID, 1);

         for (World world : this.plugin.getServer().getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
               this.executeRegion(stand, () -> {
                  UUID standGraveUUID = this.getArmorStandGraveUUID(stand);
                  if (targetGraveUUID.equals(standGraveUUID)) {
                     if (stand.isValid()) {
                        stand.remove();
                        this.plugin.debugMessage("[Holograms] Purged ArmorStand hologram entity=" + stand.getUniqueId() + " grave=" + targetGraveUUID, 2);
                     }
                  }
               });
            }
         }
      }
   }

   private boolean isHologramExists(UUID graveUUID) {
      boolean hologramExists = false;

      try {
         for (EntityData entityData : this.plugin.getCacheManager().getEntityMap().values()) {
            if (entityData instanceof HologramData hologramData
               && hologramData.getBackend() == HologramData.Backend.ARMOR_STAND
               && graveUUID.equals(hologramData.getUUIDGrave())) {
               hologramExists = true;
               break;
            }
         }
      } catch (Throwable var6) {
      }

      return hologramExists;
   }

   private boolean isCachedHologramEntity(UUID graveUUID, UUID entityUUID) {
      if (graveUUID != null && entityUUID != null) {
         try {
            for (EntityData entityData : this.plugin.getCacheManager().getEntityMap().values()) {
               if (entityData instanceof HologramData hologramData
                  && hologramData.getBackend() == HologramData.Backend.ARMOR_STAND
                  && graveUUID.equals(hologramData.getUUIDGrave())
                  && entityUUID.equals(hologramData.getUUIDEntity())) {
                  return true;
               }
            }
         } catch (Throwable var6) {
         }

         return false;
      } else {
         return false;
      }
   }

   public Grave hasGrave(UUID graveUUID) {
      return graveUUID == null ? null : this.plugin.getCacheManager().getGraveMap().get(graveUUID);
   }

   private UUID extractGraveUUIDFromStand(ArmorStand stand) {
      if (stand == null) {
         return null;
      } else {
         try {
            for (String tag : stand.getScoreboardTags()) {
               if (tag.startsWith("graveHologramGraveUUID:")) {
                  String raw = tag.substring("graveHologramGraveUUID:".length()).trim();

                  try {
                     return UUID.fromString(raw);
                  } catch (IllegalArgumentException var6) {
                     this.plugin.debugMessage("[Cleanup] Invalid grave UUID tag on ArmorStand " + stand.getUniqueId() + ": " + raw, 2);
                     return null;
                  }
               }
            }
         } catch (Throwable var7) {
         }

         return null;
      }
   }

   private UUID getArmorStandGraveUUID(ArmorStand stand) {
      if (stand == null) {
         return null;
      } else {
         try {
            if (this.plugin.getVersionManager().hasPersistentData()) {
               PersistentDataContainer pdc = stand.getPersistentDataContainer();
               String raw = (String)pdc.get(GraveHologramKeys.GRAVE_UUID, PersistentDataType.STRING);
               if (raw != null) {
                  return UUID.fromString(raw);
               }
            }
         } catch (Throwable var4) {
         }

         return this.extractGraveUUIDFromStand(stand);
      }
   }

   private String toLocKey(Location loc) {
      return loc != null && loc.getWorld() != null ? loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ() : null;
   }

   private void executeRegion(Location loc, Runnable task) {
      if (this.plugin.getVersionManager().isFolia()) {
         SchedulerManager sched = this.plugin.getSchedulerManager();
         if (sched != null) {
            sched.execute(loc, task);
            return;
         }
      }

      if (this.plugin.getVersionManager().isPaper()) {
         SchedulerManager sched = this.plugin.getSchedulerManager();
         if (sched != null) {
            sched.runTask(task);
            return;
         }
      }

      this.plugin.getServer().getScheduler().runTask(this.plugin, task);
   }

   private void executeRegion(Entity entity, Runnable task) {
      if (this.plugin.getVersionManager().isFolia()) {
         SchedulerManager sched = this.plugin.getSchedulerManager();
         if (sched != null) {
            sched.execute(entity, task);
            return;
         }
      }

      if (!this.plugin.getVersionManager().isPaper() || !this.executePaperEntityTask(entity, task)) {
         this.plugin.getServer().getScheduler().runTask(this.plugin, task);
      }
   }

   private boolean executePaperEntityTask(Entity entity, Runnable task) {
      if (entity == null) {
         return false;
      } else {
         try {
            Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
            scheduler.getClass()
               .getMethod("execute", Plugin.class, Runnable.class, Runnable.class, long.class)
               .invoke(scheduler, this.plugin, task, (Runnable)() -> {}, 1L);
            return true;
         } catch (Throwable var4) {
            return false;
         }
      }
   }
}
