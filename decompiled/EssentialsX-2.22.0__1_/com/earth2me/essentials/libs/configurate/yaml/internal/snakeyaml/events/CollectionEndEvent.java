package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.events;

import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.error.Mark;

public abstract class CollectionEndEvent extends Event {
   public CollectionEndEvent(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }
}
