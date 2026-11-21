package ac.grim.grimac.api.storage.admin;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.List;

@ApiStatus.Experimental
public record IndexStats(@NotNull List<IndexStats.Entry> entries) {
   public IndexStats(@NotNull List<IndexStats.Entry> entries) {
      entries = List.copyOf(entries);
      this.entries = entries;
   }

   public record Entry(@NotNull String name, long sizeBytes, long entryCount, double usageRate) {
   }
}
