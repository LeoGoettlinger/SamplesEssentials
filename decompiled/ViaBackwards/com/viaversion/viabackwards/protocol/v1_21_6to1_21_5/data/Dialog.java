package com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.input.BooleanInput;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.input.NumberRangeInput;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.input.SingleOptionInput;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.input.TextInput;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.widget.ItemWidget;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.widget.TextWidget;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.widget.Widget;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.storage.RegistryAndTags;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.storage.ServerLinks;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectIterator;
import com.viaversion.viaversion.util.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Dialog implements Widget {
   private final List<Widget> widgets = new ArrayList<>();
   private final Tag title;
   @Nullable
   private final Tag externalTitle;
   private final boolean canCloseWithEscape;
   private final Dialog.AfterAction afterAction;
   @Nullable
   private Button actionButton;
   @Nullable
   private Button yesButton;
   @Nullable
   private Button noButton;
   private int columns;
   private int buttonWidth;

   public Dialog(RegistryAndTags registryAndTags, ServerLinks serverLinks, CompoundTag tag) {
      String type = tag.getString("type");
      if (type == null) {
         throw new IllegalArgumentException("Dialog type is missing in tag: " + tag);
      } else {
         this.title = tag.get("title");
         this.externalTitle = tag.get("external_title");
         this.canCloseWithEscape = tag.getBoolean("can_close_with_escape", true);
         this.afterAction = Dialog.AfterAction.valueOf(tag.getString("after_action", "close").toUpperCase(Locale.ROOT));
         ListTag<CompoundTag> bodyListTag = tag.getListTag("body", CompoundTag.class);
         if (bodyListTag == null) {
            CompoundTag bodyTag = tag.getCompoundTag("body");
            if (bodyTag != null) {
               bodyListTag = new ListTag(CompoundTag.class);
               bodyListTag.add(bodyTag);
            }
         }

         if (bodyListTag != null) {
            for (CompoundTag bodyTag : bodyListTag) {
               this.fillBodyWidget(bodyTag);
            }
         }

         ListTag<CompoundTag> inputListTag = tag.getListTag("inputs", CompoundTag.class);
         if (inputListTag != null) {
            for (CompoundTag inputTag : inputListTag) {
               this.fillInputWidget(inputTag);
            }
         }

         String var12 = Key.stripMinecraftNamespace(type);
         switch (var12) {
            case "notice":
               this.fillNoticeDialog(tag);
               break;
            case "server_links":
               this.fillServerLinksDialog(serverLinks, tag);
               break;
            case "dialog_list":
               this.fillDialogList(registryAndTags, serverLinks, tag);
               break;
            case "multi_action":
               this.fillMultiActionDialog(tag);
               break;
            case "confirmation":
               this.fillConfirmationDialog(tag);
               break;
            default:
               throw new IllegalArgumentException("Unknown dialog type: " + type + " in tag: " + tag);
         }
      }
   }

   private void fillBodyWidget(CompoundTag tag) {
      String type = tag.getString("type");
      if (type == null) {
         throw new IllegalArgumentException("Dialog type is missing in tag: " + tag);
      } else {
         if (Key.stripMinecraftNamespace(type).equals("plain_message")) {
            this.widgets.add(new TextWidget(tag));
         } else {
            if (!Key.stripMinecraftNamespace(type).equals("item")) {
               throw new IllegalArgumentException("Unknown dialog body type: " + type + " in tag: " + tag);
            }

            this.widgets.add(new ItemWidget(tag));
         }
      }
   }

   private void fillInputWidget(CompoundTag tag) {
      String type = tag.getString("type");
      if (type == null) {
         throw new IllegalArgumentException("Dialog type is missing in tag: " + tag);
      } else {
         List var10000 = this.widgets;
         String var3 = Key.stripMinecraftNamespace(type);

         var10000.add(switch (var3) {
            case "boolean" -> new BooleanInput(tag);
            case "number_range" -> new NumberRangeInput(tag);
            case "single_option" -> new SingleOptionInput(tag);
            case "text" -> new TextInput(tag);
            default -> throw new IllegalArgumentException("Unknown dialog input type: " + type + " in tag: " + tag);
         });
      }
   }

   private void fillNoticeDialog(CompoundTag tag) {
      CompoundTag actionTag = tag.getCompoundTag("action");
      this.actionButton = actionTag == null ? Button.DEFAULT : new Button(this, actionTag);
   }

   private void fillServerLinksDialog(ServerLinks serverLinks, CompoundTag tag) {
      this.fillDialogBase(tag);
      this.buttonWidth = tag.getInt("button_width", 150);
      if (serverLinks != null) {
         ObjectIterator var3 = serverLinks.links().entrySet().iterator();

         while (var3.hasNext()) {
            Entry<Tag, String> entry = (Entry<Tag, String>)var3.next();
            Tag label = entry.getKey();
            String url = entry.getValue();
            this.widgets.add(Button.openUrl(label, url));
         }
      }
   }

   private void fillDialogList(RegistryAndTags registryAndTags, ServerLinks serverLinks, CompoundTag tag) {
      ListTag<CompoundTag> dialogsTag = tag.getListTag("dialogs", CompoundTag.class);
      if (dialogsTag == null) {
         CompoundTag dialogTag = tag.getCompoundTag("dialogs");
         if (dialogTag != null) {
            dialogsTag = new ListTag(CompoundTag.class);
            dialogsTag.add(dialogTag);
         }

         ListTag<StringTag> registryDialogsTag = tag.getListTag("dialogs", StringTag.class);
         StringTag registryDialogTag = tag.getStringTag("dialogs");
         if (registryDialogTag != null) {
            registryDialogsTag = new ListTag(StringTag.class);
            registryDialogsTag.add(registryDialogTag);
         }

         if (registryDialogsTag != null) {
            dialogsTag = new ListTag(CompoundTag.class);

            for (StringTag nameTag : registryDialogsTag) {
               String key = nameTag.getValue();
               if (key.startsWith("#")) {
                  for (CompoundTag entry : registryAndTags.fromRegistryKey(key.substring(1))) {
                     dialogsTag.add(entry);
                  }
               } else {
                  dialogsTag.add(registryAndTags.fromRegistry(key));
               }
            }
         }
      }

      this.widgets.addAll(dialogsTag.stream().map(dialog -> new Dialog(registryAndTags, serverLinks, dialog)).toList());
      this.fillDialogBase(tag);
      this.buttonWidth = tag.getInt("button_width", 150);
   }

   private void fillDialogBase(CompoundTag tag) {
      CompoundTag exitActionTag = tag.getCompoundTag("exit_action");
      this.actionButton = exitActionTag == null ? null : new Button(this, exitActionTag);
      int columns = tag.getInt("columns", 2);
      if (columns < 1) {
         throw new IllegalArgumentException("Columns must be non-negative, got: " + columns);
      } else {
         this.columns = columns;
      }
   }

   private void fillMultiActionDialog(CompoundTag tag) {
      ListTag<CompoundTag> actionsTag = tag.getListTag("actions", CompoundTag.class);
      if (actionsTag != null && !actionsTag.isEmpty()) {
         this.widgets.addAll(actionsTag.stream().map(actionTag -> new Button(this, actionTag)).toList());
         this.fillDialogBase(tag);
      } else {
         throw new IllegalArgumentException("Actions must not be empty in tag: " + tag);
      }
   }

   private void fillConfirmationDialog(CompoundTag tag) {
      this.yesButton = new Button(this, tag.getCompoundTag("yes"));
      this.noButton = new Button(this, tag.getCompoundTag("no"));
   }

   public Tag title() {
      return this.title;
   }

   @Nullable
   public Tag externalTitle() {
      return this.externalTitle;
   }

   public boolean canCloseWithEscape() {
      return this.canCloseWithEscape;
   }

   public Dialog.AfterAction afterAction() {
      return this.afterAction;
   }

   @Nullable
   public Button actionButton() {
      return this.actionButton;
   }

   @Nullable
   public Button yesButton() {
      return this.yesButton;
   }

   @Nullable
   public Button noButton() {
      return this.noButton;
   }

   public int columns() {
      return this.columns;
   }

   public int buttonWidth() {
      return this.buttonWidth;
   }

   public List<Widget> widgets() {
      return this.widgets;
   }

   public static enum AfterAction {
      CLOSE,
      NONE,
      WAIT_FOR_RESPONSE;
   }
}
