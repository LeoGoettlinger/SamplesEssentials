package com.sk89q.worldedit.util.collection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.LocatedBlock;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.Collection;
import java.util.Iterator;
import javax.annotation.Nullable;

public class LocatedBlockList implements Iterable<LocatedBlock> {
   private final BlockMap<BaseBlock> blocks = BlockMap.createForBaseBlock();
   private final PositionList order = PositionList.create(WorldEdit.getInstance().getConfiguration().extendedYLimit);

   public LocatedBlockList() {
   }

   public LocatedBlockList(Collection<? extends LocatedBlock> collection) {
      for (LocatedBlock locatedBlock : collection) {
         this.add(locatedBlock.getLocation(), locatedBlock.getBlock());
      }
   }

   public void add(LocatedBlock setBlockCall) {
      Preconditions.checkNotNull(setBlockCall);
      this.add(setBlockCall.getLocation(), setBlockCall.getBlock());
   }

   public <B extends BlockStateHolder<B>> void add(BlockVector3 location, B block) {
      this.blocks.put(location, block.toBaseBlock());
      this.order.add(location);
   }

   public boolean containsLocation(BlockVector3 location) {
      return this.blocks.containsKey(location);
   }

   @Nullable
   public BaseBlock get(BlockVector3 location) {
      return this.blocks.get(location);
   }

   public int size() {
      return this.order.size();
   }

   public void clear() {
      this.blocks.clear();
      this.order.clear();
   }

   @Override
   public Iterator<LocatedBlock> iterator() {
      return Iterators.transform(this.order.iterator(), position -> new LocatedBlock(position, this.blocks.get(position)));
   }

   public Iterator<LocatedBlock> reverseIterator() {
      return Iterators.transform(this.order.reverseIterator(), position -> new LocatedBlock(position, this.blocks.get(position)));
   }
}
