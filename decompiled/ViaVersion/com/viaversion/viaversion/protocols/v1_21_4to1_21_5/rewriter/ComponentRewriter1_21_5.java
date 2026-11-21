package com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.rewriter.text.ComponentRewriterBase;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.StringUtil;
import com.viaversion.viaversion.util.TagUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ComponentRewriter1_21_5 extends JsonNBTComponentRewriter<ClientboundPacket1_21_2> {
   public ComponentRewriter1_21_5(Protocol1_21_4To1_21_5 protocol) {
      super(protocol, ComponentRewriterBase.ReadType.NBT);
   }

   @Override
   protected void processCompoundTag(UserConnection connection, CompoundTag tag) {
      super.processCompoundTag(connection, tag);
      if (tag.remove("hoverEvent") instanceof CompoundTag hoverEvent) {
         tag.put("hover_event", hoverEvent);
      }

      if (tag.remove("clickEvent") instanceof CompoundTag clickEvent) {
         try {
            this.updateClickEvent(clickEvent);
         } catch (URISyntaxException | IllegalArgumentException var5) {
            return;
         }

         tag.put("click_event", clickEvent);
      }
   }

   @Override
   protected void handleShowItem(UserConnection connection, CompoundTag itemTag, @Nullable CompoundTag componentsTag) {
      super.handleShowItem(connection, itemTag, componentsTag);
      if (componentsTag != null) {
         CompoundTag tooltipDisplay = new CompoundTag();
         boolean hideTooltip = TagUtil.removeNamespaced(componentsTag, "hide_tooltip");
         ListTag<StringTag> hiddenComponents = new ListTag<>(StringTag.class);
         if (TagUtil.removeNamespaced(componentsTag, "hide_additional_tooltip")) {
            for (StructuredDataKey<?> key : BlockItemPacketRewriter1_21_5.HIDE_ADDITIONAL_KEYS) {
               hiddenComponents.add(new StringTag(key.identifier()));
            }
         }

         this.updateHiddenComponents(componentsTag, "unbreakable", hiddenComponents);
         this.updateHiddenComponents(componentsTag, "can_place_on", hiddenComponents);
         this.updateHiddenComponents(componentsTag, "can_break", hiddenComponents);
         this.updateHiddenComponents(componentsTag, "dyed_color", hiddenComponents);
         this.updateHiddenComponents(componentsTag, "attribute_modifiers", hiddenComponents);
         this.updateHiddenComponents(componentsTag, "trim", hiddenComponents);
         this.updateHiddenComponents(componentsTag, "enchantments", hiddenComponents);
         this.updateHiddenComponents(componentsTag, "stored_enchantments", hiddenComponents);
         this.updateHiddenComponents(componentsTag, "jukebox_playable", hiddenComponents);
         if (hideTooltip || !hiddenComponents.isEmpty()) {
            tooltipDisplay.putBoolean("hide_tooltip", hideTooltip);
            tooltipDisplay.put("hidden_components", hiddenComponents);
            componentsTag.put("tooltip_display", tooltipDisplay);
         }

         CompoundTag attributeModifiers = TagUtil.getNamespacedCompoundTag(componentsTag, "attribute_modifiers");
         if (attributeModifiers != null) {
            this.removeDataComponents(componentsTag, new String[]{"attribute_modifiers"});
            componentsTag.put("attribute_modifiers", attributeModifiers.get("modifiers"));
         }

         CompoundTag dyedColor = TagUtil.getNamespacedCompoundTag(componentsTag, "dyed_color");
         if (dyedColor != null) {
            this.removeDataComponents(componentsTag, new String[]{"dyed_color"});
            componentsTag.put("dyed_color", dyedColor.get("rgb"));
         }

         this.handleAdventureModePredicate(componentsTag, "can_break");
         this.handleAdventureModePredicate(componentsTag, "can_place_on");
         this.handleEnchantments(componentsTag, "enchantments");
         this.handleEnchantments(componentsTag, "stored_enchantments");
         this.updateUglyJson(componentsTag, connection);
         this.removeDataComponents(componentsTag, new StructuredDataKey[]{StructuredDataKey.INSTRUMENT1_21_2, StructuredDataKey.JUKEBOX_PLAYABLE1_21});
      }
   }

   private void updateHiddenComponents(CompoundTag componentsTag, String key, ListTag<StringTag> hiddenComponents) {
      CompoundTag component = TagUtil.getNamespacedCompoundTag(componentsTag, key);
      if (component != null) {
         boolean showInTooltip = component.getBoolean("show_in_tooltip", true);
         if (!showInTooltip) {
            hiddenComponents.add(new StringTag(key));
         }

         component.remove("show_in_tooltip");
      }
   }

   private void handleAdventureModePredicate(CompoundTag componentsTag, String key) {
      CompoundTag predicate = TagUtil.getNamespacedCompoundTag(componentsTag, key);
      if (predicate != null) {
         ListTag<CompoundTag> blockPredicates = predicate.getListTag("predicates", CompoundTag.class);
         this.removeDataComponents(componentsTag, new String[]{key});
         componentsTag.put(key, blockPredicates);
      }
   }

   private void handleEnchantments(CompoundTag componentsTag, String key) {
      CompoundTag enchantments = TagUtil.getNamespacedCompoundTag(componentsTag, key);
      if (enchantments != null && enchantments.remove("levels") instanceof CompoundTag levels) {
         enchantments.putAll(levels);
      }
   }

   @Override
   protected void handleHoverEvent(UserConnection connection, CompoundTag hoverEventTag) {
      String action = hoverEventTag.getString("action");
      if (action != null) {
         switch (action) {
            case "show_text":
               this.updateShowTextHover(connection, hoverEventTag);
               break;
            case "show_entity":
               this.updateShowEntityHover(connection, hoverEventTag);
               break;
            case "show_item":
               this.updateShowItemHover(connection, hoverEventTag);
         }
      }
   }

   private void updateClickEvent(CompoundTag clickEventTag) throws URISyntaxException {
      String action = clickEventTag.getString("action");
      if (action != null) {
         if (action.equals("open_url")) {
            StringTag url = clickEventTag.getStringTag("value");
            URI uri = new URI(url.getValue());
            if (!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme())) {
               throw new IllegalArgumentException("Invalid URL");
            }

            clickEventTag.put("url", url);
         } else if (action.equals("change_page")) {
            int page = Integer.parseInt(clickEventTag.getString("value"));
            if (page < 1) {
               throw new IllegalArgumentException("Invalid page number");
            }

            clickEventTag.putInt("page", page);
         } else if (action.equals("run_command") || action.equals("suggest_command")) {
            String value = clickEventTag.getString("value");
            StringBuilder command = new StringBuilder();

            for (int i = 0; i < value.length(); i++) {
               char c = value.charAt(i);
               if (c != 167 && c >= ' ' && c != 127) {
                  command.append(c);
               } else if (action.equals("suggest_command") && c == '\n') {
                  command.append(c);
               }
            }

            clickEventTag.putString("command", command.toString());
         }
      }
   }

   private void updateShowTextHover(UserConnection connection, CompoundTag hoverEventTag) {
      Tag value = hoverEventTag.get("value");
      if (value != null) {
         this.processTag(connection, value);
      } else {
         Tag contents = hoverEventTag.remove("contents");
         this.processTag(connection, contents);
         hoverEventTag.put("value", contents);
      }
   }

   private void updateShowItemHover(UserConnection connection, CompoundTag hoverEventTag) {
      this.convertLegacyItemContents(hoverEventTag);
      Tag contentsTag = hoverEventTag.remove("contents");
      if (contentsTag instanceof CompoundTag compoundContents) {
         Tag countTag = compoundContents.get("count");
         if (countTag != null) {
            hoverEventTag.put("count", countTag);
         }

         Tag idTag = compoundContents.get("id");
         if (idTag != null) {
            hoverEventTag.put("id", idTag);
         }

         CompoundTag componentsTag = compoundContents.getCompoundTag("components");
         this.handleShowItem(connection, compoundContents, componentsTag);
         if (componentsTag != null) {
            hoverEventTag.put("components", componentsTag);
         }
      } else if (contentsTag instanceof StringTag inlinedContents) {
         hoverEventTag.put("id", inlinedContents);
      }
   }

   private void updateUglyJson(CompoundTag componentsTag, UserConnection connection) {
      this.updateUglyJson(componentsTag, "item_name", connection);
      this.updateUglyJson(componentsTag, "custom_name", connection);
      String loreKey = TagUtil.getNamespacedTagKey(componentsTag, "lore");
      ListTag<StringTag> lore = componentsTag.getListTag(loreKey, StringTag.class);
      if (lore != null) {
         componentsTag.put(loreKey, this.updateComponentList(connection, lore, false));
      }
   }

   public ListTag<CompoundTag> updateComponentList(UserConnection connection, ListTag<StringTag> messages, boolean skipInvalid) {
      ListTag<CompoundTag> updatedMessages = new ListTag<>(CompoundTag.class);

      for (StringTag message : messages) {
         Tag output;
         try {
            output = skipInvalid ? this.uglyJsonToTagUncaught(connection, message.getValue()) : this.uglyJsonToTag(connection, message.getValue());
         } catch (Exception var9) {
            continue;
         }

         CompoundTag wrappedComponent = new CompoundTag();
         wrappedComponent.putString("text", "");
         wrappedComponent.put("extra", new ListTag<>(List.of(output)));
         updatedMessages.add(wrappedComponent);
      }

      return updatedMessages;
   }

   private void updateUglyJson(CompoundTag componentsTag, String key, UserConnection connection) {
      String actualKey = TagUtil.getNamespacedTagKey(componentsTag, key);
      String json = componentsTag.getString(actualKey);
      if (json != null) {
         componentsTag.put(actualKey, this.uglyJsonToTag(connection, json));
      }
   }

   private void updateShowEntityHover(UserConnection connection, CompoundTag hoverEventTag) {
      this.convertLegacyEntityContents(hoverEventTag);
      if (hoverEventTag.remove("contents") instanceof CompoundTag contents) {
         Tag nameTag = contents.get("name");
         if (nameTag != null) {
            this.processTag(connection, nameTag);
            hoverEventTag.put("name", nameTag);
         }

         Tag uuidTag = contents.get("id");
         if (uuidTag != null) {
            hoverEventTag.put("uuid", uuidTag);
         }

         StringTag typeTag = contents.getStringTag("type");
         if (typeTag != null) {
            typeTag.setValue(this.protocol.getEntityRewriter().mappedEntityIdentifier(typeTag.getValue()));
            hoverEventTag.put("id", typeTag);
         }
      }
   }

   public Tag uglyJsonToTagUncaught(UserConnection connection, String value) {
      Tag contents = SerializerVersion.V1_21_4.toTag(SerializerVersion.V1_21_4.toComponent(value));
      this.processTag(connection, contents);
      return contents;
   }

   public Tag uglyJsonToTag(UserConnection connection, String value) {
      try {
         return this.uglyJsonToTagUncaught(connection, value);
      } catch (Exception var4) {
         if (Via.getConfig().logTextComponentConversionErrors()) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting json text component: " + StringUtil.forLogging(value), (Throwable)var4);
         }

         return new StringTag("<error>");
      }
   }

   @Override
   protected void handleWrittenBookContents(UserConnection connection, CompoundTag tag) {
      CompoundTag book = TagUtil.getNamespacedCompoundTag(tag, "written_book_content");
      if (book != null) {
         ListTag<CompoundTag> pagesTag = book.getListTag("pages", CompoundTag.class);
         if (pagesTag != null) {
            for (CompoundTag compoundTag : pagesTag) {
               String raw = compoundTag.getString("raw");
               compoundTag.put("raw", this.uglyJsonToTag(connection, raw));
               String filtered = compoundTag.getString("filtered");
               if (filtered != null) {
                  compoundTag.put("filtered", this.uglyJsonToTag(connection, filtered));
               }
            }
         }
      }
   }

   @Override
   protected SerializerVersion inputSerializerVersion() {
      return SerializerVersion.V1_21_4;
   }
}
