package com.viaversion.viabackwards.protocol.v1_21to1_20_5.rewriter;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.rewriters.BackwardsFullStructuredItemRewriter;
import com.viaversion.viabackwards.api.rewriters.StructuredEnchantmentRewriter;
import com.viaversion.viabackwards.protocol.v1_21to1_20_5.Protocol1_21To1_20_5;
import com.viaversion.viabackwards.protocol.v1_21to1_20_5.storage.EnchantmentsPaintingsStorage;
import com.viaversion.viabackwards.protocol.v1_21to1_20_5.storage.OpenScreenStorage;
import com.viaversion.viabackwards.protocol.v1_21to1_20_5.storage.PlayerRotationStorage;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.EitherHolder;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_21;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.JukeboxPlayable;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_21.AttributeModifier;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_21.ModifierData;
import com.viaversion.viaversion.api.minecraft.item.data.JukeboxPlayable.JukeboxSong;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap.Entry;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectIterator;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Enchantments1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.rewriter.IdRewriteFunction;
import com.viaversion.viaversion.util.SerializerVersion;
import java.util.ArrayList;
import java.util.List;

public final class BlockItemPacketRewriter1_21 extends BackwardsFullStructuredItemRewriter<ClientboundPacket1_21, ServerboundPacket1_20_5, Protocol1_21To1_20_5> {
   private final StructuredEnchantmentRewriter enchantmentRewriter = new StructuredEnchantmentRewriter(this);

   public BlockItemPacketRewriter1_21(Protocol1_21To1_20_5 protocol) {
      super(protocol);
   }

   public void registerPackets() {
      ((Protocol1_21To1_20_5)this.protocol).replaceClientbound(ClientboundPackets1_21.OPEN_SCREEN, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int menuType = (Integer)wrapper.passthrough(Types.VAR_INT);
         ((OpenScreenStorage)wrapper.user().get(OpenScreenStorage.class)).setMenuType(menuType);
         ((Protocol1_21To1_20_5)this.protocol).getComponentRewriter().passthroughAndProcess(wrapper);
      });
      ((Protocol1_21To1_20_5)this.protocol).registerClientbound(ClientboundPackets1_21.CONTAINER_SET_DATA, wrapper -> {
         wrapper.passthrough(Types.UNSIGNED_BYTE);
         short property = (Short)wrapper.passthrough(Types.SHORT);
         if (property >= 4 && property <= 6) {
            OpenScreenStorage openScreenStorage = (OpenScreenStorage)wrapper.user().get(OpenScreenStorage.class);
            if (openScreenStorage.menuType() != 13) {
               return;
            }

            short enchantmentId = (Short)wrapper.read(Types.SHORT);
            EnchantmentsPaintingsStorage storage = (EnchantmentsPaintingsStorage)wrapper.user().get(EnchantmentsPaintingsStorage.class);
            String key = storage.enchantments().idToKey(enchantmentId);
            int mappedId = key != null ? Enchantments1_20_5.keyToId(key) : -1;
            wrapper.write(Types.SHORT, (short)mappedId);
         }
      });
      ((Protocol1_21To1_20_5)this.protocol).registerClientbound(ClientboundPackets1_21.HORSE_SCREEN_OPEN, wrapper -> {
         wrapper.passthrough(Types.UNSIGNED_BYTE);
         int columns = (Integer)wrapper.read(Types.VAR_INT);
         wrapper.write(Types.VAR_INT, columns * 3 + 1);
      });
      ((Protocol1_21To1_20_5)this.protocol).replaceClientbound(ClientboundPackets1_21.LEVEL_EVENT, wrapper -> {
         int event = (Integer)wrapper.passthrough(Types.INT);
         wrapper.passthrough(Types.BLOCK_POSITION1_14);
         int data = (Integer)wrapper.read(Types.INT);
         if (event == 1010) {
            int itemId = ((EnchantmentsPaintingsStorage)wrapper.user().get(EnchantmentsPaintingsStorage.class)).jubeboxSongToItem(data);
            if (itemId == -1) {
               wrapper.cancel();
               return;
            }

            wrapper.write(Types.INT, itemId);
         } else if (event == 2001) {
            wrapper.write(Types.INT, ((Protocol1_21To1_20_5)this.protocol).getMappingData().getNewBlockStateId(data));
         } else {
            wrapper.write(Types.INT, data);
         }
      });
      ((Protocol1_21To1_20_5)this.protocol).registerServerbound(ServerboundPackets1_20_5.USE_ITEM, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         PlayerRotationStorage rotation = (PlayerRotationStorage)wrapper.user().get(PlayerRotationStorage.class);
         wrapper.write(Types.FLOAT, rotation.yaw());
         wrapper.write(Types.FLOAT, rotation.pitch());
      });
      new RecipeRewriter1_20_3(this.protocol).register1_20_5(ClientboundPackets1_21.UPDATE_RECIPES);
   }

   public Item handleItemToClient(UserConnection connection, Item item) {
      if (item.isEmpty()) {
         return item;
      } else {
         StructuredDataContainer data = item.dataContainer();
         data.setIdLookup(this.protocol, true);
         EnchantmentsPaintingsStorage storage = (EnchantmentsPaintingsStorage)connection.get(EnchantmentsPaintingsStorage.class);
         IdRewriteFunction idRewriteFunction = id -> {
            String key = storage.enchantments().idToKey(id);
            return key != null ? Enchantments1_20_5.keyToId(key) : -1;
         };
         StructuredEnchantmentRewriter.DescriptionSupplier descriptionSupplier = (id, level) -> {
            Tag description = storage.enchantmentDescription(id);
            if (description == null) {
               return new StringTag("Unknown enchantment");
            } else {
               TextComponent component = SerializerVersion.V1_20_5.toComponent(description);
               component.getStyle().setItalic(false);
               component.getStyle().setFormatting(TextFormatting.GRAY);
               if (level != 1 || storage.enchantmentMaxLevel(id) != 1) {
                  component.getSiblings().add(new StringComponent(" "));
                  component.getSiblings().add(new TranslationComponent("enchantment.level.%s".formatted(level), new Object[0]));
               }

               return SerializerVersion.V1_20_5.toTag(component);
            }
         };
         this.enchantmentRewriter.rewriteEnchantmentsToClient(data, StructuredDataKey.ENCHANTMENTS1_20_5, idRewriteFunction, descriptionSupplier, false);
         this.enchantmentRewriter.rewriteEnchantmentsToClient(data, StructuredDataKey.STORED_ENCHANTMENTS1_20_5, idRewriteFunction, descriptionSupplier, true);
         int identifier = item.identifier();
         this.backupInconvertibleData(item);
         item = super.handleItemToClient(connection, item);
         com.viaversion.viaversion.protocols.v1_20_5to1_21.rewriter.BlockItemPacketRewriter1_21.downgradeItemData(item);
         if (data.has(StructuredDataKey.RARITY)) {
            return item;
         } else {
            boolean trident = identifier == 1188;
            if (trident || identifier == 1200) {
               data.set(StructuredDataKey.RARITY, trident ? 3 : 1);
               this.saveTag(this.createCustomTag(item), new ByteTag(true), "rarity");
            }

            return item;
         }
      }
   }

   @Override
   public Item handleItemToServer(UserConnection connection, Item item) {
      if (item.isEmpty()) {
         return item;
      } else {
         StructuredDataContainer data = item.dataContainer();
         data.setIdLookup(this.protocol, false);
         EnchantmentsPaintingsStorage storage = (EnchantmentsPaintingsStorage)connection.get(EnchantmentsPaintingsStorage.class);
         this.rewriteEnchantmentToServer(storage, item, StructuredDataKey.ENCHANTMENTS1_20_5);
         this.rewriteEnchantmentToServer(storage, item, StructuredDataKey.STORED_ENCHANTMENTS1_20_5);
         this.enchantmentRewriter.handleToServer(item);
         item = super.handleItemToServer(connection, item);
         com.viaversion.viaversion.protocols.v1_20_5to1_21.rewriter.BlockItemPacketRewriter1_21.updateItemData(item);
         this.restoreInconvertibleData(item);
         CompoundTag customData = (CompoundTag)data.get(StructuredDataKey.CUSTOM_DATA);
         if (customData == null) {
            return item;
         } else {
            if (customData.remove(this.nbtTagName("rarity")) != null) {
               data.remove(StructuredDataKey.RARITY);
               this.removeCustomTag(data, customData);
            }

            return item;
         }
      }
   }

   private void rewriteEnchantmentToServer(EnchantmentsPaintingsStorage storage, Item item, StructuredDataKey<Enchantments> key) {
      Enchantments enchantments = (Enchantments)item.dataContainer().get(key);
      if (enchantments != null) {
         List<BlockItemPacketRewriter1_21.PendingIdChange> updatedIds = new ArrayList<>();
         ObjectIterator var6 = enchantments.enchantments().int2IntEntrySet().iterator();

         while (var6.hasNext()) {
            Entry entry = (Entry)var6.next();
            int id = entry.getIntKey();
            String enchantmentKey = Enchantments1_20_5.idToKey(id);
            if (enchantmentKey != null) {
               int mappedId = storage.enchantments().keyToId(enchantmentKey);
               if (id != mappedId) {
                  int level = entry.getIntValue();
                  updatedIds.add(new BlockItemPacketRewriter1_21.PendingIdChange(id, mappedId, level));
               }
            }
         }

         for (BlockItemPacketRewriter1_21.PendingIdChange change : updatedIds) {
            enchantments.remove(change.id);
         }

         for (BlockItemPacketRewriter1_21.PendingIdChange change : updatedIds) {
            enchantments.add(change.mappedId, change.level);
         }
      }
   }

   private void backupInconvertibleData(Item item) {
      StructuredDataContainer data = item.dataContainer();
      data.setIdLookup(this.protocol, true);
      CompoundTag backupTag = new CompoundTag();
      JukeboxPlayable jukeboxPlayable = (JukeboxPlayable)data.get(StructuredDataKey.JUKEBOX_PLAYABLE1_21);
      if (jukeboxPlayable != null) {
         CompoundTag tag = new CompoundTag();
         if (jukeboxPlayable.song().hasHolder()) {
            Holder<JukeboxSong> songHolder = jukeboxPlayable.song().holder();
            tag.put("song", this.holderToTag(songHolder, (song, songTag) -> {
               this.saveSoundEventHolder(songTag, song.soundEvent());
               songTag.put("description", song.description());
               songTag.putFloat("length_in_seconds", song.lengthInSeconds());
               songTag.putInt("comparator_output", song.comparatorOutput());
            }));
         } else {
            tag.putString("song_identifier", jukeboxPlayable.song().key());
         }

         tag.putBoolean("show_in_tooltip", jukeboxPlayable.showInTooltip());
         backupTag.put("jukebox_playable", tag);
      }

      AttributeModifiers1_21 attributeModifiers = (AttributeModifiers1_21)data.get(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21);
      if (attributeModifiers != null) {
         ListTag<StringTag> attributeIds = new ListTag(StringTag.class);

         for (AttributeModifier modifier : attributeModifiers.modifiers()) {
            attributeIds.add(new StringTag(modifier.modifier().id()));
         }

         backupTag.put("attribute_modifiers", attributeIds);
      }

      if (!backupTag.isEmpty()) {
         this.saveTag(this.createCustomTag(item), backupTag, "inconvertible_data");
      }
   }

   private void restoreInconvertibleData(Item item) {
      StructuredDataContainer data = item.dataContainer();
      CompoundTag customData = (CompoundTag)data.get(StructuredDataKey.CUSTOM_DATA);
      if (customData != null && customData.remove(this.nbtTagName("inconvertible_data")) instanceof CompoundTag tag) {
         CompoundTag jukeboxPlayableTag = tag.getCompoundTag("jukebox_playable");
         if (jukeboxPlayableTag != null) {
            String songIdentifier = jukeboxPlayableTag.getString("song_identifier");
            EitherHolder<JukeboxSong> song;
            if (songIdentifier != null) {
               song = EitherHolder.of(songIdentifier);
            } else {
               song = EitherHolder.of(this.restoreHolder(jukeboxPlayableTag, "song", songTag -> {
                  Holder<SoundEvent> soundEvent = this.restoreSoundEventHolder(songTag);
                  Tag description = songTag.get("description");
                  float lengthInSeconds = songTag.getFloat("length_in_seconds");
                  int comparatorOutput = songTag.getInt("comparator_output");
                  return new JukeboxSong(soundEvent, description, lengthInSeconds, comparatorOutput);
               }));
            }

            JukeboxPlayable jukeboxPlayable = new JukeboxPlayable(song, jukeboxPlayableTag.getBoolean("show_in_tooltip"));
            data.set(StructuredDataKey.JUKEBOX_PLAYABLE1_21, jukeboxPlayable);
         }

         ListTag<StringTag> attributeIds = tag.getListTag("attribute_modifiers", StringTag.class);
         AttributeModifiers1_21 attributeModifiers = (AttributeModifiers1_21)data.get(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21);
         if (attributeIds != null && attributeModifiers != null && attributeIds.size() == attributeModifiers.modifiers().length) {
            for (int i = 0; i < attributeIds.size(); i++) {
               String id = ((StringTag)attributeIds.get(i)).getValue();
               AttributeModifier modifier = attributeModifiers.modifiers()[i];
               ModifierData updatedModifierData = new ModifierData(id, modifier.modifier().amount(), modifier.modifier().operation());
               attributeModifiers.modifiers()[i] = new AttributeModifier(modifier.attribute(), updatedModifierData, modifier.slotType());
            }
         }

         this.removeCustomTag(data, customData);
      }
   }

   private record PendingIdChange(int id, int mappedId, int level) {
   }
}
