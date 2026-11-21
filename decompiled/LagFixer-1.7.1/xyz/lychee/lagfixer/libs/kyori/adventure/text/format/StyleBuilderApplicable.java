package xyz.lychee.lagfixer.libs.kyori.adventure.text.format;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.ComponentBuilder;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.ComponentBuilderApplicable;

@FunctionalInterface
public interface StyleBuilderApplicable extends ComponentBuilderApplicable {
   @Contract(
      mutates = "param"
   )
   void styleApply(@NotNull final Style.Builder style);

   @Override
   default void componentBuilderApply(@NotNull final ComponentBuilder<?, ?> component) {
      component.style(this::styleApply);
   }
}
