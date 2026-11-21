package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.Anchor;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.FlowStyle;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;
import java.util.Optional;

public final class SequenceStartEvent extends CollectionStartEvent {
   public SequenceStartEvent(
      Optional<Anchor> anchor, Optional<String> tag, boolean implicit, FlowStyle flowStyle, Optional<Mark> startMark, Optional<Mark> endMark
   ) {
      super(anchor, tag, implicit, flowStyle, startMark, endMark);
   }

   public SequenceStartEvent(Optional<Anchor> anchor, Optional<String> tag, boolean implicit, FlowStyle flowStyle) {
      this(anchor, tag, implicit, flowStyle, Optional.empty(), Optional.empty());
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.SequenceStart;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder("+SEQ");
      if (this.getFlowStyle() == FlowStyle.FLOW) {
         builder.append(" []");
      }

      builder.append(super.toString());
      return builder.toString();
   }
}
