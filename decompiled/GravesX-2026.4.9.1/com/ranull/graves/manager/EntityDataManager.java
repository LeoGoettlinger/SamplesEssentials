package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.data.ChunkData;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.type.Grave;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class EntityDataManager {
   private final Graves plugin;
   private static Method SERVER_GET_ENTITY;

   public EntityDataManager(Graves plugin) {
      this.plugin = plugin;
   }

   public void createEntityData(Entity entity, Grave grave, EntityData.Type type) {
      this.createEntityData(entity.getLocation(), entity.getUniqueId(), grave.getUUID(), type);
   }

   public void createEntityData(Location location, UUID entityUUID, UUID graveUUID, EntityData.Type type) {
      EntityData entityData = new EntityData(location.clone(), entityUUID, graveUUID, type);
      this.plugin.getDataManager().addEntityData(entityData);
      if (this.plugin.getIntegrationManager().hasMultiPaper()) {
         this.plugin.getIntegrationManager().getMultiPaper().notifyEntityCreation(entityData);
      }
   }

   public EntityData getEntityData(Location location, UUID uuid) {
      if (this.plugin.getDataManager().hasChunkData(location)) {
         ChunkData chunkData = this.plugin.getDataManager().getChunkData(location);
         if (chunkData.getEntityDataMap().containsKey(uuid)) {
            return chunkData.getEntityDataMap().get(uuid);
         }
      }

      return null;
   }

   public Grave getGrave(Location location, UUID uuid) {
      EntityData entityData = this.getEntityData(location, uuid);
      return entityData != null && this.plugin.getCacheManager().getGraveMap().containsKey(entityData.getUUIDGrave())
         ? this.plugin.getCacheManager().getGraveMap().get(entityData.getUUIDGrave())
         : null;
   }

   public Grave getGrave(Entity entity) {
      return this.getGrave(entity.getLocation(), entity.getUniqueId());
   }

   public void removeEntityData(EntityData entityData) {
      this.removeEntityData(Collections.singletonList(entityData));
   }

   public List<EntityData> getLoadedEntityDataList(Grave grave) {
      List<EntityData> entityDataList = new ArrayList<>();

      for (Entry<String, ChunkData> chunkDataEntry : this.plugin.getCacheManager().getChunkMap().entrySet()) {
         ChunkData chunkData = chunkDataEntry.getValue();
         if (chunkData.isLoaded()) {
            for (EntityData entityData : new ArrayList<>(chunkData.getEntityDataMap().values())) {
               if (entityData != null && grave.getUUID().equals(entityData.getUUIDGrave())) {
                  entityDataList.add(entityData);
               }
            }
         }
      }

      return entityDataList;
   }

   public Map<EntityData, Entity> getEntityDataMap(List<EntityData> entityDataList) {
      Map<EntityData, Entity> entityDataMap = new HashMap<>();

      for (EntityData entityData : entityDataList) {
         if (entityData != null) {
            Location location = entityData.getLocation();
            if (location != null) {
               World world = location.getWorld();
               if (world != null) {
                  UUID uuid = entityData.getUUIDEntity();
                  if (uuid != null) {
                     Entity found = this.fastGetEntity(uuid);
                     if (found != null) {
                        entityDataMap.put(entityData, found);
                     } else {
                        int cx = location.getBlockX() >> 4;
                        int cz = location.getBlockZ() >> 4;
                        AtomicReference<Entity> ref = new AtomicReference<>(null);
                        CountDownLatch latch = new CountDownLatch(1);
                        this.plugin.getChunkManager().ensureLoadedAndExecute(location, location, false, false, () -> {
                           try {
                              Entity quick = this.fastGetEntity(uuid);
                              if (quick == null) {
                                 Chunk chunk = world.getChunkAt(cx, cz);

                                 for (Entity e : chunk.getEntities()) {
                                    if (e != null && uuid.equals(e.getUniqueId())) {
                                       ref.set(e);
                                       return;
                                    }
                                 }

                                 return;
                              }

                              ref.set(quick);
                           } catch (Throwable var16) {
                              this.plugin.getLogger().severe(var16.getMessage());
                              this.plugin.logStackTrace(var16);
                              return;
                           } finally {
                              latch.countDown();
                           }
                        });

                        try {
                           latch.await(25L, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException var14) {
                           Thread.currentThread().interrupt();
                        }

                        Entity scanned = ref.get();
                        if (scanned != null) {
                           entityDataMap.put(entityData, scanned);
                        }
                     }
                  }
               }
            }
         }
      }

      return entityDataMap;
   }

   public void removeEntityData(List<EntityData> entityDataList) {
      List<EntityData> removedEntityDataList = new ArrayList<>();

      for (EntityData entityData : entityDataList) {
         if (entityData != null) {
            Location location = entityData.getLocation();
            if (location != null) {
               World world = location.getWorld();
               if (world != null) {
                  UUID uuid = entityData.getUUIDEntity();
                  if (uuid != null) {
                     Entity found = this.fastGetEntity(uuid);
                     if (found != null) {
                        removedEntityDataList.add(entityData);
                     } else {
                        int cx = location.getBlockX() >> 4;
                        int cz = location.getBlockZ() >> 4;
                        AtomicReference<Entity> ref = new AtomicReference<>(null);
                        CountDownLatch latch = new CountDownLatch(1);
                        this.plugin.getChunkManager().ensureLoadedAndExecute(location, location, false, false, () -> {
                           try {
                              Entity quick = this.fastGetEntity(uuid);
                              if (quick == null) {
                                 Chunk chunk = world.getChunkAt(cx, cz);

                                 for (Entity e : chunk.getEntities()) {
                                    if (e != null && uuid.equals(e.getUniqueId())) {
                                       ref.set(e);
                                       return;
                                    }
                                 }

                                 return;
                              }

                              ref.set(quick);
                           } catch (Throwable var16) {
                              this.plugin.getLogger().severe(var16.getMessage());
                              this.plugin.logStackTrace(var16);
                              return;
                           } finally {
                              latch.countDown();
                           }
                        });

                        try {
                           latch.await(25L, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException var14) {
                           Thread.currentThread().interrupt();
                        }

                        if (ref.get() != null) {
                           removedEntityDataList.add(entityData);
                        }
                     }
                  }
               }
            }
         }
      }

      this.plugin.getDataManager().removeEntityData(removedEntityDataList);
   }

   private Entity fastGetEntity(UUID uuid) {
      try {
         if (SERVER_GET_ENTITY != null) {
            Object e = SERVER_GET_ENTITY.invoke(this.plugin.getServer(), uuid);
            if (e instanceof Entity) {
               return (Entity)e;
            }
         }
      } catch (Throwable var3) {
      }

      return null;
   }

   private Entity scanChunkForEntityRegionSafe(World world, int cx, int cz, UUID uuid, Location anchor) {
      if (world != null && uuid != null) {
         Location chunkLoc = new Location(world, (cx << 4) + 8, anchor != null ? anchor.getY() : 64.0, (cz << 4) + 8);
         Location useAnchor = anchor != null ? anchor : chunkLoc;
         AtomicReference<Entity> ref = new AtomicReference<>(null);
         CountDownLatch latch = new CountDownLatch(1);
         this.plugin.getChunkManager().ensureLoadedAndExecute(useAnchor, chunkLoc, false, false, () -> {
            try {
               Chunk chunk = world.getChunkAt(cx, cz);

               for (Entity e : chunk.getEntities()) {
                  if (uuid.equals(e.getUniqueId())) {
                     ref.set(e);
                     break;
                  }
               }
            } catch (Throwable var15) {
               this.plugin.getLogger().severe(var15.getMessage());
               this.plugin.logStackTrace(var15);
            } finally {
               latch.countDown();
            }
         });

         try {
            latch.await(25L, TimeUnit.MILLISECONDS);
         } catch (InterruptedException var11) {
            Thread.currentThread().interrupt();
         }

         return ref.get();
      } else {
         return null;
      }
   }

   static {
      try {
         SERVER_GET_ENTITY = Server.class.getMethod("getEntity", UUID.class);
      } catch (Throwable var1) {
      }
   }
}
