package ac.grim.grimac.api.storage.history;

import ac.grim.grimac.api.storage.model.VerboseFormat;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public record ViolationEntry(
   int checkId,
   @NotNull String stableKey,
   @NotNull String displayName,
   @NotNull String description,
   long offsetFromSessionStartMs,
   double vl,
   @Nullable String verbose,
   @NotNull VerboseFormat verboseFormat
) {
   public ViolationEntry(
      int checkId,
      @NotNull String stableKey,
      @NotNull String displayName,
      @NotNull String description,
      long offsetFromSessionStartMs,
      double vl,
      @Nullable String verbose,
      @NotNull VerboseFormat verboseFormat
   ) {
      if (description == null) {
         description = "";
      }

      this.checkId = checkId;
      this.stableKey = stableKey;
      this.displayName = displayName;
      this.description = description;
      this.offsetFromSessionStartMs = offsetFromSessionStartMs;
      this.vl = vl;
      this.verbose = verbose;
      this.verboseFormat = verboseFormat;
   }
}
