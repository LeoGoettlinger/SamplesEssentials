package com.fastasyncworldedit.core.math;

import com.fastasyncworldedit.core.math.sparsebits.SparseBitSet;
import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.collection.BlockVector3Set;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import javax.annotation.Nonnull;

public class LocalBlockVectorSet implements BlockVector3Set {
   private final SparseBitSet set;
   private int offsetX;
   private int offsetZ;
   private int offsetY = 128;

   public LocalBlockVectorSet() {
      this.offsetX = this.offsetZ = Integer.MAX_VALUE;
      this.set = new SparseBitSet();
   }

   public LocalBlockVectorSet(int x, int z) {
      this.offsetX = x;
      this.offsetZ = z;
      this.set = new SparseBitSet();
   }

   public LocalBlockVectorSet(int x, int y, int z) {
      this.offsetX = x;
      this.offsetY = y;
      this.offsetZ = z;
      this.set = new SparseBitSet();
   }

   public static BlockVector3Set wrap(LocalBlockVectorSet set) {
      return new LocalBlockVectorSet.BlockVector3SetHolder(set);
   }

   public static BlockVector3Set wrapped() {
      return new LocalBlockVectorSet.BlockVector3SetHolder(new LocalBlockVectorSet());
   }

   public static BlockVector3Set wrapped(int x, int z) {
      return new LocalBlockVectorSet.BlockVector3SetHolder(new LocalBlockVectorSet(x, z));
   }

   public static BlockVector3Set wrapped(int x, int y, int z) {
      return new LocalBlockVectorSet.BlockVector3SetHolder(new LocalBlockVectorSet(x, y, z));
   }

   private LocalBlockVectorSet(int x, int y, int z, SparseBitSet set) {
      this.offsetX = x;
      this.offsetY = y;
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

   public int offsetX() {
      return this.offsetX;
   }

   public int offsetY() {
      return this.offsetY;
   }

   public int offsetZ() {
      return this.offsetZ;
   }

   @Override
   public boolean contains(int x, int y, int z) {
      return this.set.get(MathMan.tripleSearchCoords(x - this.offsetX, y - this.offsetY, z - this.offsetZ));
   }

   @Override
   public boolean contains(Object o) {
      return o instanceof BlockVector3 v ? this.contains(v.x(), v.y(), v.z()) : false;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.13.0"
   )
   public LocalBlockVectorSet clone() {
      return this.copy();
   }

   public LocalBlockVectorSet copy() {
      return new LocalBlockVectorSet(this.offsetX, this.offsetY, this.offsetZ, this.set.clone());
   }

   @Override
   public boolean containsRadius(int x, int y, int z, int radius) {
      if (radius <= 0) {
         return this.contains(x, y, z);
      } else {
         int length = radius * 2;
         if (this.size() < length * length * length) {
            int index = -1;

            while ((index = this.set.nextSetBit(index + 1)) != -1) {
               int b1 = index & 0xFF;
               int b2 = index >> 8 & 0xFF;
               int b3 = index >> 15 & 0xFF;
               int b4 = index >> 23 & 0xFF;
               int ix = this.offsetX + (b3 + ((b2 & 7) << 8) << 21 >> 21);
               int iy = this.offsetY + b1 * ((b2 >> 6 & 1) == 0 ? 1 : -1);
               int iz = this.offsetZ + (b4 + ((b2 >> 3 & 7) << 8) << 21 >> 21);
               if (Math.abs(ix - x) <= radius && Math.abs(iz - z) <= radius && Math.abs(iy - y) <= radius) {
                  return true;
               }
            }

            return false;
         } else {
            for (int xx = -radius; xx <= radius; xx++) {
               int rx = x + xx;

               for (int yy = -radius; yy <= radius; yy++) {
                  int ry = y + yy;

                  for (int zz = -radius; zz <= radius; zz++) {
                     if (this.contains(rx, ry, z + zz)) {
                        return true;
                     }
                  }
               }
            }

            return false;
         }
      }
   }

   @Override
   public void setOffset(int x, int z) {
      this.offsetX = x;
      this.offsetZ = z;
   }

   @Override
   public void setOffset(int x, int y, int z) {
      this.offsetX = x;
      this.offsetY = y;
      this.offsetZ = z;
   }

   protected MutableBlockVector3 getIndex(int getIndex) {
      int size = this.size();
      if (getIndex > size) {
         return null;
      } else {
         int index = -1;

         for (int i = 0; i <= getIndex; i++) {
            index = this.set.nextSetBit(index + 1);
         }

         if (index != -1) {
            int b1 = index & 0xFF;
            int b2 = index >> 8 & 0xFF;
            int b3 = index >> 15 & 0xFF;
            int b4 = index >> 23 & 0xFF;
            int x = this.offsetX + (b3 + ((b2 & 7) << 8) << 21 >> 21);
            int y = this.offsetY + b1 * ((b2 >> 6 & 1) == 0 ? 1 : -1);
            int z = this.offsetZ + (b4 + ((b2 >> 3 & 7) << 8) << 21 >> 21);
            return MutableBlockVector3.get(x, y, z);
         } else {
            return null;
         }
      }
   }

   @Nonnull
   @Override
   public Iterator<BlockVector3> iterator() {
      return new Iterator<BlockVector3>() {
         final MutableBlockVector3 mutable = new MutableBlockVector3(0, 0, 0);
         int index = LocalBlockVectorSet.this.set.nextSetBit(0);
         int previous = -1;

         @Override
         public void remove() {
            LocalBlockVectorSet.this.set.clear(this.previous);
         }

         @Override
         public boolean hasNext() {
            return this.index != -1;
         }

         public BlockVector3 next() {
            if (this.index != -1) {
               int b1 = this.index & 0xFF;
               int b2 = this.index >> 8 & 0xFF;
               int b3 = this.index >> 15 & 0xFF;
               int b4 = this.index >> 23 & 0xFF;
               int x = LocalBlockVectorSet.this.offsetX + (b3 + ((b2 & 7) << 8) << 21 >> 21);
               int y = LocalBlockVectorSet.this.offsetY + b1 * ((b2 >> 6 & 1) == 0 ? 1 : -1);
               int z = LocalBlockVectorSet.this.offsetZ + (b4 + ((b2 >> 3 & 7) << 8) << 21 >> 21);
               this.mutable.mutX(x);
               this.mutable.mutY(y);
               this.mutable.mutZ(z);
               this.previous = this.index;
               this.index = LocalBlockVectorSet.this.set.nextSetBit(this.index + 1);
               return this.mutable;
            } else {
               return null;
            }
         }
      };
   }

   @Nonnull
   public BlockVector3[] toArray() {
      return this.toArray(new BlockVector3[0]);
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
         index = this.set.nextSetBit(index);
         int b1 = index & 0xFF;
         int b2 = index >> 8 & 0xFF;
         int b3 = index >> 15 & 0xFF;
         int b4 = index >> 23 & 0xFF;
         int x = this.offsetX + (b3 + ((b2 & 7) << 8) << 21 >> 21);
         int y = this.offsetY + b1 * ((b2 >> 6 & 1) == 0 ? 1 : -1);
         int z = this.offsetZ + (b4 + ((b2 >> 3 & 7) << 8) << 21 >> 21);
         array[i] = (T)BlockVector3.at(x, y, z);
         index++;
      }

      return array;
   }

   public boolean canAdd(int x, int y, int z) {
      if (this.offsetX == Integer.MAX_VALUE) {
         return false;
      } else {
         int relX = x - this.offsetX;
         int relZ = z - this.offsetZ;
         if (relX <= 1023 && relX >= -1024 && relZ <= 1023 && relZ >= -1024) {
            int relY = y - this.offsetY;
            return relY >= -256 && relY <= 255;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean add(int x, int y, int z) {
      if (this.offsetX == Integer.MAX_VALUE) {
         this.offsetX = x;
         this.offsetZ = z;
      }

      int relX = x - this.offsetX;
      int relZ = z - this.offsetZ;
      int relY = y - this.offsetY;
      if (relX <= 1023 && relX >= -1024 && relZ <= 1023 && relZ >= -1024 && relY >= -256 && relY <= 255) {
         int index = this.getIndex(x, y, z);
         if (this.set.get(index)) {
            return false;
         } else {
            this.set.set(index);
            return true;
         }
      } else {
         throw new IndexOutOfBoundsException(
            "LocalVectorSet can only contain vectors within 1024 blocks (cuboid) of the first entry. Attempted to set block at "
               + x
               + ", "
               + y
               + ", "
               + z
               + ". With origin "
               + this.offsetX
               + " "
               + this.offsetY
               + " "
               + this.offsetZ
         );
      }
   }

   public boolean add(BlockVector3 vector) {
      return this.add(vector.x(), vector.y(), vector.z());
   }

   private int getIndex(BlockVector3 vector) {
      return MathMan.tripleSearchCoords(vector.x() - this.offsetX, vector.y() - this.offsetY, vector.z() - this.offsetZ);
   }

   private int getIndex(int x, int y, int z) {
      return MathMan.tripleSearchCoords(x - this.offsetX, y - this.offsetY, z - this.offsetZ);
   }

   public boolean remove(int x, int y, int z) {
      int relX = x - this.offsetX;
      int relZ = z - this.offsetZ;
      int relY = y - this.offsetY;
      if (relX <= 1023 && relX >= -1024 && relZ <= 1023 && relZ >= -1024 && relY >= -256 && relY <= 255) {
         int index = MathMan.tripleSearchCoords(relX, relY, relZ);
         boolean value = this.set.get(index);
         this.set.clear(index);
         return value;
      } else {
         return false;
      }
   }

   @Override
   public boolean remove(Object o) {
      return o instanceof BlockVector3 v ? this.remove(v.x(), v.y(), v.z()) : false;
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
   public boolean addAll(Collection<? extends BlockVector3> c) {
      boolean result = false;

      for (BlockVector3 v : c) {
         result |= this.add(v);
      }

      return result;
   }

   @Override
   public boolean retainAll(@Nonnull Collection<?> c) {
      boolean result = false;
      int size = this.size();
      int index = -1;
      MutableBlockVector3 mVec = MutableBlockVector3.get(0, 0, 0);

      for (int i = 0; i < size; i++) {
         index = this.set.nextSetBit(index + 1);
         int b1 = index & 0xFF;
         int b2 = index >> 8 & 0xFF;
         int b3 = index >> 15 & 0xFF;
         int b4 = index >> 23 & 0xFF;
         int x = this.offsetX + (b3 + ((b2 & 7) << 8) << 21 >> 21);
         int y = this.offsetY + b1 * ((b2 >> 6 & 1) == 0 ? 1 : -1);
         int z = this.offsetZ + (b4 + ((b2 >> 3 & 7) << 8) << 21 >> 21);
         mVec.mutX(x);
         mVec.mutY(y);
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

   public void forEach(LocalBlockVectorSet.BlockVectorSetVisitor visitor) {
      int size = this.size();
      int index = -1;

      for (int i = 0; i < size; i++) {
         index = this.set.nextSetBit(index + 1);
         int b1 = index & 0xFF;
         int b2 = index >> 8 & 0xFF;
         int b3 = index >> 15 & 0xFF;
         int b4 = index >> 23 & 0xFF;
         int x = this.offsetX + (b3 + ((b2 & 7) << 8) << 21 >> 21);
         int y = this.offsetY + b1 * ((b2 >> 6 & 1) == 0 ? 1 : -1);
         int z = this.offsetZ + (b4 + ((b2 >> 3 & 7) << 8) << 21 >> 21);
         visitor.run(x, y, z, index);
      }
   }

   @Override
   public void clear() {
      this.offsetZ = Integer.MAX_VALUE;
      this.offsetX = Integer.MAX_VALUE;
      this.set.clear();
   }

   public boolean isInitialised() {
      return this.offsetX != Integer.MAX_VALUE && this.offsetZ != Integer.MAX_VALUE;
   }

   private static class BlockVector3SetHolder implements BlockVector3Set {
      BlockVector3Set set;

      private BlockVector3SetHolder(BlockVector3Set set) {
         this.set = set;
      }

      @Override
      public boolean add(int x, int y, int z) {
         try {
            return this.set.add(x, y, z);
         } catch (IndexOutOfBoundsException var7) {
            if (this.set instanceof LocalBlockVectorSet localBlockVectorSet) {
               this.set = new BlockVectorSet(localBlockVectorSet);
               return this.set.add(x, y, z);
            } else {
               throw var7;
            }
         }
      }

      @Override
      public boolean contains(int x, int y, int z) {
         return this.set.contains(x, y, z);
      }

      @Override
      public void setOffset(int x, int z) {
         this.set.setOffset(x, z);
      }

      @Override
      public void setOffset(int x, int y, int z) {
         this.set.setOffset(x, y, z);
      }

      @Override
      public boolean containsRadius(int x, int y, int z, int radius) {
         return this.set.containsRadius(x, y, z, radius);
      }

      @Override
      public int size() {
         return this.set.size();
      }

      @Override
      public boolean isEmpty() {
         return this.set.isEmpty();
      }

      @Override
      public boolean contains(Object o) {
         return this.set.contains(o);
      }

      @Nonnull
      @Override
      public Iterator<BlockVector3> iterator() {
         return this.set.iterator();
      }

      @Nonnull
      @Override
      public Object[] toArray() {
         return this.set.toArray();
      }

      @Nonnull
      @Override
      public <T> T[] toArray(@Nonnull T[] a) {
         return (T[])this.set.toArray(a);
      }

      public boolean add(BlockVector3 blockVector3) {
         return this.set.add(blockVector3);
      }

      @Override
      public boolean remove(Object o) {
         return this.set.remove(o);
      }

      @Override
      public boolean containsAll(@Nonnull Collection<?> c) {
         return this.set.containsAll(c);
      }

      @Override
      public boolean addAll(@Nonnull Collection<? extends BlockVector3> c) {
         return this.set.addAll(c);
      }

      @Override
      public boolean retainAll(@Nonnull Collection<?> c) {
         return this.set.retainAll(c);
      }

      @Override
      public boolean removeAll(@Nonnull Collection<?> c) {
         return this.set.removeAll(c);
      }

      @Override
      public void clear() {
         this.set.clear();
      }

      public LocalBlockVectorSet.BlockVector3SetHolder copy() {
         BlockVector3Set copied = this.set.copy();
         return new LocalBlockVectorSet.BlockVector3SetHolder(copied);
      }
   }

   public interface BlockVectorSetVisitor {
      void run(int x, int y, int z, int index);
   }
}
