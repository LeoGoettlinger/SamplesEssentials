package com.sk89q.worldedit.regions.shape;

import com.fastasyncworldedit.core.math.MutableVector3;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.expression.ExpressionEnvironment;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;

public class WorldEditExpressionEnvironment implements ExpressionEnvironment {
   private static final Vector3 BLOCK_CENTER_OFFSET = Vector3.at(0.5, 0.5, 0.5);
   private final Vector3 unit;
   private final Vector3 zero2;
   private Vector3 current = new MutableVector3(Vector3.ZERO);
   private final Extent extent;

   public WorldEditExpressionEnvironment(EditSession editSession, Vector3 unit, Vector3 zero) {
      this((Extent)editSession, unit, zero);
   }

   public WorldEditExpressionEnvironment(Extent extent, Vector3 unit, Vector3 zero) {
      this.extent = extent;
      this.unit = unit;
      this.zero2 = zero.add(BLOCK_CENTER_OFFSET);
   }

   public BlockVector3 toWorld(double x, double y, double z) {
      return Vector3.at(x, y, z).multiply(this.unit).add(this.zero2).toBlockPoint();
   }

   @Override
   public int getBlockType(double x, double y, double z) {
      return this.extent.getBlock(this.toWorld(x, y, z)).getBlockType().getLegacyCombinedId() >> 4;
   }

   @Override
   public int getBlockData(double x, double y, double z) {
      return this.extent.getBlock(this.toWorld(x, y, z)).getBlockType().getLegacyCombinedId() & 15;
   }

   @Override
   public int getBlockTypeAbs(double x, double y, double z) {
      return this.extent.getBlock(BlockVector3.at(x, y, z)).getBlockType().getLegacyCombinedId() >> 4;
   }

   @Override
   public int getBlockDataAbs(double x, double y, double z) {
      return this.extent.getBlock(this.toWorld(x, y, z)).getBlockType().getLegacyCombinedId() & 15;
   }

   @Override
   public int getBlockTypeRel(double x, double y, double z) {
      return this.extent.getBlock(this.toWorldRel(x, y, z).toBlockPoint()).getBlockType().getLegacyCombinedId() >> 4;
   }

   @Override
   public int getBlockDataRel(double x, double y, double z) {
      return this.extent.getBlock(this.toWorldRel(x, y, z).toBlockPoint()).getBlockType().getLegacyCombinedId() & 15;
   }

   public void setCurrentBlock(int x, int y, int z) {
      this.current.setComponents(x, y, z);
   }

   public Vector3 toWorldRel(double x, double y, double z) {
      return this.current.add(x, y, z);
   }

   public WorldEditExpressionEnvironment clone() {
      return new WorldEditExpressionEnvironment(this.extent, this.unit, this.zero2.subtract(BLOCK_CENTER_OFFSET));
   }

   public void setCurrentBlock(Vector3 current) {
      this.current = current;
   }
}
