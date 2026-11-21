package ac.grim.grimac.shaded.kyori.adventure.text.serializer.gson;

import ac.grim.grimac.shaded.kyori.adventure.text.format.TextDecoration;
import com.google.gson.TypeAdapter;

final class TextDecorationSerializer {
   static final TypeAdapter<TextDecoration> INSTANCE = IndexedSerializer.strict("text decoration", TextDecoration.NAMES);

   private TextDecorationSerializer() {
   }
}
