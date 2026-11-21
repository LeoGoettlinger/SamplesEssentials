package com.magmaguy.elitemobs.advancedcombat.minions;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class CorpseOpportunityLedger {
   private final Map<UUID, CorpseOpportunityLedger.Entry> entries = new HashMap<>();

   public void offer(UUID corpseId, Set<UUID> eligiblePlayers, long expiresAtTick) {
      Objects.requireNonNull(corpseId, "corpseId");
      Objects.requireNonNull(eligiblePlayers, "eligiblePlayers");
      if (eligiblePlayers.isEmpty()) {
         throw new IllegalArgumentException("A corpse needs an eligible player");
      } else if (this.entries.containsKey(corpseId)) {
         throw new IllegalStateException("Corpse already exists: " + corpseId);
      } else {
         this.entries.put(corpseId, new CorpseOpportunityLedger.Entry(new LinkedHashSet<>(eligiblePlayers), expiresAtTick));
      }
   }

   public boolean availableTo(UUID corpseId, UUID playerId, long currentTick) {
      CorpseOpportunityLedger.Entry entry = this.liveEntry(corpseId, currentTick);
      return entry != null && entry.remainingPlayers().contains(playerId);
   }

   public boolean consume(UUID corpseId, UUID playerId, long currentTick) {
      Objects.requireNonNull(playerId, "playerId");
      CorpseOpportunityLedger.Entry entry = this.liveEntry(corpseId, currentTick);
      if (entry != null && entry.remainingPlayers().remove(playerId)) {
         if (entry.remainingPlayers().isEmpty()) {
            this.entries.remove(corpseId);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean contains(UUID corpseId) {
      return this.entries.containsKey(corpseId);
   }

   public boolean discard(UUID corpseId) {
      Objects.requireNonNull(corpseId, "corpseId");
      return this.entries.remove(corpseId) != null;
   }

   public Set<UUID> expire(long currentTick) {
      LinkedHashSet<UUID> expired = new LinkedHashSet<>();
      this.entries.entrySet().removeIf(entry -> {
         boolean remove = currentTick >= entry.getValue().expiresAtTick();
         if (remove) {
            expired.add(entry.getKey());
         }

         return remove;
      });
      return Set.copyOf(expired);
   }

   public Set<UUID> removePlayer(UUID playerId) {
      Objects.requireNonNull(playerId, "playerId");
      LinkedHashSet<UUID> empty = new LinkedHashSet<>();
      this.entries.forEach((corpseIdx, entry) -> {
         entry.remainingPlayers().remove(playerId);
         if (entry.remainingPlayers().isEmpty()) {
            empty.add(corpseIdx);
         }
      });

      for (UUID corpseId : empty) {
         this.entries.remove(corpseId);
      }

      return Set.copyOf(empty);
   }

   private CorpseOpportunityLedger.Entry liveEntry(UUID corpseId, long currentTick) {
      Objects.requireNonNull(corpseId, "corpseId");
      CorpseOpportunityLedger.Entry entry = this.entries.get(corpseId);
      if (entry == null) {
         return null;
      } else {
         return currentTick < entry.expiresAtTick() ? entry : null;
      }
   }

   private record Entry(Set<UUID> remainingPlayers, long expiresAtTick) {
   }
}
