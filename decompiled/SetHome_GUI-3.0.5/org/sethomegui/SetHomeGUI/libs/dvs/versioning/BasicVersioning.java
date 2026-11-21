package org.sethomegui.SetHomeGUI.libs.dvs.versioning;

import org.jetbrains.annotations.NotNull;
import org.sethomegui.SetHomeGUI.libs.dvs.Pattern;
import org.sethomegui.SetHomeGUI.libs.dvs.segment.Segment;

public class BasicVersioning extends AutomaticVersioning {
   public static final Pattern PATTERN = new Pattern(Segment.range(1, Integer.MAX_VALUE));

   public BasicVersioning(@NotNull String route) {
      super(PATTERN, route);
   }
}
