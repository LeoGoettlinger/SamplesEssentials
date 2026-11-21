package com.earth2me.essentials.libs.snakeyaml.events;

import com.earth2me.essentials.libs.snakeyaml.error.Mark;

public abstract class CollectionEndEvent extends Event {
   public CollectionEndEvent(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }
}
