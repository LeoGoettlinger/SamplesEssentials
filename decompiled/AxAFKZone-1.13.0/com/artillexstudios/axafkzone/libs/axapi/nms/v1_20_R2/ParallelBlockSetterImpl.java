package com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R2;

import com.artillexstudios.axafkzone.libs.axapi.libs.math3.distribution.EnumeratedDistribution;
import com.artillexstudios.axafkzone.libs.axapi.libs.math3.util.Pair;
import com.artillexstudios.axafkzone.libs.axapi.selection.Cuboid;
import com.artillexstudios.axafkzone.libs.axapi.selection.ParallelBlockSetter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.LightEngineThreaded;
import net.minecraft.server.level.PlayerChunk;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.HeightMap.Type;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R2.block.data.CraftBlockData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParallelBlockSetterImpl implements ParallelBlockSetter {
   private static final ExecutorService executor = Executors.newSingleThreadExecutor();
   private static final ExecutorService parallelExecutor = Executors.newVirtualThreadPerTaskExecutor();
   private static final Logger log = LoggerFactory.getLogger(ParallelBlockSetterImpl.class);
   private final WorldServer level;

   public ParallelBlockSetterImpl(WorldServer level) {
      this.level = level;
   }

   public ChunkSection copy(ChunkSection section) {
      try {
         Constructor<ChunkSection> constructor = ChunkSection.class.getDeclaredConstructor(ChunkSection.class);
         constructor.setAccessible(true);
         return constructor.newInstance(section);
      } catch (Exception var4) {
         log.error("An unexpected issue occurred while initializing ParallelBlockSetter. Is your version supported?", var4);
         throw new RuntimeException(var4);
      }
   }

   @Override
   public void fill(Cuboid selection, EnumeratedDistribution<BlockData> distribution, IntConsumer consumer) {
      AtomicInteger blockCount = new AtomicInteger();
      int chunkMinX = selection.getMinX() >> 4;
      int chunkMaxX = selection.getMaxX() >> 4;
      int chunkMinZ = selection.getMinZ() >> 4;
      int chunkMaxZ = selection.getMaxZ() >> 4;
      List<Pair<BlockData, Double>> pmf = distribution.getPmf();
      List<CompletableFuture<?>> chunkTasks = new ArrayList<>();

      for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
         int minX = Math.max(selection.getMinX(), chunkX << 4);
         int maxX = Math.min(selection.getMaxX(), (chunkX << 4) + 15);

         for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
            int minZ = Math.max(selection.getMinZ(), chunkZ << 4);
            int maxZ = Math.min(selection.getMaxZ(), (chunkZ << 4) + 15);
            Chunk levelChunk = this.level.d(chunkX, chunkZ);
            List<CompletableFuture<?>> chunkFutures = new ArrayList<>();
            int lastSectionIndex = -1;

            for (int y = selection.getMinY(); y <= selection.getMaxY(); y++) {
               int sectionIndex = levelChunk.e(y);
               if (lastSectionIndex != sectionIndex) {
                  lastSectionIndex = sectionIndex;
                  ChunkSection section = levelChunk.b(sectionIndex);
                  ChunkSection newSection = this.copy(section);
                  CompletableFuture<?> future = CompletableFuture.runAsync(() -> {
                     EnumeratedDistribution<BlockData> newDistribution = new EnumeratedDistribution(pmf);

                     for (int i = selection.getMinY(); i <= selection.getMaxY(); i++) {
                        int m = levelChunk.e(i);
                        if (m >= sectionIndex) {
                           if (m > sectionIndex) {
                              break;
                           }

                           for (int x = minX; x <= maxX; x++) {
                              for (int z = minZ; z <= maxZ; z++) {
                                 CraftBlockData type = (CraftBlockData)newDistribution.sample();
                                 int j = x & 15;
                                 int k = i & 15;
                                 int l = z & 15;
                                 IBlockData state = type.getState();
                                 blockCount.incrementAndGet();
                                 newSection.a(j, k, l, state, true);
                                 this.updateHeightMap(levelChunk, j, i, l, state);
                              }
                           }
                        }
                     }

                     try {
                        MinecraftServer.getServer().g(() -> levelChunk.d()[sectionIndex] = newSection).get();
                     } catch (ExecutionException | InterruptedException var21x) {
                        throw new RuntimeException(var21x);
                     }
                  }, parallelExecutor);
                  chunkFutures.add(future);
               }
            }

            CompletableFuture<?> thisChunk = CompletableFuture.allOf(chunkFutures.toArray(new CompletableFuture[0]));
            thisChunk.thenAccept(ignored -> MinecraftServer.getServer().g(() -> {
               LightEngineThreaded lightEngine = this.level.I.a();
               levelChunk.a(true);
               this.sendUpdatePacket(levelChunk);
            }));
            chunkTasks.add(thisChunk);
         }
      }

      CompletableFuture<?> future = CompletableFuture.allOf(chunkTasks.toArray(new CompletableFuture[0]));
      future.thenAccept(ignored -> consumer.accept(blockCount.get()));
   }

   private void sendUpdatePacket(@NotNull Chunk chunk) {
      PlayerChunk playerChunk = this.level.k().a.b(chunk.f().longKey);
      if (playerChunk != null) {
         List<EntityPlayer> playersInRange = playerChunk.x.a(playerChunk.l(), false);
      }
   }

   private void updateHeightMap(IChunkAccess chunk, int x, int y, int z, IBlockData blockState) {
      ((HeightMap)chunk.h.get(Type.e)).a(x, y, z, blockState);
      ((HeightMap)chunk.h.get(Type.f)).a(x, y, z, blockState);
      ((HeightMap)chunk.h.get(Type.d)).a(x, y, z, blockState);
      ((HeightMap)chunk.h.get(Type.b)).a(x, y, z, blockState);
   }
}
