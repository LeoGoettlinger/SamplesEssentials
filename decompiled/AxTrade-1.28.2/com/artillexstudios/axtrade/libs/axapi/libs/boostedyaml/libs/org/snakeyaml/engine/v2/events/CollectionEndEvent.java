package com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;
import java.util.Optional;

public abstract class CollectionEndEvent extends Event {
   public CollectionEndEvent(Optional<Mark> startMark, Optional<Mark> endMark) {
      super(startMark, endMark);
   }

   public CollectionEndEvent() {
   }
}
