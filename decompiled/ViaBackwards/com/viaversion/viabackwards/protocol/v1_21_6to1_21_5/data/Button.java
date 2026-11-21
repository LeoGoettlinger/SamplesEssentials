package com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.input.Input;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.widget.Widget;
import com.viaversion.viaversion.util.Key;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Button implements Widget {
   public static final Button DEFAULT = defaulted();
   private static final String[] CLICK_EVENTS = new String[]{
      "open_url", "run_command", "suggest_command", "show_dialog", "change_page", "copy_to_clipboard", "custom"
   };
   private final Dialog dialog;
   private final Tag label;
   private final int width;
   @Nullable
   private final Tag tooltip;
   @Nullable
   private CompoundTag clickEvent;
   @Nullable
   private Template template;
   @Nullable
   private String id;
   @Nullable
   private CompoundTag additions;

   public Button(Dialog dialog, CompoundTag tag) {
      this.dialog = dialog;
      this.label = tag.get("label");
      this.tooltip = tag.get("tooltip");
      int width = tag.getInt("width", 150);
      if (width >= 1 && width <= 1024) {
         this.width = width;
         CompoundTag actionTag = tag.getCompoundTag("action");
         if (actionTag != null) {
            String type = actionTag.getString("type");
            if (type == null) {
               throw new IllegalArgumentException("Action type is missing in tag: " + tag);
            } else {
               for (String event : CLICK_EVENTS) {
                  if (Key.stripMinecraftNamespace(type).equals(event)) {
                     this.clickEvent = actionTag.copy();
                     this.clickEvent.put("action", this.clickEvent.remove("type"));
                     return;
                  }
               }

               if (Key.stripMinecraftNamespace(type).equals("dynamic/run_command")) {
                  this.template = Template.fromString(actionTag.getString("template"));
               } else if (Key.stripMinecraftNamespace(type).equals("dynamic/custom")) {
                  this.id = actionTag.getString("id");
                  this.additions = actionTag.getCompoundTag("additions");
               }
            }
         }
      } else {
         throw new IllegalArgumentException("Width must be between 1 and 1024, got: " + width);
      }
   }

   private static Button defaulted() {
      CompoundTag tag = new CompoundTag();
      CompoundTag label = new CompoundTag();
      tag.put("label", label);
      label.putString("translate", "gui.ok");
      return new Button(null, tag);
   }

   public static Button openUrl(Tag label, String url) {
      CompoundTag tag = new CompoundTag();
      CompoundTag actionTag = new CompoundTag();
      CompoundTag openUrlTag = new CompoundTag();
      tag.put("label", label);
      tag.put("action", actionTag);
      actionTag.putString("type", "open_url");
      actionTag.put("action", openUrlTag);
      openUrlTag.putString("url", url);
      return new Button(null, tag);
   }

   @Nullable
   public CompoundTag clickEvent() {
      if (this.clickEvent != null) {
         return this.clickEvent;
      } else if (this.dialog == null) {
         return null;
      } else {
         Input[] inputs = this.dialog.widgets().stream().filter(widget -> widget instanceof Input).toArray(Input[]::new);
         if (this.template != null) {
            Map<String, String> substitutions = new HashMap<>();

            for (Input input : inputs) {
               substitutions.put(input.key(), input.asCommandSubstitution());
            }

            CompoundTag tag = new CompoundTag();
            tag.putString("action", "run_command");
            tag.putString("command", this.template.instantiate(substitutions));
            return tag;
         } else if (this.id == null) {
            return null;
         } else {
            CompoundTag additions = this.additions != null ? this.additions.copy() : new CompoundTag();

            for (Input input : inputs) {
               additions.put(input.key(), input.asTag());
            }

            CompoundTag tag = new CompoundTag();
            tag.putString("action", "custom");
            tag.putString("id", this.id);
            tag.put("payload", additions);
            return tag;
         }
      }
   }

   public Tag label() {
      return this.label;
   }

   @Nullable
   public Tag tooltip() {
      return this.tooltip;
   }

   public int width() {
      return this.width;
   }
}
