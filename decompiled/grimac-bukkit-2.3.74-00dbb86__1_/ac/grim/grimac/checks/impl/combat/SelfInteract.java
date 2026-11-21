package ac.grim.grimac.checks.impl.combat;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAttack;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSpectateEntity;

@CheckData(
   name = "SelfInteract",
   stableKey = "grim.badpackets.self_hit",
   description = "Interacted with self"
)
public class SelfInteract extends Check implements PacketCheck {
   public SelfInteract(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
         WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
         this.onInteract(event, packet.getEntityId());
      }

      if (event.getPacketType() == PacketType.Play.Client.ATTACK) {
         WrapperPlayClientAttack packet = new WrapperPlayClientAttack(event);
         this.onInteract(event, packet.getEntityId());
      }

      if (event.getPacketType() == PacketType.Play.Client.SPECTATE_ENTITY) {
         WrapperPlayClientSpectateEntity packet = new WrapperPlayClientSpectateEntity(event);
         this.onInteract(event, packet.getEntityId());
      }
   }

   private void onInteract(PacketReceiveEvent event, int entityId) {
      if (this.player.cameraEntity.isSelf() && entityId == this.player.entityID && this.flag() && this.shouldModifyPackets()) {
         event.setCancelled(true);
         this.player.onPacketCancel();
      }
   }
}
