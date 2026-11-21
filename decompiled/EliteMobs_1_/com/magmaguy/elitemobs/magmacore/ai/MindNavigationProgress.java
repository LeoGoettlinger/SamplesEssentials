package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Objects;

public record MindNavigationProgress(long gameTick, MindPosition position) {
   public MindNavigationProgress(long gameTick, MindPosition position) {
      Objects.requireNonNull(position, "position");
      this.gameTick = gameTick;
      this.position = position;
   }
}
