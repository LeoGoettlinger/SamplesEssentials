package com.magmaguy.elitemobs.magmacore.util;

import com.magmaguy.elitemobs.magmacore.MagmaCore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class TemporaryBlockManager implements Listener {
   private static final String OWNED_KEY = "nightbreak_owned_temporary_block";
   private static final Map<TemporaryBlockManager.BlockKey, TemporaryBlockManager.OwnedBlock> ownedBlocks = new HashMap<>();

   public static boolean canOwn(Block block) {
      return block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)
         && !block.hasMetadata("nightbreak_owned_temporary_block")
         && !(block.getState() instanceof TileState);
   }

   public static TemporaryBlockManager.OwnedBlock replaceOwned(Block block, BlockData replacement, Plugin owner) {
      return replaceOwned(block, replacement, owner, false);
   }

   private static TemporaryBlockManager.OwnedBlock replaceOwned(Block block, BlockData replacement, Plugin owner, boolean legacyPlacement) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Block replacement requires the server thread");
      } else {
         Objects.requireNonNull(replacement, "replacement");
         Objects.requireNonNull(owner, "owner");
         if (owner.isEnabled() && canOwn(block)) {
            TemporaryBlockManager.OwnedBlock lease = new TemporaryBlockManager.OwnedBlock(block, replacement, owner, legacyPlacement);
            block.setMetadata("nightbreak_owned_temporary_block", new FixedMetadataValue(owner, lease.token));
            ownedBlocks.put(lease.key, lease);

            try {
               block.setBlockData(replacement, legacyPlacement);
               return lease;
            } catch (RuntimeException var6) {
               lease.close();
               throw var6;
            }
         } else {
            return null;
         }
      }
   }

   private TemporaryBlockManager() {
   }

   public static void initialize(JavaPlugin plugin) {
      Bukkit.getPluginManager().registerEvents(new TemporaryBlockManager(), plugin);
   }

   public static void addTemporaryBlock(Block block, int ticks, Material replacementMaterial) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Block replacement requires the server thread");
      } else {
         TemporaryBlockManager.OwnedBlock lease = ownedBlocks.get(TemporaryBlockManager.BlockKey.of(block));
         if (lease == null || lease.legacyPlacement) {
            if (lease != null || !block.hasMetadata("nightbreak_owned_temporary_block")) {
               if (ticks <= 0) {
                  if (lease != null) {
                     lease.release(false);
                  }

                  block.setType(replacementMaterial);
               } else {
                  if (lease != null && !block.getBlockData().equals(lease.replacement)) {
                     lease.release(false);
                     lease = null;
                  }

                  if (lease == null) {
                     lease = replaceOwned(block, replacementMaterial.createBlockData(), MagmaCore.getInstance().getRequestingPlugin(), true);
                     if (lease == null) {
                        return;
                     }
                  } else {
                     if (lease.expiryTask != null) {
                        lease.expiryTask.cancel();
                     }

                     block.setType(replacementMaterial);
                     lease.replacement = block.getBlockData().clone();
                  }

                  TemporaryBlockManager.OwnedBlock current = lease;
                  long generation = ++lease.expiryGeneration;

                  try {
                     lease.expiryTask = Bukkit.getScheduler().runTaskLater(MagmaCore.getInstance().getRequestingPlugin(), () -> {
                        if (current.expiryGeneration == generation) {
                           current.close();
                        }
                     }, ticks);
                  } catch (RuntimeException var8) {
                     current.close();
                     throw var8;
                  }
               }
            }
         }
      }
   }

   public static void addTemporaryBlock(Block block, int ticks, Material replacementMaterial, boolean requireAir) {
      if (!requireAir || block.getType().isAir()) {
         addTemporaryBlock(block, ticks, replacementMaterial);
      }
   }

   public static boolean isTemporaryBlock(Block block) {
      return block.hasMetadata("nightbreak_owned_temporary_block");
   }

   public static void removeTemporaryBlock(Block block) {
      TemporaryBlockManager.OwnedBlock lease = ownedBlocks.get(TemporaryBlockManager.BlockKey.of(block));
      if (lease != null) {
         lease.release(false);
      }
   }

   public static void shutdown() {
      for (TemporaryBlockManager.OwnedBlock lease : new ArrayList<>(ownedBlocks.values())) {
         lease.close();
      }
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.MONITOR
   )
   public void onBlockBreak(BlockBreakEvent event) {
      if (isTemporaryBlock(event.getBlock())) {
         event.setDropItems(false);
         removeTemporaryBlock(event.getBlock());
      }
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.MONITOR
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      removeTemporaryBlock(event.getBlock());
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.MONITOR
   )
   public void onWorldUnload(WorldUnloadEvent event) {
      for (TemporaryBlockManager.OwnedBlock lease : new ArrayList<>(ownedBlocks.values())) {
         if (lease.key.world().equals(event.getWorld().getUID())) {
            lease.release(true, false);
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.MONITOR
   )
   public void onChunkUnload(ChunkUnloadEvent event) {
      for (TemporaryBlockManager.OwnedBlock lease : new ArrayList<>(ownedBlocks.values())) {
         if (lease.key.world().equals(event.getWorld().getUID())
            && lease.key.x() >> 4 == event.getChunk().getX()
            && lease.key.z() >> 4 == event.getChunk().getZ()) {
            lease.release(true, false);
         }
      }
   }

   private record BlockKey(UUID world, int x, int y, int z) {
      static TemporaryBlockManager.BlockKey of(Block block) {
         return new TemporaryBlockManager.BlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
      }
   }

   public static final class OwnedBlock implements AutoCloseable {
      private final TemporaryBlockManager.BlockKey key;
      private final Plugin owner;
      private final String token = UUID.randomUUID().toString();
      private final BlockData original;
      private BlockData replacement;
      private boolean closed;
      private final boolean legacyPlacement;
      private BukkitTask expiryTask;
      private long expiryGeneration;

      private OwnedBlock(Block block, BlockData replacement, Plugin owner, boolean legacyPlacement) {
         this.key = TemporaryBlockManager.BlockKey.of(block);
         this.owner = owner;
         this.original = block.getBlockData().clone();
         this.replacement = replacement.clone();
         this.legacyPlacement = legacyPlacement;
      }

      @Override
      public void close() {
         this.release(true);
      }

      private void release(boolean restore) {
         this.release(restore, this.legacyPlacement);
      }

      private void release(boolean restore, boolean applyPhysics) {
         if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Block restoration requires the server thread");
         } else if (!this.closed) {
            this.closed = true;
            if (this.expiryTask != null) {
               this.expiryTask.cancel();
            }

            TemporaryBlockManager.ownedBlocks.remove(this.key, this);
            World world = Bukkit.getWorld(this.key.world());
            if (world != null && world.isChunkLoaded(this.key.x() >> 4, this.key.z() >> 4)) {
               Block block = world.getBlockAt(this.key.x(), this.key.y(), this.key.z());
               boolean owned = block.getMetadata("nightbreak_owned_temporary_block")
                  .stream()
                  .anyMatch(value -> value.getOwningPlugin() == this.owner && this.token.equals(value.asString()));
               if (owned) {
                  block.removeMetadata("nightbreak_owned_temporary_block", this.owner);
                  if (restore && block.getBlockData().equals(this.replacement)) {
                     block.setBlockData(this.original, applyPhysics);
                  }
               }
            }
         }
      }
   }
}
