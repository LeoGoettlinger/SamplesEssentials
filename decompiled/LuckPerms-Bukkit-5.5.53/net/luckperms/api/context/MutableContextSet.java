package net.luckperms.api.context;

import java.util.Objects;
import net.luckperms.api.LuckPermsProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface MutableContextSet extends ContextSet {
   @NonNull
   static MutableContextSet create() {
      return LuckPermsProvider.get().getContextManager().getContextSetFactory().mutable();
   }

   @NonNull
   static MutableContextSet of(@NonNull String key, @NonNull String value) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(value, "value");
      MutableContextSet set = create();
      set.add(key, value);
      return set;
   }

   void add(@NonNull String var1, @NonNull String var2);

   default void add(@NonNull Context entry) {
      Objects.requireNonNull(entry, "entry");
      this.add(entry.getKey(), entry.getValue());
   }

   default void addAll(@NonNull Iterable<Context> iterable) {
      for (Context e : Objects.requireNonNull(iterable, "iterable")) {
         this.add(e);
      }
   }

   void addAll(@NonNull ContextSet var1);

   void remove(@NonNull String var1, @NonNull String var2);

   void removeAll(@NonNull String var1);

   void clear();
}
