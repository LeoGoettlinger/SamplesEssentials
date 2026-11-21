package com.magmaguy.elitemobs.magmacore.enchantments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.inventory.ItemStack;

final class EnchantmentValues {
   private static final int MAX_NODES = 2048;
   private static final int MAX_DEPTH = 16;
   private static final int MAX_STRING = 8192;

   private EnchantmentValues() {
   }

   static Map<String, Object> copy(Map<?, ?> value) {
      if (value == null) {
         throw new IllegalArgumentException("Missing payload");
      } else {
         return (Map<String, Object>)copy(value, 0, new int[]{0});
      }
   }

   private static Object copy(Object value, int depth, int[] count) {
      if (++count[0] > 2048 || depth > 16) {
         throw new IllegalArgumentException("Enchantment payload exceeds structural limits");
      } else if (value instanceof String string) {
         if (string.length() > 8192) {
            throw new IllegalArgumentException("Payload string too long");
         } else {
            return string;
         }
      } else if (!(value instanceof Boolean)
         && !(value instanceof UUID)
         && !(value instanceof Integer)
         && !(value instanceof Long)
         && !(value instanceof Short)
         && !(value instanceof Byte)) {
         if (value instanceof Double number && Double.isFinite(number)) {
            return number;
         } else if (value instanceof Float number && Float.isFinite(number)) {
            return number;
         } else if (value instanceof ItemStack item) {
            return item.clone();
         } else if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();

            for (Entry<?, ?> entry : map.entrySet()) {
               if (!(entry.getKey() instanceof String key) || key.length() > 128) {
                  throw new IllegalArgumentException("Payload keys must be strings of at most 128 characters");
               }

               result.put(key, copy(entry.getValue(), depth + 1, count));
            }

            return Collections.unmodifiableMap(result);
         } else if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("Unsupported enchantment payload value");
         } else {
            List<Object> result = new ArrayList<>();

            for (Object entry : list) {
               result.add(copy(entry, depth + 1, count));
            }

            return Collections.unmodifiableList(result);
         }
      } else {
         return value;
      }
   }
}
