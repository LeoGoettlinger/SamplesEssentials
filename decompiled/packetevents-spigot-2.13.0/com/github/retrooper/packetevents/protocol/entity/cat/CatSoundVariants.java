package com.github.retrooper.packetevents.protocol.entity.cat;

import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class CatSoundVariants {
   private static final VersionedRegistry<CatSoundVariant> REGISTRY = new VersionedRegistry<>("cat_sound_variant");
   public static final CatSoundVariant CLASSIC = define("classic", "cat");
   public static final CatSoundVariant ROYAL = define("royal", "cat_royal");

   private CatSoundVariants() {
   }

   @Internal
   private static CatSoundVariant define(String name, String soundId) {
      CatSoundSet adultSounds = CatSoundSet.getOrThrow("entity." + soundId + ".");
      CatSoundSet babySounds = CatSoundSet.getOrThrow("entity.baby_cat.");
      return REGISTRY.define(name, data -> new StaticCatSoundVariant(data, adultSounds, babySounds));
   }

   public static VersionedRegistry<CatSoundVariant> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }
}
