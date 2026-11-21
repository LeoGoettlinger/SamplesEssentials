package net.kyori.adventure.util;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

public interface ComponentMessageThrowable {
   @Nullable
   static Component getMessage(@Nullable final Throwable throwable) {
      return throwable instanceof ComponentMessageThrowable cmt ? cmt.componentMessage() : null;
   }

   @Nullable
   static Component getOrConvertMessage(@Nullable final Throwable throwable) {
      if (throwable instanceof ComponentMessageThrowable cmt) {
         return cmt.componentMessage();
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
