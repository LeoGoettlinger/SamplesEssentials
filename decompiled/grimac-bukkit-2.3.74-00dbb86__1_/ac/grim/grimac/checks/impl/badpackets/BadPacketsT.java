package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;

@CheckData(
   name = "BadPacketsT",
   stableKey = "grim.badpackets.invalid_interact_vector",
   description = "Sent an entity interaction vector outside the target player's hitbox"
)
public class BadPacketsT extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("{f64:%.5f}/{f64:%.5f}/{f64:%.5f}");
   private final double maxHorizontalDisplacement;
   private final double minVerticalDisplacement;
   private final double maxVerticalDisplacement;

   public BadPacketsT(GrimPlayer player) {
      super(player);
      double expansion = player.getClientVersion().isOlderThan(ClientVersion.V_1_9) ? 0.1 : 0.0;
      this.maxHorizontalDisplacement = 0.3001 + expansion;
      this.minVerticalDisplacement = -1.0E-4 - expansion;
      this.maxVerticalDisplacement = 1.8001 + expansion;
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType().equals(PacketType.Play.Client.INTERACT_ENTITY)) {
         WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
         if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) {
            return;
         }

         Vector3d targetVector = wrapper.getLocation();
         if (targetVector == null) {
            return;
         }

         PacketEntity packetEntity = this.player.compensatedEntities.getEntity(wrapper.getEntityId());
         if (packetEntity == null) {
            return;
         }

         if (!EntityTypes.PLAYER.equals(packetEntity.getType())) {
            return;
         }

         float scale = (float)packetEntity.getAttributeValue(Attributes.SCALE);
         if (targetVector.y > this.minVerticalDisplacement * scale
            && targetVector.y < this.maxVerticalDisplacement * scale
            && Math.abs(targetVector.x) < this.maxHorizontalDisplacement * scale
            && Math.abs(targetVector.z) < this.maxHorizontalDisplacement * scale) {
            return;
         }

         this.flag(V.write(this.verbose()).f64(targetVector.x).f64(targetVector.y).f64(targetVector.z));
      }
   }
}
