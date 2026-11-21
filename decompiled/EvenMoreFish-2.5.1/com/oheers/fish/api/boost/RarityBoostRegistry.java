package com.oheers.fish.api.boost;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class RarityBoostRegistry implements EMFRegistry<RarityWeightBoost> {
   private static final RarityBoostRegistry instance = new RarityBoostRegistry();
   private final Map<String, RarityWeightBoost> registry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

   private RarityBoostRegistry() {
   }

   @NonNull
   public static RarityBoostRegistry getInstance() {
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
   public Map<String, RarityWeightBoost> getRegistry() {
      return Map.copyOf(this.registry);
   }

   @Nullable
   public RarityWeightBoost get(@NonNull String key) {
      return this.registry.get(key);
   }

   @NonNull
   public RarityWeightBoost getOrDefault(@NonNull String key, @NonNull RarityWeightBoost defaultValue) {
      return this.registry.getOrDefault(key, defaultValue);
   }

   @Override
   public boolean unregister(@NonNull String key) {
      return this.registry.remove(key) != null;
   }

   public boolean register(@NonNull RarityWeightBoost value, boolean force) {
      if (!force && this.registry.containsKey(value.getKey())) {
         return false;
      } else {
         this.registry.put(value.getKey(), value);
         EMFPlugin.getInstance().debug("Registered " + value.getKey() + " RarityWeightBoost");
         return true;
      }
   }

   public double combinedMultiplier(@NonNull Player fisher, @NonNull Location location, @NonNull String rarityId) {
      double combined = 1.0;

      for (RarityWeightBoost boost : this.registry.values()) {
         combined *= boost.weightMultiplier(fisher, location, rarityId);
      }

      return Math.max(0.0, combined);
   }
}
