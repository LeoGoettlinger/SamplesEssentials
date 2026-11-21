package com.fastasyncworldedit.core.extent;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.math.MutableVector3;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

public class PositionTransformExtent extends ResettableExtent {
   private transient MutableBlockVector3 mutable = new MutableBlockVector3();
   private transient BlockVector3 min;
   private Transform transform;

   public PositionTransformExtent(Extent parent, Transform transform) {
      super(parent);
      this.transform = transform;
   }

   @Override
   public ResettableExtent setExtent(Extent extent) {
      this.mutable = new MutableBlockVector3();
      this.min = null;
      return super.setExtent(extent);
   }

   @Override
   public void setOrigin(BlockVector3 pos) {
      this.min = pos;
   }

   private BlockVector3 getPos(BlockVector3 pos) {
      if (this.min == null) {
         this.min = pos;
      }

      this.mutable.mutX(pos.x() - this.min.x());
      this.mutable.mutY(pos.y() - this.min.y());
      this.mutable.mutZ(pos.z() - this.min.z());
      MutableVector3 tmp = new MutableVector3(this.transform.apply(this.mutable.toVector3()));
      return this.min.add(tmp.roundHalfUp().toBlockPoint());
   }

   private BlockVector3 getPos(int x, int y, int z) {
      if (this.min == null) {
         this.min = BlockVector3.at(x, y, z);
      }

      this.mutable.mutX(x - this.min.x());
      this.mutable.mutY(y - this.min.y());
      this.mutable.mutZ(z - this.min.z());
      MutableVector3 tmp = new MutableVector3(this.transform.apply(this.mutable.toVector3()));
      return this.min.add(tmp.roundHalfUp().toBlockPoint());
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return super.getBlock(this.getPos(x, y, z));
   }

   @Override
   public BlockState getBlock(BlockVector3 position) {
      return super.getBlock(this.getPos(position));
   }

   @Override
   public BaseBlock getFullBlock(BlockVector3 position) {
      return super.getFullBlock(this.getPos(position));
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      this.mutable.mutX(position.x());
      this.mutable.mutZ(position.z());
      this.mutable.mutY(position.y());
      return super.getBiome(this.getPos(this.mutable.x(), this.mutable.y(), this.mutable.z()));
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(int x, int y, int z, B block) throws WorldEditException {
      return this.setBlock(this.getPos(x, y, z), block);
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
      return super.setBlock(this.getPos(location), block);
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      this.mutable.mutX(position.x());
      this.mutable.mutZ(position.z());
      this.mutable.mutY(position.y());
      return super.setBiome(this.getPos(this.mutable.x(), this.mutable.y(), this.mutable.z()), biome);
   }

   public void setTransform(Transform transform) {
      this.transform = transform;
   }
}
