package com.sk89q.worldedit.history.change;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;

public record BiomeChange3D(BlockVector3 position, BiomeType previous, BiomeType current) implements Change {
   public BiomeChange3D(BlockVector3 position, BiomeType previous, BiomeType current) {
      Preconditions.checkNotNull(position);
      Preconditions.checkNotNull(previous);
      Preconditions.checkNotNull(current);
      this.position = position;
      this.previous = previous;
      this.current = current;
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
   public BiomeType getPrevious() {
      return this.previous;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public BiomeType getCurrent() {
      return this.current;
   }

   @Override
   public void undo(UndoContext context) throws WorldEditException {
      ((Extent)Preconditions.checkNotNull(context.getExtent())).setBiome(this.position, this.previous);
   }

   @Override
   public void redo(UndoContext context) throws WorldEditException {
      ((Extent)Preconditions.checkNotNull(context.getExtent())).setBiome(this.position, this.current);
   }
}
