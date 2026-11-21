package com.viaversion.viaversion.protocols.v26_2to26_3.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;

public final class MappingData26_3 extends MappingDataBase {
   private static final String[] BREWING_INPUTS = new String[]{"potion", "splash_potion", "lingering_potion", "glass_bottle"};
   private static final String[] BREWING_REAGENTS = new String[]{
      "blaze_powder",
      "breeze_rod",
      "cobweb",
      "dragon_breath",
      "fermented_spider_eye",
      "ghast_tear",
      "glistering_melon_slice",
      "glowstone_dust",
      "golden_carrot",
      "gunpowder",
      "magma_cream",
      "nether_wart",
      "phantom_membrane",
      "pufferfish",
      "rabbit_foot",
      "redstone",
      "slime_block",
      "spider_eye",
      "stone",
      "sugar",
      "turtle_helmet"
   };
   private CompoundTag blockTransformerRegistry;
   private int[] brewingInputIds;
   private int[] brewingReagentIds;

   public MappingData26_3() {
      super("26.2", "26.3");
   }

   @Override
   protected void loadExtras(CompoundTag data) {
      this.blockTransformerRegistry = MappingDataLoader.INSTANCE.loadNBTFromFile("block-transformer-registry-26.3.nbt");
      this.brewingInputIds = this.itemIds(BREWING_INPUTS);
      this.brewingReagentIds = this.itemIds(BREWING_REAGENTS);
   }

   private int[] itemIds(String[] identifiers) {
      IntList ids = new IntArrayList(identifiers.length);

      for (String identifier : identifiers) {
         int id = this.getFullItemMappings().mappedId(identifier);
         if (id != -1) {
            ids.add(id);
         }
      }

      return ids.toIntArray();
   }

   public CompoundTag blockTransformerRegistry() {
      return this.blockTransformerRegistry;
   }

   public int[] brewingInputIds() {
      return this.brewingInputIds;
   }

   public int[] brewingReagentIds() {
      return this.brewingReagentIds;
   }
}
