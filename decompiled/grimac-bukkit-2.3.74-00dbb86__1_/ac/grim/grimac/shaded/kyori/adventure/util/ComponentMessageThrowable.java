package ac.grim.grimac.shaded.kyori.adventure.util;

import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;

public interface ComponentMessageThrowable {
   @Nullable
   static Component getMessage(@Nullable final Throwable throwable) {
      return throwable instanceof ComponentMessageThrowable ? ((ComponentMessageThrowable)throwable).componentMessage() : null;
   }

   @Nullable
   static Component getOrConvertMessage(@Nullable final Throwable throwable) {
      if (throwable instanceof ComponentMessageThrowable) {
         return ((ComponentMessageThrowable)throwable).componentMessage();
      } else {
         if (throwable != null) {
            String message = throwable.getMessage();
            if (message != null) {
               return Component.text(message);
            }
         }

         return null;
      }
   }

   @Nullable
   Component componentMessage();
}
