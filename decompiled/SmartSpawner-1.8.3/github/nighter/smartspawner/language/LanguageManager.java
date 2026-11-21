package github.nighter.smartspawner.language;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.language.cache.LanguageCache;
import github.nighter.smartspawner.language.file.LanguageFileType;
import github.nighter.smartspawner.language.file.LanguageRepository;
import github.nighter.smartspawner.language.format.PlaceholderFormatter;
import github.nighter.smartspawner.language.format.SmallCapsFormatter;
import github.nighter.smartspawner.language.section.FormattingLanguageSection;
import github.nighter.smartspawner.language.section.GuiLanguageSection;
import github.nighter.smartspawner.language.section.HologramLanguageSection;
import github.nighter.smartspawner.language.section.ItemLanguageSection;
import github.nighter.smartspawner.language.section.MessageLanguageSection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class LanguageManager {
   private final LanguageRepository repository;
   private final LanguageCache cache;
   private final PlaceholderFormatter placeholderFormatter;
   private final SmallCapsFormatter smallCapsFormatter;
   private final MessageLanguageSection messages;
   private final GuiLanguageSection guiLanguage;
   private final GuiLanguageSection commandGuiLanguage;
   private final ItemLanguageSection items;
   private final FormattingLanguageSection formatting;
   private final HologramLanguageSection hologram;
   private final AtomicInteger cacheHits = new AtomicInteger();
   private final AtomicInteger cacheMisses = new AtomicInteger();

   public LanguageManager(JavaPlugin plugin) {
      this(plugin, LanguageFileType.values());
   }

   public LanguageManager(SmartSpawner plugin, LanguageFileType... fileTypes) {
      this((JavaPlugin)plugin, fileTypes);
   }

   private LanguageManager(JavaPlugin plugin, LanguageFileType... fileTypes) {
      this.repository = new LanguageRepository(plugin, fileTypes);
      this.repository.initialize();
      this.cache = new LanguageCache(this.cacheHits, this.cacheMisses);
      this.placeholderFormatter = new PlaceholderFormatter(this.cache, this.cacheHits, this.cacheMisses);
      this.smallCapsFormatter = new SmallCapsFormatter(this.cache, this.cacheHits, this.cacheMisses);
      this.messages = new MessageLanguageSection(this.repository::current, this.placeholderFormatter::apply, this.placeholderFormatter::applyOnlyPlaceholders);
      this.guiLanguage = this.createGuiSection("GUI", LanguageFileType.GUI);
      this.commandGuiLanguage = this.createGuiSection("command GUI", LanguageFileType.COMMAND_GUI);
      this.items = new ItemLanguageSection(
         this.repository::current,
         () -> this.repository.isActive(LanguageFileType.ITEMS),
         this.cache,
         this.placeholderFormatter,
         this.placeholderFormatter::apply
      );
      this.formatting = new FormattingLanguageSection(
         this.repository::current,
         () -> this.repository.isActive(LanguageFileType.FORMATTING),
         this.cache,
         this.placeholderFormatter,
         this.placeholderFormatter::apply
      );
      this.hologram = new HologramLanguageSection(plugin, this.repository::current);
   }

   private GuiLanguageSection createGuiSection(String label, LanguageFileType fileType) {
      return new GuiLanguageSection(
         label,
         () -> fileType == LanguageFileType.COMMAND_GUI ? this.repository.current().commandGui() : this.repository.current().gui(),
         () -> this.repository.isActive(fileType),
         this.placeholderFormatter::apply,
         1000,
         250,
         250
      );
   }

   public String getDefaultLocale() {
      return this.repository.defaultLocale();
   }

   public void loadLanguages() {
      this.repository.loadLanguages();
   }

   public void loadLanguages(LanguageFileType... fileTypes) {
      this.repository.loadLanguages(fileTypes);
   }

   public void reloadLanguages() {
      this.clearCache();
      this.repository.reload();
   }

   public String getMessage(String key, Map<String, String> placeholders) {
      return this.messages.message(key, placeholders);
   }

   public String getMessageWithoutPrefix(String key, Map<String, String> placeholders) {
      return this.messages.messageWithoutPrefix(key, placeholders);
   }

   public String getMessageForConsole(String key, Map<String, String> placeholders) {
      return this.messages.consoleMessage(key, placeholders);
   }

   public String getTitle(String key, Map<String, String> placeholders) {
      return this.messages.title(key, placeholders);
   }

   public String getSubtitle(String key, Map<String, String> placeholders) {
      return this.messages.subtitle(key, placeholders);
   }

   public String getActionBar(String key, Map<String, String> placeholders) {
      return this.messages.actionBar(key, placeholders);
   }

   public String getSound(String key) {
      return this.messages.sound(key);
   }

   String getRawMessage(String path, Map<String, String> placeholders) {
      return this.messages.raw(path, placeholders);
   }

   public boolean keyExists(String key) {
      return this.messages.contains(key);
   }

   public String getCommandConfig(String path, String defaultValue) {
      return this.messages.commandConfig(path, defaultValue);
   }

   public String getCommandConfig(String path, String defaultValue, Map<String, String> placeholders) {
      return this.messages.commandConfig(path, defaultValue, placeholders);
   }

   public GuiLanguageSection gui() {
      return this.guiLanguage;
   }

   public GuiLanguageSection commandGui() {
      return this.commandGuiLanguage;
   }

   public String getGuiTitle(String key) {
      return this.guiLanguage.title(key);
   }

   public String getGuiTitle(String key, Map<String, String> placeholders) {
      return this.guiLanguage.title(key, placeholders);
   }

   public String getGuiConfigString(String key, String defaultValue) {
      return this.guiLanguage.configString(key, defaultValue);
   }

   public String getGuiItemName(String key) {
      return this.guiLanguage.name(key);
   }

   public String getGuiItemName(String key, Map<String, String> placeholders) {
      return this.guiLanguage.name(key, placeholders);
   }

   public String[] getGuiItemLore(String key) {
      return this.guiLanguage.lore(key);
   }

   public String[] getGuiItemLore(String key, Map<String, String> placeholders) {
      return this.guiLanguage.lore(key, placeholders);
   }

   public List<String> getGuiItemLoreAsList(String key) {
      return this.guiLanguage.loreList(key);
   }

   public List<String> getGuiItemLoreAsList(String key, Map<String, String> placeholders) {
      return this.guiLanguage.loreList(key, placeholders);
   }

   public List<String> getGuiItemLoreWithMultilinePlaceholders(String key, Map<String, String> placeholders) {
      return this.guiLanguage.loreWithMultilinePlaceholders(key, placeholders);
   }

   public String getVanillaItemName(Material material) {
      return this.items.vanillaName(material);
   }

   public String[] getVanillaItemLore(Material material) {
      return this.items.vanillaLore(material);
   }

   public String getItemName(String key) {
      return this.items.name(key);
   }

   public String getItemName(String key, Map<String, String> placeholders) {
      return this.items.name(key, placeholders);
   }

   public String getItemVariantKey(String section, String variant, String field) {
      return this.items.variantKey(section, variant, field);
   }

   public String[] getItemLore(String key) {
      return this.items.lore(key);
   }

   public String[] getItemLore(String key, Map<String, String> placeholders) {
      return this.items.lore(key, placeholders);
   }

   public List<String> getItemLoreWithMultilinePlaceholders(String key, Map<String, String> placeholders) {
      return this.items.loreWithMultilinePlaceholders(key, placeholders);
   }

   public Component buildTranslatableLootLine(String templateKey, Material material, String amount, String chance) {
      return this.items.translatableLootLine(templateKey, material, amount, chance);
   }

   public Component buildTranslatableGuiLootLine(String templateKey, Material material, String amount, String chance) {
      return this.guiLanguage.translatableLootLine(templateKey, material, amount, chance);
   }

   public Component buildTranslatableGuiLootLine(String templateKey, ItemStack item, String amount, String chance) {
      return this.guiLanguage.translatableLootLine(templateKey, this.getItemDisplayName(item), amount, chance);
   }

   public Component buildTranslatableLootLine(String templateKey, ItemStack item, String amount, String chance) {
      return this.items.translatableLootLine(templateKey, this.getItemDisplayName(item), amount, chance);
   }

   public Component getItemDisplayName(ItemStack item) {
      if (item == null) {
         return Component.empty();
      } else {
         ItemMeta meta = item.getItemMeta();
         if (meta != null && meta.hasDisplayName()) {
            Component custom = meta.displayName();
            if (custom != null) {
               return custom;
            }
         }

         return item.effectiveName();
      }
   }

   public List<Component> buildItemLoreAsComponents(String key, Map<String, String> stringPlaceholders, List<Component> lootItemComponents, String emptyLootKey) {
      return this.items.loreComponents(key, stringPlaceholders, lootItemComponents, emptyLootKey);
   }

   public List<Component> buildGuiLoreAsComponents(String key, Map<String, String> stringPlaceholders, List<Component> lootItemComponents, String emptyLootKey) {
      return this.guiLanguage.loreComponents(key, stringPlaceholders, lootItemComponents, emptyLootKey);
   }

   public String formatNumber(double number) {
      return this.formatting.number(number);
   }

   public String getFormattedMobName(EntityType type) {
      return this.formatting.mobName(type);
   }

   public String getHologramText() {
      return this.hologram.text();
   }

   public void clearCache() {
      this.cache.clear();
      this.items.clearCache();
      this.guiLanguage.clearCache();
      this.commandGuiLanguage.clearCache();
   }

   public String applyPlaceholdersAndColors(String text, Map<String, String> placeholders) {
      return this.placeholderFormatter.apply(text, placeholders);
   }

   public String applyOnlyPlaceholders(String text, Map<String, String> placeholders) {
      return this.placeholderFormatter.applyOnlyPlaceholders(text, placeholders);
   }

   public String getSmallCaps(String text) {
      return this.smallCapsFormatter.apply(text);
   }

   public String formatEnumName(String enumName) {
      return this.placeholderFormatter.formatEnumName(enumName);
   }

   public String getColorCode(String path) {
      return this.guiLanguage.colorCode(path);
   }

   public Map<String, Object> getCacheStats() {
      Map<String, Object> stats = new HashMap<>();
      stats.put("string_cache_size", this.cache.getStringCacheSize());
      stats.put("string_cache_capacity", this.cache.getStringCacheCapacity());
      stats.put("lore_cache_size", this.cache.getLoreCacheSize());
      stats.put("lore_cache_capacity", this.cache.getLoreCacheCapacity());
      stats.put("lore_list_cache_size", this.cache.getLoreListCacheSize());
      stats.put("lore_list_cache_capacity", this.cache.getLoreListCacheCapacity());
      this.guiLanguage.addCacheStats(stats, "gui");
      this.commandGuiLanguage.addCacheStats(stats, "command_gui");
      stats.put("entity_name_cache_size", this.cache.getEntityNameCacheSize());
      stats.put("entity_name_cache_capacity", this.cache.getEntityNameCacheCapacity());
      stats.put("small_caps_cache_size", this.cache.getSmallCapsCacheSize());
      stats.put("small_caps_cache_capacity", this.cache.getSmallCapsCacheCapacity());
      stats.put("material_name_cache_size", this.cache.getMaterialNameCacheSize());
      stats.put("material_name_cache_capacity", this.cache.getMaterialNameCacheCapacity());
      stats.put("cache_hits", this.cacheHits.get());
      stats.put("cache_misses", this.cacheMisses.get());
      stats.put("hit_ratio", this.cacheHits.get() > 0 ? (double)this.cacheHits.get() / (this.cacheHits.get() + this.cacheMisses.get()) : 0.0);
      return stats;
   }
}
