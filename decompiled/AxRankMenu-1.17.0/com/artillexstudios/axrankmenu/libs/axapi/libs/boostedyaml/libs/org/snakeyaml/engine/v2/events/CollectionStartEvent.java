package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.Anchor;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.FlowStyle;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;
import java.util.Objects;
import java.util.Optional;

public abstract class CollectionStartEvent extends NodeEvent {
   private final Optional<String> tag;
   private final boolean implicit;
   private final FlowStyle flowStyle;

   public CollectionStartEvent(
      Optional<Anchor> anchor, Optional<String> tag, boolean implicit, FlowStyle flowStyle, Optional<Mark> startMark, Optional<Mark> endMark
   ) {
      super(anchor, startMark, endMark);
      Objects.requireNonNull(tag);
      this.tag = tag;
      this.implicit = implicit;
      Objects.requireNonNull(flowStyle);
      this.flowStyle = flowStyle;
   }

   public Optional<String> getTag() {
      return this.tag;
   }

   public boolean isImplicit() {
      return this.implicit;
   }

   public FlowStyle getFlowStyle() {
      return this.flowStyle;
   }

   public boolean isFlow() {
      return FlowStyle.FLOW == this.flowStyle;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      this.getAnchor().ifPresent(a -> builder.append(" &" + a));
      if (!this.implicit) {
         this.getTag().ifPresent(theTag -> builder.append(" <" + theTag + ">"));
      }

      return builder.toString();
   }
}
