package com.sk89q.worldedit.regions;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extent.filter.block.ChunkFilterBlock;
import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.util.MathMan;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.world.World;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

public class EllipsoidRegion extends AbstractRegion {
   private BlockVector3 center;
   private Vector3 radius;
   private Vector3 radiusSqr;
   private BlockVector3 radiusSqrBlocks;
   private Vector3 inverseRadiusSqr;
   private int radiusLengthSqr;
   private boolean sphere;
   private static final BigDecimal ELLIPSOID_BASE_MULTIPLIER = BigDecimal.valueOf(Math.PI * 4.0 / 3.0);

   public EllipsoidRegion(BlockVector3 pos1, Vector3 pos2) {
      this(null, pos1, pos2);
   }

   public EllipsoidRegion(World world, BlockVector3 center, Vector3 radius) {
      super(world);
      this.center = center;
      this.setRadius(radius);
   }

   public EllipsoidRegion(EllipsoidRegion ellipsoidRegion) {
      this(ellipsoidRegion.world, ellipsoidRegion.center, ellipsoidRegion.getRadius());
   }

   @Override
   public BlockVector3 getMinimumPoint() {
      return this.center.toVector3().subtract(this.getRadius()).toBlockPoint();
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      return this.center.toVector3().add(this.getRadius()).toBlockPoint();
   }

   @Override
   public long getVolume() {
      return ELLIPSOID_BASE_MULTIPLIER.multiply(BigDecimal.valueOf(this.radius.x()))
         .multiply(BigDecimal.valueOf(this.radius.y()))
         .multiply(BigDecimal.valueOf(this.radius.z()))
         .setScale(0, RoundingMode.FLOOR)
         .longValue();
   }

   @Override
   public int getWidth() {
      return (int)(2.0 * this.radius.x());
   }

   @Override
   public int getHeight() {
      return (int)(2.0 * this.radius.y());
   }

   @Override
   public int getLength() {
      return (int)(2.0 * this.radius.z());
   }

   private BlockVector3 calculateDiff(BlockVector3... changes) throws RegionOperationException {
      BlockVector3 diff = BlockVector3.ZERO.add(changes);
      if ((diff.x() & 1) + (diff.y() & 1) + (diff.z() & 1) != 0) {
         throw new RegionOperationException(Caption.of("worldedit.selection.ellipsoid.error.even-horizontal"));
      } else {
         return diff.divide(2).floor();
      }
   }

   private Vector3 calculateChanges(BlockVector3... changes) {
      Vector3 total = Vector3.ZERO;

      for (BlockVector3 change : changes) {
         total = total.add(change.abs().toVector3());
      }

      return total.divide(2.0).floor();
   }

   @Override
   public void expand(BlockVector3... changes) throws RegionOperationException {
      this.center = this.center.add(this.calculateDiff(changes));
      this.setRadius(this.radius.add(this.calculateChanges(changes)));
   }

   @Override
   public void contract(BlockVector3... changes) throws RegionOperationException {
      this.center = this.center.subtract(this.calculateDiff(changes));
      Vector3 newRadius = this.radius.subtract(this.calculateChanges(changes));
      this.setRadius(Vector3.at(1.5, 1.5, 1.5).getMaximum(newRadius));
   }

   @Override
   public void shift(BlockVector3 change) throws RegionOperationException {
      this.center = this.center.add(change);
   }

   @Override
   public Vector3 getCenter() {
      return this.center.toVector3();
   }

   public void setCenter(BlockVector3 center) {
      this.center = center;
   }

   public Vector3 getRadius() {
      return this.radius.subtract(0.5, 0.5, 0.5);
   }

   public void setRadius(Vector3 radius) {
      this.radius = radius.add(0.5, 0.5, 0.5);
      this.radiusSqr = radius.multiply(radius);
      this.radiusSqrBlocks = this.radiusSqr.toBlockPoint();
      this.radiusLengthSqr = (int)this.radiusSqr.x();
      this.sphere = radius.y() == radius.x() && radius.x() == radius.z();
      this.inverseRadiusSqr = Vector3.ONE.divide(this.radiusSqr);
   }

   @Override
   public Set<BlockVector2> getChunks() {
      Set<BlockVector2> chunks = new HashSet<>();
      BlockVector3 min = this.getMinimumPoint();
      BlockVector3 max = this.getMaximumPoint();
      int centerY = this.center.y();

      for (int x = min.x(); x <= max.x(); x++) {
         for (int z = min.z(); z <= max.z(); z++) {
            if (this.contains(x, centerY, z)) {
               chunks.add(BlockVector2.at(x >> 4, z >> 4));
            }
         }
      }

      return chunks;
   }

   @Override
   public boolean contains(int x, int y, int z) {
      int cx = x - this.center.x();
      int cx2 = cx * cx;
      if (cx2 > this.radiusSqrBlocks.x()) {
         return false;
      } else {
         int cz = z - this.center.z();
         int cz2 = cz * cz;
         if (cz2 > this.radiusSqrBlocks.z()) {
            return false;
         } else {
            int cy = y - this.center.y();
            int cy2 = cy * cy;
            if (this.radiusSqrBlocks.y() < this.getWorldMaxY() && cy2 > this.radiusSqrBlocks.y()) {
               return false;
            } else if (this.sphere) {
               return cx2 + cy2 + cz2 <= this.radiusLengthSqr;
            } else {
               double cxd = cx2 * this.inverseRadiusSqr.x();
               double cyd = cy2 * this.inverseRadiusSqr.y();
               double czd = cz2 * this.inverseRadiusSqr.z();
               return cxd + cyd + czd <= 1.0;
            }
         }
      }
   }

   @Override
   public boolean contains(BlockVector3 position) {
      return this.contains(position.x(), position.y(), position.z());
   }

   @Override
   public boolean contains(int x, int z) {
      int cx = x - this.center.x();
      int cx2 = cx * cx;
      if (cx2 > this.radiusSqrBlocks.x()) {
         return false;
      } else {
         int cz = z - this.center.z();
         int cz2 = cz * cz;
         if (cz2 > this.radiusSqrBlocks.z()) {
            return false;
         } else {
            double cxd = cx2 * this.inverseRadiusSqr.x();
            double czd = cz2 * this.inverseRadiusSqr.z();
            return cxd + czd <= 1.0;
         }
      }
   }

   @Override
   public String toString() {
      return this.center + " - " + this.getRadius();
   }

   public void extendRadius(Vector3 minRadius) {
      this.setRadius(minRadius.getMaximum(this.getRadius()));
   }

   public EllipsoidRegion clone() {
      return (EllipsoidRegion)super.clone();
   }

   private void filterSpherePartial(int y1, int y2, int bx, int bz, Filter filter, ChunkFilterBlock block, IChunkGet get, IChunkSet set) {
      int minSection = y1 >> 4;
      int maxSection = y2 >> 4;
      int yStart = y1 & 15;
      int yEnd = y2 & 15;
      if (minSection == maxSection) {
         this.filterSpherePartial(minSection, 0, 15, bx, bz, filter, block, get, set);
      }

      if (yStart != this.getWorldMinY()) {
         this.filterSpherePartial(minSection, yStart, 15, bx, bz, filter, block, get, set);
         minSection++;
      }

      if (yEnd != 15) {
         this.filterSpherePartial(maxSection, 0, yEnd, bx, bz, filter, block, get, set);
         maxSection--;
      }

      for (int layer = minSection; layer <= maxSection; layer++) {
         this.filterSpherePartial(layer, 0, 15, bx, bz, filter, block, get, set);
      }
   }

   private void filterSpherePartial(int layer, int y1, int y2, int bx, int bz, Filter filter, ChunkFilterBlock block, IChunkGet get, IChunkSet set) {
      int cx = this.center.x();
      int cy = this.center.y();
      int cz = this.center.z();
      block.initLayer(get, set, layer);
      int by = layer << 4;
      int y = y1;

      for (int yy = by + y1; y <= y2; yy++) {
         int diffY = cy - yy;
         int remainderY = this.radiusLengthSqr - diffY * diffY;
         if (remainderY >= 0) {
            for (int z = 0; z < 16; z++) {
               int zz = z + bz;
               int diffZ = cz - zz;
               int remainderZ = remainderY - diffZ * diffZ;
               if (remainderZ >= 0) {
                  int diffX = (int)Math.floor(Math.sqrt(remainderZ));
                  int minX = Math.max(0, cx - diffX - bx);
                  int maxX = Math.min(15, cx + diffX - bx);
                  block.filter(filter, minX, y, z, maxX, y, z);
               }
            }
         }

         y++;
      }
   }

   @Override
   public void filter(IChunk chunk, Filter filter, ChunkFilterBlock block, IChunkGet get, IChunkSet set, boolean full) {
      int cx = this.center.x();
      int cz = this.center.z();
      int bx = chunk.getX() << 4;
      int bz = chunk.getZ() << 4;
      int tx = bx + 15;
      int tz = bz + 15;
      int cx1 = Math.abs(bx - cx);
      int cx2 = Math.abs(tx - cx);
      int cxMin = Math.min(cx1, cx2);
      int cxMax = Math.max(cx1, cx2);
      int cxMin2 = cxMin * cxMin;
      int cxMax2 = cxMax * cxMax;
      int cz1 = Math.abs(bz - cz);
      int cz2 = Math.abs(tz - cz);
      int czMin = Math.min(cz1, cz2);
      int czMax = Math.max(cz1, cz2);
      int czMin2 = czMin * czMin;
      int czMax2 = czMax * czMax;
      if (this.sphere) {
         if (cxMin2 + czMin2 >= this.radiusLengthSqr) {
            super.filter(chunk, filter, block, get, set, full);
            return;
         }

         int diffY2 = this.radiusLengthSqr - cxMax2 - czMax2;
         if (diffY2 < 0) {
            super.filter(chunk, filter, block, get, set, full);
            return;
         }

         int cy = this.center.y();
         int diffYFull = MathMan.usqrt(diffY2);
         int yBotFull = Math.max(this.getWorldMinY(), cy - diffYFull);
         int yTopFull = Math.min(this.getWorldMaxY(), cy + diffYFull);
         if (yBotFull != yTopFull && yBotFull > yTopFull) {
         }

         this.filter(chunk, filter, block, get, set, yBotFull, yTopFull, full);
         if (yBotFull == this.getWorldMinY() && yTopFull == this.getWorldMaxY()) {
            return;
         }

         int diffYPartial = MathMan.usqrt(this.radiusLengthSqr - cxMin * cxMin - czMin * czMin);
         if (yBotFull != this.getWorldMinY()) {
            int yBotPartial = Math.max(this.getWorldMinY(), cy - diffYPartial);
            this.filterSpherePartial(yBotPartial, yBotFull - 1, bx, bz, filter, block, get, set);
         }

         if (yTopFull != this.getWorldMaxY()) {
            int yTopPartial = Math.min(this.getWorldMaxY(), cy + diffYPartial);
            this.filterSpherePartial(yTopFull + 1, yTopPartial, bx, bz, filter, block, get, set);
         }
      } else {
         super.filter(chunk, filter, block, get, set, full);
      }
   }
}
