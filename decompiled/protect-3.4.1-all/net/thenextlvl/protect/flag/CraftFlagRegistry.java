package net.thenextlvl.protect.flag;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern.Value;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

public final class CraftFlagRegistry implements FlagRegistry {
   private final Map<Plugin, Set<Flag<?>>> registry = new HashMap<>();

   public Map<Plugin, Set<Flag<?>>> getRegistry() {
      return this.registry;
   }

   @NullMarked
   @Override
   public Set<Flag<?>> getFlags() {
      return this.registry.values().stream().flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet());
   }

   @NullMarked
   @Override
   public Set<Flag<?>> getFlags(Plugin plugin) {
      return this.registry.get(plugin);
   }

   @NullMarked
   @Override
   public <T> Optional<Flag<T>> getFlag(Key key) {
      return this.getFlags().stream().filter(flag -> flag.key().equals(key)).map(flag -> (Flag<T>)flag).findAny();
   }

   @NonNull
   @Override
   public <T> Flag<T> register(@NonNull Plugin plugin, @NonNull Class<? extends T> type, @Value @NonNull String name, T defaultValue) throws IllegalStateException {
      return this.register(plugin, name, key -> new CraftFlag<>(key, type, defaultValue));
   }

   @NonNull
   @Override
   public <T> ProtectionFlag<T> register(
      @NonNull Plugin plugin, @NonNull Class<? extends T> type, @Value @NonNull String name, T defaultValue, T protectedValue
   ) throws IllegalStateException {
      return this.register(plugin, name, key -> new CraftProtectionFlag<>(key, type, defaultValue, protectedValue));
   }

   private <T extends Flag<?>> T register(@NonNull Plugin plugin, @Value @NonNull String name, @NonNull Function<Key, T> function) {
      Key key = Key.key(plugin.getName().replace("-", "_").toLowerCase(), name);
      T flag = (T)function.apply(key);
      if (this.registry.computeIfAbsent(plugin, p -> new HashSet<>()).add(flag)) {
         return flag;
      } else {
         throw new IllegalStateException("Already registered flag: " + key);
      }
   }

   @Override
   public boolean unregister(@NonNull Key key) {
      for (Set<Flag<?>> flags : this.registry.values()) {
         if (flags.removeIf(flag -> flag.key().equals(key))) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean unregisterAll(@NonNull Plugin plugin) {
      Set<Flag<?>> remove = this.registry.remove(plugin);
      return remove != null && !remove.isEmpty();
   }
}
