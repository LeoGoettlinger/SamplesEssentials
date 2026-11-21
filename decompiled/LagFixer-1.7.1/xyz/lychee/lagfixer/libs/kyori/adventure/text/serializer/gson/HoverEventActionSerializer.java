package xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.gson;

import com.google.gson.TypeAdapter;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.event.HoverEvent;

final class HoverEventActionSerializer {
   static final TypeAdapter<HoverEvent.Action<?>> INSTANCE = IndexedSerializer.lenient("hover action", HoverEvent.Action.NAMES);

   private HoverEventActionSerializer() {
   }
}
