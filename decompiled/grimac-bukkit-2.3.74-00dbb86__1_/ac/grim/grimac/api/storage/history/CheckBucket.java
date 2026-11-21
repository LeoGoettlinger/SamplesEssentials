package ac.grim.grimac.api.storage.history;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.List;

@ApiStatus.Experimental
public record CheckBucket(long bucketStartOffsetMs, @NotNull List<CheckCount> checks) {
   public CheckBucket(long bucketStartOffsetMs, @NotNull List<CheckCount> checks) {
      checks = checks == null ? List.of() : List.copyOf(checks);
      this.bucketStartOffsetMs = bucketStartOffsetMs;
      this.checks = checks;
   }
}
