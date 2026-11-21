package libs.com.ryderbelserion.vital.paper.api.builders.gui.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import libs.com.ryderbelserion.vital.common.util.AdvUtil;
import libs.com.ryderbelserion.vital.paper.api.builders.gui.interfaces.GuiAction;
import libs.com.ryderbelserion.vital.paper.api.builders.gui.interfaces.GuiFiller;
import libs.com.ryderbelserion.vital.paper.api.builders.gui.interfaces.GuiItem;
import libs.com.ryderbelserion.vital.paper.api.builders.gui.interfaces.GuiType;
import libs.com.ryderbelserion.vital.paper.api.builders.gui.interfaces.types.IBaseGui;
import libs.com.ryderbelserion.vital.paper.api.builders.gui.listeners.GuiListener;
import libs.com.ryderbelserion.vital.paper.api.builders.gui.objects.components.InteractionComponent;
import libs.com.ryderbelserion.vital.paper.api.catches.GenericException;
import libs.com.ryderbelserion.vital.paper.util.MiscUtil;
import libs.com.ryderbelserion.vital.paper.util.scheduler.FoliaRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseGui implements InventoryHolder, Listener, IBaseGui {
   private static final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(BaseGui.class);
   private final GuiFiller filler = new GuiFiller(this);
   private final Map<Integer, GuiAction<InventoryClickEvent>> slotActions;
   private final Map<Integer, GuiItem> guiItems;
   private final Set<InteractionComponent> interactionComponents;
   private GuiAction<InventoryClickEvent> defaultTopClickAction;
   private GuiAction<InventoryClickEvent> playerInventoryAction;
   private GuiAction<InventoryClickEvent> outsideClickAction;
   private GuiAction<InventoryClickEvent> defaultClickAction;
   private GuiAction<InventoryCloseEvent> closeGuiAction;
   private GuiAction<InventoryOpenEvent> openGuiAction;
   private GuiAction<InventoryDragEvent> dragAction;
   private GuiType guiType = GuiType.CHEST;
   private Inventory inventory;
   private boolean isUpdating;
   private String title;
   private int rows;

   public BaseGui(@NotNull String title, int rows, Set<InteractionComponent> components) {
      this.title = title;
      this.rows = rows;
      int size = this.rows * 9;
      this.slotActions = new LinkedHashMap<>(size);
      this.guiItems = new LinkedHashMap<>(size);
      this.interactionComponents = this.safeCopy(components);
      this.inventory = plugin.getServer().createInventory(this, size, this.title());
   }

   public BaseGui(@NotNull String title, GuiType guiType, Set<InteractionComponent> components) {
      this.title = title;
      this.slotActions = new LinkedHashMap<>(guiType.getLimit());
      this.guiItems = new LinkedHashMap<>(guiType.getLimit());
      this.interactionComponents = this.safeCopy(components);
      this.inventory = plugin.getServer().createInventory(this, guiType.getInventoryType(), this.title());
      this.guiType = guiType;
   }

   @Override
   public final Map<Integer, GuiItem> getGuiItems() {
      return Collections.unmodifiableMap(this.guiItems);
   }

   @NotNull
   @Override
   public final String getTitle() {
      return PlainTextComponentSerializer.plainText().serialize(this.title());
   }

   @Override
   public void setTitle(@NotNull String title) {
      this.title = title;
   }

   @NotNull
   @Override
   public final Component title() {
      return AdvUtil.parse(this.title);
   }

   @Override
   public final int getRows() {
      return this.rows;
   }

   @Override
   public void setRows(int rows) {
      this.rows = rows;
   }

   @Override
   public final int getSize() {
      return this.getRows() * 9;
   }

   @Override
   public final GuiType getGuiType() {
      return this.guiType;
   }

   @Override
   public final GuiFiller getFiller() {
      return this.filler;
   }

   @Override
   public void addInteractionComponent(InteractionComponent... components) {
      this.interactionComponents.addAll(Arrays.asList(components));
   }

   @Override
   public void removeInteractionComponent(InteractionComponent component) {
      this.interactionComponents.remove(component);
   }

   @Override
   public final boolean canPerformOtherActions() {
      return !this.interactionComponents.contains(InteractionComponent.PREVENT_OTHER_ACTIONS);
   }

   @Override
   public final boolean isInteractionsDisabled() {
      return this.interactionComponents.size() == InteractionComponent.VALUES.size();
   }

   @Override
   public final boolean canPlaceItems() {
      return !this.interactionComponents.contains(InteractionComponent.PREVENT_ITEM_PLACE);
   }

   @Override
   public final boolean canTakeItems() {
      return !this.interactionComponents.contains(InteractionComponent.PREVENT_ITEM_TAKE);
   }

   @Override
   public final boolean canSwapItems() {
      return !this.interactionComponents.contains(InteractionComponent.PREVENT_ITEM_SWAP);
   }

   @Override
   public final boolean canDropItems() {
      return !this.interactionComponents.contains(InteractionComponent.PREVENT_ITEM_DROP);
   }

   @Override
   public void giveItem(Player player, ItemStack itemStack) {
      player.getInventory().addItem(new ItemStack[]{GuiKeys.strip(itemStack)});
   }

   @Override
   public void giveItem(Player player, ItemStack... itemStacks) {
      Arrays.asList(itemStacks).forEach(item -> this.giveItem(player, item));
   }

   @Override
   public void setItem(int slot, GuiItem guiItem) {
      this.validateSlot(slot);
      this.guiItems.put(slot, guiItem);
      this.inventory.setItem(slot, guiItem.getItemStack());
   }

   @Override
   public void setItem(int row, int col, @NotNull GuiItem guiItem) {
      this.setItem(this.getSlotFromRowColumn(row, col), guiItem);
   }

   @Override
   public void setItem(@NotNull List<Integer> slots, @NotNull GuiItem guiItem) {
      for (int slot : slots) {
         this.setItem(slot, guiItem);
      }
   }

   @Override
   public void addItem(@NotNull GuiItem... items) {
      this.addItem(false, items);
   }

   @Override
   public void addItem(boolean expandIfFull, @NotNull GuiItem... items) {
      List<GuiItem> notAddedItems = new ArrayList<>();

      for (GuiItem guiItem : items) {
         for (int slot = 0; slot < this.rows * 9; slot++) {
            if (this.guiItems.get(slot) == null) {
               this.guiItems.put(slot, guiItem);
               break;
            }

            if (slot == this.rows * 9 - 1) {
               notAddedItems.add(guiItem);
            }
         }
      }

      if (expandIfFull && this.rows < 6 && !notAddedItems.isEmpty() && (this.guiType == null || this.guiType == GuiType.CHEST)) {
         this.rows++;
         this.updateInventories();
         this.addItem(true, notAddedItems.toArray(new GuiItem[0]));
      }
   }

   @Override
   public void removeItem(ItemStack itemStack) {
      String key = GuiKeys.getUUID(itemStack);
      Optional<Entry<Integer, GuiItem>> entry = this.guiItems.entrySet().stream().filter(it -> {
         String pair = it.getValue().getUuid().toString();
         return key.equalsIgnoreCase(pair);
      }).findFirst();
      entry.ifPresent(it -> {
         this.guiItems.remove(it.getKey());
         this.inventory.remove(it.getValue().getItemStack());
      });
   }

   @Override
   public void removeItem(int row, int col) {
      this.removeItem(this.getSlotFromRowColumn(row, col));
   }

   @Override
   public void removeItem(GuiItem guiItem) {
      this.removeItem(guiItem.getItemStack());
   }

   @Override
   public void removeItem(int slot) {
      this.validateSlot(slot);
      this.guiItems.remove(slot);
   }

   @Nullable
   @Override
   public final GuiItem getGuiItem(int slot) {
      return this.guiItems.get(slot);
   }

   @Override
   public void addSlotAction(int slot, @Nullable GuiAction<InventoryClickEvent> slotAction) {
      this.slotActions.put(slot, slotAction);
   }

   @Nullable
   @Override
   public final GuiAction<InventoryClickEvent> getSlotAction(int slot) {
      return this.slotActions.get(slot);
   }

   @Override
   public void close(final Player player, final Reason reason, boolean isDelayed) {
      if (isDelayed) {
         (new FoliaRunnable(plugin.getServer().getGlobalRegionScheduler()) {
            @Override
            public void run() {
               player.closeInventory(reason != null ? reason : Reason.PLUGIN);
            }
         }).runDelayed(plugin, 2L);
      } else {
         player.closeInventory(reason != null ? reason : Reason.PLUGIN);
      }
   }

   @Override
   public void close(Player player, boolean isDelayed) {
      this.close(player, null, isDelayed);
   }

   @Override
   public void close(Player player) {
      this.close(player, true);
   }

   @Override
   public void updateTitle(Player player) {
      MiscUtil.updateTitle(player, this.title);
   }

   @Override
   public void updateInventory(Player player) {
      this.inventory.clear();
      this.populate();
      player.updateInventory();
   }

   @Override
   public void updateTitles() {
      plugin.getServer().getOnlinePlayers().forEach(player -> {
         InventoryHolder inventory = player.getOpenInventory().getTopInventory().getHolder(false);
         if (inventory instanceof BaseGui) {
            this.updateTitle(player);
         }
      });
   }

   @Override
   public void updateInventories() {
      Inventory inventory = this.inventory;
      inventory.getViewers().forEach(humanEntity -> {
         if (humanEntity instanceof Player player) {
            this.updateInventory(player);
         }
      });
   }

   @Override
   public final boolean isUpdating() {
      return this.isUpdating;
   }

   @Override
   public void setUpdating(boolean updating) {
      this.isUpdating = updating;
   }

   @Nullable
   @Override
   public final GuiAction<InventoryClickEvent> getDefaultClickAction() {
      return this.defaultClickAction;
   }

   @Override
   public void setDefaultClickAction(@Nullable GuiAction<InventoryClickEvent> defaultClickAction) {
      this.defaultClickAction = defaultClickAction;
   }

   @Nullable
   @Override
   public final GuiAction<InventoryClickEvent> getDefaultTopClickAction() {
      return this.defaultTopClickAction;
   }

   @Override
   public void setDefaultTopClickAction(@Nullable GuiAction<InventoryClickEvent> defaultTopClickAction) {
      this.defaultTopClickAction = defaultTopClickAction;
   }

   @Nullable
   @Override
   public final GuiAction<InventoryClickEvent> getPlayerInventoryAction() {
      return this.playerInventoryAction;
   }

   @Override
   public void setPlayerInventoryAction(@Nullable GuiAction<InventoryClickEvent> playerInventoryAction) {
      this.playerInventoryAction = playerInventoryAction;
   }

   @Nullable
   @Override
   public final GuiAction<InventoryDragEvent> getDragAction() {
      return this.dragAction;
   }

   @Override
   public void setDragAction(@Nullable GuiAction<InventoryDragEvent> dragAction) {
      this.dragAction = dragAction;
   }

   @Nullable
   @Override
   public final GuiAction<InventoryCloseEvent> getCloseGuiAction() {
      return this.closeGuiAction;
   }

   @Override
   public void setCloseGuiAction(@Nullable GuiAction<InventoryCloseEvent> closeGuiAction) {
      this.closeGuiAction = closeGuiAction;
   }

   @Nullable
   @Override
   public final GuiAction<InventoryOpenEvent> getOpenGuiAction() {
      return this.openGuiAction;
   }

   @Override
   public void setOpenGuiAction(@Nullable GuiAction<InventoryOpenEvent> openGuiAction) {
      this.openGuiAction = openGuiAction;
   }

   @Nullable
   @Override
   public final GuiAction<InventoryClickEvent> getOutsideClickAction() {
      return this.outsideClickAction;
   }

   @Override
   public void setOutsideClickAction(@Nullable GuiAction<InventoryClickEvent> outsideClickAction) {
      this.outsideClickAction = outsideClickAction;
   }

   @Override
   public void open(Player player, boolean purge) {
      if (!player.isSleeping()) {
         if (purge) {
            this.inventory.clear();
            this.populate();
         }

         player.openInventory(this.inventory);
      }
   }

   @Override
   public void open(Player player) {
      this.open(player, true);
   }

   @NotNull
   public final Inventory getInventory() {
      return this.inventory;
   }

   @NotNull
   public final GuiItem asGuiItem(ItemStack itemStack) {
      return this.asGuiItem(itemStack, null);
   }

   @NotNull
   public final GuiItem asGuiItem(ItemStack itemStack, @Nullable GuiAction<InventoryClickEvent> action) {
      return new GuiItem(itemStack, action);
   }

   public void populate() {
      for (Entry<Integer, GuiItem> entry : this.guiItems.entrySet()) {
         this.inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
      }
   }

   public final int getSlotFromRowColumn(int row, int col) {
      return col + (row - 1) * 9 - 1;
   }

   private void validateSlot(int slot) {
      int limit = this.guiType.getLimit();
      if (this.guiType == GuiType.CHEST) {
         if (slot < 0 || slot >= this.rows * limit) {
            this.throwInvalidSlot(slot);
         }
      } else {
         if (slot < 0 || slot > limit) {
            this.throwInvalidSlot(slot);
         }
      }
   }

   private void throwInvalidSlot(int slot) {
      if (this.guiType == GuiType.CHEST) {
         throw new GenericException("Slot " + slot + " is not valid for the gui type - " + this.guiType.name() + " and rows - " + this.rows + "!");
      } else {
         throw new GenericException("Slot " + slot + " is not valid for the gui type - " + this.guiType.name() + "!");
      }
   }

   static {
      plugin.getServer().getPluginManager().registerEvents(new GuiListener(), plugin);
   }
}
