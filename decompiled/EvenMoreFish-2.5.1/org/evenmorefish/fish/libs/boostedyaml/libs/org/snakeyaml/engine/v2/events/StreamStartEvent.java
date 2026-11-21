package org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events;

import java.util.Optional;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;

public final class StreamStartEvent extends Event {
   public StreamStartEvent(Optional<Mark> startMark, Optional<Mark> endMark) {
      super(startMark, endMark);
   }

   public StreamStartEvent() {
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.StreamStart;
   }

   @Override
   public String toString() {
      return "+STR";
   }
}
