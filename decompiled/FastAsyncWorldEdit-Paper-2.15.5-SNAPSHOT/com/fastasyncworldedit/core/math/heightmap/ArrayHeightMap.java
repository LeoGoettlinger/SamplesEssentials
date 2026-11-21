package com.fastasyncworldedit.core.math.heightmap;

public class ArrayHeightMap extends ScalableHeightMap {
   private final char[][] height;
   private final int width;
   private final int length;
   private final double scale;
   private double rx;
   private double rz;

   public ArrayHeightMap(char[][] height, double scale) {
      this.setSize(5);
      this.height = height;
      this.width = height.length;
      this.length = height[0].length;
      this.scale = scale;
   }

   @Override
   public void setSize(int size) {
      super.setSize(size);
      this.rx = (double)this.width / (size << 1);
      this.rz = (double)this.length / (size << 1);
   }

   @Override
   public double getHeight(int x, int z) {
      x = (int)Math.clamp((x + this.size) * this.rx, 0.0, (double)(this.width - 1));
      z = (int)Math.clamp((z + this.size) * this.rz, 0.0, (double)(this.length - 1));
      return this.height[x][z] * this.size / this.scale;
   }
}
