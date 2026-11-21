package github.nighter.smartspawner.language.section;

import github.nighter.smartspawner.language.format.ColorUtil;
import github.nighter.smartspawner.language.format.LanguageComponentFormatter;
import github.nighter.smartspawner.utils.LRUCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

public final class GuiLanguageSection {
   private final String label;
   private final Supplier<YamlConfiguration> configSupplier;
   private final BooleanSupplier enabled;
   private final BiFunction<String, Map<String, String>, String> formatter;
   private final LRUCache<String, String> nameCache;
   private final LRUCache<String, String[]> loreCache;
   private final LRUCache<String, List<String>> loreListCache;
   private final AtomicInteger cacheHits = new AtomicInteger();
   private final AtomicInteger cacheMisses = new AtomicInteger();

   public GuiLanguageSection(
      String label,
      Supplier<YamlConfiguration> configSupplier,
      BooleanSupplier enabled,
      BiFunction<String, Map<String, String>, String> formatter,
      int stringCacheSize,
      int loreCacheSize,
      int loreListCacheSize
   ) {
      this.label = label;
      this.configSupplier = configSupplier;
      this.enabled = enabled;
      this.formatter = formatter;
      this.nameCache = new LRUCache<>(stringCacheSize);
      this.loreCache = new LRUCache<>(loreCacheSize);
      this.loreListCache = new LRUCache<>(loreListCacheSize);
   }

   public String title(String key) {
      return this.title(key, Collections.emptyMap());
   }

   public String title(String key, Map<String, String> placeholders) {
      if (!this.enabled.getAsBoolean()) {
         return null;
      } else {
         String title = this.config().getString(key);
         return title == null ? "Missing " + this.label + " title: " + key : this.formatter.apply(title, placeholders);
      }
   }

   public String configString(String key, String defaultValue) {
      if (!this.enabled.getAsBoolean()) {
         return defaultValue;
      } else {
         String value = this.config().getString(key);
         return value != null && !value.isBlank() ? value : defaultValue;
      }
   }

   public boolean contains(String key) {
      return this.enabled.getAsBoolean() && this.config().isString(key);
   }

   public String name(String key) {
      return this.name(key, Collections.emptyMap());
   }

   public String name(String key, Map<String, String> placeholders) {
      if (!this.enabled.getAsBoolean()) {
         return null;
      } else {
         String cacheKey = this.cacheKey(key, placeholders);
         String cached = this.nameCache.get(cacheKey);
         if (cached != null) {
            this.cacheHits.incrementAndGet();
            return cached;
         } else {
            this.cacheMisses.incrementAndGet();
            String name = this.config().getString(key);
            if (name == null) {
               return "Missing " + this.label + " item name: " + key;
            } else {
               String result = this.formatter.apply(name, placeholders);
               this.nameCache.put(cacheKey, result);
               return result;
            }
         }
      }
   }

   public String[] lore(String key) {
      return this.lore(key, Collections.emptyMap());
   }

   public String[] lore(String key, Map<String, String> placeholders) {
      if (!this.enabled.getAsBoolean()) {
         return new String[0];
      } else {
         String cacheKey = this.cacheKey(key, placeholders);
         String[] cached = this.loreCache.get(cacheKey);
         if (cached != null) {
            this.cacheHits.incrementAndGet();
            return cached;
         } else {
            this.cacheMisses.incrementAndGet();
            String[] result = this.config().getStringList(key).stream().map(line -> this.formatter.apply(line, placeholders)).toArray(String[]::new);
            this.loreCache.put(cacheKey, result);
            return result;
         }
      }
   }

   public List<String> loreList(String key) {
      return this.loreList(key, Collections.emptyMap());
   }

   public List<String> loreList(String key, Map<String, String> placeholders) {
      if (!this.enabled.getAsBoolean()) {
         return Collections.emptyList();
      } else {
         String cacheKey = this.cacheKey(key, placeholders);
         List<String> cached = this.loreListCache.get(cacheKey);
         if (cached != null) {
            this.cacheHits.incrementAndGet();
            return cached;
         } else {
            this.cacheMisses.incrementAndGet();
            List<String> result = this.config().getStringList(key).stream().map(line -> this.formatter.apply(line, placeholders)).toList();
            this.loreListCache.put(cacheKey, result);
            return result;
         }
      }
   }

   public List<String> loreWithMultilinePlaceholders(String key, Map<String, String> placeholders) {
      if (!this.enabled.getAsBoolean()) {
         return Collections.emptyList();
      } else {
         List<String> result = new ArrayList<>();

         for (String line : this.config().getStringList(key)) {
            String multilineKey = this.findMultilinePlaceholder(line, placeholders);
            if (multilineKey == null) {
               result.add(this.formatter.apply(line, placeholders));
            } else {
               Map<String, String> singleLinePlaceholders = new HashMap<>(placeholders);
               String multilineValue = singleLinePlaceholders.remove(multilineKey);
               String processedLine = this.replacePlaceholders(line, singleLinePlaceholders);
               String placeholder = "{" + multilineKey + "}";
               String lineStart = processedLine.substring(0, processedLine.indexOf(placeholder));
               String[] valueLines = multilineValue.split("\n");
               result.add(ColorUtil.translateHexColorCodes(processedLine.replace(placeholder, valueLines[0])));

               for (int i = 1; i < valueLines.length; i++) {
                  result.add(ColorUtil.translateHexColorCodes(lineStart + valueLines[i]));
               }
            }
         }

         return result;
      }
   }

   public Component translatableLootLine(String templateKey, Material material, String amount, String chance) {
      return LanguageComponentFormatter.translatableLootLine(this.config().getString(templateKey), material, amount, chance);
   }

   public Component translatableLootLine(String templateKey, Component itemName, String amount, String chance) {
      return LanguageComponentFormatter.translatableLootLine(this.config().getString(templateKey), itemName, amount, chance);
   }

   public List<Component> loreWithItemName(String key, Map<String, String> placeholders, String placeholder, Component itemName) {
      return !this.enabled.getAsBoolean()
         ? Collections.emptyList()
         : LanguageComponentFormatter.loreWithItemName(
            this.config().getStringList(key), line -> this.formatter.apply(line, placeholders), placeholder, itemName
         );
   }

   public List<Component> loreComponents(String key, Map<String, String> placeholders, List<Component> lootItemComponents, String emptyLootKey) {
      return !this.enabled.getAsBoolean()
         ? Collections.emptyList()
         : LanguageComponentFormatter.loreComponents(
            this.config().getStringList(key),
            line -> this.formatter.apply(line, placeholders),
            () -> this.config().getString(emptyLootKey, ""),
            lootItemComponents
         );
   }

   public String colorCode(String path) {
      if (!this.enabled.getAsBoolean()) {
         return ChatColor.WHITE.toString();
      } else {
         String color = this.config().getString(path);
         return color == null ? ChatColor.WHITE.toString() : this.formatter.apply(color, Collections.emptyMap());
      }
   }

   public void clearCache() {
      this.nameCache.clear();
      this.loreCache.clear();
      this.loreListCache.clear();
   }

   public void addCacheStats(Map<String, Object> stats, String prefix) {
      stats.put(prefix + "_name_cache_size", this.nameCache.size());
      stats.put(prefix + "_name_cache_capacity", this.nameCache.capacity());
      stats.put(prefix + "_lore_cache_size", this.loreCache.size());
      stats.put(prefix + "_lore_cache_capacity", this.loreCache.capacity());
      stats.put(prefix + "_lore_list_cache_size", this.loreListCache.size());
      stats.put(prefix + "_lore_list_cache_capacity", this.loreListCache.capacity());
      stats.put(prefix + "_cache_hits", this.cacheHits.get());
      stats.put(prefix + "_cache_misses", this.cacheMisses.get());
   }

   private YamlConfiguration config() {
      YamlConfiguration config = this.configSupplier.get();
      return config == null ? new YamlConfiguration() : config;
   }

   private String cacheKey(String key, Map<String, String> placeholders) {
      if (placeholders != null && !placeholders.isEmpty()) {
         StringBuilder builder = new StringBuilder(key);
         List<String> keys = new ArrayList<>(placeholders.keySet());
         Collections.sort(keys);

         for (String placeholderKey : keys) {
            builder.append('|').append(placeholderKey).append('=').append(placeholders.get(placeholderKey));
         }

         return builder.toString();
      } else {
         return key;
      }
   }

   private String findMultilinePlaceholder(String line, Map<String, String> placeholders) {
      if (placeholders != null && !placeholders.isEmpty()) {
         for (Entry<String, String> entry : placeholders.entrySet()) {
            String value = entry.getValue();
            if (value != null && value.contains("\n") && line.contains("{" + entry.getKey() + "}")) {
               return entry.getKey();
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private String replacePlaceholders(String line, Map<String, String> placeholders) {
      String result = line;

      for (Entry<String, String> entry : placeholders.entrySet()) {
         result = result.replace("{" + entry.getKey() + "}", entry.getValue());
      }

      return result;
   }
}
