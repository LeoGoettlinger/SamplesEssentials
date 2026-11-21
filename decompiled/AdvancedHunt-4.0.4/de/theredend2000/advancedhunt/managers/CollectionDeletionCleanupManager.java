package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.caffeine.cache.Cache;
import de.theredend2000.advancedhunt.lib.caffeine.cache.Caffeine;
import de.theredend2000.advancedhunt.util.HeadHelper;
import de.theredend2000.advancedhunt.util.ItemsAdderAdapter;
import de.theredend2000.advancedhunt.util.MaterialUtils;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class CollectionDeletionCleanupManager implements Listener {
   private static final Material DEFAULT_FALLBACK_MATERIAL = Material.STONE;
   private static final int MAX_QUEUED_EDITS = 200000;
   private final Main plugin;
   private final Map<CollectionDeletionCleanupManager.ChunkKey, List<TreasureWorldEdit>> pendingByChunk = new ConcurrentHashMap<>();
   private final AtomicInteger queuedEdits = new AtomicInteger(0);
   private final Cache<CollectionDeletionCleanupManager.ChunkKey, Boolean> chunkTouched = Caffeine.newBuilder()
      .expireAfterWrite(30L, TimeUnit.MINUTES)
      .maximumSize(100000L)
      .build();
   private BukkitTask cleanupTask;

   public CollectionDeletionCleanupManager(Main plugin) {
      this.plugin = plugin;
   }

   public void start() {
      Bukkit.getPluginManager().registerEvents(this, this.plugin);
      this.cleanupTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, () -> {
         int removedEdits = 0;

         for (CollectionDeletionCleanupManager.ChunkKey key : this.pendingByChunk.keySet()) {
            if (this.chunkTouched.getIfPresent(key) == null) {
               List<TreasureWorldEdit> removed = this.pendingByChunk.remove(key);
               if (removed != null) {
                  removedEdits += removed.size();
               }
            }
         }

         if (removedEdits > 0) {
            this.queuedEdits.addAndGet(-removedEdits);
         }
      }, 1200L, 1200L);
   }

   public void stop() {
      HandlerList.unregisterAll(this);
      if (this.cleanupTask != null) {
         this.cleanupTask.cancel();
         this.cleanupTask = null;
      }

      this.pendingByChunk.clear();
      this.chunkTouched.invalidateAll();
      this.queuedEdits.set(0);
   }

   public void scheduleEdits(List<TreasureWorldEdit> edits) {
      if (edits != null && !edits.isEmpty()) {
         if (!Bukkit.isPrimaryThread()) {
            List<TreasureWorldEdit> snapshot = new ArrayList<>(edits);
            Bukkit.getScheduler().runTask(this.plugin, () -> this.scheduleEdits(snapshot));
         } else {
            List<TreasureWorldEdit> validEdits = new ArrayList<>(edits.size());

            for (TreasureWorldEdit edit : edits) {
               if (edit != null) {
                  String worldName = edit.getWorldName();
                  if (worldName != null && !worldName.trim().isEmpty()) {
                     validEdits.add(edit);
                  }
               }
            }

            if (!validEdits.isEmpty()) {
               int currentQueued = this.queuedEdits.get();
               int budget = 200000 - currentQueued;
               if (budget <= 0) {
                  this.plugin
                     .getLogger()
                     .warning("Collection deletion cleanup queue is full (" + currentQueued + " edits). Dropping " + validEdits.size() + " scheduled edits.");
               } else {
                  if (validEdits.size() > budget) {
                     this.plugin
                        .getLogger()
                        .warning(
                           "Collection deletion cleanup queue nearing limit ("
                              + currentQueued
                              + "/"
                              + 200000
                              + " edits). Truncating scheduled edits from "
                              + validEdits.size()
                              + " to "
                              + budget
                              + "."
                        );
                     validEdits = new ArrayList<>(validEdits.subList(0, budget));
                  }

                  this.queuedEdits.addAndGet(validEdits.size());
                  Map<CollectionDeletionCleanupManager.ChunkKey, List<TreasureWorldEdit>> grouped = new HashMap<>();

                  for (TreasureWorldEdit editx : validEdits) {
                     CollectionDeletionCleanupManager.ChunkKey key = new CollectionDeletionCleanupManager.ChunkKey(
                        editx.getWorldName(), editx.getChunkX(), editx.getChunkZ()
                     );
                     grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(editx);
                  }

                  for (Entry<CollectionDeletionCleanupManager.ChunkKey, List<TreasureWorldEdit>> entry : grouped.entrySet()) {
                     CollectionDeletionCleanupManager.ChunkKey key = entry.getKey();
                     List<TreasureWorldEdit> chunkEdits = entry.getValue();
                     if (chunkEdits != null && !chunkEdits.isEmpty()) {
                        this.chunkTouched.put(key, Boolean.TRUE);
                        World world = Bukkit.getWorld(key.worldName);
                        if (world != null && world.isChunkLoaded(key.chunkX, key.chunkZ)) {
                           Bukkit.getScheduler().runTask(this.plugin, () -> this.applyEdits(world, new ArrayList<>(chunkEdits)));
                        } else {
                           this.pendingByChunk.compute(key, (k, existing) -> {
                              if (existing == null) {
                                 existing = Collections.synchronizedList(new ArrayList<>());
                              }

                              existing.addAll(chunkEdits);
                              return existing;
                           });
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onChunkLoad(ChunkLoadEvent event) {
      if (event != null && event.getChunk() != null && event.getWorld() != null) {
         CollectionDeletionCleanupManager.ChunkKey key = new CollectionDeletionCleanupManager.ChunkKey(
            event.getWorld().getName(), event.getChunk().getX(), event.getChunk().getZ()
         );
         List<TreasureWorldEdit> edits = this.pendingByChunk.remove(key);
         if (edits != null && !edits.isEmpty()) {
            this.chunkTouched.put(key, Boolean.TRUE);
            this.applyEdits(event.getWorld(), new ArrayList<>(edits));
         }
      }
   }

   private void applyEdits(final World world, final List<TreasureWorldEdit> edits) {
      if (world != null && edits != null && !edits.isEmpty()) {
         (new BukkitRunnable() {
            int index = 0;

            public void run() {
               int processed = 0;

               while (this.index < edits.size() && processed < 200) {
                  TreasureWorldEdit edit = edits.get(this.index++);
                  processed++;

                  try {
                     CollectionDeletionCleanupManager.this.applySingleEdit(world, edit);
                  } catch (Throwable var4) {
                  }
               }

               if (this.index >= edits.size()) {
                  CollectionDeletionCleanupManager.this.queuedEdits.addAndGet(-edits.size());
                  this.cancel();
               }
            }
         }).runTaskTimer(this.plugin, 1L, 1L);
      }
   }

   private void applySingleEdit(World world, TreasureWorldEdit edit) {
      if (world != null && edit != null) {
         if (edit.getWorldName() != null && world.getName().equalsIgnoreCase(edit.getWorldName())) {
            Location loc = new Location(world, edit.getX(), edit.getY(), edit.getZ());
            if (edit.getAction() == TreasureWorldEdit.Action.REMOVE) {
               this.removeTreasureBlock(edit, loc);
            } else {
               this.restoreTreasureBlock(edit, loc);
            }
         }
      }
   }

   private boolean isItemsAdder(TreasureWorldEdit edit) {
      return edit != null && edit.getMaterial() != null && "ITEMS_ADDER".equalsIgnoreCase(edit.getMaterial());
   }

   private boolean isItemsAdderFurniture(TreasureWorldEdit edit) {
      if (edit == null) {
         return false;
      } else {
         String blockState = edit.getBlockState();
         return blockState != null && !blockState.isEmpty() ? ItemsAdderAdapter.isCustomFurnitureId(blockState) : false;
      }
   }

   private void removeTreasureBlock(TreasureWorldEdit edit, Location loc) {
      if (edit != null && loc != null && loc.getWorld() != null) {
         World world = loc.getWorld();
         int cx = loc.getBlockX() >> 4;
         int cz = loc.getBlockZ() >> 4;
         if (world.isChunkLoaded(cx, cz)) {
            if (this.isItemsAdder(edit)) {
               if (this.isItemsAdderFurniture(edit)) {
                  ItemsAdderAdapter.removeCustomFurniture(loc);
               } else if (!ItemsAdderAdapter.removeCustomBlock(loc)) {
                  world.getBlockAt(loc).setType(Material.AIR, false);
               }
            } else {
               Block block = world.getBlockAt(loc);
               if (!MaterialUtils.isAir(block.getType())) {
                  block.setType(Material.AIR, false);
               }
            }
         }
      }
   }

   private void restoreTreasureBlock(TreasureWorldEdit edit, Location loc) {
      if (edit != null && loc != null && loc.getWorld() != null) {
         World world = loc.getWorld();
         int cx = loc.getBlockX() >> 4;
         int cz = loc.getBlockZ() >> 4;
         if (world.isChunkLoaded(cx, cz)) {
            if (this.isItemsAdder(edit)) {
               if (this.isItemsAdderFurniture(edit)) {
                  ItemsAdderAdapter.spawnCustomFurniture(edit.getBlockState(), loc);
               } else {
                  ItemsAdderAdapter.placeCustomBlock(edit.getBlockState(), loc);
               }
            } else {
               Block block = world.getBlockAt(loc);
               if (MaterialUtils.isAir(block.getType())) {
                  Material type = this.resolveMaterial(edit.getMaterial());
                  if (type != null) {
                     BlockData data = this.resolveBlockData(edit.getBlockState(), type);

                     try {
                        if (data != null) {
                           block.setBlockData(data, false);
                        } else {
                           block.setType(type, false);
                        }
                     } catch (Throwable var10) {
                        block.setType(type, false);
                     }

                     this.applyNbtIfPresent(block, edit.getNbtData());
                  }
               }
            }
         }
      }
   }

   private void applyNbtIfPresent(Block block, String nbtData) {
      if (block != null) {
         if (nbtData != null && !nbtData.isEmpty()) {
            try {
               BlockState state = block.getState();
               if (HeadHelper.applySkullProfile(state, nbtData)) {
                  state.update(true, false);
                  return;
               }

               NBT.modify(state, nbt -> {
                  try {
                     ReadWriteNBT data = NBT.parseNBT(nbtData);
                     nbt.mergeCompound(data);
                  } catch (Throwable var3x) {
                  }
               });
               state.update(true, false);
            } catch (Throwable var4) {
            }
         }
      }
   }

   private BlockData resolveBlockData(String blockState, Material fallbackMaterial) {
      if (blockState != null && !blockState.isEmpty() && !this.isLegacyBlockData(blockState)) {
         try {
            return Bukkit.createBlockData(blockState);
         } catch (Throwable var5) {
         }
      }

      if (fallbackMaterial != null) {
         try {
            return fallbackMaterial.createBlockData();
         } catch (Throwable var4) {
         }
      }

      return null;
   }

   private Material resolveMaterial(String materialName) {
      if (materialName != null && !materialName.isEmpty()) {
         String key = materialName.toUpperCase(Locale.ROOT);

         try {
            Material material = Material.getMaterial(key);
            if (material != null) {
               return material;
            }
         } catch (Throwable var4) {
         }

         return DEFAULT_FALLBACK_MATERIAL;
      } else {
         return null;
      }
   }

   private boolean isLegacyBlockData(String blockState) {
      if (blockState != null && !blockState.isEmpty()) {
         for (int i = 0; i < blockState.length(); i++) {
            if (!Character.isDigit(blockState.charAt(i))) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static final class ChunkKey {
      private final String worldName;
      private final int chunkX;
      private final int chunkZ;

      private ChunkKey(String worldName, int chunkX, int chunkZ) {
         this.worldName = worldName == null ? "" : worldName.toLowerCase(Locale.ROOT);
         this.chunkX = chunkX;
         this.chunkZ = chunkZ;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof CollectionDeletionCleanupManager.ChunkKey)) {
            return false;
         } else {
            CollectionDeletionCleanupManager.ChunkKey that = (CollectionDeletionCleanupManager.ChunkKey)o;
            if (this.chunkX != that.chunkX) {
               return false;
            } else {
               return this.chunkZ != that.chunkZ ? false : this.worldName.equals(that.worldName);
            }
         }
      }

      @Override
      public int hashCode() {
         int result = this.worldName.hashCode();
         result = 31 * result + this.chunkX;
         return 31 * result + this.chunkZ;
      }
   }
}
