package ac.grim.grimac.checks.impl.scaffolding;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.GameMode;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;
import ac.grim.grimac.utils.change.BlockModification;
import ac.grim.grimac.utils.nmsutil.Materials;

@CheckData(
   name = "AirLiquidPlace",
   stableKey = "grim.scaffolding.air_liquid_place",
   description = "Placed a block against an invalid support"
)
public class AirLiquidPlace extends BlockPlaceCheck {
   public AirLiquidPlace(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onBlockPlace(BlockPlace place) {
      if (this.player.gamemode != GameMode.CREATIVE) {
         Vector3i blockPos = place.position;
         StateType placeAgainst = this.player.compensatedWorld.getBlockType(blockPos.getX(), blockPos.getY(), blockPos.getZ());
         int currentTick = GrimAPI.INSTANCE.getTickManager().currentTick;

         for (BlockModification blockModification : this.player
            .blockHistory
            .getRecentModifications(
               blockModificationx -> currentTick - blockModificationx.tick() < 2
                  && blockPos.equals(blockModificationx.location())
                  && (
                     blockModificationx.cause() == BlockModification.Cause.START_DIGGING
                        || blockModificationx.cause() == BlockModification.Cause.HANDLE_NETTY_SYNC_TRANSACTION
                  )
            )) {
            StateType stateType = blockModification.oldBlockContents().getType();
            if (!stateType.isAir() && !Materials.isNoPlaceLiquid(stateType)) {
               return;
            }
         }

         if ((placeAgainst.isAir() || Materials.isNoPlaceLiquid(placeAgainst)) && this.flag() && this.shouldModifyPackets() && this.shouldCancel()) {
            place.resync();
         }
      }
   }

   @Override
   public void onReload(ConfigManager config) {
      this.cancelVL = config.getIntElse(this.getConfigName() + ".cancelVL", 0);
   }
}
