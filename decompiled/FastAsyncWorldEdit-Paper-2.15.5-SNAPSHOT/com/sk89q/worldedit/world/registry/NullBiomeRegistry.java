package com.sk89q.worldedit.world.registry;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.translation.TranslationManager;
import com.sk89q.worldedit.world.biome.BiomeData;
import com.sk89q.worldedit.world.biome.BiomeType;
import javax.annotation.Nullable;

public class NullBiomeRegistry implements BiomeRegistry {
   @Override
   public Component getRichName(BiomeType biomeType) {
      return Caption.of(TranslationManager.makeTranslationKey("biome", biomeType.id()));
   }

   @Deprecated
   @Nullable
   @Override
   public BiomeData getData(BiomeType biome) {
      return null;
   }
}
