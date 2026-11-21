package com.artillexstudios.axafkzone.libs.axapi.gui.configuration;

import com.artillexstudios.axafkzone.libs.axapi.config.adapters.MapConfigurationGetter;
import com.artillexstudios.axafkzone.libs.axapi.context.HashMapContext;
import com.artillexstudios.axafkzone.libs.axapi.gui.configuration.actions.Action;
import com.artillexstudios.axafkzone.libs.axapi.gui.configuration.actions.Actions;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.Gui;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.GuiItem;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.GuiKeys;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.implementation.PaginatedGui;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.provider.GuiItemProvider;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.provider.implementation.CachingGuiItemProvider;
import com.artillexstudios.axafkzone.libs.axapi.placeholders.PlaceholderParameters;
import com.artillexstudios.axafkzone.libs.axapi.utils.ItemBuilder;
import com.artillexstudios.axafkzone.libs.axapi.utils.UncheckedUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axafkzone.libs.axapi.utils.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ConfigurationBackedGui<T extends Gui> {
   private static final Pattern SPLIT_PATTERN = Pattern.compile("-");
   private final ConfigurationBackedGuiBuilder builder;

   public ConfigurationBackedGui(ConfigurationBackedGuiBuilder builder) {
      this.builder = builder;
   }

   private void setItems(T gui, HashMapContext globalContext) {
      List<MapConfigurationGetter> items = this.builder.configuration().getList("items", MapConfigurationGetter::new);
      if (items == null) {
         items = new ArrayList<>();
         if (this.builder.supportsLegacy()) {
            for (Object key : this.builder.configuration().getKeys()) {
               String keyString = key.toString();
               if (!this.builder.overrideItems().containsKey(keyString) && !this.builder.ignoredKeys().contains(keyString)) {
                  items.add(this.builder.configuration().getConfiguration(keyString));
               }
            }
         }
      }

      for (MapConfigurationGetter item : items) {
         Object slotConfig = item.getObject("slots");
         if (slotConfig == null) {
            LogUtils.warn("Failed to add item: {} to the gui, because it didn't have any slots set up!", item);
         } else {
            IntList slots = this.slots(slotConfig);
            List<Action<?>> actions = Actions.INSTANCE.parseAll(item.getList("actions"));
            GuiItemProvider provider = new CachingGuiItemProvider(
               new GuiItem(ctx -> ItemBuilder.create(item, new PlaceholderParameters(ctx)).wrapped(), (context, event) -> {
                  context.merge(globalContext);

                  for (Action<?> action : actions) {
                     action.run((Player)event.getWhoClicked(), context);
                  }
               })
            );
            slots.intIterator().forEachRemaining(integer -> gui.setItem(integer, provider));
         }
      }

      for (Entry<String, Consumer<InventoryClickEvent>> entry : this.builder.overrideItems().entrySet()) {
         MapConfigurationGetter itemx = this.builder.configuration().getConfiguration(entry.getKey());
         if (itemx == null) {
            if (FeatureFlags.STRICT_ITEM_OVERRIDE_HANDLING.get()) {
               LogUtils.error(
                  "Could not find item within section {} in file! Did you remove it? This is not allowed! Please regenerate the file!", entry.getKey()
               );
               throw new IllegalStateException();
            }

            return;
         }

         Object slotConfig = itemx.getObject("slots");
         if (slotConfig == null) {
            LogUtils.warn("Failed to add item: {} to the gui, because it didn't have any slots set up!", itemx);
         } else {
            IntList slots = this.slots(slotConfig);
            List<Action<?>> actions = Actions.INSTANCE.parseAll(itemx.getList("actions"));
            GuiItemProvider provider = new CachingGuiItemProvider(
               new GuiItem(ctx -> ItemBuilder.create(item, new PlaceholderParameters(ctx)).wrapped(), (context, event) -> {
                  context.merge(globalContext);

                  for (Action<?> action : actions) {
                     action.run((Player)event.getWhoClicked(), context);
                  }

                  entry.getValue().accept(event);
               })
            );
            slots.intIterator().forEachRemaining(integer -> gui.setItem(integer, provider));
         }
      }
   }

   public IntList slots(Object slots) {
      return (IntList)(switch (slots) {
         case Number number -> IntList.of(number.intValue());
         case String str -> this.parseSlot(str);
         case List<?> list -> {
            IntList newList = new IntArrayList(list.size());

            for (Object o : list) {
               newList.addAll(this.slots(o));
            }

            yield newList;
         }
         default -> IntList.of();
      });
   }

   public ConfigurationBackedGuiBuilder builder() {
      return this.builder;
   }

   private IntList parseSlot(String str) {
      if (!str.contains("-")) {
         return IntList.of(Integer.parseInt(str));
      } else {
         IntList list = new IntArrayList();
         String[] arr = SPLIT_PATTERN.split(str);
         int from = Integer.parseInt(arr[0]);
         int to = Integer.parseInt(arr[1]);
         int min = Math.min(from, to);
         int max = Math.max(from, to);

         for (int i = min; i <= max; i++) {
            list.add(i);
         }

         return list;
      }
   }

   public synchronized T create(Player player, HashMapContext globalContext) throws IllegalStateException {
      this.builder.guiBuilder().context(globalContext);
      T gui = UncheckedUtils.unsafeCast(this.builder.guiBuilder().build(player));
      if (gui instanceof PaginatedGui paginatedGui && this.builder.objectProvider() != null) {
         paginatedGui.setItemsProvider(this.builder.objectProvider().apply(globalContext));
      }

      gui.disableAllInteractions();

      try {
         this.setItems(gui, globalContext);
      } catch (IllegalStateException var5) {
         return null;
      }

      if (this.builder.inventoryCloseListener() != null) {
         gui.onClose(this.builder.inventoryCloseListener());
      }

      if (this.builder.inventoryOpenListener() != null) {
         gui.onOpen(this.builder.inventoryOpenListener());
      }

      if (this.builder.playerInventoryClickListener() != null) {
         gui.onPlayerInventoryClick(this.builder.playerInventoryClickListener());
      }

      if (this.builder.refreshInterval() != null) {
         gui.setRefreshInterval(this.builder.refreshInterval());
      }

      return gui;
   }

   public T create(Player player) throws IllegalStateException {
      return this.create(player, HashMapContext.create().with(GuiKeys.PLAYER, player));
   }
}
