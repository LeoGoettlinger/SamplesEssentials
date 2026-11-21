package com.sk89q.worldedit.util.formatting.text.serializer.legacy;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.Style;
import com.sk89q.worldedit.util.formatting.text.serializer.ComponentSerializer;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface LegacyComponentSerializer extends ComponentSerializer<Component, TextComponent, String> {
   @Deprecated
   LegacyComponentSerializer INSTANCE = LegacyComponentSerializerImpl.INSTANCE;
   char CHARACTER = '§';

   @NonNull
   static LegacyComponentSerializer legacy() {
      return LegacyComponentSerializerImpl.INSTANCE;
   }

   @NonNull
   static LegacyComponentSerializer legacyLinking() {
      return LinkingLegacyComponentSerializer.NO_STYLE;
   }

   @NonNull
   static LegacyComponentSerializer legacyLinking(@NonNull final Style style) {
      return new LinkingLegacyComponentSerializer(Objects.requireNonNull(style));
   }

   @NonNull
   default TextComponent deserialize(@NonNull final String input) {
      return this.deserialize(input, '§');
   }

   @NonNull
   TextComponent deserialize(@NonNull final String input, final char character);

   @NonNull
   default String serialize(@NonNull final Component component) {
      return this.serialize(component, '§');
   }

   @NonNull
   String serialize(@NonNull final Component component, final char character);
}
