package com.magmaguy.elitemobs.api.mind;

import java.util.Objects;
import org.bukkit.NamespacedKey;

public record EliteMindBodyProfile(EliteMindBodyLocomotion locomotion, double uniformScale, boolean entityCollidable, NamespacedKey carrierType) {
   public static final NamespacedKey DEFAULT_CARRIER_TYPE = NamespacedKey.minecraft("zombie");
   public static final EliteMindBodyProfile GROUNDED = new EliteMindBodyProfile(EliteMindBodyLocomotion.GROUNDED, 1.0, true, DEFAULT_CARRIER_TYPE);

   public EliteMindBodyProfile(EliteMindBodyLocomotion locomotion, double uniformScale, boolean entityCollidable) {
      this(locomotion, uniformScale, entityCollidable, DEFAULT_CARRIER_TYPE);
   }

   public EliteMindBodyProfile(EliteMindBodyLocomotion locomotion, double uniformScale, boolean entityCollidable, NamespacedKey carrierType) {
      Objects.requireNonNull(locomotion, "locomotion");
      if (Double.isFinite(uniformScale) && !(uniformScale <= 0.0)) {
         Objects.requireNonNull(carrierType, "carrierType");
         this.locomotion = locomotion;
         this.uniformScale = uniformScale;
         this.entityCollidable = entityCollidable;
         this.carrierType = carrierType;
      } else {
         throw new IllegalArgumentException("uniformScale must be finite and positive");
      }
   }

   public static EliteMindBodyProfile standard(EliteMindBodyLocomotion locomotion) {
      return new EliteMindBodyProfile(locomotion, 1.0, true, DEFAULT_CARRIER_TYPE);
   }

   public static EliteMindBodyProfile forCarrier(NamespacedKey carrierType, EliteMindBodyLocomotion locomotion) {
      return new EliteMindBodyProfile(locomotion, 1.0, true, carrierType);
   }
}
