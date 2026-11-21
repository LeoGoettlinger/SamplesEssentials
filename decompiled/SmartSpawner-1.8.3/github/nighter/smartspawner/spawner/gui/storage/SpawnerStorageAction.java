package github.nighter.smartspawner.spawner.gui.storage;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.api.events.SpawnerDropAllEvent;
import github.nighter.smartspawner.api.events.SpawnerTakeAllEvent;
import github.nighter.smartspawner.api.gui.GuiLayoutType;
import github.nighter.smartspawner.language.LanguageManager;
import github.nighter.smartspawner.language.MessageService;
import github.nighter.smartspawner.logging.SpawnerEventType;
import github.nighter.smartspawner.spawner.data.SpawnerManager;
import github.nighter.smartspawner.spawner.gui.layout.GuiButton;
import github.nighter.smartspawner.spawner.gui.layout.GuiLayout;
import github.nighter.smartspawner.spawner.gui.main.SpawnerMenuUI;
import github.nighter.smartspawner.spawner.gui.sell.SpawnerSellConfirmUI;
import github.nighter.smartspawner.spawner.gui.storage.filter.FilterConfigUI;
import github.nighter.smartspawner.spawner.gui.synchronization.SpawnerGuiViewManager;
import github.nighter.smartspawner.spawner.lootgen.loot.LootItem;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.spawner.properties.VirtualInventory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public class SpawnerStorageAction implements Listener {
   private final SmartSpawner plugin;
   private final LanguageManager languageManager;
   private final SpawnerMenuUI spawnerMenuUI;
   private final SpawnerGuiViewManager spawnerGuiViewManager;
   private final MessageService messageService;
   private final FilterConfigUI filterConfigUI;
   private final SpawnerManager spawnerManager;
   private static final int INVENTORY_SIZE = 54;
   private static final int STORAGE_SLOTS = 45;
   private final Map<UUID, Long> lastItemClickTime = new ConcurrentHashMap<>();
   private static final long ITEM_CLICK_DELAY_MS = 100L;
   private final Map<UUID, Map<String, Long>> lastWarningTimes = new ConcurrentHashMap<>();
   private static final long WARNING_COOLDOWN_MS = 1000L;

   public SpawnerStorageAction(SmartSpawner plugin) {
      this.plugin = plugin;
      this.languageManager = plugin.getLanguageManager();
      this.spawnerMenuUI = plugin.getSpawnerMenuUI();
      this.spawnerGuiViewManager = plugin.getSpawnerGuiViewManager();
      this.messageService = plugin.getMessageService();
      this.filterConfigUI = plugin.getFilterConfigUI();
      this.spawnerManager = plugin.getSpawnerManager();
      this.loadConfig();
   }

   public void loadConfig() {
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player player && event.getInventory().getHolder(false) instanceof StoragePageHolder holder) {
         SpawnerData var8 = holder.getSpawnerData();
         int slot = event.getRawSlot();
         event.setCancelled(true);
         if (var8.isSelling()) {
            this.plugin.getMessageService().sendMessage(player, "action_in_progress");
         } else if (slot >= 0 && slot < 54) {
            if (this.isItemSlot(slot)) {
               this.handleItemSlotClick(player, slot, holder, var8, event);
            } else {
               if (this.isControlSlot(slot, holder.getLayout())) {
                  ItemStack clickedItem = event.getCurrentItem();
                  if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                     return;
                  }

                  this.handleControlSlotClick(player, slot, holder, var8, event.getInventory(), event.getClick(), holder.getLayout());
               }
            }
         }
      }
   }

   private void handleControlSlotClick(
      Player player, int slot, StoragePageHolder holder, SpawnerData spawner, Inventory inventory, ClickType clickType, GuiLayout layout
   ) {
      Optional<GuiButton> buttonOpt = layout.getButtonAtSlot(slot);
      if (!buttonOpt.isEmpty()) {
         GuiButton button = buttonOpt.get();
         String clickTypeString = this.getClickTypeString(clickType);
         String action = button.getActionWithFallback(clickTypeString);
         if (action != null && !action.isEmpty()) {
            if (!"none".equals(action)) {
               if (this.plugin.getGuiButtonInteractionService().tryUse(player, GuiLayoutType.STORAGE_GUI, button)) {
                  switch (action) {
                     case "sort_items":
                        this.playActionResult(player, button, clickTypeString, this.handleSortItemsClick(player, spawner, inventory));
                        break;
                     case "open_filter":
                        this.plugin.getGuiButtonInteractionService().playNavigateSound(player, button, clickTypeString);
                        this.openFilterConfig(player, spawner);
                        break;
                     case "previous_page":
                        if (holder.getCurrentPage() > 1) {
                           this.plugin.getGuiButtonInteractionService().playNavigateSound(player, button, clickTypeString);
                           this.updatePageContent(player, spawner, holder.getCurrentPage() - 1, inventory);
                        }
                        break;
                     case "take_all":
                        this.playActionResult(player, button, clickTypeString, this.handleTakeAllItems(player, inventory));
                        break;
                     case "next_page":
                        if (holder.getCurrentPage() < holder.getTotalPages()) {
                           this.plugin.getGuiButtonInteractionService().playNavigateSound(player, button, clickTypeString);
                           this.updatePageContent(player, spawner, holder.getCurrentPage() + 1, inventory);
                        }
                        break;
                     case "drop_page":
                        this.playActionResult(player, button, clickTypeString, this.handleDropPageItems(player, spawner, inventory));
                        break;
                     case "sell_all":
                        this.handleSellAction(player, spawner, false, button, clickTypeString, inventory);
                        break;
                     case "sell_and_exp":
                        this.handleSellAction(player, spawner, true, button, clickTypeString, inventory);
                        break;
                     case "collect_exp":
                        this.playActionResult(player, button, clickTypeString, this.handleCollectExpAction(player, spawner, inventory));
                        break;
                     case "return_main":
                        this.plugin.getGuiButtonInteractionService().playNavigateSound(player, button, clickTypeString);
                        this.handleReturnToMainMenu(player, spawner);
                     case "none":
                        break;
                     default:
                        this.plugin.getLogger().warning("Unknown storage action: " + action);
                  }
               }
            }
         }
      }
   }

   private String getClickTypeString(ClickType clickType) {
      return switch (clickType) {
         case LEFT -> "left_click";
         case RIGHT -> "right_click";
         case SHIFT_LEFT -> "shift_left_click";
         case SHIFT_RIGHT -> "shift_right_click";
         default -> "left_click";
      };
   }

   private void handleSellAction(Player player, SpawnerData spawner, boolean collectExp, GuiButton sourceButton, String sourceClickType, Inventory inventory) {
      if (!this.plugin.hasSellIntegration()) {
         this.plugin.getGuiButtonInteractionService().playFailSound(player, sourceButton, sourceClickType);
      } else if (!player.hasPermission("smartspawner.sellall")) {
         this.messageService.sendMessage(player, "no_permission");
         this.plugin.getGuiButtonInteractionService().playFailSound(player, sourceButton, sourceClickType);
      } else if (spawner.getVirtualInventory().getUsedSlots() != 0) {
         this.plugin
            .getSpawnerSellConfirmUI()
            .openSellConfirmGui(player, spawner, SpawnerSellConfirmUI.PreviousGui.STORAGE, collectExp, sourceButton, sourceClickType);
      } else {
         if (collectExp && spawner.getSpawnerExp() > 0L) {
            boolean success = this.handleCollectExpAction(player, spawner, inventory);
            this.playActionResult(player, sourceButton, sourceClickType, success);
         } else {
            this.messageService.sendMessage(player, "spawner_storage_empty");
            this.plugin.getGuiButtonInteractionService().playFailSound(player, sourceButton, sourceClickType);
         }
      }
   }

   private boolean handleCollectExpAction(Player player, SpawnerData spawner, Inventory inventory) {
      boolean collected = this.plugin.getSpawnerMenuAction().tryCollectExpForPlayer(player, spawner);
      if (collected) {
         StoragePageHolder holder = (StoragePageHolder)inventory.getHolder(false);
         if (holder != null) {
            this.plugin.getSpawnerStorageUI().updateDisplay(inventory, spawner, holder.getCurrentPage(), holder.getTotalPages());
         }
      }

      return collected;
   }

   private void handleReturnToMainMenu(Player player, SpawnerData spawner) {
      player.closeInventory();
      this.spawnerMenuUI.openSpawnerMenu(player, spawner, true);
   }

   private boolean isControlSlot(int slot, GuiLayout layout) {
      return layout != null && layout.isSlotUsed(slot);
   }

   private boolean isItemSlot(int slot) {
      return slot >= 0 && slot < 45;
   }

   private void handleItemSlotClick(Player player, int slot, StoragePageHolder holder, SpawnerData spawner, InventoryClickEvent event) {
      if (!this.isItemClickTooFrequent(player)) {
         this.lastItemClickTime.put(player.getUniqueId(), System.currentTimeMillis());
         Inventory inventory = event.getInventory();
         ItemStack clickedItem = inventory.getItem(slot);
         if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            ClickType clickType = event.getClick();
            int amountToTake;
            switch (clickType) {
               case LEFT:
                  amountToTake = 1;
                  break;
               case RIGHT:
                  amountToTake = (int)Math.ceil(clickedItem.getAmount() / 2.0);
                  break;
               case SHIFT_LEFT:
               case SHIFT_RIGHT:
                  amountToTake = clickedItem.getAmount();
                  break;
               default:
                  return;
            }

            this.transferToPlayerInventory(player, clickedItem, amountToTake, inventory, spawner, holder);
         }
      }
   }

   private void transferToPlayerInventory(
      Player player, ItemStack clickedItem, int amountToTake, Inventory storageInv, SpawnerData spawner, StoragePageHolder holder
   ) {
      PlayerInventory playerInv = player.getInventory();
      ItemStack toTransfer = clickedItem.clone();
      toTransfer.setAmount(amountToTake);
      int amountMoved = 0;
      int remaining = amountToTake;

      for (int i = 0; i < 36 && remaining > 0; i++) {
         ItemStack slot = playerInv.getItem(i);
         if (slot != null && slot.getType() != Material.AIR && slot.isSimilar(toTransfer)) {
            int space = slot.getMaxStackSize() - slot.getAmount();
            if (space > 0) {
               int add = Math.min(space, remaining);
               slot.setAmount(slot.getAmount() + add);
               amountMoved += add;
               remaining -= add;
            }
         }
      }

      for (int ix = 0; ix < 36 && remaining > 0; ix++) {
         ItemStack slot = playerInv.getItem(ix);
         if (slot == null || slot.getType() == Material.AIR) {
            int stackSize = Math.min(remaining, toTransfer.getMaxStackSize());
            ItemStack newStack = toTransfer.clone();
            newStack.setAmount(stackSize);
            playerInv.setItem(ix, newStack);
            amountMoved += stackSize;
            remaining -= stackSize;
         }
      }

      if (amountMoved > 0) {
         ItemStack removed = toTransfer.clone();
         removed.setAmount(amountMoved);
         if (spawner.removeItemsAndUpdateSellValue(List.of(removed))) {
            this.updatePageAfterRemoval(player, storageInv, spawner, holder);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5F, 1.0F);
            if (remaining > 0) {
               this.sendThrottledWarning(player, "inventory_full");
            }
         }
      } else {
         this.sendThrottledWarning(player, "inventory_full");
      }
   }

   private void updatePageAfterRemoval(Player player, Inventory inventory, SpawnerData spawner, StoragePageHolder holder) {
      int newTotalPages = this.calculateTotalPages(spawner);
      int currentPage = holder.getCurrentPage();
      int adjustedPage = Math.max(1, Math.min(currentPage, newTotalPages));
      holder.setTotalPages(newTotalPages);
      if (adjustedPage != currentPage) {
         holder.setCurrentPage(adjustedPage);
      }

      holder.updateOldUsedSlots();
      SpawnerStorageUI spawnerStorageUI = this.plugin.getSpawnerStorageUI();
      spawnerStorageUI.updateDisplay(inventory, spawner, adjustedPage, newTotalPages);
      if (newTotalPages != currentPage || adjustedPage != currentPage) {
         this.updateInventoryTitle(player, spawner, adjustedPage, newTotalPages);
      }

      spawner.updateHologramData();
      this.spawnerGuiViewManager.updateSpawnerMenuViewers(spawner);
      if (spawner.getMaxSpawnerLootSlots() > holder.getOldUsedSlots() && spawner.getIsAtCapacity()) {
         spawner.setIsAtCapacity(false);
      }

      spawner.markStorageDirty();
   }

   private boolean handleDropPageItems(Player player, SpawnerData spawner, Inventory inventory) {
      StoragePageHolder holder = (StoragePageHolder)inventory.getHolder(false);
      if (holder == null) {
         return false;
      } else {
         List<ItemStack> pageItems = new ArrayList<>();
         int itemsFoundCount = 0;

         for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
               pageItems.add(item.clone());
               itemsFoundCount += item.getAmount();
               inventory.setItem(i, null);
            }
         }

         if (pageItems.isEmpty()) {
            this.messageService.sendMessage(player, "spawner_storage_empty");
            return false;
         } else {
            if (SpawnerDropAllEvent.getHandlerList().getRegisteredListeners().length != 0) {
               SpawnerDropAllEvent event = new SpawnerDropAllEvent(player, spawner.getSpawnerLocation(), pageItems);
               Bukkit.getPluginManager().callEvent(event);
               if (event.isCancelled()) {
                  return false;
               }

               pageItems = event.getItems();
            }

            int itemsFound = itemsFoundCount;
            spawner.removeItemsAndUpdateSellValue(pageItems);
            this.dropItemsInDirection(player, pageItems);
            int newTotalPages = this.calculateTotalPages(spawner);
            if (holder.getCurrentPage() > newTotalPages) {
               holder.setCurrentPage(Math.max(1, newTotalPages));
            }

            holder.setTotalPages(newTotalPages);
            holder.updateOldUsedSlots();
            spawner.updateHologramData();
            this.spawnerGuiViewManager.updateSpawnerMenuViewers(spawner);
            if (spawner.getMaxSpawnerLootSlots() > holder.getOldUsedSlots() && spawner.getIsAtCapacity()) {
               spawner.setIsAtCapacity(false);
            }

            spawner.markStorageDirty();
            if (this.plugin.getSpawnerActionLogger() != null) {
               this.plugin
                  .getSpawnerActionLogger()
                  .log(
                     SpawnerEventType.SPAWNER_DROP_PAGE_ITEMS,
                     builder -> builder.player(player.getName(), player.getUniqueId())
                        .location(spawner.getSpawnerLocation())
                        .entityType(spawner.getEntityType())
                        .metadata("items_dropped", itemsFound)
                        .metadata("page_number", holder.getCurrentPage())
                  );
            }

            this.updatePageContent(player, spawner, holder.getCurrentPage(), inventory);
            return true;
         }
      }
   }

   private void dropItemsInDirection(Player player, List<ItemStack> items) {
      if (!items.isEmpty()) {
         Location playerLoc = player.getLocation();
         World world = player.getWorld();
         UUID playerUUID = player.getUniqueId();
         double yaw = Math.toRadians(playerLoc.getYaw());
         double pitch = Math.toRadians(playerLoc.getPitch());
         double sinYaw = -Math.sin(yaw);
         double cosYaw = Math.cos(yaw);
         double cosPitch = Math.cos(pitch);
         double sinPitch = -Math.sin(pitch);
         Location dropLocation = playerLoc.clone();
         dropLocation.add(sinYaw * 0.3, 1.2, cosYaw * 0.3);
         Vector velocity = new Vector(sinYaw * cosPitch * 0.3, sinPitch * 0.3 + 0.1, cosYaw * cosPitch * 0.3);

         for (ItemStack item : items) {
            Item droppedItem = world.dropItem(dropLocation, item, drop -> {
               drop.setThrower(playerUUID);
               drop.setPickupDelay(40);
            });
            droppedItem.setVelocity(velocity);
         }
      }
   }

   private void openFilterConfig(Player player, SpawnerData spawner) {
      this.filterConfigUI.openFilterConfigGUI(player, spawner);
   }

   private void updatePageContent(Player player, SpawnerData spawner, int newPage, Inventory inventory) {
      SpawnerStorageUI spawnerStorageUI = this.plugin.getSpawnerStorageUI();
      StoragePageHolder holder = (StoragePageHolder)inventory.getHolder(false);
      int totalPages = this.calculateTotalPages(spawner);

      assert holder != null;

      holder.setTotalPages(totalPages);
      holder.setCurrentPage(newPage);
      holder.updateOldUsedSlots();
      spawnerStorageUI.updateDisplay(inventory, spawner, newPage, totalPages);
      this.updateInventoryTitle(player, spawner, newPage, totalPages);
   }

   private int calculateTotalPages(SpawnerData spawner) {
      int usedSlots = spawner.getVirtualInventory().getUsedSlots();
      return Math.max(1, (int)Math.ceil(usedSlots / 45.0));
   }

   private void updateInventoryTitle(Player player, SpawnerData spawner, int page, int totalPages) {
      String newTitle = this.plugin.getSpawnerStorageUI().getStorageTitle(spawner, page, totalPages);

      try {
         player.getOpenInventory().setTitle(newTitle);
      } catch (Exception var7) {
         this.openLootPage(player, spawner, page);
      }
   }

   private boolean isItemClickTooFrequent(Player player) {
      long now = System.currentTimeMillis();
      long last = this.lastItemClickTime.getOrDefault(player.getUniqueId(), 0L);
      if (now - last < 100L) {
         this.sendThrottledWarning(player, "click_too_fast");
         return true;
      } else {
         return false;
      }
   }

   private void sendThrottledWarning(Player player, String messageKey) {
      long now = System.currentTimeMillis();
      Map<String, Long> playerWarnings = this.lastWarningTimes.computeIfAbsent(player.getUniqueId(), ignored -> new ConcurrentHashMap<>());
      long lastWarning = playerWarnings.getOrDefault(messageKey, 0L);
      if (now - lastWarning >= 1000L) {
         playerWarnings.put(messageKey, now);
         this.messageService.sendMessage(player, messageKey);
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      UUID playerId = event.getPlayer().getUniqueId();
      this.lastItemClickTime.remove(playerId);
      this.lastWarningTimes.remove(playerId);
   }

   private boolean handleSortItemsClick(Player player, SpawnerData spawner, Inventory inventory) {
      if (spawner.getLootConfig() != null && spawner.getLootConfig().getAllItems() != null) {
         List<LootItem> lootItems = spawner.getLootConfig().getAllItems();
         if (lootItems.isEmpty()) {
            return false;
         } else {
            Material currentSort = spawner.getPreferredSortItem();
            List<Material> sortedLoot = lootItems.stream().map(LootItem::material).distinct().sorted(Comparator.comparing(Enum::name)).toList();
            if (sortedLoot.isEmpty()) {
               return false;
            } else {
               Material nextSort;
               if (currentSort == null) {
                  nextSort = sortedLoot.getFirst();
               } else {
                  int currentIndex = sortedLoot.indexOf(currentSort);
                  if (currentIndex == -1) {
                     nextSort = sortedLoot.getFirst();
                  } else {
                     int nextIndex = (currentIndex + 1) % sortedLoot.size();
                     nextSort = sortedLoot.get(nextIndex);
                  }
               }

               spawner.setPreferredSortItem(nextSort);
               spawner.markStorageDirty();
               this.spawnerManager.queueSpawnerForSaving(spawner.getSpawnerId());
               spawner.getVirtualInventory().sortItems(nextSort);
               StoragePageHolder holder = (StoragePageHolder)inventory.getHolder(false);
               if (holder != null) {
                  this.updatePageContent(player, spawner, holder.getCurrentPage(), inventory);
               }

               if (this.plugin.getSpawnerActionLogger() != null) {
                  this.plugin
                     .getSpawnerActionLogger()
                     .log(
                        SpawnerEventType.SPAWNER_ITEMS_SORT,
                        builder -> builder.player(player.getName(), player.getUniqueId())
                           .location(spawner.getSpawnerLocation())
                           .entityType(spawner.getEntityType())
                           .metadata("sort_item", nextSort.name())
                           .metadata("previous_sort", currentSort != null ? currentSort.name() : "none")
                     );
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   private void openLootPage(Player player, SpawnerData spawner, int page) {
      SpawnerStorageUI spawnerStorageUI = this.plugin.getSpawnerStorageUI();
      int totalPages = this.calculateTotalPages(spawner);
      int finalPage = Math.max(1, Math.min(page, totalPages));
      Inventory pageInventory = spawnerStorageUI.createStorageInventory(player, spawner, finalPage, totalPages);
      if (this.plugin.getSpawnerActionLogger() != null) {
         this.plugin
            .getSpawnerActionLogger()
            .log(
               SpawnerEventType.SPAWNER_STORAGE_OPEN,
               builder -> builder.player(player.getName(), player.getUniqueId())
                  .location(spawner.getSpawnerLocation())
                  .entityType(spawner.getEntityType())
                  .metadata("page", finalPage)
                  .metadata("total_pages", totalPages)
            );
      }

      player.openInventory(pageInventory);
   }

   public boolean handleTakeAllItems(Player player, Inventory sourceInventory) {
      StoragePageHolder holder = (StoragePageHolder)sourceInventory.getHolder(false);
      SpawnerData spawner = holder.getSpawnerData();
      VirtualInventory virtualInv = spawner.getVirtualInventory();
      Map<Integer, ItemStack> sourceItems = new HashMap<>();

      for (int i = 0; i < 45; i++) {
         ItemStack item = sourceInventory.getItem(i);
         if (item != null && item.getType() != Material.AIR) {
            sourceItems.put(i, item.clone());
         }
      }

      if (sourceItems.isEmpty()) {
         this.messageService.sendMessage(player, "spawner_storage_empty");
         return false;
      } else {
         if (SpawnerTakeAllEvent.getHandlerList().getRegisteredListeners().length != 0) {
            SpawnerTakeAllEvent event = new SpawnerTakeAllEvent(player, spawner.getSpawnerLocation(), sourceItems);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               return false;
            }

            sourceItems = event.getItems();
         }

         SpawnerStorageAction.TransferResult result = this.transferItems(player, sourceInventory, sourceItems, virtualInv);
         this.sendTransferMessage(player, result);
         player.updateInventory();
         if (result.anyItemMoved) {
            int newTotalPages = this.calculateTotalPages(spawner);
            int currentPage = holder.getCurrentPage();
            int adjustedPage = Math.max(1, Math.min(currentPage, newTotalPages));
            holder.setTotalPages(newTotalPages);
            if (adjustedPage != currentPage) {
               holder.setCurrentPage(adjustedPage);
               SpawnerStorageUI spawnerStorageUI = this.plugin.getSpawnerStorageUI();
               spawnerStorageUI.updateDisplay(sourceInventory, spawner, adjustedPage, newTotalPages);
            }

            this.updateInventoryTitle(player, spawner, adjustedPage, newTotalPages);
            this.spawnerGuiViewManager.updateSpawnerMenuViewers(spawner);
            if (spawner.getMaxSpawnerLootSlots() > holder.getOldUsedSlots() && spawner.getIsAtCapacity()) {
               spawner.setIsAtCapacity(false);
            }

            spawner.markStorageDirty();
            if (this.plugin.getSpawnerActionLogger() != null) {
               int itemsLeft = spawner.getVirtualInventory().getUsedSlots();
               this.plugin
                  .getSpawnerActionLogger()
                  .log(
                     SpawnerEventType.SPAWNER_ITEM_TAKE_ALL,
                     builder -> builder.player(player.getName(), player.getUniqueId())
                        .location(spawner.getSpawnerLocation())
                        .entityType(spawner.getEntityType())
                        .metadata("items_taken", result.totalMoved)
                        .metadata("items_left", itemsLeft)
                  );
            }
         }

         return result.anyItemMoved;
      }
   }

   private void playActionResult(Player player, GuiButton button, String clickType, boolean success) {
      if (success) {
         this.plugin.getGuiButtonInteractionService().playSuccessSound(player, button, clickType);
      } else {
         this.plugin.getGuiButtonInteractionService().playFailSound(player, button, clickType);
      }
   }

   private SpawnerStorageAction.TransferResult transferItems(
      Player player, Inventory sourceInventory, Map<Integer, ItemStack> sourceItems, VirtualInventory virtualInv
   ) {
      boolean anyItemMoved = false;
      boolean inventoryFull = false;
      PlayerInventory playerInv = player.getInventory();
      int totalAmountMoved = 0;
      List<ItemStack> itemsToRemove = new ArrayList<>();

      for (Entry<Integer, ItemStack> entry : sourceItems.entrySet()) {
         int sourceSlot = entry.getKey();
         ItemStack itemToMove = entry.getValue();
         int amountToMove = itemToMove.getAmount();
         int amountMoved = 0;

         for (int i = 0; i < 36 && amountToMove > 0; i++) {
            ItemStack targetItem = playerInv.getItem(i);
            if (targetItem == null || targetItem.getType() == Material.AIR) {
               ItemStack newStack = itemToMove.clone();
               newStack.setAmount(Math.min(amountToMove, itemToMove.getMaxStackSize()));
               playerInv.setItem(i, newStack);
               amountMoved += newStack.getAmount();
               amountToMove -= newStack.getAmount();
               anyItemMoved = true;
            } else if (targetItem.isSimilar(itemToMove)) {
               int spaceInStack = targetItem.getMaxStackSize() - targetItem.getAmount();
               if (spaceInStack > 0) {
                  int addAmount = Math.min(spaceInStack, amountToMove);
                  targetItem.setAmount(targetItem.getAmount() + addAmount);
                  amountMoved += addAmount;
                  amountToMove -= addAmount;
                  anyItemMoved = true;
               }
            }
         }

         if (amountMoved > 0) {
            totalAmountMoved += amountMoved;
            ItemStack movedItem = itemToMove.clone();
            movedItem.setAmount(amountMoved);
            itemsToRemove.add(movedItem);
            if (amountMoved == itemToMove.getAmount()) {
               sourceInventory.setItem(sourceSlot, null);
            } else {
               ItemStack remaining = itemToMove.clone();
               remaining.setAmount(itemToMove.getAmount() - amountMoved);
               sourceInventory.setItem(sourceSlot, remaining);
               inventoryFull = true;
            }
         }

         if (inventoryFull) {
            break;
         }
      }

      if (!itemsToRemove.isEmpty()) {
         StoragePageHolder holder = (StoragePageHolder)sourceInventory.getHolder(false);
         SpawnerData spawnerData = holder.getSpawnerData();
         spawnerData.removeItemsAndUpdateSellValue(itemsToRemove);
         spawnerData.updateHologramData();
         holder.updateOldUsedSlots();
      }

      return new SpawnerStorageAction.TransferResult(anyItemMoved, inventoryFull, totalAmountMoved);
   }

   private void sendTransferMessage(Player player, SpawnerStorageAction.TransferResult result) {
      if (!result.anyItemMoved) {
         this.sendThrottledWarning(player, "inventory_full");
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onInventoryOpen(InventoryOpenEvent event) {
      if (event.getPlayer() instanceof Player player && event.getInventory().getHolder(false) instanceof StoragePageHolder holder) {
         if (this.isStorageInUseByOther(player, holder.getSpawnerData())) {
            event.setCancelled(true);
            this.messageService.sendMessage(player, "storage_in_use");
            Scheduler.runEntityTask(player, () -> {
               if (player.isOnline() && player.getOpenInventory().getType() == InventoryType.CRAFTING) {
                  player.closeInventory();
               }
            });
         }
      }
   }

   public boolean isStorageInUseByOther(Player player, SpawnerData spawner) {
      String spawnerId = spawner.getSpawnerId();

      for (Player viewer : this.spawnerGuiViewManager.getViewers(spawnerId)) {
         if (!viewer.getUniqueId().equals(player.getUniqueId()) && this.isViewingStorage(viewer, spawnerId)) {
            return true;
         }
      }

      return false;
   }

   private boolean isViewingStorage(Player viewer, String spawnerId) {
      return viewer.getOpenInventory().getTopInventory().getHolder(false) instanceof StoragePageHolder holder
         && holder.getSpawnerData().getSpawnerId().equals(spawnerId);
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      if (event.getInventory().getHolder(false) instanceof StoragePageHolder) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      Inventory inventory = event.getInventory();
      if (inventory.getHolder(false) instanceof StoragePageHolder holder) {
         this.handleInventoryClose(holder);
      }
   }

   private void handleInventoryClose(StoragePageHolder holder) {
      SpawnerData spawner = holder.getSpawnerData();
      if (spawner.isStorageDirty()) {
         this.plugin.getSpawnerManager().markSpawnerModified(spawner.getSpawnerId());
         spawner.clearStorageDirty();
      }
   }

   private record TransferResult(boolean anyItemMoved, boolean inventoryFull, int totalMoved) {
   }
}
