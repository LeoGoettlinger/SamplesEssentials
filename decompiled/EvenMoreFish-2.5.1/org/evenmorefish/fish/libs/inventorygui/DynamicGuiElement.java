package org.evenmorefish.fish.libs.inventorygui;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DynamicGuiElement extends GuiElement {
   private Function<HumanEntity, GuiElement> query;
   private Map<UUID, DynamicGuiElement.CacheEntry> cachedElements = new ConcurrentHashMap<>();

   public DynamicGuiElement(char slotChar, Supplier<GuiElement> query) {
      this(slotChar, h -> query.get());
   }

   public DynamicGuiElement(char slotChar, Function<HumanEntity, GuiElement> query) {
      super(slotChar);
      this.query = query;
   }

   public void update() {
      for (UUID playerId : new ArrayList<>(this.cachedElements.keySet())) {
         Player p = this.gui.getPlugin().getServer().getPlayer(playerId);
         if (p != null && p.isOnline()) {
            this.update(p);
         } else {
            this.cachedElements.remove(playerId);
         }
      }
   }

   public DynamicGuiElement.CacheEntry update(HumanEntity player) {
      DynamicGuiElement.CacheEntry cacheEntry = new DynamicGuiElement.CacheEntry(this.queryElement(player));
      if (cacheEntry.element instanceof DynamicGuiElement) {
         ((DynamicGuiElement)cacheEntry.element).update(player);
      } else if (cacheEntry.element instanceof GuiElementGroup) {
         InventoryGui.updateElements(player, ((GuiElementGroup)cacheEntry.element).getElements());
      }

      this.cachedElements.put(player.getUniqueId(), cacheEntry);
      return cacheEntry;
   }

   @Override
   public void setGui(InventoryGui gui) {
      super.setGui(gui);
   }

   @Override
   public ItemStack getItem(HumanEntity who, int slot) {
      GuiElement element = this.getCachedElement(who);
      return element != null ? element.getItem(who, slot) : null;
   }

   @Override
   public GuiElement.Action getAction(HumanEntity who) {
      GuiElement element = this.getCachedElement(who);
      return element != null ? element.getAction(who) : null;
   }

   public Function<HumanEntity, GuiElement> getQuery() {
      return this.query;
   }

   public void setQuery(Function<HumanEntity, GuiElement> query) {
      this.query = query;
   }

   public GuiElement queryElement(HumanEntity who) {
      GuiElement element = this.getQuery().apply(who);
      if (element != null) {
         element.setGui(this.gui);
         element.setSlots(this.slots);
      }

      return element;
   }

   public GuiElement getCachedElement(HumanEntity who) {
      DynamicGuiElement.CacheEntry cached = this.cachedElements.get(who.getUniqueId());
      if (cached == null) {
         cached = this.update(who);
      }

      return cached.getElement();
   }

   public GuiElement removeCachedElement(HumanEntity who) {
      DynamicGuiElement.CacheEntry cached = this.cachedElements.remove(who.getUniqueId());
      return cached != null ? cached.getElement() : null;
   }

   public long getLastCached(HumanEntity who) {
      DynamicGuiElement.CacheEntry cached = this.cachedElements.get(who.getUniqueId());
      return cached != null ? cached.getCreated() : -1L;
   }

   public class CacheEntry {
      private final GuiElement element;
      private final long created = System.currentTimeMillis();

      CacheEntry(GuiElement element) {
         this.element = element;
      }

      public GuiElement getElement() {
         return this.element;
      }

      public long getCreated() {
         return this.created;
      }
   }
}
