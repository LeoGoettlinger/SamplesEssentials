package com.fastasyncworldedit.bukkit.adapter;

import com.fastasyncworldedit.core.queue.implementation.packet.ChunkPacket;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

public abstract class MapChunkUtil<T> {
   protected Field fieldX;
   protected Field fieldZ;
   protected Field fieldHeightMap;
   protected Field fieldBitMask;
   protected Field fieldChunkData;
   protected Field fieldBlockEntities;
   protected Field fieldFull;

   protected abstract T createPacket();

   public T create(BukkitImplAdapter adapter, ChunkPacket packet) {
      try {
         int bitMask = packet.getChunk().getBitMask();
         T nmsPacket = this.createPacket();
         this.fieldX.setInt(nmsPacket, packet.getChunkX());
         this.fieldZ.setInt(nmsPacket, packet.getChunkZ());
         this.fieldBitMask.set(nmsPacket, packet.getChunk().getBitMask());
         if (this.fieldHeightMap != null) {
            Object heightMap = adapter.fromNative(packet.getHeightMap());
            this.fieldHeightMap.set(nmsPacket, heightMap);
         }

         this.fieldChunkData.set(nmsPacket, packet.getSectionBytes());
         Map<BlockVector3, CompoundTag> tiles = packet.getChunk().getTiles();
         ArrayList<Object> nmsTiles = new ArrayList<>(tiles.size());

         for (Entry<BlockVector3, CompoundTag> entry : tiles.entrySet()) {
            Object nmsTag = adapter.fromNative(entry.getValue());
            nmsTiles.add(nmsTag);
         }

         this.fieldBlockEntities.set(nmsPacket, nmsTiles);
         this.fieldFull.set(nmsPacket, packet.isFull());
         return nmsPacket;
      } catch (Exception var10) {
         var10.printStackTrace();
         return null;
      }
   }
}
