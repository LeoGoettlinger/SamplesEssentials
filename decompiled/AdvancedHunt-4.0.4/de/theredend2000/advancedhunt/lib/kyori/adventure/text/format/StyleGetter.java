package de.theredend2000.advancedhunt.lib.kyori.adventure.text.format;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.ClickEvent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.HoverEvent;
import java.util.EnumMap;
import java.util.Map;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jspecify.annotations.Nullable;

@NonExtendable
public interface StyleGetter {
   @Nullable
   Key font();

   @Nullable
   TextColor color();

   @Nullable
   ShadowColor shadowColor();

   default boolean hasDecoration(final TextDecoration decoration) {
      return this.decoration(decoration) == TextDecoration.State.TRUE;
   }

   TextDecoration.State decoration(final TextDecoration decoration);

   @Unmodifiable
   default Map<TextDecoration, TextDecoration.State> decorations() {
      Map<TextDecoration, TextDecoration.State> decorations = new EnumMap<>(TextDecoration.class);
      int i = 0;

      for (int length = DecorationMap.DECORATIONS.length; i < length; i++) {
         TextDecoration decoration = DecorationMap.DECORATIONS[i];
         TextDecoration.State value = this.decoration(decoration);
         decorations.put(decoration, value);
      }

      return decorations;
   }

   @Nullable
   ClickEvent<?> clickEvent();

   @Nullable
   HoverEvent<?> hoverEvent();

   @Nullable
   String insertion();
}
