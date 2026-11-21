package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Objects;
import java.util.UUID;

public record MindActionContext(UUID logicalOwner, MobBody body, long gameTick, long generation) {
   public MindActionContext(UUID logicalOwner, MobBody body, long gameTick, long generation) {
      Objects.requireNonNull(logicalOwner, "logicalOwner");
      Objects.requireNonNull(body, "body");
      if (generation < 1L) {
         throw new IllegalArgumentException("generation must be positive");
      } else {
         this.logicalOwner = logicalOwner;
         this.body = body;
         this.gameTick = gameTick;
         this.generation = generation;
      }
   }
}
