package ac.grim.grimac.checks.impl.breaking;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.verbose.VerboseCodecs;
import ac.grim.grimac.checks.type.BlockBreakCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.utils.anticheat.update.BlockBreak;

@CheckData(
   name = "InvalidBreak",
   stableKey = "grim.breaking.invalid_break",
   description = "Sent impossible block face id"
)
public class InvalidBreak extends Check implements BlockBreakCheck {
   private static final Verbose V = Verbose.of("face={sint}, action={digging}");

   public InvalidBreak(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onBlockBreak(BlockBreak blockBreak) {
      if (blockBreak.faceId != 255
         || blockBreak.action != DiggingAction.CANCELLED_DIGGING
         || !this.player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_7_10)) {
         if ((blockBreak.faceId < 0 || blockBreak.faceId > 5)
            && this.flag(V.write(this.verbose()).sint(blockBreak.faceId).uint(VerboseCodecs.enumId(blockBreak.action)))
            && this.shouldModifyPackets()) {
            blockBreak.cancel();
         }
      }
   }
}
