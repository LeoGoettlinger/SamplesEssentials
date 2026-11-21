package net.kyori.adventure.text.renderer;

import java.util.function.Function;
import net.kyori.adventure.text.Component;

public interface ComponentRenderer<C> {
   Component render(final Component component, final C context);

   default <T> ComponentRenderer<T> mapContext(final Function<T, C> transformer) {
      return (component, ctx) -> this.render(component, transformer.apply(ctx));
   }
}
