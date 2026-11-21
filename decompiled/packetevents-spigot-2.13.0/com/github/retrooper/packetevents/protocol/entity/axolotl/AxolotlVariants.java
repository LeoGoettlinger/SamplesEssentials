package com.github.retrooper.packetevents.protocol.entity.axolotl;

import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class AxolotlVariants {
   private static final VersionedRegistry<AxolotlVariant> REGISTRY = new VersionedRegistry<>("axolotl_variant");
   public static final AxolotlVariant LUCY = define("lucy");
   public static final AxolotlVariant WILD = define("wild");
   public static final AxolotlVariant GOLD = define("gold");
   public static final AxolotlVariant CYAN = define("cyan");
   public static final AxolotlVariant BLUE = define("blue");

   private AxolotlVariants() {
   }

   @Internal
   public static AxolotlVariant define(String name) {
      return REGISTRY.define(name, StaticAxolotlVariant::new);
   }

   public static VersionedRegistry<AxolotlVariant> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }
}
