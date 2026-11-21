package net.luckperms.api.query;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface OptionKey<T> {
   @NonNull
   static <T> OptionKey<T> of(@NonNull String name, @NonNull Class<T> type) {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(type, "type");
      return new SimpleOptionKey<>(name, type);
   }

   @NonNull
   String name();

   @NonNull
   Class<T> type();
}
