package com.sk89q.worldedit.world.registry;

public interface Registries {
   BlockRegistry getBlockRegistry();

   ItemRegistry getItemRegistry();

   EntityRegistry getEntityRegistry();

   BiomeRegistry getBiomeRegistry();

   BlockCategoryRegistry getBlockCategoryRegistry();

   ItemCategoryRegistry getItemCategoryRegistry();
}
