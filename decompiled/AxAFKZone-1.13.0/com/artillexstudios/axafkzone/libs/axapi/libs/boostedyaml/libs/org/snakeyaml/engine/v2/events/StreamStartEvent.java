package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;
import java.util.Optional;

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
