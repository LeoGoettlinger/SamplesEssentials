package com.sk89q.worldedit.util.collection;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.google.common.collect.AbstractIterator;
import com.sk89q.worldedit.math.BlockVector3;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.Iterator;

class VectorPositionList implements PositionList {
   private final IntList delegateX = new IntArrayList();
   private final IntList delegateY = new IntArrayList();
   private final IntList delegateZ = new IntArrayList();

   @Override
   public BlockVector3 get(int index) {
      return BlockVector3.at(this.delegateX.getInt(index), this.delegateY.getInt(index), this.delegateZ.getInt(index));
   }

   @Override
   public void add(BlockVector3 vector) {
      this.delegateX.add(vector.x());
      this.delegateY.add(vector.y());
      this.delegateZ.add(vector.z());
   }

   @Override
   public int size() {
      return this.delegateX.size();
   }

   @Override
   public void clear() {
      this.delegateX.clear();
      this.delegateY.clear();
      this.delegateZ.clear();
   }

   @Override
   public Iterator<BlockVector3> iterator() {
      return new AbstractIterator<BlockVector3>() {
         private final IntIterator iteratorX = VectorPositionList.this.delegateX.iterator();
         private final IntIterator iteratorY = VectorPositionList.this.delegateY.iterator();
         private final IntIterator iteratorZ = VectorPositionList.this.delegateZ.iterator();
         private final MutableBlockVector3 mutable = new MutableBlockVector3();

         protected BlockVector3 computeNext() {
            return (BlockVector3)(!this.iteratorX.hasNext()
               ? (BlockVector3)this.endOfData()
               : this.mutable.setComponents(this.iteratorX.nextInt(), this.iteratorY.nextInt(), this.iteratorZ.nextInt()));
         }
      };
   }

   @Override
   public Iterator<BlockVector3> reverseIterator() {
      return new AbstractIterator<BlockVector3>() {
         private final IntListIterator iteratorX = VectorPositionList.this.delegateX.listIterator(VectorPositionList.this.delegateX.size());
         private final IntListIterator iteratorY = VectorPositionList.this.delegateY.listIterator(VectorPositionList.this.delegateY.size());
         private final IntListIterator iteratorZ = VectorPositionList.this.delegateZ.listIterator(VectorPositionList.this.delegateZ.size());
         private final MutableBlockVector3 mutable = new MutableBlockVector3();

         protected BlockVector3 computeNext() {
            return (BlockVector3)(!this.iteratorX.hasPrevious()
               ? (BlockVector3)this.endOfData()
               : this.mutable.setComponents(this.iteratorX.previousInt(), this.iteratorY.previousInt(), this.iteratorZ.previousInt()));
         }
      };
   }
}
