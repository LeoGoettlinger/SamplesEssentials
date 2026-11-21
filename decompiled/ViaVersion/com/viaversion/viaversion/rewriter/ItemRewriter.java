package com.viaversion.viaversion.rewriter;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.item.HashedItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.rewriter.ComponentRewriter;
import com.viaversion.viaversion.api.rewriter.RewriterBase;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.data.item.ItemHasherBase;
import com.viaversion.viaversion.data.item.OriginalHashedItem;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.util.Limit;
import com.viaversion.viaversion.util.Rewritable;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ItemRewriter<C extends ClientboundPacketType, S extends ServerboundPacketType, T extends Protocol<C, ?, ?, S>>
   extends RewriterBase<T>
   implements com.viaversion.viaversion.api.rewriter.ItemRewriter<T> {
   private final Type<Item> itemType;
   private final Type<Item> mappedItemType;
   private final Type<Item[]> itemArrayType;
   private final Type<Item[]> mappedItemArrayType;
   private final Type<Item> itemCostType;
   private final Type<Item> itemTemplateType;
   private final Type<Item> mappedItemTemplateType;
   private final Type<Item> mappedItemCostType;
   private final Type<Item> optionalItemCostType;
   private final Type<Item> mappedOptionalItemCostType;

   public ItemRewriter(T protocol) {
      super(protocol);
      VersionedTypesHolder types = protocol.types();
      VersionedTypesHolder mappedTypes = protocol.mappedTypes();
      this.itemType = types.item();
      this.itemArrayType = types.itemArray();
      this.mappedItemType = mappedTypes.item();
      this.mappedItemArrayType = mappedTypes.itemArray();
      this.itemTemplateType = types.itemTemplate();
      this.mappedItemTemplateType = mappedTypes.itemTemplate();
      this.itemCostType = types.itemCost();
      this.mappedItemCostType = mappedTypes.itemCost();
      this.optionalItemCostType = types.optionalItemCost();
      this.mappedOptionalItemCostType = mappedTypes.optionalItemCost();
   }

   public ItemRewriter(T protocol, Type<Item> itemType, Type<Item[]> itemArrayType, Type<Item> mappedItemType, Type<Item[]> mappedItemArrayType) {
      super(protocol);
      this.itemType = itemType;
      this.itemArrayType = itemArrayType;
      this.mappedItemType = mappedItemType;
      this.mappedItemArrayType = mappedItemArrayType;
      this.itemTemplateType = null;
      this.mappedItemTemplateType = null;
      this.itemCostType = null;
      this.mappedItemCostType = null;
      this.optionalItemCostType = null;
      this.mappedOptionalItemCostType = null;
   }

   public ItemRewriter(T protocol, Type<Item> itemType, Type<Item[]> itemArrayType) {
      this(protocol, itemType, itemArrayType, itemType, itemArrayType);
   }

   @Nullable
   @Override
   public Item handleItemToClient(UserConnection connection, @Nullable Item item) {
      if (item == null) {
         return null;
      } else {
         if (this.protocol.getMappingData() != null && !Mappings.isIntIdIdentity(this.protocol.getMappingData().getItemMappings())) {
            item.setIdentifier(this.protocol.getMappingData().getNewItemId(item.identifier()));
         }

         return item;
      }
   }

   @Nullable
   @Override
   public Item handleItemToServer(UserConnection connection, @Nullable Item item) {
      if (item == null) {
         return null;
      } else {
         if (this.protocol.getMappingData() != null && !Mappings.isIntIdIdentity(this.protocol.getMappingData().getItemMappings())) {
            item.setIdentifier(this.protocol.getMappingData().getOldItemId(item.identifier()));
         }

         return item;
      }
   }

   @Override
   public HashedItem handleHashedItem(UserConnection connection, HashedItem item) {
      if (item instanceof OriginalHashedItem restoredItem) {
         return restoredItem.backupTagName().equals(this.nbtTagName()) ? restoredItem.asRegularItem() : item;
      } else {
         MappingData mappingData = this.protocol.getMappingData();
         if (mappingData == null) {
            return item;
         } else {
            FullMappings dataComponentMappings = mappingData.getDataComponentSerializerMappings();
            if (dataComponentMappings != null) {
               if (!dataComponentMappings.isIdentity()) {
                  this.updateHashedItemDataComponentIds(item, dataComponentMappings.inverse());
               }

               int customDataId = dataComponentMappings.id("custom_data");
               if (item.dataHashesById().containsKey(customDataId)) {
                  int customDataHash = item.dataHashesById().get(customDataId);
                  ItemHasherBase itemHasher = this.itemHasher(connection);
                  OriginalHashedItem originalHashedItem = itemHasher.originalHashedItem(customDataHash, item);
                  if (originalHashedItem != null) {
                     return (HashedItem)(originalHashedItem.backupTagName().equals(this.nbtTagName()) ? originalHashedItem.asRegularItem() : originalHashedItem);
                  }
               }
            }

            if (mappingData.getItemMappings() != null) {
               item.setIdentifier(mappingData.getOldItemId(item.identifier()));
            }

            return item;
         }
      }
   }

   public void registerSetContent(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.UNSIGNED_BYTE);
         Item[] items = wrapper.passthroughAndMap(this.itemArrayType, this.mappedItemArrayType);

         for (int i = 0; i < items.length; i++) {
            items[i] = this.handleItemToClient(wrapper.user(), items[i]);
         }
      });
   }

   public void registerSetContent1_17_1(C packetType) {
      this.registerSetContent1_17_1(packetType, Types.UNSIGNED_BYTE);
   }

   public void registerSetContent1_21_2(C packetType) {
      this.registerSetContent1_17_1(packetType, Types.VAR_INT);
   }

   private void registerSetContent1_17_1(C packetType, Type<? extends Number> containerIdType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(containerIdType);
         wrapper.passthrough(Types.VAR_INT);
         Item[] items = wrapper.passthroughAndMap(this.itemArrayType, this.mappedItemArrayType);

         for (int i = 0; i < items.length; i++) {
            items[i] = this.handleItemToClientAndTrackHash(wrapper.user(), items[i]);
         }

         this.passthroughClientboundItemAndTrackHash(wrapper);
      });
   }

   public void registerOpenScreen(C packetType) {
      if (this.protocol.getMappingData() != null && !Mappings.isIntIdIdentity(this.protocol.getMappingData().getMenuMappings())
         || this.protocol.getComponentRewriter() != null) {
         this.protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT);
            int windowType = wrapper.read(Types.VAR_INT);
            if (this.protocol.getMappingData() != null && this.protocol.getMappingData().getMenuMappings() != null) {
               windowType = this.protocol.getMappingData().getMenuMappings().getNewId(windowType);
               if (windowType == -1) {
                  wrapper.cancel();
                  return;
               }
            }

            wrapper.write(Types.VAR_INT, windowType);
            if (this.protocol.getComponentRewriter() != null) {
               this.protocol.getComponentRewriter().passthroughAndProcess(wrapper);
            }
         });
      }
   }

   public void registerSetSlot(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.SHORT);
         this.passthroughClientboundItem(wrapper);
      });
   }

   public void registerSetSlot1_17_1(C packetType) {
      this.registerSetSlot1_17_1(packetType, Types.BYTE);
   }

   public void registerSetSlot1_21_2(C packetType) {
      this.registerSetSlot1_17_1(packetType, Types.VAR_INT);
   }

   private void registerSetSlot1_17_1(C packetType, Type<? extends Number> containerIdType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(containerIdType);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.SHORT);
         this.passthroughClientboundItemAndTrackHash(wrapper);
      });
   }

   public void registerSetEquippedItem(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         this.passthroughClientboundItem(wrapper);
      });
   }

   public void registerSetEquipment(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);

         byte slot;
         do {
            slot = wrapper.passthrough(Types.BYTE);
            this.passthroughClientboundItem(wrapper);
         } while (slot < 0);
      });
   }

   public void registerSetCreativeModeSlot(S packetType) {
      this.protocol.registerServerbound(packetType, wrapper -> {
         wrapper.passthrough(Types.SHORT);
         wrapper.write(this.itemType, this.handleItemToServer(wrapper.user(), wrapper.read(this.mappedItemType)));
      });
   }

   public void registerSetCreativeModeSlot1_20_5(S packetType) {
      this.protocol.registerServerbound(packetType, wrapper -> {
         if (this.protocol.getEntityRewriter() != null && !this.protocol.getEntityRewriter().<EntityTracker>tracker(wrapper.user()).canInstaBuild()) {
            wrapper.cancel();
         } else {
            wrapper.passthrough(Types.SHORT);
            wrapper.write(this.itemType, this.handleItemToServer(wrapper.user(), wrapper.read(this.mappedItemType)));
         }
      });
   }

   public void registerContainerClick(S packetType) {
      this.protocol.registerServerbound(packetType, wrapper -> {
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.SHORT);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.SHORT);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.write(this.itemType, this.handleItemToServer(wrapper.user(), wrapper.read(this.mappedItemType)));
      });
   }

   public void registerContainerClick1_17_1(S packetType) {
      this.registerContainerClick1_17_1(packetType, Types.BYTE);
   }

   public void registerContainerClick1_21_2(S packetType) {
      this.registerContainerClick1_17_1(packetType, Types.VAR_INT);
   }

   public void registerContainerClick1_17_1(S packetType, Type<? extends Number> containerIdType) {
      this.protocol.registerServerbound(packetType, wrapper -> {
         wrapper.passthrough(containerIdType);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.SHORT);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.VAR_INT);
         int length = Limit.max(wrapper.passthrough(Types.VAR_INT), 128);

         for (int i = 0; i < length; i++) {
            wrapper.passthrough(Types.SHORT);
            wrapper.write(this.itemType, this.handleItemToServer(wrapper.user(), wrapper.read(this.mappedItemType)));
         }

         wrapper.write(this.itemType, this.handleItemToServer(wrapper.user(), wrapper.read(this.mappedItemType)));
      });
   }

   public void registerContainerClick1_21_5(S packetType) {
      this.protocol.registerServerbound(packetType, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.SHORT);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.VAR_INT);
         int affectedItems = Limit.max(wrapper.passthrough(Types.VAR_INT), 128);

         for (int i = 0; i < affectedItems; i++) {
            wrapper.passthrough(Types.SHORT);
            this.passthroughHashedItem(wrapper);
         }

         this.passthroughHashedItem(wrapper);
      });
   }

   public void registerSetPlayerInventory(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         this.passthroughClientboundItemAndTrackHash(wrapper);
      });
   }

   public void registerCooldown(C packetType) {
      if (this.protocol.getMappingData() != null && !Mappings.isIntIdIdentity(this.protocol.getMappingData().getItemMappings())) {
         this.protocol.registerClientbound(packetType, wrapper -> {
            int itemId = wrapper.read(Types.VAR_INT);
            wrapper.write(Types.VAR_INT, this.protocol.getMappingData().getNewItemId(itemId));
         });
      }
   }

   public void registerCooldown1_21_2(C packetType) {
      if (this.protocol.getMappingData() != null && !Mappings.isFullIdentity(this.protocol.getMappingData().getFullItemMappings())) {
         this.protocol.registerClientbound(packetType, wrapper -> {
            String itemIdentifier = wrapper.read(Types.STRING);
            if (itemIdentifier != null) {
               itemIdentifier = Rewritable.mappedIdentifier(this.protocol.getMappingData().getFullItemMappings(), itemIdentifier);
            }

            wrapper.write(Types.STRING, itemIdentifier);
         });
      }
   }

   public void registerCustomPayloadTradeList(C packetType) {
      this.protocol.registerClientbound(packetType, new PacketHandlers() {
         @Override
         protected void register() {
            this.map((Type<T>)Types.STRING);
            this.handlerSoftFail(wrapper -> {
               String channel = wrapper.get(Types.STRING, 0);
               if (channel.equals("MC|TrList")) {
                  ItemRewriter.this.handleTradeList(wrapper);
               }
            });
         }
      });
   }

   public void handleTradeList(PacketWrapper wrapper) {
      wrapper.passthrough(Types.INT);
      int size = wrapper.passthrough(Types.UNSIGNED_BYTE);

      for (int i = 0; i < size; i++) {
         this.passthroughClientboundItem(wrapper);
         this.passthroughClientboundItem(wrapper);
         if (wrapper.passthrough(Types.BOOLEAN)) {
            this.passthroughClientboundItem(wrapper);
         }

         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.INT);
         wrapper.passthrough(Types.INT);
      }
   }

   public void registerMerchantOffers1_14_4(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int size = wrapper.passthrough(Types.UNSIGNED_BYTE);

         for (int i = 0; i < size; i++) {
            this.passthroughClientboundItem(wrapper);
            this.passthroughClientboundItem(wrapper);
            if (wrapper.passthrough(Types.BOOLEAN)) {
               this.passthroughClientboundItem(wrapper);
            }

            wrapper.passthrough(Types.BOOLEAN);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.FLOAT);
            wrapper.passthrough(Types.INT);
         }
      });
   }

   public void registerMerchantOffers1_19(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            this.passthroughClientboundItem(wrapper);
            this.passthroughClientboundItem(wrapper);
            this.passthroughClientboundItem(wrapper);
            wrapper.passthrough(Types.BOOLEAN);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.FLOAT);
            wrapper.passthrough(Types.INT);
         }
      });
   }

   public void registerMerchantOffers1_20_5(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            Item input = wrapper.read(this.itemCostType);
            wrapper.write(this.mappedItemCostType, this.handleItemToClientAndTrackHash(wrapper.user(), input));
            this.passthroughClientboundItem(wrapper);
            Item secondInput = wrapper.read(this.optionalItemCostType);
            if (secondInput != null) {
               this.handleItemToClientAndTrackHash(wrapper.user(), secondInput);
            }

            wrapper.write(this.mappedOptionalItemCostType, secondInput);
            wrapper.passthrough(Types.BOOLEAN);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.FLOAT);
            wrapper.passthrough(Types.INT);
         }
      });
   }

   public void registerAdvancements(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.BOOLEAN);
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.OPTIONAL_STRING);
            if (wrapper.passthrough(Types.BOOLEAN)) {
               JsonElement title = wrapper.passthrough(Types.COMPONENT);
               JsonElement description = wrapper.passthrough(Types.COMPONENT);
               ComponentRewriter componentRewriter = this.protocol.getComponentRewriter();
               if (componentRewriter != null) {
                  componentRewriter.processText(wrapper.user(), title);
                  componentRewriter.processText(wrapper.user(), description);
               }

               this.passthroughClientboundItem(wrapper);
               wrapper.passthrough(Types.VAR_INT);
               int flags = wrapper.passthrough(Types.INT);
               if ((flags & 1) != 0) {
                  wrapper.passthrough(Types.STRING);
               }

               wrapper.passthrough(Types.FLOAT);
               wrapper.passthrough(Types.FLOAT);
            }

            wrapper.passthrough(Types.STRING_ARRAY);
            int arrayLength = wrapper.passthrough(Types.VAR_INT);

            for (int array = 0; array < arrayLength; array++) {
               wrapper.passthrough(Types.STRING_ARRAY);
            }
         }
      });
   }

   public void registerAdvancements1_20_3(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.BOOLEAN);
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.OPTIONAL_STRING);
            if (wrapper.passthrough(Types.BOOLEAN)) {
               this.passthroughAdvancementDisplayData(wrapper);
               wrapper.passthrough(Types.FLOAT);
               wrapper.passthrough(Types.FLOAT);
            }

            int requirements = wrapper.passthrough(Types.VAR_INT);

            for (int array = 0; array < requirements; array++) {
               wrapper.passthrough(Types.STRING_ARRAY);
            }

            wrapper.passthrough(Types.BOOLEAN);
         }
      });
   }

   public void registerAdvancements26_3(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.BOOLEAN);
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.OPTIONAL_STRING);
            if (wrapper.passthrough(Types.BOOLEAN)) {
               this.passthroughAdvancementDisplayData(wrapper);
            }

            int requirements = wrapper.passthrough(Types.VAR_INT);

            for (int array = 0; array < requirements; array++) {
               wrapper.passthrough(Types.STRING_ARRAY);
            }

            wrapper.passthrough(Types.BOOLEAN);
         }

         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
      });
   }

   private void passthroughAdvancementDisplayData(PacketWrapper wrapper) {
      Tag title = wrapper.passthrough(Types.TRUSTED_TAG);
      Tag description = wrapper.passthrough(Types.TRUSTED_TAG);
      ComponentRewriter componentRewriter = this.protocol.getComponentRewriter();
      if (componentRewriter != null) {
         componentRewriter.processTag(wrapper.user(), title);
         componentRewriter.processTag(wrapper.user(), description);
      }

      this.passthroughClientboundItemTemplate(wrapper);
      wrapper.passthrough(Types.VAR_INT);
      int flags = wrapper.passthrough(Types.INT);
      if ((flags & 1) != 0) {
         wrapper.passthrough(Types.STRING);
      }
   }

   public void registerSetCursorItem(C packetType) {
      this.protocol.registerClientbound(packetType, this::passthroughClientboundItemAndTrackHash);
   }

   public void registerContainerSetData(C packetType) {
      if (this.protocol.getMappingData() != null && !Mappings.isIntIdIdentity(this.protocol.getMappingData().getEnchantmentMappings())) {
         this.protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.UNSIGNED_BYTE);
            short property = wrapper.passthrough(Types.SHORT);
            if (property >= 4 && property <= 6) {
               short enchantmentId = (short)this.protocol.getMappingData().getEnchantmentMappings().getNewId(wrapper.read(Types.SHORT));
               wrapper.write(Types.SHORT, enchantmentId);
            }
         });
      }
   }

   @Nullable
   protected Item handleItemToClientAndTrackHash(UserConnection connection, @Nullable Item item) {
      ItemHasher itemHasher = this.itemHasher(connection);
      if (itemHasher == null) {
         return this.handleItemToClient(connection, item);
      } else {
         itemHasher.setProcessingClientboundInventoryPacket(true);

         Item var4;
         try {
            var4 = this.handleItemToClient(connection, item);
         } finally {
            itemHasher.setProcessingClientboundInventoryPacket(false);
         }

         return var4;
      }
   }

   protected void passthroughClientboundItemAndTrackHash(PacketWrapper wrapper) {
      ItemHasher itemHasher = this.itemHasher(wrapper.user());
      if (itemHasher == null) {
         this.passthroughClientboundItem(wrapper);
      } else {
         itemHasher.setProcessingClientboundInventoryPacket(true);

         try {
            this.passthroughClientboundItem(wrapper);
         } finally {
            itemHasher.setProcessingClientboundInventoryPacket(false);
         }
      }
   }

   protected void passthroughClientboundItem(PacketWrapper wrapper) {
      Item item = this.handleItemToClient(wrapper.user(), wrapper.read(this.itemType));
      wrapper.write(this.mappedItemType, item);
   }

   protected void passthroughClientboundItemTemplate(PacketWrapper wrapper) {
      Item item = this.handleItemToClient(wrapper.user(), wrapper.read(this.itemTemplateType()));
      wrapper.write(this.mappedItemTemplateType(), item);
   }

   protected void passthroughHashedItem(PacketWrapper wrapper) {
      HashedItem item = this.handleHashedItem(wrapper.user(), wrapper.read(Types.HASHED_ITEM));
      wrapper.write(Types.HASHED_ITEM, item);
   }

   @Nullable
   protected <T extends ItemHasher> T itemHasher(UserConnection connection) {
      return connection.getItemHasher(this.protocol);
   }

   protected void updateHashedItemDataComponentIds(HashedItem item, FullMappings mappings) {
      Int2IntMap addedData = item.dataHashesById();
      if (!addedData.isEmpty()) {
         for (int id : addedData.keySet().toIntArray()) {
            int mappedId = mappings.getNewId(id);
            if (mappedId != id) {
               int hash = addedData.remove(id);
               if (mappedId != -1) {
                  addedData.put(mappedId, hash);
               }
            }
         }
      }

      IntSet removedData = item.removedDataIds();
      if (!removedData.isEmpty()) {
         for (int idx : removedData.toIntArray()) {
            int mappedId = mappings.getNewId(idx);
            if (mappedId != idx) {
               removedData.remove(idx);
               if (mappedId != -1) {
                  removedData.add(mappedId);
               }
            }
         }
      }
   }

   @Override
   public Type<Item> itemType() {
      return this.itemType;
   }

   @Override
   public Type<Item[]> itemArrayType() {
      return this.itemArrayType;
   }

   @Override
   public Type<Item> mappedItemType() {
      return this.mappedItemType;
   }

   @Override
   public Type<Item[]> mappedItemArrayType() {
      return this.mappedItemArrayType;
   }

   @Override
   public Type<Item> itemTemplateType() {
      return this.itemTemplateType;
   }

   @Override
   public Type<Item> mappedItemTemplateType() {
      return this.mappedItemTemplateType;
   }
}
