package com.fastasyncworldedit.core.util.collection;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class SummedColorTable {
   private final long[] reds;
   private final long[] greens;
   private final long[] blues;
   private final long[] alpha;
   private final int[] hasAlpha;
   private final int length;
   private final int width;
   private final float[] areaInverses;
   private final float[] alphaInverse;

   public SummedColorTable(BufferedImage image, final boolean calculateAlpha) {
      int[] raw = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
      this.width = image.getWidth();
      this.length = image.getHeight();
      this.reds = new long[raw.length];
      this.greens = new long[raw.length];
      this.blues = new long[raw.length];
      this.hasAlpha = new int[raw.length];
      this.alpha = calculateAlpha ? new long[raw.length] : null;
      this.alphaInverse = calculateAlpha ? new float[256] : null;
      this.areaInverses = new float[1048576];

      for (int i = 0; i < this.areaInverses.length; i++) {
         this.areaInverses[i] = 1.0F / (i + 1);
      }

      int index = 0;
      if (calculateAlpha) {
         for (int i = 0; i < this.length; i++) {
            for (int j = 0; j < this.width; index++) {
               int color = raw[index];
               int alpha = color >> 24 & 0xFF;
               int red;
               int green;
               int blue;
               switch (alpha) {
                  case 0:
                     blue = 0;
                     green = 0;
                     red = 0;
                     break;
                  case 255:
                     red = color >> 16 & 0xFF;
                     green = color >> 8 & 0xFF;
                     blue = color >> 0 & 0xFF;
                     break;
                  default:
                     red = (color >> 16 & 0xFF) * alpha >> 8;
                     green = (color >> 8 & 0xFF) * alpha >> 8;
                     blue = (color >> 0 & 0xFF) * alpha >> 8;
               }

               this.reds[index] = this.getVal(i, j, index, (long)red, this.reds);
               this.greens[index] = this.getVal(i, j, index, (long)green, this.greens);
               this.blues[index] = this.getVal(i, j, index, (long)blue, this.blues);
               this.alpha[index] = this.getVal(i, j, index, (long)alpha, this.alpha);
               this.hasAlpha[index] = this.getVal(i, j, index, alpha > 0 ? 1 : 0, this.hasAlpha);
               j++;
            }
         }

         for (int i = 1; i < this.alphaInverse.length; i++) {
            this.alphaInverse[i] = 256.0F / i;
         }
      } else {
         for (int i = 0; i < this.length; i++) {
            for (int j = 0; j < this.width; index++) {
               int color = raw[index];
               int red;
               int green;
               int blue;
               int alpha;
               if (color >> 24 != 0) {
                  alpha = 1;
                  red = color >> 16 & 0xFF;
                  green = color >> 8 & 0xFF;
                  blue = color >> 0 & 0xFF;
               } else {
                  blue = 0;
                  green = 0;
                  red = 0;
                  alpha = 0;
               }

               this.reds[index] = this.getVal(i, j, index, (long)red, this.reds);
               this.greens[index] = this.getVal(i, j, index, (long)green, this.greens);
               this.blues[index] = this.getVal(i, j, index, (long)blue, this.blues);
               this.hasAlpha[index] = this.getVal(i, j, index, alpha, this.hasAlpha);
               j++;
            }
         }
      }
   }

   public int averageRGB(int x1, int z1, int x2, int z2) {
      int minX = Math.max(0, x1);
      int minZ = Math.max(0, z1);
      int maxX = Math.min(this.width - 1, x2);
      int maxZ = Math.min(this.length - 1, z2);
      int XZ = maxZ * this.width + maxX;
      long totRed = this.reds[XZ];
      long totGreen = this.greens[XZ];
      long totBlue = this.blues[XZ];
      int area = this.hasAlpha[XZ];
      if (minX > 0) {
         int pos = maxZ * this.width + minX - 1;
         totRed -= this.reds[pos];
         totGreen -= this.greens[pos];
         totBlue -= this.blues[pos];
         area -= this.hasAlpha[pos];
      }

      if (minZ > 0) {
         int pos = minZ * this.width - this.width + maxX;
         totRed -= this.reds[pos];
         totGreen -= this.greens[pos];
         totBlue -= this.blues[pos];
         area -= this.hasAlpha[pos];
      }

      if (minZ > 0 && minX > 0) {
         int pos = minZ * this.width - this.width + minX - 1;
         totRed += this.reds[pos];
         totGreen += this.greens[pos];
         totBlue += this.blues[pos];
         area += this.hasAlpha[pos];
      }

      if (area == 0) {
         return 0;
      } else {
         float factor = this.areaInverses[area - 1];
         return -16777216 + ((int)((float)totRed * factor) << 16) + ((int)((float)totGreen * factor) << 8) + ((int)((float)totBlue * factor) << 0);
      }
   }

   public int averageRGBA(int x1, int z1, int x2, int z2) {
      int minX = Math.max(0, x1);
      int minZ = Math.max(0, z1);
      int maxX = Math.min(this.width - 1, x2);
      int maxZ = Math.min(this.length - 1, z2);
      int XZ = maxZ * this.width + maxX;
      long totRed = this.reds[XZ];
      long totGreen = this.greens[XZ];
      long totBlue = this.blues[XZ];
      long totAlpha = this.alpha[XZ];
      int area = this.hasAlpha[XZ];
      if (minX > 0) {
         int pos = maxZ * this.width + minX - 1;
         totRed -= this.reds[pos];
         totGreen -= this.greens[pos];
         totBlue -= this.blues[pos];
         totAlpha -= this.alpha[pos];
         area -= this.hasAlpha[pos];
      }

      if (minZ > 0) {
         int pos = minZ * this.width - this.width + maxX;
         totRed -= this.reds[pos];
         totGreen -= this.greens[pos];
         totBlue -= this.blues[pos];
         totAlpha -= this.alpha[pos];
         area -= this.hasAlpha[pos];
      }

      if (minZ > 0 && minX > 0) {
         int pos = minZ * this.width - this.width + minX - 1;
         totRed += this.reds[pos];
         totGreen += this.greens[pos];
         totBlue += this.blues[pos];
         totAlpha += this.alpha[pos];
         area += this.hasAlpha[pos];
      }

      if (totAlpha == 0L) {
         return 0;
      } else {
         float factor = this.areaInverses[area - 1];
         float alpha = (float)totAlpha * factor;
         factor = factor * 256.0F / alpha;
         return (Math.clamp((long)((int)alpha), 0, 255) << 24)
            + ((int)((float)totRed * factor) << 16)
            + ((int)((float)totGreen * factor) << 8)
            + ((int)((float)totBlue * factor) << 0);
      }
   }

   private long getVal(int row, int col, int index, long curr, long[] summed) {
      if (index == 0) {
         return curr;
      } else if (row == 0 && col != 0) {
         long leftSum = summed[index - 1];
         return curr + leftSum;
      } else if (row != 0 && col == 0) {
         long topSum = summed[index - this.width];
         return curr + topSum;
      } else {
         long leftSum = summed[index - 1];
         long topSum = summed[index - this.width];
         long topLeftSum = summed[index - this.width - 1];
         return curr + leftSum + topSum - topLeftSum;
      }
   }

   private int getVal(int row, int col, int index, int curr, int[] summed) {
      if (index == 0) {
         return curr;
      } else if (row == 0 && col != 0) {
         int leftSum = summed[index - 1];
         return curr + leftSum;
      } else if (row != 0 && col == 0) {
         int topSum = summed[index - this.width];
         return curr + topSum;
      } else {
         int leftSum = summed[index - 1];
         int topSum = summed[index - this.width];
         int topLeftSum = summed[index - this.width - 1];
         return curr + leftSum + topSum - topLeftSum;
      }
   }
}
