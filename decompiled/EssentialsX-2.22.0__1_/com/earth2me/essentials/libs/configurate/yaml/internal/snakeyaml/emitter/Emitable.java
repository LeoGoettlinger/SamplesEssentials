package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.emitter;

import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.events.Event;
import java.io.IOException;

public interface Emitable {
   void emit(Event var1) throws IOException;
}
