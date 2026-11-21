package com.sk89q.worldedit.world.registry;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.biome.BiomeData;
import com.sk89q.worldedit.world.biome.BiomeType;
import javax.annotation.Nullable;

public interface BiomeRegistry {
   Component getRichName(BiomeType biomeType);

   @Deprecated
   @Nullable
   BiomeData getData(BiomeType biome);
}
