package com.fastasyncworldedit.core.extent.processor.lighting;

import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.processor.ProcessorScope;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.sk89q.worldedit.extent.Extent;
import javax.annotation.Nullable;

public class RelightProcessor implements IBatchProcessor {
   private final Relighter relighter;

   public RelightProcessor(Relighter relighter) {
      if (relighter instanceof NullRelighter) {
         throw new IllegalArgumentException("NullRelighter cannot be used for a RelightProcessor");
      } else {
         this.relighter = relighter;
      }
   }

   @Override
   public IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      if (Settings.settings().LIGHTING.MODE == 2) {
         this.relighter.addChunk(chunk.getX(), chunk.getZ(), null, chunk.getBitMask());
      } else if (Settings.settings().LIGHTING.MODE == 1) {
         byte[] fix = new byte[get.getSectionCount()];
         boolean relight = false;

         for (int i = get.getMaxSectionPosition(); i >= get.getMinSectionPosition(); i--) {
            if (set.hasSection(i)) {
               relight = true;
               break;
            }

            fix[i - get.getMinSectionPosition()] = 1;
         }

         if (relight) {
            this.relighter.addChunk(chunk.getX(), chunk.getZ(), fix, chunk.getBitMask());
         }
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
}
