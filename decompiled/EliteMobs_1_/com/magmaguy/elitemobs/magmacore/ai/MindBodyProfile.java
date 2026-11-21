package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record MindBodyProfile(MindBodyLocomotion locomotion, double uniformScale, boolean entityCollidable, String carrierType) {
   public static final String DEFAULT_CARRIER_TYPE = "minecraft:zombie";
   private static final Pattern CARRIER_KEY = Pattern.compile("[a-z0-9._-]+:[a-z0-9/._-]+");
   public static final MindBodyProfile GROUNDED = new MindBodyProfile(MindBodyLocomotion.GROUNDED, 1.0, true, "minecraft:zombie");

   public MindBodyProfile(MindBodyLocomotion locomotion, double uniformScale, boolean entityCollidable) {
      this(locomotion, uniformScale, entityCollidable, "minecraft:zombie");
   }

   public MindBodyProfile(MindBodyLocomotion locomotion, double uniformScale, boolean entityCollidable, String carrierType) {
      Objects.requireNonNull(locomotion, "locomotion");
      if (Double.isFinite(uniformScale) && !(uniformScale <= 0.0)) {
         carrierType = Objects.requireNonNull(carrierType, "carrierType").toLowerCase(Locale.ROOT);
         if (!CARRIER_KEY.matcher(carrierType).matches()) {
            throw new IllegalArgumentException("carrierType must be a namespaced Minecraft entity key");
         } else {
            this.locomotion = locomotion;
            this.uniformScale = uniformScale;
            this.entityCollidable = entityCollidable;
            this.carrierType = carrierType;
         }
      } else {
         throw new IllegalArgumentException("uniformScale must be finite and positive");
      }
   }

   public static MindBodyProfile standard(MindBodyLocomotion locomotion) {
      return new MindBodyProfile(locomotion, 1.0, true, "minecraft:zombie");
   }

   public static MindBodyProfile forCarrier(String carrierType, MindBodyLocomotion locomotion) {
      return new MindBodyProfile(locomotion, 1.0, true, carrierType);
   }
}
