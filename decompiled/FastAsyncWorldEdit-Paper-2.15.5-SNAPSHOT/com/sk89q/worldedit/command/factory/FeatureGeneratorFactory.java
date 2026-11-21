package com.sk89q.worldedit.command.factory;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.generator.FeatureGenerator;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;

public final class FeatureGeneratorFactory implements Contextual<FeatureGenerator> {
   private final ConfiguredFeatureType type;

   public FeatureGeneratorFactory(ConfiguredFeatureType type) {
      this.type = type;
   }

   public FeatureGenerator createFromContext(EditContext input) {
      return new FeatureGenerator((EditSession)input.getDestination(), this.type);
   }

   @Override
   public String toString() {
      return "feature of type " + this.type;
   }
}
