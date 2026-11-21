package com.fastasyncworldedit.core.math;

import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.collection.BlockVector3Set;
import com.sk89q.worldedit.math.BlockVector3;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class BlockVectorSet extends AbstractCollection<BlockVector3> implements BlockVector3Set {
   private final Long2ObjectLinkedOpenHashMap<LocalBlockVectorSet> localSets = new Long2ObjectLinkedOpenHashMap(4);

   public BlockVectorSet() {
   }

   public BlockVectorSet(LocalBlockVectorSet localSet) {
      if (localSet != null && localSet.isInitialised()) {
         int offsetX = localSet.offsetX();
         int offsetY = localSet.offsetY();
         int offsetZ = localSet.offsetZ();
         if ((offsetX & 2047) == 0 && (offsetY & 511) == 0 && (offsetZ & 2047) == 0) {
            this.localSets.put(MathMan.tripleWorldCoord(offsetX >> 11, offsetY >> 9, offsetZ >> 11), localSet);
         } else {
            localSet.forEach((x, y, z, index) -> this.add(x, y, z));
         }
      }
   }

   private BlockVectorSet(Map<Long, ? extends LocalBlockVectorSet> sets) {
      this.localSets.putAll(sets);
   }

   @Override
   public int size() {
      int size = 0;
      ObjectBidirectionalIterator var2 = this.localSets.long2ObjectEntrySet().iterator();

      while (var2.hasNext()) {
         Entry<LocalBlockVectorSet> entry = (Entry<LocalBlockVectorSet>)var2.next();
         size += ((LocalBlockVectorSet)entry.getValue()).size();
      }

      return size;
   }

   public BlockVector3 get(int index) {
      int count = 0;
      ObjectBidirectionalIterator var3 = this.localSets.long2ObjectEntrySet().iterator();

      while (var3.hasNext()) {
         Entry<LocalBlockVectorSet> entry = (Entry<LocalBlockVectorSet>)var3.next();
         LocalBlockVectorSet set = (LocalBlockVectorSet)entry.getValue();
         int size = set.size();
         int newSize = count + size;
         if (newSize > index) {
            int localIndex = index - count;
            MutableBlockVector3 pos = set.getIndex(localIndex);
            if (pos != null) {
               long triple = entry.getLongKey();
               int cx = (int)MathMan.untripleWorldCoordX(triple);
               int cy = (int)MathMan.untripleWorldCoordY(triple);
               int cz = (int)MathMan.untripleWorldCoordZ(triple);
               pos.mutX((cx << 11) + pos.x());
               pos.mutY((cy << 9) + pos.y());
               pos.mutZ((cz << 11) + pos.z());
               return pos.toImmutable();
            }
         }

         count += newSize;
      }

      return null;
   }

   @Override
   public boolean isEmpty() {
      ObjectBidirectionalIterator var1 = this.localSets.long2ObjectEntrySet().iterator();

      while (var1.hasNext()) {
         Entry<LocalBlockVectorSet> entry = (Entry<LocalBlockVectorSet>)var1.next();
         if (!((LocalBlockVectorSet)entry.getValue()).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean contains(int x, int y, int z) {
      int indexedY = y + 128 >> 9;
      long triple = MathMan.tripleWorldCoord(x >> 11, indexedY, z >> 11);
      LocalBlockVectorSet localMap = (LocalBlockVectorSet)this.localSets.get(triple);
      return localMap != null && localMap.contains(x & 2047, (y + 128 & 511) - 128, z & 2047);
   }

   @Override
   public void setOffset(final int x, final int z) {
   }

   @Override
   public void setOffset(final int x, final int y, final int z) {
   }

   @Override
   public boolean containsRadius(final int x, final int y, final int z, final int radius) {
      if (radius <= 0) {
         return this.contains(x, y, z);
      } else if (!this.contains(x - radius, y, z - radius)) {
         return false;
      } else if (!this.contains(x + radius, y, z + radius)) {
         return false;
      } else if (!this.contains(x - radius, y, z + radius)) {
         return false;
      } else if (!this.contains(x + radius, y, z - radius)) {
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

   @Override
   public boolean contains(Object o) {
      return o instanceof BlockVector3 v ? this.contains(v.x(), v.y(), v.z()) : false;
   }

   @Nonnull
   @Override
   public Iterator<BlockVector3> iterator() {
      final ObjectIterator<Entry<LocalBlockVectorSet>> entries = this.localSets.long2ObjectEntrySet().iterator();
      return !entries.hasNext() ? Collections.emptyIterator() : new Iterator<BlockVector3>() {
         Entry<LocalBlockVectorSet> entry = (Entry<LocalBlockVectorSet>)entries.next();
         Iterator<BlockVector3> entryIter = ((LocalBlockVectorSet)BlockVectorSet.super.entry.getValue()).iterator();
         final MutableBlockVector3 mutable = new MutableBlockVector3();

         @Override
         public void remove() {
            this.entryIter.remove();
         }

         @Override
         public boolean hasNext() {
            return this.entryIter.hasNext() || entries.hasNext();
         }

         public BlockVector3 next() {
            while (!this.entryIter.hasNext()) {
               if (!entries.hasNext()) {
                  throw new NoSuchElementException("End of iterator");
               }

               this.entry = (Entry<LocalBlockVectorSet>)entries.next();
               this.entryIter = ((LocalBlockVectorSet)this.entry.getValue()).iterator();
            }

            BlockVector3 localPos = this.entryIter.next();
            long triple = this.entry.getLongKey();
            int cx = (int)MathMan.untripleWorldCoordX(triple);
            int cy = (int)MathMan.untripleWorldCoordY(triple);
            int cz = (int)MathMan.untripleWorldCoordZ(triple);
            return this.mutable.setComponents((cx << 11) + localPos.x(), (cy << 9) + localPos.y(), (cz << 11) + localPos.z());
         }
      };
   }

   public boolean add(BlockVector3 vector) {
      return this.add(vector.x(), vector.y(), vector.z());
   }

   @Override
   public boolean add(int x, int y, int z) {
      int indexedY = y + 128 >> 9;
      long triple = MathMan.tripleWorldCoord(x >> 11, indexedY, z >> 11);
      LocalBlockVectorSet localMap = (LocalBlockVectorSet)this.localSets.get(triple);
      if (localMap == null) {
         localMap = new LocalBlockVectorSet();
         localMap.setOffset(1024, 1024);
         this.localSets.put(triple, localMap);
      }

      return localMap.add(x & 2047, (y + 128 & 511) - 128, z & 2047);
   }

   public boolean remove(int x, int y, int z) {
      int indexedY = y + 128 >> 9;
      long triple = MathMan.tripleWorldCoord(x >> 11, indexedY, z >> 11);
      LocalBlockVectorSet localMap = (LocalBlockVectorSet)this.localSets.get(triple);
      if (localMap != null && localMap.remove(x & 2047, (y + 128 & 511) - 128, z & 2047)) {
         if (localMap.isEmpty()) {
            this.localSets.remove(triple);
         }

         return true;
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
      boolean modified = false;
      Iterator<BlockVector3> it = this.iterator();

      while (it.hasNext()) {
         if (!c.contains(it.next())) {
            it.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean removeAll(Collection<?> c) {
      boolean result = false;

      for (Object o : c) {
         result |= this.remove(o);
      }

      return result;
   }

   @Override
   public void clear() {
      this.localSets.clear();
   }

   public BlockVectorSet copy() {
      return new BlockVectorSet(
         this.localSets.long2ObjectEntrySet().stream().collect(Collectors.toMap(Entry::getLongKey, e -> ((LocalBlockVectorSet)e.getValue()).copy()))
      );
   }
}
