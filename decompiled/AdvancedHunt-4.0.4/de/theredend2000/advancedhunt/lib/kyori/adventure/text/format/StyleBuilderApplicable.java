package de.theredend2000.advancedhunt.lib.kyori.adventure.text.format;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.ComponentBuilder;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.ComponentBuilderApplicable;
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
