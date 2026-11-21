package com.earth2me.essentials.libs.snakeyaml.events;

import com.earth2me.essentials.libs.snakeyaml.DumperOptions;
import com.earth2me.essentials.libs.snakeyaml.error.Mark;

public final class MappingStartEvent extends CollectionStartEvent {
   public MappingStartEvent(String anchor, String tag, boolean implicit, Mark startMark, Mark endMark, DumperOptions.FlowStyle flowStyle) {
      super(anchor, tag, implicit, startMark, endMark, flowStyle);
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.MappingStart;
   }
}
