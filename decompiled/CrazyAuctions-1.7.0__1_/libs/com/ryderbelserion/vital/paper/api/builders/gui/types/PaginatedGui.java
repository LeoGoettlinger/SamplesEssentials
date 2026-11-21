package libs.com.ryderbelserion.vital.paper.api.builders.gui.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import libs.com.ryderbelserion.vital.paper.api.builders.gui.interfaces.GuiItem;
import libs.com.ryderbelserion.vital.paper.api.builders.gui.interfaces.types.IPaginatedGui;
import libs.com.ryderbelserion.vital.paper.api.builders.gui.objects.components.InteractionComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaginatedGui extends BaseGui implements IPaginatedGui {
   private final List<GuiItem> pageItems = new ArrayList<>();
   private final Map<Integer, GuiItem> currentPage;
   private int pageNumber = 1;
   private int pageSize;

   public PaginatedGui(@NotNull String title, int pageSize, int rows, @NotNull Set<InteractionComponent> components) {
      super(title, rows, components);
      if (pageSize == 0) {
         this.calculatePageSize();
      } else {
         this.pageSize = pageSize;
      }

      int size = rows * 9;
      this.currentPage = new LinkedHashMap<>(size);
   }

   @Override
   public final PaginatedGui setPageSize(int pageSize) {
      this.pageSize = pageSize;
      return this;
   }

   @Override
   public void addItem(@NotNull GuiItem guiItem) {
      this.pageItems.add(guiItem);
   }

   @Override
   public void addItem(@NotNull GuiItem... items) {
      this.pageItems.addAll(Arrays.asList(items));
   }

   @Override
   public void removePageItem(@NotNull GuiItem guiItem) {
      this.pageItems.remove(guiItem);
      this.updatePage();
   }

   @Override
   public void removePageItem(@NotNull ItemStack itemStack) {
      String key = GuiKeys.getUUID(itemStack);
      Optional<GuiItem> guiItem = this.pageItems.stream().filter(it -> {
         String pair = it.getUuid().toString();
         return key.equalsIgnoreCase(pair);
      }).findFirst();
      guiItem.ifPresent(this::removePageItem);
   }

   @Override
   public void updatePageItem(int slot, @NotNull ItemStack itemStack) {
      if (this.currentPage.containsKey(slot)) {
         GuiItem guiItem = this.currentPage.get(slot);
         guiItem.setItemStack(itemStack);
         this.getInventory().setItem(slot, guiItem.getItemStack());
      }
   }

   @Override
   public void updatePageItem(int row, int col, @NotNull ItemStack itemStack) {
      this.updatePageItem(this.getSlotFromRowColumn(row, col), itemStack);
   }

   @Override
   public void updatePageItem(int row, int col, @NotNull GuiItem guiItem) {
      this.updatePageItem(this.getSlotFromRowColumn(row, col), guiItem);
   }

   @Override
   public void updatePageItem(int slot, @NotNull GuiItem guiItem) {
      if (this.currentPage.containsKey(slot)) {
         int index = this.pageItems.indexOf(this.currentPage.get(slot));
         this.currentPage.put(slot, guiItem);
         this.pageItems.set(index, guiItem);
         this.getInventory().setItem(slot, guiItem.getItemStack());
      }
   }

   @Override
   public void open(@NotNull Player player, boolean purge) {
      this.open(player, 1, null);
   }

   @Override
   public void open(@NotNull Player player) {
      this.open(player, 1, null);
   }

   @Override
   public void open(@NotNull Player player, @NotNull Consumer<PaginatedGui> consumer) {
      this.open(player, 1, consumer);
   }

   @Override
   public void open(@NotNull Player player, int openPage, @Nullable Consumer<PaginatedGui> consumer) {
      if (!player.isSleeping()) {
         if (openPage <= this.getMaxPages() || openPage > 0) {
            this.pageNumber = openPage;
         }

         this.getInventory().clear();
         this.currentPage.clear();
         this.populate();
         if (this.pageSize == 0 || this.pageSize == this.getSize()) {
            this.calculatePageSize();
         }

         this.populatePage();
         if (consumer != null) {
            consumer.accept(this);
         }

         player.openInventory(this.getInventory());
      }
   }

   @Override
   public final int getNextPageNumber() {
      return this.pageNumber + 1 > this.getMaxPages() ? this.pageNumber : this.pageNumber + 1;
   }

   @Override
   public final int getPreviousPageNumber() {
      return this.pageNumber - 1 == 0 ? this.pageNumber : this.pageNumber - 1;
   }

   @Override
   public final boolean next() {
      if (this.pageNumber + 1 > this.getMaxPages()) {
         return false;
      } else {
         this.pageNumber++;
         this.updatePage();
         return true;
      }
   }

   @Override
   public final boolean previous() {
      if (this.pageNumber - 1 == 0) {
         return false;
      } else {
         this.pageNumber--;
         this.updatePage();
         return true;
      }
   }

   @Override
   public final GuiItem getPageItem(int slot) {
      return this.currentPage.get(slot);
   }

   @Override
   public void setPageNumber(int pageNumber) {
      this.pageNumber = pageNumber;
   }

   @Override
   public final int getCurrentPageNumber() {
      return this.pageNumber;
   }

   @Override
   public final List<GuiItem> getItemsFromPage(int givenPage) {
      int page = givenPage - 1;
      List<GuiItem> guiPage = new ArrayList<>();
      int max = page * this.pageSize + this.pageSize;
      if (max > this.pageItems.size()) {
         max = this.pageItems.size();
      }

      for (int i = page * this.pageSize; i < max; i++) {
         guiPage.add(this.pageItems.get(i));
      }

      return guiPage;
   }

   @Override
   public final Map<Integer, GuiItem> getCurrentPageItems() {
      return this.currentPage;
   }

   @Override
   public void clearPageContents() {
      for (Entry<Integer, GuiItem> entry : this.currentPage.entrySet()) {
         this.getInventory().setItem(entry.getKey(), null);
      }
   }

   @Override
   public void clearPageItems(boolean update) {
      this.pageItems.clear();
      if (update) {
         this.updatePage();
      }
   }

   @Override
   public void clearPageItems() {
      this.clearPageItems(false);
   }

   @Override
   public final int getMaxPages() {
      return (int)Math.ceil((double)this.pageItems.size() / this.pageSize);
   }

   @Override
   public void populatePage() {
      int slot = 0;
      int inventorySize = this.getInventory().getSize();
      Iterator<GuiItem> iterator = this.getItemsFromPage(this.pageNumber).iterator();

      while (iterator.hasNext() && slot < inventorySize) {
         if (this.getGuiItem(slot) == null && this.getInventory().getItem(slot) == null) {
            GuiItem guiItem = iterator.next();
            this.currentPage.put(slot, guiItem);
            this.getInventory().setItem(slot, guiItem.getItemStack());
            slot++;
         } else {
            slot++;
         }
      }
   }

   @Override
   public void updatePage() {
      this.clearPageContents();
      this.populatePage();
   }

   @Override
   public final void calculatePageSize() {
      int counter = 0;

      for (int slot = 0; slot < this.getSize(); slot++) {
         if (this.getInventory().getItem(slot) == null) {
            counter++;
         }
      }

      this.pageSize = counter;
   }

   @Override
   public void updateInventory(Player player) {
      this.getInventory().clear();
      this.populate();
   }
}
