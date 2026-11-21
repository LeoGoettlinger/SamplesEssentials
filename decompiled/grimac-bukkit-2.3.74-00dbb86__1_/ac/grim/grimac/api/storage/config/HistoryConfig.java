package ac.grim.grimac.api.storage.config;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public record HistoryConfig(int entriesPerPage, long groupIntervalMs) {
   public static HistoryConfig defaults() {
      return new HistoryConfig(15, 30000L);
   }
}
