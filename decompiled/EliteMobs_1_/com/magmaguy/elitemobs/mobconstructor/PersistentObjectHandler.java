package com.magmaguy.elitemobs.mobconstructor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.InstancedDungeonRemoveEvent;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.utils.ChunkVectorizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class PersistentObjectHandler {
   private static final ListMultimap<PersistentObjectHandler.Bucket, PersistentObjectHandler> persistentObjects = Multimaps.synchronizedListMultimap(
      ArrayListMultimap.create()
   );
   private static final Map<ChunkVectorizer.Key, BukkitTask> pendingChunkLoads = new HashMap<>();
   private final PersistentObject persistentObject;
   private final String worldName;
   private Location persistentLocation;
   private PersistentObjectHandler.ChunkBucket chunk;

   public PersistentObjectHandler(PersistentObject persistentObject) {
      this.persistentObject = persistentObject;
      Location initial = persistentObject.getPersistentLocation();
      this.persistentLocation = initial == null ? null : initial.clone();
      this.worldName = initial != null && initial.getWorld() != null ? initial.getWorld().getName() : persistentObject.getWorldName();
      if (this.persistentLocation != null && this.persistentLocation.getWorld() != null && Bukkit.getWorld(this.persistentLocation.getWorld().getUID()) != null
         )
       {
         this.addChunkKey(this);
      } else {
         this.addWorldKey(this);
      }
   }

   public static void shutdown() {
      pendingChunkLoads.values().forEach(BukkitTask::cancel);
      pendingChunkLoads.clear();
      persistentObjects.clear();
   }

   private void addChunkKey(PersistentObjectHandler simplePersistentEntity) {
      if (this.persistentLocation != null && this.persistentLocation.getWorld() != null) {
         this.chunk = new PersistentObjectHandler.ChunkBucket(
            ChunkVectorizer.key(this.persistentLocation.getBlockX() >> 4, this.persistentLocation.getBlockZ() >> 4, this.persistentLocation.getWorld().getUID())
         );
         persistentObjects.put(simplePersistentEntity.chunk, simplePersistentEntity);
      } else {
         this.addWorldKey(simplePersistentEntity);
      }
   }

   private void addWorldKey(PersistentObjectHandler persistentObjectHandler) {
      persistentObjects.put(new PersistentObjectHandler.WorldBucket(persistentObjectHandler.worldName), persistentObjectHandler);
   }

   public void worldLoad(World world) {
      this.persistentObject.worldLoad(world);
      this.remove();
      if (this.persistentLocation == null) {
         Location restored = this.persistentObject.getPersistentLocation();
         this.persistentLocation = restored == null ? null : restored.clone();
      }

      if (this.persistentLocation != null) {
         this.persistentLocation.setWorld(world);
      }

      this.addChunkKey(this);
   }

   public void worldUnload() {
      this.persistentObject.worldUnload();
      synchronized (persistentObjects) {
         if (persistentObjects.containsEntry(this.chunk, this)
            || persistentObjects.containsEntry(new PersistentObjectHandler.WorldBucket(this.worldName), this)) {
            this.remove();
            this.addWorldKey(this);
            if (this.persistentLocation != null) {
               this.persistentLocation.setWorld(null);
            }
         }
      }
   }

   public void updatePersistentLocation(Location location) {
      this.remove();
      this.persistentLocation = location == null ? null : location.clone();
      this.addChunkKey(this);
   }

   public void remove() {
      persistentObjects.remove(this.chunk, this);
      persistentObjects.remove(new PersistentObjectHandler.WorldBucket(this.worldName), this);
   }

   public static void removeForWorld(UUID worldUUID) {
      if (worldUUID != null) {
         for (PersistentObjectHandler handler : snapshotAll()) {
            Location handlerLocation = handler.persistentLocation;
            if (handler.chunk != null && handler.chunk.key.worldId().equals(worldUUID)
               || handlerLocation != null && handlerLocation.getWorld() != null && handlerLocation.getWorld().getUID().equals(worldUUID)) {
               handler.remove();
            }
         }
      }
   }

   private static List<PersistentObjectHandler> snapshot(PersistentObjectHandler.Bucket key) {
      synchronized (persistentObjects) {
         return new ArrayList<>(persistentObjects.get(key));
      }
   }

   private static List<PersistentObjectHandler> snapshotAll() {
      synchronized (persistentObjects) {
         return new ArrayList<>(persistentObjects.values());
      }
   }

   public static void loadWorld(World world) {
      if (world != null) {
         for (PersistentObjectHandler persistentObjectHandler : snapshot(new PersistentObjectHandler.WorldBucket(world.getName()))) {
            try {
               persistentObjectHandler.worldLoad(world);
            } catch (Exception var4) {
               Logger.warn("Failed to restore a persistent object in world " + world.getName() + " : " + var4.getMessage());
               var4.printStackTrace();
            }
         }
      }
   }

   @Generated
   public Location getPersistentLocation() {
      return this.persistentLocation;
   }

   private sealed interface Bucket permits PersistentObjectHandler.WorldBucket, PersistentObjectHandler.ChunkBucket {
   }

   private record ChunkBucket(ChunkVectorizer.Key key) implements PersistentObjectHandler.Bucket {
   }

   public static class PersistentObjectHandlerEvents implements Listener {
      private static PersistentObjectHandler.ChunkBucket chunkLocation(Chunk chunk) {
         return new PersistentObjectHandler.ChunkBucket(ChunkVectorizer.key(chunk));
      }

      private static void loadChunk(List<PersistentObjectHandler> persistentObjectHandlers) {
         persistentObjectHandlers.forEach(persistentObjectHandler -> persistentObjectHandler.persistentObject.chunkLoad());
      }

      private static void unloadChunk(List<PersistentObjectHandler> persistentObjectHandlers) {
         persistentObjectHandlers.forEach(persistentObjectHandler -> {
            if (!(persistentObjectHandler.persistentObject instanceof PersistentMovingEntity)) {
               persistentObjectHandler.persistentObject.chunkUnload();
            }
         });
      }

      private static void unloadWorld(World world) {
         List<PersistentObjectHandler> copy = new ArrayList<>();

         for (PersistentObjectHandler persistentObjectHandler : PersistentObjectHandler.snapshotAll()) {
            if (Objects.equals(persistentObjectHandler.worldName, world.getName())) {
               copy.add(persistentObjectHandler);
            }
         }

         copy.forEach(PersistentObjectHandler::worldUnload);
      }

      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.HIGHEST
      )
      public void chunkLoadEvent(ChunkLoadEvent event) {
         final ChunkVectorizer.Key key = ChunkVectorizer.key(event.getChunk());
         final PersistentObjectHandler.ChunkBucket bucket = chunkLocation(event.getChunk());
         if (!PersistentObjectHandler.snapshot(bucket).isEmpty() && !PersistentObjectHandler.pendingChunkLoads.containsKey(key)) {
            BukkitTask task = (new BukkitRunnable() {
               public void run() {
                  World world = Bukkit.getWorld(key.worldId());
                  List<PersistentObjectHandler> handlers = PersistentObjectHandler.snapshot(bucket);
                  if (world != null && world.isChunkLoaded(key.x(), key.z()) && !handlers.isEmpty()) {
                     Location location = new Location(world, key.x() * 16.0 + 8.0, world.getMinHeight(), key.z() * 16.0 + 8.0);
                     if (!NMSManager.isEnabled() || NMSManager.getAdapter().isPositionEntityTicking(location)) {
                        this.finish();
                        PersistentObjectHandler.PersistentObjectHandlerEvents.loadChunk(handlers);
                     }
                  } else {
                     this.finish();
                  }
               }

               private void finish() {
                  PersistentObjectHandler.pendingChunkLoads.remove(key);
                  this.cancel();
               }
            }).runTaskTimer(MetadataHandler.PLUGIN, 1L, 1L);
            PersistentObjectHandler.pendingChunkLoads.put(key, task);
         }
      }

      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.MONITOR
      )
      public void worldUnloadEvent(WorldUnloadEvent event) {
         unloadWorld(event.getWorld());
      }

      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.MONITOR
      )
      public void worldLoadEvent(WorldLoadEvent event) {
         PersistentObjectHandler.loadWorld(event.getWorld());
      }

      @EventHandler(
         priority = EventPriority.MONITOR,
         ignoreCancelled = true
      )
      public void chunkUnloadEvent(ChunkUnloadEvent event) {
         PersistentObjectHandler.ChunkBucket chunkLocation = chunkLocation(event.getChunk());
         List<PersistentObjectHandler> simplePersistentEntityList = PersistentObjectHandler.snapshot(chunkLocation);
         unloadChunk(simplePersistentEntityList);
      }

      @EventHandler
      public void onInstanceRemove(InstancedDungeonRemoveEvent event) {
         PersistentObjectHandler.persistentObjects.removeAll(new PersistentObjectHandler.WorldBucket(event.getDungeonInstance().getInstancedWorldName()));
      }
   }

   private record WorldBucket(String name) implements PersistentObjectHandler.Bucket {
   }
}
