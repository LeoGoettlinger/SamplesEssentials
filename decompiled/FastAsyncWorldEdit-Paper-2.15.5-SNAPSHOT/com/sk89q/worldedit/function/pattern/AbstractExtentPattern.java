package com.sk89q.worldedit.function.pattern;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extent.Extent;

public abstract class AbstractExtentPattern extends AbstractPattern implements ExtentPattern {
   private final Extent extent;

   public AbstractExtentPattern(Extent extent) {
      this.extent = extent;
      Preconditions.checkNotNull(extent);
   }

   @Override
   public Extent getExtent() {
      return this.extent;
   }
}
