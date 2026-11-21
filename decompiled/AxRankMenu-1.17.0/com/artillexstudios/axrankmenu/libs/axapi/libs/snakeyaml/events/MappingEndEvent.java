package com.artillexstudios.axrankmenu.libs.axapi.libs.snakeyaml.events;

import com.artillexstudios.axrankmenu.libs.axapi.libs.snakeyaml.error.Mark;

public final class MappingEndEvent extends CollectionEndEvent {
   public MappingEndEvent(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.MappingEnd;
   }
}
