package com.github.retrooper.packetevents.protocol.entity.salmon;

import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class SalmonVariants {
   private static final VersionedRegistry<SalmonVariant> REGISTRY = new VersionedRegistry<>("salmon_variant");
   public static final SalmonVariant SMALL = define("small");
   public static final SalmonVariant MEDIUM = define("medium");
   public static final SalmonVariant LARGE = define("large");

   private SalmonVariants() {
   }

   @Internal
   public static SalmonVariant define(String name) {
      return REGISTRY.define(name, StaticSalmonVariant::new);
   }

   public static VersionedRegistry<SalmonVariant> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }
}
