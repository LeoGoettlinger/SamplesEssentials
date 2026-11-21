package com.artillexstudios.axrankmenu.libs.axapi.libs.snakeyaml.events;

import com.artillexstudios.axrankmenu.libs.axapi.libs.snakeyaml.error.Mark;

public abstract class CollectionEndEvent extends Event {
   public CollectionEndEvent(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }
}
