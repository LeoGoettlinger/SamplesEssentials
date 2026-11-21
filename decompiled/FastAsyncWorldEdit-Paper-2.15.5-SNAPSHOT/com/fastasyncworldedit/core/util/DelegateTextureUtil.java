package com.fastasyncworldedit.core.util;

import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DelegateTextureUtil extends TextureUtil {
   private final TextureUtil parent;

   public DelegateTextureUtil(TextureUtil parent) throws FileNotFoundException {
      super(parent.getFolder());
      this.parent = parent;
   }

   @Override
   public TextureUtil fork() {
      try {
         return new DelegateTextureUtil(this.parent.fork());
      } catch (FileNotFoundException var2) {
         throw new RuntimeException(var2);
      }
   }

   @Override
   public BlockType getNearestBlock(int color) {
      return this.parent.getNearestBlock(color);
   }

   @Override
   public BlockType getNextNearestBlock(int color) {
      return this.parent.getNextNearestBlock(color);
   }

   @Override
   public BlockType[] getNearestLayer(int color) {
      return this.parent.getNearestLayer(color);
   }

   @Override
   public BlockType getLighterBlock(BlockType block) {
      return this.parent.getLighterBlock(block);
   }

   @Override
   public BlockType getDarkerBlock(BlockType block) {
      return this.parent.getDarkerBlock(block);
   }

   @Override
   public BlockType getLighterBlock(final int color) {
      return this.parent.getLighterBlock(color);
   }

   @Override
   public BlockType getDarkerBlock(final int color) {
      return this.parent.getDarkerBlock(color);
   }

   @Override
   public int getColor(BlockType block) {
      return this.parent.getColor(block);
   }

   @Override
   public int getColor(final BiomeType biome) {
      return this.parent.getColor(biome);
   }

   @Override
   public boolean getIsBlockCloserThanBiome(char[] blockAndBiomeIdOutput, int color, int biomePriority) {
      return this.parent.getIsBlockCloserThanBiome(blockAndBiomeIdOutput, color, biomePriority);
   }

   @Override
   public int getBiomeMix(int[] biomeIdsOutput, int color) {
      return this.parent.getBiomeMix(biomeIdsOutput, color);
   }

   @Override
   public TextureUtil.BiomeColor getBiome(int biome) {
      return this.parent.getBiome(biome);
   }

   @Override
   public TextureUtil.BiomeColor getNearestBiome(int color) {
      return this.parent.getNearestBiome(color);
   }

   @Override
   public File getFolder() {
      return this.parent.getFolder();
   }

   @Override
   public void calculateLayerArrays() {
      this.parent.calculateLayerArrays();
   }

   @Override
   public void loadModTextures() throws IOException {
      this.parent.loadModTextures();
   }

   @Override
   public BlockType getNearestBlock(BlockType block, boolean darker) {
      return this.parent.getNearestBlock(block, darker);
   }

   @Override
   public BlockType getNearestBlock(int color, boolean darker) {
      return this.parent.getNearestBlock(color, darker);
   }

   @Override
   public boolean hasAlpha(int color) {
      return this.parent.hasAlpha(color);
   }
}
