package net.luckperms.api.context;

import java.util.Objects;
import net.luckperms.api.LuckPermsProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ImmutableContextSet extends ContextSet {
   @NonNull
   static ImmutableContextSet.Builder builder() {
      return LuckPermsProvider.get().getContextManager().getContextSetFactory().immutableBuilder();
   }

   @NonNull
   static ImmutableContextSet empty() {
      return LuckPermsProvider.get().getContextManager().getContextSetFactory().immutableEmpty();
   }

   @NonNull
   static ImmutableContextSet of(@NonNull String key, @NonNull String value) {
      return LuckPermsProvider.get().getContextManager().getContextSetFactory().immutableOf(key, value);
   }

   @Deprecated
   @NonNull
   @Override
   ImmutableContextSet immutableCopy();

   public interface Builder {
      @NonNull
      ImmutableContextSet.Builder add(@NonNull String var1, @NonNull String var2);

      @NonNull
      default ImmutableContextSet.Builder add(@NonNull Context entry) {
         Objects.requireNonNull(entry, "entry");
         this.add(entry.getKey(), entry.getValue());
         return this;
      }

      @NonNull
      default ImmutableContextSet.Builder addAll(@NonNull Iterable<Context> iterable) {
         for (Context e : Objects.requireNonNull(iterable, "iterable")) {
            this.add(e);
         }

         return this;
      }

      @NonNull
      ImmutableContextSet.Builder addAll(@NonNull ContextSet var1);

      @NonNull
      ImmutableContextSet build();
   }
}
