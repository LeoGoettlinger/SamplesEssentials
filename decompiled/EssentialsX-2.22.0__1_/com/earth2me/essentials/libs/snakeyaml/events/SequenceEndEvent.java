package com.earth2me.essentials.libs.snakeyaml.events;

import com.earth2me.essentials.libs.snakeyaml.error.Mark;

public final class SequenceEndEvent extends CollectionEndEvent {
   public SequenceEndEvent(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.SequenceEnd;
   }
}
