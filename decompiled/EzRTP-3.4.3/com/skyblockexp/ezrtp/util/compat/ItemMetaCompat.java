package com.skyblockexp.ezrtp.util.compat;

import com.skyblockexp.ezrtp.util.MessageUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemMetaCompat {
   private ItemMetaCompat() {
   }

   public static void setDisplayName(ItemMeta meta, Component component) {
      if (meta != null && component != null) {
         try {
            String mmPreview = MessageUtil.serializeToMiniMessage(component);
            if (mmPreview != null) {
               boolean hasGradient = mmPreview.toLowerCase().contains("<gradient");
               boolean hasHex = Pattern.compile("(?i)<#([0-9a-fA-F]{1,6})>").matcher(mmPreview).find();
               if (hasGradient || hasHex) {
                  String legacy = MessageUtil.componentToLegacy(component);
                  String withoutHex = legacy.replaceAll("(?i)\\u00A7#[0-9a-fA-F]{6}", "");
                  String plain = MessageUtil.stripColors(withoutHex);
                  Component plainComponent = Component.text(plain);

                  for (Method m : meta.getClass().getMethods()) {
                     String name = m.getName();
                     if (name.equals("displayName") || name.equals("setDisplayName")) {
                        Class<?>[] params = m.getParameterTypes();
                        if (params.length == 1) {
                           try {
                              m.invoke(meta, plainComponent);
                              return;
                           } catch (InvocationTargetException | IllegalArgumentException var17) {
                           } catch (LinkageError | ReflectiveOperationException var18) {
                           }
                        }
                     }
                  }

                  try {
                     Method set = meta.getClass().getMethod("setDisplayName", String.class);
                     set.invoke(meta, plain);
                     return;
                  } catch (ReflectiveOperationException var22) {
                  }
               }
            }
         } catch (Throwable var23) {
         }

         for (Method mx : meta.getClass().getMethods()) {
            String name = mx.getName();
            if (name.equals("displayName") || name.equals("setDisplayName")) {
               Class<?>[] params = mx.getParameterTypes();
               if (params.length == 1) {
                  try {
                     mx.invoke(meta, component);
                     return;
                  } catch (InvocationTargetException | IllegalArgumentException var20) {
                     try {
                        String mm = MessageUtil.serializeToMiniMessage(component);
                        ClassLoader loader = safeClassLoader(meta.getClass().getClassLoader());
                        Class<?> mmClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage", true, loader);
                        Method factory = mmClass.getMethod("miniMessage");
                        Object mmInst = factory.invoke(null);
                        Method deser = mmClass.getMethod("deserialize", String.class);
                        Object targetComp = deser.invoke(mmInst, mm);
                        mx.invoke(meta, targetComp);
                        return;
                     } catch (Throwable var19) {
                     }
                  } catch (LinkageError | ReflectiveOperationException var21) {
                  }
               }
            }
         }

         try {
            Method set = meta.getClass().getMethod("setDisplayName", String.class);
            set.invoke(meta, MessageUtil.componentToLegacy(component));
         } catch (ReflectiveOperationException var16) {
         }
      }
   }

   public static void setLore(ItemMeta meta, List<Component> lore) {
      if (meta != null) {
         if (lore == null) {
            try {
               meta.setLore(null);
            } catch (Exception var9) {
            }
         } else {
            for (Method m : meta.getClass().getMethods()) {
               if (m.getName().equals("lore")) {
                  Class<?>[] params = m.getParameterTypes();
                  if (params.length == 1) {
                     try {
                        m.invoke(meta, lore);
                        return;
                     } catch (InvocationTargetException | IllegalArgumentException var14) {
                        try {
                           List<Object> converted = convertLoreForRuntime(lore, meta.getClass().getClassLoader());
                           m.invoke(meta, converted);
                           return;
                        } catch (Throwable var13) {
                        }
                     } catch (LinkageError | ReflectiveOperationException var15) {
                     }
                  }
               }
            }

            for (Method mx : meta.getClass().getMethods()) {
               if (mx.getName().equals("setLore")) {
                  Class<?>[] params = mx.getParameterTypes();
                  if (params.length == 1) {
                     try {
                        mx.invoke(meta, lore);
                        return;
                     } catch (InvocationTargetException | IllegalArgumentException var11) {
                        try {
                           List<Object> converted = convertLoreForRuntime(lore, meta.getClass().getClassLoader());
                           mx.invoke(meta, converted);
                           return;
                        } catch (Throwable var10) {
                        }
                     } catch (LinkageError | ReflectiveOperationException var12) {
                     }
                  }
               }
            }

            List<String> legacy = new ArrayList<>(lore.size());

            for (Component c : lore) {
               legacy.add(MessageUtil.componentToLegacy(c));
            }

            trySetLoreString(meta, legacy);
         }
      }
   }

   private static void trySetLoreString(ItemMeta meta, List<String> legacy) {
      for (Method m : meta.getClass().getMethods()) {
         if (m.getName().equals("setLore")) {
            Class<?>[] params = m.getParameterTypes();
            if (params.length == 1) {
               try {
                  m.invoke(meta, legacy);
                  return;
               } catch (IllegalArgumentException var8) {
               } catch (LinkageError | ReflectiveOperationException var9) {
               }
            }
         }
      }
   }

   private static List<Object> convertLoreForRuntime(List<Component> lore, ClassLoader loader) throws Exception {
      ClassLoader safeLoader = safeClassLoader(loader);
      Class<?> mmClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage", true, safeLoader);
      Method factory = mmClass.getMethod("miniMessage");
      Object mmInst = factory.invoke(null);
      Method deser = mmClass.getMethod("deserialize", String.class);
      List<Object> converted = new ArrayList<>(lore.size());

      for (Component c : lore) {
         String mm = MessageUtil.serializeToMiniMessage(c);
         converted.add(deser.invoke(mmInst, mm));
      }

      return converted;
   }

   private static ClassLoader safeClassLoader(ClassLoader loader) {
      return loader != null ? loader : ItemMetaCompat.class.getClassLoader();
   }
}
