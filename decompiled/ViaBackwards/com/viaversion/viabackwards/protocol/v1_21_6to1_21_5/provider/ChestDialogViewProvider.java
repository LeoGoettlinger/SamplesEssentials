package com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.provider;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viabackwards.api.DialogStyleConfig;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.Protocol1_21_6To1_21_5;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.Button;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.Dialog;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.input.BooleanInput;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.input.NumberRangeInput;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.input.SingleOptionInput;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.input.TextInput;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.widget.ItemWidget;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.widget.TextWidget;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.widget.Widget;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.storage.ChestDialogStorage;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.storage.ClickEvents;
import com.viaversion.viabackwards.utils.ChatUtil;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.minecraft.item.data.TooltipDisplay;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.libs.fastutil.ints.IntSortedSets;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.MathUtil;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ChestDialogViewProvider implements DialogViewProvider {
   private static final int INVENTORY_SIZE = 27;
   private static final int INVENTORY_LAST_ROW = 18;
   private final Protocol1_21_6To1_21_5 protocol;

   public ChestDialogViewProvider(Protocol1_21_6To1_21_5 protocol) {
      this.protocol = protocol;
   }

   @Override
   public void openDialog(UserConnection connection, Dialog dialog) {
      State state = connection.getProtocolInfo().getClientState();
      if (state != State.CONFIGURATION) {
         List<Tag> texts = new ArrayList<>();

         for (Widget widget : new ArrayList<>(dialog.widgets())) {
            if (widget instanceof TextWidget textWidget) {
               texts.add(textWidget.label());
               dialog.widgets().remove(textWidget);
            } else if (!texts.isEmpty()) {
               dialog.widgets().add(dialog.widgets().indexOf(widget), new ChestDialogViewProvider.MultiTextWidget(texts.toArray(Tag[]::new)));
               texts.clear();
            }
         }

         if (!texts.isEmpty()) {
            dialog.widgets().add(new ChestDialogViewProvider.MultiTextWidget(texts.toArray(Tag[]::new)));
            texts.clear();
         }

         ChestDialogStorage previousStorage = (ChestDialogStorage)connection.get(ChestDialogStorage.class);
         ChestDialogStorage storage = new ChestDialogStorage(this, dialog);
         if (previousStorage != null) {
            storage.setPreviousDialog(previousStorage.dialog());
         }

         connection.put(storage);
         this.openChestView(connection, storage, ChestDialogStorage.Phase.DIALOG_VIEW);
      }
   }

   public void openChestView(UserConnection connection, ChestDialogStorage storage, ChestDialogStorage.Phase phase) {
      storage.setPhase(connection, phase);
      PacketWrapper openScreen = PacketWrapper.create(ClientboundPackets1_21_5.OPEN_SCREEN, connection);
      openScreen.write(Types.VAR_INT, storage.containerId());
      openScreen.write(Types.VAR_INT, 2);
      openScreen.write(Types.TRUSTED_TAG, this.handleTag(connection, storage.dialog().title()));
      openScreen.send(Protocol1_21_6To1_21_5.class);
      this.updateDialog(connection, storage.dialog());
   }

   @Override
   public void closeDialog(UserConnection connection) {
      State state = connection.getProtocolInfo().getClientState();
      if (state != State.CONFIGURATION) {
         ChestDialogStorage storage = (ChestDialogStorage)connection.get(ChestDialogStorage.class);
         if (storage != null) {
            PacketWrapper containerClose = PacketWrapper.create(ClientboundPackets1_21_5.CONTAINER_CLOSE, connection);
            containerClose.write(Types.VAR_INT, storage.containerId());
            containerClose.send(Protocol1_21_6To1_21_5.class);
            if (storage.previousDialog() != null) {
               this.openDialog(connection, storage.previousDialog());
            } else {
               connection.remove(ChestDialogStorage.class);
            }
         }
      }
   }

   public boolean clickDialog(UserConnection connection, int container, int slot, byte mouse, int mode) {
      ChestDialogStorage storage = (ChestDialogStorage)connection.get(ChestDialogStorage.class);
      if (storage == null || storage.containerId() != container) {
         return false;
      } else if (mode != 0 || slot < 0 || slot >= 27) {
         this.updateDialog(connection, storage.dialog());
         return true;
      } else if (storage.phase() == ChestDialogStorage.Phase.ANVIL_VIEW) {
         this.openChestView(connection, storage, ChestDialogStorage.Phase.DIALOG_VIEW);
         return true;
      } else if (storage.phase() != ChestDialogStorage.Phase.WAITING_FOR_RESPONSE) {
         if (slot == 26) {
            int pages = MathUtil.ceil(storage.items().length / 18.0F);
            if (mouse == 0) {
               storage.page++;
            } else if (mouse == 1) {
               storage.page--;
            }

            storage.page = MathUtil.clamp(storage.page, 0, pages - 1);
         }

         slot += storage.page * 18;
         List<Widget> widgets = storage.dialog().widgets();
         if (slot < widgets.size()) {
            Widget widget = widgets.get(slot);
            if (widget instanceof BooleanInput booleanInput) {
               this.clickBooleanInput(booleanInput);
            } else if (widget instanceof NumberRangeInput numberRangeInput) {
               this.clickNumberRangeInput(numberRangeInput, mouse);
            } else if (widget instanceof TextInput textInput) {
               this.clickTextInput(connection, textInput);
            } else if (widget instanceof SingleOptionInput singleOptionInput) {
               this.clickSingleOptionInput(singleOptionInput);
            } else if (widget instanceof Button button) {
               this.clickButton(connection, storage.dialog().afterAction(), button);
            } else if (widget instanceof Dialog dialog) {
               this.clickDialogButton(connection, dialog);
            }
         }

         if (slot == storage.confirmationYesIndex()) {
            Button yesButton = storage.dialog().yesButton();
            if (yesButton != null) {
               this.clickButton(connection, storage.dialog().afterAction(), yesButton);
            }
         }

         if (slot == storage.confirmationNoIndex()) {
            Button noButton = storage.dialog().noButton();
            if (noButton != null) {
               this.clickButton(connection, storage.dialog().afterAction(), noButton);
            }
         }

         if (slot == storage.actionIndex()) {
            Button actionButton = storage.dialog().actionButton();
            if (actionButton != null) {
               this.clickButton(connection, storage.dialog().afterAction(), actionButton);
            }
         }

         ChestDialogStorage currentStorage = (ChestDialogStorage)connection.get(ChestDialogStorage.class);
         if (currentStorage == storage && connection.has(ChestDialogStorage.class) && storage.phase() == ChestDialogStorage.Phase.DIALOG_VIEW) {
            this.updateDialog(connection, storage.dialog());
         }

         return true;
      } else {
         if (slot == storage.actionIndex() && storage.closeButtonEnabled()) {
            this.closeDialog(connection);
         } else {
            this.updateDialog(connection, storage.dialog());
         }

         return true;
      }
   }

   public void updateDialog(UserConnection connection, Dialog dialog) {
      ChestDialogStorage storage = (ChestDialogStorage)connection.get(ChestDialogStorage.class);
      PacketWrapper containerSetContent = PacketWrapper.create(ClientboundPackets1_21_5.CONTAINER_SET_CONTENT, connection);
      containerSetContent.write(Types.VAR_INT, storage.containerId());
      containerSetContent.write(Types.VAR_INT, 0);
      containerSetContent.write(VersionedTypes.V1_21_5.itemArray, this.getItems(connection, storage, dialog));
      containerSetContent.write(VersionedTypes.V1_21_5.item, StructuredItem.empty());
      containerSetContent.send(Protocol1_21_6To1_21_5.class);
   }

   protected Item createPageNavigationItem() {
      DialogStyleConfig config = ViaBackwards.getConfig().dialogStyleConfig();
      return this.createItem("minecraft:arrow", ChatUtil.translate(config.pageNavigationTitle()), config.pageNavigationNext(), config.pageNavigationPrevious());
   }

   protected Item createActionButtonItem(UserConnection connection, Button button) {
      Tag label = this.handleTag(connection, button.label());
      return button.tooltip() == null
         ? this.createItem("minecraft:oak_button", label)
         : this.createItem("minecraft:oak_button", label, this.handleTag(connection, button.tooltip()));
   }

   protected Item createCloseButtonItem(Tag label) {
      return this.createItem("minecraft:oak_button", label);
   }

   protected Item getItemWidget(UserConnection connection, ItemWidget itemWidget) {
      String identifier = itemWidget.item().getString("id");
      int count = itemWidget.item().getInt("count", 1);
      Tag label = ChatUtil.translate(Key.stripMinecraftNamespace(identifier));
      Item item = this.createItem(identifier, label);
      item.setAmount(count);
      if (itemWidget.description() != null) {
         item.dataContainer().set(StructuredDataKey.LORE, new Tag[]{this.handleTag(connection, ChatUtil.fixStyle(itemWidget.description().label()))});
      }

      if (!itemWidget.showTooltip()) {
         item.dataContainer().set(StructuredDataKey.TOOLTIP_DISPLAY, new TooltipDisplay(true, IntSortedSets.EMPTY_SET));
      }

      return item;
   }

   protected Item getMultiTextWidget(UserConnection connection, ChestDialogViewProvider.MultiTextWidget multiTextWidget) {
      List<Tag> lines = new ArrayList<>();

      for (Tag label : multiTextWidget.labels()) {
         Tag[] split = ChatUtil.split(ChatUtil.fixStyle(label), "\n");

         for (Tag line : split) {
            lines.add(this.handleTag(connection, line));
         }
      }

      Tag[] lore = new Tag[lines.size() - 1];

      for (int i = 1; i < lines.size(); i++) {
         lore[i - 1] = lines.get(i);
      }

      return this.createItem("minecraft:paper", lines.get(0), lore);
   }

   protected Item getBooleanInput(UserConnection connection, BooleanInput booleanInput) {
      DialogStyleConfig config = ViaBackwards.getConfig().dialogStyleConfig();
      String item = booleanInput.value() ? "minecraft:lime_dye" : "minecraft:gray_dye";
      Tag[] label = ChatUtil.split(booleanInput.label(), "\n");
      if (label.length == 1) {
         return this.createItem(item, this.handleTag(connection, booleanInput.label()), ChatUtil.translate(config.toggleValue()));
      } else {
         Tag[] lore = new Tag[label.length];

         for (int i = 1; i < label.length; i++) {
            lore[i - 1] = this.handleTag(connection, ChatUtil.fixStyle(label[i]));
         }

         lore[lore.length - 1] = ChatUtil.translate(config.toggleValue());
         return this.createItem(item, this.handleTag(connection, label[0]), lore);
      }
   }

   protected void clickBooleanInput(BooleanInput booleanInput) {
      booleanInput.setValue(!booleanInput.value());
   }

   protected Item getNumberRangeInput(UserConnection connection, NumberRangeInput numberRangeInput) {
      DialogStyleConfig config = ViaBackwards.getConfig().dialogStyleConfig();
      Tag label = this.handleTag(connection, numberRangeInput.displayName());
      return this.createItem(
         "minecraft:clock",
         label,
         String.format(config.increaseValue(), numberRangeInput.step()),
         String.format(config.decreaseValue(), numberRangeInput.step()),
         String.format(config.valueRange(), numberRangeInput.start(), numberRangeInput.end())
      );
   }

   protected void clickNumberRangeInput(NumberRangeInput numberRangeInput, int mouse) {
      float value = numberRangeInput.value();
      if (mouse == 0) {
         value += numberRangeInput.step();
      } else if (mouse == 1) {
         value -= numberRangeInput.step();
      }

      numberRangeInput.setClampedValue(value);
   }

   protected Item getTextInput(UserConnection connection, TextInput textInput) {
      DialogStyleConfig config = ViaBackwards.getConfig().dialogStyleConfig();
      Tag currentValue = ChatUtil.translate(String.format(config.currentValue(), textInput.value()));
      if (textInput.label() == null) {
         return this.createItem("minecraft:writable_book", currentValue);
      } else {
         Tag label = this.handleTag(connection, textInput.label());
         return this.createItem("minecraft:writable_book", label, currentValue, ChatUtil.translate(config.editValue()));
      }
   }

   protected void clickTextInput(UserConnection connection, TextInput textInput) {
      ChestDialogStorage storage = (ChestDialogStorage)connection.get(ChestDialogStorage.class);
      this.openAnvilView(connection, storage, ChatUtil.translate("§7Edit text"), textInput.value(), textInput);
   }

   protected Item getSingleOptionInput(UserConnection connection, SingleOptionInput singleOptionInput) {
      DialogStyleConfig config = ViaBackwards.getConfig().dialogStyleConfig();
      Tag displayName = singleOptionInput.options()[singleOptionInput.value()].computeDisplay();
      Tag label;
      if (singleOptionInput.label() != null) {
         label = ChatUtil.translate("options.generic_value", singleOptionInput.label(), displayName);
      } else {
         label = displayName;
      }

      return this.createItem("minecraft:bookshelf", this.handleTag(connection, label), config.nextOption(), config.previousOption());
   }

   protected void clickSingleOptionInput(SingleOptionInput singleOptionInput) {
      singleOptionInput.setClampedValue(singleOptionInput.value() + 1);
   }

   protected Item getButton(UserConnection connection, Button button) {
      return this.createItem("minecraft:oak_button", this.handleTag(connection, button.label()));
   }

   public void clickButton(UserConnection connection, Dialog.AfterAction afterAction, @Nullable Button button) {
      ChestDialogStorage storage = (ChestDialogStorage)connection.get(ChestDialogStorage.class);
      switch (afterAction) {
         case CLOSE:
            this.closeDialog(connection);
            break;
         case WAIT_FOR_RESPONSE:
            storage.setPhase(null, ChestDialogStorage.Phase.WAITING_FOR_RESPONSE);
      }

      if (button != null && button.clickEvent() != null) {
         CompoundTag clickEvent = button.clickEvent();
         String action = Key.stripMinecraftNamespace(clickEvent.getString("action"));
         switch (action) {
            case "open_url":
               String url = clickEvent.getString("url");
               this.openAnvilView(connection, storage, ChatUtil.translate("Open URL"), url, null);
               break;
            case "run_command":
               PacketWrapper chatCommand = PacketWrapper.create(ServerboundPackets1_21_6.CHAT_COMMAND, connection);
               String command = clickEvent.getString("command");
               if (command.startsWith("/")) {
                  command = command.substring(1);
               }

               chatCommand.write(Types.STRING, command);
               chatCommand.sendToServer(Protocol1_21_6To1_21_5.class);
               break;
            case "copy_to_clipboard":
               String value = clickEvent.getString("value");
               this.openAnvilView(connection, storage, ChatUtil.translate("Copy to clipboard"), value, null);
         }

         ClickEvents.handleClickEvent(connection, clickEvent);
      }
   }

   protected Item createTextInputItem(String value) {
      DialogStyleConfig config = ViaBackwards.getConfig().dialogStyleConfig();
      return this.createItem("minecraft:paper", ChatUtil.translate(value), config.setText());
   }

   protected Item createTextCopyItem(String value) {
      DialogStyleConfig config = ViaBackwards.getConfig().dialogStyleConfig();
      return this.createItem("minecraft:paper", ChatUtil.translate(value), config.close());
   }

   protected void openAnvilView(UserConnection connection, ChestDialogStorage storage, Tag title, String value, TextInput textInput) {
      storage.setPhase(connection, ChestDialogStorage.Phase.ANVIL_VIEW);
      PacketWrapper openScreen = PacketWrapper.create(ClientboundPackets1_21_5.OPEN_SCREEN, connection);
      openScreen.write(Types.VAR_INT, storage.containerId());
      openScreen.write(Types.VAR_INT, 8);
      openScreen.write(Types.TRUSTED_TAG, title);
      openScreen.send(Protocol1_21_6To1_21_5.class);
      Item[] items = new Item[]{textInput != null ? this.createTextInputItem(value) : this.createTextCopyItem(value)};
      storage.setCurrentTextInput(textInput);
      PacketWrapper containerSetContent = PacketWrapper.create(ClientboundPackets1_21_5.CONTAINER_SET_CONTENT, connection);
      containerSetContent.write(Types.VAR_INT, storage.containerId());
      containerSetContent.write(Types.VAR_INT, 0);
      containerSetContent.write(VersionedTypes.V1_21_5.itemArray, items);
      containerSetContent.write(VersionedTypes.V1_21_5.item, StructuredItem.empty());
      containerSetContent.send(Protocol1_21_6To1_21_5.class);
   }

   public void updateAnvilText(UserConnection connection, String value) {
      if (!value.isEmpty()) {
         ChestDialogStorage storage = (ChestDialogStorage)connection.get(ChestDialogStorage.class);
         if (storage.currentTextInput() != null) {
            storage.currentTextInput().setClampedValue(value);
         }
      }
   }

   protected Item getDialog(UserConnection connection, Dialog dialog) {
      Tag title = dialog.externalTitle() != null ? dialog.externalTitle() : dialog.title();
      return this.createItem("minecraft:command_block", this.handleTag(connection, title));
   }

   protected void clickDialogButton(UserConnection connection, Dialog dialog) {
      this.closeDialog(connection);
      this.openDialog(connection, dialog);
   }

   protected Item getItem(UserConnection connection, Widget widget) {
      if (widget instanceof ItemWidget itemWidget) {
         return this.getItemWidget(connection, itemWidget);
      } else if (widget instanceof ChestDialogViewProvider.MultiTextWidget multiTextWidget) {
         return this.getMultiTextWidget(connection, multiTextWidget);
      } else if (widget instanceof BooleanInput booleanInput) {
         return this.getBooleanInput(connection, booleanInput);
      } else if (widget instanceof NumberRangeInput numberRangeInput) {
         return this.getNumberRangeInput(connection, numberRangeInput);
      } else if (widget instanceof TextInput textInput) {
         return this.getTextInput(connection, textInput);
      } else if (widget instanceof SingleOptionInput singleOptionInput) {
         return this.getSingleOptionInput(connection, singleOptionInput);
      } else if (widget instanceof Button button) {
         return this.getButton(connection, button);
      } else if (widget instanceof Dialog dialog) {
         return this.getDialog(connection, dialog);
      } else {
         throw new IllegalArgumentException("Unknown widget type: " + widget.getClass().getName());
      }
   }

   protected Item[] getItems(UserConnection connection, ChestDialogStorage storage, Dialog dialog) {
      Item[] items = StructuredItem.emptyArray(27);
      int confirmationYesIndex = -1;
      int confirmationNoIndex = -1;
      int actionIndex = -1;
      if (storage.phase() == ChestDialogStorage.Phase.WAITING_FOR_RESPONSE) {
         int var12 = 13;
         items[var12] = this.createCloseButtonItem(storage.closeButtonLabel());
         storage.setItems(items, confirmationYesIndex, confirmationNoIndex, var12);
         return items;
      } else {
         List<Widget> widgets = dialog.widgets();
         if (widgets.size() > 18) {
            int begin = storage.page * 18;
            int end = Math.min((storage.page + 1) * 18, widgets.size());

            for (int i = 0; i < end - begin; i++) {
               items[i] = this.getItem(connection, widgets.get(begin + i));
            }

            items[26] = this.createPageNavigationItem();
         } else {
            for (int i = 0; i < widgets.size(); i++) {
               items[i] = this.getItem(connection, widgets.get(i));
            }
         }

         if (dialog.yesButton() != null && dialog.noButton() != null) {
            confirmationYesIndex = widgets.isEmpty() ? 11 : 20;
            confirmationNoIndex = widgets.isEmpty() ? 15 : 24;
            items[confirmationYesIndex] = this.createActionButtonItem(connection, dialog.yesButton());
            items[confirmationNoIndex] = this.createActionButtonItem(connection, dialog.noButton());
         }

         if (dialog.actionButton() != null) {
            actionIndex = widgets.isEmpty() ? 13 : 18;
            items[actionIndex] = this.createActionButtonItem(connection, dialog.actionButton());
         }

         storage.setItems(items, confirmationYesIndex, confirmationNoIndex, actionIndex);
         return items;
      }
   }

   @Nullable
   protected Tag handleTag(UserConnection connection, @Nullable Tag tag) {
      if (tag == null) {
         return null;
      } else {
         this.protocol.getComponentRewriter().processTag(connection, tag);
         return tag;
      }
   }

   protected Item createItem(String identifier, Tag name) {
      return this.createItem(identifier, name);
   }

   protected Item createItem(String identifier, Tag name, Tag... description) {
      int id = this.protocol.getMappingData().getFullItemMappings().mappedId(identifier);
      StructuredDataContainer data = new StructuredDataContainer();
      data.setIdLookup(this.protocol, true);
      data.set(StructuredDataKey.ITEM_NAME, name);
      if (description != null) {
         data.set(StructuredDataKey.LORE, description);
      }

      return new StructuredItem(id, 1, data);
   }

   protected Item createItem(String identifier, Tag name, String... description) {
      int id = this.protocol.getMappingData().getFullItemMappings().mappedId(identifier);
      StructuredDataContainer data = new StructuredDataContainer();
      data.setIdLookup(this.protocol, true);
      data.set(StructuredDataKey.ITEM_NAME, name);
      if (description.length > 0) {
         List<Tag> lore = new ArrayList<>();

         for (String s : description) {
            lore.add(ChatUtil.translate(s));
         }

         data.set(StructuredDataKey.LORE, lore.toArray(new Tag[0]));
      }

      return new StructuredItem(id, 1, data);
   }

   public record MultiTextWidget(Tag[] labels) implements Widget {
   }
}
