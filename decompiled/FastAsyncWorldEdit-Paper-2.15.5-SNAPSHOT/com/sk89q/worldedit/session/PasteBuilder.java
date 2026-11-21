package com.sk89q.worldedit.session;

import com.fastasyncworldedit.core.math.transform.MutatingOperationTransformHolder;
import com.fastasyncworldedit.core.util.MaskTraverser;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.Transform;

public class PasteBuilder {
   private final Clipboard clipboard;
   private final Transform transform;
   private final Extent targetExtent;
   private Mask sourceMask = Masks.alwaysTrue();
   private BlockVector3 to = BlockVector3.ZERO;
   private boolean ignoreAirBlocks;
   private boolean copyEntities = true;
   private boolean copyBiomes;
   private RegionFunction canApply;

   PasteBuilder(ClipboardHolder holder, Extent targetExtent) {
      Preconditions.checkNotNull(holder);
      Preconditions.checkNotNull(targetExtent);
      this.clipboard = holder.getClipboard();
      this.transform = holder.getTransform();
      this.targetExtent = targetExtent;
   }

   public PasteBuilder to(BlockVector3 to) {
      this.to = to;
      return this;
   }

   public PasteBuilder maskSource(Mask sourceMask) {
      if (sourceMask == null) {
         this.sourceMask = Masks.alwaysTrue();
         return this;
      } else {
         this.sourceMask = sourceMask;
         return this;
      }
   }

   public PasteBuilder ignoreAirBlocks(boolean ignoreAirBlocks) {
      this.ignoreAirBlocks = ignoreAirBlocks;
      return this;
   }

   public PasteBuilder copyEntities(boolean copyEntities) {
      this.copyEntities = copyEntities;
      return this;
   }

   public PasteBuilder copyBiomes(boolean copyBiomes) {
      this.copyBiomes = copyBiomes;
      return this;
   }

   public PasteBuilder filter(RegionFunction function) {
      this.canApply = function;
      return this;
   }

   public Operation build() {
      Extent extent = this.clipboard;
      Transform transform = this.transform;
      if (!transform.isIdentity()) {
         transform = MutatingOperationTransformHolder.transform(transform, true);
         extent = new BlockTransformExtent(extent, transform);
      }

      ForwardExtentCopy copy = new ForwardExtentCopy(extent, this.clipboard.getRegion(), this.clipboard.getOrigin(), this.targetExtent, this.to);
      copy.setTransform(transform);
      copy.setCopyingEntities(this.copyEntities);
      copy.setCopyingBiomes(this.copyBiomes && this.clipboard.hasBiomes());
      if (this.canApply != null) {
         copy.setFilterFunction(this.canApply);
      }

      if (this.ignoreAirBlocks) {
         this.sourceMask = MaskIntersection.of(this.sourceMask, new ExistingBlockMask(this.clipboard));
      }

      if (this.targetExtent instanceof EditSession) {
         Mask esSourceMask = ((EditSession)this.targetExtent).getSourceMask();
         if (esSourceMask == Masks.alwaysFalse()) {
            return null;
         }

         if (esSourceMask != null) {
            new MaskTraverser(esSourceMask).reset(extent);
            ((EditSession)this.targetExtent).setSourceMask(null);
            this.sourceMask = MaskIntersection.of(this.sourceMask, esSourceMask);
         }
      }

      if (this.sourceMask != null && this.sourceMask != Masks.alwaysTrue()) {
         copy.setSourceMask(this.sourceMask);
      }

      copy.setCopyingEntities(this.copyEntities);
      copy.setCopyingBiomes(this.copyBiomes && this.clipboard.hasBiomes());
      return copy;
   }
}
