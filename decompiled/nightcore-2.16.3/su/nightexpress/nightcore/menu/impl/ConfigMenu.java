package su.nightexpress.nightcore.menu.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.event.inventory.InventoryType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightCorePlugin;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.api.AutoFilled;
import su.nightexpress.nightcore.menu.click.ClickAction;
import su.nightexpress.nightcore.menu.click.ClickType;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.util.Enums;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.text.night.NightMessage;

@Deprecated
public abstract class ConfigMenu<P extends NightCorePlugin> extends AbstractMenu<P> {
   protected static final String DEFAULT_ITEM_SECTION = "Content";
   protected final FileConfig cfg;
   protected final Map<String, ItemHandler> handlerMap;
   protected String itemSection;
   protected boolean applyPAPI;

   public ConfigMenu(@NonNull P plugin, @NonNull FileConfig config) {
      super(plugin);
      this.cfg = config;
      this.handlerMap = new HashMap<>();
      this.itemSection = "Content";
      this.addHandler(ItemHandler.forClose(this));
      if (this instanceof AutoFilled) {
         this.addHandler(ItemHandler.forNextPage(this));
         this.addHandler(ItemHandler.forPreviousPage(this));
      }
   }

   @NonNull
   protected abstract MenuOptions createDefaultOptions();

   @NonNull
   protected abstract List<MenuItem> createDefaultItems();

   public void load() {
      this.loadConfig();
   }

   protected abstract void loadAdditional();

   public void loadConfig() {
      MenuOptions defaultOptions = this.createDefaultOptions();
      String title = ConfigValue.create("Settings.Title", defaultOptions.getTitle(), "GUI title.").read(this.cfg);
      int size = ConfigValue.create(
            "Settings.Size",
            defaultOptions.getSize(),
            "GUI size. Must be multiply of 9.",
            "Useful for '" + InventoryType.CHEST.name() + "' Inventory Type only."
         )
         .read(this.cfg);
      InventoryType type = (InventoryType)ConfigValue.create(
            "Settings.Inventory_Type",
            InventoryType.class,
            defaultOptions.getType(),
            "GUI type.",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/inventory/InventoryType.html"
         )
         .read(this.cfg);
      int autoRefresh = ConfigValue.create(
            "Settings.Auto_Refresh", defaultOptions.getAutoRefresh(), "Sets GUI auto-refresh interval (in seconds). Set this to 0 to disable."
         )
         .read(this.cfg);
      boolean applyPAPI = ConfigValue.create(
            "Settings.PlaceholderAPI.Enabled",
            this.applyPAPI,
            "Sets whether PlaceholderAPI placeholders will be applied on all items from the '" + this.itemSection + "' section of this GUI.",
            "[*] Disable if you don't use any PlaceholderAPI placeholders on your items to improve GUI performance."
         )
         .read(this.cfg);
      this.getOptions().setTitle(NightMessage.asLegacy(title));
      this.getOptions().setSize(size);
      this.getOptions().setType(type);
      this.getOptions().setAutoRefresh(autoRefresh);
      this.loadAdditional();
      if (!this.cfg.contains(this.itemSection)) {
         this.createDefaultItems().forEach(menuItem -> {
            AtomicInteger count = new AtomicInteger();
            String raw = ItemUtil.getItemName(menuItem.getItemStack());
            String name = StringUtil.lowerCaseUnderscoreStrict(NightMessage.stripTags(raw));
            String finalName = name;

            while (this.cfg.contains(this.itemSection + "." + finalName)) {
               finalName = name + "_" + count.incrementAndGet();
            }

            this.writeItem(menuItem, this.itemSection + "." + finalName);
         });
      }

      this.cfg.getSection(this.itemSection).forEach(sId -> {
         MenuItem menuItem = this.readItem(this.itemSection + "." + sId);
         this.addItem(menuItem);
         if (applyPAPI) {
            menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> ItemReplacer.replacePlaceholderAPI(itemStack, viewer.getPlayer()));
         }
      });
      List<String> comments = new ArrayList<>();
      comments.add("=".repeat(20) + " GUI CONTENT " + "=".repeat(20));
      comments.add("You can freely edit items in this section as you wish (add, remove, modify items).");
      comments.add("The following values are available as button Types:");
      comments.addAll(this.handlerMap.keySet().stream().map(String::toUpperCase).sorted(String::compareTo).toList());
      comments.add("=".repeat(20) + " ITEM OPTIONS " + "=".repeat(20));
      comments.add("> Item: Item to display. Please check: https://nightexpressdev.com/nightcore/configuration/item-formation/");
      comments.add("> Priority: Button priority. Better values will override other item(s) in the same slot(s).");
      comments.add("> Slots: Button slots. From [0] to [Size - 1]. Split with commas.");
      comments.add("> Click_Commands: Execute custom commands on click. PlaceholderAPI available here.");
      comments.add("    Available click types: " + Enums.inline(ClickType.class));
      comments.add("    Use prefix 'player:' to run command by a player.");
      comments.add("    Click_Commands:");
      comments.add("      LEFT:");
      comments.add("      - say Hello");
      comments.add("      - give %player_name% diamond 1");
      comments.add("      - player: menu open shops");
      comments.add("=".repeat(50));
      this.cfg.setComments(this.itemSection, comments);
      this.cfg.saveChanges();
   }

   @Override
   public void clear() {
      super.clear();
      this.handlerMap.clear();
   }

   public void addHandler(@NonNull ItemHandler handler) {
      this.handlerMap.put(handler.getName(), handler);
   }

   public void addHandler(@NonNull String name, @NonNull ClickAction action) {
      this.addHandler(new ItemHandler(name, action));
   }

   @Nullable
   public ItemHandler getHandler(@NonNull String name) {
      return this.handlerMap.get(name.toLowerCase());
   }

   public boolean removeHandler(@NonNull String name) {
      return this.handlerMap.remove(name.toLowerCase()) != null;
   }

   @NonNull
   protected MenuItem readItem(@NonNull String path) {
      String handlerName = this.cfg.getString(path + ".Type", "default");
      NightItem item = this.cfg.getCosmeticItem(path + ".Item");
      int[] slots = this.cfg.getIntArray(path + ".Slots");
      int priority = this.cfg.getInt(path + ".Priority");
      MenuItem menuItem = new MenuItem(item).setPriority(priority).setSlots(slots);
      ItemHandler handler = this.getHandler(handlerName);
      if (handler != null) {
         menuItem.setHandler(handler);
      }

      if (this.cfg.contains(path + ".Click_Commands")) {
         Map<ClickType, List<String>> commandMap = new HashMap<>();

         for (String sType : this.cfg.getSection(path + ".Click_Commands")) {
            ClickType clickType = StringUtil.getEnum(sType, ClickType.class).orElse(null);
            if (clickType != null) {
               List<String> commands = this.cfg.getStringList(path + ".Click_Commands." + sType);
               if (!commands.isEmpty()) {
                  commandMap.put(clickType, commands);
               }
            }
         }

         ClickAction clickCommands = (viewer, event) -> {
            List<String> commandsx = commandMap.getOrDefault(ClickType.from(event), Collections.emptyList());
            commandsx.forEach(command -> Players.dispatchCommand(viewer.getPlayer(), command));
         };
         menuItem.addClick(clickCommands);
      }

      return menuItem;
   }

   protected void writeItem(@NonNull MenuItem menuItem, @NonNull String path) {
      this.cfg.set(path + ".Priority", menuItem.getPriority());
      this.cfg.setItem(path + ".Item", menuItem.getItemStack());
      this.cfg.setIntArray(path + ".Slots", menuItem.getSlots());
      this.cfg.set(path + ".Type", menuItem.getHandler().getName());
   }
}
