package com.viaversion.viabackwards.protocol.v1_21_5to1_21_4.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.LongArrayTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.rewriters.BackwardsStructuredItemRewriter;
import com.viaversion.viabackwards.protocol.v1_21_5to1_21_4.Protocol1_21_5To1_21_4;
import com.viaversion.viabackwards.protocol.v1_21_5to1_21_4.storage.HorseDataStorage;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.PaintingVariant;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk1_18;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.Heightmap;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_5;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_2;
import com.viaversion.viaversion.api.minecraft.item.HashedItem;
import com.viaversion.viaversion.api.minecraft.item.HashedStructuredItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.BlocksAttacks;
import com.viaversion.viaversion.api.minecraft.item.data.Equippable;
import com.viaversion.viaversion.api.minecraft.item.data.ToolProperties;
import com.viaversion.viaversion.api.minecraft.item.data.TooltipDisplay;
import com.viaversion.viaversion.api.minecraft.item.data.TropicalFishPattern;
import com.viaversion.viaversion.api.minecraft.item.data.Weapon;
import com.viaversion.viaversion.api.minecraft.item.data.BlocksAttacks.DamageReduction;
import com.viaversion.viaversion.api.minecraft.item.data.BlocksAttacks.ItemDamageFunction;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrimMaterial1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ProvidesTrimMaterial1_20_5;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.rewriter.ComponentRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkBiomesType1_19_4;
import com.viaversion.viaversion.api.type.types.chunk.ChunkBiomesType1_21_5;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_21_5;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.data.item.ItemHasherBase;
import com.viaversion.viaversion.data.item.OriginalHashedItem;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntLinkedOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.packet.ServerboundPacket1_21_4;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.packet.ServerboundPackets1_21_4;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.util.Either;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Limit;
import com.viaversion.viaversion.util.MathUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter1_21_5
   extends BackwardsStructuredItemRewriter<ClientboundPacket1_21_5, ServerboundPacket1_21_4, Protocol1_21_5To1_21_4> {
   private static final int SADDLE_EQUIPMENT_SLOT = 7;
   static final byte SADDLED_FLAG = 4;

   public BlockItemPacketRewriter1_21_5(Protocol1_21_5To1_21_4 protocol) {
      super(protocol);
   }

   public void registerPackets() {
      ((Protocol1_21_5To1_21_4)this.protocol)
         .replaceClientbound(
            ClientboundPackets1_21_5.LEVEL_CHUNK_WITH_LIGHT,
            wrapper -> {
               EntityTracker tracker = ((Protocol1_21_5To1_21_4)this.protocol).getEntityRewriter().tracker(wrapper.user());
               Mappings blockStateMappings = ((Protocol1_21_5To1_21_4)this.protocol).getMappingData().getBlockStateMappings();
               Type<Chunk> chunkType = new ChunkType1_21_5(
                  tracker.currentWorldSectionHeight(), MathUtil.ceilLog2(blockStateMappings.size()), MathUtil.ceilLog2(tracker.biomesSent())
               );
               Chunk chunk = (Chunk)wrapper.read(chunkType);
               ((Protocol1_21_5To1_21_4)this.protocol).getBlockRewriter().handleChunk(chunk);
               ((Protocol1_21_5To1_21_4)this.protocol).getBlockRewriter().handleBlockEntities(chunk, wrapper.user());
               Type<Chunk> newChunkType = new ChunkType1_20_2(
                  tracker.currentWorldSectionHeight(), MathUtil.ceilLog2(blockStateMappings.mappedSize()), MathUtil.ceilLog2(tracker.biomesSent())
               );
               CompoundTag heightmapTag = new CompoundTag();

               for (Heightmap heightmap : chunk.heightmaps()) {
                  String typeKey = this.heightmapType(heightmap.type());
                  if (typeKey == null) {
                     ((Protocol1_21_5To1_21_4)this.protocol).getLogger().warning("Unknown heightmap type id: " + heightmap.type());
                  } else {
                     heightmapTag.put(typeKey, new LongArrayTag(heightmap.data()));
                  }
               }

               Chunk mappedChunk = new Chunk1_18(chunk.getX(), chunk.getZ(), chunk.getSections(), heightmapTag, chunk.blockEntities());
               wrapper.write(newChunkType, mappedChunk);
            }
         );
      ((Protocol1_21_5To1_21_4)this.protocol).registerClientbound(ClientboundPackets1_21_5.CHUNKS_BIOMES, wrapper -> {
         EntityTracker tracker = ((Protocol1_21_5To1_21_4)this.protocol).getEntityRewriter().tracker(wrapper.user());
         int globalPaletteBiomeBits = MathUtil.ceilLog2(tracker.biomesSent());
         Type<DataPalette[]> biomesType = new ChunkBiomesType1_21_5(tracker.currentWorldSectionHeight(), globalPaletteBiomeBits);
         Type<DataPalette[]> newBiomesType = new ChunkBiomesType1_19_4(tracker.currentWorldSectionHeight(), globalPaletteBiomeBits);
         int size = (Integer)wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.CHUNK_POSITION);
            wrapper.passthroughAndMap(biomesType, newBiomesType);
         }
      });
      ((Protocol1_21_5To1_21_4)this.protocol).replaceServerbound(ServerboundPackets1_21_4.SET_CREATIVE_MODE_SLOT, wrapper -> {
         wrapper.passthrough(Types.SHORT);
         Item item = this.handleItemToServer(wrapper.user(), (Item)wrapper.read(this.mappedItemType()));
         wrapper.write(VersionedTypes.V1_21_5.lengthPrefixedItem(), item);
      });
      ((Protocol1_21_5To1_21_4)this.protocol).replaceServerbound(ServerboundPackets1_21_4.CONTAINER_CLICK, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.SHORT);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.VAR_INT);
         int affectedItems = Limit.max((Integer)wrapper.passthrough(Types.VAR_INT), 128);

         for (int i = 0; i < affectedItems; i++) {
            wrapper.passthrough(Types.SHORT);
            this.itemToHashedItem(wrapper);
         }

         this.itemToHashedItem(wrapper);
      });
      ((Protocol1_21_5To1_21_4)this.protocol)
         .replaceClientbound(
            ClientboundPackets1_21_5.SET_EQUIPMENT,
            wrapper -> {
               int entityId = (Integer)wrapper.passthrough(Types.VAR_INT);
               TrackedEntity trackedEntity = ((Protocol1_21_5To1_21_4)this.protocol).getEntityRewriter().tracker(wrapper.user()).entity(entityId);
               IntList keptSlots = new IntArrayList();
               List<Item> keptItems = new ArrayList<>();

               byte value;
               do {
                  value = (Byte)wrapper.read(Types.BYTE);
                  int equipmentSlot = value & 127;
                  Item item = (Item)wrapper.read(this.itemType());
                  if (equipmentSlot == 7) {
                     if (trackedEntity != null
                        && (
                           trackedEntity.entityType().isOrHasParent(EntityTypes1_21_5.ABSTRACT_HORSE)
                              || trackedEntity.entityType().isOrHasParent(EntityTypes1_21_5.PIG)
                              || trackedEntity.entityType().isOrHasParent(EntityTypes1_21_5.STRIDER)
                        )) {
                        this.sendSaddledEntityData(wrapper.user(), trackedEntity, entityId, item.identifier() == 800);
                     }
                  } else {
                     keptSlots.add(equipmentSlot);
                     keptItems.add(this.handleItemToClient(wrapper.user(), item));
                  }
               } while (value < 0);

               if (keptSlots.isEmpty()) {
                  wrapper.cancel();
               } else {
                  for (int i = 0; i < keptSlots.size(); i++) {
                     int slot = keptSlots.getInt(i);
                     boolean more = i < keptSlots.size() - 1;
                     wrapper.write(Types.BYTE, (byte)(more ? slot | -128 : slot));
                     wrapper.write(this.mappedItemType(), keptItems.get(i));
                  }
               }
            }
         );
      ((Protocol1_21_5To1_21_4)this.protocol).replaceClientbound(ClientboundPackets1_21_5.UPDATE_ADVANCEMENTS, wrapper -> {
         wrapper.passthrough(Types.BOOLEAN);
         int size = (Integer)wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.OPTIONAL_STRING);
            if ((Boolean)wrapper.passthrough(Types.BOOLEAN)) {
               Tag title = (Tag)wrapper.passthrough(Types.TRUSTED_TAG);
               Tag description = (Tag)wrapper.passthrough(Types.TRUSTED_TAG);
               ComponentRewriter componentRewriter = ((Protocol1_21_5To1_21_4)this.protocol).getComponentRewriter();
               if (componentRewriter != null) {
                  componentRewriter.processTag(wrapper.user(), title);
                  componentRewriter.processTag(wrapper.user(), description);
               }

               this.passthroughClientboundItem(wrapper);
               wrapper.passthrough(Types.VAR_INT);
               int flags = (Integer)wrapper.passthrough(Types.INT);
               if ((flags & 1) != 0) {
                  this.convertClientAsset(wrapper);
               }

               wrapper.passthrough(Types.FLOAT);
               wrapper.passthrough(Types.FLOAT);
            }

            int requirements = (Integer)wrapper.passthrough(Types.VAR_INT);

            for (int array = 0; array < requirements; array++) {
               wrapper.passthrough(Types.STRING_ARRAY);
            }

            wrapper.passthrough(Types.BOOLEAN);
         }

         wrapper.passthrough(Types.STRING_ARRAY);
         int progressSize = (Integer)wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < progressSize; i++) {
            wrapper.passthrough(Types.STRING);
            int criterionSize = (Integer)wrapper.passthrough(Types.VAR_INT);

            for (int j = 0; j < criterionSize; j++) {
               wrapper.passthrough(Types.STRING);
               wrapper.passthrough(Types.OPTIONAL_LONG);
            }
         }

         wrapper.read(Types.BOOLEAN);
      });
   }

   protected void storeOriginalHashedItemIfNeeded(UserConnection connection, Item item, ItemHasherBase hasher, HashedItem originalHashedItem) {
      if (originalHashedItem != null) {
         if (originalHashedItem instanceof OriginalHashedItem restoredHashedItem) {
            hasher.trackOriginalHashedItem(
               (CompoundTag)item.dataContainer().get(StructuredDataKey.CUSTOM_DATA), originalHashedItem, restoredHashedItem.backupTagName()
            );
         } else {
            this.storeOriginalHashedItemInTag(connection, item, hasher, originalHashedItem);
         }
      }
   }

   protected boolean isFirstServerbound(UserConnection connection) {
      return true;
   }

   private void itemToHashedItem(PacketWrapper wrapper) {
      Item item = (Item)wrapper.read(this.mappedItemType());
      if (Item.isEmpty(item)) {
         wrapper.write(Types.HASHED_ITEM, HashedStructuredItem.empty());
      } else {
         HashedItem originalHash = this.originalHashedItemFromBackup(item);
         if (originalHash != null) {
            wrapper.write(Types.HASHED_ITEM, originalHash);
         } else {
            item.dataContainer().data().clear();
            Item serverItem = this.handleItemToServer(wrapper.user(), item);
            wrapper.write(Types.HASHED_ITEM, new HashedStructuredItem(serverItem.identifier(), serverItem.amount()));
         }
      }
   }

   @Nullable
   private HashedItem originalHashedItemFromBackup(Item item) {
      CompoundTag customData = (CompoundTag)item.dataContainer().get(StructuredDataKey.CUSTOM_DATA);
      CompoundTag originalHashes;
      if (customData != null && (originalHashes = customData.getCompoundTag("VV|original_hashes")) != null) {
         OriginalHashedItem originalHashedItem = this.backedUpOriginalHashes(originalHashes, item);
         return (HashedItem)(originalHashedItem.backupTagName().equals(this.nbtTagName()) ? originalHashedItem.asRegularItem() : originalHashedItem);
      } else {
         return null;
      }
   }

   private void convertClientAsset(PacketWrapper wrapper) {
      String background = (String)wrapper.read(Types.STRING);
      String namespace = Key.namespace(background);
      String path = Key.stripNamespace(background);
      wrapper.write(Types.STRING, namespace + ":textures/" + path + ".png");
   }

   private void sendSaddledEntityData(UserConnection connection, TrackedEntity trackedEntity, int entityId, boolean saddled) {
      EntityData entityData = null;
      if (trackedEntity.entityType().isOrHasParent(EntityTypes1_21_5.ABSTRACT_HORSE)) {
         byte data = 0;
         HorseDataStorage horseDataStorage = (HorseDataStorage)trackedEntity.get(HorseDataStorage.class);
         if (horseDataStorage != null) {
            if (horseDataStorage.saddled() == saddled) {
               return;
            }

            data = horseDataStorage.data();
         }

         trackedEntity.put(new HorseDataStorage(data, saddled));
         if (saddled) {
            data = (byte)(data | 4);
         }

         entityData = new EntityData(17, ((EntityDataTypes1_21_2)VersionedTypes.V1_21_4.entityDataTypes).byteType, data);
      } else if (trackedEntity.entityType().isOrHasParent(EntityTypes1_21_5.PIG)) {
         entityData = new EntityData(17, ((EntityDataTypes1_21_2)VersionedTypes.V1_21_4.entityDataTypes).booleanType, saddled);
      } else if (trackedEntity.entityType().isOrHasParent(EntityTypes1_21_5.STRIDER)) {
         entityData = new EntityData(19, ((EntityDataTypes1_21_2)VersionedTypes.V1_21_4.entityDataTypes).booleanType, saddled);
      }

      if (entityData != null) {
         PacketWrapper entityDataPacket = PacketWrapper.create(ClientboundPackets1_21_2.SET_ENTITY_DATA, connection);
         List<EntityData> entityDataList = new ArrayList<>();
         entityDataList.add(entityData);
         entityDataPacket.write(Types.VAR_INT, entityId);
         entityDataPacket.write(VersionedTypes.V1_21_4.entityDataList, entityDataList);
         entityDataPacket.send(Protocol1_21_5To1_21_4.class);
      }
   }

   private String heightmapType(int id) {
      return switch (id) {
         case 0 -> "WORLD_SURFACE_WG";
         case 1 -> "WORLD_SURFACE";
         case 2 -> "OCEAN_FLOOR_WG";
         case 3 -> "OCEAN_FLOOR";
         case 4 -> "MOTION_BLOCKING";
         case 5 -> "MOTION_BLOCKING_NO_LEAVES";
         default -> null;
      };
   }

   @Override
   protected void backupInconvertibleData(UserConnection connection, Item item, StructuredDataContainer dataContainer, CompoundTag backupTag) {
      super.backupInconvertibleData(connection, item, dataContainer, backupTag);
      ToolProperties toolProperties = (ToolProperties)dataContainer.get(StructuredDataKey.TOOL1_21_5);
      if (toolProperties != null && !toolProperties.canDestroyBlocksInCreative()) {
         backupTag.putBoolean("tool", false);
      }

      Equippable equippable = (Equippable)dataContainer.get(StructuredDataKey.EQUIPPABLE1_21_5);
      if (equippable != null && equippable.equipOnInteract()) {
         backupTag.putBoolean("equippable", true);
      }

      Weapon weapon = (Weapon)dataContainer.get(StructuredDataKey.WEAPON);
      if (weapon != null) {
         CompoundTag weaponTag = new CompoundTag();
         backupTag.put("weapon", weaponTag);
         weaponTag.putInt("item_damage_per_attack", weapon.itemDamagePerAttack());
         weaponTag.putFloat("disable_blocking_for_seconds", weapon.disableBlockingForSeconds());
      }

      ProvidesTrimMaterial1_20_5 providesTrimMaterial = (ProvidesTrimMaterial1_20_5)dataContainer.get(StructuredDataKey.PROVIDES_TRIM_MATERIAL1_21_5);
      if (providesTrimMaterial != null) {
         Tag materialTag = this.eitherHolderToTag(providesTrimMaterial.material(), (material, tag) -> {
            tag.putString("asset_name", material.assetName());
            tag.putInt("item_id", material.itemId());
            tag.putFloat("item_model_index", material.itemModelIndex());
            CompoundTag overrideArmorMaterials = new CompoundTag();
            material.overrideArmorMaterials().forEach(overrideArmorMaterials::putString);
            tag.put("override_armor_materials", overrideArmorMaterials);
            tag.put("description", material.description());
         });
         backupTag.put("provides_trim_material", materialTag);
      }

      BlocksAttacks blocksAttacks = (BlocksAttacks)dataContainer.get(StructuredDataKey.BLOCKS_ATTACKS1_21_5);
      if (blocksAttacks != null) {
         CompoundTag blocksAttackTag = new CompoundTag();
         backupTag.put("blocks_attack", blocksAttackTag);
         blocksAttackTag.putFloat("block_delay_seconds", blocksAttacks.blockDelaySeconds());
         blocksAttackTag.putFloat("disable_cooldown_scale", blocksAttacks.disableCooldownScale());
         ListTag<CompoundTag> damageReductions = new ListTag(CompoundTag.class);
         blocksAttackTag.put("damage_reductions", damageReductions);

         for (DamageReduction damageReduction : blocksAttacks.damageReductions()) {
            CompoundTag damageReductionTag = new CompoundTag();
            damageReductionTag.putFloat("horizontal_blocking_angle", damageReduction.horizontalBlockingAngle());
            if (damageReduction.type() != null) {
               damageReductionTag.put("type", this.holderSetToTag(damageReduction.type()));
            }

            damageReductionTag.putFloat("base", damageReduction.base());
            damageReductionTag.putFloat("factor", damageReduction.factor());
            damageReductions.add(damageReductionTag);
         }

         CompoundTag itemDamageTag = new CompoundTag();
         blocksAttackTag.put("item_damage", itemDamageTag);
         itemDamageTag.putFloat("threshold", blocksAttacks.itemDamage().threshold());
         itemDamageTag.putFloat("base", blocksAttacks.itemDamage().base());
         itemDamageTag.putFloat("factor", blocksAttacks.itemDamage().factor());
         if (blocksAttacks.bypassedBy() != null) {
            itemDamageTag.putString("bypassed_by", blocksAttacks.bypassedBy().tagKey());
         }

         if (blocksAttacks.blockSound() != null) {
            blocksAttackTag.put("block_sound", this.holderToTag(blocksAttacks.blockSound(), (x$0, x$1) -> this.saveSoundEvent(x$0, x$1)));
         }

         if (blocksAttacks.disableSound() != null) {
            blocksAttackTag.put("disable_sound", this.holderToTag(blocksAttacks.disableSound(), (x$0, x$1) -> this.saveSoundEvent(x$0, x$1)));
         }
      }

      TooltipDisplay tooltipDisplay = (TooltipDisplay)dataContainer.get(StructuredDataKey.TOOLTIP_DISPLAY);
      if (tooltipDisplay != null) {
         backupTag.put("hidden_components", new IntArrayTag(tooltipDisplay.hiddenComponents().toIntArray()));
      }

      TropicalFishPattern tropicalFishPattern = (TropicalFishPattern)dataContainer.get(StructuredDataKey.TROPICAL_FISH_PATTERN);
      if (tropicalFishPattern != null) {
         backupTag.putInt("tropical_fish_pattern", tropicalFishPattern.packedId());
      }

      this.saveKeyData(StructuredDataKey.PROVIDES_BANNER_PATTERNS1_21_5, dataContainer, backupTag);
      this.saveFloatData(StructuredDataKey.POTION_DURATION_SCALE, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.VILLAGER_VARIANT, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.FOX_VARIANT, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.SALMON_SIZE, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.PARROT_VARIANT, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.TROPICAL_FISH_BASE_COLOR, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.TROPICAL_FISH_PATTERN_COLOR, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.MOOSHROOM_VARIANT, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.RABBIT_VARIANT, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.FROG_VARIANT, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.HORSE_VARIANT, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.LLAMA_VARIANT, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.AXOLOTL_VARIANT, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.CAT_VARIANT, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.CAT_COLLAR, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.SHEEP_COLOR, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.SHULKER_COLOR, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.WOLF_SOUND_VARIANT, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.COW_VARIANT, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.PIG_VARIANT, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.WOLF_VARIANT, dataContainer, backupTag);
      this.saveIntData(StructuredDataKey.WOLF_COLLAR, dataContainer, backupTag);
      Either<Integer, String> chickenVariant = (Either<Integer, String>)dataContainer.get(StructuredDataKey.CHICKEN_VARIANT1_21_5);
      if (chickenVariant != null) {
         if (chickenVariant.isLeft()) {
            backupTag.putInt("chicken_variant", (Integer)chickenVariant.left());
         } else {
            backupTag.putString("chicken_variant", (String)chickenVariant.right());
         }
      }

      this.saveHolderData(StructuredDataKey.PAINTING_VARIANT, dataContainer, backupTag, (paintingVariant, tag) -> {
         tag.putInt("width", paintingVariant.width());
         tag.putInt("height", paintingVariant.height());
         tag.putString("asset_id", paintingVariant.assetId());
         if (paintingVariant.title() != null) {
            tag.put("title", paintingVariant.title());
         }

         if (paintingVariant.author() != null) {
            tag.put("author", paintingVariant.author());
         }
      });
      this.saveHolderData(StructuredDataKey.BREAK_SOUND, dataContainer, backupTag, (x$0, x$1) -> this.saveSoundEvent(x$0, x$1));
   }

   protected void handleItemDataComponentsToClient(UserConnection connection, Item item, StructuredDataContainer dataContainer) {
      super.handleItemDataComponentsToClient(connection, item, dataContainer);
      com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.BlockItemPacketRewriter1_21_5.downgradeItemData(item);
   }

   protected void handleItemDataComponentsToServer(UserConnection connection, Item item, StructuredDataContainer container) {
      super.handleItemDataComponentsToServer(connection, item, container);
      com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.BlockItemPacketRewriter1_21_5.updateItemData(item);
   }

   @Override
   protected void restoreBackupData(Item item, StructuredDataContainer data, CompoundTag customData) {
      super.restoreBackupData(item, data, customData);
      if (customData.remove(this.nbtTagName("backup")) instanceof CompoundTag backupTag) {
         IntArrayTag hiddenComponentsTag = backupTag.getIntArrayTag("hidden_components");
         if (hiddenComponentsTag != null) {
            data.set(
               StructuredDataKey.TOOLTIP_DISPLAY,
               new TooltipDisplay(data.has(StructuredDataKey.HIDE_TOOLTIP), new IntLinkedOpenHashSet(hiddenComponentsTag.getValue()))
            );
         }

         if (backupTag.contains("tool")) {
            boolean canDestroyBlocksInCreative = backupTag.getBoolean("tool");
            data.replace(
               StructuredDataKey.TOOL1_20_5,
               StructuredDataKey.TOOL1_21_5,
               t -> new ToolProperties(t.rules(), t.defaultMiningSpeed(), t.damagePerBlock(), canDestroyBlocksInCreative)
            );
         }

         if (backupTag.getBoolean("equippable")) {
            data.replace(
               StructuredDataKey.EQUIPPABLE1_21_2,
               StructuredDataKey.EQUIPPABLE1_21_5,
               e -> new Equippable(
                  e.equipmentSlot(), e.soundEvent(), e.model(), e.cameraOverlay(), e.allowedEntities(), e.dispensable(), e.swappable(), e.damageOnHurt(), true
               )
            );
         }

         CompoundTag weaponTag = backupTag.getCompoundTag("weapon");
         if (weaponTag != null) {
            data.set(StructuredDataKey.WEAPON, new Weapon(weaponTag.getInt("item_damage_per_attack"), weaponTag.getFloat("disable_blocking_for_seconds")));
         }

         Tag materialTag = backupTag.get("provides_trim_material");
         if (materialTag != null) {
            data.set(
               StructuredDataKey.PROVIDES_TRIM_MATERIAL1_21_5,
               new ProvidesTrimMaterial1_20_5(this.restoreEitherHolder(backupTag, "provides_trim_material", tag -> {
                  String assetName = tag.getString("asset_name");
                  int itemId = tag.getInt("item_id");
                  float itemModelIndex = tag.getFloat("item_model_index");
                  CompoundTag overrideArmorMaterialsTag = tag.getCompoundTag("override_armor_materials");
                  Map<String, String> overrideArmorMaterials = new HashMap<>();

                  for (String key : overrideArmorMaterialsTag.keySet()) {
                     overrideArmorMaterials.put(key, overrideArmorMaterialsTag.getString(key));
                  }

                  Tag description = tag.get("description");
                  return new ArmorTrimMaterial1_20_5(assetName, itemId, itemModelIndex, overrideArmorMaterials, description);
               }))
            );
         }

         CompoundTag blocksAttackTag = backupTag.getCompoundTag("blocks_attack");
         if (blocksAttackTag != null) {
            float blockDelaySeconds = blocksAttackTag.getFloat("block_delay_seconds");
            float disableCooldownScale = blocksAttackTag.getFloat("disable_cooldown_scale");
            CompoundTag itemDamageTag = blocksAttackTag.getCompoundTag("item_damage");
            ItemDamageFunction itemDamage = new ItemDamageFunction(
               itemDamageTag.getFloat("threshold"), itemDamageTag.getFloat("base"), itemDamageTag.getFloat("factor")
            );
            String bypassedBy = itemDamageTag.getString("bypassed_by");
            Holder<SoundEvent> blockSound = blocksAttackTag.contains("block_sound")
               ? this.restoreHolder(blocksAttackTag, "block_sound", this::tagToSound)
               : null;
            Holder<SoundEvent> disableSound = blocksAttackTag.contains("disable_sound")
               ? this.restoreHolder(blocksAttackTag, "disable_sound", this::tagToSound)
               : null;
            List<DamageReduction> damageReductions = new ArrayList<>();

            for (CompoundTag damageReductionTag : blocksAttackTag.getListTag("damage_reductions", CompoundTag.class)) {
               float horizontalBlockingAngle = damageReductionTag.getFloat("horizontal_blocking_angle");
               HolderSet type = damageReductionTag.contains("type") ? this.restoreHolderSet(damageReductionTag, "type") : null;
               float base = damageReductionTag.getFloat("base");
               float factor = damageReductionTag.getFloat("factor");
               damageReductions.add(new DamageReduction(horizontalBlockingAngle, type, base, factor));
            }

            data.set(
               StructuredDataKey.BLOCKS_ATTACKS1_21_5,
               new BlocksAttacks(
                  blockDelaySeconds,
                  disableCooldownScale,
                  damageReductions.toArray(new DamageReduction[0]),
                  itemDamage,
                  bypassedBy != null ? HolderSet.of(bypassedBy) : null,
                  blockSound,
                  disableSound
               )
            );
         }

         IntTag chickenVariant = backupTag.getIntTag("chicken_variant");
         if (chickenVariant != null) {
            data.set(StructuredDataKey.CHICKEN_VARIANT1_21_5, Either.left(chickenVariant.asInt()));
         } else {
            String chickenVariantKey = backupTag.getString("chicken_variant");
            if (chickenVariantKey != null) {
               data.set(StructuredDataKey.CHICKEN_VARIANT1_21_5, Either.right(chickenVariantKey));
            }
         }

         IntTag tropicalFishPattern = backupTag.getIntTag("tropical_fish_pattern");
         if (tropicalFishPattern != null) {
            data.set(StructuredDataKey.TROPICAL_FISH_PATTERN, new TropicalFishPattern(tropicalFishPattern.asInt()));
         }

         this.restoreKeyData(StructuredDataKey.PROVIDES_BANNER_PATTERNS1_21_5, data, backupTag);
         this.restoreFloatData(StructuredDataKey.POTION_DURATION_SCALE, data, backupTag);
         this.restoreIntData(StructuredDataKey.VILLAGER_VARIANT, data, backupTag);
         this.restoreIntData(StructuredDataKey.FOX_VARIANT, data, backupTag);
         this.restoreIntData(StructuredDataKey.SALMON_SIZE, data, backupTag);
         this.restoreIntData(StructuredDataKey.PARROT_VARIANT, data, backupTag);
         this.restoreIntData(StructuredDataKey.TROPICAL_FISH_BASE_COLOR, data, backupTag);
         this.restoreIntData(StructuredDataKey.TROPICAL_FISH_PATTERN_COLOR, data, backupTag);
         this.restoreIntData(StructuredDataKey.MOOSHROOM_VARIANT, data, backupTag);
         this.restoreIntData(StructuredDataKey.RABBIT_VARIANT, data, backupTag);
         this.restoreIntData(StructuredDataKey.FROG_VARIANT, data, backupTag);
         this.restoreIntData(StructuredDataKey.HORSE_VARIANT, data, backupTag);
         this.restoreIntData(StructuredDataKey.LLAMA_VARIANT, data, backupTag);
         this.restoreIntData(StructuredDataKey.AXOLOTL_VARIANT, data, backupTag);
         this.restoreIntData(StructuredDataKey.CAT_VARIANT, data, backupTag);
         this.restoreIntData(StructuredDataKey.CAT_COLLAR, data, backupTag);
         this.restoreIntData(StructuredDataKey.SHEEP_COLOR, data, backupTag);
         this.restoreIntData(StructuredDataKey.SHULKER_COLOR, data, backupTag);
         this.restoreIntData(StructuredDataKey.WOLF_SOUND_VARIANT, data, backupTag);
         this.restoreIntData(StructuredDataKey.COW_VARIANT, data, backupTag);
         this.restoreIntData(StructuredDataKey.PIG_VARIANT, data, backupTag);
         this.restoreIntData(StructuredDataKey.WOLF_VARIANT, data, backupTag);
         this.restoreIntData(StructuredDataKey.WOLF_COLLAR, data, backupTag);
         this.restoreHolderData(StructuredDataKey.BREAK_SOUND, data, backupTag, this::tagToSound);
         this.restoreHolderData(StructuredDataKey.PAINTING_VARIANT, data, backupTag, tag -> {
            int width = tag.getInt("width");
            int height = tag.getInt("height");
            String assetId = tag.getString("asset_id");
            Tag title = tag.get("title");
            Tag author = tag.get("author");
            return new PaintingVariant(width, height, assetId, title, author);
         });
         this.removeCustomTag(data, customData);
      }
   }

   private SoundEvent tagToSound(CompoundTag tag) {
      String identifier = tag.getString("identifier");
      FloatTag fixedRangeTag = tag.getFloatTag("fixed_range");
      return new SoundEvent(identifier, fixedRangeTag != null ? fixedRangeTag.asFloat() : null);
   }
}
