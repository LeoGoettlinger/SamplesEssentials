package net.kyori.adventure.text.format;

public sealed interface TextDecorationAndState extends StyleBuilderApplicable permits TextDecorationAndStateImpl {
   TextDecoration decoration();

   TextDecoration.State state();

   @Override
   default void styleApply(final Style.Builder style) {
      style.decoration(this.decoration(), this.state());
   }
}
