package com.magmaguy.elitemobs.easyminecraftgoals.v26.mind;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.MoveControl.Operation;

final class NativeMindFlyingMoveControl extends MoveControl {
   private static final double ARRIVAL_DISTANCE_SQUARED = 2.5000003E-7;
   private final int maximumPitchTurn;
   private final boolean hoversInPlace;

   NativeMindFlyingMoveControl(Mob mob, int maximumPitchTurn, boolean hoversInPlace) {
      super(mob);
      this.maximumPitchTurn = maximumPitchTurn;
      this.hoversInPlace = hoversInPlace;
   }

   public void tick() {
      if (this.operation != Operation.MOVE_TO) {
         if (!this.hoversInPlace) {
            this.mob.setNoGravity(false);
         }

         this.mob.setYya(0.0F);
         this.mob.setZza(0.0F);
      } else {
         this.operation = Operation.WAIT;
         this.mob.setNoGravity(true);
         double x = this.wantedX - this.mob.getX();
         double y = this.wantedY - this.mob.getY();
         double z = this.wantedZ - this.mob.getZ();
         double distanceSquared = x * x + y * y + z * z;
         if (distanceSquared < 2.5000003E-7) {
            this.mob.setYya(0.0F);
            this.mob.setZza(0.0F);
         } else {
            float yaw = (float)(Mth.atan2(z, x) * 180.0F / (float)Math.PI) - 90.0F;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), yaw, 90.0F));
            double speedAttribute = this.mob.onGround()
               ? this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)
               : (
                  this.mob.getAttributes().hasAttribute(Attributes.FLYING_SPEED)
                     ? this.mob.getAttributeValue(Attributes.FLYING_SPEED)
                     : this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)
               );
            float speed = (float)(this.speedModifier * speedAttribute);
            this.mob.setSpeed(speed);
            double horizontalDistance = Math.sqrt(x * x + z * z);
            if (Math.abs(y) > 1.0E-5 || Math.abs(horizontalDistance) > 1.0E-5) {
               float pitch = (float)(-(Mth.atan2(y, horizontalDistance) * 180.0F / (float)Math.PI));
               this.mob.setXRot(this.rotlerp(this.mob.getXRot(), pitch, this.maximumPitchTurn));
               this.mob.setYya(y > 0.0 ? speed : -speed);
            }
         }
      }
   }
}
