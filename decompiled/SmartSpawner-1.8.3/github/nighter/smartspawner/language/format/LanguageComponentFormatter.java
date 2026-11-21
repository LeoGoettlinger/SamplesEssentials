package github.nighter.smartspawner.language.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;

public final class LanguageComponentFormatter {
   private static final Pattern HEX_TEMPLATE_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
   private static final Map<Character, TextColor> LEGACY_COLOR_MAP;

   private LanguageComponentFormatter() {
   }

   public static Component translatableLootLine(String template, Material material, String amount, String chance) {
      return translatableLootLine(template, Component.translatable(material.translationKey()), amount, chance);
   }

   public static Component translatableLootLine(String template, Component itemName, String amount, String chance) {
      if (template == null) {
         return noItalic(((TextComponent)Component.text(amount + " ").append(itemName)).append(Component.text(" (" + chance + ")")));
      } else {
         String resolved = template.replace("{amount}", amount).replace("{chance}", chance);
         String placeholder = "{item_name}";
         int index = resolved.indexOf(placeholder);
         if (index < 0) {
            return noItalic(LegacyComponentSerializer.legacySection().deserialize(ColorUtil.translateHexColorCodes(resolved)));
         } else {
            String beforeRaw = resolved.substring(0, index);
            String afterRaw = resolved.substring(index + placeholder.length());
            TextColor itemColor = extractLastColor(beforeRaw, TextColor.color(16777215));
            Component before = LegacyComponentSerializer.legacySection().deserialize(ColorUtil.translateHexColorCodes(beforeRaw));
            Component after = LegacyComponentSerializer.legacySection().deserialize(ColorUtil.translateHexColorCodes(afterRaw));
            return noItalic(before.append(itemName.colorIfAbsent(itemColor)).append(after));
         }
      }
   }

   public static List<Component> loreWithItemName(List<String> template, Function<String, String> lineFormatter, String placeholder, Component itemName) {
      LegacyComponentSerializer legacySerial = LegacyComponentSerializer.legacySection();
      List<Component> result = new ArrayList<>(template.size());

      for (String line : template) {
         int index = line.indexOf(placeholder);
         if (index < 0) {
            result.add(noItalic(legacySerial.deserialize(lineFormatter.apply(line))));
         } else {
            String beforeRaw = line.substring(0, index);
            String afterRaw = line.substring(index + placeholder.length());
            TextColor itemColor = extractLastColor(beforeRaw, TextColor.color(16777215));
            Component before = legacySerial.deserialize(lineFormatter.apply(beforeRaw));
            Component after = legacySerial.deserialize(lineFormatter.apply(afterRaw));
            result.add(noItalic(before.append(itemName.colorIfAbsent(itemColor)).append(after)));
         }
      }

      return result;
   }

   public static List<Component> loreComponents(
      List<String> template, Function<String, String> lineFormatter, Supplier<String> emptyLootLine, List<Component> lootItemComponents
   ) {
      LegacyComponentSerializer legacySerial = LegacyComponentSerializer.legacySection();
      List<Component> result = new ArrayList<>(template.size() + lootItemComponents.size());

      for (String line : template) {
         if (line.contains("{loot_items}")) {
            if (lootItemComponents.isEmpty()) {
               String emptyRaw = emptyLootLine.get();
               result.add(noItalic(legacySerial.deserialize(ColorUtil.translateHexColorCodes(emptyRaw == null ? "" : emptyRaw))));
            } else {
               result.addAll(lootItemComponents);
            }
         } else {
            result.add(noItalic(legacySerial.deserialize(lineFormatter.apply(line))));
         }
      }

      return result;
   }

   private static Component noItalic(Component component) {
      return component.decoration(TextDecoration.ITALIC, false);
   }

   private static TextColor extractLastColor(String text, TextColor defaultColor) {
      if (text != null && !text.isEmpty()) {
         TextColor last = null;
         int lastPosition = -1;
         Matcher matcher = HEX_TEMPLATE_PATTERN.matcher(text);

         while (matcher.find()) {
            if (matcher.start() > lastPosition) {
               lastPosition = matcher.start();
               last = TextColor.color(Integer.parseInt(matcher.group(1), 16));
            }
         }

         for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '&') {
               TextColor legacyColor = LEGACY_COLOR_MAP.get(Character.toLowerCase(text.charAt(i + 1)));
               if (legacyColor != null && i > lastPosition) {
                  lastPosition = i;
                  last = legacyColor;
               }
            }
         }

         return last != null ? last : defaultColor;
      } else {
         return defaultColor;
      }
   }

   static {
      Map<Character, TextColor> colors = new HashMap<>(16);
      colors.put('0', TextColor.color(0));
      colors.put('1', TextColor.color(170));
      colors.put('2', TextColor.color(43520));
      colors.put('3', TextColor.color(43690));
      colors.put('4', TextColor.color(11141120));
      colors.put('5', TextColor.color(11141290));
      colors.put('6', TextColor.color(16755200));
      colors.put('7', TextColor.color(11184810));
      colors.put('8', TextColor.color(5592405));
      colors.put('9', TextColor.color(5592575));
      colors.put('a', TextColor.color(5635925));
      colors.put('b', TextColor.color(5636095));
      colors.put('c', TextColor.color(16733525));
      colors.put('d', TextColor.color(16733695));
      colors.put('e', TextColor.color(16777045));
      colors.put('f', TextColor.color(16777215));
      LEGACY_COLOR_MAP = Collections.unmodifiableMap(colors);
   }
}
