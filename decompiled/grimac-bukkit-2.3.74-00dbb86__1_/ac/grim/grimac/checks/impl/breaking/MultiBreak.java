package ac.grim.grimac.checks.impl.breaking;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.verbose.VerboseCodecs;
import ac.grim.grimac.checks.type.BlockBreakCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.utils.anticheat.update.BlockBreak;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import java.util.ArrayList;
import java.util.List;

@CheckData(
   name = "MultiBreak",
   stableKey = "grim.breaking.multi_break",
   description = "Tried to break multiple different blocks in the same movement tick",
   experimental = true
)
public class MultiBreak extends Check implements BlockBreakCheck {
   private static final Verbose V = Verbose.of("face={face}, lastFace={face}, pos={mcpos}, lastPos={mcpos}");
   private final List<MultiBreak.FlagData> flags = new ArrayList<>();
   private boolean hasBroken;
   private BlockFace lastFace;
   private Vector3i lastPos;

   public MultiBreak(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onBlockBreak(BlockBreak blockBreak) {
      if (blockBreak.action != DiggingAction.CANCELLED_DIGGING) {
         if (this.hasBroken && (blockBreak.face != this.lastFace || !blockBreak.position.equals(this.lastPos))) {
            int face = VerboseCodecs.enumId(blockBreak.face);
            int previousFace = VerboseCodecs.enumId(this.lastFace);
            if (!this.player.canSkipTicks()) {
               Verbose.Writer buf = V.write(this.verbose())
                  .uint(face)
                  .uint(previousFace)
                  .mcPos(blockBreak.position.x, blockBreak.position.y, blockBreak.position.z)
                  .mcPos(this.lastPos.x, this.lastPos.y, this.lastPos.z);
               if (this.flag(buf) && this.shouldModifyPackets()) {
                  blockBreak.cancel();
               }
            } else {
               this.flags.add(new MultiBreak.FlagData(face, previousFace, blockBreak.position, this.lastPos));
            }
         }

         this.lastFace = blockBreak.face;
         this.lastPos = blockBreak.position;
         this.hasBroken = true;
      }
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (!this.player.cameraEntity.isSelf() || this.isTickPacket(event.getPacketType())) {
         this.hasBroken = false;
      }
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (this.player.canSkipTicks()) {
         if (this.player.isTickingReliablyFor(3)) {
            for (MultiBreak.FlagData data : this.flags) {
               this.flag(
                  V.write(this.verbose())
                     .uint(data.face())
                     .uint(data.previousFace())
                     .mcPos(data.pos().x, data.pos().y, data.pos().z)
                     .mcPos(data.previousPos().x, data.previousPos().y, data.previousPos().z)
               );
            }
         }

         this.flags.clear();
      }
   }

   private record FlagData(int face, int previousFace, Vector3i pos, Vector3i previousPos) {
   }
}
