package net.thenextlvl.protect.flag;

import java.util.Objects;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NonNull;

public record CraftProtectionFlag<T>(@NonNull Key key, @NonNull Class<? extends T> type, T defaultValue, T protectedValue) implements ProtectionFlag<T> {
   public int compareTo(@NonNull Flag<?> flag) {
      return this.key().compareTo(flag.key());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof CraftProtectionFlag<?> craftFlag ? Objects.equals(this.key, craftFlag.key) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.key);
   }
}
