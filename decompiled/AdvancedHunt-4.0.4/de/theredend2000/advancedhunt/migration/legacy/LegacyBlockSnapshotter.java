package de.theredend2000.advancedhunt.migration.legacy;

import de.theredend2000.advancedhunt.util.BlockUtils;
import de.theredend2000.advancedhunt.util.ItemsAdderAdapter;
import de.theredend2000.advancedhunt.util.MaterialUtils;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class LegacyBlockSnapshotter {
   private LegacyBlockSnapshotter() {
   }

   public static CompletableFuture<Map<LegacyLocationKey, LegacyBlockSnapshotter.Snapshot>> snapshotBlocks(
      final Plugin plugin, final List<LegacyLocationKey> locations, int perTick
   ) {
      final CompletableFuture<Map<LegacyLocationKey, LegacyBlockSnapshotter.Snapshot>> future = new CompletableFuture<>();
      final Map<LegacyLocationKey, LegacyBlockSnapshotter.Snapshot> out = new HashMap<>();
      if (locations != null && !locations.isEmpty()) {
         final int batch = Math.max(1, perTick);
         (new BukkitRunnable() {
            int idx = 0;

            public void run() {
               try {
                  for (int end = Math.min(this.idx + batch, locations.size()); this.idx < end; this.idx++) {
                     LegacyLocationKey key = locations.get(this.idx);
                     World world = Bukkit.getWorld(key.world());
                     if (world != null) {
                        Location loc = new Location(world, key.x(), key.y(), key.z());
                        Block block = loc.getBlock();
                        if (block != null && !MaterialUtils.isAir(block.getType())) {
                           String nbtData;
                           try {
                              nbtData = (String)NBT.get(block.getState(), ReadableNBT::toString);
                           } catch (Exception var9) {
                              nbtData = null;
                           }

                           String material;
                           String blockState;
                           if (ItemsAdderAdapter.isItemsAdderBlock(block)) {
                              material = "ITEMS_ADDER";
                              blockState = ItemsAdderAdapter.getCustomBlockId(block);
                              if (blockState == null || blockState.isEmpty()) {
                                 continue;
                              }
                           } else {
                              material = block.getType().name();
                              blockState = BlockUtils.getBlockStateString(block);
                           }

                           out.put(key, new LegacyBlockSnapshotter.Snapshot(material, blockState, nbtData));
                        }
                     }
                  }

                  if (this.idx >= locations.size()) {
                     this.cancel();
                     future.complete(out);
                  }
               } catch (Throwable var10) {
                  this.cancel();
                  plugin.getLogger().log(Level.SEVERE, "Legacy block snapshot failed", var10);
                  future.completeExceptionally(var10);
               }
            }
         }).runTaskTimer(plugin, 1L, 1L);
         return future;
      } else {
         future.complete(out);
         return future;
      }
   }

   public static final class Snapshot {
      public final String material;
      public final String blockState;
      public final String nbtData;

      private Snapshot(String material, String blockState, String nbtData) {
         this.material = material;
         this.blockState = blockState;
         this.nbtData = nbtData;
      }

      static LegacyBlockSnapshotter.Snapshot of(String material, String blockState, String nbtData) {
         return new LegacyBlockSnapshotter.Snapshot(material, blockState, nbtData);
      }
   }
}
