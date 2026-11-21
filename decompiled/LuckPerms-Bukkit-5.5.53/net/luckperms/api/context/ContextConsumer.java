package net.luckperms.api.context;

import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface ContextConsumer {
   void accept(@NonNull String var1, @NonNull String var2);

   default void accept(@NonNull Context context) {
      this.accept(context.getKey(), context.getValue());
   }

   default void accept(@NonNull ContextSet contextSet) {
      for (Context entry : contextSet) {
         this.accept(entry);
      }
   }
}
