package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.data.DataRepository;
import de.theredend2000.advancedhunt.model.PlaceItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlaceItemManager {
   private final Main plugin;
   private final DataRepository repository;
   private final Map<UUID, PlaceItem> items = new ConcurrentHashMap<>();
   private final Map<String, String> groups = new ConcurrentHashMap<>();

   public PlaceItemManager(Main plugin, DataRepository repository) {
      this.plugin = plugin;
      this.repository = repository;
   }

   public CompletableFuture<Void> reloadItems() {
      CompletableFuture<List<PlaceItem>> itemsFuture = this.repository.loadPlaceItems();
      CompletableFuture<Set<String>> groupsFuture = this.repository.loadPlaceItemGroups().exceptionally(ex -> Collections.emptySet());
      return itemsFuture.<Set<String>, Object>thenCombine(groupsFuture, (list, persistedGroups) -> {
         this.items.clear();
         this.groups.clear();
         if (persistedGroups != null) {
            for (String group : persistedGroups) {
               this.addGroup(group);
            }
         }

         if (list != null) {
            for (PlaceItem preset : list) {
               if (preset != null) {
                  this.items.put(preset.getId(), preset);
                  this.addGroup(preset.getGroup());
               }
            }
         }

         return null;
      }).thenAccept(v -> {});
   }

   public Optional<PlaceItem> getItem(UUID id) {
      return id == null ? Optional.empty() : Optional.ofNullable(this.items.get(id));
   }

   public List<PlaceItem> getAllItems() {
      return new ArrayList<>(this.items.values());
   }

   public List<String> getGroups() {
      TreeSet<String> groups = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

      for (String group : this.groups.values()) {
         if (group != null && !group.trim().isEmpty()) {
            groups.add(group);
         }
      }

      return new ArrayList<>(groups);
   }

   public boolean hasGroup(String group) {
      return group != null && !group.trim().isEmpty() ? this.groups.containsKey(normalize(group)) : false;
   }

   public CompletableFuture<Boolean> createGroup(String group) {
      if (group != null && !group.trim().isEmpty()) {
         String trimmed = group.trim();
         return this.hasGroup(trimmed)
            ? CompletableFuture.completedFuture(false)
            : this.repository.createPlaceItemGroup(trimmed).thenCompose(v -> this.reloadItems()).thenApply(v -> true).exceptionally(ex -> {
               this.plugin.getLogger().warning("Failed to create place preset group: " + ex.getMessage());
               return false;
            });
      } else {
         return CompletableFuture.completedFuture(false);
      }
   }

   public CompletableFuture<Boolean> renameGroup(String oldGroup, String newGroup) {
      if (oldGroup != null && !oldGroup.trim().isEmpty() && newGroup != null && !newGroup.trim().isEmpty()) {
         String oldTrimmed = oldGroup.trim();
         String newTrimmed = newGroup.trim();
         if (!this.hasGroup(oldTrimmed)) {
            return CompletableFuture.completedFuture(false);
         } else if (this.hasGroup(newTrimmed)) {
            return CompletableFuture.completedFuture(false);
         } else {
            return normalize(oldTrimmed).equals(normalize(newTrimmed))
               ? CompletableFuture.completedFuture(false)
               : this.repository.renamePlaceItemGroup(oldTrimmed, newTrimmed).thenCompose(v -> {
                  List<PlaceItem> toMove = this.getItemsInGroup(oldTrimmed);
                  if (toMove.isEmpty()) {
                     return CompletableFuture.completedFuture(null);
                  } else {
                     CompletableFuture<?>[] futures = new CompletableFuture[toMove.size()];

                     for (int i = 0; i < toMove.size(); i++) {
                        PlaceItem preset = toMove.get(i);
                        preset.setGroup(newTrimmed);
                        futures[i] = this.repository.savePlaceItem(preset);
                     }

                     return CompletableFuture.allOf(futures);
                  }
               }).thenCompose(v -> this.reloadItems()).thenApply(v -> true).exceptionally(ex -> {
                  this.plugin.getLogger().warning("Failed to rename place preset group: " + ex.getMessage());
                  return false;
               });
         }
      } else {
         return CompletableFuture.completedFuture(false);
      }
   }

   public CompletableFuture<Boolean> deleteGroup(String group) {
      if (group != null && !group.trim().isEmpty()) {
         String trimmed = group.trim();
         if (!this.hasGroup(trimmed)) {
            return CompletableFuture.completedFuture(false);
         } else {
            List<PlaceItem> inGroup = this.getItemsInGroup(trimmed);
            CompletableFuture<?>[] deleteFutures = new CompletableFuture[inGroup.size()];

            for (int i = 0; i < inGroup.size(); i++) {
               deleteFutures[i] = this.repository.deletePlaceItem(inGroup.get(i).getId());
            }

            return CompletableFuture.allOf(deleteFutures)
               .thenCompose(v -> this.repository.deletePlaceItemGroup(trimmed))
               .thenCompose(v -> this.reloadItems())
               .thenApply(v -> true)
               .exceptionally(ex -> {
                  this.plugin.getLogger().warning("Failed to delete place preset group: " + ex.getMessage());
                  return false;
               });
         }
      } else {
         return CompletableFuture.completedFuture(false);
      }
   }

   public List<PlaceItem> getItemsInGroup(String group) {
      if (group == null) {
         return Collections.emptyList();
      } else {
         List<PlaceItem> list = new ArrayList<>();

         for (PlaceItem preset : this.items.values()) {
            if (preset.getGroup() != null && preset.getGroup().equalsIgnoreCase(group)) {
               list.add(preset);
            }
         }

         list.sort(Comparator.comparing(PlaceItem::getName, String.CASE_INSENSITIVE_ORDER));
         return list;
      }
   }

   public boolean hasPresetNameInGroup(String group, String name) {
      if (group != null && name != null) {
         String g = normalize(group);
         String n = normalize(name);

         for (PlaceItem preset : this.items.values()) {
            if (preset.getGroup() != null && preset.getName() != null && normalize(preset.getGroup()).equals(g) && normalize(preset.getName()).equals(n)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public CompletableFuture<Boolean> createItem(String group, String name, String itemData) {
      if (group == null || group.trim().isEmpty() || name == null || name.trim().isEmpty() || itemData == null || itemData.trim().isEmpty()) {
         return CompletableFuture.completedFuture(false);
      } else if (this.hasPresetNameInGroup(group, name)) {
         return CompletableFuture.completedFuture(false);
      } else {
         PlaceItem preset = new PlaceItem(UUID.randomUUID(), group.trim(), name.trim(), itemData);
         return this.repository.savePlaceItem(preset).thenCompose(v -> this.reloadItems()).thenApply(v -> true).exceptionally(ex -> {
            this.plugin.getLogger().warning("Failed to create place preset: " + ex.getMessage());
            return false;
         });
      }
   }

   public CompletableFuture<Void> saveItem(PlaceItem preset) {
      return preset == null ? CompletableFuture.completedFuture(null) : this.repository.savePlaceItem(preset).thenCompose(v -> this.reloadItems());
   }

   public CompletableFuture<Void> deleteItem(UUID presetId) {
      return presetId == null ? CompletableFuture.completedFuture(null) : this.repository.deletePlaceItem(presetId).thenCompose(v -> this.reloadItems());
   }

   private static String normalize(String value) {
      return value.trim().toLowerCase(Locale.ROOT);
   }

   private void addGroup(String group) {
      if (group != null && !group.trim().isEmpty()) {
         String trimmed = group.trim();
         this.groups.putIfAbsent(normalize(trimmed), trimmed);
      }
   }
}
