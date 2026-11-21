package ac.grim.grimac.checks.impl.packetorder;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import java.util.ArrayDeque;

@CheckData(
   name = "PacketOrderL",
   stableKey = "grim.packetorder.drop_item_order",
   description = "Sent drop, inventory open, or offhand swap packets in an invalid order",
   experimental = true
)
public class PacketOrderL extends Check implements PostPredictionCheck {
   private static final Verbose V = Verbose.of("[inventory|swap]");
   static final int ACTION_INVENTORY = 0;
   static final int ACTION_SWAP = 1;
   private final ArrayDeque<Integer> flags = new ArrayDeque<>();

   public PacketOrderL(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS
         && new WrapperPlayClientClientStatus(event).getAction() == WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT
         && this.player.packetOrderProcessor.isDropping()) {
         if (!this.player.canSkipTicks()) {
            if (this.flag(V.write(this.verbose()).bool(true)) && this.shouldModifyPackets()) {
               event.setCancelled(true);
               this.player.onPacketCancel();
            }
         } else {
            this.flags.add(0);
         }
      }

      if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING
         && new WrapperPlayClientPlayerDigging(event).getAction() == DiggingAction.SWAP_ITEM_WITH_OFFHAND
         && this.player.packetOrderProcessor.isDropping()) {
         if (!this.player.canSkipTicks()) {
            if (this.flag(V.write(this.verbose()).bool(false)) && this.shouldModifyPackets()) {
               event.setCancelled(true);
               this.player.onPacketCancel();
            }
         } else {
            this.flags.add(1);
         }
      }
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (this.player.canSkipTicks()) {
         if (this.player.isTickingReliablyFor(3)) {
            for (int action : this.flags) {
               this.flag(V.write(this.verbose()).bool(action == 0));
            }
         }

         this.flags.clear();
      }
   }
}
