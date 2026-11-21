package com.fastasyncworldedit.core.configuration;

import com.fastasyncworldedit.core.configuration.file.YamlConfiguration;
import com.fastasyncworldedit.core.util.StringMan;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public class Config {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final Map<String, Object> removedKeyVals = new HashMap<>();
   @Nullable
   private Map<String, Config.FromNode> copyTo = new HashMap<>();
   private boolean performCopyTo = false;
   private List<String> existingMigrateNodes = null;

   public Config() {
      this.save(new PrintWriter(new ByteArrayOutputStream(0)), this.getClass(), this, 0, null);
      this.performCopyTo = true;
   }

   private <T> T get(String key, Class<?> root) {
      String[] split = key.split("\\.");
      Object instance = this.getInstance(split, root);
      if (instance != null) {
         Field field = this.getField(split, instance);
         if (field != null) {
            try {
               return (T)field.get(instance);
            } catch (IllegalAccessException var7) {
               LOGGER.error("Failed to get config option: {}", key, var7);
               return null;
            }
         }
      }

      LOGGER.error("Failed to get config option: {}", key);
      return null;
   }

   private void setLoadedNode(String key, Object value, Class<?> root) {
      String[] split = key.split("\\.");
      Object instance = this.getInstance(split, root);
      if (instance != null) {
         Field field = this.getField(split, instance);
         if (field != null) {
            try {
               if (field.getAnnotation(Config.Final.class) != null) {
                  return;
               }

               if (this.copyTo != null) {
                  this.copyTo.remove(key);
                  this.copyTo
                     .replaceAll(
                        (copyToNode, fromNode) -> (Config.FromNode)(!key.equals(fromNode.node())
                           ? fromNode
                           : new Config.FromNode(key, fromNode.computation, value))
                     );
               }

               Config.Migrate migrate = field.getAnnotation(Config.Migrate.class);
               if (migrate != null) {
                  this.existingMigrateNodes.add(migrate.value());
               }

               if (field.getType() == String.class && !(value instanceof String)) {
                  value = value + "";
               }

               field.set(instance, value);
               return;
            } catch (Throwable var8) {
               LOGGER.error("Failed to set config option: {}", key);
            }
         }
      }

      this.removedKeyVals.put(key, value);
      LOGGER.warn(
         "Failed to set config option: {}: {} | {} | {}.yml. This is likely because it was removed or was set with an invalid value.",
         key,
         value,
         instance,
         root.getSimpleName()
      );
   }

   public boolean load(File file) {
      if (!file.exists()) {
         return false;
      } else {
         this.existingMigrateNodes = new ArrayList<>();
         YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

         for (String key : yml.getKeys(true)) {
            Object value = yml.get(key);
            if (!(value instanceof MemorySection)) {
               this.setLoadedNode(key, value, this.getClass());
            }
         }

         for (String node : this.existingMigrateNodes) {
            this.removedKeyVals.remove(node);
         }

         this.existingMigrateNodes = null;
         return true;
      }
   }

   public void save(File file) {
      try {
         if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null) {
               file.getParentFile().mkdirs();
            }

            file.createNewFile();
         }

         PrintWriter writer = new PrintWriter(file);
         this.save(writer, this.getClass(), this, 0, null);
         writer.close();
      } catch (Throwable var4) {
         LOGGER.error("Failed to save config file: {}", file, var4);
      }
   }

   private String toYamlString(Object value, String spacing) {
      if (!(value instanceof List)) {
         if (value instanceof String stringValue) {
            return stringValue.isEmpty() ? "''" : "\"" + stringValue + "\"";
         } else {
            return value != null ? value.toString() : "null";
         }
      } else {
         Collection<?> listValue = (Collection<?>)value;
         if (listValue.isEmpty()) {
            return "[]";
         } else {
            StringBuilder m = new StringBuilder();

            for (Object obj : listValue) {
               m.append(System.lineSeparator()).append(spacing).append("- ").append(this.toYamlString(obj, spacing));
            }

            return m.toString();
         }
      }
   }

   private void save(PrintWriter writer, Class<?> clazz, final Object instance, int indent, String parentNode) {
      try {
         String CTRF = System.lineSeparator();
         String spacing = " ".repeat(indent);

         for (Field field : clazz.getFields()) {
            if (field.getAnnotation(Config.Ignore.class) == null) {
               Class<?> current = field.getType();
               if (field.getAnnotation(Config.Ignore.class) == null) {
                  Config.Comment comment = field.getAnnotation(Config.Comment.class);
                  if (comment != null) {
                     for (String commentLine : comment.value()) {
                        writer.write(spacing + "# " + commentLine + CTRF);
                     }
                  }

                  if (current == Config.ConfigBlock.class) {
                     current = (Class<?>)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
                     this.handleConfigBlockSave(writer, instance, indent, field, spacing, CTRF, current, parentNode);
                  } else {
                     if (!this.removedKeyVals.isEmpty()) {
                        Config.Migrate migrate = field.getAnnotation(Config.Migrate.class);
                        Object value;
                        if (migrate != null && (value = this.removedKeyVals.remove(migrate.value())) != null) {
                           field.set(instance, value);
                        }
                     }

                     Config.CopiedFrom copiedFrom;
                     if (this.copyTo != null && (copiedFrom = field.getAnnotation(Config.CopiedFrom.class)) != null) {
                        String node = this.toNodeName(field.getName());
                        node = parentNode == null ? node : parentNode + "." + node;
                        Config.FromNode entry = this.copyTo.remove(node);
                        if (entry == null) {
                           this.copyTo.put(node, new Config.FromNode(copiedFrom.value(), null, null));
                        } else {
                           Object copiedVal;
                           if ((copiedVal = entry.val()) != null) {
                              field.set(instance, copiedVal);
                           }
                        }
                     }

                     Config.ComputedFrom computedFrom;
                     if (this.copyTo != null && (computedFrom = field.getAnnotation(Config.ComputedFrom.class)) != null) {
                        String node = this.toNodeName(field.getName());
                        node = parentNode == null ? node : parentNode + "." + node;
                        Config.FromNode entry = this.copyTo.remove(node);
                        if (entry == null) {
                           this.copyTo.put(node, new Config.FromNode(computedFrom.node(), computedFrom.computer(), null));
                        } else {
                           Object copiedVal;
                           if ((copiedVal = entry.val()) != null) {
                              ConfigOptComputation<?> computer = (ConfigOptComputation<?>)computedFrom.computer().getDeclaredConstructor().newInstance();
                              field.set(instance, computer.apply(copiedVal));
                           }
                        }
                     }

                     Config.Create create = field.getAnnotation(Config.Create.class);
                     if (create != null) {
                        Object value = field.get(instance);
                        this.setAccessible(field);
                        if (indent == 0) {
                           writer.write(CTRF);
                        }

                        comment = current.getAnnotation(Config.Comment.class);
                        if (comment != null) {
                           for (String commentLine : comment.value()) {
                              writer.write(spacing + "# " + commentLine + CTRF);
                           }
                        }

                        String node = this.toNodeName(current.getSimpleName());
                        writer.write(spacing + node + ":" + CTRF);
                        if (value == null) {
                           field.set(instance, value = current.getDeclaredConstructor().newInstance());
                        }

                        this.save(writer, current, value, indent + 2, parentNode == null ? node : parentNode + "." + node);
                     } else {
                        writer.write(spacing + this.toNodeName(field.getName() + ": ") + this.toYamlString(field.get(instance), spacing) + CTRF);
                     }
                  }
               }
            }
         }
      } catch (Throwable var22) {
         LOGGER.error("Failed to save config file", var22);
      }

      if (parentNode == null && this.performCopyTo) {
         this.performCopyTo = false;
         this.copyTo = null;
      }
   }

   private <T> void handleConfigBlockSave(
      PrintWriter writer, Object instance, int indent, Field field, String spacing, String CTRF, Class<T> current, String parentNode
   ) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
      Config.Comment comment = current.getAnnotation(Config.Comment.class);
      if (comment != null) {
         for (String commentLine : comment.value()) {
            writer.write(spacing + "# " + commentLine + CTRF);
         }
      }

      Config.BlockName blockNames = current.getAnnotation(Config.BlockName.class);
      if (blockNames != null) {
         String node = this.toNodeName(current.getSimpleName());
         writer.write(spacing + this.toNodeName(current.getSimpleName()) + ":" + CTRF);
         Config.ConfigBlock<T> configBlock = (Config.ConfigBlock<T>)field.get(instance);
         if (configBlock == null || configBlock.getInstances().isEmpty()) {
            configBlock = new Config.ConfigBlock<>();
            field.set(instance, configBlock);

            for (String blockName : blockNames.value()) {
               configBlock.put(blockName, current.getDeclaredConstructor().newInstance());
            }
         }

         for (Entry<String, T> entry : configBlock.getRaw().entrySet()) {
            String key = entry.getKey();
            writer.write(spacing + "  " + this.toNodeName(key) + ":" + CTRF);
            this.save(writer, current, entry.getValue(), indent + 4, parentNode == null ? node : parentNode + "." + node);
         }
      }
   }

   private Field getField(String[] split, Object instance) {
      try {
         Field field = instance.getClass().getField(this.toFieldName(split[split.length - 1]));
         this.setAccessible(field);
         return field;
      } catch (Throwable var4) {
         LOGGER.warn(
            "Invalid config field: {} for {}. It is possible this is because it has been removed.",
            StringMan.join((Object[])split, "."),
            this.toNodeName(instance.getClass().getSimpleName())
         );
         return null;
      }
   }

   private Object getInstance(String[] split, Class<?> root) {
      try {
         Class<?> clazz = root == null ? MethodHandles.lookup().lookupClass() : root;
         Object instance = this;

         while (split.length > 0) {
            if (split.length == 1) {
               return instance;
            }

            Class<?> found = null;
            Class<?>[] classes = clazz.getDeclaredClasses();
            Class[] instanceField = classes;
            int value = classes.length;
            int var9 = 0;

            while (true) {
               if (var9 < value) {
                  Class<?> current = instanceField[var9];
                  if (!StringMan.isEqual(current.getSimpleName(), this.toFieldName(split[0]))) {
                     var9++;
                     continue;
                  }

                  found = current;
               }

               try {
                  Field instanceFieldx = clazz.getDeclaredField(this.toFieldName(split[0]));
                  this.setAccessible(instanceFieldx);
                  if (instanceFieldx.getType() != Config.ConfigBlock.class) {
                     Object valuex = instanceFieldx.get(instance);
                     if (valuex == null) {
                        valuex = found.getDeclaredConstructor().newInstance();
                        instanceFieldx.set(instance, valuex);
                     }

                     clazz = found;
                     instance = valuex;
                     split = Arrays.copyOfRange(split, 1, split.length);
                  } else {
                     Config.ConfigBlock valuex = (Config.ConfigBlock)instanceFieldx.get(instance);
                     if (valuex == null) {
                        valuex = new Config.ConfigBlock();
                        instanceFieldx.set(instance, valuex);
                     }

                     instance = valuex.get(split[1]);
                     if (instance == null) {
                        instance = found.getDeclaredConstructor().newInstance();
                        valuex.put(split[1], instance);
                     }

                     clazz = found;
                     split = Arrays.copyOfRange(split, 2, split.length);
                  }
                  break;
               } catch (NoSuchFieldException var11) {
                  if (found != null) {
                     split = Arrays.copyOfRange(split, 1, split.length);
                     clazz = found;
                     instance = found.getDeclaredConstructor().newInstance();
                     break;
                  }

                  return null;
               }
            }
         }
      } catch (Throwable var12) {
         LOGGER.error("Failed retrieving instance for config node: {}", StringUtil.joinString(split, "."), var12);
      }

      return null;
   }

   private String toFieldName(String node) {
      return node.toUpperCase(Locale.ROOT).replaceAll("-", "_");
   }

   private String toNodeName(String field) {
      return field.toLowerCase(Locale.ROOT).replace("_", "-");
   }

   private void setAccessible(Field field) throws NoSuchFieldException, IllegalAccessException {
      field.setAccessible(true);
      if (Modifier.isFinal(field.getModifiers())) {
         Field modifiersField = Field.class.getDeclaredField("modifiers");
         modifiersField.setAccessible(true);
         modifiersField.setInt(field, field.getModifiers() & -17);
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD, ElementType.TYPE})
   public @interface BlockName {
      String[] value();
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD, ElementType.TYPE})
   public @interface Comment {
      String[] value();
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD})
   @interface ComputedFrom {
      String node();

      Class<? extends ConfigOptComputation<?>> computer();
   }

   @Config.Ignore
   public static class ConfigBlock<T> {
      private final HashMap<String, T> INSTANCES = new HashMap<>();

      public T remove(String key) {
         return this.INSTANCES.remove(key);
      }

      public T get(String key) {
         return this.INSTANCES.get(key);
      }

      public void put(String key, T value) {
         this.INSTANCES.put(key, value);
      }

      public Collection<T> getInstances() {
         return this.INSTANCES.values();
      }

      public Collection<String> getSections() {
         return this.INSTANCES.keySet();
      }

      private Map<String, T> getRaw() {
         return this.INSTANCES;
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD})
   public @interface CopiedFrom {
      String value();
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD})
   public @interface Create {
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD})
   public @interface Final {
   }

   private record FromNode(String node, Class<? extends ConfigOptComputation<?>> computation, Object val) {
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD, ElementType.TYPE})
   public @interface Ignore {
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD})
   public @interface Migrate {
      String value();
   }
}
