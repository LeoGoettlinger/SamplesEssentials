package ch.njol.skript;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.events.bukkit.PreScriptLoadEvent;
import ch.njol.skript.lang.ExecutionIntent;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.CountingLogHandler;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.structures.StructOptions;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.HintManager;
import ch.njol.util.OpenCloseable;
import ch.njol.util.StringUtils;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptWarning;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.util.event.Event;
import org.skriptlang.skript.util.event.EventRegistry;

public class ScriptLoader {
   public static final String DISABLED_SCRIPT_PREFIX = "-";
   public static final int DISABLED_SCRIPT_PREFIX_LENGTH = "-".length();
   private static final Set<Script> loadedScripts = Collections.synchronizedSortedSet(new TreeSet<>(new Comparator<Script>() {
      public int compare(Script s1, Script s2) {
         File f1 = s1.getConfig().getFile();
         File f2 = s2.getConfig().getFile();
         if (f1 != null && f2 != null) {
            File f1Parent = f1.getParentFile();
            File f2Parent = f2.getParentFile();
            if (this.isSubDir(f1Parent, f2Parent)) {
               return -1;
            } else {
               return this.isSubDir(f2Parent, f1Parent) ? 1 : f1.compareTo(f2);
            }
         } else {
            throw new IllegalArgumentException("Scripts will null config files cannot be sorted.");
         }
      }

      private boolean isSubDir(File directory, File subDir) {
         for (File parentDir = directory.getParentFile(); parentDir != null; parentDir = parentDir.getParentFile()) {
            if (subDir.equals(parentDir)) {
               return true;
            }
         }

         return false;
      }
   }));
   private static final FileFilter loadedScriptFilter = f -> f != null
      && (f.isDirectory() && !f.getName().startsWith(".") || !f.isDirectory() && StringUtils.endsWithIgnoreCase(f.getName(), ".sk"))
      && !f.getName().startsWith("-")
      && !f.isHidden();
   private static final Set<File> disabledScripts = Collections.synchronizedSet(new HashSet<>());
   private static final FileFilter disabledScriptFilter = f -> f != null
      && (f.isDirectory() && !f.getName().startsWith(".") || !f.isDirectory() && StringUtils.endsWithIgnoreCase(f.getName(), ".sk"))
      && f.getName().startsWith("-")
      && !f.isHidden();
   private static final BlockingQueue<Runnable> loadQueue = new LinkedBlockingQueue<>();
   private static final ThreadGroup asyncLoaderThreadGroup = new ThreadGroup("Skript async loaders");
   private static final List<ScriptLoader.AsyncLoaderThread> loaderThreads = new ArrayList<>();
   private static int asyncLoaderSize;
   private static Executor executor;
   private static final Set<File> trackedFiles = new HashSet<>();
   private static final EventRegistry<ScriptLoader.LoaderEvent> eventRegistry = new EventRegistry<>();

   private static ParserInstance getParser() {
      return ParserInstance.get();
   }

   @Nullable
   public static Script getScript(File file) {
      if (!file.isFile()) {
         throw new IllegalArgumentException("Something other than a file was provided.");
      } else {
         try {
            file = file.getCanonicalFile();
         } catch (IOException var3) {
            Skript.exception(var3, "An exception occurred while trying to get the canonical file of: " + file);
            return null;
         }

         for (Script script : loadedScripts) {
            if (file.equals(script.getConfig().getFile())) {
               return script;
            }
         }

         return null;
      }
   }

   public static Set<Script> getScripts(File directory) {
      if (!directory.isDirectory()) {
         throw new IllegalArgumentException("Something other than a directory was provided.");
      } else {
         Set<Script> scripts = new HashSet<>();

         for (File file : directory.listFiles(loadedScriptFilter)) {
            if (file.isDirectory()) {
               scripts.addAll(getScripts(file));
            } else {
               Script script = getScript(file);
               if (script != null) {
                  scripts.add(script);
               }
            }
         }

         return scripts;
      }
   }

   static void updateDisabledScripts(Path path) {
      disabledScripts.clear();

      try (Stream<Path> files = Files.walk(path)) {
         files.map(Path::toFile).filter(disabledScriptFilter::accept).forEach(disabledScripts::add);
      } catch (Exception var6) {
         Skript.exception(var6, "An error occurred while trying to update the list of disabled scripts!");
      }
   }

   public static boolean isAsync() {
      return asyncLoaderSize > 0;
   }

   public static boolean isParallel() {
      return asyncLoaderSize > 1;
   }

   @UnknownNullability
   public static Executor getExecutor() {
      return executor;
   }

   public static void setAsyncLoaderSize(int size) throws IllegalStateException {
      asyncLoaderSize = size;
      if (size <= 0) {
         for (ScriptLoader.AsyncLoaderThread thread : loaderThreads) {
            thread.cancelExecution();
         }
      } else {
         while (loaderThreads.size() > size) {
            ScriptLoader.AsyncLoaderThread thread = loaderThreads.removeLast();
            thread.cancelExecution();
         }

         while (loaderThreads.size() < size) {
            loaderThreads.add(ScriptLoader.AsyncLoaderThread.create());
         }

         if (loaderThreads.size() != size) {
            throw new IllegalStateException();
         } else {
            executor = Executors.newFixedThreadPool(asyncLoaderSize, new ThreadFactory() {
               private final AtomicInteger threadId = new AtomicInteger(0);

               @Override
               public Thread newThread(@NotNull Runnable runnable) {
                  Thread thread = new Thread(ScriptLoader.asyncLoaderThreadGroup, runnable, "Skript async loaders thread " + this.threadId.incrementAndGet());
                  thread.setDaemon(true);
                  return thread;
               }
            });
         }
      }
   }

   private static <T> CompletableFuture<T> makeFuture(Supplier<T> supplier, OpenCloseable openCloseable) {
      CompletableFuture<T> future = new CompletableFuture<>();
      Runnable task = () -> {
         try {
            openCloseable.open();

            T t;
            try {
               t = supplier.get();
            } finally {
               openCloseable.close();
            }

            future.complete(t);
         } catch (Throwable var8) {
            future.completeExceptionally(var8);
            Skript.exception(var8);
         }
      };
      if (isAsync() && Bukkit.isPrimaryThread()) {
         loadQueue.add(task);
      } else {
         task.run();

         assert future.isDone();
      }

      return future;
   }

   private static boolean track(File file) {
      return !trackedFiles.add(file);
   }

   private static void untrack(Script script) {
      trackedFiles.remove(script.getConfig().getFile());
   }

   public static CompletableFuture<ScriptLoader.ScriptInfo> loadScripts(File file, OpenCloseable openCloseable) {
      return loadScripts(loadStructures(file), openCloseable);
   }

   public static CompletableFuture<ScriptLoader.ScriptInfo> loadScripts(Set<File> files, OpenCloseable openCloseable) {
      return loadScripts(files.stream().sorted().map(ScriptLoader::loadStructures).flatMap(Collection::stream).collect(Collectors.toList()), openCloseable);
   }

   private static CompletableFuture<ScriptLoader.ScriptInfo> loadScripts(List<Config> configs, OpenCloseable openCloseable) {
      if (configs.isEmpty()) {
         return CompletableFuture.completedFuture(new ScriptLoader.ScriptInfo());
      } else {
         record LoadingStructure(ScriptLoader.LoadingScriptInfo loadingScriptInfo, Structure structure) {
         }

         eventRegistry().events(ScriptLoader.ScriptPreInitEvent.class).forEach(event -> event.onPreInit(configs));
         Bukkit.getPluginManager().callEvent(new PreScriptLoadEvent(configs));
         ScriptLoader.ScriptInfo scriptInfo = new ScriptLoader.ScriptInfo();
         List<ScriptLoader.LoadingScriptInfo> scripts = new ArrayList<>();
         List<CompletableFuture<Void>> scriptInfoFutures = new ArrayList<>();

         for (Config config : configs) {
            if (config == null) {
               throw new NullPointerException();
            }

            CompletableFuture<Void> future = makeFuture(() -> {
               ScriptLoader.LoadingScriptInfo info = loadScript(config);
               scripts.add(info);
               scriptInfo.add(new ScriptLoader.ScriptInfo(1, info.structures.size()));
               return null;
            }, openCloseable);
            scriptInfoFutures.add(future);
         }

         return CompletableFuture.allOf(scriptInfoFutures.toArray(new CompletableFuture[0]))
            .thenApply(
               ignored -> {
                  ParserInstance parser = getParser();

                  ScriptLoader.ScriptInfo var6x;
                  try {
                     openCloseable.open();
                     List<LoadingStructure> loadingStructures = scripts.stream()
                        .flatMap(infox -> infox.structures.stream().map(structure -> new LoadingStructure(infox, structure)))
                        .sorted(Comparator.comparing(pair -> pair.structure().getPriority()))
                        .collect(Collectors.toCollection(ArrayList::new));
                     loadingStructures.removeIf(loadingStructure -> {
                        ScriptLoader.LoadingScriptInfo loadingInfo = loadingStructure.loadingScriptInfo();
                        Structure structure = loadingStructure.structure();
                        parser.setActive(loadingInfo.script);
                        parser.setCurrentStructure(structure);
                        parser.setNode(loadingInfo.nodeMap.get(structure));

                        try {
                           if (!structure.preLoad()) {
                              loadingInfo.structures.remove(structure);
                              return true;
                           } else {
                              return false;
                           }
                        } catch (Exception var5x) {
                           Skript.exception(var5x, "An error occurred while trying to preLoad a Structure.");
                           loadingInfo.structures.remove(structure);
                           return true;
                        }
                     });
                     parser.setInactive();
                     loadingStructures.removeIf(loadingStructure -> {
                        ScriptLoader.LoadingScriptInfo loadingInfo = loadingStructure.loadingScriptInfo();
                        Structure structure = loadingStructure.structure();
                        parser.setActive(loadingInfo.script);
                        parser.setCurrentStructure(structure);
                        parser.setNode(loadingInfo.nodeMap.get(structure));

                        try {
                           if (!structure.load()) {
                              loadingInfo.structures.remove(structure);
                              return true;
                           } else {
                              return false;
                           }
                        } catch (Exception var5x) {
                           Skript.exception(var5x, "An error occurred while trying to load a Structure.");
                           loadingInfo.structures.remove(structure);
                           return true;
                        }
                     });
                     parser.setInactive();
                     loadingStructures.removeIf(loadingStructure -> {
                        ScriptLoader.LoadingScriptInfo loadingInfo = loadingStructure.loadingScriptInfo();
                        Structure structure = loadingStructure.structure();
                        parser.setActive(loadingInfo.script);
                        parser.setCurrentStructure(structure);
                        parser.setNode(loadingInfo.nodeMap.get(structure));

                        try {
                           if (!structure.postLoad()) {
                              loadingInfo.structures.remove(structure);
                              return true;
                           } else {
                              return false;
                           }
                        } catch (Exception var5x) {
                           Skript.exception(var5x, "An error occurred while trying to postLoad a Structure.");
                           loadingInfo.structures.remove(structure);
                           return true;
                        }
                     });
                     parser.setInactive();
                     scripts.forEach(loadingInfo -> {
                        Script script = loadingInfo.script;
                        parser.setActive(script);
                        parser.setNode(script.getConfig().getMainNode());
                        eventRegistry().events(ScriptLoader.ScriptLoadEvent.class).forEach(event -> event.onLoad(parser, script));
                        script.eventRegistry().events(ScriptLoader.ScriptLoadEvent.class).forEach(event -> event.onLoad(parser, script));
                     });
                     parser.setInactive();
                     var6x = scriptInfo;
                  } catch (Exception var14) {
                     throw Skript.exception(var14);
                  } finally {
                     parser.setInactive();

                     for (ScriptLoader.LoadingScriptInfo info : scripts) {
                        untrack(info.script);
                     }

                     openCloseable.close();
                  }

                  return var6x;
               }
            )
            .exceptionally(t -> {
               throw Skript.exception(t);
            });
      }
   }

   private static ScriptLoader.LoadingScriptInfo loadScript(Config config) {
      if (config.getFile() == null) {
         throw new IllegalArgumentException("A config must have a file to be loaded.");
      } else {
         ParserInstance parser = getParser();
         Map<Structure, Node> nodeMap = new HashMap<>();
         List<Structure> structures = new ArrayList<>();
         Script script = new Script(config, structures);
         parser.setActive(script);

         try {
            if (SkriptConfig.keepConfigsLoaded.value()) {
               SkriptConfig.configs.add(config);
            }

            try (CountingLogHandler ignored = new CountingLogHandler(SkriptLogger.SEVERE).start()) {
               for (Node node : config.getMainNode()) {
                  if (!(node instanceof SimpleNode) && !(node instanceof SectionNode)) {
                     Skript.error("could not interpret line as a structure");
                  } else {
                     String line = node.getKey();
                     if (line != null) {
                        line = replaceOptions(line);
                        if (SkriptParser.validateLine(line)) {
                           if (Skript.logVeryHigh() && !Skript.debug()) {
                              Skript.info("loading trigger '" + line + "'");
                           }

                           Structure structure = Structure.parse(line, node, "Can't understand this structure: " + line);
                           if (structure != null) {
                              structures.add(structure);
                              nodeMap.put(structure, node);
                           }
                        }
                     }
                  }
               }

               if (Skript.logHigh()) {
                  int count = structures.size();
                  Skript.info("loaded " + count + " structure" + (count == 1 ? "" : "s") + " from '" + config.getFileName() + "'");
               }
            }
         } catch (Exception var19) {
            Skript.exception(var19, "Could not load " + config.getFileName());
         } finally {
            parser.setInactive();
         }

         Callable<Void> callable = () -> {
            File file = config.getFile();

            assert file != null;

            File disabledFile = new File(file.getParentFile(), "-" + file.getName());
            disabledScripts.remove(disabledFile);
            loadedScripts.add(script);
            eventRegistry().events(ScriptLoader.ScriptInitEvent.class).forEach(event -> event.onInit(script));
            return null;
         };
         if (isAsync()) {
            Task.callSync(callable);
         } else {
            try {
               callable.call();
            } catch (Exception var16) {
               Skript.exception(var16);
            }
         }

         return new ScriptLoader.LoadingScriptInfo(script, structures, nodeMap);
      }
   }

   private static List<Config> loadStructures(File directory) {
      if (!directory.isDirectory()) {
         Config config = loadStructure(directory);
         return config != null ? Collections.singletonList(config) : Collections.emptyList();
      } else {
         try {
            directory = directory.getCanonicalFile();
         } catch (IOException var9) {
            Skript.exception(var9, "An exception occurred while trying to get the canonical file of: " + directory);
            return new ArrayList<>();
         }

         File[] files = directory.listFiles(loadedScriptFilter);

         assert files != null;

         Arrays.sort((Object[])files);
         List<Config> loadedDirectories = new ArrayList<>(files.length);
         List<Config> loadedFiles = new ArrayList<>(files.length);

         for (File file : files) {
            if (file.isDirectory()) {
               loadedDirectories.addAll(loadStructures(file));
            } else {
               Config cfg = loadStructure(file);
               if (cfg != null) {
                  loadedFiles.add(cfg);
               }
            }
         }

         loadedDirectories.addAll(loadedFiles);
         return loadedDirectories;
      }
   }

   @Nullable
   private static Config loadStructure(File file) {
      try {
         file = file.getCanonicalFile();
      } catch (IOException var3) {
         Skript.exception(var3, "An exception occurred while trying to get the canonical file of: " + file);
         return null;
      }

      if (!file.exists()) {
         Script script = getScript(file);
         if (script != null) {
            unloadScript(script);
         }

         return null;
      } else {
         track(file);

         try {
            String name = Skript.getInstance()
               .getDataFolder()
               .toPath()
               .toAbsolutePath()
               .resolve("scripts")
               .relativize(file.toPath().toAbsolutePath())
               .toString();
            return loadStructure(Files.newInputStream(file.toPath()), name);
         } catch (IOException var2) {
            Skript.error("Could not load " + file.getName() + ": " + ExceptionUtils.toString(var2));
            return null;
         }
      }
   }

   @Nullable
   private static Config loadStructure(InputStream source, String name) {
      try {
         return new Config(
            source, name, Skript.getInstance().getDataFolder().toPath().resolve("scripts").resolve(name).toFile().getCanonicalFile(), true, false, ":"
         );
      } catch (IOException var3) {
         Skript.error("Could not load " + name + ": " + ExceptionUtils.toString(var3));
         return null;
      }
   }

   public static ScriptLoader.ScriptInfo unloadScripts(Set<Script> scripts) {
      record UnloadingStructure(Script script, Structure structure) {
      }

      scripts = scripts.stream().filter(scriptx -> !track(scriptx.getConfig().getFile())).collect(Collectors.toSet());

      for (Script script : scripts) {
         if (!loadedScripts.contains(script)) {
            throw new SkriptAPIException("The script at '" + script.getConfig().getPath() + "' is not loaded!");
         }

         if (script.getConfig().getFile() == null) {
            throw new IllegalArgumentException("A script must have a file to be unloaded.");
         }
      }

      ParserInstance parser = getParser();
      Comparator<UnloadingStructure> unloadComparator = Comparator.comparing(unloadingStructurex -> unloadingStructurex.structure().getPriority());
      unloadComparator = unloadComparator.reversed();
      List<UnloadingStructure> unloadingStructures = scripts.stream()
         .flatMap(scriptx -> scriptx.getStructures().stream().map(structurex -> new UnloadingStructure(scriptx, structurex)))
         .sorted(unloadComparator)
         .collect(Collectors.toCollection(ArrayList::new));

      for (Script script : scripts) {
         eventRegistry().events(ScriptLoader.ScriptUnloadEvent.class).forEach(event -> event.onUnload(parser, script));
         script.eventRegistry().events(ScriptLoader.ScriptUnloadEvent.class).forEach(event -> event.onUnload(parser, script));
      }

      for (UnloadingStructure unloadingStructure : unloadingStructures) {
         Script script = unloadingStructure.script();
         Structure structure = unloadingStructure.structure();
         parser.setActive(script);
         structure.unload();
      }

      parser.setInactive();
      ScriptLoader.ScriptInfo info = new ScriptLoader.ScriptInfo();

      for (UnloadingStructure unloadingStructure : unloadingStructures) {
         Script script = unloadingStructure.script();
         Structure structure = unloadingStructure.structure();
         info.structures++;
         parser.setActive(script);
         structure.postUnload();
      }

      parser.setInactive();

      for (Script script : scripts) {
         info.files++;
         script.clearData();
         script.invalidate();
         loadedScripts.remove(script);
         File scriptFile = script.getConfig().getFile();

         assert scriptFile != null;

         disabledScripts.add(new File(scriptFile.getParentFile(), "-" + scriptFile.getName()));
         untrack(script);
      }

      return info;
   }

   public static ScriptLoader.ScriptInfo unloadScript(Script script) {
      return unloadScripts(Collections.singleton(script));
   }

   public static CompletableFuture<ScriptLoader.ScriptInfo> reloadScript(Script script, OpenCloseable openCloseable) {
      return reloadScripts(Collections.singleton(script), openCloseable);
   }

   public static CompletableFuture<ScriptLoader.ScriptInfo> reloadScripts(Set<Script> scripts, OpenCloseable openCloseable) {
      unloadScripts(scripts);
      List<Config> configs = new ArrayList<>();

      for (Script script : scripts) {
         Config config = loadStructure(script.getConfig().getFile());
         if (config == null) {
            return CompletableFuture.completedFuture(new ScriptLoader.ScriptInfo());
         }

         configs.add(config);
      }

      return loadScripts(configs, openCloseable);
   }

   public static String replaceOptions(String string) {
      ParserInstance parser = getParser();
      if (!parser.isActive()) {
         return string;
      } else {
         StructOptions.OptionsData optionsData = parser.getCurrentScript().getData(StructOptions.OptionsData.class);
         return optionsData == null ? string : optionsData.replaceOptions(string);
      }
   }

   public static ArrayList<TriggerItem> loadItems(SectionNode node) {
      ParserInstance parser = getParser();
      if (Skript.debug()) {
         parser.setIndentation(parser.getIndentation() + "    ");
      }

      ArrayList<TriggerItem> items = new ArrayList<>();
      parser.getHintManager().enterScope(true);
      boolean freezeScope = false;
      boolean executionStops = false;

      for (Node subNode : node) {
         parser.setNode(subNode);
         String subNodeKey = subNode.getKey();
         if (subNodeKey == null) {
            throw new IllegalArgumentException("Encountered node with null key: '" + subNode + "'");
         }

         String expr = replaceOptions(subNodeKey);
         if (SkriptParser.validateLine(expr)) {
            TriggerItem item = null;
            if (subNode instanceof SimpleNode) {
               long start = System.currentTimeMillis();
               item = Statement.parse(expr, items, "Can't understand this condition/effect: " + expr);
               if (item == null) {
                  continue;
               }

               long requiredTime = SkriptConfig.longParseTimeWarningThreshold.value().getAs(Timespan.TimePeriod.MILLISECOND);
               if (requiredTime > 0L) {
                  long timeTaken = System.currentTimeMillis() - start;
                  if (timeTaken > requiredTime) {
                     Skript.warning(
                        "The current line took a long time to parse ("
                           + new Timespan(timeTaken)
                           + "). Avoid using long lines and use parentheses to create clearer instructions."
                     );
                  }
               }

               if (Skript.debug() || subNode.debug()) {
                  Skript.debug(TextComponentParser.instance().escape(parser.getIndentation() + item.toString(null, true)));
               }

               items.add(item);
            } else {
               if (!(subNode instanceof SectionNode subSection)) {
                  continue;
               }

               RetainingLogHandler handler = SkriptLogger.startRetainingLog();

               try {
                  parser.getHintManager().enterScope(false);
                  item = Section.parse(expr, "Can't understand this section: " + expr, subSection, items);
                  if (item == null) {
                     RetainingLogHandler backup = handler.backup();
                     handler.clear();
                     item = Statement.parse(expr, "Can't understand this condition/effect: " + expr, subSection, items);
                     if (item == null) {
                        if (!handler.hasErrors()) {
                           handler.restore(backup);
                           continue;
                        }

                        LogEntry errorEntry = handler.getFirstError();

                        assert errorEntry != null;

                        String error = errorEntry.getMessage();
                        if (error.contains("Can't understand this condition/effect:")) {
                           handler.restore(backup);
                           continue;
                        }

                        LogEntry backupErrorEntry = backup.getFirstError();
                        if (backupErrorEntry == null) {
                           continue;
                        }

                        String backupError = backupErrorEntry.getMessage();
                        if (backupError.contains("tried to claim the current section, but it was already claimed by")
                           || !backupError.contains("Can't understand this section: ")
                              && error.contains(
                                 "is a valid statement but cannot function as a section (:) because there is no syntax in the line to manage it."
                              )) {
                           handler.restore(backup);
                        }
                        continue;
                     }
                  }
               } finally {
                  HintManager hintManager = parser.getHintManager();
                  if (item == null) {
                     hintManager.clearScope(0, false);
                  }

                  hintManager.exitScope();
                  RetainingLogHandler afterParse = handler.backup();
                  handler.clear();
                  handler.printLog();
                  if (item != null && (Skript.debug() || subNode.debug())) {
                     Skript.debug(TextComponentParser.instance().escape(parser.getIndentation() + item.toString(null, true)));
                  }

                  afterParse.printLog();
               }

               items.add(item);
            }

            if (executionStops
               && !SkriptConfig.disableUnreachableCodeWarnings.value()
               && parser.isActive()
               && !parser.getCurrentScript().suppressesWarning(ScriptWarning.UNREACHABLE_CODE)) {
               Skript.warning("Unreachable code. The previous statement stops further execution.");
            }

            executionStops = item.executionIntent() != null;
            if (executionStops && !freezeScope) {
               freezeScope = true;
               if (item.executionIntent() instanceof ExecutionIntent.StopSections intent) {
                  parser.getHintManager().mergeScope(0, intent.levels(), true);
               }
            }
         }
      }

      if (freezeScope) {
         parser.getHintManager().clearScope(0, false);
      }

      parser.getHintManager().exitScope();

      for (int i = 0; i < items.size() - 1; i++) {
         items.get(i).setNext(items.get(i + 1));
      }

      parser.setNode(node);
      if (Skript.debug()) {
         parser.setIndentation(parser.getIndentation().substring(0, parser.getIndentation().length() - 4));
      }

      return items;
   }

   @Internal
   public static Script createDummyScript(String name, @Nullable File file) {
      Config config = new Config(name, file);
      return new Script(config, Collections.emptyList());
   }

   public static Set<Script> getLoadedScripts() {
      return Set.copyOf(loadedScripts);
   }

   public static Set<File> getDisabledScripts() {
      return Set.copyOf(disabledScripts);
   }

   public static FileFilter getLoadedScriptsFilter() {
      return loadedScriptFilter;
   }

   public static FileFilter getDisabledScriptsFilter() {
      return disabledScriptFilter;
   }

   public static EventRegistry<ScriptLoader.LoaderEvent> eventRegistry() {
      return eventRegistry;
   }

   @Nullable
   public static File getScriptFromName(String script) {
      return getScriptFromName(script, Skript.getInstance().getScriptsFolder());
   }

   @Nullable
   public static File getScriptFromName(String script, File directory) {
      if (script.endsWith("/") || script.endsWith("\\")) {
         script = script.replace('/', File.separatorChar).replace('\\', File.separatorChar);
      } else if (!StringUtils.endsWithIgnoreCase(script, ".sk")) {
         int dot = script.lastIndexOf(46);
         if (dot > 0 && !script.substring(dot + 1).isEmpty()) {
            return null;
         }

         script = script + ".sk";
      }

      if (script.startsWith("-")) {
         script = script.substring(DISABLED_SCRIPT_PREFIX_LENGTH);
      }

      File scriptFile = new File(directory, script);
      if (!scriptFile.exists()) {
         scriptFile = new File(scriptFile.getParentFile(), "-" + scriptFile.getName());
         if (!scriptFile.exists()) {
            return null;
         }
      }

      try {
         return !TestMode.ENABLED && !scriptFile.getCanonicalPath().startsWith(directory.getCanonicalPath() + File.separator)
            ? null
            : scriptFile.getCanonicalFile();
      } catch (IOException var4) {
         throw Skript.exception(var4, "An exception occurred while trying to get the script file from the string '" + script + "'");
      }
   }

   private static class AsyncLoaderThread extends Thread {
      private boolean shouldRun = true;

      public static ScriptLoader.AsyncLoaderThread create() {
         ScriptLoader.AsyncLoaderThread thread = new ScriptLoader.AsyncLoaderThread();
         thread.start();
         return thread;
      }

      private AsyncLoaderThread() {
         super(ScriptLoader.asyncLoaderThreadGroup, (Runnable)null);
      }

      @Override
      public void run() {
         while (this.shouldRun) {
            try {
               Runnable runnable = ScriptLoader.loadQueue.poll(100L, TimeUnit.MILLISECONDS);
               if (runnable != null) {
                  runnable.run();
               }
            } catch (InterruptedException var2) {
               Skript.exception(var2);
            }
         }
      }

      public void cancelExecution() {
         this.shouldRun = false;
      }
   }

   public interface LoaderEvent extends Event {
   }

   private record LoadingScriptInfo(Script script, List<Structure> structures, Map<Structure, Node> nodeMap) {
   }

   public static class ScriptInfo {
      public int files;
      public int structures;

      public ScriptInfo() {
      }

      public ScriptInfo(int numFiles, int numStructures) {
         this.files = numFiles;
         this.structures = numStructures;
      }

      public ScriptInfo(ScriptLoader.ScriptInfo other) {
         this.files = other.files;
         this.structures = other.structures;
      }

      public void add(ScriptLoader.ScriptInfo other) {
         this.files = this.files + other.files;
         this.structures = this.structures + other.structures;
      }

      public void subtract(ScriptLoader.ScriptInfo other) {
         this.files = this.files - other.files;
         this.structures = this.structures - other.structures;
      }

      @Override
      public String toString() {
         return "ScriptInfo{files=" + this.files + ",structures=" + this.structures + "}";
      }
   }

   @FunctionalInterface
   public interface ScriptInitEvent extends ScriptLoader.LoaderEvent {
      void onInit(Script var1);
   }

   @FunctionalInterface
   public interface ScriptLoadEvent extends ScriptLoader.LoaderEvent, Script.Event {
      void onLoad(ParserInstance var1, Script var2);
   }

   @FunctionalInterface
   public interface ScriptPreInitEvent extends ScriptLoader.LoaderEvent {
      void onPreInit(Collection<Config> var1);
   }

   @FunctionalInterface
   public interface ScriptUnloadEvent extends ScriptLoader.LoaderEvent, Script.Event {
      void onUnload(ParserInstance var1, Script var2);
   }
}
