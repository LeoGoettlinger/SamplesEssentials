package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.frog;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

public final class FrogVariants {
   private static final VersionedRegistry<FrogVariant> REGISTRY = new VersionedRegistry<>("frog_variant");
   public static final FrogVariant COLD = define("cold", "cold");
   public static final FrogVariant TEMPERATE = define("temperate", "temperate");
   public static final FrogVariant WARM = define("warm", "warm");

   private FrogVariants() {
   }

   @ApiStatus.Internal
   public static FrogVariant define(String name, String texture) {
      ResourceLocation assetId = new ResourceLocation("entity/frog/frog_" + texture);
      return REGISTRY.define(name, data -> new StaticFrogVariant(data, assetId));
   }

   public static VersionedRegistry<FrogVariant> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }
}
