package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.InventoryCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.utils.inventory.InventoryDesyncStatus;

@CheckData(
   name = "InventoryF",
   stableKey = "grim.inventory.click_without_open",
   setback = 3.0,
   description = "Sent a click window packet without a open inventory",
   experimental = true
)
public class InventoryF extends InventoryCheck {
   public InventoryF(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (!PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9)
         && !this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
         super.onPacketReceive(event);
         if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            if (this.player.hasInventoryOpen || this.player.inventoryDesyncStatus != InventoryDesyncStatus.NOT_DESYNCED) {
               this.reward();
            } else if (this.flag()) {
               if (this.shouldModifyPackets()) {
                  event.setCancelled(true);
                  this.player.onPacketCancel();
               }

               if (!this.isNoSetbackPermission()) {
                  this.closeInventory();
               }
            }
         }
      }
   }
}
