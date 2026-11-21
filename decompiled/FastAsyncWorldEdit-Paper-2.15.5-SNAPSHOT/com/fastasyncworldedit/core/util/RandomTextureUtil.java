package com.fastasyncworldedit.core.util;

import com.sk89q.worldedit.world.block.BlockType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.FileNotFoundException;
import java.util.concurrent.ThreadLocalRandom;

public class RandomTextureUtil extends CachedTextureUtil {
   private int index;
   private final int[] biomeMixBuffer = new int[3];
   private final Int2ObjectOpenHashMap<Integer> offsets = new Int2ObjectOpenHashMap();
   private final Int2ObjectOpenHashMap<int[]> biomeMixes = new Int2ObjectOpenHashMap();

   public RandomTextureUtil(TextureUtil parent) throws FileNotFoundException {
      super(parent);
   }

   private RandomTextureUtil(
      TextureUtil parent,
      Int2ObjectOpenHashMap<BlockType> colorBlockMap,
      Int2ObjectOpenHashMap<Integer> colorBiomeMap,
      Int2ObjectOpenHashMap<BlockType[]> colorLayerMap
   ) throws FileNotFoundException {
      super(parent, colorBlockMap, colorBiomeMap, colorLayerMap);
   }

   @Override
   public TextureUtil fork() {
      try {
         return new RandomTextureUtil(this.parent, this.colorBlockMap, this.colorBiomeMap, this.colorLayerMap);
      } catch (FileNotFoundException var2) {
         throw new RuntimeException(var2);
      }
   }

   protected int addRandomColor(int c1, int c2) {
      int red1 = c1 >> 16 & 0xFF;
      int green1 = c1 >> 8 & 0xFF;
      int blue1 = c1 >> 0 & 0xFF;
      byte red2 = (byte)(c2 >> 16);
      byte green2 = (byte)(c2 >> 8);
      byte blue2 = (byte)(c2 >> 0);
      int red = Math.clamp((long)(red1 + this.random(red2)), 0, 255);
      int green = Math.clamp((long)(green1 + this.random(green2)), 0, 255);
      int blue = Math.clamp((long)(blue1 + this.random(blue2)), 0, 255);
      return (red << 16) + (green << 8) + (blue << 0) + -16777216;
   }

   private int random(int i) {
      if (i < 0) {
         int i1 = -i;
         return -ThreadLocalRandom.current().nextInt(i1);
      } else {
         return i == 0 ? 0 : ThreadLocalRandom.current().nextInt(i);
      }
   }

   @Override
   public boolean getIsBlockCloserThanBiome(char[] blockAndBiomeIdOutput, int color, int biomePriority) {
      BlockType block = this.getNearestBlock(color);
      int[] mix = (int[])this.biomeMixes.getOrDefault(color, null);
      if (mix == null) {
         int average = this.getBiomeMix(this.biomeMixBuffer, color);
         mix = new int[4];
         System.arraycopy(this.biomeMixBuffer, 0, mix, 0, 3);
         mix[3] = average;
         this.biomeMixes.put(color, mix);
      }

      if (++this.index > 2) {
         this.index = 0;
      }

      int biomeId = mix[this.index];
      int biomeAvColor = mix[3];
      int blockColor = this.getColor(block);
      blockAndBiomeIdOutput[0] = block.getDefaultState().getOrdinalChar();
      blockAndBiomeIdOutput[1] = (char)biomeId;
      return colorDistance(biomeAvColor, color) - biomePriority > colorDistance(blockColor, color);
   }

   @Override
   public TextureUtil.BiomeColor getNearestBiome(int color) {
      int[] mix = (int[])this.biomeMixes.getOrDefault(color, null);
      if (mix == null) {
         int average = this.getBiomeMix(this.biomeMixBuffer, color);
         mix = new int[4];
         System.arraycopy(this.biomeMixBuffer, 0, mix, 0, 3);
         mix[3] = average;
         this.biomeMixes.put(color, mix);
      }

      if (++this.index > 2) {
         this.index = 0;
      }

      int biomeId = mix[this.index];
      return this.getBiome(biomeId);
   }

   @Override
   public BlockType getNearestBlock(int color) {
      int offsetColor = (Integer)this.offsets.getOrDefault(color, 0);
      if (offsetColor != 0) {
         offsetColor = this.addRandomColor(color, offsetColor);
      } else {
         offsetColor = color;
      }

      BlockType res = super.getNearestBlock(offsetColor);
      if (res == null) {
         return null;
      } else {
         int newColor = this.getColor(res);
         byte dr = (byte)((color >> 16 & 0xFF) - (newColor >> 16 & 0xFF));
         byte dg = (byte)((color >> 8 & 0xFF) - (newColor >> 8 & 0xFF));
         byte db = (byte)((color >> 0 & 0xFF) - (newColor >> 0 & 0xFF));
         this.offsets.put(color, (dr << 16) + (dg << 8) + (db << 0));
         return res;
      }
   }
}
