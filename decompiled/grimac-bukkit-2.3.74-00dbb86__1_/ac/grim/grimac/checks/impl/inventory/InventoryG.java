package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.InventoryCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

@CheckData(
   name = "InventoryG",
   stableKey = "grim.inventory.entity_action_open",
   setback = 3.0,
   description = "Sent a entity action packet while inventory is open",
   experimental = true
)
public class InventoryG extends InventoryCheck {
   public InventoryG(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (!this.player.packetStateData.lastPacketWasTeleport) {
         super.onPacketReceive(event);
         if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction wrapper = new WrapperPlayClientEntityAction(event);
            WrapperPlayClientEntityAction.Action action = wrapper.getAction();
            if (action == WrapperPlayClientEntityAction.Action.STOP_SNEAKING || action == WrapperPlayClientEntityAction.Action.STOP_SPRINTING) {
               return;
            }

            if (this.player.hasInventoryOpen) {
               if (this.flag() && !this.isNoSetbackPermission()) {
                  this.closeInventory();
               }
            } else {
               this.reward();
            }
         }
      }
   }
}
