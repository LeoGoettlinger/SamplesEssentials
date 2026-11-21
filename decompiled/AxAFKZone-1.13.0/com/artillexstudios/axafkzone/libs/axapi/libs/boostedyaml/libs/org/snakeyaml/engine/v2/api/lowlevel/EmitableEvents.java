package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.api.lowlevel;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.emitter.Emitable;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events.Event;
import java.util.ArrayList;
import java.util.List;

class EmitableEvents implements Emitable {
   private final List<Event> events = new ArrayList<>();

   @Override
   public void emit(Event event) {
      this.events.add(event);
   }

   public List<Event> getEvents() {
      return this.events;
   }
}
