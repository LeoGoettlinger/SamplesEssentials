package com.viaversion.viabackwards.protocol.v1_21_2to1_21.rewriter;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.rewriters.BackwardsFullStructuredItemRewriter;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.Protocol1_21_2To1_21;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage.BackwardsStorables1_21_2;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage.InventoryStateIdStorage;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage.RecipeStorage;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage.SignStorage;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.minecraft.item.data.DeathProtection;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantable;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.Equippable;
import com.viaversion.viaversion.api.minecraft.item.data.FoodProperties1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.Instrument1_21_2;
import com.viaversion.viaversion.api.minecraft.item.data.ItemModel;
import com.viaversion.viaversion.api.minecraft.item.data.PotionEffect;
import com.viaversion.viaversion.api.minecraft.item.data.PotionEffectData;
import com.viaversion.viaversion.api.minecraft.item.data.Repairable;
import com.viaversion.viaversion.api.minecraft.item.data.UseCooldown;
import com.viaversion.viaversion.api.minecraft.item.data.consumable.ApplyStatusEffects;
import com.viaversion.viaversion.api.minecraft.item.data.consumable.ConsumeEffect;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Limit;
import com.viaversion.viaversion.util.Unit;
import java.util.ArrayList;
import java.util.List;

public final class BlockItemPacketRewriter1_21_2
   extends BackwardsFullStructuredItemRewriter<ClientboundPacket1_21_2, ServerboundPacket1_20_5, Protocol1_21_2To1_21> {
   public BlockItemPacketRewriter1_21_2(Protocol1_21_2To1_21 protocol) {
      super(protocol);
   }

   public void registerPackets() {
      ((Protocol1_21_2To1_21)this.protocol)
         .replaceClientbound(
            ClientboundPackets1_21_2.LEVEL_CHUNK_WITH_LIGHT,
            wrapper -> {
               Chunk chunk = ((Protocol1_21_2To1_21)this.protocol).getBlockRewriter().handleChunk1_18(wrapper);
               ((Protocol1_21_2To1_21)this.protocol).getBlockRewriter().handleBlockEntities(chunk, wrapper.user());
               if (wrapper.user().getProtocolInfo().protocolVersion().equalTo(ProtocolVersion.v1_21)) {
                  EntityTracker tracker = wrapper.user().getEntityTracker(this.protocol);
                  SignStorage storage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).signStorage();
                  storage.removeSigns(chunk.getX(), chunk.getZ());

                  for (int i = 0; i < chunk.getSections().length; i++) {
                     ChunkSection section = chunk.getSections()[i];
                     if (section.getNonAirBlocksCount() != 0) {
                        DataPalette blockPalette = section.palette(PaletteType.BLOCKS);
                        int ySection = i;
                        blockPalette.forEachMatchingCoordinate(
                           this::signBlockState,
                           idx -> storage.addSign(
                              new BlockPosition(
                                 ChunkSection.xFromIndex(idx) + (chunk.getX() << 4),
                                 ChunkSection.yFromIndex(idx) + tracker.currentMinY() + (ySection << 4),
                                 ChunkSection.zFromIndex(idx) + (chunk.getZ() << 4)
                              )
                           )
                        );
                     }
                  }
               }
            }
         );
      ((Protocol1_21_2To1_21)this.protocol).replaceClientbound(ClientboundPackets1_21_2.BLOCK_UPDATE, wrapper -> {
         BlockPosition position = (BlockPosition)wrapper.passthrough(Types.BLOCK_POSITION1_14);
         int blockId = (Integer)wrapper.read(Types.VAR_INT);
         int mappedBlockId = ((Protocol1_21_2To1_21)this.protocol).getMappingData().getNewBlockStateId(blockId);
         wrapper.write(Types.VAR_INT, mappedBlockId);
         if (wrapper.user().getProtocolInfo().protocolVersion().equalTo(ProtocolVersion.v1_21)) {
            SignStorage storage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).signStorage();
            storage.removeSign(position);
            if (this.signBlockState(mappedBlockId)) {
               storage.addSign(position);
            }
         }
      });
      ((Protocol1_21_2To1_21)this.protocol).replaceClientbound(ClientboundPackets1_21_2.SECTION_BLOCKS_UPDATE, wrapper -> {
         long position = (Long)wrapper.passthrough(Types.LONG);
         int chunkX = (int)(position >> 42);
         int chunkY = (int)(position << 44 >> 44);
         int chunkZ = (int)(position << 22 >> 42);
         SignStorage signStorage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).signStorage();
         boolean equalToV1_21 = wrapper.user().getProtocolInfo().protocolVersion().equalTo(ProtocolVersion.v1_21);

         for (BlockChangeRecord record : (BlockChangeRecord[])wrapper.passthrough(Types.VAR_LONG_BLOCK_CHANGE_ARRAY)) {
            record.setBlockId(((Protocol1_21_2To1_21)this.protocol).getMappingData().getNewBlockStateId(record.getBlockId()));
            if (equalToV1_21) {
               int x = record.getSectionX() + (chunkX << 4);
               int y = record.getSectionY() + (chunkY << 4);
               int z = record.getSectionZ() + (chunkZ << 4);
               BlockPosition pos = new BlockPosition(x, y, z);
               if (this.signBlockState(record.getBlockId())) {
                  signStorage.addSign(pos);
               } else {
                  signStorage.removeSign(pos);
               }
            }
         }
      });
      ((Protocol1_21_2To1_21)this.protocol).registerClientbound(ClientboundPackets1_21_2.OPEN_SIGN_EDITOR, wrapper -> {
         if (wrapper.user().getProtocolInfo().protocolVersion().equalTo(ProtocolVersion.v1_21)) {
            BlockPosition position = (BlockPosition)wrapper.passthrough(Types.BLOCK_POSITION1_14);
            if (!((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).signStorage().isSign(position)) {
               wrapper.cancel();
            }
         }
      });
      ((Protocol1_21_2To1_21)this.protocol).replaceClientbound(ClientboundPackets1_21_2.COOLDOWN, wrapper -> {
         MappingData mappingData = ((Protocol1_21_2To1_21)this.protocol).getMappingData();
         String itemIdentifier = (String)wrapper.read(Types.STRING);
         int id = mappingData.getFullItemMappings().id(itemIdentifier);
         if (id != -1) {
            int mappedId = mappingData.getFullItemMappings().getNewId(id);
            wrapper.write(Types.VAR_INT, mappedId);
         } else {
            wrapper.cancel();
         }
      });
      ((Protocol1_21_2To1_21)this.protocol)
         .registerClientbound(ClientboundPackets1_21_2.SET_CURSOR_ITEM, ClientboundPackets1_21.CONTAINER_SET_SLOT, wrapper -> {
            wrapper.write(Types.BYTE, (byte)-1);
            wrapper.write(Types.VAR_INT, ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).inventoryStateIdStorage().stateId());
            wrapper.write(Types.SHORT, Short.valueOf((short)-1));
            this.passthroughClientboundItem(wrapper);
         });
      ((Protocol1_21_2To1_21)this.protocol).replaceClientbound(ClientboundPackets1_21_2.OPEN_SCREEN, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int containerType = (Integer)wrapper.passthrough(Types.VAR_INT);
         if (containerType == 21) {
            ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).inventoryStateIdStorage().setSmithingTableOpen(true);
         }

         ((Protocol1_21_2To1_21)this.protocol).getComponentRewriter().passthroughAndProcess(wrapper);
      });
      ((Protocol1_21_2To1_21)this.protocol).replaceClientbound(ClientboundPackets1_21_2.CONTAINER_SET_CONTENT, wrapper -> {
         this.varIntToUnsignedByte(wrapper);
         int stateId = (Integer)wrapper.passthrough(Types.VAR_INT);
         ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).inventoryStateIdStorage().setStateId(stateId);
         Item[] items = (Item[])wrapper.read(this.itemArrayType());
         wrapper.write(this.mappedItemArrayType(), items);

         for (int i = 0; i < items.length; i++) {
            items[i] = this.handleItemToClient(wrapper.user(), items[i]);
         }

         this.passthroughClientboundItem(wrapper);
      });
      ((Protocol1_21_2To1_21)this.protocol).replaceClientbound(ClientboundPackets1_21_2.CONTAINER_SET_SLOT, wrapper -> {
         int containerId = (Integer)wrapper.read(Types.VAR_INT);
         wrapper.write(Types.BYTE, (byte)containerId);
         int stateId = (Integer)wrapper.passthrough(Types.VAR_INT);
         ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).inventoryStateIdStorage().setStateId(stateId);
         short slotId = (Short)wrapper.passthrough(Types.SHORT);
         Item item = this.handleItemToClient(wrapper.user(), (Item)wrapper.read(this.itemType()));
         if (containerId == 0 && slotId >= 5 && slotId <= 8) {
            wrapper.write(this.mappedItemType(), item.copy());
            EntityTracker tracker = wrapper.user().getEntityTracker(this.protocol);
            PacketWrapper equipment = PacketWrapper.create(ClientboundPackets1_21.SET_EQUIPMENT, wrapper.user());
            equipment.write(Types.VAR_INT, tracker.clientEntityId());
            equipment.write(Types.BYTE, (byte)(10 - slotId));
            equipment.write(this.mappedItemType(), item);
            equipment.send(Protocol1_21_2To1_21.class);
         } else {
            wrapper.write(this.mappedItemType(), item);
         }
      });
      ((Protocol1_21_2To1_21)this.protocol).registerClientbound(ClientboundPackets1_21_2.CONTAINER_SET_DATA, wrapper -> {
         this.varIntToUnsignedByte(wrapper);
         if (((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).inventoryStateIdStorage().smithingTableOpen()) {
            wrapper.cancel();
         }
      });
      ((Protocol1_21_2To1_21)this.protocol).registerClientbound(ClientboundPackets1_21_2.CONTAINER_CLOSE, wrapper -> {
         this.varIntToUnsignedByte(wrapper);
         ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).inventoryStateIdStorage().setSmithingTableOpen(false);
      });
      ((Protocol1_21_2To1_21)this.protocol).registerClientbound(ClientboundPackets1_21_2.SET_HELD_SLOT, ClientboundPackets1_21.SET_CARRIED_ITEM);
      ((Protocol1_21_2To1_21)this.protocol).registerClientbound(ClientboundPackets1_21_2.HORSE_SCREEN_OPEN, this::varIntToUnsignedByte);
      ((Protocol1_21_2To1_21)this.protocol).registerServerbound(ServerboundPackets1_20_5.CONTAINER_CLOSE, wrapper -> {
         this.byteToVarInt(wrapper);
         ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).inventoryStateIdStorage().setSmithingTableOpen(false);
      });
      ((Protocol1_21_2To1_21)this.protocol).replaceServerbound(ServerboundPackets1_20_5.CONTAINER_CLICK, wrapper -> {
         this.byteToVarInt(wrapper);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.SHORT);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.VAR_INT);
         int length = Limit.max((Integer)wrapper.passthrough(Types.VAR_INT), 128);

         for (int i = 0; i < length; i++) {
            wrapper.passthrough(Types.SHORT);
            wrapper.write(this.itemType(), this.handleItemToServer(wrapper.user(), (Item)wrapper.read(this.mappedItemType())));
         }

         wrapper.write(this.itemType(), this.handleItemToServer(wrapper.user(), (Item)wrapper.read(this.mappedItemType())));
      });
      ((Protocol1_21_2To1_21)this.protocol)
         .replaceServerbound(
            ServerboundPackets1_20_5.SET_CREATIVE_MODE_SLOT,
            wrapper -> {
               if (((Protocol1_21_2To1_21)this.protocol).getEntityRewriter() != null
                  && !((Protocol1_21_2To1_21)this.protocol).getEntityRewriter().tracker(wrapper.user()).canInstaBuild()) {
                  wrapper.cancel();
               } else {
                  short slot = (Short)wrapper.passthrough(Types.SHORT);
                  Item item = (Item)wrapper.read(this.mappedItemType());
                  InventoryStateIdStorage storage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).inventoryStateIdStorage();
                  boolean empty = Item.isEmpty(item);
                  if (empty && slot == storage.nextClearedSlot()) {
                     if (slot == 45) {
                        storage.setNextClearedSlot(0);
                        this.sendEmptyInventorySlots(wrapper.user());
                     } else {
                        storage.setNextClearedSlot(slot + 1);
                     }
                  } else {
                     storage.setNextClearedSlot(empty && slot == 0 ? 1 : 0);
                  }

                  wrapper.write(this.itemType(), this.handleItemToServer(wrapper.user(), item));
               }
            }
         );
      ((Protocol1_21_2To1_21)this.protocol).registerServerbound(ServerboundPackets1_20_5.USE_ITEM_ON, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.BLOCK_POSITION1_14);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.write(Types.BOOLEAN, false);
      });
      ((Protocol1_21_2To1_21)this.protocol).replaceClientbound(ClientboundPackets1_21_2.EXPLODE, wrapper -> {
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.write(Types.FLOAT, 0.0F);
         wrapper.write(Types.VAR_INT, 0);
         double knockbackX = 0.0;
         double knockbackY = 0.0;
         double knockbackZ = 0.0;
         if ((Boolean)wrapper.read(Types.BOOLEAN)) {
            knockbackX = (Double)wrapper.read(Types.DOUBLE);
            knockbackY = (Double)wrapper.read(Types.DOUBLE);
            knockbackZ = (Double)wrapper.read(Types.DOUBLE);
         }

         wrapper.write(Types.FLOAT, (float)knockbackX);
         wrapper.write(Types.FLOAT, (float)knockbackY);
         wrapper.write(Types.FLOAT, (float)knockbackZ);
         wrapper.write(Types.VAR_INT, 0);
         Particle explosionParticle = (Particle)wrapper.read(VersionedTypes.V1_21.particle());
         ((Protocol1_21_2To1_21)this.protocol).getParticleRewriter().rewriteParticle(wrapper.user(), explosionParticle);
         wrapper.write(VersionedTypes.V1_21_2.particle(), explosionParticle);
         wrapper.write(VersionedTypes.V1_21_2.particle(), explosionParticle);
         new SoundRewriter(this.protocol).soundHolderHandler().handle(wrapper);
      });
      ((Protocol1_21_2To1_21)this.protocol).registerClientbound(ClientboundPackets1_21_2.RECIPE_BOOK_ADD, null, wrapper -> {
         RecipeStorage recipeStorage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).recipeStorage();
         int size = (Integer)wrapper.read(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            recipeStorage.readRecipe(wrapper);
         }

         boolean replace = (Boolean)wrapper.read(Types.BOOLEAN);
         if (replace) {
            recipeStorage.clearRecipes();
         }

         recipeStorage.sendRecipes(wrapper.user());
         wrapper.cancel();
      });
      ((Protocol1_21_2To1_21)this.protocol).registerClientbound(ClientboundPackets1_21_2.RECIPE_BOOK_REMOVE, ClientboundPackets1_21.RECIPE, wrapper -> {
         RecipeStorage recipeStorage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).recipeStorage();
         int[] ids = (int[])wrapper.read(Types.VAR_INT_ARRAY_PRIMITIVE);
         recipeStorage.lockRecipes(wrapper, ids);
      });
      ((Protocol1_21_2To1_21)this.protocol).registerClientbound(ClientboundPackets1_21_2.RECIPE_BOOK_SETTINGS, null, wrapper -> {
         RecipeStorage recipeStorage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).recipeStorage();
         boolean[] settings = new boolean[8];

         for (int i = 0; i < 8; i++) {
            settings[i] = (Boolean)wrapper.read(Types.BOOLEAN);
         }

         recipeStorage.setRecipeBookSettings(settings);
         wrapper.cancel();
      });
      ((Protocol1_21_2To1_21)this.protocol).registerClientbound(ClientboundPackets1_21_2.UPDATE_RECIPES, wrapper -> {
         int size = (Integer)wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.read(Types.STRING);
            wrapper.read(Types.VAR_INT_ARRAY_PRIMITIVE);
         }

         RecipeStorage recipeStorage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).recipeStorage();
         recipeStorage.readStoneCutterRecipes(wrapper);
         wrapper.cancel();
      });
      ((Protocol1_21_2To1_21)this.protocol).registerClientbound(ClientboundPackets1_21_2.PLACE_GHOST_RECIPE, wrapper -> {
         this.varIntToByte(wrapper);
         wrapper.cancel();
      });
      ((Protocol1_21_2To1_21)this.protocol).registerServerbound(ServerboundPackets1_20_5.PLACE_RECIPE, wrapper -> {
         this.byteToVarInt(wrapper);
         String recipe = Key.stripMinecraftNamespace((String)wrapper.read(Types.STRING));
         wrapper.write(Types.VAR_INT, Integer.parseInt(recipe));
      });
      ((Protocol1_21_2To1_21)this.protocol).registerServerbound(ServerboundPackets1_20_5.RECIPE_BOOK_SEEN_RECIPE, wrapper -> {
         String recipe = Key.stripMinecraftNamespace((String)wrapper.read(Types.STRING));
         wrapper.write(Types.VAR_INT, Integer.parseInt(recipe));
      });
      ((Protocol1_21_2To1_21)this.protocol)
         .registerClientbound(ClientboundPackets1_21_2.SET_PLAYER_INVENTORY, ClientboundPackets1_21.CONTAINER_SET_SLOT, wrapper -> {
            wrapper.write(Types.BYTE, (byte)-2);
            wrapper.write(Types.VAR_INT, 0);
            int slot = (Integer)wrapper.read(Types.VAR_INT);
            wrapper.write(Types.SHORT, (short)slot);
            this.passthroughClientboundItem(wrapper);
         });
   }

   private void sendEmptyInventorySlots(UserConnection connection) {
      BackwardsStorables1_21_2 storables = (BackwardsStorables1_21_2)connection.storables(this.protocol);
      int stateId = storables.inventoryStateIdStorage().stateId();

      for (int slot = 1; slot <= 45; slot++) {
         PacketWrapper setSlotPacket = PacketWrapper.create(ClientboundPackets1_21.CONTAINER_SET_SLOT, connection);
         setSlotPacket.write(Types.BYTE, (byte)0);
         setSlotPacket.write(Types.VAR_INT, stateId);
         setSlotPacket.write(Types.SHORT, (short)slot);
         setSlotPacket.write(this.mappedItemType(), StructuredItem.empty());
         setSlotPacket.send(Protocol1_21_2To1_21.class);
      }
   }

   private void varIntToUnsignedByte(PacketWrapper wrapper) {
      int containerId = (Integer)wrapper.read(Types.VAR_INT);
      wrapper.write(Types.UNSIGNED_BYTE, (short)containerId);
   }

   private void varIntToByte(PacketWrapper wrapper) {
      int containerId = (Integer)wrapper.read(Types.VAR_INT);
      wrapper.write(Types.BYTE, (byte)containerId);
   }

   private void byteToVarInt(PacketWrapper wrapper) {
      byte containerId = (Byte)wrapper.read(Types.BYTE);
      wrapper.write(Types.VAR_INT, Integer.valueOf(containerId));
   }

   private boolean signBlockState(int blockStateId) {
      return blockStateId >= 4302 && blockStateId <= 4589 || blockStateId >= 4762 && blockStateId <= 5625 || blockStateId >= 19276 && blockStateId <= 19355;
   }

   public Item handleItemToClient(UserConnection connection, Item item) {
      this.backupInconvertibleData(item);
      item = super.handleItemToClient(connection, item);
      com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter.BlockItemPacketRewriter1_21_2.downgradeItemData(item);
      return item;
   }

   @Override
   public Item handleItemToServer(UserConnection connection, Item item) {
      item = super.handleItemToServer(connection, item);
      StructuredDataContainer data = item.dataContainer();
      FoodProperties1_20_5 food = (FoodProperties1_20_5)data.get(StructuredDataKey.FOOD1_21);
      if (food != null && food.usingConvertsTo() != null) {
         this.handleItemToServer(connection, food.usingConvertsTo());
      }

      com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter.BlockItemPacketRewriter1_21_2.updateItemData(item);
      this.restoreInconvertibleData(item);
      Enchantments enchantments = (Enchantments)data.get(StructuredDataKey.ENCHANTMENTS1_20_5);
      if (enchantments != null) {
         boolean removed = enchantments.enchantments().values().removeIf(level -> level == 0);
         if (removed && enchantments.size() == 0) {
            data.set(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, true);
         }
      }

      Enchantments storedEnchantments = (Enchantments)data.get(StructuredDataKey.STORED_ENCHANTMENTS1_20_5);
      if (storedEnchantments != null) {
         storedEnchantments.enchantments().values().removeIf(level -> level == 0);
      }

      return item;
   }

   private void backupInconvertibleData(Item item) {
      StructuredDataContainer data = item.dataContainer();
      data.setIdLookup(this.protocol, true);
      CompoundTag backupTag = new CompoundTag();
      Holder<Instrument1_21_2> instrument = (Holder<Instrument1_21_2>)data.get(StructuredDataKey.INSTRUMENT1_21_2);
      if (instrument != null && instrument.isDirect()) {
         backupTag.put("instrument_description", ((Instrument1_21_2)instrument.value()).description());
      }

      Repairable repairable = (Repairable)data.get(StructuredDataKey.REPAIRABLE);
      if (repairable != null) {
         backupTag.put("repairable", this.holderSetToTag(repairable.items()));
      }

      Enchantable enchantable = (Enchantable)data.get(StructuredDataKey.ENCHANTABLE);
      if (enchantable != null) {
         backupTag.putInt("enchantable", enchantable.value());
      }

      UseCooldown useCooldown = (UseCooldown)data.get(StructuredDataKey.USE_COOLDOWN);
      if (useCooldown != null) {
         CompoundTag tag = new CompoundTag();
         tag.putFloat("seconds", useCooldown.seconds());
         if (useCooldown.cooldownGroup() != null) {
            tag.putString("cooldown_group", useCooldown.cooldownGroup());
         }

         backupTag.put("use_cooldown", tag);
      }

      ItemModel itemModel = (ItemModel)data.get(StructuredDataKey.ITEM_MODEL);
      if (itemModel != null) {
         backupTag.putString("item_model", itemModel.key().original());
      }

      Equippable equippable = (Equippable)data.get(StructuredDataKey.EQUIPPABLE1_21_2);
      if (equippable != null) {
         CompoundTag tag = new CompoundTag();
         tag.putInt("equipment_slot", equippable.equipmentSlot());
         this.saveSoundEventHolder(tag, equippable.soundEvent());
         String model = equippable.model();
         if (model != null) {
            tag.putString("model", model);
         }

         String cameraOverlay = equippable.cameraOverlay();
         if (cameraOverlay != null) {
            tag.putString("camera_overlay", cameraOverlay);
         }

         if (equippable.allowedEntities() != null) {
            tag.put("allowed_entities", this.holderSetToTag(equippable.allowedEntities()));
         }

         tag.putBoolean("dispensable", equippable.dispensable());
         tag.putBoolean("swappable", equippable.swappable());
         tag.putBoolean("damage_on_hurt", equippable.damageOnHurt());
         backupTag.put("equippable", tag);
      }

      Unit glider = (Unit)data.get(StructuredDataKey.GLIDER);
      if (glider != null) {
         backupTag.putBoolean("glider", true);
      }

      Key tooltipStyle = (Key)data.get(StructuredDataKey.TOOLTIP_STYLE);
      if (tooltipStyle != null) {
         backupTag.putString("tooltip_style", tooltipStyle.original());
      }

      DeathProtection deathProtection = (DeathProtection)data.get(StructuredDataKey.DEATH_PROTECTION1_21_2);
      if (deathProtection != null) {
         ListTag<CompoundTag> tagx = new ListTag(CompoundTag.class);

         for (ConsumeEffect<?> effect : deathProtection.deathEffects()) {
            CompoundTag effectTag = new CompoundTag();
            this.convertConsumableEffect(effectTag, effect);
            tagx.add(effectTag);
         }

         backupTag.put("death_protection", tagx);
      }

      if (!backupTag.isEmpty()) {
         this.saveTag(this.createCustomTag(item), backupTag, "inconvertible_data");
      }
   }

   private void convertConsumableEffect(CompoundTag tag, ConsumeEffect<?> effect) {
      tag.putInt("id", effect.id());
      if (effect.type() == ApplyStatusEffects.TYPE && effect.value() instanceof ApplyStatusEffects value) {
         tag.putString("type", "apply_effects");
         ListTag<CompoundTag> effects = new ListTag(CompoundTag.class);

         for (PotionEffect potionEffect : value.effects()) {
            CompoundTag effectTag = new CompoundTag();
            effectTag.putInt("effect", potionEffect.effect());
            this.convertPotionEffectData(effectTag, potionEffect.effectData());
            effects.add(effectTag);
         }

         tag.put("effects", effects);
         tag.putFloat("probability", value.probability());
      } else if (effect.type() == Types.HOLDER_SET && effect.value() instanceof HolderSet set) {
         tag.putString("type", "remove_effects");
         tag.put("remove_effects", this.holderSetToTag(set));
      } else if (effect.type() == Types.EMPTY) {
         tag.putString("type", "clear_all_effects");
      } else if (effect.type() == Types.FLOAT) {
         tag.putString("type", "teleport_randomly");
         tag.putFloat("probability", (Float)effect.value());
      } else if (effect.type() == Types.SOUND_EVENT && effect.value() instanceof Holder sound) {
         tag.putString("type", "play_sound");
         this.saveSoundEventHolder(tag, sound);
      }
   }

   private void convertPotionEffectData(CompoundTag tag, PotionEffectData data) {
      tag.putInt("amplifier", data.amplifier());
      tag.putInt("duration", data.duration());
      tag.putBoolean("ambient", data.ambient());
      tag.putBoolean("show_particles", data.showParticles());
      tag.putBoolean("show_icon", data.showIcon());
      if (data.hiddenEffect() != null) {
         CompoundTag hiddenEffect = new CompoundTag();
         this.convertPotionEffectData(hiddenEffect, data.hiddenEffect());
         tag.put("hidden_effect", hiddenEffect);
      }
   }

   private ConsumeEffect<?> convertConsumableEffect(CompoundTag tag) {
      int id = tag.getInt("id");
      String type = tag.getString("type");
      if (!"apply_effects".equals(type)) {
         if ("remove_effects".equals(type)) {
            HolderSet set = this.restoreHolderSet(tag, "remove_effects");
            return new ConsumeEffect(id, Types.HOLDER_SET, set);
         } else if ("clear_all_effects".equals(type)) {
            return new ConsumeEffect(id, Types.EMPTY, Unit.INSTANCE);
         } else if ("teleport_randomly".equals(type)) {
            float probability = tag.getFloat("probability");
            return new ConsumeEffect(id, Types.FLOAT, probability);
         } else if ("play_sound".equals(type)) {
            Holder<SoundEvent> sound = this.restoreSoundEventHolder(tag);
            return new ConsumeEffect(id, Types.SOUND_EVENT, sound);
         } else {
            return null;
         }
      } else {
         ListTag<CompoundTag> effects = tag.getListTag("effects", CompoundTag.class);
         List<PotionEffect> potionEffects = new ArrayList<>();

         for (int i = 0; i < effects.size(); i++) {
            CompoundTag effectTag = (CompoundTag)effects.get(i);
            int effect = effectTag.getInt("effect");
            PotionEffectData data = this.convertPotionEffectData(effectTag);
            potionEffects.add(new PotionEffect(effect, data));
         }

         float probability = tag.getFloat("probability");
         return new ConsumeEffect(id, ApplyStatusEffects.TYPE, new ApplyStatusEffects(potionEffects.toArray(PotionEffect[]::new), probability));
      }
   }

   private PotionEffectData convertPotionEffectData(CompoundTag tag) {
      int amplifier = tag.getInt("amplifier");
      int duration = tag.getInt("duration");
      boolean ambient = tag.getBoolean("ambient");
      boolean showParticles = tag.getBoolean("show_particles");
      boolean showIcon = tag.getBoolean("show_icon");
      CompoundTag hiddenEffect = tag.getCompoundTag("hidden_effect");
      return new PotionEffectData(
         amplifier, duration, ambient, showParticles, showIcon, hiddenEffect != null ? this.convertPotionEffectData(hiddenEffect) : null
      );
   }

   private void restoreInconvertibleData(Item item) {
      StructuredDataContainer data = item.dataContainer();
      CompoundTag customData = (CompoundTag)data.get(StructuredDataKey.CUSTOM_DATA);
      if (customData != null && customData.remove(this.nbtTagName("inconvertible_data")) instanceof CompoundTag backupTag) {
         Holder<Instrument1_21_2> instrument = (Holder<Instrument1_21_2>)data.get(StructuredDataKey.INSTRUMENT1_21_2);
         if (instrument != null && instrument.isDirect()) {
            Tag description = backupTag.get("instrument_description");
            if (description != null) {
               Instrument1_21_2 delegate = (Instrument1_21_2)instrument.value();
               data.set(
                  StructuredDataKey.INSTRUMENT1_21_2,
                  Holder.of(new Instrument1_21_2(delegate.soundEvent(), delegate.useDuration(), delegate.range(), description))
               );
            }
         }

         if (backupTag.contains("repairable")) {
            data.set(StructuredDataKey.REPAIRABLE, new Repairable(this.restoreHolderSet(backupTag, "repairable")));
         }

         IntTag enchantable = backupTag.getIntTag("enchantable");
         if (enchantable != null) {
            data.set(StructuredDataKey.ENCHANTABLE, new Enchantable(enchantable.asInt()));
         }

         CompoundTag useCooldown = backupTag.getCompoundTag("use_cooldown");
         if (useCooldown != null) {
            float seconds = useCooldown.getFloat("seconds");
            String cooldownGroup = useCooldown.getString("cooldown_group");
            data.set(StructuredDataKey.USE_COOLDOWN, new UseCooldown(seconds, cooldownGroup));
         }

         String itemModel = backupTag.getString("item_model");
         if (itemModel != null) {
            data.set(StructuredDataKey.ITEM_MODEL, new ItemModel(Key.of(itemModel)));
         }

         CompoundTag equippable = backupTag.getCompoundTag("equippable");
         if (equippable != null) {
            int equipmentSlot = equippable.getInt("equipment_slot");
            Holder<SoundEvent> soundEvent = this.restoreSoundEventHolder(equippable);
            String model = equippable.getString("model");
            String cameraOverlay = equippable.getString("camera_overlay");
            HolderSet allowedEntities = equippable.contains("allowed_entities") ? this.restoreHolderSet(equippable, "allowed_entities") : null;
            boolean dispensable = equippable.getBoolean("dispensable");
            boolean swappable = equippable.getBoolean("swappable");
            boolean damageOnHurt = equippable.getBoolean("damage_on_hurt");
            data.set(
               StructuredDataKey.EQUIPPABLE1_21_2,
               new Equippable(equipmentSlot, soundEvent, model, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt)
            );
         }

         ByteTag glider = backupTag.getByteTag("glider");
         if (glider != null) {
            data.set(StructuredDataKey.GLIDER, Unit.INSTANCE);
         }

         String tooltipStyle = backupTag.getString("tooltip_style");
         if (tooltipStyle != null) {
            data.set(StructuredDataKey.TOOLTIP_STYLE, Key.of(tooltipStyle));
         }

         ListTag<CompoundTag> deathProtection = backupTag.getListTag("death_protection", CompoundTag.class);
         if (deathProtection != null) {
            List<ConsumeEffect<?>> effects = new ArrayList<>();

            for (int i = 0; i < deathProtection.size(); i++) {
               ConsumeEffect<?> effect = this.convertConsumableEffect((CompoundTag)deathProtection.get(i));
               if (effect != null) {
                  effects.add(effect);
               }
            }

            data.set(StructuredDataKey.DEATH_PROTECTION1_21_2, new DeathProtection(effects.toArray(ConsumeEffect[]::new)));
         }

         this.removeCustomTag(data, customData);
      }
   }
}
