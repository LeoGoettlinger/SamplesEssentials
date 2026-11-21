package dev.demora.demoralegendaryweapons.weapons.ravagerhorn;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Ravager;

public final class RideStateCache {
   private final DemoraLegendaryWeapons plugin;
   private final Map<UUID, Ravager> mountByPlayer = new HashMap<>();
   private final Map<UUID, Long> cooldownEnd = new HashMap<>();
   private final Map<UUID, Long> rideStartAt = new HashMap<>();
   private final Map<UUID, Long> lastHitAt = new HashMap<>();

   public RideStateCache(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
   }

   public void putMount(UUID playerId, Ravager ravager) {
      this.mountByPlayer.put(playerId, ravager);
      this.rideStartAt.put(playerId, System.currentTimeMillis());
   }

   public Ravager getMount(UUID playerId) {
      return this.mountByPlayer.get(playerId);
   }

   public void removeRider(UUID playerId) {
      this.mountByPlayer.remove(playerId);
      this.rideStartAt.remove(playerId);
   }

   public Long getRideStart(UUID playerId) {
      return this.rideStartAt.get(playerId);
   }

   public void recordHit(UUID entityId, long time) {
      this.lastHitAt.put(entityId, time);
   }

   public boolean hasRecentHit(UUID entityId) {
      return this.lastHitAt.containsKey(entityId);
   }

   public Long getLastHitAt(UUID entityId) {
      return this.lastHitAt.get(entityId);
   }

   public void startCooldown(UUID playerId) {
      long sec = this.plugin.getConfig().getLong("weapons.ravager_horn.cooldown_seconds", 40L);
      if (sec <= 0L) {
         this.cooldownEnd.remove(playerId);
      } else {
         this.cooldownEnd.put(playerId, System.currentTimeMillis() + sec * 1000L);
      }
   }

   public long getRemainingCooldownSeconds(UUID playerId) {
      Long end = this.cooldownEnd.get(playerId);
      if (end == null) {
         return 0L;
      } else {
         long rem = end - System.currentTimeMillis();
         if (rem <= 0L) {
            this.cooldownEnd.remove(playerId);
            return 0L;
         } else {
            return rem / 1000L;
         }
      }
   }
}
