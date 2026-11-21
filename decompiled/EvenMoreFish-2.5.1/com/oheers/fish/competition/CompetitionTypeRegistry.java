package com.oheers.fish.competition;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import java.util.Map;
import java.util.TreeMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CompetitionTypeRegistry implements EMFRegistry<CompetitionType> {
   private static final CompetitionTypeRegistry instance = new CompetitionTypeRegistry();
   private final Map<String, CompetitionType> registry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

   private CompetitionTypeRegistry() {
   }

   @NonNull
   public static CompetitionTypeRegistry getInstance() {
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
   public Map<String, CompetitionType> getRegistry() {
      return Map.copyOf(this.registry);
   }

   @Nullable
   public CompetitionType get(@NonNull String key) {
      return this.registry.get(key);
   }

   @NonNull
   public CompetitionType getOrDefault(@NonNull String key, @NonNull CompetitionType defaultValue) {
      return this.registry.getOrDefault(key, defaultValue);
   }

   @Override
   public boolean unregister(@NonNull String key) {
      return this.registry.remove(key) != null;
   }

   public boolean register(@NonNull CompetitionType value, boolean force) {
      if (!force && this.registry.containsKey(value.getKey())) {
         return false;
      } else {
         this.registry.put(value.getKey(), value);
         EMFPlugin.getInstance().debug("Registered " + value.getKey() + " CompetitionType");
         return true;
      }
   }
}
