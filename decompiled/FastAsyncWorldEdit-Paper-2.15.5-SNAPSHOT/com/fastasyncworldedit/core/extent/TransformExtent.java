package com.fastasyncworldedit.core.extent;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.math.MutableVector3;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

@Deprecated(
   forRemoval = true,
   since = "2.9.2"
)
public class TransformExtent extends BlockTransformExtent {
   private final MutableVector3 mutable1 = new MutableVector3();
   private final MutableBlockVector3 mutable2 = new MutableBlockVector3();
   private BlockVector3 min;

   public TransformExtent(Extent parent) {
      super(parent);
   }

   @Override
   public ResettableExtent setExtent(Extent extent) {
      this.min = null;
      return super.setExtent(extent);
   }

   @Override
   public BlockVector3 getMinimumPoint() {
      BlockVector3 pos1 = this.getPos(super.getMinimumPoint());
      BlockVector3 pos2 = this.getPos(super.getMaximumPoint());
      return pos1.getMinimum(pos2);
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      BlockVector3 pos1 = this.getPos(super.getMinimumPoint());
      BlockVector3 pos2 = this.getPos(super.getMaximumPoint());
      return pos1.getMaximum(pos2);
   }

   @Override
   public void setOrigin(BlockVector3 pos) {
      this.min = pos;
   }

   public BlockVector3 getPos(BlockVector3 pos) {
      if (this.min == null) {
         this.min = pos;
      }

      this.mutable1.mutX(pos.x() - this.min.x());
      this.mutable1.mutY(pos.y() - this.min.y());
      this.mutable1.mutZ(pos.z() - this.min.z());
      Vector3 tmp = this.getTransform().apply(this.mutable1);
      this.mutable2.mutX(tmp.x() + this.min.x());
      this.mutable2.mutY(tmp.y() + this.min.y());
      this.mutable2.mutZ(tmp.z() + this.min.z());
      return this.mutable2;
   }

   public BlockVector3 getPos(int x, int y, int z) {
      if (this.min == null) {
         this.min = BlockVector3.at(x, y, z);
      }

      this.mutable1.mutX(x - this.min.x());
      this.mutable1.mutY(y - this.min.y());
      this.mutable1.mutZ(z - this.min.z());
      Vector3 tmp = this.getTransform().apply(this.mutable1);
      this.mutable2.mutX(tmp.x() + this.min.x());
      this.mutable2.mutY(tmp.y() + this.min.y());
      this.mutable2.mutZ(tmp.z() + this.min.z());
      return tmp.toBlockPoint();
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      BlockVector3 p = this.getPos(x, y, z);
      return this.transform(super.getBlock(p.x(), p.y(), p.z()));
   }

   @Override
   public BaseBlock getFullBlock(BlockVector3 position) {
      return this.transform(super.getFullBlock(this.getPos(position)));
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      BlockVector3 p = this.getPos(x, y, z);
      return super.getBiomeType(p.x(), p.y(), p.z());
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      return super.setBlock(this.getPos(x, y, z), this.transformInverse(block));
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
      return super.setBlock(this.getPos(location), (B)this.transformInverse(block));
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      BlockVector3 p = this.getPos(x, y, z);
      return super.setBiome(p.x(), p.y(), p.z(), biome);
   }
}
