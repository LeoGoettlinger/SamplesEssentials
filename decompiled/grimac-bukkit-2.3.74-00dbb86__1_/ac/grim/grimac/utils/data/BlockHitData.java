package ac.grim.grimac.utils.data;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.utils.math.Vector3dm;
import lombok.Generated;

public class BlockHitData extends HitData {
   Vector3i position;
   WrappedBlockState state;
   BlockFace closestDirection;

   public BlockHitData(Vector3i position, Vector3dm blockHitLocation, BlockFace closestDirection, WrappedBlockState state) {
      super(blockHitLocation);
      this.position = position;
      this.closestDirection = closestDirection;
      this.state = state;
   }

   public Vector3d getRelativeBlockHitLocation() {
      return new Vector3d(
         this.blockHitLocation.getX() - this.position.getX(),
         this.blockHitLocation.getY() - this.position.getY(),
         this.blockHitLocation.getZ() - this.position.getZ()
      );
   }

   @Generated
   public Vector3i position() {
      return this.position;
   }

   @Generated
   public WrappedBlockState state() {
      return this.state;
   }

   @Generated
   public BlockFace closestDirection() {
      return this.closestDirection;
   }

   @Generated
   @Override
   public String toString() {
      return "BlockHitData(position=" + this.position() + ", state=" + this.state() + ", closestDirection=" + this.closestDirection() + ")";
   }
}
