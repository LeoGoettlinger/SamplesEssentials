package me.chancesd.pvpmanager.tasks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.chancesd.pvpmanager.event.PlayerTagEvent;
import me.chancesd.pvpmanager.event.PlayerUntagEvent;
import me.chancesd.pvpmanager.manager.DependencyManager;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.setting.Lang;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class RegionCheckTask extends BukkitRunnable implements Listener {
   private final PlayerManager playerHandler;
   private final Map<CombatPlayer, Location> lastLocations = new ConcurrentHashMap<>();
   private final DependencyManager dependencyManager;

   public RegionCheckTask(PlayerManager playerHandler, DependencyManager dependencyManager) {
      this.playerHandler = playerHandler;
      this.dependencyManager = dependencyManager;
   }

   public void run() {
      for (CombatPlayer combatPlayer : this.playerHandler.getPlayersInCombat()) {
         Player player = combatPlayer.getPlayer();
         Location playerLocation = player.getLocation();
         if (!this.dependencyManager.canAttackAt(player, playerLocation)) {
            Location lastLocation = this.lastLocations.get(combatPlayer);
            if (lastLocation != null) {
               lastLocation.setPitch(playerLocation.getPitch());
               lastLocation.setYaw(playerLocation.getYaw());
               combatPlayer.getExemptions().setCanBypassTeleportBlock(true);
               ScheduleUtils.teleport(player, lastLocation, "Failed to teleport player out of non-PvP region")
                  .thenAccept(success -> combatPlayer.getExemptions().setCanBypassTeleportBlock(false));
               Log.debug("Tried to teleport " + player.getName() + " out of a safezone");
               combatPlayer.message(Lang.PUSHBACK_WARNING, new Object[0]);
            }
         } else {
            this.lastLocations.put(combatPlayer, playerLocation);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerTag(PlayerTagEvent event) {
      Location location = event.getPlayer().getLocation();
      if (this.dependencyManager.canAttackAt(event.getPlayer(), location)) {
         this.lastLocations.put(event.getCombatPlayer(), location);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerTag(PlayerUntagEvent event) {
      this.lastLocations.remove(event.getCombatPlayer());
   }
}
