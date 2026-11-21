package com.github.retrooper.packetevents.protocol.entity.pig;

import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class PigVariants {
   private static final VersionedRegistry<PigVariant> REGISTRY = new VersionedRegistry<>("pig_variant");
   public static final PigVariant COLD = define("cold", PigVariant.ModelType.COLD, "cold");
   public static final PigVariant TEMPERATE = define("temperate", PigVariant.ModelType.NORMAL, "temperate");
   public static final PigVariant WARM = define("warm", PigVariant.ModelType.NORMAL, "warm");

   private PigVariants() {
   }

   @Internal
   public static PigVariant define(String name, PigVariant.ModelType modelType, String texture) {
      ResourceLocation assetId = new ResourceLocation("entity/pig/pig_" + texture);
      ResourceLocation babyAssetId = new ResourceLocation("entity/pig/pig_" + texture + "_baby");
      return REGISTRY.define(name, data -> new StaticPigVariant(data, modelType, assetId, babyAssetId));
   }

   public static VersionedRegistry<PigVariant> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }
}
