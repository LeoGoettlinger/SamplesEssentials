package com.artillexstudios.axtrade.libs.axapi.gui.inventory.renderer;

import com.artillexstudios.axtrade.libs.axapi.context.HashMapContext;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.BakedGuiItem;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.Gui;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.GuiKeys;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.renderer.strategy.RenderStrategy;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.renderer.strategy.StreamingRenderStrategy;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.renderer.strategy.WaitingRenderStrategy;
import com.artillexstudios.axtrade.libs.axapi.nms.NMSHandlers;
import com.artillexstudios.axtrade.libs.axapi.utils.PaperUtils;
import com.artillexstudios.axtrade.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.util.concurrent.Semaphore;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

public class InventoryRenderer implements InventoryHolder {
   private final Semaphore renderLock = new Semaphore(1, true);
   private final Int2ObjectMap<BakedGuiItem> items = Int2ObjectMaps.synchronize(new Int2ObjectArrayMap());
   private final Player player;
   private long lastClick;
   private boolean closed = true;
   private Inventory inventory;
   private Gui currentGui;
   private Component lastTitle;

   public InventoryRenderer(Player player) {
      this.player = player;
   }

   public void queue(Gui gui) {
      if (FeatureFlags.DEBUG.get()) {
         LogUtils.debug("Enqueued!");
      }

      Thread.ofVirtual().start(() -> {
         if (FeatureFlags.DEBUG.get()) {
            LogUtils.debug("Running from thread!");
         }

         this.render(gui);
      });
   }

   @Blocking
   public void render(Gui gui) {
      if (this.currentGui != gui || this.renderLock.tryAcquire()) {
         if (this.currentGui != gui) {
            this.renderLock.acquireUninterruptibly();
         }

         HashMapContext context = this.createContext(gui);
         boolean newInventory = this.buildInventory(gui, context);
         this.currentGui = gui;
         RenderStrategy strategy = this.getStrategy();
         strategy.render(gui, context, newInventory, this.items, this.inventory, this.renderLock);
      }
   }

   private RenderStrategy getStrategy() {
      return (RenderStrategy)(FeatureFlags.GUI_WAIT_FOR_ALL.get() ? new WaitingRenderStrategy() : new StreamingRenderStrategy());
   }

   private HashMapContext createContext(Gui gui) {
      return new HashMapContext().with(GuiKeys.PLAYER, this.player).with(GuiKeys.GUI, gui).merge(gui.getContext());
   }

   public void onTitleUpdate(HashMapContext context) {
      Component newTitle = this.currentGui.provideTitle(context);
      if (!newTitle.equals(this.lastTitle)) {
         this.lastTitle = newTitle;
         NMSHandlers.getNmsHandler().setTitle(this.inventory, newTitle);
      }
   }

   public boolean buildInventory(Gui gui, HashMapContext context) {
      if (this.currentGui != null && this.currentGui.getType() == gui.getType() && this.currentGui.getRows() == gui.getRows()) {
         return false;
      } else {
         this.recreateGui(gui, context);
         return true;
      }
   }

   private void recreateGui(Gui gui, HashMapContext context) {
      Component title = gui.provideTitle(context);
      this.lastTitle = title;
      if (gui.getType() == InventoryType.CHEST) {
         this.inventory = PaperUtils.createInventory(this, gui.getRows() * 9, title);
      } else {
         this.inventory = PaperUtils.createInventory(this, gui.getType(), title);
      }
   }

   public void handleClick(InventoryClickEvent event) {
      if (this.currentGui.isDisableAllInteractions()) {
         event.setCancelled(true);
      }

      long now = System.currentTimeMillis();
      if (now - this.lastClick >= FeatureFlags.INVENTORY_CLICK_COOLDOWN.get().intValue()) {
         this.lastClick = now;
         if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
            ((BakedGuiItem)this.items.get(event.getRawSlot())).eventConsumer().accept(event);
         } else {
            this.currentGui.getPlayerInventoryClickListener().accept(event);
         }
      }
   }

   public Gui getCurrentGui() {
      return this.currentGui;
   }

   public boolean isClosed() {
      return this.closed;
   }

   public void setClosed(boolean closed) {
      this.closed = closed;
   }

   public void handleDrag(InventoryDragEvent event) {
      if (this.currentGui.isDisableAllInteractions()) {
         event.setCancelled(true);
      }
   }

   public void handleClose(InventoryCloseEvent event) {
      this.closed = true;
      this.currentGui.getInventoryCloseListener().accept(event);
   }

   public void handleOpen(InventoryOpenEvent event) {
      this.currentGui.getInventoryOpenListener().accept(event);
   }

   public Player getPlayer() {
      return this.player;
   }

   @NotNull
   public Inventory getInventory() {
      return this.inventory;
   }
}
