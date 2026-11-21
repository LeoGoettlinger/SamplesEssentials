package ac.grim.grimac.checks.impl.scaffolding;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;

@CheckData(
   name = "PositionPlace",
   stableKey = "grim.scaffolding.position_place",
   description = "Placed a block against a hidden face"
)
public class PositionPlace extends BlockPlaceCheck {
   public PositionPlace(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onBlockPlace(BlockPlace place) {
      if (place.material != StateTypes.SCAFFOLDING && !this.player.inVehicle()) {
         SimpleCollisionBox combined = this.getCombinedBox(place);
         double[] possibleEyeHeights = this.player.getPossibleEyeHeights();
         double minEyeHeight = Double.MAX_VALUE;
         double maxEyeHeight = Double.MIN_VALUE;

         for (double height : possibleEyeHeights) {
            minEyeHeight = Math.min(minEyeHeight, height);
            maxEyeHeight = Math.max(maxEyeHeight, height);
         }

         double movementThreshold = this.player.packetStateData.didLastMovementIncludePosition && !this.player.canSkipTicks()
            ? 0.0
            : this.player.getMovementThreshold();
         SimpleCollisionBox eyePositions = new SimpleCollisionBox(
            this.player.x, this.player.y + minEyeHeight, this.player.z, this.player.x, this.player.y + maxEyeHeight, this.player.z
         );
         eyePositions.expand(movementThreshold);
         if (!eyePositions.isIntersected(combined)) {
            boolean flag = switch (place.getFace()) {
               case NORTH -> eyePositions.minZ > combined.minZ;
               case SOUTH -> eyePositions.maxZ < combined.maxZ;
               case EAST -> eyePositions.maxX < combined.maxX;
               case WEST -> eyePositions.minX > combined.minX;
               case UP -> eyePositions.maxY < combined.maxY;
               case DOWN -> eyePositions.minY > combined.minY;
               default -> false;
            };
            if (flag && this.flag() && this.shouldModifyPackets() && this.shouldCancel()) {
               place.resync();
            }
         }
      }
   }
}
