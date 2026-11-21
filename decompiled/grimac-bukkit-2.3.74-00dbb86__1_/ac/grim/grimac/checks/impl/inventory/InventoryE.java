package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.InventoryCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(
   name = "InventoryE",
   stableKey = "grim.inventory.held_item_change_open",
   setback = 3.0,
   description = "Sent a held item change packet while inventory is open"
)
public class InventoryE extends InventoryCheck {
   private long lastTransaction = Long.MAX_VALUE;

   public InventoryE(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      super.onPacketReceive(event);
      if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
         if (this.player.hasInventoryOpen) {
            if (this.lastTransaction < this.player.lastTransactionReceived.get() && this.flag()) {
               if (this.shouldModifyPackets()) {
                  event.setCancelled(true);
                  this.player.onPacketCancel();
                  this.player.inventory.needResend = true;
               }

               if (!this.isNoSetbackPermission()) {
                  this.closeInventory();
               }
            }
         } else {
            this.reward();
         }
      }
   }

   @Override
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPacketType() == PacketType.Play.Server.HELD_ITEM_CHANGE) {
         this.lastTransaction = this.player.lastTransactionSent.get();
      }
   }
}
