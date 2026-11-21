package com.fastasyncworldedit.core.util.collection;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nonnull;

public final class MemBlockSet extends BlockSet {
   public static final int BITS_PER_WORD = 6;
   public static final int WORDS = 4096 >> 6;
   public static final MemBlockSet.IRow NULL_ROW_X = new MemBlockSet.NullRowX();
   public static final MemBlockSet.IRow NULL_ROW_Z = new MemBlockSet.NullRowZ();
   public static final MemBlockSet.IRow NULL_ROW_Y = new MemBlockSet.NullRowY();
   public final MemBlockSet.IRow[] rows;
   public final MutableBlockVector3 mutable;
   private final int minSectionPosition;
   private final int maxSectionPosition;

   public MemBlockSet(int size, int offsetX, int offsetZ, int minSectionPosition, int maxSectionPosition) {
      super(offsetX, offsetZ);
      this.rows = new MemBlockSet.IRow[size];

      for (int i = 0; i < size; i++) {
         this.rows[i] = NULL_ROW_X;
      }

      this.minSectionPosition = minSectionPosition;
      this.maxSectionPosition = maxSectionPosition;
      this.mutable = new MutableBlockVector3();
   }

   @Override
   public boolean contains(int x, int y, int z) {
      x -= this.getBlockOffsetX();
      z -= this.getBlockOffsetZ();
      return this.rows[x >> 4].get(this.rows, x, y, z - this.getBlockOffsetZ());
   }

   @Override
   public boolean add(int x, int y, int z) {
      x -= this.getBlockOffsetX();
      z -= this.getBlockOffsetZ();
      return this.rows[x >> 4].add(this.rows, x, y, z - this.getBlockOffsetZ(), this.minSectionPosition, this.maxSectionPosition);
   }

   @Override
   public void set(int x, int y, int z) {
      x -= this.getBlockOffsetX();
      z -= this.getBlockOffsetZ();
      this.rows[x >> 4].set(this.rows, x, y, z - this.getBlockOffsetZ(), this.minSectionPosition, this.maxSectionPosition);
   }

   @Override
   public void clear(int x, int y, int z) {
      x -= this.getBlockOffsetX();
      z -= this.getBlockOffsetZ();
      this.rows[x >> 4].clear(this.rows, x, y, z - this.getBlockOffsetZ());
   }

   @Override
   public boolean remove(int x, int y, int z) {
      x -= this.getBlockOffsetX();
      z -= this.getBlockOffsetZ();
      return this.rows[x >> 4].remove(this.rows, x, y, z - this.getBlockOffsetZ());
   }

   @Override
   public BlockVector3 getMinimumPoint() {
      return BlockVector3.at(this.getMinX(), this.getMinimumY(), this.getMinZ());
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      return BlockVector3.at(this.getMaxX(), this.getMaximumY(), this.getMaxZ());
   }

   @Override
   public Set<BlockVector2> getChunks() {
      return new AbstractSet<BlockVector2>() {
         @Nonnull
         @Override
         public Iterator<BlockVector2> iterator() {
            return new Iterator<BlockVector2>() {
               private boolean hasNext;
               private int X;
               private int Z;
               private int setX;
               private int setZ;

               {
                  this.init();
               }

               private void init() {
                  while (this.X < MemBlockSet.this.rows.length) {
                     MemBlockSet.IRow nullRowX = MemBlockSet.this.rows[this.X];
                     if (nullRowX instanceof MemBlockSet.RowX) {
                        for (MemBlockSet.RowX rowx = (MemBlockSet.RowX)nullRowX; this.Z < rowx.rows.length; this.Z++) {
                           MemBlockSet.IRow nullRowZ = rowx.rows[this.Z];
                           if (nullRowZ instanceof MemBlockSet.RowZ) {
                              this.setX = this.X;
                              this.setZ = this.Z++;
                              this.hasNext = true;
                              return;
                           }
                        }

                        this.Z = 0;
                     }

                     this.X++;
                  }

                  this.hasNext = false;
               }

               @Override
               public boolean hasNext() {
                  return this.hasNext;
               }

               public BlockVector2 next() {
                  BlockVector2 result = BlockVector2.at(this.setX + MemBlockSet.this.getBlockOffsetX(), this.setZ + MemBlockSet.this.getBlockOffsetZ());
                  this.init();
                  return result;
               }

               @Override
               public void remove() {
                  throw new UnsupportedOperationException("This set is immutable.");
               }
            };
         }

         @Override
         public int size() {
            int size = 0;

            for (MemBlockSet.IRow nullRowX : MemBlockSet.this.rows) {
               if (nullRowX instanceof MemBlockSet.RowX rowx) {
                  for (int Z = 0; Z < rowx.rows.length; Z++) {
                     MemBlockSet.IRow nullRowZ = rowx.rows[Z];
                     if (nullRowZ instanceof MemBlockSet.RowZ) {
                        size++;
                     }
                  }
               }
            }

            return size;
         }

         @Override
         public boolean isEmpty() {
            return MemBlockSet.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (o instanceof BlockVector2 other) {
               MemBlockSet.IRow rowx = MemBlockSet.this.rows[other.x() - MemBlockSet.this.getChunkOffsetX()];
               if (rowx instanceof MemBlockSet.RowX) {
                  return ((MemBlockSet.RowX)rowx).rows[other.z() - MemBlockSet.this.getChunkOffsetZ()] instanceof MemBlockSet.RowZ;
               }
            }

            return false;
         }
      };
   }

   @Override
   public Set<BlockVector3> getChunkCubes() {
      return new AbstractSet<BlockVector3>() {
         @Override
         public Iterator<BlockVector3> iterator() {
            return new Iterator<BlockVector3>() {
               private final MutableBlockVector3 mutable = new MutableBlockVector3();
               private boolean hasNext;
               private int X;
               private int Z;
               private int Y;
               private int setX;
               private int setY;
               private int setZ;

               {
                  this.init();
               }

               private void init() {
                  while (this.X < MemBlockSet.this.rows.length) {
                     MemBlockSet.IRow nullRowX = MemBlockSet.this.rows[this.X];
                     if (nullRowX instanceof MemBlockSet.RowX) {
                        for (MemBlockSet.RowX rowx = (MemBlockSet.RowX)nullRowX; this.Z < rowx.rows.length; this.Z++) {
                           MemBlockSet.IRow nullRowZ = rowx.rows[this.Z];
                           if (nullRowZ instanceof MemBlockSet.RowZ) {
                              for (MemBlockSet.RowZ rowz = (MemBlockSet.RowZ)nullRowZ; this.Y < rowz.rows.length; this.Y++) {
                                 MemBlockSet.IRow nullRowY = rowz.rows[this.Y];
                                 if (nullRowY instanceof MemBlockSet.RowY) {
                                    this.setX = this.X;
                                    this.setY = this.Y;
                                    this.setZ = this.Z++;
                                 }
                              }

                              this.Y = 0;
                           }
                        }

                        this.Z = 0;
                     }

                     this.X++;
                  }

                  this.hasNext = false;
               }

               @Override
               public boolean hasNext() {
                  return this.hasNext;
               }

               public BlockVector3 next() {
                  this.mutable
                     .setComponents(
                        this.setX + MemBlockSet.this.getBlockOffsetX(),
                        this.setY - (MemBlockSet.this.minSectionPosition << 4),
                        this.setZ + MemBlockSet.this.getBlockOffsetX()
                     );
                  this.init();
                  return this.mutable;
               }

               @Override
               public void remove() {
                  throw new UnsupportedOperationException("This set is immutable.");
               }
            };
         }

         @Override
         public int size() {
            int size = 0;

            for (MemBlockSet.IRow nullRowX : MemBlockSet.this.rows) {
               if (nullRowX instanceof MemBlockSet.RowX rowx) {
                  for (int Z = 0; Z < rowx.rows.length; Z++) {
                     if (rowx.rows[Z] instanceof MemBlockSet.RowZ rowz) {
                        for (int Y = 0; Y < rowz.rows.length; Y++) {
                           MemBlockSet.IRow nullRowY = rowz.rows[Y];
                           if (nullRowY instanceof MemBlockSet.RowY) {
                              size++;
                           }
                        }
                     }
                  }
               }
            }

            return size;
         }

         @Override
         public boolean isEmpty() {
            return MemBlockSet.this.isEmpty();
         }

         @Override
         public boolean contains(Object o) {
            if (o instanceof BlockVector3 other) {
               MemBlockSet.IRow rowx = MemBlockSet.this.rows[other.x() - MemBlockSet.this.getChunkOffsetX()];
               if (rowx instanceof MemBlockSet.RowX) {
                  MemBlockSet.IRow rowz = ((MemBlockSet.RowX)rowx).rows[other.z()];
                  if (rowz instanceof MemBlockSet.RowZ) {
                     return ((MemBlockSet.RowZ)rowz).rows[other.y() - (MemBlockSet.this.minSectionPosition << 4) - MemBlockSet.this.getChunkOffsetZ()] instanceof MemBlockSet.RowY;
                  }
               }
            }

            return false;
         }
      };
   }

   @Override
   public int getMinimumY() {
      int maxY = this.maxSectionPosition;
      int maxy = 16;
      int by = Integer.MAX_VALUE;

      for (MemBlockSet.IRow nullRowX : this.rows) {
         if (nullRowX instanceof MemBlockSet.RowX rowx) {
            label68:
            for (int Z = 0; Z < rowx.rows.length; Z++) {
               if (rowx.rows[Z] instanceof MemBlockSet.RowZ rowz) {
                  for (int Y = 0; Y <= maxY; Y++) {
                     if (rowz.rows[Y] instanceof MemBlockSet.RowY rowY) {
                        int localMaxy = Y == maxY ? maxy : 15;
                        int y = 0;

                        for (int i = 0; y < localMaxy; y++) {
                           for (int xz = 0; xz < 4; i++) {
                              long val = rowY.bits[i];
                              if (val != 0L) {
                                 if (y == 0) {
                                    maxY = Y - 1;
                                    maxy = 16;
                                 } else {
                                    maxY = Y;
                                    maxy = y;
                                 }

                                 by = (Y << 4) + y;
                                 if (by == 0) {
                                    return 0;
                                 }
                                 continue label68;
                              }

                              xz++;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return by;
   }

   @Override
   public int getMaximumY() {
      int maxY = 0;
      int maxy = 0;
      int by = Integer.MIN_VALUE;

      for (MemBlockSet.IRow nullRowX : this.rows) {
         if (nullRowX instanceof MemBlockSet.RowX rowx) {
            label68:
            for (int Z = 0; Z < rowx.rows.length; Z++) {
               if (rowx.rows[Z] instanceof MemBlockSet.RowZ rowz) {
                  for (int Y = this.maxSectionPosition; Y >= maxY; Y--) {
                     if (rowz.rows[Y] instanceof MemBlockSet.RowY rowY) {
                        int localMaxy = Y == maxY ? maxy : 0;
                        int y = 15;

                        for (int i = 63; y >= localMaxy; y--) {
                           for (int xz = 3; xz >= 0; i--) {
                              long val = rowY.bits[i];
                              if (val != 0L) {
                                 if (y == 15) {
                                    maxY = Y + 1;
                                    maxy = 0;
                                 } else {
                                    maxY = Y;
                                    maxy = y + 1;
                                 }

                                 by = (Y << 4) + y;
                                 if (by == (this.maxSectionPosition << 4) + 15) {
                                    return (this.maxSectionPosition << 4) + 15;
                                 }
                                 continue label68;
                              }

                              xz--;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return by;
   }

   public int getMaxZ() {
      int maxChunkZ = 0;
      int maxz = -1;
      int tz = Integer.MIN_VALUE;

      label66:
      for (int X = this.rows.length - 1; X >= 0; X--) {
         if (this.rows[X] instanceof MemBlockSet.RowX rowx) {
            for (int Z = rowx.rows.length - 1; Z >= maxChunkZ; Z--) {
               if (rowx.rows[Z] instanceof MemBlockSet.RowZ rowz) {
                  if (Z != maxChunkZ) {
                     maxChunkZ = Z;
                     maxz = -1;
                  }

                  for (int Y = rowz.rows.length - 1; Y >= 0; Y--) {
                     if (rowz.rows[Y] instanceof MemBlockSet.RowY rowY) {
                        int y = 15;

                        for (int i1 = 63; y >= 0; i1 -= 4) {
                           int z = 12;

                           for (int i = i1; z > maxz - 3; i--) {
                              long bitBuffer = rowY.bits[i];
                              if (bitBuffer != 0L) {
                                 int highest = this.highestBit(bitBuffer);
                                 maxz = z + (highest >> 4);
                                 if (maxz == 15) {
                                    tz = (maxChunkZ << 4) + 15;
                                    maxChunkZ++;
                                    continue label66;
                                 }

                                 tz = Math.max(tz, (maxChunkZ << 4) + maxz);
                                 break;
                              }

                              z -= 4;
                           }

                           y--;
                        }
                     }
                  }
                  break;
               }
            }
         }
      }

      return tz + this.getBlockOffsetZ();
   }

   public int getMaxX() {
      for (int X = this.rows.length - 1; X >= 0; X--) {
         if (this.rows[X] instanceof MemBlockSet.RowX rowx) {
            int tx = X << 4;
            long or = 0L;

            for (int Z = rowx.rows.length - 1; Z >= 0; Z--) {
               if (rowx.rows[Z] instanceof MemBlockSet.RowZ rowz) {
                  for (int Y = rowz.rows.length - 1; Y >= 0; Y--) {
                     if (rowz.rows[Y] instanceof MemBlockSet.RowY rowY) {
                        or |= Arrays.stream(rowY.bits).reduce(0L, (a, b) -> a | b);
                        or = or & 65535L | or >> 16 & 65535L | or >> 32 & 65535L | or >> 48 & 65535L;
                        if (this.highestBit(or) == 15) {
                           return tx + 15;
                        }
                     }
                  }
               }
            }

            int highest = this.highestBit(or);
            if (highest != 64) {
               return tx + highest + this.getBlockOffsetX();
            }
         }
      }

      return Integer.MAX_VALUE;
   }

   public int getMinZ() {
      int maxChunkZ = this.rows.length - 1;
      int maxz = 16;
      int bz = Integer.MAX_VALUE;

      label66:
      for (MemBlockSet.IRow nullRowX : this.rows) {
         if (nullRowX instanceof MemBlockSet.RowX rowx) {
            for (int Z = 0; Z <= maxChunkZ; Z++) {
               if (rowx.rows[Z] instanceof MemBlockSet.RowZ rowz) {
                  if (Z != maxChunkZ) {
                     maxChunkZ = Z;
                     maxz = 16;
                  }

                  for (int Y = 0; Y < rowz.rows.length; Y++) {
                     if (rowz.rows[Y] instanceof MemBlockSet.RowY rowY) {
                        int y = 0;

                        for (int i1 = 0; y < 16; i1 += 4) {
                           int z = 0;

                           for (int i = i1; z < maxz; i++) {
                              long bitBuffer = rowY.bits[i];
                              if (bitBuffer != 0L) {
                                 int lowest = this.lowestBit(bitBuffer);
                                 maxz = z + (lowest >> 4);
                                 if (maxz == 0) {
                                    bz = maxChunkZ << 4;
                                    maxChunkZ--;
                                    continue label66;
                                 }

                                 bz = Math.min(bz, (maxChunkZ << 4) + maxz);
                                 break;
                              }

                              z += 4;
                           }

                           y++;
                        }
                     }
                  }
                  break;
               }
            }
         }
      }

      return bz + this.getBlockOffsetZ();
   }

   public int getMinX() {
      for (int X = 0; X < this.rows.length; X++) {
         if (this.rows[X] instanceof MemBlockSet.RowX rowx) {
            int bx = X << 4;
            long or = 0L;

            for (int Z = 0; Z < rowx.rows.length; Z++) {
               if (rowx.rows[Z] instanceof MemBlockSet.RowZ rowz) {
                  for (int Y = 0; Y < rowz.rows.length; Y++) {
                     if (rowz.rows[Y] instanceof MemBlockSet.RowY rowY) {
                        or |= Arrays.stream(rowY.bits).reduce(0L, (a, b) -> a | b);
                        or = or & 65535L | or >> 16 & 65535L | or >> 32 & 65535L | or >> 48 & 65535L;
                        if (this.lowestBit(or) == 0) {
                           return bx;
                        }
                     }
                  }
               }
            }

            int lowest = this.lowestBit(or);
            if (lowest != 64) {
               return bx + lowest + this.getBlockOffsetX();
            }
         }
      }

      return Integer.MAX_VALUE;
   }

   public void iterate(MemBlockSet.BlockIterator iterator) {
      for (int X = 0; X < this.rows.length; X++) {
         if (this.rows[X] instanceof MemBlockSet.RowX rowx) {
            int bx = this.getBlockOffsetX() + (X << 4);

            for (int Z = 0; Z < rowx.rows.length; Z++) {
               if (rowx.rows[Z] instanceof MemBlockSet.RowZ rowz) {
                  int bz = this.getBlockOffsetZ() + (Z << 4);

                  for (int Y = 0; Y < rowz.rows.length; Y++) {
                     if (rowz.rows[Y] instanceof MemBlockSet.RowY rowY) {
                        int by = Y - this.minSectionPosition << 4;
                        int y = 0;

                        for (int i = 0; y < 16; y++) {
                           for (int z = 0; z < 16; i++) {
                              long bitBuffer = rowY.bits[i];
                              if (bitBuffer != 0L) {
                                 if (bitBuffer != -1L) {
                                    do {
                                       long lowBit = Long.lowestOneBit(bitBuffer);
                                       int bitIndex = Long.bitCount(lowBit - 1L);
                                       int x = bitIndex & 15;
                                       int zz = z + (bitIndex >> 4);
                                       iterator.apply(bx + x, by + y, bz + zz);
                                       bitBuffer ^= lowBit;
                                    } while (bitBuffer != 0L);
                                 } else {
                                    for (int zz = z; zz < z + 4; zz++) {
                                       for (int x = 0; x < 16; x++) {
                                          iterator.apply(bx + x, by + y, bz + zz);
                                       }
                                    }
                                 }
                              }

                              z += 4;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public Iterator<BlockVector3> iterator() {
      return new Iterator<BlockVector3>() {
         private int bx;
         private int by;
         private int bz;
         private int zz;
         private int yy;
         private MemBlockSet.RowX rowX;
         private MemBlockSet.RowZ rowZ;
         private long[] bits;
         private int bitsIndex = 0;
         private int yIndex = 0;
         private int zIndex = 0;
         private int xIndex = 0;
         private long bitBuffer = 0L;
         private boolean next;

         {
            if (this.nextRowX() && this.nextRowZ() && this.nextRowY()) {
               this.next = this.nextLong();
            }
         }

         private boolean nextRowX() {
            while (this.xIndex < MemBlockSet.this.rows.length) {
               this.bx = MemBlockSet.this.getBlockOffsetX() + (this.xIndex << 4);
               MemBlockSet.IRow nullRowX = MemBlockSet.this.rows[this.xIndex++];
               if (nullRowX instanceof MemBlockSet.RowX) {
                  this.rowX = (MemBlockSet.RowX)nullRowX;
                  return true;
               }
            }

            return false;
         }

         private boolean nextRowZ() {
            while (this.zIndex < this.rowX.rows.length) {
               this.bz = MemBlockSet.this.getBlockOffsetZ() + (this.zIndex << 4);
               MemBlockSet.IRow nullRowZ = this.rowX.rows[this.zIndex++];
               if (nullRowZ instanceof MemBlockSet.RowZ) {
                  this.rowZ = (MemBlockSet.RowZ)nullRowZ;
                  return true;
               }
            }

            if (this.nextRowX()) {
               this.zIndex = 0;
               return this.nextRowZ();
            } else {
               return false;
            }
         }

         private boolean nextRowY() {
            while (this.yIndex < this.rowZ.rows.length) {
               this.by = this.yIndex << 4;
               if (this.rowZ.rows[this.yIndex++] instanceof MemBlockSet.RowY rowY) {
                  this.bits = rowY.bits;
                  return true;
               }
            }

            if (this.nextRowZ()) {
               this.yIndex = 0;
               return this.nextRowY();
            } else {
               return false;
            }
         }

         private boolean nextLong() {
            if (this.bitBuffer == 0L) {
               do {
                  this.bitBuffer = this.bits[this.bitsIndex++];
                  if (this.bitsIndex == this.bits.length) {
                     this.bitsIndex = 0;
                     if (!this.nextRowY()) {
                        return this.next = false;
                     }
                  }
               } while (this.bitBuffer == 0L);

               this.zz = this.bz + (this.bitsIndex - 1 << 2 & 15);
               this.yy = this.by + (this.bitsIndex - 1 >> 2);
            }

            return true;
         }

         @Override
         public boolean hasNext() {
            return this.next;
         }

         public BlockVector3 next() {
            long lowBit = Long.lowestOneBit(this.bitBuffer);
            int bitIndex = Long.bitCount(lowBit - 1L);
            MemBlockSet.this.mutable.setComponents(this.bx + (bitIndex & 15), this.yy, this.zz + bitIndex);
            this.bitBuffer ^= lowBit;
            this.nextLong();
            return MemBlockSet.this.mutable;
         }

         @Override
         public void remove() {
            MemBlockSet.this.remove(MemBlockSet.this.mutable);
         }
      };
   }

   @Override
   public boolean isEmpty() {
      for (MemBlockSet.IRow nullRowX : this.rows) {
         if (nullRowX instanceof MemBlockSet.RowX rowx) {
            for (int Z = 0; Z < rowx.rows.length; Z++) {
               if (rowx.rows[Z] instanceof MemBlockSet.RowZ rowz) {
                  for (int Y = 0; Y < 16; Y++) {
                     if (rowz.rows[Y] instanceof MemBlockSet.RowY rowY) {
                        for (long bit : rowY.bits) {
                           if (bit != 0L) {
                              return true;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   @Override
   public int size() {
      return (int)this.sizeLong();
   }

   public long sizeLong() {
      long total = 0L;
      long lastBit = 0L;
      int lastCount = 0;

      for (MemBlockSet.IRow nullRowX : this.rows) {
         if (nullRowX instanceof MemBlockSet.RowX rowx) {
            for (int z = 0; z < rowx.rows.length; z++) {
               if (rowx.rows[z] instanceof MemBlockSet.RowZ rowz) {
                  for (int y = 0; y < 16; y++) {
                     if (rowz.rows[y] instanceof MemBlockSet.RowY rowY) {
                        for (long bit : rowY.bits) {
                           if (bit != 0L) {
                              if (bit == -1L) {
                                 total += 64L;
                              } else if (bit == lastBit) {
                                 total += lastCount;
                              } else {
                                 lastBit = bit;
                                 total += lastCount = Long.bitCount(bit);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return total;
   }

   @Override
   public void clear() {
      Arrays.fill(this.rows, NULL_ROW_X);
   }

   private static MemBlockSet.IRow[] resize(MemBlockSet.IRow[] arr, MemBlockSet.IRow def) {
      int len = arr.length;
      int newLen = len == 1 ? 1 : Integer.highestOneBit(len - 1) * 2;
      MemBlockSet.IRow[] copy = Arrays.copyOf(arr, newLen, MemBlockSet.IRow[].class);

      for (int i = len; i < newLen; i++) {
         copy[i] = def;
      }

      return copy;
   }

   public interface BlockIterator {
      void apply(int x, int y, int z);
   }

   public interface IRow {
      default boolean get(MemBlockSet.IRow[] rows, int x, int y, int z) {
         return false;
      }

      void set(MemBlockSet.IRow[] rows, int x, int y, int z, int minSectionPosition, int maxSectionPosition);

      default boolean add(MemBlockSet.IRow[] rows, int x, int y, int z, int minSectionPosition, int maxSectionPosition) {
         this.set(rows, x, y, z, minSectionPosition, maxSectionPosition);
         return true;
      }

      default boolean remove(MemBlockSet.IRow[] rows, int x, int y, int z) {
         this.remove(rows, x, y, z);
         return false;
      }

      default void clear(MemBlockSet.IRow[] rows, int x, int y, int z) {
      }
   }

   public static final class NullRowX implements MemBlockSet.IRow {
      @Override
      public void set(MemBlockSet.IRow[] parent, int x, int y, int z, int minSectionPosition, int maxSectionPosition) {
         MemBlockSet.IRow row = new MemBlockSet.RowX(parent.length);
         parent[x >> 4] = row;
         row.set(parent, x, y, z, minSectionPosition, maxSectionPosition);
      }
   }

   public static final class NullRowY implements MemBlockSet.IRow {
      @Override
      public void set(MemBlockSet.IRow[] parent, int x, int y, int z, int minSectionPosition, int maxSectionPosition) {
         MemBlockSet.IRow row = new MemBlockSet.RowY();
         parent[y >> 4] = row;
         row.set(parent, x, y, z, minSectionPosition, maxSectionPosition);
      }
   }

   public static final class NullRowZ implements MemBlockSet.IRow {
      @Override
      public void set(MemBlockSet.IRow[] parent, int x, int y, int z, int minSectionPosition, int maxSectionPosition) {
         MemBlockSet.IRow row = new MemBlockSet.RowZ(minSectionPosition, maxSectionPosition);
         parent[z >> 4] = row;
         row.set(parent, x, y, z, minSectionPosition, maxSectionPosition);
      }
   }

   public static final class RowX implements MemBlockSet.IRow {
      private final MemBlockSet.IRow[] rows;

      public RowX(int size) {
         this.rows = new MemBlockSet.IRow[size];

         for (int i = 0; i < size; i++) {
            this.rows[i] = MemBlockSet.NULL_ROW_Z;
         }
      }

      @Override
      public boolean get(MemBlockSet.IRow[] parent, int x, int y, int z) {
         return this.rows[z >> 4].get(this.rows, x, y, z);
      }

      @Override
      public void set(MemBlockSet.IRow[] parent, int x, int y, int z, int minSectionPosition, int maxSectionPosition) {
         this.rows[z >> 4].set(this.rows, x, y, z, minSectionPosition, maxSectionPosition);
      }

      @Override
      public boolean add(MemBlockSet.IRow[] parent, int x, int y, int z, int minSectionPosition, int maxSectionPosition) {
         return this.rows[z >> 4].add(this.rows, x, y, z, minSectionPosition, maxSectionPosition);
      }

      @Override
      public void clear(MemBlockSet.IRow[] parent, int x, int y, int z) {
         this.rows[z >> 4].clear(this.rows, x, y, z);
      }

      @Override
      public boolean remove(MemBlockSet.IRow[] parent, int x, int y, int z) {
         return this.rows[z >> 4].remove(this.rows, x, y, z);
      }
   }

   public static final class RowY implements MemBlockSet.IRow {
      private final long[] bits = new long[MemBlockSet.WORDS];

      public long[] getBits() {
         return this.bits;
      }

      @Override
      public boolean get(MemBlockSet.IRow[] parent, int x, int y, int z) {
         int i = (y & 15) << 8 | (z & 15) << 4 | x & 15;
         return (this.bits[i >> 6] & 1L << (i & 63)) != 0L;
      }

      @Override
      public void set(MemBlockSet.IRow[] parent, int x, int y, int z, int minSectionPosition, int maxSectionPosition) {
         int i = (y & 15) << 8 | (z & 15) << 4 | x & 15;
         this.bits[i >> 6] = this.bits[i >> 6] | 1L << (i & 63);
      }

      @Override
      public boolean add(MemBlockSet.IRow[] parent, int x, int y, int z, int minSectionPosition, int maxSectionPosition) {
         int i = (y & 15) << 8 | (z & 15) << 4 | x & 15;
         int offset = i >> 6;
         long value = this.bits[offset];
         long mask = 1L << (i & 63);
         if ((value & mask) == 0L) {
            this.bits[offset] = value | mask;
            return true;
         } else {
            return false;
         }
      }

      @Override
      public void clear(MemBlockSet.IRow[] parent, int x, int y, int z) {
         int i = (y & 15) << 8 | (z & 15) << 4 | x & 15;
         this.bits[i >> 6] = this.bits[i >> 6] & ~(1L << (i & 63));
      }

      @Override
      public boolean remove(MemBlockSet.IRow[] rows, int x, int y, int z) {
         int i = (y & 15) << 8 | (z & 15) << 4 | x & 15;
         int offset = i >> 6;
         long value = this.bits[offset];
         long mask = 1L << (i & 63);
         if ((value & mask) != 0L) {
            this.bits[offset] = value & ~mask;
            return true;
         } else {
            return false;
         }
      }
   }

   public static final class RowZ implements MemBlockSet.IRow {
      public final MemBlockSet.IRow[] rows;

      public RowZ(int minSectionPosition, int maxSectionPosition) {
         this.rows = new MemBlockSet.IRow[maxSectionPosition - minSectionPosition + 1];
         this.reset();
      }

      public MemBlockSet.IRow getRow(int i) {
         return this.rows[i];
      }

      @Override
      public boolean get(MemBlockSet.IRow[] parent, int x, int y, int z) {
         return this.rows[y >> 4].get(this.rows, x, y, z);
      }

      @Override
      public void set(MemBlockSet.IRow[] parent, int x, int y, int z, int minSectionPosition, int maxSectionPosition) {
         this.rows[y >> 4].set(this.rows, x, y, z, minSectionPosition, maxSectionPosition);
      }

      @Override
      public boolean add(MemBlockSet.IRow[] parent, int x, int y, int z, int minSectionPosition, int maxSectionPosition) {
         return this.rows[y >> 4].add(this.rows, x, y, z, minSectionPosition, maxSectionPosition);
      }

      @Override
      public void clear(MemBlockSet.IRow[] parent, int x, int y, int z) {
         this.rows[y >> 4].set(this.rows, x, y, z, 0, 0);
      }

      @Override
      public boolean remove(MemBlockSet.IRow[] parent, int x, int y, int z) {
         return this.rows[y >> 4].remove(this.rows, x, y, z);
      }

      public boolean isEmpty() {
         for (MemBlockSet.IRow row : this.rows) {
            if (row instanceof MemBlockSet.RowY) {
               return false;
            }
         }

         return true;
      }

      public void reset(int layer) {
         this.rows[layer] = MemBlockSet.NULL_ROW_Y;
      }

      public void reset() {
         Arrays.fill(this.rows, MemBlockSet.NULL_ROW_Y);
      }
   }
}
