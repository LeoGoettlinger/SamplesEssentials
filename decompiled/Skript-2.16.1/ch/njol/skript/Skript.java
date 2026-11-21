package ch.njol.skript;

import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.bstats.bukkit.Metrics;
import ch.njol.skript.bukkitutil.BurgerHelper;
import ch.njol.skript.classes.data.BukkitClasses;
import ch.njol.skript.classes.data.BukkitEventValues;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.classes.data.DefaultConverters;
import ch.njol.skript.classes.data.DefaultFunctions;
import ch.njol.skript.classes.data.DefaultOperations;
import ch.njol.skript.classes.data.JavaClasses;
import ch.njol.skript.classes.data.SkriptClasses;
import ch.njol.skript.command.Commands;
import ch.njol.skript.doc.Documentation;
import ch.njol.skript.events.EvtSkript;
import ch.njol.skript.expressions.arithmetic.ExprArithmetic;
import ch.njol.skript.hooks.Hook;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.log.BukkitLoggerFilter;
import ch.njol.skript.log.CountingLogHandler;
import ch.njol.skript.log.ErrorDescLogHandler;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.LogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.TestingLogHandler;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.registrations.Feature;
import ch.njol.skript.test.runner.EffObjectives;
import ch.njol.skript.test.runner.SkriptAsyncJUnitTest;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.test.runner.SkriptTestEvent;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.skript.test.runner.TestTracker;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.update.ReleaseManifest;
import ch.njol.skript.update.ReleaseStatus;
import ch.njol.skript.update.UpdateManifest;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.EmptyStacktraceException;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Version;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Closeable;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.iterator.CheckedIterator;
import ch.njol.util.coll.iterator.EnumerationIterable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.invoke.StringConcatFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.server.ServerLoadEvent.LoadType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.junit.After;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.bukkit.BukkitModule;
import org.skriptlang.skript.bukkit.SkriptMetrics;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.log.runtime.BukkitRuntimeErrorConsumer;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.common.CommonModule;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.experiment.ExperimentRegistry;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyRegistry;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.lang.structure.StructureInfo;
import org.skriptlang.skript.log.runtime.RuntimeErrorManager;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.ClassLoader;

public final class Skript extends JavaPlugin implements Listener {
   private int pluginBridge;
   @Nullable
   private static Skript instance = null;
   @UnknownNullability
   private static org.skriptlang.skript.Skript skript = null;
   @UnknownNullability
   private static org.skriptlang.skript.Skript unmodifiableSkript = null;
   private static boolean disabled = false;
   private static boolean partDisabled = false;
   @Nullable
   private SkriptUpdater updater;
   private static Version minecraftVersion = new Version(666);
   private static Version UNKNOWN_VERSION = new Version(666);
   private static ServerPlatform serverPlatform = ServerPlatform.BUKKIT_UNKNOWN;
   @Nullable
   private static Version version = null;
   @Deprecated(
      since = "2.9.0",
      forRemoval = true
   )
   @UnknownNullability
   private static ExperimentRegistry experimentRegistry;
   public static final Message m_invalid_reload = new Message("skript.invalid reload");
   public static final Message m_finished_loading = new Message("skript.finished loading");
   public static final Message m_no_errors = new Message("skript.no errors");
   public static final Message m_no_scripts = new Message("skript.no scripts");
   private static final PluralizingArgsMessage m_scripts_loaded = new PluralizingArgsMessage("skript.scripts loaded");
   private static final Message WARNING_MESSAGE = new Message("skript.warning message");
   private static final Message RESTART_MESSAGE = new Message("skript.restart message");
   private static final Set<Class<? extends Hook<?>>> disabledHookRegistrations = new HashSet<>();
   private static boolean finishedLoadingHooks = false;
   private File scriptsFolder;
   @Nullable
   static Metrics metrics;
   private static final Collection<Closeable> closeOnDisable = Collections.synchronizedCollection(new ArrayList<>());
   private static final boolean IS_STOPPING_EXISTS = methodExists(Server.class, "isStopping");
   @Nullable
   private static Method IS_RUNNING;
   @Nullable
   private static Object MC_SERVER;
   public static final String SCRIPTSFOLDER = "scripts";
   public static final double EPSILON = 1.0E-10;
   public static final double EPSILON_MULT = 1.00001;
   public static final int MAXBLOCKID = 255;
   public static final int MAXDATAVALUE = 65535;
   public static final UncaughtExceptionHandler UEH;
   private static boolean acceptRegistrations;
   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   private static final Set<SkriptAddon> addons;
   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   @Nullable
   private static SkriptAddon addon;
   private static final String EXCEPTION_PREFIX = "#!#! ";
   private static Map<String, PluginDescriptionFile> pluginPackages;
   private static boolean checkedPlugins;
   private static boolean tainted;
   private static boolean errored;
   private static final Message SKRIPT_PREFIX_MESSAGE;

   public static Skript getInstance() {
      if (instance == null) {
         throw new IllegalStateException();
      } else {
         return instance;
      }
   }

   public static org.skriptlang.skript.Skript instance() {
      if (unmodifiableSkript == null) {
         throw new SkriptAPIException("Skript is still initializing");
      } else {
         return unmodifiableSkript;
      }
   }

   public Skript() throws IllegalStateException {
      if (instance != null) {
         throw new IllegalStateException("Cannot create multiple instances of Skript!");
      } else {
         instance = this;
      }
   }

   public static void updateMinecraftVersion() {
      String bukkitV = Bukkit.getBukkitVersion();
      Matcher m = Pattern.compile("\\d+\\.\\d+(\\.\\d+)?").matcher(bukkitV);
      if (!m.find()) {
         minecraftVersion = new Version(666, 0, 0);
      } else {
         minecraftVersion = new Version(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(m.group()));
      }
   }

   public static Version getVersion() {
      Version v = version;
      if (v == null) {
         throw new IllegalStateException();
      } else {
         return v;
      }
   }

   public static String getWarningMessage() {
      return WARNING_MESSAGE.getValueOrDefault(
         "It appears that /reload or another plugin reloaded Skript. This is not supported behaviour and may cause issues."
      );
   }

   public static String getRestartMessage() {
      return RESTART_MESSAGE.getValueOrDefault("Please consider restarting the server instead.");
   }

   public static ServerPlatform getServerPlatform() {
      if (classExists("net.glowstone.GlowServer")) {
         return ServerPlatform.BUKKIT_GLOWSTONE;
      } else if (classExists("co.aikar.timings.Timings")) {
         return ServerPlatform.BUKKIT_PAPER;
      } else if (classExists("org.spigotmc.SpigotConfig")) {
         return ServerPlatform.BUKKIT_SPIGOT;
      } else {
         return !classExists("org.bukkit.craftbukkit.CraftServer") && !classExists("org.bukkit.craftbukkit.Main")
            ? ServerPlatform.BUKKIT_UNKNOWN
            : ServerPlatform.BUKKIT_CRAFTBUKKIT;
      }
   }

   private static boolean checkServerPlatform() {
      String bukkitV = Bukkit.getBukkitVersion();
      Matcher m = Pattern.compile("\\d+\\.\\d+(\\.\\d+)?").matcher(bukkitV);
      if (!m.find()) {
         error(
            "The Bukkit version '"
               + bukkitV
               + "' does not contain a version number which is required for Skript to enable or disable certain features. Skript will still work, but you might get random errors if you use features that are not available in your version of Bukkit."
         );
         minecraftVersion = new Version(666, 0, 0);
      } else {
         minecraftVersion = new Version(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(m.group()));
      }

      debug("Loading for Minecraft " + minecraftVersion);
      if (!isRunningMinecraft(1, 9)) {
         error("This version of Skript does not work with Minecraft " + minecraftVersion + " and requires Minecraft 1.9.4+");
         error("You probably want Skript 2.2 or 2.1 (Google to find where to get them)");
         error("Note that those versions are, of course, completely unsupported!");
         return false;
      } else {
         serverPlatform = getServerPlatform();
         debug("Server platform: " + serverPlatform);
         if (!serverPlatform.works) {
            error("It seems that this server platform (" + serverPlatform.name + ") does not work with Skript.");
            if (!SkriptConfig.allowUnsafePlatforms.value()) {
               error("To prevent potentially unsafe behaviour, Skript has been disabled.");
               error("You may re-enable it by adding a configuration option 'allow unsafe platforms: true'");
               error("Note that it is unlikely that Skript works correctly even if you do so.");
               error("A better idea would be to install Paper or Spigot in place of your current server.");
               return false;
            }

            error("However, you have chosen to ignore this. Skript will probably still not work.");
         } else if (!serverPlatform.supported) {
            warning("This server platform (" + serverPlatform.name + ") is not supported by Skript.");
            warning("It will still probably work, but if it does not, you are on your own.");
            warning("Skript officially supports Paper and Spigot.");
         }

         return true;
      }
   }

   public static boolean isHookEnabled(Class<? extends Hook<?>> hook) {
      return !disabledHookRegistrations.contains(hook);
   }

   public static boolean isFinishedLoadingHooks() {
      return finishedLoadingHooks;
   }

   @SafeVarargs
   public static void disableHookRegistration(Class<? extends Hook<?>>... hooks) {
      if (finishedLoadingHooks) {
         throw new SkriptAPIException("Disabling hooks is not possible after Skript has been enabled!");
      } else {
         Collections.addAll(disabledHookRegistrations, hooks);
      }
   }

   public static ExperimentRegistry experiments() {
      return experimentRegistry;
   }

   public File getScriptsFolder() {
      if (!this.scriptsFolder.isDirectory()) {
         this.scriptsFolder.mkdirs();
      }

      return this.scriptsFolder;
   }

   public static RuntimeErrorManager getRuntimeErrorManager() {
      return RuntimeErrorManager.getInstance();
   }

   public void onEnable() {
      this.pluginBridge = this.preparePluginBridge();
      Bukkit.getPluginManager().registerEvents(this, this);
      if (disabled) {
         error(m_invalid_reload.toString());
         this.setEnabled(false);
      } else {
         this.handleJvmArguments();
         version = new Version(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.getDescription().getVersion()));

         try {
            this.updater = new SkriptUpdater();
         } catch (Exception var35) {
            exception(var35, "Update checker could not be initialized.");
         }

         if (!this.getDataFolder().isDirectory()) {
            this.getDataFolder().mkdirs();
         }

         this.scriptsFolder = new File(this.getDataFolder(), "scripts");
         File config = new File(this.getDataFolder(), "config.sk");
         File features = new File(this.getDataFolder(), "features.sk");
         File lang = new File(this.getDataFolder(), "lang");
         File aliasesFolder = new File(this.getDataFolder(), "aliases");
         if (!this.scriptsFolder.isDirectory() || !config.exists() || !features.exists() || !lang.exists() || !aliasesFolder.exists()) {
            ZipFile f = null;

            try {
               boolean populateExamples = false;
               if (!this.scriptsFolder.isDirectory()) {
                  if (!this.scriptsFolder.mkdirs()) {
                     throw new IOException("Could not create the directory " + this.scriptsFolder);
                  }

                  populateExamples = true;
               }

               boolean populateLanguageFiles = false;
               if (!lang.isDirectory()) {
                  if (!lang.mkdirs()) {
                     throw new IOException("Could not create the directory " + lang);
                  }

                  populateLanguageFiles = true;
               }

               if (!aliasesFolder.isDirectory() && !aliasesFolder.mkdirs()) {
                  throw new IOException("Could not create the directory " + aliasesFolder);
               }

               f = new ZipFile(this.getFile());

               for (ZipEntry e : new EnumerationIterable<>(f.entries())) {
                  if (!e.isDirectory()) {
                     File saveTo = null;
                     if (populateExamples && e.getName().startsWith("scripts/")) {
                        String fileName = e.getName().substring(e.getName().indexOf("/") + 1);
                        if (!fileName.startsWith("-")) {
                           fileName = "-" + fileName;
                        }

                        saveTo = new File(this.scriptsFolder, fileName);
                     } else if (populateLanguageFiles && e.getName().startsWith("lang/") && !e.getName().endsWith("default.lang")) {
                        String fileName = e.getName().substring(e.getName().lastIndexOf("/") + 1);
                        saveTo = new File(lang, fileName);
                     } else if (e.getName().equals("config.sk")) {
                        if (!config.exists()) {
                           saveTo = config;
                        }
                     } else if (e.getName().startsWith("features.sk") && !features.exists()) {
                        saveTo = features;
                     }

                     if (saveTo != null) {
                        InputStream in = f.getInputStream(e);

                        try {
                           assert in != null;

                           FileUtils.save(in, saveTo);
                        } finally {
                           in.close();
                        }
                     }
                  }
               }

               info("Successfully generated the config and the example scripts.");
            } catch (ZipException var36) {
            } catch (IOException var37) {
               error("Error generating the default files: " + ExceptionUtils.toString(var37));
            } finally {
               if (f != null) {
                  try {
                     f.close();
                  } catch (IOException var32) {
                  }
               }
            }
         }

         skript = org.skriptlang.skript.Skript.of(this.getClass(), this.getName());
         unmodifiableSkript = new ModernSkriptBridge.SpecialUnmodifiableSkript(skript);
         skript.localizer().setSourceDirectories("lang", this.getDataFolder().getAbsolutePath() + File.separatorChar + "lang");
         getAddonInstance();
         experimentRegistry = new ExperimentRegistry(this);
         Feature.registerAll(getAddonInstance(), experimentRegistry);
         skript.storeRegistry(PropertyRegistry.class, new PropertyRegistry(this));
         Property.registerDefaultProperties();
         final EventValueRegistry eventValueRegistry = EventValueRegistry.empty(this);
         skript.storeRegistry(EventValueRegistry.class, eventValueRegistry);
         EventValues.setEventValueRegistry(eventValueRegistry);
         new JavaClasses();
         new SkriptClasses();
         new BukkitClasses();
         SkriptConfig.load();
         if (!checkServerPlatform()) {
            disabled = true;
            this.setEnabled(false);
         } else {
            if (methodExists(Server.class, "getPauseWhenEmptyTime")) {
               int pauseThreshold = this.getServer().getPauseWhenEmptyTime();
               if (pauseThreshold > -1) {
                  warning("Minecraft server pausing is enabled!");
                  warning("Scripts that interact with the world or entities may not work as intended when the server is paused and may crash your server.");
                  warning("Consider setting 'pause-when-empty-seconds' to -1 in server.properties to make sure you don't encounter any issues.");
               }
            }

            SkriptConfig.eventRegistry().register(SkriptConfig.ReloadEvent.class, RuntimeErrorManager::refresh);
            RuntimeErrorManager.refresh();
            getRuntimeErrorManager().addConsumer(new BukkitRuntimeErrorConsumer());
            if (TestMode.VERBOSITY != null) {
               SkriptLogger.setVerbosity(Verbosity.valueOf(TestMode.VERBOSITY));
            }

            if (this.updater != null) {
               CommandSender console = Bukkit.getConsoleSender();

               assert console != null;

               assert this.updater != null;

               this.updater.updateCheck(console);
            }

            PluginCommand skriptCommand = this.getCommand("skript");

            assert skriptCommand != null;

            skriptCommand.setExecutor(new SkriptCommand());
            skriptCommand.setTabCompleter(new SkriptCommandTabCompleter());
            AddonModule legacyModule = new AddonModule() {
               @Override
               public void init(org.skriptlang.skript.addon.SkriptAddon addon) {
                  new DefaultComparators();
                  new DefaultConverters();
                  ChatMessages.registerListeners();
               }

               @Override
               public void load(org.skriptlang.skript.addon.SkriptAddon addon) {
                  try {
                     Skript.getAddonInstance()
                        .loadClasses("ch.njol.skript", "conditions", "effects", "events", "expressions", "entity", "literals", "sections", "structures");
                  } catch (IOException var3) {
                     throw new RuntimeException(var3);
                  }

                  BukkitEventValues.register(eventValueRegistry);
                  new DefaultFunctions();
                  new DefaultOperations();
               }

               @Override
               public String name() {
                  return "legacy";
               }
            };

            try {
               skript.loadModules(new AddonModule[]{legacyModule, new CommonModule(), new BukkitModule()});
            } catch (Exception var33) {
               exception(var33, "Failed to load one of Skript's modules: " + var33.getLocalizedMessage());
               this.setEnabled(false);
               return;
            }

            final CompletableFuture<Boolean> aliases = Aliases.loadAsync();
            Commands.registerListeners();
            if (logNormal()) {
               info(" " + Language.get("skript.copyright"));
            }

            final long tick = testing() ? ((World)Bukkit.getWorlds().get(0)).getFullTime() : 0L;
            Bukkit.getScheduler()
               .scheduleSyncDelayedTask(
                  this,
                  new Runnable() {
                     @Override
                     public void run() {
                        assert ((World)Bukkit.getWorlds().get(0)).getFullTime() == tick;

                        try (JarFile jar = new JarFile(Skript.this.getFile())) {
                           for (JarEntry ex : new EnumerationIterable<>(jar.entries())) {
                              if (ex.getName().startsWith("ch/njol/skript/hooks/")
                                 && ex.getName().endsWith("Hook.class")
                                 && StringUtils.count(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(ex.getName()), '/') <= 5) {
                                 String c = ex.getName().replace('/', '.').substring(0, ex.getName().length() - ".class".length());

                                 try {
                                    Class<?> hook = Class.forName(c, true, Skript.this.getClassLoader());
                                    if (Hook.class.isAssignableFrom(hook)
                                       && !Modifier.isAbstract(hook.getModifiers())
                                       && Skript.isHookEnabled((Class<? extends Hook<?>>)hook)) {
                                       hook.getDeclaredConstructor().setAccessible(true);
                                       hook.getDeclaredConstructor().newInstance();
                                    }
                                 } catch (ClassNotFoundException var24) {
                                    Skript.exception(var24, "Cannot load class " + c);
                                 } catch (ExceptionInInitializerError var25) {
                                    Skript.exception(var25.getCause(), "Class " + c + " generated an exception while loading");
                                 } catch (Exception var26) {
                                    Skript.exception(var26, "Exception initializing hook: " + c);
                                 }
                              }
                           }
                        } catch (IOException var30) {
                           Skript.error("Error while loading plugin hooks" + (var30.getLocalizedMessage() == null ? "" : ": " + var30.getLocalizedMessage()));
                           Skript.exception(var30);
                        }

                        Skript.finishedLoadingHooks = true;

                        try {
                           aliases.get();
                        } catch (ExecutionException | InterruptedException var22) {
                           Skript.exception(var22, "Could not load aliases concurrently");
                        }

                        if (TestMode.ENABLED) {
                           Skript.info("Preparing Skript for testing...");
                           Skript.tainted = true;

                           try {
                              Skript.getAddonInstance().loadClasses("ch.njol.skript.test.runner");
                              if (TestMode.JUNIT) {
                                 Skript.getAddonInstance().loadClasses("org.skriptlang.skript.test.junit.registration");
                              }
                           } catch (IOException var21) {
                              Skript.exception("Failed to load testing environment.");
                              Bukkit.getServer().shutdown();
                           }
                        }

                        Skript.stopAcceptingRegistrations();
                        Documentation.generate();
                        if (Skript.logNormal()) {
                           Skript.info("Loading variables...");
                        }

                        long vls = System.currentTimeMillis();
                        LogHandler h = SkriptLogger.startLogHandler(
                           new ErrorDescLogHandler() {
                              @Override
                              public LogHandler.LogResult log(LogEntry entry) {
                                 super.log(entry);
                                 if (entry.level.intValue() >= Level.SEVERE.intValue()) {
                                    Skript.logEx(entry.message);
                                    return LogHandler.LogResult.DO_NOT_LOG;
                                 } else {
                                    return LogHandler.LogResult.LOG;
                                 }
                              }

                              @Override
                              protected void beforeErrors() {
                                 Skript.logEx();
                                 Skript.logEx("===!!!=== Skript variable load error ===!!!===");
                                 Skript.logEx("Unable to load (all) variables:");
                              }

                              @Override
                              protected void afterErrors() {
                                 Skript.logEx();
                                 Skript.logEx(
                                    "Skript will work properly, but old variables might not be available at all and new ones may or may not be saved until Skript is able to create a backup of the old file and/or is able to connect to the database (which requires a restart of Skript)!"
                                 );
                                 Skript.logEx();
                              }
                           }
                        );

                        try (CountingLogHandler c = new CountingLogHandler(SkriptLogger.SEVERE).start()) {
                           if (!Variables.load() && c.getCount() == 0) {
                              Skript.error("(no information available)");
                           }
                        } finally {
                           h.stop();
                        }

                        long var34 = System.currentTimeMillis() - vls;
                        if (Skript.logNormal()) {
                           Skript.info("Loaded " + Variables.numVariables() + " variables in " + var34 / 100L / 10.0 + " seconds");
                        }

                        Skript.debug("Early init done");
                        if (TestMode.ENABLED) {
                           if (TestMode.DEV_MODE) {
                              Skript.this.runTests();
                           } else {
                              World world = (World)Bukkit.getWorlds().get(0);
                              world.setSpawnLocation(0, 0, 0);
                              Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> {
                                 world.addPluginChunkTicket(0, 0, Skript.getInstance());
                                 world.addPluginChunkTicket(100, 100, Skript.getInstance());
                                 Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> Skript.this.runTests(), 100L);
                              }, 5L);
                           }
                        }

                        Skript.metrics = new Metrics(Skript.getInstance(), 722);
                        SkriptMetrics.setupMetrics(Skript.metrics);
                        Date start = new Date();
                        CountingLogHandler logHandler = new CountingLogHandler(Level.SEVERE);
                        File scriptsFolder = Skript.this.getScriptsFolder();
                        ScriptLoader.updateDisabledScripts(scriptsFolder.toPath());
                        ScriptLoader.loadScripts(scriptsFolder, logHandler)
                           .thenAccept(
                              scriptInfo -> {
                                 try {
                                    if (logHandler.getCount() == 0) {
                                       Skript.info(Skript.m_no_errors.toString());
                                    }

                                    if (scriptInfo.files == 0) {
                                       Skript.warning(Skript.m_no_scripts.toString());
                                    }

                                    if (Skript.logNormal() && scriptInfo.files > 0) {
                                       Skript.info(Skript.m_scripts_loaded.toString(scriptInfo.files, scriptInfo.structures, start.difference(new Date())));
                                    }

                                    Skript.info(Skript.m_finished_loading.toString());
                                    if (!ScriptLoader.isAsync()) {
                                       EvtSkript.onSkriptStart();
                                       Filter filter = record -> record == null
                                          ? false
                                          : record.getMessage() == null || !record.getMessage().toLowerCase(Locale.ENGLISH).startsWith("can't keep up!");
                                       BukkitLoggerFilter.addFilter(filter);
                                       Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.this, () -> BukkitLoggerFilter.removeFilter(filter), 1L);
                                    } else {
                                       Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.this, EvtSkript::onSkriptStart);
                                    }
                                 } catch (Exception var5x) {
                                    throw Skript.exception(var5x);
                                 }
                              }
                           );
                     }
                  }
               );
            if (!TestMode.ENABLED) {
               Bukkit.getPluginManager().registerEvents(new Skript.JoinUpdateNotificationListener(), this);
            }

            Bukkit.getPluginManager().registerEvents(new Skript.ServerReloadListener(), this);
            SkriptTimings.setSkript(this);
         }
      }
   }

   private void runTests() {
      info("Skript testing environment enabled, starting...");
      AtomicLong shutdownDelay = new AtomicLong(0L);
      List<Class<?>> asyncTests = new ArrayList<>();
      CompletableFuture<Void> onAsyncComplete = CompletableFuture.completedFuture(null);
      if (TestMode.GEN_DOCS) {
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "skript gen-docs");
      } else {
         if (TestMode.DEV_MODE) {
            info("Test development mode enabled. Test scripts are at " + TestMode.TEST_DIR);
            return;
         }

         info("Loading all tests from " + TestMode.TEST_DIR);
         TestingLogHandler errorCounter = new TestingLogHandler(Level.SEVERE);

         try {
            errorCounter.start();
            ScriptLoader.loadScripts(new File(this.getScriptsFolder(), "-examples" + File.separator), errorCounter);
            ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());
            ScriptLoader.loadScripts(TestMode.TEST_DIR.toFile(), errorCounter);
         } finally {
            errorCounter.stop();
         }

         Bukkit.getPluginManager().callEvent(new SkriptTestEvent());
         if (errorCounter.getCount() > 0) {
            TestTracker.testStarted("parse scripts");
            TestTracker.testFailed(errorCounter.getCount() + " error(s) found");
         }

         if (errored) {
            TestTracker.testStarted("run scripts");
            TestTracker.testFailed("exception was thrown during execution");
         }

         if (TestMode.JUNIT) {
            AtomicLong milliseconds = new AtomicLong(0L);
            AtomicLong tests = new AtomicLong(0L);
            AtomicLong fails = new AtomicLong(0L);
            AtomicLong ignored = new AtomicLong(0L);
            AtomicLong size = new AtomicLong(0L);
            info("Running sync JUnit tests...");

            try {
               Set<Class<?>> classes = new HashSet<>();
               ClassLoader.builder()
                  .addSubPackages("org.skriptlang.skript", "ch.njol.skript")
                  .filter(fqn -> fqn.endsWith("Test"))
                  .initialize(true)
                  .deep(true)
                  .forEachClass(clazzx -> {
                     if (!clazzx.isAnonymousClass() && !clazzx.isLocalClass()) {
                        classes.add(clazzx);
                     }
                  })
                  .build()
                  .loadClasses(Skript.class, this.getFile());
               classes.remove(SkriptJUnitTest.class);
               classes.remove(SkriptAsyncJUnitTest.class);
               size.set(classes.size());

               for (Class<?> clazz : classes) {
                  if (SkriptAsyncJUnitTest.class.isAssignableFrom(clazz)) {
                     asyncTests.add(clazz);
                  } else {
                     this.runTest(clazz, shutdownDelay, tests, milliseconds, ignored, fails);
                  }
               }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException var16) {
               exception(var16, "Failed to initalize test JUnit classes.");
            }

            if (ignored.get() > 0L) {
               warning("There were " + ignored + " ignored test cases! This can mean they are not properly setup in order in that class!");
            }

            onAsyncComplete = CompletableFuture.runAsync(
               () -> {
                  info("Running async JUnit tests...");

                  try {
                     for (Class<?> clazzx : asyncTests) {
                        this.runTest(clazzx, shutdownDelay, tests, milliseconds, ignored, fails);
                     }
                  } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException var10x) {
                     exception(var10x, "Failed to initalize test JUnit classes.");
                  }

                  if (ignored.get() > 0L) {
                     warning("There were " + ignored + " ignored test cases! This can mean they are not properly setup in order in that class!");
                  }

                  info("Completed " + tests + " JUnit tests in " + size + " classes with " + fails + " failures in " + milliseconds + " milliseconds.");
               }
            );
         }
      }

      onAsyncComplete.thenRun(() -> {
         double display = shutdownDelay.get() / 20.0;
         info("Testing done, shutting down the server in " + display + " second" + (display == 1.0 ? "" : "s") + "...");
         Bukkit.getScheduler().runTaskLater(this, () -> {
            info("Shutting down server.");
            if (TestMode.JUNIT && !EffObjectives.isJUnitComplete()) {
               EffObjectives.fail();
            }

            info("Collecting results to " + TestMode.RESULTS_FILE);
            String results = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(TestTracker.collectResults());

            try {
               Files.write(TestMode.RESULTS_FILE, results.getBytes(StandardCharsets.UTF_8));
            } catch (IOException var3x) {
               exception(var3x, "Failed to write test results.");
            }

            Bukkit.getScheduler().runTaskLater(this, () -> Bukkit.getServer().shutdown(), 1L);
         }, shutdownDelay.get());
      });
   }

   private void runTest(Class<?> clazz, AtomicLong shutdownDelay, AtomicLong tests, AtomicLong milliseconds, AtomicLong ignored, AtomicLong fails) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
      String test = clazz.getName();
      SkriptJUnitTest.setCurrentJUnitTest(test);
      SkriptJUnitTest.setShutdownDelay(0L);
      info("Running JUnit test '" + test + "'");
      Result result = JUnitCore.runClasses(new Class[]{clazz});
      TestTracker.testStarted("JUnit: '" + test + "'");
      boolean overrides = false;

      for (Method method : clazz.getDeclaredMethods()) {
         if (method.isAnnotationPresent(After.class)) {
            if (SkriptJUnitTest.getShutdownDelay() > 1L) {
               warning(
                  "Methods annotated with @After in happen instantaneously, and '"
                     + test
                     + "' requires a delay. Do test cleanup in the junit script file or 'cleanup' method."
               );
            }

            if (method.getName().equals("cleanup")) {
               overrides = true;
            }
         }
      }

      if (SkriptJUnitTest.getShutdownDelay() > 1L && !overrides) {
         error(
            "The JUnit class '"
               + test
               + "' does not override the method 'cleanup', thus the test data will instantly be cleaned up despite requiring a longer shutdown time: "
               + SkriptJUnitTest.getShutdownDelay()
         );
      }

      shutdownDelay.set(Math.max(shutdownDelay.get(), SkriptJUnitTest.getShutdownDelay()));
      tests.getAndAdd(result.getRunCount());
      milliseconds.getAndAdd(result.getRunTime());
      ignored.getAndAdd(result.getIgnoreCount());
      fails.getAndAdd(result.getFailureCount());

      for (Failure failure : result.getFailures()) {
         String message = failure.getMessage() == null ? "" : " " + failure.getMessage();
         TestTracker.JUnitTestFailed(test, message);
         exception(failure.getException(), "JUnit test '" + failure.getTestHeader() + " failed.");
      }

      if (SkriptJUnitTest.class.isAssignableFrom(clazz) && !SkriptAsyncJUnitTest.class.isAssignableFrom(clazz)) {
         ((SkriptJUnitTest)clazz.getConstructor().newInstance()).cleanup();
      }

      SkriptJUnitTest.clearJUnitTest();
   }

   private void handleJvmArguments() {
      Path folder = this.getDataFolder().toPath();
      String burgerEnabled = System.getProperty("skript.burger.enable");
      if (burgerEnabled != null) {
         tainted = true;
         String version = System.getProperty("skript.burger.version");
         String burgerInput;
         if (version == null) {
            String inputFile = System.getProperty("skript.burger.file");
            if (inputFile == null) {
               exception("burger enabled but skript.burger.file not provided");
               return;
            }

            try {
               burgerInput = new String(Files.readAllBytes(Paths.get(inputFile)), StandardCharsets.UTF_8);
            } catch (IOException var12) {
               exception(var12);
               return;
            }
         } else {
            try {
               Path data = folder.resolve("burger-" + version + ".json");
               if (!Files.exists(data)) {
                  URL url = new URL("https://pokechu22.github.io/Burger/" + version + ".json");

                  try (InputStream is = url.openStream()) {
                     Files.copy(is, data);
                  }
               }

               burgerInput = new String(Files.readAllBytes(data), StandardCharsets.UTF_8);
            } catch (IOException var14) {
               exception(var14);
               return;
            }
         }

         try {
            BurgerHelper burger = new BurgerHelper(burgerInput);
            Map<String, Material> materials = burger.mapMaterials();
            Map<Integer, Material> ids = BurgerHelper.mapIds();
            Gson gson = new Gson();
            Files.write(folder.resolve("materials_mappings.json"), gson.toJson(materials).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            Files.write(folder.resolve("id_mappings.json"), gson.toJson(ids).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
         } catch (IOException var11) {
            exception(var11);
         }
      }
   }

   public static Version getMinecraftVersion() {
      return minecraftVersion;
   }

   public static boolean isRunningCraftBukkit() {
      return serverPlatform == ServerPlatform.BUKKIT_CRAFTBUKKIT;
   }

   public static boolean isRunningMinecraft(int major, int minor) {
      if (minecraftVersion.compareTo(UNKNOWN_VERSION) == 0) {
         updateMinecraftVersion();
      }

      return minecraftVersion.compareTo(major, minor) >= 0;
   }

   public static boolean isRunningMinecraft(int major, int minor, int revision) {
      if (minecraftVersion.compareTo(UNKNOWN_VERSION) == 0) {
         updateMinecraftVersion();
      }

      return minecraftVersion.compareTo(major, minor, revision) >= 0;
   }

   public static boolean isRunningMinecraft(Version v) {
      if (minecraftVersion.compareTo(UNKNOWN_VERSION) == 0) {
         updateMinecraftVersion();
      }

      return minecraftVersion.compareTo(v) >= 0;
   }

   public static boolean classExists(String className) {
      try {
         Class.forName(className);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }

   public static boolean methodExists(Class<?> c, String methodName, Class<?>... parameterTypes) {
      try {
         c.getDeclaredMethod(methodName, parameterTypes);
         return true;
      } catch (NoSuchMethodException var4) {
         return false;
      } catch (SecurityException var5) {
         return false;
      }
   }

   public static boolean methodExists(Class<?> c, String methodName, Class<?>[] parameterTypes, Class<?> returnType) {
      try {
         Method m = c.getDeclaredMethod(methodName, parameterTypes);
         return m.getReturnType() == returnType;
      } catch (NoSuchMethodException var5) {
         return false;
      } catch (SecurityException var6) {
         return false;
      }
   }

   public static boolean fieldExists(Class<?> c, String fieldName) {
      try {
         c.getDeclaredField(fieldName);
         return true;
      } catch (NoSuchFieldException var3) {
         return false;
      } catch (SecurityException var4) {
         return false;
      }
   }

   @Nullable
   public static Metrics getMetrics() {
      return metrics;
   }

   public static void closeOnDisable(Closeable closeable) {
      closeOnDisable.add(closeable);
   }

   @EventHandler
   public void onPluginDisable(PluginDisableEvent event) {
      Plugin plugin = event.getPlugin();
      PluginDescriptionFile descriptionFile = plugin.getDescription();
      if ((descriptionFile.getDepend().contains("Skript") || descriptionFile.getSoftDepend().contains("Skript")) && !this.isServerRunning()) {
         this.beforeDisable();
      }
   }

   private boolean isServerRunning() {
      if (IS_STOPPING_EXISTS) {
         return !Bukkit.getServer().isStopping();
      } else {
         try {
            return (Boolean)IS_RUNNING.invoke(MC_SERVER);
         } catch (InvocationTargetException | IllegalAccessException var2) {
            throw new RuntimeException(var2);
         }
      }
   }

   private void beforeDisable() {
      partDisabled = true;
      EvtSkript.onSkriptStop();
      ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());
   }

   public void onDisable() {
      if (!disabled) {
         disabled = true;
         if (!partDisabled) {
            this.beforeDisable();
         }

         Bukkit.getScheduler().cancelTasks(this);

         for (Closeable c : closeOnDisable) {
            try {
               c.close();
            } catch (Exception var4) {
               exception(var4, "An error occurred while shutting down.", "This might or might not cause any issues.");
            }
         }

         experimentRegistry = null;
      }
   }

   public static void outdatedError() {
      error(
         "Skript v"
            + getInstance().getDescription().getVersion()
            + " is not fully compatible with Bukkit "
            + Bukkit.getVersion()
            + ". Some feature(s) will be broken until you update Skript."
      );
   }

   public static void outdatedError(Exception e) {
      outdatedError();
      if (testing()) {
         e.printStackTrace();
      }
   }

   public static String toString(double n) {
      return StringUtils.toString(n, SkriptConfig.numberAccuracy.value());
   }

   public static Thread newThread(Runnable r, String name) {
      Thread t = new Thread(r, name);
      t.setUncaughtExceptionHandler(UEH);
      return t;
   }

   public static boolean isAcceptRegistrations() {
      if (instance == null) {
         throw new IllegalStateException("Skript was never loaded");
      } else {
         return acceptRegistrations && instance.isEnabled();
      }
   }

   public static void checkAcceptRegistrations() {
      if (!isAcceptRegistrations() && !testing()) {
         throw new SkriptAPIException("Registration can only be done during plugin initialization");
      }
   }

   private static void stopAcceptingRegistrations() {
      Converters.createChainedConverters();
      ExprArithmetic.registerExpression();
      acceptRegistrations = false;
      Classes.onRegistrationsStop();
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static SkriptAddon registerAddon(JavaPlugin plugin) {
      checkAcceptRegistrations();
      SkriptAddon addon = new SkriptAddon(plugin);
      addons.add(addon);
      return addon;
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   @Nullable
   public static SkriptAddon getAddon(JavaPlugin plugin) {
      if (plugin == getInstance()) {
         return getAddonInstance();
      } else {
         for (SkriptAddon addon : getAddons()) {
            if (addon.plugin == plugin) {
               return addon;
            }
         }

         return null;
      }
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   @Nullable
   public static SkriptAddon getAddon(String name) {
      if (name.equals(getInstance().getName())) {
         return getAddonInstance();
      } else {
         for (SkriptAddon addon : getAddons()) {
            if (addon.getName().equals(name)) {
               return addon;
            }
         }

         return null;
      }
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   @Unmodifiable
   public static Collection<SkriptAddon> getAddons() {
      Set<SkriptAddon> addons = new HashSet<>(Skript.addons);
      addons.addAll(
         instance()
            .addons()
            .stream()
            .filter(addon -> addons.stream().noneMatch(oldAddon -> oldAddon.name().equals(addon.name())))
            .map(SkriptAddon::fromModern)
            .collect(Collectors.toSet())
      );
      return Collections.unmodifiableCollection(addons);
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static SkriptAddon getAddonInstance() {
      if (addon == null) {
         addon = SkriptAddon.fromModern(instance());
      }

      return addon;
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   @Internal
   public static Origin getSyntaxOrigin(Class<?> source) {
      JavaPlugin plugin;
      try {
         plugin = JavaPlugin.getProvidingPlugin(source);
      } catch (IllegalArgumentException var3) {
         return Origin.UNKNOWN;
      }

      SkriptAddon addon = getAddon(plugin);
      return addon != null ? Origin.of(addon) : Origin.UNKNOWN;
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static <E extends Condition> void registerCondition(Class<E> conditionClass, String... patterns) throws IllegalArgumentException {
      registerCondition(conditionClass, Condition.ConditionType.COMBINED, patterns);
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static <E extends Condition> void registerCondition(Class<E> conditionClass, Condition.ConditionType type, String... patterns) throws IllegalArgumentException {
      checkAcceptRegistrations();
      skript.syntaxRegistry()
         .register(
            SyntaxRegistry.CONDITION,
            SyntaxInfo.builder(conditionClass).priority(type.priority()).origin(getSyntaxOrigin(conditionClass)).addPatterns(patterns).build()
         );
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static <E extends Effect> void registerEffect(Class<E> effectClass, String... patterns) throws IllegalArgumentException {
      checkAcceptRegistrations();
      skript.syntaxRegistry()
         .register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(effectClass).origin(getSyntaxOrigin(effectClass)).addPatterns(patterns).build());
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static <E extends Section> void registerSection(Class<E> sectionClass, String... patterns) throws IllegalArgumentException {
      checkAcceptRegistrations();
      skript.syntaxRegistry()
         .register(SyntaxRegistry.SECTION, SyntaxInfo.builder(sectionClass).origin(getSyntaxOrigin(sectionClass)).addPatterns(patterns).build());
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   @Unmodifiable
   public static Collection<SyntaxElementInfo<? extends Statement>> getStatements() {
      return instance()
         .syntaxRegistry()
         .syntaxes(SyntaxRegistry.STATEMENT)
         .stream()
         .map(SyntaxElementInfo::fromModern)
         .collect(Collectors.toUnmodifiableList());
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   @Unmodifiable
   public static Collection<SyntaxElementInfo<? extends Condition>> getConditions() {
      return instance()
         .syntaxRegistry()
         .syntaxes(SyntaxRegistry.CONDITION)
         .stream()
         .map(SyntaxElementInfo::fromModern)
         .collect(Collectors.toUnmodifiableList());
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   @Unmodifiable
   public static Collection<SyntaxElementInfo<? extends Effect>> getEffects() {
      return instance().syntaxRegistry().syntaxes(SyntaxRegistry.EFFECT).stream().map(SyntaxElementInfo::fromModern).collect(Collectors.toUnmodifiableList());
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   @Unmodifiable
   public static Collection<SyntaxElementInfo<? extends Section>> getSections() {
      return instance().syntaxRegistry().syntaxes(SyntaxRegistry.SECTION).stream().map(SyntaxElementInfo::fromModern).collect(Collectors.toUnmodifiableList());
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static <E extends Expression<T>, T> void registerExpression(Class<E> expressionClass, Class<T> returnType, ExpressionType type, String... patterns) throws IllegalArgumentException {
      checkAcceptRegistrations();
      skript.syntaxRegistry()
         .register(
            SyntaxRegistry.EXPRESSION,
            ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                           expressionClass, returnType
                        )
                        .priority(type.priority()))
                     .origin(getSyntaxOrigin(expressionClass)))
                  .addPatterns(patterns))
               .build()
         );
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static Iterator<ExpressionInfo<?, ?>> getExpressions() {
      List<ExpressionInfo<?, ?>> list = new ArrayList<>();

      for (DefaultSyntaxInfos.Expression<?, ?> info : instance().syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)) {
         list.add(SyntaxElementInfo.fromModern(info));
      }

      return list.iterator();
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static Iterator<ExpressionInfo<?, ?>> getExpressions(Class<?>... returnTypes) {
      return new CheckedIterator<>(getExpressions(), info -> {
         if (info != null && info.returnType != Object.class) {
            for (Class<?> returnType : returnTypes) {
               assert returnType != null;

               if (Converters.converterExists(info.returnType, returnType)) {
                  return true;
               }
            }

            return false;
         } else {
            return true;
         }
      });
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static <E extends SkriptEvent> SkriptEventInfo<E> registerEvent(String name, Class<E> c, Class<? extends Event> event, String... patterns) {
      return registerEvent(name, c, new Class[]{event}, patterns);
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static <E extends SkriptEvent> SkriptEventInfo<E> registerEvent(String name, Class<E> eventClass, Class<? extends Event>[] events, String... patterns) {
      checkAcceptRegistrations();

      for (int i = 0; i < patterns.length; i++) {
         patterns[i] = BukkitSyntaxInfos.fixPattern(patterns[i]);
      }

      SkriptEventInfo.ModernSkriptEventInfo<E> legacy = new SkriptEventInfo.ModernSkriptEventInfo<>(name, patterns, eventClass, "", events);
      skript.syntaxRegistry().register((SyntaxRegistry.Key<SkriptEventInfo.ModernSkriptEventInfo<E>>)BukkitSyntaxInfos.Event.KEY, legacy);
      return legacy;
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static <E extends Structure> void registerStructure(Class<E> structureClass, String... patterns) {
      checkAcceptRegistrations();
      skript.syntaxRegistry()
         .register(
            SyntaxRegistry.STRUCTURE,
            DefaultSyntaxInfos.Structure.builder(structureClass).origin(getSyntaxOrigin(structureClass)).addPatterns(patterns).build()
         );
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static <E extends Structure> void registerSimpleStructure(Class<E> structureClass, String... patterns) {
      checkAcceptRegistrations();
      skript.syntaxRegistry()
         .register(
            SyntaxRegistry.STRUCTURE,
            DefaultSyntaxInfos.Structure.builder(structureClass)
               .origin(getSyntaxOrigin(structureClass))
               .addPatterns(patterns)
               .nodeType(DefaultSyntaxInfos.Structure.NodeType.SIMPLE)
               .build()
         );
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static <E extends Structure> void registerStructure(Class<E> structureClass, EntryValidator entryValidator, String... patterns) {
      registerStructure(structureClass, entryValidator, DefaultSyntaxInfos.Structure.NodeType.SECTION, patterns);
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static <E extends Structure> void registerStructure(
      Class<E> structureClass, EntryValidator entryValidator, DefaultSyntaxInfos.Structure.NodeType nodeType, String... patterns
   ) {
      checkAcceptRegistrations();
      skript.syntaxRegistry()
         .register(
            SyntaxRegistry.STRUCTURE,
            DefaultSyntaxInfos.Structure.builder(structureClass)
               .origin(getSyntaxOrigin(structureClass))
               .addPatterns(patterns)
               .entryValidator(entryValidator)
               .nodeType(nodeType)
               .build()
         );
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   @Unmodifiable
   public static Collection<SkriptEventInfo<?>> getEvents() {
      return instance()
         .syntaxRegistry()
         .syntaxes(BukkitSyntaxInfos.Event.KEY)
         .stream()
         .map(SyntaxElementInfo::fromModern)
         .collect(Collectors.toUnmodifiableList());
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   @Unmodifiable
   public static List<StructureInfo<? extends Structure>> getStructures() {
      return instance()
         .syntaxRegistry()
         .syntaxes(SyntaxRegistry.STRUCTURE)
         .stream()
         .map(SyntaxElementInfo::fromModern)
         .collect(Collectors.toUnmodifiableList());
   }

   public static boolean dispatchCommand(CommandSender sender, String command) {
      try {
         if (sender instanceof Player) {
            PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent((Player)sender, "/" + command);
            Bukkit.getPluginManager().callEvent(e);
            return !e.isCancelled() && e.getMessage().startsWith("/") ? Bukkit.dispatchCommand(e.getPlayer(), e.getMessage().substring(1)) : false;
         } else {
            ServerCommandEvent e = new ServerCommandEvent(sender, command);
            Bukkit.getPluginManager().callEvent(e);
            return !e.getCommand().isEmpty() && !e.isCancelled() ? Bukkit.dispatchCommand(e.getSender(), e.getCommand()) : false;
         }
      } catch (Exception var3) {
         var3.printStackTrace();
         return false;
      }
   }

   public static boolean logNormal() {
      return SkriptLogger.log(Verbosity.NORMAL);
   }

   public static boolean logHigh() {
      return SkriptLogger.log(Verbosity.HIGH);
   }

   public static boolean logVeryHigh() {
      return SkriptLogger.log(Verbosity.VERY_HIGH);
   }

   public static boolean debug() {
      return SkriptLogger.debug();
   }

   public static boolean testing() {
      return debug() || Skript.class.desiredAssertionStatus();
   }

   public static boolean log(Verbosity minVerb) {
      return SkriptLogger.log(minVerb);
   }

   public static void debug(String info) {
      if (debug()) {
         SkriptLogger.log(SkriptLogger.DEBUG, info);
      }
   }

   public static void debug(String message, Object... objects) {
      if (debug()) {
         debug(message.formatted(objects));
      }
   }

   public static void info(String info) {
      SkriptLogger.log(Level.INFO, info);
   }

   public static void warning(String warning) {
      SkriptLogger.log(Level.WARNING, warning);
   }

   public static void error(@Nullable String error) {
      if (error != null) {
         SkriptLogger.log(Level.SEVERE, error);
      }
   }

   public static void error(String message, Object... objects) {
      error(message.formatted(objects));
   }

   public static void error(String error, ErrorQuality quality) {
      SkriptLogger.log(new LogEntry(SkriptLogger.SEVERE, quality, error));
   }

   public static EmptyStacktraceException exception(String... info) {
      return exception(null, info);
   }

   public static EmptyStacktraceException exception(@Nullable Throwable cause, String... info) {
      return exception(cause, null, null, info);
   }

   public static EmptyStacktraceException exception(@Nullable Throwable cause, @Nullable Thread thread, String... info) {
      return exception(cause, thread, null, info);
   }

   public static EmptyStacktraceException exception(@Nullable Throwable cause, @Nullable TriggerItem item, String... info) {
      return exception(cause, null, item, info);
   }

   public static void markErrored() {
      errored = true;
   }

   public static EmptyStacktraceException exception(@Nullable Throwable cause, @Nullable Thread thread, @Nullable TriggerItem item, String... info) {
      errored = true;
      if (cause instanceof EmptyStacktraceException) {
         return new EmptyStacktraceException();
      } else {
         if (!checkedPlugins) {
            initializePluginPackages();
            checkedPlugins = true;
         }

         logErrorDetails(cause, info, thread, item);
         return new EmptyStacktraceException();
      }
   }

   private static void initializePluginPackages() {
      for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
         if (!plugin.getName().equals("Skript")) {
            PluginDescriptionFile desc = plugin.getDescription();
            if (desc.getDepend().contains("Skript") || desc.getSoftDepend().contains("Skript")) {
               String mainClassPackage = getPackageName(desc.getMain());
               pluginPackages.put(mainClassPackage, desc);
               if (debug()) {
                  info("Identified potential addon: " + desc.getFullName() + " (" + mainClassPackage + ")");
               }
            }
         }
      }
   }

   private static String getPackageName(String qualifiedClassName) {
      int lastDotIndex = qualifiedClassName.lastIndexOf(46);
      return lastDotIndex == -1 ? "" : qualifiedClassName.substring(0, lastDotIndex);
   }

   private static void logErrorDetails(@Nullable Throwable cause, String[] info, @Nullable Thread thread, @Nullable TriggerItem item) {
      String issuesUrl = "https://github.com/SkriptLang/Skript/issues";
      String downloadUrl = "https://github.com/SkriptLang/Skript/releases/latest";
      logEx();
      logEx("[Skript] Severe Error:");
      logEx(info);
      logEx();
      Set<PluginDescriptionFile> stackPlugins = identifyPluginsInStackTrace(Thread.currentThread().getStackTrace());
      logPlatformSupportInfo(issuesUrl, downloadUrl, stackPlugins);
      logEx();
      logEx("Stack trace:");
      logStackTrace(cause);
      logEx();
      logVersionInfo();
      logEx();
      logCurrentState(thread, item);
      logEx("End of Error.");
      logEx();
   }

   private static Set<PluginDescriptionFile> identifyPluginsInStackTrace(StackTraceElement[] stackTrace) {
      Set<PluginDescriptionFile> stackPlugins = new HashSet<>();

      for (StackTraceElement element : stackTrace) {
         pluginPackages.entrySet()
            .stream()
            .filter(entry -> element.getClassName().startsWith(entry.getKey()))
            .forEach(entry -> stackPlugins.add(entry.getValue()));
      }

      return stackPlugins;
   }

   private static void logPlatformSupportInfo(String issuesUrl, String downloadUrl, Set<PluginDescriptionFile> stackPlugins) {
      SkriptUpdater updater = getInstance().getUpdater();
      if (tainted) {
         logEx("Skript is running with developer command-line options. Consider disabling them if not a developer.");
      } else if (getInstance().getDescription().getVersion().contains("nightly")) {
         logEx("You're running a (buggy) nightly version of Skript. If this is not a test server, switch to a stable release.");
         logEx("Please report this bug to: " + issuesUrl);
      } else if (!serverPlatform.supported) {
         String supportedPlatforms = getSupportedPlatforms();
         logEx(
            "Your server platform appears to be unsupported by Skript. Consider switching to one of the supported platforms ("
               + supportedPlatforms
               + ") for better compatibility."
         );
      } else if (updater != null && updater.getReleaseStatus() == ReleaseStatus.OUTDATED) {
         logEx("You're running an outdated version of Skript! Update to the latest version here: " + downloadUrl);
      } else {
         logEx("An unexpected error occurred with Skript. This issue is likely not your fault.");
         logExAddonInfo(issuesUrl, stackPlugins);
      }
   }

   private static String getSupportedPlatforms() {
      return Arrays.stream(ServerPlatform.values()).filter(platform -> platform.supported).map(Enum::name).collect(Collectors.joining(", "));
   }

   private static void logExAddonInfo(String issuesUrl, Set<PluginDescriptionFile> stackPlugins) {
      if (pluginPackages.isEmpty()) {
         logEx("Report the issue: " + issuesUrl);
      } else {
         logEx("You are using some plugins that alter how Skript works (addons).");
         if (stackPlugins.isEmpty()) {
            logEx("Full list of addons:");
            pluginPackages.values().forEach(desc -> logEx(getPluginDescription(desc)));
            logEx("We could not identify related addons, it might also be a Skript issue.");
         } else {
            logEx("The following plugins are likely related to this error:");
            stackPlugins.forEach(desc -> logEx(getPluginDescription(desc)));
         }

         logEx("Try temporarily removing the listed plugins one by one to identify the cause.");
         logEx("If removing a plugin resolves the issue, please report the problem to the plugin developer.");
      }
   }

   private static String getPluginDescription(PluginDescriptionFile desc) {
      String website = desc.getWebsite();
      return desc.getFullName() + (website != null && !website.isEmpty() ? " (" + website + ")" : "");
   }

   private static void logStackTrace(@Nullable Throwable cause) {
      if (cause == null || cause.getStackTrace().length == 0) {
         logEx("Warning: no/empty exception given, dumping current stack trace instead");
         cause = new Exception("EmptyStacktraceException cause");
      }

      while (cause != null) {
         logEx((cause == null ? "" : "Caused by: ") + cause.toString());

         for (StackTraceElement element : cause.getStackTrace()) {
            logEx("    at " + element.toString());
         }

         cause = cause.getCause();
      }
   }

   private static void logVersionInfo() {
      SkriptUpdater updater = getInstance().getUpdater();
      if (updater != null) {
         ReleaseStatus status = updater.getReleaseStatus();
         logEx("Skript: " + getVersion() + " (" + status.toString() + ")");
         ReleaseManifest current = updater.getCurrentRelease();
         logEx("    Flavor: " + current.flavor);
         logEx("    Date: " + current.date);
      } else {
         logEx("Skript: " + getVersion() + " (unknown; likely custom)");
      }

      logEx("Bukkit: " + Bukkit.getBukkitVersion());
      logEx("Minecraft: " + getMinecraftVersion());
      logEx("Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version") + ")");
      logEx("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version"));
      logEx();
      logEx("Server platform: " + serverPlatform.name + (serverPlatform.supported ? "" : " (unsupported)"));
   }

   private static void logCurrentState(@Nullable Thread thread, @Nullable TriggerItem item) {
      logEx("Current node: " + SkriptLogger.getNode());
      logEx("Current item: " + (item == null ? "null" : item.toString(null, true)));
      if (item != null && item.getTrigger() != null) {
         Trigger trigger = item.getTrigger();
         Script script = trigger.getScript();
         logEx(
            "Current trigger: "
               + trigger.toString(null, true)
               + " ("
               + (script == null ? "null" : script.getConfig().getFileName())
               + ", line "
               + trigger.getLineNumber()
               + ")"
         );
      }

      logEx("Thread: " + (thread == null ? Thread.currentThread() : thread).getName());
      logEx("Language: " + Language.getName());
      logEx("Link parse mode: " + TextComponentParser.instance().linkParseMode());
   }

   static void logEx() {
      SkriptLogger.LOGGER.severe("#!#! ");
   }

   static void logEx(String... lines) {
      for (String line : lines) {
         SkriptLogger.LOGGER.severe("#!#! " + line);
      }
   }

   public static String getSkriptPrefix() {
      return SKRIPT_PREFIX_MESSAGE.getValueOrDefault("<grey>[<gold>Skript<grey>] <reset>");
   }

   public static void info(CommandSender sender, String info) {
      sender.sendMessage(TextComponentParser.instance().parseSafe(getSkriptPrefix() + info));
   }

   public static void broadcast(String message, String permission) {
      Bukkit.broadcast(TextComponentParser.instance().parseSafe(getSkriptPrefix() + message), permission);
   }

   public static void adminBroadcast(String message) {
      broadcast(message, "skript.admin");
   }

   public static void message(CommandSender sender, String info) {
      sender.sendMessage(TextComponentParser.instance().parseSafe(info));
   }

   public static void error(CommandSender sender, String error) {
      sender.sendMessage(TextComponentParser.instance().parseSafe(getSkriptPrefix() + "<dark_red>" + error));
   }

   @Nullable
   public SkriptUpdater getUpdater() {
      return this.updater;
   }

   static {
      if (!IS_STOPPING_EXISTS) {
         Server server = Bukkit.getServer();
         Class<?> clazz = server.getClass();

         Method serverMethod;
         try {
            serverMethod = clazz.getMethod("getServer");
         } catch (NoSuchMethodException var6) {
            throw new RuntimeException(var6);
         }

         try {
            MC_SERVER = serverMethod.invoke(server);
         } catch (InvocationTargetException | IllegalAccessException var5) {
            throw new RuntimeException(var5);
         }

         try {
            String isRunningMethod = "isRunning";
            if (isRunningMinecraft(1, 20, 5)) {
               isRunningMethod = "x";
            } else if (isRunningMinecraft(1, 20)) {
               isRunningMethod = "v";
            } else if (isRunningMinecraft(1, 19)) {
               isRunningMethod = "u";
            } else if (isRunningMinecraft(1, 18)) {
               isRunningMethod = "v";
            }

            IS_RUNNING = MC_SERVER.getClass().getMethod(isRunningMethod);
         } catch (NoSuchMethodException var4) {
            throw new RuntimeException(var4);
         }
      }

      UEH = new UncaughtExceptionHandler() {
         @Override
         public void uncaughtException(@Nullable Thread t, @Nullable Throwable e) {
            Skript.exception(e, "Exception in thread " + (t == null ? null : t.getName()));
         }
      };
      acceptRegistrations = true;
      addons = new HashSet<>();
      pluginPackages = new HashMap<>();
      checkedPlugins = false;
      tainted = false;
      errored = false;
      SKRIPT_PREFIX_MESSAGE = new Message("skript.prefix");
   }

   private class JoinUpdateNotificationListener implements Listener {
      @EventHandler
      public void onJoin(final PlayerJoinEvent event) {
         if (event.getPlayer().hasPermission("skript.admin")) {
            new Task(Skript.this, 0L) {
               @Override
               public void run() {
                  Player player = event.getPlayer();
                  SkriptUpdater updater = Skript.this.getUpdater();
                  if (updater != null && updater.getReleaseStatus() == ReleaseStatus.OUTDATED) {
                     UpdateManifest update = updater.getUpdateManifest();
                     if (update != null) {
                        Skript.info(player, SkriptUpdater.m_update_available.toString(update.id, Skript.getVersion()));
                        player.sendMessage(
                           TextComponentParser.instance()
                              .parse("Download it at: <aqua><underlined><click:open_url:" + update.downloadUrl + ">" + update.downloadUrl)
                        );
                     }
                  }
               }
            };
         }
      }
   }

   private static class ServerReloadListener implements Listener {
      @EventHandler
      public void onServerReload(ServerLoadEvent event) {
         if (event.getType() == LoadType.RELOAD) {
            for (OfflinePlayer player : Bukkit.getOperators()) {
               if (player.isOnline()) {
                  player.getPlayer().sendMessage(ChatColor.YELLOW + Skript.getWarningMessage());
                  player.getPlayer().sendMessage(ChatColor.YELLOW + Skript.getRestartMessage());
               }
            }

            Skript.warning(Skript.getWarningMessage());
            Skript.warning(Skript.getRestartMessage());
         }
      }
   }
}
