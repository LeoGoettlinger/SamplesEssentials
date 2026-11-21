package github.nighter.smartspawner.spawner.item;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.language.LanguageManager;
import github.nighter.smartspawner.spawner.config.ItemSpawnerSettingsConfig;
import github.nighter.smartspawner.spawner.config.SpawnerDisplayConfigurator;
import github.nighter.smartspawner.spawner.config.SpawnerSettingsConfig;
import github.nighter.smartspawner.spawner.lootgen.loot.EntityLootConfig;
import github.nighter.smartspawner.spawner.lootgen.loot.LootItem;
import github.nighter.smartspawner.utils.ItemTooltipUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SpawnerItemFactory {
   private static final long CACHE_EXPIRY_TIME_MS = TimeUnit.MINUTES.toMillis(30L);
   private static final int MAX_CACHE_SIZE = 100;
   private final SmartSpawner plugin;
   private final LanguageManager languageManager;
   private static NamespacedKey VANILLA_SPAWNER_KEY;
   private final NamespacedKey configNameKey;
   private final NamespacedKey itemSpawnerMaterialKey;
   private final Map<String, ItemStack> spawnerItemCache = new HashMap<>();
   private final Map<String, Long> cacheTimestamps = new HashMap<>();
   private long lastCacheCleanup = System.currentTimeMillis();

   public SpawnerItemFactory(SmartSpawner plugin) {
      this.plugin = plugin;
      this.languageManager = plugin.getLanguageManager();
      VANILLA_SPAWNER_KEY = new NamespacedKey(plugin, "vanilla_spawner");
      this.configNameKey = new NamespacedKey(plugin, "spawner_config_name");
      this.itemSpawnerMaterialKey = new NamespacedKey(plugin, "item_spawner_material");
   }

   public void reload() {
      this.clearAllCaches();
   }

   public void clearAllCaches() {
      this.spawnerItemCache.clear();
      this.cacheTimestamps.clear();
      this.lastCacheCleanup = System.currentTimeMillis();
   }

   private void cleanupCacheIfNeeded() {
      long currentTime = System.currentTimeMillis();
      if (currentTime - this.lastCacheCleanup >= TimeUnit.MINUTES.toMillis(1L)) {
         this.lastCacheCleanup = currentTime;
         Iterator<Entry<String, Long>> iterator = this.cacheTimestamps.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<String, Long> entry = iterator.next();
            if (currentTime - entry.getValue() > CACHE_EXPIRY_TIME_MS) {
               this.spawnerItemCache.remove(entry.getKey());
               iterator.remove();
            }
         }
      }
   }

   public ItemStack createSmartSpawnerItem(EntityType entityType) {
      return this.createSmartSpawnerItem(entityType, 1);
   }

   public ItemStack createSmartSpawnerItem(EntityType entityType, int amount) {
      SpawnerSettingsConfig.MobDefinition definition = this.plugin.getSpawnerSettingsConfig().getBaseDefinition(entityType);
      return this.createSmartSpawnerItem(definition == null ? null : definition.name(), entityType, amount);
   }

   public ItemStack createSmartSpawnerItem(String configName, int amount) {
      SpawnerSettingsConfig.MobDefinition definition = this.plugin.getSpawnerSettingsConfig().resolve(configName);
      return definition == null ? null : this.createSmartSpawnerItem(definition.name(), definition.entityType(), amount);
   }

   public ItemStack createSmartSpawnerItem(String configName, EntityType entityType, int amount) {
      this.cleanupCacheIfNeeded();
      String cacheKey = "mob:" + (configName != null ? configName : entityType.name());
      if (amount == 1) {
         ItemStack cachedItem = this.spawnerItemCache.get(cacheKey);
         if (cachedItem != null) {
            return cachedItem.clone();
         }
      }

      ItemStack spawner = new ItemStack(Material.SPAWNER, amount);
      ItemMeta meta = spawner.getItemMeta();
      if (meta != null && entityType != null && entityType != EntityType.UNKNOWN) {
         if (meta instanceof BlockStateMeta blockMeta && blockMeta.getBlockState() instanceof CreatureSpawner cs) {
            SpawnerDisplayConfigurator.applyMob(this.plugin, cs, configName, entityType);
            blockMeta.setBlockState(cs);
         }

         SpawnerSettingsConfig.MobDefinition definition = this.plugin.getSpawnerSettingsConfig().resolveOrBase(configName, entityType);
         String entityTypeName = definition != null && definition.displayName() != null
            ? definition.displayName()
            : this.languageManager.getFormattedMobName(entityType);
         String entityTypeNameSmallCaps = this.languageManager.getSmallCaps(entityTypeName);
         EntityLootConfig lootConfig = definition != null ? definition.lootConfig() : null;
         List<LootItem> lootItems = lootConfig != null ? lootConfig.getAllItems() : Collections.emptyList();
         Map<String, String> placeholders = new HashMap<>();
         placeholders.put("entity", entityTypeName);
         placeholders.put("ᴇɴᴛɪᴛʏ", entityTypeNameSmallCaps);
         placeholders.put("exp", String.valueOf(lootConfig != null ? lootConfig.experience() : 0));
         List<LootItem> sortedLootItems = new ArrayList<>(lootItems);
         sortedLootItems.sort(Comparator.comparing(item -> item.material().name()));
         String nameKey = this.itemKey("smart_spawner", entityType.name(), "name");
         String lootItemsKey = this.itemKey("smart_spawner", entityType.name(), "loot_items");
         String emptyLootKey = this.itemKey("smart_spawner", entityType.name(), "loot_items_empty");
         String loreKey = this.itemKey("smart_spawner", entityType.name(), "lore");
         List<Component> lootComponents = new ArrayList<>(sortedLootItems.size());

         for (LootItem item : sortedLootItems) {
            String amountRange = item.minAmount() == item.maxAmount() ? String.valueOf(item.minAmount()) : item.minAmount() + "-" + item.maxAmount();
            String chance = String.format("%.1f", item.chance());
            lootComponents.add(this.languageManager.buildTranslatableLootLine(lootItemsKey, item.template(), amountRange, chance));
         }

         String displayName = this.languageManager.getItemName(nameKey, placeholders);
         meta.setDisplayName(displayName);
         List<Component> lore = this.languageManager.buildItemLoreAsComponents(loreKey, placeholders, lootComponents, emptyLootKey);
         if (!lore.isEmpty()) {
            meta.lore(lore);
         }

         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
         if (configName != null) {
            meta.getPersistentDataContainer().set(this.configNameKey, PersistentDataType.STRING, configName);
         }

         spawner.setItemMeta(meta);
      }

      ItemTooltipUtil.hideTooltip(spawner);
      if (amount == 1) {
         this.spawnerItemCache.put(cacheKey, spawner.clone());
         this.cacheTimestamps.put(cacheKey, System.currentTimeMillis());
         if (this.spawnerItemCache.size() > 100) {
            String oldestKey = null;
            long oldestTime = Long.MAX_VALUE;

            for (Entry<String, Long> entry : this.cacheTimestamps.entrySet()) {
               if (entry.getValue() < oldestTime) {
                  oldestTime = entry.getValue();
                  oldestKey = entry.getKey();
               }
            }

            if (oldestKey != null) {
               this.spawnerItemCache.remove(oldestKey);
               this.cacheTimestamps.remove(oldestKey);
            }
         }
      }

      return spawner;
   }

   public ItemStack createVanillaSpawnerItem(EntityType entityType) {
      return this.createVanillaSpawnerItem(entityType, 1);
   }

   public ItemStack createVanillaSpawnerItem(EntityType entityType, int amount) {
      ItemStack spawner = new ItemStack(Material.SPAWNER, amount);
      ItemMeta meta = spawner.getItemMeta();
      if (meta != null && entityType != null && entityType != EntityType.UNKNOWN) {
         if (meta instanceof BlockStateMeta blockMeta && blockMeta.getBlockState() instanceof CreatureSpawner cs) {
            cs.setSpawnedType(entityType);
            blockMeta.setBlockState(cs);
         }

         String entityTypeName = this.languageManager.getFormattedMobName(entityType);
         Map<String, String> placeholders = new HashMap<>();
         placeholders.put("entity", entityTypeName);
         placeholders.put("ᴇɴᴛɪᴛʏ", this.languageManager.getSmallCaps(entityTypeName));
         String nameKey = this.itemKey("vanilla_spawner", entityType.name(), "name");
         String loreKey = this.itemKey("vanilla_spawner", entityType.name(), "lore");
         String displayName = this.languageManager.getItemName(nameKey, placeholders);
         if (displayName != null && !displayName.isEmpty() && !displayName.equals(nameKey)) {
            meta.setDisplayName(displayName);
         }

         List<String> lore = this.languageManager.getItemLoreWithMultilinePlaceholders(loreKey, placeholders);
         if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
            ItemTooltipUtil.hideTooltip(spawner);
         }

         meta.getPersistentDataContainer().set(VANILLA_SPAWNER_KEY, PersistentDataType.BOOLEAN, true);
         spawner.setItemMeta(meta);
      }

      return spawner;
   }

   public ItemStack createItemSpawnerItem(Material itemMaterial) {
      return this.createItemSpawnerItem(itemMaterial, 1);
   }

   public ItemStack createItemSpawnerItem(Material itemMaterial, int amount) {
      ItemSpawnerSettingsConfig.ItemDefinition definition = this.plugin.getItemSpawnerSettingsConfig().getBaseDefinition(itemMaterial);
      return this.createItemSpawnerItem(definition == null ? null : definition.name(), itemMaterial, amount);
   }

   public ItemStack createItemSpawnerItem(String configName, int amount) {
      ItemSpawnerSettingsConfig.ItemDefinition definition = this.plugin.getItemSpawnerSettingsConfig().resolve(configName);
      return definition == null ? null : this.createItemSpawnerItem(definition.name(), definition.material(), amount);
   }

   public ItemStack createItemSpawnerItem(String configName, Material itemMaterial, int amount) {
      this.cleanupCacheIfNeeded();
      String cacheKey = "item:" + (configName != null ? configName : itemMaterial.name());
      if (amount == 1) {
         ItemStack cachedItem = this.spawnerItemCache.get(cacheKey);
         if (cachedItem != null) {
            return cachedItem.clone();
         }
      }

      ItemStack spawner = new ItemStack(Material.SPAWNER, amount);
      ItemMeta meta = spawner.getItemMeta();
      if (meta != null && itemMaterial != null) {
         if (meta instanceof BlockStateMeta blockMeta && blockMeta.getBlockState() instanceof CreatureSpawner cs) {
            SpawnerDisplayConfigurator.applyItem(this.plugin, cs, configName, itemMaterial);
            blockMeta.setBlockState(cs);
         }

         ItemSpawnerSettingsConfig.ItemDefinition definition = this.plugin.getItemSpawnerSettingsConfig().resolveOrBase(configName, itemMaterial);
         String itemName = definition != null && definition.displayName() != null
            ? definition.displayName()
            : this.languageManager.getVanillaItemName(itemMaterial);
         String itemNameSmallCaps = this.languageManager.getSmallCaps(itemName);
         EntityLootConfig lootConfig = definition != null ? definition.lootConfig() : null;
         List<LootItem> lootItems = lootConfig != null ? lootConfig.getAllItems() : Collections.emptyList();
         Map<String, String> placeholders = new HashMap<>();
         placeholders.put("entity", itemName);
         placeholders.put("ᴇɴᴛɪᴛʏ", itemNameSmallCaps);
         placeholders.put("exp", String.valueOf(lootConfig != null ? lootConfig.experience() : 0));
         List<LootItem> sortedLootItems = new ArrayList<>(lootItems);
         sortedLootItems.sort(Comparator.comparing(item -> item.material().name()));
         String nameKey = this.itemKey("item_spawner", itemMaterial.name(), "name");
         String lootItemsKey = this.itemKey("item_spawner", itemMaterial.name(), "loot_items");
         String emptyLootKey = this.itemKey("item_spawner", itemMaterial.name(), "loot_items_empty");
         String loreKey = this.itemKey("item_spawner", itemMaterial.name(), "lore");
         List<Component> lootComponents = new ArrayList<>(sortedLootItems.size());

         for (LootItem item : sortedLootItems) {
            String amountRange = item.minAmount() == item.maxAmount() ? String.valueOf(item.minAmount()) : item.minAmount() + "-" + item.maxAmount();
            String chance = String.format("%.1f", item.chance());
            lootComponents.add(this.languageManager.buildTranslatableLootLine(lootItemsKey, item.template(), amountRange, chance));
         }

         String displayName = this.languageManager.getItemName(nameKey, placeholders);
         if (displayName == null || displayName.isEmpty() || displayName.equals(nameKey)) {
            displayName = itemName;
         }

         meta.setDisplayName(displayName);
         List<Component> lore = this.languageManager.buildItemLoreAsComponents(loreKey, placeholders, lootComponents, emptyLootKey);
         if (!lore.isEmpty()) {
            meta.lore(lore);
         }

         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
         meta.getPersistentDataContainer().set(this.itemSpawnerMaterialKey, PersistentDataType.STRING, itemMaterial.name());
         if (configName != null) {
            meta.getPersistentDataContainer().set(this.configNameKey, PersistentDataType.STRING, configName);
         }

         spawner.setItemMeta(meta);
      }

      ItemTooltipUtil.hideTooltip(spawner);
      if (amount == 1) {
         this.spawnerItemCache.put(cacheKey, spawner.clone());
         this.cacheTimestamps.put(cacheKey, System.currentTimeMillis());
         this.trimCacheToLimit();
      }

      return spawner;
   }

   private void trimCacheToLimit() {
      if (this.spawnerItemCache.size() > 100) {
         String oldestKey = null;
         long oldestTime = Long.MAX_VALUE;

         for (Entry<String, Long> entry : this.cacheTimestamps.entrySet()) {
            if (entry.getValue() < oldestTime) {
               oldestTime = entry.getValue();
               oldestKey = entry.getKey();
            }
         }

         if (oldestKey != null) {
            this.spawnerItemCache.remove(oldestKey);
            this.cacheTimestamps.remove(oldestKey);
         }
      }
   }

   private String itemKey(String section, String variant, String field) {
      return this.languageManager.getItemVariantKey(section, variant, field);
   }
}
