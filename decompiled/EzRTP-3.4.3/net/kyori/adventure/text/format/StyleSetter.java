package net.kyori.adventure.text.format;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.util.ARGBLike;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jspecify.annotations.Nullable;

@NonExtendable
public interface StyleSetter<T extends StyleSetter<?>> {
   T font(@Nullable final Key font);

   T color(@Nullable final TextColor color);

   T colorIfAbsent(@Nullable final TextColor color);

   T shadowColor(@Nullable final ARGBLike argb);

   T shadowColorIfAbsent(@Nullable final ARGBLike argb);

   default T decorate(final TextDecoration decoration) {
      return this.decoration(decoration, TextDecoration.State.TRUE);
   }

   default T decorate(final TextDecoration... decorations) {
      Map<TextDecoration, TextDecoration.State> map = new EnumMap<>(TextDecoration.class);

      for (TextDecoration decoration : decorations) {
         map.put(decoration, TextDecoration.State.TRUE);
      }

      return this.decorations(map);
   }

   default T decoration(final TextDecoration decoration, final boolean flag) {
      return this.decoration(decoration, TextDecoration.State.byBoolean(flag));
   }

   T decoration(final TextDecoration decoration, final TextDecoration.State state);

   T decorationIfAbsent(final TextDecoration decoration, final TextDecoration.State state);

   T decorations(final Map<TextDecoration, TextDecoration.State> decorations);

   default T decorations(final Set<TextDecoration> decorations, final boolean flag) {
      return this.decorations(decorations.stream().collect(Collectors.toMap(Function.identity(), decoration -> TextDecoration.State.byBoolean(flag))));
   }

   T clickEvent(@Nullable final ClickEvent<?> event);

   T hoverEvent(@Nullable final HoverEventSource<?> source);

   T insertion(@Nullable final String insertion);
}
