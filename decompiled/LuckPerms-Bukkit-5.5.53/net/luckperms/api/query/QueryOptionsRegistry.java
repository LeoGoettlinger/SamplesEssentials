package net.luckperms.api.query;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface QueryOptionsRegistry {
   @NonNull
   QueryOptions defaultContextualOptions();

   @NonNull
   QueryOptions defaultNonContextualOptions();
}
