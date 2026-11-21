package com.donutorders.gui;

import com.donutorders.manager.GUIManager;
import com.donutorders.model.Order;
import com.donutorders.model.OrderStatus;
import com.donutorders.util.ItemUtils;
import com.donutorders.util.MessageHelper;
import com.donutorders.util.NumberFormatter;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class YourOrdersGUI extends BaseGUI {
   private static final int PAGE_SIZE = 45;
   private static final int SLOT_PREV = 45;
   private static final int SLOT_BROWSE = 48;
   private static final int SLOT_NEW = 49;
   private static final int SLOT_NEXT = 53;
   private final GUIManager guiManager;
   private final List<Order> orders;
   private final int page;
   private final int maxPage;
   private final YourOrdersGUI.ViewMode viewMode;
   private final String adminTargetName;
   private final UUID adminTargetUuid;

   public YourOrdersGUI(GUIManager guiManager, List<Order> orders, int page, YourOrdersGUI.ViewMode viewMode, String adminTargetName) {
      this(guiManager, orders, page, viewMode, null, adminTargetName);
   }

   public YourOrdersGUI(GUIManager guiManager, List<Order> orders, int page, YourOrdersGUI.ViewMode viewMode, UUID adminTargetUuid, String adminTargetName) {
      super(Bukkit.createInventory(null, 54, titleFor(viewMode, adminTargetName)));
      this.guiManager = guiManager;
      this.orders = orders;
      this.page = page;
      this.maxPage = orders.isEmpty() ? 0 : Math.max(0, (orders.size() - 1) / 45);
      this.viewMode = viewMode;
      this.adminTargetName = adminTargetName;
      this.adminTargetUuid = adminTargetUuid;
      this.build();
   }

   private static String titleFor(YourOrdersGUI.ViewMode viewMode, String adminTargetName) {
      return switch (viewMode) {
         case ACTIVE -> MessageHelper.get("gui.your-orders.title", "ʏᴏᴜʀ ᴏʀᴅᴇʀꜱ");
         case HISTORY -> MessageHelper.get("gui.order-history.title", "ᴏʀᴅᴇʀ ʜɪꜱᴛᴏʀʏ");
         case ADMIN_HISTORY -> MessageHelper.getNamed(
            "gui.order-history.admin-title", "ᴏʀᴅᴇʀ ʜɪꜱᴛᴏʀʏ: {player}", "player", adminTargetName != null ? adminTargetName : "?"
         );
      };
   }

   private void build() {
      int start = this.page * 45;
      int end = Math.min(start + 45, this.orders.size());

      for (int i = start; i < end; i++) {
         this.inventory.setItem(i - start, this.buildOrderItem(this.orders.get(i)));
      }

      if (this.page > 0) {
         this.inventory
            .setItem(
               45,
               ItemUtils.createGuiItem(
                  Material.ARROW,
                  MessageHelper.get("buttons.previous.name", "&7« ᴘʀᴇᴠɪᴏᴜꜱ"),
                  MessageHelper.getList("buttons.previous.lore", "page", String.valueOf(this.page), "max_page", String.valueOf(this.maxPage + 1))
               )
            );
      }

      if (this.page < this.maxPage) {
         this.inventory
            .setItem(
               53,
               ItemUtils.createGuiItem(
                  Material.ARROW,
                  MessageHelper.get("buttons.next.name", "&7ɴᴇxᴛ »"),
                  MessageHelper.getList("buttons.next.lore", "page", String.valueOf(this.page + 2), "max_page", String.valueOf(this.maxPage + 1))
               )
            );
      }

      if (this.viewMode == YourOrdersGUI.ViewMode.ACTIVE) {
         this.inventory
            .setItem(
               49,
               ItemUtils.createGuiItem(
                  Material.LIME_STAINED_GLASS_PANE,
                  MessageHelper.get("gui.your-orders.new-order.name", "&a&lɴᴇᴡ ᴏʀᴅᴇʀ"),
                  MessageHelper.getList("gui.your-orders.new-order.lore")
               )
            );
         this.inventory
            .setItem(
               48,
               ItemUtils.createGuiItem(
                  Material.COMPASS,
                  MessageHelper.get("gui.your-orders.browse-market.name", "&b&lʙʀᴏᴡꜱᴇ ᴍᴀʀᴋᴇᴛ"),
                  MessageHelper.getList("gui.your-orders.browse-market.lore")
               )
            );
      } else if (this.viewMode == YourOrdersGUI.ViewMode.HISTORY) {
         this.inventory
            .setItem(
               49,
               ItemUtils.createGuiItem(
                  Material.BOOK,
                  MessageHelper.get("gui.order-history.active-orders.name", "&e&lᴀᴄᴛɪᴠᴇ ᴏʀᴅᴇʀꜱ"),
                  MessageHelper.getList("gui.order-history.active-orders.lore")
               )
            );
         this.inventory
            .setItem(
               48,
               ItemUtils.createGuiItem(
                  Material.COMPASS,
                  MessageHelper.get("gui.your-orders.browse-market.name", "&b&lʙʀᴏᴡꜱᴇ ᴍᴀʀᴋᴇᴛ"),
                  MessageHelper.getList("gui.your-orders.browse-market.lore")
               )
            );
      }

      this.fillEmpty();
   }

   private ItemStack buildOrderItem(Order order) {
      Material mat = order.getItemTemplate().getType();
      String statusColor = MessageHelper.statusColor(order.getStatus());
      String statusName = MessageHelper.statusName(order.getStatus());
      boolean hasStash = order.getStatus() == OrderStatus.PENDING
         || order.getAmountFulfilled() > 0
         || order.getStatus() == OrderStatus.EXPIRED
         || order.getStatus() == OrderStatus.CANCELLED;
      String stashLine = hasStash
         ? MessageHelper.get("gui.your-orders.stash-has-items", "&e⬛ ꜱᴛᴀꜱʜ ʜᴀꜱ ɪᴛᴇᴍꜱ")
         : MessageHelper.get("gui.your-orders.stash-empty", "&7ꜱᴛᴀꜱʜ ɪꜱ ᴇᴍᴘᴛʏ");
      return ItemUtils.createGuiItem(
         mat,
         MessageHelper.getNamed(
            "gui.your-orders.order-item.name",
            "{status_color}{item}",
            "status_color",
            statusColor,
            "item",
            ItemUtils.describeOrderItem(order.getItemTemplate())
         ),
         MessageHelper.getList(
            "gui.your-orders.order-item.lore",
            "status_color",
            statusColor,
            "status",
            statusName,
            "fulfilled",
            NumberFormatter.format(order.getAmountFulfilled()),
            "requested",
            NumberFormatter.format(order.getAmountRequested()),
            "price",
            NumberFormatter.formatPrice(order.getPricePerItem()),
            "expiry",
            order.getFormattedExpiry(),
            "stash_line",
            stashLine
         )
      );
   }

   @Override
   public void handleClick(Player player, int slot, ItemStack clicked, ClickType type) {
      if (this.viewMode == YourOrdersGUI.ViewMode.ACTIVE) {
         if (slot == 49) {
            this.guiManager.openNewOrderPicker(player);
            return;
         }

         if (slot == 48) {
            this.guiManager.openPublicOrders(player, 0);
            return;
         }
      } else if (this.viewMode == YourOrdersGUI.ViewMode.HISTORY) {
         if (slot == 49) {
            this.guiManager.openYourActiveOrders(player, 0);
            return;
         }

         if (slot == 48) {
            this.guiManager.openPublicOrders(player, 0);
            return;
         }
      }

      if (slot == 45 && this.page > 0) {
         this.reopen(player, this.page - 1);
      } else if (slot == 53 && this.page < this.maxPage) {
         this.reopen(player, this.page + 1);
      } else if (this.viewMode != YourOrdersGUI.ViewMode.ADMIN_HISTORY) {
         int orderIndex = this.page * 45 + slot;
         if (slot < 45 && orderIndex < this.orders.size()) {
            this.guiManager.openOrderDetail(player, this.orders.get(orderIndex).getOrderId());
         }
      }
   }

   private void reopen(Player player, int newPage) {
      switch (this.viewMode) {
         case ACTIVE:
            this.guiManager.openYourActiveOrders(player, newPage);
            break;
         case HISTORY:
            this.guiManager.openYourOrderHistory(player, newPage);
            break;
         case ADMIN_HISTORY:
            if (this.adminTargetUuid != null) {
               this.guiManager.openAdminOrderHistory(player, this.adminTargetUuid, this.adminTargetName, newPage);
            }
      }
   }

   public static enum ViewMode {
      ACTIVE,
      HISTORY,
      ADMIN_HISTORY;
   }
}
