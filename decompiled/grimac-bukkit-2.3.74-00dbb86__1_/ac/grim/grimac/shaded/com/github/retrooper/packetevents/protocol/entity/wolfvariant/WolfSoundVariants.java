package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.wolfvariant;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.sound.Sounds;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

public final class WolfSoundVariants {
   private static final VersionedRegistry<WolfSoundVariant> REGISTRY = new VersionedRegistry<>("wolf_sound_variant");
   public static final WolfSoundVariant CLASSIC = define("classic", "wolf");
   public static final WolfSoundVariant PUGLIN = define("puglin", "wolf_puglin");
   public static final WolfSoundVariant SAD = define("sad", "wolf_sad");
   public static final WolfSoundVariant ANGRY = define("angry", "wolf_angry");
   public static final WolfSoundVariant GRUMPY = define("grumpy", "wolf_grumpy");
   public static final WolfSoundVariant BIG = define("big", "wolf_big");
   public static final WolfSoundVariant CUTE = define("cute", "wolf_cute");

   private WolfSoundVariants() {
   }

   public static VersionedRegistry<WolfSoundVariant> getRegistry() {
      return REGISTRY;
   }

   @ApiStatus.Internal
   public static WolfSoundVariant define(String name, String soundId) {
      WolfSoundSet adultSounds = WolfSoundSet.getOrThrow("entity." + soundId + ".", Sounds.ENTITY_WOLF_STEP);
      WolfSoundSet babySounds = WolfSoundSet.getOrThrow("entity.baby_wolf.", Sounds.ENTITY_BABY_WOLF_STEP);
      return define(name, adultSounds, babySounds);
   }

   @ApiStatus.Internal
   public static WolfSoundVariant define(String name, WolfSoundSet adultSounds, WolfSoundSet babySounds) {
      return REGISTRY.define(name, data -> new StaticWolfSoundVariant(data, adultSounds, babySounds));
   }

   static {
      REGISTRY.unloadMappings();
   }
}
