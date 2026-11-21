package github.nighter.smartspawner.spawner.gui.main;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.api.events.SpawnerOpenGUIEvent;
import github.nighter.smartspawner.language.LanguageManager;
import github.nighter.smartspawner.spawner.config.SpawnerMobHeadTexture;
import github.nighter.smartspawner.spawner.gui.layout.GuiButton;
import github.nighter.smartspawner.spawner.gui.layout.GuiLayout;
import github.nighter.smartspawner.spawner.lootgen.loot.EntityLootConfig;
import github.nighter.smartspawner.spawner.lootgen.loot.LootItem;
import github.nighter.smartspawner.spawner.properties.ItemSignature;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.spawner.properties.VirtualInventory;
import github.nighter.smartspawner.utils.ItemTooltipUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpawnerMenuUI {
   private static final int INVENTORY_SIZE = 27;
   private static final int TICKS_PER_SECOND = 20;
   private static final Map<String, String> EMPTY_PLACEHOLDERS = Collections.emptyMap();
   private static final String LOOT_ITEM_FORMAT_KEY = "spawner_storage_item.loot_items";
   private static final String EMPTY_LOOT_MESSAGE_KEY = "spawner_storage_item.loot_items_empty";
   private final SmartSpawner plugin;
   private final LanguageManager languageManager;
   private Material cachedStorageMaterial = Material.CHEST;
   private Material cachedExpMaterial = Material.EXPERIENCE_BOTTLE;
   private final Map<String, ItemStack> itemCache = new ConcurrentHashMap<>();
   private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
   private static final long CACHE_EXPIRY_TIME_MS = 30000L;

   public SpawnerMenuUI(SmartSpawner plugin) {
      this.plugin = plugin;
      this.languageManager = plugin.getLanguageManager();
      this.loadConfig();
   }

   public void loadConfig() {
      this.clearCache();
      GuiLayout layout = this.plugin.getGuiLayoutConfig().getCurrentMainLayout();
      Material storageMaterial = Material.CHEST;
      Material expMaterial = Material.EXPERIENCE_BOTTLE;

      for (GuiButton button : layout.getAllButtons().values()) {
         String action = button.getDefaultAction();
         if (action != null) {
            if ("open_storage".equals(action)) {
               storageMaterial = button.getMaterial();
            } else if ("collect_exp".equals(action)) {
               expMaterial = button.getMaterial();
            }
         }
      }

      this.cachedStorageMaterial = storageMaterial;
      this.cachedExpMaterial = expMaterial;
   }

   public void clearCache() {
      this.itemCache.clear();
      this.cacheTimestamps.clear();
   }

   public void invalidateSpawnerCache(String spawnerId) {
      this.itemCache.entrySet().removeIf(entry -> entry.getKey().startsWith(spawnerId + "|"));
      this.cacheTimestamps.entrySet().removeIf(entry -> entry.getKey().startsWith(spawnerId + "|"));
   }

   private boolean isCacheEntryExpired(String cacheKey) {
      Long timestamp = this.cacheTimestamps.get(cacheKey);
      return timestamp == null || System.currentTimeMillis() - timestamp > 30000L;
   }

   public void openSpawnerMenu(Player player, SpawnerData spawner, boolean refresh) {
      if (SpawnerOpenGUIEvent.getHandlerList().getRegisteredListeners().length != 0) {
         SpawnerOpenGUIEvent openEvent = new SpawnerOpenGUIEvent(player, spawner.getSpawnerLocation(), spawner.getEntityType(), spawner.getStackSize(), refresh);
         Bukkit.getPluginManager().callEvent(openEvent);
         if (openEvent.isCancelled()) {
            return;
         }
      }

      GuiLayout layout = this.plugin.getGuiLayoutConfig().getMainLayout(spawner, player);
      Inventory menu = this.createMenu(spawner, layout);
      ItemStack[] items = new ItemStack[27];

      for (GuiButton button : layout.getAllButtons().values()) {
         if (button.isEnabled() && (!button.hasCondition() || this.evaluateButtonCondition(button, player))) {
            ItemStack item;
            if (button.isInfoButton()) {
               item = this.createSpawnerInfoItem(player, spawner, button);
            } else {
               String action = this.getAnyActionFromButton(button);
               if (action == null || action.isEmpty()) {
                  action = "none";
               }

               switch (action) {
                  case "open_storage":
                     item = this.createLootStorageItem(spawner, button);
                     break;
                  case "collect_exp":
                     item = this.createExpItem(spawner, button);
                     break;
                  case "sell_and_exp":
                     item = this.createSpawnerInfoItem(player, spawner, button);
                     break;
                  case "none":
                     item = this.createStaticItem(button);
                     break;
                  default:
                     item = this.createStaticItem(button);
               }
            }

            if (item != null) {
               items[button.getSlot()] = item;
            }
         }
      }

      for (int i = 0; i < items.length; i++) {
         if (items[i] != null) {
            menu.setItem(i, items[i]);
         }
      }

      player.openInventory(menu);
      if (!refresh) {
         this.plugin.getGuiButtonInteractionService().playOpenSound(player);
      }

      if (this.plugin.getSpawnerGuiViewManager().isTimerPlaceholdersEnabled() && spawner.getSpawnerStop().get()) {
         this.plugin.getSpawnerGuiViewManager().forceTimerUpdateInactive(player, spawner);
      }
   }

   private Inventory createMenu(SpawnerData spawner, GuiLayout layout) {
      String entityName = spawner.getDisplayName();
      String entityNameSmallCaps = this.languageManager.getSmallCaps(entityName);
      Map<String, String> placeholders = new HashMap<>(4);
      placeholders.put("entity", entityName);
      placeholders.put("ᴇɴᴛɪᴛʏ", entityNameSmallCaps);
      placeholders.put("amount", String.valueOf(spawner.getStackSize()));
      String title;
      if (spawner.getStackSize() > 1) {
         title = this.languageManager.getGuiTitle("gui_title_main.stacked_spawner", placeholders);
      } else {
         title = this.languageManager.getGuiTitle("gui_title_main.single_spawner", placeholders);
      }

      return Bukkit.createInventory(new SpawnerMenuHolder(spawner, layout), 27, title);
   }

   public ItemStack createLootStorageItem(SpawnerData spawner, GuiButton button) {
      VirtualInventory virtualInventory = spawner.getVirtualInventory();
      int currentItems = virtualInventory.getUsedSlots();
      int maxSlots = spawner.getMaxSpawnerLootSlots();
      String buttonConfig = button != null ? button.getMaterial().name() + "_" + button.getCustomTexture() : "default";
      String cacheKey = spawner.getSpawnerId() + "|storage|" + currentItems + "|" + maxSlots + "|" + virtualInventory.hashCode() + "|" + buttonConfig;
      ItemStack cachedItem = this.itemCache.get(cacheKey);
      if (cachedItem != null && !this.isCacheEntryExpired(cacheKey)) {
         return cachedItem.clone();
      } else {
         String nameTemplate = this.languageManager.getGuiItemName("spawner_storage_item.name", EMPTY_PLACEHOLDERS);
         List<String> loreTemplate = this.languageManager.getGuiItemLoreAsList("spawner_storage_item.lore", EMPTY_PLACEHOLDERS);
         Set<String> availablePlaceholders = Set.of("max_slots", "current_items", "percent_storage_rounded", "total_sell_price", "loot_items");
         Set<String> usedPlaceholders = new HashSet<>();
         usedPlaceholders.addAll(this.detectUsedPlaceholders(nameTemplate, availablePlaceholders));
         usedPlaceholders.addAll(this.detectUsedPlaceholders(loreTemplate, availablePlaceholders));
         Map<String, String> placeholders = new HashMap<>();
         if (usedPlaceholders.contains("max_slots")) {
            placeholders.put("max_slots", this.languageManager.formatNumber(maxSlots));
         }

         if (usedPlaceholders.contains("current_items")) {
            placeholders.put("current_items", String.valueOf(currentItems));
         }

         if (usedPlaceholders.contains("percent_storage_rounded")) {
            int percentStorage = this.calculatePercentage(currentItems, maxSlots);
            placeholders.put("percent_storage_rounded", String.valueOf(percentStorage));
         }

         if (usedPlaceholders.contains("total_sell_price")) {
            if (spawner.isSellValueDirty()) {
               spawner.recalculateSellValue();
            }

            placeholders.put("total_sell_price", this.languageManager.formatNumber(spawner.getAccumulatedSellValue()));
         }

         List<Component> finalLootComponents = usedPlaceholders.contains("loot_items")
            ? this.buildLootItemComponents(spawner, virtualInventory.getConsolidatedItems())
            : Collections.emptyList();
         Consumer<ItemMeta> metaModifier = meta -> {
            meta.setDisplayName(this.languageManager.getGuiItemName("spawner_storage_item.name", placeholders));
            List<Component> lore = this.languageManager
               .buildGuiLoreAsComponents("spawner_storage_item.lore", placeholders, finalLootComponents, "spawner_storage_item.loot_items_empty");
            if (!lore.isEmpty()) {
               meta.lore(lore);
            }
         };
         ItemStack chestItem;
         if (button != null && button.getMaterial() == Material.PLAYER_HEAD && button.getCustomTexture() != null && !button.getCustomTexture().trim().isEmpty()
            )
          {
            chestItem = SpawnerMobHeadTexture.getCustomHeadFromTexture(button.getCustomTexture(), metaModifier);
         } else {
            Material mat = button != null ? button.getMaterial() : this.cachedStorageMaterial;
            chestItem = new ItemStack(mat);
            chestItem.editMeta(metaModifier);
         }

         if (chestItem.getType() == Material.BUNDLE) {
            ItemTooltipUtil.hideTooltip(chestItem);
         }

         this.itemCache.put(cacheKey, chestItem.clone());
         this.cacheTimestamps.put(cacheKey, System.currentTimeMillis());
         return chestItem;
      }
   }

   public ItemStack createSpawnerInfoItem(Player player, SpawnerData spawner, GuiButton button) {
      EntityType entityType = spawner.getEntityType();
      int stackSize = spawner.getStackSize();
      VirtualInventory virtualInventory = spawner.getVirtualInventory();
      int currentItems = virtualInventory.getUsedSlots();
      int maxSlots = spawner.getMaxSpawnerLootSlots();
      long currentExp = spawner.getSpawnerExp();
      long maxExp = spawner.getMaxStoredExp();
      boolean hasShopPermission = this.plugin.hasSellIntegration() && player.hasPermission("smartspawner.sellall");
      String buttonConfig = button != null ? button.getMaterial().name() + "_" + button.getCustomTexture() : "default";
      String cacheKey = spawner.getSpawnerId()
         + "|info|"
         + currentItems
         + "|"
         + maxSlots
         + "|"
         + currentExp
         + "|"
         + maxExp
         + "|"
         + hasShopPermission
         + "|"
         + buttonConfig;
      ItemStack cachedItem = this.itemCache.get(cacheKey);
      if (cachedItem != null && !this.isCacheEntryExpired(cacheKey)) {
         ItemStack result = cachedItem.clone();
         this.applyTimerPlaceholder(result, spawner, player);
         return result;
      } else {
         String nameTemplate = this.languageManager.getGuiItemName("spawner_info_item.name", EMPTY_PLACEHOLDERS);
         String loreKey = hasShopPermission ? "spawner_info_item.lore" : "spawner_info_item.lore_no_shop";
         List<String> loreTemplate = this.languageManager.getGuiItemLoreAsList(loreKey, EMPTY_PLACEHOLDERS);
         Set<String> availablePlaceholders = Set.of(
            "entity",
            "ᴇɴᴛɪᴛʏ",
            "stack_size",
            "range",
            "delay",
            "min_mobs",
            "max_mobs",
            "current_items",
            "max_items",
            "percent_storage_decimal",
            "percent_storage_rounded",
            "current_exp",
            "max_exp",
            "raw_current_exp",
            "raw_max_exp",
            "percent_exp_decimal",
            "percent_exp_rounded",
            "total_sell_price",
            "time"
         );
         Set<String> usedPlaceholders = new HashSet<>();
         usedPlaceholders.addAll(this.detectUsedPlaceholders(nameTemplate, availablePlaceholders));
         usedPlaceholders.addAll(this.detectUsedPlaceholders(loreTemplate, availablePlaceholders));
         Map<String, String> placeholders = new HashMap<>();
         if (usedPlaceholders.contains("entity") || usedPlaceholders.contains("ᴇɴᴛɪᴛʏ")) {
            String entityName = spawner.getDisplayName();
            if (usedPlaceholders.contains("entity")) {
               placeholders.put("entity", entityName);
            }

            if (usedPlaceholders.contains("ᴇɴᴛɪᴛʏ")) {
               placeholders.put("ᴇɴᴛɪᴛʏ", this.languageManager.getSmallCaps(entityName));
            }
         }

         if (usedPlaceholders.contains("stack_size")) {
            placeholders.put("stack_size", String.valueOf(stackSize));
         }

         if (usedPlaceholders.contains("range")) {
            placeholders.put("range", String.valueOf(spawner.getSpawnerRange()));
         }

         if (usedPlaceholders.contains("delay")) {
            long delaySeconds = spawner.getSpawnDelay() / 20L;
            placeholders.put("delay", String.valueOf(delaySeconds));
         }

         if (usedPlaceholders.contains("min_mobs")) {
            placeholders.put("min_mobs", String.valueOf(spawner.getMinMobs()));
         }

         if (usedPlaceholders.contains("max_mobs")) {
            placeholders.put("max_mobs", String.valueOf(spawner.getMaxMobs()));
         }

         if (usedPlaceholders.contains("current_items")) {
            placeholders.put("current_items", String.valueOf(currentItems));
         }

         if (usedPlaceholders.contains("max_items")) {
            placeholders.put("max_items", this.languageManager.formatNumber(maxSlots));
         }

         if (usedPlaceholders.contains("percent_storage_decimal") || usedPlaceholders.contains("percent_storage_rounded")) {
            double percentStorageDecimal = maxSlots > 0 ? (double)currentItems / maxSlots * 100.0 : 0.0;
            if (usedPlaceholders.contains("percent_storage_decimal")) {
               String formattedPercentStorage = String.format("%.1f", percentStorageDecimal);
               placeholders.put("percent_storage_decimal", formattedPercentStorage);
            }

            if (usedPlaceholders.contains("percent_storage_rounded")) {
               int percentStorageRounded = (int)Math.round(percentStorageDecimal);
               placeholders.put("percent_storage_rounded", String.valueOf(percentStorageRounded));
            }
         }

         if (usedPlaceholders.contains("current_exp")) {
            placeholders.put("current_exp", this.languageManager.formatNumber(currentExp));
         }

         if (usedPlaceholders.contains("max_exp")) {
            placeholders.put("max_exp", this.languageManager.formatNumber(maxExp));
         }

         if (usedPlaceholders.contains("raw_current_exp")) {
            placeholders.put("raw_current_exp", String.valueOf(currentExp));
         }

         if (usedPlaceholders.contains("raw_max_exp")) {
            placeholders.put("raw_max_exp", String.valueOf(maxExp));
         }

         if (usedPlaceholders.contains("percent_exp_decimal") || usedPlaceholders.contains("percent_exp_rounded")) {
            double percentExpDecimal = maxExp > 0L ? (double)currentExp / maxExp * 100.0 : 0.0;
            if (usedPlaceholders.contains("percent_exp_decimal")) {
               String formattedPercentExp = String.format("%.1f", percentExpDecimal);
               placeholders.put("percent_exp_decimal", formattedPercentExp);
            }

            if (usedPlaceholders.contains("percent_exp_rounded")) {
               int percentExpRounded = (int)Math.round(percentExpDecimal);
               placeholders.put("percent_exp_rounded", String.valueOf(percentExpRounded));
            }
         }

         if (usedPlaceholders.contains("total_sell_price")) {
            if (spawner.isSellValueDirty()) {
               spawner.recalculateSellValue();
            }

            double totalSellPrice = spawner.getAccumulatedSellValue();
            placeholders.put("total_sell_price", this.languageManager.formatNumber(totalSellPrice));
         }

         Consumer<ItemMeta> metaModifier = meta -> {
            meta.setDisplayName(this.languageManager.getGuiItemName("spawner_info_item.name", placeholders));
            List<String> lore = this.languageManager.getGuiItemLoreWithMultilinePlaceholders(loreKey, placeholders);
            meta.setLore(lore);
         };
         ItemStack spawnerItem;
         if (spawner.isItemSpawner()) {
            spawnerItem = SpawnerMobHeadTexture.getSpawnerHead(spawner, metaModifier);
         } else if (button != null
            && button.getMaterial() == Material.PLAYER_HEAD
            && button.getCustomTexture() != null
            && !button.getCustomTexture().trim().isEmpty()) {
            spawnerItem = SpawnerMobHeadTexture.getCustomHeadFromTexture(button.getCustomTexture(), metaModifier);
         } else if (button != null && button.getMaterial() == Material.PLAYER_HEAD) {
            spawnerItem = SpawnerMobHeadTexture.getSpawnerHead(spawner, metaModifier);
         } else if (button != null) {
            spawnerItem = new ItemStack(button.getMaterial());
            spawnerItem.editMeta(metaModifier);
         } else {
            spawnerItem = SpawnerMobHeadTexture.getSpawnerHead(spawner, metaModifier);
         }

         if (spawnerItem.getType() == Material.SPAWNER) {
            ItemTooltipUtil.hideTooltip(spawnerItem);
         }

         this.itemCache.put(cacheKey, spawnerItem.clone());
         this.cacheTimestamps.put(cacheKey, System.currentTimeMillis());
         this.applyTimerPlaceholder(spawnerItem, spawner, player);
         return spawnerItem;
      }
   }

   private void applyTimerPlaceholder(ItemStack item, SpawnerData spawner, Player player) {
      if (item != null) {
         ItemMeta meta = item.getItemMeta();
         if (meta != null) {
            boolean nameHasTimer = meta.hasDisplayName() && meta.getDisplayName().contains("{time}");
            List<String> lore = meta.hasLore() ? meta.getLore() : null;
            boolean loreHasTimer = false;
            if (lore != null) {
               for (String line : lore) {
                  if (line.contains("{time}")) {
                     loreHasTimer = true;
                     break;
                  }
               }
            }

            if (nameHasTimer || loreHasTimer) {
               String timerValue = this.plugin.getSpawnerGuiViewManager().calculateTimerDisplay(spawner, player);
               if (nameHasTimer) {
                  meta.setDisplayName(meta.getDisplayName().replace("{time}", timerValue));
               }

               if (loreHasTimer) {
                  List<String> updatedLore = new ArrayList<>(lore.size());

                  for (String linex : lore) {
                     updatedLore.add(linex.replace("{time}", timerValue));
                  }

                  meta.setLore(updatedLore);
               }

               item.setItemMeta(meta);
            }
         }
      }
   }

   public ItemStack createExpItem(SpawnerData spawner, GuiButton button) {
      long currentExp = spawner.getSpawnerExp();
      long maxExp = spawner.getMaxStoredExp();
      int percentExp = this.calculatePercentage(currentExp, maxExp);
      String buttonConfig = button != null ? button.getMaterial().name() + "_" + button.getCustomTexture() : "default";
      String cacheKey = spawner.getSpawnerId() + "|exp|" + currentExp + "|" + maxExp + "|" + buttonConfig;
      ItemStack cachedItem = this.itemCache.get(cacheKey);
      if (cachedItem != null && !this.isCacheEntryExpired(cacheKey)) {
         return cachedItem.clone();
      } else {
         String formattedExp = this.languageManager.formatNumber(currentExp);
         String formattedMaxExp = this.languageManager.formatNumber(maxExp);
         Map<String, String> placeholders = new HashMap<>(5);
         placeholders.put("current_exp", formattedExp);
         placeholders.put("raw_current_exp", String.valueOf(currentExp));
         placeholders.put("max_exp", formattedMaxExp);
         placeholders.put("percent_exp_rounded", String.valueOf(percentExp));
         placeholders.put("u_max_exp", String.valueOf(maxExp));
         Consumer<ItemMeta> metaModifier = meta -> {
            meta.setDisplayName(this.languageManager.getGuiItemName("exp_info_item.name", placeholders));
            List<String> loreExp = this.languageManager.getGuiItemLoreAsList("exp_info_item.lore", placeholders);
            meta.setLore(loreExp);
         };
         ItemStack expItem;
         if (button != null && button.getMaterial() == Material.PLAYER_HEAD && button.getCustomTexture() != null && !button.getCustomTexture().trim().isEmpty()
            )
          {
            expItem = SpawnerMobHeadTexture.getCustomHeadFromTexture(button.getCustomTexture(), metaModifier);
         } else {
            Material mat = button != null ? button.getMaterial() : this.cachedExpMaterial;
            expItem = new ItemStack(mat);
            expItem.editMeta(metaModifier);
         }

         if (expItem.getType() == Material.BUNDLE) {
            ItemTooltipUtil.hideTooltip(expItem);
         }

         this.itemCache.put(cacheKey, expItem.clone());
         this.cacheTimestamps.put(cacheKey, System.currentTimeMillis());
         return expItem;
      }
   }

   private ItemStack createStaticItem(GuiButton button) {
      ItemStack item;
      if (button.getMaterial() == Material.PLAYER_HEAD && button.getCustomTexture() != null && !button.getCustomTexture().trim().isEmpty()) {
         item = SpawnerMobHeadTexture.getCustomHeadFromTexture(button.getCustomTexture(), null);
      } else {
         item = new ItemStack(button.getMaterial());
      }

      if (item.getType() == Material.SPAWNER) {
         ItemTooltipUtil.hideTooltip(item);
      }

      return item;
   }

   private int calculatePercentage(long current, long maximum) {
      return maximum > 0L ? (int)((double)current / maximum * 100.0) : 0;
   }

   private List<Component> buildLootItemComponents(SpawnerData spawner, Map<ItemSignature, Long> storedItems) {
      Map<Material, Long> materialAmountMap = new HashMap<>();

      for (Entry<ItemSignature, Long> entry : storedItems.entrySet()) {
         Material material = entry.getKey().getMaterial();
         materialAmountMap.merge(material, entry.getValue(), Long::sum);
      }

      EntityLootConfig lootConfig = spawner.getLootConfig();
      List<LootItem> possibleLootItems = lootConfig != null ? lootConfig.getAllItems() : Collections.emptyList();
      if (possibleLootItems.isEmpty() && storedItems.isEmpty()) {
         return Collections.emptyList();
      } else {
         List<Component> components = new ArrayList<>();
         if (!possibleLootItems.isEmpty()) {
            possibleLootItems.sort(Comparator.comparing(item -> item.material().name()));

            for (LootItem lootItem : possibleLootItems) {
               Material material = lootItem.material();
               long amount = materialAmountMap.getOrDefault(material, 0L);
               String formattedAmount = this.languageManager.formatNumber(amount);
               String chance = String.format("%.1f", lootItem.chance()) + "%";
               components.add(
                  this.languageManager.buildTranslatableGuiLootLine("spawner_storage_item.loot_items", lootItem.template(), formattedAmount, chance)
               );
            }
         } else {
            List<Entry<ItemSignature, Long>> sortedItems = new ArrayList<>(storedItems.entrySet());
            sortedItems.sort(Comparator.comparing(e -> e.getKey().getMaterialName()));

            for (Entry<ItemSignature, Long> entry : sortedItems) {
               long amount = entry.getValue();
               String formattedAmount = this.languageManager.formatNumber(amount);
               components.add(
                  this.languageManager.buildTranslatableGuiLootLine("spawner_storage_item.loot_items", entry.getKey().getTemplate(), formattedAmount, "")
               );
            }
         }

         return components;
      }
   }

   private Set<String> detectUsedPlaceholders(String text, Set<String> availablePlaceholders) {
      Set<String> usedPlaceholders = new HashSet<>();
      if (text != null && !text.isEmpty()) {
         for (String placeholder : availablePlaceholders) {
            if (text.contains("{" + placeholder + "}")) {
               usedPlaceholders.add(placeholder);
            }
         }

         return usedPlaceholders;
      } else {
         return usedPlaceholders;
      }
   }

   private Set<String> detectUsedPlaceholders(List<String> textList, Set<String> availablePlaceholders) {
      Set<String> usedPlaceholders = new HashSet<>();
      if (textList != null && !textList.isEmpty()) {
         for (String text : textList) {
            for (String placeholder : availablePlaceholders) {
               if (text.contains("{" + placeholder + "}")) {
                  usedPlaceholders.add(placeholder);
               }
            }
         }

         return usedPlaceholders;
      } else {
         return usedPlaceholders;
      }
   }

   private boolean evaluateButtonCondition(GuiButton button, Player player) {
      String condition = button.getCondition();
      if (condition != null && !condition.isEmpty()) {
         switch (condition) {
            case "sell_integration":
               return this.plugin.hasSellIntegration();
            case "no_sell_integration":
               return !this.plugin.hasSellIntegration();
            default:
               this.plugin.getLogger().warning("Unknown button condition: " + condition);
               return true;
         }
      } else {
         return true;
      }
   }

   private String getAnyActionFromButton(GuiButton button) {
      String action = button.getDefaultAction();
      if (action != null && !action.isEmpty()) {
         return action;
      } else {
         action = button.getAction("left_click");
         if (action != null && !action.isEmpty()) {
            return action;
         } else {
            action = button.getAction("right_click");
            return action != null && !action.isEmpty() ? action : null;
         }
      }
   }
}
