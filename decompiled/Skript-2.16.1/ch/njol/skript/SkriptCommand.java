package ch.njol.skript;

import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.command.CommandHelp;
import ch.njol.skript.doc.Documentation;
import ch.njol.skript.doc.HTMLGenerator;
import ch.njol.skript.doc.JSONGenerator;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RedirectingLogHandler;
import ch.njol.skript.log.TestingLogHandler;
import ch.njol.skript.log.TimingLogHandler;
import ch.njol.skript.test.runner.SkriptTestEvent;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.skript.test.runner.TestTracker;
import ch.njol.skript.test.utils.TestResults;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.OpenCloseable;
import ch.njol.util.StringUtils;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

public class SkriptCommand implements CommandExecutor {
   private static final String CONFIG_NODE = "skript command";
   private static final ArgsMessage m_reloading = new ArgsMessage("skript command.reload.reloading");
   private static final CommandHelp SKRIPT_COMMAND_HELP = new CommandHelp("<gray>/<gold>skript", SkriptColor.LIGHT_CYAN, "skript command.help")
      .add(new CommandHelp("reload", SkriptColor.DARK_RED).add("all").add("config").add("aliases").add("scripts").add("<script>"))
      .add(new CommandHelp("enable", SkriptColor.DARK_RED).add("all").add("<script>"))
      .add(new CommandHelp("disable", SkriptColor.DARK_RED).add("all").add("<script>"))
      .add(new CommandHelp("update", SkriptColor.DARK_RED).add("check").add("changes"))
      .add("list")
      .add("show")
      .add("info")
      .add("help");
   private static final ArgsMessage m_reloaded;
   private static final ArgsMessage m_reload_error;
   private static final ArgsMessage m_invalid_script;
   private static final ArgsMessage m_invalid_folder;

   private static void reloading(CommandSender sender, String what, RedirectingLogHandler logHandler, Object... args) {
      what = args.length == 0 ? Language.get("skript command.reload." + what) : Language.format("skript command.reload." + what, args);
      String message = StringUtils.fixCapitalization(m_reloading.toString(what));
      Skript.info(sender, message);
      String text = Language.format("skript command.reload.player reload", sender.getName(), what);
      logHandler.log(new LogEntry(Level.INFO, text), sender);
   }

   private static void reloaded(CommandSender sender, RedirectingLogHandler logHandler, TimingLogHandler timingLogHandler, String what, Object... args) {
      what = args.length == 0
         ? Language.get("skript command.reload." + what)
         : PluralizingArgsMessage.format(Language.format("skript command.reload." + what, args));
      String timeTaken = String.valueOf(timingLogHandler.getTimeTaken());
      if (logHandler.numErrors() == 0) {
         String message = StringUtils.fixCapitalization(PluralizingArgsMessage.format(m_reloaded.toString(what, timeTaken)));
         logHandler.log(new LogEntry(Level.INFO, message));
      } else {
         String message = StringUtils.fixCapitalization(PluralizingArgsMessage.format(m_reload_error.toString(what, logHandler.numErrors(), timeTaken)));
         logHandler.log(new LogEntry(Level.SEVERE, message));
      }
   }

   private static void info(CommandSender sender, String what, Object... args) {
      what = args.length == 0 ? Language.get("skript command." + what) : PluralizingArgsMessage.format(Language.format("skript command." + what, args));
      Skript.info(sender, StringUtils.fixCapitalization(what));
   }

   private static void error(CommandSender sender, String what, Object... args) {
      what = args.length == 0 ? Language.get("skript command." + what) : PluralizingArgsMessage.format(Language.format("skript command." + what, args));
      Skript.error(sender, StringUtils.fixCapitalization(what));
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!SKRIPT_COMMAND_HELP.test(sender, args)) {
         return true;
      } else {
         Set<CommandSender> recipients = new HashSet<>();
         recipients.add(sender);
         if (args[0].equalsIgnoreCase("reload")) {
            recipients.addAll(Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("skript.reloadnotify")).collect(Collectors.toSet()));
         }

         try {
            boolean errorCounter;
            try (
               RedirectingLogHandler logHandler = new RedirectingLogHandler(recipients, "").start();
               TimingLogHandler timingLogHandler = new TimingLogHandler().start();
            ) {
               if (!args[0].equalsIgnoreCase("reload")) {
                  if (args[0].equalsIgnoreCase("enable")) {
                     if (args[1].equalsIgnoreCase("all")) {
                        try {
                           info(sender, "enable.all.enabling");
                           ScriptLoader.loadScripts(toggleFiles(Skript.getInstance().getScriptsFolder(), true), logHandler).thenAccept(scriptInfo -> {
                              if (logHandler.numErrors() == 0) {
                                 info(sender, "enable.all.enabled");
                              } else {
                                 error(sender, "enable.all.error", logHandler.numErrors());
                              }
                           });
                        } catch (IOException var19) {
                           error(sender, "enable.all.io error", ExceptionUtils.toString(var19));
                        }

                        return true;
                     }

                     File scriptFile = getScriptFromArgs(sender, args);
                     if (scriptFile == null) {
                        return true;
                     }

                     if (scriptFile.isDirectory()) {
                        try {
                           scriptFiles = toggleFiles(scriptFile, true);
                        } catch (IOException var23) {
                           error(sender, "enable.folder.io error", scriptFile.getName(), ExceptionUtils.toString(var23));
                           return true;
                        }

                        if (scriptFiles.isEmpty()) {
                           info(sender, "enable.folder.empty", scriptFile.getName());
                           return true;
                        }

                        String fileName = scriptFile.getName();
                        info(sender, "enable.folder.enabling", fileName, scriptFiles.size());
                        ScriptLoader.loadScripts(scriptFiles, logHandler).thenAccept(scriptInfo -> {
                           if (logHandler.numErrors() == 0) {
                              info(sender, "enable.folder.enabled", fileName, scriptInfo.files);
                           } else {
                              error(sender, "enable.folder.error", fileName, logHandler.numErrors());
                           }
                        });
                        return true;
                     }

                     if (ScriptLoader.getLoadedScriptsFilter().accept(scriptFile)) {
                        info(sender, "enable.single.already enabled", scriptFile.getName(), StringUtils.join(args, " ", 1, args.length));
                        return true;
                     }

                     try {
                        scriptFile = toggleFile(scriptFile, true);
                     } catch (IOException var20) {
                        error(
                           sender,
                           "enable.single.io error",
                           scriptFile.getName().substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH),
                           ExceptionUtils.toString(var20)
                        );
                        return true;
                     }

                     String fileName = scriptFile.getName();
                     info(sender, "enable.single.enabling", fileName);
                     ScriptLoader.loadScripts(scriptFile, logHandler).thenAccept(scriptInfo -> {
                        if (logHandler.numErrors() == 0) {
                           info(sender, "enable.single.enabled", fileName);
                        } else {
                           error(sender, "enable.single.error", fileName, logHandler.numErrors());
                        }
                     });
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("disable")) {
                     if (args[1].equalsIgnoreCase("all")) {
                        ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());

                        try {
                           toggleFiles(Skript.getInstance().getScriptsFolder(), false);
                           info(sender, "disable.all.disabled");
                        } catch (IOException var18) {
                           error(sender, "disable.all.io error", ExceptionUtils.toString(var18));
                        }

                        return true;
                     }

                     File scriptFilex = getScriptFromArgs(sender, args);
                     if (scriptFilex == null) {
                        return true;
                     }

                     if (scriptFilex.isDirectory()) {
                        ScriptLoader.unloadScripts(ScriptLoader.getScripts(scriptFilex));

                        try {
                           scripts = toggleFiles(scriptFilex, false);
                        } catch (IOException var22) {
                           error(sender, "disable.folder.io error", scriptFilex.getName(), ExceptionUtils.toString(var22));
                           return true;
                        }

                        if (scripts.isEmpty()) {
                           info(sender, "disable.folder.empty", scriptFilex.getName());
                           return true;
                        }

                        info(sender, "disable.folder.disabled", scriptFilex.getName(), scripts.size());
                        return true;
                     }

                     if (ScriptLoader.getDisabledScriptsFilter().accept(scriptFilex)) {
                        info(sender, "disable.single.already disabled", scriptFilex.getName().substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH));
                        return true;
                     }

                     Script script = ScriptLoader.getScript(scriptFilex);
                     if (script != null) {
                        ScriptLoader.unloadScript(script);
                     }

                     String fileName = scriptFilex.getName();

                     try {
                        toggleFile(scriptFilex, false);
                     } catch (IOException var21) {
                        error(sender, "disable.single.io error", scriptFilex.getName(), ExceptionUtils.toString(var21));
                        return true;
                     }

                     info(sender, "disable.single.disabled", fileName);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("update")) {
                     SkriptUpdater updater = Skript.getInstance().getUpdater();
                     if (updater != null) {
                        if (args[1].equalsIgnoreCase("check")) {
                           updater.updateCheck(sender);
                        } else if (args[1].equalsIgnoreCase("changes")) {
                           updater.changesCheck(sender);
                           return true;
                        }

                        return true;
                     }

                     Skript.info(sender, SkriptUpdater.m_internal_error + "");
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("info")) {
                     info(sender, "info.aliases");
                     info(sender, "info.documentation");
                     info(sender, "info.tutorials");
                     info(sender, "info.server", Bukkit.getVersion());
                     SkriptUpdater updater = Skript.getInstance().getUpdater();
                     if (updater != null) {
                        info(sender, "info.version", Skript.getVersion() + " (" + updater.getCurrentRelease().flavor + ")");
                     } else {
                        info(sender, "info.version", Skript.getVersion());
                     }

                     Collection<org.skriptlang.skript.addon.SkriptAddon> addons = Skript.instance().addons();
                     info(sender, "info.addons", addons.isEmpty() ? "None" : "");

                     for (org.skriptlang.skript.addon.SkriptAddon addon : addons) {
                        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(addon.source());
                        PluginDescriptionFile desc = plugin.getDescription();
                        String web = desc.getWebsite();
                        Skript.info(sender, " - " + desc.getFullName() + (web != null ? " (" + web + ")" : ""));
                     }

                     List<String> dependencies = Skript.getInstance().getDescription().getSoftDepend();
                     boolean dependenciesFound = false;

                     for (String dep : dependencies) {
                        Plugin plugin = Bukkit.getPluginManager().getPlugin(dep);
                        if (plugin != null) {
                           if (!dependenciesFound) {
                              dependenciesFound = true;
                              info(sender, "info.dependencies", "");
                           }

                           String ver = plugin.getDescription().getVersion();
                           Skript.info(sender, " - " + plugin.getName() + " v" + ver);
                        }
                     }

                     if (!dependenciesFound) {
                        info(sender, "info.dependencies", "None");
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("gen-docs")) {
                     File templateDir = Documentation.getDocsTemplateDirectory();
                     File outputDir = Documentation.getDocsOutputDirectory();
                     outputDir.mkdirs();
                     Skript.info(sender, "Generating docs...");
                     JSONGenerator.of(Skript.instance()).generate(outputDir.toPath().resolve("docs.json"));
                     if (templateDir.exists()) {
                        HTMLGenerator htmlGenerator = new HTMLGenerator(templateDir, outputDir);
                        htmlGenerator.generate();
                        Skript.info(sender, "All documentation generated!");
                        return true;
                     }

                     Skript.info(sender, "JSON-only documentation generated!");
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("test") && TestMode.DEV_MODE) {
                     File scriptFilexx;
                     if (args.length == 1) {
                        scriptFilexx = TestMode.lastTestFile;
                        if (scriptFilexx == null) {
                           Skript.error(sender, "No test script has been run yet!");
                           return true;
                        }
                     } else if (args[1].equalsIgnoreCase("all")) {
                        scriptFilexx = TestMode.TEST_DIR.toFile();
                     } else {
                        scriptFilexx = getScriptFromArgs(sender, args, TestMode.TEST_DIR.toFile());
                        TestMode.lastTestFile = scriptFilexx;
                     }

                     if (scriptFilexx != null && scriptFilexx.exists()) {
                        timingLogHandler.close();
                        logHandler.close();
                        TestingLogHandler errorCounterx = new TestingLogHandler(Level.SEVERE).start();
                        ScriptLoader.loadScripts(scriptFilexx, errorCounterx)
                           .thenAccept(scriptInfo -> Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> {
                              Bukkit.getPluginManager().callEvent(new SkriptTestEvent());
                              ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());
                              TestResults testResults = TestTracker.collectResults();
                              String[] lines = testResults.createReport().split("\n");

                              for (String line : lines) {
                                 Skript.info(sender, line);
                              }

                              Skript.info(sender, "Collecting results to " + TestMode.RESULTS_FILE);
                              String results = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(testResults);

                              try {
                                 Files.writeString(TestMode.RESULTS_FILE, results);
                              } catch (IOException var7x) {
                                 Skript.exception(var7x, "Failed to write test results.");
                              }
                           }));
                        return true;
                     }

                     Skript.error(sender, "Test script doesn't exist!");
                     return true;
                  }

                  if (!args[0].equalsIgnoreCase("list") && !args[0].equalsIgnoreCase("show")) {
                     if (args[0].equalsIgnoreCase("help")) {
                        SKRIPT_COMMAND_HELP.showHelp(sender);
                     }

                     return true;
                  }

                  info(sender, "list.enabled.header");
                  ScriptLoader.getLoadedScripts()
                     .stream()
                     .map(script -> script.getConfig().getFileName())
                     .forEach(name -> info(sender, "list.enabled.element", name));
                  info(sender, "list.disabled.header");
                  ScriptLoader.getDisabledScripts()
                     .stream()
                     .flatMap(file -> file.isDirectory() ? getSubFiles(file).stream() : Arrays.stream(new File[]{file}))
                     .map(File::getPath)
                     .map(path -> path.substring(Skript.getInstance().getScriptsFolder().getPath().length() + 1))
                     .forEach(path -> info(sender, "list.disabled.element", path));
                  return true;
               }

               if (args[1].equalsIgnoreCase("all")) {
                  reloading(sender, "config, aliases and scripts", logHandler);
                  SkriptConfig.load();
                  Aliases.clear();
                  Aliases.loadAsync().thenRun(() -> {
                     ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());
                     ScriptLoader.loadScripts(Skript.getInstance().getScriptsFolder(), OpenCloseable.combine(logHandler, timingLogHandler))
                        .thenAccept(info -> {
                           if (info.files == 0) {
                              Skript.warning(Skript.m_no_scripts.toString());
                           }

                           reloaded(sender, logHandler, timingLogHandler, "config, aliases and scripts");
                        });
                  });
                  return true;
               }

               if (args[1].equalsIgnoreCase("scripts")) {
                  reloading(sender, "scripts", logHandler);
                  ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());
                  ScriptLoader.loadScripts(Skript.getInstance().getScriptsFolder(), OpenCloseable.combine(logHandler, timingLogHandler)).thenAccept(info -> {
                     if (info.files == 0) {
                        Skript.warning(Skript.m_no_scripts.toString());
                     }

                     reloaded(sender, logHandler, timingLogHandler, "scripts");
                  });
                  return true;
               }

               if (args[1].equalsIgnoreCase("config")) {
                  reloading(sender, "main config", logHandler);
                  SkriptConfig.load();
                  reloaded(sender, logHandler, timingLogHandler, "main config");
                  return true;
               }

               if (args[1].equalsIgnoreCase("aliases")) {
                  reloading(sender, "aliases", logHandler);
                  Aliases.clear();
                  Aliases.loadAsync().thenRun(() -> reloaded(sender, logHandler, timingLogHandler, "aliases"));
                  return true;
               }

               File scriptFilexxx = getScriptFromArgs(sender, args);
               if (scriptFilexxx != null) {
                  if (scriptFilexxx.isDirectory()) {
                     String fileName = scriptFilexxx.getName();
                     reloading(sender, "scripts in folder", logHandler, fileName);
                     ScriptLoader.unloadScripts(ScriptLoader.getScripts(scriptFilexxx));
                     ScriptLoader.loadScripts(scriptFilexxx, OpenCloseable.combine(logHandler, timingLogHandler)).thenAccept(scriptInfo -> {
                        if (scriptInfo.files == 0) {
                           info(sender, "reload.empty folder", fileName);
                        } else if (logHandler.numErrors() == 0) {
                           reloaded(sender, logHandler, timingLogHandler, "x scripts in folder success", fileName, scriptInfo.files);
                        } else {
                           reloaded(sender, logHandler, timingLogHandler, "x scripts in folder error", fileName, scriptInfo.files);
                        }
                     });
                     return true;
                  }

                  if (ScriptLoader.getDisabledScriptsFilter().accept(scriptFilexxx)) {
                     info(
                        sender,
                        "reload.script disabled",
                        scriptFilexxx.getName().substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH),
                        StringUtils.join(args, " ", 1, args.length)
                     );
                     return true;
                  }

                  reloading(sender, "script", logHandler, scriptFilexxx.getName());
                  Script script = ScriptLoader.getScript(scriptFilexxx);
                  if (script != null) {
                     ScriptLoader.unloadScript(script);
                  }

                  ScriptLoader.loadScripts(scriptFilexxx, OpenCloseable.combine(logHandler, timingLogHandler))
                     .thenAccept(scriptInfo -> reloaded(sender, logHandler, timingLogHandler, "script", scriptFile.getName()));
                  return true;
               }

               errorCounter = true;
            }

            return errorCounter;
         } catch (Exception var26) {
            Skript.exception(var26, "Exception occurred in Skript's main command", "Used command: /" + label + " " + StringUtils.join(args, " "));
            return true;
         }
      }
   }

   private static List<File> getSubFiles(File file) {
      List<File> files = new ArrayList<>();
      if (file.isDirectory()) {
         for (File listFile : file.listFiles(f -> !f.isHidden())) {
            if (listFile.isDirectory()) {
               files.addAll(getSubFiles(listFile));
            } else if (listFile.getName().endsWith(".sk")) {
               files.add(listFile);
            }
         }
      }

      return files;
   }

   @Nullable
   private static File getScriptFromArgs(CommandSender sender, String[] args) {
      return getScriptFromArgs(sender, args, Skript.getInstance().getScriptsFolder());
   }

   @Nullable
   private static File getScriptFromArgs(CommandSender sender, String[] args, File directoryFile) {
      String script = StringUtils.join(args, " ", 1, args.length);
      File f = ScriptLoader.getScriptFromName(script, directoryFile);
      if (f != null) {
         return f;
      } else {
         boolean directory = script.endsWith("/") || script.endsWith("\\") || script.endsWith(File.separator);
         Skript.error(sender, (directory ? m_invalid_folder : m_invalid_script).toString(script));
         return null;
      }
   }

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   @Nullable
   public static File getScriptFromName(String script) {
      return ScriptLoader.getScriptFromName(script);
   }

   private static File toggleFile(File file, boolean enable) throws IOException {
      return enable
         ? FileUtils.move(file, new File(file.getParentFile(), file.getName().substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH)), false)
         : FileUtils.move(file, new File(file.getParentFile(), "-" + file.getName()), false);
   }

   private static Set<File> toggleFiles(File folder, boolean enable) throws IOException {
      FileFilter filter = enable ? ScriptLoader.getDisabledScriptsFilter() : ScriptLoader.getLoadedScriptsFilter();
      Set<File> changed = new HashSet<>();

      for (File file : folder.listFiles()) {
         if (file.isDirectory()) {
            changed.addAll(toggleFiles(file, enable));
         } else if (filter.accept(file)) {
            String fileName = file.getName();
            changed.add(
               FileUtils.move(
                  file, new File(file.getParentFile(), enable ? fileName.substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH) : "-" + fileName), false
               )
            );
         }
      }

      return changed;
   }

   static {
      if (TestMode.GEN_DOCS || Documentation.isDocsTemplateFound()) {
         SKRIPT_COMMAND_HELP.add("gen-docs");
      }

      if (TestMode.DEV_MODE) {
         SKRIPT_COMMAND_HELP.add("test");
      }

      m_reloaded = new ArgsMessage("skript command.reload.reloaded");
      m_reload_error = new ArgsMessage("skript command.reload.error");
      m_invalid_script = new ArgsMessage("skript command.invalid script");
      m_invalid_folder = new ArgsMessage("skript command.invalid folder");
   }
}
