package com.donutorders.manager;

import com.donutorders.gui.BaseGUI;
import com.donutorders.gui.CollectStashGUI;
import com.donutorders.gui.ConfirmDeliveryGUI;
import com.donutorders.gui.DeliverItemsGUI;
import com.donutorders.gui.EnchantLevelGUI;
import com.donutorders.gui.EnchantPickerGUI;
import com.donutorders.gui.NewOrderGUI;
import com.donutorders.gui.OrderDetailGUI;
import com.donutorders.gui.PublicOrdersGUI;
import com.donutorders.gui.YourOrdersGUI;
import com.donutorders.model.Order;
import com.donutorders.model.OrderStatus;
import com.donutorders.scheduler.FoliaScheduler;
import com.donutorders.storage.StorageManager;
import com.donutorders.util.ItemUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GUIManager {
   private final StorageManager storage;
   private final OrderManager orderManager;
   private final ChatInputHandler chatInput;
   private final ConcurrentHashMap<UUID, GUIManager.PlayerGUIState> states = new ConcurrentHashMap<>();

   public GUIManager(StorageManager storage, OrderManager orderManager, ChatInputHandler chatInput) {
      this.storage = storage;
      this.orderManager = orderManager;
      this.chatInput = chatInput;
   }

   public void openPublicOrders(Player player, int page) {
      FoliaScheduler.runAtEntity(player, () -> {
         List<Order> active = this.storage.getAllActiveOrders();
         PublicOrdersGUI gui = new PublicOrdersGUI(this, active, page);
         GUIManager.PlayerGUIState state = new GUIManager.PlayerGUIState();
         state.type = GUIManager.GUIType.PUBLIC_ORDERS;
         state.gui = gui;
         player.openInventory(gui.getInventory());
         this.states.put(player.getUniqueId(), state);
      }, null);
   }

   public void openYourActiveOrders(Player player, int page) {
      this.openYourOrdersView(player, page, YourOrdersGUI.ViewMode.ACTIVE, null, null);
   }

   public void openYourOrderHistory(Player player, int page) {
      this.openYourOrdersView(player, page, YourOrdersGUI.ViewMode.HISTORY, null, null);
   }

   public void openAdminOrderHistory(Player viewer, UUID targetUuid, String targetName, int page) {
      this.openYourOrdersView(viewer, page, YourOrdersGUI.ViewMode.ADMIN_HISTORY, targetUuid, targetName);
   }

   /** @deprecated */
   public void openYourOrders(Player player, int page) {
      this.openYourActiveOrders(player, page);
   }

   private void openYourOrdersView(Player player, int page, YourOrdersGUI.ViewMode viewMode, UUID adminTargetUuid, String adminTargetName) {
      FoliaScheduler.runAtEntity(player, () -> {
         UUID ownerUuid = viewMode == YourOrdersGUI.ViewMode.ADMIN_HISTORY ? adminTargetUuid : player.getUniqueId();
         List<Order> playerOrders = filterOrdersForView(this.storage.getPlayerOrders(ownerUuid), viewMode);
         YourOrdersGUI gui = new YourOrdersGUI(this, playerOrders, page, viewMode, adminTargetUuid, adminTargetName);
         GUIManager.PlayerGUIState state = new GUIManager.PlayerGUIState();
         state.type = GUIManager.GUIType.YOUR_ORDERS;
         state.gui = gui;
         player.openInventory(gui.getInventory());
         this.states.put(player.getUniqueId(), state);
      }, null);
   }

   private static List<Order> filterOrdersForView(List<Order> orders, YourOrdersGUI.ViewMode viewMode) {
      List<Order> filtered = new ArrayList<>();

      for (Order order : orders) {
         OrderStatus status = order.getStatus();
         if (viewMode == YourOrdersGUI.ViewMode.ACTIVE) {
            if (status == OrderStatus.ACTIVE || status == OrderStatus.PENDING) {
               filtered.add(order);
            }
         } else if (status == OrderStatus.COMPLETED || status == OrderStatus.EXPIRED || status == OrderStatus.CANCELLED || status == OrderStatus.CLAIMED) {
            filtered.add(order);
         }
      }

      return filtered;
   }

   public void openNewOrderPicker(Player player) {
      this.openNewOrderPicker(player, 0, true);
   }

   public void openNewOrderPicker(Player player, int page) {
      this.openNewOrderPicker(player, page, false);
   }

   private void openNewOrderPicker(Player player, int page, boolean clearSearch) {
      FoliaScheduler.runAtEntity(player, () -> {
         GUIManager.PlayerGUIState existing = this.states.get(player.getUniqueId());
         String searchQuery = clearSearch ? null : (existing != null ? existing.materialSearchQuery : null);
         NewOrderGUI gui = new NewOrderGUI(this, Math.max(0, page), searchQuery);
         GUIManager.PlayerGUIState state = new GUIManager.PlayerGUIState();
         state.type = GUIManager.GUIType.NEW_ORDER;
         state.gui = gui;
         state.materialSearchQuery = searchQuery;
         player.openInventory(gui.getInventory());
         this.states.put(player.getUniqueId(), state);
      }, null);
   }

   public void openNewOrderPickerWithSearch(Player player, String searchQuery) {
      FoliaScheduler.runAtEntity(player, () -> {
         String query = searchQuery != null && !searchQuery.isBlank() ? searchQuery.trim() : null;
         NewOrderGUI gui = new NewOrderGUI(this, 0, query);
         GUIManager.PlayerGUIState state = new GUIManager.PlayerGUIState();
         state.type = GUIManager.GUIType.NEW_ORDER;
         state.gui = gui;
         state.materialSearchQuery = query;
         player.openInventory(gui.getInventory());
         this.states.put(player.getUniqueId(), state);
      }, null);
   }

   public void openEnchantPicker(Player player, int page) {
      FoliaScheduler.runAtEntity(player, () -> {
         EnchantPickerGUI gui = new EnchantPickerGUI(this, Math.max(0, page));
         GUIManager.PlayerGUIState state = new GUIManager.PlayerGUIState();
         state.type = GUIManager.GUIType.ENCHANT_PICKER;
         state.gui = gui;
         player.openInventory(gui.getInventory());
         this.states.put(player.getUniqueId(), state);
      }, null);
   }

   public void openEnchantLevel(Player player, Enchantment enchantment) {
      FoliaScheduler.runAtEntity(player, () -> {
         EnchantLevelGUI gui = new EnchantLevelGUI(this, enchantment);
         GUIManager.PlayerGUIState state = this.states.get(player.getUniqueId());
         if (state == null) {
            state = new GUIManager.PlayerGUIState();
         }

         state.type = GUIManager.GUIType.ENCHANT_LEVEL;
         state.gui = gui;
         player.openInventory(gui.getInventory());
         this.states.put(player.getUniqueId(), state);
      }, null);
   }

   public void openDeliverItems(Player player, UUID orderId) {
      Order order = this.storage.getOrder(orderId);
      if (order != null) {
         FoliaScheduler.runAtEntity(player, () -> {
            DeliverItemsGUI gui = new DeliverItemsGUI(this, order);
            GUIManager.PlayerGUIState state = new GUIManager.PlayerGUIState();
            state.type = GUIManager.GUIType.DELIVER_ITEMS;
            state.gui = gui;
            state.contextOrderId = orderId;
            state.deliverySlots = new ItemStack[45];
            player.openInventory(gui.getInventory());
            this.states.put(player.getUniqueId(), state);
         }, null);
      }
   }

   public void openConfirmDelivery(Player player, UUID orderId, ItemStack[] items) {
      Order order = this.storage.getOrder(orderId);
      if (order == null) {
         FoliaScheduler.runAtEntity(player, () -> ItemUtils.giveOrDropAll(player, items, true), null);
      } else {
         FoliaScheduler.runAtEntity(player, () -> {
            ConfirmDeliveryGUI gui = new ConfirmDeliveryGUI(this, player, order, items);
            GUIManager.PlayerGUIState state = new GUIManager.PlayerGUIState();
            state.type = GUIManager.GUIType.CONFIRM_DELIVERY;
            state.gui = gui;
            state.contextOrderId = orderId;
            state.deliverySlots = items;
            player.openInventory(gui.getInventory());
            this.states.put(player.getUniqueId(), state);
         }, null);
      }
   }

   public void openCollectStash(Player player, UUID orderId) {
      this.storage.loadStash(orderId, stash -> FoliaScheduler.runAtEntity(player, () -> {
         Order order = this.storage.getOrder(orderId);
         if (order != null) {
            CollectStashGUI gui = new CollectStashGUI(this, order, stash);
            GUIManager.PlayerGUIState state = new GUIManager.PlayerGUIState();
            state.type = GUIManager.GUIType.COLLECT_STASH;
            state.gui = gui;
            state.contextOrderId = orderId;
            player.openInventory(gui.getInventory());
            this.states.put(player.getUniqueId(), state);
         }
      }, null));
   }

   public void openOrderDetail(Player player, UUID orderId) {
      Order order = this.storage.getOrder(orderId);
      if (order != null) {
         this.storage.loadStash(orderId, stash -> FoliaScheduler.runAtEntity(player, () -> {
            boolean hasStashItems = false;

            for (ItemStack i : stash) {
               if (i != null && i.getType() != Material.AIR) {
                  hasStashItems = true;
                  break;
               }
            }

            OrderDetailGUI gui = new OrderDetailGUI(this, order, hasStashItems);
            GUIManager.PlayerGUIState state = new GUIManager.PlayerGUIState();
            state.type = GUIManager.GUIType.ORDER_DETAIL;
            state.gui = gui;
            state.contextOrderId = orderId;
            player.openInventory(gui.getInventory());
            this.states.put(player.getUniqueId(), state);
         }, null));
      }
   }

   public GUIManager.PlayerGUIState getState(UUID playerUUID) {
      return this.states.get(playerUUID);
   }

   public void clearState(UUID playerUUID) {
      this.states.remove(playerUUID);
   }

   public void returnHeldDeliveryItems(Player player) {
      this.returnHeldDeliveryItems(player, null);
   }

   public void returnHeldDeliveryItems(Player player, List<ItemStack> deathDrops) {
      if (player != null) {
         GUIManager.PlayerGUIState state = this.states.get(player.getUniqueId());
         if (state != null && state.gui != null) {
            if (state.gui instanceof DeliverItemsGUI deliverGUI) {
               deliverGUI.returnItems(player, deathDrops);
            } else if (state.gui instanceof ConfirmDeliveryGUI confirmGUI) {
               confirmGUI.returnItems(player, deathDrops);
            }
         }
      }
   }

   public void setState(UUID playerUUID, GUIManager.PlayerGUIState state) {
      this.states.put(playerUUID, state);
   }

   public StorageManager getStorage() {
      return this.storage;
   }

   public OrderManager getOrderManager() {
      return this.orderManager;
   }

   public ChatInputHandler getChatInput() {
      return this.chatInput;
   }

   public static enum GUIType {
      PUBLIC_ORDERS,
      YOUR_ORDERS,
      NEW_ORDER,
      ENCHANT_PICKER,
      ENCHANT_LEVEL,
      DELIVER_ITEMS,
      CONFIRM_DELIVERY,
      COLLECT_STASH,
      ORDER_DETAIL;
   }

   public static class PlayerGUIState {
      public GUIManager.GUIType type;
      public BaseGUI gui;
      public UUID contextOrderId;
      public ItemStack[] deliverySlots;
      public ItemStack selectedItem;
      public Integer pendingAmount;
      public int enchantPickerPage;
      public String materialSearchQuery;
   }
}
