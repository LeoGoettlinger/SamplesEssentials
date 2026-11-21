package dev.demora.demoralegendaryweapons.weapons.withersickles;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class SickleCooldownManager {
   private final DemoraLegendaryWeapons plugin;
   private final Map<UUID, Long> throwCooldowns = new HashMap<>();

   public SickleCooldownManager(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
   }

   public boolean isOnCooldown(Player player) {
      Long end = this.throwCooldowns.get(player.getUniqueId());
      if (end == null) {
         return false;
      } else if (System.currentTimeMillis() >= end) {
         this.throwCooldowns.remove(player.getUniqueId());
         return false;
      } else {
         return true;
      }
   }

   public long getRemainingCooldown(Player player) {
      Long end = this.throwCooldowns.get(player.getUniqueId());
      if (end == null) {
         return 0L;
      } else {
         long diff = end - System.currentTimeMillis();
         if (diff <= 0L) {
            this.throwCooldowns.remove(player.getUniqueId());
            return 0L;
         } else {
            return (long)Math.ceil(diff / 1000.0);
         }
      }
   }

   public void setCooldown(Player player) {
      long cooldownTime = this.plugin.getConfig().getInt("weapons.wither_sickles.throw_cooldown", 10) * 1000L;
      this.throwCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownTime);
   }

   public long getCooldownEndTime(Player player) {
      Long end = this.throwCooldowns.get(player.getUniqueId());
      if (end == null) {
         long cooldownTime = this.plugin.getConfig().getInt("weapons.wither_sickles.throw_cooldown", 10) * 1000L;
         end = System.currentTimeMillis() + cooldownTime;
         this.throwCooldowns.put(player.getUniqueId(), end);
      }

      return end;
   }

   public void removePlayerData(UUID playerId) {
      this.throwCooldowns.remove(playerId);
   }
}
