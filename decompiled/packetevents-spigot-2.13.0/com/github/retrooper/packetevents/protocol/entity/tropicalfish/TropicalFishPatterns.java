package com.github.retrooper.packetevents.protocol.entity.tropicalfish;

import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import java.util.Collection;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class TropicalFishPatterns {
   private static final VersionedRegistry<TropicalFishPattern> REGISTRY = new VersionedRegistry<>("tropical_fish_pattern");
   public static final TropicalFishPattern KOB = define("kob", TropicalFishPattern.Base.SMALL);
   public static final TropicalFishPattern SUNSTREAK = define("sunstreak", TropicalFishPattern.Base.SMALL);
   public static final TropicalFishPattern SNOOPER = define("snooper", TropicalFishPattern.Base.SMALL);
   public static final TropicalFishPattern DASHER = define("dasher", TropicalFishPattern.Base.SMALL);
   public static final TropicalFishPattern BRINELY = define("brinely", TropicalFishPattern.Base.SMALL);
   public static final TropicalFishPattern SPOTTY = define("spotty", TropicalFishPattern.Base.SMALL);
   public static final TropicalFishPattern FLOPPER = define("flopper", TropicalFishPattern.Base.LARGE);
   public static final TropicalFishPattern STRIPEY = define("stripey", TropicalFishPattern.Base.LARGE);
   public static final TropicalFishPattern GLITTER = define("glitter", TropicalFishPattern.Base.LARGE);
   public static final TropicalFishPattern BLOCKFISH = define("blockfish", TropicalFishPattern.Base.LARGE);
   public static final TropicalFishPattern BETTY = define("betty", TropicalFishPattern.Base.LARGE);
   public static final TropicalFishPattern CLAYFISH = define("clayfish", TropicalFishPattern.Base.LARGE);

   private TropicalFishPatterns() {
   }

   @Internal
   public static TropicalFishPattern define(String name, TropicalFishPattern.Base base) {
      return REGISTRY.define(name, typesBuilderData -> new StaticTropicalFishPattern(typesBuilderData, base));
   }

   public static VersionedRegistry<TropicalFishPattern> getRegistry() {
      return REGISTRY;
   }

   public static Collection<TropicalFishPattern> values() {
      return REGISTRY.getEntries();
   }

   static {
      REGISTRY.unloadMappings();
   }
}
