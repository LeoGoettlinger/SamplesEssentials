package com.viaversion.viabackwards.api.rewriters;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.data.MappedItem;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.EitherHolder;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.CustomModelData1_21_4;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import com.viaversion.viaversion.util.ArrayUtil;
import com.viaversion.viaversion.util.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BackwardsStructuredItemRewriter<C extends ClientboundPacketType, S extends ServerboundPacketType, T extends BackwardsProtocol<C, ?, ?, S>>
   extends StructuredItemRewriter<C, S, T> {
   private static final int[] EMPTY_INT_ARRAY = new int[0];
   private static final String GLOBAL_MODEL_DATA_MARKER = "VB|injected_cmd";
   private final String nbtTagName;

   public BackwardsStructuredItemRewriter(T protocol) {
      super(protocol);
      this.nbtTagName = "VB|" + protocol.getClass().getSimpleName();
   }

   protected void backupInconvertibleData(UserConnection connection, Item item, StructuredDataContainer dataContainer, CompoundTag backupTag) {
      super.backupInconvertibleData(connection, item, dataContainer, backupTag);
      BackwardsMappingData mappingData = ((BackwardsProtocol)this.protocol).getMappingData();
      MappedItem mappedItem = mappingData != null ? mappingData.getMappedItem(item.identifier()) : null;
      if (mappedItem != null) {
         CompoundTag customTag = this.createCustomTag(item);
         customTag.putInt(this.nbtTagName("id"), item.identifier());
         if (ViaBackwards.getConfig().passOriginalItemNameToResourcePacks()) {
            if (connection.getProtocolInfo().protocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_4)) {
               this.addCustomModelData(item, mappedItem, customTag);
            } else if (!dataContainer.has(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5)) {
               dataContainer.set(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5, mappedItem.customModelData());
            }
         }

         if (!dataContainer.has(StructuredDataKey.CUSTOM_NAME)) {
            dataContainer.set(StructuredDataKey.CUSTOM_NAME, mappedItem.tagName());
            customTag.putBoolean(this.nbtTagName("added_custom_name"), true);
         }
      }
   }

   private void addCustomModelData(Item item, MappedItem mappedItem, CompoundTag customTag) {
      StructuredDataContainer dataContainer = item.dataContainer();
      CustomModelData1_21_4 customModelData = (CustomModelData1_21_4)dataContainer.get(StructuredDataKey.CUSTOM_MODEL_DATA1_21_4);
      if (customModelData == null) {
         customModelData = new CustomModelData1_21_4(
            new float[]{mappedItem.customModelData()},
            new boolean[0],
            new String[]{((BackwardsProtocol)this.protocol).getMappingData().getFullItemMappings().identifier(item.identifier())},
            EMPTY_INT_ARRAY
         );
         dataContainer.set(StructuredDataKey.CUSTOM_MODEL_DATA1_21_4, customModelData);
         customTag.putBoolean("VB|injected_cmd", true);
         customTag.putBoolean(this.nbtTagName("added_custom_model_data"), true);
      } else if (!customTag.contains("VB|injected_cmd")) {
         String identifier = ((BackwardsProtocol)this.protocol).getMappingData().getFullItemMappings().identifier(item.identifier());
         dataContainer.set(
            StructuredDataKey.CUSTOM_MODEL_DATA1_21_4,
            new CustomModelData1_21_4(
               customModelData.floats(), customModelData.booleans(), (String[])ArrayUtil.add(customModelData.strings(), identifier), customModelData.colors()
            )
         );
         customTag.putBoolean("VB|injected_cmd", true);
         customTag.putString(this.nbtTagName("injected_custom_model_data"), identifier);
      }
   }

   protected void restoreBackupData(Item item, StructuredDataContainer container, CompoundTag customData) {
      super.restoreBackupData(item, container, customData);
      if (this.removeBackupTag(customData, "id") instanceof IntTag originalTag) {
         item.setIdentifier(originalTag.asInt());
         this.removeCustomTag(container, customData);
      }

      if (this.removeBackupTag(customData, "injected_custom_model_data") instanceof StringTag injectedCustomModelData) {
         customData.remove("VB|injected_cmd");
         container.replace(StructuredDataKey.CUSTOM_MODEL_DATA1_21_4, customModelData -> {
            String target = injectedCustomModelData.getValue();
            String[] strings = customModelData.strings();

            for (int i = 0; i < strings.length; i++) {
               if (strings[i].equals(target)) {
                  String[] filteredStrings = (String[])ArrayUtil.remove(strings, i);
                  return new CustomModelData1_21_4(customModelData.floats(), customModelData.booleans(), filteredStrings, customModelData.colors());
               }
            }

            return customModelData;
         });
      } else if (this.removeBackupTag(customData, "added_custom_model_data") != null) {
         customData.remove("VB|injected_cmd");
         container.remove(StructuredDataKey.CUSTOM_MODEL_DATA1_21_4);
      }
   }

   protected void saveListTag(CompoundTag tag, ListTag<?> original, String name) {
      String backupName = this.nbtTagName(name);
      if (!tag.contains(backupName)) {
         tag.put(backupName, original.copy());
      }
   }

   @Nullable
   public <T extends Tag> ListTag<T> removeListTag(CompoundTag tag, String tagName, Class<T> tagType) {
      String backupName = this.nbtTagName(tagName);
      ListTag<T> data = tag.getListTag(backupName, tagType);
      if (data == null) {
         return null;
      } else {
         tag.remove(backupName);
         return data;
      }
   }

   protected void saveGenericTagList(CompoundTag tag, List<Tag> original, String name) {
      String backupName = this.nbtTagName(name);
      if (!tag.contains(backupName)) {
         CompoundTag output = new CompoundTag();

         for (int i = 0; i < original.size(); i++) {
            output.put(Integer.toString(i), original.get(i));
         }

         tag.put(backupName, output);
      }
   }

   protected List<Tag> removeGenericTagList(CompoundTag tag, String name) {
      String backupName = this.nbtTagName(name);
      CompoundTag data = tag.getCompoundTag(backupName);
      if (data == null) {
         return null;
      } else {
         tag.remove(backupName);
         return new ArrayList<>(data.values());
      }
   }

   protected Tag holderSetToTag(HolderSet set) {
      return (Tag)(set.hasIds() ? new IntArrayTag(set.ids()) : new StringTag(set.tagKey()));
   }

   protected HolderSet restoreHolderSet(CompoundTag tag, String key) {
      Tag savedTag = tag.get(key);
      if (savedTag == null) {
         return HolderSet.of(EMPTY_INT_ARRAY);
      } else if (savedTag instanceof StringTag tagKey) {
         return HolderSet.of(tagKey.getValue());
      } else {
         return savedTag instanceof IntArrayTag idsTag ? HolderSet.of(idsTag.getValue()) : HolderSet.of(EMPTY_INT_ARRAY);
      }
   }

   protected <V> Tag holderToTag(Holder<V> holder, BiConsumer<V, CompoundTag> valueSaveFunction) {
      if (holder.hasId()) {
         return new IntTag(holder.id());
      } else {
         CompoundTag savedTag = new CompoundTag();
         valueSaveFunction.accept((V)holder.value(), savedTag);
         return savedTag;
      }
   }

   protected <V> Tag eitherHolderToTag(EitherHolder<V> holder, BiConsumer<V, CompoundTag> valueSaveFunction) {
      return (Tag)(holder.hasKey() ? new StringTag(holder.key()) : this.holderToTag(holder.holder(), valueSaveFunction));
   }

   protected <V> void saveEitherHolderData(
      StructuredDataKey<EitherHolder<V>> key, StructuredDataContainer data, CompoundTag backupTag, BiConsumer<V, CompoundTag> valueSaveFunction
   ) {
      EitherHolder<V> holder = (EitherHolder<V>)data.get(key);
      if (holder != null) {
         backupTag.put(key.identifier(), this.eitherHolderToTag(holder, valueSaveFunction));
      }
   }

   protected <V> void saveHolderData(
      StructuredDataKey<Holder<V>> key, StructuredDataContainer data, CompoundTag backupTag, BiConsumer<V, CompoundTag> valueSaveFunction
   ) {
      Holder<V> holder = (Holder<V>)data.get(key);
      if (holder != null) {
         backupTag.put(key.identifier(), this.holderToTag(holder, valueSaveFunction));
      }
   }

   protected <V> Holder<V> restoreHolder(CompoundTag tag, String key, Function<CompoundTag, V> valueRestoreFunction) {
      Tag savedTag = tag.get(key);
      if (savedTag == null) {
         return Holder.of(0);
      } else if (savedTag instanceof IntTag idTag) {
         return Holder.of(idTag.asInt());
      } else {
         return savedTag instanceof CompoundTag compoundTag ? Holder.of(valueRestoreFunction.apply(compoundTag)) : Holder.of(0);
      }
   }

   protected <V> EitherHolder<V> restoreEitherHolder(CompoundTag tag, String key, Function<CompoundTag, V> valueRestoreFunction) {
      Tag savedTag = tag.get(key);
      if (savedTag == null) {
         return EitherHolder.of(Holder.of(0));
      } else {
         return savedTag instanceof StringTag keyTag ? EitherHolder.of(keyTag.getValue()) : EitherHolder.of(this.restoreHolder(tag, key, valueRestoreFunction));
      }
   }

   protected <V> void restoreHolderData(
      StructuredDataKey<Holder<V>> key, StructuredDataContainer data, CompoundTag backupTag, Function<CompoundTag, V> valueRestoreFunction
   ) {
      if (backupTag.contains(key.identifier())) {
         data.set(key, this.restoreHolder(backupTag, key.identifier(), valueRestoreFunction));
      }
   }

   protected void saveStringData(StructuredDataKey<String> key, StructuredDataContainer data, CompoundTag backupTag) {
      String value = (String)data.get(key);
      if (value != null) {
         backupTag.putString(key.identifier(), value);
      }
   }

   protected void restoreStringData(StructuredDataKey<String> key, StructuredDataContainer data, CompoundTag backupTag) {
      String value = backupTag.getString(key.identifier());
      if (value != null) {
         data.set(key, value);
      }
   }

   protected void saveKeyData(StructuredDataKey<Key> key, StructuredDataContainer data, CompoundTag backupTag) {
      Key value = (Key)data.get(key);
      if (value != null) {
         backupTag.putString(key.identifier(), value.original());
      }
   }

   protected void restoreKeyData(StructuredDataKey<Key> key, StructuredDataContainer data, CompoundTag backupTag) {
      String value = backupTag.getString(key.identifier());
      if (value != null) {
         data.set(key, Key.of(value));
      }
   }

   protected void saveIntData(StructuredDataKey<Integer> key, StructuredDataContainer data, CompoundTag backupTag) {
      Integer variant = (Integer)data.get(key);
      if (variant != null) {
         backupTag.putInt(key.identifier(), variant);
      }
   }

   protected void restoreIntData(StructuredDataKey<Integer> key, StructuredDataContainer data, CompoundTag backupTag) {
      IntTag variant = backupTag.getIntTag(key.identifier());
      if (variant != null) {
         data.set(key, variant.asInt());
      }
   }

   protected void saveFloatData(StructuredDataKey<Float> key, StructuredDataContainer data, CompoundTag backupTag) {
      Float variant = (Float)data.get(key);
      if (variant != null) {
         backupTag.putFloat(key.identifier(), variant);
      }
   }

   protected void restoreFloatData(StructuredDataKey<Float> key, StructuredDataContainer data, CompoundTag backupTag) {
      FloatTag variant = backupTag.getFloatTag(key.identifier());
      if (variant != null) {
         data.set(key, variant.asFloat());
      }
   }

   protected void saveSoundEventHolder(CompoundTag tag, Holder<SoundEvent> holder) {
      tag.put("sound_event", this.holderToTag(holder, this::saveSoundEvent));
   }

   protected void saveSoundEvent(SoundEvent soundEvent, CompoundTag tag) {
      tag.putString("identifier", soundEvent.identifier());
      if (soundEvent.fixedRange() != null) {
         tag.putFloat("fixed_range", soundEvent.fixedRange());
      }
   }

   protected Holder<SoundEvent> restoreSoundEventHolder(CompoundTag tag) {
      return this.restoreSoundEventHolder(tag, "sound_event");
   }

   protected Holder<SoundEvent> restoreSoundEventHolder(CompoundTag tag, String key) {
      return this.restoreHolder(tag, key, soundEventTag -> {
         String identifier = soundEventTag.getString("identifier");
         FloatTag fixedRange = soundEventTag.getFloatTag("fixed_range");
         return new SoundEvent(identifier, fixedRange != null ? fixedRange.asFloat() : null);
      });
   }

   public String nbtTagName() {
      return this.nbtTagName;
   }
}
