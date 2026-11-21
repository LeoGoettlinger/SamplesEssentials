package com.fastasyncworldedit.core.extent;

import com.fastasyncworldedit.core.limit.FaweLimit;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.regions.RegionWrapper;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionIntersection;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import javax.annotation.Nullable;

public class MultiRegionExtent extends FaweRegionExtent {
   @Nullable
   private final RegionIntersection intersection;
   @Nullable
   private final RegionIntersection disallowedIntersection;
   @Nullable
   private final Region[] allowed;
   @Nullable
   private final Region[] disallowed;
   @Nullable
   private Region region;
   private int index = 0;

   public MultiRegionExtent(Extent extent, FaweLimit limit, @Nullable Region[] allowed, @Nullable Region[] disallowed) {
      super(extent, limit);
      if (allowed != null && !allowed[0].isGlobal()) {
         this.region = allowed[0];
         this.allowed = allowed;
         this.intersection = new RegionIntersection(Arrays.asList(allowed));
      } else {
         this.region = null;
         this.allowed = null;
         this.intersection = null;
      }

      if (disallowed != null && disallowed.length > 0) {
         this.disallowed = disallowed;
         this.disallowedIntersection = new RegionIntersection(Arrays.asList(disallowed));
      } else {
         this.disallowed = null;
         this.disallowedIntersection = null;
      }
   }

   @Override
   public boolean contains(int x, int y, int z) {
      if (this.region != null && this.region.contains(x, y, z)) {
         if (this.disallowed != null) {
            for (Region disallow : this.disallowed) {
               if (disallow.contains(x, y, z)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         boolean result = this.allowed == null;
         if (!result) {
            for (int i = 0; i < this.allowed.length; i++) {
               if (i != this.index) {
                  Region current = this.allowed[i];
                  if (current.contains(x, y, z)) {
                     this.region = current;
                     this.index = i;
                     result = true;
                     break;
                  }
               }
            }
         }

         if (result && this.disallowed != null) {
            for (Region disallowx : this.disallowed) {
               if (disallowx.contains(x, y, z)) {
                  return false;
               }
            }

            return true;
         } else {
            return result;
         }
      }
   }

   @Override
   public boolean contains(int x, int z) {
      if (this.region != null && this.region.contains(x, z)) {
         if (this.disallowed != null) {
            for (Region disallow : this.disallowed) {
               if (disallow.contains(x, z)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         boolean result = this.allowed == null;
         if (!result) {
            for (int i = 0; i < this.allowed.length; i++) {
               if (i != this.index) {
                  Region current = this.allowed[i];
                  if (current.contains(x, z)) {
                     this.region = current;
                     this.index = i;
                     result = true;
                     break;
                  }
               }
            }
         }

         if (result && this.disallowed != null) {
            for (Region disallowx : this.disallowed) {
               if (disallowx.contains(x, z)) {
                  return false;
               }
            }

            return true;
         } else {
            return result;
         }
      }
   }

   @Override
   public Collection<Region> getRegions() {
      return this.allowed == null ? List.of(RegionWrapper.GLOBAL()) : Arrays.asList(this.allowed);
   }

   @Override
   public boolean processGet(int chunkX, int chunkZ) {
      boolean result = this.allowed == null;
      if (!result) {
         for (Region region : this.allowed) {
            if (region.containsChunk(chunkX, chunkZ)) {
               result = true;
               break;
            }
         }
      }

      if (result && this.disallowed != null) {
         for (Region regionx : this.disallowed) {
            if (regionx.containsChunk(chunkX, chunkZ)) {
               return false;
            }
         }

         return true;
      } else {
         return result;
      }
   }

   @Override
   public IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      if (this.intersection != null) {
         set = this.intersection.processSet(chunk, get, set);
      }

      if (this.disallowedIntersection != null) {
         set = this.disallowedIntersection.processSet(chunk, get, set, true);
      }

      return set;
   }

   @Override
   public Future<?> postProcessSet(final IChunk chunk, final IChunkGet get, final IChunkSet set) {
      return this.intersection.postProcessSet(chunk, get, set);
   }

   @Override
   public void postProcess(IChunk chunk, IChunkGet get, IChunkSet set) {
      this.intersection.postProcess(chunk, get, set);
   }
}
