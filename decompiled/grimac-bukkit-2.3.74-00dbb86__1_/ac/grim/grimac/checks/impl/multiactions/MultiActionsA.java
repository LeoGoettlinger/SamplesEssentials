package ac.grim.grimac.checks.impl.multiactions;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.InteractionHand;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckData(
   name = "MultiActionsA",
   stableKey = "grim.multiactions.attack_while_using",
   description = "Attacked while using an item",
   experimental = true
)
public class MultiActionsA extends Check implements PacketCheck {
   public MultiActionsA(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (this.player.packetStateData.isSlowedByUsingItem()
         && (
            this.player.packetStateData.lastSlotSelected == this.player.packetStateData.getSlowedByUsingItemSlot()
               || this.player.packetStateData.itemInUseHand == InteractionHand.OFF_HAND
         )
         && (
            event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY
                  && new WrapperPlayClientInteractEntity(event).getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK
               || event.getPacketType() == PacketType.Play.Client.ATTACK
               || event.getPacketType() == PacketType.Play.Client.SPECTATE_ENTITY
               || event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING && new WrapperPlayClientPlayerDigging(event).getAction() == DiggingAction.STAB
         )
         && this.flag()
         && this.shouldModifyPackets()) {
         event.setCancelled(true);
         this.player.onPacketCancel();
      }
   }
}
