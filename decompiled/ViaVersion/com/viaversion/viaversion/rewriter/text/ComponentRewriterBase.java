package com.viaversion.viaversion.rewriter.text;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.data.ChatType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.rewriter.ComponentRewriter;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.libs.gson.JsonSyntaxException;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.TagUtil;
import java.util.BitSet;
import java.util.Collection;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class ComponentRewriterBase<C extends ClientboundPacketType> implements ComponentRewriter {
   protected final Protocol<C, ?, ?, ?> protocol;
   protected final ComponentRewriterBase.ReadType type;

   protected ComponentRewriterBase(Protocol<C, ?, ?, ?> protocol, ComponentRewriterBase.ReadType type) {
      this.protocol = protocol;
      this.type = type;
   }

   public void registerComponentPacket(C packetType) {
      this.protocol.registerClientbound(packetType, this::passthroughAndProcess);
   }

   public void registerBossEvent(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.UUID);
         int action = wrapper.passthrough(Types.VAR_INT);
         if (action == 0 || action == 3) {
            this.passthroughAndProcess(wrapper);
         }
      });
   }

   public void registerLoginDisconnect() {
      this.protocol
         .registerClientbound(
            State.LOGIN, ClientboundLoginPackets.LOGIN_DISCONNECT, wrapper -> this.processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT))
         );
   }

   public void registerOpenScreen1_14(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
      });
   }

   public void registerTabList(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         this.passthroughAndProcess(wrapper);
         this.passthroughAndProcess(wrapper);
      });
   }

   public void registerSetObjective(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.STRING);
         byte action = wrapper.passthrough(Types.BYTE);
         if (action == 0 || action == 2) {
            this.passthroughAndProcess(wrapper);
         }
      });
   }

   public void registerSetScore1_20_3(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.VAR_INT);
         this.passthroughAndProcessOptional(wrapper);
         if (wrapper.passthrough(Types.BOOLEAN)) {
            int numberFormatType = wrapper.passthrough(Types.VAR_INT);
            if (numberFormatType == 1) {
               this.passthroughAndProcess(wrapper);
            } else if (numberFormatType == 2) {
               this.passthroughAndProcess(wrapper);
            }
         }
      });
   }

   public void registerSetPlayerTeam1_13(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.STRING);
         byte action = wrapper.passthrough(Types.BYTE);
         if (action == 0 || action == 2) {
            this.passthroughAndProcess(wrapper);
            wrapper.passthrough(Types.BYTE);
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.VAR_INT);
            this.passthroughAndProcess(wrapper);
            this.passthroughAndProcess(wrapper);
         }
      });
   }

   public void registerSetPlayerTeam1_21_5(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.STRING);
         byte action = wrapper.passthrough(Types.BYTE);
         if (action == 0 || action == 2) {
            this.passthroughAndProcess(wrapper);
            wrapper.passthrough(Types.BYTE);
            wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.VAR_INT);
            this.passthroughAndProcess(wrapper);
            this.passthroughAndProcess(wrapper);
         }
      });
   }

   public void registerSetPlayerTeam26_2(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.STRING);
         byte action = wrapper.passthrough(Types.BYTE);
         if (action == 0 || action == 2) {
            this.passthroughAndProcess(wrapper);
            this.passthroughAndProcess(wrapper);
            this.passthroughAndProcess(wrapper);
            wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.BOOL_OPTIONAL_VAR_INT);
            wrapper.passthrough(Types.BYTE);
         }
      });
   }

   public void registerPlayerInfoUpdate1_21_4(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         BitSet actions = wrapper.passthrough(Types.PROFILE_ACTIONS_ENUM1_21_4);
         if (actions.get(5)) {
            int entries = wrapper.passthrough(Types.VAR_INT);

            for (int i = 0; i < entries; i++) {
               wrapper.passthrough(Types.UUID);
               if (actions.get(0)) {
                  wrapper.passthrough(Types.STRING);
                  wrapper.passthrough(Types.PROFILE_PROPERTY_ARRAY);
               }

               if (actions.get(1) && wrapper.passthrough(Types.BOOLEAN)) {
                  wrapper.passthrough(Types.UUID);
                  wrapper.passthrough(Types.PROFILE_KEY);
               }

               if (actions.get(2)) {
                  wrapper.passthrough(Types.VAR_INT);
               }

               if (actions.get(3)) {
                  wrapper.passthrough(Types.BOOLEAN);
               }

               if (actions.get(4)) {
                  wrapper.passthrough(Types.VAR_INT);
               }

               this.processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG));
               if (actions.get(6)) {
                  wrapper.passthrough(Types.VAR_INT);
               }

               if (actions.get(7)) {
                  wrapper.passthrough(Types.BOOLEAN);
               }
            }
         }
      });
   }

   public void registerPlayerCombatKill1_20(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         this.passthroughAndProcess(wrapper);
      });
   }

   public void registerDisguisedChat(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         this.passthroughAndProcess(wrapper);
         wrapper.passthrough(ChatType.TYPE);
         this.passthroughAndProcess(wrapper);
         this.passthroughAndProcessOptional(wrapper);
      });
   }

   @Override
   public void passthroughAndProcess(PacketWrapper wrapper) {
      switch (this.type) {
         case JSON:
            this.processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT));
            break;
         case NBT:
            this.processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_TAG));
      }
   }

   public void passthroughAndProcessOptional(PacketWrapper wrapper) {
      switch (this.type) {
         case JSON:
            this.processText(wrapper.user(), wrapper.passthrough(Types.OPTIONAL_COMPONENT));
            break;
         case NBT:
            this.processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG));
      }
   }

   @Override
   public JsonElement processText(UserConnection connection, String value) {
      try {
         JsonElement root = JsonParser.parseString(value);
         this.processText(connection, root);
         return root;
      } catch (JsonSyntaxException var4) {
         if (Via.getManager().isDebug()) {
            this.protocol.getLogger().severe("Error when trying to parse json: " + value);
            throw var4;
         } else {
            return new JsonPrimitive(value);
         }
      }
   }

   @Override
   public void processText(UserConnection connection, JsonElement element) {
      if (element != null && !element.isJsonNull()) {
         if (element.isJsonArray()) {
            this.processJsonArray(connection, element.getAsJsonArray());
         } else if (element.isJsonObject()) {
            this.processJsonObject(connection, element.getAsJsonObject());
         }
      }
   }

   protected void processJsonArray(UserConnection connection, JsonArray array) {
      for (JsonElement jsonElement : array) {
         this.processText(connection, jsonElement);
      }
   }

   protected void processJsonObject(UserConnection connection, JsonObject object) {
      JsonElement translate = object.get("translate");
      if (translate != null && translate.isJsonPrimitive()) {
         this.handleTranslate(object, translate.getAsString());
         JsonElement with = object.get("with");
         if (with != null && with.isJsonArray()) {
            this.processJsonArray(connection, with.getAsJsonArray());
         }
      }

      JsonElement extra = object.get("extra");
      if (extra != null && extra.isJsonArray()) {
         this.processJsonArray(connection, extra.getAsJsonArray());
      }

      JsonElement hoverEvent = object.get(this.hoverEventKey());
      if (hoverEvent != null && hoverEvent.isJsonObject()) {
         this.handleHoverEvent(connection, hoverEvent.getAsJsonObject());
      }
   }

   protected abstract void handleHoverEvent(UserConnection var1, JsonObject var2);

   protected void handleTranslate(JsonObject object, String translate) {
   }

   @Override
   public void processTag(UserConnection connection, @Nullable Tag tag) {
      if (tag != null) {
         if (tag instanceof ListTag) {
            this.processListTag(connection, (ListTag<?>)tag);
         } else if (tag instanceof CompoundTag) {
            this.processCompoundTag(connection, (CompoundTag)tag);
         }
      }
   }

   private void processListTag(UserConnection connection, ListTag<?> tag) {
      for (Tag entry : tag) {
         this.processTag(connection, entry);
      }
   }

   protected void processCompoundTag(UserConnection connection, CompoundTag tag) {
      StringTag translate = tag.getStringTag("translate");
      if (translate != null) {
         this.handleTranslate(connection, tag, translate);
         ListTag<?> with = tag.getListTag("with");
         if (with != null) {
            this.processListTag(connection, with);
         }
      }

      ListTag<?> extra = tag.getListTag("extra");
      if (extra != null) {
         this.processListTag(connection, extra);
      }

      CompoundTag hoverEvent = tag.getCompoundTag(this.hoverEventKey());
      if (hoverEvent != null) {
         this.handleHoverEvent(connection, hoverEvent);
      }
   }

   protected void handleTranslate(UserConnection connection, CompoundTag parentTag, StringTag translateTag) {
   }

   protected abstract void handleHoverEvent(UserConnection var1, CompoundTag var2);

   @Override
   public final void handleShowItem(UserConnection connection, CompoundTag itemTag) {
      this.handleShowItem(connection, itemTag, itemTag.getCompoundTag("components"));
   }

   protected void handleShowItem(UserConnection connection, CompoundTag itemTag, @Nullable CompoundTag componentsTag) {
      StringTag idTag = itemTag.getStringTag("id");
      String mappedId = this.protocol.getMappingData().getFullItemMappings().mappedIdentifier(idTag.getValue());
      if (mappedId != null) {
         idTag.setValue(mappedId);
      }

      if (componentsTag != null) {
         this.handleNestedComponent(connection, TagUtil.getNamespacedTag(componentsTag, "item_name"));
         this.handleNestedComponent(connection, TagUtil.getNamespacedTag(componentsTag, "custom_name"));
         this.handleLore(connection, componentsTag);
         this.handleWrittenBookContents(connection, componentsTag);
         this.handleAttributeModifiers(componentsTag);
         this.handleContainerContents(connection, componentsTag);
         this.handleItemArrayContents(connection, componentsTag, "bundle_contents");
         this.handleItemArrayContents(connection, componentsTag, "charged_projectiles");
         this.handleNestedItem(connection, componentsTag, "use_remainder");
         this.handleNestedItem(connection, componentsTag, "sulfur_cube_content");
         this.handleNestedIdentifiers(componentsTag);
         this.removeDataComponents(componentsTag, "lock", "debug_stick_state");
         this.removeDataComponents(componentsTag, "equippable", "consumable", "blocks_attacks", "break_sound", "note_block_sound", "tool", "block_transformer");
      }
   }

   private void handleNestedItem(UserConnection connection, CompoundTag componentsTag, String componentKey) {
      CompoundTag useRemainder = TagUtil.getNamespacedCompoundTag(componentsTag, componentKey);
      if (useRemainder != null) {
         this.handleShowItem(connection, useRemainder);
      }
   }

   protected void handleNestedIdentifiers(CompoundTag componentsTag) {
      FullMappings soundMappings = this.protocol.getMappingData().getFullSoundMappings();
      if (!Mappings.isFullIdentity(soundMappings)) {
         if (TagUtil.getNamespacedTag(componentsTag, "jukebox_playable") instanceof CompoundTag tag) {
            this.handleSoundEventTag(tag.get("sound_event"), soundMappings);
         }

         if (TagUtil.getNamespacedTag(componentsTag, "instrument") instanceof CompoundTag tag) {
            this.handleSoundEventTag(tag.get("sound_event"), soundMappings);
         }
      }

      this.handleTypedEntityData(componentsTag, "entity_data", this.protocol.getMappingData().getEntityMappings());
      this.handleTypedEntityData(componentsTag, "block_entity_data", this.protocol.getMappingData().getBlockEntityMappings());
   }

   private void handleSoundEventTag(@Nullable Tag soundEventTag, FullMappings soundMappings) {
      if (soundEventTag instanceof StringTag stringTag) {
         String mapped = soundMappings.mappedIdentifier(stringTag.getValue());
         if (mapped != null) {
            stringTag.setValue(mapped);
         }
      } else if (soundEventTag instanceof CompoundTag compoundTag) {
         StringTag soundId = compoundTag.getStringTag("sound_id");
         if (soundId != null) {
            String mapped = soundMappings.mappedIdentifier(soundId.getValue());
            if (mapped != null) {
               soundId.setValue(mapped);
            }
         }
      }
   }

   private void handleTypedEntityData(CompoundTag componentsTag, String key, @Nullable FullMappings mappings) {
      if (!Mappings.isFullIdentity(mappings)) {
         CompoundTag data = TagUtil.getNamespacedCompoundTag(componentsTag, key);
         if (data != null) {
            StringTag idTag = data.getStringTag("id");
            if (idTag != null) {
               String mapped = mappings.mappedIdentifier(idTag.getValue());
               if (mapped != null) {
                  idTag.setValue(mapped);
               }
            }
         }
      }
   }

   protected void handleAttributeModifiers(CompoundTag tag) {
      FullMappings mappings = this.protocol.getMappingData().getAttributeMappings();
      if (!Mappings.isFullIdentity(mappings)) {
         ListTag<CompoundTag> attributeModifiers = TagUtil.getNamespacedCompoundTagList(tag, "attribute_modifiers");
         if (attributeModifiers != null) {
            attributeModifiers.getValue().removeIf(attributeTag -> {
               StringTag typeTag = attributeTag.getStringTag("type");
               if (typeTag == null) {
                  return false;
               } else {
                  String mappedId = mappings.mappedIdentifier(typeTag.getValue());
                  if (mappedId != null) {
                     typeTag.setValue(mappedId);
                     return false;
                  } else {
                     return true;
                  }
               }
            });
         }
      }
   }

   protected void handleContainerContents(UserConnection connection, CompoundTag tag) {
      ListTag<CompoundTag> container = TagUtil.getNamespacedCompoundTagList(tag, "container");
      if (container != null) {
         for (CompoundTag entryTag : container) {
            this.handleShowItem(connection, entryTag.getCompoundTag("item"));
         }
      }
   }

   protected void handleLore(UserConnection connection, CompoundTag tag) {
      ListTag<? extends Tag> loreTag = TagUtil.getNamespacedTagList(tag, "lore");
      if (loreTag != null) {
         for (Tag lore : loreTag) {
            this.handleNestedComponent(connection, lore);
         }
      }
   }

   protected void handleWrittenBookContents(UserConnection connection, CompoundTag tag) {
      CompoundTag book = TagUtil.getNamespacedCompoundTag(tag, "written_book_content");
      if (book != null) {
         ListTag<CompoundTag> pagesTag = book.getListTag("pages", CompoundTag.class);
         if (pagesTag != null) {
            for (CompoundTag compoundTag : pagesTag) {
               this.handleNestedComponent(connection, compoundTag.get("raw"));
               this.handleNestedComponent(connection, compoundTag.get("filtered"));
            }
         }
      }
   }

   protected void handleItemArrayContents(UserConnection connection, CompoundTag tag, String key) {
      ListTag<CompoundTag> container = TagUtil.getNamespacedCompoundTagList(tag, key);
      if (container != null) {
         for (CompoundTag itemTag : container) {
            this.handleShowItem(connection, itemTag);
         }
      }
   }

   protected void removeDataComponents(CompoundTag tag, Collection<StructuredDataKey<?>> keys) {
      for (StructuredDataKey<?> key : keys) {
         this.removeDataComponent(tag, key.identifier());
      }
   }

   protected void removeDataComponents(CompoundTag tag, StructuredDataKey<?>... keys) {
      for (StructuredDataKey<?> key : keys) {
         this.removeDataComponent(tag, key.identifier());
      }
   }

   protected void removeDataComponents(CompoundTag tag, String... keys) {
      for (String key : keys) {
         this.removeDataComponent(tag, key);
      }
   }

   private boolean removeDataComponent(CompoundTag tag, String key) {
      return TagUtil.removeNamespaced(tag, key) || tag.remove("!" + Key.namespaced(key)) != null || tag.remove("!" + Key.stripMinecraftNamespace(key)) != null;
   }

   protected abstract void handleNestedComponent(UserConnection var1, @Nullable Tag var2);

   protected abstract String hoverEventKey();

   public static enum ReadType {
      JSON,
      NBT;
   }
}
