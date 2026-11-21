package com.skyblockexp.ezrtp.util;

import com.skyblockexp.ezrtp.util.compat.ItemMetaCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

public final class LoreUtil {
   private LoreUtil() {
   }

   public static void setLore(ItemMeta meta, List<String> rawLore, Player player, Logger logger) {
      if (rawLore.isEmpty()) {
         logger.info("LoreUtil.setLore: rawLore is empty, skipping");
      } else {
         logger.info("LoreUtil.setLore: rawLore = " + rawLore);
         logger.info("LoreUtil.setLore: hasMethod = " + hasMethod(meta.getClass(), "lore", List.class));
         if (hasMethod(meta.getClass(), "lore", List.class)) {
            logger.info("LoreUtil.setLore: Using Paper API");
            List<Component> lore = new ArrayList<>();

            for (String line : rawLore) {
               String resolvedLine = PlaceholderUtil.resolvePlaceholders(player, line, logger);
               lore.add(MessageUtil.parseMiniMessage(resolvedLine));
            }

            ItemMetaCompat.setLore(meta, lore);
            logger.info("LoreUtil.setLore: Set Paper lore: " + lore);
         } else {
            logger.info("LoreUtil.setLore: Using Spigot API");
            List<String> resolvedLore = new ArrayList<>();

            for (String line : rawLore) {
               String resolvedLine = PlaceholderUtil.resolvePlaceholders(player, line, logger);
               logger.info("LoreUtil.setLore: resolvedLine = " + resolvedLine);
               Component component = MessageUtil.parseMiniMessage(resolvedLine);
               String legacy = MessageUtil.componentToLegacy(component);
               logger.info("LoreUtil.setLore: legacy = " + legacy);
               resolvedLore.add(legacy);
            }

            ItemMetaCompat.setLore(meta, loreFromComponents(resolvedLore));
            logger.info("LoreUtil.setLore: final lore = " + resolvedLore);
            logger.info("LoreUtil.setLore: meta.hasLore() = " + meta.hasLore());
            if (meta.hasLore()) {
               logger.info("LoreUtil.setLore: meta.getLore() = " + meta.getLore());
            }
         }
      }
   }

   public static void setDisplayName(ItemMeta meta, String rawDisplayName, Player player, Logger logger) {
      if (rawDisplayName != null && !rawDisplayName.isBlank()) {
         String resolvedName = PlaceholderUtil.resolvePlaceholders(player, rawDisplayName, logger);
         Component component = MessageUtil.parseMiniMessage(resolvedName);
         ItemMetaCompat.setDisplayName(meta, component);
      }
   }

   private static List<Component> loreFromComponents(List<String> legacyLines) {
      List<Component> list = new ArrayList<>();

      for (String s : legacyLines) {
         if (s == null) {
            list.add(Component.empty());
         } else {
            String amp = s.replace('§', '&');
            list.add(MessageUtil.legacyToComponent(amp));
         }
      }

      return list;
   }

   public static boolean validateMiniMessage(String miniMessageString, String context, Logger logger) {
      if (miniMessageString != null && !miniMessageString.isBlank()) {
         try {
            MessageUtil.parseMiniMessage(miniMessageString);
            return true;
         } catch (Exception var4) {
            logger.warning("Invalid MiniMessage in " + context + ": '" + miniMessageString + "' - " + var4.getMessage());
            return false;
         }
      } else {
         return true;
      }
   }

   private static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
      try {
         clazz.getMethod(methodName, parameterTypes);
         return true;
      } catch (NoSuchMethodException var4) {
         return false;
      }
   }
}
