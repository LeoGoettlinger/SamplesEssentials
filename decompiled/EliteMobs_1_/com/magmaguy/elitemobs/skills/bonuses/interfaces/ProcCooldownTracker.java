package com.magmaguy.elitemobs.skills.bonuses.interfaces;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public final class ProcCooldownTracker {
   private static final Map<UUID, Map<String, Long>> LAST_PROC_MILLIS = new ConcurrentHashMap<>();

   private ProcCooldownTracker() {
   }

   public static boolean tryConsume(Player player, String skillId, long cooldown) {
      if (cooldown <= 0L) {
         return true;
      } else {
         long now = System.currentTimeMillis();
         Map<String, Long> skills = LAST_PROC_MILLIS.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
         Long last = skills.get(skillId);
         if (last != null && now - last < cooldown) {
            return false;
         } else {
            skills.put(skillId, now);
            return true;
         }
      }
   }

   public static void clearAll() {
      LAST_PROC_MILLIS.clear();
   }

   public static void clear(Player player) {
      LAST_PROC_MILLIS.remove(player.getUniqueId());
   }
}
