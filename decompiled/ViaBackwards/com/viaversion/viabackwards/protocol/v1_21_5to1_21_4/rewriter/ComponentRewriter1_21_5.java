package com.viaversion.viabackwards.protocol.v1_21_5to1_21_4.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.rewriters.text.NBTComponentRewriter;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPacket1_21_5;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.TagUtil;
import java.util.HashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ComponentRewriter1_21_5 extends NBTComponentRewriter<ClientboundPacket1_21_5> {
   public ComponentRewriter1_21_5(BackwardsProtocol<ClientboundPacket1_21_5, ?, ?, ?> protocol) {
      super(protocol);
   }

   protected void processCompoundTag(UserConnection connection, CompoundTag tag) {
      super.processCompoundTag(connection, tag);
      if (tag.remove("hover_event") instanceof CompoundTag hoverEvent) {
         tag.put("hoverEvent", hoverEvent);
      }

      if (tag.remove("click_event") instanceof CompoundTag clickEvent) {
         tag.put("clickEvent", clickEvent);
         this.updateClickEvent(clickEvent);
      }
   }

   protected void handleHoverEvent(UserConnection connection, CompoundTag hoverEventTag) {
      String action = hoverEventTag.getString("action");
      if (action != null) {
         switch (action) {
            case "show_text":
               this.updateShowTextHover(hoverEventTag);
               break;
            case "show_entity":
               this.updateShowEntityHover(hoverEventTag);
               break;
            case "show_item":
               this.updateShowItemHover(connection, hoverEventTag);
         }
      }
   }

   private void updateClickEvent(CompoundTag clickEventTag) {
      String action = clickEventTag.getString("action");
      if (action != null) {
         switch (action) {
            case "open_url":
               clickEventTag.put("value", clickEventTag.getStringTag("url"));
               break;
            case "change_page":
               clickEventTag.putString("value", Integer.toString(clickEventTag.getInt("page")));
               break;
            case "run_command":
               StringTag command = clickEventTag.getStringTag("command");
               if (command != null && !command.getValue().startsWith("/")) {
                  command.setValue("/" + command.getValue());
               }

               clickEventTag.put("value", command);
               break;
            case "suggest_command":
               clickEventTag.put("value", clickEventTag.getStringTag("command"));
         }
      }
   }

   private void updateShowTextHover(CompoundTag hoverEventTag) {
      Tag text = hoverEventTag.remove("value");
      hoverEventTag.put("contents", text);
   }

   private void updateShowItemHover(UserConnection connection, CompoundTag hoverEventTag) {
      CompoundTag contents = new CompoundTag();
      hoverEventTag.put("contents", contents);
      if (hoverEventTag.get("count") instanceof NumberTag countTag) {
         contents.put("count", countTag);
      }

      if (hoverEventTag.get("id") instanceof StringTag idTag) {
         contents.put("id", idTag);
      }

      CompoundTag componentsTag = hoverEventTag.getCompoundTag("components");
      this.handleShowItem(connection, contents, componentsTag);
      if (componentsTag != null) {
         hoverEventTag.remove("components");
         contents.put("components", componentsTag);
      }
   }

   protected void handleShowItem(UserConnection connection, CompoundTag itemTag, @Nullable CompoundTag componentsTag) {
      super.handleShowItem(connection, itemTag, componentsTag);
      if (componentsTag != null) {
         this.insertUglyJson(componentsTag, connection);
         this.updateDataComponents(componentsTag);
         this.removeDataComponents(componentsTag, com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.BlockItemPacketRewriter1_21_5.NEW_DATA_TO_REMOVE);
      }
   }

   private void updateDataComponents(CompoundTag componentsTag) {
      CompoundTag tooltipDisplay = TagUtil.getNamespacedCompoundTag(componentsTag, "tooltip_display");
      Set<String> hiddenComponents = Set.of();
      if (tooltipDisplay != null) {
         ListTag<StringTag> hiddenComponentsTag = tooltipDisplay.getListTag("hidden_components", StringTag.class);
         if (hiddenComponentsTag != null) {
            hiddenComponents = new HashSet<>(hiddenComponentsTag.size());

            for (StringTag stringTag : hiddenComponentsTag) {
               hiddenComponents.add(Key.stripMinecraftNamespace(stringTag.getValue()));
            }
         }
      }

      if (hiddenComponents.containsAll(
         com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.BlockItemPacketRewriter1_21_5.HIDE_ADDITIONAL_KEYS
            .stream()
            .map(StructuredDataKey::identifier)
            .toList()
      )) {
         componentsTag.put("hide_additional_tooltip", new CompoundTag());
      }

      ListTag<CompoundTag> attributeModifiers = TagUtil.getNamespacedCompoundTagList(componentsTag, "attribute_modifiers");
      if (attributeModifiers != null) {
         TagUtil.removeNamespaced(componentsTag, "attribute_modifiers");
         CompoundTag attributesParent = new CompoundTag();
         attributesParent.put("modifiers", attributeModifiers);
         attributesParent.putBoolean("show_in_tooltip", hiddenComponents.contains("attribute_modifiers"));
         componentsTag.put("attribute_modifiers", attributesParent);
      }

      NumberTag dyedColor = TagUtil.getNamespacedNumberTag(componentsTag, "dyed_color");
      if (dyedColor != null) {
         TagUtil.removeNamespaced(componentsTag, "dyed_color");
         CompoundTag dyedColorParent = new CompoundTag();
         dyedColorParent.put("rgb", dyedColor);
         dyedColorParent.putBoolean("show_in_tooltip", hiddenComponents.contains("dyed_color"));
         componentsTag.put("dyed_color", dyedColorParent);
      }

      this.updateShowInTooltip(componentsTag, "unbreakable", hiddenComponents);
      this.updateShowInTooltip(componentsTag, "dyed_color", hiddenComponents);
      this.updateShowInTooltip(componentsTag, "trim", hiddenComponents);
      this.updateShowInTooltip(componentsTag, "jukebox_playable", hiddenComponents);
      this.handleAdventureModePredicate(componentsTag, "can_place_on", hiddenComponents);
      this.handleAdventureModePredicate(componentsTag, "can_break", hiddenComponents);
      this.handleEnchantments(componentsTag, "enchantments", hiddenComponents);
      this.handleEnchantments(componentsTag, "stored_enchantments", hiddenComponents);
      this.removeDataComponents(componentsTag, new StructuredDataKey[]{StructuredDataKey.INSTRUMENT1_21_5, StructuredDataKey.JUKEBOX_PLAYABLE1_21_5});
   }

   private void updateShowInTooltip(CompoundTag tag, String key, Set<String> hiddenComponents) {
      CompoundTag data = TagUtil.getNamespacedCompoundTag(tag, key);
      if (data != null) {
         data.putBoolean("show_in_tooltip", !hiddenComponents.contains(key));
      }
   }

   private void handleAdventureModePredicate(CompoundTag componentsTag, String key, Set<String> hiddenComponents) {
      ListTag<CompoundTag> blockPredicates = TagUtil.getNamespacedCompoundTagList(componentsTag, key);
      if (blockPredicates != null) {
         this.removeDataComponents(componentsTag, new String[]{key});
         CompoundTag predicate = new CompoundTag();
         predicate.put("predicates", blockPredicates);
         predicate.putBoolean("show_in_tooltip", !hiddenComponents.contains(key));
         componentsTag.put(key, predicate);
      }
   }

   private void handleEnchantments(CompoundTag componentsTag, String key, Set<String> hiddenComponents) {
      CompoundTag levels = TagUtil.getNamespacedCompoundTag(componentsTag, key);
      if (levels != null) {
         TagUtil.removeNamespaced(componentsTag, key);
         CompoundTag enchantments = new CompoundTag();
         enchantments.put("levels", levels);
         enchantments.putBoolean("show_in_tooltip", !hiddenComponents.contains(key));
         componentsTag.put(key, enchantments);
      }
   }

   private void insertUglyJson(CompoundTag componentsTag, UserConnection connection) {
      this.insertUglyJson(componentsTag, "item_name", connection);
      this.insertUglyJson(componentsTag, "custom_name", connection);
      String loreKey = TagUtil.getNamespacedTagKey(componentsTag, "lore");
      ListTag<?> lore = componentsTag.getListTag(loreKey);
      if (lore != null) {
         componentsTag.put(loreKey, this.updateComponentList(connection, lore));
      }
   }

   public ListTag<StringTag> updateComponentList(UserConnection connection, ListTag<?> messages) {
      ListTag<StringTag> updatedMessages = new ListTag(StringTag.class);

      for (Tag message : messages) {
         updatedMessages.add(new StringTag(this.toUglyJson(connection, message)));
      }

      return updatedMessages;
   }

   private void insertUglyJson(CompoundTag componentsTag, String key, UserConnection connection) {
      String actualKey = TagUtil.getNamespacedTagKey(componentsTag, key);
      Tag tag = componentsTag.get(actualKey);
      if (tag != null) {
         componentsTag.putString(actualKey, this.toUglyJson(connection, tag));
      }
   }

   private void updateShowEntityHover(CompoundTag hoverEventTag) {
      CompoundTag contents = new CompoundTag();
      hoverEventTag.put("contents", contents);
      Tag nameTag = hoverEventTag.remove("name");
      if (nameTag != null) {
         contents.put("name", nameTag);
      }

      if (hoverEventTag.remove("id") instanceof StringTag idTag) {
         idTag.setValue(this.protocol.getEntityRewriter().mappedEntityIdentifier(idTag.getValue()));
         contents.put("type", idTag);
      }

      Tag uuidTag = hoverEventTag.remove("uuid");
      if (uuidTag != null) {
         contents.put("id", uuidTag);
      }
   }

   String toUglyJson(UserConnection connection, Tag value) {
      this.processTag(connection, value);
      return SerializerVersion.V1_21_4.toString(SerializerVersion.V1_21_4.toComponent(value));
   }

   protected void handleWrittenBookContents(UserConnection connection, CompoundTag tag) {
      CompoundTag book = TagUtil.getNamespacedCompoundTag(tag, "written_book_content");
      if (book != null) {
         ListTag<CompoundTag> pagesTag = book.getListTag("pages", CompoundTag.class);
         if (pagesTag != null) {
            for (CompoundTag compoundTag : pagesTag) {
               Tag raw = compoundTag.get("raw");
               compoundTag.putString("raw", this.toUglyJson(connection, raw));
               Tag filtered = compoundTag.get("filtered");
               if (filtered != null) {
                  compoundTag.putString("filtered", this.toUglyJson(connection, raw));
               }
            }
         }
      }
   }
}
