package ch.jalu.configme.resource;

import ch.jalu.configme.exception.ConfigMeException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

public class YamlFileReader implements PropertyReader {
   private final Path path;
   private final Charset charset;
   @Nullable
   private final Map<String, Object> root;

   public YamlFileReader(@NotNull Path path) {
      this(path, StandardCharsets.UTF_8, true);
   }

   public YamlFileReader(@NotNull Path path, @NotNull Charset charset) {
      this(path, charset, true);
   }

   public YamlFileReader(@NotNull Path path, @NotNull Charset charset, boolean splitDotPaths) {
      this.path = path;
      this.charset = charset;
      this.root = this.loadFile(splitDotPaths);
   }

   @Deprecated
   public YamlFileReader(@NotNull File file) {
      this(file.toPath(), StandardCharsets.UTF_8);
   }

   @Nullable
   @Override
   public Object getObject(@NotNull String path) {
      if (path.isEmpty()) {
         return this.root;
      } else {
         Object node = this.root;
         String[] keys = path.split("\\.");

         for (String key : keys) {
            node = getEntryIfIsMap(key, node);
            if (node == null) {
               return null;
            }
         }

         return node;
      }
   }

   @Nullable
   @Override
   public String getString(@NotNull String path) {
      return this.getTypedObject(path, String.class);
   }

   @Nullable
   @Override
   public Integer getInt(@NotNull String path) {
      Number n = this.getTypedObject(path, Number.class);
      return n == null ? null : n.intValue();
   }

   @Nullable
   @Override
   public Double getDouble(@NotNull String path) {
      Number n = this.getTypedObject(path, Number.class);
      return n == null ? null : n.doubleValue();
   }

   @Nullable
   @Override
   public Boolean getBoolean(@NotNull String path) {
      return this.getTypedObject(path, Boolean.class);
   }

   @Nullable
   @Override
   public List<?> getList(@NotNull String path) {
      return this.getTypedObject(path, List.class);
   }

   @Override
   public boolean contains(@NotNull String path) {
      return this.getObject(path) != null;
   }

   @NotNull
   @Override
   public Set<String> getKeys(boolean onlyLeafNodes) {
      if (this.root == null) {
         return Collections.emptySet();
      } else {
         Set<String> allKeys = new LinkedHashSet<>();
         this.collectKeysIntoSet("", this.root, allKeys, onlyLeafNodes);
         return allKeys;
      }
   }

   @NotNull
   @Override
   public Set<String> getChildKeys(@NotNull String path) {
      Object object = this.getObject(path);
      if (object instanceof Map) {
         String pathPrefix = path.isEmpty() ? "" : path + ".";
         return ((Map)object).keySet().stream().map(childPath -> pathPrefix + childPath).collect(Collectors.toCollection(LinkedHashSet::new));
      } else {
         return Collections.emptySet();
      }
   }

   private void collectKeysIntoSet(@NotNull String path, @NotNull Map<String, Object> map, @NotNull Set<String> result, boolean onlyLeafNodes) {
      for (Entry<String, Object> entry : map.entrySet()) {
         String childPath = path.isEmpty() ? entry.getKey() : path + "." + entry.getKey();
         if (!onlyLeafNodes || isLeafValue(entry.getValue())) {
            result.add(childPath);
         }

         if (entry.getValue() instanceof Map) {
            this.collectKeysIntoSet(childPath, (Map<String, Object>)entry.getValue(), result, onlyLeafNodes);
         }
      }
   }

   private static boolean isLeafValue(@Nullable Object o) {
      return !(o instanceof Map) || ((Map)o).isEmpty();
   }

   @Nullable
   protected Map<String, Object> loadFile(boolean splitDotPaths) {
      try (
         InputStream is = Files.newInputStream(this.path);
         InputStreamReader isr = new InputStreamReader(is, this.charset);
      ) {
         Map<Object, Object> rootMap = (Map<Object, Object>)new Yaml().load(isr);
         return this.normalizeMap(rootMap, splitDotPaths);
      } catch (IOException var40) {
         throw new ConfigMeException("Could not read file '" + this.path + "'", var40);
      } catch (ClassCastException var41) {
         throw new ConfigMeException("Top-level is not a map in '" + this.path + "'", var41);
      } catch (YAMLException var42) {
         throw new ConfigMeException("YAML error while trying to load file '" + this.path + "'", var42);
      }
   }

   @Nullable
   protected Map<String, Object> normalizeMap(@Nullable Map<Object, Object> map, boolean splitDotPaths) {
      return new MapNormalizer(splitDotPaths).normalizeMap(map);
   }

   @Deprecated
   @NotNull
   protected final File getFile() {
      return this.path.toFile();
   }

   @NotNull
   protected final Path getPath() {
      return this.path;
   }

   @Deprecated
   @Nullable
   protected final Map<String, Object> getRoot() {
      return this.root;
   }

   @Nullable
   protected <T> T getTypedObject(@NotNull String path, @NotNull Class<T> clazz) {
      Object value = this.getObject(path);
      return clazz.isInstance(value) ? clazz.cast(value) : null;
   }

   @Nullable
   private static Object getEntryIfIsMap(@NotNull String key, @Nullable Object value) {
      return value instanceof Map ? ((Map)value).get(key) : null;
   }
}
