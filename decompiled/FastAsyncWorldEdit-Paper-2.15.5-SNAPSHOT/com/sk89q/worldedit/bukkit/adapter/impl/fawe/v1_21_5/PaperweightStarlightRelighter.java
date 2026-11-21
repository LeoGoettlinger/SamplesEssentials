package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v1_21_5;

import com.fastasyncworldedit.bukkit.adapter.StarlightRelighter;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.math.IntPair;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.TicketType.TicketUse;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class PaperweightStarlightRelighter extends StarlightRelighter<ServerLevel, ChunkPos> {
   private static final TicketType<Unit> FAWE_TICKET = new TicketType(0L, false, TicketUse.LOADING);
   private static final int LIGHT_LEVEL = 32 + ChunkPyramid.LOADING_PYRAMID.getStepTo(ChunkStatus.FULL).getAccumulatedRadiusOf(ChunkStatus.LIGHT);

   public PaperweightStarlightRelighter(ServerLevel serverLevel, IQueueExtent<?> queue) {
      super(serverLevel, queue);
   }

   protected ChunkPos createChunkPos(final long chunkKey) {
      return new ChunkPos(chunkKey);
   }

   @Override
   protected long asLong(final int chunkX, final int chunkZ) {
      return ChunkPos.asLong(chunkX, chunkZ);
   }

   protected CompletableFuture<?> chunkLoadFuture(final ChunkPos chunkPos) {
      return this.serverLevel
         .getWorld()
         .getChunkAtAsync(chunkPos.x, chunkPos.z)
         .thenAccept(c -> this.serverLevel.getChunkSource().addTicketAtLevel(FAWE_TICKET, chunkPos, LIGHT_LEVEL));
   }

   @Override
   protected void invokeRelight(Set<ChunkPos> coords, Consumer<ChunkPos> chunkCallback, IntConsumer processCallback) {
      try {
         this.serverLevel.getChunkSource().getLightEngine().starlight$serverRelightChunks(coords, chunkCallback, processCallback);
      } catch (Exception var5) {
         LOGGER.error("Error occurred on relighting", var5);
      }
   }

   @Override
   protected void postProcessChunks(Set<ChunkPos> coords) {
      boolean delay = Settings.settings().LIGHTING.DELAY_PACKET_SENDING;

      for (ChunkPos pos : coords) {
         int x = pos.x;
         int z = pos.z;
         if (delay) {
            PaperweightPlatformAdapter.sendChunk(new IntPair(x, z), this.serverLevel, x, z);
         }

         this.serverLevel.getChunkSource().removeTicketAtLevel(FAWE_TICKET, pos, LIGHT_LEVEL);
      }
   }
}
