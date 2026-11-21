package com.donutorders.util;

import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;

public final class OrderBroadcast {
   public static final String MESSAGE_PATH = "order-created-broadcast";
   public static final String DEFAULT_MESSAGE = "&e{0} &7created a buy order: &f{1} &7× {2} &7@ &f{3} &7each";
   public static final String CONFIG_PATH = "orders.broadcast-on-create";

   private OrderBroadcast() {
   }

   public static boolean isEnabled(FileConfiguration config) {
      return config == null || config.getBoolean("orders.broadcast-on-create", true);
   }

   public static boolean shouldNotify(UUID recipient, UUID buyer) {
      return recipient != null && buyer != null ? !recipient.equals(buyer) : false;
   }

   public static String formatBody(String buyerName, String item, int amount, String price) {
      return MessageHelper.get("order-created-broadcast", "&e{0} &7created a buy order: &f{1} &7× {2} &7@ &f{3} &7each", buyerName, item, amount, price);
   }
}
