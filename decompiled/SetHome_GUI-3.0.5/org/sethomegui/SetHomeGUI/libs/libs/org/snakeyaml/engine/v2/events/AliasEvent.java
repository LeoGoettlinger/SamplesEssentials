package org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.events;

import java.util.Optional;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.common.Anchor;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.exceptions.Mark;

public final class AliasEvent extends NodeEvent {
   private final Anchor alias;

   public AliasEvent(Optional<Anchor> anchor, Optional<Mark> startMark, Optional<Mark> endMark) {
      super(anchor, startMark, endMark);
      this.alias = anchor.orElseThrow(() -> new NullPointerException("Anchor is required in AliasEvent"));
   }

   public AliasEvent(Optional<Anchor> anchor) {
      this(anchor, Optional.empty(), Optional.empty());
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.Alias;
   }

   @Override
   public String toString() {
      return "=ALI *" + this.alias;
   }

   public Anchor getAlias() {
      return this.alias;
   }
}
