package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v26_2.regen;

import com.fastasyncworldedit.bukkit.adapter.Regenerator;
import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.queue.IChunkCache;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.implementation.chunk.ChunkCache;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.adapter.Refraction;
import com.sk89q.worldedit.bukkit.paperlib.PaperLib;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.io.file.SafeFiles;
import com.sk89q.worldedit.world.RegenOptions;
import io.papermc.paper.world.PaperWorldLoader.LoadedWorldData;
import io.papermc.paper.world.saveddata.PaperWorldPDC;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Util;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;

public class PaperweightRegen extends Regenerator {
   private static final String REGEN_WORLD_NAME = "faweregentempworld";
   private static final Field serverWorldsField;
   private static final Field paperConfigField;
   private static final Field generatorSettingBaseSupplierField;
   private ServerLevel originalServerWorld;
   private ServerLevel freshWorld;
   private LevelStorageAccess session;
   private Path tempDir;

   public PaperweightRegen(World originalBukkitWorld, Region region, Extent target, RegenOptions options) {
      super(originalBukkitWorld, region, target, options);
   }

   @Override
   protected void runTasks(final BooleanSupplier shouldKeepTicking) {
      while (shouldKeepTicking.getAsBoolean()) {
         if (!this.freshWorld.getChunkSource().pollTask()) {
            return;
         }
      }
   }

   @Override
   protected boolean prepare() {
      this.originalServerWorld = ((CraftWorld)this.originalBukkitWorld).getHandle();
      this.seed = this.options.getSeed().orElse(this.originalServerWorld.getSeed());
      return true;
   }

   @Override
   protected boolean initNewWorld() throws Exception {
      if (!PaperLib.isPaper()) {
         throw new UnsupportedOperationException("Regen requires Paper");
      } else {
         this.tempDir = Files.createTempDirectory("FastAsyncWorldEditWorldGen");
         Environment environment = this.originalBukkitWorld.getEnvironment();
         ChunkGenerator generator = this.originalBukkitWorld.getGenerator();
         LevelStorageSource levelStorageSource = LevelStorageSource.createDefault(this.tempDir);
         ResourceKey<LevelStem> levelStemResourceKey = this.getWorldDimKey(environment);
         this.session = levelStorageSource.createAccess("faweregentempworld");
         MinecraftServer server = this.originalServerWorld.getCraftServer().getServer();
         WorldOptions originalOpts = this.originalServerWorld.worldGenSettings.options();
         WorldOptions newOpts = this.options.getSeed().isPresent() ? originalOpts.withSeed(OptionalLong.of(this.seed)) : originalOpts;
         WorldGenSettings newWorldGenSettings = new WorldGenSettings(newOpts, this.originalServerWorld.worldGenSettings.dimensions());
         LoadedWorldData loadedWorldData = new LoadedWorldData(
            "faweregentempworld",
            UUID.randomUUID(),
            new PaperWorldPDC((CraftPersistentDataContainer)this.originalBukkitWorld.getPersistentDataContainer()),
            this.originalServerWorld.serverLevelData
         );
         BiomeProvider biomeProvider = this.getBiomeProvider();
         SavedDataStorage savedDataStorage = new SavedDataStorage(
            this.session.getDimensionPath(this.originalServerWorld.dimension()).resolve(LevelResource.DATA.id()),
            server.getFixerUpper(),
            server.registryAccess()
         );
         this.freshWorld = Fawe.instance()
            .getQueueHandler()
            .sync(
               () -> new ServerLevel(
                  server,
                  Util.backgroundExecutor(),
                  this.session,
                  newWorldGenSettings,
                  this.originalServerWorld.dimension(),
                  new LevelStem(this.originalServerWorld.dimensionTypeRegistration(), this.originalServerWorld.getChunkSource().getGenerator()),
                  this.originalServerWorld.isDebug(),
                  this.seed,
                  ImmutableList.of(),
                  false,
                  levelStemResourceKey,
                  environment,
                  generator,
                  biomeProvider,
                  savedDataStorage,
                  loadedWorldData
               ) {
                  private final Holder<Biome> singleBiome = PaperweightRegen.this.options.hasBiomeType()
                     ? (Holder)DedicatedServer.getServer()
                        .registryAccess()
                        .lookupOrThrow(Registries.BIOME)
                        .asHolderIdMap()
                        .byIdOrThrow(WorldEditPlugin.getInstance().getBukkitImplAdapter().getInternalBiomeId(PaperweightRegen.this.options.getBiomeType()))
                     : null;

                  @Nonnull
                  public Holder<Biome> getUncachedNoiseBiome(int biomeX, int biomeY, int biomeZ) {
                     return PaperweightRegen.this.options.hasBiomeType() ? this.singleBiome : super.getUncachedNoiseBiome(biomeX, biomeY, biomeZ);
                  }

                  public void save(final ProgressListener progressListener, final boolean flush, final boolean savingDisabled) {
                  }

                  public void save(final ProgressListener progressListener, final boolean flush, final boolean savingDisabled, final boolean close) {
                  }
               }
            )
            .get();
         this.freshWorld.noSave = true;
         this.removeWorldFromWorldsMap();
         if (paperConfigField != null) {
            paperConfigField.set(this.freshWorld, this.originalServerWorld.paperConfig());
         }

         return true;
      }
   }

   @Override
   protected void cleanup() {
      try {
         this.session.close();
      } catch (Exception var5) {
      }

      try {
         Fawe.instance().getQueueHandler().sync(() -> {
            try {
               this.freshWorld.getChunkSource().getDataStorage().cache.clear();
               this.freshWorld.getChunkSource().close(false);
            } catch (Exception var2x) {
               throw new RuntimeException(var2x);
            }
         });
      } catch (Exception var4) {
      }

      try {
         Fawe.instance().getQueueHandler().sync(this::removeWorldFromWorldsMap);
      } catch (Exception var3) {
      }

      try {
         SafeFiles.tryHardToDeleteDir(this.tempDir);
      } catch (Exception var2) {
      }
   }

   @Override
   protected IChunkCache<IChunkGet> initSourceQueueCache() {
      return new ChunkCache<>(BukkitAdapter.adapt(this.freshWorld.getWorld()));
   }

   private void removeWorldFromWorldsMap() {
      try {
         Map<String, World> map = (Map<String, World>)serverWorldsField.get(Bukkit.getServer());
         map.remove("faweregentempworld");
      } catch (IllegalAccessException var2) {
         throw new RuntimeException(var2);
      }
   }

   private ResourceKey<LevelStem> getWorldDimKey(Environment env) {
      return switch (env) {
         case NETHER -> LevelStem.NETHER;
         case THE_END -> LevelStem.END;
         default -> LevelStem.OVERWORLD;
      };
   }

   static {
      try {
         serverWorldsField = CraftServer.class.getDeclaredField("worlds");
         serverWorldsField.setAccessible(true);

         Field tmpPaperConfigField;
         try {
            tmpPaperConfigField = Level.class.getDeclaredField("paperConfig");
            tmpPaperConfigField.setAccessible(true);
         } catch (Exception var2) {
            tmpPaperConfigField = null;
         }

         paperConfigField = tmpPaperConfigField;
         generatorSettingBaseSupplierField = NoiseBasedChunkGenerator.class.getDeclaredField(Refraction.pickName("settings", "e"));
         generatorSettingBaseSupplierField.setAccessible(true);
      } catch (Exception var3) {
         throw new RuntimeException(var3);
      }
   }
}
