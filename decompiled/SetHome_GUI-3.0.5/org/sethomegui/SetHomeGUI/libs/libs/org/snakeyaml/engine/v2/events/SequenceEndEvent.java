package org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.events;

import java.util.Optional;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.exceptions.Mark;

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
