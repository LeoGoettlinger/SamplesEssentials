package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.chicken;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

public final class ChickenVariants {
   private static final VersionedRegistry<ChickenVariant> REGISTRY = new VersionedRegistry<>("chicken_variant");
   public static final ChickenVariant COLD = define("cold", ChickenVariant.ModelType.COLD, "cold");
   public static final ChickenVariant TEMPERATE = define("temperate", ChickenVariant.ModelType.NORMAL, "temperate");
   public static final ChickenVariant WARM = define("warm", ChickenVariant.ModelType.NORMAL, "warm");

   private ChickenVariants() {
   }

   @ApiStatus.Internal
   public static ChickenVariant define(String name, ChickenVariant.ModelType modelType, String texture) {
      ResourceLocation assetId = new ResourceLocation("entity/chicken/chicken_" + texture);
      ResourceLocation babyAssetId = new ResourceLocation("entity/chicken/chicken_" + texture + "_baby");
      return REGISTRY.define(name, data -> new StaticChickenVariant(data, modelType, assetId, babyAssetId));
   }

   public static VersionedRegistry<ChickenVariant> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }
}
