package com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.parser;

import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.events.Event;

public interface Parser {
   boolean checkEvent(Event.ID var1);

   Event peekEvent();

   Event getEvent();
}
