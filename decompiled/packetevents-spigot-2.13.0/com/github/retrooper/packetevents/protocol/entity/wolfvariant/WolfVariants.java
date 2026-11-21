package com.github.retrooper.packetevents.protocol.entity.wolfvariant;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.protocol.world.biome.Biome;
import com.github.retrooper.packetevents.protocol.world.biome.Biomes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import java.util.Collections;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class WolfVariants {
   private static final VersionedRegistry<WolfVariant> REGISTRY = new VersionedRegistry<>("wolf_variant");
   public static final WolfVariant PALE = define("pale", "wolf", new MappedEntitySet<>(Collections.singletonList(Biomes.TAIGA)));
   public static final WolfVariant SPOTTED = define("spotted", new MappedEntitySet<>(ResourceLocation.minecraft("is_savanna")));
   public static final WolfVariant SNOWY = define("snowy", new MappedEntitySet<>(Collections.singletonList(Biomes.GROVE)));
   public static final WolfVariant BLACK = define("black", new MappedEntitySet<>(Collections.singletonList(Biomes.OLD_GROWTH_PINE_TAIGA)));
   public static final WolfVariant ASHEN = define("ashen", new MappedEntitySet<>(Collections.singletonList(Biomes.SNOWY_TAIGA)));
   public static final WolfVariant RUSTY = define("rusty", new MappedEntitySet<>(ResourceLocation.minecraft("is_jungle")));
   public static final WolfVariant WOODS = define("woods", new MappedEntitySet<>(Collections.singletonList(Biomes.FOREST)));
   public static final WolfVariant CHESTNUT = define("chestnut", new MappedEntitySet<>(Collections.singletonList(Biomes.OLD_GROWTH_SPRUCE_TAIGA)));
   public static final WolfVariant STRIPED = define("striped", new MappedEntitySet<>(ResourceLocation.minecraft("is_badlands")));

   private WolfVariants() {
   }

   @Internal
   public static WolfVariant define(String name, MappedEntitySet<Biome> biomes) {
      return define(name, "wolf_" + name, biomes);
   }

   @Internal
   public static WolfVariant define(String name, String assetId, MappedEntitySet<Biome> biomes) {
      WolfAssetSet assets = WolfAssetSet.getOrThrow("minecraft", "entity/wolf/" + assetId, "");
      WolfAssetSet babyAssets = WolfAssetSet.getOrThrow("minecraft", "entity/wolf/" + assetId, "_baby");
      return define(name, assets, babyAssets, biomes);
   }

   @Internal
   public static WolfVariant define(String name, WolfAssetSet assets, WolfAssetSet babyAssets, MappedEntitySet<Biome> biomes) {
      return REGISTRY.define(name, data -> new StaticWolfVariant(data, assets, babyAssets, biomes));
   }

   public static VersionedRegistry<WolfVariant> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }
}
