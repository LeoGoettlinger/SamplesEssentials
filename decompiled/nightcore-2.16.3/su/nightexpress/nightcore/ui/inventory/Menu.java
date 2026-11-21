package su.nightexpress.nightcore.ui.inventory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.inventory.item.MenuItem;
import su.nightexpress.nightcore.ui.inventory.viewer.MenuViewer;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;

public interface Menu {
   void onPrepare(@NonNull ViewerContext var1, @NonNull InventoryView var2, @NonNull Inventory var3, @NonNull List<MenuItem> var4);

   void onReady(@NonNull ViewerContext var1, @NonNull InventoryView var2, @NonNull Inventory var3);

   void onRender(@NonNull ViewerContext var1, @NonNull InventoryView var2, @NonNull Inventory var3);

   void load();

   void load(@NonNull Path var1);

   void load(@NonNull FileConfig var1);

   void tick();

   void refresh();

   void refresh(@NonNull Player var1);

   void close();

   void close(@NonNull Player var1);

   void close(@NonNull UUID var1);

   void handleClick(@NonNull Player var1, @NonNull InventoryClickEvent var2);

   void handleDrag(@NonNull Player var1, @NonNull InventoryDragEvent var2);

   void handleClose(@NonNull Player var1, @NonNull InventoryCloseEvent var2, @NonNull MenuRegistry var3);

   boolean hasViewers();

   boolean isViewer(@NonNull Player var1);

   boolean isViewer(@NonNull UUID var1);

   @NonNull
   NightComponent getTitle(@NonNull ViewerContext var1);

   @NonNull
   MenuType getType(@NonNull MenuViewer var1);

   @Nullable
   MenuViewer getViewer(@NonNull Player var1);

   @Nullable
   MenuViewer getViewer(@NonNull UUID var1);

   @NonNull
   Collection<MenuViewer> getViewers();

   @NonNull
   MenuDataRegistry getDataRegistry();

   @NonNull
   Map<String, MenuItem> getItemsToDisplay();

   @NonNull
   Map<String, MenuItem> getDefaultButtons();

   @NonNull
   Map<String, MenuItem> getConfigButtons();

   @Deprecated
   @NonNull
   Map<String, MenuItem> getDefaultItems();

   @Deprecated
   @NonNull
   Map<String, MenuItem> getConfigItems();

   int getAutoRefreshInterval();

   long getAutoRefreshIn();

   void setAutoRefreshIn(long var1);

   boolean isAutoRefreshTime();

   boolean isPlaceholderIntegrationEnabled();
}
