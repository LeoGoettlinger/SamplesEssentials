package ac.grim.grimac.predictionengine.predictions.input;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.utils.math.Vector3dm;

public interface Input {
   Vector3dm vector();

   Input normalize(GrimPlayer var1);

   static Input createInput(GrimPlayer player, float sideways, float vertical, float forward) {
      return (Input)(player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14)
         ? new DoubleInput(sideways, vertical, forward)
         : new FloatInput(sideways, vertical, forward));
   }
}
