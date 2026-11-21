package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.InventoryCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckData(
   name = "InventoryB",
   stableKey = "grim.inventory.dig_open",
   setback = 3.0,
   description = "Started digging blocks while inventory is open"
)
public class InventoryB extends InventoryCheck {
   public InventoryB(GrimPlayer player) {
      super(player);
   }

   public void handle(PacketReceiveEvent event, WrapperPlayClientPlayerDigging wrapper) {
      if (wrapper.getAction() == DiggingAction.START_DIGGING) {
         if (this.player.hasInventoryOpen) {
            if (this.flag()) {
               if (this.shouldModifyPackets()) {
                  event.setCancelled(true);
                  this.player.onPacketCancel();
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
}
