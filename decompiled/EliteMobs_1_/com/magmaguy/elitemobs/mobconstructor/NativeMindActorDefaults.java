package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import java.util.Objects;
import org.bukkit.entity.EntityType;

final class NativeMindActorDefaults {
   private static final double LEGACY_BASE_HEALTH = 20.0;

   private NativeMindActorDefaults() {
   }

   static String nameTemplate(EntityType entityType) {
      Objects.requireNonNull(entityType, "entityType");
      return MobPropertiesConfig.defaultNameTemplate(entityType);
   }

   static double baseHealth(EliteMobProperties properties, double nativeBaseHealth) {
      if (properties != null && Double.isFinite(properties.getDefaultMaxHealth()) && properties.getDefaultMaxHealth() > 0.0) {
         return properties.getDefaultMaxHealth();
      } else {
         return Double.isFinite(nativeBaseHealth) && nativeBaseHealth > 0.0 ? nativeBaseHealth : 20.0;
      }
   }

   static void requireRandomizedPowerSupport(EntityType entityType, EliteMobProperties properties, boolean randomized) {
      if (randomized && properties == null) {
         throw new IllegalArgumentException("No configured elite properties for carrier " + entityType);
      }
   }
}
