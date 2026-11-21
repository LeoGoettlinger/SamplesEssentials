package de.theredend2000.advancedhunt.lib.kyori.adventure.text.event;

import java.util.function.UnaryOperator;
import org.jspecify.annotations.Nullable;

public interface HoverEventSource<V> {
   @Nullable
   static <V> HoverEvent<V> unbox(@Nullable final HoverEventSource<V> source) {
      return source != null ? source.asHoverEvent() : null;
   }

   default HoverEvent<V> asHoverEvent() {
      return this.asHoverEvent(UnaryOperator.identity());
   }

   HoverEvent<V> asHoverEvent(final UnaryOperator<V> op);
}
