package com.fastasyncworldedit.core.history.change;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.history.change.Change;

public class MutableTileChange implements Change {
   public CompoundTag tag;
   public boolean create;

   public MutableTileChange(CompoundTag tag, boolean create) {
      this.tag = tag;
      this.create = create;
   }

   @Override
   public void undo(UndoContext context) throws WorldEditException {
      if (!this.create) {
         this.create(context);
      }
   }

   @Override
   public void redo(UndoContext context) throws WorldEditException {
      if (this.create) {
         this.create(context);
      }
   }

   public void create(UndoContext context) {
      int x = this.tag.getInt("x");
      int y = this.tag.getInt("y");
      int z = this.tag.getInt("z");
      context.getExtent().setTile(x, y, z, this.tag);
   }
}
