package ac.grim.grimac.checks.type;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import ac.grim.grimac.shaded.jetbrains.annotations.MustBeInvokedByOverriders;

public class InventoryCheck extends BlockPlaceCheck implements PacketCheck {
   protected static final long NONE = Long.MAX_VALUE;
   protected long closeTransaction = Long.MAX_VALUE;
   protected int closePacketsToSkip;

   public InventoryCheck(GrimPlayer player) {
      super(player);
   }

   @MustBeInvokedByOverriders
   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
         if (this.closeTransaction != Long.MAX_VALUE && this.shouldModifyPackets()) {
            event.setCancelled(true);
            this.player.onPacketCancel();
            this.player.inventory.needResend = true;
         }
      } else if (event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW && this.closeTransaction != Long.MAX_VALUE && this.closePacketsToSkip-- <= 0) {
         this.closeTransaction = Long.MAX_VALUE;
      }
   }

   public void closeInventory() {
      if (this.closeTransaction == Long.MAX_VALUE) {
         int windowId = this.player.inventory.openWindowID;
         this.player.user.writePacket((PacketWrapper<?>)(new WrapperPlayServerCloseWindow(windowId)));
         this.closePacketsToSkip = 1;
         PacketEvents.getAPI()
            .getProtocolManager()
            .receivePacket(this.player.user.getChannel(), (PacketWrapper<?>)(new WrapperPlayClientCloseWindow(windowId)));
         this.player.sendTransaction();
         int transaction = this.player.lastTransactionSent.get();
         this.closeTransaction = transaction;
         this.player.latencyUtils.addRealTimeTask(transaction, () -> {
            if (this.closeTransaction == transaction) {
               this.closeTransaction = Long.MAX_VALUE;
            }
         });
         this.player.user.flushPackets();
      }
   }
}
