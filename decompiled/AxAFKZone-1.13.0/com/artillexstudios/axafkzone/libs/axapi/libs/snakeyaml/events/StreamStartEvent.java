package com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.events;

import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.error.Mark;

public final class StreamStartEvent extends Event {
   public StreamStartEvent(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.StreamStart;
   }
}
