package xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.gson;

import com.google.gson.TypeAdapter;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.format.TextDecoration;

final class TextDecorationSerializer {
   static final TypeAdapter<TextDecoration> INSTANCE = IndexedSerializer.strict("text decoration", TextDecoration.NAMES);

   private TextDecorationSerializer() {
   }
}
