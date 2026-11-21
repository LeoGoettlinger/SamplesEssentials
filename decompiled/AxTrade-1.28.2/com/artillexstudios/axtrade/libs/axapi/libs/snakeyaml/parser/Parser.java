package com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.parser;

import com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.events.Event;

public interface Parser {
   boolean checkEvent(Event.ID var1);

   Event peekEvent();

   Event getEvent();
}
