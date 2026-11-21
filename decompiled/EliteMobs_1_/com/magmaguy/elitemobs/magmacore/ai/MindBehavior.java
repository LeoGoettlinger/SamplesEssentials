package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Set;

public interface MindBehavior {
   String identifier();

   default int priority() {
      return 0;
   }

   default Set<MindControl> controls() {
      return Set.of();
   }

   boolean canStart(MindContext var1);

   default boolean canContinue(MindContext context) {
      return this.canStart(context);
   }

   default void start(MindContext context) {
   }

   void tick(MindContext var1);

   default void stop(MindContext context, MindStopReason reason) {
   }
}
