package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.cow;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class CowVariants {
   private static final VersionedRegistry<CowVariant> REGISTRY = new VersionedRegistry<>("cow_variant");
   public static final CowVariant COLD = define("cold", CowVariant.ModelType.COLD, "cold_cow");
   public static final CowVariant TEMPERATE = define("temperate", CowVariant.ModelType.NORMAL, "temperate_cow");
   public static final CowVariant WARM = define("warm", CowVariant.ModelType.WARM, "warm_cow");

   private CowVariants() {
   }

   @ApiStatus.Internal
   public static CowVariant define(String name, CowVariant.ModelType modelType, String texture) {
      ResourceLocation assetId = new ResourceLocation("entity/cow/cow_" + texture);
      ResourceLocation babyAssetId = new ResourceLocation("entity/cow/cow_" + texture + "_baby");
      return REGISTRY.define(name, data -> new StaticCowVariant(data, modelType, assetId, babyAssetId));
   }

   public static VersionedRegistry<CowVariant> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }
}
