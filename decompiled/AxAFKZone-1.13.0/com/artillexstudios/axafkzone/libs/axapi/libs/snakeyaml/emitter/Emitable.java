package com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.emitter;

import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.events.Event;
import java.io.IOException;

public interface Emitable {
   void emit(Event var1) throws IOException;
}
