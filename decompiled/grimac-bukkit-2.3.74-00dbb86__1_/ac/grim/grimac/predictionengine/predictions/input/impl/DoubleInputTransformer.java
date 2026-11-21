package ac.grim.grimac.predictionengine.predictions.input.impl;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.predictionengine.predictions.input.DoubleInput;
import ac.grim.grimac.predictionengine.predictions.input.Input;
import ac.grim.grimac.predictionengine.predictions.input.InputTransformer;
import ac.grim.grimac.utils.math.GrimMath;
import ac.grim.grimac.utils.math.Vector3dm;
import ac.grim.grimac.utils.math.VectorUtils;

public class DoubleInputTransformer implements InputTransformer<DoubleInput> {
   public DoubleInput transformInputsToVector(GrimPlayer player, int sideways, int vertical, int forward) {
      float bestPossibleX;
      float bestPossibleZ;
      if (player.isSlowMovement) {
         bestPossibleX = sideways * player.sneakingSpeedMultiplier;
         bestPossibleZ = forward * player.sneakingSpeedMultiplier;
      } else {
         bestPossibleX = Math.min(Math.max(-1.0F, (float)Math.round((float)sideways)), 1.0F);
         bestPossibleZ = Math.min(Math.max(-1.0F, (float)Math.round((float)forward)), 1.0F);
      }

      if (player.packetStateData.isSlowedByUsingItem()) {
         bestPossibleX *= 0.2F;
         bestPossibleZ *= 0.2F;
      }

      Vector3dm inputVector = new Vector3dm(bestPossibleX, 0.0F, bestPossibleZ);
      inputVector.multiply(0.98F);
      inputVector = new Vector3dm((float)inputVector.getX(), (float)inputVector.getY(), (float)inputVector.getZ());
      if (inputVector.lengthSquared() > 1.0) {
         double d0 = VectorUtils.getVanillaLength(player.getClientVersion(), inputVector);
         inputVector = new Vector3dm(inputVector.getX() / d0, inputVector.getY() / d0, inputVector.getZ() / d0);
      }

      return new DoubleInput(inputVector.getX(), inputVector.getY(), inputVector.getZ());
   }

   @Override
   public Vector3dm getMovementResultFromInput(GrimPlayer player, Input inputVector, float speed, float yaw) {
      if (inputVector instanceof DoubleInput input) {
         float yawRadians = GrimMath.radians(yaw);
         float sin = player.trigHandler.sin(yawRadians);
         float cos = player.trigHandler.cos(yawRadians);
         double xResult = input.sideways() * cos - input.forward() * sin;
         double zResult = input.forward() * cos + input.sideways() * sin;
         return new Vector3dm(xResult * speed, input.vertical() * speed, zResult * speed);
      } else {
         throw new IllegalStateException("Expected input vector of type DoubleInput, but got " + inputVector.getClass().getSimpleName());
      }
   }
}
