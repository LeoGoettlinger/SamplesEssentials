package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v26_2;

import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.ChunkEntitySlices;
import ca.spottedleaf.moonrise.patches.chunk_system.scheduling.ChunkHolderManager;
import com.fastasyncworldedit.bukkit.adapter.CachedBukkitAdapter;
import com.fastasyncworldedit.bukkit.adapter.DelegateSemaphore;
import com.fastasyncworldedit.bukkit.adapter.NMSAdapter;
import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.math.BitArrayUnstretched;
import com.fastasyncworldedit.core.math.IntPair;
import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.TaskManager;
import com.mojang.serialization.DataResult;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.bukkit.adapter.Refraction;
import com.sk89q.worldedit.bukkit.adapter.impl.v26_2.PaperweightBlockMaterial;
import com.sk89q.worldedit.bukkit.paperlib.PaperLib;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import io.papermc.paper.util.MCUtil;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.function.IntFunction;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.ThreadingDetector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.level.chunk.PalettedContainerRO.PackedData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.CraftChunk;
import org.enginehub.linbus.tree.LinCompoundTag;

public final class PaperweightPlatformAdapter extends NMSAdapter {
   public static final Field fieldData;
   public static final Constructor<?> dataConstructor;
   public static final Field fieldStorage;
   public static final Field fieldPalette;
   private static final MethodHandle palettedContainerUnpackSpigot;
   private static final Field fieldTickingFluidCount;
   private static final Field fieldTickingBlockCount;
   private static final Field fieldBiomes;
   private static final MethodHandle methodGetVisibleChunk;
   private static final Field fieldThreadingDetector;
   private static final Field fieldLock;
   private static final MethodHandle methodRemoveGameEventListener;
   private static final MethodHandle methodremoveTickingBlockEntity;
   private static final Field fieldRemove;
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static Field SERVER_LEVEL_ENTITY_MANAGER;
   static final MethodHandle PALETTED_CONTAINER_GET;
   private static final ThreadLocal<DelegateSemaphore> SEMAPHORE_THREAD_LOCAL;

   public static LinValueOutput createOutput() {
      return LinValueOutput.createWithContext(ProblemReporter.DISCARDING, DedicatedServer.getServer().registryAccess());
   }

   public static LinValueInput createInput(LinCompoundTag input) {
      return LinValueInput.create(ProblemReporter.DISCARDING, DedicatedServer.getServer().registryAccess(), input);
   }

   static boolean setSectionAtomic(String worldName, IntPair pair, LevelChunkSection[] sections, LevelChunkSection expected, LevelChunkSection value, int layer) {
      return NMSAdapter.setSectionAtomic(worldName, pair, sections, expected, value, layer);
   }

   static DelegateSemaphore applyLock(LevelChunkSection section) {
      if (PaperLib.isPaper()) {
         return SEMAPHORE_THREAD_LOCAL.get();
      } else {
         try {
            synchronized (section) {
               PalettedContainer<BlockState> blocks = section.getStates();
               ThreadingDetector currentThreadingDetector = (ThreadingDetector)fieldThreadingDetector.get(blocks);
               DelegateSemaphore var10000;
               synchronized (currentThreadingDetector) {
                  Semaphore currentLock = (Semaphore)fieldLock.get(currentThreadingDetector);
                  if (currentLock instanceof DelegateSemaphore delegateSemaphore) {
                     return delegateSemaphore;
                  }

                  DelegateSemaphore newLock = new DelegateSemaphore(1, currentLock);
                  fieldLock.set(currentThreadingDetector, newLock);
                  var10000 = newLock;
               }

               return var10000;
            }
         } catch (Throwable var11) {
            LOGGER.error("Error apply DelegateSemaphore", var11);
            throw new RuntimeException(var11);
         }
      }
   }

   public static CompletableFuture<LevelChunk> ensureLoaded(ServerLevel serverLevel, int chunkX, int chunkZ) {
      LevelChunk levelChunk = getChunkImmediatelyAsync(serverLevel, chunkX, chunkZ);
      if (levelChunk != null) {
         return CompletableFuture.completedFuture(levelChunk);
      } else {
         if (PaperLib.isPaper()) {
            CompletableFuture<LevelChunk> future = serverLevel.getWorld().getChunkAtAsync(chunkX, chunkZ, true, true).thenApply(chunk -> {
               addTicket(serverLevel, chunkX, chunkZ);

               try {
                  return toLevelChunk(chunk);
               } catch (Throwable var5x) {
                  LOGGER.error("Could not asynchronously load chunk at {},{}", chunkX, chunkZ, var5x);
                  return null;
               }
            });

            try {
               if (!future.isCompletedExceptionally() || future.isDone() && future.get() != null) {
                  return future;
               }

               Throwable t = future.exceptionNow();
               LOGGER.error("Asynchronous chunk load at {},{} exceptionally completed immediately", chunkX, chunkZ, t);
            } catch (ExecutionException | InterruptedException var6) {
               LOGGER.error("Unexpected error when getting completed future at chunk {},{}. Returning to default.", chunkX, chunkZ, var6);
            }
         }

         return CompletableFuture.supplyAsync(() -> TaskManager.taskManager().sync(() -> serverLevel.getChunk(chunkX, chunkZ)));
      }
   }

   private static LevelChunk toLevelChunk(Chunk chunk) {
      return (LevelChunk)((CraftChunk)chunk).getHandle(ChunkStatus.FULL);
   }

   @Nullable
   public static LevelChunk getChunkImmediatelyAsync(ServerLevel serverLevel, int chunkX, int chunkZ) {
      if (!PaperLib.isPaper()) {
         LevelChunk nmsChunk = serverLevel.getChunkSource().getChunk(chunkX, chunkZ, false);
         if (nmsChunk != null) {
            return nmsChunk;
         } else {
            return Fawe.isMainThread() ? serverLevel.getChunk(chunkX, chunkZ) : null;
         }
      } else {
         LevelChunk nmsChunk = serverLevel.getChunkSource().getChunkAtIfCachedImmediately(chunkX, chunkZ);
         if (nmsChunk != null) {
            addTicket(serverLevel, chunkX, chunkZ);
            return nmsChunk;
         } else {
            nmsChunk = serverLevel.getChunkSource().getChunkAtIfLoadedImmediately(chunkX, chunkZ);
            if (nmsChunk != null) {
               addTicket(serverLevel, chunkX, chunkZ);
               return nmsChunk;
            } else {
               return Fawe.isMainThread() ? serverLevel.getChunk(chunkX, chunkZ) : null;
            }
         }
      }
   }

   private static void addTicket(ServerLevel serverLevel, int chunkX, int chunkZ) {
      MCUtil.MAIN_EXECUTOR.execute(() -> serverLevel.getChunkSource().addTicketWithRadius(ChunkHolderManager.UNLOAD_COOLDOWN, new ChunkPos(chunkX, chunkZ), 0));
   }

   public static ChunkHolder getPlayerChunk(ServerLevel nmsWorld, final int chunkX, final int chunkZ) {
      ChunkMap chunkMap = nmsWorld.getChunkSource().chunkMap;

      try {
         return (ChunkHolder)methodGetVisibleChunk.invoke((ChunkMap)chunkMap, (long)ChunkPos.pack(chunkX, chunkZ));
      } catch (Throwable var5) {
         throw new RuntimeException(var5);
      }
   }

   public static void sendChunk(IntPair pair, ServerLevel nmsWorld, int chunkX, int chunkZ) {
      ChunkHolder chunkHolder = getPlayerChunk(nmsWorld, chunkX, chunkZ);
      if (chunkHolder != null) {
         LevelChunk levelChunk;
         if (PaperLib.isPaper()) {
            levelChunk = nmsWorld.getChunkSource().getChunkAtIfLoadedImmediately(chunkX, chunkZ);
         } else {
            levelChunk = (LevelChunk)chunkHolder.getTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).orElse(null);
         }

         if (levelChunk != null) {
            NMSAdapter.StampLockHolder lockHolder = new NMSAdapter.StampLockHolder();
            NMSAdapter.beginChunkPacketSend(nmsWorld.getWorld().getName(), pair, lockHolder);
            if (lockHolder.chunkLock != null) {
               MinecraftServer.getServer().execute(() -> {
                  try {
                     ChunkPos pos = levelChunk.getPos();
                     ClientboundLevelChunkWithLightPacket packet;
                     if (PaperLib.isPaper()) {
                        packet = new ClientboundLevelChunkWithLightPacket(levelChunk, nmsWorld.getLightEngine(), null, null, false);
                     } else {
                        packet = new ClientboundLevelChunkWithLightPacket(levelChunk, nmsWorld.getLightEngine(), null, null);
                     }

                     nearbyPlayers(nmsWorld, pos).forEach(p -> p.connection.send(packet));
                  } finally {
                     NMSAdapter.endChunkPacketSend(nmsWorld.getWorld().getName(), pair, lockHolder);
                  }
               });
            }
         }
      }
   }

   private static List<ServerPlayer> nearbyPlayers(ServerLevel serverLevel, ChunkPos coordIntPair) {
      return serverLevel.getChunkSource().chunkMap.getPlayers(coordIntPair, false);
   }

   public static LevelChunkSection newChunkSection(
      final int layer,
      final char[] blocks,
      CachedBukkitAdapter adapter,
      RegistryAccess registryAccess,
      Strategy<BlockState> strategy,
      @Nullable PalettedContainer<Holder<Biome>> biomes
   ) {
      return newChunkSection(layer, null, blocks, adapter, registryAccess, strategy, biomes);
   }

   public static LevelChunkSection newChunkSection(
      final int layer,
      final IntFunction<char[]> get,
      char[] set,
      CachedBukkitAdapter adapter,
      RegistryAccess registryAccess,
      Strategy<BlockState> strategy,
      @Nullable PalettedContainer<Holder<Biome>> biomes
   ) {
      if (set == null) {
         return newChunkSection(registryAccess, biomes);
      } else {
         int[] blockToPalette = FaweCache.INSTANCE.BLOCK_TO_PALETTE.get();
         int[] paletteToBlock = FaweCache.INSTANCE.PALETTE_TO_BLOCK.get();
         long[] blockStates = FaweCache.INSTANCE.BLOCK_STATES.get();
         int[] blocksCopy = FaweCache.INSTANCE.SECTION_BLOCKS.get();

         LevelChunkSection var28;
         try {
            int num_palette;
            if (get == null) {
               num_palette = createPalette(blockToPalette, paletteToBlock, blocksCopy, set, adapter, true);
            } else {
               num_palette = createPalette(layer, blockToPalette, paletteToBlock, blocksCopy, get, set, adapter, true);
            }

            boolean singleValue = num_palette == 1;
            LongStream bits;
            if (singleValue) {
               bits = null;
            } else {
               int bitsPerEntry = Mth.ceillog2(num_palette);
               if (bitsPerEntry < 4) {
                  bitsPerEntry = 4;
               }

               int blockBitArrayEnd = MathMan.longArrayLength(bitsPerEntry, 4096);
               BitArrayUnstretched bitArray = new BitArrayUnstretched(bitsPerEntry, 4096, blockStates);
               bitArray.fromRaw(blocksCopy);
               bits = Arrays.stream(blockStates, 0, blockBitArrayEnd);
            }

            List<BlockState> palette = new ArrayList<>();

            for (int i = 0; i < num_palette; i++) {
               int ordinal = paletteToBlock[i];
               PaperweightBlockMaterial material = (PaperweightBlockMaterial)BlockTypesCache.states[ordinal].getMaterial();
               palette.add(material.getState());
            }

            PackedData<BlockState> packedData = new PackedData(palette, Optional.ofNullable(bits));
            DataResult<PalettedContainer<BlockState>> result;
            if (PaperLib.isPaper()) {
               result = PalettedContainer.unpack(strategy, packedData, Blocks.AIR.defaultBlockState(), null);
            } else {
               result = (DataResult)palettedContainerUnpackSpigot.invokeExact((Strategy)strategy, (PackedData)packedData);
            }

            if (biomes == null) {
               biomes = PalettedContainerFactory.create(registryAccess).createForBiomes();
            }

            var28 = new LevelChunkSection((PalettedContainer)result.getOrThrow(), biomes);
         } catch (Throwable var21) {
            throw new RuntimeException("Failed to create block palette", var21);
         } finally {
            Arrays.fill(blockToPalette, Integer.MAX_VALUE);
            Arrays.fill(paletteToBlock, Integer.MAX_VALUE);
            Arrays.fill(blockStates, 0L);
            Arrays.fill(blocksCopy, 0);
         }

         return var28;
      }
   }

   private static LevelChunkSection newChunkSection(RegistryAccess registryAccess, @Nullable PalettedContainer<Holder<Biome>> biomes) {
      PalettedContainerFactory factory = PalettedContainerFactory.create(registryAccess);
      return biomes == null ? new LevelChunkSection(factory) : new LevelChunkSection(factory.createForBlockStates(), biomes);
   }

   public static void setBiomesToChunkSection(LevelChunkSection section, PalettedContainer<Holder<Biome>> biomes) {
      try {
         fieldBiomes.set(section, biomes);
      } catch (IllegalAccessException var3) {
         LOGGER.error("Could not set biomes to chunk section", var3);
      }
   }

   public static PalettedContainer<Holder<Biome>> getBiomePalettedContainer(BiomeType[] biomes, IdMap<Holder<Biome>> biomeRegistry) {
      if (biomes == null) {
         return null;
      } else {
         BukkitImplAdapter<?> adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
         List<Holder<Biome>> palette = new ArrayList<>();

         for (BiomeType biomeType : new LinkedList<>(Set.of(biomes))) {
            if (biomeType == null) {
               palette.add((Holder<Biome>)biomeRegistry.byId(adapter.getInternalBiomeId(BiomeTypes.PLAINS)));
            } else {
               palette.add((Holder<Biome>)biomeRegistry.byId(adapter.getInternalBiomeId(biomeType)));
            }
         }

         int biomeCount = palette.size();
         int bitsPerEntry = MathMan.log2nlz(biomeCount - 1);
         if (bitsPerEntry > 3) {
            bitsPerEntry = MathMan.log2nlz(biomeRegistry.size() - 1);
         }

         int bitsPerEntryNonZero = Math.max(bitsPerEntry, 1);
         int arrayLength = MathMan.longArrayLength(bitsPerEntryNonZero, 64);
         Strategy<Holder<Biome>> strategy = Strategy.createForBiomes(biomeRegistry);
         PackedData<Holder<Biome>> packedData = new PackedData(palette, Optional.of(LongStream.of()), bitsPerEntry);
         DataResult<PalettedContainer<Holder<Biome>>> result;
         if (PaperLib.isPaper()) {
            result = PalettedContainer.unpack(strategy, packedData, (Holder)biomeRegistry.byIdOrThrow(adapter.getInternalBiomeId(BiomeTypes.PLAINS)), null);
         } else {
            try {
               result = (DataResult)palettedContainerUnpackSpigot.invokeExact((Strategy)strategy, (PackedData)packedData);
            } catch (Throwable var18) {
               throw new RuntimeException("Failed to create biome palette for Spigot", var18);
            }
         }

         PalettedContainer<Holder<Biome>> biomePalettedContainer = (PalettedContainer<Holder<Biome>>)result.getOrThrow();
         int index = 0;

         for (int y = 0; y < 4; y++) {
            for (int z = 0; z < 4; z++) {
               for (int x = 0; x < 4; index++) {
                  BiomeType biomeTypex = biomes[index];
                  if (biomeTypex != null) {
                     Holder<Biome> biome = (Holder<Biome>)biomeRegistry.byId(
                        WorldEditPlugin.getInstance().getBukkitImplAdapter().getInternalBiomeId(biomeTypex)
                     );
                     if (biome != null) {
                        biomePalettedContainer.set(x, y, z, biome);
                     }
                  }

                  x++;
               }
            }
         }

         return biomePalettedContainer;
      }
   }

   public static void clearCounts(final LevelChunkSection section) throws IllegalAccessException {
      fieldTickingFluidCount.setShort(section, (short)0);
      fieldTickingBlockCount.setShort(section, (short)0);
   }

   public static BiomeType adapt(Holder<Biome> biome, LevelAccessor levelAccessor) {
      Registry<Biome> biomeRegistry = levelAccessor.registryAccess().lookupOrThrow(Registries.BIOME);
      int id = biomeRegistry.getId((Biome)biome.value());
      return id < 0 ? BiomeTypes.OCEAN : BiomeTypes.getLegacy(id);
   }

   static void removeBeacon(BlockEntity beacon, LevelChunk levelChunk) {
      try {
         BlockEntity blockEntity = (BlockEntity)levelChunk.blockEntities.remove(beacon.getBlockPos());
         if (blockEntity != null) {
            if (!levelChunk.getLevel().isClientSide()) {
               methodRemoveGameEventListener.invoke((LevelChunk)levelChunk, (BlockEntity)beacon, (Level)levelChunk.getLevel());
            }

            fieldRemove.set(beacon, true);
         }

         methodremoveTickingBlockEntity.invoke((LevelChunk)levelChunk, (BlockPos)beacon.getBlockPos());
      } catch (Throwable var3) {
         LOGGER.error("Error removing beacon", var3);
      }
   }

   static List<Entity> getEntities(LevelChunk chunk) {
      if (PaperLib.isPaper()) {
         return Optional.ofNullable(chunk.getLevel().moonrise$getEntityLookup().getChunk(chunk.locX, chunk.locZ))
            .<List<Entity>>map(ChunkEntitySlices::getAllEntities)
            .orElse(Collections.emptyList());
      } else {
         try {
            return getEntitySectionManager((ServerLevel)chunk.getLevel()).getEntities(chunk.getPos());
         } catch (IllegalAccessException var2) {
            throw new RuntimeException("Failed to lookup entities [PAPER=false]", var2);
         }
      }
   }

   static PersistentEntitySectionManager<Entity> getEntitySectionManager(ServerLevel level) throws IllegalAccessException {
      return (PersistentEntitySectionManager<Entity>)SERVER_LEVEL_ENTITY_MANAGER.get(level);
   }

   static {
      Lookup lookup = MethodHandles.lookup();

      try {
         fieldData = PalettedContainer.class.getDeclaredField(Refraction.pickName("data", "b"));
         fieldData.setAccessible(true);
         Class<?> dataClazz = fieldData.getType();
         dataConstructor = dataClazz.getDeclaredConstructors()[0];
         dataConstructor.setAccessible(true);
         fieldStorage = dataClazz.getDeclaredField(Refraction.pickName("storage", "b"));
         fieldStorage.setAccessible(true);
         fieldPalette = dataClazz.getDeclaredField(Refraction.pickName("palette", "c"));
         fieldPalette.setAccessible(true);
         palettedContainerUnpackSpigot = PaperLib.isPaper()
            ? null
            : lookup.findStatic(PalettedContainer.class, "a", MethodType.methodType(DataResult.class, Strategy.class, PackedData.class));
         fieldTickingFluidCount = LevelChunkSection.class.getDeclaredField(Refraction.pickName("tickingFluidCount", "g"));
         fieldTickingFluidCount.setAccessible(true);
         fieldTickingBlockCount = LevelChunkSection.class.getDeclaredField(Refraction.pickName("tickingBlockCount", "f"));
         fieldTickingBlockCount.setAccessible(true);

         Field tmpFieldBiomes;
         try {
            tmpFieldBiomes = LevelChunkSection.class.getDeclaredField("biomes");
         } catch (NoSuchFieldException var7) {
            tmpFieldBiomes = LevelChunkSection.class.getDeclaredField("i");
         }

         fieldBiomes = tmpFieldBiomes;
         fieldBiomes.setAccessible(true);
         Method getVisibleChunkIfPresent = ChunkMap.class.getDeclaredMethod(Refraction.pickName("getVisibleChunkIfPresent", "b"), long.class);
         getVisibleChunkIfPresent.setAccessible(true);
         methodGetVisibleChunk = lookup.unreflect(getVisibleChunkIfPresent);
         if (!PaperLib.isPaper()) {
            fieldThreadingDetector = PalettedContainer.class.getDeclaredField(Refraction.pickName("threadingDetector", "d"));
            fieldThreadingDetector.setAccessible(true);
            fieldLock = ThreadingDetector.class.getDeclaredField(Refraction.pickName("lock", "c"));
            fieldLock.setAccessible(true);
            SERVER_LEVEL_ENTITY_MANAGER = ServerLevel.class.getDeclaredField(Refraction.pickName("entityManager", "M"));
            SERVER_LEVEL_ENTITY_MANAGER.setAccessible(true);
         } else {
            fieldThreadingDetector = null;
            fieldLock = null;
         }

         Method removeGameEventListener = LevelChunk.class
            .getDeclaredMethod(Refraction.pickName("removeGameEventListener", "a"), BlockEntity.class, ServerLevel.class);
         removeGameEventListener.setAccessible(true);
         methodRemoveGameEventListener = lookup.unreflect(removeGameEventListener);
         Method removeBlockEntityTicker = LevelChunk.class.getDeclaredMethod(Refraction.pickName("removeBlockEntityTicker", "k"), BlockPos.class);
         removeBlockEntityTicker.setAccessible(true);
         methodremoveTickingBlockEntity = lookup.unreflect(removeBlockEntityTicker);
         fieldRemove = BlockEntity.class.getDeclaredField(Refraction.pickName("remove", "p"));
         fieldRemove.setAccessible(true);
         Method palettedContainerGet = PalettedContainer.class.getDeclaredMethod(Refraction.pickName("get", "a"), int.class);
         palettedContainerGet.setAccessible(true);
         PALETTED_CONTAINER_GET = lookup.unreflect(palettedContainerGet);
      } catch (Error | RuntimeException var8) {
         throw var8;
      } catch (Exception var9) {
         throw new RuntimeException(var9);
      }

      SEMAPHORE_THREAD_LOCAL = ThreadLocal.withInitial(() -> new DelegateSemaphore(1, null));
   }
}
