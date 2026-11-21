package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.pig;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.sound.Sounds;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PigSoundVariants {
   private static final VersionedRegistry<PigSoundVariant> REGISTRY = new VersionedRegistry<>("pig_sound_variant");
   public static final PigSoundVariant CLASSIC = define("classic", "pig");
   public static final PigSoundVariant MINI = define("mini", "pig_mini");
   public static final PigSoundVariant BIG = define("big", "pig_big");

   private PigSoundVariants() {
   }

   @ApiStatus.Internal
   private static PigSoundVariant define(String name, String soundId) {
      PigSoundSet adultSounds = PigSoundSet.getOrThrow("entity." + soundId + ".", Sounds.ENTITY_PIG_STEP);
      PigSoundSet babySounds = PigSoundSet.getOrThrow("entity.baby_pig.", Sounds.ENTITY_BABY_PIG_STEP);
      return REGISTRY.define(name, data -> new StaticPigSoundVariant(data, adultSounds, babySounds));
   }

   public static VersionedRegistry<PigSoundVariant> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }
}
