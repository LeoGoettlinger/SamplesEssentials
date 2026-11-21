package net.kyori.adventure.text;

import org.jetbrains.annotations.Contract;

@FunctionalInterface
public interface ComponentBuilderApplicable {
   @Contract(
      mutates = "param"
   )
   void componentBuilderApply(final ComponentBuilder<?, ?> component);
}
