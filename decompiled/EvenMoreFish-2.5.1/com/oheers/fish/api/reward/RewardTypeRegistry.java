package com.oheers.fish.api.reward;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import java.util.Map;
import java.util.TreeMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class RewardTypeRegistry implements EMFRegistry<RewardType> {
   private static final RewardTypeRegistry instance = new RewardTypeRegistry();
   private final Map<String, RewardType> registry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

   private RewardTypeRegistry() {
   }

   @NonNull
   public static RewardTypeRegistry getInstance() {
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
   public Map<String, RewardType> getRegistry() {
      return Map.copyOf(this.registry);
   }

   @Nullable
   public RewardType get(@NonNull String key) {
      return this.registry.get(key);
   }

   @NonNull
   public RewardType getOrDefault(@NonNull String key, @NonNull RewardType defaultValue) {
      return this.registry.getOrDefault(key, defaultValue);
   }

   @Override
   public boolean unregister(@NonNull String key) {
      return this.registry.remove(key) != null;
   }

   public boolean register(@NonNull RewardType value, boolean force) {
      if (!force && this.registry.containsKey(value.getKey())) {
         return false;
      } else {
         this.registry.put(value.getKey(), value);
         EMFPlugin.getInstance().debug("Registered " + value.getKey() + " RewardType");
         return true;
      }
   }
}
