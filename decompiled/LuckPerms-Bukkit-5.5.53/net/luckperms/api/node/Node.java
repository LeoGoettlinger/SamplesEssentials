package net.luckperms.api.node;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.node.metadata.NodeMetadataKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface Node {
   @NonNull
   static NodeBuilder<?, ?> builder(@NonNull String key) {
      return LuckPermsProvider.get().getNodeBuilderRegistry().forKey(key);
   }

   @NonNull
   NodeType<?> getType();

   @NonNull
   String getKey();

   boolean getValue();

   default boolean isNegated() {
      return !this.getValue();
   }

   @NonNull
   @Unmodifiable
   Collection<String> resolveShorthand();

   boolean hasExpiry();

   @Nullable
   Instant getExpiry();

   boolean hasExpired();

   @Nullable
   Duration getExpiryDuration();

   @NonNull
   ImmutableContextSet getContexts();

   <T> Optional<T> getMetadata(@NonNull NodeMetadataKey<T> var1);

   default <T> T metadata(@NonNull NodeMetadataKey<T> key) throws IllegalStateException {
      return this.getMetadata(key).orElseThrow(() -> new IllegalStateException("Node '" + this.getKey() + "' does not have '" + key.name() + "' attached."));
   }

   @Override
   boolean equals(Object var1);

   boolean equals(@NonNull Node var1, @NonNull NodeEqualityPredicate var2);

   @NonNull
   NodeBuilder<?, ?> toBuilder();
}
