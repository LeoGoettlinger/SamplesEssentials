package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.cow;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class CowSoundVariants {
   private static final VersionedRegistry<CowSoundVariant> REGISTRY = new VersionedRegistry<>("cow_sound_variant");
   public static final CowSoundVariant CLASSIC = define("classic", "cow");
   public static final CowSoundVariant MOODY = define("moody", "cow_moody");

   private CowSoundVariants() {
   }

   @ApiStatus.Internal
   private static CowSoundVariant define(String name, String soundId) {
      CowSoundSet sounds = CowSoundSet.getOrThrow("entity." + soundId + ".");
      return REGISTRY.define(name, data -> new StaticCowSoundVariant(data, sounds));
   }

   public static VersionedRegistry<CowSoundVariant> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }
}
