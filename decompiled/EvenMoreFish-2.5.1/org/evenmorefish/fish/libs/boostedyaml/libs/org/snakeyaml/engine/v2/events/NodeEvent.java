package org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events;

import java.util.Objects;
import java.util.Optional;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.Anchor;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;

public abstract class NodeEvent extends Event {
   private final Optional<Anchor> anchor;

   public NodeEvent(Optional<Anchor> anchor, Optional<Mark> startMark, Optional<Mark> endMark) {
      super(startMark, endMark);
      Objects.requireNonNull(anchor);
      this.anchor = anchor;
   }

   public Optional<Anchor> getAnchor() {
      return this.anchor;
   }
}
