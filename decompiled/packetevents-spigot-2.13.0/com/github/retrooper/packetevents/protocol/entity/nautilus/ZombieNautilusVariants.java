package com.github.retrooper.packetevents.protocol.entity.nautilus;

import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ZombieNautilusVariants {
   private static final VersionedRegistry<ZombieNautilusVariant> REGISTRY = new VersionedRegistry<>("zombie_nautilus_variant");
   public static final ZombieNautilusVariant TEMPERATE = define("temperate", ZombieNautilusVariant.ModelType.NORMAL, "zombie_nautilus");
   public static final ZombieNautilusVariant WARM = define("warm", ZombieNautilusVariant.ModelType.WARM, "zombie_nautilus_coral");

   private ZombieNautilusVariants() {
   }

   @Internal
   public static ZombieNautilusVariant define(String name, ZombieNautilusVariant.ModelType modelType, String texture) {
      ResourceLocation assetId = new ResourceLocation("entity/nautilus/" + texture);
      return REGISTRY.define(name, data -> new StaticZombieNautilusVariant(data, modelType, assetId));
   }

   public static VersionedRegistry<ZombieNautilusVariant> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }
}
