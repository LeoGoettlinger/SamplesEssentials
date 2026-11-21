package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import org.jetbrains.annotations.Contract;

@FunctionalInterface
public interface ComponentBuilderApplicable {
   @Contract(
      mutates = "param"
   )
   void componentBuilderApply(final ComponentBuilder<?, ?> component);
}
