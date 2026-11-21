package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.clock;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class WorldClocks {
   private static final VersionedRegistry<WorldClock> REGISTRY = new VersionedRegistry<>("world_clock");
   public static final WorldClock OVERWORLD = define("overworld");
   public static final WorldClock THE_END = define("the_end");

   private WorldClocks() {
   }

   public static VersionedRegistry<WorldClock> getRegistry() {
      return REGISTRY;
   }

   @ApiStatus.Internal
   public static WorldClock define(String name) {
      return REGISTRY.define(name, StaticWorldClock::new);
   }

   static {
      REGISTRY.unloadMappings();
   }
}
