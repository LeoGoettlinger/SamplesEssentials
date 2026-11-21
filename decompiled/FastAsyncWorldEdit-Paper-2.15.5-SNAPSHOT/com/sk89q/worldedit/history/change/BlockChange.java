package com.sk89q.worldedit.history.change;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;

public record BlockChange(BlockVector3 position, BaseBlock previous, BaseBlock current) implements Change {
   public BlockChange(BlockVector3 position, BaseBlock previous, BaseBlock current) {
      Preconditions.checkNotNull(position);
      Preconditions.checkNotNull(previous);
      Preconditions.checkNotNull(current);
      this.position = position;
      this.previous = previous;
      this.current = current;
   }

   public <BP extends BlockStateHolder<BP>, BC extends BlockStateHolder<BC>> BlockChange(BlockVector3 position, BP previous, BC current) {
      this(position, previous.toBaseBlock(), current.toBaseBlock());
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public BlockVector3 getPosition() {
      return this.position;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public BaseBlock getPrevious() {
      return this.previous;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public BaseBlock getCurrent() {
      return this.current;
   }

   @Override
   public void undo(UndoContext context) throws WorldEditException {
      ((Extent)Preconditions.checkNotNull(context.getExtent())).setBlock(this.position, this.previous);
   }

   @Override
   public void redo(UndoContext context) throws WorldEditException {
      ((Extent)Preconditions.checkNotNull(context.getExtent())).setBlock(this.position, this.current);
   }
}
