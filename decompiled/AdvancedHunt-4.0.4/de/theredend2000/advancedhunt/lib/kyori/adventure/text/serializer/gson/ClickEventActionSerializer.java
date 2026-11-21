package de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.gson;

import com.google.gson.TypeAdapter;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.ClickEvent;

final class ClickEventActionSerializer {
   static final TypeAdapter<ClickEvent.Action> INSTANCE = IndexedSerializer.lenient("click action", ClickEvent.Action.NAMES);

   private ClickEventActionSerializer() {
   }
}
