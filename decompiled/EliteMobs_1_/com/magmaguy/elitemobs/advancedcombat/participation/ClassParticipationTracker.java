package com.magmaguy.elitemobs.advancedcombat.participation;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.parties.Party;
import com.magmaguy.elitemobs.parties.PartyManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class ClassParticipationTracker {
   private static final double SUPPORT_ENCOUNTER_RADIUS_SQUARED = 1600.0;
   private final Map<UUID, Set<UUID>> participantsByElite = new HashMap<>();

   public void recordThreat(Player player, Collection<EliteEntity> elites) {
      for (EliteEntity elite : elites) {
         this.record(elite, player.getUniqueId());
      }
   }

   public void recordSupport(Player player) {
      Set<UUID> supportedPlayers = partyMembers(player);
      Location origin = player.getLocation();

      for (Entity entity : origin.getWorld().getNearbyEntities(origin, 40.0, 40.0, 40.0)) {
         EliteEntity elite = EntityTracker.getEliteMobEntity(entity);
         if (elite != null
            && elite.getLivingEntity() != null
            && elite.getLivingEntity().isValid()
            && elite.getLivingEntity().getWorld().equals(player.getWorld())
            && !(elite.getLivingEntity().getLocation().distanceSquared(origin) > 1600.0)) {
            boolean engaged = elite.getDamagers().keySet().stream().<UUID>map(OfflinePlayer::getUniqueId).anyMatch(supportedPlayers::contains)
               || elite.getAggro().keySet().stream().<UUID>map(OfflinePlayer::getUniqueId).anyMatch(supportedPlayers::contains);
            if (engaged) {
               this.record(elite, player.getUniqueId());
            }
         }
      }
   }

   public Set<Player> participants(EliteEntity elite) {
      Set<UUID> identities = new HashSet<>(this.participantsByElite.getOrDefault(elite.getEliteUUID(), Set.of()));

      for (Player contributor : elite.getDamagers().keySet()) {
         identities.add(contributor.getUniqueId());
      }

      LinkedHashSet<Player> players = new LinkedHashSet<>();

      for (UUID playerId : identities) {
         Player player = Bukkit.getPlayer(playerId);
         if (player != null && player.isOnline() && player.isValid()) {
            players.add(player);
         }
      }

      return Set.copyOf(players);
   }

   public void clear(EliteEntity elite) {
      this.participantsByElite.remove(elite.getEliteUUID());
   }

   public void discard(UUID playerId) {
      this.participantsByElite.values().removeIf(players -> {
         players.remove(playerId);
         return players.isEmpty();
      });
   }

   public void clearAll() {
      this.participantsByElite.clear();
   }

   private void record(EliteEntity elite, UUID playerId) {
      if (elite != null && elite.getLivingEntity() != null) {
         this.participantsByElite.computeIfAbsent(elite.getEliteUUID(), ignored -> new HashSet<>()).add(playerId);
      }
   }

   private static Set<UUID> partyMembers(Player player) {
      Party party = PartyManager.getParty(player.getUniqueId());
      return party == null ? Set.of(player.getUniqueId()) : party.getMembers();
   }
}
