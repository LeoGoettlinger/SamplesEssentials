package com.fastasyncworldedit.core.util;

import com.fastasyncworldedit.core.extent.processor.BatchProcessorHolder;
import com.fastasyncworldedit.core.extent.processor.MultiBatchProcessor;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import java.util.ArrayDeque;
import java.util.Queue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public class ProcessorTraverser<T extends IBatchProcessor> {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final T root;

   public ProcessorTraverser(@Nonnull T root) {
      this.root = root;
   }

   @Nullable
   public <U extends IBatchProcessor> U find(Class<U> clazz) {
      try {
         Queue<IBatchProcessor> processors = new ArrayDeque<>();
         IBatchProcessor processor = this.root;

         while (!clazz.isAssignableFrom(processor.getClass())) {
            if (processor instanceof MultiBatchProcessor multiProcessor) {
               processors.addAll(multiProcessor.getBatchProcessors());
            } else if (processor instanceof BatchProcessorHolder holder) {
               processors.add(holder.getProcessor());
            }

            if ((processor = processors.poll()) == null) {
               return null;
            }
         }

         return clazz.cast(processor);
      } catch (Throwable var6) {
         LOGGER.error("Error traversing processors", var6);
         return null;
      }
   }
}
