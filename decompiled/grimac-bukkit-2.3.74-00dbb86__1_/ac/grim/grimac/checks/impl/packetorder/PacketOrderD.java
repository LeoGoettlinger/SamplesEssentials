package ac.grim.grimac.checks.impl.packetorder;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.InteractionHand;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(
   name = "PacketOrderD",
   stableKey = "grim.packetorder.interact_hand_order",
   description = "Sent offhand entity interaction before the matching mainhand interaction",
   experimental = true
)
public class PacketOrderD extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("[Skipped Mainhand|requiredEntity={sint}, entity={sint}, requiredSneaking={bool}, sneaking={bool}]");
   private boolean sentMainhand;
   private int requiredEntity;
   private boolean requiredSneaking;

   public PacketOrderD(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY && this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
         WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
         WrapperPlayClientInteractEntity.InteractAction action = packet.getAction();
         if (action != WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            boolean sneaking = packet.isSneaking().orElse(false);
            int entity = packet.getEntityId();
            if (packet.getHand() == InteractionHand.OFF_HAND) {
               if (action == WrapperPlayClientInteractEntity.InteractAction.INTERACT
                  || this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1)) {
                  if (!this.sentMainhand && this.flag(V.write(this.verbose()).bool(true).sint(0).sint(0).bool(false).bool(false)) && this.shouldModifyPackets()
                     )
                   {
                     event.setCancelled(true);
                     this.player.onPacketCancel();
                  }

                  this.sentMainhand = false;
               }

               if (action == WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT
                  && (sneaking != this.requiredSneaking || entity != this.requiredEntity)
                  && this.flag(V.write(this.verbose()).bool(false).sint(this.requiredEntity).sint(entity).bool(this.requiredSneaking).bool(sneaking))
                  && this.shouldModifyPackets()) {
                  event.setCancelled(true);
                  this.player.onPacketCancel();
               }
            } else {
               this.requiredEntity = entity;
               this.requiredSneaking = sneaking;
               this.sentMainhand = true;
            }
         }
      }

      if (this.isTickPacket(event.getPacketType())) {
         this.sentMainhand = false;
      }
   }
}
