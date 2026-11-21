package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.parser;

import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.events.Event;

public interface Parser {
   boolean checkEvent(Event.ID var1);

   Event peekEvent();

   Event getEvent();
}
