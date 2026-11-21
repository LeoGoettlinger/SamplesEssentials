package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import java.util.Objects;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

public final class ClassAbilityDamage {
   public static final ClassAbilityDamage.Delivery NATIVE = (target, amount, source) -> Objects.requireNonNull(
         NMSManager.getAdapter(), "Class damage requires the native adapter"
      )
      .damageWithoutCooldown(target, amount, source);
   private final ClassAbilityDamage.Delivery delivery;

   ClassAbilityDamage(ClassAbilityDamage.Delivery delivery) {
      this.delivery = Objects.requireNonNull(delivery, "delivery");
   }

   double apply(Player caster, Projectile projectile, LivingEntity target, double amount, CombatDamageContext.ClassAbilityDamageDomain domain) {
      if (target != null && target.isValid() && !target.isDead() && !(target.getHealth() <= 0.0) && Double.isFinite(amount) && !(amount <= 0.0)) {
         double before = target.getHealth() + target.getAbsorptionAmount();
         Runnable hit = () -> CombatDamageContext.runClassAbilityDamage(
            domain, () -> this.delivery.damage(target, amount, (Entity)(projectile == null ? caster : projectile))
         );
         if (projectile == null) {
            hit.run();
         } else {
            CustomModel.runProjectileDamageBypass(hit);
         }

         if (!target.isValid()) {
            return Math.min(before, amount);
         } else {
            double after = Math.max(0.0, target.getHealth()) + Math.max(0.0, target.getAbsorptionAmount());
            return Math.max(0.0, before - after);
         }
      } else {
         return 0.0;
      }
   }

   @FunctionalInterface
   public interface Delivery {
      void damage(LivingEntity var1, double var2, Entity var4);
   }
}
