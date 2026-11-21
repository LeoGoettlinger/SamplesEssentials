package org.snakeyaml.engine.v2.events;

import java.util.Optional;
import org.snakeyaml.engine.v2.exceptions.Mark;

public final class MappingEndEvent extends CollectionEndEvent {
   public MappingEndEvent(Optional<Mark> startMark, Optional<Mark> endMark) {
      super(startMark, endMark);
   }

   public MappingEndEvent() {
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.MappingEnd;
   }

   @Override
   public String toString() {
      return "-MAP";
   }
}
