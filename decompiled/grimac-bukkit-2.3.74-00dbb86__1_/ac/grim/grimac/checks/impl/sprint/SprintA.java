package ac.grim.grimac.checks.impl.sprint;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;

@CheckData(
   name = "SprintA",
   stableKey = "grim.sprint.hunger",
   description = "Sprinting with too low hunger",
   setback = 0.0
)
public class SprintA extends Check implements PostPredictionCheck {
   private static final Verbose V = Verbose.of("hunger={uint}");

   public SprintA(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (predictionComplete.isChecked()) {
         if (!this.player.canFly && !EntityTypes.isTypeInstanceOf(this.player.getVehicleType(), EntityTypes.CAMEL)) {
            if (this.player.food <= 6.0F) {
               if (this.player.isSprinting) {
                  if (this.flag(V.write(this.verbose()).uint(this.player.food))) {
                     if (this.shouldModifyPackets()) {
                        this.player.onPacketCancel();
                     }

                     this.setbackIfAboveSetbackVL();
                  }
               } else {
                  this.reward();
               }
            }
         }
      }
   }
}
