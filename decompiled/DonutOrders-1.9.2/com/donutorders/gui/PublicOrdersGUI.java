package com.donutorders.gui;

import com.donutorders.manager.GUIManager;
import com.donutorders.model.Order;
import com.donutorders.model.OrderStatus;
import com.donutorders.util.ItemUtils;
import com.donutorders.util.MessageHelper;
import com.donutorders.util.NumberFormatter;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PublicOrdersGUI extends BaseGUI {
   private static final int PAGE_SIZE = 45;
   private static final int SLOT_PREV = 45;
   private static final int SLOT_MY = 48;
   private static final int SLOT_NEXT = 53;
   private final GUIManager guiManager;
   private final List<Order> orders;
   private final int page;
   private final int maxPage;

   public PublicOrdersGUI(GUIManager guiManager, List<Order> orders, int page) {
      super(Bukkit.createInventory(null, 54, MessageHelper.get("gui.public-orders.title", "ᴏʀᴅᴇʀꜱ")));
      this.guiManager = guiManager;
      this.orders = orders;
      this.page = page;
      this.maxPage = Math.max(0, (orders.size() - 1) / 45);
      this.build();
   }

   private void build() {
      int start = this.page * 45;
      int end = Math.min(start + 45, this.orders.size());

      for (int i = start; i < end; i++) {
         Order o = this.orders.get(i);
         this.inventory.setItem(i - start, this.buildOrderItem(o));
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

      this.inventory
         .setItem(
            48,
            ItemUtils.createGuiItem(
               Material.PLAYER_HEAD,
               MessageHelper.get("gui.public-orders.my-orders.name", "&b&lᴍʏ ᴏʀᴅᴇʀꜱ"),
               MessageHelper.getList("gui.public-orders.my-orders.lore")
            )
         );
      this.fillEmpty();
   }

   private ItemStack buildOrderItem(Order order) {
      ItemStack template = order.getItemTemplate();
      String itemName = ItemUtils.describeOrderItem(template);
      return ItemUtils.createGuiItem(
         template.getType(),
         MessageHelper.getNamed("gui.public-orders.order-item.name", "&f{item}", "item", itemName),
         MessageHelper.getList(
            "gui.public-orders.order-item.lore",
            "buyer",
            order.getBuyerName(),
            "remaining",
            NumberFormatter.format(order.getAmountRemaining()),
            "requested",
            NumberFormatter.format(order.getAmountRequested()),
            "price",
            NumberFormatter.formatPrice(order.getPricePerItem()),
            "total",
            NumberFormatter.formatPrice(order.getPricePerItem() * order.getAmountRemaining()),
            "expiry",
            order.getFormattedExpiry()
         )
      );
   }

   @Override
   public void handleClick(Player player, int slot, ItemStack clicked, ClickType type) {
      if (slot == 45 && this.page > 0) {
         this.guiManager.openPublicOrders(player, this.page - 1);
      } else if (slot == 53 && this.page < this.maxPage) {
         this.guiManager.openPublicOrders(player, this.page + 1);
      } else if (slot == 48) {
         this.guiManager.openYourActiveOrders(player, 0);
      } else {
         int orderIndex = this.page * 45 + slot;
         if (slot < 45 && orderIndex < this.orders.size()) {
            Order order = this.orders.get(orderIndex);
            if (order.getBuyerUUID().equals(player.getUniqueId())) {
               MessageHelper.send(player, "delivery-own-order", "&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ꜰᴜʟꜰɪʟʟ ʏᴏᴜʀ ᴏᴡɴ ᴏʀᴅᴇʀ.");
               return;
            }

            if (order.getStatus() != OrderStatus.ACTIVE) {
               return;
            }

            this.guiManager.openDeliverItems(player, order.getOrderId());
         }
      }
   }
}
