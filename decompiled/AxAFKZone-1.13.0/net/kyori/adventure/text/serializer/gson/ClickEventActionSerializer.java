package net.kyori.adventure.text.serializer.gson;

import com.google.gson.TypeAdapter;
import net.kyori.adventure.text.event.ClickEvent.Action;

final class ClickEventActionSerializer {
   static final TypeAdapter<Action> INSTANCE = IndexedSerializer.lenient("click action", Action.NAMES);

   private ClickEventActionSerializer() {
   }
}
