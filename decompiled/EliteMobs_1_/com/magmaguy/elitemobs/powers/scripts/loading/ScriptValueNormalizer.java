package com.magmaguy.elitemobs.powers.scripts.loading;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.configuration.ConfigurationSection;

public final class ScriptValueNormalizer {
   private ScriptValueNormalizer() {
   }

   public static Map<String, Object> normalizeSection(ConfigurationSection configurationSection) {
      return (Map<String, Object>)(configurationSection == null ? new LinkedHashMap<>() : normalizeMap(configurationSection.getValues(false)));
   }

   public static Map<String, Object> normalizeMap(Map<?, ?> values) {
      LinkedHashMap<String, Object> normalizedValues = new LinkedHashMap<>();
      if (values == null) {
         return normalizedValues;
      } else {
         for (Entry<?, ?> entry : values.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
               throw new IllegalArgumentException("Script maps only support string keys. Invalid key: " + entry.getKey());
            }

            normalizedValues.put(key, normalizeValue(entry.getValue()));
         }

         return normalizedValues;
      }
   }

   public static Object normalizeValue(Object value) {
      if (value instanceof ConfigurationSection configurationSection) {
         return normalizeSection(configurationSection);
      } else if (value instanceof Map<?, ?> mapValue) {
         return normalizeMap(mapValue);
      } else if (!(value instanceof List<?> listValue)) {
         return value;
      } else {
         List<Object> normalizedList = new ArrayList<>();

         for (Object listEntry : listValue) {
            normalizedList.add(normalizeValue(listEntry));
         }

         return normalizedList;
      }
   }
}
