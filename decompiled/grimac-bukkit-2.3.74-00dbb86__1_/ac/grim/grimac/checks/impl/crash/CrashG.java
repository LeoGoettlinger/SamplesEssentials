package ac.grim.grimac.checks.impl.crash;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUseItem;
import ac.grim.grimac.utils.anticheat.update.BlockBreak;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;

@CheckData(
   name = "CrashG",
   stableKey = "grim.crash.negative_sequence",
   description = "Sent negative sequence id"
)
public class CrashG extends BlockPlaceCheck {
   public CrashG(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.USE_ITEM && this.isSupportedVersion()) {
         WrapperPlayClientUseItem use = new WrapperPlayClientUseItem(event);
         if (use.getSequence() < 0) {
            this.flag();
            event.setCancelled(true);
            this.player.onPacketCancel();
         }
      }
   }

   @Override
   public void onBlockBreak(BlockBreak blockBreak) {
      if (blockBreak.sequence < 0 && this.isSupportedVersion()) {
         this.flag();
         blockBreak.cancel();
      }
   }

   @Override
   public void onBlockPlace(BlockPlace place) {
      if (place.sequence < 0 && this.isSupportedVersion()) {
         this.flag();
         place.resync();
      }
   }

   private boolean isSupportedVersion() {
      return this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_19)
         && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19);
   }
}
