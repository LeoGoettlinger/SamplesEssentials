package com.fastasyncworldedit.core.queue;

import com.fastasyncworldedit.core.extent.processor.EmptyBatchProcessor;
import com.fastasyncworldedit.core.extent.processor.MultiBatchProcessor;
import com.fastasyncworldedit.core.extent.processor.ProcessorScope;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.util.NbtUtils;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import javax.annotation.Nullable;

public interface IBatchProcessor {
   IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set);

   default Future<?> postProcessSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      return CompletableFuture.completedFuture(null);
   }

   default void postProcess(IChunk chunk, IChunkGet get, IChunkSet set) {
      this.postProcessSet(chunk, get, set);
   }

   default boolean processGet(int chunkX, int chunkZ) {
      return true;
   }

   default IChunkGet processGet(IChunkGet get) {
      return get;
   }

   @Nullable
   Extent construct(Extent child);

   default boolean trimY(IChunkSet set, int minY, int maxY, final boolean keepInsideRange) {
      int minLayer = minY >> 4;
      int maxLayer = maxY >> 4;
      if (keepInsideRange) {
         for (int layer = set.getMinSectionPosition(); layer <= set.getMaxSectionPosition(); layer++) {
            if (set.hasSection(layer)) {
               if (layer >= minLayer && layer <= maxLayer) {
                  if (layer <= minLayer || layer >= maxLayer) {
                     char[] blocks = set.loadIfPresent(layer);
                     if (blocks != null) {
                        if (layer == minLayer) {
                           int index = (minY & 15) << 8;

                           for (int i = 0; i < index; i++) {
                              blocks[i] = 0;
                           }
                        }

                        if (layer == maxLayer) {
                           int index = (maxY & 15) + 1 << 8;

                           for (int i = index; i < blocks.length; i++) {
                              blocks[i] = 0;
                           }
                        }

                        set.setBlocks(layer, blocks);
                     }
                  }
               } else {
                  set.setBlocks(layer, null);
               }
            }
         }

         try {
            for (int layerx = minY - 15 >> 4; layerx < maxY + 15 >> 4; layerx++) {
               if (set.hasSection(layerx)) {
                  return true;
               }
            }
         } catch (ArrayIndexOutOfBoundsException var14) {
            WorldEdit.logger.error("IBatchProcessor: minY = {} , layer = {}", minY, minY - 15 >> 4, var14);
         }

         return false;
      } else {
         int chunkMaxY = (set.getMaxSectionPosition() << 4) + 15;
         int chunkMinY = set.getMinSectionPosition() << 4;
         if (maxY >= chunkMaxY && minY <= chunkMinY) {
            set.reset();
            return false;
         } else {
            boolean hasBlocks = false;

            for (int layerxx = set.getMinSectionPosition(); layerxx <= set.getMaxSectionPosition(); layerxx++) {
               if (layerxx < minLayer || layerxx > maxLayer) {
                  hasBlocks |= set.hasSection(layerxx);
               } else if (layerxx == minLayer) {
                  char[] arr = set.loadIfPresent(layerxx);
                  if (arr != null) {
                     int index = (minY & 15) << 8;
                     Arrays.fill(arr, index, 4096, '\u0000');
                  }

                  set.setBlocks(layerxx, arr);
               } else if (layerxx != maxLayer) {
                  set.setBlocks(layerxx, null);
               } else {
                  char[] arr = set.loadIfPresent(layerxx);
                  if (arr != null) {
                     int index = (maxY + 1 & 15) << 8;

                     for (int i = 0; i < index; i++) {
                        arr[i] = 0;
                     }
                  }

                  set.setBlocks(layerxx, arr);
               }
            }

            return hasBlocks;
         }
      }
   }

   @Deprecated(
      forRemoval = true,
      since = "2.8.4"
   )
   default boolean trimNBT(IChunkSet set, Function<BlockVector3, Boolean> contains) {
      Collection<FaweCompoundTag> ents = set.entities();
      if (!ents.isEmpty()) {
         ents.removeIf(ent -> !contains.apply(NbtUtils.entityPosition(ent).toBlockPoint()));
      }

      Map<BlockVector3, FaweCompoundTag> tiles = set.tiles();
      if (!tiles.isEmpty()) {
         tiles.entrySet().removeIf(blockVector3CompoundTagEntry -> !contains.apply(blockVector3CompoundTagEntry.getKey()));
      }

      return !tiles.isEmpty() || !ents.isEmpty();
   }

   default boolean trimNBT(IChunkSet set, Function<BlockVector3, Boolean> containsEntity, Function<BlockVector3, Boolean> containsTile) {
      Collection<FaweCompoundTag> ents = set.entities();
      if (!ents.isEmpty()) {
         ents.removeIf(ent -> !containsEntity.apply(NbtUtils.entityPosition(ent).toBlockPoint()));
      }

      Map<BlockVector3, FaweCompoundTag> tiles = set.tiles();
      if (!tiles.isEmpty()) {
         tiles.entrySet().removeIf(blockVector3CompoundTagEntry -> !containsTile.apply(blockVector3CompoundTagEntry.getKey()));
      }

      return !tiles.isEmpty() || !ents.isEmpty();
   }

   default IBatchProcessor join(IBatchProcessor other) {
      return MultiBatchProcessor.of(this, other);
   }

   default IBatchProcessor joinPost(IBatchProcessor other) {
      return MultiBatchProcessor.of(this, other);
   }

   default void flush() {
   }

   default <T extends IBatchProcessor> IBatchProcessor remove(Class<T> clazz) {
      return (IBatchProcessor)(clazz.isInstance(this) ? EmptyBatchProcessor.getInstance() : this);
   }

   default ProcessorScope getScope() {
      return ProcessorScope.CUSTOM;
   }
}
