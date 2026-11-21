package net.kyori.adventure.text;

record TranslationArgumentImpl(Object value) implements TranslationArgument {
   private static final Component TRUE = Component.text("true");
   private static final Component FALSE = Component.text("false");

   @Override
   public Component asComponent() {
      if (this.value instanceof Component component) {
         return component;
      } else if (this.value instanceof Boolean bool) {
         return bool ? TRUE : FALSE;
      } else {
         return Component.text(String.valueOf(this.value));
      }
   }
}
