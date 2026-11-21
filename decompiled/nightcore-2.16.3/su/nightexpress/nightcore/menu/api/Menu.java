package su.nightexpress.nightcore.menu.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightCorePlugin;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.click.ClickResult;
import su.nightexpress.nightcore.menu.item.MenuItem;

@Deprecated
public interface Menu {
   void clear();

   default void flush() {
      this.getViewers().forEach(this::flush);
   }

   default void flush(@NonNull MenuViewer viewer) {
      this.flush(viewer.getPlayer());
   }

   void flush(@NonNull Player var1);

   default boolean isViewer(@NonNull Player player) {
      return this.getViewer(player) != null;
   }

   void close();

   void close(@NonNull Player var1);

   boolean close(@NonNull NightCorePlugin var1);

   void runNextTick(@NonNull Runnable var1);

   void onClick(@NonNull MenuViewer var1, @NonNull ClickResult var2, @NonNull InventoryClickEvent var3);

   void onDrag(@NonNull MenuViewer var1, @NonNull InventoryDragEvent var2);

   void onClose(@NonNull MenuViewer var1, @NonNull InventoryCloseEvent var2);

   boolean open(@NonNull MenuViewer var1);

   boolean open(@NonNull Player var1);

   boolean canOpen(@NonNull Player var1);

   boolean isPersistent();

   @NonNull
   Collection<MenuViewer> getViewers();

   @Nullable
   MenuViewer getViewer(@NonNull Player var1);

   @NonNull
   MenuViewer getViewerOrCreate(@NonNull Player var1);

   @NonNull
   List<MenuItem> getItems(@NonNull MenuViewer var1);

   @Nullable
   MenuItem getItem(int var1);

   @Nullable
   MenuItem getItem(@NonNull MenuViewer var1, int var2);

   @NonNull
   MenuItem addItem(@NonNull ItemStack var1, int... var2);

   @NonNull
   MenuItem addWeakItem(@NonNull Player var1, @NonNull ItemStack var2, int... var3);

   @NonNull
   MenuItem addItem(@NonNull MenuItem var1);

   @NonNull
   UUID getId();

   @NonNull
   Set<MenuItem> getItems();

   @NonNull
   MenuOptions getOptions();
}
