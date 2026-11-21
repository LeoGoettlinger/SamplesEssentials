package com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.WolfVariant;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_5;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.storage.MessageIndexStorage;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandlerEvent;
import com.viaversion.viaversion.util.UUIDUtil;
import java.util.concurrent.ThreadLocalRandom;

public final class EntityPacketRewriter1_21_5 extends EntityRewriter<ClientboundPacket1_21_2, Protocol1_21_4To1_21_5> {
   private static final int ATTACK_BLOCKED_ENTITY_EVENT = 29;
   private static final int SHIELD_DISABLED_ENTITY_EVENT = 30;
   private static final int SADDLE_ITEM_ID = 800;
   private static final byte SADDLE_EQUIPMENT_SLOT = 7;

   public EntityPacketRewriter1_21_5(Protocol1_21_4To1_21_5 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      this.protocol.registerClientbound(ClientboundPackets1_21_2.ADD_EXPERIENCE_ORB, ClientboundPackets1_21_5.ADD_ENTITY, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.write(Types.UUID, UUIDUtil.randomUUID());
         wrapper.write(Types.VAR_INT, EntityTypes1_21_5.EXPERIENCE_ORB.getId());
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.write(Types.BYTE, (byte)0);
         wrapper.write(Types.BYTE, (byte)0);
         wrapper.write(Types.BYTE, (byte)0);
         wrapper.passthroughAndMap(Types.SHORT, Types.VAR_INT);
         wrapper.write(Types.SHORT, (short)0);
         wrapper.write(Types.SHORT, (short)0);
         wrapper.write(Types.SHORT, (short)0);
      });
      this.protocol
         .appendClientbound(
            ClientboundConfigurationPackets1_21.FINISH_CONFIGURATION,
            wrapper -> {
               this.sendEntityVariants(wrapper.user(), "minecraft:frog_variant", "frog", true, "temperate", "warm", "cold");
               this.sendEntityVariants(
                  wrapper.user(),
                  "minecraft:cat_variant",
                  "cat",
                  false,
                  "tabby",
                  "black",
                  "red",
                  "siamese",
                  "british_shorthair",
                  "calico",
                  "persian",
                  "ragdoll",
                  "white",
                  "jellie",
                  "all_black"
               );
               this.sendEntityVariants(wrapper.user(), "minecraft:pig_variant", "pig", true, "temperate");
               this.sendEntityVariants(wrapper.user(), "minecraft:cow_variant", "cow", true, "temperate");
               this.sendEntityVariants(wrapper.user(), "minecraft:chicken_variant", "chicken", true, "temperate");
               PacketWrapper wolfSoundVariantsPacket = PacketWrapper.create(ClientboundConfigurationPackets1_21.REGISTRY_DATA, wrapper.user());
               wolfSoundVariantsPacket.write(Types.STRING, "minecraft:wolf_sound_variant");
               wolfSoundVariantsPacket.write(Types.REGISTRY_ENTRY_ARRAY, new RegistryEntry[]{this.wolfSoundVariant()});
               wolfSoundVariantsPacket.send(Protocol1_21_4To1_21_5.class);
            }
         );
      this.protocol.appendClientbound(ClientboundPackets1_21_2.LOGIN, wrapper -> wrapper.user().get(MessageIndexStorage.class).setIndex(0));
      this.protocol.registerClientbound(ClientboundPackets1_21_2.UPDATE_MOB_EFFECT, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int effectId = wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         byte flags = wrapper.read(Types.BYTE);
         if (effectId == 8) {
            flags = (byte)(flags | 8);
         }

         wrapper.write(Types.BYTE, flags);
      });
      this.protocol.replaceClientbound(ClientboundPackets1_21_2.SET_PLAYER_TEAM, wrapper -> {
         wrapper.passthrough(Types.STRING);
         byte action = wrapper.passthrough(Types.BYTE);
         if (action == 0 || action == 2) {
            this.protocol.getComponentRewriter().passthroughAndProcess(wrapper);
            wrapper.passthrough(Types.BYTE);
            String nametagVisibility = wrapper.read(Types.STRING);
            String collisionRule = wrapper.read(Types.STRING);
            wrapper.write(Types.VAR_INT, this.visibilityId(nametagVisibility));
            wrapper.write(Types.VAR_INT, this.collisionId(collisionRule));
            wrapper.passthrough(Types.VAR_INT);
            this.protocol.getComponentRewriter().passthroughAndProcess(wrapper);
            this.protocol.getComponentRewriter().passthroughAndProcess(wrapper);
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_21_2.ENTITY_EVENT, wrapper -> {
         int entityId = wrapper.read(Types.INT);
         byte event = wrapper.read(Types.BYTE);
         if (event == 29) {
            this.playShieldSound(wrapper, entityId, 1273, 1.0F);
         } else if (event == 30) {
            this.playShieldSound(wrapper, entityId, 1274, 0.8F);
         } else {
            wrapper.write(Types.INT, entityId);
            wrapper.write(Types.BYTE, event);
         }
      });
   }

   private void playShieldSound(PacketWrapper wrapper, int entityId, int soundId, float volume) {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      wrapper.setPacketType(ClientboundPackets1_21_5.SOUND_ENTITY);
      wrapper.write(Types.SOUND_EVENT, Holder.of(soundId));
      wrapper.write(Types.VAR_INT, 7);
      wrapper.write(Types.VAR_INT, entityId);
      wrapper.write(Types.FLOAT, volume);
      wrapper.write(Types.FLOAT, 0.8F + random.nextFloat() * 0.4F);
      wrapper.write(Types.LONG, random.nextLong());
   }

   private RegistryEntry wolfSoundVariant() {
      CompoundTag classicWolfSoundVariant = new CompoundTag();
      classicWolfSoundVariant.putString("ambient_sound", "entity.wolf.ambient");
      classicWolfSoundVariant.putString("death_sound", "entity.wolf.death");
      classicWolfSoundVariant.putString("growl_sound", "entity.wolf.growl");
      classicWolfSoundVariant.putString("hurt_sound", "entity.wolf.hurt");
      classicWolfSoundVariant.putString("pant_sound", "entity.wolf.pant");
      classicWolfSoundVariant.putString("whine_sound", "entity.wolf.whine");
      return new RegistryEntry("classic", classicWolfSoundVariant);
   }

   private int collisionId(String collisionRule) {
      return switch (collisionRule) {
         case "always" -> 0;
         case "never" -> 1;
         case "pushOtherTeams" -> 2;
         case "pushOwnTeam" -> 3;
         default -> 0;
      };
   }

   private int visibilityId(String visibilityRule) {
      return switch (visibilityRule) {
         case "always" -> 0;
         case "never" -> 1;
         case "hideForOtherTeams" -> 2;
         case "hideForOwnTeam" -> 3;
         default -> 0;
      };
   }

   private void sendEntityVariants(UserConnection connection, String key, String entityName, boolean suffixedWithOwnName, String... entryKeys) {
      PacketWrapper variantsPacket = PacketWrapper.create(ClientboundConfigurationPackets1_21.REGISTRY_DATA, connection);
      variantsPacket.write(Types.STRING, key);
      RegistryEntry[] entries = new RegistryEntry[entryKeys.length];

      for (int i = 0; i < entryKeys.length; i++) {
         CompoundTag tag = new CompoundTag();
         String assetId = "entity/" + entityName + "/" + entryKeys[i];
         if (suffixedWithOwnName) {
            assetId = assetId + "_" + entityName;
         }

         tag.putString("asset_id", assetId);
         entries[i] = new RegistryEntry(entryKeys[i], tag);
      }

      variantsPacket.write(Types.REGISTRY_ENTRY_ARRAY, entries);
      variantsPacket.send(Protocol1_21_4To1_21_5.class);
   }

   @Override
   protected void registerRewrites() {
      this.dataTypeMapper()
         .added(VersionedTypes.V1_21_5.entityDataTypes.cowVariantType)
         .added(VersionedTypes.V1_21_5.entityDataTypes.wolfSoundVariantType)
         .added(VersionedTypes.V1_21_5.entityDataTypes.pigVariantType)
         .added(VersionedTypes.V1_21_5.entityDataTypes.chickenVariantType)
         .skip(VersionedTypes.V1_21_4.entityDataTypes.wolfVariantType)
         .register();
      this.filter().dataType(VersionedTypes.V1_21_4.entityDataTypes.wolfVariantType).handler((event, data) -> {
         Holder<WolfVariant> wolfVariant = data.value();
         data.setTypeAndValue(VersionedTypes.V1_21_5.entityDataTypes.wolfVariantType, wolfVariant.hasId() ? wolfVariant.id() : 0);
      });
      this.registerEntityDataTypeHandler(
         VersionedTypes.V1_21_5.entityDataTypes.itemType,
         VersionedTypes.V1_21_5.entityDataTypes.blockStateType,
         VersionedTypes.V1_21_5.entityDataTypes.optionalBlockStateType,
         VersionedTypes.V1_21_5.entityDataTypes.particleType,
         VersionedTypes.V1_21_5.entityDataTypes.particlesType,
         VersionedTypes.V1_21_5.entityDataTypes.componentType,
         VersionedTypes.V1_21_5.entityDataTypes.optionalComponentType
      );
      this.filter().type(EntityTypes1_21_5.ABSTRACT_MINECART).index(11).handler((event, data) -> {
         int state = (Integer)data.getValue();
         int mappedBlockState = this.protocol.getMappingData().getNewBlockStateId(state);
         if (mappedBlockState == 0) {
            event.cancel();
         } else {
            data.setTypeAndValue(VersionedTypes.V1_21_5.entityDataTypes.optionalBlockStateType, mappedBlockState);
         }
      });
      this.filter().type(EntityTypes1_21_5.ABSTRACT_MINECART).removeIndex(13);
      this.filter().type(EntityTypes1_21_5.MOOSHROOM).index(17).handler((event, data) -> {
         String typeName = data.value();
         int typeId = typeName.equals("red") ? 0 : 1;
         data.setTypeAndValue(VersionedTypes.V1_21_5.entityDataTypes.varIntType, typeId);
      });
      this.filter().type(EntityTypes1_21_5.PIG).index(17).handler((event, data) -> {
         boolean saddled = data.<Boolean>value();
         this.sendSaddleEquipment(event, saddled);
      });
      this.filter().type(EntityTypes1_21_5.PIG).removeIndex(17);
      this.filter().type(EntityTypes1_21_5.STRIDER).index(19).handler((event, data) -> {
         event.cancel();
         boolean saddled = data.<Boolean>value();
         this.sendSaddleEquipment(event, saddled);
      });
      this.filter().type(EntityTypes1_21_5.ABSTRACT_HORSE).index(17).handler((event, data) -> {
         byte flags = data.<Byte>value();
         this.sendSaddleEquipment(event, (flags & 4) != 0);
      });
      this.filter().type(EntityTypes1_21_5.DOLPHIN).removeIndex(17);
      this.filter().type(EntityTypes1_21_5.TURTLE).cancel(22);
      this.filter().type(EntityTypes1_21_5.TURTLE).cancel(21);
      this.filter().type(EntityTypes1_21_5.TURTLE).cancel(20);
      this.filter().type(EntityTypes1_21_5.TURTLE).removeIndex(17);
   }

   private void sendSaddleEquipment(EntityDataHandlerEvent event, boolean saddled) {
      PacketWrapper equipmentPacket = PacketWrapper.create(ClientboundPackets1_21_5.SET_EQUIPMENT, event.user());
      equipmentPacket.write(Types.VAR_INT, event.entityId());
      equipmentPacket.write(Types.BYTE, (byte)7);
      equipmentPacket.write(VersionedTypes.V1_21_5.item, saddled ? new StructuredItem(800, 1) : StructuredItem.empty());
      equipmentPacket.send(Protocol1_21_4To1_21_5.class);
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes1_21_5.getTypeFromId(type);
   }
}
