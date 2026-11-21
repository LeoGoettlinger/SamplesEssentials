package net.luckperms.api.context;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Context {
   static boolean isValidKey(@Nullable String key) {
      if (key != null && !key.isEmpty()) {
         int i = 0;

         for (int n = key.length(); i < n; i++) {
            if (key.charAt(i) != ' ') {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   static boolean isValidValue(@Nullable String value) {
      return isValidKey(value);
   }

   @NonNull
   String getKey();

   @NonNull
   String getValue();
}
