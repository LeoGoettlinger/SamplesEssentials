package net.luckperms.api.context;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface ContextSetFactory {
   @NonNull
   ImmutableContextSet.Builder immutableBuilder();

   @NonNull
   ImmutableContextSet immutableOf(@NonNull String var1, @NonNull String var2);

   @NonNull
   ImmutableContextSet immutableEmpty();

   @NonNull
   MutableContextSet mutable();
}
