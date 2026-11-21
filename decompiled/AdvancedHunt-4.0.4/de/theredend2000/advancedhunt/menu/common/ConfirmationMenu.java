package de.theredend2000.advancedhunt.menu.common;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ConfirmationMenu extends Menu {
   private final String title;
   private final Consumer<InventoryClickEvent> onConfirm;
   private final Consumer<InventoryClickEvent> onCancel;

   public ConfirmationMenu(Player player, Main plugin, String title, Consumer<InventoryClickEvent> onConfirm, Consumer<InventoryClickEvent> onCancel) {
      super(player, plugin);
      this.title = title;
      this.onConfirm = onConfirm;
      this.onCancel = onCancel;
   }

   @Override
   public String getMenuName() {
      return this.title;
   }

   @Override
   public int getSlots() {
      return 27;
   }

   @Override
   public void handleMenu(InventoryClickEvent e) {
   }

   @Override
   public void addCloseOrBack() {
   }

   @Override
   public void setMenuItems() {
      this.fillBorders(super.FILLER_GLASS);
      this.addButton(
         11, new ItemBuilder(XMaterial.LIME_TERRACOTTA).setDisplayName(this.plugin.getMessageManager().getMessage("gui.common.confirm")).build(), e -> {
            if (this.onConfirm != null) {
               this.onConfirm.accept(e);
            }
         }
      );
      this.addButton(
         15, new ItemBuilder(XMaterial.RED_TERRACOTTA).setDisplayName(this.plugin.getMessageManager().getMessage("gui.common.cancel")).build(), e -> {
            if (this.onCancel != null) {
               this.onCancel.accept(e);
            }
         }
      );
   }
}
