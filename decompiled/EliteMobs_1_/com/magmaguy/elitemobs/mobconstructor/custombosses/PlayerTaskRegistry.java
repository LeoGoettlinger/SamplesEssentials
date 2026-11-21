package com.magmaguy.elitemobs.mobconstructor.custombosses;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

final class PlayerTaskRegistry {
   private final Map<UUID, EnumMap<PlayerTaskRegistry.Role, Runnable>> cancellationsByPlayer = new HashMap<>();

   void replace(UUID playerId, PlayerTaskRegistry.Role role, Runnable cancellation) {
      Objects.requireNonNull(playerId, "playerId");
      Objects.requireNonNull(role, "role");
      Objects.requireNonNull(cancellation, "cancellation");
      this.cancel(playerId, role);
      this.cancellationsByPlayer.computeIfAbsent(playerId, ignored -> new EnumMap<>(PlayerTaskRegistry.Role.class)).put(role, cancellation);
   }

   void cancel(UUID playerId, PlayerTaskRegistry.Role role) {
      EnumMap<PlayerTaskRegistry.Role, Runnable> playerTasks = this.cancellationsByPlayer.get(playerId);
      if (playerTasks != null) {
         Runnable cancellation = playerTasks.remove(role);
         if (playerTasks.isEmpty()) {
            this.cancellationsByPlayer.remove(playerId);
         }

         if (cancellation != null) {
            cancellation.run();
         }
      }
   }

   void release(UUID playerId, PlayerTaskRegistry.Role role) {
      EnumMap<PlayerTaskRegistry.Role, Runnable> playerTasks = this.cancellationsByPlayer.get(playerId);
      if (playerTasks != null) {
         playerTasks.remove(role);
         if (playerTasks.isEmpty()) {
            this.cancellationsByPlayer.remove(playerId);
         }
      }
   }

   void cancelPlayer(UUID playerId) {
      EnumMap<PlayerTaskRegistry.Role, Runnable> playerTasks = this.cancellationsByPlayer.remove(playerId);
      if (playerTasks != null) {
         new ArrayList<>(playerTasks.values()).forEach(Runnable::run);
      }
   }

   void cancelAll() {
      ArrayList<Runnable> cancellations = new ArrayList<>();
      this.cancellationsByPlayer.values().forEach(tasks -> cancellations.addAll(tasks.values()));
      this.cancellationsByPlayer.clear();
      cancellations.forEach(Runnable::run);
   }

   boolean contains(UUID playerId, PlayerTaskRegistry.Role role) {
      EnumMap<PlayerTaskRegistry.Role, Runnable> playerTasks = this.cancellationsByPlayer.get(playerId);
      return playerTasks != null && playerTasks.containsKey(role);
   }

   Set<UUID> playerIds() {
      return new HashSet<>(this.cancellationsByPlayer.keySet());
   }

   Set<UUID> playerIds(PlayerTaskRegistry.Role role) {
      Set<UUID> playerIds = new HashSet<>();

      for (Entry<UUID, EnumMap<PlayerTaskRegistry.Role, Runnable>> entry : this.cancellationsByPlayer.entrySet()) {
         if (entry.getValue().containsKey(role)) {
            playerIds.add(entry.getKey());
         }
      }

      return playerIds;
   }

   boolean isEmpty() {
      return this.cancellationsByPlayer.isEmpty();
   }

   static enum Role {
      PLAYBACK_LOOP,
      DELAYED_START;
   }
}
