package ac.grim.grimac.events.packets.worldreader.multiblockchange;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;

public class LegacyMultiBlockChangeHandler implements VersionedMultiBlockChangeHandler {
   @Override
   public void handleMultiBlockChange(GrimPlayer player, PacketSendEvent event) {
      WrapperPlayServerMultiBlockChange multiBlockChange = new WrapperPlayServerMultiBlockChange(event);
      WrapperPlayServerMultiBlockChange.EncodedBlock[] blocks = multiBlockChange.getBlocks();

      for (WrapperPlayServerMultiBlockChange.EncodedBlock blockChange : blocks) {
         if (Math.abs(blockChange.getX() - player.x) < 16.0
            && Math.abs(blockChange.getY() - player.y) < 16.0
            && Math.abs(blockChange.getZ() - player.z) < 16.0
            && player.lastTransSent + 2L < System.currentTimeMillis()) {
            player.sendTransaction();
            break;
         }
      }

      player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
         for (WrapperPlayServerMultiBlockChange.EncodedBlock blockChangex : blocks) {
            player.compensatedWorld.updateBlock(blockChangex.getX(), blockChangex.getY(), blockChangex.getZ(), blockChangex.getBlockId());
         }
      });
   }
}
