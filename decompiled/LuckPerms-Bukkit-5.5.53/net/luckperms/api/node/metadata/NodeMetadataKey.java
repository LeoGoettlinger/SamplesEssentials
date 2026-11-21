package net.luckperms.api.node.metadata;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface NodeMetadataKey<T> {
   @NonNull
   static <T> NodeMetadataKey<T> of(@NonNull String name, @NonNull Class<T> type) {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(type, "type");
      return new SimpleNodeMetadataKey<>(name, type);
   }

   @NonNull
   String name();

   @NonNull
   Class<T> type();
}
