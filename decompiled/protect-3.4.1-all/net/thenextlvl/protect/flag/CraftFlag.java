package net.thenextlvl.protect.flag;

import java.util.Objects;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NonNull;

public record CraftFlag<T>(@NonNull Key key, @NonNull Class<? extends T> type, T defaultValue) implements Flag<T> {
   public int compareTo(@NonNull Flag<?> flag) {
      return this.key().compareTo(flag.key());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof CraftFlag<?> craftFlag ? Objects.equals(this.key, craftFlag.key) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.key);
   }
}
