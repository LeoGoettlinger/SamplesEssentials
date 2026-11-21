package com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.events;

import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.error.Mark;

public final class SequenceEndEvent extends CollectionEndEvent {
   public SequenceEndEvent(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.SequenceEnd;
   }
}
