package com.sk89q.worldedit.history.changeset;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.LocatedBlock;
import com.sk89q.worldedit.util.collection.LocatedBlockList;
import java.util.Iterator;

public class BlockOptimizedHistory extends ArrayListHistory {
   private final LocatedBlockList previous = new LocatedBlockList();
   private final LocatedBlockList current = new LocatedBlockList();

   private static Change createChange(LocatedBlock block) {
      return new BlockChange(block.getLocation(), block.getBlock(), block.getBlock());
   }

   @Override
   public void add(Change change) {
      Preconditions.checkNotNull(change);
      if (change instanceof BlockChange blockChange) {
         BlockVector3 position = blockChange.getPosition();
         if (!this.previous.containsLocation(position)) {
            this.previous.add(position, blockChange.previous());
         }

         this.current.add(position, blockChange.getCurrent());
      } else {
         super.add(change);
      }
   }

   @Override
   public Iterator<Change> forwardIterator() {
      return Iterators.concat(super.forwardIterator(), Iterators.transform(this.current.iterator(), BlockOptimizedHistory::createChange));
   }

   @Override
   public Iterator<Change> backwardIterator() {
      return Iterators.concat(super.backwardIterator(), Iterators.transform(this.previous.reverseIterator(), BlockOptimizedHistory::createChange));
   }

   @Override
   public int size() {
      return super.size() + this.previous.size();
   }
}
