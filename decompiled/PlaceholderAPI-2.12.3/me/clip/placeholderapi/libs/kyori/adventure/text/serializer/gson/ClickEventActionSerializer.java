package me.clip.placeholderapi.libs.kyori.adventure.text.serializer.gson;

import com.google.gson.TypeAdapter;
import me.clip.placeholderapi.libs.kyori.adventure.text.event.ClickEvent;

final class ClickEventActionSerializer {
   static final TypeAdapter<ClickEvent.Action> INSTANCE = IndexedSerializer.lenient("click action", ClickEvent.Action.NAMES);

   private ClickEventActionSerializer() {
   }
}
