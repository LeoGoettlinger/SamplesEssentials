package su.nightexpress.nightcore.ui.menu.type;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.event.MenuOpenEvent;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.dialog.DialogManager;
import su.nightexpress.nightcore.ui.menu.Menu;
import su.nightexpress.nightcore.ui.menu.MenuRegistry;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.click.ClickResult;
import su.nightexpress.nightcore.ui.menu.data.Linked;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Placeholders;
import su.nightexpress.nightcore.util.bridge.Software;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.text.night.NightMessage;

@Deprecated
public abstract class AbstractMenu<P extends NightPlugin> implements Menu {
   protected final P plugin;
   protected final Set<MenuItem> items;
   protected MenuType menuType;
   protected String title;
   protected boolean persistent;
   protected int autoRefreshInterval;
   protected long autoRefreshIn;
   protected boolean applyPlaceholderAPI;

   public AbstractMenu(@NonNull P plugin, @NonNull MenuType menuType, @NonNull String title) {
      this.plugin = plugin;
      this.items = new HashSet<>();
      this.setMenuType(menuType);
      this.setTitle(title);
      this.setPersistent(true);
      this.setApplyPlaceholderAPI(false);
      this.setAutoRefreshInterval(-1);
      this.setAutoRefreshIn(-1L);
   }

   @Override
   public void clear() {
      this.close();
      this.items.clear();
   }

   @Override
   public void close() {
      MenuRegistry.getViewers(this).forEach(this::closeFully);
   }

   @Override
   public void close(@NonNull Player player) {
      MenuViewer viewer = this.getViewer(player);
      if (viewer != null) {
         this.closeFully(viewer);
      }
   }

   protected void closeFully(@NonNull MenuViewer viewer) {
      viewer.getPlayer().closeInventory();
      this.onClose(viewer);
   }

   @Override
   public void tick() {
      if (this.autoRefreshInterval > 0) {
         if (this.isReadyToRefresh()) {
            this.flush();
            this.setAutoRefreshIn(this.autoRefreshInterval);
         }

         if (this.autoRefreshIn > 0L) {
            this.autoRefreshIn--;
         }
      }
   }

   @Override
   public void runNextTick(@NonNull Runnable runnable) {
      this.plugin.runTask(runnable);
   }

   @Override
   public void flush() {
      this.getViewers().forEach(this::flush);
   }

   @Override
   public void flush(@NonNull MenuViewer viewer) {
      this.flush(viewer.getPlayer());
   }

   @Override
   public void flush(@NonNull Player player) {
      this.flush(player, viewer -> {});
   }

   @Override
   public void flush(@NonNull Player player, @NonNull Consumer<MenuViewer> consumer) {
      this.open(player, consumer);
   }

   @Override
   public boolean canOpen(@NonNull Player player) {
      return !player.isSleeping();
   }

   protected boolean open(@NonNull Player player, @NonNull Consumer<MenuViewer> onViewSet) {
      if (!this.canOpen(player)) {
         this.close(player);
         return false;
      } else {
         MenuOpenEvent event = new MenuOpenEvent(player, this);
         this.plugin.getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            this.close(player);
            return false;
         } else {
            this.plugin.runTask(player.getLocation(), () -> {
               MenuViewer viewer = this.getViewerOrCreate(player);
               viewer.removeItems();
               onViewSet.accept(viewer);
               InventoryView view = viewer.getView();
               if (view != null && !viewer.isRebuildMenu()) {
                  view.getTopInventory().clear();
               } else {
                  if (view != null && this instanceof Linked<?> linked) {
                     linked.getCache().addAnchor(player);
                  }

                  String title = this.getTitle(viewer);
                  if (this.isApplyPlaceholderAPI()) {
                     title = Placeholders.forPlayerWithPAPI(player).apply(title);
                  }

                  view = Software.get().createView(this.menuType, NightMessage.parse(title), player);
                  viewer.assignInventory(view);
                  player.openInventory(view);
               }

               this.onPrepare(viewer, view);
               Inventory inventory = view.getTopInventory();
               this.getItems(viewer).forEach(menuItem -> {
                  NightItem item = menuItem.getItem().copy();
                  ItemHandler handler = menuItem.getHandler();
                  if (this.isApplyPlaceholderAPI()) {
                     item.replacement(replacer -> replacer.replacePlaceholderAPI(player));
                  }

                  if (handler != null && handler.getOptions() != null) {
                     handler.getOptions().modifyDisplay(viewer, item);
                  }

                  this.onItemPrepare(viewer, menuItem, item);
                  ItemStack itemStack = item.getItemStack();

                  for (int slot : menuItem.getSlots()) {
                     if (slot >= 0 && slot < inventory.getSize()) {
                        inventory.setItem(slot, itemStack);
                     }
                  }
               });
               this.onReady(viewer, inventory);
               MenuRegistry.assign(viewer);
            });
            return true;
         }
      }
   }

   protected abstract void onPrepare(@NonNull MenuViewer var1, @NonNull InventoryView var2);

   protected abstract void onReady(@NonNull MenuViewer var1, @NonNull Inventory var2);

   protected void onItemPrepare(@NonNull MenuViewer viewer, @NonNull MenuItem menuItem, @NonNull NightItem item) {
   }

   @Override
   public void onClick(@NonNull MenuViewer viewer, @NonNull ClickResult result, @NonNull InventoryClickEvent event) {
      event.setCancelled(true);
      if (!result.isInventory()) {
         MenuItem menuItem = this.getItem(viewer, result.getSlot());
         if (menuItem != null) {
            menuItem.click(viewer, event);
            viewer.setLastClickTime(System.currentTimeMillis());
         }
      }
   }

   @Override
   public void onDrag(@NonNull MenuViewer viewer, @NonNull InventoryDragEvent event) {
      event.setCancelled(true);
   }

   @Override
   public void onClose(@NonNull MenuViewer viewer, @NonNull InventoryCloseEvent event) {
      this.onClose(viewer);
   }

   protected void onClose(@NonNull MenuViewer viewer) {
      viewer.removeItems();
      MenuRegistry.terminate(viewer.getPlayer());
      if (this.getViewers().isEmpty() && !this.isPersistent()) {
         this.clear();
      }
   }

   @NonNull
   @Override
   public Set<MenuViewer> getViewers() {
      return MenuRegistry.getViewers(this);
   }

   @Override
   public boolean isViewer(@NonNull Player player) {
      return this.getViewer(player) != null;
   }

   @Nullable
   @Override
   public MenuViewer getViewer(@NonNull Player player) {
      MenuViewer viewer = MenuRegistry.getViewer(player);
      return viewer != null && viewer.isMenu(this) ? viewer : null;
   }

   @NonNull
   private MenuViewer getViewerOrCreate(@NonNull Player player) {
      MenuViewer viewer = this.getViewer(player);
      return viewer == null ? new MenuViewer(this, player) : viewer;
   }

   @NonNull
   @Override
   public List<MenuItem> getItems(@NonNull MenuViewer viewer) {
      Set<MenuItem> items = new HashSet<>(viewer.getItems());
      items.addAll(this.getItems());
      return items.stream().filter(menuItem -> menuItem.canSee(viewer)).sorted(Comparator.comparingInt(MenuItem::getPriority)).toList();
   }

   @Nullable
   @Override
   public MenuItem getItem(int slot) {
      return this.getItems().stream().filter(item -> Lists.contains(item.getSlots(), slot)).max(Comparator.comparingInt(MenuItem::getPriority)).orElse(null);
   }

   @Nullable
   @Override
   public MenuItem getItem(@NonNull MenuViewer viewer, int slot) {
      return this.getItems(viewer)
         .stream()
         .filter(menuItem -> Lists.contains(menuItem.getSlots(), slot))
         .max(Comparator.comparingInt(MenuItem::getPriority))
         .orElse(null);
   }

   @Override
   public void addItem(@NonNull MenuViewer viewer, @NonNull MenuItem.Builder builder) {
      this.addItem(viewer, builder.build());
   }

   @Override
   public void addItem(@NonNull MenuViewer viewer, @NonNull MenuItem menuItem) {
      viewer.addItem(menuItem);
   }

   @Override
   public void addItem(@NonNull MenuItem.Builder builder) {
      this.addItem(builder.build());
   }

   @Override
   public void addItem(@NonNull MenuItem menuItem) {
      this.items.add(menuItem);
   }

   @Deprecated
   @Override
   public void handleInput(@NonNull Dialog.Builder builder) {
      Dialog dialog = builder.build();
      DialogManager.startDialog(dialog);
      Player player = dialog.getPlayer();
      this.runNextTick(player::closeInventory);
   }

   @NonNull
   @Override
   public Set<MenuItem> getItems() {
      return this.items;
   }

   @NonNull
   protected String getTitle(@NonNull MenuViewer viewer) {
      return this.title;
   }

   @NonNull
   @Override
   public MenuType getMenuType() {
      return this.menuType;
   }

   @Override
   public void setMenuType(@NonNull MenuType menuType) {
      this.menuType = menuType;
   }

   @NonNull
   @Override
   public String getTitle() {
      return this.title;
   }

   @Override
   public void setTitle(@NonNull String title) {
      this.title = title;
   }

   @Override
   public boolean isPersistent() {
      return this.persistent;
   }

   public void setPersistent(boolean persistent) {
      this.persistent = persistent;
   }

   @Override
   public int getAutoRefreshInterval() {
      return this.autoRefreshInterval;
   }

   @Override
   public void setAutoRefreshInterval(int autoRefreshInterval) {
      this.autoRefreshInterval = autoRefreshInterval;
      this.setAutoRefreshIn(autoRefreshInterval);
   }

   @Override
   public long getAutoRefreshIn() {
      return this.autoRefreshIn;
   }

   @Override
   public void setAutoRefreshIn(long autoRefreshIn) {
      this.autoRefreshIn = autoRefreshIn;
   }

   @Override
   public boolean isReadyToRefresh() {
      return this.autoRefreshIn == 0L;
   }

   @Override
   public boolean isApplyPlaceholderAPI() {
      return this.applyPlaceholderAPI;
   }

   @Override
   public void setApplyPlaceholderAPI(boolean applyPlaceholderAPI) {
      this.applyPlaceholderAPI = applyPlaceholderAPI;
   }
}
