package net.kyori.adventure.text.serializer.gson;

import com.google.gson.TypeAdapter;
import net.kyori.adventure.text.event.HoverEvent.Action;

final class HoverEventActionSerializer {
   static final TypeAdapter<Action<?>> INSTANCE = IndexedSerializer.lenient("hover action", Action.NAMES);

   private HoverEventActionSerializer() {
   }
}
