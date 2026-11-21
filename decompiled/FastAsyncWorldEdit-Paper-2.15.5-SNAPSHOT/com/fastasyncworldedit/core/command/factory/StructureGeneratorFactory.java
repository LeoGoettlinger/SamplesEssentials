package com.fastasyncworldedit.core.command.factory;

import com.fastasyncworldedit.core.function.generator.StructureGenerator;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.world.generation.StructureType;

public final class StructureGeneratorFactory implements Contextual<StructureGenerator> {
   private final StructureType type;

   public StructureGeneratorFactory(StructureType type) {
      this.type = type;
   }

   public StructureGenerator createFromContext(EditContext input) {
      return new StructureGenerator((EditSession)input.getDestination(), this.type);
   }

   @Override
   public String toString() {
      return "structure of type " + this.type;
   }
}
