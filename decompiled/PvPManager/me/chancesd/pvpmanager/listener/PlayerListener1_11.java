package me.chancesd.pvpmanager.listener;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.ItemCooldown;
import me.chancesd.pvpmanager.setting.ItemKey;
import me.chancesd.pvpmanager.setting.Lang;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

public class PlayerListener1_11 implements Listener {
   private final PlayerManager playerManager;

   public PlayerListener1_11(PlayerManager ph) {
      this.playerManager = ph;
   }

   @EventHandler
   public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
      Projectile entity = event.getEntity();
      ProjectileSource shooter = entity.getShooter();
      ItemKey key = ItemKey.wildcard(Material.ENDER_PEARL);
      ItemCooldown itemCooldown = Conf.getItemCooldown(key);
      if (itemCooldown != null && entity.getType() == EntityType.ENDER_PEARL && shooter instanceof Player player) {
         CombatPlayer combatPlayer = this.playerManager.getUnchecked(player);
         if (combatPlayer != null) {
            ScheduleUtils.runPlatformTask(() -> combatPlayer.setItemCooldown(key, itemCooldown), player);
         }
      }
   }

   @EventHandler
   public void onEntityRessurrect(EntityResurrectEvent event) {
      if (Conf.BLOCK_TOTEM_UNDYING.asBool() && !event.isCancelled() && event.getEntityType() == EntityType.PLAYER) {
         Player player = (Player)event.getEntity();
         CombatPlayer pvPlayer = this.playerManager.get(player);
         if (pvPlayer.isInCombat()) {
            event.setCancelled(true);
            pvPlayer.message(Lang.TOTEM_BLOCKED_IN_COMBAT, new Object[0]);
         }
      }
   }
}
