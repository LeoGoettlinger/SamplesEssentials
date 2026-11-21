package com.earth2me.essentials.libs.kyori.adventure.text.serializer.gson;

import com.earth2me.essentials.libs.kyori.adventure.text.event.HoverEvent;
import com.google.gson.TypeAdapter;

final class HoverEventActionSerializer {
   static final TypeAdapter<HoverEvent.Action<?>> INSTANCE = IndexedSerializer.lenient("hover action", HoverEvent.Action.NAMES);

   private HoverEventActionSerializer() {
   }
}
