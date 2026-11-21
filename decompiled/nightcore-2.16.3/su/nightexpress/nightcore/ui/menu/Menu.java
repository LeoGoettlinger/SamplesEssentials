package su.nightexpress.nightcore.ui.menu;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.MenuType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.click.ClickResult;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;

@Deprecated
public interface Menu {
   void tick();

   void clear();

   void flush();

   void flush(@NonNull MenuViewer var1);

   void flush(@NonNull Player var1);

   void flush(@NonNull Player var1, @NonNull Consumer<MenuViewer> var2);

   boolean isViewer(@NonNull Player var1);

   void close();

   void close(@NonNull Player var1);

   void runNextTick(@NonNull Runnable var1);

   void onClick(@NonNull MenuViewer var1, @NonNull ClickResult var2, @NonNull InventoryClickEvent var3);

   void onDrag(@NonNull MenuViewer var1, @NonNull InventoryDragEvent var2);

   void onClose(@NonNull MenuViewer var1, @NonNull InventoryCloseEvent var2);

   @Deprecated
   void handleInput(@NonNull Dialog.Builder var1);

   boolean canOpen(@NonNull Player var1);

   boolean isPersistent();

   @NonNull
   Set<MenuViewer> getViewers();

   @Nullable
   MenuViewer getViewer(@NonNull Player var1);

   @NonNull
   List<MenuItem> getItems(@NonNull MenuViewer var1);

   @Nullable
   MenuItem getItem(int var1);

   @Nullable
   MenuItem getItem(@NonNull MenuViewer var1, int var2);

   void addItem(@NonNull MenuViewer var1, @NonNull MenuItem.Builder var2);

   void addItem(@NonNull MenuViewer var1, @NonNull MenuItem var2);

   void addItem(@NonNull MenuItem.Builder var1);

   void addItem(@NonNull MenuItem var1);

   @NonNull
   Set<MenuItem> getItems();

   @NonNull
   MenuType getMenuType();

   void setMenuType(@NonNull MenuType var1);

   @NonNull
   String getTitle();

   void setTitle(@NonNull String var1);

   int getAutoRefreshInterval();

   void setAutoRefreshInterval(int var1);

   boolean isReadyToRefresh();

   long getAutoRefreshIn();

   void setAutoRefreshIn(long var1);

   boolean isApplyPlaceholderAPI();

   void setApplyPlaceholderAPI(boolean var1);
}
