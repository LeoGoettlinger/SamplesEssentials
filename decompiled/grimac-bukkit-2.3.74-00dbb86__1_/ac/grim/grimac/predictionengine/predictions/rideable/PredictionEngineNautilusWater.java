package ac.grim.grimac.predictionengine.predictions.rideable;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.predictionengine.predictions.PredictionEngine;
import ac.grim.grimac.predictionengine.predictions.input.Input;
import ac.grim.grimac.utils.data.VectorData;
import java.util.List;
import java.util.Set;
import lombok.Generated;

public class PredictionEngineNautilusWater extends PredictionEngine {
   private final Input movementVector;
   private final double multiplier;

   @Override
   public void endOfTick(GrimPlayer player, double delta) {
      for (VectorData vector : player.getPossibleVelocitiesMinusKnockback()) {
         vector.vector.setX(vector.vector.getX() * this.multiplier);
         vector.vector.setY(vector.vector.getY() * this.multiplier);
         vector.vector.setZ(vector.vector.getZ() * this.multiplier);
      }
   }

   @Override
   public void addJumpsToPossibilities(GrimPlayer player, Set<VectorData> existingVelocities) {
      PredictionEngineRideableUtils.handleJumps(player, existingVelocities);
   }

   @Override
   public List<VectorData> applyInputsToVelocityPossibilities(GrimPlayer player, Set<VectorData> possibleVectors, float speed) {
      return PredictionEngineRideableUtils.applyInputsToVelocityPossibilities(this.movementVector, player, possibleVectors, speed);
   }

   @Generated
   public PredictionEngineNautilusWater(Input movementVector, double multiplier) {
      this.movementVector = movementVector;
      this.multiplier = multiplier;
   }
}
