package com.magmaguy.elitemobs.skills;

import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public final class HealthPercentagePreserver {
   private HealthPercentagePreserver() {
   }

   public static void during(final Player player, final AttributeInstance maximumHealth, Runnable mutation) {
      during(new HealthPercentagePreserver.HealthPool() {
         @Override
         public double current() {
            return player.getHealth();
         }

         @Override
         public double maximum() {
            return maximumHealth.getValue();
         }

         @Override
         public boolean dead() {
            return player.isDead();
         }

         @Override
         public void setCurrent(double health) {
            player.setHealth(health);
         }
      }, mutation);
   }

   static void during(HealthPercentagePreserver.HealthPool healthPool, Runnable mutation) {
      double oldMaximum = positiveMaximum(healthPool.maximum());
      double oldHealth = healthPool.current();
      double healthRatio = Double.isFinite(oldHealth) ? Math.max(0.0, Math.min(1.0, oldHealth / oldMaximum)) : 0.0;
      mutation.run();
      if (!healthPool.dead()) {
         double newMaximum = positiveMaximum(healthPool.maximum());
         healthPool.setCurrent(Math.max(0.01, Math.min(newMaximum, newMaximum * healthRatio)));
      }
   }

   private static double positiveMaximum(double maximum) {
      return Double.isFinite(maximum) && maximum > 0.0 ? maximum : 1.0;
   }

   interface HealthPool {
      double current();

      double maximum();

      boolean dead();

      void setCurrent(double var1);
   }
}
