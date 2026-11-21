package com.earth2me.essentials.config;

import com.earth2me.essentials.libs.configurate.CommentedConfigurationNode;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public final class ConfigurateUtil {
   private ConfigurateUtil() {
   }

   public static Set<String> getRootNodeKeys(final EssentialsConfiguration config) {
      return getKeys(config.getRootNode());
   }

   public static Set<String> getKeys(final CommentedConfigurationNode node) {
      if (node != null && node.isMap()) {
         Set<String> keys = new LinkedHashSet<>();

         for (Object obj : node.childrenMap().keySet()) {
            keys.add(String.valueOf(obj));
         }

         return keys;
      } else {
         return Collections.emptySet();
      }
   }

   public static Map<String, CommentedConfigurationNode> getMap(final CommentedConfigurationNode node) {
      if (node != null && node.isMap()) {
         Map<String, CommentedConfigurationNode> map = new LinkedHashMap<>();

         for (Entry<Object, CommentedConfigurationNode> entry : node.childrenMap().entrySet()) {
            map.put(String.valueOf(entry.getKey()), entry.getValue());
         }

         return map;
      } else {
         return Collections.emptyMap();
      }
   }

   public static Map<String, Object> getRawMap(final EssentialsConfiguration config, final String key) {
      return config != null && key != null ? getRawMap(config.getSection(key)) : Collections.emptyMap();
   }

   public static Map<String, Object> getRawMap(final CommentedConfigurationNode node) {
      if (node != null && node.isMap()) {
         Map<String, Object> map = new LinkedHashMap<>();

         for (Entry<Object, CommentedConfigurationNode> entry : node.childrenMap().entrySet()) {
            map.put(String.valueOf(entry.getKey()), entry.getValue().raw());
         }

         return map;
      } else {
         return Collections.emptyMap();
      }
   }

   public static List<Map<?, ?>> getMapList(final CommentedConfigurationNode node) {
      List<?> list = null;

      try {
         list = node.getList(Object.class);
      } catch (SerializationException var5) {
      }

      List<Map<?, ?>> result = new ArrayList<>();
      if (list == null) {
         return result;
      } else {
         for (Object object : list) {
            if (object instanceof Map) {
               result.add((Map<?, ?>)object);
            }
         }

         return result;
      }
   }

   public static BigDecimal toBigDecimal(final String input, final BigDecimal def) {
      if (input != null && !input.isEmpty()) {
         try {
            return new BigDecimal(input, MathContext.DECIMAL128);
         } catch (ArithmeticException | NumberFormatException var3) {
            return def;
         }
      } else {
         return def;
      }
   }

   public static boolean isDouble(final CommentedConfigurationNode node) {
      return node != null && node.raw() instanceof Double;
   }

   public static boolean isInt(final CommentedConfigurationNode node) {
      return node != null && node.raw() instanceof Integer;
   }

   public static boolean isString(final CommentedConfigurationNode node) {
      return node != null && node.raw() instanceof String;
   }
}
