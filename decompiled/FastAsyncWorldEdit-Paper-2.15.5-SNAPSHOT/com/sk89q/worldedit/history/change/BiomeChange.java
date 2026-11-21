package com.sk89q.worldedit.history.change;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.biome.BiomeType;

@Deprecated
public class BiomeChange implements Change {
   private final BlockVector2 position;
   private final BiomeType previous;
   private final BiomeType current;

   public BiomeChange(BlockVector2 position, BiomeType previous, BiomeType current) {
      Preconditions.checkNotNull(position);
      Preconditions.checkNotNull(previous);
      Preconditions.checkNotNull(current);
      this.position = position;
      this.previous = previous;
      this.current = current;
   }

   public BlockVector2 getPosition() {
      return this.position;
   }

   public BiomeType getPrevious() {
      return this.previous;
   }

   public BiomeType getCurrent() {
      return this.current;
   }

   @Override
   public void undo(UndoContext context) throws WorldEditException {
      ((Extent)Preconditions.checkNotNull(context.getExtent())).setBiome(this.position.toBlockVector3(), this.previous);
   }

   @Override
   public void redo(UndoContext context) throws WorldEditException {
      ((Extent)Preconditions.checkNotNull(context.getExtent())).setBiome(this.position.toBlockVector3(), this.current);
   }
}
