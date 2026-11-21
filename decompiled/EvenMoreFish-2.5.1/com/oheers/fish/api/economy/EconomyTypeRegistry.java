package com.oheers.fish.api.economy;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import java.util.Map;
import java.util.TreeMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class EconomyTypeRegistry implements EMFRegistry<EconomyType> {
   private static final EconomyTypeRegistry instance = new EconomyTypeRegistry();
   private final Map<String, EconomyType> registry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

   private EconomyTypeRegistry() {
   }

   @NonNull
   public static EconomyTypeRegistry getInstance() {
      return instance;
   }

   @Override
   public boolean isEmpty() {
      return this.registry.isEmpty();
   }

   @Override
   public void clear() {
      this.registry.clear();
   }

   @NonNull
   @Override
   public Map<String, EconomyType> getRegistry() {
      return Map.copyOf(this.registry);
   }

   @Nullable
   public EconomyType get(@NonNull String key) {
      return this.registry.get(key);
   }

   @NonNull
   public EconomyType getOrDefault(@NonNull String key, @NonNull EconomyType defaultValue) {
      return this.registry.getOrDefault(key, defaultValue);
   }

   @Override
   public boolean unregister(@NonNull String key) {
      return this.registry.remove(key) != null;
   }

   public boolean register(@NonNull EconomyType value, boolean force) {
      if (!force && this.registry.containsKey(value.getKey())) {
         return false;
      } else {
         this.registry.put(value.getKey(), value);
         EMFPlugin.getInstance().debug("Registered " + value.getKey() + " EconomyType");
         return true;
      }
   }
}
