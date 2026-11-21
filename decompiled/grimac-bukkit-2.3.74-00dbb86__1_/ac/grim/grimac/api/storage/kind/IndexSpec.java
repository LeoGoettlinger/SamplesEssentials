package ac.grim.grimac.api.storage.kind;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.List;

@ApiStatus.Experimental
public record IndexSpec(@NotNull String name, @NotNull List<String> fields, boolean unique, boolean caseInsensitivePrefix) {
   public IndexSpec(@NotNull String name, @NotNull List<String> fields, boolean unique, boolean caseInsensitivePrefix) {
      fields = List.copyOf(fields);
      if (name.isEmpty()) {
         throw new IllegalArgumentException("name");
      } else if (fields.isEmpty()) {
         throw new IllegalArgumentException("fields");
      } else {
         this.name = name;
         this.fields = fields;
         this.unique = unique;
         this.caseInsensitivePrefix = caseInsensitivePrefix;
      }
   }

   @NotNull
   public static IndexSpec of(@NotNull String name, @NotNull String... fields) {
      return new IndexSpec(name, List.of(fields), false, false);
   }

   @NotNull
   public static IndexSpec unique(@NotNull String name, @NotNull String... fields) {
      return new IndexSpec(name, List.of(fields), true, false);
   }
}
