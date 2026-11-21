package net.luckperms.api.metastacking;

import java.util.List;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;

public interface MetaStackFactory {
   @NonNull
   Optional<MetaStackElement> fromString(@NonNull String var1);

   @NonNull
   @Unmodifiable
   List<MetaStackElement> fromStrings(@NonNull List<String> var1);

   @NonNull
   MetaStackDefinition createDefinition(
      @NonNull List<MetaStackElement> var1, @NonNull DuplicateRemovalFunction var2, @NonNull String var3, @NonNull String var4, @NonNull String var5
   );
}
