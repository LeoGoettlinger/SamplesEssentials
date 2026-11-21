package org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events;

import java.util.Optional;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;

public abstract class CollectionEndEvent extends Event {
   public CollectionEndEvent(Optional<Mark> startMark, Optional<Mark> endMark) {
      super(startMark, endMark);
   }

   public CollectionEndEvent() {
   }
}
