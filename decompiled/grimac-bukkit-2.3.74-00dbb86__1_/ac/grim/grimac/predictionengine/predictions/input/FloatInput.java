package ac.grim.grimac.predictionengine.predictions.input;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.math.Vector3dm;

public record FloatInput(float sideways, float vertical, float forward) implements Input {
   @Override
   public Vector3dm vector() {
      return new Vector3dm(this.sideways, this.vertical, this.forward);
   }

   @Override
   public Input normalize(GrimPlayer player) {
      return this;
   }
}
