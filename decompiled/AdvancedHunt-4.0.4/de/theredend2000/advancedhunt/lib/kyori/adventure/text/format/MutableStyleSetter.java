package de.theredend2000.advancedhunt.lib.kyori.adventure.text.format;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface MutableStyleSetter<T extends MutableStyleSetter<?>> extends StyleSetter<T> {
   @Contract("_ -> this")
   default T decorate(final TextDecoration... decorations) {
      for (TextDecoration decoration : decorations) {
         this.decorate(decoration);
      }

      return (T)this;
   }

   @Contract("_ -> this")
   default T decorations(final Map<TextDecoration, TextDecoration.State> decorations) {
      Objects.requireNonNull(decorations, "decorations");

      for (Entry<TextDecoration, TextDecoration.State> entry : decorations.entrySet()) {
         this.decoration(entry.getKey(), entry.getValue());
      }

      return (T)this;
   }

   @Contract("_, _ -> this")
   default T decorations(final Set<TextDecoration> decorations, final boolean flag) {
      TextDecoration.State state = TextDecoration.State.byBoolean(flag);
      decorations.forEach(decoration -> this.decoration(decoration, state));
      return (T)this;
   }
}
