package su.nightexpress.nightcore.menu.api;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.item.ItemOptions;
import su.nightexpress.nightcore.menu.item.MenuItem;

@Deprecated
public interface AutoFilled<I> extends Menu {
   default boolean open(@NonNull Player player, int page) {
      return this.open(this.getViewerOrCreate(player), page);
   }

   default boolean open(@NonNull MenuViewer viewer, int page) {
      viewer.setPage(page);
      return this.open(viewer);
   }

   void onAutoFill(@NonNull MenuViewer var1, @NonNull AutoFill<I> var2);

   default void autoFill(@NonNull MenuViewer viewer) {
      AutoFill<I> autoFill = new AutoFill<>();
      this.onAutoFill(viewer, autoFill);
      this.getAutoFillItems(viewer, autoFill).forEach(this::addItem);
   }

   @NonNull
   default List<MenuItem> getAutoFillItems(@NonNull MenuViewer viewer, @NonNull AutoFill<I> autoFill) {
      Player player = viewer.getPlayer();
      List<MenuItem> items = new ArrayList<>();
      List<I> origin = autoFill.getItems();
      int[] slots = autoFill.getSlots();
      int limit = slots.length;
      int pages = (int)Math.ceil((double)origin.size() / limit);
      viewer.setPages(pages);
      viewer.setPage(Math.min(viewer.getPage(), viewer.getPages()));
      int skip = (viewer.getPage() - 1) * limit;
      List<I> list = origin.stream().skip(skip).limit(limit).toList();
      int count = 0;

      for (I object : list) {
         ItemStack item = autoFill.getItemCreator().apply(object);
         ItemOptions options = ItemOptions.personalWeak(player);
         MenuItem menuItem = new MenuItem(item).setPriority(100).setOptions(options).setSlots(slots[count++]).addClick(autoFill.getClickAction().apply(object));
         items.add(menuItem);
      }

      return items;
   }
}
