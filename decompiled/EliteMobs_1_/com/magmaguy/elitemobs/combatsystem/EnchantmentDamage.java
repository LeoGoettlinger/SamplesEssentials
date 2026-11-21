package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.MetadataHandler;
import java.util.Map;
import java.util.UUID;

public final class EnchantmentDamage {
   private EnchantmentDamage() {
   }

   public static void apply(UUID attackId, Map<String, Object> providerFacts, Runnable application) {
      if (MetadataHandler.PLUGIN != null && MetadataHandler.PLUGIN.isEnabled()) {
         if (providerFacts.get("elitemobs") instanceof Map<?, ?> facts
            && facts.get("loud_strikes") instanceof Number bonus
            && Double.isFinite(bonus.doubleValue())
            && !(bonus.doubleValue() < 0.0)) {
            double threatBonus = bonus.doubleValue();
            CombatDamageContext.PlayerDamageSource source = new CombatDamageContext.PlayerDamageSource(attackId, null, false, threatBonus);
            CombatDamageContext.runPlayerToEliteBypass(source, application);
         } else {
            throw new IllegalArgumentException("Missing captured EM combat facts");
         }
      } else {
         throw new IllegalStateException("EliteMobs damage owner is unavailable");
      }
   }
}
