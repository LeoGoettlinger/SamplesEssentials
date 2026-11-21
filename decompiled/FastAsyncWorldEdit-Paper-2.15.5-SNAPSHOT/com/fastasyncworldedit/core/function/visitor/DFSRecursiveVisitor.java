package com.fastasyncworldedit.core.function.visitor;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;

public class DFSRecursiveVisitor extends DFSVisitor {
   private final Mask mask;

   public DFSRecursiveVisitor(final Mask mask, final RegionFunction function) {
      this(mask, function, Integer.MAX_VALUE, Integer.MAX_VALUE);
   }

   public DFSRecursiveVisitor(final Mask mask, final RegionFunction function, int maxDepth, int maxBranching) {
      super(function, maxDepth, maxBranching);
      Preconditions.checkNotNull(mask);
      this.mask = mask;
   }

   @Override
   public boolean isVisitable(final BlockVector3 from, final BlockVector3 to) {
      return this.mask.test(to);
   }
}
