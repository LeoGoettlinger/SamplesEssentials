package ac.grim.grimac.checks.impl.packetorder;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;

@CheckData(
   name = "PacketOrderA",
   stableKey = "grim.packetorder.window_click_order",
   description = "Sent pickup and quick-move inventory clicks in an invalid order",
   experimental = true
)
public class PacketOrderA extends Check implements PostPredictionCheck {
   private int invalid;

   public PacketOrderA(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
         WrapperPlayClientClickWindow.WindowClickType clickType = new WrapperPlayClientClickWindow(event).getWindowClickType();
         if ((clickType == WrapperPlayClientClickWindow.WindowClickType.PICKUP || clickType == WrapperPlayClientClickWindow.WindowClickType.PICKUP_ALL)
               && this.player.packetOrderProcessor.isQuickMoveClicking()
            || clickType == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE && this.player.packetOrderProcessor.isPickUpClicking()) {
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
