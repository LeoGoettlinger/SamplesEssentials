package net.luckperms.api.context;

import java.util.Optional;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface ContextManager {
   @NonNull
   ImmutableContextSet getContext(@NonNull Object var1);

   @NonNull
   Optional<ImmutableContextSet> getContext(@NonNull User var1);

   @NonNull
   ImmutableContextSet getStaticContext();

   @NonNull
   QueryOptions.Builder queryOptionsBuilder(@NonNull QueryMode var1);

   @NonNull
   QueryOptions getQueryOptions(@NonNull Object var1);

   @NonNull
   Optional<QueryOptions> getQueryOptions(@NonNull User var1);

   @NonNull
   QueryOptions getStaticQueryOptions();

   void registerCalculator(@NonNull ContextCalculator<?> var1);

   void unregisterCalculator(@NonNull ContextCalculator<?> var1);

   void signalContextUpdate(@NonNull Object var1);

   @Internal
   @NonNull
   ContextSetFactory getContextSetFactory();

   @Deprecated
   default void invalidateCache(@NonNull Object subject) {
      this.signalContextUpdate(subject);
   }
}
