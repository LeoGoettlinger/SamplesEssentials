package org.evenmorefish.fish.libs.boostedyaml.dvs.versioning;

import org.evenmorefish.fish.libs.boostedyaml.dvs.Pattern;
import org.evenmorefish.fish.libs.boostedyaml.dvs.segment.Segment;
import org.jetbrains.annotations.NotNull;

public class BasicVersioning extends AutomaticVersioning {
   public static final Pattern PATTERN = new Pattern(Segment.range(1, Integer.MAX_VALUE));

   public BasicVersioning(@NotNull String route) {
      super(PATTERN, route);
   }
}
