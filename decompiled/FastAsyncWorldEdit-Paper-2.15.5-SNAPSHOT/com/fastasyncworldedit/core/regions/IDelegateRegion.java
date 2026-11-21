package com.fastasyncworldedit.core.regions;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.world.World;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IDelegateRegion extends Region {
   Region getRegion();

   @Nonnull
   @Override
   default Iterator<BlockVector3> iterator() {
      return this.getRegion().iterator();
   }

   @Override
   default BlockVector3 getMinimumPoint() {
      return this.getRegion().getMinimumPoint();
   }

   @Override
   default BlockVector3 getMaximumPoint() {
      return this.getRegion().getMaximumPoint();
   }

   @Override
   default Vector3 getCenter() {
      return this.getRegion().getCenter();
   }

   @Override
   default int getArea() {
      return this.getRegion().getArea();
   }

   @Override
   default int getWidth() {
      return this.getRegion().getWidth();
   }

   @Override
   default int getHeight() {
      return this.getRegion().getHeight();
   }

   @Override
   default int getLength() {
      return this.getRegion().getLength();
   }

   @Override
   default void expand(BlockVector3... changes) throws RegionOperationException {
      this.getRegion().expand(changes);
   }

   @Override
   default void contract(BlockVector3... changes) throws RegionOperationException {
      this.getRegion().contract(changes);
   }

   @Override
   default void shift(BlockVector3 change) throws RegionOperationException {
      this.getRegion().shift(change);
   }

   @Override
   default boolean contains(BlockVector3 position) {
      return this.getRegion().contains(position);
   }

   @Override
   default Set<BlockVector2> getChunks() {
      return this.getRegion().getChunks();
   }

   @Override
   default Set<BlockVector3> getChunkCubes() {
      return this.getRegion().getChunkCubes();
   }

   @Nullable
   @Override
   default World getWorld() {
      return this.getRegion().getWorld();
   }

   @Override
   default void setWorld(@Nullable World world) {
      this.getRegion().setWorld(world);
   }

   @Override
   default Region clone() {
      return this.getRegion().clone();
   }

   @Override
   default List<BlockVector2> polygonize(int maxPoints) {
      return this.getRegion().polygonize(maxPoints);
   }
}
