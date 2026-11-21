package com.viaversion.viabackwards.protocol.v1_21_5to1_21_4.rewriter;

import com.viaversion.viabackwards.api.rewriters.EntityRewriter;
import com.viaversion.viabackwards.protocol.v1_21_5to1_21_4.Protocol1_21_5To1_21_4;
import com.viaversion.viabackwards.protocol.v1_21_5to1_21_4.storage.HorseDataStorage;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.WolfVariant;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_5;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_2;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_5;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import java.util.UUID;

public final class EntityPacketRewriter1_21_5 extends EntityRewriter<ClientboundPacket1_21_5, Protocol1_21_5To1_21_4> {
   public EntityPacketRewriter1_21_5(Protocol1_21_5To1_21_4 protocol) {
      super(
         protocol,
         ((EntityDataTypes1_21_2)protocol.mappedTypes().entityDataTypes()).optionalComponentType,
         ((EntityDataTypes1_21_2)protocol.mappedTypes().entityDataTypes()).booleanType
      );
   }

   public void registerPackets() {
      ((Protocol1_21_5To1_21_4)this.protocol).replaceClientbound(ClientboundPackets1_21_5.ADD_ENTITY, wrapper -> {
         int entityId = (Integer)wrapper.passthrough(Types.VAR_INT);
         UUID uuid = (UUID)wrapper.read(Types.UUID);
         int entityType = (Integer)wrapper.read(Types.VAR_INT);
         if (entityType != EntityTypes1_21_5.EXPERIENCE_ORB.getId()) {
            wrapper.write(Types.UUID, uuid);
            wrapper.write(Types.VAR_INT, entityType);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.BYTE);
            wrapper.passthrough(Types.BYTE);
            wrapper.passthrough(Types.BYTE);
            int data = (Integer)wrapper.passthrough(Types.VAR_INT);
            this.trackSpawnWithData1_19(wrapper, data);
         } else {
            this.tracker(wrapper.user()).addEntity(entityId, EntityTypes1_21_5.EXPERIENCE_ORB);
            wrapper.setPacketType(ClientboundPackets1_21_2.ADD_EXPERIENCE_ORB);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.read(Types.BYTE);
            wrapper.read(Types.BYTE);
            wrapper.read(Types.BYTE);
            int data = (Integer)wrapper.read(Types.VAR_INT);
            wrapper.write(Types.SHORT, (short)data);
            short velocityX = (Short)wrapper.read(Types.SHORT);
            short velocityY = (Short)wrapper.read(Types.SHORT);
            short velocityZ = (Short)wrapper.read(Types.SHORT);
            if (velocityX != 0 || velocityY != 0 || velocityZ != 0) {
               PacketWrapper motionPacket = wrapper.create(ClientboundPackets1_21_2.SET_ENTITY_MOTION);
               motionPacket.write(Types.VAR_INT, entityId);
               motionPacket.write(Types.SHORT, velocityX);
               motionPacket.write(Types.SHORT, velocityY);
               motionPacket.write(Types.SHORT, velocityZ);
               wrapper.send(Protocol1_21_5To1_21_4.class);
               motionPacket.send(Protocol1_21_5To1_21_4.class);
               wrapper.cancel();
            }
         }
      });
      ((Protocol1_21_5To1_21_4)this.protocol).replaceClientbound(ClientboundPackets1_21_5.SET_PLAYER_TEAM, wrapper -> {
         wrapper.passthrough(Types.STRING);
         byte action = (Byte)wrapper.passthrough(Types.BYTE);
         if (action == 0 || action == 2) {
            ((Protocol1_21_5To1_21_4)this.protocol).getComponentRewriter().passthroughAndProcess(wrapper);
            wrapper.passthrough(Types.BYTE);
            int nametagVisibility = (Integer)wrapper.read(Types.VAR_INT);
            int collisionRule = (Integer)wrapper.read(Types.VAR_INT);
            wrapper.write(Types.STRING, this.visibility(nametagVisibility));
            wrapper.write(Types.STRING, this.collision(collisionRule));
            wrapper.passthrough(Types.VAR_INT);
            ((Protocol1_21_5To1_21_4)this.protocol).getComponentRewriter().passthroughAndProcess(wrapper);
            ((Protocol1_21_5To1_21_4)this.protocol).getComponentRewriter().passthroughAndProcess(wrapper);
         }
      });
   }

   private String visibility(int id) {
      return switch (id) {
         case 0 -> "always";
         case 1 -> "never";
         case 2 -> "hideForOtherTeams";
         case 3 -> "hideForOwnTeam";
         default -> "always";
      };
   }

   private String collision(int id) {
      return switch (id) {
         case 0 -> "always";
         case 1 -> "never";
         case 2 -> "pushOtherTeams";
         case 3 -> "pushOwnTeam";
         default -> "always";
      };
   }

   protected void registerRewrites() {
      EntityDataTypes1_21_5 entityDataTypes = (EntityDataTypes1_21_5)VersionedTypes.V1_21_5.entityDataTypes;
      EntityDataTypes1_21_2 mappedEntityDataTypes = (EntityDataTypes1_21_2)VersionedTypes.V1_21_4.entityDataTypes;
      this.dataTypeMapper()
         .removed(entityDataTypes.cowVariantType)
         .removed(entityDataTypes.wolfSoundVariantType)
         .removed(entityDataTypes.pigVariantType)
         .removed(entityDataTypes.chickenVariantType)
         .skip(entityDataTypes.wolfVariantType)
         .register();
      this.filter().dataType(entityDataTypes.wolfVariantType).handler((event, data) -> {
         int type = (Integer)data.value();
         Holder<WolfVariant> variant = Holder.of(type);
         data.setTypeAndValue(mappedEntityDataTypes.wolfVariantType, variant);
      });
      this.registerEntityDataTypeHandler1_20_3(
         mappedEntityDataTypes.itemType,
         mappedEntityDataTypes.blockStateType,
         mappedEntityDataTypes.optionalBlockStateType,
         mappedEntityDataTypes.particleType,
         mappedEntityDataTypes.particlesType,
         mappedEntityDataTypes.componentType,
         mappedEntityDataTypes.optionalComponentType
      );
      this.filter().dataType(mappedEntityDataTypes.frogVariantType).handler((event, data) -> {
         int value = (Integer)data.value();
         String variantKey = this.tracker(event.user()).registryKeys("frog_variant").idToKey(value);
         byte var10000;
         if (variantKey == null) {
            var10000 = 0;
         } else {
            switch (variantKey) {
               case "cold":
                  var10000 = 2;
                  break;
               case "temperate":
                  var10000 = 0;
                  break;
               case "warm":
                  var10000 = 1;
                  break;
               default:
                  var10000 = 0;
            }
         }

         int newValue = var10000;
         data.setValue(newValue);
      });
      this.filter().dataType(mappedEntityDataTypes.catVariantType).handler((event, data) -> {
         int value = (Integer)data.value();
         String variantKey = this.tracker(event.user()).registryKeys("cat_variant").idToKey(value);
         byte var10000;
         if (variantKey == null) {
            var10000 = 1;
         } else {
            switch (variantKey) {
               case "all_black":
                  var10000 = 10;
                  break;
               case "black":
                  var10000 = 1;
                  break;
               case "british_shorthair":
                  var10000 = 4;
                  break;
               case "calico":
                  var10000 = 5;
                  break;
               case "jellie":
                  var10000 = 9;
                  break;
               case "persian":
                  var10000 = 6;
                  break;
               case "ragdoll":
                  var10000 = 7;
                  break;
               case "red":
                  var10000 = 2;
                  break;
               case "siamese":
                  var10000 = 3;
                  break;
               case "tabby":
                  var10000 = 0;
                  break;
               case "white":
                  var10000 = 8;
                  break;
               default:
                  var10000 = 1;
            }
         }

         int newValue = var10000;
         data.setValue(newValue);
      });
      this.filter().type(EntityTypes1_21_5.ABSTRACT_MINECART).addIndex(13);
      this.filter().type(EntityTypes1_21_5.ABSTRACT_MINECART).index(11).handler((event, data) -> {
         int state = (Integer)data.getValue();
         if (state == 0) {
            event.cancel();
         } else {
            int mappedBlockState = ((Protocol1_21_5To1_21_4)this.protocol).getMappingData().getNewBlockStateId(state);
            data.setTypeAndValue(entityDataTypes.varIntType, mappedBlockState);
            event.createExtraData(new EntityData(13, mappedEntityDataTypes.booleanType, true));
         }
      });
      this.filter().type(EntityTypes1_21_5.MOOSHROOM).index(17).handler((event, data) -> {
         int typeId = (Integer)data.value();
         String typeName = typeId == 0 ? "red" : "brown";
         data.setTypeAndValue(entityDataTypes.stringType, typeName);
      });
      this.filter().type(EntityTypes1_21_5.ABSTRACT_HORSE).index(17).handler((event, data) -> {
         TrackedEntity entity = event.trackedEntity();
         byte horseData = (Byte)data.value();
         boolean saddled = false;
         HorseDataStorage horseDataStorage = (HorseDataStorage)entity.get(HorseDataStorage.class);
         if (horseDataStorage != null && horseDataStorage.saddled()) {
            saddled = true;
            data.setValue((byte)(horseData | 4));
         }

         entity.put(new HorseDataStorage(horseData, saddled));
      });
      this.filter().type(EntityTypes1_21_5.CHICKEN).cancel(17);
      this.filter().type(EntityTypes1_21_5.COW).cancel(17);
      this.filter().type(EntityTypes1_21_5.PIG).cancel(18);
      this.filter().type(EntityTypes1_21_5.WOLF).cancel(23);
      this.filter().type(EntityTypes1_21_5.EXPERIENCE_ORB).cancel(8);
      this.filter().type(EntityTypes1_21_5.DOLPHIN).addIndex(17);
      this.filter().type(EntityTypes1_21_5.TURTLE).addIndex(17);
      this.filter().type(EntityTypes1_21_5.PIG).addIndex(17);
      this.filter().type(EntityTypes1_21_5.STRIDER).addIndex(19);
   }

   public EntityType typeFromId(int type) {
      return EntityTypes1_21_5.getTypeFromId(type);
   }
}
