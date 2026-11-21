package com.fastasyncworldedit.core.function.mask;

import com.fastasyncworldedit.core.math.BlockVectorSet;
import com.fastasyncworldedit.core.math.LocalBlockVectorSet;
import com.fastasyncworldedit.core.util.collection.BlockVector3Set;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.AbstractExtentMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import javax.annotation.Nullable;

public class CachedMask extends AbstractDelegateMask implements ResettableMask {
   private final boolean hasExtent;
   private transient BlockVector3Set cache_checked;
   private transient BlockVector3Set cache_results;

   public CachedMask(Mask mask) {
      this(mask, false);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.13.0"
   )
   public CachedMask(Mask mask, boolean local) {
      super(mask);
      this.hasExtent = mask instanceof AbstractExtentMask;
      if (local) {
         this.cache_checked = LocalBlockVectorSet.wrapped();
         this.cache_results = LocalBlockVectorSet.wrapped();
      } else {
         this.cache_checked = new BlockVectorSet();
         this.cache_results = new BlockVectorSet();
      }
   }

   public static CachedMask cache(Mask mask) {
      return mask instanceof CachedMask ? (CachedMask)mask : new CachedMask(mask);
   }

   @Override
   public void reset() {
      this.cache_checked = new BlockVectorSet();
      this.cache_results = new BlockVectorSet();
      this.resetCache();
   }

   private void resetCache() {
      this.cache_checked.clear();
      this.cache_results.clear();
   }

   @Override
   public boolean test(BlockVector3 vector) {
      int x = vector.x();
      int y = vector.y();
      int z = vector.z();
      boolean check = this.cache_checked.add(x, y, z);
      if (!check) {
         return this.cache_results.contains(x, y, z);
      } else {
         boolean result = this.getMask().test(vector);
         if (result) {
            this.cache_results.add(x, y, z);
         }

         return result;
      }
   }

   public boolean test(@Nullable Extent extent, BlockVector3 vector) {
      if (this.hasExtent && this.getMask() instanceof AbstractExtentMask mask) {
         int var9 = vector.x();
         int y = vector.y();
         int z = vector.z();
         boolean check = this.cache_checked.add(var9, y, z);
         if (!check) {
            return this.cache_results.contains(var9, y, z);
         } else {
            boolean result = mask.test(extent, vector);
            if (result) {
               this.cache_results.add(var9, y, z);
            }

            return result;
         }
      } else {
         return this.test(vector);
      }
   }

   @Override
   public Mask copy() {
      return new CachedMask(this.getMask().copy(), this.cache_checked instanceof LocalBlockVectorSet);
   }
}
