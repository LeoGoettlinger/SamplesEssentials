package ch.jalu.configme.resource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MapNormalizer {
   private final boolean splitDotPaths;

   public MapNormalizer(boolean splitDotPaths) {
      this.splitDotPaths = splitDotPaths;
   }

   protected final boolean splitDotPaths() {
      return this.splitDotPaths;
   }

   @Nullable
   public Map<String, Object> normalizeMap(@Nullable Map<Object, Object> loadedMap) {
      return loadedMap == null ? null : this.createNormalizedMapIfNeeded(loadedMap).orElse(loadedMap);
   }

   @NotNull
   protected Optional<Map<String, Object>> createNormalizedMapIfNeeded(@NotNull Object value) {
      if (!(value instanceof Map)) {
         return Optional.empty();
      } else {
         Map<Object, Object> map = (Map<Object, Object>)value;
         boolean mapNeedsModification = false;

         for (Entry<Object, Object> entry : map.entrySet()) {
            this.createNormalizedMapIfNeeded(entry.getValue()).ifPresent(newMap -> map.put(entry.getKey(), newMap));
            if (!mapNeedsModification && this.isKeyInvalid(entry.getKey())) {
               mapNeedsModification = true;
            }
         }

         if (!mapNeedsModification) {
            return Optional.empty();
         } else {
            Map<String, Object> cleanedMap = new LinkedHashMap<>(map.size());

            for (Entry<Object, Object> entryx : map.entrySet()) {
               this.addValueIntoMap(cleanedMap, Objects.toString(entryx.getKey()), entryx.getValue());
            }

            return Optional.of(cleanedMap);
         }
      }
   }

   protected boolean isKeyInvalid(@NotNull Object key) {
      return !(key instanceof String) ? true : this.splitDotPaths && ((String)key).contains(".");
   }

   protected void addValueIntoMap(@NotNull Map<String, Object> map, @NotNull String path, @NotNull Object value) {
      int dotPosition = this.splitDotPaths ? path.indexOf(".") : -1;
      if (dotPosition > -1) {
         String pathElement = path.substring(0, dotPosition);
         Map<String, Object> mapAtPath = this.getOrInsertMap(map, pathElement);
         this.addValueIntoMap(mapAtPath, path.substring(dotPosition + 1), value);
      } else if (value instanceof Map) {
         Map<String, Object> mapAtPath = this.getOrInsertMap(map, path);
         Map<?, ?> mapValue = (Map<?, ?>)value;
         mapValue.forEach((entryKey, entryValue) -> this.addValueIntoMap(mapAtPath, Objects.toString(entryKey), entryValue));
      } else {
         map.put(path, value);
      }
   }

   @NotNull
   protected Map<String, Object> getOrInsertMap(@NotNull Map<String, Object> parentMap, @NotNull String path) {
      Object value = parentMap.get(path);
      if (value instanceof Map) {
         return (Map<String, Object>)value;
      } else {
         Map<String, Object> newMap = new LinkedHashMap<>();
         parentMap.put(path, newMap);
         return newMap;
      }
   }
}
