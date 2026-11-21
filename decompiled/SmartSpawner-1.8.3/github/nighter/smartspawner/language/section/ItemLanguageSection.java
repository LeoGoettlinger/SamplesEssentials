package github.nighter.smartspawner.language.section;

import github.nighter.smartspawner.language.cache.LanguageCache;
import github.nighter.smartspawner.language.file.LocaleData;
import github.nighter.smartspawner.language.format.LanguageComponentFormatter;
import github.nighter.smartspawner.language.format.PlaceholderFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public final class ItemLanguageSection {
   private static final Map<String, String> EMPTY_PLACEHOLDERS = Collections.emptyMap();
   private final Supplier<LocaleData> localeSupplier;
   private final BooleanSupplier enabled;
   private final LanguageCache cache;
   private final PlaceholderFormatter placeholderFormatter;
   private final BiFunction<String, Map<String, String>, String> formatter;
   private final Map<String, String> variantKeyCache = new ConcurrentHashMap<>();

   public ItemLanguageSection(
      Supplier<LocaleData> localeSupplier,
      BooleanSupplier enabled,
      LanguageCache cache,
      PlaceholderFormatter placeholderFormatter,
      BiFunction<String, Map<String, String>, String> formatter
   ) {
      this.localeSupplier = localeSupplier;
      this.enabled = enabled;
      this.cache = cache;
      this.placeholderFormatter = placeholderFormatter;
      this.formatter = formatter;
   }

   public String vanillaName(Material material) {
      if (material == null) {
         return "Unknown Item";
      } else {
         String cacheKey = "material|" + material.name();
         String cached = this.cache.getCachedMaterialName(cacheKey);
         if (cached != null) {
            return cached;
         } else {
            String name = this.enabled.getAsBoolean() ? this.locale().items().getString("item." + material.name() + ".name") : null;
            name = name == null ? this.placeholderFormatter.formatEnumName(material.name()) : this.formatter.apply(name, null);
            this.cache.putCachedMaterialName(cacheKey, name);
            return name;
         }
      }
   }

   public String[] vanillaLore(Material material) {
      return material == null ? new String[0] : this.lore("item." + material.name() + ".lore");
   }

   public String name(String key) {
      return this.name(key, EMPTY_PLACEHOLDERS);
   }

   public String name(String key, Map<String, String> placeholders) {
      if (!this.enabled.getAsBoolean()) {
         return key;
      } else {
         String name = this.locale().items().getString(key);
         return name == null ? key : this.formatter.apply(name, placeholders);
      }
   }

   public String variantKey(String section, String variant, String field) {
      if (!this.enabled.getAsBoolean()) {
         return section + "." + field;
      } else {
         String cacheKey = section + "|" + variant + "|" + field;
         return this.variantKeyCache.computeIfAbsent(cacheKey, ignored -> this.resolveVariantKey(section, variant, field));
      }
   }

   public void clearCache() {
      this.variantKeyCache.clear();
   }

   private String resolveVariantKey(String section, String variant, String field) {
      String variantKey = section + "." + variant + "." + field;
      if (this.locale().items().contains(variantKey)) {
         return variantKey;
      } else {
         String defaultKey = section + ".default." + field;
         return this.locale().items().contains(defaultKey) ? defaultKey : section + "." + field;
      }
   }

   public String[] lore(String key) {
      return this.lore(key, EMPTY_PLACEHOLDERS);
   }

   public String[] lore(String key, Map<String, String> placeholders) {
      if (!this.enabled.getAsBoolean()) {
         return new String[0];
      } else {
         String cacheKey = this.placeholderFormatter.cacheKey(key, placeholders);
         String[] cached = this.cache.getCachedLore(cacheKey);
         if (cached != null) {
            return cached;
         } else {
            String[] result = this.locale().items().getStringList(key).stream().map(line -> this.formatter.apply(line, placeholders)).toArray(String[]::new);
            this.cache.putCachedLore(cacheKey, result);
            return result;
         }
      }
   }

   public List<String> loreWithMultilinePlaceholders(String key, Map<String, String> placeholders) {
      if (!this.enabled.getAsBoolean()) {
         return Collections.emptyList();
      } else {
         List<String> result = new ArrayList<>();

         for (String line : this.locale().items().getStringList(key)) {
            result.addAll(this.placeholderFormatter.processMultilinePlaceholders(line, placeholders));
         }

         return result;
      }
   }

   public Component translatableLootLine(String templateKey, Material material, String amount, String chance) {
      return LanguageComponentFormatter.translatableLootLine(this.locale().items().getString(templateKey), material, amount, chance);
   }

   public Component translatableLootLine(String templateKey, Component itemName, String amount, String chance) {
      return LanguageComponentFormatter.translatableLootLine(this.locale().items().getString(templateKey), itemName, amount, chance);
   }

   public List<Component> loreComponents(String key, Map<String, String> placeholders, List<Component> lootItemComponents, String emptyLootKey) {
      return !this.enabled.getAsBoolean()
         ? Collections.emptyList()
         : LanguageComponentFormatter.loreComponents(
            this.locale().items().getStringList(key),
            line -> this.formatter.apply(line, placeholders),
            () -> this.locale().items().getString(emptyLootKey, ""),
            lootItemComponents
         );
   }

   private LocaleData locale() {
      return this.localeSupplier.get();
   }
}
