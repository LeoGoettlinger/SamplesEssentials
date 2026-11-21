package com.magmaguy.elitemobs.magmacore.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.magmaguy.elitemobs.magmacore.util.minimessage.MiniMessageParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DialogManager {
   private static final Map<Player, Object> dialogOwners = new WeakHashMap<>();

   private DialogManager() {
   }

   private static String stripLegacyColorCodes(String value) {
      if (value != null && value.indexOf(167) >= 0) {
         StringBuilder sanitized = new StringBuilder(value.length());

         for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == 167 && i + 1 < value.length()) {
               i++;
            } else {
               sanitized.append(c);
            }
         }

         return sanitized.toString();
      } else {
         return value;
      }
   }

   public static JsonObject serializeItemComponents(ItemStack itemStack) {
      try {
         JsonObject components = new JsonObject();
         if (!itemStack.hasItemMeta()) {
            return components;
         } else {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta.hasDisplayName()) {
               components.add("minecraft:custom_name", DialogManager.TextComponent.of(meta.getDisplayName()));
            }

            if (meta.hasLore() && meta.getLore() != null) {
               JsonArray loreArray = new JsonArray();

               for (String loreLine : meta.getLore()) {
                  loreArray.add(DialogManager.TextComponent.of(loreLine));
               }

               components.add("minecraft:lore", loreArray);
            }

            if (meta.hasCustomModelData()) {
               components.addProperty("minecraft:custom_model_data", meta.getCustomModelData());
            }

            if (meta.isUnbreakable()) {
               JsonObject unbreakable = new JsonObject();
               unbreakable.addProperty("show_in_tooltip", false);
               components.add("minecraft:unbreakable", unbreakable);
            }

            if (itemStack.getType().getMaxDurability() > 0) {
               components.addProperty("minecraft:max_stack_size", itemStack.getType().getMaxStackSize());
            }

            return components;
         }
      } catch (Exception var6) {
         Bukkit.getLogger().warning("Failed to serialize item components: " + var6.getMessage());
         var6.printStackTrace();
         return new JsonObject();
      }
   }

   public static void sendDialog(Player player, DialogManager.DialogBuilder builder) {
      sendDialog(player, builder, null);
   }

   public static void sendDialog(Player player, DialogManager.DialogBuilder builder, Object owner) {
      dialogOwners.remove(player);
      if (Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:dialog show " + player.getName() + " " + builder.build()) && owner != null) {
         dialogOwners.put(player, owner);
      }
   }

   public static void clearOwnedDialog(Player player, Object owner) {
      if (owner != null && dialogOwners.get(player) == owner) {
         if (Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:dialog clear " + player.getName())) {
            dialogOwners.remove(player, owner);
         }
      }
   }

   public static void forgetDialogOwner(Player player, Object owner) {
      dialogOwners.remove(player, owner);
   }

   public interface Action {
      JsonObject toJson();
   }

   public static class ActionButton {
      private final JsonElement label;
      private final DialogManager.Action action;
      private JsonElement tooltip;
      private Integer width;

      private ActionButton(JsonElement label, DialogManager.Action action) {
         this.label = Objects.requireNonNull(label, "label");
         this.action = Objects.requireNonNull(action, "action");
      }

      public static DialogManager.ActionButton of(JsonElement label, DialogManager.Action action) {
         return new DialogManager.ActionButton(label, action);
      }

      public static DialogManager.ActionButton of(String label, DialogManager.Action action) {
         return new DialogManager.ActionButton(DialogManager.TextComponent.of(label), action);
      }

      public DialogManager.ActionButton tooltip(JsonElement tooltip) {
         this.tooltip = tooltip;
         return this;
      }

      public DialogManager.ActionButton tooltip(String text) {
         return this.tooltip(DialogManager.TextComponent.of(text));
      }

      public DialogManager.ActionButton width(int width) {
         if (width >= 1 && width <= 1024) {
            this.width = width;
            return this;
         } else {
            throw new IllegalArgumentException("width must be between 1 and 1024");
         }
      }

      private JsonObject toJson() {
         JsonObject obj = new JsonObject();
         obj.add("label", this.label);
         if (this.tooltip != null) {
            obj.add("tooltip", this.tooltip);
         }

         if (this.width != null) {
            obj.addProperty("width", this.width);
         }

         obj.add("action", this.action.toJson());
         return obj;
      }
   }

   public static enum AfterAction {
      CLOSE("close"),
      NONE("none"),
      WAIT_FOR_RESPONSE("wait_for_response");

      private final String value;

      private AfterAction(String value) {
         this.value = value;
      }
   }

   public interface BodyElement {
      JsonObject toJson();
   }

   public static class BooleanInput implements DialogManager.InputControl {
      private final String key;
      private final JsonElement label;
      private Boolean initial;
      private String onTrue;
      private String onFalse;

      private BooleanInput(String key, JsonElement label) {
         this.key = Objects.requireNonNull(key, "key");
         this.label = Objects.requireNonNull(label, "label");
      }

      public static DialogManager.BooleanInput of(String key, JsonElement label) {
         return new DialogManager.BooleanInput(key, label);
      }

      public static DialogManager.BooleanInput of(String key, String label) {
         return new DialogManager.BooleanInput(key, DialogManager.TextComponent.of(label));
      }

      public DialogManager.BooleanInput initial(boolean initial) {
         this.initial = initial;
         return this;
      }

      public DialogManager.BooleanInput onTrue(String value) {
         this.onTrue = value;
         return this;
      }

      public DialogManager.BooleanInput onFalse(String value) {
         this.onFalse = value;
         return this;
      }

      @Override
      public JsonObject toJson() {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", "minecraft:boolean");
         obj.addProperty("key", this.key);
         obj.add("label", this.label);
         if (this.initial != null) {
            obj.addProperty("initial", this.initial);
         }

         if (this.onTrue != null) {
            obj.addProperty("on_true", this.onTrue);
         }

         if (this.onFalse != null) {
            obj.addProperty("on_false", this.onFalse);
         }

         return obj;
      }
   }

   public static class ChangePageAction implements DialogManager.Action {
      private final int page;

      public ChangePageAction(int page) {
         this.page = page;
      }

      @Override
      public JsonObject toJson() {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", "change_page");
         obj.addProperty("page", this.page);
         return obj;
      }
   }

   public static class ConfirmationDialogBuilder extends DialogManager.DialogBuilder<DialogManager.ConfirmationDialogBuilder> {
      private DialogManager.ActionButton yesButton;
      private DialogManager.ActionButton noButton;

      public ConfirmationDialogBuilder() {
         super("minecraft:confirmation");
      }

      public DialogManager.ConfirmationDialogBuilder yes(DialogManager.ActionButton button) {
         this.yesButton = button;
         return this;
      }

      public DialogManager.ConfirmationDialogBuilder no(DialogManager.ActionButton button) {
         this.noButton = button;
         return this;
      }

      @Override
      public JsonObject build() {
         JsonObject obj = this.buildBase();
         if (this.yesButton != null) {
            obj.add("yes", this.yesButton.toJson());
         }

         if (this.noButton != null) {
            obj.add("no", this.noButton.toJson());
         }

         return obj;
      }
   }

   public static class CopyToClipboardAction implements DialogManager.Action {
      private final String value;

      public CopyToClipboardAction(String value) {
         this.value = Objects.requireNonNull(value, "value");
      }

      @Override
      public JsonObject toJson() {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", "copy_to_clipboard");
         obj.addProperty("value", this.value);
         return obj;
      }
   }

   public static class CustomAction implements DialogManager.Action {
      private final String id;
      private String payload;

      public CustomAction(String id) {
         this.id = Objects.requireNonNull(id, "id");
      }

      public DialogManager.CustomAction payload(String payload) {
         this.payload = payload;
         return this;
      }

      @Override
      public JsonObject toJson() {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", "custom");
         obj.addProperty("id", this.id);
         if (this.payload != null) {
            obj.addProperty("payload", this.payload);
         }

         return obj;
      }
   }

   public abstract static class DialogBuilder<T extends DialogManager.DialogBuilder<T>> {
      protected final String type;
      protected final List<DialogManager.BodyElement> bodyElements = new ArrayList<>();
      protected final List<DialogManager.InputControl> inputControls = new ArrayList<>();
      protected JsonElement title;
      protected JsonElement externalTitle;
      protected Boolean canCloseWithEscape;
      protected Boolean pause;
      protected DialogManager.AfterAction afterAction;

      protected DialogBuilder(String type) {
         this.type = type;
      }

      public T title(JsonElement title) {
         this.title = title;
         return (T)this;
      }

      public T title(String text) {
         return this.title(DialogManager.TextComponent.of(text));
      }

      public T externalTitle(JsonElement externalTitle) {
         this.externalTitle = externalTitle;
         return (T)this;
      }

      public T externalTitle(String text) {
         return this.externalTitle(DialogManager.TextComponent.of(text));
      }

      public T addBody(DialogManager.BodyElement element) {
         this.bodyElements.add(element);
         return (T)this;
      }

      public T addInput(DialogManager.InputControl control) {
         this.inputControls.add(control);
         return (T)this;
      }

      public T canCloseWithEscape(boolean canCloseWithEscape) {
         this.canCloseWithEscape = canCloseWithEscape;
         return (T)this;
      }

      public T pause(boolean pause) {
         this.pause = pause;
         return (T)this;
      }

      public T afterAction(DialogManager.AfterAction afterAction) {
         this.afterAction = afterAction;
         return (T)this;
      }

      protected JsonObject buildBase() {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", this.type);
         if (this.title != null) {
            obj.add("title", this.title);
         }

         if (this.externalTitle != null) {
            obj.add("external_title", this.externalTitle);
         }

         if (!this.bodyElements.isEmpty()) {
            if (this.bodyElements.size() == 1) {
               obj.add("body", this.bodyElements.get(0).toJson());
            } else {
               JsonArray arr = new JsonArray();

               for (DialogManager.BodyElement element : this.bodyElements) {
                  arr.add(element.toJson());
               }

               obj.add("body", arr);
            }
         }

         if (!this.inputControls.isEmpty()) {
            JsonArray inputsArray = new JsonArray();

            for (DialogManager.InputControl control : this.inputControls) {
               inputsArray.add(control.toJson());
            }

            obj.add("inputs", inputsArray);
         }

         if (this.canCloseWithEscape != null) {
            obj.addProperty("can_close_with_escape", this.canCloseWithEscape);
         }

         if (this.pause != null) {
            obj.addProperty("pause", this.pause);
         }

         if (this.afterAction != null) {
            obj.addProperty("after_action", this.afterAction.value);
         }

         return obj;
      }

      public abstract JsonObject build();

      public String toJson() {
         return this.build().toString();
      }
   }

   public static class DialogListDialogBuilder extends DialogManager.DialogBuilder<DialogManager.DialogListDialogBuilder> {
      private final List<DialogManager.DialogReference> dialogs = new ArrayList<>();
      private DialogManager.ActionButton exitAction;
      private Integer columns;
      private Integer buttonWidth;

      public DialogListDialogBuilder() {
         super("minecraft:dialog_list");
      }

      public DialogManager.DialogListDialogBuilder addDialog(DialogManager.DialogReference ref) {
         Objects.requireNonNull(ref, "dialog reference");
         this.dialogs.add(ref);
         return this;
      }

      public DialogManager.DialogListDialogBuilder exitAction(DialogManager.ActionButton button) {
         this.exitAction = button;
         return this;
      }

      public DialogManager.DialogListDialogBuilder columns(int columns) {
         if (columns < 1) {
            throw new IllegalArgumentException("columns must be >= 1");
         } else {
            this.columns = columns;
            return this;
         }
      }

      public DialogManager.DialogListDialogBuilder buttonWidth(int width) {
         if (width >= 1 && width <= 1024) {
            this.buttonWidth = width;
            return this;
         } else {
            throw new IllegalArgumentException("width must be between 1 and 1024");
         }
      }

      @Override
      public JsonObject build() {
         JsonObject obj = this.buildBase();
         if (!this.dialogs.isEmpty()) {
            JsonArray arr = new JsonArray();

            for (DialogManager.DialogReference ref : this.dialogs) {
               arr.add(ref.toJsonElement());
            }

            obj.add("dialogs", arr);
         }

         if (this.exitAction != null) {
            obj.add("exit_action", this.exitAction.toJson());
         }

         if (this.columns != null) {
            obj.addProperty("columns", this.columns);
         }

         if (this.buttonWidth != null) {
            obj.addProperty("button_width", this.buttonWidth);
         }

         return obj;
      }
   }

   public static class DialogReference {
      private final String id;
      private final DialogManager.DialogBuilder<?> inline;

      private DialogReference(String id, DialogManager.DialogBuilder<?> inline) {
         this.id = id;
         this.inline = inline;
      }

      public static DialogManager.DialogReference id(String id) {
         return new DialogManager.DialogReference(Objects.requireNonNull(id, "id"), null);
      }

      public static DialogManager.DialogReference inline(DialogManager.DialogBuilder<?> builder) {
         return new DialogManager.DialogReference(null, Objects.requireNonNull(builder, "builder"));
      }

      private JsonElement toJsonElement() {
         return (JsonElement)(this.id != null ? new JsonPrimitive(this.id) : this.inline.build());
      }
   }

   public static class DynamicCustomAction implements DialogManager.Action {
      private final String id;
      private final Map<String, JsonElement> additions = new HashMap<>();

      public DynamicCustomAction(String id) {
         this.id = Objects.requireNonNull(id, "id");
      }

      public DialogManager.DynamicCustomAction add(String key, JsonElement value) {
         this.additions.put(key, value);
         return this;
      }

      public DialogManager.DynamicCustomAction add(String key, String value) {
         this.additions.put(key, new JsonPrimitive(value));
         return this;
      }

      @Override
      public JsonObject toJson() {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", "dynamic/custom");
         obj.addProperty("id", this.id);
         if (!this.additions.isEmpty()) {
            JsonObject addObj = new JsonObject();

            for (Entry<String, JsonElement> entry : this.additions.entrySet()) {
               addObj.add(entry.getKey(), entry.getValue());
            }

            obj.add("additions", addObj);
         }

         return obj;
      }
   }

   public static class DynamicRunCommandAction implements DialogManager.Action {
      private final String template;

      public DynamicRunCommandAction(String template) {
         this.template = DialogManager.stripLegacyColorCodes(Objects.requireNonNull(template, "template"));
      }

      @Override
      public JsonObject toJson() {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", "dynamic/run_command");
         obj.addProperty("template", this.template);
         return obj;
      }
   }

   public interface InputControl {
      JsonObject toJson();
   }

   public static class ItemBody implements DialogManager.BodyElement {
      private final String itemId;
      private final int count;
      private JsonObject components;
      private JsonElement description;
      private Boolean showDecoration;
      private Boolean showTooltip;
      private Integer width;
      private Integer height;

      private ItemBody(String itemId, int count) {
         this.itemId = Objects.requireNonNull(itemId, "itemId");
         this.count = count;
      }

      public static DialogManager.ItemBody of(String itemId, int count) {
         return new DialogManager.ItemBody(itemId, count);
      }

      public DialogManager.ItemBody components(JsonObject components) {
         this.components = components;
         return this;
      }

      public DialogManager.ItemBody description(JsonElement description) {
         this.description = description;
         return this;
      }

      public DialogManager.ItemBody description(String text) {
         return this.description(DialogManager.TextComponent.of(text));
      }

      public DialogManager.ItemBody showDecoration(boolean show) {
         this.showDecoration = show;
         return this;
      }

      public DialogManager.ItemBody showTooltip(boolean show) {
         this.showTooltip = show;
         return this;
      }

      public DialogManager.ItemBody width(int width) {
         if (width >= 1 && width <= 256) {
            this.width = width;
            return this;
         } else {
            throw new IllegalArgumentException("width must be between 1 and 256");
         }
      }

      public DialogManager.ItemBody height(int height) {
         if (height >= 1 && height <= 256) {
            this.height = height;
            return this;
         } else {
            throw new IllegalArgumentException("height must be between 1 and 256");
         }
      }

      @Override
      public JsonObject toJson() {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", "minecraft:item");
         JsonObject itemObj = new JsonObject();
         itemObj.addProperty("id", this.itemId);
         if (this.count > 0) {
            itemObj.addProperty("count", this.count);
         }

         if (this.components != null) {
            itemObj.add("components", this.components);
         }

         obj.add("item", itemObj);
         if (this.description != null) {
            JsonObject desc = new JsonObject();
            desc.add("contents", this.description);
            obj.add("description", desc);
         }

         if (this.showDecoration != null) {
            obj.addProperty("show_decoration", this.showDecoration);
         }

         if (this.showTooltip != null) {
            obj.addProperty("show_tooltip", this.showTooltip);
         }

         if (this.width != null) {
            obj.addProperty("width", this.width);
         }

         if (this.height != null) {
            obj.addProperty("height", this.height);
         }

         return obj;
      }
   }

   public static class MultiActionDialogBuilder extends DialogManager.DialogBuilder<DialogManager.MultiActionDialogBuilder> {
      private final List<DialogManager.ActionButton> actions = new ArrayList<>();
      private Integer columns;
      private DialogManager.ActionButton exitAction;

      public MultiActionDialogBuilder() {
         super("minecraft:multi_action");
      }

      public DialogManager.MultiActionDialogBuilder addAction(DialogManager.ActionButton button) {
         Objects.requireNonNull(button, "button");
         this.actions.add(button);
         return this;
      }

      public DialogManager.MultiActionDialogBuilder columns(int columns) {
         if (columns < 1) {
            throw new IllegalArgumentException("columns must be >= 1");
         } else {
            this.columns = columns;
            return this;
         }
      }

      public DialogManager.MultiActionDialogBuilder exitAction(DialogManager.ActionButton button) {
         this.exitAction = button;
         return this;
      }

      @Override
      public JsonObject build() {
         if (this.actions.isEmpty()) {
            throw new IllegalStateException("multi_action dialog requires at least one action");
         } else {
            JsonObject obj = this.buildBase();
            JsonArray arr = new JsonArray();

            for (DialogManager.ActionButton btn : this.actions) {
               arr.add(btn.toJson());
            }

            obj.add("actions", arr);
            if (this.columns != null) {
               obj.addProperty("columns", this.columns);
            }

            if (this.exitAction != null) {
               obj.add("exit_action", this.exitAction.toJson());
            }

            return obj;
         }
      }
   }

   public static class NoticeDialogBuilder extends DialogManager.DialogBuilder<DialogManager.NoticeDialogBuilder> {
      private DialogManager.ActionButton action;

      public NoticeDialogBuilder() {
         super("minecraft:notice");
      }

      public DialogManager.NoticeDialogBuilder action(DialogManager.ActionButton action) {
         this.action = action;
         return this;
      }

      @Override
      public JsonObject build() {
         JsonObject obj = this.buildBase();
         if (this.action != null) {
            obj.add("action", this.action.toJson());
         }

         return obj;
      }
   }

   public static class NumberRangeInput implements DialogManager.InputControl {
      private final String key;
      private final JsonElement label;
      private String labelFormat;
      private Integer width;
      private Float start;
      private Float end;
      private Float step;
      private Float initial;

      private NumberRangeInput(String key, JsonElement label) {
         this.key = Objects.requireNonNull(key, "key");
         this.label = Objects.requireNonNull(label, "label");
      }

      public static DialogManager.NumberRangeInput of(String key, JsonElement label) {
         return new DialogManager.NumberRangeInput(key, label);
      }

      public static DialogManager.NumberRangeInput of(String key, String label) {
         return new DialogManager.NumberRangeInput(key, DialogManager.TextComponent.of(label));
      }

      public DialogManager.NumberRangeInput labelFormat(String format) {
         this.labelFormat = format;
         return this;
      }

      public DialogManager.NumberRangeInput width(int width) {
         if (width >= 1 && width <= 1024) {
            this.width = width;
            return this;
         } else {
            throw new IllegalArgumentException("width must be between 1 and 1024");
         }
      }

      public DialogManager.NumberRangeInput start(float start) {
         this.start = start;
         return this;
      }

      public DialogManager.NumberRangeInput end(float end) {
         this.end = end;
         return this;
      }

      public DialogManager.NumberRangeInput step(float step) {
         this.step = step;
         return this;
      }

      public DialogManager.NumberRangeInput initial(float initial) {
         this.initial = initial;
         return this;
      }

      @Override
      public JsonObject toJson() {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", "minecraft:number_range");
         obj.addProperty("key", this.key);
         obj.add("label", this.label);
         if (this.labelFormat != null) {
            obj.addProperty("label_format", this.labelFormat);
         }

         if (this.width != null) {
            obj.addProperty("width", this.width);
         }

         if (this.start != null) {
            obj.addProperty("start", this.start);
         }

         if (this.end != null) {
            obj.addProperty("end", this.end);
         }

         if (this.step != null) {
            obj.addProperty("step", this.step);
         }

         if (this.initial != null) {
            obj.addProperty("initial", this.initial);
         }

         return obj;
      }
   }

   public static class OpenUrlAction implements DialogManager.Action {
      private final String url;

      public OpenUrlAction(String url) {
         this.url = Objects.requireNonNull(url, "url");
      }

      @Override
      public JsonObject toJson() {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", "open_url");
         obj.addProperty("url", this.url);
         return obj;
      }
   }

   public static class PlainMessageBody implements DialogManager.BodyElement {
      private final JsonElement contents;
      private Integer width;

      private PlainMessageBody(JsonElement contents) {
         this.contents = contents;
      }

      public static DialogManager.PlainMessageBody of(JsonElement contents) {
         return new DialogManager.PlainMessageBody(contents);
      }

      public static DialogManager.PlainMessageBody of(String text) {
         return new DialogManager.PlainMessageBody(DialogManager.TextComponent.of(text));
      }

      public DialogManager.PlainMessageBody width(int width) {
         if (width >= 1 && width <= 1024) {
            this.width = width;
            return this;
         } else {
            throw new IllegalArgumentException("width must be between 1 and 1024");
         }
      }

      @Override
      public JsonObject toJson() {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", "minecraft:plain_message");
         obj.add("contents", this.contents);
         if (this.width != null) {
            obj.addProperty("width", this.width);
         }

         return obj;
      }
   }

   public static class RunCommandAction implements DialogManager.Action {
      private final String command;

      public RunCommandAction(String command) {
         this.command = DialogManager.stripLegacyColorCodes(Objects.requireNonNull(command, "command"));
      }

      @Override
      public JsonObject toJson() {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", "run_command");
         obj.addProperty("command", this.command);
         return obj;
      }
   }

   public static class ServerLinksDialogBuilder extends DialogManager.DialogBuilder<DialogManager.ServerLinksDialogBuilder> {
      private DialogManager.ActionButton exitAction;
      private Integer columns;
      private Integer buttonWidth;

      public ServerLinksDialogBuilder() {
         super("minecraft:server_links");
      }

      public DialogManager.ServerLinksDialogBuilder exitAction(DialogManager.ActionButton button) {
         this.exitAction = button;
         return this;
      }

      public DialogManager.ServerLinksDialogBuilder columns(int columns) {
         if (columns < 1) {
            throw new IllegalArgumentException("columns must be >= 1");
         } else {
            this.columns = columns;
            return this;
         }
      }

      public DialogManager.ServerLinksDialogBuilder buttonWidth(int width) {
         if (width >= 1 && width <= 1024) {
            this.buttonWidth = width;
            return this;
         } else {
            throw new IllegalArgumentException("width must be between 1 and 1024");
         }
      }

      @Override
      public JsonObject build() {
         JsonObject obj = this.buildBase();
         if (this.exitAction != null) {
            obj.add("exit_action", this.exitAction.toJson());
         }

         if (this.columns != null) {
            obj.addProperty("columns", this.columns);
         }

         if (this.buttonWidth != null) {
            obj.addProperty("button_width", this.buttonWidth);
         }

         return obj;
      }
   }

   public static class ShowDialogAction implements DialogManager.Action {
      private final DialogManager.DialogReference dialog;

      public ShowDialogAction(DialogManager.DialogReference dialog) {
         this.dialog = Objects.requireNonNull(dialog, "dialog");
      }

      @Override
      public JsonObject toJson() {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", "show_dialog");
         obj.add("dialog", this.dialog.toJsonElement());
         return obj;
      }
   }

   public static class SingleOptionInput implements DialogManager.InputControl {
      private final String key;
      private final JsonElement label;
      private final List<DialogManager.SingleOptionInput.Option> options = new ArrayList<>();
      private Boolean labelVisible;
      private Integer width;

      private SingleOptionInput(String key, JsonElement label) {
         this.key = Objects.requireNonNull(key, "key");
         this.label = Objects.requireNonNull(label, "label");
      }

      public static DialogManager.SingleOptionInput of(String key, JsonElement label) {
         return new DialogManager.SingleOptionInput(key, label);
      }

      public static DialogManager.SingleOptionInput of(String key, String label) {
         return new DialogManager.SingleOptionInput(key, DialogManager.TextComponent.of(label));
      }

      public DialogManager.SingleOptionInput labelVisible(boolean visible) {
         this.labelVisible = visible;
         return this;
      }

      public DialogManager.SingleOptionInput width(int width) {
         if (width >= 1 && width <= 1024) {
            this.width = width;
            return this;
         } else {
            throw new IllegalArgumentException("width must be between 1 and 1024");
         }
      }

      public DialogManager.SingleOptionInput addOption(String id, JsonElement display, boolean initial) {
         this.options.add(new DialogManager.SingleOptionInput.Option(id, display, initial));
         return this;
      }

      public DialogManager.SingleOptionInput addOption(String id, String display, boolean initial) {
         this.options.add(new DialogManager.SingleOptionInput.Option(id, DialogManager.TextComponent.of(display), initial));
         return this;
      }

      @Override
      public JsonObject toJson() {
         if (this.options.isEmpty()) {
            throw new IllegalStateException("single_option input requires at least one option");
         } else {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "minecraft:single_option");
            obj.addProperty("key", this.key);
            obj.add("label", this.label);
            if (this.labelVisible != null) {
               obj.addProperty("label_visible", this.labelVisible);
            }

            if (this.width != null) {
               obj.addProperty("width", this.width);
            }

            JsonArray opts = new JsonArray();

            for (DialogManager.SingleOptionInput.Option opt : this.options) {
               opts.add(opt.toJson());
            }

            obj.add("options", opts);
            return obj;
         }
      }

      private static class Option {
         private final String id;
         private final JsonElement display;
         private final boolean initial;

         Option(String id, JsonElement display, boolean initial) {
            this.id = id;
            this.display = display;
            this.initial = initial;
         }

         JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", this.id);
            obj.add("display", this.display);
            if (this.initial) {
               obj.addProperty("initial", true);
            }

            return obj;
         }
      }
   }

   public static class SuggestCommandAction implements DialogManager.Action {
      private final String command;

      public SuggestCommandAction(String command) {
         this.command = DialogManager.stripLegacyColorCodes(Objects.requireNonNull(command, "command"));
      }

      @Override
      public JsonObject toJson() {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", "suggest_command");
         obj.addProperty("command", this.command);
         return obj;
      }
   }

   public static final class TextComponent {
      private TextComponent() {
      }

      public static JsonElement of(String text) {
         if (text == null) {
            text = "";
         }

         if (!containsFormatting(text)) {
            JsonObject obj = new JsonObject();
            obj.addProperty("text", text);
            return obj;
         } else {
            return JsonParser.parseString(ComponentSerializer.toString(MiniMessageParser.parse(text)));
         }
      }

      private static boolean containsFormatting(String text) {
         for (int i = 0; i < text.length() - 1; i++) {
            char c = text.charAt(i);
            if (c == 167) {
               return true;
            }

            if (c == '&' && isLegacyCode(text.charAt(i + 1))) {
               return true;
            }
         }

         return text.contains("<");
      }

      private static boolean isLegacyCode(char c) {
         return "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx#".indexOf(c) >= 0;
      }
   }

   public static class TextInput implements DialogManager.InputControl {
      private final String key;
      private final JsonElement label;
      private Integer width;
      private Boolean labelVisible;
      private String initial;
      private Integer maxLength;
      private Integer maxLines;
      private Integer height;

      private TextInput(String key, JsonElement label) {
         this.key = Objects.requireNonNull(key, "key");
         this.label = Objects.requireNonNull(label, "label");
      }

      public static DialogManager.TextInput of(String key, JsonElement label) {
         return new DialogManager.TextInput(key, label);
      }

      public static DialogManager.TextInput of(String key, String label) {
         return new DialogManager.TextInput(key, DialogManager.TextComponent.of(label));
      }

      public DialogManager.TextInput width(int width) {
         if (width >= 1 && width <= 1024) {
            this.width = width;
            return this;
         } else {
            throw new IllegalArgumentException("width must be between 1 and 1024");
         }
      }

      public DialogManager.TextInput labelVisible(boolean visible) {
         this.labelVisible = visible;
         return this;
      }

      public DialogManager.TextInput initial(String initial) {
         this.initial = initial;
         return this;
      }

      public DialogManager.TextInput maxLength(int maxLength) {
         this.maxLength = maxLength;
         return this;
      }

      public DialogManager.TextInput multiline(Integer maxLines, Integer height) {
         this.maxLines = maxLines;
         this.height = height;
         return this;
      }

      @Override
      public JsonObject toJson() {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", "minecraft:text");
         obj.addProperty("key", this.key);
         obj.add("label", this.label);
         if (this.width != null) {
            obj.addProperty("width", this.width);
         }

         if (this.labelVisible != null) {
            obj.addProperty("label_visible", this.labelVisible);
         }

         if (this.initial != null) {
            obj.addProperty("initial", this.initial);
         }

         if (this.maxLength != null) {
            obj.addProperty("max_length", this.maxLength);
         }

         if (this.maxLines != null || this.height != null) {
            JsonObject multi = new JsonObject();
            if (this.maxLines != null) {
               multi.addProperty("max_lines", this.maxLines);
            }

            if (this.height != null) {
               multi.addProperty("height", this.height);
            }

            obj.add("multiline", multi);
         }

         return obj;
      }
   }
}
