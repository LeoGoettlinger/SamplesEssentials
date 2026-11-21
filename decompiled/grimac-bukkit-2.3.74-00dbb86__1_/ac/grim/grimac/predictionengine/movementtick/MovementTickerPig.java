package ac.grim.grimac.predictionengine.movementtick;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.predictionengine.predictions.input.Input;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes;
import ac.grim.grimac.utils.data.packetentity.PacketEntityRideable;

public class MovementTickerPig extends MovementTickerRideable {
   public MovementTickerPig(GrimPlayer player) {
      super(player);
      this.movementInput = Input.createInput(player, 0.0F, 0.0F, 1.0F);
   }

   @Override
   public float getSteeringSpeed() {
      PacketEntityRideable pig = (PacketEntityRideable)this.player.compensatedEntities.self.getRiding();
      return (float)pig.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.225F;
   }
}
