package com.viaversion.viaversion.rewriter;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.EitherHolder;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.HashedItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableComponent;
import com.viaversion.viaversion.api.minecraft.item.data.WrittenBook;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.data.item.ItemHasherBase;
import com.viaversion.viaversion.data.item.OriginalHashedItem;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.util.Rewritable;
import java.util.List;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StructuredItemRewriter<C extends ClientboundPacketType, S extends ServerboundPacketType, T extends Protocol<C, ?, ?, S>>
   extends ItemRewriter<C, S, T> {
   public static final String MARKER_KEY = "VV|custom_data";
   protected static final String ORIGINAL_HASHES_KEY = "VV|original_hashes";

   public StructuredItemRewriter(T protocol) {
      super(protocol);
   }

   @Override
   public Item handleItemToClient(UserConnection connection, Item item) {
      if (Item.isEmpty(item)) {
         return item;
      } else {
         ItemHasherBase itemHasher = this.itemHasher(connection);
         HashedItem originalHashedItem = this.hashItemIfNeeded(connection, item, itemHasher);
         StructuredDataContainer dataContainer = item.dataContainer();
         this.updateItemDataComponentTypeIds(dataContainer, true);
         CompoundTag backupTag = new CompoundTag();
         this.backupInconvertibleData(connection, item, dataContainer, backupTag);
         if (!backupTag.isEmpty()) {
            this.saveTag(this.createCustomTag(item), backupTag, "backup");
         }

         MappingData mappingData = this.protocol.getMappingData();
         if (mappingData != null && mappingData.getItemMappings() != null) {
            item.setIdentifier(mappingData.getNewItemId(item.identifier()));
         }

         this.handleRewritablesToClient(connection, dataContainer, itemHasher);
         this.handleItemDataComponentsToClient(connection, item, dataContainer);
         if (originalHashedItem != null) {
            this.storeOriginalHashedItemIfNeeded(connection, item, itemHasher, originalHashedItem);
         }

         return item;
      }
   }

   @Nullable
   protected HashedItem hashItemIfNeeded(UserConnection connection, Item item, @Nullable ItemHasherBase hasher) {
      if (hasher != null && hasher.isProcessingClientboundInventoryPacket()) {
         CompoundTag customData = item.dataContainer().get(StructuredDataKey.CUSTOM_DATA);
         CompoundTag originalHashes;
         if (customData == null || (originalHashes = customData.getCompoundTag("VV|original_hashes")) == null) {
            return hasher.toHashedItem(item, false);
         } else {
            return this.isFirstServerbound(connection) ? this.backedUpOriginalHashes(originalHashes, item) : null;
         }
      } else {
         return null;
      }
   }

   protected void storeOriginalHashedItemIfNeeded(UserConnection connection, Item item, ItemHasherBase hasher, HashedItem originalHashedItem) {
      if (originalHashedItem != null && (!originalHashedItem.dataHashesById().isEmpty() || !originalHashedItem.removedDataIds().isEmpty())) {
         if (originalHashedItem instanceof OriginalHashedItem restoredHashedItem) {
            hasher.trackOriginalHashedItem(item.dataContainer().get(StructuredDataKey.CUSTOM_DATA), originalHashedItem, restoredHashedItem.backupTagName());
         } else {
            HashedItem hashedItem = hasher.toHashedItem(item, true);
            this.normalizeHashedItemToServer(hashedItem);
            if (!hashedItem.dataHashesById().equals(originalHashedItem.dataHashesById())
               || !hashedItem.removedDataIds().equals(originalHashedItem.removedDataIds())) {
               this.storeOriginalHashedItemInTag(connection, item, hasher, originalHashedItem);
            }
         }
      }
   }

   protected void storeOriginalHashedItemInTag(UserConnection connection, Item item, ItemHasherBase hasher, HashedItem originalHashedItem) {
      CompoundTag originalHashes = new CompoundTag();
      originalHashes.putInt("id", originalHashedItem.identifier());
      originalHashes.putString("backup_tag", this.nbtTagName());

      for (Int2IntMap.Entry entry : originalHashedItem.dataHashesById().int2IntEntrySet()) {
         originalHashes.putInt(Integer.toString(entry.getIntKey()), entry.getIntValue());
      }

      originalHashes.put("removed", new IntArrayTag(originalHashedItem.removedDataIds().toIntArray()));
      CompoundTag customTag = this.createCustomTag(item);
      customTag.put("VV|original_hashes", originalHashes);
      if (this.isFirstServerbound(connection)) {
         hasher.trackOriginalHashedItem(customTag, originalHashedItem, this.nbtTagName());
      }
   }

   private void normalizeHashedItemToServer(HashedItem item) {
      MappingData mappingData = this.protocol.getMappingData();
      if (mappingData != null && !Mappings.isIntIdIdentity(mappingData.getDataComponentSerializerMappings())) {
         this.updateHashedItemDataComponentIds(item, mappingData.getDataComponentSerializerMappings().inverse());
      }
   }

   protected boolean isFirstServerbound(UserConnection connection) {
      for (Protocol<?, ?, ?, ?> protocol : connection.getProtocolInfo().getPipeline().pipes()) {
         if (!protocol.isBaseProtocol() && connection.getItemHasher(protocol) instanceof ItemHasherBase) {
            return protocol.getClass() == this.protocol.getClass();
         }
      }

      return false;
   }

   @Nullable
   protected OriginalHashedItem backedUpOriginalHashes(CompoundTag originalHashes, Item item) {
      IntTag idTag = originalHashes.getIntTag("id");
      String backupTagName = originalHashes.getString("backup_tag");
      if (idTag != null && backupTagName != null) {
         OriginalHashedItem hashedItem = new OriginalHashedItem(idTag.asInt(), item.amount(), backupTagName);

         try {
            for (Entry<String, Tag> entry : originalHashes.entrySet()) {
               if (entry.getValue() instanceof IntTag hashTag) {
                  String key = entry.getKey();
                  if (!key.equals("id")) {
                     hashedItem.dataHashesById().put(Integer.parseInt(key), hashTag.asInt());
                  }
               }
            }
         } catch (NumberFormatException var11) {
            return null;
         }

         IntArrayTag removedTag = originalHashes.getIntArrayTag("removed");
         if (removedTag != null) {
            for (int id : removedTag.getValue()) {
               hashedItem.removedDataIds().add(id);
            }
         }

         return hashedItem;
      } else {
         return null;
      }
   }

   @Override
   public Item handleItemToServer(UserConnection connection, Item item) {
      if (Item.isEmpty(item)) {
         return item;
      } else {
         MappingData mappingData = this.protocol.getMappingData();
         if (mappingData != null && mappingData.getItemMappings() != null) {
            item.setIdentifier(mappingData.getOldItemId(item.identifier()));
         }

         this.updateItemDataComponentTypeIds(item.dataContainer(), false);
         this.handleRewritablesToServer(connection, item.dataContainer());
         this.restoreBackupData(item);
         this.handleItemDataComponentsToServer(connection, item, item.dataContainer());
         return item;
      }
   }

   protected void updateItemDataComponentTypeIds(StructuredDataContainer container, boolean mappedNames) {
      MappingData mappingData = this.protocol.getMappingData();
      if (mappingData != null) {
         FullMappings dataComponentMappings = mappingData.getDataComponentSerializerMappings();
         if (dataComponentMappings != null) {
            if (!mappedNames) {
               dataComponentMappings = dataComponentMappings.inverse();
            }

            container.setIdLookup(this.protocol, mappedNames);
            container.updateIds(this.protocol, dataComponentMappings::getNewId);
         }
      }
   }

   protected void handleItemDataComponentsToClient(UserConnection connection, Item item, StructuredDataContainer container) {
      if (this.protocol.getComponentRewriter() != null) {
         this.updateTextComponent(connection, item, StructuredDataKey.ITEM_NAME, "item_name");
         this.updateTextComponent(connection, item, StructuredDataKey.CUSTOM_NAME, "custom_name");
         Tag[] lore = container.get(StructuredDataKey.LORE);
         if (lore != null) {
            for (Tag tag : lore) {
               this.protocol.getComponentRewriter().processTag(connection, tag);
            }
         }

         WrittenBook book = container.get(StructuredDataKey.WRITTEN_BOOK_CONTENT);
         if (book != null) {
            for (FilterableComponent page : book.pages()) {
               this.protocol.getComponentRewriter().processTag(connection, page.raw());
               if (page.isFiltered()) {
                  this.protocol.getComponentRewriter().processTag(connection, page.filtered());
               }
            }
         }
      }

      this.replaceAnnoyingKeys(container, this.protocol.types(), this.protocol.mappedTypes());
   }

   protected void handleItemDataComponentsToServer(UserConnection connection, Item item, StructuredDataContainer container) {
      this.replaceAnnoyingKeys(container, this.protocol.mappedTypes(), this.protocol.types());
   }

   protected void handleRewritablesToClient(UserConnection connection, StructuredDataContainer container, @Nullable ItemHasher itemHasher) {
      if (itemHasher != null && itemHasher.isProcessingClientboundInventoryPacket()) {
         itemHasher.setProcessingClientboundInventoryPacket(false);

         try {
            this.handleRewritables(connection, true, container, this::handleItemToClient);
         } finally {
            itemHasher.setProcessingClientboundInventoryPacket(true);
         }
      } else {
         this.handleRewritables(connection, true, container, this::handleItemToClient);
      }
   }

   protected void handleRewritablesToServer(UserConnection connection, StructuredDataContainer container) {
      this.handleRewritables(connection, false, container, this::handleItemToServer);
   }

   private void handleRewritables(
      UserConnection connection, boolean clientbound, StructuredDataContainer container, StructuredItemRewriter.ItemHandler itemHandler
   ) {
      for (StructuredData<?> data : container.data().values()) {
         if (!data.isEmpty()) {
            Object value = data.value();
            if (value instanceof Item itemValue) {
               ((StructuredData<Item>)data).setValue(itemHandler.rewrite(connection, itemValue));
            } else if (value instanceof Item[] items) {
               for (int i = 0; i < items.length; i++) {
                  items[i] = itemHandler.rewrite(connection, items[i]);
               }
            } else if (value instanceof Rewritable rewritable) {
               this.setDataUnchecked(data, rewritable.rewrite(connection, this.protocol, clientbound));
            } else if (value instanceof Holder<?> holder) {
               if (holder.isDirect() && holder.value() instanceof Rewritable) {
                  ((StructuredData<Holder<?>>)data).setValue(this.updateHolderUnchecked(holder, connection, clientbound));
               }
            } else if (value instanceof EitherHolder<?> eitherHolder
               && eitherHolder.hasHolder()
               && eitherHolder.holder().isDirect()
               && eitherHolder.holder().value() instanceof Rewritable) {
               ((StructuredData<EitherHolder<?>>)data).setValue(EitherHolder.of(this.updateHolderUnchecked(eitherHolder.holder(), connection, clientbound)));
            }
         }
      }
   }

   private <V> void setDataUnchecked(StructuredData<V> data, Object value) {
      data.setValue((V)value);
   }

   private <V> Holder<V> updateHolderUnchecked(Holder<V> holder, UserConnection connection, boolean clientbound) {
      return holder.updateValue(val -> // $VF: Couldn't be decompiled);
   }

   protected void updateTextComponent(UserConnection connection, Item item, StructuredDataKey<Tag> key, String backupKey) {
      Tag name = item.dataContainer().get(key);
      if (name instanceof CompoundTag || name instanceof ListTag) {
         Tag originalName = name.copy();
         this.protocol.getComponentRewriter().processTag(connection, name);
         if (!name.equals(originalName)) {
            this.saveTag(this.createCustomTag(item), originalName, backupKey);
         }
      }
   }

   protected void restoreBackupData(Item item) {
      StructuredDataContainer container = item.dataContainer();
      CompoundTag customData = container.get(StructuredDataKey.CUSTOM_DATA);
      if (customData != null) {
         this.restoreBackupData(item, container, customData);
         this.removeCustomTag(container, customData);
      }
   }

   protected void backupInconvertibleData(UserConnection connection, Item item, StructuredDataContainer container, CompoundTag backupTag) {
   }

   protected void restoreBackupData(Item item, StructuredDataContainer container, CompoundTag customData) {
      customData.remove("VV|original_hashes");
      if (this.removeBackupTag(customData, "added_custom_name") != null) {
         container.remove(StructuredDataKey.CUSTOM_NAME);
      } else {
         Tag customName = this.removeBackupTag(customData, "custom_name");
         if (customName != null) {
            container.set(StructuredDataKey.CUSTOM_NAME, customName);
         }

         Tag itemName = this.removeBackupTag(customData, "item_name");
         if (itemName != null) {
            container.set(StructuredDataKey.ITEM_NAME, itemName);
         }
      }
   }

   protected CompoundTag createCustomTag(Item item) {
      StructuredDataContainer data = item.dataContainer();
      CompoundTag customData = data.get(StructuredDataKey.CUSTOM_DATA);
      if (customData == null) {
         customData = new CompoundTag();
         customData.putBoolean("VV|custom_data", true);
         data.set(StructuredDataKey.CUSTOM_DATA, customData);
      }

      return customData;
   }

   protected void saveTag(CompoundTag customData, Tag tag, String name) {
      String backupName = this.nbtTagName(name);
      if (!customData.contains(backupName)) {
         customData.put(backupName, tag);
      }
   }

   @Nullable
   protected Tag removeBackupTag(CompoundTag customData, String tagName) {
      return customData.remove(this.nbtTagName(tagName));
   }

   protected void removeCustomTag(StructuredDataContainer data, CompoundTag customData) {
      if (customData.size() == 1 && customData.contains("VV|custom_data")) {
         data.remove(StructuredDataKey.CUSTOM_DATA);
      }
   }

   protected void passthroughLengthPrefixedItem(PacketWrapper wrapper) {
      Item item = this.handleItemToServer(wrapper.user(), wrapper.read(this.protocol.mappedTypes().lengthPrefixedItem()));
      wrapper.write(this.protocol.types().lengthPrefixedItem(), item);
   }

   private void replaceAnnoyingKeys(StructuredDataContainer container, VersionedTypesHolder types, VersionedTypesHolder mappedTypes) {
      List<StructuredDataKey<?>> keys = types.structuredDataKeys().keys();
      List<StructuredDataKey<?>> mappedKeys = mappedTypes.structuredDataKeys().keys();
      int minSize = Math.min(keys.size(), mappedKeys.size());

      for (int i = 0; i < minSize; i++) {
         StructuredDataKey<?> key = keys.get(i);
         StructuredDataKey<?> mappedKey = mappedKeys.get(i);
         replaceKeyUnchecked(container, key, mappedKey);
      }
   }

   private static <T> void replaceKeyUnchecked(StructuredDataContainer container, StructuredDataKey<T> key, StructuredDataKey<?> mappedKey) {
      Preconditions.checkArgument(key.type().getOutputClass() == mappedKey.type().getOutputClass(), "Type mismatch: %s vs %s", new Object[]{key, mappedKey});
      container.replaceKey(key, (StructuredDataKey<T>)mappedKey);
   }

   public void registerSetCreativeModeSlot1_21_5(S packetType) {
      this.protocol.registerServerbound(packetType, wrapper -> {
         if (!this.protocol.getEntityRewriter().<EntityTracker>tracker(wrapper.user()).canInstaBuild()) {
            wrapper.cancel();
         } else {
            wrapper.passthrough(Types.SHORT);
            this.passthroughLengthPrefixedItem(wrapper);
         }
      });
   }

   public void registerShowDialog(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         Holder<CompoundTag> holder = wrapper.passthrough(Types.TRUSTED_COMPOUND_TAG_HOLDER);
         if (holder.isDirect()) {
            this.protocol.getRegistryDataRewriter().updateDialog(wrapper.user(), holder.value());
         }
      });
   }

   public void registerShowDialogDirect(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         CompoundTag dialogTag = wrapper.passthrough(Types.TRUSTED_COMPOUND_TAG);
         this.protocol.getRegistryDataRewriter().updateDialog(wrapper.user(), dialogTag);
      });
   }

   @FunctionalInterface
   private interface ItemHandler {
      Item rewrite(UserConnection var1, Item var2);
   }
}
