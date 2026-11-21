package com.sk89q.worldedit;

import com.fastasyncworldedit.core.function.mask.BlockMaskBuilder;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.util.formatting.component.TextUtils;
import com.sk89q.worldedit.util.io.file.ArchiveNioSupports;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import com.sk89q.worldedit.world.snapshot.SnapshotRepository;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotDatabase;
import com.sk89q.worldedit.world.snapshot.experimental.fs.FileSystemSnapshotDatabase;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.apache.logging.log4j.Logger;

public abstract class LocalConfiguration {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   public static int MAX_RADIUS;
   public static int MAX_SUPER_RADIUS;
   public static int MAX_BRUSH_RADIUS;
   public static int MAX_BUTCHER_RADIUS;
   public boolean profile = false;
   public boolean traceUnflushedSessions = true;
   public Set<String> disallowedBlocks = new HashSet<>();
   protected BlockMask disallowedBlocksMask;
   @Deprecated
   public int defaultChangeLimit = -1;
   @Deprecated
   public int maxChangeLimit = -1;
   public int defaultVerticalHeight = 256;
   public int defaultMaxPolygonalPoints = -1;
   public int maxPolygonalPoints = 20;
   public int defaultMaxPolyhedronPoints = -1;
   public int maxPolyhedronPoints = 20;
   public String shellSaveType = "";
   public boolean snapshotsConfigured = false;
   public SnapshotRepository snapshotRepo = null;
   public SnapshotDatabase snapshotDatabase = null;
   @Deprecated
   public int maxRadius = -1;
   @Deprecated
   public int maxSuperPickaxeSize = 5;
   @Deprecated
   public int maxBrushRadius = 6;
   public boolean logCommands = false;
   public String logFile = "";
   public String logFormat = "[%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s]: %5$s%6$s%n";
   public boolean registerHelp = true;
   public String wandItem = "minecraft:wooden_axe";
   public boolean superPickaxeDrop = true;
   public boolean superPickaxeManyDrop = true;
   public boolean useInventory = false;
   public boolean useInventoryOverride = false;
   public boolean useInventoryCreativeOverride = false;
   public boolean navigationUseGlass = true;
   public String navigationWand = "minecraft:compass";
   public int navigationWandMaxDistance = 50;
   public int scriptTimeout = 3000;
   public int calculationTimeout = 100;
   public int maxCalculationTimeout = 300;
   public Set<String> allowedDataCycleBlocks = new HashSet<>();
   public String saveDir = "schematics";
   public String scriptsDir = "craftscripts";
   public boolean showHelpInfo = true;
   public int butcherDefaultRadius = -1;
   @Deprecated
   public int butcherMaxRadius = -1;
   public boolean allowSymlinks = false;
   public boolean serverSideCUI = true;
   public boolean extendedYLimit = false;
   public boolean commandBlockSupport = false;
   public String defaultLocaleName = "default";
   public Locale defaultLocale = Locale.getDefault();

   protected String[] getDefaultDisallowedBlocks() {
      List<BlockType> blockTypes = Lists.newArrayList();
      return blockTypes.stream().filter(Objects::nonNull).map(BlockType::id).toArray(String[]::new);
   }

   public abstract void load();

   public boolean checkDisallowedBlocks(BlockStateHolder holder) {
      if (this.disallowedBlocks.isEmpty()) {
         return false;
      } else {
         if (this.disallowedBlocksMask == null) {
            BlockMaskBuilder builder = new BlockMaskBuilder();

            for (String blockRegex : this.disallowedBlocks) {
               try {
                  builder.addRegex(blockRegex);
               } catch (InputParseException var6) {
                  var6.printStackTrace();
               }
            }

            this.disallowedBlocksMask = builder.build(new NullExtent());
         }

         return this.disallowedBlocksMask.test(holder.toImmutableState());
      }
   }

   @Deprecated
   public File getWorkingDirectory() {
      return this.getWorkingDirectoryPath().toFile();
   }

   public Path getWorkingDirectoryPath() {
      return Paths.get(".");
   }

   public void initializeSnapshotConfiguration(String directory, boolean experimental) {
      this.snapshotRepo = null;
      this.snapshotDatabase = null;
      this.snapshotsConfigured = false;
      if (!directory.isEmpty()) {
         if (experimental) {
            try {
               this.snapshotDatabase = FileSystemSnapshotDatabase.maybeCreate(Paths.get(directory), ArchiveNioSupports.combined());
               this.snapshotsConfigured = true;
            } catch (IOException var4) {
               LOGGER.warn("Failed to open snapshotDatabase", var4);
            }
         } else {
            this.snapshotRepo = new SnapshotRepository(directory);
            this.snapshotsConfigured = true;
         }
      }
   }

   public String convertLegacyItem(String legacy) {
      String item = legacy;

      try {
         String[] splitter = item.split(":", 2);
         int id = 0;
         byte data = 0;
         if (splitter.length == 1) {
            id = Integer.parseInt(item);
         } else {
            id = Integer.parseInt(splitter[0]);
            data = Byte.parseByte(splitter[1]);
         }

         item = LegacyMapper.getInstance().getItemFromLegacy(id, data).id();
      } catch (Throwable var6) {
      }

      return item;
   }

   public void setDefaultLocaleName(String localeName) {
      this.defaultLocaleName = localeName;
      if (localeName.equals("default")) {
         this.defaultLocale = Locale.getDefault();
      } else {
         this.defaultLocale = TextUtils.getLocaleByMinecraftTag(localeName);
      }
   }
}
