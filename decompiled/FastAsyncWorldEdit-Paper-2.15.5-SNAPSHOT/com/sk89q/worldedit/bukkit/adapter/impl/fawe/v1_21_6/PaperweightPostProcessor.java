package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v1_21_6;

import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.processor.ProcessorScope;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class PaperweightPostProcessor implements IBatchProcessor {
   @Override
   public IChunkSet processSet(final IChunk chunk, final IChunkGet get, final IChunkSet set) {
      return set;
   }

   @Override
   public void postProcess(final IChunk chunk, final IChunkGet iChunkGet, final IChunkSet iChunkSet) {
      boolean tickFluid = Settings.settings().EXPERIMENTAL.ALLOW_TICK_FLUIDS;
      if (tickFluid) {
         PaperweightGetBlocks_Copy getBlocks = (PaperweightGetBlocks_Copy)iChunkGet;

         for (int layer = iChunkSet.getMinSectionPosition(); layer <= iChunkSet.getMaxSectionPosition(); layer++) {
            char[] set = iChunkSet.loadIfPresent(layer);
            if (set != null) {
               char[] get = null;

               for (int i = 0; i < 4096; i++) {
                  char ordinal = set[i];
                  char replacedOrdinal = 0;
                  boolean fromGet = false;
                  if (ordinal == 0) {
                     if (get == null) {
                        get = getBlocks.load(layer);
                     }

                     if (get == null) {
                        break;
                     }

                     fromGet = true;
                     ordinal = replacedOrdinal = get[i];
                  }

                  if (ordinal != 0) {
                     if (!fromGet) {
                        if (get == null) {
                           get = getBlocks.load(layer);
                        }

                        replacedOrdinal = get[i];
                     }

                     boolean ticking = BlockTypesCache.ticking[ordinal];
                     boolean replacedWasTicking = BlockTypesCache.ticking[replacedOrdinal];
                     boolean replacedWasLiquid = false;
                     BlockState replacedState = null;
                     if (ticking
                        || replacedWasTicking && (replacedWasLiquid = (replacedState = BlockState.getFromOrdinal(replacedOrdinal)).getMaterial().isLiquid())) {
                        BlockState state = BlockState.getFromOrdinal(ordinal);
                        boolean liquid = state.getMaterial().isLiquid();
                        int x = i & 15;
                        int y = i >> 8 & 15;
                        int z = i >> 4 & 15;
                        BlockPos position = new BlockPos((chunk.getX() << 4) + x, (layer << 4) + y, (chunk.getZ() << 4) + z);
                        if (liquid || replacedWasLiquid) {
                           if (liquid) {
                              this.addFluid(getBlocks.serverLevel, state, position);
                           } else if (this.wasAdjacentToWater(get, set, i, x, y, z)) {
                              this.addFluid(getBlocks.serverLevel, replacedState, position);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Nullable
   @Override
   public Extent construct(final Extent child) {
      throw new UnsupportedOperationException("Processing only");
   }

   @Override
   public ProcessorScope getScope() {
      return ProcessorScope.READING_BLOCKS;
   }

   private boolean wasAdjacentToWater(char[] get, char[] set, int i, int x, int y, int z) {
      if (set != null && get != null) {
         char reserved = 0;
         char ordinal;
         if (x > 0 && set[i - 1] != reserved && BlockTypesCache.ticking[ordinal = get[i - 1]] && this.isFluid(ordinal)) {
            return true;
         } else if (x < 15 && set[i + 1] != reserved && BlockTypesCache.ticking[ordinal = get[i + 1]] && this.isFluid(ordinal)) {
            return true;
         } else if (z > 0 && set[i - 16] != reserved && BlockTypesCache.ticking[ordinal = get[i - 16]] && this.isFluid(ordinal)) {
            return true;
         } else if (z < 15 && set[i + 16] != reserved && BlockTypesCache.ticking[ordinal = get[i + 16]] && this.isFluid(ordinal)) {
            return true;
         } else if (y > 0 && set[i - 256] != reserved && BlockTypesCache.ticking[ordinal = get[i - 256]] && this.isFluid(ordinal)) {
            return true;
         } else {
            return y < 15 && set[i + 256] != reserved ? BlockTypesCache.ticking[ordinal = get[i + 256]] && this.isFluid(ordinal) : false;
         }
      } else {
         return false;
      }
   }

   private boolean isFluid(char ordinal) {
      return BlockState.getFromOrdinal(ordinal).getMaterial().isLiquid();
   }

   private void addFluid(final ServerLevel serverLevel, final BlockState replacedState, final BlockPos position) {
      Fluid type;
      if (replacedState.getBlockType() == BlockTypes.LAVA) {
         type = replacedState.<Integer>getState(PropertyKey.LEVEL) == 0 ? Fluids.LAVA : Fluids.FLOWING_LAVA;
      } else {
         type = replacedState.<Integer>getState(PropertyKey.LEVEL) == 0 ? Fluids.WATER : Fluids.FLOWING_WATER;
      }

      serverLevel.scheduleTick(position, type, type.getTickDelay(serverLevel));
   }
}
