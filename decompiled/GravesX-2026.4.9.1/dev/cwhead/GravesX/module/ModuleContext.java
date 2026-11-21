package dev.cwhead.GravesX.module;

import com.ranull.graves.Graves;
import dev.cwhead.GravesX.module.util.LibraryImporter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;

public final class ModuleContext {
   private final Graves plugin;
   private final String moduleName;
   private final ClassLoader moduleClassLoader;
   private final File dataFolder;
   private final Logger logger;
   private final LibraryImporter importer;
   private final File configFile;
   private YamlConfiguration config;
   private volatile boolean disabling = false;
   private final List<Listener> listeners = new CopyOnWriteArrayList<>();
   private final List<ModuleContext.ServiceReg> services = new CopyOnWriteArrayList<>();
   private final List<AutoCloseable> closeables = new CopyOnWriteArrayList<>();
   private final List<Runnable> shutdownHooks = new CopyOnWriteArrayList<>();
   private volatile GravesXModuleController controller;

   public ModuleContext(Graves plugin, String moduleName, ClassLoader moduleClassLoader, LibraryImporter importer) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      this.moduleName = Objects.requireNonNull(moduleName, "moduleName");
      this.moduleClassLoader = Objects.requireNonNull(moduleClassLoader, "moduleClassLoader");
      this.importer = importer;
      this.logger = plugin.getLogger();
      this.dataFolder = new File(plugin.getDataFolder(), "modules" + File.separator + moduleName);
      if (!this.dataFolder.exists() && !this.dataFolder.mkdirs()) {
         this.logger.warning("[Modules] Could not create data folder for " + moduleName + " at " + this.dataFolder.getPath());
      }

      this.configFile = new File(this.dataFolder, "config.yml");
   }

   public Graves getPlugin() {
      return this.plugin;
   }

   public String getModuleName() {
      return this.moduleName;
   }

   public File getDataFolder() {
      return this.dataFolder;
   }

   public Logger getLogger() {
      return this.logger;
   }

   public ClassLoader getClassLoader() {
      return this.moduleClassLoader;
   }

   public InputStream getResource(String path) {
      Objects.requireNonNull(path, "path");
      return this.moduleClassLoader.getResourceAsStream(path);
   }

   public Reader getTextResource(String path) {
      InputStream in = this.getResource(path);
      return in == null ? null : new InputStreamReader(in, StandardCharsets.UTF_8);
   }

   public File resolveFile(String relativePath) {
      Objects.requireNonNull(relativePath, "relativePath");
      return new File(this.dataFolder, relativePath);
   }

   public File ensureSubfolder(String name) {
      Objects.requireNonNull(name, "name");
      File f = new File(this.dataFolder, name);
      if (!f.exists() && !f.mkdirs()) {
         this.logger.warning("[Modules] Could not create subfolder '" + name + "' for " + this.moduleName + " at " + f.getPath());
      }

      return f;
   }

   public YamlConfiguration loadYaml(String relativePath) {
      File file = this.resolveFile(relativePath);
      return YamlConfiguration.loadConfiguration(file);
   }

   public void saveYaml(String relativePath, YamlConfiguration yaml) {
      Objects.requireNonNull(yaml, "yaml");
      File file = this.resolveFile(relativePath);

      try {
         yaml.save(file);
      } catch (IOException var5) {
         this.logger.warning("[Modules] Failed to save YAML '" + relativePath + "' for " + this.moduleName + ": " + var5.getMessage());
      }
   }

   public boolean supportsFolia() {
      GravesXModuleController c = this.controller;
      return c != null && c.supportsFolia();
   }

   public void saveDefaultConfig() {
      try {
         Path baseDir = this.configFile.getParentFile().toPath();
         Files.createDirectories(baseDir);
         JarFile jarFile = this.findModuleJar();
         if (jarFile != null) {
            this.extractYamlEntries(jarFile, baseDir);

            try {
               jarFile.close();
            } catch (IOException var8) {
            }
         } else if (!this.configFile.exists()) {
            try (InputStream in = this.moduleClassLoader.getResourceAsStream("config.yml")) {
               if (in != null) {
                  Files.copy(in, this.configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
               }
            }
         }
      } catch (Exception var10) {
         this.logger.warning("[Modules] Failed extracting default YAMLs for " + this.moduleName + ": " + var10.getMessage());
      }

      if (!this.configFile.exists()) {
         try {
            saveString(this.configFile.toPath(), "# Auto-generated config for " + this.moduleName + System.lineSeparator());
         } catch (Exception var6) {
            this.logger.warning("[Modules] Failed to write default config for " + this.moduleName + ": " + var6.getMessage());
         }
      }

      this.reloadConfig();
   }

   private JarFile findModuleJar() {
      try {
         URL marker = this.moduleClassLoader.getResource("module.yml");
         if (marker != null && "jar".equalsIgnoreCase(marker.getProtocol())) {
            JarURLConnection juc = (JarURLConnection)marker.openConnection();
            return juc.getJarFile();
         }
      } catch (Throwable var8) {
      }

      if (this.moduleClassLoader instanceof URLClassLoader urlCl) {
         for (URL u : urlCl.getURLs()) {
            try {
               Path p = Paths.get(u.toURI());
               if (Files.isRegularFile(p) && p.getFileName().toString().toLowerCase().endsWith(".jar")) {
                  return new JarFile(p.toFile());
               }
            } catch (Exception var7) {
            }
         }
      }

      return null;
   }

   private void extractYamlEntries(JarFile jarFile, Path baseDir) {
      Path baseAbs = baseDir.toAbsolutePath().normalize();
      Enumeration<JarEntry> entries = jarFile.entries();

      while (entries.hasMoreElements()) {
         JarEntry e = entries.nextElement();
         if (!e.isDirectory()) {
            String name = e.getName();
            String lower = name.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".yml")) {
               String last = name.substring(name.lastIndexOf(47) + 1);
               if (!"module.yml".equalsIgnoreCase(last)) {
                  try {
                     Path target = baseAbs.resolve(name).normalize();
                     if (!target.startsWith(baseAbs)) {
                        this.logger.warning("[Modules] Skipping suspicious path in JAR: " + name);
                     } else if (!Files.exists(target)) {
                        Path parent = target.getParent();
                        if (parent != null) {
                           Files.createDirectories(parent);
                        }

                        try (InputStream in = jarFile.getInputStream(e)) {
                           Files.copy(in, target);
                        }

                        if (Files.isSymbolicLink(target)) {
                           Files.delete(target);
                           this.logger.warning("[Modules] Skipped symlink file: " + name);
                        }
                     }
                  } catch (Exception var16) {
                     this.logger.warning("[Modules] Failed to write default file " + name + " for " + this.moduleName + ": " + var16.getMessage());
                  }
               }
            }
         }
      }
   }

   public FileConfiguration getConfig() {
      if (this.config == null) {
         this.reloadConfig();
      }

      return this.config;
   }

   public void saveConfig() {
      if (this.config != null) {
         try {
            this.config.save(this.configFile);
         } catch (Exception var2) {
            this.logger.warning("[Modules] Failed to save config for " + this.moduleName + ": " + var2.getMessage());
         }
      }
   }

   public void reloadConfig() {
      YamlConfiguration cfg = YamlConfiguration.loadConfiguration(this.configFile);

      try (InputStream def = this.moduleClassLoader.getResourceAsStream("config.yml")) {
         if (def != null) {
            YamlConfiguration defCfg = YamlConfiguration.loadConfiguration(new InputStreamReader(def, StandardCharsets.UTF_8));
            cfg.setDefaults(defCfg);
            cfg.options().copyDefaults(true);
         }
      } catch (Exception var7) {
      }

      this.config = cfg;
   }

   public void saveResource(String path, boolean replace) {
      Path outPath = this.dataFolder.toPath().resolve(path);
      if (!Files.exists(outPath) || replace) {
         try {
            Path parent = outPath.getParent();
            if (parent != null) {
               Files.createDirectories(parent);
            }

            try (InputStream in = this.moduleClassLoader.getResourceAsStream(path)) {
               if (in != null) {
                  Files.copy(in, outPath, StandardCopyOption.REPLACE_EXISTING);
                  return;
               }

               this.logger.warning("[Modules] Resource not found in module JAR: " + path);
            }
         } catch (Exception var10) {
            this.logger.warning("[Modules] Failed saving resource " + path + " for " + this.moduleName + ": " + var10.getMessage());
         }
      }
   }

   public <T extends Listener> T registerListener(T listener) {
      Objects.requireNonNull(listener, "listener");
      if (this.disabling) {
         return listener;
      } else {
         Bukkit.getPluginManager().registerEvents(listener, this.plugin);
         this.listeners.add(listener);
         return listener;
      }
   }

   public void unregisterListener(Listener listener) {
      if (listener != null) {
         HandlerList.unregisterAll(listener);
         this.listeners.remove(listener);
      }
   }

   private void unregisterAllListeners() {
      for (Listener listener : snapshot(this.listeners)) {
         HandlerList.unregisterAll(listener);
      }

      this.listeners.clear();
   }

   private Runnable guard(Runnable r) {
      return () -> {
         if (!this.disabling) {
            r.run();
         }
      };
   }

   public void runTask(Runnable r) {
      if (!this.disabling) {
         this.plugin.getSchedulerManager().runTask(this.guard(r));
      }
   }

   public void runTaskLater(Runnable r, long delay) {
      if (!this.disabling) {
         this.plugin.getSchedulerManager().runTaskLater(this.guard(r), delay);
      }
   }

   public void runTaskTimer(Runnable r, long delay, long period) {
      if (!this.disabling) {
         this.plugin.getSchedulerManager().runTaskTimer(this.guard(r), delay, period);
      }
   }

   public void runTaskAsync(Runnable r) {
      if (!this.disabling) {
         this.plugin.getSchedulerManager().runTaskAsynchronously(this.guard(r));
      }
   }

   public void runTaskTimerAsync(Runnable r, long delay, long period) {
      if (!this.disabling) {
         this.plugin.getSchedulerManager().runTaskTimerAsynchronously(this.guard(r), delay, period);
      }
   }

   public void executeRegion(Location location, Runnable r) {
      if (location != null && r != null) {
         if (!this.disabling) {
            this.plugin.getSchedulerManager().execute(location, this.guard(r));
         }
      }
   }

   public <T> void registerService(Class<T> service, T provider) {
      this.registerService(service, provider, ServicePriority.Normal);
   }

   public <T> void registerService(Class<T> service, T provider, ServicePriority prio) {
      if (!this.disabling) {
         this.plugin.getServer().getServicesManager().register(service, provider, this.plugin, prio);
         this.services.add(new ModuleContext.ServiceReg(service, provider));
      }
   }

   public <T extends AutoCloseable> T registerCloseable(T closeable) {
      if (this.disabling) {
         return closeable;
      } else {
         this.closeables.add(closeable);
         return closeable;
      }
   }

   public void addShutdownHook(Runnable hook) {
      if (!this.disabling) {
         this.shutdownHooks.add(Objects.requireNonNull(hook, "hook"));
      }
   }

   public void importLibrary(String coordinates) {
      if (!this.disabling) {
         if (this.importer != null) {
            this.importer.importLibrary(this, coordinates);
         } else {
            this.logger.info("[Modules] importLibrary() called for " + this.moduleName + " (no importer configured yet).");
         }
      }
   }

   void _internalPreDisable() {
      this.disabling = true;
   }

   void _internalCleanup() {
      for (Runnable r : snapshot(this.shutdownHooks)) {
         safeRun(r);
      }

      this.shutdownHooks.clear();
      this.unregisterAllListeners();

      for (ModuleContext.ServiceReg reg : snapshot(this.services)) {
         try {
            this.plugin.getServer().getServicesManager().unregister(reg.type, reg.provider);
         } catch (Throwable var5) {
         }
      }

      this.services.clear();

      for (AutoCloseable c : snapshot(this.closeables)) {
         try {
            c.close();
         } catch (Throwable var4) {
         }
      }

      this.closeables.clear();
   }

   void _internalAttachController(GravesXModuleController controller) {
      this.controller = controller;
   }

   public GravesXModuleController getGravesXModules() {
      return this.controller;
   }

   boolean _internalIsDisablingOrDisabled() {
      return this.disabling;
   }

   private static <T> List<T> snapshot(List<T> list) {
      return new ArrayList<>(list);
   }

   private static void safeRun(Runnable r) {
      try {
         r.run();
      } catch (Throwable var2) {
      }
   }

   private static void saveString(Path file, String contents) throws IOException {
      Files.createDirectories(file.getParent());
      Files.writeString(file, contents, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
   }

   private static final class ServiceReg {
      final Class<?> type;
      final Object provider;

      ServiceReg(Class<?> type, Object provider) {
         this.type = type;
         this.provider = provider;
      }
   }
}
