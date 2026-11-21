package github.nighter.smartspawner.spawner.gui.sell;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.language.LanguageManager;
import github.nighter.smartspawner.spawner.config.SpawnerMobHeadTexture;
import github.nighter.smartspawner.spawner.gui.layout.GuiButton;
import github.nighter.smartspawner.spawner.gui.layout.GuiLayout;
import github.nighter.smartspawner.spawner.lootgen.loot.EntityLootConfig;
import github.nighter.smartspawner.spawner.lootgen.loot.LootItem;
import github.nighter.smartspawner.spawner.properties.ItemSignature;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.utils.ItemTooltipUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpawnerSellConfirmUI {
   private static final int GUI_SIZE = 27;
   private final SmartSpawner plugin;
   private final LanguageManager languageManager;

   public SpawnerSellConfirmUI(SmartSpawner plugin) {
      this.plugin = plugin;
      this.languageManager = plugin.getLanguageManager();
   }

   public void reload() {
   }

   public void openSellConfirmGui(Player player, SpawnerData spawner, SpawnerSellConfirmUI.PreviousGui previousGui, boolean collectExp) {
      this.openSellConfirmGui(player, spawner, previousGui, collectExp, null, "click");
   }

   public void openSellConfirmGui(Player player, SpawnerData spawner, SpawnerSellConfirmUI.PreviousGui previousGui, boolean collectExp, GuiButton sourceButton) {
      this.openSellConfirmGui(player, spawner, previousGui, collectExp, sourceButton, "click");
   }

   public void openSellConfirmGui(
      Player player, SpawnerData spawner, SpawnerSellConfirmUI.PreviousGui previousGui, boolean collectExp, GuiButton sourceButton, String sourceClickType
   ) {
      if (player != null && spawner != null) {
         if (spawner.getVirtualInventory().getUsedSlots() == 0) {
            this.plugin.getMessageService().sendMessage(player, "spawner_storage_empty");
            if (sourceButton != null) {
               this.plugin.getGuiButtonInteractionService().playFailSound(player, sourceButton, sourceClickType);
            }
         } else if (this.plugin.getGuiLayoutConfig().isSkipSellConfirmation()) {
            if (!spawner.isSelling()) {
               long expCollected = 0L;
               long expMending = 0L;
               if (collectExp) {
                  long[] expData = this.plugin.getSpawnerMenuAction().collectExpSilently(player, spawner);
                  expCollected = expData[0];
                  expMending = expData[1];
               }

               player.closeInventory();
               Runnable onComplete = sourceButton == null ? null : () -> {
                  if (spawner.getVirtualInventory().getUsedSlots() == 0) {
                     this.plugin.getGuiButtonInteractionService().playSuccessSound(player, sourceButton, sourceClickType);
                  } else {
                     this.plugin.getGuiButtonInteractionService().playFailSound(player, sourceButton, sourceClickType);
                  }
               };
               this.plugin.getSpawnerSellManager().sellAllItems(player, spawner, onComplete, expCollected, expMending);
            }
         } else if (!spawner.isSelling()) {
            if (sourceButton != null) {
               this.plugin.getGuiButtonInteractionService().playNavigateSound(player, sourceButton, sourceClickType);
            }

            String title = this.languageManager.getGuiTitle("gui_title_sell_confirm", null);
            GuiLayout layout = this.plugin.getGuiLayoutConfig().getSellConfirmLayout(spawner, player);
            Inventory gui = Bukkit.createInventory(new SpawnerSellConfirmHolder(spawner, previousGui, collectExp, layout), 27, title);
            this.populateSellConfirmGui(gui, player, spawner, collectExp, layout);
            player.openInventory(gui);
         }
      }
   }

   private void populateSellConfirmGui(Inventory gui, Player player, SpawnerData spawner, boolean collectExp, GuiLayout layout) {
      Map<String, String> placeholders = this.createPlaceholders(spawner, collectExp);
      if (layout == null) {
         this.plugin.getLogger().warning("Sell confirm layout not loaded, using empty GUI");
      } else {
         for (GuiButton button : layout.getAllButtons().values()) {
            if (button.isEnabled() && (!button.hasCondition() || this.evaluateButtonCondition(button))) {
               ItemStack buttonItem;
               if (button.isInfoButton()) {
                  buttonItem = this.createSpawnerInfoButton(player, spawner, placeholders);
               } else {
                  String action = this.getAnyActionFromButton(button);
                  if (action == null || action.isEmpty()) {
                     continue;
                  }

                  switch (action) {
                     case "cancel":
                        buttonItem = this.createCancelButton(button.getMaterial(), placeholders);
                        break;
                     case "confirm":
                        buttonItem = this.createConfirmButton(button.getMaterial(), placeholders, collectExp);
                        break;
                     case "none":
                        buttonItem = this.createSpawnerInfoButton(player, spawner, placeholders);
                        break;
                     default:
                        this.plugin.getLogger().warning("Unknown action in sell confirm GUI: " + action);
                        continue;
                  }
               }

               gui.setItem(button.getSlot(), buttonItem);
            }
         }
      }
   }

   private boolean evaluateButtonCondition(GuiButton button) {
      String var2 = button.getCondition();

      return switch (var2) {
         case "sell_integration", "shop_integration" -> this.plugin.hasSellIntegration();
         case "no_sell_integration", "no_shop_integration" -> !this.plugin.hasSellIntegration();
         default -> true;
      };
   }

   private ItemStack createCancelButton(Material material, Map<String, String> placeholders) {
      String name = this.languageManager.getGuiItemName("button_sell_cancel.name", placeholders);
      String[] lore = this.languageManager.getGuiItemLore("button_sell_cancel.lore", placeholders);
      return this.createButton(material, name, lore);
   }

   private ItemStack createConfirmButton(Material material, Map<String, String> placeholders, boolean collectExp) {
      String buttonKey = collectExp ? "button_sell_confirm_with_exp" : "button_sell_confirm";
      String name = this.languageManager.getGuiItemName(buttonKey + ".name", placeholders);
      String[] lore = this.languageManager.getGuiItemLore(buttonKey + ".lore", placeholders);
      return this.createButton(material, name, lore);
   }

   private ItemStack createSpawnerInfoButton(Player player, SpawnerData spawner, Map<String, String> placeholders) {
      Map<ItemSignature, Long> storedItems = spawner.getVirtualInventory().getConsolidatedItems();
      List<Component> lootComponents = this.buildSellInfoLootComponents(spawner, storedItems);
      Consumer<ItemMeta> metaModifier = meta -> {
         meta.setDisplayName(this.languageManager.getGuiItemName("button_sell_info.name", placeholders));
         List<Component> lore = this.languageManager
            .buildGuiLoreAsComponents("button_sell_info.lore", placeholders, lootComponents, "button_sell_info.loot_items_empty");
         if (!lore.isEmpty()) {
            meta.lore(lore);
         }

         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
      };
      ItemStack spawnerItem = SpawnerMobHeadTexture.getSpawnerHead(spawner, metaModifier);
      if (spawnerItem.getType() == Material.SPAWNER) {
         ItemTooltipUtil.hideTooltip(spawnerItem);
      }

      return spawnerItem;
   }

   private List<Component> buildSellInfoLootComponents(SpawnerData spawner, Map<ItemSignature, Long> storedItems) {
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
               components.add(this.languageManager.buildTranslatableGuiLootLine("button_sell_info.loot_items", material, formattedAmount, chance));
            }
         } else {
            List<Entry<ItemSignature, Long>> sortedItems = new ArrayList<>(storedItems.entrySet());
            sortedItems.sort(Comparator.comparing(e -> e.getKey().getMaterialName()));

            for (Entry<ItemSignature, Long> entry : sortedItems) {
               Material material = entry.getKey().getMaterial();
               long amount = entry.getValue();
               String formattedAmount = this.languageManager.formatNumber(amount);
               components.add(this.languageManager.buildTranslatableGuiLootLine("button_sell_info.loot_items", material, formattedAmount, ""));
            }
         }

         return components;
      }
   }

   private Map<String, String> createPlaceholders(SpawnerData spawner, boolean collectExp) {
      Map<String, String> placeholders = new HashMap<>(12);
      boolean isItemSpawner = spawner.isItemSpawner();
      String entityName;
      if (isItemSpawner) {
         Material spawnedItem = spawner.getSpawnedItemMaterial();
         entityName = spawner.getDisplayName();
         placeholders.put("spawnedItem", spawnedItem.name());
      } else {
         EntityType entityType = spawner.getEntityType();
         entityName = spawner.getDisplayName();
         placeholders.put("entityType", entityType.name());
      }

      placeholders.put("entity", entityName);
      placeholders.put("ᴇɴᴛɪᴛʏ", this.languageManager.getSmallCaps(entityName));
      if (spawner.isSellValueDirty()) {
         spawner.recalculateSellValue();
      }

      double totalSellPrice = spawner.getAccumulatedSellValue();
      int currentItems = spawner.getVirtualInventory().getUsedSlots();
      int maxItems = spawner.getMaxSpawnerLootSlots();
      double percentStorageDecimal = maxItems > 0 ? (double)currentItems / maxItems * 100.0 : 0.0;
      long currentExp = spawner.getSpawnerExp();
      placeholders.put("total_sell_price", this.languageManager.formatNumber(totalSellPrice));
      placeholders.put("current_items", Integer.toString(currentItems));
      placeholders.put("max_items", Integer.toString(maxItems));
      placeholders.put("percent_storage_decimal", String.format("%.1f", percentStorageDecimal));
      placeholders.put("percent_storage_rounded", String.valueOf((int)Math.round(percentStorageDecimal)));
      placeholders.put("current_exp", this.languageManager.formatNumber(currentExp));
      return placeholders;
   }

   private ItemStack createButton(Material material, String name, String[] lore) {
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         if (name != null) {
            meta.setDisplayName(name);
         }

         if (lore != null && lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
         }

         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
         item.setItemMeta(meta);
      }

      return item;
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

   public static enum PreviousGui {
      MAIN_MENU,
      STORAGE;
   }
}
