package com.sk89q.worldedit.function.block;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.internal.helper.MCDirections;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTag;

public class ExtentBlockCopy implements RegionFunction {
   private final Extent source;
   private final Extent destination;
   private final BlockVector3 from;
   private final BlockVector3 to;
   private final Transform transform;

   public ExtentBlockCopy(Extent source, BlockVector3 from, Extent destination, BlockVector3 to, Transform transform) {
      Preconditions.checkNotNull(source);
      Preconditions.checkNotNull(from);
      Preconditions.checkNotNull(destination);
      Preconditions.checkNotNull(to);
      Preconditions.checkNotNull(transform);
      this.source = source;
      this.from = from;
      this.destination = destination;
      this.to = to;
      this.transform = transform;
   }

   @Override
   public boolean apply(BlockVector3 position) throws WorldEditException {
      BaseBlock block = this.source.getFullBlock(position);
      BlockVector3 orig = position.subtract(this.from);
      BlockVector3 transformed = this.transform.apply(orig.toVector3()).toBlockPoint();
      block = this.transformNbtData(block);
      return this.destination.setBlock(transformed.add(this.to), block);
   }

   private BaseBlock transformNbtData(BaseBlock state) {
      LinCompoundTag tag = state.getNbt();
      if (tag != null) {
         LinTag<?> rotTag = tag.value().get("Rot");
         if (rotTag.value() instanceof Number number) {
            int rot = number.intValue();
            Direction direction = MCDirections.fromRotation(rot);
            if (direction != null) {
               Vector3 vector = this.transform.apply(direction.toVector()).subtract(this.transform.apply(Vector3.ZERO)).normalize();
               Direction newDirection = Direction.findClosest(vector, Direction.Flag.CARDINAL | Direction.Flag.ORDINAL | Direction.Flag.SECONDARY_ORDINAL);
               if (newDirection != null) {
                  return state.toBaseBlock(tag.toBuilder().putByte("Rot", (byte)MCDirections.toRotation(newDirection)).build());
               }
            }
         }
      }

      return state;
   }
}
