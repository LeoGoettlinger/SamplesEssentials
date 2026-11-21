package com.viaversion.viabackwards.protocol.v1_20_5to1_20_3.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.rewriters.BackwardsFullStructuredItemRewriter;
import com.viaversion.viabackwards.api.rewriters.StructuredEnchantmentRewriter;
import com.viaversion.viabackwards.protocol.v1_20_5to1_20_3.Protocol1_20_5To1_20_3;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.Particle.ParticleData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.minecraft.item.data.FireworkExplosion;
import com.viaversion.viaversion.api.minecraft.item.data.Fireworks;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.Protocol1_20_3To1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter.StructuredDataConverter;
import com.viaversion.viaversion.util.Key;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter1_20_5
   extends BackwardsFullStructuredItemRewriter<ClientboundPacket1_20_5, ServerboundPacket1_20_3, Protocol1_20_5To1_20_3> {
   private static final StructuredDataConverter DATA_CONVERTER = new StructuredDataConverter(true);
   private final Protocol1_20_3To1_20_5 vvProtocol = (Protocol1_20_3To1_20_5)Via.getManager().getProtocolManager().getProtocol(Protocol1_20_3To1_20_5.class);
   private final StructuredEnchantmentRewriter enchantmentRewriter = new StructuredEnchantmentRewriter(this);

   public BlockItemPacketRewriter1_20_5(Protocol1_20_5To1_20_3 protocol) {
      super(protocol);
      this.enchantmentRewriter.setRewriteIds(false);
   }

   public void registerPackets() {
      ((Protocol1_20_5To1_20_3)this.protocol).replaceClientbound(ClientboundPackets1_20_5.BLOCK_ENTITY_DATA, wrapper -> {
         wrapper.passthrough(Types.BLOCK_POSITION1_14);
         wrapper.passthrough(Types.VAR_INT);
         CompoundTag tag = (CompoundTag)wrapper.passthrough(Types.TRUSTED_COMPOUND_TAG);
         ((Protocol1_20_5To1_20_3)this.protocol).getBlockRewriter().updateBlockEntityTag(tag);
      });
      ((Protocol1_20_5To1_20_3)this.protocol).registerServerbound(ServerboundPackets1_20_3.CONTAINER_BUTTON_CLICK, wrapper -> {
         int containerId = (Byte)wrapper.read(Types.BYTE) & 255;
         int buttonId = (Byte)wrapper.read(Types.BYTE) & 255;
         wrapper.write(Types.VAR_INT, containerId);
         wrapper.write(Types.VAR_INT, buttonId);
      });
      ((Protocol1_20_5To1_20_3)this.protocol).replaceClientbound(ClientboundPackets1_20_5.LEVEL_PARTICLES, wrapper -> {
         wrapper.write(Types.VAR_INT, 0);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         float data = (Float)wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.INT);
         Particle particle = (Particle)wrapper.read(VersionedTypes.V1_20_5.particle());
         ((Protocol1_20_5To1_20_3)this.protocol).getParticleRewriter().rewriteParticle(wrapper.user(), particle);
         if (particle.id() == ((Protocol1_20_5To1_20_3)this.protocol).getMappingData().getParticleMappings().mappedId("entity_effect")) {
            int color = (Integer)particle.removeArgument(0).getValue();
            if (data == 0.0F) {
               wrapper.set(Types.FLOAT, 3, (float)color);
            }
         } else if (particle.id() == ((Protocol1_20_5To1_20_3)this.protocol).getMappingData().getParticleMappings().mappedId("dust_color_transition")) {
            particle.add(3, Types.FLOAT, (Float)particle.removeArgument(6).getValue());
         }

         wrapper.set(Types.VAR_INT, 0, particle.id());

         for (ParticleData<?> argument : particle.getArguments()) {
            argument.write(wrapper);
         }
      });
      ((Protocol1_20_5To1_20_3)this.protocol).replaceClientbound(ClientboundPackets1_20_5.EXPLODE, wrapper -> {
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.FLOAT);
         int blocks = (Integer)wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < blocks; i++) {
            wrapper.passthrough(Types.BYTE);
            wrapper.passthrough(Types.BYTE);
            wrapper.passthrough(Types.BYTE);
         }

         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.VAR_INT);
         Particle smallExplosionParticle = (Particle)wrapper.passthroughAndMap(VersionedTypes.V1_20_5.particle(), Types1_20_3.PARTICLE);
         Particle largeExplosionParticle = (Particle)wrapper.passthroughAndMap(VersionedTypes.V1_20_5.particle(), Types1_20_3.PARTICLE);
         ((Protocol1_20_5To1_20_3)this.protocol).getParticleRewriter().rewriteParticle(wrapper.user(), smallExplosionParticle);
         ((Protocol1_20_5To1_20_3)this.protocol).getParticleRewriter().rewriteParticle(wrapper.user(), largeExplosionParticle);
         Holder<SoundEvent> soundEventHolder = (Holder<SoundEvent>)wrapper.read(Types.SOUND_EVENT);
         if (soundEventHolder.isDirect()) {
            SoundEvent soundEvent = (SoundEvent)soundEventHolder.value();
            wrapper.write(Types.STRING, soundEvent.identifier());
            wrapper.write(Types.OPTIONAL_FLOAT, soundEvent.fixedRange());
         } else {
            int soundId = ((Protocol1_20_5To1_20_3)this.protocol).getMappingData().getSoundMappings().getNewId(soundEventHolder.id());
            String soundKey = Protocol1_20_3To1_20_5.MAPPINGS.soundName(soundId);
            wrapper.write(Types.STRING, soundKey != null ? soundKey : "minecraft:entity.generic.explode");
            wrapper.write(Types.OPTIONAL_FLOAT, null);
         }
      });
      ((Protocol1_20_5To1_20_3)this.protocol).replaceClientbound(ClientboundPackets1_20_5.MERCHANT_OFFERS, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int size = (Integer)wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            Item input = this.handleItemToClient(wrapper.user(), (Item)wrapper.read(VersionedTypes.V1_20_5.itemCost()));
            this.cleanInput(input);
            wrapper.write(Types.ITEM1_20_2, input);
            Item result = this.handleItemToClient(wrapper.user(), (Item)wrapper.read(VersionedTypes.V1_20_5.item()));
            wrapper.write(Types.ITEM1_20_2, result);
            Item secondInput = (Item)wrapper.read(VersionedTypes.V1_20_5.optionalItemCost());
            if (secondInput != null) {
               secondInput = this.handleItemToClient(wrapper.user(), secondInput);
               this.cleanInput(secondInput);
            }

            wrapper.write(Types.ITEM1_20_2, secondInput);
            wrapper.passthrough(Types.BOOLEAN);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.FLOAT);
            wrapper.passthrough(Types.INT);
         }
      });
      ((Protocol1_20_5To1_20_3)this.protocol).registerClientbound(ClientboundPackets1_20_5.MAP_ITEM_DATA, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.BOOLEAN);
         if ((Boolean)wrapper.passthrough(Types.BOOLEAN)) {
            int icons = (Integer)wrapper.passthrough(Types.VAR_INT);

            for (int i = 0; i < icons; i++) {
               int decorationType = (Integer)wrapper.read(Types.VAR_INT);
               wrapper.write(Types.VAR_INT, decorationType == 34 ? 32 : decorationType);
               wrapper.passthrough(Types.BYTE);
               wrapper.passthrough(Types.BYTE);
               wrapper.passthrough(Types.BYTE);
               wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG);
            }
         }
      });
      RecipeRewriter1_20_3<ClientboundPacket1_20_5> recipeRewriter = new RecipeRewriter1_20_3(this.protocol);
      ((Protocol1_20_5To1_20_3)this.protocol).registerClientbound(ClientboundPackets1_20_5.UPDATE_RECIPES, wrapper -> {
         int size = (Integer)wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            String recipeIdentifier = (String)wrapper.read(Types.STRING);
            int serializerTypeId = (Integer)wrapper.read(Types.VAR_INT);
            String serializerType = ((Protocol1_20_5To1_20_3)this.protocol).getMappingData().getRecipeSerializerMappings().mappedIdentifier(serializerTypeId);
            wrapper.write(Types.STRING, serializerType);
            wrapper.write(Types.STRING, recipeIdentifier);
            recipeRewriter.handleRecipeType(wrapper, Key.stripMinecraftNamespace(serializerType));
         }
      });
   }

   private void cleanInput(@Nullable Item item) {
      if (item != null && item.tag() != null) {
         CompoundTag tag = item.tag();
         StructuredDataConverter.removeBackupTag(tag);
         CompoundTag display = tag.getCompoundTag("display");
         if (display != null) {
            this.removeEmptyList(display, "Lore");
            if (display.isEmpty()) {
               tag.remove("display");
            }
         }

         this.removeEmptyList(tag, "Enchantments");
         this.removeEmptyList(tag, "AttributeModifiers");
         if (tag.getInt("RepairCost", -1) == 0) {
            tag.remove("RepairCost");
         }

         if (tag.isEmpty()) {
            item.setTag(null);
         }
      }
   }

   private void removeEmptyList(CompoundTag tag, String key) {
      ListTag<?> list = tag.getListTag(key);
      if (list != null && list.isEmpty()) {
         tag.remove(key);
      }
   }

   @Nullable
   public Item handleItemToClient(UserConnection connection, Item item) {
      if (item.isEmpty()) {
         return null;
      } else {
         StructuredDataContainer data = item.dataContainer();
         item.dataContainer().setIdLookup(this.protocol, true);
         this.enchantmentRewriter.handleToClient(item);
         item = super.handleItemToClient(connection, item);
         this.updateTextComponent(connection, item, StructuredDataKey.ITEM_NAME, "item_name");
         this.updateTextComponent(connection, item, StructuredDataKey.CUSTOM_NAME, "custom_name");
         Tag[] lore = (Tag[])data.get(StructuredDataKey.LORE);
         if (lore != null) {
            for (Tag tag : lore) {
               ((Protocol1_20_5To1_20_3)this.protocol).getComponentRewriter().processTag(connection, tag);
            }
         }

         if (item.identifier() == 1105 && !data.has(StructuredDataKey.FIREWORKS)) {
            data.set(StructuredDataKey.FIREWORKS, new Fireworks(1, new FireworkExplosion[0]));
         }

         CompoundTag customData = (CompoundTag)data.get(StructuredDataKey.CUSTOM_DATA);
         if (customData != null) {
            customData = customData.copy();
         }

         Item oldItem = this.vvProtocol.getItemRewriter().toOldItem(connection, item, DATA_CONVERTER);
         if (customData != null) {
            if (oldItem.tag() == null) {
               oldItem.setTag(new CompoundTag());
            }

            oldItem.tag().put(this.nbtTagName(), customData);
         } else if (oldItem.tag() != null && oldItem.tag().isEmpty()) {
            oldItem.setTag(null);
         }

         return oldItem;
      }
   }

   protected void handleItemDataComponentsToClient(UserConnection connection, Item item, StructuredDataContainer container) {
   }

   protected void handleItemDataComponentsToServer(UserConnection connection, Item item, StructuredDataContainer container) {
   }

   protected void handleRewritablesToClient(UserConnection connection, StructuredDataContainer container, @Nullable ItemHasher itemHasher) {
   }

   protected void handleRewritablesToServer(UserConnection connection, StructuredDataContainer container) {
   }

   @Override
   public Item handleItemToServer(UserConnection connection, @Nullable Item item) {
      if (item == null) {
         return StructuredItem.empty();
      } else {
         CompoundTag strippedTag = this.checkStoredHash(connection, item);
         Item structuredItem = this.vvProtocol.getItemRewriter().toStructuredItem(connection, item);
         if (item.tag() != null && item.tag().get(this.nbtTagName()) instanceof CompoundTag tag) {
            structuredItem.dataContainer().set(StructuredDataKey.CUSTOM_DATA, tag);
         }

         if (strippedTag != null) {
            structuredItem.dataContainer().set(StructuredDataKey.CUSTOM_DATA, strippedTag);
         }

         structuredItem.dataContainer().setIdLookup(this.protocol, false);
         this.enchantmentRewriter.handleToServer(structuredItem);
         return super.handleItemToServer(connection, structuredItem);
      }
   }

   @Nullable
   private CompoundTag checkStoredHash(UserConnection connection, Item item) {
      ProtocolInfo protocolInfo = connection.getProtocolInfo();
      CompoundTag tag = item.tag();
      if (tag != null
         && !protocolInfo.serverProtocolVersion().olderThan(ProtocolVersion.v1_21_5)
         && !((Protocol1_20_5To1_20_3)this.protocol).getEntityRewriter().tracker(connection).canInstaBuild()) {
         CompoundTag hashes = tag.getCompoundTag("VV|original_hashes");
         if (hashes == null) {
            item.setTag(null);
            return null;
         } else {
            CompoundTag strippedTag = new CompoundTag();
            strippedTag.put("VV|original_hashes", hashes);
            item.setTag(strippedTag);
            return strippedTag;
         }
      } else {
         return null;
      }
   }
}
