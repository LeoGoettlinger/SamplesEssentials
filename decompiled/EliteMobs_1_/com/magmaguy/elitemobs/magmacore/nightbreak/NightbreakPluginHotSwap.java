package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public final class NightbreakPluginHotSwap {
   public static final String APPLY_MODE_CONFIG_PATH = "nightbreak.applyPluginUpdatesWithoutRestart";
   public static final String MODE_NEVER = "never";
   public static final String MODE_WHEN_EMPTY = "when-empty";
   public static final String MODE_IMMEDIATELY = "immediately";
   static final List<String> APPLY_MODE_CONFIG_COMMENTS = List.of(
      "Applies a downloaded plugin update without restarting the server.",
      "never: wait for the next restart (default).",
      "when-empty: apply it the next time no players are online.",
      "immediately: apply it as soon as it is downloaded. Bosses, arenas and",
      "dungeon instances in progress end when the plugin reloads.",
      "Plugins from other authors that depend on this plugin block the swap."
   );
   private static final String WORK_DIRECTORY = ".nightbreak-hotswap";
   private static final String ROUTINE_DIRECTORY_PREFIX = "routine-";
   private static final String OWN_MAIN_CLASS_PREFIX = "com.magmaguy.";
   private static final long WHEN_EMPTY_CHECK_TICKS = 600L;
   private static final Map<String, String> RESTART_ONLY = new ConcurrentHashMap<>();

   private NightbreakPluginHotSwap() {
   }

   public static void requireRestartForUpdates(JavaPlugin plugin, String reason) {
      RESTART_ONLY.put(plugin.getName(), reason);
   }

   static boolean offersRestartFreeUpdates(JavaPlugin plugin) {
      return !RESTART_ONLY.containsKey(plugin.getName());
   }

   public static boolean hasStagedUpdate(JavaPlugin plugin) {
      File pluginJar = findPluginJar(plugin);
      return pluginJar != null && new File(NightbreakPluginUpdater.updateFolder(plugin), pluginJar.getName()).isFile();
   }

   public static boolean applyStagedUpdate(JavaPlugin plugin, CommandSender sender) {
      NightbreakPluginHotSwap.PlanResult result = plan(plugin);
      if (result.plan() == null) {
         if (sender != null) {
            Logger.sendSimpleMessage(sender, "&c" + result.refusal());
         }

         return false;
      } else {
         NightbreakPluginHotSwap.Plan plan = result.plan();
         if (System.getProperty("nightbreak.pluginHotSwap.active") != null) {
            if (sender != null) {
               Logger.sendSimpleMessage(sender, "&cAnother plugin swap is already in progress.");
            }

            return false;
         } else {
            Runnable routine;
            try {
               routine = createRoutine(plan, sender);
            } catch (IOException | RuntimeException | ReflectiveOperationException var7) {
               plugin.getLogger().warning("Could not prepare the plugin swap: " + var7);
               if (sender != null) {
                  Logger.sendSimpleMessage(sender, "&cCould not prepare the plugin swap. Check the console.");
               }

               return false;
            }

            System.setProperty("nightbreak.pluginHotSwap.active", plan.targetName());

            try {
               Bukkit.getScheduler().runTask(plugin, routine);
            } catch (RuntimeException var6) {
               System.clearProperty("nightbreak.pluginHotSwap.active");
               throw var6;
            }

            if (sender != null) {
               Logger.sendSimpleMessage(
                  sender, "&7Applying " + plan.targetName() + " " + plan.stagedVersion() + " over " + plan.runningVersion() + " without a restart..."
               );
               if (plan.loadOrder().size() > 1) {
                  Logger.sendSimpleMessage(
                     sender, "&7These plugins depend on it and will reload too: &f" + String.join(", ", plan.loadOrder().subList(1, plan.loadOrder().size()))
                  );
               }
            }

            return true;
         }
      }
   }

   static boolean applyDownloadedUpdate(JavaPlugin plugin, CommandSender sender) {
      String mode = applyMode(plugin);
      if ("never".equals(mode)) {
         return false;
      } else {
         CommandSender recipient = (CommandSender)(sender == null ? Bukkit.getConsoleSender() : sender);
         if (!"immediately".equals(mode) && !Bukkit.getOnlinePlayers().isEmpty()) {
            NightbreakPluginHotSwap.PlanResult result = plan(plugin);
            if (result.plan() == null) {
               Logger.sendSimpleMessage(recipient, "&c" + result.refusal());
               return false;
            } else {
               Logger.sendSimpleMessage(recipient, "&7The plugin update will be applied the next time no players are online.");
               int[] taskId = new int[]{-1};
               taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                  if (Bukkit.getOnlinePlayers().isEmpty()) {
                     Bukkit.getScheduler().cancelTask(taskId[0]);
                     applyStagedUpdate(plugin, Bukkit.getConsoleSender());
                  }
               }, 600L, 600L);
               return true;
            }
         } else {
            return applyStagedUpdate(plugin, recipient);
         }
      }
   }

   static String applyMode(JavaPlugin plugin) {
      if (!offersRestartFreeUpdates(plugin)) {
         return "never";
      } else {
         File configFile = new File(plugin.getDataFolder(), "config.yml");
         if (!configFile.exists()) {
            return "never";
         } else {
            String mode = YamlConfiguration.loadConfiguration(configFile)
               .getString("nightbreak.applyPluginUpdatesWithoutRestart", "never")
               .trim()
               .toLowerCase(Locale.ROOT);

            return switch (mode) {
               case "when-empty", "immediately" -> mode;
               default -> "never";
            };
         }
      }
   }

   static NightbreakPluginHotSwap.PlanResult plan(JavaPlugin plugin) {
      if (!Bukkit.isPrimaryThread()) {
         return NightbreakPluginHotSwap.PlanResult.refused("Plugin swaps must start on the server thread.");
      } else {
         String restartReason = RESTART_ONLY.get(plugin.getName());
         if (restartReason != null) {
            return NightbreakPluginHotSwap.PlanResult.refused(plugin.getName() + " updates need a server restart: " + restartReason);
         } else {
            File pluginJar = findPluginJar(plugin);
            if (pluginJar == null) {
               return NightbreakPluginHotSwap.PlanResult.refused("Could not find the jar " + plugin.getName() + " was loaded from.");
            } else {
               File stagedJar = new File(NightbreakPluginUpdater.updateFolder(plugin), pluginJar.getName());
               if (!stagedJar.isFile()) {
                  return NightbreakPluginHotSwap.PlanResult.refused("No downloaded update is waiting for " + plugin.getName() + ".");
               } else {
                  PluginDescriptionFile stagedDescription = readDescription(stagedJar);
                  if (stagedDescription == null) {
                     return NightbreakPluginHotSwap.PlanResult.refused("The staged update for " + plugin.getName() + " has no readable plugin.yml.");
                  } else {
                     DownloadedPluginJarValidator.ValidationResult validation = DownloadedPluginJarValidator.validate(
                        stagedJar, List.of(plugin.getName()), stagedDescription.getVersion()
                     );
                     if (!validation.valid()) {
                        return NightbreakPluginHotSwap.PlanResult.refused("The staged update is not valid: " + validation.detail());
                     } else {
                        String runningVersion = plugin.getDescription().getVersion();
                        if (NightbreakPluginUpdater.compareVersions(stagedDescription.getVersion(), runningVersion) <= 0) {
                           return NightbreakPluginHotSwap.PlanResult.refused(
                              "The staged update (" + stagedDescription.getVersion() + ") is not newer than the running version (" + runningVersion + ")."
                           );
                        } else {
                           Plugin[] serverOrder = Bukkit.getPluginManager().getPlugins();
                           Set<String> members = new LinkedHashSet<>();
                           members.add(plugin.getName());
                           List<String> foreignDependents = new ArrayList<>();
                           boolean grew = true;

                           while (grew) {
                              grew = false;

                              for (Plugin candidate : serverOrder) {
                                 if (candidate.isEnabled() && !members.contains(candidate.getName()) && dependsOnAny(candidate, members)) {
                                    if (!candidate.getDescription().getMain().startsWith("com.magmaguy.")) {
                                       if (!foreignDependents.contains(candidate.getName())) {
                                          foreignDependents.add(candidate.getName());
                                       }
                                    } else {
                                       members.add(candidate.getName());
                                       grew = true;
                                    }
                                 }
                              }
                           }

                           if (!foreignDependents.isEmpty()) {
                              return NightbreakPluginHotSwap.PlanResult.refused(
                                 "These plugins depend on "
                                    + plugin.getName()
                                    + " and may not survive a reload: "
                                    + String.join(", ", foreignDependents)
                                    + ". Restart the server to apply the update."
                              );
                           } else {
                              List<String> loadOrder = new ArrayList<>();
                              List<File> loadFiles = new ArrayList<>();
                              loadOrder.add(plugin.getName());
                              loadFiles.add(pluginJar);

                              for (Plugin candidatex : serverOrder) {
                                 if (candidatex != plugin && members.contains(candidatex.getName())) {
                                    File candidateJar = candidatex instanceof JavaPlugin javaPlugin ? findPluginJar(javaPlugin) : null;
                                    if (candidateJar == null) {
                                       return NightbreakPluginHotSwap.PlanResult.refused("Could not find the jar " + candidatex.getName() + " was loaded from.");
                                    }

                                    loadOrder.add(candidatex.getName());
                                    loadFiles.add(candidateJar);
                                 }
                              }

                              List<File> heldBack = new ArrayList<>();
                              File[] staged = stagedJar.getParentFile().listFiles();
                              if (staged != null) {
                                 for (File file : staged) {
                                    if (!file.equals(stagedJar) && file.getName().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                                       PluginDescriptionFile description = readDescription(file);
                                       if (description != null && members.contains(description.getName())) {
                                          heldBack.add(file);
                                       }
                                    }
                                 }
                              }

                              File workDirectory = new File(pluginJar.getParentFile(), ".nightbreak-hotswap");
                              return new NightbreakPluginHotSwap.PlanResult(
                                 new NightbreakPluginHotSwap.Plan(
                                    plugin.getName(),
                                    runningVersion,
                                    stagedDescription.getVersion(),
                                    stagedJar,
                                    workDirectory,
                                    List.copyOf(loadOrder),
                                    List.copyOf(loadFiles),
                                    List.copyOf(heldBack)
                                 ),
                                 null
                              );
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean dependsOnAny(Plugin candidate, Set<String> names) {
      PluginDescriptionFile description = candidate.getDescription();

      for (String dependency : description.getDepend()) {
         if (names.contains(dependency)) {
            return true;
         }
      }

      for (String dependencyx : description.getSoftDepend()) {
         if (names.contains(dependencyx)) {
            return true;
         }
      }

      return false;
   }

   private static Runnable createRoutine(NightbreakPluginHotSwap.Plan plan, CommandSender sender) throws IOException, ReflectiveOperationException {
      String className = PluginHotSwapRoutine.class.getName();

      byte[] bytecode;
      try (InputStream input = NightbreakPluginHotSwap.class.getClassLoader().getResourceAsStream(className.replace('.', '/') + ".class")) {
         if (input == null) {
            throw new IOException("Missing " + className + " bytecode.");
         }

         bytecode = input.readAllBytes();
      }

      deleteStaleRoutines(plan.workDirectory());
      Path routineDirectory = plan.workDirectory().toPath().resolve("routine-" + UUID.randomUUID());
      Path classFile = routineDirectory.resolve(className.replace('.', '/') + ".class");
      Files.createDirectories(classFile.getParent());
      Files.write(classFile, bytecode);
      URLClassLoader classLoader = new URLClassLoader(new URL[]{routineDirectory.toUri().toURL()}, Bukkit.class.getClassLoader());
      Class<?> routineClass = Class.forName(className, true, classLoader);
      List<String> loadOrder = plan.loadOrder();
      String[] unloadOrder = new String[loadOrder.size()];

      for (int i = 0; i < loadOrder.size(); i++) {
         unloadOrder[i] = loadOrder.get(loadOrder.size() - 1 - i);
      }

      return (Runnable)routineClass.getConstructor(
            String.class, String[].class, String[].class, File[].class, File.class, File[].class, File.class, File.class, CommandSender.class
         )
         .newInstance(
            plan.targetName(),
            unloadOrder,
            loadOrder.toArray(new String[0]),
            plan.loadFiles().toArray(new File[0]),
            plan.stagedJar(),
            plan.heldBack().toArray(new File[0]),
            plan.workDirectory(),
            routineDirectory.toFile(),
            sender
         );
   }

   private static void deleteStaleRoutines(File workDirectory) {
      File[] entries = workDirectory.listFiles();
      if (entries != null) {
         for (File entry : entries) {
            if (entry.isDirectory() && entry.getName().startsWith("routine-")) {
               PluginHotSwapRoutine.deleteRecursively(entry);
            }
         }
      }
   }

   static File findPluginJar(JavaPlugin plugin) {
      File pluginsFolder = plugin.getDataFolder().getParentFile();
      if (pluginsFolder == null) {
         return null;
      } else {
         String codeSourceName = codeSourceName(plugin);
         if (codeSourceName != null) {
            File candidate = new File(pluginsFolder, codeSourceName);
            if (declaresPlugin(candidate, plugin.getName())) {
               return candidate;
            }
         }

         File[] files = pluginsFolder.listFiles();
         if (files == null) {
            return null;
         } else {
            for (File file : files) {
               if (file.getName().toLowerCase(Locale.ROOT).endsWith(".jar") && declaresPlugin(file, plugin.getName())) {
                  return file;
               }
            }

            return null;
         }
      }
   }

   private static String codeSourceName(JavaPlugin plugin) {
      try {
         URL location = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
         return new File(location.toURI()).getName();
      } catch (Exception var2) {
         return null;
      }
   }

   private static boolean declaresPlugin(File jar, String pluginName) {
      if (!jar.isFile()) {
         return false;
      } else {
         PluginDescriptionFile description = readDescription(jar);
         return description != null && description.getName().equals(pluginName);
      }
   }

   private static PluginDescriptionFile readDescription(File jar) {
      try {
         PluginDescriptionFile var4;
         try (JarFile jarFile = new JarFile(jar)) {
            JarEntry entry = jarFile.getJarEntry("plugin.yml");
            if (entry == null) {
               return null;
            }

            try (InputStream input = jarFile.getInputStream(entry)) {
               var4 = new PluginDescriptionFile(input);
            }
         }

         return var4;
      } catch (InvalidDescriptionException | RuntimeException | IOException var10) {
         return null;
      }
   }

   record Plan(
      String targetName,
      String runningVersion,
      String stagedVersion,
      File stagedJar,
      File workDirectory,
      List<String> loadOrder,
      List<File> loadFiles,
      List<File> heldBack
   ) {
   }

   record PlanResult(NightbreakPluginHotSwap.Plan plan, String refusal) {
      static NightbreakPluginHotSwap.PlanResult refused(String refusal) {
         return new NightbreakPluginHotSwap.PlanResult(null, refusal);
      }
   }
}
