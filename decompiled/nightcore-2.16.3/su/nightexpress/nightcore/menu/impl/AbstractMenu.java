package su.nightexpress.nightcore.menu.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightCorePlugin;
import su.nightexpress.nightcore.dialog.Dialog;
import su.nightexpress.nightcore.event.PlayerOpenMenuEvent;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.Menu;
import su.nightexpress.nightcore.menu.click.ClickResult;
import su.nightexpress.nightcore.menu.item.ItemOptions;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.util.Lists;

@Deprecated
public abstract class AbstractMenu<P extends NightCorePlugin> implements Menu {
   public static final Map<UUID, Menu> PLAYER_MENUS = new HashMap<>();
   protected final P plugin;
   protected final UUID id;
   protected final MenuOptions options;
   protected final Map<UUID, MenuViewer> viewers;
   protected final Set<MenuItem> items;

   public static void closeAll() {
      getActiveMenus().forEach(Menu::close);
   }

   public static void closeAll(@NonNull NightCorePlugin plugin) {
      getActiveMenus().forEach(menu -> menu.close(plugin));
   }

   public static void clearAll(@NonNull NightCorePlugin plugin) {
      getActiveMenus().stream().distinct().forEach(Menu::clear);
   }

   public static Collection<Menu> getActiveMenus() {
      return new HashSet<>(PLAYER_MENUS.values());
   }

   public static void purge(@NonNull Player player) {
      Menu menu = getMenu(player);
      if (menu != null) {
         menu.close(player);
      }
   }

   public AbstractMenu(@NonNull P plugin) {
      this(plugin, "NC Inventory", InventoryType.CHEST);
   }

   public AbstractMenu(@NonNull P plugin, @NonNull String title, @NonNull InventoryType type) {
      this(plugin, new MenuOptions(title, type));
   }

   @Deprecated
   public AbstractMenu(@NonNull P plugin, @NonNull String title, int size) {
      this(plugin, new MenuOptions(title, size, InventoryType.CHEST));
   }

   public AbstractMenu(@NonNull P plugin, @NonNull String title, @NonNull MenuSize size) {
      this(plugin, new MenuOptions(title, size));
   }

   public AbstractMenu(@NonNull P plugin, @NonNull MenuOptions options) {
      this.plugin = plugin;
      this.id = UUID.randomUUID();
      this.options = new MenuOptions(options);
      this.viewers = new HashMap<>();
      this.items = new HashSet<>();
   }

   @Nullable
   public static Menu getMenu(@NonNull Player player) {
      return PLAYER_MENUS.get(player.getUniqueId());
   }

   @Override
   public void clear() {
      this.close();
      this.getItems().clear();
      this.getViewers().clear();
   }

   @Override
   public void close() {
      new HashSet<>(this.getViewers()).forEach(viewer -> this.close(viewer.getPlayer()));
      this.viewers.clear();
   }

   @Override
   public void close(@NonNull Player player) {
      Menu current = getMenu(player);
      if (current == this && player.getOpenInventory().getType() != InventoryType.CRAFTING && player.getOpenInventory().getType() != InventoryType.CREATIVE) {
         player.closeInventory();
      } else {
         this.onClose(player);
      }
   }

   @Override
   public boolean close(@NonNull NightCorePlugin plugin) {
      if (this.plugin == plugin) {
         this.close();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void runNextTick(@NonNull Runnable runnable) {
      this.plugin.runTask(runnable);
   }

   @Override
   public void flush(@NonNull Player player) {
      this.open(player);
   }

   @Override
   public boolean canOpen(@NonNull Player player) {
      return !player.isSleeping();
   }

   @Override
   public boolean open(@NonNull MenuViewer viewer) {
      return this.open(viewer.getPlayer());
   }

   @Override
   public boolean open(@NonNull Player player) {
      if (!this.canOpen(player)) {
         this.purgeViewer(player);
         return false;
      } else {
         PlayerOpenMenuEvent event = new PlayerOpenMenuEvent(player, this);
         this.plugin.getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            this.purgeViewer(player);
            return false;
         } else {
            MenuOptions options = new MenuOptions(this.getOptions());
            MenuViewer viewer = this.getViewerOrCreate(player);
            this.getItems().removeIf(menuItem -> menuItem.getOptions().canBeDestroyed(viewer));
            this.onPrepare(viewer, options);
            if (!viewer.hasInventory()) {
               viewer.openInventory(options.createInventory());
            } else {
               viewer.flushInventory(options);
            }

            Inventory inventory = viewer.getInventory();
            if (inventory == null) {
               this.plugin.debug("Could not create " + this.getClass().getSimpleName() + " menu for '" + player.getName() + "'.");
               this.purgeViewer(player);
               return false;
            } else if (inventory.getType() == InventoryType.CRAFTING) {
               this.plugin.warn("Got CRAFTING inventory when trying to open " + this.getClass().getSimpleName() + " menu for '" + player.getName() + "'.");
               this.purgeViewer(player);
               return false;
            } else {
               this.getItems(viewer).forEach(menuItem -> {
                  ItemStack item = menuItem.getItemStack();
                  menuItem.getOptions().modifyDisplay(viewer, item);

                  for (int slot : menuItem.getSlots()) {
                     if (slot >= 0 && slot < inventory.getSize()) {
                        inventory.setItem(slot, item);
                     }
                  }
               });
               this.onReady(viewer, inventory);
               PLAYER_MENUS.put(player.getUniqueId(), this);
               return true;
            }
         }
      }
   }

   protected abstract void onPrepare(@NonNull MenuViewer var1, @NonNull MenuOptions var2);

   protected abstract void onReady(@NonNull MenuViewer var1, @NonNull Inventory var2);

   @Override
   public void onClick(@NonNull MenuViewer viewer, @NonNull ClickResult result, @NonNull InventoryClickEvent event) {
      event.setCancelled(true);
      if (!result.isInventory()) {
         MenuItem menuItem = this.getItem(viewer, result.getSlot());
         if (menuItem != null) {
            menuItem.getHandler().getClickActions().forEach(action -> action.onClick(viewer, event));
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
      this.onClose(viewer.getPlayer());
   }

   public void onClose(@NonNull Player player) {
      MenuViewer viewer = this.purgeViewer(player);
      if (viewer != null && this instanceof Linked<?> linked && !linked.isLinkPersistent() && !Dialog.contains(player)) {
         linked.getLink().clear(viewer);
      }

      PLAYER_MENUS.remove(player.getUniqueId());
      if (this.getViewers().isEmpty() && !this.isPersistent()) {
         this.clear();
      }
   }

   @Nullable
   private MenuViewer purgeViewer(@NonNull Player player) {
      MenuViewer viewer = this.viewers.remove(player.getUniqueId());
      if (viewer != null) {
         this.getItems().removeIf(menuItem -> menuItem.getOptions().canBeDestroyed(viewer));
      }

      return viewer;
   }

   @Override
   public boolean isPersistent() {
      return true;
   }

   @NonNull
   @Override
   public Collection<MenuViewer> getViewers() {
      return this.getViewersMap().values();
   }

   @Nullable
   @Override
   public MenuViewer getViewer(@NonNull Player player) {
      return this.getViewersMap().get(player.getUniqueId());
   }

   @NonNull
   @Override
   public MenuViewer getViewerOrCreate(@NonNull Player player) {
      return this.getViewersMap().computeIfAbsent(player.getUniqueId(), k -> new MenuViewer(player));
   }

   @NonNull
   @Override
   public List<MenuItem> getItems(@NonNull MenuViewer viewer) {
      return this.getItems().stream().filter(menuItem -> menuItem.canSee(viewer)).sorted(Comparator.comparingInt(MenuItem::getPriority)).toList();
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

   @NonNull
   @Override
   public MenuItem addItem(@NonNull ItemStack item, int... slots) {
      return this.addItem(new MenuItem(item, slots));
   }

   @NonNull
   @Override
   public MenuItem addWeakItem(@NonNull Player player, @NonNull ItemStack item, int... slots) {
      MenuItem menuItem = new MenuItem(item, slots);
      menuItem.setOptions(ItemOptions.personalWeak(player));
      return this.addItem(menuItem);
   }

   @NonNull
   @Override
   public MenuItem addItem(@NonNull MenuItem menuItem) {
      this.getItems().add(menuItem);
      return menuItem;
   }

   @NonNull
   @Override
   public UUID getId() {
      return this.id;
   }

   @NonNull
   private Map<UUID, MenuViewer> getViewersMap() {
      return this.viewers;
   }

   @NonNull
   @Override
   public Set<MenuItem> getItems() {
      return this.items;
   }

   @NonNull
   @Override
   public MenuOptions getOptions() {
      return this.options;
   }
}
