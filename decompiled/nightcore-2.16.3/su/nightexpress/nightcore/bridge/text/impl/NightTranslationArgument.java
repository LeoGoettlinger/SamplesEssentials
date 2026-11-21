package su.nightexpress.nightcore.bridge.text.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.text.NightAbstractComponent;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;

public class NightTranslationArgument {
   private static final NightComponent TRUE = NightComponent.text("true");
   private static final NightComponent FALSE = NightComponent.text("false");
   private final Object value;

   @NonNull
   public static NightTranslationArgument bool(boolean value) {
      return new NightTranslationArgument(value);
   }

   @NonNull
   public static NightTranslationArgument numeric(@NonNull Number value) {
      return new NightTranslationArgument(value);
   }

   @NonNull
   public static NightTranslationArgument component(@NonNull NightComponent value) {
      return new NightTranslationArgument(value);
   }

   NightTranslationArgument(@NonNull Object value) {
      this.value = value;
   }

   @NonNull
   public Object value() {
      return this.value;
   }

   @NonNull
   public NightComponent asComponent() {
      if (this.value instanceof NightComponent) {
         return (NightAbstractComponent)this.value;
      } else if (this.value instanceof Boolean b) {
         return b ? TRUE : FALSE;
      } else {
         return NightComponent.text(String.valueOf(this.value));
      }
   }
}
