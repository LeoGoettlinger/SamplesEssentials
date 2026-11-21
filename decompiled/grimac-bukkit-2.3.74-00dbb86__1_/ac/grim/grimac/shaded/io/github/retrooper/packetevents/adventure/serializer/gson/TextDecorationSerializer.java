package ac.grim.grimac.shaded.io.github.retrooper.packetevents.adventure.serializer.gson;

import ac.grim.grimac.shaded.kyori.adventure.text.format.TextDecoration;
import com.google.gson.TypeAdapter;

final class TextDecorationSerializer {
   static final TypeAdapter<TextDecoration> INSTANCE = IndexedSerializer.strict("text decoration", TextDecoration.NAMES);

   private TextDecorationSerializer() {
   }
}
