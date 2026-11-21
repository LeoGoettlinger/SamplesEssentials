package me.chancesd.pvpmanager.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.chancesd.pvpmanager.manager.DependencyManager;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.tasks.RegionCheckTask;
import me.chancesd.pvpmanager.utils.CombatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class MoveListener1_9 implements Listener {
   private final PlayerManager playerManager;
   private final DependencyManager depManager;
   private final Cache<UUID, Player> cache = CacheBuilder.newBuilder().weakValues().expireAfterWrite(1L, TimeUnit.SECONDS).build();
   private final double pushbackForce;

   public MoveListener1_9(PlayerManager playerManager, DependencyManager depManager) {
      this.playerManager = playerManager;
      this.depManager = depManager;
      double force = Conf.PUSHBACK_FORCE.asDouble();
      if (force > 4.0) {
         Log.infoColor(ChatColor.RED + "Pushback force too high, setting it to 4");
         this.pushbackForce = 4.0;
      } else if (force < 0.1) {
         Log.infoColor(ChatColor.RED + "Pushback force too low, setting it to 0.1");
         this.pushbackForce = 0.1;
      } else {
         this.pushbackForce = force;
      }

      RegionCheckTask regionCheckTask = new RegionCheckTask(playerManager, depManager);
      Bukkit.getPluginManager().registerEvents(regionCheckTask, playerManager.getPlugin());
      ScheduleUtils.runPlatformAsyncTimer(regionCheckTask, 20L, 20L);
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public final void onPlayerMove(PlayerMoveEvent event) {
      Location locTo = event.getTo();
      Location locFrom = event.getFrom();
      if (locTo != null && (locFrom.getBlockX() != locTo.getBlockX() || locFrom.getBlockZ() != locTo.getBlockZ() || locFrom.getBlockY() != locTo.getBlockY())) {
         Player player = event.getPlayer();
         CombatPlayer combatPlayer = this.playerManager.getUnchecked(player);
         if (combatPlayer != null && combatPlayer.isInCombat()) {
            if (!this.depManager.canAttackAt(null, locTo) && this.depManager.canAttackAt(null, locFrom)) {
               Vector newVel = locFrom.toVector().subtract(locTo.toVector());
               newVel.setY(0).normalize().multiply(this.pushbackForce).setY(0.5);
               CombatUtils.checkGlide(combatPlayer);
               player.setVelocity(this.sanitizeVector(newVel));
               if (!this.cache.asMap().containsKey(player.getUniqueId())) {
                  combatPlayer.message(Lang.PUSHBACK_WARNING, new Object[0]);
                  player.getWorld().playEffect(player.getEyeLocation(), Effect.SMOKE, 3);
                  this.cache.put(player.getUniqueId(), player);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public final void onPlayerTeleport(PlayerTeleportEvent event) {
      if (event.getCause() == TeleportCause.ENDER_PEARL) {
         Player player = event.getPlayer();
         CombatPlayer combatPlayer = this.playerManager.getUnchecked(player);
         Location locTo = event.getTo();
         Location locFrom = event.getFrom();
         if (combatPlayer != null && combatPlayer.isInCombat() && !this.depManager.canAttackAt(null, locTo) && this.depManager.canAttackAt(null, locFrom)) {
            event.setCancelled(true);
            combatPlayer.message(Lang.PUSHBACK_WARNING, new Object[0]);
         }
      }
   }

   @NotNull
   private Vector sanitizeVector(@NotNull Vector vel) {
      if (Double.isNaN(vel.getX())) {
         vel.setX(0);
      }

      if (Double.isNaN(vel.getZ())) {
         vel.setZ(0);
      }

      return vel;
   }
}
