package org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.FlowStyle;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;

public abstract class CollectionNode<T> extends Node {
   private FlowStyle flowStyle;

   public CollectionNode(Tag tag, FlowStyle flowStyle, Optional<Mark> startMark, Optional<Mark> endMark) {
      super(tag, startMark, endMark);
      this.setFlowStyle(flowStyle);
   }

   public abstract List<T> getValue();

   public FlowStyle getFlowStyle() {
      return this.flowStyle;
   }

   public void setFlowStyle(FlowStyle flowStyle) {
      Objects.requireNonNull(flowStyle, "Flow style must be provided.");
      this.flowStyle = flowStyle;
   }

   public void setEndMark(Optional<Mark> endMark) {
      this.endMark = endMark;
   }
}
