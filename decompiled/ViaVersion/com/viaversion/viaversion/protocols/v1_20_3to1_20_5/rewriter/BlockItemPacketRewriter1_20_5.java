package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.GlobalBlockPosition;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.minecraft.item.data.AdventureModePredicate;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.BannerPattern;
import com.viaversion.viaversion.api.minecraft.item.data.BannerPatternLayer;
import com.viaversion.viaversion.api.minecraft.item.data.Bee;
import com.viaversion.viaversion.api.minecraft.item.data.BlockPredicate;
import com.viaversion.viaversion.api.minecraft.item.data.BlockStateProperties;
import com.viaversion.viaversion.api.minecraft.item.data.DebugStickState;
import com.viaversion.viaversion.api.minecraft.item.data.DyedColor;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableComponent;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableString;
import com.viaversion.viaversion.api.minecraft.item.data.FireworkExplosion;
import com.viaversion.viaversion.api.minecraft.item.data.Fireworks;
import com.viaversion.viaversion.api.minecraft.item.data.FoodProperties1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.Instrument1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.LodestoneTracker;
import com.viaversion.viaversion.api.minecraft.item.data.PotDecorations1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.PotionContents;
import com.viaversion.viaversion.api.minecraft.item.data.PotionEffect;
import com.viaversion.viaversion.api.minecraft.item.data.PotionEffectData;
import com.viaversion.viaversion.api.minecraft.item.data.StatePropertyMatcher;
import com.viaversion.viaversion.api.minecraft.item.data.SuspiciousStewEffect;
import com.viaversion.viaversion.api.minecraft.item.data.ToolProperties;
import com.viaversion.viaversion.api.minecraft.item.data.ToolRule;
import com.viaversion.viaversion.api.minecraft.item.data.Unbreakable;
import com.viaversion.viaversion.api.minecraft.item.data.WritableBook;
import com.viaversion.viaversion.api.minecraft.item.data.WrittenBook;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrim1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrimMaterial1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrimPattern;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2ObjectArrayMap;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.Protocol1_20_3To1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Attributes1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.BannerPatterns1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.DyeColors;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Enchantments1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.EquipmentSlots1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Instruments1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.MapDecorations1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.MaxStackSize1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.PotionEffects1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Potions1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.ArmorTrimStorage;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.BannerPatternStorage;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.ProtocolStorables1_20_5;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Either;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.MathUtil;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.UUIDUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter1_20_5 extends ItemRewriter<ClientboundPacket1_20_3, ServerboundPacket1_20_5, Protocol1_20_3To1_20_5> {
   public static final String[] MOB_TAGS = new String[]{
      "NoAI", "Silent", "NoGravity", "Glowing", "Invulnerable", "Health", "Age", "Variant", "HuntingCooldown", "BucketVariantTag"
   };
   public static final String[] ATTRIBUTE_OPERATIONS = new String[]{"add_value", "add_multiplied_base", "add_multiplied_total"};
   private static final StructuredDataConverter DATA_CONVERTER = new StructuredDataConverter(false);
   private static final GameProfile.Property[] EMPTY_PROPERTIES = new GameProfile.Property[0];
   private static final StatePropertyMatcher[] EMPTY_PROPERTY_MATCHERS = new StatePropertyMatcher[0];

   public BlockItemPacketRewriter1_20_5(Protocol1_20_3To1_20_5 protocol) {
      super(protocol, Types.ITEM1_20_2, Types.ITEM1_20_2_ARRAY, VersionedTypes.V1_20_5.item, VersionedTypes.V1_20_5.itemArray);
   }

   @Override
   public void registerPackets() {
      this.protocol.replaceClientbound(ClientboundPackets1_20_3.LEVEL_CHUNK_WITH_LIGHT, wrapper -> {
         Chunk chunk = this.protocol.getBlockRewriter().handleChunk1_18(wrapper);

         for (int i = 0; i < chunk.blockEntities().size(); i++) {
            BlockEntity blockEntity = chunk.blockEntities().get(i);
            if (this.isUnknownBlockEntity(blockEntity.typeId())) {
               chunk.blockEntities().remove(i--);
            } else {
               this.updateBlockEntityTag(wrapper.user(), null, blockEntity.tag());
            }
         }
      });
      this.protocol.replaceClientbound(ClientboundPackets1_20_3.BLOCK_ENTITY_DATA, wrapper -> {
         wrapper.passthrough(Types.BLOCK_POSITION1_14);
         int typeId = wrapper.passthrough(Types.VAR_INT);
         if (this.isUnknownBlockEntity(typeId)) {
            wrapper.cancel();
         } else {
            CompoundTag tag = wrapper.read(Types.TRUSTED_COMPOUND_TAG);
            if (tag != null) {
               this.updateBlockEntityTag(wrapper.user(), null, tag);
            } else {
               tag = new CompoundTag();
            }

            wrapper.write(Types.TRUSTED_COMPOUND_TAG, tag);
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_20_5.CONTAINER_BUTTON_CLICK, wrapper -> {
         byte containerId = wrapper.read(Types.VAR_INT).byteValue();
         byte buttonId = wrapper.read(Types.VAR_INT).byteValue();
         wrapper.write(Types.BYTE, containerId);
         wrapper.write(Types.BYTE, buttonId);
      });
      this.protocol.replaceClientbound(ClientboundPackets1_20_3.UPDATE_ADVANCEMENTS, wrapper -> {
         wrapper.passthrough(Types.BOOLEAN);
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.OPTIONAL_STRING);
            if (wrapper.passthrough(Types.BOOLEAN)) {
               wrapper.passthrough(Types.TRUSTED_TAG);
               wrapper.passthrough(Types.TRUSTED_TAG);
               Item item = this.handleNonEmptyItemToClient(wrapper.user(), wrapper.read(this.itemType()));
               wrapper.write(this.mappedItemType(), item);
               wrapper.passthrough(Types.VAR_INT);
               int flags = wrapper.passthrough(Types.INT);
               if ((flags & 1) != 0) {
                  wrapper.passthrough(Types.STRING);
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
      });
      this.protocol.replaceClientbound(ClientboundPackets1_20_3.LEVEL_PARTICLES, wrapper -> {
         int particleId = wrapper.read(Types.VAR_INT);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         float offX = wrapper.passthrough(Types.FLOAT);
         float offY = wrapper.passthrough(Types.FLOAT);
         float offZ = wrapper.passthrough(Types.FLOAT);
         float data = wrapper.passthrough(Types.FLOAT);
         int count = wrapper.passthrough(Types.INT);
         ParticleMappings mappings = this.protocol.getMappingData().getParticleMappings();
         int mappedId = mappings.getNewId(particleId);
         Particle particle = new Particle(mappedId);
         if (mappedId == mappings.mappedId("entity_effect")) {
            int color;
            if (data == 0.0F) {
               color = 0;
            } else if (count != 0) {
               color = ThreadLocalRandom.current().nextInt();
            } else {
               int red = Math.round(offX * 255.0F);
               int green = Math.round(offY * 255.0F);
               int blue = Math.round(offZ * 255.0F);
               color = red << 16 | green << 8 | blue;
            }

            particle.add(Types.INT, EntityPacketRewriter1_20_5.withAlpha(color));
         } else if (particleId == mappings.id("dust_color_transition")) {
            for (int i = 0; i < 7; i++) {
               particle.add(Types.FLOAT, wrapper.read(Types.FLOAT));
            }

            particle.add(Types.FLOAT, particle.<Float>removeArgument(3).getValue());
         } else if (mappings.isBlockParticle(particleId)) {
            int blockStateId = wrapper.read(Types.VAR_INT);
            particle.add(Types.VAR_INT, this.protocol.getMappingData().getNewBlockStateId(blockStateId));
         } else if (mappings.isItemParticle(particleId)) {
            Item item = this.handleNonEmptyItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_20_2));
            particle.add(VersionedTypes.V1_20_5.item, item);
         } else if (particleId == mappings.id("dust")) {
            for (int i = 0; i < 4; i++) {
               particle.add(Types.FLOAT, wrapper.read(Types.FLOAT));
            }
         } else if (particleId == mappings.id("vibration")) {
            int sourceTypeId = wrapper.read(Types.VAR_INT);
            particle.add(Types.VAR_INT, sourceTypeId);
            if (sourceTypeId == 0) {
               particle.add(Types.BLOCK_POSITION1_14, wrapper.read(Types.BLOCK_POSITION1_14));
            } else if (sourceTypeId == 1) {
               particle.add(Types.VAR_INT, wrapper.read(Types.VAR_INT));
               particle.add(Types.FLOAT, wrapper.read(Types.FLOAT));
            } else {
               this.protocol.getLogger().warning("Unknown vibration path position source type: " + sourceTypeId);
            }

            particle.add(Types.VAR_INT, wrapper.read(Types.VAR_INT));
         } else if (particleId == mappings.id("sculk_charge")) {
            particle.add(Types.FLOAT, wrapper.read(Types.FLOAT));
         } else if (particleId == mappings.id("shriek")) {
            particle.add(Types.VAR_INT, wrapper.read(Types.VAR_INT));
         }

         wrapper.write(VersionedTypes.V1_20_5.particle, particle);
      });
      this.protocol.registerClientbound(ClientboundPackets1_20_3.EXPLODE, wrapper -> {
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.FLOAT);
         int blocks = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < blocks; i++) {
            wrapper.passthrough(Types.BYTE);
            wrapper.passthrough(Types.BYTE);
            wrapper.passthrough(Types.BYTE);
         }

         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.VAR_INT);
         Particle smallExplosionParticle = wrapper.passthroughAndMap(Types1_20_3.PARTICLE, VersionedTypes.V1_20_5.particle);
         Particle largeExplosionParticle = wrapper.passthroughAndMap(Types1_20_3.PARTICLE, VersionedTypes.V1_20_5.particle);
         this.protocol.getParticleRewriter().rewriteParticle(wrapper.user(), smallExplosionParticle);
         this.protocol.getParticleRewriter().rewriteParticle(wrapper.user(), largeExplosionParticle);
         String sound = wrapper.read(Types.STRING);
         Float range = wrapper.read(Types.OPTIONAL_FLOAT);
         wrapper.write(Types.SOUND_EVENT, Holder.of(new SoundEvent(sound, range)));
      });
      this.protocol.replaceClientbound(ClientboundPackets1_20_3.MERCHANT_OFFERS, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            Item input = this.handleNonEmptyItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_20_2));
            wrapper.write(VersionedTypes.V1_20_5.itemCost, input);
            Item output = this.handleNonEmptyItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_20_2));
            wrapper.write(VersionedTypes.V1_20_5.item, output);
            Item secondInput = wrapper.read(Types.ITEM1_20_2);
            if (secondInput != null) {
               secondInput = this.handleItemToClient(wrapper.user(), secondInput);
               if (secondInput.isEmpty()) {
                  secondInput = null;
               }
            }

            wrapper.write(VersionedTypes.V1_20_5.optionalItemCost, secondInput);
            wrapper.passthrough(Types.BOOLEAN);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.FLOAT);
            wrapper.passthrough(Types.INT);
         }
      });
      RecipeRewriter1_20_5<ClientboundPacket1_20_3> recipeRewriter = new RecipeRewriter1_20_5<>(this.protocol);
      this.protocol.registerClientbound(ClientboundPackets1_20_3.UPDATE_RECIPES, wrapper -> {
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            String type = wrapper.read(Types.STRING);
            wrapper.passthrough(Types.STRING);
            wrapper.write(Types.VAR_INT, this.protocol.getMappingData().getRecipeSerializerMappings().mappedId(type));
            recipeRewriter.handleRecipeType(wrapper, type);
         }
      });
      this.protocol.replaceServerbound(ServerboundPackets1_20_5.SET_CREATIVE_MODE_SLOT, wrapper -> {
         if (!this.protocol.getEntityRewriter().<EntityTracker>tracker(wrapper.user()).canInstaBuild()) {
            wrapper.cancel();
         } else {
            wrapper.passthrough(Types.SHORT);
            wrapper.write(this.itemType(), this.handleItemToServer(wrapper.user(), wrapper.read(this.mappedItemType())));
         }
      });
   }

   public Item handleNonEmptyItemToClient(UserConnection connection, @Nullable Item item) {
      item = this.handleItemToClient(connection, item);
      return (Item)(item.isEmpty() ? new StructuredItem(1, 1) : item);
   }

   @Override
   public Item handleItemToClient(UserConnection connection, @Nullable Item item) {
      if (item == null) {
         return StructuredItem.empty();
      } else {
         CompoundTag tag = item.tag();
         Item structuredItem = this.toStructuredItem(connection, item);
         if (tag != null) {
            tag.putBoolean(this.nbtTagName(), true);
            structuredItem.dataContainer().set(StructuredDataKey.CUSTOM_DATA, tag);
         }

         this.appendItemDataFixComponents(connection, structuredItem);
         if (Via.getConfig().handleInvalidItemCount() && structuredItem.amount() > MaxStackSize1_20_3.getMaxStackSize(structuredItem.identifier())) {
            structuredItem.dataContainer().set(StructuredDataKey.MAX_STACK_SIZE, structuredItem.amount());
         }

         return super.handleItemToClient(connection, structuredItem);
      }
   }

   @Nullable
   @Override
   public Item handleItemToServer(UserConnection connection, Item item) {
      if (item.isEmpty()) {
         return null;
      } else {
         super.handleItemToServer(connection, item);
         return this.toOldItem(connection, item, DATA_CONVERTER);
      }
   }

   public Item toOldItem(UserConnection connection, Item item, StructuredDataConverter dataConverter) {
      StructuredDataContainer data = item.dataContainer();
      data.setIdLookup(this.protocol, true);
      StructuredData<CompoundTag> customData = data.getNonEmptyData(StructuredDataKey.CUSTOM_DATA);
      CompoundTag tag = customData != null ? customData.value() : new CompoundTag();
      DataItem dataItem = new DataItem(item.identifier(), (byte)item.amount(), tag);
      if (!dataConverter.backupInconvertibleData() && customData != null && tag.remove(this.nbtTagName()) != null) {
         return dataItem;
      } else {
         for (StructuredData<?> structuredData : data.data().values()) {
            dataConverter.writeToTag(connection, structuredData, tag);
         }

         if (tag.isEmpty()) {
            dataItem.setTag(null);
         }

         return dataItem;
      }
   }

   public Item toStructuredItem(UserConnection connection, Item old) {
      CompoundTag tag = old.tag();
      StructuredItem item = new StructuredItem(old.identifier(), (byte)old.amount(), new StructuredDataContainer());
      StructuredDataContainer data = item.dataContainer();
      data.setIdLookup(this.protocol, true);
      if (tag == null) {
         return item;
      } else {
         int hideFlagsValue = tag.getInt("HideFlags");
         if ((hideFlagsValue & 32) != 0) {
            data.set(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP);
         }

         this.updateDisplay(connection, data, tag.getCompoundTag("display"), hideFlagsValue);
         NumberTag damage = tag.getNumberTag("Damage");
         if (damage != null && damage.asInt() > 0) {
            data.set(StructuredDataKey.DAMAGE, damage.asInt());
         }

         NumberTag repairCost = tag.getNumberTag("RepairCost");
         if (repairCost != null && repairCost.asInt() > 0) {
            data.set(StructuredDataKey.REPAIR_COST, repairCost.asInt());
         }

         NumberTag customModelData = tag.getNumberTag("CustomModelData");
         if (customModelData != null) {
            data.set(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5, customModelData.asInt());
         }

         CompoundTag blockState = tag.getCompoundTag("BlockStateTag");
         if (blockState != null) {
            this.updateBlockState(data, blockState);
         }

         CompoundTag entityTag = tag.getCompoundTag("EntityTag");
         if (entityTag != null) {
            entityTag = entityTag.copy();
            if (entityTag.contains("variant")) {
               entityTag.putString("id", "minecraft:painting");
            }

            if (entityTag.contains("id")) {
               data.set(StructuredDataKey.ENTITY_DATA1_20_5, entityTag);
            }
         }

         CompoundTag blockEntityTag = tag.getCompoundTag("BlockEntityTag");
         if (blockEntityTag != null) {
            CompoundTag clonedTag = blockEntityTag.copy();
            this.updateBlockEntityTag(connection, data, clonedTag);
            CompoundTag spawnData = clonedTag.getCompoundTag("SpawnData");
            if (spawnData == null) {
               spawnData = clonedTag.getCompoundTag("spawn_data");
            }

            if (spawnData != null) {
               CompoundTag entity = spawnData.getCompoundTag("entity");
               if (entity != null && entity.getString("id") != null) {
                  this.addBlockEntityId(clonedTag, clonedTag.contains("SpawnData") ? "mob_spawner" : "trial_spawner");
               }
            }

            if (clonedTag.contains("id")) {
               item.dataContainer().set(StructuredDataKey.BLOCK_ENTITY_DATA1_20_5, clonedTag);
            }
         }

         CompoundTag debugProperty = tag.getCompoundTag("DebugProperty");
         if (debugProperty != null) {
            data.set(StructuredDataKey.DEBUG_STICK_STATE, new DebugStickState(debugProperty.copy()));
         }

         NumberTag unbreakable = tag.getNumberTag("Unbreakable");
         if (unbreakable != null && unbreakable.asBoolean()) {
            data.set(StructuredDataKey.UNBREAKABLE1_20_5, new Unbreakable((hideFlagsValue & 4) == 0));
         }

         CompoundTag trimTag = tag.getCompoundTag("Trim");
         if (trimTag != null) {
            this.updateArmorTrim(connection, data, trimTag, (hideFlagsValue & 128) == 0);
         }

         CompoundTag explosionTag = tag.getCompoundTag("Explosion");
         if (explosionTag != null) {
            data.set(StructuredDataKey.FIREWORK_EXPLOSION, this.readExplosion(explosionTag));
         }

         ListTag<StringTag> recipesTag = tag.getListTag("Recipes", StringTag.class);
         if (recipesTag != null) {
            data.set(StructuredDataKey.RECIPES, recipesTag);
         }

         NumberTag trackedTag = tag.getNumberTag("LodestoneTracked");
         if (trackedTag != null) {
            CompoundTag lodestonePosTag = tag.getCompoundTag("LodestonePos");
            String lodestoneDimension = tag.getString("LodestoneDimension");
            this.updateLodestoneTracker(trackedTag.asBoolean(), lodestonePosTag, lodestoneDimension, data);
         }

         ListTag<CompoundTag> effectsTag = tag.getListTag("effects", CompoundTag.class);
         if (effectsTag != null) {
            this.updateEffects(effectsTag, data);
         }

         String instrument = tag.getString("instrument");
         if (instrument != null) {
            int id = Instruments1_20_3.keyToId(instrument);
            if (id != -1) {
               data.set(StructuredDataKey.INSTRUMENT1_20_5, Holder.of(id));
            }
         }

         ListTag<CompoundTag> attributeModifiersTag = tag.getListTag("AttributeModifiers", CompoundTag.class);
         boolean showAttributes = (hideFlagsValue & 2) == 0;
         if (attributeModifiersTag != null) {
            this.updateAttributes(data, attributeModifiersTag, showAttributes);
         } else if (!showAttributes) {
            data.set(StructuredDataKey.ATTRIBUTE_MODIFIERS1_20_5, new AttributeModifiers1_20_5(new AttributeModifiers1_20_5.AttributeModifier[0], false));
         }

         CompoundTag fireworksTag = tag.getCompoundTag("Fireworks");
         if (fireworksTag != null) {
            ListTag<CompoundTag> explosionsTag = fireworksTag.getListTag("Explosions", CompoundTag.class);
            this.updateFireworks(data, fireworksTag, explosionsTag);
         }

         if (old.identifier() == 1085) {
            this.updateWritableBookPages(data, tag);
         } else if (old.identifier() == 1086) {
            this.updateWrittenBookPages(connection, data, tag);
         }

         this.updatePotionTags(data, tag);
         this.updateMobTags(data, tag);
         this.updateItemList(connection, data, tag, "ChargedProjectiles", StructuredDataKey.V1_20_5.chargedProjectiles);
         if (old.identifier() == 927) {
            this.updateItemList(connection, data, tag, "Items", StructuredDataKey.V1_20_5.bundleContents);
         }

         this.updateEnchantments(data, tag, "Enchantments", StructuredDataKey.ENCHANTMENTS1_20_5, (hideFlagsValue & 1) == 0);
         this.updateEnchantments(data, tag, "StoredEnchantments", StructuredDataKey.STORED_ENCHANTMENTS1_20_5, (hideFlagsValue & 32) == 0);
         NumberTag mapId = tag.getNumberTag("map");
         if (mapId != null) {
            data.set(StructuredDataKey.MAP_ID, mapId.asInt());
         }

         ListTag<CompoundTag> decorationsTag = tag.getListTag("Decorations", CompoundTag.class);
         if (decorationsTag != null) {
            this.updateMapDecorations(data, decorationsTag);
         }

         this.updateProfile(data, tag.get("SkullOwner"));
         ListTag<StringTag> canPlaceOnTag = tag.getListTag("CanPlaceOn", StringTag.class);
         if (canPlaceOnTag != null) {
            data.set(StructuredDataKey.CAN_PLACE_ON1_20_5, this.updateBlockPredicates(connection, canPlaceOnTag, (hideFlagsValue & 16) == 0));
         }

         ListTag<StringTag> canDestroyTag = tag.getListTag("CanDestroy", StringTag.class);
         if (canDestroyTag != null) {
            data.set(StructuredDataKey.CAN_BREAK1_20_5, this.updateBlockPredicates(connection, canDestroyTag, (hideFlagsValue & 8) == 0));
         }

         CompoundTag backupTag = StructuredDataConverter.removeBackupTag(tag);
         if (backupTag != null) {
            this.restoreFromBackupTag(backupTag, data);
         }

         return item;
      }
   }

   private void appendItemDataFixComponents(UserConnection connection, Item item) {
      ProtocolVersion serverVersion = connection.getProtocolInfo().serverProtocolVersion();
      if (serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_17_1) && item.identifier() == 1182) {
         item.dataContainer().set(StructuredDataKey.MAX_DAMAGE, 326);
      }
   }

   private int unmappedItemId(String name) {
      return this.protocol.getMappingData().getFullItemMappings().id(name);
   }

   private int toMappedItemId(String name) {
      int unmappedId = this.unmappedItemId(name);
      return unmappedId != -1 ? this.protocol.getMappingData().getNewItemId(unmappedId) : -1;
   }

   private void restoreFromBackupTag(CompoundTag backupTag, StructuredDataContainer data) {
      CompoundTag instrument = backupTag.getCompoundTag("instrument");
      if (instrument != null) {
         this.restoreInstrumentFromBackup(instrument, data);
      }

      IntArrayTag potDecorationsTag = backupTag.getIntArrayTag("pot_decorations");
      if (potDecorationsTag != null && potDecorationsTag.getValue().length == 4) {
         data.set(StructuredDataKey.POT_DECORATIONS1_20_5, new PotDecorations1_20_5(potDecorationsTag.getValue()));
      }

      ByteTag enchantmentGlintOverride = backupTag.getByteTag("enchantment_glint_override");
      if (enchantmentGlintOverride != null) {
         data.set(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, enchantmentGlintOverride.asBoolean());
      }

      if (backupTag.contains("hide_tooltip")) {
         data.set(StructuredDataKey.HIDE_TOOLTIP);
      }

      Tag intangibleProjectile = backupTag.get("intangible_projectile");
      if (intangibleProjectile != null) {
         data.set(StructuredDataKey.INTANGIBLE_PROJECTILE, intangibleProjectile);
      }

      IntTag maxStackSize = backupTag.getIntTag("max_stack_size");
      if (maxStackSize != null) {
         data.set(StructuredDataKey.MAX_STACK_SIZE, MathUtil.clamp(maxStackSize.asInt(), 1, 99));
      }

      IntTag maxDamage = backupTag.getIntTag("max_damage");
      if (maxDamage != null) {
         data.set(StructuredDataKey.MAX_DAMAGE, Math.max(maxDamage.asInt(), 1));
      }

      IntTag rarity = backupTag.getIntTag("rarity");
      if (rarity != null) {
         data.set(StructuredDataKey.RARITY, rarity.asInt());
      }

      CompoundTag food = backupTag.getCompoundTag("food");
      if (food != null) {
         this.restoreFoodFromBackup(food, data);
      }

      if (backupTag.contains("fire_resistant")) {
         data.set(StructuredDataKey.FIRE_RESISTANT);
      }

      CompoundTag tool = backupTag.getCompoundTag("tool");
      if (tool != null) {
         this.restoreToolFromBackup(tool, data);
      }

      IntTag ominousBottleAmplifier = backupTag.getIntTag("ominous_bottle_amplifier");
      if (ominousBottleAmplifier != null) {
         data.set(StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER, MathUtil.clamp(ominousBottleAmplifier.asInt(), 0, 4));
      }

      ListTag<CompoundTag> bannerPatterns = backupTag.getListTag("banner_patterns", CompoundTag.class);
      if (bannerPatterns != null) {
         this.restoreBannerPatternsFromBackup(bannerPatterns, data);
      }

      CompoundTag trim = backupTag.getCompoundTag("trim");
      if (trim != null) {
         this.restoreArmorTrimFromBackup(trim, data);
      }
   }

   private void restoreInstrumentFromBackup(CompoundTag instrument, StructuredDataContainer data) {
      int useDuration = instrument.getInt("use_duration");
      float range = instrument.getFloat("range");
      CompoundTag soundEventTag = instrument.getCompoundTag("sound_event");
      Holder<SoundEvent> soundEvent;
      if (soundEventTag != null) {
         StringTag identifier = soundEventTag.getStringTag("identifier");
         if (identifier == null) {
            return;
         }

         soundEvent = Holder.of(new SoundEvent(identifier.getValue(), soundEventTag.contains("fixed_range") ? soundEventTag.getFloat("fixed_range") : null));
      } else {
         soundEvent = Holder.of(instrument.getInt("sound_event"));
      }

      data.set(StructuredDataKey.INSTRUMENT1_20_5, Holder.of(new Instrument1_20_5(soundEvent, useDuration, range)));
   }

   private void restoreFoodFromBackup(CompoundTag food, StructuredDataContainer data) {
      int nutrition = food.getInt("nutrition");
      float saturation = food.getFloat("saturation");
      boolean canAlwaysEat = food.getBoolean("can_always_eat");
      float eatSeconds = food.getFloat("eat_seconds");
      ListTag<CompoundTag> possibleEffectsTag = food.getListTag("possible_effects", CompoundTag.class);
      if (possibleEffectsTag != null) {
         List<FoodProperties1_20_5.FoodEffect> possibleEffects = new ArrayList<>();

         for (CompoundTag effect : possibleEffectsTag) {
            CompoundTag potionEffectTag = effect.getCompoundTag("effect");
            if (potionEffectTag != null) {
               possibleEffects.add(
                  new FoodProperties1_20_5.FoodEffect(
                     new PotionEffect(potionEffectTag.getInt("effect"), this.readPotionEffectData(potionEffectTag)), effect.getFloat("probability")
                  )
               );
            }
         }

         data.set(
            StructuredDataKey.FOOD1_20_5,
            new FoodProperties1_20_5(nutrition, saturation, canAlwaysEat, eatSeconds, null, possibleEffects.toArray(new FoodProperties1_20_5.FoodEffect[0]))
         );
      }
   }

   private void restoreToolFromBackup(CompoundTag tool, StructuredDataContainer data) {
      ListTag<CompoundTag> rulesTag = tool.getListTag("rules", CompoundTag.class);
      if (rulesTag != null) {
         List<ToolRule> rules = new ArrayList<>();

         for (CompoundTag tag : rulesTag) {
            HolderSet blocks = null;
            if (tag.get("blocks") instanceof StringTag blocksTag) {
               blocks = HolderSet.of(blocksTag.getValue());
            } else {
               IntArrayTag blockIds = tag.getIntArrayTag("blocks");
               if (blockIds != null) {
                  blocks = HolderSet.of(blockIds.getValue());
               }
            }

            if (blocks != null) {
               rules.add(
                  new ToolRule(
                     blocks,
                     tag.contains("speed") ? tag.getFloat("speed") : null,
                     tag.contains("correct_for_drops") ? tag.getBoolean("correct_for_drops") : null
                  )
               );
            }
         }

         data.set(
            StructuredDataKey.TOOL1_20_5,
            new ToolProperties(rules.toArray(new ToolRule[0]), tool.getFloat("default_mining_speed"), tool.getInt("damage_per_block"))
         );
      }
   }

   private void restoreBannerPatternsFromBackup(ListTag<CompoundTag> bannerPatterns, StructuredDataContainer data) {
      List<BannerPatternLayer> patternLayer = new ArrayList<>();

      for (CompoundTag tag : bannerPatterns) {
         CompoundTag patternTag = tag.getCompoundTag("pattern");
         Holder<BannerPattern> pattern;
         if (patternTag != null) {
            String assetId = patternTag.getString("asset_id");
            String translationKey = patternTag.getString("translation_key");
            pattern = Holder.of(new BannerPattern(assetId, translationKey));
         } else {
            pattern = Holder.of(tag.getInt("pattern"));
         }

         int dyeColor = tag.getInt("dye_color");
         patternLayer.add(new BannerPatternLayer(pattern, dyeColor));
      }

      data.set(StructuredDataKey.BANNER_PATTERNS, patternLayer.toArray(new BannerPatternLayer[0]));
   }

   private void restoreArmorTrimFromBackup(CompoundTag trim, StructuredDataContainer data) {
      Tag materialTag = trim.get("material");
      Holder<ArmorTrimMaterial1_20_5> materialHolder;
      if (materialTag instanceof IntTag materialIntTag) {
         materialHolder = Holder.of(materialIntTag.asInt());
      } else {
         if (!(materialTag instanceof CompoundTag materialCompoundTag)) {
            return;
         }

         ArmorTrimMaterial1_20_5 material = this.readTrimMaterial(materialCompoundTag);
         if (material == null) {
            return;
         }

         materialHolder = Holder.of(material);
      }

      Holder<ArmorTrimPattern> patternHolder = Holder.of(trim.getInt("pattern"));
      boolean showInTooltip = trim.getBoolean("show_in_tooltip");
      data.set(StructuredDataKey.TRIM1_20_5, new ArmorTrim1_20_5(materialHolder, patternHolder, showInTooltip));
   }

   private AdventureModePredicate updateBlockPredicates(UserConnection connection, ListTag<StringTag> tag, boolean showInTooltip) {
      BlockPredicate[] predicates = tag.stream()
         .map(StringTag::getValue)
         .map(rawPredicate -> this.deserializeBlockPredicate(connection, rawPredicate))
         .filter(Objects::nonNull)
         .toArray(BlockPredicate[]::new);
      return new AdventureModePredicate(predicates, showInTooltip);
   }

   @Nullable
   private BlockPredicate deserializeBlockPredicate(UserConnection connection, String rawPredicate) {
      int propertiesStartIndex = rawPredicate.indexOf(91);
      int tagStartIndex = rawPredicate.indexOf(123);
      int idLength = rawPredicate.length();
      if (propertiesStartIndex != -1) {
         idLength = propertiesStartIndex;
      }

      if (tagStartIndex != -1) {
         idLength = propertiesStartIndex != -1 ? Math.min(propertiesStartIndex, tagStartIndex) : tagStartIndex;
      }

      String identifier = rawPredicate.substring(0, idLength);
      HolderSet holders;
      if (!identifier.startsWith("#")) {
         int id = Protocol1_20_3To1_20_5.MAPPINGS.blockId(identifier);
         if (id == -1) {
            return null;
         }

         holders = HolderSet.of(new int[]{id});
      } else {
         String tagKey = identifier.substring(1);
         ProtocolStorables1_20_5 storables = connection.storables(this.protocol);
         if (!storables.tagKeys().isValidIdentifier(tagKey)) {
            return null;
         }

         holders = HolderSet.of(tagKey);
      }

      int propertiesEndIndex = rawPredicate.indexOf(93);
      List<StatePropertyMatcher> propertyMatchers = new ArrayList<>();
      if (propertiesStartIndex != -1 && propertiesEndIndex != -1) {
         for (String property : rawPredicate.substring(propertiesStartIndex + 1, propertiesEndIndex).split(",")) {
            int propertySplitIndex = property.indexOf(61);
            if (propertySplitIndex != -1) {
               String propertyId = property.substring(0, propertySplitIndex).trim();
               String propertyValue = property.substring(propertySplitIndex + 1).trim();
               propertyMatchers.add(new StatePropertyMatcher(propertyId, Either.left(propertyValue)));
            }
         }
      }

      int tagEndIndex = rawPredicate.indexOf(125);
      CompoundTag tag = null;
      if (tagStartIndex != -1 && tagEndIndex != -1) {
         try {
            tag = (CompoundTag)SerializerVersion.V1_20_3.toTag(rawPredicate.substring(tagStartIndex, tagEndIndex + 1));
         } catch (Exception var17) {
            if (Via.getManager().isDebug()) {
               Protocol1_20_3To1_20_5.LOGGER
                  .log(Level.SEVERE, "Failed to parse block predicate tag: " + rawPredicate.substring(tagStartIndex, tagEndIndex + 1), var17);
            }
         }
      }

      return new BlockPredicate(holders, propertyMatchers.isEmpty() ? null : propertyMatchers.toArray(EMPTY_PROPERTY_MATCHERS), tag);
   }

   private void updateAttributes(StructuredDataContainer data, ListTag<CompoundTag> attributeModifiersTag, boolean showInTooltip) {
      List<AttributeModifiers1_20_5.AttributeModifier> modifiers = new ArrayList<>();

      for (int i = 0; i < attributeModifiersTag.size(); i++) {
         CompoundTag modifierTag = attributeModifiersTag.get(i);
         String attributeName = modifierTag.getString("AttributeName");
         String name = modifierTag.getString("Name");
         NumberTag amountTag = modifierTag.getNumberTag("Amount");
         IntArrayTag uuidTag = modifierTag.getIntArrayTag("UUID");
         String slotType = modifierTag.getString("Slot", "any");
         if (name != null && attributeName != null && amountTag != null && uuidTag != null) {
            int slotTypeId = EquipmentSlots1_20_5.keyToId(slotType);
            if (slotTypeId != -1) {
               int operationId = modifierTag.getInt("Operation");
               if (operationId >= 0 && operationId <= 2) {
                  int attributeId = Attributes1_20_5.keyToId(attributeName);
                  if (attributeId != -1) {
                     modifiers.add(
                        new AttributeModifiers1_20_5.AttributeModifier(
                           attributeId,
                           new AttributeModifiers1_20_5.ModifierData(UUIDUtil.fromIntArray(uuidTag.getValue()), name, amountTag.asDouble(), operationId),
                           slotTypeId
                        )
                     );
                  }
               }
            }
         }
      }

      data.set(
         StructuredDataKey.ATTRIBUTE_MODIFIERS1_20_5,
         new AttributeModifiers1_20_5(modifiers.toArray(new AttributeModifiers1_20_5.AttributeModifier[0]), showInTooltip)
      );
   }

   private PotionEffectData readPotionEffectData(CompoundTag tag) {
      byte amplifier = tag.getByte("amplifier");
      int duration = tag.getInt("duration");
      boolean ambient = tag.getBoolean("ambient");
      boolean showParticles = tag.getBoolean("show_particles");
      boolean showIcon = tag.getBoolean("show_icon");
      PotionEffectData hiddenEffect = null;
      CompoundTag hiddenEffectTag = tag.getCompoundTag("hidden_effect");
      if (hiddenEffectTag != null) {
         hiddenEffect = this.readPotionEffectData(hiddenEffectTag);
      }

      return new PotionEffectData(amplifier, duration, ambient, showParticles, showIcon, hiddenEffect);
   }

   private void updatePotionTags(StructuredDataContainer data, CompoundTag tag) {
      String potion = tag.getString("Potion");
      Integer potionId = null;
      if (potion != null) {
         int id = Potions1_20_5.keyToId(potion);
         if (id != -1) {
            potionId = id;
         }
      }

      NumberTag customPotionColorTag = tag.getNumberTag("CustomPotionColor");
      ListTag<CompoundTag> customPotionEffectsTag = tag.getListTag("custom_potion_effects", CompoundTag.class);
      PotionEffect[] potionEffects = null;
      if (customPotionEffectsTag != null) {
         potionEffects = customPotionEffectsTag.stream().map(effectTag -> {
            String identifier = effectTag.getString("id");
            if (identifier == null) {
               return null;
            } else {
               int id = PotionEffects1_20_5.keyToId(identifier);
               return id == -1 ? null : new PotionEffect(id, this.readPotionEffectData(effectTag));
            }
         }).filter(Objects::nonNull).toArray(PotionEffect[]::new);
      }

      if (potionId != null || customPotionColorTag != null || potionEffects != null) {
         data.set(
            StructuredDataKey.POTION_CONTENTS1_20_5,
            new PotionContents(
               potionId, customPotionColorTag != null ? customPotionColorTag.asInt() : null, potionEffects != null ? potionEffects : new PotionEffect[0]
            )
         );
      }
   }

   private void updateArmorTrim(UserConnection connection, StructuredDataContainer data, CompoundTag trimTag, boolean showInTooltip) {
      Tag materialTag = trimTag.get("material");
      ArmorTrimStorage trimStorage = connection.get(ArmorTrimStorage.class);
      Holder<ArmorTrimMaterial1_20_5> materialHolder;
      if (materialTag instanceof StringTag materialStringTag) {
         int id = trimStorage.trimMaterials().keyToId(materialStringTag.getValue());
         if (id == -1) {
            return;
         }

         materialHolder = Holder.of(id);
      } else {
         if (!(materialTag instanceof CompoundTag materialCompoundTag)) {
            return;
         }

         ArmorTrimMaterial1_20_5 material = this.readTrimMaterial(materialCompoundTag);
         if (material == null) {
            return;
         }

         materialHolder = Holder.of(material);
      }

      Tag patternTag = trimTag.get("pattern");
      Holder<ArmorTrimPattern> patternHolder;
      if (patternTag instanceof StringTag patternStringTag) {
         int id = trimStorage.trimPatterns().keyToId(patternStringTag.getValue());
         if (id == -1) {
            return;
         }

         patternHolder = Holder.of(id);
      } else {
         if (!(patternTag instanceof CompoundTag patternCompoundTag)) {
            return;
         }

         String assetId = patternCompoundTag.getString("assetId");
         String templateItem = patternCompoundTag.getString("templateItem");
         if (assetId == null || templateItem == null) {
            return;
         }

         int templateItemId = StructuredDataConverter.removeItemBackupTag(patternCompoundTag, this.toMappedItemId(templateItem));
         if (templateItemId == -1) {
            return;
         }

         Tag descriptionTag = patternCompoundTag.get("description");
         boolean decal = patternCompoundTag.getBoolean("decal");
         patternHolder = Holder.of(new ArmorTrimPattern(assetId, templateItemId, descriptionTag, decal));
      }

      data.set(StructuredDataKey.TRIM1_20_5, new ArmorTrim1_20_5(materialHolder, patternHolder, showInTooltip));
   }

   private ArmorTrimMaterial1_20_5 readTrimMaterial(CompoundTag tag) {
      StringTag assetNameTag = tag.getStringTag("asset_name");
      StringTag ingredientTag = tag.getStringTag("ingredient");
      if (assetNameTag != null && ingredientTag != null) {
         int ingredientId = StructuredDataConverter.removeItemBackupTag(tag, this.toMappedItemId(ingredientTag.getValue()));
         if (ingredientId == -1) {
            return null;
         } else {
            NumberTag itemModelIndexTag = tag.getNumberTag("item_model_index");
            CompoundTag overrideArmorMaterialsTag = tag.getCompoundTag("override_armor_materials");
            Tag descriptionTag = tag.get("description");
            Map<String, String> overrideArmorMaterials = new Object2ObjectArrayMap<>();
            if (overrideArmorMaterialsTag != null) {
               for (Entry<String, Tag> entry : overrideArmorMaterialsTag.entrySet()) {
                  if (entry.getValue() instanceof StringTag valueTag) {
                     overrideArmorMaterials.put(entry.getKey(), valueTag.getValue());
                  }
               }
            }

            return new ArmorTrimMaterial1_20_5(
               assetNameTag.getValue(), ingredientId, itemModelIndexTag != null ? itemModelIndexTag.asFloat() : 0.0F, overrideArmorMaterials, descriptionTag
            );
         }
      } else {
         return null;
      }
   }

   private void updateMobTags(StructuredDataContainer data, CompoundTag tag) {
      CompoundTag bucketEntityData = new CompoundTag();

      for (String mobTagKey : MOB_TAGS) {
         Tag mobTag = tag.get(mobTagKey);
         if (mobTag != null) {
            bucketEntityData.put(mobTagKey, mobTag);
         }
      }

      if (!bucketEntityData.isEmpty()) {
         data.set(StructuredDataKey.BUCKET_ENTITY_DATA, bucketEntityData);
      }
   }

   private void updateBlockState(StructuredDataContainer data, CompoundTag blockState) {
      Map<String, String> properties = new HashMap<>();

      for (Entry<String, Tag> entry : blockState.entrySet()) {
         Tag value = entry.getValue();
         if (value instanceof StringTag valueStringTag) {
            properties.put(entry.getKey(), valueStringTag.getValue());
         } else if (value instanceof IntTag valueIntTag) {
            properties.put(entry.getKey(), Integer.toString(valueIntTag.asInt()));
         }
      }

      data.set(StructuredDataKey.BLOCK_STATE, new BlockStateProperties(properties));
   }

   private void updateFireworks(StructuredDataContainer data, CompoundTag fireworksTag, ListTag<CompoundTag> explosionsTag) {
      int flightDuration = fireworksTag.getByte("Flight");
      Fireworks fireworks = new Fireworks(
         flightDuration,
         explosionsTag != null ? explosionsTag.stream().limit(256L).map(this::readExplosion).toArray(FireworkExplosion[]::new) : new FireworkExplosion[0]
      );
      data.set(StructuredDataKey.FIREWORKS, fireworks);
   }

   private void updateEffects(ListTag<CompoundTag> effects, StructuredDataContainer data) {
      List<SuspiciousStewEffect> suspiciousStewEffects = new ArrayList<>();

      for (int i = 0; i < effects.size(); i++) {
         CompoundTag effect = effects.get(i);
         String effectIdString = effect.getString("id", "luck");
         int duration = effect.getInt("duration");
         int effectId = PotionEffects1_20_5.keyToId(effectIdString);
         if (effectId != -1) {
            SuspiciousStewEffect stewEffect = new SuspiciousStewEffect(effectId, duration);
            suspiciousStewEffects.add(stewEffect);
         }
      }

      data.set(StructuredDataKey.SUSPICIOUS_STEW_EFFECTS, suspiciousStewEffects.toArray(SuspiciousStewEffect[]::new));
   }

   private void updateLodestoneTracker(boolean tracked, CompoundTag lodestonePosTag, String lodestoneDimension, StructuredDataContainer data) {
      GlobalBlockPosition position = null;
      if (lodestonePosTag != null && lodestoneDimension != null) {
         int x = lodestonePosTag.getInt("X");
         int y = lodestonePosTag.getInt("Y");
         int z = lodestonePosTag.getInt("Z");
         position = new GlobalBlockPosition(lodestoneDimension, x, y, z);
      }

      data.set(StructuredDataKey.LODESTONE_TRACKER, new LodestoneTracker(position, tracked));
   }

   private FireworkExplosion readExplosion(CompoundTag tag) {
      int shape = tag.getInt("Type");
      IntArrayTag colors = tag.getIntArrayTag("Colors");
      IntArrayTag fadeColors = tag.getIntArrayTag("FadeColors");
      boolean trail = tag.getBoolean("Trail");
      boolean flicker = tag.getBoolean("Flicker");
      return new FireworkExplosion(
         shape, colors != null ? colors.getValue() : new int[0], fadeColors != null ? fadeColors.getValue() : new int[0], trail, flicker
      );
   }

   private void updateWritableBookPages(StructuredDataContainer data, CompoundTag tag) {
      ListTag<StringTag> pagesTag = tag.getListTag("pages", StringTag.class);
      CompoundTag filteredPagesTag = tag.getCompoundTag("filtered_pages");
      if (pagesTag != null) {
         List<FilterableString> pages = new ArrayList<>();

         for (int i = 0; i < pagesTag.size(); i++) {
            StringTag page = pagesTag.get(i);
            String filtered = null;
            if (filteredPagesTag != null) {
               StringTag filteredPage = filteredPagesTag.getStringTag(String.valueOf(i));
               if (filteredPage != null) {
                  filtered = this.limit(filteredPage.getValue(), 1024);
               }
            }

            pages.add(new FilterableString(this.limit(page.getValue(), 1024), filtered));
            if (pages.size() == 100) {
               break;
            }
         }

         data.set(StructuredDataKey.WRITABLE_BOOK_CONTENT, new WritableBook(pages.toArray(new FilterableString[0])));
      }
   }

   private void updateWrittenBookPages(UserConnection connection, StructuredDataContainer data, CompoundTag tag) {
      String title = tag.getString("title");
      String author = tag.getString("author");
      ListTag<StringTag> pagesTag = tag.getListTag("pages", StringTag.class);
      boolean valid = author != null && title != null && title.length() <= 32 && pagesTag != null;
      if (valid) {
         for (StringTag page : pagesTag) {
            if (page.getValue().length() > 32767) {
               valid = false;
               break;
            }
         }
      }

      List<FilterableComponent> pages = new ArrayList<>();
      if (valid) {
         CompoundTag filteredPagesTag = tag.getCompoundTag("filtered_pages");

         for (int i = 0; i < pagesTag.size(); i++) {
            StringTag pagex = pagesTag.get(i);
            Tag filtered = null;
            if (filteredPagesTag != null) {
               StringTag filteredPage = filteredPagesTag.getStringTag(String.valueOf(i));
               if (filteredPage != null) {
                  try {
                     filtered = this.jsonToTag(connection, filteredPage);
                  } catch (Exception var16) {
                     continue;
                  }
               }
            }

            Tag parsedPage;
            try {
               parsedPage = this.jsonToTag(connection, pagex);
            } catch (Exception var15) {
               continue;
            }

            pages.add(new FilterableComponent(parsedPage, filtered));
         }
      } else {
         CompoundTag invalidPage = new CompoundTag();
         invalidPage.putString("text", "* Invalid book tag *");
         invalidPage.putString("color", "#AA0000");
         pages.add(new FilterableComponent(invalidPage, null));
      }

      String filteredTitle = tag.getString("filtered_title");
      int generation = tag.getInt("generation");
      boolean resolved = tag.getBoolean("resolved");
      WrittenBook writtenBook = new WrittenBook(
         new FilterableString(this.limit(title == null ? "" : title, 32), this.limit(filteredTitle, 32)),
         author == null ? "" : author,
         MathUtil.clamp(generation, 0, 3),
         pages.toArray(new FilterableComponent[0]),
         resolved
      );
      data.set(StructuredDataKey.WRITTEN_BOOK_CONTENT, writtenBook);
   }

   private Tag jsonToTag(UserConnection connection, StringTag stringTag) {
      Tag tag = ComponentUtil.jsonStringToTag(stringTag.getValue(), SerializerVersion.V1_20_3, SerializerVersion.V1_20_3);
      this.protocol.getComponentRewriter().processTag(connection, tag);
      return tag;
   }

   private void updateItemList(UserConnection connection, StructuredDataContainer data, CompoundTag tag, String key, StructuredDataKey<Item[]> dataKey) {
      ListTag<CompoundTag> itemsTag = tag.getListTag(key, CompoundTag.class);
      if (itemsTag != null) {
         Item[] items = itemsTag.stream()
            .limit(256L)
            .map(item -> this.itemFromTag(connection, item.copy()))
            .filter(item -> !item.isEmpty())
            .toArray(Item[]::new);
         data.set(dataKey, items);
      }
   }

   private Item itemFromTag(UserConnection connection, CompoundTag item) {
      String id = item.getString("id");
      if (id == null) {
         return StructuredItem.empty();
      } else {
         int itemId = StructuredDataConverter.removeItemBackupTag(item, this.unmappedItemId(id));
         if (itemId == -1) {
            return StructuredItem.empty();
         } else {
            byte count = item.getByte("Count", (byte)1);
            CompoundTag tag = item.getCompoundTag("tag");
            return this.handleItemToClient(connection, new DataItem(itemId, count, tag));
         }
      }
   }

   private void updateEnchantments(StructuredDataContainer data, CompoundTag tag, String key, StructuredDataKey<Enchantments> newKey, boolean show) {
      ListTag<CompoundTag> enchantmentsTag = tag.getListTag(key, CompoundTag.class);
      if (enchantmentsTag != null) {
         Enchantments enchantments = new Enchantments(new Int2IntOpenHashMap(), show);

         for (CompoundTag enchantment : enchantmentsTag) {
            String id = enchantment.getString("id");
            NumberTag lvl = enchantment.getNumberTag("lvl");
            if (id != null && lvl != null) {
               if (Key.stripMinecraftNamespace(id).equals("sweeping")) {
                  id = Key.namespaced("sweeping_edge");
               }

               int intId = Enchantments1_20_5.keyToId(id);
               if (intId != -1) {
                  enchantments.enchantments().put(intId, MathUtil.clamp(lvl.asInt(), 0, 255));
               }
            }
         }

         data.set(newKey, enchantments);
         if (!enchantmentsTag.isEmpty() && enchantments.size() == 0) {
            data.set(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, true);
         }
      }
   }

   private void updateProfile(StructuredDataContainer data, Tag skullOwnerTag) {
      if (skullOwnerTag instanceof StringTag nameTag) {
         String name = nameTag.getValue();
         if (this.isValidName(name)) {
            data.set(StructuredDataKey.PROFILE1_20_5, new GameProfile(name, null, EMPTY_PROPERTIES));
         }
      } else if (skullOwnerTag instanceof CompoundTag skullOwner) {
         String name = skullOwner.getString("Name", "");
         if (!this.isValidName(name)) {
            name = null;
         }

         IntArrayTag idTag = skullOwner.getIntArrayTag("Id");
         UUID uuid = null;
         if (idTag != null) {
            uuid = UUIDUtil.fromIntArray(idTag.getValue());
         }

         List<GameProfile.Property> properties = new ArrayList<>(1);
         CompoundTag propertiesTag = skullOwner.getCompoundTag("Properties");
         if (propertiesTag != null) {
            this.updateProperties(propertiesTag, properties);
         }

         data.set(StructuredDataKey.PROFILE1_20_5, new GameProfile(name, uuid, properties.toArray(EMPTY_PROPERTIES)));
      }
   }

   @Nullable
   private String limit(@Nullable String s, int length) {
      if (s == null) {
         return null;
      } else {
         return s.length() > length ? s.substring(0, length) : s;
      }
   }

   private void updateBees(StructuredDataContainer data, ListTag<CompoundTag> beesTag) {
      Bee[] bees = beesTag.stream().map(bee -> {
         CompoundTag entityData = bee.getCompoundTag("EntityData");
         if (entityData == null) {
            return null;
         } else {
            int ticksInHive = bee.getInt("TicksInHive");
            int minOccupationTicks = bee.getInt("MinOccupationTicks");
            return new Bee(entityData, ticksInHive, minOccupationTicks);
         }
      }).filter(Objects::nonNull).toArray(Bee[]::new);
      data.set(StructuredDataKey.BEES1_20_5, bees);
   }

   private void updateProperties(CompoundTag propertiesTag, List<GameProfile.Property> properties) {
      for (Entry<String, Tag> entry : propertiesTag.entrySet()) {
         Object var6 = entry.getValue();
         if (var6 instanceof ListTag) {
            for (Tag propertyTag : (ListTag)var6) {
               if (propertyTag instanceof CompoundTag compoundTag) {
                  String value = compoundTag.getString("Value", "");
                  String signature = compoundTag.getString("Signature");
                  properties.add(new GameProfile.Property(this.limit(entry.getKey(), 64), value, this.limit(signature, 1024)));
                  if (properties.size() == 16) {
                     return;
                  }
               }
            }
         }
      }
   }

   private void updateMapDecorations(StructuredDataContainer data, ListTag<CompoundTag> decorationsTag) {
      CompoundTag updatedDecorationsTag = new CompoundTag();

      for (CompoundTag decorationTag : decorationsTag) {
         String id = decorationTag.getString("id", "");
         int type = decorationTag.getInt("type");
         double x = decorationTag.getDouble("x");
         double z = decorationTag.getDouble("z");
         float rotation = decorationTag.getFloat("rot");
         CompoundTag updatedDecorationTag = new CompoundTag();
         updatedDecorationTag.putString("type", MapDecorations1_20_5.idToKey(type));
         updatedDecorationTag.putDouble("x", x);
         updatedDecorationTag.putDouble("z", z);
         updatedDecorationTag.putFloat("rotation", rotation);
         updatedDecorationsTag.put(id, updatedDecorationTag);
      }

      data.set(StructuredDataKey.MAP_DECORATIONS, updatedDecorationsTag);
   }

   private void updateDisplay(UserConnection connection, StructuredDataContainer data, CompoundTag displayTag, int hideFlags) {
      if (displayTag != null) {
         NumberTag mapColorTag = displayTag.getNumberTag("MapColor");
         if (mapColorTag != null) {
            data.set(StructuredDataKey.MAP_COLOR, mapColorTag.asInt());
         }

         StringTag nameTag = displayTag.getStringTag("Name");
         if (nameTag != null) {
            try {
               Tag convertedName = this.jsonToTag(connection, nameTag);
               data.set(StructuredDataKey.CUSTOM_NAME, convertedName);
            } catch (Exception var10) {
            }
         }

         ListTag<StringTag> loreTag = displayTag.getListTag("Lore", StringTag.class);
         if (loreTag != null) {
            try {
               data.set(StructuredDataKey.LORE, loreTag.stream().limit(256L).map(t -> this.jsonToTag(connection, t)).toArray(Tag[]::new));
            } catch (Exception var9) {
            }
         }

         NumberTag colorTag = displayTag.getNumberTag("color");
         if (colorTag != null) {
            data.set(StructuredDataKey.DYED_COLOR1_20_5, new DyedColor(colorTag.asInt(), (hideFlags & 64) == 0));
         }
      }
   }

   private void addBlockEntityId(CompoundTag tag, String id) {
      if (!tag.contains("id")) {
         tag.putString("id", id);
      }
   }

   private boolean isUnknownBlockEntity(int id) {
      return id < 0 || id > 42;
   }

   private void updateBlockEntityTag(UserConnection connection, @Nullable StructuredDataContainer data, CompoundTag tag) {
      if (tag != null) {
         if (data != null) {
            StringTag lockTag = tag.getStringTag("Lock");
            if (lockTag != null) {
               data.set(StructuredDataKey.LOCK1_20_5, lockTag);
            }

            ListTag<CompoundTag> beesTag = tag.getListTag("Bees", CompoundTag.class);
            if (beesTag != null) {
               this.updateBees(data, beesTag);
               this.addBlockEntityId(tag, "beehive");
            }

            ListTag<StringTag> sherdsTag = tag.getListTag("sherds", StringTag.class);
            if (sherdsTag != null && sherdsTag.size() == 4) {
               String backSherd = sherdsTag.get(0).getValue();
               String leftSherd = sherdsTag.get(1).getValue();
               String rightSherd = sherdsTag.get(2).getValue();
               String frontSherd = sherdsTag.get(3).getValue();
               data.set(
                  StructuredDataKey.POT_DECORATIONS1_20_5,
                  new PotDecorations1_20_5(
                     this.toMappedItemId(backSherd), this.toMappedItemId(leftSherd), this.toMappedItemId(rightSherd), this.toMappedItemId(frontSherd)
                  )
               );
               this.addBlockEntityId(tag, "decorated_pot");
            }

            StringTag noteBlockSoundTag = tag.getStringTag("note_block_sound");
            if (noteBlockSoundTag != null) {
               data.set(StructuredDataKey.NOTE_BLOCK_SOUND, Key.of(noteBlockSoundTag.getValue()));
               this.addBlockEntityId(tag, "player_head");
            }

            StringTag lootTableTag = tag.getStringTag("LootTable");
            if (lootTableTag != null) {
               long lootTableSeed = tag.getLong("LootTableSeed");
               CompoundTag containerLoot = new CompoundTag();
               containerLoot.putString("loot_table", lootTableTag.getValue());
               containerLoot.putLong("loot_table_seed", lootTableSeed);
               data.set(StructuredDataKey.CONTAINER_LOOT, containerLoot);
            }

            if (tag.remove("Base") instanceof NumberTag baseColorIntTag) {
               data.set(StructuredDataKey.BASE_COLOR, baseColorIntTag.asInt());
            }

            ListTag<CompoundTag> itemsTag = tag.getListTag("Items", CompoundTag.class);
            if (itemsTag != null) {
               int highestSlot = 0;
               int i = 0;

               for (int size = Math.min(itemsTag.size(), 256); i < size; i++) {
                  CompoundTag itemTag = itemsTag.get(i);
                  Item item = this.itemFromTag(connection, itemTag);
                  if (!item.isEmpty()) {
                     int slot = itemTag.getByte("Slot");
                     highestSlot = MathUtil.clamp(slot, highestSlot, 255);
                  }
               }

               Item[] filteredItems = new Item[highestSlot + 1];
               Arrays.fill(filteredItems, StructuredItem.empty());

               for (CompoundTag itemTag : itemsTag) {
                  Item item = this.itemFromTag(connection, itemTag);
                  if (!item.isEmpty()) {
                     int slot = itemTag.getByte("Slot");
                     if (slot >= 0 && slot < filteredItems.length) {
                        filteredItems[slot] = item;
                     }
                  }
               }

               data.set(StructuredDataKey.V1_20_5.container, filteredItems);
               this.addBlockEntityId(tag, "shulker_box");
            }
         }

         Tag skullOwnerTag = tag.remove("SkullOwner");
         if (skullOwnerTag instanceof StringTag nameTag) {
            CompoundTag profileTag = new CompoundTag();
            profileTag.putString("name", nameTag.getValue());
            tag.put("profile", profileTag);
         } else if (skullOwnerTag instanceof CompoundTag skullOwnerCompoundTag) {
            this.updateSkullOwnerTag(tag, skullOwnerCompoundTag);
         }

         ListTag<CompoundTag> patternsTag = tag.getListTag("Patterns", CompoundTag.class);
         if (patternsTag != null) {
            ProtocolStorables1_20_5 storables = connection.storables(this.protocol);
            BannerPatternStorage patternStorage = storables.bannerPatterns();
            BannerPatternLayer[] layers = patternsTag.stream()
               .map(
                  patternTag -> {
                     String pattern = patternTag.getString("Pattern", "");
                     int color = patternTag.getInt("Color", -1);
                     String fullPatternIdentifier = BannerPatterns1_20_5.compactToFullId(pattern);
                     if (fullPatternIdentifier != null && color != -1) {
                        patternTag.remove("Pattern");
                        patternTag.remove("Color");
                        patternTag.putString("pattern", fullPatternIdentifier);
                        patternTag.putString("color", DyeColors.idToKey(color));
                        int id = patternStorage != null
                           ? patternStorage.bannerPatterns().keyToId(fullPatternIdentifier)
                           : BannerPatterns1_20_5.keyToId(fullPatternIdentifier);
                        return id != -1 ? new BannerPatternLayer(Holder.of(id), color) : null;
                     } else {
                        return null;
                     }
                  }
               )
               .filter(Objects::nonNull)
               .toArray(BannerPatternLayer[]::new);
            tag.remove("Patterns");
            tag.put("patterns", patternsTag);
            this.addBlockEntityId(tag, "banner");
            if (data != null) {
               data.set(StructuredDataKey.BANNER_PATTERNS, layers);
            }
         }

         this.removeEmptyItem(tag, "item");
         this.removeEmptyItem(tag, "RecordItem");
         this.removeEmptyItem(tag, "Book");
      }
   }

   private void removeEmptyItem(CompoundTag tag, String key) {
      CompoundTag itemTag = tag.getCompoundTag(key);
      if (itemTag != null) {
         int id = itemTag.getInt("id");
         if (id == 0) {
            tag.remove(key);
         }
      }
   }

   private void updateSkullOwnerTag(CompoundTag tag, CompoundTag skullOwnerTag) {
      CompoundTag profileTag = new CompoundTag();
      tag.put("profile", profileTag);
      String name = skullOwnerTag.getString("Name");
      if (name != null && this.isValidName(name)) {
         profileTag.putString("name", name);
      }

      IntArrayTag idTag = skullOwnerTag.getIntArrayTag("Id");
      if (idTag != null) {
         profileTag.put("id", idTag);
      }

      if (skullOwnerTag.remove("Properties") instanceof CompoundTag propertiesTag) {
         ListTag var17 = new ListTag<>(CompoundTag.class);

         for (Entry<String, Tag> entry : propertiesTag.entrySet()) {
            Object var11 = entry.getValue();
            if (var11 instanceof ListTag) {
               for (Tag propertyTag : (ListTag)var11) {
                  if (propertyTag instanceof CompoundTag propertyCompoundTag) {
                     CompoundTag updatedPropertyTag = new CompoundTag();
                     String value = propertyCompoundTag.getString("Value", "");
                     String signature = propertyCompoundTag.getString("Signature");
                     updatedPropertyTag.putString("name", entry.getKey());
                     updatedPropertyTag.putString("value", value);
                     if (signature != null) {
                        updatedPropertyTag.putString("signature", signature);
                     }

                     var17.add(updatedPropertyTag);
                  }
               }
            }
         }

         profileTag.put("properties", var17);
      }
   }

   private boolean isValidName(String name) {
      if (name.length() > 16) {
         return false;
      } else {
         int i = 0;

         for (int len = name.length(); i < len; i++) {
            char c = name.charAt(i);
            if (c < '!' || c > '~') {
               return false;
            }
         }

         return true;
      }
   }
}
