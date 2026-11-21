package ac.grim.grimac.predictionengine.predictions.rideable;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.predictionengine.predictions.PredictionEngineLava;
import ac.grim.grimac.predictionengine.predictions.input.Input;
import ac.grim.grimac.utils.data.VectorData;
import java.util.List;
import java.util.Set;
import lombok.Generated;

public class PredictionEngineRideableLava extends PredictionEngineLava {
   private final Input movementVector;

   @Override
   public void addJumpsToPossibilities(GrimPlayer player, Set<VectorData> existingVelocities) {
      PredictionEngineRideableUtils.handleJumps(player, existingVelocities);
   }

   @Override
   public List<VectorData> applyInputsToVelocityPossibilities(GrimPlayer player, Set<VectorData> possibleVectors, float speed) {
      return PredictionEngineRideableUtils.applyInputsToVelocityPossibilities(this.movementVector, player, possibleVectors, speed);
   }

   @Generated
   public PredictionEngineRideableLava(Input movementVector) {
      this.movementVector = movementVector;
   }
}
