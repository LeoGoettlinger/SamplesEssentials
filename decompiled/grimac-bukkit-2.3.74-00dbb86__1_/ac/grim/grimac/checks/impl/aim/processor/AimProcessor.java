package ac.grim.grimac.checks.impl.aim.processor;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;
import ac.grim.grimac.utils.data.Pair;
import ac.grim.grimac.utils.lists.RunningMode;
import ac.grim.grimac.utils.math.GrimMath;

public class AimProcessor extends Check implements RotationCheck {
   private static final int SIGNIFICANT_SAMPLES_THRESHOLD = 15;
   private static final int TOTAL_SAMPLES_THRESHOLD = 80;
   public double sensitivityX;
   public double sensitivityY;
   public double divisorX;
   public double divisorY;
   public double modeX;
   public double modeY;
   public double deltaDotsX;
   public double deltaDotsY;
   private final RunningMode xRotMode = new RunningMode(80);
   private final RunningMode yRotMode = new RunningMode(80);
   private float lastXRot;
   private float lastYRot;

   public AimProcessor(GrimPlayer playerData) {
      super(playerData);
   }

   public static double convertToSensitivity(double var13) {
      double var11 = var13 / 0.15F / 8.0;
      double var9 = Math.cbrt(var11);
      return (var9 - 0.2F) / 0.6F;
   }

   @Override
   public void process(RotationUpdate rotationUpdate) {
      rotationUpdate.setProcessor(this);
      float deltaXRot = rotationUpdate.getDeltaXRotABS();
      this.divisorX = GrimMath.gcd(deltaXRot, this.lastXRot);
      if (deltaXRot > 0.0F && deltaXRot < 5.0F && this.divisorX > GrimMath.MINIMUM_DIVISOR) {
         this.xRotMode.add(this.divisorX);
         this.lastXRot = deltaXRot;
      }

      float deltaYRot = rotationUpdate.getDeltaYRotABS();
      this.divisorY = GrimMath.gcd(deltaYRot, this.lastYRot);
      if (deltaYRot > 0.0F && deltaYRot < 5.0F && this.divisorY > GrimMath.MINIMUM_DIVISOR) {
         this.yRotMode.add(this.divisorY);
         this.lastYRot = deltaYRot;
      }

      if (this.xRotMode.size() > 15) {
         Pair<Double, Integer> modeX = this.xRotMode.getMode();
         if (modeX.second() > 15) {
            this.modeX = modeX.first();
            this.sensitivityX = convertToSensitivity(this.modeX);
         }
      }

      if (this.yRotMode.size() > 15) {
         Pair<Double, Integer> modeY = this.yRotMode.getMode();
         if (modeY.second() > 15) {
            this.modeY = modeY.first();
            this.sensitivityY = convertToSensitivity(this.modeY);
         }
      }

      this.deltaDotsX = deltaXRot / this.modeX;
      this.deltaDotsY = deltaYRot / this.modeY;
   }
}
