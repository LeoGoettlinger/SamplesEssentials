package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(
   name = "BadPacketsD",
   stableKey = "grim.badpackets.invalid_pitch",
   description = "Sent an invalid rotation pitch outside the -90 to 90 range"
)
public class BadPacketsD extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("pitch={f32}");

   public BadPacketsD(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (!this.player.packetStateData.lastPacketWasTeleport) {
         if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            float pitch = new WrapperPlayClientPlayerFlying(event).getLocation().getPitch();
            if ((pitch > 90.0F || pitch < -90.0F) && this.flag(V.write(this.verbose()).f32(pitch)) && this.shouldModifyPackets()) {
               if (this.player.pitch > 90.0F) {
                  this.player.pitch = 90.0F;
               }

               if (this.player.pitch < -90.0F) {
                  this.player.pitch = -90.0F;
               }

               event.setCancelled(true);
               this.player.onPacketCancel();
            }
         }
      }
   }
}
