package me.chancesd.pvpmanager.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.chancesd.pvpmanager.manager.DependencyManager;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class MoveListener implements Listener {
   private final PlayerManager ph;
   private final DependencyManager depManager;
   private final Cache<UUID, Player> cache = CacheBuilder.newBuilder().weakValues().expireAfterWrite(1L, TimeUnit.SECONDS).build();
   private final double pushbackForce = Conf.PUSHBACK_FORCE.asDouble();

   public MoveListener(PlayerManager ph) {
      this.ph = ph;
      this.depManager = ph.getPlugin().getDependencyManager();
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public final void onPlayerMove(PlayerMoveEvent event) {
      Location locTo = event.getTo();
      Location locFrom = event.getFrom();
      if (locTo != null && (locFrom.getBlockX() != locTo.getBlockX() || locFrom.getBlockZ() != locTo.getBlockZ() || locFrom.getBlockY() != locTo.getBlockY())) {
         Player player = event.getPlayer();
         CombatPlayer pvplayer = this.ph.get(player);
         if (pvplayer.isInCombat()) {
            if (!this.depManager.canAttackAt(null, locTo) && this.depManager.canAttackAt(null, locFrom)) {
               Vector newVel = locFrom.toVector().subtract(locTo.toVector());
               newVel.setY(newVel.getY() + 0.1).normalize().multiply(this.pushbackForce);
               player.setVelocity(newVel);
               if (!this.cache.asMap().containsKey(event.getPlayer().getUniqueId())) {
                  pvplayer.message(Lang.PUSHBACK_WARNING, new Object[0]);
                  this.cache.put(player.getUniqueId(), player);
               }
            }
         }
      }
   }
}
