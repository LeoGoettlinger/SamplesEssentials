package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import org.jetbrains.annotations.Contract;

public sealed interface TextComponent extends ScopedComponent<TextComponent> permits TextComponentImpl, VirtualComponent {
   String content();

   @Contract(
      pure = true
   )
   TextComponent content(final String content);

   TextComponent.Builder toBuilder();

   public sealed interface Builder extends ComponentBuilder<TextComponent, TextComponent.Builder> permits TextComponentImpl.BuilderImpl {
      String content();

      @Contract("_ -> this")
      TextComponent.Builder content(final String content);
   }
}
