package com.sk89q.worldedit.function.generator;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.generation.TreeType;

public final class TreeGenerator implements RegionFunction {
   private final TreeType treeType;
   private final EditSession editSession;

   public TreeGenerator(EditSession editSession, TreeType treeType) {
      this.editSession = editSession;
      this.treeType = treeType;
   }

   @Override
   public boolean apply(BlockVector3 position) throws WorldEditException {
      return this.editSession.getWorld().generateTree(this.treeType, this.editSession, position);
   }
}
