package com.fastasyncworldedit.bukkit.adapter;

import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkCache;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.implementation.SingleThreadQueueExtent;
import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Regenerator {
   protected final World originalBukkitWorld;
   protected final Region region;
   protected final Extent target;
   protected final RegenOptions options;
   protected long seed;
   protected SingleThreadQueueExtent source;

   public Regenerator(World originalBukkitWorld, Region region, Extent target, RegenOptions options) {
      this.originalBukkitWorld = originalBukkitWorld;
      this.region = region;
      this.target = target;
      this.options = options;
   }

   public boolean regenerate() throws Exception {
      if (!this.prepare()) {
         return false;
      } else {
         try {
            if (!this.initNewWorld()) {
               this.cleanup0();
               return false;
            }
         } catch (Exception var3) {
            this.cleanup0();
            throw var3;
         }

         try {
            this.copyToWorld();
         } catch (Exception var2) {
            this.cleanup0();
            throw var2;
         }

         this.cleanup0();
         return true;
      }
   }

   protected abstract void runTasks(BooleanSupplier shouldKeepTicking);

   private void createSource() {
      this.source = new SingleThreadQueueExtent(
         BukkitWorld.HAS_MIN_Y ? this.originalBukkitWorld.getMinHeight() : 0, BukkitWorld.HAS_MIN_Y ? this.originalBukkitWorld.getMaxHeight() : 256
      );
      this.source.init(this.target, this.initSourceQueueCache(), null);
   }

   private void copyToWorld() {
      this.createSource();
      long timeoutPerTick = TimeUnit.MILLISECONDS.toNanos(10L);
      int taskId = TaskManager.taskManager().repeat(() -> {
         long startTime = System.nanoTime();
         this.runTasks(() -> System.nanoTime() - startTime < timeoutPerTick);
      }, 1);
      boolean genbiomes = this.options.shouldRegenBiomes();
      boolean hasBiome = this.options.hasBiomeType();
      BiomeType biome = this.options.getBiomeType();
      Pattern pattern;
      if (!genbiomes && !hasBiome) {
         pattern = new Regenerator.PlacementPattern();
      } else if (hasBiome) {
         pattern = new Regenerator.WithBiomePlacementPattern((ignored1, ignored2) -> biome);
      } else {
         pattern = new Regenerator.WithBiomePlacementPattern(
            (vec, chunk) -> chunk != null ? chunk.getBiomeType(vec.x() & 15, vec.y(), vec.z() & 15) : this.source.getBiome(vec)
         );
      }

      this.target.setBlocks(this.region, pattern);
      TaskManager.taskManager().cancel(taskId);
   }

   private void cleanup0() {
      this.cleanup();
   }

   protected abstract boolean prepare();

   protected abstract boolean initNewWorld() throws Exception;

   protected abstract void cleanup();

   protected abstract IChunkCache<IChunkGet> initSourceQueueCache();

   protected BiomeProvider getBiomeProvider() {
      return (BiomeProvider)(this.options.hasBiomeType() ? new Regenerator.SingleBiomeProvider() : this.originalBukkitWorld.getBiomeProvider());
   }

   private abstract class ChunkwisePattern implements Pattern {
      @Nullable
      protected IChunk chunk;

      @NotNull
      @Override
      public <T extends IChunk> T applyChunk(final T chunk, @Nullable final Region region) {
         this.chunk = Regenerator.this.source.getOrCreateChunk(chunk.getX(), chunk.getZ());
         return chunk;
      }

      @Override
      public void finishChunk(final IChunk chunk) {
         this.chunk = null;
      }

      @Override
      public abstract Pattern fork();
   }

   public static enum Concurrency {
      FULL,
      RADIUS,
      NONE;
   }

   private class PlacementPattern extends Regenerator.ChunkwisePattern {
      @Override
      public BaseBlock applyBlock(final BlockVector3 position) {
         return Regenerator.this.source.getFullBlock(position);
      }

      @Override
      public boolean apply(final Extent extent, final BlockVector3 get, final BlockVector3 set) throws WorldEditException {
         BaseBlock fullBlock;
         if (this.chunk != null) {
            fullBlock = this.chunk.getFullBlock(get.x() & 15, get.y(), get.z() & 15);
         } else {
            fullBlock = Regenerator.this.source.getFullBlock(get.x(), get.y(), get.z());
         }

         return set.setFullBlock(extent, fullBlock);
      }

      @Override
      public Pattern fork() {
         return Regenerator.this.new PlacementPattern();
      }
   }

   public class SingleBiomeProvider extends BiomeProvider {
      private final Biome biome = BukkitAdapter.adapt(Regenerator.this.options.getBiomeType());

      public Biome getBiome(final WorldInfo worldInfo, final int x, final int y, final int z) {
         return this.biome;
      }

      public List<Biome> getBiomes(final WorldInfo worldInfo) {
         return Collections.singletonList(this.biome);
      }
   }

   private class WithBiomePlacementPattern extends Regenerator.ChunkwisePattern {
      private final BiFunction<BlockVector3, IChunk, BiomeType> biomeGetter;

      private WithBiomePlacementPattern(final BiFunction<BlockVector3, IChunk, BiomeType> biomeGetter) {
         this.biomeGetter = biomeGetter;
      }

      @Override
      public BaseBlock applyBlock(final BlockVector3 position) {
         return Regenerator.this.source.getFullBlock(position);
      }

      @Override
      public boolean apply(final Extent extent, final BlockVector3 get, final BlockVector3 set) throws WorldEditException {
         BaseBlock fullBlock;
         if (this.chunk != null) {
            fullBlock = this.chunk.getFullBlock(get.x() & 15, get.y(), get.z() & 15);
         } else {
            fullBlock = Regenerator.this.source.getFullBlock(get.x(), get.y(), get.z());
         }

         return extent.setBlock(set.x(), set.y(), set.z(), fullBlock) && extent.setBiome(set.x(), set.y(), set.z(), this.biomeGetter.apply(get, this.chunk));
      }

      @Override
      public Pattern fork() {
         return Regenerator.this.new WithBiomePlacementPattern(this.biomeGetter);
      }
   }
}
