package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.verbose.VerboseCodecs;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckData(
   name = "BadPacketsL",
   stableKey = "grim.badpackets.invalid_dig",
   description = "Sent impossible dig packet"
)
public class BadPacketsL extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("pos={mcpos}, face={sint}, sequence={sint}, action={digging_lower}");

   public BadPacketsL(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
         WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
         if (packet.getAction() == DiggingAction.START_DIGGING
            || packet.getAction() == DiggingAction.FINISHED_DIGGING
            || packet.getAction() == DiggingAction.CANCELLED_DIGGING) {
            return;
         }

         boolean allowLegacyFace = this.player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_7_10)
            && packet.getAction() == DiggingAction.RELEASE_USE_ITEM;
         boolean isValidFace = packet.getBlockFaceId() == 0 || allowLegacyFace && packet.getBlockFaceId() == 255;
         if (!isValidFace
            || packet.getBlockPosition().getX() != 0
            || packet.getBlockPosition().getY() != 0
            || packet.getBlockPosition().getZ() != 0
            || packet.getSequence() != 0) {
            Vector3i pos = packet.getBlockPosition();
            Verbose.Writer buf = V.write(this.verbose())
               .mcPos(pos.getX(), pos.getY(), pos.getZ())
               .sint(packet.getBlockFaceId())
               .sint(packet.getSequence())
               .uint(VerboseCodecs.enumId(packet.getAction()));
            if (this.flag(buf) && this.shouldModifyPackets() && this.canCancel(packet.getAction())) {
               event.setCancelled(true);
               this.player.onPacketCancel();
            }
         }
      }
   }
}
