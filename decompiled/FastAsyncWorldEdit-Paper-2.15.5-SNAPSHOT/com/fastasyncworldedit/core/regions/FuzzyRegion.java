package com.fastasyncworldedit.core.regions;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.math.BlockVectorSet;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.RecursiveVisitor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.AbstractRegion;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.world.World;
import java.util.Iterator;
import javax.annotation.Nonnull;

public class FuzzyRegion extends AbstractRegion {
   private final Mask mask;
   private final BlockVectorSet set = new BlockVectorSet();
   private int minX;
   private int minY;
   private int minZ;
   private int maxX;
   private int maxY;
   private int maxZ;
   private Extent extent;

   public FuzzyRegion(World world, Extent editSession, Mask mask) {
      super(world);
      this.minX = this.minY = this.minZ = Integer.MAX_VALUE;
      this.maxX = this.maxY = this.maxZ = Integer.MIN_VALUE;
      this.extent = editSession;
      this.mask = mask;
   }

   public Mask getMask() {
      return this.mask;
   }

   @Override
   public int getArea() {
      return this.set.size();
   }

   public void select(BlockVector3 position) {
      RecursiveVisitor search = new RecursiveVisitor(this.mask, p -> {
         this.setMinMax(p.x(), p.y(), p.z());
         return true;
      }, 256, this.extent.getMinY(), this.extent.getMaxY(), this.extent);
      search.setVisited(this.set);
      this.mask.test(position);
      search.visit(position);
      Operations.completeBlindly(search);
   }

   @Nonnull
   @Override
   public Iterator<BlockVector3> iterator() {
      return this.set.iterator();
   }

   private void setMinMax(int x, int y, int z) {
      if (x > this.maxX) {
         this.maxX = x;
      }

      if (x < this.minX) {
         this.minX = x;
      }

      if (z > this.maxZ) {
         this.maxZ = z;
      }

      if (z < this.minZ) {
         this.minZ = z;
      }

      if (y > this.maxY) {
         this.maxY = y;
      }

      if (y < this.minY) {
         this.minY = y;
      }
   }

   public final void set(int x, int y, int z) throws RegionOperationException {
      this.set.add(x, y, z);
      this.setMinMax(x, y, z);
   }

   @Override
   public boolean contains(int x, int y, int z) {
      return this.set.contains(x, y, z);
   }

   @Override
   public BlockVector3 getMinimumPoint() {
      return BlockVector3.at(this.minX, this.minY, this.minZ);
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      return BlockVector3.at(this.maxX, this.maxY, this.maxZ);
   }

   @Override
   public void expand(BlockVector3... changes) throws RegionOperationException {
      throw new RegionOperationException(Caption.of("fawe.error.selection-expand"));
   }

   @Override
   public void contract(BlockVector3... changes) throws RegionOperationException {
      throw new RegionOperationException(Caption.of("fawe.error.selection-contract"));
   }

   @Override
   public boolean contains(BlockVector3 position) {
      return this.contains(position.x(), position.y(), position.z());
   }

   @Override
   public void shift(BlockVector3 change) throws RegionOperationException {
      throw new RegionOperationException(Caption.of("fawe.error.selection-shift"));
   }

   public void setExtent(EditSession extent) {
      this.extent = extent;
   }

   @Override
   public boolean containsEntireCuboid(int bx, int tx, int by, int ty, int bz, int tz) {
      return false;
   }
}
