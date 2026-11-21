package su.nightexpress.nightcore.ui.menu.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.bukkit.Material;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.item.ItemClick;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.util.bukkit.NightItem;

@Deprecated
public class MenuFiller<I> {
   private final int[] slots;
   private final Collection<I> items;
   private final Function<I, NightItem> itemCreator;
   private final Function<I, ItemClick> itemClick;

   public MenuFiller(int[] slots, @NonNull Collection<I> items, @NonNull Function<I, NightItem> itemCreator, @NonNull Function<I, ItemClick> itemClick) {
      this.slots = slots;
      this.items = items;
      this.itemCreator = itemCreator;
      this.itemClick = itemClick;
   }

   @NonNull
   public static <I> MenuFiller.Builder<I> builder(@NonNull Filled<I> menu) {
      return new MenuFiller.Builder<>();
   }

   public void addItems(@NonNull MenuViewer viewer) {
      int limit = this.slots.length;
      int pages = (int)Math.ceil((double)this.items.size() / limit);
      viewer.setPages(pages);
      viewer.setPage(Math.min(viewer.getPage(), viewer.getPages()));
      int skip = (viewer.getPage() - 1) * limit;
      List<I> list = this.items.stream().skip(skip).limit(limit).toList();
      int count = 0;

      for (I object : list) {
         NightItem item = this.itemCreator.apply(object);
         MenuItem menuItem = MenuItem.builder(item).setPriority(100).setSlots(this.slots[count++]).setHandler(this.itemClick.apply(object)).build();
         viewer.addItem(menuItem);
      }
   }

   public static class Builder<I> {
      private int[] slots = new int[0];
      private Collection<I> items = Collections.emptyList();
      private Function<I, NightItem> itemCreator = obj -> new NightItem(Material.AIR);
      private Function<I, ItemClick> itemClick = obj -> (viewer, event) -> {};

      public MenuFiller<I> build() {
         return new MenuFiller<>(this.slots, this.items, this.itemCreator, this.itemClick);
      }

      public MenuFiller.Builder<I> setSlots(int... slots) {
         this.slots = slots;
         return this;
      }

      public MenuFiller.Builder<I> setItems(@NonNull Collection<I> items) {
         this.items = items;
         return this;
      }

      public MenuFiller.Builder<I> setItemCreator(@NonNull Function<I, NightItem> itemCreator) {
         this.itemCreator = itemCreator;
         return this;
      }

      public MenuFiller.Builder<I> setItemClick(@NonNull Function<I, ItemClick> itemClick) {
         this.itemClick = itemClick;
         return this;
      }
   }
}
