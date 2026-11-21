package ac.grim.grimac.checks.impl.prediction;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.GameMode;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;

@CheckData(
   name = "GroundSpoof",
   stableKey = "grim.groundspoof.fake",
   description = "Claimed to be on ground when predicted otherwise",
   setback = 10.0,
   decay = 0.01
)
public class GroundSpoof extends Check implements PostPredictionCheck {
   private static final Verbose V = Verbose.of("claimed {bool}");

   public GroundSpoof(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (!PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_8) || this.player.gamemode != GameMode.SPECTATOR) {
         if (!this.player.exemptOnGround() && predictionComplete.isChecked()) {
            if (!this.player.getSetbackTeleportUtil().blockOffsets) {
               if (!this.player.packetStateData.lastPacketWasTeleport) {
                  boolean claimed = this.player.clientClaimsLastOnGround;
                  if (claimed != this.player.onGround) {
                     this.flagWithSetback(V.write(this.verbose()).bool(claimed));
                     this.player.checkManager.getNoFall().flipPlayerGroundStatus = true;
                  }
               }
            }
         }
      }
   }
}
