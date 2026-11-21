package ac.grim.grimac.checks.impl.breaking;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.verbose.VerboseCodecs;
import ac.grim.grimac.checks.type.BlockBreakCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.utils.anticheat.update.BlockBreak;
import ac.grim.grimac.utils.nmsutil.BlockBreakSpeed;

@CheckData(
   name = "WrongBreak",
   stableKey = "grim.breaking.wrong_break",
   description = "Sent block break progress for a different block than the one being mined"
)
public class WrongBreak extends Check implements BlockBreakCheck {
   private static final Verbose V = Verbose.of("action={digging}, last=[{mcpos}|null], pos={mcpos}");
   private final int exemptedY = this.player.getClientVersion().isOlderThan(ClientVersion.V_1_8)
      ? 255
      : (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_14) ? -1 : 4095);
   private boolean lastBlockWasInstantBreak = false;
   private Vector3i lastBlock;
   private Vector3i lastCancelledBlock;
   private Vector3i lastLastBlock = null;

   public WrongBreak(GrimPlayer player) {
      super(player);
   }

   private boolean shouldExempt(WrappedBlockState block, int yPos) {
      if (this.lastLastBlock == null && this.lastBlock != null) {
         return this.player.getClientVersion().isOlderThan(ClientVersion.V_1_14_4) && yPos != this.exemptedY
            ? false
            : this.player.getClientVersion().isOlderThan(ClientVersion.V_1_14_4) || BlockBreakSpeed.getBlockDamage(this.player, block) < 1.0;
      } else {
         return false;
      }
   }

   @Override
   public void onBlockBreak(BlockBreak blockBreak) {
      if (blockBreak.action == DiggingAction.START_DIGGING) {
         Vector3i pos = blockBreak.position;
         this.lastBlockWasInstantBreak = BlockBreakSpeed.getBlockDamage(this.player, blockBreak.block) >= 1.0;
         this.lastCancelledBlock = null;
         this.lastLastBlock = this.lastBlock;
         this.lastBlock = pos;
      }

      if (blockBreak.action == DiggingAction.CANCELLED_DIGGING) {
         Vector3i pos = blockBreak.position;
         if (!this.shouldExempt(blockBreak.block, pos.y)
            && !pos.equals(this.lastBlock)
            && (this.player.getClientVersion().isOlderThan(ClientVersion.V_1_14_4) || !this.lastBlockWasInstantBreak && pos.equals(this.lastCancelledBlock))) {
            Verbose.Writer buf = V.write(this.verbose())
               .uint(VerboseCodecs.enumId(DiggingAction.CANCELLED_DIGGING))
               .bool(this.lastBlock != null)
               .mcPos(
                  this.lastBlock == null ? 0 : this.lastBlock.x, this.lastBlock == null ? 0 : this.lastBlock.y, this.lastBlock == null ? 0 : this.lastBlock.z
               )
               .mcPos(pos.x, pos.y, pos.z);
            if (this.flag(buf) && this.shouldModifyPackets()) {
               blockBreak.cancel();
            }
         }

         this.lastCancelledBlock = pos;
         this.lastLastBlock = null;
         this.lastBlock = null;
      } else {
         if (blockBreak.action == DiggingAction.FINISHED_DIGGING) {
            Vector3i pos = blockBreak.position;
            if (!pos.equals(this.lastCancelledBlock)
               && (!this.lastBlockWasInstantBreak || this.player.getClientVersion().isOlderThan(ClientVersion.V_1_14_4))
               && !pos.equals(this.lastBlock)) {
               Verbose.Writer buf = V.write(this.verbose())
                  .uint(VerboseCodecs.enumId(DiggingAction.FINISHED_DIGGING))
                  .bool(this.lastBlock != null)
                  .mcPos(
                     this.lastBlock == null ? 0 : this.lastBlock.x,
                     this.lastBlock == null ? 0 : this.lastBlock.y,
                     this.lastBlock == null ? 0 : this.lastBlock.z
                  )
                  .mcPos(pos.x, pos.y, pos.z);
               if (this.flag(buf) && this.shouldModifyPackets()) {
                  blockBreak.cancel();
               }
            }

            if (this.player.getClientVersion().isOlderThan(ClientVersion.V_1_14_4)) {
               this.lastCancelledBlock = null;
               this.lastLastBlock = null;
               this.lastBlock = null;
            }
         }
      }
   }
}
