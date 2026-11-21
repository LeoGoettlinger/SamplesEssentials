package de.theredend2000.advancedhunt.menu;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class PagedMenu extends Menu {
   protected int page = 0;
   protected int maxItemsPerPage = 28;
   protected int index = 0;
   protected boolean hasNextPage = false;

   public PagedMenu(Player playerMenuUtility, Main plugin) {
      super(playerMenuUtility, plugin);
   }

   public void addMenuBorder() {
      for (int i = 0; i < 10; i++) {
         if (this.inventory.getItem(i) == null) {
            this.addStaticItem(i, super.FILLER_GLASS);
         }
      }

      this.addStaticItem(17, super.FILLER_GLASS);
      this.addStaticItem(18, super.FILLER_GLASS);
      this.addStaticItem(26, super.FILLER_GLASS);
      this.addStaticItem(27, super.FILLER_GLASS);
      this.addStaticItem(35, super.FILLER_GLASS);
      this.addStaticItem(36, super.FILLER_GLASS);

      for (int ix = 44; ix < 54; ix++) {
         if (this.inventory.getItem(ix) == null) {
            this.addStaticItem(ix, super.FILLER_GLASS);
         }
      }
   }

   public void addPagedButtons(int size) {
      this.addButton(
         48,
         new ItemBuilder(XMaterial.PLAYER_HEAD)
            .setSkullTexture(
               "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDU5YmUxNTU3MjAxYzdmZjFhMGIzNjk2ZDE5ZWFiNDEwNDg4MGQ2YTljZGI0ZDVmYTIxYjZkYWE5ZGIyZDEifX19"
            )
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.common.previous_page") + this.getPageIndicator(size))
            .build(),
         e -> {
            if (this.page == 0) {
               e.getWhoClicked().sendMessage(this.plugin.getMessageManager().getMessage("feedback.gui.first_page"));
            } else {
               this.page--;
               this.refresh();
            }
         }
      );
      this.addButton(
         50,
         new ItemBuilder(XMaterial.PLAYER_HEAD)
            .setSkullTexture(
               "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDJiMGMwN2ZhMGU4OTIzN2Q2NzllMTMxMTZiNWFhNzVhZWJiMzRlOWM5NjhjNmJhZGIyNTFlMTI3YmRkNWIxIn19fQ=="
            )
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.common.next_page") + this.getPageIndicator(size))
            .build(),
         e -> {
            if (this.hasNextPage) {
               this.page++;
               this.refresh();
            } else {
               e.getWhoClicked().sendMessage(this.plugin.getMessageManager().getMessage("feedback.gui.last_page"));
            }
         }
      );
   }

   public int getMaxItemsPerPage() {
      return this.maxItemsPerPage;
   }

   protected int getSlotForPagedIndex(int displayIndex) {
      int row = displayIndex / 7;
      int col = displayIndex % 7;
      return 10 + row * 9 + col;
   }

   public void addPagedItem(int index, ItemStack item, Consumer<InventoryClickEvent> action) {
      int slot = this.getSlotForPagedIndex(index);
      this.addButton(slot, item, action);
   }

   public String getPageIndicator(int size) {
      String title = "";
      if (this.getTotalPages(size) > 1) {
         title = title
            + " "
            + this.plugin
               .getMessageManager()
               .getMessage("gui.common.page_indicator", false, "%page%", String.valueOf(this.page + 1), "%total%", String.valueOf(this.getTotalPages(size)));
      }

      return title;
   }

   public int getTotalPages(int size) {
      return size <= this.maxItemsPerPage ? 1 : (int)Math.ceil((double)size / this.maxItemsPerPage);
   }
}
