package dev.cwhead.GravesX.api.inventory;

import com.ranull.graves.Graves;
import com.ranull.graves.util.InventoryUtil;
import dev.cwhead.GravesX.api.util.UtilAPI;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class InventoryAPI {
   private final Graves plugin;
   private final UtilAPI util;

   public InventoryAPI(Graves plugin, UtilAPI util) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      this.util = Objects.requireNonNull(util, "util");
   }

   public int getInventorySize(int size) {
      return InventoryUtil.getInventorySize(size);
   }

   public void equipArmor(@NotNull Inventory inventory, @NotNull Player player) {
      InventoryUtil.equipArmor(inventory, player);
   }

   public void equipItems(@NotNull Inventory inventory, @NotNull Player player) {
      InventoryUtil.equipItems(inventory, player);
   }

   public String inventoryToString(@NotNull Inventory inventory) {
      return InventoryUtil.inventoryToString(inventory, this.plugin);
   }

   public Inventory stringToInventory(@NotNull InventoryHolder inventoryHolder, @NotNull String string, @NotNull String title) {
      return InventoryUtil.stringToInventory(inventoryHolder, string, title, this.plugin);
   }
}
