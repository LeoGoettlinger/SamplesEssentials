package com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.LongArrayTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.EitherHolder;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk1_21_5;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.Heightmap;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.data.predicate.DataComponentMatchers;
import com.viaversion.viaversion.api.minecraft.data.predicate.DataComponentPredicate;
import com.viaversion.viaversion.api.minecraft.item.HashedItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.minecraft.item.data.AdventureModePredicate;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_21;
import com.viaversion.viaversion.api.minecraft.item.data.BlockPredicate;
import com.viaversion.viaversion.api.minecraft.item.data.BlocksAttacks;
import com.viaversion.viaversion.api.minecraft.item.data.DyedColor;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.JukeboxPlayable;
import com.viaversion.viaversion.api.minecraft.item.data.TooltipDisplay;
import com.viaversion.viaversion.api.minecraft.item.data.TropicalFishPattern;
import com.viaversion.viaversion.api.minecraft.item.data.Unbreakable;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrim1_20_5;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.rewriter.ComponentRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkBiomesType1_19_4;
import com.viaversion.viaversion.api.type.types.chunk.ChunkBiomesType1_21_5;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_21_5;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.IntLinkedOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSortedSet;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.storage.ItemHashStorage1_21_5;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Limit;
import com.viaversion.viaversion.util.MathUtil;
import com.viaversion.viaversion.util.Unit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter1_21_5 extends StructuredItemRewriter<ClientboundPacket1_21_2, ServerboundPacket1_21_5, Protocol1_21_4To1_21_5> {
   public static final List<StructuredDataKey<?>> HIDE_ADDITIONAL_KEYS = List.of(
      StructuredDataKey.BANNER_PATTERNS,
      StructuredDataKey.BEES1_20_5,
      StructuredDataKey.BLOCK_ENTITY_DATA1_20_5,
      StructuredDataKey.BLOCK_STATE,
      StructuredDataKey.V1_21_5.bundleContents,
      StructuredDataKey.V1_21_5.chargedProjectiles,
      StructuredDataKey.V1_21_5.container,
      StructuredDataKey.CONTAINER_LOOT,
      StructuredDataKey.FIREWORK_EXPLOSION,
      StructuredDataKey.FIREWORKS,
      StructuredDataKey.INSTRUMENT1_21_5,
      StructuredDataKey.MAP_ID,
      StructuredDataKey.PAINTING_VARIANT,
      StructuredDataKey.POT_DECORATIONS1_20_5,
      StructuredDataKey.POTION_CONTENTS1_21_2,
      StructuredDataKey.TROPICAL_FISH_PATTERN,
      StructuredDataKey.WRITTEN_BOOK_CONTENT
   );
   public static final List<StructuredDataKey<?>> NEW_DATA_TO_REMOVE = List.of(
      StructuredDataKey.TOOLTIP_DISPLAY,
      StructuredDataKey.POTION_DURATION_SCALE,
      StructuredDataKey.WEAPON,
      StructuredDataKey.VILLAGER_VARIANT,
      StructuredDataKey.WOLF_VARIANT,
      StructuredDataKey.WOLF_COLLAR,
      StructuredDataKey.FOX_VARIANT,
      StructuredDataKey.SALMON_SIZE,
      StructuredDataKey.PARROT_VARIANT,
      StructuredDataKey.TROPICAL_FISH_PATTERN,
      StructuredDataKey.TROPICAL_FISH_BASE_COLOR,
      StructuredDataKey.TROPICAL_FISH_PATTERN_COLOR,
      StructuredDataKey.MOOSHROOM_VARIANT,
      StructuredDataKey.RABBIT_VARIANT,
      StructuredDataKey.COW_VARIANT,
      StructuredDataKey.PIG_VARIANT,
      StructuredDataKey.CHICKEN_VARIANT1_21_5,
      StructuredDataKey.FROG_VARIANT,
      StructuredDataKey.HORSE_VARIANT,
      StructuredDataKey.PAINTING_VARIANT,
      StructuredDataKey.LLAMA_VARIANT,
      StructuredDataKey.AXOLOTL_VARIANT,
      StructuredDataKey.CAT_VARIANT,
      StructuredDataKey.CAT_COLLAR,
      StructuredDataKey.SHEEP_COLOR,
      StructuredDataKey.SHULKER_COLOR,
      StructuredDataKey.BLOCKS_ATTACKS1_21_5,
      StructuredDataKey.PROVIDES_TRIM_MATERIAL1_21_5,
      StructuredDataKey.BREAK_SOUND,
      StructuredDataKey.WOLF_SOUND_VARIANT,
      StructuredDataKey.PROVIDES_BANNER_PATTERNS1_21_5
   );
   private static final DataComponentMatchers EMPTY_DATA_MATCHERS = new DataComponentMatchers(new StructuredData[0], new DataComponentPredicate[0]);
   private static final Heightmap[] EMPTY_HEIGHTMAPS = new Heightmap[0];

   public BlockItemPacketRewriter1_21_5(Protocol1_21_4To1_21_5 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      this.protocol
         .replaceClientbound(
            ClientboundPackets1_21_2.LEVEL_CHUNK_WITH_LIGHT,
            wrapper -> {
               EntityTracker tracker = this.protocol.getEntityRewriter().tracker(wrapper.user());
               Mappings blockStateMappings = this.protocol.getMappingData().getBlockStateMappings();
               Type<Chunk> chunkType = new ChunkType1_20_2(
                  tracker.currentWorldSectionHeight(), MathUtil.ceilLog2(blockStateMappings.size()), MathUtil.ceilLog2(tracker.biomesSent())
               );
               Chunk chunk = wrapper.read(chunkType);
               this.protocol.getBlockRewriter().handleChunk(chunk);
               Type<Chunk> newChunkType = new ChunkType1_21_5(
                  tracker.currentWorldSectionHeight(), MathUtil.ceilLog2(blockStateMappings.mappedSize()), MathUtil.ceilLog2(tracker.biomesSent())
               );
               List<Heightmap> heightmaps = new ArrayList<>();

               for (Entry<String, Tag> entry : chunk.getHeightMap().entrySet()) {
                  int type = this.heightmapType(entry.getKey());
                  if (type == -1) {
                     this.protocol.getLogger().warning("Unknown heightmap type: " + entry.getKey());
                  } else if (entry.getValue() instanceof LongArrayTag longArrayTag) {
                     heightmaps.add(new Heightmap(type, longArrayTag.getValue()));
                  }
               }

               Chunk mappedChunk = new Chunk1_21_5(chunk.getX(), chunk.getZ(), chunk.getSections(), heightmaps.toArray(EMPTY_HEIGHTMAPS), chunk.blockEntities());
               this.protocol.getBlockRewriter().handleBlockEntities(chunk, wrapper.user());
               wrapper.write(newChunkType, mappedChunk);
            }
         );
      this.protocol.registerClientbound(ClientboundPackets1_21_2.CHUNKS_BIOMES, wrapper -> {
         EntityTracker tracker = this.protocol.getEntityRewriter().tracker(wrapper.user());
         int globalPaletteBiomeBits = MathUtil.ceilLog2(tracker.biomesSent());
         Type<DataPalette[]> biomesType = new ChunkBiomesType1_19_4(tracker.currentWorldSectionHeight(), globalPaletteBiomeBits);
         Type<DataPalette[]> newBiomesType = new ChunkBiomesType1_21_5(tracker.currentWorldSectionHeight(), globalPaletteBiomeBits);
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.CHUNK_POSITION);
            wrapper.passthroughAndMap(biomesType, newBiomesType);
         }
      });
      this.protocol.replaceServerbound(ServerboundPackets1_21_5.SET_CREATIVE_MODE_SLOT, wrapper -> {
         if (!this.protocol.getEntityRewriter().<EntityTracker>tracker(wrapper.user()).canInstaBuild()) {
            wrapper.cancel();
         } else {
            wrapper.passthrough(Types.SHORT);
            Item item = this.handleItemToServer(wrapper.user(), wrapper.read(VersionedTypes.V1_21_5.lengthPrefixedItem()));
            wrapper.write(this.itemType(), item);
         }
      });
      this.protocol
         .replaceServerbound(
            ServerboundPackets1_21_5.CONTAINER_CLICK,
            wrapper -> {
               wrapper.passthrough(Types.VAR_INT);
               wrapper.passthrough(Types.VAR_INT);
               wrapper.passthrough(Types.SHORT);
               wrapper.passthrough(Types.BYTE);
               wrapper.passthrough(Types.VAR_INT);
               int affectedItems = Limit.max(wrapper.passthrough(Types.VAR_INT), 128);

               for (int i = 0; i < affectedItems; i++) {
                  wrapper.passthrough(Types.SHORT);
                  HashedItem item = wrapper.read(Types.HASHED_ITEM);
                  wrapper.write(
                     VersionedTypes.V1_21_5.item, this.handleItemToServer(wrapper.user(), this.convertHashedItemToStructuredItem(wrapper.user(), item))
                  );
               }

               HashedItem carriedItem = wrapper.read(Types.HASHED_ITEM);
               wrapper.write(
                  VersionedTypes.V1_21_5.item, this.handleItemToServer(wrapper.user(), this.convertHashedItemToStructuredItem(wrapper.user(), carriedItem))
               );
            }
         );
      this.protocol.replaceClientbound(ClientboundPackets1_21_2.UPDATE_ADVANCEMENTS, wrapper -> {
         wrapper.passthrough(Types.BOOLEAN);
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.OPTIONAL_STRING);
            if (wrapper.passthrough(Types.BOOLEAN)) {
               Tag title = wrapper.passthrough(Types.TRUSTED_TAG);
               Tag description = wrapper.passthrough(Types.TRUSTED_TAG);
               ComponentRewriter componentRewriter = this.protocol.getComponentRewriter();
               if (componentRewriter != null) {
                  componentRewriter.processTag(wrapper.user(), title);
                  componentRewriter.processTag(wrapper.user(), description);
               }

               this.passthroughClientboundItem(wrapper);
               wrapper.passthrough(Types.VAR_INT);
               int flags = wrapper.passthrough(Types.INT);
               if ((flags & 1) != 0) {
                  this.convertClientAsset(wrapper);
               }

               wrapper.passthrough(Types.FLOAT);
               wrapper.passthrough(Types.FLOAT);
            }

            int requirements = wrapper.passthrough(Types.VAR_INT);

            for (int array = 0; array < requirements; array++) {
               wrapper.passthrough(Types.STRING_ARRAY);
            }

            wrapper.passthrough(Types.BOOLEAN);
         }

         wrapper.passthrough(Types.STRING_ARRAY);
         int progressSize = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < progressSize; i++) {
            wrapper.passthrough(Types.STRING);
            int criterionSize = wrapper.passthrough(Types.VAR_INT);

            for (int j = 0; j < criterionSize; j++) {
               wrapper.passthrough(Types.STRING);
               wrapper.passthrough(Types.OPTIONAL_LONG);
            }
         }

         wrapper.write(Types.BOOLEAN, true);
      });
   }

   private void convertClientAsset(PacketWrapper wrapper) {
      String background = wrapper.read(Types.STRING);
      String namespace = Key.namespace(background);
      String path = Key.stripNamespace(background);
      if (path.startsWith("textures/") && path.endsWith(".png")) {
         String stripped = path.substring("textures/".length(), path.length() - ".png".length());
         wrapper.write(Types.STRING, namespace + ":" + stripped);
      } else {
         wrapper.write(Types.STRING, namespace + ":" + path);
      }
   }

   private int heightmapType(String id) {
      return switch (id) {
         case "WORLD_SURFACE_WG" -> 0;
         case "WORLD_SURFACE" -> 1;
         case "OCEAN_FLOOR_WG" -> 2;
         case "OCEAN_FLOOR" -> 3;
         case "MOTION_BLOCKING" -> 4;
         case "MOTION_BLOCKING_NO_LEAVES" -> 5;
         default -> -1;
      };
   }

   @Override
   public Item handleItemToClient(UserConnection connection, Item item) {
      if (item.isEmpty()) {
         return item;
      } else {
         MappingData mappingData = this.protocol.getMappingData();
         if (mappingData != null && mappingData.getItemMappings() != null) {
            item.setIdentifier(mappingData.getNewItemId(item.identifier()));
         }

         StructuredDataContainer dataContainer = item.dataContainer();
         this.updateItemDataComponentTypeIds(dataContainer, true);
         this.handleRewritablesToClient(connection, dataContainer, null);
         updateItemData(item);
         this.handleItemDataComponentsToClient(connection, item, dataContainer);
         this.appendItemDataFixComponents(connection, item);
         ItemHashStorage1_21_5 itemHasher = (ItemHashStorage1_21_5)this.itemHasher(connection);
         if (itemHasher.isProcessingClientboundInventoryPacket()) {
            for (StructuredData<?> data : dataContainer.data().values()) {
               itemHasher.trackStructuredData(data);
            }
         }

         return item;
      }
   }

   @Override
   protected void handleItemDataComponentsToServer(UserConnection connection, Item item, StructuredDataContainer container) {
      downgradeItemData(item);
      super.handleItemDataComponentsToServer(connection, item, container);
   }

   public static void updateItemData(Item item) {
      StructuredDataContainer dataContainer = item.dataContainer();
      dataContainer.replaceKey(StructuredDataKey.TOOL1_20_5, StructuredDataKey.TOOL1_21_5);
      dataContainer.replaceKey(StructuredDataKey.EQUIPPABLE1_21_2, StructuredDataKey.EQUIPPABLE1_21_5);
      dataContainer.replaceKey(StructuredDataKey.CONSUMABLE1_21_2, StructuredDataKey.CONSUMABLE1_21_5);
      dataContainer.replace(StructuredDataKey.INSTRUMENT1_21_2, StructuredDataKey.INSTRUMENT1_21_5, EitherHolder::of);
      IntSortedSet hiddenComponents = new IntLinkedOpenHashSet(4);
      boolean hideTooltip = dataContainer.hasValue(StructuredDataKey.HIDE_TOOLTIP);
      if (dataContainer.hasValue(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP)) {
         FullMappings mappings = Protocol1_21_4To1_21_5.MAPPINGS.getDataComponentSerializerMappings();

         for (StructuredDataKey<?> key : HIDE_ADDITIONAL_KEYS) {
            hiddenComponents.add(mappings.mappedId(key.identifier()));
         }
      }

      StructuredData<Unbreakable> unbreakableData = dataContainer.getNonEmptyData(StructuredDataKey.UNBREAKABLE1_20_5);
      if (unbreakableData != null && !unbreakableData.value().showInTooltip()) {
         hiddenComponents.add(unbreakableData.id());
      }

      StructuredData<AdventureModePredicate> canPlaceOnData = dataContainer.getNonEmptyData(StructuredDataKey.CAN_PLACE_ON1_20_5);
      if (canPlaceOnData != null && !canPlaceOnData.value().showInTooltip()) {
         hiddenComponents.add(canPlaceOnData.id());
      }

      StructuredData<AdventureModePredicate> canBreak = dataContainer.getNonEmptyData(StructuredDataKey.CAN_BREAK1_20_5);
      if (canBreak != null && !canBreak.value().showInTooltip()) {
         hiddenComponents.add(canBreak.id());
      }

      StructuredData<DyedColor> dyedColorData = dataContainer.getNonEmptyData(StructuredDataKey.DYED_COLOR1_20_5);
      if (dyedColorData != null && !dyedColorData.value().showInTooltip()) {
         hiddenComponents.add(dyedColorData.id());
      }

      StructuredData<AttributeModifiers1_21> attributeModifiersData = dataContainer.getNonEmptyData(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21);
      if (attributeModifiersData != null && !attributeModifiersData.value().showInTooltip()) {
         hiddenComponents.add(attributeModifiersData.id());
      }

      StructuredData<ArmorTrim1_20_5> armorTrimData = dataContainer.getNonEmptyData(StructuredDataKey.TRIM1_21_4);
      if (armorTrimData != null && !armorTrimData.value().showInTooltip()) {
         hiddenComponents.add(armorTrimData.id());
      }

      StructuredData<Enchantments> enchantmentsData = dataContainer.getNonEmptyData(StructuredDataKey.ENCHANTMENTS1_20_5);
      if (enchantmentsData != null && !enchantmentsData.value().showInTooltip()) {
         hiddenComponents.add(enchantmentsData.id());
      }

      StructuredData<Enchantments> storedEnchantmentsData = dataContainer.getNonEmptyData(StructuredDataKey.STORED_ENCHANTMENTS1_20_5);
      if (storedEnchantmentsData != null && !storedEnchantmentsData.value().showInTooltip()) {
         hiddenComponents.add(storedEnchantmentsData.id());
      }

      StructuredData<JukeboxPlayable> jukeboxPlayable = dataContainer.getNonEmptyData(StructuredDataKey.JUKEBOX_PLAYABLE1_21);
      if (jukeboxPlayable != null && !jukeboxPlayable.value().showInTooltip()) {
         hiddenComponents.add(jukeboxPlayable.id());
      }

      if ((hideTooltip || !hiddenComponents.isEmpty()) && !dataContainer.has(StructuredDataKey.TOOLTIP_DISPLAY)) {
         dataContainer.set(StructuredDataKey.TOOLTIP_DISPLAY, new TooltipDisplay(hideTooltip, hiddenComponents));
      }

      updateBucketVariant(dataContainer);
      dataContainer.replace(StructuredDataKey.UNBREAKABLE1_20_5, StructuredDataKey.UNBREAKABLE1_21_5, unbreakable -> Unit.INSTANCE);
      dataContainer.replace(
         StructuredDataKey.CAN_PLACE_ON1_20_5, StructuredDataKey.V1_21_5.canPlaceOn, BlockItemPacketRewriter1_21_5::updateAdventureModePredicate
      );
      dataContainer.replace(StructuredDataKey.CAN_BREAK1_20_5, StructuredDataKey.V1_21_5.canBreak, BlockItemPacketRewriter1_21_5::updateAdventureModePredicate);
      dataContainer.replaceKey(StructuredDataKey.JUKEBOX_PLAYABLE1_21, StructuredDataKey.JUKEBOX_PLAYABLE1_21_5);
      dataContainer.replaceKey(StructuredDataKey.DYED_COLOR1_20_5, StructuredDataKey.DYED_COLOR1_21_5);
      dataContainer.replaceKey(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21, StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_5);
      dataContainer.replaceKey(StructuredDataKey.TRIM1_21_4, StructuredDataKey.TRIM1_21_5);
      dataContainer.replaceKey(StructuredDataKey.ENCHANTMENTS1_20_5, StructuredDataKey.ENCHANTMENTS1_21_5);
      dataContainer.replaceKey(StructuredDataKey.STORED_ENCHANTMENTS1_20_5, StructuredDataKey.STORED_ENCHANTMENTS1_21_5);
      dataContainer.remove(StructuredDataKey.HIDE_TOOLTIP);
      dataContainer.remove(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP);
   }

   private static AdventureModePredicate updateAdventureModePredicate(AdventureModePredicate predicate) {
      BlockPredicate[] blockPredicates = new BlockPredicate[predicate.predicates().length];

      for (int i = 0; i < predicate.predicates().length; i++) {
         BlockPredicate blockPredicate = predicate.predicates()[i];
         blockPredicates[i] = new BlockPredicate(blockPredicate.holderSet(), blockPredicate.propertyMatchers(), blockPredicate.tag(), EMPTY_DATA_MATCHERS);
      }

      return new AdventureModePredicate(blockPredicates);
   }

   private static void updateBucketVariant(StructuredDataContainer dataContainer) {
      CompoundTag bucketEntityData = dataContainer.get(StructuredDataKey.BUCKET_ENTITY_DATA);
      if (bucketEntityData != null) {
         IntTag bucketVariantTag = bucketEntityData.removeUnchecked("BucketVariantTag");
         if (bucketVariantTag != null) {
            int packedVariant = bucketVariantTag.asInt();
            dataContainer.set(StructuredDataKey.TROPICAL_FISH_BASE_COLOR, packedVariant >> 16 & 0xFF);
            dataContainer.set(StructuredDataKey.TROPICAL_FISH_PATTERN_COLOR, packedVariant >> 24 & 0xFF);
            dataContainer.set(StructuredDataKey.TROPICAL_FISH_PATTERN, new TropicalFishPattern(packedVariant & 65535));
         }
      }
   }

   public static void downgradeItemData(Item item) {
      StructuredDataContainer dataContainer = item.dataContainer();
      dataContainer.replaceKey(StructuredDataKey.TOOL1_21_5, StructuredDataKey.TOOL1_20_5);
      dataContainer.replaceKey(StructuredDataKey.EQUIPPABLE1_21_5, StructuredDataKey.EQUIPPABLE1_21_2);
      dataContainer.replaceKey(StructuredDataKey.CONSUMABLE1_21_5, StructuredDataKey.CONSUMABLE1_21_2);
      dataContainer.replace(
         StructuredDataKey.INSTRUMENT1_21_5, StructuredDataKey.INSTRUMENT1_21_2, instrument -> instrument.hasHolder() ? instrument.holder() : null
      );
      TooltipDisplay tooltipDisplay = dataContainer.get(StructuredDataKey.TOOLTIP_DISPLAY);
      if (tooltipDisplay != null) {
         if (tooltipDisplay.hideTooltip()) {
            dataContainer.set(StructuredDataKey.HIDE_TOOLTIP);
         }

         FullMappings mappings = Protocol1_21_4To1_21_5.MAPPINGS.getDataComponentSerializerMappings();
         if (tooltipDisplay.hiddenComponents()
            .containsAll(HIDE_ADDITIONAL_KEYS.stream().map(key -> mappings.id(key.identifier())).filter(id -> id != -1).toList())) {
            dataContainer.set(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP);
         }
      }

      downgradeBucketVariant(dataContainer);
      dataContainer.replace(
         StructuredDataKey.UNBREAKABLE1_21_5,
         StructuredDataKey.UNBREAKABLE1_20_5,
         unbreakable -> new Unbreakable(shouldShowToServer(tooltipDisplay, StructuredDataKey.UNBREAKABLE1_20_5))
      );
      updateShowInTooltip(
         dataContainer,
         tooltipDisplay,
         StructuredDataKey.DYED_COLOR1_21_5,
         StructuredDataKey.DYED_COLOR1_20_5,
         dyedColor -> new DyedColor(dyedColor.rgb(), false)
      );
      updateShowInTooltip(
         dataContainer,
         tooltipDisplay,
         StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_5,
         StructuredDataKey.ATTRIBUTE_MODIFIERS1_21,
         attributeModifiers -> new AttributeModifiers1_21(attributeModifiers.modifiers(), false)
      );
      updateShowInTooltip(
         dataContainer,
         tooltipDisplay,
         StructuredDataKey.TRIM1_21_5,
         StructuredDataKey.TRIM1_21_4,
         trim -> new ArmorTrim1_20_5(trim.material(), trim.pattern(), false)
      );
      updateShowInTooltip(
         dataContainer,
         tooltipDisplay,
         StructuredDataKey.ENCHANTMENTS1_21_5,
         StructuredDataKey.ENCHANTMENTS1_20_5,
         enchantments -> new Enchantments(enchantments.enchantments(), false)
      );
      updateShowInTooltip(
         dataContainer,
         tooltipDisplay,
         StructuredDataKey.STORED_ENCHANTMENTS1_21_5,
         StructuredDataKey.STORED_ENCHANTMENTS1_20_5,
         enchantments -> new Enchantments(enchantments.enchantments(), false)
      );
      updateShowInTooltip(
         dataContainer,
         tooltipDisplay,
         StructuredDataKey.V1_21_5.canPlaceOn,
         StructuredDataKey.CAN_PLACE_ON1_20_5,
         canPlaceOn -> new AdventureModePredicate(canPlaceOn.predicates(), false)
      );
      updateShowInTooltip(
         dataContainer,
         tooltipDisplay,
         StructuredDataKey.V1_21_5.canBreak,
         StructuredDataKey.CAN_BREAK1_20_5,
         canBreak -> new AdventureModePredicate(canBreak.predicates(), false)
      );
      updateShowInTooltip(
         dataContainer,
         tooltipDisplay,
         StructuredDataKey.JUKEBOX_PLAYABLE1_21_5,
         StructuredDataKey.JUKEBOX_PLAYABLE1_21,
         playable -> new JukeboxPlayable(playable.song(), false)
      );
      dataContainer.remove(NEW_DATA_TO_REMOVE);
   }

   private static void downgradeBucketVariant(StructuredDataContainer dataContainer) {
      TropicalFishPattern tropicalFishPattern = dataContainer.get(StructuredDataKey.TROPICAL_FISH_PATTERN);
      if (tropicalFishPattern != null) {
         Integer base = dataContainer.get(StructuredDataKey.TROPICAL_FISH_BASE_COLOR);
         Integer pattern = dataContainer.get(StructuredDataKey.TROPICAL_FISH_PATTERN_COLOR);
         int baseColor = base != null ? base : 16777215;
         int patternColor = pattern != null ? pattern : 16777215;
         int packedVariant = (patternColor & 0xFF) << 24 | (baseColor & 0xFF) << 16 | tropicalFishPattern.packedId() & 65535;
         CompoundTag bucketEntityData = dataContainer.get(StructuredDataKey.BUCKET_ENTITY_DATA);
         if (bucketEntityData == null) {
            dataContainer.set(StructuredDataKey.BUCKET_ENTITY_DATA, bucketEntityData = new CompoundTag());
         }

         bucketEntityData.put("BucketVariantTag", new IntTag(packedVariant));
      }
   }

   private StructuredItem convertHashedItemToStructuredItem(UserConnection connection, HashedItem hashedItem) {
      StructuredItem item = new StructuredItem(hashedItem.identifier(), hashedItem.amount());
      ItemHashStorage1_21_5 hasher = (ItemHashStorage1_21_5)this.itemHasher(connection);
      Map<StructuredDataKey<?>, StructuredData<?>> structuredDataMap = item.dataContainer().data();

      for (Int2IntMap.Entry hashEntry : hashedItem.dataHashesById().int2IntEntrySet()) {
         StructuredData<?> structuredData = hasher.dataFromHash(hashEntry.getIntKey(), hashEntry.getIntValue());
         if (structuredData != null) {
            structuredDataMap.put(structuredData.key(), structuredData);
         }
      }

      for (int dataId : hashedItem.removedDataIds()) {
         StructuredDataKey<?> structuredDataKey = VersionedTypes.V1_21_5.structuredData.key(dataId);
         structuredDataMap.put(structuredDataKey, StructuredData.empty(structuredDataKey, dataId));
      }

      return item;
   }

   private void appendItemDataFixComponents(UserConnection connection, Item item) {
      ProtocolVersion serverVersion = connection.getProtocolInfo().serverProtocolVersion();
      if (serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_8)
         && (item.identifier() == 858 || item.identifier() == 863 || item.identifier() == 873 || item.identifier() == 868 || item.identifier() == 878)) {
         item.dataContainer().remove(StructuredDataKey.CONSUMABLE1_21_5);
         item.dataContainer()
            .set(
               StructuredDataKey.BLOCKS_ATTACKS1_21_5,
               new BlocksAttacks(
                  0.0F,
                  0.0F,
                  new BlocksAttacks.DamageReduction[]{new BlocksAttacks.DamageReduction(90.0F, null, -0.5F, 0.5F)},
                  new BlocksAttacks.ItemDamageFunction(0.0F, 0.0F, 0.0F),
                  null,
                  null,
                  null
               )
            );
      }
   }

   private static <T> void updateShowInTooltip(
      StructuredDataContainer container, @Nullable TooltipDisplay display, StructuredDataKey<T> key, StructuredDataKey<T> mappedKey, Function<T, T> function
   ) {
      if (shouldShowToServer(display, key)) {
         container.replaceKey(key, mappedKey);
      } else {
         container.replace(key, mappedKey, function);
      }
   }

   private static boolean shouldShowToServer(@Nullable TooltipDisplay display, StructuredDataKey<?> key) {
      if (display == null) {
         return true;
      } else {
         int unmappedId = Protocol1_21_4To1_21_5.MAPPINGS.getDataComponentSerializerMappings().id(key.identifier());
         return !display.hiddenComponents().contains(unmappedId);
      }
   }
}
