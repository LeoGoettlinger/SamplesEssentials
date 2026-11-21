package com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.dvs.versioning;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.dvs.Pattern;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.dvs.segment.Segment;
import org.jetbrains.annotations.NotNull;

public class BasicVersioning extends AutomaticVersioning {
   public static final Pattern PATTERN = new Pattern(Segment.range(1, Integer.MAX_VALUE));

   public BasicVersioning(@NotNull String route) {
      super(PATTERN, route);
   }
}
