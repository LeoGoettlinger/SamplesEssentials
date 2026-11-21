package me.chancesd.pvpmanager.listener;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.ProtectionResult;
import me.chancesd.pvpmanager.sdutils.utils.Utils;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.utils.CombatUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;

public class EntityListener1_20_4 implements Listener {
   private final PlayerManager ph;

   public EntityListener1_20_4(PlayerManager ph) {
      this.ph = ph;
      this.registerKnockBackEvent();
   }

   private void registerKnockBackEvent() {
      if (Utils.isPaper()) {
         Utils.registerEvent(
            this.ph.getPlugin(), this, EntityKnockbackByEntityEvent.class, event -> this.onPlayerKnockback(event, event.getHitBy(), event.getEntity())
         );
      } else {
         Utils.registerEvent(
            this.ph.getPlugin(),
            this,
            org.bukkit.event.entity.EntityKnockbackByEntityEvent.class,
            event -> this.onPlayerKnockback(event, event.getSourceEntity(), event.getEntity())
         );
      }
   }

   private void onPlayerKnockback(Cancellable event, Entity entityAttacker, Entity entityDefender) {
      if (!CombatUtils.isWorldExcluded(entityDefender.getWorld().getName())) {
         Player attacker = null;
         Player attacked = null;
         if (entityDefender instanceof Player defender) {
            attacked = defender;
            if (entityAttacker instanceof Player playerAttacker) {
               attacker = playerAttacker;
            }

            if (entityAttacker instanceof Projectile proj && proj.getShooter() instanceof Player player) {
               attacker = player;
            }
         }

         if (attacked != null && attacker != null && !attacker.equals(attacked)) {
            ProtectionResult result = this.ph.checkProtection(attacker, attacked);
            if (result.isProtected()) {
               event.setCancelled(true);
               Lang.messageProtection(result, attacker, attacked);
            }
         }
      }
   }
}
