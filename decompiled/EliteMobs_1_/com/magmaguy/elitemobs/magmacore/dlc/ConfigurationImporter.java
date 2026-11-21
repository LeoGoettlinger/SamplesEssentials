package com.magmaguy.elitemobs.magmacore.dlc;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.magmaguy.elitemobs.magmacore.MagmaCore;
import com.magmaguy.elitemobs.magmacore.config.OutdatedConfigurationArchive;
import com.magmaguy.elitemobs.magmacore.events.ModelInstallationEvent;
import com.magmaguy.elitemobs.magmacore.initialization.PluginInitializationState;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.WorldFolderResolver;
import com.magmaguy.elitemobs.magmacore.util.ZipFile;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ConfigurationImporter {
   private static final int WINDOWS_ACCESS_RETRY_ATTEMPTS = 5;
   private static final long WINDOWS_ACCESS_RETRY_BASE_DELAY_MILLIS = 25L;
   private static final long SYNC_WORLD_UNLOAD_POLL_MILLIS = 25L;
   private final JavaPlugin ownerPlugin;
   private final Path eliteMobsPath;
   private final Path extractioncraftPath;
   private final Path betterStructuresPath;
   private final Path resurrectionChestPath;
   private final Path freeMinecraftModelsPath;
   private final Path modelEnginePath;
   private final Path eternalTDPath;
   private final Path megaBlockSurvivorsPath;
   private final Path worldCannonPath;
   private ConfigurationImporter.PluginPlatform pluginPlatform;
   private File importsFolder;
   private boolean modelsInstalled = false;
   private boolean eliteMobsContentImported = false;
   private boolean betterStructuresContentImported = false;

   public ConfigurationImporter(JavaPlugin ownerPlugin) {
      this.ownerPlugin = ownerPlugin == null ? MagmaCore.getInstance().getRequestingPlugin() : ownerPlugin;
      Path pluginsDirectory = Path.of(this.ownerPlugin.getDataFolder().getParentFile().getAbsolutePath());
      this.eliteMobsPath = pluginsDirectory.resolve("EliteMobs");
      this.extractioncraftPath = pluginsDirectory.resolve("Extractioncraft");
      this.betterStructuresPath = pluginsDirectory.resolve("BetterStructures");
      this.resurrectionChestPath = pluginsDirectory.resolve("ResurrectionChest");
      this.freeMinecraftModelsPath = pluginsDirectory.resolve("FreeMinecraftModels");
      this.modelEnginePath = pluginsDirectory.resolve("ModelEngine");
      this.eternalTDPath = pluginsDirectory.resolve("EternalTD");
      this.megaBlockSurvivorsPath = pluginsDirectory.resolve("MegaBlockSurvivors");
      this.worldCannonPath = pluginsDirectory.resolve("CannonRTP");
      if (this.createImportsDirectory()) {
         this.importsFolder = this.getImportsDirectory();
         if (this.importsFolder != null) {
            try {
               if (sortedChildren(this.importsFolder).length == 0) {
                  return;
               }
            } catch (IOException var6) {
               Logger.warn("Failed to inspect import directory " + this.importsFolder.getPath() + "!");
               var6.printStackTrace();
               return;
            }

            this.pluginPlatform = getPluginPlatform(this.ownerPlugin.getName());
            this.archiveOutdatedConfigurations("before import");
            this.processImportsFolder();
            this.archiveOutdatedConfigurations("after import");
            if (Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")
               && this.modelsInstalled
               && !this.ownerPlugin.getName().equals("FreeMinecraftModels")) {
               if (Bukkit.isPrimaryThread()) {
                  Bukkit.getPluginManager().callEvent(new ModelInstallationEvent());
               } else {
                  try {
                     Bukkit.getScheduler().callSyncMethod(this.ownerPlugin, () -> {
                        Bukkit.getPluginManager().callEvent(new ModelInstallationEvent());
                        return null;
                     }).get(30L, TimeUnit.SECONDS);
                  } catch (InterruptedException var4) {
                     Thread.currentThread().interrupt();
                     throw new IllegalStateException("Interrupted while starting imported model reload", var4);
                  } catch (TimeoutException | ExecutionException var5) {
                     throw new IllegalStateException("Could not start imported model reload", var5);
                  }
               }
            }

            if (this.betterStructuresContentImported) {
               this.requestBetterStructuresReload();
            }
         }
      }
   }

   private void requestBetterStructuresReload() {
      final Plugin betterStructures = Bukkit.getPluginManager().getPlugin("BetterStructures");
      if (betterStructures != null && betterStructures.isEnabled()) {
         final long deadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(10L);
         (new BukkitRunnable() {
               public void run() {
                  if (!betterStructures.isEnabled()) {
                     this.cancel();
                  } else {
                     PluginInitializationState state = MagmaCore.getInitializationState("BetterStructures");
                     if (state != PluginInitializationState.INITIALIZING || System.nanoTime() >= deadline) {
                        this.cancel();
                        if (state != PluginInitializationState.INITIALIZED) {
                           if (state == PluginInitializationState.INITIALIZING) {
                              Logger.warn(
                                 "BetterStructures was still initializing 10 minutes after "
                                    + ConfigurationImporter.this.ownerPlugin.getName()
                                    + " imported its content; run /bs reload to load it."
                              );
                           }
                        } else {
                           try {
                              betterStructures.getClass()
                                 .getMethod("reloadImportedContent", CommandSender.class)
                                 .invoke(betterStructures, Bukkit.getConsoleSender());
                           } catch (ReflectiveOperationException var3) {
                              Logger.warn(
                                 ConfigurationImporter.this.ownerPlugin.getName()
                                    + " imported BetterStructures content, but this BetterStructures version cannot reload it automatically; run /bs reload."
                              );
                           }
                        }
                     }
                  }
               }
            })
            .runTaskTimer(this.ownerPlugin, 1L, 20L);
      }
   }

   private static boolean deleteDirectory(File file) {
      final boolean[] complete = new boolean[]{true};

      try {
         Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
            private void failed(Path path, IOException failure) {
               complete[0] = false;
               Logger.warn("Could not clean up " + path + ": " + failure.getMessage());
            }

            private FileVisitResult delete(Path path) {
               try {
                  Files.deleteIfExists(path);
               } catch (IOException var3x) {
                  this.failed(path, var3x);
               }

               return FileVisitResult.CONTINUE;
            }

            public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) {
               return this.delete(path);
            }

            public FileVisitResult visitFileFailed(Path path, IOException failure) {
               if (!(failure instanceof NoSuchFileException)) {
                  this.failed(path, failure);
               }

               return FileVisitResult.CONTINUE;
            }

            public FileVisitResult postVisitDirectory(Path path, IOException failure) {
               if (failure != null) {
                  this.failed(path, failure);
               }

               return this.delete(path);
            }
         });
      } catch (IOException var3) {
         complete[0] = false;
         Logger.warn("Could not clean up " + file + ": " + var3.getMessage());
      }

      return complete[0];
   }

   private void archiveOutdatedConfigurations(String phase) {
      long started = System.nanoTime();

      try {
         for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof JavaPlugin target && (target.isEnabled() || target == this.ownerPlugin)) {
               OutdatedConfigurationArchive.archiveFor(target);
            }
         }
      } finally {
         Logger.info("Retirement scan " + phase + ": " + elapsedMillis(started) + " ms.");
      }
   }

   private static long elapsedMillis(long started) {
      return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
   }

   private void moveWorlds(File worldcontainerFile, ConfigurationImporter.ImportTransaction transaction) throws IOException {
      for (File file : sortedChildren(worldcontainerFile)) {
         File worldContainer = Bukkit.getWorldContainer().getCanonicalFile();
         Path worldContainerPath = worldContainer.toPath().normalize().toAbsolutePath();
         Path destinationPath = worldContainerPath.resolve(file.getName());
         File destinationFile = destinationPath.toFile();
         if (Bukkit.getWorld(file.getName()) != null || WorldFolderResolver.folderExists(file.getName())) {
            Logger.info("Overriding existing directory " + destinationFile.getPath());
            if (Bukkit.getWorld(file.getName()) != null) {
               boolean unloaded;
               if (Bukkit.isPrimaryThread()) {
                  unloaded = unloadWorldForImport(file.getName());
               } else {
                  Future<Boolean> unloadFuture = Bukkit.getScheduler().callSyncMethod(this.ownerPlugin, () -> unloadWorldForImport(file.getName()));
                  unloaded = awaitWorldUnload(unloadFuture, () -> MagmaCore.isShutdownRequested(this.ownerPlugin), file.getName());
               }

               if (!unloaded) {
                  throw new IOException(
                     "World " + file.getName() + " refused to unload; retaining the import archive rather than replacing a live world directory."
                  );
               }

               Logger.warn("Unloaded world " + file.getName() + " for safe replacement!");
            }

            transaction.displaceDirectory(WorldFolderResolver.legacyFolder(file.getName()));
            transaction.displaceDirectory(WorldFolderResolver.modernFolder(file.getName()));
         }

         moveDirectory(file, destinationPath, transaction);
      }
   }

   private static boolean unloadWorldForImport(String worldName) {
      World world = Bukkit.getWorld(worldName);
      if (world == null) {
         return true;
      } else {
         World fallback = Bukkit.getWorlds().stream().filter(candidate -> candidate != world).findFirst().orElse(null);
         if (fallback == null) {
            return false;
         } else {
            for (Player player : List.copyOf(world.getPlayers())) {
               if (!player.teleport(fallback.getSpawnLocation())) {
                  return false;
               }
            }

            return Bukkit.unloadWorld(world, false);
         }
      }
   }

   static boolean awaitWorldUnload(Future<Boolean> unloadFuture, BooleanSupplier shutdownRequested, String worldName) throws IOException {
      Objects.requireNonNull(unloadFuture, "unloadFuture");
      Objects.requireNonNull(shutdownRequested, "shutdownRequested");

      while (!shutdownRequested.getAsBoolean()) {
         try {
            boolean unloaded = unloadFuture.get(25L, TimeUnit.MILLISECONDS);
            if (shutdownRequested.getAsBoolean()) {
               throw cancelWorldUnloadForShutdown(unloadFuture, worldName, null);
            }

            return unloaded;
         } catch (TimeoutException var5) {
         } catch (InterruptedException var6) {
            Thread.currentThread().interrupt();
            throw cancelWorldUnloadForShutdown(unloadFuture, worldName, var6);
         } catch (CancellationException var7) {
            InterruptedIOException interrupted = new InterruptedIOException("Main-thread world unload was canceled for " + worldName + ".");
            interrupted.initCause(var7);
            throw interrupted;
         } catch (ExecutionException var8) {
            Throwable cause = (Throwable)(var8.getCause() == null ? var8 : var8.getCause());
            throw new IOException("Failed to unload world " + worldName + " on the main thread.", cause);
         }
      }

      throw cancelWorldUnloadForShutdown(unloadFuture, worldName, null);
   }

   private static InterruptedIOException cancelWorldUnloadForShutdown(Future<Boolean> unloadFuture, String worldName, Throwable cause) {
      unloadFuture.cancel(false);
      InterruptedIOException interrupted = new InterruptedIOException("Shutdown requested while waiting to unload world " + worldName + ".");
      if (cause != null) {
         interrupted.initCause(cause);
      }

      return interrupted;
   }

   private static void moveDirectory(File unzippedDirectory, Path targetPath, ConfigurationImporter.ImportTransaction transaction) throws IOException {
      transaction.prepareDirectory(targetPath);

      for (File file : sortedChildren(unzippedDirectory)) {
         mergeImportEntry(file.toPath(), targetPath.resolve(file.getName()), transaction);
      }
   }

   private static void moveFile(File file, Path targetPath, ConfigurationImporter.ImportTransaction transaction) throws IOException {
      transaction.prepareDirectory(targetPath);
      mergeImportEntry(file.toPath(), targetPath.resolve(file.getName()), transaction);
   }

   static void mergeImportEntry(Path source, Path destination) throws IOException {
      Path normalizedDestination = destination.normalize().toAbsolutePath();
      Path transactionParent = normalizedDestination.getParent();
      if (transactionParent == null) {
         throw new IOException("Import destination has no parent: " + destination);
      } else {
         Files.createDirectories(transactionParent);
         ConfigurationImporter.ImportTransaction transaction = ConfigurationImporter.ImportTransaction.create(transactionParent);

         try {
            mergeImportEntry(source, destination, transaction);
            transaction.commit();
         } catch (RuntimeException | IOException var6) {
            transaction.rollback(var6);
            throw var6;
         }
      }
   }

   private static void mergeImportEntry(Path source, Path destination, ConfigurationImporter.ImportTransaction transaction) throws IOException {
      Path normalizedSource = source.normalize().toAbsolutePath();
      Path normalizedDestination = destination.normalize().toAbsolutePath();
      if (Files.isDirectory(normalizedSource)) {
         transaction.prepareDirectory(normalizedDestination);

         try (Stream<Path> children = Files.list(normalizedSource)) {
            for (Path child : children.sorted(Comparator.comparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER)).toList()) {
               mergeImportEntry(child, normalizedDestination.resolve(child.getFileName()), transaction);
            }
         }
      } else if (!Files.isRegularFile(normalizedSource)) {
         throw new IOException("Import source is not a regular file: " + normalizedSource);
      } else {
         transaction.prepareFile(normalizedDestination);
         copyFileAtomicallyWithRetry(normalizedSource, normalizedDestination);
      }
   }

   private static void copyFileAtomicallyWithRetry(Path source, Path destination) throws IOException {
      for (int attempt = 1; attempt <= 5; attempt++) {
         try {
            copyFileAtomically(source, destination);
            return;
         } catch (AccessDeniedException var7) {
            if (attempt == 5) {
               throw var7;
            }

            try {
               Thread.sleep(25L << attempt - 1);
            } catch (InterruptedException var6) {
               Thread.currentThread().interrupt();
               InterruptedIOException interrupted = new InterruptedIOException("Interrupted while retrying import of " + source);
               interrupted.initCause(var6);
               throw interrupted;
            }
         }
      }
   }

   private static void copyFileAtomically(Path source, Path destination) throws IOException {
      String destinationName = destination.getFileName().toString();
      Path temporary = Files.createTempFile(destination.getParent(), "." + destinationName + ".import-", ".tmp");

      try {
         Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);

         try {
            Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
         } catch (AtomicMoveNotSupportedException var8) {
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
         }
      } finally {
         Files.deleteIfExists(temporary);
      }
   }

   private static File[] sortedChildren(File directory) throws IOException {
      File[] children = directory.listFiles();
      if (children == null) {
         throw new IOException("Failed to list directory " + directory.getPath());
      } else {
         Arrays.sort(children, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
         return children;
      }
   }

   private boolean createImportsDirectory() {
      Path configurationsPath = Paths.get(this.ownerPlugin.getDataFolder().getAbsolutePath());
      Path importsPath = configurationsPath.normalize().resolve("imports");
      if (!Files.isDirectory(importsPath)) {
         try {
            File importsFile = importsPath.toFile();
            if (!importsFile.getParentFile().exists()) {
               importsPath.toFile().mkdirs();
            }

            Files.createDirectories(importsPath);
            return true;
         } catch (Exception var4) {
            Logger.warn("Failed to create import directory! Tell the dev!");
            var4.printStackTrace();
            return false;
         }
      } else {
         return true;
      }
   }

   private File getImportsDirectory() {
      try {
         return Paths.get(this.ownerPlugin.getDataFolder().getCanonicalPath()).resolve("imports").toFile();
      } catch (Exception var2) {
         Logger.warn("Failed to get imports folder! Report this to the dev!");
         var2.printStackTrace();
         return null;
      }
   }

   static ConfigurationImporter.PluginPlatform getPluginPlatform(String name) {
      if (name == null) {
         return ConfigurationImporter.PluginPlatform.NONE;
      } else {
         String declaredPlatform = name.replace("\ufeff", "").trim();
         String var2 = declaredPlatform.toLowerCase(Locale.ROOT);
         switch (var2) {
            case "elitemobs":
               return ConfigurationImporter.PluginPlatform.ELITEMOBS;
            case "extractioncraft":
               return ConfigurationImporter.PluginPlatform.EXTRACTIONCRAFT;
            case "betterstructures":
               return ConfigurationImporter.PluginPlatform.BETTERSTRUCTURES;
            case "resurrectionchest":
               return ConfigurationImporter.PluginPlatform.RESURRECTIONCHEST;
            case "freeminecraftmodels":
               return ConfigurationImporter.PluginPlatform.FREEMINECRAFTMODELS;
            case "eternaltd":
               return ConfigurationImporter.PluginPlatform.ETERNALTD;
            case "megablocksurvivors":
               return ConfigurationImporter.PluginPlatform.MEGABLOCKSURVIVORS;
            case "cannonrtp":
            case "worldcannon":
            case "world_cannon":
               return ConfigurationImporter.PluginPlatform.WORLDCANNON;
            default:
               return ConfigurationImporter.PluginPlatform.NONE;
         }
      }
   }

   private void processImportsFolder() {
      File[] importEntries;
      try {
         importEntries = sortedChildren(this.importsFolder);
      } catch (IOException var12) {
         Logger.warn("Failed to inspect import directory " + this.importsFolder.getPath() + "!");
         var12.printStackTrace();
         return;
      }

      for (File zippedFile : importEntries) {
         if (zippedFile.isDirectory() || !isOsMetadataFile(zippedFile.getName())) {
            if (zippedFile.getName().endsWith(".zip")) {
               this.importFile(zippedFile, true);
            } else if (this.pluginPlatform == ConfigurationImporter.PluginPlatform.FREEMINECRAFTMODELS && zippedFile.getName().endsWith(".bbmodel")) {
               this.processBbmodel(zippedFile);
            } else if (!zippedFile.isDirectory()) {
               Logger.warn("File " + zippedFile.getPath() + " can't be imported! It will be skipped.");
            } else {
               boolean incorrectlyUnzippedFolder = false;

               File[] children;
               try {
                  children = sortedChildren(zippedFile);
               } catch (IOException var13) {
                  Logger.warn("Failed to inspect import directory " + zippedFile.getPath() + "!");
                  var13.printStackTrace();
                  continue;
               }

               for (File iteratedFile : children) {
                  if (iteratedFile.getName().equalsIgnoreCase("pack.meta")) {
                     incorrectlyUnzippedFolder = true;
                     break;
                  }
               }

               if (incorrectlyUnzippedFolder) {
                  this.importFile(zippedFile, false);
               }
            }
         }
      }
   }

   private static boolean isOsMetadataFile(String fileName) {
      return fileName.startsWith(".") || fileName.equalsIgnoreCase("Thumbs.db") || fileName.equalsIgnoreCase("desktop.ini");
   }

   private void importFile(File source, boolean zipped) {
      Path stagingDirectory = null;
      long extractionMillis = 0L;
      long installationMillis = 0L;
      boolean installed = false;

      try {
         File unzippedFolder = source;
         if (zipped) {
            long started = System.nanoTime();

            try {
               String archiveName = source.getName();
               String baseName = archiveName.substring(0, archiveName.length() - ".zip".length()).replaceAll("[^A-Za-z0-9._-]", "_");
               stagingDirectory = Files.createTempDirectory(this.importsFolder.toPath(), "." + baseName + ".extract-");
               unzippedFolder = ZipFile.unzip(source, stagingDirectory.toFile());
            } finally {
               extractionMillis = elapsedMillis(started);
            }
         }

         long started = System.nanoTime();

         try {
            installed = this.processUnzippedFile(unzippedFolder);
         } finally {
            installationMillis = elapsedMillis(started);
         }
      } catch (Exception var34) {
         Logger.warn("Failed to import " + source.getPath() + ": " + var34.getMessage());
         var34.printStackTrace();
      } finally {
         long started = System.nanoTime();
         boolean cleaned = true;
         if (stagingDirectory != null) {
            cleaned = deleteDirectory(stagingDirectory.toFile());
         }

         if (installed) {
            cleaned = deleteDirectory(source) && cleaned;
         }

         Logger.info(
            (installed ? "Imported " : "Import failed for ")
               + source.getName()
               + ": extraction "
               + extractionMillis
               + " ms, installation "
               + installationMillis
               + " ms, cleanup "
               + elapsedMillis(started)
               + " ms"
               + (cleaned ? "." : "; cleanup incomplete, see warnings.")
         );
         if (!installed) {
            Logger.warn("Retaining " + source.getPath() + " for retry.");
         }
      }
   }

   private boolean processUnzippedFile(File unzippedFolder) {
      ConfigurationImporter.PluginPlatform platform = this.pluginPlatform;

      File[] unzippedFiles;
      try {
         unzippedFiles = sortedChildren(unzippedFolder);
      } catch (IOException var13) {
         Logger.warn("Failed to inspect extracted import " + unzippedFolder.getPath() + "!");
         var13.printStackTrace();
         return false;
      }

      for (File unzippedFile : unzippedFiles) {
         if (unzippedFile.getName().equalsIgnoreCase("pack.meta")) {
            platform = getPluginPlatform(this.readPackMeta(unzippedFile));
         }
      }

      if (platform == ConfigurationImporter.PluginPlatform.NONE) {
         Logger.warn("Import " + unzippedFolder.getPath() + " does not declare a supported plugin platform; the archive will be retained.");
         return false;
      } else {
         List<ConfigurationImporter.ImportCandidate> candidates = new ArrayList<>();
         boolean rejected = false;

         for (File unzippedFilex : unzippedFiles) {
            if (!ConfigurationImportRegistry.isSkippedFolder(unzippedFilex.getName())) {
               Path targetPath = this.getTargetPath(unzippedFilex.getName(), platform);
               if (targetPath == null) {
                  if (!ConfigurationImportRegistry.hasResolver(unzippedFilex.getName(), platform)) {
                     rejected = true;
                     Logger.warn("Rejecting import because top-level entry '" + unzippedFilex.getName() + "' is not recognized for " + platform + ".");
                  }
               } else if (unzippedFilex.getName().equalsIgnoreCase("freeminecraftmodels") && !this.alignFmmModelsFolder(unzippedFilex)) {
                  rejected = true;
               } else {
                  candidates.add(new ConfigurationImporter.ImportCandidate(unzippedFilex, targetPath));
               }
            }
         }

         if (!rejected && !candidates.isEmpty()) {
            ConfigurationImporter.ImportTransaction transaction;
            try {
               transaction = ConfigurationImporter.ImportTransaction.create(this.importsFolder.toPath());
            } catch (IOException var12) {
               Logger.warn("Failed to create a transaction for import " + unzippedFolder.getPath() + "; the archive will be retained.");
               var12.printStackTrace();
               return false;
            }

            try {
               for (ConfigurationImporter.ImportCandidate candidate : candidates) {
                  this.moveUnzippedFiles(candidate.source(), candidate.targetPath(), transaction);
               }

               transaction.commit();

               for (ConfigurationImporter.ImportCandidate candidate : candidates) {
                  this.recordSuccessfulImport(candidate);
               }

               return true;
            } catch (RuntimeException | IOException var14) {
               Exception exception = var14;
               Logger.warn(
                  "Failed to import " + unzippedFolder.getPath() + "; rolling back every destination changed by this archive and retaining it for retry."
               );

               try {
                  transaction.rollback(exception);
               } catch (IOException var11) {
                  Logger.warn("Import rollback was incomplete for " + unzippedFolder.getPath() + "; inspect the logged destinations before retrying.");
                  var11.printStackTrace();
               }

               var14.printStackTrace();
               return false;
            }
         } else {
            if (candidates.isEmpty()) {
               Logger.warn("Import " + unzippedFolder.getPath() + " contains no installable content.");
            }

            return false;
         }
      }
   }

   private boolean alignFmmModelsFolder(File fmmFolder) {
      String modelsName = ConfigurationImportRegistry.resolveFmmModelsFolder(this).getFileName().toString();
      File[] children = fmmFolder.listFiles(File::isDirectory);
      if (children == null) {
         return true;
      } else {
         for (File child : children) {
            if (child.getName().equalsIgnoreCase("models") && !child.getName().equals(modelsName) && !child.renameTo(new File(fmmFolder, modelsName))) {
               Logger.warn("Rejecting import because " + child.getPath() + " could not be aligned with FreeMinecraftModels' " + modelsName + " folder.");
               return false;
            }
         }

         return true;
      }
   }

   private void moveUnzippedFiles(File unzippedFile, Path targetPath, ConfigurationImporter.ImportTransaction transaction) throws IOException {
      transaction.prepareDirectory(targetPath);
      if (unzippedFile.isDirectory()) {
         if (unzippedFile.getName().equalsIgnoreCase("worldcontainer")) {
            this.moveWorlds(unzippedFile, transaction);
         } else {
            moveDirectory(unzippedFile, targetPath, transaction);
         }
      } else {
         moveFile(unzippedFile, targetPath, transaction);
      }
   }

   private void recordSuccessfulImport(ConfigurationImporter.ImportCandidate candidate) {
      String sourceName = candidate.source().getName();
      if (sourceName.equalsIgnoreCase("models")
         || sourceName.equalsIgnoreCase("modelengine")
         || candidate.targetPath().normalize().toAbsolutePath().startsWith(this.freeMinecraftModelsPath.normalize().toAbsolutePath())) {
         this.modelsInstalled = true;
      }

      if (!this.ownerPlugin.getName().equalsIgnoreCase("EliteMobs")
         && candidate.targetPath().normalize().toAbsolutePath().startsWith(this.eliteMobsPath.normalize().toAbsolutePath())) {
         this.eliteMobsContentImported = true;
      }

      if (!this.ownerPlugin.getName().equalsIgnoreCase("BetterStructures")
         && candidate.targetPath().normalize().toAbsolutePath().startsWith(this.betterStructuresPath.normalize().toAbsolutePath())) {
         this.betterStructuresContentImported = true;
      }
   }

   private Path getTargetPath(String folder, ConfigurationImporter.PluginPlatform platform) {
      return ConfigurationImportProfiles.resolve(this, folder, platform);
   }

   static void mergeImportEntriesAtomically(List<ConfigurationImporter.ImportTransfer> transfers, Path transactionWorkspace) throws IOException {
      ConfigurationImporter.ImportTransaction transaction = ConfigurationImporter.ImportTransaction.create(transactionWorkspace);

      try {
         for (ConfigurationImporter.ImportTransfer transfer : transfers) {
            mergeImportEntry(transfer.source(), transfer.destination(), transaction);
         }

         transaction.commit();
      } catch (RuntimeException | IOException var5) {
         transaction.rollback(var5);
         throw var5;
      }
   }

   Path getEliteMobsPath() {
      return this.eliteMobsPath;
   }

   Path getExtractioncraftPath() {
      return this.extractioncraftPath;
   }

   Path getBetterStructuresPath() {
      return this.betterStructuresPath;
   }

   Path getResurrectionChestPath() {
      return this.resurrectionChestPath;
   }

   Path getFreeMinecraftModelsPath() {
      return this.freeMinecraftModelsPath;
   }

   Path getModelEnginePath() {
      return this.modelEnginePath;
   }

   Path getEternalTDPath() {
      return this.eternalTDPath;
   }

   Path getMegaBlockSurvivorsPath() {
      return this.megaBlockSurvivorsPath;
   }

   Path getWorldCannonPath() {
      return this.worldCannonPath;
   }

   void markModelsInstalled() {
      this.modelsInstalled = true;
   }

   public boolean isModelsInstalled() {
      return this.modelsInstalled;
   }

   public boolean isEliteMobsContentImported() {
      return this.eliteMobsContentImported;
   }

   private String readPackMeta(File packMetaFile) {
      if (packMetaFile != null && packMetaFile.exists()) {
         try {
            Path filePath = packMetaFile.getCanonicalFile().toPath().normalize().toAbsolutePath();
            return Files.readString(filePath, StandardCharsets.UTF_8);
         } catch (IOException var3) {
            Logger.warn("Failed to read pack.meta file " + packMetaFile.getPath() + ". Ensure the file is readable.");
            var3.printStackTrace();
            return null;
         }
      } else {
         Logger.warn("File " + (packMetaFile != null ? packMetaFile.getPath() : "null") + " does not exist or is not valid.");
         return null;
      }
   }

   private void processBbmodel(File bbmodelFile) {
      Gson readerGson = new Gson();

      Reader reader;
      try {
         reader = Files.newBufferedReader(bbmodelFile.getCanonicalFile().toPath());
      } catch (Exception var17) {
         Logger.warn("Failed to read file " + bbmodelFile.getAbsolutePath());
         return;
      }

      Map<?, ?> jsonMap = (Map<?, ?>)readerGson.fromJson(reader, Map.class);
      int blockBenchVersion = this.detectVersion(jsonMap);
      List mergedOutliner = this.mergeGroupsAndOutliner(jsonMap, blockBenchVersion);
      Gson writerGson = new Gson();
      HashMap<String, Object> minifiedMap = new HashMap<>();
      minifiedMap.put("meta", jsonMap.get("meta"));
      minifiedMap.put("resolution", jsonMap.get("resolution"));
      minifiedMap.put("elements", jsonMap.get("elements"));
      minifiedMap.put("outliner", mergedOutliner);
      ArrayList<Map> minifiedTextures = new ArrayList<>();
      ((ArrayList)jsonMap.get("textures"))
         .forEach(
            innerMap -> minifiedTextures.add(
               Map.of(
                  "source", ((LinkedTreeMap)innerMap).get("source"), "id", ((LinkedTreeMap)innerMap).get("id"), "name", ((LinkedTreeMap)innerMap).get("name")
               )
            )
         );
      minifiedMap.put("textures", minifiedTextures);
      minifiedMap.put("animations", jsonMap.get("animations"));
      List<String> parentFiles = new ArrayList<>();
      File currentFile = bbmodelFile;

      while (true) {
         File parentFile = currentFile.getParentFile();
         if (parentFile.getName().equals("imports")) {
            String modelsDirectory = ConfigurationImportRegistry.resolveFmmModelsFolder(this).toFile().getAbsolutePath();
            String pathName;
            if (parentFiles.isEmpty()) {
               pathName = modelsDirectory + File.separatorChar + bbmodelFile.getName().replace(".bbmodel", ".fmmodel");
            } else {
               StringBuilder sb = new StringBuilder(modelsDirectory);

               for (int i = parentFiles.size() - 1; i >= 0; i--) {
                  sb.append(File.separatorChar).append(parentFiles.get(i));
               }

               sb.append(File.separatorChar).append(bbmodelFile.getName().replace(".bbmodel", ".fmmodel"));
               pathName = sb.toString();
            }

            try {
               FileUtils.writeStringToFile(new File(pathName), writerGson.toJson(minifiedMap), StandardCharsets.UTF_8);
               return;
            } catch (IOException var16) {
               Logger.warn("Failed to generate the minified file!");
               var16.printStackTrace();
               throw new RuntimeException(var16);
            }
         }

         parentFiles.add(parentFile.getName());
         currentFile = parentFile;
      }
   }

   private int detectVersion(Map<?, ?> bbmodelData) {
      try {
         Map<?, ?> meta = (Map<?, ?>)bbmodelData.get("meta");
         if (meta == null) {
            Logger.warn("Missing 'meta' field in model. Defaulting to version 4.");
            return 4;
         } else {
            Object versionObj = meta.get("format_version");
            if (versionObj == null) {
               Logger.warn("Missing 'format_version' in meta. Defaulting to version 4.");
               return 4;
            } else {
               String versionStr = versionObj.toString();
               String[] parts = versionStr.split("\\.");
               return Integer.parseInt(parts[0]);
            }
         }
      } catch (Exception var6) {
         Logger.warn("Failed to parse format_version. Error: " + var6.getMessage() + ". Defaulting to version 4.");
         return 4;
      }
   }

   private List mergeGroupsAndOutliner(Map<?, ?> bbmodelData, int blockBenchVersion) {
      List outlinerValues = (ArrayList)bbmodelData.get("outliner");
      if (blockBenchVersion < 5) {
         return outlinerValues;
      } else {
         List groupsList = (ArrayList)bbmodelData.get("groups");
         if (groupsList == null) {
            return outlinerValues;
         } else {
            HashMap<String, Map> groupsMap = new HashMap<>();

            for (Object groupObj : groupsList) {
               if (groupObj instanceof Map group) {
                  String uuid = (String)group.get("uuid");
                  if (uuid != null) {
                     groupsMap.put(uuid, group);
                  }
               }
            }

            return this.processOutlinerItems(outlinerValues, groupsMap);
         }
      }
   }

   private List processOutlinerItems(List items, HashMap<String, Map> groupsMap) {
      List result = new ArrayList();

      for (Object item : items) {
         if (item instanceof String) {
            result.add(item);
         } else if (item instanceof Map outlinerItem) {
            String uuid = (String)outlinerItem.get("uuid");
            Map mergedItem;
            if (uuid != null && groupsMap.containsKey(uuid)) {
               Map groupData = groupsMap.get(uuid);
               mergedItem = new HashMap(groupData);
            } else {
               mergedItem = new HashMap(outlinerItem);
            }

            if (outlinerItem.containsKey("children")) {
               List children = (List)outlinerItem.get("children");
               if (children != null && !children.isEmpty()) {
                  List processedChildren = this.processOutlinerItems(children, groupsMap);
                  mergedItem.put("children", processedChildren);
               }
            }

            result.add(mergedItem);
         }
      }

      return result;
   }

   private record ImportCandidate(File source, Path targetPath) {
   }

   private static final class ImportTransaction {
      private final Path backupRoot;
      private final Map<Path, Path> fileBackups = new LinkedHashMap<>();
      private final Map<Path, Path> displacedDirectories = new LinkedHashMap<>();
      private final Set<Path> createdFiles = new LinkedHashSet<>();
      private final Set<Path> createdDirectories = new LinkedHashSet<>();
      private int nextBackupId;
      private boolean closed;

      private ImportTransaction(Path backupRoot) {
         this.backupRoot = backupRoot;
      }

      static ConfigurationImporter.ImportTransaction create(Path workspace) throws IOException {
         Path normalizedWorkspace = workspace.normalize().toAbsolutePath();
         Files.createDirectories(normalizedWorkspace);
         return new ConfigurationImporter.ImportTransaction(Files.createTempDirectory(normalizedWorkspace, ".import-transaction-"));
      }

      void prepareDirectory(Path directory) throws IOException {
         this.ensureOpen();
         Path normalized = directory.normalize().toAbsolutePath();
         if (Files.exists(normalized, LinkOption.NOFOLLOW_LINKS)) {
            if (Files.isSymbolicLink(normalized) || !Files.isDirectory(normalized, LinkOption.NOFOLLOW_LINKS)) {
               throw new IOException("Import directory target is not a safe directory: " + normalized);
            }
         } else {
            List<Path> missing = new ArrayList<>();

            Path cursor;
            for (cursor = normalized; cursor != null && !Files.exists(cursor, LinkOption.NOFOLLOW_LINKS); cursor = cursor.getParent()) {
               missing.add(cursor);
            }

            if (cursor == null || !Files.isSymbolicLink(cursor) && Files.isDirectory(cursor, LinkOption.NOFOLLOW_LINKS)) {
               Files.createDirectories(normalized);
               this.createdDirectories.addAll(missing);
            } else {
               throw new IOException("Import directory parent is not a safe directory: " + cursor);
            }
         }
      }

      void prepareFile(Path destination) throws IOException {
         this.ensureOpen();
         Path normalized = destination.normalize().toAbsolutePath();
         Path parent = normalized.getParent();
         if (parent == null) {
            throw new IOException("Import file target has no parent: " + normalized);
         } else {
            this.prepareDirectory(parent);
            if (!this.fileBackups.containsKey(normalized) && !this.createdFiles.contains(normalized)) {
               if (Files.exists(normalized, LinkOption.NOFOLLOW_LINKS)) {
                  if (Files.isSymbolicLink(normalized) || !Files.isRegularFile(normalized, LinkOption.NOFOLLOW_LINKS)) {
                     throw new IOException("Import file target is not a safe regular file: " + normalized);
                  }

                  Path backup = this.nextBackupPath("files");
                  Files.copy(normalized, backup, StandardCopyOption.COPY_ATTRIBUTES);
                  this.fileBackups.put(normalized, backup);
               } else {
                  this.createdFiles.add(normalized);
               }
            }
         }
      }

      void displaceDirectory(Path directory) throws IOException {
         this.ensureOpen();
         Path normalized = directory.normalize().toAbsolutePath();
         if (!this.displacedDirectories.containsKey(normalized) && Files.exists(normalized, LinkOption.NOFOLLOW_LINKS)) {
            if (!Files.isSymbolicLink(normalized) && Files.isDirectory(normalized, LinkOption.NOFOLLOW_LINKS)) {
               Path backup = this.nextBackupPath("directories");
               moveWithAtomicFallback(normalized, backup);
               this.displacedDirectories.put(normalized, backup);
            } else {
               throw new IOException("World import target is not a safe directory: " + normalized);
            }
         }
      }

      void commit() {
         this.ensureOpen();
         this.closed = true;

         try {
            deleteRecursively(this.backupRoot);
         } catch (IOException var2) {
            Logger.warn("Imported content successfully, but failed to remove transaction backup " + this.backupRoot + ": " + var2.getMessage());
         }
      }

      void rollback(Throwable originalFailure) throws IOException {
         this.ensureOpen();
         this.closed = true;
         IOException rollbackFailure = null;

         for (Path created : reverseByDepth(this.createdFiles)) {
            rollbackFailure = collectFailure(rollbackFailure, () -> Files.deleteIfExists(created));
         }

         for (Entry<Path, Path> backup : this.fileBackups.entrySet()) {
            rollbackFailure = collectFailure(rollbackFailure, () -> ConfigurationImporter.copyFileAtomicallyWithRetry(backup.getValue(), backup.getKey()));
         }

         for (Path created : reverseByDepth(this.createdDirectories)) {
            rollbackFailure = collectFailure(rollbackFailure, () -> Files.deleteIfExists(created));
         }

         List<Entry<Path, Path>> displaced = new ArrayList<>(this.displacedDirectories.entrySet());
         Collections.reverse(displaced);

         for (Entry<Path, Path> backup : displaced) {
            rollbackFailure = collectFailure(rollbackFailure, () -> {
               if (Files.exists(backup.getKey(), LinkOption.NOFOLLOW_LINKS)) {
                  deleteRecursively(backup.getKey());
               }

               Path parent = backup.getKey().getParent();
               if (parent != null) {
                  Files.createDirectories(parent);
               }

               moveWithAtomicFallback(backup.getValue(), backup.getKey());
            });
         }

         if (rollbackFailure != null) {
            Logger.warn(
               "Import rollback was incomplete. Recovery copies are retained at " + this.backupRoot + ". Restore the affected destinations before retrying."
            );
            this.fileBackups.forEach((destination, backupx) -> Logger.warn("Import recovery file: " + backupx + " -> " + destination));
            this.displacedDirectories.forEach((destination, backupx) -> Logger.warn("Import recovery directory: " + backupx + " -> " + destination));
            rollbackFailure.addSuppressed(originalFailure);
            throw new IOException("Import rollback failed; recovery copies retained at " + this.backupRoot, rollbackFailure);
         } else {
            try {
               deleteRecursively(this.backupRoot);
            } catch (IOException var6) {
               var6.addSuppressed(originalFailure);
               throw var6;
            }
         }
      }

      private Path nextBackupPath(String kind) throws IOException {
         Path directory = this.backupRoot.resolve(kind);
         Files.createDirectories(directory);
         return directory.resolve(String.format(Locale.ROOT, "%08d", this.nextBackupId++));
      }

      private void ensureOpen() {
         if (this.closed) {
            throw new IllegalStateException("Import transaction is already closed.");
         }
      }

      private static List<Path> reverseByDepth(Collection<Path> paths) {
         return paths.stream().sorted(Comparator.comparingInt(Path::getNameCount).reversed()).toList();
      }

      private static IOException collectFailure(IOException existing, ConfigurationImporter.ImportTransaction.IoOperation operation) {
         try {
            operation.run();
            return existing;
         } catch (IOException var3) {
            if (existing == null) {
               return var3;
            } else {
               existing.addSuppressed(var3);
               return existing;
            }
         }
      }

      private static void moveWithAtomicFallback(Path source, Path destination) throws IOException {
         try {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException var3) {
            Files.move(source, destination);
         }
      }

      private static void deleteRecursively(Path root) throws IOException {
         if (Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
            if (!Files.isSymbolicLink(root) && Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
               try (Stream<Path> paths = Files.walk(root)) {
                  for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                     Files.deleteIfExists(path);
                  }
               }
            } else {
               Files.deleteIfExists(root);
            }
         }
      }

      @FunctionalInterface
      private interface IoOperation {
         void run() throws IOException;
      }
   }

   record ImportTransfer(Path source, Path destination) {
      ImportTransfer(Path source, Path destination) {
         Objects.requireNonNull(source, "source");
         Objects.requireNonNull(destination, "destination");
         this.source = source;
         this.destination = destination;
      }
   }

   static enum PluginPlatform {
      ELITEMOBS,
      EXTRACTIONCRAFT,
      BETTERSTRUCTURES,
      FREEMINECRAFTMODELS,
      ETERNALTD,
      MEGABLOCKSURVIVORS,
      WORLDCANNON,
      RESURRECTIONCHEST,
      NONE;
   }
}
