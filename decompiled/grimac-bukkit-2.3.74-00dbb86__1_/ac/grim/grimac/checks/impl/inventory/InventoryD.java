package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.InventoryCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import ac.grim.grimac.utils.data.VectorData;
import ac.grim.grimac.utils.data.VehicleData;
import java.util.StringJoiner;

@CheckData(
   name = "InventoryD",
   stableKey = "grim.inventory.move_open",
   setback = 1.0,
   decay = 0.25
)
public class InventoryD extends InventoryCheck {
   private int horseJumpVerbose;

   public InventoryD(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (predictionComplete.isChecked()
         && !predictionComplete.getData().isTeleport()
         && !this.player.getSetbackTeleportUtil().blockOffsets
         && !this.player.packetStateData.lastPacketWasTeleport
         && !this.player.packetStateData.isSlowedByUsingItem()
         && System.currentTimeMillis() - this.player.lastBlockPlaceUseItem >= 50L) {
         if (this.player.hasInventoryOpen) {
            boolean inVehicle = this.player.inVehicle();
            boolean isJumping;
            boolean isMoving;
            if (inVehicle) {
               VehicleData vehicle = this.player.vehicleData;
               isJumping = !vehicle.pendingJumps.isEmpty() && this.horseJumpVerbose++ >= 1;
               isMoving = vehicle.nextVehicleForward != 0.0F || vehicle.nextVehicleHorizontal != 0.0F;
            } else {
               VectorData.MoveVectorData move = this.findMovement(this.player.predictedVelocity);
               isJumping = this.player.predictedVelocity.isJump();
               isMoving = move != null && (move.x != 0 || move.z != 0);
            }

            if (!isMoving && !isJumping) {
               this.reward();
               return;
            }

            if (this.flag()) {
               if (!this.isNoSetbackPermission()) {
                  this.closeInventory();
               }

               StringJoiner joiner = new StringJoiner(" ");
               if (isMoving) {
                  joiner.add("moving");
               }

               if (isJumping) {
                  joiner.add("jumping");
               }

               if (inVehicle) {
                  joiner.add("inVehicle");
               }

               this.alert(joiner.toString());
            }
         } else {
            this.horseJumpVerbose = 0;
         }
      }
   }

   private VectorData.MoveVectorData findMovement(VectorData vectorData) {
      if (vectorData instanceof VectorData.MoveVectorData) {
         return (VectorData.MoveVectorData)vectorData;
      } else {
         while (vectorData != null) {
            vectorData = vectorData.lastVector;
            if (vectorData instanceof VectorData.MoveVectorData) {
               return (VectorData.MoveVectorData)vectorData;
            }
         }

         return null;
      }
   }
}
