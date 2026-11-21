package ac.grim.grimac.api.storage.query;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.UUID;

@ApiStatus.Experimental
public final class Deletes {
   private Deletes() {
   }

   public static Deletes.ByPlayer byPlayer(UUID uuid) {
      return new Deletes.ByPlayer(uuid);
   }

   public static Deletes.OlderThan olderThan(long maxAgeMs) {
      return new Deletes.OlderThan(maxAgeMs);
   }

   public record ByPlayer(UUID uuid) implements DeleteCriteria {
   }

   public record OlderThan(long maxAgeMs) implements DeleteCriteria {
   }
}
