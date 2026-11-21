package ac.grim.grimac.checks.impl.packetorder;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.verbose.VerboseCodecs;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.InteractionHand;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;

@CheckData(
   name = "PacketOrderC",
   stableKey = "grim.packetorder.interact_order",
   description = "Sent INTERACT and INTERACT_AT entity packets in the wrong order"
)
public class PacketOrderC extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("Skipped Interact-At")
      .or("Skipped Interact")
      .or("Skipped Interact (Tick)")
      .or("requiredEntity={sint}, entity={sint}, requiredHand={hand}, hand={hand}, requiredSneaking={bool}, sneaking={bool}");
   static final int KIND_SKIPPED_INTERACT_AT = 0;
   static final int KIND_SKIPPED_INTERACT = 1;
   static final int KIND_SKIPPED_INTERACT_TICK = 2;
   static final int KIND_MISMATCH = 3;
   private final boolean exempt = this.player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_7_10)
      || this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1);
   private boolean sentInteractAt = false;
   private int requiredEntity;
   private InteractionHand requiredHand;
   private boolean requiredSneaking;

   public PacketOrderC(GrimPlayer player) {
      super(player);
   }

   private Verbose.Writer writeKind(int kind) {
      return V.write(this.verbose(), kind);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (!this.exempt) {
         if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            PacketEntity entity = this.player.compensatedEntities.entityMap.get(packet.getEntityId());
            if (entity != null && entity.getType() == EntityTypes.ARMOR_STAND) {
               return;
            }

            boolean sneaking = packet.isSneaking().orElse(false);
            switch (packet.getAction()) {
               case INTERACT:
                  if (!this.sentInteractAt) {
                     if (this.flag(this.writeKind(0)) && this.shouldModifyPackets()) {
                        event.setCancelled(true);
                        this.player.onPacketCancel();
                     }
                  } else if ((packet.getEntityId() != this.requiredEntity || packet.getHand() != this.requiredHand || sneaking != this.requiredSneaking)
                     && this.flag(
                        V.write(this.verbose(), 3)
                           .sint(this.requiredEntity)
                           .sint(packet.getEntityId())
                           .uint(VerboseCodecs.enumId(this.requiredHand))
                           .uint(VerboseCodecs.enumId(packet.getHand()))
                           .bool(this.requiredSneaking)
                           .bool(sneaking)
                     )
                     && this.shouldModifyPackets()) {
                     event.setCancelled(true);
                     this.player.onPacketCancel();
                  }

                  this.sentInteractAt = false;
                  break;
               case INTERACT_AT:
                  if (this.sentInteractAt && this.flag(this.writeKind(1)) && this.shouldModifyPackets()) {
                     event.setCancelled(true);
                     this.player.onPacketCancel();
                  }

                  this.requiredHand = packet.getHand();
                  this.requiredEntity = packet.getEntityId();
                  this.requiredSneaking = sneaking;
                  this.sentInteractAt = true;
            }
         }

         if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType()) && this.sentInteractAt) {
            this.sentInteractAt = false;
            this.flag(this.writeKind(2));
         }
      }
   }
}
