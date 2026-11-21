package org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.api.lowlevel;

import java.util.ArrayList;
import java.util.List;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.emitter.Emitable;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.events.Event;

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
