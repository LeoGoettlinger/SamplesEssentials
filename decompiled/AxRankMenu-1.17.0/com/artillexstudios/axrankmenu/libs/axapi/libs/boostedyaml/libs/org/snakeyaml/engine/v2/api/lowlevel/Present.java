package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.api.lowlevel;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.api.DumpSettings;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.emitter.Emitter;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events.Event;
import java.util.Iterator;
import java.util.Objects;

public class Present {
   private final DumpSettings settings;

   public Present(DumpSettings settings) {
      Objects.requireNonNull(settings, "DumpSettings cannot be null");
      this.settings = settings;
   }

   public String emitToString(Iterator<Event> events) {
      Objects.requireNonNull(events, "events cannot be null");
      StreamToStringWriter writer = new StreamToStringWriter();
      Emitter emitter = new Emitter(this.settings, writer);
      events.forEachRemaining(emitter::emit);
      return writer.toString();
   }
}
