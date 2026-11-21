package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.translation.TranslationManager;
import com.sk89q.worldedit.world.biome.BiomeData;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.registry.BiomeRegistry;
import javax.annotation.Nullable;
import org.bukkit.block.Biome;

class BukkitBiomeRegistry implements BiomeRegistry {
   @Override
   public Component getRichName(BiomeType biomeType) {
      return TranslatableComponent.of(TranslationManager.makeTranslationKey("biome", biomeType.id()));
   }

   @Deprecated
   @Nullable
   @Override
   public BiomeData getData(BiomeType biome) {
      Biome bukkitBiome = BukkitAdapter.adapt(biome);
      return bukkitBiome == null ? null : bukkitBiome::name;
   }
}
