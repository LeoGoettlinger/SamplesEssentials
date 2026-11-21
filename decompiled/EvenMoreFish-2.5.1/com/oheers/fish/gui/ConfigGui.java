package com.oheers.fish.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.GuiFillerConfig;
import com.oheers.fish.config.gui.GuiConfig;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.utils.ItemUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.inventorygui.GuiElement;
import org.evenmorefish.fish.libs.inventorygui.GuiPageElement;
import org.evenmorefish.fish.libs.inventorygui.GuiStorageElement;
import org.evenmorefish.fish.libs.inventorygui.InventoryGui;
import org.evenmorefish.fish.libs.inventorygui.StaticGuiElement;
import org.jspecify.annotations.NonNull;

public class ConfigGui {
   protected final Map<String, BiConsumer<ConfigGui, GuiElement.Click>> actions = GuiUtils.getActionMap();
   protected final GuiConfig config;
   protected final Player player;
   @NonNull
   private final Map<String, EMFMessage> replacements = new HashMap<>();
   private InventoryGui gui;
   private InventoryGui.CloseAction closeAction = null;

   public ConfigGui(@NonNull GuiConfig config, @NonNull HumanEntity human) {
      this.config = config;
      if (human instanceof Player p) {
         this.player = p;
      } else {
         throw new UnsupportedOperationException("Cannot open ConfigGui for a non-player.");
      }
   }

   public void addReplacement(@NonNull String variable, @NonNull String replacement) {
      this.replacements.put(variable, EMFSingleMessage.fromString(replacement));
   }

   public void addReplacement(@NonNull String variable, @NonNull Component replacement) {
      this.replacements.put(variable, EMFSingleMessage.of(replacement));
   }

   public void addReplacement(@NonNull String variable, @NonNull EMFSingleMessage replacement) {
      this.replacements.put(variable, replacement);
   }

   public void addReplacements(@NonNull Map<String, EMFSingleMessage> replacements) {
      this.replacements.putAll(replacements);
   }

   public void setCloseAction(@NonNull InventoryGui.CloseAction closeAction) {
      this.closeAction = closeAction;
   }

   @NonNull
   public InventoryGui getGui() {
      if (this.gui == null) {
         throw new IllegalStateException("ConfigGui#createGui has not been called!");
      } else {
         return this.gui;
      }
   }

   public Section getGuiConfig() {
      return this.config.getConfig();
   }

   public void open() {
      this.getGui().show(this.player);
   }

   public void createGui() {
      InventoryGui gui = new InventoryGui(EvenMoreFish.getInstance(), this.config.getTitle().getLegacyMessage(this.player), this.config.getLayout());
      this.loadFiller(gui, this.config.getConfig());
      this.loadItems(gui, this.config.getConfig());
      gui.setCloseAction(this.closeAction);
      this.gui = gui;
   }

   private void loadFiller(@NonNull InventoryGui gui, @NonNull Section config) {
      String fillerStr = config.getString("filler");
      if (fillerStr != null) {
         Material filler = ItemUtils.getMaterial(fillerStr);
         if (filler != null) {
            ItemStack item = new ItemStack(filler);
            item.editMeta(meta -> meta.displayName(Component.empty()));
            gui.setFiller(item);
            gui.addElements(GuiFillerConfig.getInstance().getDefaultFillerItems(this));
         }
      }
   }

   private void loadItems(@NonNull InventoryGui gui, @NonNull Section config) {
      config.getRoutesAsStrings(false).forEach(key -> {
         Section itemSection = config.getSection(key);
         if (itemSection != null && itemSection.contains("item")) {
            this.addGuiItem(gui, itemSection);
         }
      });
   }

   protected void addGuiItem(@NonNull InventoryGui gui, @NonNull Section itemSection) {
      char character = FishUtils.getCharFromString(itemSection.getString("character", "#"), '#');
      if (character != '#') {
         ItemFactory factory = ItemFactory.itemFactory(itemSection);
         ItemStack item = factory.createItem(this.player.getUniqueId(), this.replacements);
         if (!item.isEmpty()) {
            GuiPageElement.PageAction pageAction = FishUtils.getEnumValue(GuiPageElement.PageAction.class, itemSection.getString("page-action"));
            if (pageAction != null) {
               gui.addElement(this.fetchPageElement(character, item, itemSection, pageAction));
            } else {
               StaticGuiElement actionElement = new StaticGuiElement(character, item, click -> {
                  this.executeClickAction(itemSection, click);
                  this.executeClickCommands(itemSection, click);
                  return true;
               });
               gui.addElement(actionElement);
            }
         }
      }
   }

   private GuiPageElement fetchPageElement(char character, @NonNull ItemStack item, @NonNull Section section, @NonNull GuiPageElement.PageAction pageAction) {
      ItemFactory factory = ItemFactory.itemFactory(section, null, "fallback-item");
      ItemStack fallbackItem = factory.isUsingFallbackBaseItem() ? null : factory.createItem(this.player.getUniqueId(), this.replacements);
      return new EMFGuiPageElement(character, item, fallbackItem, pageAction);
   }

   private void executeClickAction(@NonNull Section section, @NonNull GuiElement.Click click) {
      Section actionSection = section.getSection("click-action");
      if (actionSection == null) {
         BiConsumer<ConfigGui, GuiElement.Click> action = this.actions.get(section.getString("click-action", ""));
         if (action != null) {
            action.accept(this, click);
         }
      } else {
         BiConsumer<ConfigGui, GuiElement.Click> action = switch (click.getType()) {
            case LEFT -> (BiConsumer)this.actions.get(actionSection.getString("left", ""));
            case RIGHT -> (BiConsumer)this.actions.get(actionSection.getString("right", ""));
            case MIDDLE -> (BiConsumer)this.actions.get(actionSection.getString("middle", ""));
            case DROP -> (BiConsumer)this.actions.get(actionSection.getString("drop", ""));
            default -> null;
         };
         if (action != null) {
            action.accept(this, click);
         }
      }
   }

   private void executeClickCommands(@NonNull Section section, @NonNull GuiElement.Click click) {
      HumanEntity player = click.getWhoClicked();

      for (String command : section.getStringList("click-commands")) {
         Bukkit.dispatchCommand(player, command);
      }
   }

   public void doRescue() {
      this.gui.getElements().forEach(element -> {
         if (element instanceof GuiStorageElement storageElement) {
            Inventory inv = storageElement.getStorage();
            if (!inv.isEmpty()) {
               FishUtils.giveItems(inv.getStorageContents(), this.player);
               inv.clear();
            }
         }
      });
   }
}
