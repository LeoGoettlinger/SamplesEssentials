package github.nighter.smartspawner.spawner.properties;

import com.google.common.util.concurrent.AtomicDouble;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.commands.hologram.SpawnerHologram;
import github.nighter.smartspawner.hooks.economy.ItemPriceManager;
import github.nighter.smartspawner.spawner.config.ItemSpawnerSettingsConfig;
import github.nighter.smartspawner.spawner.config.SpawnerHead;
import github.nighter.smartspawner.spawner.config.SpawnerNameNotices;
import github.nighter.smartspawner.spawner.config.SpawnerSettingsConfig;
import github.nighter.smartspawner.spawner.lootgen.loot.EntityLootConfig;
import github.nighter.smartspawner.spawner.lootgen.loot.LootItem;
import github.nighter.smartspawner.spawner.sell.SellResult;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpawnerData {
   private final SmartSpawner plugin;
   private String spawnerId;
   private final Location spawnerLocation;
   private final ReentrantLock inventoryLock = new ReentrantLock();
   private final ReentrantLock lootGenerationLock = new ReentrantLock();
   private final ReentrantLock dataLock = new ReentrantLock();
   private final AtomicBoolean selling = new AtomicBoolean(false);
   private final AtomicBoolean storageDirty = new AtomicBoolean(false);
   private long baseMaxStoredExp;
   private int baseMaxStoragePages;
   private int baseMinMobs;
   private int baseMaxMobs;
   private long spawnerExp;
   private Boolean spawnerActive;
   private Integer spawnerRange;
   private AtomicBoolean spawnerStop;
   private Boolean isAtCapacity;
   private Long lastSpawnTime;
   private long spawnDelay;
   private EntityType entityType;
   private String configName;
   private EntityLootConfig lootConfig;
   private SpawnerSettingsConfig.MobDefinition mobDefinition;
   private ItemSpawnerSettingsConfig.ItemDefinition itemDefinition;
   private Material spawnedItemMaterial;
   private int maxStoragePages;
   private int maxSpawnerLootSlots;
   private long maxStoredExp;
   private int minMobs;
   private int maxMobs;
   private int stackSize;
   private int maxStackSize;
   private VirtualInventory virtualInventory;
   private final Set<Material> filteredItems = new HashSet<>();
   private String lastInteractedPlayer;
   private SellResult lastSellResult;
   private boolean lastSellProcessed;
   private AtomicDouble accumulatedSellValue;
   private volatile boolean sellValueDirty;
   private SpawnerHologram hologram;
   private long cachedSpawnDelay;
   private Material preferredSortItem;
   private volatile Map<ItemSignature, Long> preGeneratedItems;
   private volatile long preGeneratedExperience;
   private volatile boolean isPreGenerating;
   private volatile Boolean cachedHasNoLoot = null;

   public SpawnerData(String id, Location location, EntityType type, SmartSpawner plugin) {
      this(id, location, type, defaultMobName(plugin, type), plugin);
   }

   public SpawnerData(String id, Location location, EntityType type, String configName, SmartSpawner plugin) {
      this.plugin = plugin;
      this.spawnerId = id;
      this.spawnerLocation = location;
      this.entityType = type;
      this.configName = configName != null && !configName.isBlank() ? configName : defaultMobName(plugin, type);
      this.spawnedItemMaterial = null;
      this.initializeDefaults();
      this.loadConfigurationValues();
      this.calculateStackBasedValues();
      this.initializeComponents();
   }

   public SpawnerData(String id, Location location, Material itemMaterial, SmartSpawner plugin) {
      this(id, location, itemMaterial, defaultItemName(plugin, itemMaterial), plugin);
   }

   public SpawnerData(String id, Location location, Material itemMaterial, String configName, SmartSpawner plugin) {
      this.plugin = plugin;
      this.spawnerId = id;
      this.spawnerLocation = location;
      this.entityType = EntityType.ITEM;
      this.spawnedItemMaterial = itemMaterial;
      this.configName = configName != null && !configName.isBlank() ? configName : defaultItemName(plugin, itemMaterial);
      this.initializeDefaults();
      this.loadConfigurationValues();
      this.calculateStackBasedValues();
      this.initializeComponents();
   }

   private void initializeDefaults() {
      this.spawnerExp = 0L;
      this.spawnerActive = true;
      this.spawnerStop = new AtomicBoolean(true);
      this.isAtCapacity = false;
      this.stackSize = 1;
      this.lastSpawnTime = System.currentTimeMillis();
      this.preferredSortItem = null;
      this.accumulatedSellValue = new AtomicDouble(0.0);
      this.sellValueDirty = true;
   }

   public void loadConfigurationValues() {
      this.baseMaxStoredExp = this.plugin.getConfig().getLong("spawner_properties.default.max_stored_exp", 1000L);
      this.baseMaxStoragePages = this.plugin.getConfig().getInt("spawner_properties.default.max_storage_pages", 1);
      this.baseMinMobs = this.plugin.getConfig().getInt("spawner_properties.default.min_mobs", 1);
      this.baseMaxMobs = this.plugin.getConfig().getInt("spawner_properties.default.max_mobs", 4);
      this.maxStackSize = this.plugin.getConfig().getInt("spawner_properties.default.max_stack_size", 1000);
      this.spawnDelay = this.plugin.getTimeFromConfig("spawner_properties.default.delay", "25s");
      this.cachedSpawnDelay = (this.spawnDelay + 20L) * 50L;
      this.spawnerRange = this.plugin.getConfig().getInt("spawner_properties.default.range", 16);
      this.refreshDefinition();
   }

   private void refreshDefinition() {
      String resolved;
      if (this.isItemSpawner()) {
         ItemSpawnerSettingsConfig settings = this.plugin.getItemSpawnerSettingsConfig();
         ItemSpawnerSettingsConfig.ItemDefinition own = settings.resolve(this.configName);
         if (own != null && own.material() != this.spawnedItemMaterial) {
            own = null;
         }

         this.itemDefinition = own != null ? own : settings.getBaseDefinition(this.spawnedItemMaterial);
         this.mobDefinition = null;
         resolved = own != null ? own.name() : null;
         this.lootConfig = this.itemDefinition != null ? this.itemDefinition.lootConfig() : null;
      } else {
         SpawnerSettingsConfig settings = this.plugin.getSpawnerSettingsConfig();
         SpawnerSettingsConfig.MobDefinition own = settings.resolve(this.configName);
         if (own != null && own.entityType() != this.entityType) {
            own = null;
         }

         this.mobDefinition = own != null ? own : settings.getBaseDefinition(this.entityType);
         this.itemDefinition = null;
         resolved = own != null ? own.name() : null;
         this.lootConfig = this.mobDefinition != null ? this.mobDefinition.lootConfig() : null;
      }

      if (resolved == null) {
         SpawnerNameNotices.missing(this.plugin, this.configName, this.isItemSpawner());
      } else if (!resolved.equals(this.configName)) {
         this.configName = resolved;
         if (this.plugin.getSpawnerManager() != null) {
            this.plugin.getSpawnerManager().markSpawnerModified(this.spawnerId);
         }
      }
   }

   public String getDisplayName() {
      if (this.isItemSpawner()) {
         return this.itemDefinition != null && this.itemDefinition.displayName() != null
            ? this.itemDefinition.displayName()
            : this.plugin.getLanguageManager().getVanillaItemName(this.spawnedItemMaterial);
      } else {
         return this.mobDefinition != null && this.mobDefinition.displayName() != null
            ? this.mobDefinition.displayName()
            : this.plugin.getLanguageManager().getFormattedMobName(this.entityType);
      }
   }

   public SpawnerHead getHead() {
      if (this.isItemSpawner()) {
         return this.itemDefinition != null ? this.itemDefinition.head() : this.plugin.getItemSpawnerSettingsConfig().getBaseHead(this.spawnedItemMaterial);
      } else {
         return this.mobDefinition != null && this.mobDefinition.head() != null
            ? this.mobDefinition.head()
            : this.plugin.getSpawnerSettingsConfig().getBaseHead(this.entityType);
      }
   }

   private static String defaultMobName(SmartSpawner plugin, EntityType type) {
      SpawnerSettingsConfig.MobDefinition definition = plugin.getSpawnerSettingsConfig().getBaseDefinition(type);
      return definition != null ? definition.name() : type.name().toLowerCase(Locale.ROOT);
   }

   private static String defaultItemName(SmartSpawner plugin, Material material) {
      ItemSpawnerSettingsConfig.ItemDefinition definition = plugin.getItemSpawnerSettingsConfig().getBaseDefinition(material);
      return definition != null ? definition.name() : material.name().toLowerCase(Locale.ROOT);
   }

   public void recalculateAfterConfigReload() {
      this.calculateStackBasedValues();
      this.sellValueDirty = true;
      this.updateHologramData();
      if (this.plugin.getSpawnerMenuUI() != null) {
         this.plugin.getSpawnerMenuUI().invalidateSpawnerCache(this.spawnerId);
      }
   }

   public void recalculateAfterAPIModification() {
      this.calculateStackBasedValues();
      this.updateHologramData();
      if (this.plugin.getSpawnerMenuUI() != null) {
         this.plugin.getSpawnerMenuUI().invalidateSpawnerCache(this.spawnerId);
      }
   }

   private void calculateStackBasedValues() {
      this.maxStoredExp = this.clampToLong(this.baseMaxStoredExp * this.stackSize, 0L, Long.MAX_VALUE);
      this.maxStoragePages = this.clampToInt((long)this.baseMaxStoragePages * this.stackSize, 0, Integer.MAX_VALUE);
      this.setMaxSpawnerLootSlots(this.clampToInt(this.maxStoragePages * 45L, 0, Integer.MAX_VALUE));
      this.minMobs = this.clampToInt((long)this.baseMinMobs * this.stackSize, 0, Integer.MAX_VALUE);
      this.maxMobs = this.clampToInt((long)this.baseMaxMobs * this.stackSize, 0, Integer.MAX_VALUE);
      this.spawnerExp = this.clampToLong(this.spawnerExp, 0L, this.maxStoredExp);
   }

   public void setMaxSpawnerLootSlots(int maxSpawnerLootSlots) {
      this.maxSpawnerLootSlots = Math.max(0, maxSpawnerLootSlots);
      if (this.virtualInventory != null) {
         this.virtualInventory.setMaxSlots(this.maxSpawnerLootSlots);
      }
   }

   public void setVirtualInventory(VirtualInventory virtualInventory) {
      this.virtualInventory = virtualInventory;
      if (this.virtualInventory != null) {
         this.virtualInventory.setMaxSlots(this.maxSpawnerLootSlots);
      }
   }

   public void setSpawnDelay(long baseSpawnerDelay) {
      this.spawnDelay = baseSpawnerDelay > 0L ? baseSpawnerDelay : 500L;
      long ticksWithBuffer = this.spawnDelay > 9223372036854775787L ? Long.MAX_VALUE : this.spawnDelay + 20L;
      this.cachedSpawnDelay = ticksWithBuffer > 184467440737095516L ? Long.MAX_VALUE : ticksWithBuffer * 50L;
      if (baseSpawnerDelay <= 0L) {
         this.plugin.getLogger().warning("Invalid spawner delay value. Setting to default: 500 ticks (25s)");
      }
   }

   public void setSpawnDelayFromConfig() {
      long delay = this.plugin.getTimeFromConfig("spawner_properties.default.delay", "25s");
      if (delay <= 0L) {
         this.plugin.getLogger().warning("Invalid spawner delay value in config. Setting to default: 500 ticks (25s)");
         delay = 500L;
      }

      this.setSpawnDelay(delay);
   }

   private void initializeComponents() {
      this.virtualInventory = new VirtualInventory(this.maxSpawnerLootSlots);
      if (this.plugin.getConfig().getBoolean("hologram.enabled", false)) {
         this.createHologram();
      }

      if (this.preferredSortItem == null && this.lootConfig != null && this.lootConfig.getAllItems() != null) {
         List<LootItem> lootItems = this.lootConfig.getAllItems();
         if (!lootItems.isEmpty()) {
            List<Material> sortedLoot = lootItems.stream().map(LootItem::material).distinct().sorted(Comparator.comparing(Enum::name)).toList();
            if (!sortedLoot.isEmpty()) {
               this.preferredSortItem = sortedLoot.getFirst();
            }
         }
      }

      this.virtualInventory.sortItems(this.preferredSortItem);
   }

   private void createHologram() {
      this.hologram = new SpawnerHologram(this.spawnerLocation);
      this.hologram.createHologram();
      this.updateHologramData();
   }

   public void setStackSize(int stackSize) {
      this.setStackSize(stackSize, true);
   }

   public void setStackSize(int stackSize, boolean restartHopper) {
      this.dataLock.lock();

      try {
         this.inventoryLock.lock();

         try {
            this.updateStackSize(stackSize, restartHopper);
         } finally {
            this.inventoryLock.unlock();
         }
      } finally {
         this.dataLock.unlock();
      }
   }

   private void updateStackSize(int newStackSize, boolean restartHopper) {
      if (newStackSize <= 0) {
         this.stackSize = 1;
         this.plugin.getLogger().warning("Invalid stack size. Setting to 1");
      } else if (newStackSize > this.maxStackSize && newStackSize > this.stackSize) {
         this.plugin.getLogger().warning("Stack size " + newStackSize + " exceeds maximum " + this.maxStackSize + ". Ignoring.");
      } else {
         this.stackSize = newStackSize;
         this.calculateStackBasedValues();
         this.lastSpawnTime = System.currentTimeMillis();
         this.updateHologramData();
         if (this.plugin.getSpawnerMenuUI() != null) {
            this.plugin.getSpawnerMenuUI().invalidateSpawnerCache(this.spawnerId);
         }
      }
   }

   public void setSpawnerExp(long exp) {
      this.spawnerExp = Math.clamp(exp, 0L, this.maxStoredExp);
      this.updateHologramData();
      if (this.plugin.getSpawnerMenuUI() != null) {
         this.plugin.getSpawnerMenuUI().invalidateSpawnerCache(this.spawnerId);
      }
   }

   public void setSpawnerExpData(long exp) {
      this.spawnerExp = Math.max(0L, exp);
   }

   public void setBaseMaxStoredExp(long baseMaxStoredExp) {
      this.baseMaxStoredExp = Math.max(0L, baseMaxStoredExp);
   }

   private int clampToInt(long value, int min, int max) {
      if (value < min) {
         return min;
      } else {
         return value > max ? max : (int)value;
      }
   }

   private long clampToLong(long value, long min, long max) {
      if (value < min) {
         return min;
      } else {
         return value > max ? max : value;
      }
   }

   public void updateHologramData() {
      if (this.hologram != null) {
         this.hologram
            .updateData(
               this.stackSize,
               this.entityType,
               this.spawnedItemMaterial,
               this.getDisplayName(),
               this.spawnerExp,
               this.maxStoredExp,
               this.virtualInventory.getUsedSlots(),
               this.maxSpawnerLootSlots
            );
      }
   }

   public void reloadHologramData() {
      if (this.hologram != null) {
         this.hologram.remove();
         this.createHologram();
      }
   }

   public void refreshHologram() {
      if (this.plugin.getConfig().getBoolean("hologram.enabled", false)) {
         if (this.hologram == null) {
            this.createHologram();
         }
      } else if (this.hologram != null) {
         this.removeHologram();
      }
   }

   public void removeHologram() {
      if (this.hologram != null) {
         this.hologram.remove();
         this.hologram = null;
      }
   }

   public boolean isCompletelyFull() {
      return this.virtualInventory.getUsedSlots() >= this.maxSpawnerLootSlots && this.spawnerExp >= this.maxStoredExp;
   }

   public boolean updateCapacityStatus() {
      boolean newStatus = this.isCompletelyFull();
      if (newStatus != this.isAtCapacity) {
         this.isAtCapacity = newStatus;
         return true;
      } else {
         return false;
      }
   }

   public void setEntityType(EntityType newType) {
      this.entityType = newType;
      this.configName = defaultMobName(this.plugin, newType);
      this.refreshDefinition();
      this.sellValueDirty = true;
      this.updateHologramData();
   }

   public boolean toggleItemFilter(Material material) {
      boolean wasFiltered = this.filteredItems.contains(material);
      if (wasFiltered) {
         this.filteredItems.remove(material);
      } else {
         this.filteredItems.add(material);
      }

      return !wasFiltered;
   }

   public List<LootItem> getValidLootItems() {
      return this.lootConfig == null
         ? Collections.emptyList()
         : this.lootConfig.getAllItems().stream().filter(this::isLootItemValid).collect(Collectors.toList());
   }

   private boolean isLootItemValid(LootItem item) {
      return item.isAvailable() && !this.filteredItems.contains(item.material());
   }

   public int getEntityExperienceValue() {
      return this.lootConfig != null ? this.lootConfig.experience() : 0;
   }

   public boolean hasNoLootOrExperience() {
      if (this.cachedHasNoLoot != null) {
         return this.cachedHasNoLoot;
      } else {
         boolean result = this.lootConfig == null || this.lootConfig.experience() == 0 && this.getValidLootItems().isEmpty();
         this.cachedHasNoLoot = result;
         return result;
      }
   }

   public void setLootConfig() {
      this.refreshDefinition();
      this.sellValueDirty = true;
      this.cachedHasNoLoot = null;
   }

   public void setLastSellResult(SellResult sellResult) {
      this.lastSellResult = sellResult;
      this.lastSellProcessed = false;
   }

   public void markLastSellAsProcessed() {
      this.lastSellProcessed = true;
      this.lastSellResult = null;
   }

   public boolean isSelling() {
      return this.selling.get();
   }

   public boolean startSelling() {
      return this.selling.compareAndSet(false, true);
   }

   public void stopSelling() {
      this.selling.set(false);
   }

   public boolean isStorageDirty() {
      return this.storageDirty.get();
   }

   public void markStorageDirty() {
      this.storageDirty.set(true);
   }

   public void clearStorageDirty() {
      this.storageDirty.set(false);
   }

   public void updateLastInteractedPlayer(String playerName) {
      this.lastInteractedPlayer = playerName;
   }

   public void markSellValueDirty() {
      this.sellValueDirty = true;
   }

   public double getAccumulatedSellValue() {
      return this.accumulatedSellValue.get();
   }

   public void incrementSellValue(Map<ItemSignature, Long> itemsAdded, Map<String, Double> priceCache) {
      if (itemsAdded != null && !itemsAdded.isEmpty()) {
         double addedValue = 0.0;

         for (Entry<ItemSignature, Long> entry : itemsAdded.entrySet()) {
            double itemPrice = this.findItemPrice(entry.getKey(), priceCache);
            if (itemPrice > 0.0) {
               addedValue += itemPrice * entry.getValue().longValue();
            }
         }

         if (addedValue > 0.0) {
            this.accumulatedSellValue.addAndGet(addedValue);
         }

         this.sellValueDirty = false;
      }
   }

   public void decrementSellValue(List<ItemStack> itemsRemoved, Map<String, Double> priceCache) {
      if (itemsRemoved != null && !itemsRemoved.isEmpty()) {
         Map<ItemSignature, Long> consolidated = new HashMap<>();

         for (ItemStack item : itemsRemoved) {
            if (item != null && item.getAmount() > 0) {
               ItemSignature sig = VirtualInventory.getSignature(item);
               consolidated.merge(sig, (long)item.getAmount(), Long::sum);
            }
         }

         this.decrementSellValue(consolidated, priceCache);
      }
   }

   public void decrementSellValue(Map<ItemSignature, Long> itemsRemoved, Map<String, Double> priceCache) {
      if (itemsRemoved != null && !itemsRemoved.isEmpty()) {
         double removedValue = 0.0;

         for (Entry<ItemSignature, Long> entry : itemsRemoved.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
               double itemPrice = this.findItemPrice(entry.getKey(), priceCache);
               if (itemPrice > 0.0) {
                  removedValue += itemPrice * entry.getValue().longValue();
               }
            }
         }

         this.subtractAccumulatedSellValue(removedValue);
      }
   }

   public void recalculateSellValue() {
      if (this.lootConfig == null) {
         this.accumulatedSellValue.set(0.0);
         this.sellValueDirty = false;
      } else {
         Map<String, Double> priceCache = this.createPriceCache();
         Map<ItemSignature, Long> items = this.virtualInventory.getConsolidatedItems();
         double totalValue = 0.0;

         for (Entry<ItemSignature, Long> entry : items.entrySet()) {
            double itemPrice = this.findItemPrice(entry.getKey(), priceCache);
            if (itemPrice > 0.0) {
               totalValue += itemPrice * entry.getValue().longValue();
            }
         }

         this.accumulatedSellValue.set(totalValue);
         this.sellValueDirty = false;
      }
   }

   private void subtractAccumulatedSellValue(double removedValue) {
      if (!(removedValue <= 0.0)) {
         double current;
         double updated;
         do {
            current = this.accumulatedSellValue.get();
            updated = Math.max(0.0, current - removedValue);
         } while (!this.accumulatedSellValue.compareAndSet(current, updated));
      }
   }

   public Map<String, Double> createPriceCache() {
      if (this.lootConfig == null) {
         return new HashMap<>();
      } else {
         ItemPriceManager priceManager = this.plugin.getItemPriceManager();
         Map<String, Double> cache = new HashMap<>();

         for (LootItem lootItem : this.lootConfig.getAllItems()) {
            double price = priceManager != null ? priceManager.getPrice(lootItem.material()) : 0.0;
            if (price <= 0.0) {
               price = lootItem.sellPrice();
            }

            if (price > 0.0) {
               ItemStack template = lootItem.createItemStack();
               if (template != null) {
                  String key = this.createItemKey(template);
                  cache.put(key, price);
               }
            }
         }

         return cache;
      }
   }

   private double findItemPrice(ItemSignature itemSignature, Map<String, Double> priceCache) {
      if (priceCache == null) {
         return 0.0;
      } else {
         String itemKey = this.createItemKey(itemSignature);
         Double price = priceCache.get(itemKey);
         return price != null ? price : 0.0;
      }
   }

   private String createItemKey(ItemStack itemStack) {
      return itemStack == null ? "null" : this.createItemKey(new ItemSignature(itemStack));
   }

   private String createItemKey(ItemSignature itemSignature) {
      StringBuilder key = new StringBuilder();
      key.append(itemSignature.getMaterial().name());
      ItemMeta meta = itemSignature.getUnsafeTemplateRef().getItemMeta();
      if (itemSignature.hasItemMeta() && meta.hasEnchants()) {
         key.append("_enchants:");
         meta.getEnchants()
            .entrySet()
            .stream()
            .sorted(Entry.comparingByKey(Comparator.comparing(enchantment -> enchantment.getKey().toString())))
            .forEach(entry -> key.append(((Enchantment)entry.getKey()).getKey()).append(":").append(entry.getValue()).append(","));
      }

      if (itemSignature.hasItemMeta() && meta.hasDisplayName()) {
         key.append("_name:").append(meta.displayName());
      }

      return key.toString();
   }

   public void addItemsAndUpdateSellValue(Map<ItemSignature, Long> items) {
      if (items != null && !items.isEmpty()) {
         this.inventoryLock.lock();

         try {
            this.virtualInventory.addItems(items);
            if (!this.sellValueDirty) {
               Map<String, Double> priceCache = this.createPriceCache();
               this.incrementSellValue(items, priceCache);
            }
         } finally {
            this.inventoryLock.unlock();
         }
      }
   }

   public boolean removeItemsAndUpdateSellValue(List<ItemStack> items) {
      if (items != null && !items.isEmpty()) {
         Map<ItemSignature, Long> itemsToRemove = new HashMap<>();

         for (ItemStack item : items) {
            if (item != null && item.getAmount() > 0) {
               ItemSignature sig = VirtualInventory.getSignature(item);
               itemsToRemove.merge(sig, (long)item.getAmount(), Long::sum);
            }
         }

         return this.removeItemsAndUpdateSellValue(itemsToRemove);
      } else {
         return true;
      }
   }

   public boolean removeItemsAndUpdateSellValue(Map<ItemSignature, Long> items) {
      if (items != null && !items.isEmpty()) {
         this.inventoryLock.lock();

         boolean var7;
         try {
            boolean removed = this.virtualInventory.removeItems(items);
            if (removed && !this.sellValueDirty) {
               Map<String, Double> priceCache = this.createPriceCache();
               this.decrementSellValue(items, priceCache);
            }

            var7 = removed;
         } finally {
            this.inventoryLock.unlock();
         }

         return var7;
      } else {
         return true;
      }
   }

   public synchronized void storePreGeneratedLoot(Map<ItemSignature, Long> items, long experience) {
      this.preGeneratedItems = items;
      this.preGeneratedExperience = experience;
   }

   public synchronized Map<ItemSignature, Long> getAndClearPreGeneratedItems() {
      Map<ItemSignature, Long> items = this.preGeneratedItems;
      this.preGeneratedItems = null;
      return items;
   }

   public synchronized long getAndClearPreGeneratedExperience() {
      long exp = this.preGeneratedExperience;
      this.preGeneratedExperience = 0L;
      return exp;
   }

   public synchronized boolean hasPreGeneratedLoot() {
      return this.preGeneratedItems != null && !this.preGeneratedItems.isEmpty() || this.preGeneratedExperience > 0L;
   }

   public synchronized void setPreGenerating(boolean generating) {
      this.isPreGenerating = generating;
   }

   public synchronized boolean isPreGenerating() {
      return this.isPreGenerating;
   }

   public synchronized void clearPreGeneratedLoot() {
      this.preGeneratedItems = null;
      this.preGeneratedExperience = 0L;
      this.isPreGenerating = false;
   }

   public boolean isItemSpawner() {
      return this.entityType == EntityType.ITEM && this.spawnedItemMaterial != null;
   }

   @Generated
   public SmartSpawner getPlugin() {
      return this.plugin;
   }

   @Generated
   public String getSpawnerId() {
      return this.spawnerId;
   }

   @Generated
   public void setSpawnerId(String spawnerId) {
      this.spawnerId = spawnerId;
   }

   @Generated
   public Location getSpawnerLocation() {
      return this.spawnerLocation;
   }

   @Generated
   public ReentrantLock getInventoryLock() {
      return this.inventoryLock;
   }

   @Generated
   public ReentrantLock getLootGenerationLock() {
      return this.lootGenerationLock;
   }

   @Generated
   public ReentrantLock getDataLock() {
      return this.dataLock;
   }

   @Generated
   public long getBaseMaxStoredExp() {
      return this.baseMaxStoredExp;
   }

   @Generated
   public int getBaseMaxStoragePages() {
      return this.baseMaxStoragePages;
   }

   @Generated
   public void setBaseMaxStoragePages(int baseMaxStoragePages) {
      this.baseMaxStoragePages = baseMaxStoragePages;
   }

   @Generated
   public int getBaseMinMobs() {
      return this.baseMinMobs;
   }

   @Generated
   public void setBaseMinMobs(int baseMinMobs) {
      this.baseMinMobs = baseMinMobs;
   }

   @Generated
   public int getBaseMaxMobs() {
      return this.baseMaxMobs;
   }

   @Generated
   public void setBaseMaxMobs(int baseMaxMobs) {
      this.baseMaxMobs = baseMaxMobs;
   }

   @Generated
   public long getSpawnerExp() {
      return this.spawnerExp;
   }

   @Generated
   public Boolean getSpawnerActive() {
      return this.spawnerActive;
   }

   @Generated
   public void setSpawnerActive(Boolean spawnerActive) {
      this.spawnerActive = spawnerActive;
   }

   @Generated
   public Integer getSpawnerRange() {
      return this.spawnerRange;
   }

   @Generated
   public void setSpawnerRange(Integer spawnerRange) {
      this.spawnerRange = spawnerRange;
   }

   @Generated
   public AtomicBoolean getSpawnerStop() {
      return this.spawnerStop;
   }

   @Generated
   public Boolean getIsAtCapacity() {
      return this.isAtCapacity;
   }

   @Generated
   public void setIsAtCapacity(Boolean isAtCapacity) {
      this.isAtCapacity = isAtCapacity;
   }

   @Generated
   public Long getLastSpawnTime() {
      return this.lastSpawnTime;
   }

   @Generated
   public void setLastSpawnTime(Long lastSpawnTime) {
      this.lastSpawnTime = lastSpawnTime;
   }

   @Generated
   public long getSpawnDelay() {
      return this.spawnDelay;
   }

   @Generated
   public EntityType getEntityType() {
      return this.entityType;
   }

   @Generated
   public String getConfigName() {
      return this.configName;
   }

   @Generated
   public void setConfigName(String configName) {
      this.configName = configName;
   }

   @Generated
   public EntityLootConfig getLootConfig() {
      return this.lootConfig;
   }

   @Generated
   public void setLootConfig(EntityLootConfig lootConfig) {
      this.lootConfig = lootConfig;
   }

   @Generated
   public SpawnerSettingsConfig.MobDefinition getMobDefinition() {
      return this.mobDefinition;
   }

   @Generated
   public ItemSpawnerSettingsConfig.ItemDefinition getItemDefinition() {
      return this.itemDefinition;
   }

   @Generated
   public Material getSpawnedItemMaterial() {
      return this.spawnedItemMaterial;
   }

   @Generated
   public void setSpawnedItemMaterial(Material spawnedItemMaterial) {
      this.spawnedItemMaterial = spawnedItemMaterial;
   }

   @Generated
   public int getMaxStoragePages() {
      return this.maxStoragePages;
   }

   @Generated
   public int getMaxSpawnerLootSlots() {
      return this.maxSpawnerLootSlots;
   }

   @Generated
   public long getMaxStoredExp() {
      return this.maxStoredExp;
   }

   @Generated
   public void setMaxStoredExp(long maxStoredExp) {
      this.maxStoredExp = maxStoredExp;
   }

   @Generated
   public int getMinMobs() {
      return this.minMobs;
   }

   @Generated
   public void setMinMobs(int minMobs) {
      this.minMobs = minMobs;
   }

   @Generated
   public int getMaxMobs() {
      return this.maxMobs;
   }

   @Generated
   public void setMaxMobs(int maxMobs) {
      this.maxMobs = maxMobs;
   }

   @Generated
   public int getStackSize() {
      return this.stackSize;
   }

   @Generated
   public int getMaxStackSize() {
      return this.maxStackSize;
   }

   @Generated
   public void setMaxStackSize(int maxStackSize) {
      this.maxStackSize = maxStackSize;
   }

   @Generated
   public VirtualInventory getVirtualInventory() {
      return this.virtualInventory;
   }

   @Generated
   public Set<Material> getFilteredItems() {
      return this.filteredItems;
   }

   @Generated
   public String getLastInteractedPlayer() {
      return this.lastInteractedPlayer;
   }

   @Generated
   public void setLastInteractedPlayer(String lastInteractedPlayer) {
      this.lastInteractedPlayer = lastInteractedPlayer;
   }

   @Generated
   public SellResult getLastSellResult() {
      return this.lastSellResult;
   }

   @Generated
   public boolean isLastSellProcessed() {
      return this.lastSellProcessed;
   }

   @Generated
   public boolean isSellValueDirty() {
      return this.sellValueDirty;
   }

   @Generated
   public long getCachedSpawnDelay() {
      return this.cachedSpawnDelay;
   }

   @Generated
   public void setCachedSpawnDelay(long cachedSpawnDelay) {
      this.cachedSpawnDelay = cachedSpawnDelay;
   }

   @Generated
   public Material getPreferredSortItem() {
      return this.preferredSortItem;
   }

   @Generated
   public void setPreferredSortItem(Material preferredSortItem) {
      this.preferredSortItem = preferredSortItem;
   }
}
