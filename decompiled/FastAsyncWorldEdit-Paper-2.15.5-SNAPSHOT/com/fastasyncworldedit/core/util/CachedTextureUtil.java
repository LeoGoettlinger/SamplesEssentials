package com.fastasyncworldedit.core.util;

import com.sk89q.worldedit.world.block.BlockType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.FileNotFoundException;

public class CachedTextureUtil extends DelegateTextureUtil {
   protected final TextureUtil parent;
   protected final transient Int2ObjectOpenHashMap<BlockType> colorBlockMap;
   protected final transient Int2ObjectOpenHashMap<Integer> colorBiomeMap;
   protected final transient Int2ObjectOpenHashMap<BlockType[]> colorLayerMap;

   public CachedTextureUtil(TextureUtil parent) throws FileNotFoundException {
      super(parent);
      this.parent = parent;
      this.colorBlockMap = new Int2ObjectOpenHashMap();
      this.colorBiomeMap = new Int2ObjectOpenHashMap();
      this.colorLayerMap = new Int2ObjectOpenHashMap();
   }

   protected CachedTextureUtil(
      TextureUtil parent,
      Int2ObjectOpenHashMap<BlockType> colorBlockMap,
      Int2ObjectOpenHashMap<Integer> colorBiomeMap,
      Int2ObjectOpenHashMap<BlockType[]> colorLayerMap
   ) throws FileNotFoundException {
      super(parent);
      this.parent = parent;
      this.colorBlockMap = new Int2ObjectOpenHashMap(colorBlockMap);
      this.colorBiomeMap = new Int2ObjectOpenHashMap(colorBiomeMap);
      this.colorLayerMap = new Int2ObjectOpenHashMap(colorLayerMap);
   }

   @Override
   public TextureUtil fork() {
      try {
         return new CachedTextureUtil(this.parent.fork(), this.colorBlockMap, this.colorBiomeMap, this.colorLayerMap);
      } catch (FileNotFoundException var2) {
         throw new RuntimeException(var2);
      }
   }

   @Override
   public BlockType[] getNearestLayer(int color) {
      BlockType[] closest = (BlockType[])this.colorLayerMap.get(color);
      if (closest != null) {
         return closest;
      } else {
         closest = this.parent.getNearestLayer(color);
         if (closest != null) {
            this.colorLayerMap.put(color, (BlockType[])closest.clone());
         }

         return closest;
      }
   }

   @Override
   public TextureUtil.BiomeColor getNearestBiome(int color) {
      Integer value = (Integer)this.colorBiomeMap.get(color);
      if (value != null) {
         return this.getBiome(value);
      } else {
         TextureUtil.BiomeColor result = this.parent.getNearestBiome(color);
         if (result != null) {
            this.colorBiomeMap.put(color, result.id);
         }

         return result;
      }
   }

   @Override
   public BlockType getNearestBlock(int color) {
      BlockType value = (BlockType)this.colorBlockMap.get(color);
      if (value != null) {
         return value;
      } else {
         BlockType result = this.parent.getNearestBlock(color);
         if (result != null) {
            this.colorBlockMap.put(color, result);
         }

         return result;
      }
   }
}
