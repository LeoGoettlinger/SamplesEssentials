package ac.grim.grimac.checks.impl.scaffolding;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.math.Vector3dm;
import ac.grim.grimac.utils.math.VectorUtils;

@CheckData(
   name = "FarPlace",
   stableKey = "grim.scaffolding.far_place",
   description = "Placing blocks from too far away"
)
public class FarPlace extends BlockPlaceCheck {
   public FarPlace(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onBlockPlace(BlockPlace place) {
      if (this.player.cameraEntity.isSelf() && !this.player.inVehicle()) {
         Vector3i blockPos = place.position;
         if (place.material != StateTypes.SCAFFOLDING) {
            double min = Double.MAX_VALUE;
            double[] possibleEyeHeights = this.player.getPossibleEyeHeights();

            for (double d : possibleEyeHeights) {
               SimpleCollisionBox box = new SimpleCollisionBox(blockPos);
               Vector3dm best = VectorUtils.cutBoxToVector(this.player.x, this.player.y + d, this.player.z, box);
               min = Math.min(min, best.distanceSquared(this.player.x, this.player.y + d, this.player.z));
            }

            double maxReach = this.player.compensatedEntities.self.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
            double threshold = this.player.getMovementThreshold();
            maxReach += Math.hypot(threshold, threshold);
            if (min > maxReach * maxReach && this.flag() && this.shouldModifyPackets() && this.shouldCancel()) {
               place.resync();
            }
         }
      }
   }
}
