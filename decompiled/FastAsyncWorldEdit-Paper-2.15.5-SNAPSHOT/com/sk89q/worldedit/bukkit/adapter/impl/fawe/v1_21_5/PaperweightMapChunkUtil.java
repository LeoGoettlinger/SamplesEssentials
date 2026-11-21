package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v1_21_5;

import com.fastasyncworldedit.bukkit.adapter.MapChunkUtil;
import com.sk89q.worldedit.bukkit.adapter.Refraction;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;

public class PaperweightMapChunkUtil extends MapChunkUtil<ClientboundLevelChunkWithLightPacket> {
   public PaperweightMapChunkUtil() throws NoSuchFieldException {
      this.fieldX = ClientboundLevelChunkPacketData.class.getDeclaredField(Refraction.pickName("TWO_MEGABYTES", "a"));
      this.fieldZ = ClientboundLevelChunkWithLightPacket.class.getDeclaredField(Refraction.pickName("x", "b"));
      this.fieldBitMask = ClientboundLevelChunkWithLightPacket.class.getDeclaredField(Refraction.pickName("z", "c"));
      this.fieldHeightMap = ClientboundLevelChunkPacketData.class.getDeclaredField(Refraction.pickName("heightmaps", "b"));
      this.fieldChunkData = ClientboundLevelChunkWithLightPacket.class.getDeclaredField(Refraction.pickName("chunkData", "d"));
      this.fieldBlockEntities = ClientboundLevelChunkPacketData.class.getDeclaredField(Refraction.pickName("buffer", "c"));
      this.fieldFull = ClientboundLevelChunkPacketData.class.getDeclaredField(Refraction.pickName("blockEntitiesData", "d"));
      this.fieldX.setAccessible(true);
      this.fieldZ.setAccessible(true);
      this.fieldBitMask.setAccessible(true);
      this.fieldHeightMap.setAccessible(true);
      this.fieldChunkData.setAccessible(true);
      this.fieldBlockEntities.setAccessible(true);
      this.fieldFull.setAccessible(true);
   }

   public ClientboundLevelChunkWithLightPacket createPacket() {
      throw new UnsupportedOperationException();
   }
}
