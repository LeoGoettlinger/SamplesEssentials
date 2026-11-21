package ac.grim.grimac.api.storage.codec;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public interface Codec<R> {
   @NotNull
   Class<R> recordType();

   @NotNull
   EncodeShape shape();

   int version();

   @ac.grim.grimac.shaded.jetbrains.annotations.Nullable
   Object indexField(@NotNull R var1, @NotNull String var2);
}
