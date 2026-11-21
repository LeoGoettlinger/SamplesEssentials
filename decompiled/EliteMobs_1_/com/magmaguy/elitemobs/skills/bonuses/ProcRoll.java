package com.magmaguy.elitemobs.skills.bonuses;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.Player;

public final class ProcRoll {
   private static final Map<UUID, Map<String, Boolean>> forced = new ConcurrentHashMap<>();

   private ProcRoll() {
   }

   public static boolean rolls(Player player, String skillId, double chance) {
      Map<String, Boolean> outcomes = forced.get(player.getUniqueId());
      Boolean outcome = outcomes == null ? null : outcomes.get(skillId);
      return outcome != null ? outcome : ThreadLocalRandom.current().nextDouble() < chance;
   }

   public static void force(UUID playerId, String skillId, Boolean outcome) {
      if (outcome == null) {
         forced.computeIfPresent(playerId, (id, outcomes) -> {
            outcomes.remove(skillId);
            return outcomes.isEmpty() ? null : outcomes;
         });
      } else {
         forced.computeIfAbsent(playerId, id -> new ConcurrentHashMap<>()).put(skillId, outcome);
      }
   }

   public static void clear(UUID playerId) {
      forced.remove(playerId);
   }
}
