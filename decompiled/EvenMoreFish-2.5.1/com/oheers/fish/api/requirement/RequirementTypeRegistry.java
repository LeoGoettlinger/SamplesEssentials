package com.oheers.fish.api.requirement;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import java.util.Map;
import java.util.TreeMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class RequirementTypeRegistry implements EMFRegistry<RequirementType> {
   private static final RequirementTypeRegistry instance = new RequirementTypeRegistry();
   private final Map<String, RequirementType> registry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

   private RequirementTypeRegistry() {
   }

   @NonNull
   public static RequirementTypeRegistry getInstance() {
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
   public Map<String, RequirementType> getRegistry() {
      return Map.copyOf(this.registry);
   }

   @Nullable
   public RequirementType get(@NonNull String key) {
      return this.registry.get(key);
   }

   @NonNull
   public RequirementType getOrDefault(@NonNull String key, @NonNull RequirementType defaultValue) {
      return this.registry.getOrDefault(key, defaultValue);
   }

   @Override
   public boolean unregister(@NonNull String key) {
      return this.registry.remove(key) != null;
   }

   public boolean register(@NonNull RequirementType value, boolean force) {
      if (!force && this.registry.containsKey(value.getKey())) {
         return false;
      } else {
         this.registry.put(value.getKey(), value);
         EMFPlugin.getInstance().debug("Registered " + value.getKey() + " RequirementType");
         return true;
      }
   }
}
