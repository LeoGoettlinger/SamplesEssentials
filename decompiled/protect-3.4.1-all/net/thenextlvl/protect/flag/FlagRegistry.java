package net.thenextlvl.protect.flag;

import java.util.Optional;
import java.util.Set;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern.Value;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

public interface FlagRegistry {
   @Contract(
      pure = true
   )
   @NonNull
   @Unmodifiable
   Set<Flag<?>> getFlags();

   @Contract(
      pure = true
   )
   @NonNull
   Set<Flag<?>> getFlags(@NonNull Plugin var1);

   @Contract(
      pure = true
   )
   @NonNull
   <T> Optional<Flag<T>> getFlag(@NonNull Key var1);

   @Contract(
      mutates = "this"
   )
   default <T> Flag<T> register(Plugin plugin, @Value String name, T defaultValue) throws IllegalStateException {
      return this.register(plugin, (Class<? extends T>)defaultValue.getClass(), name, defaultValue);
   }

   @Contract(
      mutates = "this"
   )
   @NonNull
   <T> Flag<T> register(@NonNull Plugin var1, @NonNull Class<? extends T> var2, @Value String var3, T var4) throws IllegalStateException;

   @Contract(
      mutates = "this"
   )
   default <T> ProtectionFlag<T> register(Plugin plugin, @Value String name, T defaultValue, T protectedValue) throws IllegalStateException {
      return this.register(plugin, (Class<? extends T>)defaultValue.getClass(), name, defaultValue, protectedValue);
   }

   @Contract(
      mutates = "this"
   )
   @NonNull
   <T> ProtectionFlag<T> register(@NonNull Plugin var1, @NonNull Class<? extends T> var2, @Value @NonNull String var3, T var4, T var5) throws IllegalStateException;

   @Contract(
      mutates = "this"
   )
   boolean unregister(@NonNull Key var1);

   @Contract(
      mutates = "this"
   )
   boolean unregisterAll(@NonNull Plugin var1);
}
