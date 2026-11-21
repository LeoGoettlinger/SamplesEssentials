package com.magmaguy.elitemobs.magmacore;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandManager;
import com.magmaguy.elitemobs.magmacore.command.LogifyCommand;
import com.magmaguy.elitemobs.magmacore.command.NightbreakCommand;
import com.magmaguy.elitemobs.magmacore.command.NightbreakLoginCommand;
import com.magmaguy.elitemobs.magmacore.command.NightbreakLogoutCommand;
import com.magmaguy.elitemobs.magmacore.config.OutdatedConfigurationArchive;
import com.magmaguy.elitemobs.magmacore.dlc.ConfigurationImporter;
import com.magmaguy.elitemobs.magmacore.initialization.PluginInitializationConfig;
import com.magmaguy.elitemobs.magmacore.initialization.PluginInitializationContext;
import com.magmaguy.elitemobs.magmacore.initialization.PluginInitializationManager;
import com.magmaguy.elitemobs.magmacore.initialization.PluginInitializationState;
import com.magmaguy.elitemobs.magmacore.instance.InstanceProtector;
import com.magmaguy.elitemobs.magmacore.match.MatchCore;
import com.magmaguy.elitemobs.magmacore.menus.AdvancedMenuHandler;
import com.magmaguy.elitemobs.magmacore.menus.SetupMenu;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakBulkDownloader;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakContentRefresher;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakLogoutMessages;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakPluginSpec;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakPluginStateRegistry;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakPluginUpdater;
import com.magmaguy.elitemobs.magmacore.thirdparty.CustomBiomeCompatibility;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.TemporaryBlockManager;
import com.magmaguy.elitemobs.magmacore.util.VersionChecker;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

public final class MagmaCore {
   private static MagmaCore instance;
   private static final Map<String, JavaPlugin> registeredPlugins = new HashMap<>();
   private static final Set<String> listenerRegistrations = new HashSet<>();
   private static boolean instanceProtectorRegistered = false;
   private final JavaPlugin requestingPlugin;
   private static final String NB_RSP_RESOURCE_PATH = "nightbreak_rsp_defaults";
   private static final String NB_RSP_CHECKSUM_FILE = ".nb_rsp_checksum_v3";

   private MagmaCore(JavaPlugin requestingPlugin, NightbreakPluginSpec pluginSpec) {
      instance = this;
      this.requestingPlugin = requestingPlugin;
      new AdvancedMenuHandler();
      CustomBiomeCompatibility.initializeMappings();
      Logger.info("MagmaCore v1.29-SNAPSHOT initialized!");
      instance.registerLogify();
      instance.registerNightbreakLogin();
      instance.registerNightbreakLogout(pluginSpec);
      instance.registerNightbreak();
      NightbreakAccount.initialize(requestingPlugin);
   }

   public static void checkVersionUpdate(String resourceID, String downloadURL) {
      VersionChecker.VersionCheckerEvents.setDownloadURL(downloadURL);
      VersionChecker.checkPluginVersion(resourceID, downloadURL);
      Bukkit.getPluginManager().registerEvents(new VersionChecker.VersionCheckerEvents(), instance.requestingPlugin);
   }

   public static void onEnable() {
      onEnable(instance.requestingPlugin);
   }

   public static void onEnable(JavaPlugin plugin) {
      if (plugin != null) {
         PluginInitializationManager.onEnable(plugin);
         if (listenerRegistrations.add(plugin.getName())) {
            Bukkit.getPluginManager().registerEvents(new SetupMenu.SetupMenuListeners(), plugin);
            Bukkit.getPluginManager().registerEvents(new AdvancedMenuHandler.AdvancedMenuListeners(), plugin);
            TemporaryBlockManager.initialize(plugin);
         }
      }
   }

   public static void enableWorldProtections() {
      enableWorldProtections(instance.requestingPlugin);
   }

   public static void enableWorldProtections(JavaPlugin plugin) {
      if (!instanceProtectorRegistered) {
         Bukkit.getPluginManager().registerEvents(new InstanceProtector(), plugin);
         instanceProtectorRegistered = true;
      }
   }

   public static MagmaCore createInstance(JavaPlugin requestingPlugin) {
      return createInstance(requestingPlugin, null);
   }

   public static MagmaCore createInstance(JavaPlugin requestingPlugin, NightbreakPluginSpec pluginSpec) {
      OutdatedConfigurationArchive.register(requestingPlugin);
      registeredPlugins.put(requestingPlugin.getName(), requestingPlugin);
      if (instance == null) {
         return new MagmaCore(requestingPlugin, pluginSpec);
      } else {
         NightbreakAccount.initialize(requestingPlugin);
         return instance;
      }
   }

   public static void shutdown() {
      shutdownNMSAdapter();
      CommandManager.shutdown();
      CustomBiomeCompatibility.shutdown();
      MatchCore.shutdown();
      InstanceProtector.shutdown();
      instanceProtectorRegistered = false;
      TemporaryBlockManager.shutdown();
   }

   public static void shutdown(JavaPlugin plugin) {
      if (plugin != null) {
         OutdatedConfigurationArchive.unregister(plugin);
         registeredPlugins.remove(plugin.getName());
         listenerRegistrations.remove(plugin.getName());
         NightbreakBulkDownloader.shutdown(plugin);
         NightbreakContentRefresher.shutdown(plugin);
         PluginInitializationManager.shutdown(plugin);
         NightbreakPluginUpdater.shutdown(plugin);
         NightbreakPluginStateRegistry.clear(plugin);
      }

      shutdown();
   }

   private static void shutdownNMSAdapter() {
      try {
         Class<?> nmsManagerClass = Class.forName("com.magmaguy.elitemobs.easyminecraftgoals.NMSManager");
         nmsManagerClass.getMethod("shutdown").invoke(null);
      } catch (ClassNotFoundException var1) {
      } catch (ReflectiveOperationException var2) {
         Logger.warn("Failed to shut down NMS adapter: " + var2.getMessage());
      }
   }

   public static ConfigurationImporter initializeImporter() {
      return initializeImporter(instance.requestingPlugin);
   }

   public static ConfigurationImporter initializeImporter(JavaPlugin plugin) {
      if (instance == null) {
         Bukkit.getLogger().warning("Attempted to initialize importer without first instantiating MagmaCore!");
         return null;
      } else {
         return new ConfigurationImporter(plugin);
      }
   }

   public static JavaPlugin getRegisteredPlugin(String pluginName) {
      return registeredPlugins.get(pluginName);
   }

   public static Collection<JavaPlugin> getRegisteredPlugins() {
      return Collections.unmodifiableCollection(registeredPlugins.values());
   }

   public static void startInitialization(
      JavaPlugin plugin,
      PluginInitializationConfig config,
      Consumer<PluginInitializationContext> asyncInitialization,
      Consumer<PluginInitializationContext> syncInitialization,
      Runnable onSuccess,
      Consumer<Throwable> onFailure
   ) {
      PluginInitializationManager.run(plugin, config, context -> {
         OutdatedConfigurationArchive.archiveFor(plugin);
         asyncInitialization.accept(context);
      }, syncInitialization, onSuccess, onFailure);
   }

   public static PluginInitializationState getInitializationState(String pluginName) {
      return PluginInitializationManager.getState(pluginName);
   }

   public static boolean isPluginReady(String pluginName) {
      return PluginInitializationManager.isPluginReady(pluginName);
   }

   public static void requestInitializationShutdown(JavaPlugin plugin) {
      PluginInitializationManager.requestShutdown(plugin);
   }

   public static boolean isShutdownRequested(JavaPlugin plugin) {
      return PluginInitializationManager.isShutdownRequested(plugin);
   }

   private void registerLogify() {
      SimpleCommandMap commandMap = null;

      try {
         Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
         f.setAccessible(true);
         commandMap = (SimpleCommandMap)f.get(Bukkit.getServer());
      } catch (ReflectiveOperationException var3) {
         this.requestingPlugin.getLogger().warning("Couldn’t access CommandMap: " + var3.getMessage());
         return;
      }

      if (commandMap.getCommand("logify") != null) {
         this.requestingPlugin.getLogger().info("/logify is already registered, skipping.");
      } else {
         if (Bukkit.getPluginManager().getPermission("logify.*") == null) {
            Permission perm = new Permission(
               "logify.*", "Lets admins run the /logify command, which sends the current latest server log to mclo.gs.", PermissionDefault.OP
            );
            Bukkit.getPluginManager().addPermission(perm);
         }

         Command wrapper = AdvancedCommand.toBukkitCommand(this.requestingPlugin, new LogifyCommand(this.requestingPlugin), "logify", List.of());
         commandMap.register(this.requestingPlugin.getName(), wrapper);
         Logger.info("Registered /logify command");
      }
   }

   private void registerNightbreakLogin() {
      SimpleCommandMap commandMap = null;

      try {
         Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
         f.setAccessible(true);
         commandMap = (SimpleCommandMap)f.get(Bukkit.getServer());
      } catch (ReflectiveOperationException var3) {
         this.requestingPlugin.getLogger().warning("Couldn't access CommandMap: " + var3.getMessage());
         return;
      }

      if (commandMap.getCommand("nightbreaklogin") != null) {
         this.requestingPlugin.getLogger().info("/nightbreaklogin is already registered, skipping.");
      } else {
         if (Bukkit.getPluginManager().getPermission("nightbreak.login") == null) {
            Permission perm = new Permission(
               "nightbreak.login", "Lets admins register their account token for in-game plugin content and updates.", PermissionDefault.OP
            );
            Bukkit.getPluginManager().addPermission(perm);
         }

         Command wrapper = AdvancedCommand.toBukkitCommand(
            this.requestingPlugin, new NightbreakLoginCommand(this.requestingPlugin), "nightbreaklogin", List.of("nightbreaklogin")
         );
         commandMap.register(this.requestingPlugin.getName(), wrapper);
         Logger.info("Registered /nightbreaklogin command");
      }
   }

   private void registerNightbreakLogout(NightbreakPluginSpec pluginSpec) {
      SimpleCommandMap commandMap = null;

      try {
         Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
         field.setAccessible(true);
         commandMap = (SimpleCommandMap)field.get(Bukkit.getServer());
      } catch (ReflectiveOperationException var4) {
         this.requestingPlugin.getLogger().warning("Couldn't access CommandMap: " + var4.getMessage());
         return;
      }

      if (commandMap.getCommand("nightbreaklogout") != null) {
         this.requestingPlugin.getLogger().info("/nightbreaklogout is already registered, skipping.");
      } else {
         if (Bukkit.getPluginManager().getPermission("nightbreak.login") == null) {
            Permission permission = new Permission(
               "nightbreak.login", "Lets admins connect or disconnect the shared Nightbreak account token.", PermissionDefault.OP
            );
            Bukkit.getPluginManager().addPermission(permission);
         }

         Command wrapper = AdvancedCommand.toBukkitCommand(
            this.requestingPlugin,
            new NightbreakLogoutCommand(this.requestingPlugin, pluginSpec == null ? NightbreakLogoutMessages::defaults : pluginSpec::resolveLogoutMessages),
            "nightbreaklogout",
            List.of("nightbreaklogout")
         );
         commandMap.register(this.requestingPlugin.getName(), wrapper);
         Logger.info("Registered /nightbreaklogout command");
      }
   }

   private void registerNightbreak() {
      SimpleCommandMap commandMap = null;

      try {
         Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
         f.setAccessible(true);
         commandMap = (SimpleCommandMap)f.get(Bukkit.getServer());
      } catch (ReflectiveOperationException var3) {
         this.requestingPlugin.getLogger().warning("Couldn't access CommandMap: " + var3.getMessage());
         return;
      }

      if (commandMap.getCommand("nightbreak") != null) {
         this.requestingPlugin.getLogger().info("/nightbreak is already registered, skipping.");
      } else {
         if (Bukkit.getPluginManager().getPermission("nightbreak.plugins") == null) {
            Permission perm = new Permission("nightbreak.plugins", "Lets admins browse the plugin catalog.", PermissionDefault.OP);
            Bukkit.getPluginManager().addPermission(perm);
         }

         Command wrapper = AdvancedCommand.toBukkitCommand(this.requestingPlugin, new NightbreakCommand(this.requestingPlugin), "nightbreak", List.of());
         commandMap.register(this.requestingPlugin.getName(), wrapper);
         Logger.info("Registered /nightbreak command");
      }
   }

   public static synchronized void exportSharedAssets(JavaPlugin host) {
      if (host != null) {
         try {
            Path targetPath = host.getDataFolder().toPath().resolve("resource_pack");
            Path checksumFile = targetPath.resolve(".nb_rsp_checksum_v3");
            if (!Files.isDirectory(targetPath)) {
               Files.createDirectories(targetPath);
            }

            String jarChecksum = calculateNbRspChecksum();
            if (jarChecksum == null) {
               Logger.warn("Could not calculate nightbreak_rsp_defaults checksum from MagmaCore jar!");
               return;
            }

            if (Files.exists(checksumFile)) {
               try {
                  String existingChecksum = Files.readString(checksumFile).trim();
                  if (existingChecksum.equals(jarChecksum)) {
                     return;
                  }
               } catch (IOException var5) {
               }
            }

            copyNbRspResourceFolder(targetPath);
            writeStringAtomically(checksumFile, jarChecksum);
            Logger.info("Exported nightbreak_rsp_defaults to " + targetPath + " for plugin " + host.getName() + ".");
         } catch (Exception var6) {
            Logger.warn("Failed to export nightbreak_rsp_defaults for " + host.getName() + ": " + var6.getMessage());
         }
      }
   }

   private static String calculateNbRspChecksum() {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-1");
         URL resourceUrl = MagmaCore.class.getClassLoader().getResource("nightbreak_rsp_defaults");
         if (resourceUrl == null) {
            return null;
         } else {
            if (resourceUrl.getProtocol().equals("jar")) {
               JarURLConnection jarConnection = (JarURLConnection)resourceUrl.openConnection();

               try (JarFile jarFile = jarConnection.getJarFile()) {
                  for (JarEntry entry : jarFile.stream()
                     .filter(entryx -> entryx.getName().startsWith("nightbreak_rsp_defaults/") && !entryx.isDirectory())
                     .sorted(Comparator.comparing(ZipEntry::getName))
                     .toList()) {
                     String relative = entry.getName().substring("nightbreak_rsp_defaults".length() + 1);

                     try (InputStream input = jarFile.getInputStream(entry)) {
                        updateAssetDigest(digest, relative, input);
                     }
                  }
               }
            } else {
               Path resourcePath = Paths.get(resourceUrl.toURI());
               updateAssetDigestFromDirectory(digest, resourcePath);
            }

            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();

            for (byte b : hashBytes) {
               sb.append(String.format("%02x", b));
            }

            return sb.toString();
         }
      } catch (Exception var15) {
         Logger.warn("Error calculating nightbreak_rsp_defaults checksum: " + var15.getMessage());
         return null;
      }
   }

   private static void updateAssetDigestFromDirectory(MessageDigest digest, Path sourcePath) throws IOException {
      try (Stream<Path> paths = Files.walk(sourcePath)) {
         for (Path path : paths.filter(x$0 -> Files.isRegularFile(x$0))
            .sorted(Comparator.comparing(candidate -> sourcePath.relativize(candidate).toString().replace('\\', '/')))
            .toList()) {
            String relative = sourcePath.relativize(path).toString().replace('\\', '/');

            try (InputStream input = Files.newInputStream(path)) {
               updateAssetDigest(digest, relative, input);
            }
         }
      }
   }

   private static void updateAssetDigest(MessageDigest digest, String relativePath, InputStream input) throws IOException {
      digest.update(relativePath.getBytes(StandardCharsets.UTF_8));
      digest.update((byte)0);
      byte[] buffer = new byte[8192];

      int read;
      while ((read = input.read(buffer)) != -1) {
         digest.update(buffer, 0, read);
      }

      digest.update((byte)0);
   }

   private static void copyNbRspResourceFolder(Path targetPath) throws IOException {
      URL resourceUrl = MagmaCore.class.getClassLoader().getResource("nightbreak_rsp_defaults");
      if (resourceUrl == null) {
         Logger.warn("Resource folder not found in MagmaCore jar: nightbreak_rsp_defaults");
      } else {
         Path stagedManagedDirectory = Files.createTempDirectory(targetPath, ".nb-rsp-stage-");
         boolean installed = false;

         try {
            populateStagedSharedAssets(resourceUrl, stagedManagedDirectory);
            replaceManagedAssetDirectory(stagedManagedDirectory, targetPath.resolve("nightbreak_rsp_defaults"));
            installed = true;
         } finally {
            if (!installed) {
               deleteTree(stagedManagedDirectory);
            }
         }
      }
   }

   private static void populateStagedSharedAssets(URL resourceUrl, Path stagedManagedDirectory) throws IOException {
      if (resourceUrl.getProtocol().equals("jar")) {
         JarURLConnection jarConnection = (JarURLConnection)resourceUrl.openConnection();

         try (JarFile jarFile = jarConnection.getJarFile()) {
            for (JarEntry entry : jarFile.stream()
               .filter(entryx -> entryx.getName().startsWith("nightbreak_rsp_defaults/"))
               .sorted(Comparator.comparing(ZipEntry::getName))
               .toList()) {
               String entryName = entry.getName();
               String relativePath = entryName.substring("nightbreak_rsp_defaults".length() + 1);
               if (!relativePath.isEmpty()) {
                  Path targetFile = stagedManagedDirectory.resolve(relativePath);
                  if (entry.isDirectory()) {
                     Files.createDirectories(targetFile);
                  } else {
                     Files.createDirectories(targetFile.getParent());

                     try (InputStream is = jarFile.getInputStream(entry)) {
                        Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
                     }
                  }
               }
            }
         }
      } else {
         try {
            Path sourcePath = Paths.get(resourceUrl.toURI());

            try (Stream<Path> paths = Files.walk(sourcePath)) {
               for (Path sourceFile : paths.sorted().toList()) {
                  Path targetFile = stagedManagedDirectory.resolve(sourcePath.relativize(sourceFile).toString());
                  if (Files.isDirectory(sourceFile)) {
                     Files.createDirectories(targetFile);
                  } else {
                     Files.createDirectories(targetFile.getParent());
                     Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                  }
               }
            }
         } catch (URISyntaxException var19) {
            throw new IOException("Invalid nightbreak_rsp_defaults classpath URI.", var19);
         }
      }
   }

   static String calculateDirectoryAssetChecksum(Path sourcePath) throws Exception {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      updateAssetDigestFromDirectory(digest, sourcePath);
      StringBuilder result = new StringBuilder();

      for (byte value : digest.digest()) {
         result.append(String.format("%02x", value));
      }

      return result.toString();
   }

   static void installSharedAssetDirectory(Path sourcePath, Path resourcePackRoot) throws IOException {
      Files.createDirectories(resourcePackRoot);
      Path stagedManagedDirectory = Files.createTempDirectory(resourcePackRoot, ".nb-rsp-stage-");
      boolean installed = false;

      try {
         try (Stream<Path> paths = Files.walk(sourcePath)) {
            for (Path source : paths.sorted().toList()) {
               Path destination = stagedManagedDirectory.resolve(sourcePath.relativize(source).toString());
               if (Files.isDirectory(source)) {
                  Files.createDirectories(destination);
               } else {
                  Files.createDirectories(destination.getParent());
                  Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
               }
            }
         }

         replaceManagedAssetDirectory(stagedManagedDirectory, resourcePackRoot.resolve("nightbreak_rsp_defaults"));
         installed = true;
      } finally {
         if (!installed) {
            deleteTree(stagedManagedDirectory);
         }
      }
   }

   private static void replaceManagedAssetDirectory(Path stagedDirectory, Path managedDirectory) throws IOException {
      Path backupDirectory = managedDirectory.resolveSibling(".nightbreak_rsp_defaults.backup-" + UUID.randomUUID());
      boolean previousMoved = false;
      boolean replacementInstalled = false;

      try {
         if (Files.exists(managedDirectory)) {
            movePath(managedDirectory, backupDirectory);
            previousMoved = true;
         }

         movePath(stagedDirectory, managedDirectory);
         replacementInstalled = true;
      } catch (IOException var16) {
         if (previousMoved && Files.exists(backupDirectory)) {
            try {
               if (Files.exists(managedDirectory)) {
                  deleteTree(managedDirectory);
               }

               movePath(backupDirectory, managedDirectory);
            } catch (IOException var15) {
               var16.addSuppressed(var15);
            }
         }

         throw var16;
      } finally {
         if (replacementInstalled && previousMoved && Files.exists(backupDirectory)) {
            try {
               deleteTree(backupDirectory);
            } catch (IOException var14) {
               Logger.warn("Installed shared resource-pack assets but could not remove backup " + backupDirectory + ": " + var14.getMessage());
            }
         }
      }
   }

   private static void movePath(Path source, Path destination) throws IOException {
      try {
         Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE);
      } catch (IOException var5) {
         if (!Files.exists(source)) {
            throw var5;
         }

         try {
            Files.move(source, destination);
         } catch (IOException var4) {
            var4.addSuppressed(var5);
            throw var4;
         }
      }
   }

   private static void writeStringAtomically(Path destination, String value) throws IOException {
      Path temporary = Files.createTempFile(destination.getParent(), ".nb-rsp-checksum-", ".tmp");
      boolean moved = false;

      try {
         Files.writeString(temporary, value, StandardCharsets.UTF_8);

         try {
            Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
         } catch (IOException var11) {
            if (!Files.exists(temporary)) {
               throw var11;
            }

            try {
               Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException var10) {
               var10.addSuppressed(var11);
               throw var10;
            }
         }

         moved = true;
      } finally {
         if (!moved) {
            Files.deleteIfExists(temporary);
         }
      }
   }

   private static void deleteTree(Path root) throws IOException {
      if (root != null && Files.exists(root)) {
         try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
               Files.deleteIfExists(path);
            }
         }
      }
   }

   @Generated
   public static MagmaCore getInstance() {
      return instance;
   }

   @Generated
   public JavaPlugin getRequestingPlugin() {
      return this.requestingPlugin;
   }
}
