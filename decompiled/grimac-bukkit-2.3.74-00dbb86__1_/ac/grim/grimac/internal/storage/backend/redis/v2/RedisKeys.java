package ac.grim.grimac.internal.storage.backend.redis.v2;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.Base64;
import java.util.UUID;

@ApiStatus.Internal
final class RedisKeys {
   private RedisKeys() {
   }

   @NotNull
   static String encode(@NotNull Object key) {
      if (key instanceof UUID u) {
         return u.toString();
      } else if (key instanceof Enum<?> e) {
         return String.valueOf(e.ordinal());
      } else {
         return key instanceof byte[] b ? Base64.getEncoder().encodeToString(b) : key.toString();
      }
   }
}
