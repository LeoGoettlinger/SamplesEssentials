package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R5.mind;

import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.control.ControllerMove;
import net.minecraft.world.entity.ai.control.ControllerMove.Operation;

final class NativeMindFlyingMoveControl extends ControllerMove {
   private static final double ARRIVAL_DISTANCE_SQUARED = 2.5000003E-7;
   private final int maximumPitchTurn;
   private final boolean hoversInPlace;

   NativeMindFlyingMoveControl(EntityInsentient mob, int maximumPitchTurn, boolean hoversInPlace) {
      super(mob);
      this.maximumPitchTurn = maximumPitchTurn;
      this.hoversInPlace = hoversInPlace;
   }

   public void a() {
      if (this.k != Operation.b) {
         if (!this.hoversInPlace) {
            this.d.g(false);
         }

         this.d.H(0.0F);
         this.d.G(0.0F);
      } else {
         this.k = Operation.a;
         this.d.g(true);
         double x = this.e - this.d.dC();
         double y = this.f - this.d.dE();
         double z = this.g - this.d.dI();
         double distanceSquared = x * x + y * y + z * z;
         if (distanceSquared < 2.5000003E-7) {
            this.d.H(0.0F);
            this.d.G(0.0F);
         } else {
            float yaw = (float)(MathHelper.d(z, x) * 180.0F / (float)Math.PI) - 90.0F;
            this.d.v(this.b(this.d.dP(), yaw, 90.0F));
            double speedAttribute = this.d.aK()
               ? this.d.i(GenericAttributes.w)
               : (this.d.fg().b(GenericAttributes.m) ? this.d.i(GenericAttributes.m) : this.d.i(GenericAttributes.w));
            float speed = (float)(this.h * speedAttribute);
            this.d.B(speed);
            double horizontalDistance = Math.sqrt(x * x + z * z);
            if (Math.abs(y) > 1.0E-5 || Math.abs(horizontalDistance) > 1.0E-5) {
               float pitch = (float)(-(MathHelper.d(y, horizontalDistance) * 180.0F / (float)Math.PI));
               this.d.w(this.b(this.d.dR(), pitch, this.maximumPitchTurn));
               this.d.H(y > 0.0 ? speed : -speed);
            }
         }
      }
   }
}
