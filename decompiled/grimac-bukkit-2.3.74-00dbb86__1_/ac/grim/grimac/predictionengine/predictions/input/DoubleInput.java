package ac.grim.grimac.predictionengine.predictions.input;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.math.Vector3dm;
import ac.grim.grimac.utils.math.VectorUtils;

public record DoubleInput(double sideways, double vertical, double forward) implements Input {
   @Override
   public Vector3dm vector() {
      return new Vector3dm(this.sideways, this.vertical, this.forward);
   }

   @Override
   public Input normalize(GrimPlayer player) {
      double lengthSquared = this.sideways * this.sideways + this.vertical * this.vertical + this.forward * this.forward;
      if (lengthSquared > 1.0) {
         double d0 = VectorUtils.getVanillaLength(player.getClientVersion(), this.sideways, this.vertical, this.forward);
         return new DoubleInput(this.sideways / d0, this.vertical / d0, this.forward / d0);
      } else {
         return this;
      }
   }
}
