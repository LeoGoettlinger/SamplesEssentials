package org.snakeyaml.engine.v2.parser;

import java.util.Iterator;
import org.snakeyaml.engine.v2.events.Event;

public interface Parser extends Iterator<Event> {
   boolean checkEvent(Event.ID var1);

   Event peekEvent();

   Event next();
}
