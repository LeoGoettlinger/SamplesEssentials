package com.skyblockexp.ezrtp.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemFlagCompat {
   private static final Set<String> SAFE_FLAGS = Set.of(
      "HIDE_ENCHANTS", "HIDE_ATTRIBUTES", "HIDE_UNBREAKABLE", "HIDE_DESTROYS", "HIDE_PLACED_ON", "HIDE_POTION_EFFECTS", "HIDE_DYE", "HIDE_ARMOR_TRIM"
   );

   private ItemFlagCompat() {
   }

   public static void applyStandardHideFlags(ItemMeta meta) {
      if (meta != null) {
         try {
            Class<?> itemFlagClass = Class.forName("org.bukkit.inventory.ItemFlag");
            Object[] constants = itemFlagClass.getEnumConstants();
            if (constants == null || constants.length == 0) {
               return;
            }

            List<Object> toAdd = new ArrayList<>();

            for (Object c : constants) {
               if (c instanceof Enum && SAFE_FLAGS.contains(((Enum)c).name())) {
                  toAdd.add(c);
               }
            }

            if (toAdd.isEmpty()) {
               return;
            }

            Method addItemFlagsMethod = null;

            for (Method m : meta.getClass().getMethods()) {
               if (m.getName().equals("addItemFlags")) {
                  Class<?>[] params = m.getParameterTypes();
                  if (params.length == 1 && params[0].isArray() && params[0].getComponentType().equals(itemFlagClass)) {
                     addItemFlagsMethod = m;
                     break;
                  }
               }
            }

            if (addItemFlagsMethod == null) {
               return;
            }

            Object arr = Array.newInstance(itemFlagClass, toAdd.size());

            for (int i = 0; i < toAdd.size(); i++) {
               Array.set(arr, i, toAdd.get(i));
            }

            addItemFlagsMethod.invoke(meta, arr);
         } catch (ClassNotFoundException var10) {
         } catch (InvocationTargetException | IllegalAccessException var11) {
         }
      }
   }

   public static void setItemMetaCompatibly(ItemStack itemStack, ItemMeta meta) {
      if (itemStack != null) {
         try {
            Method setItemMetaMethod = itemStack.getClass().getMethod("setItemMeta", ItemMeta.class);
            Object result = setItemMetaMethod.invoke(itemStack, meta);
            if (result instanceof Boolean && !(Boolean)result) {
               try {
                  Method oldSetItemMetaMethod = itemStack.getClass().getMethod("setItemMeta", ItemMeta.class);
                  oldSetItemMetaMethod.invoke(itemStack, meta);
                  return;
               } catch (Exception var6) {
                  return;
               }
            }
         } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException var7) {
            try {
               Method oldSetItemMetaMethod = itemStack.getClass().getMethod("setItemMeta", ItemMeta.class);
               oldSetItemMetaMethod.invoke(itemStack, meta);
            } catch (Exception var5) {
               throw new RuntimeException("Failed to set item meta", var5);
            }
         }
      }
   }
}
