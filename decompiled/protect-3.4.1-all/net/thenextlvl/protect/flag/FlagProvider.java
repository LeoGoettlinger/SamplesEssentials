package net.thenextlvl.protect.flag;

import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

public interface FlagProvider {
   @Contract(
      pure = true
   )
   @NonNull
   @Unmodifiable
   Map<Flag<?>, Object> getFlags();

   @Contract(
      mutates = "this"
   )
   void setFlags(@NonNull Map<Flag<?>, Object> var1);

   @Contract(
      mutates = "this"
   )
   <T> boolean setFlag(@NonNull Flag<T> var1, T var2);

   @Contract(
      pure = true
   )
   <T> T getFlag(@NonNull Flag<T> var1);

   @Contract(
      pure = true
   )
   <T> boolean hasFlag(@NonNull Flag<T> var1);

   @Contract(
      mutates = "this"
   )
   <T> boolean removeFlag(@NonNull Flag<T> var1);
}
