package ac.grim.grimac.checks.impl.breaking;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.verbose.VerboseCodecs;
import ac.grim.grimac.checks.type.BlockBreakCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.utils.anticheat.update.BlockBreak;

@CheckData(
   name = "PositionBreakB",
   stableKey = "grim.breaking.position_break_b",
   description = "Cancelled block breaking with an invalid block face"
)
public class PositionBreakB extends Check implements BlockBreakCheck {
   private static final Verbose V = Verbose.of("lastFace={face}, action={digging}");
   private final boolean allowLegacyFace = this.player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_7_10);
   private BlockFace lastFace;

   public PositionBreakB(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onBlockBreak(BlockBreak blockBreak) {
      if (blockBreak.action == DiggingAction.START_DIGGING && blockBreak.face == this.lastFace) {
         this.lastFace = null;
      }

      if (this.lastFace != null) {
         this.flag(V.write(this.verbose()).uint(VerboseCodecs.enumId(this.lastFace)).uint(VerboseCodecs.enumId(blockBreak.action)));
      }

      if (blockBreak.action == DiggingAction.CANCELLED_DIGGING) {
         this.lastFace = blockBreak.faceId != 0 && (!this.allowLegacyFace || blockBreak.faceId != 255) ? blockBreak.face : null;
      }
   }
}
