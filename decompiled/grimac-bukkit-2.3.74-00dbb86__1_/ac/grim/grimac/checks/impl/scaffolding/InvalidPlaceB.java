package ac.grim.grimac.checks.impl.scaffolding;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;

@CheckData(
   name = "InvalidPlaceB",
   stableKey = "grim.scaffolding.invalid_place_b",
   description = "Sent impossible block face id"
)
public class InvalidPlaceB extends BlockPlaceCheck {
   private static final Verbose V = Verbose.of("direction={sint}");

   public InvalidPlaceB(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onBlockPlace(BlockPlace place) {
      if (place.getFaceId() != 255 || !PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_8)) {
         if (place.getFaceId() < 0 || place.getFaceId() > 5) {
            int direction = place.getFaceId();
            if (this.flag(V.write(this.verbose()).sint(direction)) && this.shouldModifyPackets() && this.shouldCancel()) {
               place.resync();
            }
         }
      }
   }
}
