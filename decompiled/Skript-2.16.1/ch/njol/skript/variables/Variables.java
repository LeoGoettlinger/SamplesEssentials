package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.ConfigurationSerializer;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import ch.njol.util.SynchronizedReference;
import ch.njol.util.coll.iterator.EmptyIterator;
import ch.njol.yggdrasil.Yggdrasil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.lang.converter.Converters;

public class Variables {
   public static final short YGGDRASIL_VERSION = 1;
   public static final Yggdrasil yggdrasil = new Yggdrasil((short)1);
   public static boolean caseInsensitiveVariables = true;
   private static final String CONFIGURATION_SERIALIZABLE_PREFIX = "ConfigurationSerializable_";
   private static final Multimap<Class<? extends VariablesStorage>, String> TYPES = HashMultimap.create();
   static final List<VariablesStorage> STORAGES = new ArrayList<>();
   static final ReadWriteLock variablesLock = new ReentrantReadWriteLock(true);
   static final VariablesMap variables = new VariablesMap();
   private static final Map<Event, VariablesMap> localVariables = new ConcurrentHashMap<>();
   static final Queue<Variables.VariableChange> changeQueue = new ConcurrentLinkedQueue<>();
   private static final SynchronizedReference<Map<String, NonNullPair<Object, VariablesStorage>>> TEMP_VARIABLES = new SynchronizedReference<>(new HashMap<>());
   private static final int MAX_CONFLICT_WARNINGS = 50;
   private static int loadConflicts = 0;
   static final BlockingQueue<SerializedVariable> saveQueue = new LinkedBlockingQueue<>();
   private static volatile boolean closed = false;
   private static final Thread saveThread = Skript.newThread(() -> {
      while (!closed) {
         try {
            SerializedVariable variable = saveQueue.take();

            for (VariablesStorage variablesStorage : STORAGES) {
               if (variablesStorage.accept(variable.name)) {
                  variablesStorage.save(variable);
                  break;
               }
            }
         } catch (InterruptedException var3) {
         }
      }
   }, "Skript variable save thread");

   @UnmodifiableView
   public static List<VariablesStorage> getStores() {
      return Collections.unmodifiableList(STORAGES);
   }

   public static <T extends VariablesStorage> boolean registerStorage(Class<T> storage, String... names) {
      if (TYPES.containsKey(storage)) {
         return false;
      } else {
         for (String name : names) {
            if (TYPES.containsValue(name.toLowerCase(Locale.ENGLISH))) {
               return false;
            }
         }

         for (String namex : names) {
            TYPES.put(storage, namex.toLowerCase(Locale.ENGLISH));
         }

         return true;
      }
   }

   public static boolean load() {
      assert variables.treeMap.isEmpty();

      assert variables.hashMap.isEmpty();

      assert STORAGES.isEmpty();

      Config config = SkriptConfig.getConfig();
      if (config == null) {
         throw new SkriptAPIException("Cannot load variables before the config");
      } else {
         Node databases = config.getMainNode().get("databases");
         if (!(databases instanceof SectionNode)) {
            Skript.error("The config is missing the required 'databases' section that defines where the variables are saved");
            return false;
         } else {
            Skript.closeOnDisable(Variables::close);
            Thread loadingLoggerThread = new Thread(() -> {
               while (true) {
                  try {
                     Thread.sleep(Skript.logNormal() ? 1000L : 5000L);
                  } catch (InterruptedException var4) {
                  }

                  synchronized (TEMP_VARIABLES) {
                     Map<String, NonNullPair<Object, VariablesStorage>> tvsx = TEMP_VARIABLES.get();
                     if (tvsx == null) {
                        return;
                     }

                     Skript.info("Loaded " + tvsx.size() + " variables so far...");
                  }
               }
            });
            loadingLoggerThread.start();

            try {
               boolean successful = true;

               for (Node node : (SectionNode)databases) {
                  if (!(node instanceof SectionNode sectionNode)) {
                     Skript.error("Invalid line in databases: databases must be defined as sections");
                     successful = false;
                  } else {
                     String type = sectionNode.getValue("type");
                     if (type == null) {
                        Skript.error("Missing entry 'type' in database definition");
                        successful = false;
                     } else {
                        String name = sectionNode.getKey();

                        assert name != null;

                        Optional<?> optional = TYPES.entries()
                           .stream()
                           .filter(entry -> ((String)entry.getValue()).equalsIgnoreCase(type))
                           .map(Entry::getKey)
                           .findFirst();
                        if (!optional.isPresent()) {
                           if (!type.equalsIgnoreCase("disabled") && !type.equalsIgnoreCase("none")) {
                              Skript.error("Invalid database type '" + type + "'");
                              successful = false;
                           }
                        } else {
                           VariablesStorage variablesStorage;
                           try {
                              Class<? extends VariablesStorage> storageClass = (Class<? extends VariablesStorage>)optional.get();
                              Constructor<?> constructor = storageClass.getDeclaredConstructor(String.class);
                              constructor.setAccessible(true);
                              variablesStorage = (VariablesStorage)constructor.newInstance(type);
                           } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException var24) {
                              Skript.error("Failed to initialize database `" + name + "`");
                              successful = false;
                              continue;
                           }

                           int totalVariablesLoaded;
                           synchronized (TEMP_VARIABLES) {
                              Map<String, NonNullPair<Object, VariablesStorage>> tvs = TEMP_VARIABLES.get();

                              assert tvs != null;

                              totalVariablesLoaded = tvs.size();
                           }

                           long start = System.currentTimeMillis();
                           if (Skript.logVeryHigh()) {
                              Skript.info("Loading database '" + node.getKey() + "'...");
                           }

                           if (variablesStorage.load(sectionNode)) {
                              STORAGES.add(variablesStorage);
                           } else {
                              successful = false;
                           }

                           int newVariablesLoaded;
                           synchronized (TEMP_VARIABLES) {
                              Map<String, NonNullPair<Object, VariablesStorage>> tvs = TEMP_VARIABLES.get();

                              assert tvs != null;

                              newVariablesLoaded = tvs.size() - totalVariablesLoaded;
                           }

                           if (Skript.logVeryHigh()) {
                              Skript.info(
                                 "Loaded "
                                    + newVariablesLoaded
                                    + " variables from the database '"
                                    + sectionNode.getKey()
                                    + "' in "
                                    + (System.currentTimeMillis() - start) / 100L / 10.0
                                    + " seconds"
                              );
                           }
                        }
                     }
                  }
               }

               if (!successful) {
                  return false;
               }

               if (STORAGES.isEmpty()) {
                  Skript.error(
                     "No databases to store variables are defined. Please enable at least the default database, even if you don't use variables at all."
                  );
                  return false;
               }
            } finally {
               SkriptLogger.setNode(null);
               int notStoredVariablesCount = onStoragesLoaded();
               if (notStoredVariablesCount != 0) {
                  Skript.warning(
                     notStoredVariablesCount
                        + " variables were possibly discarded due to not belonging to any database (SQL databases keep such variables and will continue to generate this warning, while CSV discards them)."
                  );
               }

               loadingLoggerThread.interrupt();
               saveThread.start();
            }

            return true;
         }
      }
   }

   public static String[] splitVariableName(String name) {
      String sep = "::";
      int sepLen = sep.length();
      int first = name.indexOf(sep);
      if (first == -1) {
         return new String[]{name};
      } else {
         int second = name.indexOf(sep, first + sepLen);
         if (second == -1) {
            return new String[]{name.substring(0, first), name.substring(first + sepLen)};
         } else {
            List<String> parts = new ArrayList<>();
            parts.add(name.substring(0, first));
            parts.add(name.substring(first + sepLen, second));
            int start = second + sepLen;

            int index;
            while ((index = name.indexOf(sep, start)) != -1) {
               parts.add(name.substring(start, index));
               start = index + sepLen;
            }

            parts.add(name.substring(start));
            return parts.toArray(String[]::new);
         }
      }
   }

   static TreeMap<String, Object> getVariables() {
      return variables.treeMap;
   }

   static Map<String, Object> getVariablesHashMap() {
      return Collections.unmodifiableMap(variables.hashMap);
   }

   static Lock getReadLock() {
      return variablesLock.readLock();
   }

   @Nullable
   public static VariablesMap removeLocals(Event event) {
      return localVariables.remove(event);
   }

   public static void setLocalVariables(Event event, @Nullable Object map) {
      if (map != null) {
         localVariables.put(event, (VariablesMap)map);
      } else {
         removeLocals(event);
      }
   }

   @Nullable
   public static Object copyLocalVariables(Event event) {
      VariablesMap from = localVariables.get(event);
      return from == null ? null : from.copy();
   }

   public static void withLocalVariables(Event provider, Event user, @NotNull Runnable action) {
      setLocalVariables(user, copyLocalVariables(provider));
      action.run();
      setLocalVariables(provider, copyLocalVariables(user));
      removeLocals(user);
   }

   @Nullable
   public static Object getVariable(String name, @Nullable Event event, boolean local) {
      String n;
      if (caseInsensitiveVariables) {
         n = name.toLowerCase(Locale.ENGLISH);
      } else {
         n = name;
      }

      if (local) {
         VariablesMap map = localVariables.get(event);
         return map == null ? null : map.getVariable(n);
      } else {
         try {
            variablesLock.readLock().lock();
            if (!changeQueue.isEmpty()) {
               Variables.VariableChange variableChange = changeQueue.stream()
                  .filter(change -> change.name.equals(n))
                  .reduce((first, second) -> second)
                  .orElse(null);
               if (variableChange != null) {
                  return variableChange.value;
               }
            }

            return variables.getVariable(n);
         } finally {
            variablesLock.readLock().unlock();
         }
      }
   }

   public static Iterator<Pair<String, Object>> getVariableIterator(String name, final boolean local, @Nullable final Event event) {
      assert name.endsWith("*");

      Object val = getVariable(name, event, local);
      final String subName = StringUtils.substring(name, 0, -1);
      if (val == null) {
         return new EmptyIterator<>();
      } else {
         assert val instanceof TreeMap;

         final Iterator<String> keys = new ArrayList<>(((Map)val).keySet()).iterator();
         return new Iterator<Pair<String, Object>>() {
            @Nullable
            private String key;
            @Nullable
            private Object next = null;

            @Override
            public boolean hasNext() {
               if (this.next != null) {
                  return true;
               } else {
                  while (keys.hasNext()) {
                     this.key = keys.next();
                     if (this.key != null) {
                        this.next = Variable.convertIfOldPlayer(subName + this.key, local, event, Variables.getVariable(subName + this.key, event, local));
                        if (this.next != null && !(this.next instanceof TreeMap)) {
                           return true;
                        }
                     }
                  }

                  this.next = null;
                  return false;
               }
            }

            public Pair<String, Object> next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  Pair<String, Object> n = new Pair<>(this.key, this.next);
                  this.next = null;
                  return n;
               }
            }

            @Override
            public void remove() {
               if (this.key == null) {
                  throw new IllegalStateException();
               } else {
                  Variables.deleteVariable(this.key, event, local);
               }
            }
         };
      }
   }

   public static void deleteVariable(String name, @Nullable Event event, boolean local) {
      setVariable(name, null, event, local);
   }

   public static void setVariable(String name, @Nullable Object value, @Nullable Event event, boolean local) {
      if (caseInsensitiveVariables) {
         name = name.toLowerCase(Locale.ENGLISH);
      }

      if (value != null) {
         assert !name.endsWith("::*");

         ClassInfo<?> ci = Classes.getSuperClassInfo(value.getClass());
         Class<?> sas = ci.getSerializeAs();
         if (sas != null) {
            value = Converters.convert(value, sas);

            assert value != null : ci + ", " + sas;
         }
      }

      if (local) {
         assert event != null : name;

         VariablesMap map = localVariables.computeIfAbsent(event, e -> new VariablesMap());
         map.setVariable(name, value);
      } else {
         setVariable(name, value);
      }
   }

   static void setVariable(String name, @Nullable Object value) {
      if (variablesLock.writeLock().tryLock()) {
         try {
            if (!changeQueue.isEmpty()) {
               processChangeQueue();
            }

            variables.setVariable(name, value);
            saveVariableChange(name, value);
         } finally {
            variablesLock.writeLock().unlock();
         }
      } else {
         queueVariableChange(name, value);
      }
   }

   private static void queueVariableChange(String name, @Nullable Object value) {
      changeQueue.add(new Variables.VariableChange(name, value));
   }

   static void processChangeQueue() {
      while (true) {
         Variables.VariableChange change = changeQueue.poll();
         if (change == null) {
            return;
         }

         variables.setVariable(change.name, change.value);
         saveVariableChange(change.name, change.value);
      }
   }

   static boolean variableLoaded(String name, @Nullable Object value, VariablesStorage source) {
      assert Bukkit.isPrimaryThread();

      if (value == null) {
         return false;
      } else {
         synchronized (TEMP_VARIABLES) {
            Map<String, NonNullPair<Object, VariablesStorage>> tvs = TEMP_VARIABLES.get();
            if (tvs != null) {
               NonNullPair<Object, VariablesStorage> existingVariable = tvs.get(name);
               if (existingVariable != null) {
                  VariablesStorage existingVariableStorage = existingVariable.getSecond();
                  if (existingVariableStorage != source) {
                     loadConflicts++;
                     if (loadConflicts <= 50) {
                        Skript.warning(
                           "The variable {"
                              + name
                              + "} was loaded twice from different databases ("
                              + existingVariableStorage.getUserConfigurationName()
                              + " and "
                              + source.getUserConfigurationName()
                              + "), only the one from "
                              + source.getUserConfigurationName()
                              + " will be kept."
                        );
                     } else if (loadConflicts == 51) {
                        Skript.warning("[!] More than 50 variables were loaded more than once from different databases, no more warnings will be printed.");
                     }

                     existingVariableStorage.save(name, null, null);
                  }
               }

               tvs.put(name, new NonNullPair<>(value, source));
               return false;
            }
         }

         variablesLock.writeLock().lock();

         try {
            variables.setVariable(name, value);
         } finally {
            variablesLock.writeLock().unlock();
         }

         try {
            for (VariablesStorage variablesStorage : STORAGES) {
               if (variablesStorage.accept(name)) {
                  if (variablesStorage != source) {
                     SerializedVariable.Value serializedValue = serialize(value);
                     if (serializedValue == null) {
                        variablesStorage.save(name, null, null);
                     } else {
                        variablesStorage.save(name, serializedValue.type, serializedValue.data);
                     }

                     if (value != null) {
                        source.save(name, null, null);
                     }
                  }

                  return true;
               }
            }
         } catch (Exception var13) {
            Skript.exception(var13, "Error saving variable named " + name);
         }

         return false;
      }
   }

   private static int onStoragesLoaded() {
      if (loadConflicts > 50) {
         Skript.warning("A total of " + loadConflicts + " variables were loaded more than once from different databases");
      }

      Skript.debug("Databases loaded, setting variables...");
      synchronized (TEMP_VARIABLES) {
         Map<String, NonNullPair<Object, VariablesStorage>> tvs = TEMP_VARIABLES.get();
         TEMP_VARIABLES.set(null);

         assert tvs != null;

         variablesLock.writeLock().lock();

         int var12;
         try {
            int unstoredVariables = 0;

            for (Entry<String, NonNullPair<Object, VariablesStorage>> tv : tvs.entrySet()) {
               if (!variableLoaded(tv.getKey(), tv.getValue().getFirst(), tv.getValue().getSecond())) {
                  unstoredVariables++;
               }
            }

            for (VariablesStorage variablesStorage : STORAGES) {
               variablesStorage.allLoaded();
            }

            Skript.debug("Variables set. Queue size = " + saveQueue.size());
            var12 = unstoredVariables;
         } finally {
            variablesLock.writeLock().unlock();
         }

         return var12;
      }
   }

   public static SerializedVariable serialize(String name, @Nullable Object value) {
      assert Bukkit.isPrimaryThread();

      SerializedVariable.Value var;
      try {
         var = serialize(value);
      } catch (Exception var4) {
         throw Skript.exception(var4, "Error saving variable named " + name);
      }

      return new SerializedVariable(name, var);
   }

   @Nullable
   public static SerializedVariable.Value serialize(@Nullable Object value) {
      assert Bukkit.isPrimaryThread();

      return Classes.serialize(value);
   }

   private static void saveVariableChange(String name, @Nullable Object value) {
      if (!name.startsWith("-")) {
         saveQueue.add(serialize(name, value));
      }
   }

   public static void close() {
      try {
         variablesLock.writeLock().lock();
         processChangeQueue();
      } finally {
         variablesLock.writeLock().unlock();
      }

      while (saveQueue.size() > 0) {
         try {
            Thread.sleep(10L);
         } catch (InterruptedException var3) {
         }
      }

      closed = true;
      saveThread.interrupt();
   }

   public static int numVariables() {
      int var0;
      try {
         variablesLock.readLock().lock();
         var0 = variables.hashMap.size();
      } finally {
         variablesLock.readLock().unlock();
      }

      return var0;
   }

   static {
      registerStorage(FlatFileStorage.class, "csv", "file", "flatfile");
      registerStorage(SQLiteStorage.class, "sqlite");
      registerStorage(MySQLStorage.class, "mysql");
      yggdrasil.registerSingleClass(Kleenean.class, "Kleenean");
      yggdrasil.registerClassResolver(
         new ConfigurationSerializer<ConfigurationSerializable>() {
            {
               this.info = Classes.getExactClassInfo(Object.class);
            }

            @Nullable
            @Override
            public String getID(@NotNull Class<?> c) {
               return ConfigurationSerializable.class.isAssignableFrom(c) && Classes.getSuperClassInfo(c) == Classes.getExactClassInfo(Object.class)
                  ? "ConfigurationSerializable_" + ConfigurationSerialization.getAlias(c.asSubclass(ConfigurationSerializable.class))
                  : null;
            }

            @Nullable
            @Override
            public Class<? extends ConfigurationSerializable> getClass(@NotNull String id) {
               return id.startsWith("ConfigurationSerializable_")
                  ? ConfigurationSerialization.getClassByAlias(id.substring("ConfigurationSerializable_".length()))
                  : null;
            }
         }
      );
   }

   private static class VariableChange {
      public final String name;
      @Nullable
      public final Object value;

      public VariableChange(String name, @Nullable Object value) {
         this.name = name;
         this.value = value;
      }
   }
}
