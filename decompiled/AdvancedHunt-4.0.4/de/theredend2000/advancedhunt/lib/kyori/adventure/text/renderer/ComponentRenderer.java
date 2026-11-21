package de.theredend2000.advancedhunt.lib.kyori.adventure.text.renderer;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import java.util.function.Function;

public interface ComponentRenderer<C> {
   Component render(final Component component, final C context);

   default <T> ComponentRenderer<T> mapContext(final Function<T, C> transformer) {
      return (component, ctx) -> this.render(component, transformer.apply(ctx));
   }
}
