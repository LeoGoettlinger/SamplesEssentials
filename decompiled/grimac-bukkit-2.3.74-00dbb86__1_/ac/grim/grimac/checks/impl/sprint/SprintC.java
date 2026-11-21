package ac.grim.grimac.checks.impl.sprint;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;

@CheckData(
   name = "SprintC",
   stableKey = "grim.sprint.using_item",
   description = "Sprinting while using an item",
   setback = 5.0,
   experimental = true
)
public class SprintC extends Check implements PostPredictionCheck {
   private boolean flaggedLastTick = false;

   public SprintC(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (this.player.packetStateData.isSlowedByUsingItem()) {
         ClientVersion version = this.player.getClientVersion();
         if (version.isNewerThanOrEquals(ClientVersion.V_1_14_2) && version != ClientVersion.V_1_21_4) {
            return;
         }

         if (!this.player.wasTouchingWater || version.isOlderThan(ClientVersion.V_1_13)) {
            this.flaggedLastTick = false;
            return;
         }

         if (this.player.isSprinting) {
            if (this.flaggedLastTick) {
               this.flagWithSetback();
            }

            this.flaggedLastTick = true;
         } else {
            this.reward();
            this.flaggedLastTick = false;
         }
      }
   }
}
