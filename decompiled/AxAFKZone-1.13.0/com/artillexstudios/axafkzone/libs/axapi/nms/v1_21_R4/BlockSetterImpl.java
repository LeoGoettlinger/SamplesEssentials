package com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4;

import com.artillexstudios.axafkzone.libs.axapi.executor.ExceptionReportingScheduledThreadPool;
import com.artillexstudios.axafkzone.libs.axapi.selection.BlockSetter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.LightEngineThreaded;
import net.minecraft.server.level.PlayerChunk;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.HeightMap.Type;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_21_R4.block.data.CraftBlockData;
import org.jetbrains.annotations.NotNull;

public class BlockSetterImpl implements BlockSetter {
   private static final ExecutorService executor = new ExceptionReportingScheduledThreadPool(1);
   private final WorldServer level;
   private final ArrayList<ChunkCoordIntPair> chunks = new ArrayList<>();
   private Chunk chunk = null;

   public BlockSetterImpl(WorldServer level) {
      this.level = level;
   }

   @Override
   public void setBlock(int x, int y, int z, BlockData data) {
      int chunkX = x >> 4;
      int chunkZ = z >> 4;
      Chunk levelChunk;
      if (this.chunk != null && chunkX == this.chunk.f().h && chunkZ == this.chunk.f().i) {
         levelChunk = this.chunk;
      } else {
         this.chunk = levelChunk = this.level.d(chunkX, chunkZ);
         this.chunks.add(levelChunk.f());
      }

      IBlockData state = ((CraftBlockData)data).getState();
      int sectionIndex = levelChunk.f(y);
      ChunkSection section = levelChunk.b(sectionIndex);
      if (!section.c() || !state.l()) {
         int j = x & 15;
         int k = y & 15;
         int l = z & 15;
         section.a(j, k, l, state, false);
         this.updateHeightMap(levelChunk, j, y, l, state);
      }
   }

   @Override
   public void finalise() {
      for (ChunkCoordIntPair chunk : this.chunks) {
         Chunk levelChunk = this.level.d(chunk.h, chunk.i);
         levelChunk.i();
         this.sendUpdatePacket(levelChunk);
      }

      this.relight();
      this.chunks.clear();
      this.chunk = null;
   }

   private void updateHeightMap(IChunkAccess chunk, int x, int y, int z, IBlockData blockState) {
      ((HeightMap)chunk.h.get(Type.e)).a(x, y, z, blockState);
      ((HeightMap)chunk.h.get(Type.f)).a(x, y, z, blockState);
      ((HeightMap)chunk.h.get(Type.d)).a(x, y, z, blockState);
      ((HeightMap)chunk.h.get(Type.b)).a(x, y, z, blockState);
   }

   private void relight() {
      HashSet<ChunkCoordIntPair> set = new HashSet<>(this.chunks);
      MinecraftServer.getServer().h(() -> {
         LightEngineThreaded lightEngine = this.level.J.a();
         lightEngine.starlight$serverRelightChunks(set, c -> {}, c -> {});
      });
   }

   private void sendUpdatePacket(@NotNull Chunk chunk) {
      PlayerChunk playerChunk = this.level.m().a.b(chunk.f().longKey);
      if (playerChunk != null) {
         List<EntityPlayer> playersInRange = playerChunk.s.a(playerChunk.r(), false);
         executor.execute(() -> {
            ClientboundLevelChunkWithLightPacket lightPacket = new ClientboundLevelChunkWithLightPacket(chunk, this.level.B_(), null, null, false);
            int size = playersInRange.size();

            for (int i = 0; i < size; i++) {
               EntityPlayer player = playersInRange.get(i);
               player.f.b(lightPacket);
            }
         });
      }
   }
}
