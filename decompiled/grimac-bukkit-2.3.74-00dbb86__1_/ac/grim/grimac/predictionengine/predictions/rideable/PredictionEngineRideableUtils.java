package ac.grim.grimac.predictionengine.predictions.rideable;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.predictionengine.predictions.PredictionEngineNormal;
import ac.grim.grimac.predictionengine.predictions.input.Input;
import ac.grim.grimac.predictionengine.predictions.input.InputTransformer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.utils.data.IntToObjectPair;
import ac.grim.grimac.utils.data.VectorData;
import ac.grim.grimac.utils.data.packetentity.JumpableEntity;
import ac.grim.grimac.utils.math.Vector3dm;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Generated;

public final class PredictionEngineRideableUtils {
   public static Set<VectorData> handleJumps(GrimPlayer player, Set<VectorData> possibleVectors) {
      if (player.compensatedEntities.self.getRiding() instanceof JumpableEntity jumpable) {
         jumpable.executeJump(player, possibleVectors);
         boolean legacyJumpingMechanics = player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_19_3);
         boolean onGround = legacyJumpingMechanics ? player.clientControlledVerticalCollision : player.lastOnGround;
         if (onGround) {
            if (legacyJumpingMechanics) {
               jumpable.setJumpPower(0.0F);
            }

            jumpable.setJumping(false);
         }

         return possibleVectors;
      } else {
         return possibleVectors;
      }
   }

   public static List<VectorData> applyInputsToVelocityPossibilities(Input movementVector, GrimPlayer player, Set<VectorData> possibleVectors, float speed) {
      List<VectorData> returnVectors = new ArrayList<>();
      InputTransformer<?> inputTransformer = InputTransformer.getTransformer(player);

      for (VectorData possibleLastTickOutput : possibleVectors) {
         for (int applyStuckSpeed = 1; applyStuckSpeed >= 0 && (applyStuckSpeed != 0 || !player.isForceStuckSpeed()); applyStuckSpeed--) {
            VectorData result = new VectorData(
               possibleLastTickOutput.vector.clone().add(inputTransformer.getMovementResultFromInput(player, movementVector, speed, player.yaw)),
               possibleLastTickOutput,
               VectorData.VectorType.InputResult
            );
            result.input = new Vector3dm(player.vehicleData.vehicleForward, 0.0F, player.vehicleData.vehicleHorizontal);
            Vector3dm vector = result.vector.clone();
            if (applyStuckSpeed != 0) {
               vector.multiply(player.stuckSpeedMultiplier);
            }

            result = result.returnNewModified(vector, VectorData.VectorType.StuckMultiplier);
            result = result.returnNewModified(new PredictionEngineNormal().handleOnClimbable(result.vector.clone(), player), VectorData.VectorType.Climbable);
            returnVectors.add(result);
            result = new VectorData(possibleLastTickOutput.vector.clone(), possibleLastTickOutput, VectorData.VectorType.InputResult);
            result.input = new Vector3dm(player.vehicleData.vehicleForward, 0.0F, player.vehicleData.vehicleHorizontal);
            vector = result.vector.clone();
            if (applyStuckSpeed != 0) {
               vector.multiply(player.stuckSpeedMultiplier);
            }

            result = result.returnNewModified(vector, VectorData.VectorType.StuckMultiplier);
            result = result.returnNewModified(new PredictionEngineNormal().handleOnClimbable(result.vector.clone(), player), VectorData.VectorType.Climbable);
            returnVectors.add(result);
         }
      }

      return returnVectors;
   }

   public static void applyPendingJumps(GrimPlayer player) {
      IntToObjectPair<JumpableEntity> pendingJump;
      while ((pendingJump = player.vehicleData.pendingJumps.poll()) != null) {
         JumpableEntity jumpable = pendingJump.second();
         if (jumpable.canPlayerJump(player)) {
            int jumpBoost = pendingJump.first();
            if (jumpBoost < 0) {
               jumpBoost = 0;
            }

            if (jumpBoost >= 90) {
               jumpable.setJumpPower(1.0F);
            } else {
               jumpable.setJumpPower(0.4F + 0.4F * jumpBoost / 90.0F);
            }
         }
      }
   }

   @Generated
   private PredictionEngineRideableUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
