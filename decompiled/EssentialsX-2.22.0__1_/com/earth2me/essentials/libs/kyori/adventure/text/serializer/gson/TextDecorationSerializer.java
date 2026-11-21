package com.earth2me.essentials.libs.kyori.adventure.text.serializer.gson;

import com.earth2me.essentials.libs.kyori.adventure.text.format.TextDecoration;
import com.google.gson.TypeAdapter;

final class TextDecorationSerializer {
   static final TypeAdapter<TextDecoration> INSTANCE = IndexedSerializer.strict("text decoration", TextDecoration.NAMES);

   private TextDecorationSerializer() {
   }
}
