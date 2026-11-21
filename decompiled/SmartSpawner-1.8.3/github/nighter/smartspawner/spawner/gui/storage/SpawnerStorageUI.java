package github.nighter.smartspawner.spawner.gui.storage;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.language.LanguageManager;
import github.nighter.smartspawner.spawner.config.SpawnerMobHeadTexture;
import github.nighter.smartspawner.spawner.gui.layout.GuiButton;
import github.nighter.smartspawner.spawner.gui.layout.GuiLayout;
import github.nighter.smartspawner.spawner.gui.layout.GuiLayoutConfig;
import github.nighter.smartspawner.spawner.lootgen.loot.EntityLootConfig;
import github.nighter.smartspawner.spawner.lootgen.loot.LootItem;
import github.nighter.smartspawner.spawner.properties.ItemSignature;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.spawner.properties.VirtualInventory;
import github.nighter.smartspawner.utils.ItemTooltipUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Arrays;
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
import lombok.Generated;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpawnerStorageUI {
   private static final String STORAGE_TITLE = "gui_title_storage";
   private static final String SINGLE_STORAGE_TITLE = "gui_title_storage_single_spawner";
   private static final int INVENTORY_SIZE = 54;
   private final SmartSpawner plugin;
   private final LanguageManager languageManager;
   private GuiLayoutConfig layoutConfig;
   private final Map<String, ItemStack> staticButtons;
   private final Map<String, ItemStack> navigationButtonCache;
   private final Map<String, ItemStack> pageIndicatorCache;
   private final Map<String, ItemStack> sortButtonCache;
   private static final int MAX_CACHE_SIZE = 100;
   private static final int MAX_SORT_CACHE_SIZE = 300;
   private String cachedStorageTitleFormat = null;
   private String cachedSingleStorageTitleFormat = null;
   private Scheduler.Task cleanupTask;

   public SpawnerStorageUI(SmartSpawner plugin) {
      this.plugin = plugin;
      this.languageManager = plugin.getLanguageManager();
      this.layoutConfig = plugin.getGuiLayoutConfig();
      this.staticButtons = new ConcurrentHashMap<>(16);
      this.navigationButtonCache = new ConcurrentHashMap<>(16);
      this.pageIndicatorCache = new ConcurrentHashMap<>(16);
      this.sortButtonCache = new ConcurrentHashMap<>(16);
      this.initializeStaticButtons();
      this.startCleanupTask();
   }

   public void reload() {
      this.layoutConfig = this.plugin.getGuiLayoutConfig();
      this.navigationButtonCache.clear();
      this.pageIndicatorCache.clear();
      this.sortButtonCache.clear();
      this.staticButtons.clear();
      this.cachedStorageTitleFormat = null;
      this.cachedSingleStorageTitleFormat = null;
      this.initializeStaticButtons();
   }

   private void initializeStaticButtons() {
      GuiLayout layout = this.layoutConfig.getCurrentStorageLayout();

      for (GuiButton button : layout.getAllButtons().values()) {
         String action = this.getAnyActionFromButton(button);
         if (action != null) {
            switch (action) {
               case "return_main":
                  this.getStaticButton("return", button, meta -> {
                     meta.setDisplayName(this.languageManager.getGuiItemName("return_button.name"));
                     meta.setLore(this.languageManager.getGuiItemLoreAsList("return_button.lore"));
                  });
                  break;
               case "take_all":
                  this.getStaticButton("takeAll", button, meta -> {
                     meta.setDisplayName(this.languageManager.getGuiItemName("take_all_button.name"));
                     meta.setLore(this.languageManager.getGuiItemLoreAsList("take_all_button.lore"));
                  });
               case "sort_items":
               default:
                  break;
               case "drop_page":
                  this.getStaticButton("dropPage", button, meta -> {
                     meta.setDisplayName(this.languageManager.getGuiItemName("drop_page_button.name"));
                     meta.setLore(this.languageManager.getGuiItemLoreAsList("drop_page_button.lore"));
                  });
                  break;
               case "open_filter":
                  this.getStaticButton("itemFilter", button, meta -> {
                     meta.setDisplayName(this.languageManager.getGuiItemName("item_filter_button.name"));
                     meta.setLore(this.languageManager.getGuiItemLoreAsList("item_filter_button.lore"));
                  });
            }
         }
      }
   }

   private String getAnyActionFromButton(GuiButton button) {
      Map<String, String> actions = button.getActions();
      if (actions != null && !actions.isEmpty()) {
         String action = actions.get("click");
         if (action != null && !action.isEmpty()) {
            return action;
         } else {
            action = actions.get("left_click");
            if (action != null && !action.isEmpty()) {
               return action;
            } else {
               action = actions.get("right_click");
               return action != null && !action.isEmpty() ? action : null;
            }
         }
      } else {
         return null;
      }
   }

   public String getStorageTitle(SpawnerData spawner, int page, int totalPages) {
      String titleKey = "gui_title_storage";
      String titleFormat;
      if (spawner.getStackSize() == 1 && this.languageManager.gui().contains("gui_title_storage_single_spawner")) {
         titleKey = "gui_title_storage_single_spawner";
         if (this.cachedSingleStorageTitleFormat == null) {
            this.cachedSingleStorageTitleFormat = this.languageManager.getGuiTitle(titleKey);
         }

         titleFormat = this.cachedSingleStorageTitleFormat;
      } else {
         if (this.cachedStorageTitleFormat == null) {
            this.cachedStorageTitleFormat = this.languageManager.getGuiTitle(titleKey);
         }

         titleFormat = this.cachedStorageTitleFormat;
      }

      Map<String, String> placeholders = new HashMap<>(4);
      placeholders.put("current_page", String.valueOf(page));
      placeholders.put("total_pages", String.valueOf(totalPages));
      if (titleFormat.contains("{entity}") || titleFormat.contains("{ᴇɴᴛɪᴛʏ}")) {
         String entityName = spawner.getDisplayName();
         if (titleFormat.contains("{entity}")) {
            placeholders.put("entity", entityName);
         }

         if (titleFormat.contains("{ᴇɴᴛɪᴛʏ}")) {
            placeholders.put("ᴇɴᴛɪᴛʏ", this.languageManager.getSmallCaps(entityName));
         }
      }

      if (titleFormat.contains("{amount}")) {
         placeholders.put("amount", String.valueOf(spawner.getStackSize()));
      }

      return this.languageManager.getGuiTitle(titleKey, placeholders);
   }

   public Inventory createStorageInventory(Player player, SpawnerData spawner, int page, int totalPages) {
      if (totalPages == -1) {
         totalPages = this.calculateTotalPages(spawner);
      }

      page = Math.max(1, Math.min(page, totalPages));
      GuiLayout layout = this.layoutConfig.getStorageLayout(spawner, player);
      Inventory pageInv = Bukkit.createInventory(new StoragePageHolder(spawner, page, totalPages, layout), 54, this.getStorageTitle(spawner, page, totalPages));
      this.updateDisplay(pageInv, spawner, page, totalPages);
      return pageInv;
   }

   public void updateDisplay(Inventory inventory, SpawnerData spawner, int page, int totalPages) {
      if (spawner.getInventoryLock().tryLock()) {
         if (totalPages == -1) {
            totalPages = this.calculateTotalPages(spawner);
         }

         Map<Integer, ItemStack> updates = new HashMap<>();
         Set<Integer> slotsToEmpty = new HashSet<>();

         for (int i = 0; i < 45; i++) {
            slotsToEmpty.add(i);
         }

         StoragePageHolder holder = (StoragePageHolder)inventory.getHolder(false);

         assert holder != null;

         GuiLayout layout = holder.getLayout();
         if (layout != null) {
            slotsToEmpty.addAll(layout.getUsedSlots());
         }

         this.addPageItems(updates, slotsToEmpty, spawner, page);
         this.addNavigationButtons(updates, spawner, page, totalPages, layout);

         for (int slot : slotsToEmpty) {
            if (!updates.containsKey(slot)) {
               inventory.setItem(slot, null);
            }
         }

         for (Entry<Integer, ItemStack> entry : updates.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
         }

         if (this.plugin.getConfig().getBoolean("hologram.enabled", false)) {
            Scheduler.runLocationTask(spawner.getSpawnerLocation(), spawner::updateHologramData);
         }

         int oldUsedSlots = holder.getOldUsedSlots();
         int currentUsedSlots = spawner.getVirtualInventory().getUsedSlots();
         if (oldUsedSlots != currentUsedSlots) {
            int newTotalPages = this.calculateTotalPages(spawner);
            holder.setTotalPages(newTotalPages);
            holder.updateOldUsedSlots();
         }
      }
   }

   private void addPageItems(Map<Integer, ItemStack> updates, Set<Integer> slotsToEmpty, SpawnerData spawner, int page) {
      try {
         VirtualInventory virtualInv = spawner.getVirtualInventory();
         Int2ObjectMap<ItemStack> displayItems = virtualInv.getDisplayPage(page, 45);
         if (displayItems.isEmpty()) {
            return;
         }

         ObjectIterator var7 = displayItems.int2ObjectEntrySet().iterator();

         while (var7.hasNext()) {
            it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<ItemStack> entry = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<ItemStack>)var7.next();
            int displaySlot = entry.getIntKey();
            updates.put(displaySlot, (ItemStack)entry.getValue());
            slotsToEmpty.remove(displaySlot);
         }
      } finally {
         spawner.getInventoryLock().unlock();
      }
   }

   private void addNavigationButtons(Map<Integer, ItemStack> updates, SpawnerData spawner, int page, int totalPages, GuiLayout layout) {
      if (totalPages == -1) {
         totalPages = this.calculateTotalPages(spawner);
      }

      for (GuiButton button : layout.getAllButtons().values()) {
         if (button.isEnabled() && (!button.hasCondition() || this.evaluateButtonCondition(button))) {
            if (button.isInfoButton()) {
               updates.put(button.getSlot(), this.createStorageSpawnerInfoButton(spawner, button));
            } else {
               String action = this.getAnyActionFromButton(button);
               if (action != null) {
                  ItemStack item = null;
                  switch (action) {
                     case "previous_page":
                        if (page > 1) {
                           String cacheKey = "prev-" + (page - 1) + "-" + button.getCustomTexture();
                           item = this.navigationButtonCache.computeIfAbsent(cacheKey, k -> this.createNavigationButton("previous", page - 1, button));
                        }
                        break;
                     case "next_page":
                        if (page < totalPages) {
                           String cacheKey = "next-" + (page + 1) + "-" + button.getCustomTexture();
                           item = this.navigationButtonCache.computeIfAbsent(cacheKey, k -> this.createNavigationButton("next", page + 1, button));
                        }
                        break;
                     case "take_all":
                        item = this.getStaticButton("takeAll", button, meta -> {
                           meta.setDisplayName(this.languageManager.getGuiItemName("take_all_button.name"));
                           meta.setLore(this.languageManager.getGuiItemLoreAsList("take_all_button.lore"));
                        });
                        break;
                     case "sort_items":
                        item = this.createSortButton(spawner, button);
                        break;
                     case "drop_page":
                        item = this.getStaticButton("dropPage", button, meta -> {
                           meta.setDisplayName(this.languageManager.getGuiItemName("drop_page_button.name"));
                           meta.setLore(this.languageManager.getGuiItemLoreAsList("drop_page_button.lore"));
                        });
                        break;
                     case "open_filter":
                        item = this.getStaticButton("itemFilter", button, meta -> {
                           meta.setDisplayName(this.languageManager.getGuiItemName("item_filter_button.name"));
                           meta.setLore(this.languageManager.getGuiItemLoreAsList("item_filter_button.lore"));
                        });
                        break;
                     case "return_main":
                        item = this.getStaticButton("return", button, meta -> {
                           meta.setDisplayName(this.languageManager.getGuiItemName("return_button.name"));
                           meta.setLore(this.languageManager.getGuiItemLoreAsList("return_button.lore"));
                        });
                        break;
                     case "sell_all":
                        item = this.createSellButton(spawner, button);
                        break;
                     case "sell_and_exp":
                        item = this.createSellAndExpButton(spawner, button);
                        break;
                     case "collect_exp":
                        item = this.createCollectExpButton(spawner, button);
                  }

                  if (item != null) {
                     updates.put(button.getSlot(), item);
                  }
               }
            }
         }
      }
   }

   private boolean evaluateButtonCondition(GuiButton button) {
      String condition = button.getCondition();
      if (condition != null && !condition.isEmpty()) {
         switch (condition) {
            case "shop_integration":
               return this.plugin.hasSellIntegration();
            case "no_shop_integration":
               return !this.plugin.hasSellIntegration();
            default:
               this.plugin.getLogger().warning("Unknown button condition: " + condition);
               return true;
         }
      } else {
         return true;
      }
   }

   private int calculateTotalPages(SpawnerData spawner) {
      int usedSlots = spawner.getVirtualInventory().getUsedSlots();
      return Math.max(1, (int)Math.ceil(usedSlots / 45.0));
   }

   private ItemStack createButtonWithCustomTexture(GuiButton button, Consumer<ItemMeta> metaModifier) {
      ItemStack item;
      if (button.getMaterial() == Material.PLAYER_HEAD && button.getCustomTexture() != null && !button.getCustomTexture().trim().isEmpty()) {
         item = SpawnerMobHeadTexture.getCustomHeadFromTexture(button.getCustomTexture(), metaModifier);
      } else {
         item = new ItemStack(button.getMaterial());
         item.editMeta(metaModifier);
      }

      if (item.getType() == Material.BUNDLE) {
         ItemTooltipUtil.hideBundleTooltip(item);
      }

      return item;
   }

   private ItemStack getStaticButton(String action, GuiButton button, Consumer<ItemMeta> metaModifier) {
      String cacheKey = action + "|" + button.getMaterial() + "|" + button.getCustomTexture();
      return this.staticButtons.computeIfAbsent(cacheKey, ignored -> this.createButtonWithCustomTexture(button, metaModifier)).clone();
   }

   private ItemStack createNavigationButton(String type, int targetPage, GuiButton button) {
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("target_page", String.valueOf(targetPage));
      String buttonKey = type.equals("previous") ? "navigation_button_previous" : "navigation_button_next";
      String buttonName = this.languageManager.getGuiItemName(buttonKey + ".name", placeholders);
      String[] buttonLore = this.languageManager.getGuiItemLore(buttonKey + ".lore", placeholders);
      return this.createButtonWithCustomTexture(button, meta -> {
         meta.setDisplayName(buttonName);
         if (buttonLore.length > 0) {
            meta.setLore(Arrays.asList(buttonLore));
         }
      });
   }

   private ItemStack createSellButton(SpawnerData spawner, GuiButton button) {
      Map<String, String> placeholders = new HashMap<>();
      if (spawner.isSellValueDirty()) {
         spawner.recalculateSellValue();
      }

      placeholders.put("total_sell_price", this.languageManager.formatNumber(spawner.getAccumulatedSellValue()));
      return this.createButtonWithCustomTexture(button, meta -> {
         meta.setDisplayName(this.languageManager.getGuiItemName("sell_button.name", placeholders));
         meta.setLore(this.languageManager.getGuiItemLoreAsList("sell_button.lore", placeholders));
      });
   }

   private ItemStack createSellAndExpButton(SpawnerData spawner, GuiButton button) {
      Map<String, String> placeholders = new HashMap<>();
      if (spawner.isSellValueDirty()) {
         spawner.recalculateSellValue();
      }

      placeholders.put("total_sell_price", this.languageManager.formatNumber(spawner.getAccumulatedSellValue()));
      return this.createButtonWithCustomTexture(button, meta -> {
         meta.setDisplayName(this.languageManager.getGuiItemName("sell_and_exp_button.name", placeholders));
         meta.setLore(this.languageManager.getGuiItemLoreAsList("sell_and_exp_button.lore", placeholders));
      });
   }

   private ItemStack createCollectExpButton(SpawnerData spawner, GuiButton button) {
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("current_exp", this.languageManager.formatNumber(spawner.getSpawnerExp()));
      return this.createButtonWithCustomTexture(button, meta -> {
         meta.setDisplayName(this.languageManager.getGuiItemName("collect_exp_button.name", placeholders));
         meta.setLore(this.languageManager.getGuiItemLoreAsList("collect_exp_button.lore", placeholders));
      });
   }

   private ItemStack createSortButton(SpawnerData spawner, GuiButton button) {
      Material currentSort = spawner.getPreferredSortItem();
      String lootKey = spawner.isItemSpawner() ? "item:" + spawner.getSpawnedItemMaterial() : "mob:" + spawner.getEntityType();
      String cacheKey = lootKey + "|" + (currentSort == null ? "none" : currentSort.name()) + "|" + button.getMaterial() + "|" + button.getCustomTexture();
      return this.sortButtonCache.computeIfAbsent(cacheKey, ignored -> this.buildSortButton(spawner, button, currentSort)).clone();
   }

   private ItemStack buildSortButton(SpawnerData spawner, GuiButton button, Material currentSort) {
      Map<String, String> placeholders = new HashMap<>();
      String selectedItemFormat = this.languageManager.getGuiItemName("sort_items_button.selected_item");
      String unselectedItemFormat = this.languageManager.getGuiItemName("sort_items_button.unselected_item");
      String noneText = this.languageManager.getGuiItemName("sort_items_button.no_item");
      StringBuilder availableItems = new StringBuilder();
      if (spawner.getLootConfig() != null && spawner.getLootConfig().getAllItems() != null) {
         boolean first = true;

         for (LootItem lootItem : spawner.getLootConfig().getAllItems().stream().sorted(Comparator.comparing(item -> item.material().name())).toList()) {
            if (!first) {
               availableItems.append("\n");
            }

            String itemName = this.languageManager.getVanillaItemName(lootItem.material());
            String format = currentSort == lootItem.material() ? selectedItemFormat : unselectedItemFormat;
            availableItems.append(format.replace("{item_name}", itemName));
            first = false;
         }
      }

      if (availableItems.isEmpty()) {
         availableItems.append(noneText);
      }

      placeholders.put("available_items", availableItems.toString());
      return this.createButtonWithCustomTexture(button, meta -> {
         meta.setDisplayName(this.languageManager.getGuiItemName("sort_items_button.name", placeholders));
         meta.setLore(this.languageManager.getGuiItemLoreWithMultilinePlaceholders("sort_items_button.lore", placeholders));
      });
   }

   private ItemStack createStorageSpawnerInfoButton(SpawnerData spawner, GuiButton button) {
      Map<ItemSignature, Long> storedItems = spawner.getVirtualInventory().getConsolidatedItems();
      List<Component> lootComponents = this.buildStorageInfoLootComponents(spawner, storedItems);
      Map<String, String> placeholders = new HashMap<>();
      String entityName = spawner.getDisplayName();
      placeholders.put("entity", entityName);
      placeholders.put("ᴇɴᴛɪᴛʏ", this.languageManager.getSmallCaps(entityName));
      placeholders.put("stack_size", String.valueOf(spawner.getStackSize()));
      int currentItems = spawner.getVirtualInventory().getUsedSlots();
      int maxSlots = spawner.getMaxSpawnerLootSlots();
      double percentStorageDecimal = maxSlots > 0 ? (double)currentItems / maxSlots * 100.0 : 0.0;
      placeholders.put("percent_storage_decimal", String.format("%.1f", percentStorageDecimal));
      placeholders.put("percent_storage_rounded", String.valueOf((int)Math.round(percentStorageDecimal)));
      long currentExp = spawner.getSpawnerExp();
      long maxExp = spawner.getMaxStoredExp();
      double percentExpDecimal = maxExp > 0L ? (double)currentExp / maxExp * 100.0 : 0.0;
      placeholders.put("percent_exp_decimal", String.format("%.1f", percentExpDecimal));
      placeholders.put("percent_exp_rounded", String.valueOf((int)Math.round(percentExpDecimal)));
      Consumer<ItemMeta> metaModifier = meta -> {
         meta.setDisplayName(this.languageManager.getGuiItemName("storage_spawner_info_button.name", placeholders));
         List<Component> lore = this.languageManager
            .buildGuiLoreAsComponents("storage_spawner_info_button.lore", placeholders, lootComponents, "storage_spawner_info_button.loot_items_empty");
         if (!lore.isEmpty()) {
            meta.lore(lore);
         }

         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
      };
      ItemStack item;
      if (spawner.isItemSpawner()) {
         item = SpawnerMobHeadTexture.getSpawnerHead(spawner, metaModifier);
      } else if (button.getMaterial() == Material.PLAYER_HEAD) {
         String customTexture = button.getCustomTexture();
         if (customTexture != null && !customTexture.trim().isEmpty()) {
            item = SpawnerMobHeadTexture.getCustomHeadFromTexture(customTexture, metaModifier);
         } else {
            item = SpawnerMobHeadTexture.getSpawnerHead(spawner, metaModifier);
         }
      } else {
         item = new ItemStack(button.getMaterial());
         item.editMeta(metaModifier);
      }

      if (item.getType() == Material.SPAWNER) {
         ItemTooltipUtil.hideTooltip(item);
      }

      return item;
   }

   private List<Component> buildStorageInfoLootComponents(SpawnerData spawner, Map<ItemSignature, Long> storedItems) {
      Map<Material, Long> materialAmountMap = new HashMap<>();

      for (Entry<ItemSignature, Long> entry : storedItems.entrySet()) {
         Material mat = entry.getKey().getMaterial();
         materialAmountMap.merge(mat, entry.getValue(), Long::sum);
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
               Material mat = lootItem.material();
               long amount = materialAmountMap.getOrDefault(mat, 0L);
               String formattedAmount = this.languageManager.formatNumber(amount);
               String chance = String.format("%.1f", lootItem.chance()) + "%";
               components.add(this.languageManager.buildTranslatableGuiLootLine("storage_spawner_info_button.loot_items", mat, formattedAmount, chance));
            }
         } else {
            List<Entry<ItemSignature, Long>> sortedItems = new ArrayList<>(storedItems.entrySet());
            sortedItems.sort(Comparator.comparing(e -> e.getKey().getMaterialName()));

            for (Entry<ItemSignature, Long> entry : sortedItems) {
               Material mat = entry.getKey().getMaterial();
               long amount = entry.getValue();
               String formattedAmount = this.languageManager.formatNumber(amount);
               components.add(this.languageManager.buildTranslatableGuiLootLine("storage_spawner_info_button.loot_items", mat, formattedAmount, ""));
            }
         }

         return components;
      }
   }

   private void startCleanupTask() {
      this.cleanupTask = Scheduler.runTaskTimer(this::cleanupCaches, 600L, 600L);
   }

   public void cancelTasks() {
      if (this.cleanupTask != null) {
         this.cleanupTask.cancel();
         this.cleanupTask = null;
      }
   }

   private void cleanupCaches() {
      if (this.navigationButtonCache.size() > 100) {
         int toRemove = this.navigationButtonCache.size() - 50;
         List<String> keysToRemove = new ArrayList<>(this.navigationButtonCache.keySet());

         for (int i = 0; i < Math.min(toRemove, keysToRemove.size()); i++) {
            this.navigationButtonCache.remove(keysToRemove.get(i));
         }
      }

      if (this.pageIndicatorCache.size() > 100) {
         int toRemove = this.pageIndicatorCache.size() - 50;
         List<String> keysToRemove = new ArrayList<>(this.pageIndicatorCache.keySet());

         for (int i = 0; i < Math.min(toRemove, keysToRemove.size()); i++) {
            this.pageIndicatorCache.remove(keysToRemove.get(i));
         }
      }

      if (this.sortButtonCache.size() > 300) {
         int toRemove = this.sortButtonCache.size() - 150;
         List<String> keysToRemove = new ArrayList<>(this.sortButtonCache.keySet());

         for (int i = 0; i < Math.min(toRemove, keysToRemove.size()); i++) {
            this.sortButtonCache.remove(keysToRemove.get(i));
         }
      }
   }

   public void cleanup() {
      this.navigationButtonCache.clear();
      this.pageIndicatorCache.clear();
      this.sortButtonCache.clear();
      this.cachedStorageTitleFormat = null;
      this.cachedSingleStorageTitleFormat = null;
      this.cancelTasks();
      this.initializeStaticButtons();
   }

   @Generated
   public GuiLayoutConfig getLayoutConfig() {
      return this.layoutConfig;
   }
}
