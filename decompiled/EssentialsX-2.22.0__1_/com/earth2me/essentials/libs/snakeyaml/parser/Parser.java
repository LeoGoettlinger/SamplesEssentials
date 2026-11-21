package com.earth2me.essentials.libs.snakeyaml.parser;

import com.earth2me.essentials.libs.snakeyaml.events.Event;

public interface Parser {
   boolean checkEvent(Event.ID var1);

   Event peekEvent();

   Event getEvent();
}
