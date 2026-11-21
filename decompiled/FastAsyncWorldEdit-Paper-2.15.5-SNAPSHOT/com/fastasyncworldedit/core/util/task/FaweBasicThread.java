package com.fastasyncworldedit.core.util.task;

import com.sk89q.worldedit.extent.Extent;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class FaweBasicThread extends Thread implements FaweThread {
   private Extent currentExtent;

   FaweBasicThread(Runnable runnable) {
      super(runnable);
   }

   @Override
   public void clearCurrentExtent() {
      this.currentExtent = null;
   }

   @Override
   public void setCurrentExtent(final Extent extent) {
      this.currentExtent = extent;
   }

   @Override
   public Extent getCurrentExtent() {
      return this.currentExtent;
   }
}
