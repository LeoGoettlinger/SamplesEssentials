package org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.events;

import java.util.Optional;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.exceptions.Mark;

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
