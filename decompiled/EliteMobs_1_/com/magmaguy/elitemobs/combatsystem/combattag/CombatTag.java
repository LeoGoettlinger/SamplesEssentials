package com.magmaguy.elitemobs.combatsystem.combattag;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.utils.GameClock;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class CombatTag implements Listener {
   private static final int SAFETY_TICKS = 1200;
   private static final Map<UUID, CombatTag.FlightSafety> flights = new HashMap<>();

   @EventHandler(
      ignoreCancelled = true
   )
   public void onDamage(EntityDamageByEntityEvent event) {
      Player player = CombatParticipantResolver.resolveEliteCombatPlayer(event);
      if (player != null && player.getGameMode() != GameMode.CREATIVE && player.isFlying()) {
         player.setFlying(false);
         ActionBarCompositor.show(player, ActionBarCompositor.Source.COMBAT_TRANSITION, CombatTagConfig.getCombatTagMessage());
         clearFlightSafetyEffect(player);
         GameClock.initialize();
         CombatTag.FlightSafety flight = new CombatTag.FlightSafety(player);
         if (player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 1200, 0, true, true, true))) {
            flights.put(player.getUniqueId(), flight);

            try {
               flight.task = player.getServer()
                  .getScheduler()
                  .runTaskTimer(
                     MetadataHandler.PLUGIN,
                     () -> {
                        if (!player.isOnline()
                           || player.isDead()
                           || player.isOnGround()
                           || !player.getWorld().getUID().equals(flight.worldId)
                           || GameClock.getCurrentTick() - flight.started >= 1200L) {
                           flight.close();
                        }
                     },
                     1L,
                     1L
                  );
            } catch (RuntimeException var5) {
               flight.close();
               throw var5;
            }
         }
      }
   }

   static void clearFlightSafetyEffect(Player player) {
      CombatTag.FlightSafety flight = flights.get(player.getUniqueId());
      if (flight != null) {
         flight.close();
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      clearFlightSafetyEffect(event.getPlayer());
   }

   @EventHandler
   public void onDeath(PlayerDeathEvent event) {
      clearFlightSafetyEffect(event.getEntity());
   }

   @EventHandler
   public void onWorldChange(PlayerChangedWorldEvent event) {
      clearFlightSafetyEffect(event.getPlayer());
   }

   public static void shutdown() {
      for (CombatTag.FlightSafety flight : flights.values().toArray(CombatTag.FlightSafety[]::new)) {
         flight.close();
      }
   }

   private static final class FlightSafety {
      final Player player;
      final UUID worldId;
      final long started;
      final PotionEffect previous;
      BukkitTask task;

      FlightSafety(Player player) {
         this.player = player;
         this.worldId = player.getWorld().getUID();
         this.started = GameClock.getCurrentTick();
         this.previous = player.getPotionEffect(PotionEffectType.SLOW_FALLING);
      }

      void close() {
         if (CombatTag.flights.remove(this.player.getUniqueId(), this)) {
            if (this.task != null) {
               this.task.cancel();
            }

            long elapsed = Math.max(0L, GameClock.getCurrentTick() - this.started);
            long expected = Math.max(0L, 1200L - elapsed);
            PotionEffect current = this.player.getPotionEffect(PotionEffectType.SLOW_FALLING);
            if (expected != 0L
               && current != null
               && current.getAmplifier() == 0
               && current.isAmbient()
               && current.hasParticles()
               && current.hasIcon()
               && Math.abs(current.getDuration() - expected) <= 1L) {
               this.player.removePotionEffect(PotionEffectType.SLOW_FALLING);
               if (this.previous != null && !this.player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
                  int remaining = this.previous.isInfinite() ? -1 : (int)Math.max(0L, this.previous.getDuration() - elapsed);
                  if (remaining != 0) {
                     this.player
                        .addPotionEffect(
                           new PotionEffect(
                              PotionEffectType.SLOW_FALLING,
                              remaining,
                              this.previous.getAmplifier(),
                              this.previous.isAmbient(),
                              this.previous.hasParticles(),
                              this.previous.hasIcon()
                           )
                        );
                  }
               }
            }
         }
      }
   }
}
