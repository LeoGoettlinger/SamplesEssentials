package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v26_1;

import com.fastasyncworldedit.bukkit.adapter.AbstractBukkitGetBlocks;
import com.fastasyncworldedit.bukkit.adapter.DelegateSemaphore;
import com.fastasyncworldedit.bukkit.adapter.NativeEntityFunctionSet;
import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.processor.heightmap.HeightMapType;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.math.BitArrayUnstretched;
import com.fastasyncworldedit.core.math.IntPair;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.NbtUtils;
import com.fastasyncworldedit.core.util.collection.AdaptedMap;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitEntity;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.paperlib.PaperLib;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.biome.BiomeType;
import io.papermc.paper.event.block.BeaconDeactivatedEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.BitStorage;
import net.minecraft.util.ZeroBitStorage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.HashMapPalette;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LinearPalette;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.storage.ValueInput;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTagType;

public class PaperweightGetBlocks extends AbstractBukkitGetBlocks<ServerLevel, LevelChunk> {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final Function<BlockPos, BlockVector3> posNms2We = v -> BlockVector3.at(v.getX(), v.getY(), v.getZ());
   public static final Function<BlockEntity, FaweCompoundTag> NMS_TO_TILE = ((PaperweightFaweAdapter)WorldEditPlugin.getInstance().getBukkitImplAdapter())
      .blockEntityToCompoundTag();
   private final PaperweightFaweAdapter adapter = (PaperweightFaweAdapter)WorldEditPlugin.getInstance().getBukkitImplAdapter();
   private final ReadWriteLock sectionLock = new ReentrantReadWriteLock();
   private final Registry<Biome> biomeRegistry;
   private final IdMap<Holder<Biome>> biomeHolderIdMap;
   private final Object sendLock = new Object();
   private LevelChunk levelChunk;
   private LevelChunkSection[] sections;
   private DataLayer[] blockLight;
   private DataLayer[] skyLight;
   private boolean lightUpdate = false;

   public PaperweightGetBlocks(World world, int chunkX, int chunkZ) {
      this(((CraftWorld)world).getHandle(), chunkX, chunkZ);
   }

   public PaperweightGetBlocks(ServerLevel serverLevel, int chunkX, int chunkZ) {
      super(serverLevel, chunkX, chunkZ, serverLevel.getMinY(), serverLevel.getMaxY() - 1);
      this.skyLight = new DataLayer[this.getSectionCount()];
      this.blockLight = new DataLayer[this.getSectionCount()];
      this.biomeRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.BIOME);
      this.biomeHolderIdMap = this.biomeRegistry.asHolderIdMap();
   }

   @Override
   public void setLightingToGet(char[][] light, int minSectionPosition, int maxSectionPosition) {
      if (light != null) {
         this.lightUpdate = true;

         try {
            this.fillLightNibble(light, LightLayer.BLOCK, minSectionPosition, maxSectionPosition);
         } catch (Throwable var5) {
            LOGGER.error("Error setting lighting to get", var5);
         }
      }
   }

   @Override
   public void setSkyLightingToGet(char[][] light, int minSectionPosition, int maxSectionPosition) {
      if (light != null) {
         this.lightUpdate = true;

         try {
            this.fillLightNibble(light, LightLayer.SKY, minSectionPosition, maxSectionPosition);
         } catch (Throwable var5) {
            LOGGER.error("Error setting sky lighting to get", var5);
         }
      }
   }

   @Override
   public void setHeightmapToGet(HeightMapType type, int[] data) {
      BitArrayUnstretched bitArray = new BitArrayUnstretched(MathMan.log2nlz(this.getChunk().getHeight() + 1), 256);
      bitArray.fromRaw(data);
      Types nativeType = Types.valueOf(type.name());
      Heightmap heightMap = (Heightmap)this.getChunk().heightmaps.get(nativeType);
      heightMap.setRawData(this.getChunk(), nativeType, bitArray.getData());
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      LevelChunkSection section = this.getSections(false)[(y >> 4) - this.getMinSectionPosition()];
      Holder<Biome> biomes = section.getNoiseBiome(x >> 2, (y & 15) >> 2, z >> 2);
      return PaperweightPlatformAdapter.adapt(biomes, (LevelAccessor)this.serverLevel);
   }

   @Override
   public void removeSectionLighting(int layer, boolean sky) {
      SectionPos sectionPos = SectionPos.of(this.getChunk().getPos(), layer);
      DataLayer dataLayer = this.serverLevel.getChunkSource().getLightEngine().getLayerListener(LightLayer.BLOCK).getDataLayerData(sectionPos);
      if (dataLayer != null) {
         this.lightUpdate = true;
         synchronized (dataLayer) {
            byte[] bytes = dataLayer.getData();
            Arrays.fill(bytes, (byte)0);
         }
      }

      if (sky) {
         SectionPos sectionPos1 = SectionPos.of(this.getChunk().getPos(), layer);
         DataLayer dataLayer1 = this.serverLevel.getChunkSource().getLightEngine().getLayerListener(LightLayer.SKY).getDataLayerData(sectionPos1);
         if (dataLayer1 != null) {
            this.lightUpdate = true;
            synchronized (dataLayer1) {
               byte[] bytes = dataLayer1.getData();
               Arrays.fill(bytes, (byte)0);
            }
         }
      }
   }

   @Override
   public FaweCompoundTag tile(final int x, final int y, final int z) {
      BlockEntity blockEntity = this.getChunk().getBlockEntity(new BlockPos((x & 15) + (this.chunkX << 4), y, (z & 15) + (this.chunkZ << 4)));
      return blockEntity == null ? null : NMS_TO_TILE.apply(blockEntity);
   }

   @Override
   public Map<BlockVector3, FaweCompoundTag> tiles() {
      Map<BlockPos, BlockEntity> nmsTiles = this.getChunk().getBlockEntities();
      return (Map<BlockVector3, FaweCompoundTag>)(nmsTiles.isEmpty() ? Collections.emptyMap() : AdaptedMap.immutable(nmsTiles, posNms2We, NMS_TO_TILE));
   }

   @Override
   public int getSkyLight(int x, int y, int z) {
      int layer = y >> 4;
      int alayer = layer - this.getMinSectionPosition();
      if (this.skyLight[alayer] == null) {
         SectionPos sectionPos = SectionPos.of(this.getChunk().getPos(), layer);
         DataLayer dataLayer = this.serverLevel.getChunkSource().getLightEngine().getLayerListener(LightLayer.SKY).getDataLayerData(sectionPos);
         if (dataLayer == null) {
            byte[] LAYER_COUNT = new byte[2048];
            Arrays.fill(LAYER_COUNT, (byte)15);
            dataLayer = new DataLayer(LAYER_COUNT);
            this.serverLevel.getChunkSource().getLightEngine().queueSectionData(LightLayer.BLOCK, sectionPos, dataLayer);
         }

         this.skyLight[alayer] = dataLayer;
      }

      return this.skyLight[alayer].get(x & 15, y & 15, z & 15);
   }

   @Override
   public int getEmittedLight(int x, int y, int z) {
      int layer = y >> 4;
      int alayer = layer - this.getMinSectionPosition();
      if (this.blockLight[alayer] == null) {
         this.serverLevel.getRawBrightness(new BlockPos(1, 1, 1), 5);
         SectionPos sectionPos = SectionPos.of(this.getChunk().getPos(), layer);
         DataLayer dataLayer = this.serverLevel.getChunkSource().getLightEngine().getLayerListener(LightLayer.BLOCK).getDataLayerData(sectionPos);
         if (dataLayer == null) {
            byte[] LAYER_COUNT = new byte[2048];
            Arrays.fill(LAYER_COUNT, (byte)15);
            dataLayer = new DataLayer(LAYER_COUNT);
            this.serverLevel.getChunkSource().getLightEngine().queueSectionData(LightLayer.BLOCK, sectionPos, dataLayer);
         }

         this.blockLight[alayer] = dataLayer;
      }

      return this.blockLight[alayer].get(x & 15, y & 15, z & 15);
   }

   @Override
   public int[] getHeightMap(HeightMapType type) {
      long[] longArray = ((Heightmap)this.getChunk().heightmaps.get(Types.valueOf(type.name()))).getRawData();
      BitArrayUnstretched bitArray = new BitArrayUnstretched(9, 256, longArray);
      return bitArray.toRaw(new int[256]);
   }

   @Nullable
   @Override
   public FaweCompoundTag entity(final UUID uuid) {
      List<Entity> entities = PaperweightPlatformAdapter.getEntities(this.getChunk());
      Entity entity = null;

      for (Entity e : entities) {
         if (e.getUUID().equals(uuid)) {
            entity = e;
            break;
         }
      }

      if (entity != null) {
         org.bukkit.entity.Entity bukkitEnt = entity.getBukkitEntity();
         return FaweCompoundTag.of(BukkitAdapter.adapt(bukkitEnt).getState().getNbt());
      } else {
         for (FaweCompoundTag tag : this.entities()) {
            if (uuid.equals(NbtUtils.uuid(tag))) {
               return tag;
            }
         }

         return null;
      }
   }

   @Override
   public Collection<FaweCompoundTag> entities() {
      List<Entity> entities = PaperweightPlatformAdapter.getEntities(this.getChunk());
      return (Collection<FaweCompoundTag>)(entities.isEmpty() ? Collections.emptyList() : new NativeEntityFunctionSet<>(entities, Entity::getUUID, e -> {
         LinValueOutput output = PaperweightPlatformAdapter.createOutput();
         e.save(output);
         return FaweCompoundTag.of(output::buildResult);
      }));
   }

   @Override
   public Set<com.sk89q.worldedit.entity.Entity> getFullEntities() {
      List<Entity> entities = PaperweightPlatformAdapter.getEntities(this.getChunk());
      return (Set<com.sk89q.worldedit.entity.Entity>)(entities.isEmpty()
         ? Collections.emptySet()
         : new NativeEntityFunctionSet<>(entities, Entity::getUUID, e -> new BukkitEntity(e.getBukkitEntity())));
   }

   private void removeEntity(Entity entity) {
      entity.discard();
   }

   public CompletableFuture<LevelChunk> ensureLoaded(ServerLevel nmsWorld) {
      return PaperweightPlatformAdapter.ensureLoaded(nmsWorld, this.chunkX, this.chunkZ);
   }

   protected <T extends Future<T>> T internalCall(IChunkSet set, Runnable finalizer, int copyKey, LevelChunk nmsChunk, ServerLevel nmsWorld) throws Exception {
      PaperweightGetBlocks_Copy copy = this.createCopy ? new PaperweightGetBlocks_Copy(nmsChunk) : null;
      if (this.createCopy) {
         if (this.copies.containsKey(copyKey)) {
            throw new IllegalStateException("Copy key already used.");
         }

         this.copies.put(copyKey, copy);
      }

      Map<BlockPos, BlockEntity> chunkTiles = new HashMap<>(nmsChunk.getBlockEntities());
      List<BlockEntity> beacons = null;
      if (!chunkTiles.isEmpty()) {
         for (Entry<BlockPos, BlockEntity> entry : chunkTiles.entrySet()) {
            BlockPos pos = entry.getKey();
            int lx = pos.getX() & 15;
            int ly = pos.getY();
            int lz = pos.getZ() & 15;
            int layer = ly >> 4;
            if (set.hasSection(layer)) {
               int ordinal = set.getBlock(lx, ly, lz).getOrdinal();
               if (ordinal != 0) {
                  BlockEntity tile = entry.getValue();
                  if (PaperLib.isPaper() && tile instanceof BeaconBlockEntity) {
                     if (beacons == null) {
                        beacons = new ArrayList<>();
                     }

                     beacons.add(tile);
                     PaperweightPlatformAdapter.removeBeacon(tile, nmsChunk);
                  } else {
                     nmsChunk.removeBlockEntity(tile.getBlockPos());
                     if (this.createCopy) {
                        copy.storeTile(tile);
                     }
                  }
               }
            }
         }
      }

      BiomeType[][] biomes = set.getBiomes();
      int bitMask = 0;
      synchronized (nmsChunk) {
         LevelChunkSection[] levelChunkSections = nmsChunk.getSections();

         for (int layerNo = this.getMinSectionPosition(); layerNo <= this.getMaxSectionPosition(); layerNo++) {
            int getSectionIndex = layerNo - this.getMinSectionPosition();
            int setSectionIndex = layerNo - set.getMinSectionPosition();
            if (!set.hasSection(layerNo)) {
               if (biomes != null && layerNo >= set.getMinSectionPosition() && layerNo <= set.getMaxSectionPosition() && biomes[setSectionIndex] != null) {
                  synchronized (super.sectionLocks[getSectionIndex]) {
                     LevelChunkSection existingSection = levelChunkSections[getSectionIndex];
                     if (this.createCopy && existingSection != null) {
                        copy.storeBiomes(getSectionIndex, existingSection.getBiomes());
                     }

                     if (existingSection == null) {
                        PalettedContainer<Holder<Biome>> biomeData = PaperweightPlatformAdapter.getBiomePalettedContainer(
                           biomes[setSectionIndex], this.biomeHolderIdMap
                        );
                        LevelChunkSection newSection = PaperweightPlatformAdapter.newChunkSection(
                           layerNo,
                           new char[4096],
                           this.adapter,
                           this.serverLevel.registryAccess(),
                           this.serverLevel.palettedContainerFactory().blockStatesStrategy(),
                           biomeData
                        );
                        if (PaperweightPlatformAdapter.setSectionAtomic(
                           nmsWorld.getWorld().getName(), this.chunkPos, levelChunkSections, null, newSection, getSectionIndex
                        )) {
                           this.updateGet(nmsChunk, levelChunkSections, newSection, new char[4096], getSectionIndex);
                        } else {
                           existingSection = levelChunkSections[getSectionIndex];
                           if (existingSection == null) {
                              LOGGER.error("Skipping invalid null section. chunk: {}, {} layer: {}", this.chunkX, this.chunkZ, getSectionIndex);
                           }
                        }
                     } else {
                        PalettedContainer<Holder<Biome>> paletteBiomes = this.setBiomesToPalettedContainer(biomes, setSectionIndex, existingSection.getBiomes());
                        if (paletteBiomes != null) {
                           PaperweightPlatformAdapter.setBiomesToChunkSection(existingSection, paletteBiomes);
                        }
                     }
                  }
               }
            } else {
               bitMask |= 1 << getSectionIndex;
               char[] tmp = set.load(layerNo);
               char[] setArr = new char[tmp.length];
               System.arraycopy(tmp, 0, setArr, 0, tmp.length);
               synchronized (super.sectionLocks[getSectionIndex]) {
                  LevelChunkSection existingSectionx = levelChunkSections[getSectionIndex];
                  if (existingSectionx != null) {
                     PaperweightPlatformAdapter.clearCounts(existingSectionx);
                  }

                  if (this.createCopy) {
                     char[] tmpLoad = this.load(layerNo);
                     char[] copyArr = new char[4096];
                     System.arraycopy(tmpLoad, 0, copyArr, 0, 4096);
                     copy.storeSection(getSectionIndex, copyArr);
                     if (biomes != null && existingSectionx != null) {
                        copy.storeBiomes(getSectionIndex, existingSectionx.getBiomes());
                     }
                  }

                  if (existingSectionx == null) {
                     PalettedContainer<Holder<Biome>> biomeData = biomes == null
                        ? this.serverLevel.palettedContainerFactory().createForBiomes()
                        : PaperweightPlatformAdapter.getBiomePalettedContainer(biomes[setSectionIndex], this.biomeHolderIdMap);
                     LevelChunkSection newSection = PaperweightPlatformAdapter.newChunkSection(
                        layerNo,
                        setArr,
                        this.adapter,
                        this.serverLevel.registryAccess(),
                        this.serverLevel.palettedContainerFactory().blockStatesStrategy(),
                        biomeData
                     );
                     if (PaperweightPlatformAdapter.setSectionAtomic(
                        nmsWorld.getWorld().getName(), this.chunkPos, levelChunkSections, null, newSection, getSectionIndex
                     )) {
                        this.updateGet(nmsChunk, levelChunkSections, newSection, setArr, getSectionIndex);
                        continue;
                     }

                     existingSectionx = levelChunkSections[getSectionIndex];
                     if (existingSectionx == null) {
                        LOGGER.error("Skipping invalid null section. chunk: {}, {} layer: {}", this.chunkX, this.chunkZ, getSectionIndex);
                        continue;
                     }
                  }

                  PaperweightPlatformAdapter.clearCounts(existingSectionx);
                  DelegateSemaphore lock = PaperweightPlatformAdapter.applyLock(existingSectionx);
                  synchronized (lock) {
                     lock.acquire();
                     lock.release();

                     try {
                        this.sectionLock.writeLock().lock();
                        if (this.getChunk() != nmsChunk) {
                           this.levelChunk = nmsChunk;
                           this.sections = null;
                           this.reset();
                        } else if (existingSectionx != this.getSections(false)[getSectionIndex]) {
                           this.sections[getSectionIndex] = existingSectionx;
                           this.reset();
                        } else if (!Arrays.equals(this.update(getSectionIndex, new char[4096], true), this.load(layerNo))) {
                           this.reset(layerNo);
                        }
                     } finally {
                        this.sectionLock.writeLock().unlock();
                     }

                     PalettedContainer biomeDatax = this.setBiomesToPalettedContainer(biomes, setSectionIndex, existingSectionx.getBiomes());
                     LevelChunkSection newSectionx = PaperweightPlatformAdapter.newChunkSection(
                        layerNo,
                        this::load,
                        setArr,
                        this.adapter,
                        this.serverLevel.registryAccess(),
                        this.serverLevel.palettedContainerFactory().blockStatesStrategy(),
                        biomeDatax != null ? biomeDatax : (PalettedContainer)existingSectionx.getBiomes()
                     );
                     if (!PaperweightPlatformAdapter.setSectionAtomic(
                        nmsWorld.getWorld().getName(), this.chunkPos, levelChunkSections, existingSectionx, newSectionx, getSectionIndex
                     )) {
                        LOGGER.error("Skipping invalid null section. chunk: {}, {} layer: {}", this.chunkX, this.chunkZ, getSectionIndex);
                     } else {
                        this.updateGet(nmsChunk, levelChunkSections, newSectionx, setArr, getSectionIndex);
                     }
                  }
               }
            }
         }

         Map<HeightMapType, int[]> heightMaps = set.getHeightMaps();

         for (Entry<HeightMapType, int[]> entryx : heightMaps.entrySet()) {
            this.setHeightmapToGet(entryx.getKey(), entryx.getValue());
         }

         this.setLightingToGet(set.getLight(), set.getMinSectionPosition(), set.getMaxSectionPosition());
         this.setSkyLightingToGet(set.getSkyLight(), set.getMinSectionPosition(), set.getMaxSectionPosition());
         List<Runnable> syncTasks = new ArrayList<>();
         int bx = this.chunkX << 4;
         int bz = this.chunkZ << 4;
         if (beacons != null && !beacons.isEmpty()) {
            List<BlockEntity> finalBeacons = beacons;
            syncTasks.add(() -> {
               for (BlockEntity beacon : finalBeacons) {
                  BeaconBlockEntity.playSound(beacon.getLevel(), beacon.getBlockPos(), SoundEvents.BEACON_DEACTIVATE);
                  new BeaconDeactivatedEvent(CraftBlock.at(beacon.getLevel(), beacon.getBlockPos())).callEvent();
               }
            });
         }

         Set<UUID> entityRemoves = set.getEntityRemoves();
         if (entityRemoves != null && !entityRemoves.isEmpty()) {
            syncTasks.add(() -> {
               Set<UUID> entitiesRemoved = new HashSet<>();

               for (Entity entity : PaperweightPlatformAdapter.getEntities(nmsChunk)) {
                  UUID uuid = entity.getUUID();
                  if (entityRemoves.contains(uuid)) {
                     if (this.createCopy) {
                        copy.storeEntity(entity);
                     }

                     this.removeEntity(entity);
                     entitiesRemoved.add(uuid);
                     entityRemoves.remove(uuid);
                  }
               }

               if (Settings.settings().EXPERIMENTAL.REMOVE_ENTITY_FROM_WORLD_ON_CHUNK_FAIL) {
                  for (UUID uuid : entityRemoves) {
                     Entity entityx = (Entity)nmsWorld.getEntities().get(uuid);
                     if (entityx != null) {
                        this.removeEntity(entityx);
                     }
                  }
               }

               set.getEntityRemoves().clear();
               set.getEntityRemoves().addAll(entitiesRemoved);
            });
         }

         Collection<FaweCompoundTag> entities = set.entities();
         if (entities != null && !entities.isEmpty()) {
            syncTasks.add(
               () -> {
                  Iterator<FaweCompoundTag> iterator = entities.iterator();

                  while (iterator.hasNext()) {
                     FaweCompoundTag nativeTag = iterator.next();
                     LinCompoundTag linTag = nativeTag.linTag();
                     LinStringTag idTag = linTag.findTag("Id", LinTagType.stringTag());
                     LinListTag<LinDoubleTag> posTag = linTag.findListTag("Pos", LinTagType.doubleTag());
                     LinListTag<LinFloatTag> rotTag = linTag.findListTag("Rotation", LinTagType.floatTag());
                     if (idTag != null && posTag != null && rotTag != null) {
                        double x = posTag.get(0).valueAsDouble();
                        double y = posTag.get(1).valueAsDouble();
                        double z = posTag.get(2).valueAsDouble();
                        float yaw = rotTag.get(0).valueAsFloat();
                        float pitch = rotTag.get(1).valueAsFloat();
                        String id = idTag.value();
                        EntityType<?> type = (EntityType<?>)EntityType.byString(id).orElse(null);
                        if (type != null) {
                           Entity entity = type.create(nmsWorld, EntitySpawnReason.COMMAND);
                           if (entity != null) {
                              LinCompoundTag.Builder toLoadComponentBuilder = linTag.toBuilder();

                              for (String name : Constants.NO_COPY_ENTITY_NBT_FIELDS) {
                                 toLoadComponentBuilder.remove(name);
                              }

                              ValueInput input = PaperweightPlatformAdapter.createInput(toLoadComponentBuilder.build());
                              entity.load(input);
                              entity.absSnapTo(x, y, z, yaw, pitch);
                              entity.setUUID(NbtUtils.uuid(nativeTag));
                              Runnable onError = () -> LOGGER.warn(
                                 "Error creating entity of type `{}` in world `{}` at location `{},{},{}`", id, nmsWorld.getWorld().getName(), x, y, z
                              );
                              if (!set.getSideEffectSet().shouldApply(SideEffect.ENTITY_EVENTS)) {
                                 entity.spawnReason = SpawnReason.CUSTOM;
                                 entity.generation = false;
                                 if (PaperLib.isPaper()) {
                                    if (!nmsWorld.moonrise$getEntityLookup().addNewEntity(entity, false)) {
                                       onError.run();
                                    }
                                    continue;
                                 }

                                 try {
                                    PaperweightPlatformAdapter.getEntitySectionManager(nmsWorld).addNewEntity(entity);
                                    continue;
                                 } catch (IllegalAccessException var24) {
                                    LOGGER.warn("Error bypassing entity events on spawn on Spigot", var24);
                                 }
                              }

                              if (!nmsWorld.addFreshEntity(entity, SpawnReason.CUSTOM)) {
                                 onError.run();
                                 iterator.remove();
                              }
                           }
                        }
                     } else {
                        LOGGER.error("Unknown entity tag: {}", nativeTag);
                     }
                  }
               }
            );
         }

         Map<BlockVector3, FaweCompoundTag> tiles = set.tiles();
         if (tiles != null && !tiles.isEmpty()) {
            syncTasks.add(
               () -> {
                  for (Entry<BlockVector3, FaweCompoundTag> entryx : tiles.entrySet()) {
                     FaweCompoundTag nativeTag = entryx.getValue();
                     BlockVector3 blockHash = entryx.getKey();
                     int x = blockHash.x() + bx;
                     int y = blockHash.y();
                     int z = blockHash.z() + bz;
                     BlockPos posx = new BlockPos(x, y, z);
                     synchronized (nmsWorld) {
                        BlockEntity tileEntity = nmsWorld.getBlockEntity(posx);
                        if (tileEntity == null || tileEntity.isRemoved()) {
                           nmsWorld.removeBlockEntity(posx);
                           tileEntity = nmsWorld.getBlockEntity(posx);
                        }

                        if (tileEntity != null) {
                           ValueInput input = PaperweightPlatformAdapter.createInput(
                              nativeTag.linTag().toBuilder().putInt("x", x).putInt("y", y).putInt("z", z).build()
                           );
                           tileEntity.loadWithComponents(input);
                        }
                     }
                  }
               }
            );
         }

         Runnable callback;
         if (bitMask == 0 && biomes == null && !this.lightUpdate) {
            callback = null;
         } else {
            int finalMask = bitMask != 0 ? bitMask : (this.lightUpdate ? set.getBitMask() : 0);
            syncTasks.add(() -> {
               nmsChunk.setLightCorrect(true);
               nmsChunk.mustNotSave = false;
            });
            callback = () -> {
               if (!set.getSideEffectSet().shouldApply(SideEffect.LIGHTING)
                  || !Settings.settings().LIGHTING.DELAY_PACKET_SENDING
                  || finalMask == 0 && biomes != null) {
                  this.send();
               }

               if (finalizer != null) {
                  finalizer.run();
               }
            };
         }

         return this.handleCallFinalizer(syncTasks, callback, finalizer);
      }
   }

   private void updateGet(LevelChunk nmsChunk, LevelChunkSection[] chunkSections, LevelChunkSection section, char[] arr, int layer) {
      try {
         this.sectionLock.writeLock().lock();
         if (this.getChunk() != nmsChunk) {
            this.levelChunk = nmsChunk;
            this.sections = new LevelChunkSection[chunkSections.length];
            System.arraycopy(chunkSections, 0, this.sections, 0, chunkSections.length);
            this.reset();
         }

         if (this.sections == null) {
            this.sections = new LevelChunkSection[chunkSections.length];
            System.arraycopy(chunkSections, 0, this.sections, 0, chunkSections.length);
         }

         if (this.sections[layer] != section) {
            this.sections[layer] = ((LevelChunkSection[])new LevelChunkSection[]{section}.clone())[0];
         }
      } finally {
         this.sectionLock.writeLock().unlock();
      }

      this.blocks[layer] = arr;
   }

   @Override
   public void send() {
      synchronized (this.sendLock) {
         PaperweightPlatformAdapter.sendChunk(new IntPair(this.chunkX, this.chunkZ), this.serverLevel, this.chunkX, this.chunkZ);
      }
   }

   @Override
   public char[] update(int layer, char[] data, boolean aggressive) {
      LevelChunkSection section = this.getSections(aggressive)[layer];
      if (section == null) {
         data = new char[4096];
         Arrays.fill(data, '\u0001');
         return data;
      } else {
         if (data == null || data == FaweCache.INSTANCE.EMPTY_CHAR_4096 || data.length != 4096) {
            data = new char[4096];
         }

         Semaphore lock = PaperweightPlatformAdapter.applyLock(section);
         synchronized (lock) {
            char[] paletteToOrdinal;
            try {
               lock.acquire();
               PalettedContainer<BlockState> blocks = section.getStates();
               Object dataObject = PaperweightPlatformAdapter.fieldData.get(blocks);
               BitStorage bits = (BitStorage)PaperweightPlatformAdapter.fieldStorage.get(dataObject);
               if (bits instanceof ZeroBitStorage) {
                  Arrays.fill(data, this.adapter.adaptToChar((BlockState)blocks.get(0, 0, 0)));
                  return data;
               }

               Palette<BlockState> palette = (Palette<BlockState>)PaperweightPlatformAdapter.fieldPalette.get(dataObject);
               int bitsPerEntry = bits.getBits();
               long[] blockStates = bits.getRaw();
               new BitArrayUnstretched(bitsPerEntry, 4096, blockStates).toRaw(data);
               if (palette instanceof LinearPalette || palette instanceof HashMapPalette) {
                  int num_palette = palette.getSize();
                  paletteToOrdinal = FaweCache.INSTANCE.PALETTE_TO_BLOCK_CHAR.get();

                  try {
                     if (num_palette == 1) {
                        char ordinal = this.ordinal((BlockState)palette.valueFor(0), this.adapter);
                        Arrays.fill(data, ordinal);
                     } else {
                        for (int i = 0; i < num_palette; i++) {
                           char ordinal = this.ordinal((BlockState)palette.valueFor(i), this.adapter);
                           paletteToOrdinal[i] = ordinal;
                        }

                        this.adapter.mapWithPalette(data, paletteToOrdinal);
                     }
                  } finally {
                     Arrays.fill(paletteToOrdinal, 0, num_palette, '\uffff');
                  }

                  return data;
               }

               this.adapter.mapFromGlobalPalette(data);
               paletteToOrdinal = data;
            } catch (InterruptedException | IllegalAccessException var29) {
               LOGGER.error("Could not read block data from palette", var29);
               throw new RuntimeException(var29);
            } finally {
               lock.release();
            }

            return paletteToOrdinal;
         }
      }
   }

   private char ordinal(BlockState ibd, PaperweightFaweAdapter adapter) {
      return ibd == null ? '\u0001' : adapter.adaptToChar(ibd);
   }

   public LevelChunkSection[] getSections(boolean force) {
      force &= this.forceLoadSections;
      LevelChunkSection[] tmp = this.sections;
      if (tmp == null || force) {
         try {
            this.sectionLock.writeLock().lock();
            tmp = this.sections;
            if (tmp == null || force) {
               LevelChunkSection[] chunkSections = this.getChunk().getSections();
               tmp = new LevelChunkSection[chunkSections.length];
               System.arraycopy(chunkSections, 0, tmp, 0, chunkSections.length);
               this.sections = tmp;
            }
         } finally {
            this.sectionLock.writeLock().unlock();
         }
      }

      return tmp;
   }

   public LevelChunk getChunk() {
      LevelChunk levelChunk = this.levelChunk;
      if (levelChunk == null) {
         synchronized (this) {
            levelChunk = this.levelChunk;
            if (levelChunk == null) {
               try {
                  this.levelChunk = levelChunk = this.ensureLoaded(this.serverLevel).get();
               } catch (ExecutionException | InterruptedException var5) {
                  LOGGER.error("Could not get chunk at {},{}", this.chunkX, this.chunkZ, var5);
                  throw new FaweException(
                     TextComponent.of("Could not get chunk at " + this.chunkX + "," + this.chunkZ + ": " + var5.getMessage()), FaweException.Type.OTHER, false
                  );
               }
            }
         }
      }

      return levelChunk;
   }

   private void fillLightNibble(char[][] light, LightLayer lightLayer, int minSectionPosition, int maxSectionPosition) {
      for (int Y = 0; Y <= maxSectionPosition - minSectionPosition; Y++) {
         if (light[Y] != null) {
            SectionPos sectionPos = SectionPos.of(this.levelChunk.getPos(), Y + minSectionPosition);
            DataLayer dataLayer = this.serverLevel.getChunkSource().getLightEngine().getLayerListener(lightLayer).getDataLayerData(sectionPos);
            if (dataLayer == null) {
               byte[] LAYER_COUNT = new byte[2048];
               Arrays.fill(LAYER_COUNT, (byte)(lightLayer == LightLayer.SKY ? 15 : 0));
               dataLayer = new DataLayer(LAYER_COUNT);
               this.serverLevel.getChunkSource().getLightEngine().queueSectionData(lightLayer, sectionPos, dataLayer);
            }

            synchronized (dataLayer) {
               for (int x = 0; x < 16; x++) {
                  for (int y = 0; y < 16; y++) {
                     for (int z = 0; z < 16; z++) {
                        int i = y << 8 | z << 4 | x;
                        if (light[Y][i] < 16) {
                           dataLayer.set(x, y, z, light[Y][i]);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private PalettedContainer<Holder<Biome>> setBiomesToPalettedContainer(
      final BiomeType[][] biomes, final int sectionIndex, final PalettedContainerRO<Holder<Biome>> data
   ) {
      BiomeType[] sectionBiomes;
      if (biomes != null && (sectionBiomes = biomes[sectionIndex]) != null) {
         PalettedContainer<Holder<Biome>> biomeData = data.recreate();
         int y = 0;

         for (int index = 0; y < 4; y++) {
            for (int z = 0; z < 4; z++) {
               for (int x = 0; x < 4; index++) {
                  BiomeType biomeType = sectionBiomes[index];
                  if (biomeType == null) {
                     biomeData.set(x, y, z, (Holder)data.get(x, y, z));
                  } else {
                     biomeData.set(x, y, z, (Holder)this.biomeHolderIdMap.byIdOrThrow(this.adapter.getInternalBiomeId(biomeType)));
                  }

                  x++;
               }
            }
         }

         return biomeData;
      } else {
         return null;
      }
   }

   @Override
   public boolean hasSection(int layer) {
      layer -= this.getMinSectionPosition();
      return this.getSections(false)[layer] != null;
   }

   @Override
   public boolean hasNonEmptySection(int layer) {
      layer -= this.getMinSectionPosition();
      LevelChunkSection section = this.getSections(false)[layer];
      return section != null && !section.hasOnlyAir();
   }

   @Override
   public boolean trim(boolean aggressive) {
      synchronized (this) {
         if (this.sections == null && (!aggressive || this.levelChunk == null)) {
            this.skyLight = new DataLayer[this.getSectionCount()];
            this.blockLight = new DataLayer[this.getSectionCount()];
            return !aggressive || super.trim(true);
         }
      }

      if (aggressive) {
         this.sectionLock.writeLock().lock();

         boolean var21;
         try {
            synchronized (this) {
               this.skyLight = new DataLayer[this.getSectionCount()];
               this.blockLight = new DataLayer[this.getSectionCount()];
               this.sections = null;
               this.levelChunk = null;
               var21 = super.trim(true);
            }
         } finally {
            this.sectionLock.writeLock().unlock();
         }

         return var21;
      } else {
         synchronized (this) {
            for (int i = this.getMinSectionPosition(); i <= this.getMaxSectionPosition(); i++) {
               int layer = i - this.getMinSectionPosition();
               if (this.hasSection(i) && super.blocks[layer] != null) {
                  LevelChunkSection existing = this.getSections(true)[layer];

                  try {
                     PalettedContainer<BlockState> blocksExisting = existing.getStates();
                     Object dataObject = PaperweightPlatformAdapter.fieldData.get(blocksExisting);
                     Palette<BlockState> palette = (Palette<BlockState>)PaperweightPlatformAdapter.fieldPalette.get(dataObject);
                     if (!(palette instanceof LinearPalette) && !(palette instanceof HashMapPalette)) {
                        super.trim(false, i);
                     } else {
                        int paletteSize = palette.getSize();
                        if (paletteSize != 1) {
                           super.trim(false, i);
                        }
                     }
                  } catch (IllegalAccessException var18) {
                     super.trim(false, i);
                  }
               }
            }

            return true;
         }
      }
   }
}
