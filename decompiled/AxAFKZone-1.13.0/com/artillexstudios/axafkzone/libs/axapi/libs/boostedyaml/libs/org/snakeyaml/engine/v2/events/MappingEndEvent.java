package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;
import java.util.Optional;

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
