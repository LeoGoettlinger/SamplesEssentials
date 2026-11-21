package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(
   name = "BadPacketsE",
   stableKey = "grim.badpackets.invalid_position",
   description = "Sent too many movement packets without updating position"
)
public class BadPacketsE extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("ticks={uint}");
   private int noReminderTicks;
   private final int maxNoReminderTicks = this.player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8) ? 20 : 19;
   private final boolean isViaPleaseStopUsingProtocolHacksOnYourServer = this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2)
      || PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2);

   public BadPacketsE(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {
         this.noReminderTicks = 0;
      } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType()) && !this.player.packetStateData.lastPacketWasTeleport) {
         if (++this.noReminderTicks > this.maxNoReminderTicks) {
            this.flag(V.write(this.verbose()).uint(this.noReminderTicks));
         }
      } else if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE || this.isViaPleaseStopUsingProtocolHacksOnYourServer && this.player.inVehicle()
         )
       {
         this.noReminderTicks = 0;
      }
   }

   public void handleRespawn() {
      this.noReminderTicks = 0;
   }
}
