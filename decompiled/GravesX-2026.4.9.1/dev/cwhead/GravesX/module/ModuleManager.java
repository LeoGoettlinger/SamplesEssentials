package dev.cwhead.GravesX.module;

import com.ranull.graves.Graves;
import dev.cwhead.GravesX.module.command.GravesXModuleCommand;
import dev.cwhead.GravesX.module.util.LibraryImporter;
import dev.cwhead.GravesX.module.util.ModuleClassLoader;
import dev.cwhead.GravesX.module.util.ModuleInfo;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class ModuleManager {
   private final Graves plugin;
   private final Logger logger;
   private final File modulesDir;
   private LibraryImporter importer;
   private final Map<String, ModuleManager.LoadedModule> loaded = new LinkedHashMap<>();
   private final Set<String> pending = new LinkedHashSet<>();
   private List<String> topoOrder = List.of();
   private final ModuleCommandRegistrar commandRegistrar;
   private GravesXModuleController.LoadPhase currentPhase = GravesXModuleController.LoadPhase.STARTUP;

   public ModuleManager(Graves plugin) {
      this.plugin = plugin;
      this.logger = plugin.getLogger();
      this.modulesDir = new File(plugin.getDataFolder(), "modules");
      if (!this.modulesDir.exists()) {
         this.modulesDir.mkdirs();
      }

      this.commandRegistrar = new ModuleCommandRegistrar(plugin);
   }

   public void setLibraryImporter(LibraryImporter importer) {
      this.importer = importer;
   }

   public Collection<ModuleManager.LoadedModule> modules() {
      return Collections.unmodifiableCollection(this.loaded.values());
   }

   public Optional<ModuleManager.LoadedModule> get(String name) {
      return Optional.ofNullable(this.loaded.get(name));
   }

   public List<String> order() {
      return this.topoOrder;
   }

   public Set<String> pending() {
      return Collections.unmodifiableSet(this.pending);
   }

   public void loadAll() {
      File[] jars = this.modulesDir.listFiles((d, n) -> n.toLowerCase(Locale.ROOT).endsWith(".jar"));
      if (jars != null && jars.length != 0) {
         Arrays.sort((Object[])jars);

         for (File jar : jars) {
            try (JarFile jf = new JarFile(jar)) {
               JarEntry entry = jf.getJarEntry("module.yml");
               if (entry == null) {
                  this.warn("Skipping " + jar.getName() + " (missing module.yml)");
               } else {
                  ModuleInfo info;
                  try (InputStream in = jf.getInputStream(entry)) {
                     info = ModuleInfo.fromYaml(in);
                  }

                  if (info.name() == null || info.mainClass() == null) {
                     this.warn(info, "Skipping " + jar.getName() + " (missing name/main)");
                  } else if (this.loaded.containsKey(info.name())) {
                     this.warn(info, "Duplicate module name " + info.name() + "; skipping " + jar.getName());
                  } else {
                     this.info(info, "Loading " + info.name() + " version " + info.version());
                     URL url = jar.toURI().toURL();
                     ModuleClassLoader cl = new ModuleClassLoader(url, this.plugin.getClass().getClassLoader());
                     Class<?> main = Class.forName(info.mainClass(), true, cl);
                     if (!GravesXModule.class.isAssignableFrom(main)) {
                        cl.close();
                        this.warn(info, info.name() + " main does not implement Module");
                     } else {
                        GravesXModule instance = (GravesXModule)main.getDeclaredConstructor().newInstance();
                        ModuleContext ctx = new ModuleContext(this.plugin, info.name(), cl, this.importer);
                        ctx.saveDefaultConfig();
                        ModuleManager.LoadedModule lm = new ModuleManager.LoadedModule(info, cl, instance, ctx);
                        ctx._internalAttachController(new ModuleManager.ControllerImpl(lm));
                        this.importDeclaredLibraries(lm);
                        instance.onModuleLoad(ctx);
                        this.loaded.put(info.name(), lm);
                     }
                  }
               }
            } catch (Throwable var19) {
               this.severe("Failed loading " + jar.getName(), var19);
            }
         }

         this.buildTopoOrder();
      } else {
         this.logger.info("[Modules] No module jars found in " + this.modulesDir.getPath());
      }
   }

   private void importDeclaredLibraries(ModuleManager.LoadedModule lm) {
      if (lm != null) {
         List<ModuleInfo.LibraryDef> libs = lm.info.libraries();
         if (libs != null && !libs.isEmpty()) {
            if (this.importer == null) {
               this.warn(lm.info, "Module declares libraries but no LibraryImporter is configured; skipping library import.");
            } else {
               for (ModuleInfo.LibraryDef lib : libs) {
                  if (lib != null) {
                     String coords = lib.coordinates();
                     if (coords != null && !coords.isBlank()) {
                        try {
                           this.importer.importLibrary(lm.context, lib);
                        } catch (Throwable var7) {
                           lm.failed = true;
                           this.severe(lm.info, "Failed importing library " + coords + " for " + lm.info.name(), var7);
                           this.disable(lm.info.name());
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void buildTopoOrder() {
      final Map<String, Set<String>> adj = new LinkedHashMap<>();
      final Map<String, Integer> indeg = new LinkedHashMap<>();

      for (String n : this.loaded.keySet()) {
         adj.put(n, new LinkedHashSet<>());
         indeg.put(n, 0);
      }

      final class EdgeAdder {
         void add(String a, String b) {
            if (ModuleManager.this.loaded.containsKey(a) && ModuleManager.this.loaded.containsKey(b)) {
               if (adj.get(a).add(b)) {
                  indeg.put(b, indeg.get(b) + 1);
               }
            }
         }
      }

      EdgeAdder addEdge = new EdgeAdder();

      for (ModuleManager.LoadedModule lm : this.loaded.values()) {
         String me = lm.info.name();

         for (String dep : lm.info.moduleDepends()) {
            addEdge.add(dep, me);
         }

         for (String soft : lm.info.moduleSoftDepends()) {
            addEdge.add(soft, me);
         }

         for (String before : lm.info.moduleLoadBefore()) {
            addEdge.add(me, before);
         }
      }

      PriorityQueue<String> q = new PriorityQueue<>();

      for (Entry<String, Integer> e : indeg.entrySet()) {
         if (e.getValue() == 0) {
            q.add(e.getKey());
         }
      }

      List<String> order = new ArrayList<>(this.loaded.size());

      while (!q.isEmpty()) {
         String u = q.poll();
         order.add(u);

         for (String v : adj.get(u)) {
            indeg.put(v, indeg.get(v) - 1);
            if (indeg.get(v) == 0) {
               q.add(v);
            }
         }
      }

      if (order.size() != this.loaded.size()) {
         this.warn("Detected a cycle in module dependencies; falling back to partial order.");

         for (String n : this.loaded.keySet()) {
            if (!order.contains(n)) {
               order.add(n);
            }
         }
      }

      this.topoOrder = order;
   }

   public void enableAll(GravesXModuleController.LoadPhase phase) {
      if (phase == null) {
         phase = GravesXModuleController.LoadPhase.STARTUP;
      }

      if (phase.ordinal() > this.currentPhase.ordinal()) {
         this.currentPhase = phase;
      }

      for (String n : this.topoOrder) {
         ModuleManager.LoadedModule lm = this.loaded.get(n);
         if (lm != null && !lm.enabled && this.shouldAttemptInCurrentPhase(lm)) {
            this.attemptEnable(lm);
         }
      }
   }

   public void enableAll() {
      this.enableAll(GravesXModuleController.LoadPhase.COMPLETED);
   }

   public void tryEnablePending() {
      if (!this.pending.isEmpty()) {
         for (String n : this.topoOrder) {
            if (this.pending.contains(n)) {
               ModuleManager.LoadedModule lm = this.loaded.get(n);
               if (lm != null && !lm.enabled && this.shouldAttemptInCurrentPhase(lm)) {
                  this.attemptEnable(lm);
               }
            }
         }
      }
   }

   private boolean shouldAttemptInCurrentPhase(ModuleManager.LoadedModule lm) {
      GravesXModuleController.LoadPhase needed = lm.info.loadPhase();
      if (needed == null) {
         needed = GravesXModuleController.LoadPhase.COMPLETED;
      }

      return needed.ordinal() <= this.currentPhase.ordinal();
   }

   public boolean enable(String name) {
      ModuleManager.LoadedModule lm = this.loaded.get(name);
      return lm == null ? false : this.internalEnable(lm);
   }

   public boolean enable(Class<? extends GravesXModule> moduleClass) {
      ModuleManager.LoadedModule lm = this.findByClass(moduleClass);
      return lm == null ? false : this.internalEnable(lm);
   }

   public boolean enable(GravesXModule module) {
      if (module == null) {
         return false;
      } else {
         ModuleManager.LoadedModule lm = this.findByClass((Class<? extends GravesXModule>)module.getClass());
         return lm == null ? false : this.internalEnable(lm);
      }
   }

   private boolean internalEnable(ModuleManager.LoadedModule lm) {
      String name = lm.info.name();
      if (lm.failed) {
         this.info(lm.info, "Not enabling " + name + " because it previously failed to enable and was disabled.");
         return false;
      } else if (!lm.enabled && !this.shouldAttemptInCurrentPhase(lm)) {
         this.pending.add(lm.info.name());
         this.info(
            lm.info,
            "Pending "
               + lm.info.name()
               + " (waiting for load phase: "
               + (lm.info.loadPhase() != null ? lm.info.loadPhase() : GravesXModuleController.LoadPhase.COMPLETED)
               + ", current phase: "
               + this.currentPhase
               + ")"
         );
         return false;
      } else {
         return lm.enabled || this.attemptEnable(lm);
      }
   }

   public boolean disable(String name) {
      ModuleManager.LoadedModule lm = this.loaded.get(name);
      return lm == null ? false : this.internalDisable(lm);
   }

   public boolean disable(Class<? extends GravesXModule> moduleClass) {
      ModuleManager.LoadedModule lm = this.findByClass(moduleClass);
      return lm == null ? false : this.internalDisable(lm);
   }

   public boolean disable(GravesXModule module) {
      if (module == null) {
         return false;
      } else {
         ModuleManager.LoadedModule lm = this.findByClass((Class<? extends GravesXModule>)module.getClass());
         return lm == null ? false : this.internalDisable(lm);
      }
   }

   private boolean internalDisable(ModuleManager.LoadedModule lm) {
      String name = lm.info.name();
      this.info(lm.info, "Disabling Module " + name);

      try {
         lm.context._internalPreDisable();
      } catch (Throwable var8) {
      }

      try {
         lm.instance.onModuleDisable(lm.context);
      } catch (Throwable var7) {
         this.severe(lm.info, "Error in onModuleDisable for " + name, var7);
      }

      try {
         this.commandRegistrar.unregisterFor(lm);
      } catch (Throwable var6) {
      }

      try {
         lm.context._internalCleanup();
      } catch (Throwable var5) {
      }

      try {
         lm.cl.close();
      } catch (Throwable var4) {
      }

      lm.enabled = false;
      this.pending.remove(name);
      return true;
   }

   public void disableAll() {
      ListIterator<String> it = new ArrayList<>(this.topoOrder).listIterator(this.topoOrder.size());

      while (it.hasPrevious()) {
         this.disable(it.previous());
      }

      this.topoOrder = List.of();
      this.pending.clear();
      this.loaded.clear();
      this.currentPhase = GravesXModuleController.LoadPhase.STARTUP;
   }

   public List<ModuleInfo> detectModules() {
      List<ModuleInfo> out = new ArrayList<>();
      File[] jars = this.modulesDir.listFiles((d, n) -> n.toLowerCase(Locale.ROOT).endsWith(".jar"));
      if (jars != null && jars.length != 0) {
         Arrays.sort((Object[])jars);

         for (File jar : jars) {
            try (JarFile jf = new JarFile(jar)) {
               JarEntry entry = jf.getJarEntry("module.yml");
               if (entry != null) {
                  try (InputStream in = jf.getInputStream(entry)) {
                     ModuleInfo mi = ModuleInfo.fromYaml(in);
                     if (mi.name() != null) {
                        out.add(mi);
                     }
                  }
               }
            } catch (Throwable var16) {
            }
         }

         return out;
      } else {
         return out;
      }
   }

   private boolean attemptEnable(ModuleManager.LoadedModule lm) {
      if (lm.failed) {
         this.infoOnce(lm, ModuleManager.InfoFlag.FAILED_PREVIOUSLY, lm.info.name() + " will not be enabled because it previously failed.");
         return false;
      } else if (!this.shouldAttemptInCurrentPhase(lm)) {
         this.pending.add(lm.info.name());
         this.infoOnce(
            lm,
            ModuleManager.InfoFlag.PENDING_PHASE,
            "Pending "
               + lm.info.name()
               + " (waiting for load phase: "
               + (lm.info.loadPhase() != null ? lm.info.loadPhase() : GravesXModuleController.LoadPhase.COMPLETED)
               + ", current phase: "
               + this.currentPhase
               + ")"
         );
         return false;
      } else {
         List<String> missingPlugins = this.missingRequiredPlugins(lm.info);
         if (!missingPlugins.isEmpty()) {
            this.warn(lm.info, lm.info.name() + ": required plugin(s) not installed: " + String.join(", ", missingPlugins));
            lm.failed = true;
            this.disable(lm.info.name());
            return false;
         } else {
            List<String> inactivePlugins = this.inactiveRequiredPlugins(lm.info);
            if (!inactivePlugins.isEmpty()) {
               this.pending.add(lm.info.name());
               this.infoOnce(
                  lm,
                  ModuleManager.InfoFlag.PENDING_PLUGINS,
                  "Pending " + lm.info.name() + " (waiting for required plugins to enable: " + String.join(", ", inactivePlugins) + ")"
               );
               this.warnPostworldPendingRecommendation(lm.info, "plugins", inactivePlugins);
               return false;
            } else {
               List<String> missingMods = new ArrayList<>();

               for (String m : lm.info.moduleDepends()) {
                  ModuleManager.LoadedModule dep = this.loaded.get(m);
                  if (dep == null || !dep.enabled) {
                     missingMods.add(m);
                  }
               }

               if (!missingMods.isEmpty()) {
                  this.pending.add(lm.info.name());
                  this.infoOnce(
                     lm, ModuleManager.InfoFlag.PENDING_MODULES, "Pending " + lm.info.name() + " (waiting for modules: " + String.join(", ", missingMods) + ")"
                  );
                  this.warnPostworldPendingRecommendation(lm.info, "modules", missingMods);
                  return false;
               } else if (this.plugin.getVersionManager().isFolia() && !lm.context.supportsFolia()) {
                  lm.failed = true;
                  this.warn(
                     lm.info, "GravesX Module " + lm.info.name() + " is not supported on Folia. Make sure \"supportsFolia\" is set to true in module.yml."
                  );
                  this.disable(lm.info.name());
                  return false;
               } else {
                  try {
                     lm.instance.onModuleEnable(lm.context);
                     lm.enabled = true;
                     this.pending.remove(lm.info.name());

                     try {
                        this.commandRegistrar.registerFor(lm);
                     } catch (Throwable var8) {
                        lm.failed = true;
                        this.severe(lm.info, "Command registration failed for " + lm.info.name(), var8);
                        this.disable(lm.info.name());
                        return false;
                     }

                     this.infoOnce(lm, ModuleManager.InfoFlag.ENABLED, "Enabled Module " + lm.info.name());
                     return true;
                  } catch (Throwable var9) {
                     lm.failed = true;
                     this.severe(lm.info, "Failed enabling " + lm.info.name() + ". Disabling Module.", var9);
                     this.disable(lm.info.name());
                     return false;
                  }
               }
            }
         }
      }
   }

   private void warnPostworldPendingRecommendation(ModuleInfo info, String waitingOnWhat, List<String> missing) {
      if (info != null) {
         if (info.loadPhase() == GravesXModuleController.LoadPhase.POSTWORLD) {
            if (missing != null && !missing.isEmpty()) {
               this.warn(
                  info,
                  info.name()
                     + " is configured to load at POSTWORLD but is waiting for "
                     + waitingOnWhat
                     + ": "
                     + String.join(", ", missing)
                     + ". Consider setting load to COMPLETED in module.yml for "
                     + info.name()
                     + " to avoid load-order delays."
               );
            }
         }
      }
   }

   public Set<String> discoverExternalPluginHints() {
      Set<String> names = new LinkedHashSet<>();
      File[] jars = this.modulesDir.listFiles((d, n) -> n.toLowerCase(Locale.ROOT).endsWith(".jar"));
      if (jars == null) {
         return names;
      } else {
         Arrays.sort((Object[])jars);

         for (File jar : jars) {
            try (JarFile jf = new JarFile(jar)) {
               JarEntry entry = jf.getJarEntry("module.yml");
               if (entry != null) {
                  try (InputStream in = jf.getInputStream(entry)) {
                     ModuleInfo mi = ModuleInfo.fromYaml(in);
                     names.addAll(mi.pluginDepends());
                     names.addAll(mi.pluginSoftDepends());
                  }
               }
            } catch (Throwable var16) {
            }
         }

         return names;
      }
   }

   public boolean shouldDeferLoadOnExternalPlugins() {
      Set<String> hints = this.discoverExternalPluginHints();
      if (hints.isEmpty()) {
         return false;
      } else {
         for (String name : hints) {
            Plugin p = Bukkit.getPluginManager().getPlugin(name);
            if (p != null) {
               return true;
            }
         }

         return false;
      }
   }

   private List<String> missingRequiredPlugins(ModuleInfo info) {
      List<String> missing = new ArrayList<>();

      for (String req : info.pluginDepends()) {
         Plugin p = Bukkit.getPluginManager().getPlugin(req);
         if (p == null) {
            missing.add(req);
         }
      }

      return missing;
   }

   private List<String> inactiveRequiredPlugins(ModuleInfo info) {
      List<String> inactive = new ArrayList<>();

      for (String req : info.pluginDepends()) {
         Plugin p = Bukkit.getPluginManager().getPlugin(req);
         if (p != null && !p.isEnabled()) {
            inactive.add(req);
         }
      }

      return inactive;
   }

   private ModuleManager.LoadedModule findByClass(Class<? extends GravesXModule> moduleClass) {
      if (moduleClass == null) {
         return null;
      } else {
         for (ModuleManager.LoadedModule lm : this.loaded.values()) {
            if (lm.instance != null && lm.instance.getClass().equals(moduleClass)) {
               return lm;
            }
         }

         return null;
      }
   }

   private ModuleManager.LoadedModule findByKey(String key) {
      if (key != null && !key.isEmpty()) {
         String k = norm(key);
         ModuleManager.LoadedModule byName = this.loaded.get(key);
         if (byName != null) {
            return byName;
         } else {
            for (ModuleManager.LoadedModule lm : this.loaded.values()) {
               if (norm(lm.info.name()).equals(k)) {
                  return lm;
               }

               String simple = lm.instance.getClass().getSimpleName();
               String fqcn = lm.instance.getClass().getName();
               if (norm(simple).equals(k) || norm(fqcn).equals(k)) {
                  return lm;
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   void registerCommand(ModuleManager.LoadedModule owner, String label, GravesXModuleCommand command) {
      this.commandRegistrar.registerDynamicCommand(owner, label, command);
   }

   void registerCommand(ModuleManager.LoadedModule owner, String label, Class<? extends GravesXModuleCommand> commandClass) {
      this.commandRegistrar.registerDynamicCommand(owner, label, commandClass);
   }

   private void infoOnce(ModuleManager.LoadedModule lm, ModuleManager.InfoFlag flag, String message) {
      if (lm.infoFlags.add(flag)) {
         this.info(lm.info, message);
      }
   }

   private static String norm(String s) {
      return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
   }

   private void info(String m) {
      this.logger.info("[Modules] " + m);
   }

   private void info(ModuleInfo mi, String m) {
      this.logger.info("[" + mi.name() + "] " + m);
   }

   private void warn(String m) {
      this.logger.warning("[Modules] " + m);
   }

   private void warn(ModuleInfo mi, String m) {
      this.logger.warning("[" + mi.name() + "] " + m);
   }

   private void severe(String m, Throwable t) {
      this.logger.log(Level.SEVERE, "[Modules] " + m, t);
   }

   private void severe(ModuleInfo mi, String m, Throwable t) {
      this.logger.log(Level.SEVERE, "[" + mi.name() + "] " + m, t);
   }

   private void debug(String m, int num) {
      this.plugin.debugMessage("[Modules] " + m, num);
   }

   private void debug(ModuleInfo mi, String m, int num) {
      this.logger.log(Level.SEVERE, "[" + mi.name() + "] " + m, num);
   }

   private final class ControllerImpl implements GravesXModuleController {
      private final ModuleManager.LoadedModule self;

      ControllerImpl(ModuleManager.LoadedModule self) {
         this.self = Objects.requireNonNull(self, "self");
      }

      @Override
      public boolean isEnabled() {
         return this.self.enabled;
      }

      @Override
      public boolean isEnabled(String moduleKey) {
         ModuleManager.LoadedModule lm = ModuleManager.this.findByKey(moduleKey);
         return lm != null && lm.enabled;
      }

      @Override
      public void disableModule() {
         ModuleManager.this.disable(this.self.info.name());
      }

      @Override
      public boolean disableModule(String moduleKey) {
         ModuleManager.LoadedModule lm = ModuleManager.this.findByKey(moduleKey);
         return lm != null && ModuleManager.this.disable(lm.info.name());
      }

      @Override
      public void enableModule() {
         ModuleManager.this.enable(this.self.info.name());
      }

      @Override
      public boolean enableModule(String moduleKey) {
         ModuleManager.LoadedModule lm = ModuleManager.this.findByKey(moduleKey);
         return lm != null && ModuleManager.this.enable(lm.info.name());
      }

      @Override
      public GravesXModuleDescriptor getModule(String moduleKey) {
         ModuleManager.LoadedModule lm = ModuleManager.this.findByKey(moduleKey);
         return lm != null ? new ModuleManager.DescriptorImpl(lm) : null;
      }

      @Override
      public GravesXModuleDescriptor getThisModule() {
         return new ModuleManager.DescriptorImpl(this.self);
      }

      @Override
      public Collection<GravesXModuleDescriptor> listModules() {
         return ModuleManager.this.loaded.values().stream().map(ModuleManager.DescriptorImpl::new).collect(Collectors.toUnmodifiableList());
      }

      @Override
      public boolean isEnabled(GravesXModule module) {
         if (module == null) {
            return false;
         } else {
            ModuleManager.LoadedModule lm = ModuleManager.this.findByClass((Class<? extends GravesXModule>)module.getClass());
            return lm != null && lm.enabled;
         }
      }

      @Override
      public boolean isEnabled(Class<? extends GravesXModule> moduleClass) {
         if (moduleClass == null) {
            return false;
         } else {
            ModuleManager.LoadedModule lm = ModuleManager.this.findByClass(moduleClass);
            return lm != null && lm.enabled;
         }
      }

      @Override
      public boolean disableModule(GravesXModule module) {
         return module == null ? false : ModuleManager.this.disable(module);
      }

      @Override
      public boolean disableModule(Class<? extends GravesXModule> moduleClass) {
         return moduleClass == null ? false : ModuleManager.this.disable(moduleClass);
      }

      @Override
      public boolean enableModule(GravesXModule module) {
         return module == null ? false : ModuleManager.this.enable(module);
      }

      @Override
      public boolean enableModule(Class<? extends GravesXModule> moduleClass) {
         return moduleClass == null ? false : ModuleManager.this.enable(moduleClass);
      }

      @Override
      public GravesXModuleDescriptor getModule(GravesXModule module) {
         if (module == null) {
            return null;
         } else {
            ModuleManager.LoadedModule lm = ModuleManager.this.findByClass((Class<? extends GravesXModule>)module.getClass());
            return lm != null ? new ModuleManager.DescriptorImpl(lm) : null;
         }
      }

      @Override
      public GravesXModuleDescriptor getModule(Class<? extends GravesXModule> moduleClass) {
         if (moduleClass == null) {
            return null;
         } else {
            ModuleManager.LoadedModule lm = ModuleManager.this.findByClass(moduleClass);
            return lm != null ? new ModuleManager.DescriptorImpl(lm) : null;
         }
      }

      @Override
      public void registerCommand(String label, GravesXModuleCommand command) {
         if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("label must not be null/blank");
         } else if (command == null) {
            throw new IllegalArgumentException("command must not be null");
         } else {
            ModuleManager.this.registerCommand(this.self, label, command);
         }
      }

      @Override
      public void registerCommand(String label, Class<? extends GravesXModuleCommand> commandClass) {
         if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("label must not be null/blank");
         } else if (commandClass == null) {
            throw new IllegalArgumentException("commandClass must not be null");
         } else {
            ModuleManager.this.registerCommand(this.self, label, commandClass);
         }
      }
   }

   private static final class DescriptorImpl implements GravesXModuleDescriptor {
      private final ModuleManager.LoadedModule lm;

      DescriptorImpl(ModuleManager.LoadedModule lm) {
         this.lm = Objects.requireNonNull(lm, "lm");
      }

      @Override
      public String getName() {
         return this.lm.info.name();
      }

      @Override
      public String getVersion() {
         return this.lm.info.version();
      }

      @Override
      public String getDescription() {
         return this.lm.info.description();
      }

      @Override
      public String getWebsite() {
         return this.lm.info.website();
      }

      @Override
      public List<String> getAuthors() {
         return List.copyOf(this.lm.info.authors());
      }

      @Override
      public String getMainClass() {
         return this.lm.info.mainClass();
      }

      @Override
      public List<String> getModuleDepends() {
         return List.copyOf(this.lm.info.moduleDepends());
      }

      @Override
      public List<String> getModuleSoftDepends() {
         return List.copyOf(this.lm.info.moduleSoftDepends());
      }

      @Override
      public List<String> getModuleLoadBefore() {
         return List.copyOf(this.lm.info.moduleLoadBefore());
      }

      @Override
      public List<String> getPluginDepends() {
         return List.copyOf(this.lm.info.pluginDepends());
      }

      @Override
      public List<String> getPluginSoftDepends() {
         return List.copyOf(this.lm.info.pluginSoftDepends());
      }

      @Override
      public GravesXModuleController.LoadPhase getLoadPhase() {
         GravesXModuleController.LoadPhase phase = this.lm.info.loadPhase();
         return phase != null ? phase : GravesXModuleController.LoadPhase.COMPLETED;
      }

      @Override
      public List<ModuleInfo.LibraryDef> getLibraries() {
         return List.copyOf(this.lm.info.libraries());
      }

      @Override
      public boolean isEnabled() {
         return this.lm.enabled;
      }

      @Override
      public boolean supportsFolia() {
         return this.lm.info.supportsFolia();
      }
   }

   public static enum InfoFlag {
      FAILED_PREVIOUSLY,
      PENDING_PHASE,
      PENDING_PLUGINS,
      PENDING_MODULES,
      ENABLED;
   }

   public static final class LoadedModule {
      public final ModuleInfo info;
      public final ModuleClassLoader cl;
      public final GravesXModule instance;
      public final ModuleContext context;
      public boolean enabled;
      public boolean failed;
      public EnumSet<ModuleManager.InfoFlag> infoFlags = EnumSet.noneOf(ModuleManager.InfoFlag.class);

      LoadedModule(ModuleInfo info, ModuleClassLoader cl, GravesXModule instance, ModuleContext ctx) {
         this.info = info;
         this.cl = cl;
         this.instance = instance;
         this.context = ctx;
      }
   }
}
