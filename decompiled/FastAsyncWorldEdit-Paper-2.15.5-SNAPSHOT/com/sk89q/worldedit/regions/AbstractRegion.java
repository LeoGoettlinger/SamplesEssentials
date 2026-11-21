package com.sk89q.worldedit.regions;

import com.fastasyncworldedit.core.math.BlockVectorSet;
import com.google.common.primitives.Ints;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.iterator.RegionIterator;
import com.sk89q.worldedit.world.World;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

public abstract class AbstractRegion extends AbstractSet<BlockVector3> implements Region {
   @Nullable
   protected World world;

   public AbstractRegion(@Nullable World world) {
      this.world = world;
   }

   @Override
   public int size() {
      return Ints.saturatedCast(this.getVolume());
   }

   @Override
   public Vector3 getCenter() {
      return this.getMinimumPoint().add(this.getMaximumPoint()).toVector3().divide(2.0);
   }

   @Override
   public Iterator<BlockVector3> iterator() {
      return new RegionIterator(this);
   }

   @Nullable
   @Override
   public World getWorld() {
      return this.world;
   }

   @Override
   public void setWorld(World world) {
      this.world = world;
   }

   @Override
   public void shift(BlockVector3 change) throws RegionOperationException {
      this.expand(new BlockVector3[]{change});
      this.contract(new BlockVector3[]{change});
   }

   public AbstractRegion clone() {
      try {
         return (AbstractRegion)super.clone();
      } catch (CloneNotSupportedException var2) {
         return null;
      }
   }

   @Override
   public List<BlockVector2> polygonize(int maxPoints) {
      if (maxPoints >= 0 && maxPoints < 4) {
         throw new IllegalArgumentException("Cannot polygonize an AbstractRegion with no overridden polygonize method into less than 4 points.");
      } else {
         BlockVector3 min = this.getMinimumPoint();
         BlockVector3 max = this.getMaximumPoint();
         List<BlockVector2> points = new ArrayList<>(4);
         points.add(BlockVector2.at(min.x(), min.z()));
         points.add(BlockVector2.at(min.x(), max.z()));
         points.add(BlockVector2.at(max.x(), max.z()));
         points.add(BlockVector2.at(max.x(), min.z()));
         return points;
      }
   }

   @Override
   public long getVolume() {
      BlockVector3 min = this.getMinimumPoint();
      BlockVector3 max = this.getMaximumPoint();
      return (max.x() - min.x() + 1L) * (max.y() - min.y() + 1L) * (max.z() - min.z() + 1L);
   }

   @Override
   public int getWidth() {
      BlockVector3 min = this.getMinimumPoint();
      BlockVector3 max = this.getMaximumPoint();
      return max.x() - min.x() + 1;
   }

   @Override
   public int getHeight() {
      BlockVector3 min = this.getMinimumPoint();
      BlockVector3 max = this.getMaximumPoint();
      return max.y() - min.y() + 1;
   }

   @Override
   public int getLength() {
      BlockVector3 min = this.getMinimumPoint();
      BlockVector3 max = this.getMaximumPoint();
      return max.z() - min.z() + 1;
   }

   @Override
   public Set<BlockVector2> getChunks() {
      Set<BlockVector2> chunks = new HashSet<>();
      BlockVector3 minBlock = this.getMinimumPoint();
      BlockVector3 maxBlock = this.getMaximumPoint();
      BlockVector2 min = BlockVector2.at(minBlock.x() >> 4, minBlock.z() >> 4);
      BlockVector2 max = BlockVector2.at(maxBlock.x() >> 4, maxBlock.z() >> 4);

      for (int X = min.x(); X <= max.x(); X++) {
         for (int Z = min.z(); Z <= max.z(); Z++) {
            if (this.containsChunk(X, Z)) {
               chunks.add(BlockVector2.at(X, Z));
            }
         }
      }

      return chunks;
   }

   @Override
   public Set<BlockVector3> getChunkCubes() {
      Set<BlockVector3> chunks = new BlockVectorSet();
      BlockVector3 min = this.getMinimumPoint();
      BlockVector3 max = this.getMaximumPoint();

      for (int x = min.x(); x <= max.x(); x++) {
         for (int y = min.y(); y <= max.y(); y++) {
            for (int z = min.z(); z <= max.z(); z++) {
               if (this.contains(BlockVector3.at(x, y, z))) {
                  chunks.add(BlockVector3.at(x >> 4, y >> 4, z >> 4));
               }
            }
         }
      }

      return chunks;
   }

   protected final int getWorldMinY() {
      return this.world == null ? Integer.MIN_VALUE : this.world.getMinY();
   }

   protected final int getWorldMaxY() {
      return this.world == null ? Integer.MAX_VALUE : this.world.getMaxY();
   }

   @Override
   public int hashCode() {
      int worldHash = this.world == null ? 7 : this.world.hashCode();
      int result = worldHash ^ worldHash >>> 32;
      result = 31 * result + this.getMinimumPoint().hashCode();
      result = 31 * result + this.getMaximumPoint().hashCode();
      return (int)(31 * result + this.getVolume());
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         return !(o instanceof Region region)
            ? false
            : Objects.equals(this.getWorld(), region.getWorld())
               && this.getMinimumPoint().equals(region.getMinimumPoint())
               && this.getMaximumPoint().equals(region.getMaximumPoint())
               && this.getVolume() == region.getVolume();
      }
   }
}
