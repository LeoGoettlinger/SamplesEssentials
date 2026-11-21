package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

@FunctionalInterface
public interface TranslationArgumentLike extends ComponentLike {
   TranslationArgument asTranslationArgument();

   @Override
   default Component asComponent() {
      return this.asTranslationArgument().asComponent();
   }
}
