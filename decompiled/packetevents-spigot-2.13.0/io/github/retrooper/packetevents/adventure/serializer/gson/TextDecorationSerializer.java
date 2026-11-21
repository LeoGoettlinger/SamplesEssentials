package io.github.retrooper.packetevents.adventure.serializer.gson;

import com.google.gson.TypeAdapter;
import net.kyori.adventure.text.format.TextDecoration;

final class TextDecorationSerializer {
   static final TypeAdapter<TextDecoration> INSTANCE = IndexedSerializer.strict("text decoration", TextDecoration.NAMES);

   private TextDecorationSerializer() {
   }
}
