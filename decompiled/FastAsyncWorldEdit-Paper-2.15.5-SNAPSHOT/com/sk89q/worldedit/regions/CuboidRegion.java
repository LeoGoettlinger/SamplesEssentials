package com.sk89q.worldedit.regions;

import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.filter.block.ChunkFilterBlock;
import com.fastasyncworldedit.core.math.BlockVectorSet;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;

public class CuboidRegion extends AbstractRegion implements FlatRegion {
   private int minX;
   private int minY;
   private int minZ;
   private int maxX;
   private int maxY;
   private int maxZ;
   private BlockVector3 pos1;
   private BlockVector3 pos2;

   public CuboidRegion(BlockVector3 pos1, BlockVector3 pos2) {
      this(null, pos1, pos2);
   }

   public CuboidRegion(World world, BlockVector3 pos1, BlockVector3 pos2) {
      super(world);
      Preconditions.checkNotNull(pos1);
      Preconditions.checkNotNull(pos2);
      this.pos1 = pos1;
      this.pos2 = pos2;
      this.recalculate();
   }

   public CuboidRegion(World world, BlockVector3 pos1, BlockVector3 pos2, boolean clampY) {
      super(world);
      Preconditions.checkNotNull(pos1);
      Preconditions.checkNotNull(pos2);
      this.pos1 = pos1;
      this.pos2 = pos2;
      if (clampY) {
         this.recalculate();
      } else {
         this.recalculateNoClamp();
      }
   }

   public BlockVector3 getPos1() {
      return this.pos1;
   }

   public void setPos1(BlockVector3 pos1) {
      this.pos1 = pos1;
      this.recalculate();
   }

   public BlockVector3 getPos2() {
      return this.pos2;
   }

   public void setPos2(BlockVector3 pos2) {
      this.pos2 = pos2;
      this.recalculate();
   }

   protected void recalculate() {
      if (this.pos1 != null && this.pos2 != null) {
         this.pos1 = this.pos1.clampY(this.getWorldMinY(), this.getWorldMaxY());
         this.pos2 = this.pos2.clampY(this.getWorldMinY(), this.getWorldMaxY());
         this.minX = Math.min(this.pos1.x(), this.pos2.x());
         this.minY = Math.min(this.pos1.y(), this.pos2.y());
         this.minZ = Math.min(this.pos1.z(), this.pos2.z());
         this.maxX = Math.max(this.pos1.x(), this.pos2.x());
         this.maxY = Math.max(this.pos1.y(), this.pos2.y());
         this.maxZ = Math.max(this.pos1.z(), this.pos2.z());
      }
   }

   protected void recalculateNoClamp() {
      if (this.pos1 != null && this.pos2 != null) {
         this.minX = Math.min(this.pos1.x(), this.pos2.x());
         this.minY = Math.min(this.pos1.y(), this.pos2.y());
         this.minZ = Math.min(this.pos1.z(), this.pos2.z());
         this.maxX = Math.max(this.pos1.x(), this.pos2.x());
         this.maxY = Math.max(this.pos1.y(), this.pos2.y());
         this.maxZ = Math.max(this.pos1.z(), this.pos2.z());
      }
   }

   public Region getFaces() {
      BlockVector3 min = this.getMinimumPoint();
      BlockVector3 max = this.getMaximumPoint();
      return new RegionIntersection(
         new CuboidRegion(this.pos1.withX(min.x()), this.pos2.withX(min.x())),
         new CuboidRegion(this.pos1.withX(max.x()), this.pos2.withX(max.x())),
         new CuboidRegion(this.pos1.withZ(min.z()), this.pos2.withZ(min.z())),
         new CuboidRegion(this.pos1.withZ(max.z()), this.pos2.withZ(max.z())),
         new CuboidRegion(this.pos1.withY(min.y()), this.pos2.withY(min.y())),
         new CuboidRegion(this.pos1.withY(max.y()), this.pos2.withY(max.y()))
      );
   }

   public Region getWalls() {
      BlockVector3 min = this.getMinimumPoint();
      BlockVector3 max = this.getMaximumPoint();
      BlockVector3 dimensions = this.getDimensions();
      return dimensions.x() > 2 && dimensions.z() > 2
         ? new RegionIntersection(
            new CuboidRegion(this.pos1.withX(min.x()), this.pos2.withX(min.x())),
            new CuboidRegion(this.pos1.withX(max.x()), this.pos2.withX(max.x())),
            new CuboidRegion(this.pos1.withZ(min.z()).add(BlockVector3.UNIT_X), this.pos2.withZ(min.z()).subtract(BlockVector3.UNIT_X)),
            new CuboidRegion(this.pos1.withZ(max.z()).add(BlockVector3.UNIT_X), this.pos2.withZ(max.z()).subtract(BlockVector3.UNIT_X))
         )
         : new RegionIntersection(this);
   }

   @Override
   public BlockVector3 getMinimumPoint() {
      return this.pos1.getMinimum(this.pos2);
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      return this.pos1.getMaximum(this.pos2);
   }

   @Override
   public CuboidRegion getBoundingBox() {
      return this;
   }

   @Override
   public int getMinimumY() {
      return this.minY;
   }

   @Override
   public int getMaximumY() {
      return this.maxY;
   }

   @Override
   public void expand(BlockVector3... changes) {
      Preconditions.checkNotNull(changes);

      for (BlockVector3 change : changes) {
         if (change.x() > 0) {
            if (Math.max(this.pos1.x(), this.pos2.x()) == this.pos1.x()) {
               this.pos1 = this.pos1.add(change.x(), 0, 0);
            } else {
               this.pos2 = this.pos2.add(change.x(), 0, 0);
            }
         } else if (Math.min(this.pos1.x(), this.pos2.x()) == this.pos1.x()) {
            this.pos1 = this.pos1.add(change.x(), 0, 0);
         } else {
            this.pos2 = this.pos2.add(change.x(), 0, 0);
         }

         if (change.y() > 0) {
            if (Math.max(this.pos1.y(), this.pos2.y()) == this.pos1.y()) {
               this.pos1 = this.pos1.add(0, change.y(), 0);
            } else {
               this.pos2 = this.pos2.add(0, change.y(), 0);
            }
         } else if (Math.min(this.pos1.y(), this.pos2.y()) == this.pos1.y()) {
            this.pos1 = this.pos1.add(0, change.y(), 0);
         } else {
            this.pos2 = this.pos2.add(0, change.y(), 0);
         }

         if (change.z() > 0) {
            if (Math.max(this.pos1.z(), this.pos2.z()) == this.pos1.z()) {
               this.pos1 = this.pos1.add(0, 0, change.z());
            } else {
               this.pos2 = this.pos2.add(0, 0, change.z());
            }
         } else if (Math.min(this.pos1.z(), this.pos2.z()) == this.pos1.z()) {
            this.pos1 = this.pos1.add(0, 0, change.z());
         } else {
            this.pos2 = this.pos2.add(0, 0, change.z());
         }
      }

      this.recalculate();
   }

   @Override
   public void contract(BlockVector3... changes) {
      Preconditions.checkNotNull(changes);

      for (BlockVector3 change : changes) {
         if (change.x() < 0) {
            if (Math.max(this.pos1.x(), this.pos2.x()) == this.pos1.x()) {
               this.pos1 = this.pos1.add(change.x(), 0, 0);
            } else {
               this.pos2 = this.pos2.add(change.x(), 0, 0);
            }
         } else if (Math.min(this.pos1.x(), this.pos2.x()) == this.pos1.x()) {
            this.pos1 = this.pos1.add(change.x(), 0, 0);
         } else {
            this.pos2 = this.pos2.add(change.x(), 0, 0);
         }

         if (change.y() < 0) {
            if (Math.max(this.pos1.y(), this.pos2.y()) == this.pos1.y()) {
               this.pos1 = this.pos1.add(0, change.y(), 0);
            } else {
               this.pos2 = this.pos2.add(0, change.y(), 0);
            }
         } else if (Math.min(this.pos1.y(), this.pos2.y()) == this.pos1.y()) {
            this.pos1 = this.pos1.add(0, change.y(), 0);
         } else {
            this.pos2 = this.pos2.add(0, change.y(), 0);
         }

         if (change.z() < 0) {
            if (Math.max(this.pos1.z(), this.pos2.z()) == this.pos1.z()) {
               this.pos1 = this.pos1.add(0, 0, change.z());
            } else {
               this.pos2 = this.pos2.add(0, 0, change.z());
            }
         } else if (Math.min(this.pos1.z(), this.pos2.z()) == this.pos1.z()) {
            this.pos1 = this.pos1.add(0, 0, change.z());
         } else {
            this.pos2 = this.pos2.add(0, 0, change.z());
         }
      }

      this.recalculate();
   }

   @Override
   public void shift(BlockVector3 change) throws RegionOperationException {
      this.pos1 = this.pos1.add(change);
      this.pos2 = this.pos2.add(change);
      this.recalculate();
   }

   @Override
   public Set<BlockVector2> getChunks() {
      BlockVector3 min = this.getMinimumPoint();
      BlockVector3 max = this.getMaximumPoint();
      final int maxX = max.x() >> 4;
      final int minX = min.x() >> 4;
      final int maxZ = max.z() >> 4;
      final int minZ = min.z() >> 4;
      final int size = (maxX - minX + 1) * (maxZ - minZ + 1);
      return new AbstractSet<BlockVector2>() {
         @Nonnull
         @Override
         public Iterator<BlockVector2> iterator() {
            return new Iterator<BlockVector2>() {
               final int bx = minX;
               final int bz = minZ;
               final int tx = maxX;
               final int tz = maxZ;
               private int x = minX;
               private int z = minZ;
               int regionX = <unrepresentable>.super.x >> 5;
               int regionZ = <unrepresentable>.super.z >> 5;
               int rbx = Math.max(<unrepresentable>.super.bx, <unrepresentable>.super.regionX << 5);
               int rbz = Math.max(<unrepresentable>.super.bz, <unrepresentable>.super.regionZ << 5);
               int rtx = Math.min(<unrepresentable>.super.tx, 31 + (<unrepresentable>.super.regionX << 5));
               int rtz = Math.min(<unrepresentable>.super.tz, 31 + (<unrepresentable>.super.regionZ << 5));
               boolean hasNext = true;

               @Override
               public boolean hasNext() {
                  return this.hasNext;
               }

               public BlockVector2 next() {
                  int curX = this.x;
                  int curZ = this.z;
                  if (++this.x > this.rtx) {
                     if (++this.z > this.rtz) {
                        if (this.x > this.tx) {
                           this.x = this.bx;
                           if (this.z > this.tz) {
                              if (!this.hasNext) {
                                 throw new NoSuchElementException("End of iterator") {
                                    @Override
                                    public Throwable fillInStackTrace() {
                                       return this;
                                    }
                                 };
                              }

                              this.x = this.tx;
                              this.hasNext = false;
                              return BlockVector2.at(curX, curZ);
                           }
                        } else {
                           this.z = this.rbz;
                        }

                        this.regionX = this.x >> 5;
                        this.regionZ = this.z >> 5;
                        this.rbx = Math.max(this.bx, this.regionX << 5);
                        this.rbz = Math.max(this.bz, this.regionZ << 5);
                        this.rtx = Math.min(this.tx, 31 + (this.regionX << 5));
                        this.rtz = Math.min(this.tz, 31 + (this.regionZ << 5));
                     } else {
                        this.x = this.rbx;
                     }
                  }

                  return BlockVector2.at(curX, curZ);
               }
            };
         }

         @Override
         public int size() {
            return size;
         }

         @Override
         public boolean contains(Object o) {
            return !(o instanceof BlockVector2 cv) ? false : cv.x() >= minX && cv.x() <= maxX && cv.z() >= minZ && cv.z() <= maxZ;
         }
      };
   }

   @Override
   public Set<BlockVector3> getChunkCubes() {
      Set<BlockVector3> chunks = new BlockVectorSet();
      BlockVector3 min = this.getMinimumPoint();
      BlockVector3 max = this.getMaximumPoint();

      for (int x = min.x() >> 4; x <= max.x() >> 4; x++) {
         for (int z = min.z() >> 4; z <= max.z() >> 4; z++) {
            for (int y = min.y() >> 4; y <= max.y() >> 4; y++) {
               chunks.add(BlockVector3.at(x, y, z));
            }
         }
      }

      return chunks;
   }

   @Override
   public boolean contains(BlockVector3 position) {
      return this.contains(position.x(), position.y(), position.z());
   }

   @Override
   public boolean contains(int x, int y, int z) {
      return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ && y >= this.minY && y <= this.maxY;
   }

   @Override
   public boolean contains(int x, int z) {
      return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
   }

   @Override
   public Iterator<BlockVector3> iterator() {
      return Settings.settings().HISTORY.COMPRESSION_LEVEL >= 9 ? this.iterator_old() : new Iterator<BlockVector3>() {
         final MutableBlockVector3 mutable = new MutableBlockVector3(0, 0, 0);
         private final BlockVector3 min = CuboidRegion.this.getMinimumPoint();
         private final BlockVector3 max = CuboidRegion.this.getMaximumPoint();
         final int bx = CuboidRegion.super.min.x();
         final int by = CuboidRegion.super.min.y();
         final int bz = CuboidRegion.super.min.z();
         final int tx = CuboidRegion.super.max.x();
         final int ty = CuboidRegion.super.max.y();
         final int tz = CuboidRegion.super.max.z();
         private int x = CuboidRegion.super.min.x();
         private int y = CuboidRegion.super.min.y();
         private int z = CuboidRegion.super.min.z();
         int cx = CuboidRegion.super.x >> 4;
         int cz = CuboidRegion.super.z >> 4;
         int cbx = Math.max(CuboidRegion.super.bx, CuboidRegion.super.cx << 4);
         int cbz = Math.max(CuboidRegion.super.bz, CuboidRegion.super.cz << 4);
         int ctx = Math.min(CuboidRegion.super.tx, 15 + (CuboidRegion.super.cx << 4));
         int ctz = Math.min(CuboidRegion.super.tz, 15 + (CuboidRegion.super.cz << 4));
         boolean hasNext = true;

         @Override
         public boolean hasNext() {
            return this.hasNext;
         }

         public BlockVector3 next() {
            this.mutable.mutX(this.x);
            this.mutable.mutY(this.y);
            this.mutable.mutZ(this.z);
            if (++this.x > this.ctx) {
               if (++this.z > this.ctz) {
                  if (++this.y > this.ty) {
                     this.y = this.by;
                     if (this.x > this.tx) {
                        this.x = this.bx;
                        if (this.z > this.tz) {
                           if (!this.hasNext) {
                              throw new NoSuchElementException("End of iterator") {
                                 @Override
                                 public Throwable fillInStackTrace() {
                                    return this;
                                 }
                              };
                           }

                           this.x = this.tx;
                           this.y = this.ty;
                           this.hasNext = false;
                           return this.mutable;
                        }
                     } else {
                        this.z = this.cbz;
                     }

                     this.cx = this.x >> 4;
                     this.cz = this.z >> 4;
                     this.cbx = Math.max(this.bx, this.cx << 4);
                     this.cbz = Math.max(this.bz, this.cz << 4);
                     this.ctx = Math.min(this.tx, 15 + (this.cx << 4));
                     this.ctz = Math.min(this.tz, 15 + (this.cz << 4));
                  } else {
                     this.x = this.cbx;
                     this.z = this.cbz;
                  }
               } else {
                  this.x = this.cbx;
               }
            }

            return this.mutable;
         }
      };
   }

   public Iterator<BlockVector3> iterator_old() {
      final MutableBlockVector3 mutable = new MutableBlockVector3(0, 0, 0);
      return new Iterator<BlockVector3>() {
         private final BlockVector3 min = CuboidRegion.this.getMinimumPoint();
         private final BlockVector3 max = CuboidRegion.this.getMaximumPoint();
         private int nextX = CuboidRegion.super.min.x();
         private int nextY = CuboidRegion.super.min.y();
         private int nextZ = CuboidRegion.super.min.z();
         private boolean hasNext = true;

         @Override
         public boolean hasNext() {
            return this.hasNext;
         }

         public BlockVector3 next() {
            mutable.mutX(this.nextX);
            mutable.mutY(this.nextY);
            mutable.mutZ(this.nextZ);
            if (++this.nextX > this.max.x()) {
               this.nextX = this.min.x();
               if (++this.nextZ > this.max.z()) {
                  this.nextZ = this.min.z();
                  if (++this.nextY > this.max.y()) {
                     if (!this.hasNext()) {
                        throw new NoSuchElementException();
                     }

                     this.nextX = this.max.x();
                     this.nextZ = this.max.z();
                     this.nextY = this.max.y();
                     this.hasNext = false;
                  }
               }
            }

            return mutable;
         }
      };
   }

   @Override
   public Iterable<BlockVector2> asFlatRegion() {
      return () -> new Iterator<BlockVector2>() {
         private final BlockVector3 min = CuboidRegion.this.getMinimumPoint();
         private final BlockVector3 max = CuboidRegion.this.getMaximumPoint();
         private int nextX = CuboidRegion.super.min.x();
         private int nextZ = CuboidRegion.super.min.z();

         @Override
         public boolean hasNext() {
            return this.nextZ != Integer.MAX_VALUE;
         }

         public BlockVector2 next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               BlockVector2 answer = BlockVector2.at(this.nextX, this.nextZ);
               if (++this.nextX > this.max.x()) {
                  this.nextX = this.min.x();
                  if (++this.nextZ > this.max.z()) {
                     this.nextZ = Integer.MAX_VALUE;
                     this.nextX = Integer.MAX_VALUE;
                  }
               }

               return answer;
            }
         }
      };
   }

   @Override
   public String toString() {
      return this.getMinimumPoint() + " - " + this.getMaximumPoint();
   }

   public CuboidRegion clone() {
      return (CuboidRegion)super.clone();
   }

   public static CuboidRegion makeCuboid(Region region) {
      Preconditions.checkNotNull(region);
      return new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint());
   }

   public static boolean contains(CuboidRegion region) {
      BlockVector3 min = region.getMinimumPoint();
      BlockVector3 max = region.getMaximumPoint();
      return region.contains(min.x(), min.y(), min.z()) && region.contains(max.x(), max.y(), max.z());
   }

   public static CuboidRegion fromCenter(BlockVector3 origin, int apothem) {
      Preconditions.checkNotNull(origin);
      Preconditions.checkArgument(apothem >= 0, "apothem => 0 required");
      BlockVector3 size = BlockVector3.ONE.multiply(apothem);
      return new CuboidRegion(origin.subtract(size), origin.add(size));
   }

   public int getMinimumX() {
      return this.minX;
   }

   public int getMinimumZ() {
      return this.minZ;
   }

   public int getMaximumX() {
      return this.maxX;
   }

   public int getMaximumZ() {
      return this.maxZ;
   }

   public boolean chunkContainedBy(int chunkX, int chunkZ, int chunkMinY, int chunkMaxY) {
      int bx = chunkX << 4;
      int bz = chunkZ << 4;
      int tx = bx + 15;
      int tz = bz + 15;
      BlockVector3 min = this.getMinimumPoint();
      BlockVector3 max = this.getMaximumPoint();
      return min.y() <= chunkMinY && max.y() >= chunkMaxY && min.x() <= bx && max.x() >= tx && min.z() <= bz && max.z() >= tz;
   }

   @Override
   public void filter(final IChunk chunk, final Filter filter, ChunkFilterBlock block, final IChunkGet get, final IChunkSet set, boolean full) {
      int chunkX = chunk.getX();
      int chunkZ = chunk.getZ();
      if (this.minX + 15 >> 4 <= chunkX && this.maxX - 15 >> 4 >= chunkX && this.minZ + 15 >> 4 <= chunkZ && this.maxZ - 15 >> 4 >= chunkZ) {
         this.filter(chunk, filter, block, get, set, this.minY, this.maxY, full);
      } else {
         int localMinX = Math.max(this.minX, chunkX << 4) & 15;
         int localMaxX = Math.min(this.maxX, 15 + (chunkX << 4)) & 15;
         int localMinZ = Math.max(this.minZ, chunkZ << 4) & 15;
         int localMaxZ = Math.min(this.maxZ, 15 + (chunkZ << 4)) & 15;
         int yStart = this.minY & 15;
         int yEnd = this.maxY & 15;
         int minSection = this.minY >> 4;
         int maxSection = this.maxY >> 4;
         if (minSection == maxSection) {
            this.filter(chunk, filter, block, get, set, minSection, localMinX, yStart, localMinZ, localMaxX, yEnd, localMaxZ, full);
         } else {
            if (yStart != 0) {
               this.filter(chunk, filter, block, get, set, minSection, localMinX, yStart, localMinZ, localMaxX, 15, localMaxZ, full);
               minSection++;
            }

            if (yEnd != 15) {
               this.filter(chunk, filter, block, get, set, maxSection, localMinX, 0, localMinZ, localMaxX, yEnd, localMaxZ, full);
               maxSection--;
            }

            for (int layer = minSection; layer <= maxSection; layer++) {
               this.filter(chunk, filter, block, get, set, layer, localMinX, 0, localMinZ, localMaxX, 15, localMaxZ, full);
            }
         }
      }
   }

   @Override
   public IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      int bx = chunk.getX() << 4;
      int bz = chunk.getZ() << 4;
      int tx = bx + 15;
      int tz = bz + 15;
      if (bx >= this.minX && tx <= this.maxX && bz >= this.minZ && tz <= this.maxZ) {
         if (this.minY <= set.getMinSectionPosition() << 4 && this.maxY >= (set.getMaxSectionPosition() << 4) + 15) {
            return set;
         } else {
            this.trimY(set, this.minY, this.maxY, true);
            BlockVector3 chunkPos = chunk.getChunkBlockCoord().withY(0);
            this.trimNBT(set, this::contains, pos -> this.contains(pos.add(chunkPos)));
            return set;
         }
      } else if (tx >= this.minX && bx <= this.maxX && tz >= this.minZ && bz <= this.maxZ) {
         if (this.minY > set.getMinSectionPosition() << 4 || this.maxY < (set.getMaxSectionPosition() << 4) + 15) {
            this.trimY(set, this.minY, this.maxY, true);
         }

         int lowerX = Math.max(0, this.minX - bx);
         int upperX = Math.min(15, 15 + this.maxX - tx);
         int lowerZ = Math.max(0, this.minZ - bz);
         int upperZ = Math.min(15, 15 + this.maxZ - tz);
         int upperZi = upperZ + 1 << 4;
         int lowerZi = lowerZ << 4;
         boolean trimX = lowerX != 0 || upperX != 15;
         boolean trimZ = lowerZ != 0 || upperZ != 15;
         if (!trimX && !trimZ) {
            return set;
         } else {
            for (int layer = get.getMinSectionPosition(); layer <= get.getMaxSectionPosition(); layer++) {
               if (set.hasSection(layer)) {
                  char[] arr = Objects.requireNonNull(set.loadIfPresent(layer));
                  int indexY = 0;

                  for (int y = 0; y < 16; indexY += 256) {
                     if (trimZ) {
                        int index = indexY;

                        for (int z = 0; z < lowerZ; z++) {
                           for (int x = 0; x < 16; index++) {
                              arr[index] = 0;
                              x++;
                           }
                        }

                        index = indexY + upperZi;

                        for (int z = upperZ + 1; z < 16; z++) {
                           for (int x = 0; x < 16; index++) {
                              arr[index] = 0;
                              x++;
                           }
                        }
                     }

                     if (trimX) {
                        int index = indexY + lowerZi;

                        for (int z = lowerZ; z <= upperZ; index += 16) {
                           for (int x = 0; x < lowerX; x++) {
                              arr[index + x] = 0;
                           }

                           for (int x = upperX + 1; x < 16; x++) {
                              arr[index + x] = 0;
                           }

                           z++;
                        }
                     }

                     y++;
                  }

                  set.setBlocks(layer, arr);
               }
            }

            BlockVector3 chunkPos = BlockVector3.at(chunk.getX() << 4, 0, chunk.getZ() << 4);
            this.trimNBT(set, this::contains, pos -> this.contains(pos.add(chunkPos)));
            return set;
         }
      } else {
         return null;
      }
   }

   @Override
   public IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set, boolean asBlacklist) {
      if (!asBlacklist) {
         return this.processSet(chunk, get, set);
      } else {
         int bx = chunk.getX() << 4;
         int bz = chunk.getZ() << 4;
         int tx = bx + 15;
         int tz = bz + 15;
         if (bx >= this.minX && tx <= this.maxX && bz >= this.minZ && tz <= this.maxZ) {
            int sMaxY = (set.getMaxSectionPosition() << 4) + 15;
            int sMinY = set.getMinSectionPosition() << 4;
            if (this.minY <= sMinY && this.maxY >= sMaxY) {
               return null;
            } else {
               this.trimY(set, this.minY, this.maxY, false);
               BlockVector3 chunkPos = chunk.getChunkBlockCoord().withY(0);
               this.trimNBT(set, this::contains, pos -> this.contains(pos.add(chunkPos)));
               return set;
            }
         } else if (tx >= this.minX && bx <= this.maxX && tz >= this.minZ && bz <= this.maxZ) {
            if (this.minY > set.getMinSectionPosition() << 4 || this.maxY < (set.getMaxSectionPosition() << 4) + 15) {
               this.trimY(set, this.minY, this.maxY, false);
            }

            int lowerX = Math.max(0, this.minX - bx);
            int upperX = Math.min(15, 15 + this.maxX - tx);
            int lowerZ = Math.max(0, this.minZ - bz);
            int upperZ = Math.min(15, 15 + this.maxZ - tz);
            int lowerZi = lowerZ << 4;
            boolean trimX = lowerX != 0 || upperX != 15;
            boolean trimZ = lowerZ != 0 || upperZ != 15;

            for (int layer = get.getMinSectionPosition(); layer <= get.getMaxSectionPosition(); layer++) {
               if (set.hasSection(layer)) {
                  char[] arr = Objects.requireNonNull(set.loadIfPresent(layer));
                  if (trimX || trimZ) {
                     int indexY = 0;

                     for (int y = 0; y < 16; indexY += 256) {
                        if (trimZ) {
                           int index = indexY;

                           for (int z = lowerZ; z <= upperZ; z++) {
                              for (int x = 0; x < 16; index++) {
                                 arr[index] = 0;
                                 x++;
                              }
                           }
                        }

                        if (trimX) {
                           int index = indexY + lowerZi;

                           for (int z = lowerZ; z <= upperZ; index += 16) {
                              for (int x = lowerX; x <= upperX; x++) {
                                 arr[index + x] = 0;
                              }

                              z++;
                           }
                        }

                        y++;
                     }

                     set.setBlocks(layer, arr);
                  }
               }
            }

            BlockVector3 chunkPos = chunk.getChunkBlockCoord().withY(0);
            this.trimNBT(set, bv3 -> !this.contains(bv3), bv3 -> !this.contains(bv3.add(chunkPos)));
            return set;
         } else {
            return set;
         }
      }
   }
}
