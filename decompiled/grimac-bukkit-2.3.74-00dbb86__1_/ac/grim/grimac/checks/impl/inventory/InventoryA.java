package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.InventoryCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(
   name = "InventoryA",
   stableKey = "grim.inventory.attack_open",
   setback = 3.0,
   description = "Attacked an entity while inventory is open"
)
public class InventoryA extends InventoryCheck {
   public InventoryA(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      super.onPacketReceive(event);
      if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
         WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
         if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            return;
         }

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
