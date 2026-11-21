package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.events;

import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.error.Mark;

public final class StreamEndEvent extends Event {
   public StreamEndEvent(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.StreamEnd;
   }
}
