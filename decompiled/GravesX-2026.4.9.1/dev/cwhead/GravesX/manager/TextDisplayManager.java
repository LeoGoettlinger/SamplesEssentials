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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.TextDisplay.TextAlignment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class TextDisplayManager extends EntityDataManager {
   private final Graves plugin;

   public TextDisplayManager(Graves plugin) {
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
            List<String> lines = new ArrayList<>(this.plugin.getConfigManager().getConfigSection("hologram.line", grave).getStringList("hologram.line"));
            if (!lines.isEmpty()) {
               double lineHeight = this.plugin.getConfigManager().getConfigSection("hologram.height-line", grave).getDouble("hologram.height-line", 0.28);
               int lineCount = lines.size();
               double centerShiftY = (lineCount + 1) / 2.0 * lineHeight;
               Location spawnLoc = base.clone().add(0.0, centerShiftY, 0.0);
               StringBuilder sb = new StringBuilder();

               for (int i = 0; i < lineCount; i++) {
                  String raw = lines.get(i);
                  int multiplier = lineCount - i;
                  Location lineLocForParse = base.clone().add(0.0, multiplier * lineHeight, 0.0);
                  String parsed = StringUtil.parseString(raw, lineLocForParse, grave, this.plugin);
                  if (i > 0) {
                     sb.append('\n');
                  }

                  sb.append(parsed);
               }

               String finalText = sb.toString();
               this.executeRegion(spawnLoc, () -> {
                  TextDisplay display = this.spawnTextDisplay(spawnLoc);
                  if (display != null) {
                     String locKey = this.toLocKey(grave.getLocationDeath());
                     if (this.plugin.getVersionManager().hasPersistentData()) {
                        try {
                           PersistentDataContainer pdc = display.getPersistentDataContainer();
                           pdc.set(GraveHologramKeys.GRAVE_UUID, PersistentDataType.STRING, grave.getUUID().toString());
                           pdc.set(GraveHologramKeys.GRAVE_LOCATION, PersistentDataType.STRING, locKey);
                        } catch (Throwable var8) {
                        }
                     }

                     if (this.plugin.getVersionManager().hasScoreboardTags()) {
                        try {
                           display.getScoreboardTags().add("graveHologram");
                           display.getScoreboardTags().add("graveHologramGraveUUID:" + grave.getUUID());
                           display.getScoreboardTags().add("graveHologramGraveLocation:" + locKey);
                        } catch (Throwable var7x) {
                        }
                     }

                     this.applyDisplayOptions(display, grave);
                     if (this.plugin.getIntegrationManager().hasMiniMessage()) {
                        display.setText(MiniMessage.parseString(finalText));
                     } else {
                        display.setText(finalText);
                     }

                     HologramData hologramData = new HologramData(spawnLoc, display.getUniqueId(), grave.getUUID(), 0, HologramData.Backend.TEXT_DISPLAY);
                     this.plugin.getDataManager().addHologramData(hologramData);
                     if (this.plugin.getIntegrationManager().hasMultiPaper()) {
                        this.plugin.getIntegrationManager().getMultiPaper().notifyHologramCreation(hologramData);
                     }
                  }
               });
            }
         }
      }
   }

   public void removeHologram(Grave grave) {
      if (grave == null) {
         this.plugin.debugMessage("[Holograms] removeTextDisplayHologram(grave) skipped: grave is null", 1);
      } else if (grave.getUUID() == null) {
         this.plugin.debugMessage("[Holograms] removeTextDisplayHologram(grave) skipped: grave UUID is null", 1);
      } else {
         this.plugin.debugMessage("[Holograms] removeTextDisplayHologram(grave=" + grave.getUUID() + ") resolving cached TextDisplay holograms", 1);
         List<HologramData> hologramDataList = this.getCachedHologramData(grave, HologramData.Backend.TEXT_DISPLAY);
         this.plugin
            .debugMessage(
               "[Holograms] removeTextDisplayHologram(grave=" + grave.getUUID() + ") resolved " + hologramDataList.size() + " cached TextDisplay hologram(s)",
               1
            );
         if (hologramDataList.isEmpty()) {
            this.plugin.debugMessage("[Holograms] No cached TextDisplay holograms found for grave=" + grave.getUUID(), 2);
         } else {
            this.removeResolvedHolograms(hologramDataList);
         }
      }
   }

   @Deprecated(
      forRemoval = true
   )
   public void removeHologram(EntityData entityData) {
      String message = "TextDisplayManager#removeHologram(EntityData) is no longer supported. Use removeHologram(Grave) instead.";
      this.plugin.getLogger().severe(message + " entity=" + (entityData != null ? entityData.getUUIDEntity() : "null"));
      this.plugin.debugMessage("[Holograms] " + message, 1);
      throw new UnsupportedOperationException(message);
   }

   @Deprecated(
      forRemoval = true
   )
   public void removeHologram(Map<EntityData, Entity> entityDataMap) {
      String message = "TextDisplayManager#removeHologram(Map<EntityData, Entity>) is no longer supported. Use removeHologram(Grave) instead.";
      this.plugin.getLogger().severe(message + " size=" + (entityDataMap != null ? entityDataMap.size() : 0));
      this.plugin.debugMessage("[Holograms] " + message, 1);
      throw new UnsupportedOperationException(message);
   }

   private List<HologramData> getCachedHologramData(Grave grave, HologramData.Backend backend) {
      List<HologramData> hologramDataList = new ArrayList<>();
      if (grave != null && grave.getUUID() != null) {
         for (EntityData entityData : this.plugin.getCacheManager().getEntityMap().values()) {
            if (entityData instanceof HologramData hologramData
               && grave.getUUID().equals(hologramData.getUUIDGrave())
               && (backend == null || hologramData.getBackend() == backend)) {
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
         this.plugin.debugMessage("[Holograms] removeResolvedTextDisplays count=" + hologramDataList.size(), 1);
         Map<EntityData, Entity> entityDataMap = this.getEntityDataMap(new ArrayList<>(hologramDataList));
         List<EntityData> removableEntityData = new ArrayList<>();

         for (EntityData data : hologramDataList) {
            if (data != null && data instanceof HologramData hologramData && hologramData.getBackend() == HologramData.Backend.TEXT_DISPLAY) {
               removableEntityData.add(data);
               Entity finalEntity = entityDataMap.get(data);
               Location finalLocation = data.getLocation();
               UUID finalGraveUUID = data.getUUIDGrave();
               Runnable remover = () -> {
                  int removedCount = 0;

                  try {
                     if (finalEntity instanceof TextDisplay td && td.isValid()) {
                        td.remove();
                        removedCount++;
                        this.plugin.debugMessage("[Holograms] Removed direct TextDisplay entity=" + td.getUniqueId(), 2);
                     }
                  } catch (Throwable var15) {
                     this.plugin.debugMessage("[Holograms] Failed direct TextDisplay removal entity=" + data.getUUIDEntity() + ": " + var15.getMessage(), 1);
                  }

                  if (finalLocation != null && finalLocation.getWorld() != null) {
                     try {
                        for (Entity e : finalLocation.getWorld().getNearbyEntities(finalLocation, 2.0, 2.0, 2.0)) {
                           if (e instanceof TextDisplay td && td.isValid()) {
                              boolean remove = false;

                              try {
                                 if (this.plugin.getVersionManager().hasPersistentData()) {
                                    PersistentDataContainer pdc = td.getPersistentDataContainer();
                                    String storedUuid = (String)pdc.get(GraveHologramKeys.GRAVE_UUID, PersistentDataType.STRING);
                                    if (finalGraveUUID != null && storedUuid != null && storedUuid.equals(finalGraveUUID.toString())) {
                                       remove = true;
                                    }
                                 }
                              } catch (Throwable var14) {
                              }

                              try {
                                 if (!remove && this.plugin.getVersionManager().hasScoreboardTags()) {
                                    remove = td.getScoreboardTags().contains("graveHologramGraveUUID:" + finalGraveUUID);
                                 }
                              } catch (Throwable var13) {
                              }

                              if (remove) {
                                 td.remove();
                                 removedCount++;
                                 this.plugin.debugMessage("[Holograms] Removed matched TextDisplay entity=" + td.getUniqueId() + " grave=" + finalGraveUUID, 2);
                              }
                           }
                        }

                        this.plugin
                           .debugMessage("[Holograms] TextDisplay removal sweep finished entity=" + data.getUUIDEntity() + ", removed=" + removedCount, 2);
                     } catch (Throwable var16) {
                        this.plugin.logStackTrace(var16);
                     }
                  }
               };
               if (finalEntity != null) {
                  this.executeRegion(finalEntity, remover);
               } else if (finalLocation != null && finalLocation.getWorld() != null) {
                  this.executeRegion(finalLocation, remover);
               } else {
                  this.plugin.getServer().getScheduler().runTask(this.plugin, remover);
               }
            }
         }

         if (!removableEntityData.isEmpty()) {
            this.plugin.getDataManager().removeEntityData(removableEntityData);
         }
      } else {
         this.plugin.debugMessage("[Holograms] removeResolvedTextDisplays skipped: no hologram data", 2);
      }
   }

   public void updateTextDisplay(Entity entity, List<String> cfgLines, double lineHeight, Location base, Grave grave) {
      if (entity instanceof TextDisplay td) {
         this.updateTextDisplay(td, cfgLines, lineHeight, base, grave);
      }
   }

   private void updateTextDisplay(TextDisplay textDisplay, List<String> cfgLines, double lineHeight, Location base, Grave grave) {
      this.applyDisplayOptions(textDisplay, grave);
      List<String> cfgLinesOriginalOrder = new ArrayList<>(cfgLines);
      int lineCount = cfgLinesOriginalOrder.size();
      double centerShiftY = (lineCount + 1) / 2.0 * lineHeight;
      Location expectedLoc = base.clone().add(0.0, centerShiftY, 0.0);

      try {
         Location cur = textDisplay.getLocation();
         if (cur.getWorld() != null && expectedLoc.getWorld() != null && cur.getWorld().equals(expectedLoc.getWorld())) {
            double dx = cur.getX() - expectedLoc.getX();
            double dy = cur.getY() - expectedLoc.getY();
            double dz = cur.getZ() - expectedLoc.getZ();
            if (dx * dx + dy * dy + dz * dz > 1.0E-4) {
               textDisplay.teleport(expectedLoc);
            }
         }
      } catch (Throwable var19) {
         this.plugin
            .debugMessage(
               "Failed to get Target Location for grave "
                  + grave.getUUID()
                  + ". Holograms will not update. \n"
                  + Arrays.toString((Object[])var19.getStackTrace()),
               2
            );
      }

      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < lineCount; i++) {
         String raw = cfgLinesOriginalOrder.get(i);
         int multiplier = lineCount - i;
         Location lineLocForParse = base.clone().add(0.0, multiplier * lineHeight, 0.0);
         String parsed = StringUtil.parseString(raw, lineLocForParse, grave, this.plugin);
         if (i > 0) {
            sb.append('\n');
         }

         sb.append(parsed);
      }

      String fullText = sb.toString();
      if (this.plugin.getIntegrationManager().hasMiniMessage()) {
         textDisplay.setText(MiniMessage.parseString(fullText));
      } else {
         textDisplay.setText(fullText);
      }
   }

   public void purgeLingeringHolograms() {
      this.plugin.debugMessage("[Cleanup] Starting TextDisplay hologram purge across all worlds", 1);

      for (World world : this.plugin.getServer().getWorlds()) {
         this.plugin.debugMessage("[Cleanup] Scanning world " + world.getName() + " for lingering TextDisplays", 2);

         for (TextDisplay td : world.getEntitiesByClass(TextDisplay.class)) {
            this.executeRegion(td, () -> {
               if (td.isValid()) {
                  boolean hasHoloTag = false;

                  try {
                     if (this.plugin.getVersionManager().hasScoreboardTags()) {
                        hasHoloTag = td.getScoreboardTags().contains("graveHologram");
                     }
                  } catch (Throwable var11) {
                  }

                  boolean hasPdc = false;

                  try {
                     if (this.plugin.getVersionManager().hasPersistentData()) {
                        PersistentDataContainer pdc = td.getPersistentDataContainer();
                        hasPdc = pdc.has(GraveHologramKeys.GRAVE_UUID, PersistentDataType.STRING);
                     }
                  } catch (Throwable var10) {
                  }

                  if (hasHoloTag || hasPdc) {
                     UUID graveUUID = null;

                     try {
                        if (this.plugin.getVersionManager().hasPersistentData()) {
                           PersistentDataContainer pdc = td.getPersistentDataContainer();
                           String raw = (String)pdc.get(GraveHologramKeys.GRAVE_UUID, PersistentDataType.STRING);
                           if (raw != null) {
                              graveUUID = UUID.fromString(raw);
                           }
                        }
                     } catch (Throwable var9) {
                     }

                     if (graveUUID == null) {
                        try {
                           graveUUID = this.extractGraveUUIDFromTags(td);
                        } catch (Throwable var8) {
                        }
                     }

                     if (graveUUID == null) {
                        td.remove();
                        this.plugin.debugMessage("[Cleanup] Removed TextDisplay missing grave UUID entity=" + td.getUniqueId(), 2);
                     } else {
                        Grave grave = null;

                        try {
                           grave = this.hasGrave(graveUUID);
                        } catch (Throwable var7) {
                        }

                        if (grave == null) {
                           td.remove();
                           this.plugin.debugMessage("[Cleanup] Removed TextDisplay for missing grave " + graveUUID, 2);
                        } else {
                           boolean hologramExists = this.isHologramExists(graveUUID);
                           if (!hologramExists) {
                              td.remove();
                              this.plugin.debugMessage("[Cleanup] Removed orphaned TextDisplay entity=" + td.getUniqueId() + " grave=" + graveUUID, 2);
                           } else {
                              if (!this.isCachedHologramEntity(graveUUID, td.getUniqueId())) {
                                 td.remove();
                                 this.plugin.debugMessage("[Cleanup] Removed stale TextDisplay entity=" + td.getUniqueId() + " grave=" + graveUUID, 2);
                              }
                           }
                        }
                     }
                  }
               }
            });
         }
      }

      this.plugin.debugMessage("[Cleanup] Finished TextDisplay hologram purge", 1);
   }

   private boolean isHologramExists(UUID graveUUID) {
      boolean hologramExists = false;

      try {
         for (EntityData entityData : this.plugin.getCacheManager().getEntityMap().values()) {
            if (entityData instanceof HologramData hologramData
               && hologramData.getBackend() == HologramData.Backend.TEXT_DISPLAY
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
                  && hologramData.getBackend() == HologramData.Backend.TEXT_DISPLAY
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
      return this.plugin.getCacheManager().getGraveMap().get(graveUUID);
   }

   private TextDisplay spawnTextDisplay(Location loc) {
      if (loc != null && loc.getWorld() != null) {
         try {
            return loc.getWorld().spawnEntity(loc, EntityType.TEXT_DISPLAY) instanceof TextDisplay td ? td : null;
         } catch (Throwable var4) {
            return null;
         }
      } else {
         return null;
      }
   }

   private void applyDisplayOptions(TextDisplay display, Grave grave) {
      if (display != null) {
         boolean seeThrough = this.plugin
            .getConfigManager()
            .getConfigSection("hologram.text-display.see-through", grave)
            .getBoolean("hologram.text-display.see-through", true);
         display.setSeeThrough(seeThrough);
         boolean shadowed = this.plugin
            .getConfigManager()
            .getConfigSection("hologram.text-display.shadowed", grave)
            .getBoolean("hologram.text-display.shadowed", false);
         display.setShadowed(shadowed);
         boolean defaultBackground = this.plugin
            .getConfigManager()
            .getConfigSection("hologram.text-display.default-background", grave)
            .getBoolean("hologram.text-display.default-background", false);
         display.setDefaultBackground(defaultBackground);
         int textOpacity = this.plugin
            .getConfigManager()
            .getConfigSection("hologram.text-display.text-opacity", grave)
            .getInt("hologram.text-display.text-opacity", 255);
         display.setTextOpacity((byte)Math.max(0, Math.min(255, textOpacity)));
         String bg = this.plugin
            .getConfigManager()
            .getConfigSection("hologram.text-display.background", grave)
            .getString("hologram.text-display.background", "");
         if (bg != null && !bg.isBlank()) {
            Integer argb = this.parseHexColorToARGB(bg.trim());
            if (argb != null) {
               display.setBackgroundColor(Color.fromARGB(argb));
            }
         }

         int lineWidth = this.plugin
            .getConfigManager()
            .getConfigSection("hologram.text-display.line-width", grave)
            .getInt("hologram.text-display.line-width", 0);
         if (lineWidth > 0) {
            display.setLineWidth(lineWidth);
         }

         String alignRaw = this.plugin
            .getConfigManager()
            .getConfigSection("hologram.text-display.alignment", grave)
            .getString("hologram.text-display.alignment", "CENTER");
         display.setAlignment(this.parseAlignment(alignRaw));
         String billboardRaw = this.plugin
            .getConfigManager()
            .getConfigSection("hologram.text-display.billboard", grave)
            .getString("hologram.text-display.billboard", "CENTER");
         display.setBillboard(this.parseBillboard(billboardRaw));
      }
   }

   private TextAlignment parseAlignment(String raw) {
      if (raw == null) {
         return TextAlignment.CENTER;
      } else {
         try {
            return TextAlignment.valueOf(raw.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var3) {
            return TextAlignment.CENTER;
         }
      }
   }

   private Billboard parseBillboard(String raw) {
      if (raw == null) {
         return Billboard.CENTER;
      } else {
         try {
            return Billboard.valueOf(raw.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var3) {
            return Billboard.CENTER;
         }
      }
   }

   private Integer parseHexColorToARGB(String raw) {
      if (raw == null) {
         return null;
      } else {
         String s = raw.trim();
         if (s.startsWith("#")) {
            s = s.substring(1);
         }

         if (s.startsWith("0x") || s.startsWith("0X")) {
            s = s.substring(2);
         }

         if (s.length() == 6) {
            s = "FF" + s;
         }

         if (s.length() != 8) {
            return null;
         } else {
            try {
               long v = Long.parseLong(s, 16);
               return (int)(v & 4294967295L);
            } catch (NumberFormatException var5) {
               return null;
            }
         }
      }
   }

   private UUID extractGraveUUIDFromTags(Entity entity) {
      if (!this.plugin.getVersionManager().hasScoreboardTags()) {
         return null;
      } else {
         try {
            for (String tag : entity.getScoreboardTags()) {
               if (tag.startsWith("graveHologramGraveUUID:")) {
                  String raw = tag.substring("graveHologramGraveUUID:".length()).trim();

                  try {
                     return UUID.fromString(raw);
                  } catch (IllegalArgumentException var6) {
                     return null;
                  }
               }
            }
         } catch (Throwable var7) {
         }

         return null;
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
