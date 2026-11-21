package com.fastasyncworldedit.core.extent.processor.heightmap;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.extent.processor.ProcessorScope;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.Arrays;
import javax.annotation.Nullable;

public class HeightmapProcessor implements IBatchProcessor {
   private static final HeightMapType[] TYPES = HeightMapType.values();
   private static final int BLOCKS_PER_Y_SHIFT = 8;
   private static final int BLOCKS_PER_Y = 256;
   private static final char[] AIR_LAYER = new char[4096];
   private static final int NEEDED_UPDATES = TYPES.length * 256;
   private final int minY;
   private final int maxY;

   public HeightmapProcessor(int minY, int maxY) {
      this.minY = minY;
      this.maxY = maxY;
   }

   private static int index(int y, int offset) {
      return (y << 8) + offset;
   }

   @Override
   public IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      int[][] heightmaps = new int[TYPES.length][256];
      byte[] updated = new byte[256];
      int updateCount = 0;

      label121:
      for (int layer = this.maxY >> 4; layer >= this.minY >> 4; layer--) {
         boolean hasSectionSet = set.hasNonEmptySection(layer);
         boolean hasSectionGet = get.hasNonEmptySection(layer);
         if (hasSectionSet || hasSectionGet) {
            char[] setSection = hasSectionSet ? set.loadIfPresent(layer) : null;
            if (setSection == null || Arrays.equals(setSection, FaweCache.INSTANCE.EMPTY_CHAR_4096) || Arrays.equals(setSection, AIR_LAYER)) {
               hasSectionSet = false;
            }

            if (hasSectionSet || hasSectionGet) {
               char[] getSection = null;

               for (int y = 15; y >= 0; y--) {
                  for (int j = 0; j < 256; j++) {
                     char ordinal = 0;
                     if (hasSectionSet) {
                        ordinal = setSection[index(y, j)];
                     }

                     if (ordinal == 0) {
                        if (!hasSectionGet) {
                           if (!hasSectionSet) {
                              continue label121;
                           }
                           continue;
                        }

                        if (getSection == null) {
                           getSection = get.load(layer);
                           if (Arrays.equals(getSection, FaweCache.INSTANCE.EMPTY_CHAR_4096) || Arrays.equals(getSection, AIR_LAYER)) {
                              hasSectionGet = false;
                              if (!hasSectionSet) {
                                 continue label121;
                              }
                              continue;
                           }
                        }

                        ordinal = getSection[index(y, j)];
                     }

                     if (ordinal >= 4) {
                        BlockState block = BlockTypesCache.states[ordinal];
                        if (block != null) {
                           byte updateStateAtJ = updated[j];

                           for (int i = 0; i < TYPES.length; i++) {
                              int bitFlag = 1 << i;
                              if ((updateStateAtJ & bitFlag) == 0) {
                                 HeightMapType type = TYPES[i];
                                 if (type.includes(block)) {
                                    heightmaps[i][j] = (layer - get.getMinSectionPosition() << 4) + y + 1;
                                    updated[j] |= (byte)bitFlag;
                                    if (++updateCount == NEEDED_UPDATES) {
                                       break label121;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      for (int ix = 0; ix < TYPES.length; ix++) {
         set.setHeightMap(TYPES[ix], heightmaps[ix]);
      }

      return set;
   }

   @Nullable
   @Override
   public Extent construct(Extent child) {
      throw new UnsupportedOperationException("Processing only");
   }

   @Override
   public ProcessorScope getScope() {
      return ProcessorScope.READING_BLOCKS;
   }

   static {
      Arrays.fill(AIR_LAYER, '\u0001');
   }
}
