package ac.grim.grimac.api.storage.history;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public record CheckCount(int checkId, @NotNull String stableKey, @NotNull String displayName, @NotNull String description, int count) {
   public CheckCount(int checkId, @NotNull String stableKey, @NotNull String displayName, @NotNull String description, int count) {
      if (description == null) {
         description = "";
      }

      this.checkId = checkId;
      this.stableKey = stableKey;
      this.displayName = displayName;
      this.description = description;
      this.count = count;
   }
}
