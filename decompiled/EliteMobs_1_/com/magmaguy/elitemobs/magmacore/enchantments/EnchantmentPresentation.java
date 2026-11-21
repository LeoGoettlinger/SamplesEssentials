package com.magmaguy.elitemobs.magmacore.enchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

final class EnchantmentPresentation {
   private static final NamespacedKey ROOT = EnchantmentItemData.key("enchantment_presentation");
   private static final NamespacedKey LINES = EnchantmentItemData.key("lines");
   private static final NamespacedKey POSITION = EnchantmentItemData.key("position");
   private static final NamespacedKey LABELS = EnchantmentItemData.key("labels");
   private static final NamespacedKey NAME = EnchantmentItemData.key("name");
   private static final NamespacedKey MAX = EnchantmentItemData.key("max");
   private static final NamespacedKey CURSE = EnchantmentItemData.key("curse");
   private static final NamespacedKey ORIGINAL_GLINT = EnchantmentItemData.key("original_glint");
   private static final NamespacedKey APPLIED_GLINT = EnchantmentItemData.key("applied_glint");

   private EnchantmentPresentation() {
   }

   static void render(ItemMeta meta, Map<String, Integer> entries, Function<String, EnchantmentItems.Resolved> resolver) {
      render(meta, entries, resolver, UnaryOperator.identity());
   }

   static void render(
      ItemMeta meta, Map<String, Integer> entries, Function<String, EnchantmentItems.Resolved> resolver, UnaryOperator<List<String>> rebuildHostLore
   ) {
      render(meta, entries, resolver, rebuildHostLore, null);
   }

   static void render(
      ItemMeta meta,
      Map<String, Integer> entries,
      Function<String, EnchantmentItems.Resolved> resolver,
      UnaryOperator<List<String>> rebuildHostLore,
      ToIntFunction<List<String>> enchantmentPosition
   ) {
      PersistentDataContainer data = meta.getPersistentDataContainer();
      PersistentDataContainer old = (PersistentDataContainer)data.get(ROOT, PersistentDataType.TAG_CONTAINER);
      if (data.has(ROOT) && old == null) {
         throw new IllegalArgumentException("Malformed enchantment presentation record");
      } else {
         List<String> lore = meta.hasLore() ? new ArrayList<>(Objects.requireNonNull(meta.getLore())) : new ArrayList<>();
         Boolean originalGlint = meta.hasEnchantmentGlintOverride() ? meta.getEnchantmentGlintOverride() : null;
         PersistentDataContainer oldLabels = null;
         int position = 0;
         if (old != null) {
            if (!Integer.valueOf(1).equals(old.get(EnchantmentItemData.VERSION, PersistentDataType.INTEGER))) {
               throw new IllegalArgumentException("Unknown enchantment presentation version");
            }

            Integer storedPosition = (Integer)old.get(POSITION, PersistentDataType.INTEGER);
            if (old.has(POSITION) && storedPosition == null) {
               throw new IllegalArgumentException("Malformed enchantment presentation position");
            }

            position = storedPosition == null ? 0 : storedPosition;
            List<String> section = (List<String>)old.get(LINES, PersistentDataType.LIST.strings());
            int start = Math.max(0, position);
            if (position < -1
               || section == null
               || section.size() > 64
               || position == -1 && !section.isEmpty()
               || start > lore.size()
               || section.size() > lore.size() - start
               || !lore.subList(start, start + section.size()).equals(section)) {
               throw new IllegalArgumentException("Generated enchantment lore was changed externally; rebuild from the host definition");
            }

            lore.subList(start, start + section.size()).clear();
            Boolean applied = decodeGlint((Integer)old.get(APPLIED_GLINT, PersistentDataType.INTEGER));
            if (Objects.equals(originalGlint, applied)) {
               originalGlint = decodeGlint((Integer)old.get(ORIGINAL_GLINT, PersistentDataType.INTEGER));
            }

            oldLabels = (PersistentDataContainer)old.get(LABELS, PersistentDataType.TAG_CONTAINER);
         }

         lore = List.copyOf(rebuildHostLore.apply(List.copyOf(lore)));
         if (enchantmentPosition != null) {
            position = enchantmentPosition.applyAsInt(lore);
         }

         if (position >= -1 && position <= lore.size()) {
            PersistentDataContainer labels = data.getAdapterContext().newPersistentDataContainer();
            List<String> lines = new ArrayList<>();

            for (Entry<String, Integer> entry : new TreeMap<>(entries).entrySet()) {
               EnchantmentItems.Resolved resolved = resolver.apply(entry.getKey());
               EnchantmentDefinition definition = resolved == null ? null : resolved.definition();
               boolean available = resolved != null && resolved.available();
               NamespacedKey key = Objects.requireNonNull(NamespacedKey.fromString(entry.getKey()));
               PersistentDataContainer prior = oldLabels == null ? null : (PersistentDataContainer)oldLabels.get(key, PersistentDataType.TAG_CONTAINER);
               String name = definition != null ? definition.name() : (prior == null ? null : (String)prior.get(NAME, PersistentDataType.STRING));
               Integer maximum = definition != null ? definition.maxLevel() : (prior == null ? null : (Integer)prior.get(MAX, PersistentDataType.INTEGER));
               boolean curse = definition != null
                  ? definition.curse()
                  : prior != null && Byte.valueOf((byte)1).equals(prior.get(CURSE, PersistentDataType.BYTE));
               if (name == null) {
                  name = "Unavailable enchantment";
               }

               if (maximum == null) {
                  maximum = Integer.MAX_VALUE;
               }

               lines.add(
                  "§r"
                     + (curse ? "§c" : "§7")
                     + name
                     + (entry.getValue() == 1 && maximum == 1 ? "" : " " + level(entry.getValue()))
                     + (available ? "" : " §8(inactive)")
               );
               PersistentDataContainer label = data.getAdapterContext().newPersistentDataContainer();
               label.set(NAME, PersistentDataType.STRING, name);
               label.set(MAX, PersistentDataType.INTEGER, maximum);
               label.set(CURSE, PersistentDataType.BYTE, (byte)(curse ? 1 : 0));
               labels.set(key, PersistentDataType.TAG_CONTAINER, label);
            }

            Boolean appliedGlint = originalGlint == null && !entries.isEmpty() ? Boolean.TRUE : originalGlint;
            meta.setEnchantmentGlintOverride(appliedGlint);
            List<String> visibleLines = position == -1 ? List.of() : lines;
            List<String> combined = new ArrayList<>(lore);
            int startx = Math.max(0, position);
            combined.addAll(startx, visibleLines);
            meta.setLore(combined.isEmpty() ? null : combined);
            if (entries.isEmpty() && position == 0) {
               data.remove(ROOT);
            } else {
               PersistentDataContainer record = data.getAdapterContext().newPersistentDataContainer();
               record.set(EnchantmentItemData.VERSION, PersistentDataType.INTEGER, 1);
               record.set(POSITION, PersistentDataType.INTEGER, position);
               record.set(
                  LINES,
                  PersistentDataType.LIST.strings(),
                  visibleLines.isEmpty() ? List.of() : List.copyOf(Objects.requireNonNull(meta.getLore()).subList(startx, startx + visibleLines.size()))
               );
               record.set(LABELS, PersistentDataType.TAG_CONTAINER, labels);
               record.set(ORIGINAL_GLINT, PersistentDataType.INTEGER, encodeGlint(originalGlint));
               record.set(APPLIED_GLINT, PersistentDataType.INTEGER, encodeGlint(appliedGlint));
               data.set(ROOT, PersistentDataType.TAG_CONTAINER, record);
            }
         } else {
            throw new IllegalArgumentException("Enchantment section position is outside host lore");
         }
      }
   }

   static String level(int level) {
      if (level < 1) {
         throw new IllegalArgumentException("Level must be positive");
      } else if (level > 3999) {
         return Integer.toString(level);
      } else {
         int[] values = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
         String[] letters = new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
         StringBuilder result = new StringBuilder();

         for (int i = 0; i < values.length; i++) {
            while (level >= values[i]) {
               result.append(letters[i]);
               level -= values[i];
            }
         }

         return result.toString();
      }
   }

   private static int encodeGlint(Boolean value) {
      return value == null ? -1 : (value ? 1 : 0);
   }

   private static Boolean decodeGlint(Integer value) {
      if (value != null && value >= -1 && value <= 1) {
         return value == -1 ? null : value == 1;
      } else {
         throw new IllegalArgumentException("Malformed glint ownership");
      }
   }
}
