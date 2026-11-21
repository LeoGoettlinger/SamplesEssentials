package ac.grim.grimac.checks.impl.crash;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;

@CheckData(
   name = "CrashE",
   stableKey = "grim.crash.low_view_distance",
   description = "Sent a client view distance below the minimum allowed value"
)
public class CrashE extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("distance={sint}");

   public CrashE(GrimPlayer playerData) {
      super(playerData);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.CLIENT_SETTINGS) {
         WrapperPlayClientSettings wrapper = new WrapperPlayClientSettings(event);
         int viewDistance = wrapper.getViewDistance();
         if (viewDistance < 2) {
            this.flag(V.write(this.verbose()).sint(viewDistance));
            wrapper.setViewDistance(2);
         }
      }
   }
}
