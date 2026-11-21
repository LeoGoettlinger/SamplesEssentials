package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;
import java.util.Optional;

public final class SequenceEndEvent extends CollectionEndEvent {
   public SequenceEndEvent(Optional<Mark> startMark, Optional<Mark> endMark) {
      super(startMark, endMark);
   }

   public SequenceEndEvent() {
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.SequenceEnd;
   }

   @Override
   public String toString() {
      return "-SEQ";
   }
}
