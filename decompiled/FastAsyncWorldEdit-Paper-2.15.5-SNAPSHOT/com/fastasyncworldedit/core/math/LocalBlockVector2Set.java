package com.fastasyncworldedit.core.math;

import com.fastasyncworldedit.core.math.sparsebits.SparseBitSet;
import com.fastasyncworldedit.core.util.MathMan;
import com.sk89q.worldedit.math.BlockVector2;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nonnull;

public class LocalBlockVector2Set implements Set<BlockVector2> {
   private final SparseBitSet set;
   private int offsetX;
   private int offsetZ;

   public LocalBlockVector2Set() {
      this.offsetX = this.offsetZ = Integer.MAX_VALUE;
      this.set = new SparseBitSet();
   }

   public LocalBlockVector2Set(int x, int z) {
      this.offsetX = x;
      this.offsetZ = z;
      this.set = new SparseBitSet();
   }

   private LocalBlockVector2Set(int x, int z, SparseBitSet set) {
      this.offsetX = x;
      this.offsetZ = z;
      this.set = set;
   }

   @Override
   public int size() {
      return this.set.cardinality();
   }

   @Override
   public boolean isEmpty() {
      return this.set.isEmpty();
   }

   public boolean contains(int x, int z) {
      if (this.offsetX == Integer.MAX_VALUE) {
         return false;
      } else if (x <= 32767 && x >= -32769 && z <= 32769 && z >= -32767) {
         short sx = (short)(x - this.offsetX);
         short sz = (short)(z - this.offsetZ);

         try {
            return this.set.get(MathMan.pairSearchCoords(sx, sz));
         } catch (IndexOutOfBoundsException var6) {
            System.out.println(x + " " + z + "    " + sx + " " + sz);
            throw var6;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean contains(Object o) {
      return o instanceof BlockVector2 v ? this.contains(v.x(), v.z()) : false;
   }

   public LocalBlockVector2Set clone() {
      return new LocalBlockVector2Set(this.offsetX, this.offsetZ, this.set.clone());
   }

   public boolean containsRadius(int x, int z, int radius) {
      if (radius <= 0) {
         return this.contains(x, z);
      } else {
         int length = radius * 2;
         if (this.size() < length * length * length) {
            int index = -1;

            while ((index = this.set.nextSetBit(index + 1)) != -1) {
               int ix = this.offsetX + MathMan.unpairSearchCoordsX(index);
               int iz = this.offsetZ + MathMan.unpairSearchCoordsY(index);
               if (Math.abs(ix - x) <= radius && Math.abs(iz - z) <= radius) {
                  return true;
               }
            }

            return false;
         } else {
            for (int xx = -radius; xx <= radius; xx++) {
               for (int zz = -radius; zz <= radius; zz++) {
                  if (this.contains(x + xx, z + zz)) {
                     return true;
                  }
               }
            }

            return false;
         }
      }
   }

   public void setOffset(int x, int z) {
      this.offsetX = x;
      this.offsetZ = z;
   }

   protected MutableBlockVector2 getIndex(int getIndex) {
      int size = this.size();
      if (getIndex > size) {
         return null;
      } else {
         int index = -1;

         for (int i = 0; i <= getIndex; i++) {
            index = this.set.nextSetBit(index + 1);
         }

         if (index != -1) {
            int x = this.offsetX + MathMan.unpairSearchCoordsX(index);
            int z = this.offsetZ + MathMan.unpairSearchCoordsY(index);
            return MutableBlockVector2.get(x, z);
         } else {
            return null;
         }
      }
   }

   @Nonnull
   @Override
   public Iterator<BlockVector2> iterator() {
      return new Iterator<BlockVector2>() {
         final MutableBlockVector2 mutable = new MutableBlockVector2(0, 0);
         int index = LocalBlockVector2Set.this.set.nextSetBit(0);
         int previous = -1;

         @Override
         public void remove() {
            LocalBlockVector2Set.this.set.clear(this.previous);
         }

         @Override
         public boolean hasNext() {
            return this.index != -1;
         }

         public BlockVector2 next() {
            if (this.index != -1) {
               int x = LocalBlockVector2Set.this.offsetX + MathMan.unpairSearchCoordsX(this.index);
               int z = LocalBlockVector2Set.this.offsetZ + MathMan.unpairSearchCoordsY(this.index);
               this.mutable.mutX(x);
               this.mutable.mutZ(z);
               this.previous = this.index;
               this.index = LocalBlockVector2Set.this.set.nextSetBit(this.index + 1);
               return this.mutable;
            } else {
               return null;
            }
         }
      };
   }

   @Nonnull
   public BlockVector2[] toArray() {
      return this.toArray(new BlockVector2[0]);
   }

   @Nonnull
   @Override
   public <T> T[] toArray(T[] array) {
      int size = this.size();
      if (array.length < size) {
         array = (T[])Arrays.copyOf(array, size);
      } else if (array.length > size) {
         array[size] = null;
      }

      int index = 0;

      for (int i = 0; i < size; i++) {
         int x = this.offsetX + MathMan.unpairSearchCoordsX(index);
         int z = this.offsetZ + MathMan.unpairSearchCoordsY(index);
         array[i] = (T)BlockVector2.at(x, z);
         index++;
      }

      return array;
   }

   public boolean canAdd(int x, int z) {
      if (this.offsetX == Integer.MAX_VALUE) {
         return false;
      } else {
         int relX = x - this.offsetX;
         int relZ = z - this.offsetZ;
         return relX <= 32767 && relX >= -32767 && relZ <= 32727 && relZ >= -32767;
      }
   }

   public boolean add(int x, int z) {
      if (this.offsetX == Integer.MAX_VALUE) {
         this.offsetX = x;
         this.offsetZ = z;
      }

      int relX = x - this.offsetX;
      int relZ = z - this.offsetZ;
      if (relX <= 32767 && relX >= -32767 && relZ <= 32767 && relZ >= -32767) {
         int index = this.getIndex(x, z);
         if (this.set.get(index)) {
            return false;
         } else {
            this.set.set(index);
            return true;
         }
      } else {
         throw new UnsupportedOperationException(
            "LocalBlockVector2Set can only contain vectors within 32767 blocks (cuboid) of the first entry. Attempted to set block at "
               + x
               + ", "
               + z
               + ". With origin "
               + this.offsetX
               + " "
               + this.offsetZ
         );
      }
   }

   public boolean add(BlockVector2 vector) {
      return this.add(vector.x(), vector.z());
   }

   private int getIndex(BlockVector2 vector) {
      return this.getIndex(vector.x(), vector.z());
   }

   private int getIndex(int x, int z) {
      return MathMan.pairSearchCoords((short)(x - this.offsetX), (short)(z - this.offsetZ));
   }

   public boolean remove(int x, int z) {
      int relX = x - this.offsetX;
      int relZ = z - this.offsetZ;
      if (relX <= 32767 && relX >= -32767 && relZ <= 32767 && relZ >= -32767) {
         int index = MathMan.pairSearchCoords((short)(x - this.offsetX), (short)(z - this.offsetZ));
         boolean value = this.set.get(index);
         this.set.clear(index);
         return value;
      } else {
         return false;
      }
   }

   @Override
   public boolean remove(Object o) {
      return o instanceof BlockVector2 v ? this.remove(v.x(), v.z()) : false;
   }

   @Override
   public boolean containsAll(Collection<?> c) {
      for (Object o : c) {
         if (!this.contains(o)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean addAll(Collection<? extends BlockVector2> c) {
      boolean result = false;

      for (BlockVector2 v : c) {
         result |= this.add(v);
      }

      return result;
   }

   @Override
   public boolean retainAll(@Nonnull Collection<?> c) {
      boolean result = false;
      int size = this.size();
      int index = -1;
      MutableBlockVector2 mVec = MutableBlockVector2.get(0, 0);

      for (int i = 0; i < size; i++) {
         index = this.set.nextSetBit(index + 1);
         int x = this.offsetX + MathMan.unpairSearchCoordsX(index);
         int z = this.offsetZ + MathMan.unpairSearchCoordsY(index);
         mVec.mutX(x);
         mVec.mutZ(z);
         if (!c.contains(mVec)) {
            result = true;
            this.set.clear(index);
         }
      }

      return result;
   }

   @Override
   public boolean removeAll(Collection<?> c) {
      boolean result = false;

      for (Object o : c) {
         result |= this.remove(o);
      }

      return result;
   }

   public void forEach(LocalBlockVector2Set.BlockVector2SetVisitor visitor) {
      int size = this.size();
      int index = -1;

      for (int i = 0; i < size; i++) {
         index = this.set.nextSetBit(index + 1);
         int x = this.offsetX + MathMan.unpairSearchCoordsX(index);
         int z = this.offsetZ + MathMan.unpairSearchCoordsY(index);
         visitor.run(x, z, index);
      }
   }

   @Override
   public void clear() {
      this.offsetZ = Integer.MAX_VALUE;
      this.offsetX = Integer.MAX_VALUE;
      this.set.clear();
   }

   public interface BlockVector2SetVisitor {
      void run(int x, int z, int index);
   }
}
