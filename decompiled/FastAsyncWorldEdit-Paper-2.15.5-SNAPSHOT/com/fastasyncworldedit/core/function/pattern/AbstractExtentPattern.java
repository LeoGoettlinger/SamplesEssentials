package com.fastasyncworldedit.core.function.pattern;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.AbstractPattern;

public abstract class AbstractExtentPattern extends AbstractPattern {
   private final transient Extent extent;

   public AbstractExtentPattern(Extent extent) {
      Preconditions.checkNotNull(extent);
      this.extent = extent;
   }

   public final Extent getExtent() {
      return this.extent;
   }
}
