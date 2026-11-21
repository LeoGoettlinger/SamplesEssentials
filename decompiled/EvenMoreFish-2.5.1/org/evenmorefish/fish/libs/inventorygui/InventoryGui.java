package org.evenmorefish.fish.libs.inventorygui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.DragType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class InventoryGui implements Listener {
   private static final int[] ROW_WIDTHS = new int[]{3, 5, 9};
   private static final InventoryType[] INVENTORY_TYPES = new InventoryType[]{InventoryType.DISPENSER, InventoryType.HOPPER, InventoryType.CHEST};
   private static final Map<String, InventoryGui> GUI_MAP = new ConcurrentHashMap<>();
   private static final Map<UUID, ArrayDeque<InventoryGui>> GUI_HISTORY = new ConcurrentHashMap<>();
   private static final Map<String, Pattern> PATTERN_CACHE = new HashMap<>();
   private static final boolean FOLIA;
   private static String DEFAULT_CLICK_SOUND;
   private final Plugin plugin;
   private final InventoryGui.GuiListener listener;
   private InventoryGui.InventoryCreator creator;
   private BiConsumer<ItemMeta, String> itemNameSetter;
   private BiConsumer<ItemMeta, List<String>> itemLoreSetter;
   private String title;
   private boolean titleUpdated = false;
   private final char[] slots;
   private int width;
   private final GuiElement[] elementSlots;
   private final Map<Character, GuiElement> elements = new ConcurrentHashMap<>();
   private InventoryType inventoryType;
   private final Map<UUID, Inventory> inventories = new ConcurrentHashMap<>();
   private InventoryHolder owner;
   private final Map<UUID, Integer> pageNumbers = new ConcurrentHashMap<>();
   private final Map<UUID, Integer> pageAmounts = new ConcurrentHashMap<>();
   private GuiElement.Action outsideAction = click -> false;
   private InventoryGui.CloseAction closeAction = close -> true;
   private String clickSound = getDefaultClickSound();
   private boolean silent = false;

   public InventoryGui(
      Plugin plugin,
      InventoryGui.InventoryCreator creator,
      BiConsumer<ItemMeta, String> itemNameSetter,
      BiConsumer<ItemMeta, List<String>> itemLoreSetter,
      InventoryHolder owner,
      String title,
      String[] rows,
      GuiElement... elements
   ) {
      this.plugin = plugin;
      this.creator = creator;
      this.itemNameSetter = itemNameSetter;
      this.itemLoreSetter = itemLoreSetter;
      this.owner = owner;
      this.title = title;
      this.listener = new InventoryGui.GuiListener();
      this.width = ROW_WIDTHS[0];

      for (String row : rows) {
         if (row.length() > this.width) {
            this.width = row.length();
         }
      }

      for (int i = 0; i < ROW_WIDTHS.length && i < INVENTORY_TYPES.length; i++) {
         if (this.width < ROW_WIDTHS[i]) {
            this.width = ROW_WIDTHS[i];
         }

         if (this.width == ROW_WIDTHS[i]) {
            this.inventoryType = INVENTORY_TYPES[i];
            break;
         }
      }

      if (this.inventoryType == null) {
         throw new IllegalArgumentException("Could not match row setup to an inventory type!");
      } else {
         StringBuilder slotsBuilder = new StringBuilder();

         for (String rowx : rows) {
            if (rowx.length() >= this.width) {
               if (rowx.length() == this.width) {
                  slotsBuilder.append(rowx);
               } else {
                  slotsBuilder.append(rowx, 0, this.width);
               }
            } else {
               double side = (this.width - rowx.length()) / 2.0;

               for (int i = 0; i < Math.floor(side); i++) {
                  slotsBuilder.append(" ");
               }

               slotsBuilder.append(rowx);

               for (int i = 0; i < Math.ceil(side); i++) {
                  slotsBuilder.append(" ");
               }
            }
         }

         this.slots = slotsBuilder.toString().toCharArray();
         this.elementSlots = new GuiElement[this.slots.length];
         this.addElements(elements);
      }
   }

   public InventoryGui(Plugin plugin, InventoryGui.InventoryCreator creator, InventoryHolder owner, String title, String[] rows, GuiElement... elements) {
      this(plugin, creator, ItemMeta::setDisplayName, ItemMeta::setLore, owner, title, rows, elements);
   }

   public InventoryGui(Plugin plugin, InventoryHolder owner, String title, String[] rows, GuiElement... elements) {
      this(
         plugin,
         new InventoryGui.InventoryCreator(
            (gui, who, type) -> plugin.getServer().createInventory(new InventoryGui.Holder(gui), type, gui.replaceVars(who, gui.getTitle())),
            (gui, who, size) -> plugin.getServer().createInventory(new InventoryGui.Holder(gui), size, gui.replaceVars(who, gui.getTitle()))
         ),
         owner,
         title,
         rows,
         elements
      );
   }

   public InventoryGui(Plugin plugin, String title, String[] rows, GuiElement... elements) {
      this(plugin, null, title, rows, elements);
   }

   public InventoryGui(Plugin plugin, InventoryHolder owner, String title, String[] rows, Collection<GuiElement> elements) {
      this(plugin, owner, title, rows);
      this.addElements(elements);
   }

   public void setElement(int slot, GuiElement element) {
      if (slot < 0 || slot >= this.elementSlots.length) {
         throw new IllegalArgumentException("Provided slots is outside available slots! (" + this.elementSlots.length + ")");
      } else if (element.getSlots().length <= 0 && element.getGui() == null) {
         element.setSlots(new int[]{slot});
         element.setGui(this);
         this.elementSlots[slot] = element;
      } else {
         throw new IllegalStateException("Element was already added to a gui!");
      }
   }

   public void addElement(GuiElement element) {
      if (element.getSlots().length <= 0 && element.getGui() == null) {
         this.elements.put(element.getSlotChar(), element);
         element.setGui(this);
         int[] slots = this.getSlots(element.getSlotChar());
         element.setSlots(slots);

         for (int slot : slots) {
            this.elementSlots[slot] = element;
         }
      } else {
         throw new IllegalStateException("Element was already added to a gui!");
      }
   }

   private int[] getSlots(char slotChar) {
      ArrayList<Integer> slotList = new ArrayList<>();

      for (int i = 0; i < this.slots.length; i++) {
         if (this.slots[i] == slotChar) {
            slotList.add(i);
         }
      }

      return slotList.stream().mapToInt(Integer::intValue).toArray();
   }

   public void addElement(char slotChar, ItemStack item, GuiElement.Action action, String... text) {
      this.addElement(new StaticGuiElement(slotChar, item, action, text));
   }

   public void addElement(char slotChar, ItemStack item, String... text) {
      this.addElement(new StaticGuiElement(slotChar, item, null, text));
   }

   public void addElement(char slotChar, MaterialData materialData, GuiElement.Action action, String... text) {
      this.addElement(slotChar, materialData.toItemStack(1), action, text);
   }

   public void addElement(char slotChar, Material material, byte data, GuiElement.Action action, String... text) {
      this.addElement(slotChar, new MaterialData(material, data), action, text);
   }

   public void addElement(char slotChar, Material material, GuiElement.Action action, String... text) {
      this.addElement(slotChar, material, (byte)0, action, text);
   }

   public void addElements(GuiElement... elements) {
      for (GuiElement element : elements) {
         this.addElement(element);
      }
   }

   public void addElements(Collection<GuiElement> elements) {
      for (GuiElement element : elements) {
         this.addElement(element);
      }
   }

   public boolean removeElement(GuiElement element) {
      boolean removed = this.elements.remove(element.getSlotChar(), element);

      for (int slot : element.getSlots()) {
         if (this.elementSlots[slot] == element) {
            this.elementSlots[slot] = null;
            removed = true;
         }
      }

      return removed;
   }

   public GuiElement removeElement(char slotChar) {
      GuiElement element = this.getElement(slotChar);
      if (element != null) {
         this.removeElement(element);
      }

      return element;
   }

   public GuiElement removeElement(int slot) {
      if (slot >= 0 && slot < this.elementSlots.length) {
         GuiElement element = this.elementSlots[slot];
         this.elementSlots[slot] = null;
         return element;
      } else {
         return null;
      }
   }

   public InventoryGui filler(ItemStack item) {
      this.setFiller(item);
      return this;
   }

   public void setFiller(ItemStack item) {
      this.addElement(new StaticGuiElement(' ', item, " "));
   }

   public GuiElement getFiller() {
      return this.elements.get(' ');
   }

   public int getPageNumber(@NotNull HumanEntity player) {
      return this.pageNumbers.getOrDefault(player.getUniqueId(), 0);
   }

   public void setPageNumber(int pageNumber) {
      for (UUID playerId : this.inventories.keySet()) {
         Player player = this.plugin.getServer().getPlayer(playerId);
         if (player != null) {
            this.setPageNumber(player, pageNumber);
         }
      }
   }

   public void setPageNumber(HumanEntity player, int pageNumber) {
      this.setPageNumberInternal(player, pageNumber);
      this.draw(player, false);
   }

   private void setPageNumberInternal(HumanEntity player, int pageNumber) {
      this.pageNumbers.put(player.getUniqueId(), Math.max(pageNumber, 0));
   }

   public int getPageAmount(@NotNull HumanEntity player) {
      return this.pageAmounts.getOrDefault(player.getUniqueId(), 1);
   }

   private void setPageAmount(HumanEntity player, int pageAmount) {
      this.pageAmounts.put(player.getUniqueId(), pageAmount);
   }

   private void calculatePageAmount(HumanEntity player) {
      int pageAmount = 0;

      for (GuiElement element : this.elements.values()) {
         int amount = this.calculateElementSize(player, element);
         if (amount > 0 && (pageAmount - 1) * element.getSlots().length < amount && element.getSlots().length > 0) {
            pageAmount = (int)Math.ceil((double)amount / element.getSlots().length);
         }
      }

      this.setPageAmount(player, pageAmount);
      if (this.getPageNumber(player) >= pageAmount) {
         this.setPageNumberInternal(player, Math.min(0, pageAmount - 1));
      }
   }

   private int calculateElementSize(HumanEntity player, GuiElement element) {
      if (element instanceof GuiElementGroup) {
         return ((GuiElementGroup)element).size();
      } else if (element instanceof GuiStorageElement) {
         return ((GuiStorageElement)element).getStorage().getSize();
      } else {
         return element instanceof DynamicGuiElement ? this.calculateElementSize(player, ((DynamicGuiElement)element).getCachedElement(player)) : 0;
      }
   }

   public void show(HumanEntity player) {
      this.show(player, true);
   }

   public void show(HumanEntity player, boolean checkOpen) {
      this.draw(player, true, this.titleUpdated);
      if (this.titleUpdated || !checkOpen || !this.equals(getOpen(player))) {
         InventoryType type = player.getOpenInventory().getType();
         if (type != InventoryType.CRAFTING && type != InventoryType.CREATIVE) {
            this.runTask(player, () -> {
               Inventory inventoryx = this.getInventory(player);
               if (inventoryx != null) {
                  addHistory(player, this);
                  player.openInventory(inventoryx);
               }
            });
         } else {
            Inventory inventory = this.getInventory(player);
            if (inventory != null) {
               clearHistory(player);
               addHistory(player, this);
               player.openInventory(inventory);
            }
         }
      }

      this.titleUpdated = false;
   }

   public void build() {
      this.build(this.owner);
   }

   public void build(InventoryHolder owner) {
      this.setOwner(owner);
      this.listener.registerListeners();
   }

   public void draw() {
      for (UUID playerId : this.inventories.keySet()) {
         Player player = this.plugin.getServer().getPlayer(playerId);
         if (player != null) {
            this.runTaskOrNow(player, () -> this.draw(player));
         }
      }
   }

   public void draw(HumanEntity who) {
      this.draw(who, true);
   }

   public void draw(HumanEntity who, boolean updateDynamic) {
      this.draw(who, updateDynamic, false);
   }

   public void draw(HumanEntity who, boolean updateDynamic, boolean recreateInventory) {
      if (updateDynamic) {
         updateElements(who, this.elements.values());
      }

      this.calculatePageAmount(who);
      Inventory inventory = this.getInventory(who);
      if (inventory != null && !recreateInventory) {
         inventory.clear();
      } else {
         this.build();
         if (this.slots.length != this.inventoryType.getDefaultSize()) {
            inventory = this.getInventoryCreator().getSizeCreator().create(this, who, this.slots.length);
         } else {
            inventory = this.getInventoryCreator().getTypeCreator().create(this, who, this.inventoryType);
         }

         this.inventories.put(who != null ? who.getUniqueId() : null, inventory);
      }

      for (int i = 0; i < inventory.getSize(); i++) {
         GuiElement element = this.getElement(i);
         if (element == null) {
            element = this.getFiller();
         }

         if (element != null) {
            inventory.setItem(i, element.getItem(who, i));
         }
      }
   }

   protected void runTask(HumanEntity entity, Runnable task) {
      if (FOLIA) {
         entity.getScheduler().run(this.plugin, st -> task.run(), null);
      } else {
         this.plugin.getServer().getScheduler().runTask(this.plugin, task);
      }
   }

   protected void runTask(Runnable task) {
      if (FOLIA) {
         this.plugin.getServer().getGlobalRegionScheduler().run(this.plugin, st -> task.run());
      } else {
         this.plugin.getServer().getScheduler().runTask(this.plugin, task);
      }
   }

   protected void runTaskOrNow(HumanEntity entity, Runnable task) {
      if (FOLIA) {
         if (this.plugin.getServer().isOwnedByCurrentRegion(entity)) {
            task.run();
         } else {
            entity.getScheduler().run(this.plugin, st -> task.run(), null);
         }
      } else if (this.plugin.getServer().isPrimaryThread()) {
         task.run();
      } else {
         this.plugin.getServer().getScheduler().runTask(this.plugin, task);
      }
   }

   public static void updateElements(HumanEntity who, Collection<GuiElement> elements) {
      for (GuiElement element : elements) {
         if (element instanceof DynamicGuiElement) {
            ((DynamicGuiElement)element).update(who);
         } else if (element instanceof GuiElementGroup) {
            updateElements(who, ((GuiElementGroup)element).getElements());
         }
      }
   }

   public void close() {
      this.close(true);
   }

   public void close(boolean clearHistory) {
      for (Inventory inventory : this.inventories.values()) {
         for (HumanEntity viewer : new ArrayList(inventory.getViewers())) {
            this.close(viewer, clearHistory);
         }
      }
   }

   public void close(HumanEntity viewer) {
      this.close(viewer, true);
   }

   public void close(HumanEntity viewer, boolean clearHistory) {
      if (clearHistory) {
         clearHistory(viewer);
      }

      viewer.closeInventory();
   }

   public void destroy() {
      this.destroy(true);
   }

   private void destroy(boolean closeInventories) {
      if (closeInventories) {
         this.close();
      }

      for (Inventory inventory : this.inventories.values()) {
         inventory.clear();
      }

      this.inventories.clear();
      this.pageNumbers.clear();
      this.pageAmounts.clear();
      this.listener.unregisterListeners();
      this.removeFromMap();
   }

   public static void addHistory(HumanEntity player, InventoryGui gui) {
      GUI_HISTORY.putIfAbsent(player.getUniqueId(), new ArrayDeque<>());
      Deque<InventoryGui> history = getHistory(player);
      if (history.peekLast() != gui) {
         history.add(gui);
      }
   }

   public static Deque<InventoryGui> getHistory(HumanEntity player) {
      return GUI_HISTORY.getOrDefault(player.getUniqueId(), new ArrayDeque<>());
   }

   public static boolean goBack(HumanEntity player) {
      Deque<InventoryGui> history = getHistory(player);
      history.pollLast();
      if (history.isEmpty()) {
         return false;
      } else {
         InventoryGui previous = history.peekLast();
         if (previous != null) {
            previous.show(player, false);
         }

         return true;
      }
   }

   public static Deque<InventoryGui> clearHistory(HumanEntity player) {
      Deque<InventoryGui> previous = GUI_HISTORY.remove(player.getUniqueId());
      return (Deque<InventoryGui>)(previous != null ? previous : new ArrayDeque<>());
   }

   public Plugin getPlugin() {
      return this.plugin;
   }

   public InventoryGui.InventoryCreator getInventoryCreator() {
      return this.creator;
   }

   public void setInventoryCreator(InventoryGui.InventoryCreator inventoryCreator) {
      this.creator = Objects.requireNonNull(inventoryCreator);
   }

   public BiConsumer<ItemMeta, String> getItemNameSetter() {
      return this.itemNameSetter;
   }

   public void setItemNameSetter(BiConsumer<ItemMeta, String> itemNameSetter) {
      this.itemNameSetter = Objects.requireNonNull(itemNameSetter);
   }

   public BiConsumer<ItemMeta, List<String>> getItemLoreSetter() {
      return this.itemLoreSetter;
   }

   public void setItemLoreSetter(BiConsumer<ItemMeta, List<String>> itemLoreSetter) {
      this.itemLoreSetter = Objects.requireNonNull(itemLoreSetter);
   }

   public GuiElement getElement(int slot) {
      return slot >= 0 && slot < this.elementSlots.length ? this.elementSlots[slot] : null;
   }

   public GuiElement getElement(char c) {
      return this.elements.get(c);
   }

   public Collection<GuiElement> getElements() {
      return Collections.unmodifiableCollection(this.elements.values());
   }

   public void setOwner(InventoryHolder owner) {
      this.removeFromMap();
      this.owner = owner;
      if (owner instanceof Entity) {
         GUI_MAP.put(((Entity)owner).getUniqueId().toString(), this);
      } else if (owner instanceof BlockState) {
         GUI_MAP.put(((BlockState)owner).getLocation().toString(), this);
      }
   }

   public InventoryHolder getOwner() {
      return this.owner;
   }

   public boolean hasRealOwner() {
      return this.owner != null;
   }

   public GuiElement.Action getOutsideAction() {
      return this.outsideAction;
   }

   public void setOutsideAction(GuiElement.Action outsideAction) {
      this.outsideAction = outsideAction;
   }

   public InventoryGui.CloseAction getCloseAction() {
      return this.closeAction;
   }

   public void setCloseAction(InventoryGui.CloseAction closeAction) {
      this.closeAction = closeAction;
   }

   public static String getDefaultClickSound() {
      return DEFAULT_CLICK_SOUND;
   }

   public static void setDefaultClickSound(String defaultClickSound) {
      DEFAULT_CLICK_SOUND = defaultClickSound;
   }

   public String getClickSound() {
      return this.clickSound;
   }

   public void setClickSound(String soundKey) {
      this.clickSound = soundKey;
   }

   public boolean isSilent() {
      return this.silent;
   }

   public void setSilent(boolean silent) {
      this.silent = silent;
   }

   private void removeFromMap() {
      if (this.owner instanceof Entity) {
         GUI_MAP.remove(((Entity)this.owner).getUniqueId().toString(), this);
      } else if (this.owner instanceof BlockState) {
         GUI_MAP.remove(((BlockState)this.owner).getLocation().toString(), this);
      }
   }

   public static InventoryGui get(InventoryHolder holder) {
      if (holder instanceof Entity) {
         return GUI_MAP.get(((Entity)holder).getUniqueId().toString());
      } else {
         return holder instanceof BlockState ? GUI_MAP.get(((BlockState)holder).getLocation().toString()) : null;
      }
   }

   public static InventoryGui getOpen(HumanEntity player) {
      return getHistory(player).peekLast();
   }

   public String getTitle() {
      return this.title;
   }

   public void setTitle(String title) {
      this.title = title;
      this.titleUpdated = true;
   }

   public void playClickSound() {
      if (!this.isSilent() && this.clickSound != null) {
         for (Inventory inventory : this.inventories.values()) {
            for (HumanEntity humanEntity : inventory.getViewers()) {
               if (humanEntity instanceof Player) {
                  ((Player)humanEntity).playSound(humanEntity.getEyeLocation(), this.getClickSound(), 1.0F, 1.0F);
               }
            }
         }
      }
   }

   Inventory getInventory() {
      return this.getInventory(null);
   }

   private Inventory getInventory(HumanEntity who) {
      return who != null ? this.inventories.get(who.getUniqueId()) : (this.inventories.isEmpty() ? null : this.inventories.values().iterator().next());
   }

   int getWidth() {
      return this.width;
   }

   private GuiElement.Click handleInteract(InventoryInteractEvent event, ClickType clickType, int slot, ItemStack cursor) {
      GuiElement.Action action = null;
      GuiElement element = null;

      try {
         if (slot >= 0) {
            element = this.getElement(slot);
            if (element != null) {
               action = element.getAction(event.getWhoClicked());
            }
         } else {
            if (slot != -999) {
               if (event instanceof InventoryClickEvent) {
                  if (((InventoryClickEvent)event).getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                     GuiElement.Click click = new GuiElement.Click(this, slot, clickType, cursor, null, event);
                     this.simulateCollectToCursor(click);
                     return click;
                  }

                  if (((InventoryClickEvent)event).getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                     event.setCancelled(true);
                  }
               }

               return null;
            }

            action = this.outsideAction;
         }

         GuiElement.Click click = new GuiElement.Click(this, slot, clickType, cursor, element, event);
         if (action == null || action.onClick(click)) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player) {
               ((Player)event.getWhoClicked()).updateInventory();
            }
         }

         if (action != null) {
            for (UUID playerId : this.inventories.keySet()) {
               if (!event.getWhoClicked().getUniqueId().equals(playerId)) {
                  Player player = this.plugin.getServer().getPlayer(playerId);
                  if (player != null) {
                     this.draw(player, false);
                  }
               }
            }

            return click;
         }
      } catch (Throwable var11) {
         event.setCancelled(true);
         if (event.getWhoClicked() instanceof Player) {
            ((Player)event.getWhoClicked()).updateInventory();
         }

         this.plugin
            .getLogger()
            .log(
               Level.SEVERE,
               "Exception while trying to run action for click on "
                  + (element != null ? element.getClass().getSimpleName() : "empty/unknown element")
                  + " in slot "
                  + slot
                  + " of "
                  + this.getTitle()
                  + " GUI!",
               var11
            );
      }

      return null;
   }

   @Deprecated
   public void setItemText(ItemStack item, String... text) {
      this.setItemText(null, item, text);
   }

   public void setItemText(HumanEntity player, ItemStack item, String... text) {
      if (item != null && text != null && text.length > 0) {
         ItemMeta meta = item.getItemMeta();
         if (meta != null) {
            String combined = this.replaceVars(
               player, Arrays.stream(text).map(s -> s == null ? " " : s).filter(s -> !s.isEmpty()).collect(Collectors.joining("\n"))
            );
            String[] lines = combined.split("\n");
            if (text[0] != null) {
               this.getItemNameSetter().accept(meta, lines[0]);
            }

            if (lines.length > 1) {
               this.getItemLoreSetter().accept(meta, Arrays.asList(Arrays.copyOfRange(lines, 1, lines.length)));
            } else {
               meta.setLore(null);
            }

            item.setItemMeta(meta);
         }
      }
   }

   public String replaceVars(@NotNull HumanEntity player, @NotNull String text, String... replacements) {
      Map<String, String> map = new LinkedHashMap<>();

      for (int i = 0; i + 1 < replacements.length; i += 2) {
         map.putIfAbsent(replacements[i], replacements[i + 1]);
      }

      map.putIfAbsent("plugin", this.plugin.getName());

      try {
         map.putIfAbsent("owner", this.owner instanceof Nameable ? ((Nameable)this.owner).getCustomName() : "");
      } catch (NoClassDefFoundError | NoSuchMethodError var6) {
         map.putIfAbsent("owner", this.owner instanceof Entity ? ((Entity)this.owner).getCustomName() : "");
      }

      map.putIfAbsent("title", this.title);
      map.putIfAbsent("page", String.valueOf(this.getPageNumber(player) + 1));
      map.putIfAbsent("nextpage", this.getPageNumber(player) + 1 < this.getPageAmount(player) ? String.valueOf(this.getPageNumber(player) + 2) : "none");
      map.putIfAbsent("prevpage", this.getPageNumber(player) > 0 ? String.valueOf(this.getPageNumber(player)) : "none");
      map.putIfAbsent("pages", String.valueOf(this.getPageAmount(player)));
      return ChatColor.translateAlternateColorCodes('&', this.replace(text, map));
   }

   private String replace(String string, Map<String, String> replacements) {
      for (Entry<String, String> entry : replacements.entrySet()) {
         if (entry.getKey() != null) {
            String placeholder = "%" + entry.getKey() + "%";
            Pattern pattern = PATTERN_CACHE.get(placeholder);
            if (pattern == null) {
               PATTERN_CACHE.put(placeholder, pattern = Pattern.compile(placeholder, 16));
            }

            string = pattern.matcher(string).replaceAll(Matcher.quoteReplacement(entry.getValue() != null ? entry.getValue() : "null"));
         }
      }

      return string;
   }

   void simulateCollectToCursor(GuiElement.Click click) {
      if (click.getRawEvent() instanceof InventoryClickEvent) {
         InventoryClickEvent event = (InventoryClickEvent)click.getRawEvent();
         ItemStack newCursor = click.getCursor().clone();
         boolean itemInGui = false;

         for (int i = 0; i < click.getRawEvent().getView().getTopInventory().getSize(); i++) {
            if (i != event.getRawSlot()) {
               ItemStack viewItem = click.getRawEvent().getView().getTopInventory().getItem(i);
               if (newCursor.isSimilar(viewItem)) {
                  itemInGui = true;
               }

               GuiElement element = this.getElement(i);
               if (element instanceof GuiStorageElement) {
                  GuiStorageElement storageElement = (GuiStorageElement)element;
                  ItemStack otherStorageItem = storageElement.getStorageItem(click.getWhoClicked(), i);
                  if (storageElement.validateItemTake(i, otherStorageItem)) {
                     int resultSize = addToStack(newCursor, otherStorageItem);
                     if (resultSize > -1) {
                        if (resultSize == 0) {
                           otherStorageItem = null;
                        }

                        storageElement.setStorageItem(click.getWhoClicked(), i, otherStorageItem);
                        if (newCursor.getAmount() == newCursor.getMaxStackSize()) {
                           break;
                        }
                     }
                  }
               }
            }
         }

         if (itemInGui) {
            event.setCurrentItem(null);
            event.setCancelled(true);
            if (click.getWhoClicked() instanceof Player) {
               ((Player)click.getWhoClicked()).updateInventory();
            }

            if (click.getElement() instanceof GuiStorageElement) {
               ((GuiStorageElement)click.getElement()).setStorageItem(click.getWhoClicked(), click.getSlot(), null);
            }

            if (newCursor.getAmount() < newCursor.getMaxStackSize()) {
               Inventory bottomInventory = event.getView().getBottomInventory();

               for (int ix = 0; ix < bottomInventory.getContents().length; ix++) {
                  ItemStack bottomItem = bottomInventory.getItem(ix);
                  int resultSize = addToStack(newCursor, bottomItem);
                  if (resultSize > -1) {
                     if (resultSize == 0) {
                        bottomInventory.setItem(ix, null);
                     }

                     if (newCursor.getAmount() == newCursor.getMaxStackSize()) {
                        break;
                     }
                  }
               }
            }

            event.setCursor(newCursor);
            this.draw();
         }
      }
   }

   private static int addToStack(ItemStack item, ItemStack add) {
      if (item.isSimilar(add)) {
         int newAmount = item.getAmount() + add.getAmount();
         if (newAmount > item.getMaxStackSize()) {
            item.setAmount(item.getMaxStackSize());
            add.setAmount(newAmount - item.getAmount());
            return add.getAmount();
         } else {
            item.setAmount(newAmount);
            add.setAmount(0);
            return 0;
         }
      } else {
         return -1;
      }
   }

   static {
      boolean folia;
      try {
         Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
         folia = true;
      } catch (ClassNotFoundException var7) {
         folia = false;
      }

      FOLIA = folia;
      String clickSound = null;
      Map<String, String> clickSounds = new LinkedHashMap<>();
      clickSounds.put("UI_BUTTON_CLICK", "ui.button.click");
      clickSounds.put("CLICK", "random.click");

      for (Entry<String, String> entry : clickSounds.entrySet()) {
         try {
            Sound.valueOf(entry.getKey().toUpperCase(Locale.ROOT));
            clickSound = entry.getValue();
            break;
         } catch (IllegalArgumentException var8) {
         }
      }

      if (clickSound == null) {
         for (Sound sound : Sound.values()) {
            if (sound.name().contains("CLICK")) {
               clickSound = sound.name().toLowerCase(Locale.ROOT).replace('_', '.');
               break;
            }
         }
      }

      setDefaultClickSound(clickSound);
   }

   public static class Close {
      private final HumanEntity player;
      private final InventoryGui gui;
      private final InventoryCloseEvent event;

      public Close(HumanEntity player, InventoryGui gui, InventoryCloseEvent event) {
         this.player = player;
         this.gui = gui;
         this.event = event;
      }

      public HumanEntity getPlayer() {
         return this.player;
      }

      public InventoryGui getGui() {
         return this.gui;
      }

      public InventoryCloseEvent getEvent() {
         return this.event;
      }
   }

   public interface CloseAction {
      boolean onClose(InventoryGui.Close var1);
   }

   private class GuiListener extends InventoryGui.UnregisterableListener {
      private GuiListener() {
      }

      @EventHandler(
         priority = EventPriority.HIGHEST,
         ignoreCancelled = true
      )
      private void onInventoryClick(InventoryClickEvent event) {
         if (event.getInventory().equals(InventoryGui.this.getInventory(event.getWhoClicked()))) {
            int slot = -1;
            if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
               slot = event.getRawSlot();
            } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
               slot = event.getInventory().firstEmpty();
            }

            ItemStack originalCursor = event.getCursor() != null ? event.getCursor().clone() : null;
            GuiElement.Click click = InventoryGui.this.handleInteract(event, event.getClick(), slot, event.getCursor());
            if (click != null && (originalCursor == null || !originalCursor.equals(click.getCursor()))) {
               event.setCursor(click.getCursor());
            }
         } else if (InventoryGui.this.hasRealOwner() && InventoryGui.this.owner.equals(event.getInventory().getHolder())) {
            InventoryGui.this.runTask(InventoryGui.this::draw);
         }
      }

      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.MONITOR
      )
      public void onInventoryDrag(InventoryDragEvent event) {
         Inventory inventory = InventoryGui.this.getInventory(event.getWhoClicked());
         if (event.getInventory().equals(inventory)) {
            if (event.getRawSlots().size() == 1) {
               int slot = (Integer)event.getRawSlots().iterator().next();
               if (slot < event.getView().getTopInventory().getSize()) {
                  GuiElement.Click click = InventoryGui.this.handleInteract(
                     event, event.getType() == DragType.SINGLE ? ClickType.RIGHT : ClickType.LEFT, slot, event.getOldCursor()
                  );
                  if (click != null && !event.getOldCursor().equals(click.getCursor())) {
                     event.setCursor(click.getCursor());
                  }
               }

               return;
            }

            int rest = 0;
            Map<Integer, ItemStack> resetSlots = new HashMap<>();

            for (Entry<Integer, ItemStack> items : event.getNewItems().entrySet()) {
               if (items.getKey() < inventory.getSize()) {
                  GuiElement element = InventoryGui.this.getElement(items.getKey());
                  if (!(element instanceof GuiStorageElement)
                     || !((GuiStorageElement)element).setStorageItem(event.getWhoClicked(), items.getKey(), items.getValue())) {
                     ItemStack slotItem = event.getInventory().getItem(items.getKey());
                     if (!items.getValue().isSimilar(slotItem)) {
                        rest += items.getValue().getAmount();
                     } else if (slotItem != null) {
                        rest += items.getValue().getAmount() - slotItem.getAmount();
                     }

                     resetSlots.put(items.getKey(), event.getInventory().getItem(items.getKey()));
                  }
               }
            }

            InventoryGui.this.runTask(event.getWhoClicked(), () -> {
               for (Entry<Integer, ItemStack> itemsx : resetSlots.entrySet()) {
                  event.getView().getTopInventory().setItem(itemsx.getKey(), itemsx.getValue());
               }
            });
            if (rest > 0) {
               int cursorAmount = event.getCursor() != null ? event.getCursor().getAmount() : 0;
               if (!event.getOldCursor().isSimilar(event.getCursor())) {
                  event.setCursor(event.getOldCursor());
                  cursorAmount = 0;
               }

               int newCursorAmount = cursorAmount + rest;
               if (newCursorAmount <= event.getCursor().getMaxStackSize()) {
                  event.getCursor().setAmount(newCursorAmount);
               } else {
                  event.getCursor().setAmount(event.getCursor().getMaxStackSize());
                  ItemStack add = event.getCursor().clone();
                  int addAmount = newCursorAmount - event.getCursor().getMaxStackSize();
                  if (addAmount > 0) {
                     add.setAmount(addAmount);

                     for (ItemStack drop : event.getWhoClicked().getInventory().addItem(new ItemStack[]{add}).values()) {
                        event.getWhoClicked().getLocation().getWorld().dropItem(event.getWhoClicked().getLocation(), drop);
                     }
                  }
               }
            }
         }
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onInventoryClose(InventoryCloseEvent event) {
         Inventory inventory = InventoryGui.this.getInventory(event.getPlayer());
         if (event.getInventory().equals(inventory)) {
            if (InventoryGui.this.equals(InventoryGui.getOpen(event.getPlayer()))) {
               if (InventoryGui.this.closeAction != null
                  && !InventoryGui.this.closeAction.onClose(new InventoryGui.Close(event.getPlayer(), InventoryGui.this, event))) {
                  InventoryGui.clearHistory(event.getPlayer());
               } else {
                  InventoryGui.goBack(event.getPlayer());
               }
            }

            if (InventoryGui.this.inventories.size() <= 1) {
               InventoryGui.this.destroy(false);
            } else {
               inventory.clear();

               for (HumanEntity viewer : new ArrayList(inventory.getViewers())) {
                  if (viewer != event.getPlayer()) {
                     viewer.closeInventory();
                  }
               }

               InventoryGui.this.inventories.remove(event.getPlayer().getUniqueId());
               InventoryGui.this.pageAmounts.remove(event.getPlayer().getUniqueId());
               InventoryGui.this.pageNumbers.remove(event.getPlayer().getUniqueId());

               for (GuiElement element : InventoryGui.this.getElements()) {
                  if (element instanceof DynamicGuiElement) {
                     ((DynamicGuiElement)element).removeCachedElement(event.getPlayer());
                  }
               }
            }
         }
      }

      @EventHandler(
         priority = EventPriority.MONITOR,
         ignoreCancelled = true
      )
      public void onInventoryMoveItem(InventoryMoveItemEvent event) {
         if (InventoryGui.this.hasRealOwner()
            && (InventoryGui.this.owner.equals(event.getDestination().getHolder()) || InventoryGui.this.owner.equals(event.getSource().getHolder()))) {
            InventoryGui.this.runTask(InventoryGui.this::draw);
         }
      }

      @EventHandler(
         priority = EventPriority.MONITOR,
         ignoreCancelled = true
      )
      public void onDispense(BlockDispenseEvent event) {
         if (InventoryGui.this.hasRealOwner() && InventoryGui.this.owner.equals(event.getBlock().getState())) {
            InventoryGui.this.runTask(InventoryGui.this::draw);
         }
      }

      @EventHandler(
         priority = EventPriority.MONITOR,
         ignoreCancelled = true
      )
      public void onBlockBreak(BlockBreakEvent event) {
         if (InventoryGui.this.hasRealOwner() && InventoryGui.this.owner.equals(event.getBlock().getState())) {
            InventoryGui.this.destroy();
         }
      }

      @EventHandler(
         priority = EventPriority.MONITOR,
         ignoreCancelled = true
      )
      public void onEntityDeath(EntityDeathEvent event) {
         if (InventoryGui.this.hasRealOwner() && InventoryGui.this.owner.equals(event.getEntity())) {
            InventoryGui.this.destroy();
         }
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (event.getPlugin() == InventoryGui.this.plugin) {
            InventoryGui.this.destroy();
         }
      }

      protected class ItemSwapGuiListener extends InventoryGui.OptionalListener {
         @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
         )
         public void onInventoryMoveItem(PlayerSwapHandItemsEvent event) {
            Inventory inventory = InventoryGui.this.getInventory(event.getPlayer());
            if (event.getPlayer().getOpenInventory().getTopInventory().equals(inventory)) {
               event.setCancelled(true);
            }
         }
      }
   }

   public static class Holder implements InventoryHolder {
      private InventoryGui gui;

      public Holder(InventoryGui gui) {
         this.gui = gui;
      }

      public Inventory getInventory() {
         return this.gui.getInventory();
      }

      public InventoryGui getGui() {
         return this.gui;
      }
   }

   public static class InventoryCreator {
      private final InventoryGui.InventoryCreator.CreatorImplementation<InventoryType> typeCreator;
      private final InventoryGui.InventoryCreator.CreatorImplementation<Integer> sizeCreator;

      public InventoryCreator(
         InventoryGui.InventoryCreator.CreatorImplementation<InventoryType> typeCreator,
         InventoryGui.InventoryCreator.CreatorImplementation<Integer> sizeCreator
      ) {
         this.typeCreator = typeCreator;
         this.sizeCreator = sizeCreator;
      }

      public InventoryGui.InventoryCreator.CreatorImplementation<InventoryType> getTypeCreator() {
         return this.typeCreator;
      }

      public InventoryGui.InventoryCreator.CreatorImplementation<Integer> getSizeCreator() {
         return this.sizeCreator;
      }

      public interface CreatorImplementation<T> {
         Inventory create(InventoryGui var1, HumanEntity var2, T var3);
      }
   }

   private abstract class OptionalListener extends InventoryGui.UnregisterableListener {
      private OptionalListener() {
      }

      private boolean isCompatible() {
         try {
            this.getClass().getMethods();
            this.getClass().getDeclaredMethods();
            return true;
         } catch (NoClassDefFoundError var2) {
            return false;
         }
      }
   }

   private abstract class UnregisterableListener implements Listener {
      private final List<InventoryGui.UnregisterableListener> listeners;
      private boolean listenersRegistered = false;

      private UnregisterableListener() {
         List<InventoryGui.UnregisterableListener> listeners = new ArrayList<>();

         for (Class<?> innerClass : this.getClass().getDeclaredClasses()) {
            if (InventoryGui.UnregisterableListener.class.isAssignableFrom(innerClass)) {
               try {
                  InventoryGui.UnregisterableListener listener = (InventoryGui.UnregisterableListener)innerClass.getDeclaredConstructor(this.getClass())
                     .newInstance(this);
                  if (!(listener instanceof InventoryGui.OptionalListener) || ((InventoryGui.OptionalListener)listener).isCompatible()) {
                     listeners.add(listener);
                  }
               } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException var8) {
                  var8.printStackTrace();
               }
            }
         }

         this.listeners = Collections.unmodifiableList(listeners);
      }

      protected void registerListeners() {
         if (!this.listenersRegistered) {
            InventoryGui.this.plugin.getServer().getPluginManager().registerEvents(this, InventoryGui.this.plugin);

            for (InventoryGui.UnregisterableListener listener : this.listeners) {
               listener.registerListeners();
            }

            this.listenersRegistered = true;
         }
      }

      protected void unregisterListeners() {
         HandlerList.unregisterAll(this);

         for (InventoryGui.UnregisterableListener listener : this.listeners) {
            listener.unregisterListeners();
         }

         this.listenersRegistered = false;
      }
   }
}
