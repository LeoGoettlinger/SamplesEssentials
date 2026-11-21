package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;
import java.util.Optional;

public final class StreamEndEvent extends Event {
   public StreamEndEvent(Optional<Mark> startMark, Optional<Mark> endMark) {
      super(startMark, endMark);
   }

   public StreamEndEvent() {
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.StreamEnd;
   }

   @Override
   public String toString() {
      return "-STR";
   }
}
