package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v1_21_5.regen;

import com.fastasyncworldedit.bukkit.adapter.Regenerator;
import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.queue.IChunkCache;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.implementation.chunk.ChunkCache;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.adapter.Refraction;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.io.file.SafeFiles;
import com.sk89q.worldedit.world.RegenOptions;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.PrimaryLevelData.SpecialWorldProperty;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

public class PaperweightRegen extends Regenerator {
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
      this.tempDir = Files.createTempDirectory("FastAsyncWorldEditWorldGen");
      Environment environment = this.originalBukkitWorld.getEnvironment();
      ChunkGenerator generator = this.originalBukkitWorld.getGenerator();
      LevelStorageSource levelStorageSource = LevelStorageSource.createDefault(this.tempDir);
      ResourceKey<LevelStem> levelStemResourceKey = this.getWorldDimKey(environment);
      this.session = levelStorageSource.createAccess("faweregentempworld", levelStemResourceKey);
      PrimaryLevelData originalWorldData = this.originalServerWorld.serverLevelData;
      MinecraftServer server = this.originalServerWorld.getCraftServer().getServer();
      WorldOptions originalOpts = originalWorldData.worldGenOptions();
      WorldOptions newOpts = this.options.getSeed().isPresent() ? originalOpts.withSeed(OptionalLong.of(this.seed)) : originalOpts;
      LevelSettings newWorldSettings = new LevelSettings(
         "faweregentempworld",
         originalWorldData.settings.gameType(),
         originalWorldData.settings.hardcore(),
         originalWorldData.settings.difficulty(),
         originalWorldData.settings.allowCommands(),
         originalWorldData.settings.gameRules(),
         originalWorldData.settings.getDataConfiguration()
      );
      SpecialWorldProperty specialWorldProperty = originalWorldData.isFlatWorld()
         ? SpecialWorldProperty.FLAT
         : (originalWorldData.isDebugWorld() ? SpecialWorldProperty.DEBUG : SpecialWorldProperty.NONE);
      PrimaryLevelData newWorldData = new PrimaryLevelData(newWorldSettings, newOpts, specialWorldProperty, Lifecycle.stable());
      BiomeProvider biomeProvider = this.getBiomeProvider();
      this.freshWorld = Fawe.instance()
         .getQueueHandler()
         .sync(
            () -> new ServerLevel(
               server,
               server.executor,
               this.session,
               newWorldData,
               this.originalServerWorld.dimension(),
               new LevelStem(this.originalServerWorld.dimensionTypeRegistration(), this.originalServerWorld.getChunkSource().getGenerator()),
               new PaperweightRegen.RegenNoOpWorldLoadListener(),
               this.originalServerWorld.isDebug(),
               this.seed,
               ImmutableList.of(),
               false,
               this.originalServerWorld.getRandomSequences(),
               environment,
               generator,
               biomeProvider
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
      newWorldData.checkName(this.originalServerWorld.serverLevelData.getLevelName());
      if (paperConfigField != null) {
         paperConfigField.set(this.freshWorld, this.originalServerWorld.paperConfig());
      }

      return true;
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

   private static class RegenNoOpWorldLoadListener implements ChunkProgressListener {
      public void updateSpawnPos(@Nonnull ChunkPos spawnPos) {
      }

      public void onStatusChange(@Nonnull final ChunkPos pos, @Nullable final ChunkStatus status) {
      }

      public void start() {
      }

      public void stop() {
      }
   }
}
