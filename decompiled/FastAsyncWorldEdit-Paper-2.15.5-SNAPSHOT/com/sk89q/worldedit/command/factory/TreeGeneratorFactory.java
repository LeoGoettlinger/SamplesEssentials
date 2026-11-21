package com.sk89q.worldedit.command.factory;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.generator.TreeGenerator;
import com.sk89q.worldedit.world.generation.TreeType;

public final class TreeGeneratorFactory implements Contextual<TreeGenerator> {
   private final TreeType type;

   public TreeGeneratorFactory(TreeType type) {
      this.type = type;
   }

   public TreeGenerator createFromContext(EditContext input) {
      return new TreeGenerator((EditSession)input.getDestination(), this.type);
   }

   @Override
   public String toString() {
      return "tree of type " + this.type;
   }
}
