package ac.grim.grimac.checks.impl.packetorder;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;

@CheckData(
   name = "PacketOrderJ",
   stableKey = "grim.packetorder.attack_interact_use_order",
   description = "Sent use item after attacking without the expected interaction packet",
   experimental = true
)
public class PacketOrderJ extends Check implements PostPredictionCheck {
   private int invalid;

   public PacketOrderJ(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if ((event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT || event.getPacketType() == PacketType.Play.Client.USE_ITEM)
         && this.player.packetOrderProcessor.isAttacking()
         && !this.player.packetOrderProcessor.isInteracting()) {
         if (!this.player.canSkipTicks()) {
            if (this.flag() && this.shouldModifyPackets()) {
               event.setCancelled(true);
               this.player.onPacketCancel();
            }
         } else {
            this.invalid++;
         }
      }
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (this.player.canSkipTicks()) {
         if (this.player.isTickingReliablyFor(3)) {
            while (this.invalid >= 1) {
               this.flag();
               this.invalid--;
            }
         }

         this.invalid = 0;
      }
   }
}
