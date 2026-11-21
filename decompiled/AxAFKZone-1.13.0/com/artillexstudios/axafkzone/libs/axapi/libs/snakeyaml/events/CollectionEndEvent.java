package com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.events;

import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.error.Mark;

public abstract class CollectionEndEvent extends Event {
   public CollectionEndEvent(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }
}
