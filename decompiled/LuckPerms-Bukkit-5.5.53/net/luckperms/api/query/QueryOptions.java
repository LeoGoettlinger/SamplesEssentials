package net.luckperms.api.query;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ContextSatisfyMode;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface QueryOptions {
   @NonNull
   static QueryOptions.Builder builder(@NonNull QueryMode mode) {
      return LuckPermsProvider.get().getContextManager().queryOptionsBuilder(mode);
   }

   @NonNull
   static QueryOptions contextual(@NonNull ContextSet context, @NonNull Set<Flag> flags) {
      return builder(QueryMode.CONTEXTUAL).context(context).flags(flags).build();
   }

   @NonNull
   static QueryOptions contextual(@NonNull ContextSet context) {
      return builder(QueryMode.CONTEXTUAL).context(context).build();
   }

   @NonNull
   static QueryOptions nonContextual(@NonNull Set<Flag> flags) {
      return builder(QueryMode.NON_CONTEXTUAL).flags(flags).build();
   }

   @NonNull
   static QueryOptions nonContextual() {
      return LuckPermsProvider.get().getQueryOptionsRegistry().defaultNonContextualOptions();
   }

   @NonNull
   static QueryOptions defaultContextualOptions() {
      return LuckPermsProvider.get().getQueryOptionsRegistry().defaultContextualOptions();
   }

   @NonNull
   QueryMode mode();

   @NonNull
   ImmutableContextSet context();

   boolean flag(@NonNull Flag var1);

   @NonNull
   Set<Flag> flags();

   @NonNull
   <O> Optional<O> option(@NonNull OptionKey<O> var1);

   @NonNull
   Map<OptionKey<?>, Object> options();

   default boolean satisfies(@NonNull ContextSet contextSet) {
      return this.satisfies(contextSet, ContextSatisfyMode.AT_LEAST_ONE_VALUE_PER_KEY);
   }

   boolean satisfies(@NonNull ContextSet var1, @NonNull ContextSatisfyMode var2);

   @NonNull
   QueryOptions.Builder toBuilder();

   @NonExtendable
   public interface Builder {
      @NonNull
      QueryOptions.Builder mode(@NonNull QueryMode var1);

      @NonNull
      QueryOptions.Builder context(@NonNull ContextSet var1);

      @NonNull
      QueryOptions.Builder flag(@NonNull Flag var1, boolean var2);

      @NonNull
      QueryOptions.Builder flags(@NonNull Set<Flag> var1);

      @NonNull
      <O> QueryOptions.Builder option(@NonNull OptionKey<O> var1, @Nullable O var2);

      @NonNull
      QueryOptions build();
   }
}
