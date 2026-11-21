package com.earth2me.essentials.libs.kyori.adventure.text.serializer.gson;

import com.earth2me.essentials.libs.kyori.adventure.text.event.ClickEvent;
import com.google.gson.TypeAdapter;

final class ClickEventActionSerializer {
   static final TypeAdapter<ClickEvent.Action> INSTANCE = IndexedSerializer.lenient("click action", ClickEvent.Action.NAMES);

   private ClickEventActionSerializer() {
   }
}
