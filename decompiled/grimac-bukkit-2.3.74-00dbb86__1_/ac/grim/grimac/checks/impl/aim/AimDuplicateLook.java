package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(
   name = "AimDuplicateLook",
   stableKey = "grim.aim.duplicate_look",
   description = "Sent a duplicate rotation update without changing look direction"
)
public class AimDuplicateLook extends Check implements RotationCheck {
   private boolean exempt;

   public AimDuplicateLook(GrimPlayer playerData) {
      super(playerData);
   }

   @Override
   public void process(RotationUpdate rotationUpdate) {
      if (this.player.packetStateData.lastPacketWasTeleport
         || this.player.packetStateData.lastPacketWasOnePointSeventeenDuplicate
         || this.player.compensatedEntities.self.getRiding() != null) {
         this.exempt = true;
      } else if (this.exempt) {
         this.exempt = false;
      } else {
         if (rotationUpdate.getFrom().equals(rotationUpdate.getTo())) {
            this.flag();
         }
      }
   }
}
