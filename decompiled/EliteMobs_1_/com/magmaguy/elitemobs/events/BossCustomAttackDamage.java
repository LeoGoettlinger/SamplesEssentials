package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BossCustomAttackDamage {
   public static double dealCustomDamage(LivingEntity damager, LivingEntity damagee, double damage) {
      if (damager == null || damagee == null) {
         return 0.0;
      } else if (damager.equals(damagee)) {
         return 0.0;
      } else if (damagee.isInvulnerable() || damagee.getHealth() <= 0.0) {
         return 0.0;
      } else if (damagee instanceof Player
         && !((Player)damagee).getGameMode().equals(GameMode.SURVIVAL)
         && !((Player)damagee).getGameMode().equals(GameMode.ADVENTURE)) {
         return 0.0;
      } else {
         CombatDamageContext.runEliteToPlayerBypass(() -> damagee.damage(damage, damager));
         NMSManager.getAdapter().setDamageCooldownTicks(damagee, 0);
         return damage;
      }
   }
}
