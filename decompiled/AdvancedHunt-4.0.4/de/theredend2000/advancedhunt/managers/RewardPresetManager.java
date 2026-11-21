package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.data.DataRepository;
import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.RewardPreset;
import de.theredend2000.advancedhunt.model.RewardPresetType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class RewardPresetManager {
   private final Main plugin;
   private final DataRepository repository;
   private final Map<UUID, RewardPreset> treasurePresets = new ConcurrentHashMap<>();
   private final Map<UUID, RewardPreset> collectionPresets = new ConcurrentHashMap<>();

   public RewardPresetManager(Main plugin, DataRepository repository) {
      this.plugin = plugin;
      this.repository = repository;
   }

   public CompletableFuture<Void> reloadPresets() {
      CompletableFuture<List<RewardPreset>> treasureFuture = this.repository.loadRewardPresets(RewardPresetType.TREASURE);
      CompletableFuture<List<RewardPreset>> collectionFuture = this.repository.loadRewardPresets(RewardPresetType.COLLECTION);
      return treasureFuture.thenCombine(collectionFuture, (treasure, collection) -> {
         this.treasurePresets.clear();
         this.collectionPresets.clear();
         if (treasure != null) {
            for (RewardPreset preset : treasure) {
               this.treasurePresets.put(preset.getId(), preset);
            }
         }

         if (collection != null) {
            for (RewardPreset preset : collection) {
               this.collectionPresets.put(preset.getId(), preset);
            }
         }

         return null;
      });
   }

   public List<RewardPreset> getPresets(RewardPresetType type) {
      return new ArrayList<>(this.getMap(type).values());
   }

   public Optional<RewardPreset> getPreset(RewardPresetType type, UUID id) {
      return id == null ? Optional.empty() : Optional.ofNullable(this.getMap(type).get(id));
   }

   public boolean hasPresetName(RewardPresetType type, String name) {
      if (name == null) {
         return false;
      } else {
         String normalized = normalizeName(name);

         for (RewardPreset preset : this.getMap(type).values()) {
            if (normalizeName(preset.getName()).equals(normalized)) {
               return true;
            }
         }

         return false;
      }
   }

   public CompletableFuture<Boolean> createPreset(RewardPresetType type, String name, List<Reward> rewards) {
      if (name == null || name.trim().isEmpty()) {
         return CompletableFuture.completedFuture(false);
      } else if (this.hasPresetName(type, name)) {
         return CompletableFuture.completedFuture(false);
      } else {
         RewardPreset preset = new RewardPreset(UUID.randomUUID(), type, name.trim(), rewards);
         return this.repository.saveRewardPreset(preset).thenCompose(v -> this.reloadPresets()).thenApply(v -> true).exceptionally(ex -> {
            this.plugin.getLogger().warning("Failed to create preset: " + ex.getMessage());
            return false;
         });
      }
   }

   public CompletableFuture<Void> savePreset(RewardPreset preset) {
      return preset == null ? CompletableFuture.completedFuture(null) : this.repository.saveRewardPreset(preset).thenCompose(v -> this.reloadPresets());
   }

   public CompletableFuture<Void> deletePreset(RewardPresetType type, UUID presetId) {
      return presetId == null
         ? CompletableFuture.completedFuture(null)
         : this.repository.deleteRewardPreset(type, presetId).thenCompose(v -> this.reloadPresets());
   }

   private Map<UUID, RewardPreset> getMap(RewardPresetType type) {
      return type == RewardPresetType.COLLECTION ? this.collectionPresets : this.treasurePresets;
   }

   private static String normalizeName(String name) {
      return name.trim().toLowerCase(Locale.ROOT);
   }
}
