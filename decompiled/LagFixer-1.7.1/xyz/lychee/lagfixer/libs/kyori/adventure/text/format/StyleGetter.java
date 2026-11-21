package xyz.lychee.lagfixer.libs.kyori.adventure.text.format;

import java.util.EnumMap;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import xyz.lychee.lagfixer.libs.kyori.adventure.key.Key;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.event.ClickEvent;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.event.HoverEvent;

@ApiStatus.NonExtendable
public interface StyleGetter {
   @Nullable
   Key font();

   @Nullable
   TextColor color();

   @Nullable
   ShadowColor shadowColor();

   default boolean hasDecoration(@NotNull final TextDecoration decoration) {
      return this.decoration(decoration) == TextDecoration.State.TRUE;
   }

   @NotNull
   TextDecoration.State decoration(@NotNull final TextDecoration decoration);

   @NotNull
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
   ClickEvent clickEvent();

   @Nullable
   HoverEvent<?> hoverEvent();

   @Nullable
   String insertion();
}
