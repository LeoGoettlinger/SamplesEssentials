package com.artillexstudios.axrankmenu.libs.gui.guis;

import com.artillexstudios.axrankmenu.libs.gui.TriumphGui;
import com.artillexstudios.axrankmenu.libs.gui.components.GuiAction;
import com.artillexstudios.axrankmenu.libs.gui.components.GuiContainer;
import com.artillexstudios.axrankmenu.libs.gui.components.GuiType;
import com.artillexstudios.axrankmenu.libs.gui.components.InteractionModifier;
import com.artillexstudios.axrankmenu.libs.gui.components.exception.GuiException;
import com.artillexstudios.axrankmenu.libs.gui.components.util.GuiFiller;
import com.artillexstudios.axrankmenu.libs.gui.components.util.VersionHelper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseGui implements InventoryHolder {
   private static final Plugin plugin = TriumphGui.getPlugin();
   private static Method GET_SCHEDULER_METHOD = null;
   private static Method EXECUTE_METHOD = null;
   private final GuiFiller filler = new GuiFiller(this);
   private final Map<Integer, GuiItem> guiItems;
   private final Map<Integer, GuiAction<InventoryClickEvent>> slotActions;
   private final Set<InteractionModifier> interactionModifiers;
   private final GuiContainer guiContainer;
   private Inventory inventory;
   private GuiAction<InventoryClickEvent> defaultClickAction;
   private GuiAction<InventoryClickEvent> defaultTopClickAction;
   private GuiAction<InventoryClickEvent> playerInventoryAction;
   private GuiAction<InventoryDragEvent> dragAction;
   private GuiAction<InventoryCloseEvent> closeGuiAction;
   private GuiAction<InventoryOpenEvent> openGuiAction;
   private GuiAction<InventoryClickEvent> outsideClickAction;
   private boolean updating;
   private boolean runCloseAction = true;
   private boolean runOpenAction = true;

   public BaseGui(@NotNull GuiContainer guiContainer, @NotNull Set<InteractionModifier> interactionModifiers) {
      this.interactionModifiers = this.safeCopyOf(interactionModifiers);
      this.guiContainer = guiContainer;
      this.inventory = guiContainer.createInventory(this);
      this.slotActions = new LinkedHashMap<>(guiContainer.inventorySize());
      this.guiItems = new LinkedHashMap<>(guiContainer.inventorySize());
   }

   @NotNull
   private Set<InteractionModifier> safeCopyOf(@NotNull Set<InteractionModifier> set) {
      return set.isEmpty() ? EnumSet.noneOf(InteractionModifier.class) : EnumSet.copyOf(set);
   }

   @NotNull
   public Component title() {
      return this.guiContainer.title();
   }

   public void setItem(int slot, @NotNull GuiItem guiItem) {
      this.validateSlot(slot);
      this.guiItems.put(slot, guiItem);
   }

   public void removeItem(@NotNull GuiItem item) {
      this.guiItems.entrySet().stream().filter(it -> it.getValue().equals(item)).findFirst().ifPresent(it -> {
         this.guiItems.remove(it.getKey());
         this.inventory.remove(it.getValue().getItemStack());
      });
   }

   public void removeItem(@NotNull ItemStack item) {
      this.guiItems.entrySet().stream().filter(it -> it.getValue().getItemStack().equals(item)).findFirst().ifPresent(it -> {
         this.guiItems.remove(it.getKey());
         this.inventory.remove(item);
      });
   }

   public void removeItem(int slot) {
      this.validateSlot(slot);
      this.guiItems.remove(slot);
      this.inventory.setItem(slot, null);
   }

   public void removeItem(int row, int col) {
      this.removeItem(this.getSlotFromRowCol(row, col));
   }

   public void setItem(@NotNull List<Integer> slots, @NotNull GuiItem guiItem) {
      for (int slot : slots) {
         this.setItem(slot, guiItem);
      }
   }

   public void setItem(int row, int col, @NotNull GuiItem guiItem) {
      this.setItem(this.getSlotFromRowCol(row, col), guiItem);
   }

   public void clearItems() {
      this.guiItems.clear();
   }

   public void addItem(@NotNull GuiItem... items) {
      this.addItem(false, items);
   }

   public void addItem(boolean expandIfFull, @NotNull GuiItem... items) {
      List<GuiItem> notAddedItems = new ArrayList<>();
      int rows = this.guiContainer.rows();
      GuiType guiType = this.guiContainer.guiType();

      for (GuiItem guiItem : items) {
         for (int slot = 0; slot < rows * 9; slot++) {
            if (this.guiItems.get(slot) == null) {
               this.guiItems.put(slot, guiItem);
               break;
            }

            if (slot == rows * 9 - 1) {
               notAddedItems.add(guiItem);
            }
         }
      }

      if (expandIfFull && rows < 6 && !notAddedItems.isEmpty() && guiType == GuiType.CHEST) {
         if (this.guiContainer instanceof GuiContainer.Chest) {
            ((GuiContainer.Chest)this.guiContainer).rows(this.guiContainer.rows() + 1);
            this.inventory = this.guiContainer.createInventory(this);
            this.update();
            this.addItem(true, notAddedItems.toArray(new GuiItem[0]));
         }
      }
   }

   public void addSlotAction(int slot, @Nullable GuiAction<InventoryClickEvent> slotAction) {
      this.validateSlot(slot);
      this.slotActions.put(slot, slotAction);
   }

   public void addSlotAction(int row, int col, @Nullable GuiAction<InventoryClickEvent> slotAction) {
      this.addSlotAction(this.getSlotFromRowCol(row, col), slotAction);
   }

   @Nullable
   public GuiItem getGuiItem(int slot) {
      return this.guiItems.get(slot);
   }

   public boolean isUpdating() {
      return this.updating;
   }

   public void setUpdating(boolean updating) {
      this.updating = updating;
   }

   public void open(@NotNull HumanEntity player) {
      if (!player.isSleeping()) {
         this.inventory.clear();
         this.populateGui();
         player.openInventory(this.inventory);
      }
   }

   public void close(@NotNull HumanEntity player) {
      this.close(player, true);
   }

   public void close(@NotNull HumanEntity player, boolean runCloseAction) {
      Runnable task = () -> {
         this.runCloseAction = runCloseAction;
         player.closeInventory();
         this.runCloseAction = true;
      };
      if (VersionHelper.IS_FOLIA) {
         if (GET_SCHEDULER_METHOD != null && EXECUTE_METHOD != null) {
            try {
               EXECUTE_METHOD.invoke(GET_SCHEDULER_METHOD.invoke(player), plugin, task, null, 2L);
            } catch (InvocationTargetException | IllegalAccessException var5) {
               throw new GuiException("Could not invoke Folia task.", var5);
            }
         } else {
            throw new GuiException("Could not find Folia Scheduler methods.");
         }
      } else {
         Bukkit.getScheduler().runTaskLater(plugin, task, 2L);
      }
   }

   public void update() {
      this.inventory.clear();
      this.populateGui();
   }

   @NotNull
   @Contract("_ -> this")
   public BaseGui updateTitle(@NotNull Component title) {
      this.updating = true;
      List<HumanEntity> viewers = new ArrayList<>(this.inventory.getViewers());
      this.guiContainer.title(title);
      this.inventory = this.guiContainer.createInventory(this);

      for (HumanEntity player : viewers) {
         this.open(player);
      }

      this.updating = false;
      return this;
   }

   public void updateItem(int slot, @NotNull ItemStack itemStack) {
      GuiItem guiItem = this.guiItems.get(slot);
      if (guiItem == null) {
         this.updateItem(slot, new GuiItem(itemStack));
      } else {
         guiItem.setItemStack(itemStack);
         this.updateItem(slot, guiItem);
      }
   }

   public void updateItem(int row, int col, @NotNull ItemStack itemStack) {
      this.updateItem(this.getSlotFromRowCol(row, col), itemStack);
   }

   public void updateItem(int slot, @NotNull GuiItem item) {
      this.guiItems.put(slot, item);
      this.inventory.setItem(slot, item.getItemStack());
   }

   public void updateItem(int row, int col, @NotNull GuiItem item) {
      this.updateItem(this.getSlotFromRowCol(row, col), item);
   }

   @NotNull
   @Contract(" -> this")
   public BaseGui disableItemPlace() {
      this.interactionModifiers.add(InteractionModifier.PREVENT_ITEM_PLACE);
      return this;
   }

   @NotNull
   @Contract(" -> this")
   public BaseGui disableItemTake() {
      this.interactionModifiers.add(InteractionModifier.PREVENT_ITEM_TAKE);
      return this;
   }

   @NotNull
   @Contract(" -> this")
   public BaseGui disableItemSwap() {
      this.interactionModifiers.add(InteractionModifier.PREVENT_ITEM_SWAP);
      return this;
   }

   @NotNull
   @Contract(" -> this")
   public BaseGui disableItemDrop() {
      this.interactionModifiers.add(InteractionModifier.PREVENT_ITEM_DROP);
      return this;
   }

   @NotNull
   @Contract(" -> this")
   public BaseGui disableOtherActions() {
      this.interactionModifiers.add(InteractionModifier.PREVENT_OTHER_ACTIONS);
      return this;
   }

   @NotNull
   @Contract(" -> this")
   public BaseGui disableAllInteractions() {
      this.interactionModifiers.addAll(InteractionModifier.VALUES);
      return this;
   }

   @NotNull
   @Contract(" -> this")
   public BaseGui enableItemPlace() {
      this.interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_PLACE);
      return this;
   }

   @NotNull
   @Contract(" -> this")
   public BaseGui enableItemTake() {
      this.interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_TAKE);
      return this;
   }

   @NotNull
   @Contract(" -> this")
   public BaseGui enableItemSwap() {
      this.interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_SWAP);
      return this;
   }

   @NotNull
   @Contract(" -> this")
   public BaseGui enableItemDrop() {
      this.interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_DROP);
      return this;
   }

   @NotNull
   @Contract(" -> this")
   public BaseGui enableOtherActions() {
      this.interactionModifiers.remove(InteractionModifier.PREVENT_OTHER_ACTIONS);
      return this;
   }

   @NotNull
   @Contract(" -> this")
   public BaseGui enableAllInteractions() {
      this.interactionModifiers.clear();
      return this;
   }

   public boolean allInteractionsDisabled() {
      return this.interactionModifiers.size() == InteractionModifier.VALUES.size();
   }

   public boolean canPlaceItems() {
      return !this.interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_PLACE);
   }

   public boolean canTakeItems() {
      return !this.interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_TAKE);
   }

   public boolean canSwapItems() {
      return !this.interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_SWAP);
   }

   public boolean canDropItems() {
      return !this.interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_DROP);
   }

   public boolean allowsOtherActions() {
      return !this.interactionModifiers.contains(InteractionModifier.PREVENT_OTHER_ACTIONS);
   }

   @NotNull
   public GuiFiller getFiller() {
      return this.filler;
   }

   @NotNull
   public Map<Integer, GuiItem> getGuiItems() {
      return Collections.unmodifiableMap(this.guiItems);
   }

   @NotNull
   public Inventory getInventory() {
      return this.inventory;
   }

   public void setInventory(@NotNull Inventory inventory) {
      this.inventory = inventory;
   }

   public int getRows() {
      return this.guiContainer.rows();
   }

   @NotNull
   public GuiType guiType() {
      return this.guiContainer.guiType();
   }

   @Nullable
   GuiAction<InventoryClickEvent> getDefaultClickAction() {
      return this.defaultClickAction;
   }

   public void setDefaultClickAction(@Nullable GuiAction<InventoryClickEvent> defaultClickAction) {
      this.defaultClickAction = defaultClickAction;
   }

   @Nullable
   GuiAction<InventoryClickEvent> getDefaultTopClickAction() {
      return this.defaultTopClickAction;
   }

   public void setDefaultTopClickAction(@Nullable GuiAction<InventoryClickEvent> defaultTopClickAction) {
      this.defaultTopClickAction = defaultTopClickAction;
   }

   @Nullable
   GuiAction<InventoryClickEvent> getPlayerInventoryAction() {
      return this.playerInventoryAction;
   }

   public void setPlayerInventoryAction(@Nullable GuiAction<InventoryClickEvent> playerInventoryAction) {
      this.playerInventoryAction = playerInventoryAction;
   }

   @Nullable
   GuiAction<InventoryDragEvent> getDragAction() {
      return this.dragAction;
   }

   public void setDragAction(@Nullable GuiAction<InventoryDragEvent> dragAction) {
      this.dragAction = dragAction;
   }

   @Nullable
   GuiAction<InventoryCloseEvent> getCloseGuiAction() {
      return this.closeGuiAction;
   }

   public void setCloseGuiAction(@Nullable GuiAction<InventoryCloseEvent> closeGuiAction) {
      this.closeGuiAction = closeGuiAction;
   }

   @Nullable
   GuiAction<InventoryOpenEvent> getOpenGuiAction() {
      return this.openGuiAction;
   }

   public void setOpenGuiAction(@Nullable GuiAction<InventoryOpenEvent> openGuiAction) {
      this.openGuiAction = openGuiAction;
   }

   @Nullable
   GuiAction<InventoryClickEvent> getOutsideClickAction() {
      return this.outsideClickAction;
   }

   public void setOutsideClickAction(@Nullable GuiAction<InventoryClickEvent> outsideClickAction) {
      this.outsideClickAction = outsideClickAction;
   }

   @Nullable
   GuiAction<InventoryClickEvent> getSlotAction(int slot) {
      return this.slotActions.get(slot);
   }

   void populateGui() {
      for (Entry<Integer, GuiItem> entry : this.guiItems.entrySet()) {
         this.inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
      }
   }

   boolean shouldRunCloseAction() {
      return this.runCloseAction;
   }

   boolean shouldRunOpenAction() {
      return this.runOpenAction;
   }

   int getSlotFromRowCol(int row, int col) {
      return col + (row - 1) * 9 - 1;
   }

   private void validateSlot(int slot) {
      GuiType guiType = this.guiContainer.guiType();
      int limit = guiType.getLimit();
      if (guiType == GuiType.CHEST) {
         if (slot < 0 || slot >= this.guiContainer.rows() * limit) {
            this.throwInvalidSlot(slot);
         }
      } else {
         if (slot < 0 || slot > limit) {
            this.throwInvalidSlot(slot);
         }
      }
   }

   private void throwInvalidSlot(int slot) {
      if (this.guiContainer.guiType() == GuiType.CHEST) {
         throw new GuiException(
            "Slot " + slot + " is not valid for the gui type - " + this.guiContainer.guiType().name() + " and rows - " + this.guiContainer.rows() + "!"
         );
      } else {
         throw new GuiException("Slot " + slot + " is not valid for the gui type - " + this.guiContainer.guiType().name() + "!");
      }
   }

   @NotNull
   protected GuiContainer guiContainer() {
      return this.guiContainer;
   }

   static {
      try {
         GET_SCHEDULER_METHOD = Entity.class.getMethod("getScheduler");
         Class<?> entityScheduler = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
         EXECUTE_METHOD = entityScheduler.getMethod("execute", Plugin.class, Runnable.class, Runnable.class, long.class);
      } catch (ClassNotFoundException | NoSuchMethodException var1) {
      }

      Bukkit.getPluginManager().registerEvents(new GuiListener(), plugin);
      Bukkit.getPluginManager().registerEvents(new InteractionModifierListener(), plugin);
   }
}
