package com.github.retrooper.packetevents.protocol.world.clock;

import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import org.jetbrains.annotations.ApiStatus.Internal;
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

   @Internal
   public static WorldClock define(String name) {
      return REGISTRY.define(name, StaticWorldClock::new);
   }

   static {
      REGISTRY.unloadMappings();
   }
}
