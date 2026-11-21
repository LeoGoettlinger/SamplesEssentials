package net.kyori.adventure.text.format;

import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.ComponentBuilderApplicable;
import org.jetbrains.annotations.Contract;

@FunctionalInterface
public interface StyleBuilderApplicable extends ComponentBuilderApplicable {
   @Contract(
      mutates = "param"
   )
   void styleApply(final Style.Builder style);

   @Override
   default void componentBuilderApply(final ComponentBuilder<?, ?> component) {
      component.style(this::styleApply);
   }
}
