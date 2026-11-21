package ac.grim.grimac.predictionengine.predictions.rideable;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.predictionengine.predictions.PredictionEngineWater;
import ac.grim.grimac.predictionengine.predictions.input.Input;
import ac.grim.grimac.utils.data.VectorData;
import java.util.List;
import java.util.Set;
import lombok.Generated;

public class PredictionEngineRideableWater extends PredictionEngineWater {
   protected final Input movementVector;

   @Override
   public void addJumpsToPossibilities(GrimPlayer player, Set<VectorData> existingVelocities) {
      PredictionEngineRideableUtils.handleJumps(player, existingVelocities);
   }

   @Override
   public List<VectorData> applyInputsToVelocityPossibilities(GrimPlayer player, Set<VectorData> possibleVectors, float speed) {
      return PredictionEngineRideableUtils.applyInputsToVelocityPossibilities(this.movementVector, player, possibleVectors, speed);
   }

   @Generated
   public PredictionEngineRideableWater(Input movementVector) {
      this.movementVector = movementVector;
   }
}
