package com.github.retrooper.packetevents.protocol.entity.chicken;

import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ChickenSoundVariants {
   private static final VersionedRegistry<ChickenSoundVariant> REGISTRY = new VersionedRegistry<>("chicken_sound_variant");
   public static final ChickenSoundVariant CLASSIC = define("classic", "chicken");
   public static final ChickenSoundVariant PICKY = define("picky", "chicken_picky");

   private ChickenSoundVariants() {
   }

   @Internal
   private static ChickenSoundVariant define(String name, String soundId) {
      ChickenSoundSet adultSounds = ChickenSoundSet.getOrThrow("entity." + soundId + ".", Sounds.ENTITY_CHICKEN_STEP);
      ChickenSoundSet babySounds = ChickenSoundSet.getOrThrow("entity.baby_chicken.", Sounds.ENTITY_BABY_CHICKEN_STEP);
      return REGISTRY.define(name, data -> new StaticChickenSoundVariant(data, adultSounds, babySounds));
   }

   public static VersionedRegistry<ChickenSoundVariant> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }
}
