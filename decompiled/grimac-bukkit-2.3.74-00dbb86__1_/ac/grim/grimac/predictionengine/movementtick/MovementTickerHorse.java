package ac.grim.grimac.predictionengine.movementtick;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.predictionengine.predictions.input.Input;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.utils.data.packetentity.PacketEntityHorse;
import ac.grim.grimac.utils.nmsutil.Collisions;

public class MovementTickerHorse extends MovementTickerLivingVehicle {
   public MovementTickerHorse(GrimPlayer player) {
      super(player);
      PacketEntityHorse horsePacket = (PacketEntityHorse)player.compensatedEntities.self.getRiding();
      if (horsePacket.hasSaddle()) {
         player.speed = (float)horsePacket.getAttributeValue(Attributes.MOVEMENT_SPEED) + this.getExtraSpeed();
         float horizInput = player.vehicleData.vehicleHorizontal * 0.5F;
         float forwardsInput = player.vehicleData.vehicleForward;
         if (forwardsInput <= 0.0F) {
            forwardsInput *= 0.25F;
         }

         this.movementInput = Input.createInput(player, horizInput, 0.0F, forwardsInput).normalize(player);
      }
   }

   @Override
   public void livingEntityAIStep() {
      super.livingEntityAIStep();
      if (this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_17)) {
         Collisions.handleInsideBlocks(this.player);
      }
   }

   public float getExtraSpeed() {
      return 0.0F;
   }
}
