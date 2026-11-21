package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.parser;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events.Event;
import java.util.Iterator;

public interface Parser extends Iterator<Event> {
   boolean checkEvent(Event.ID var1);

   Event peekEvent();

   Event next();
}
