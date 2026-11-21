package com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_4;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_19_3;
import com.viaversion.viaversion.api.type.types.version.Types1_19_4;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.Protocol1_19_3To1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.storage.PlayerVehicleTracker;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.storage.ProtocolStorables1_19_4;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.TagUtil;

public final class EntityPacketRewriter1_19_4 extends EntityRewriter<ClientboundPackets1_19_3, Protocol1_19_3To1_19_4> {
   public EntityPacketRewriter1_19_4(Protocol1_19_3To1_19_4 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      this.protocol.registerClientbound(ClientboundPackets1_19_3.LOGIN, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.INT);
            this.map(Types.BOOLEAN);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.STRING_ARRAY);
            this.map(Types.NAMED_COMPOUND_TAG);
            this.map(Types.STRING);
            this.map(Types.STRING);
            this.handler(EntityPacketRewriter1_19_4.this.dimensionDataHandler());
            this.handler(EntityPacketRewriter1_19_4.this.biomeSizeTracker());
            this.handler(EntityPacketRewriter1_19_4.this.worldDataTrackerHandlerByKey());
            this.handler(EntityPacketRewriter1_19_4.this.playerTrackerHandler());
            this.handler(wrapper -> {
               CompoundTag registry = wrapper.get(Types.NAMED_COMPOUND_TAG, 0);
               CompoundTag damageTypeRegistry = EntityPacketRewriter1_19_4.this.protocol.getMappingData().damageTypesRegistry();
               registry.put("minecraft:damage_type", damageTypeRegistry);
               CompoundTag trimMaterialRegistry = new CompoundTag();
               trimMaterialRegistry.putString("type", "minecraft:trim_material");
               trimMaterialRegistry.put("value", new ListTag<>(CompoundTag.class));
               registry.put("minecraft:trim_material", trimMaterialRegistry);

               for (CompoundTag biomeTag : TagUtil.getRegistryEntries(registry, "worldgen/biome")) {
                  CompoundTag biomeData = biomeTag.getCompoundTag("element");
                  StringTag precipitation = biomeData.getStringTag("precipitation");
                  byte precipitationByte = (byte)(precipitation.getValue().equals("none") ? 0 : 1);
                  biomeData.put("has_precipitation", new ByteTag(precipitationByte));
               }
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_19_3.PLAYER_POSITION, new PacketHandlers() {
         @Override
         protected void register() {
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.map(Types.BYTE);
            this.map(Types.VAR_INT);
            this.handler(wrapper -> {
               if (wrapper.read(Types.BOOLEAN)) {
                  ProtocolStorables1_19_4 storables = wrapper.user().storables(EntityPacketRewriter1_19_4.this.protocol);
                  PlayerVehicleTracker playerVehicleTracker = storables.playerVehicleTracker();
                  if (playerVehicleTracker.getVehicleId() != -1) {
                     PacketWrapper bundleStart = wrapper.create(ClientboundPackets1_19_4.BUNDLE_DELIMITER);
                     bundleStart.send(Protocol1_19_3To1_19_4.class);
                     PacketWrapper setPassengers = wrapper.create(ClientboundPackets1_19_4.SET_PASSENGERS);
                     setPassengers.write(Types.VAR_INT, playerVehicleTracker.getVehicleId());
                     setPassengers.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[0]);
                     setPassengers.send(Protocol1_19_3To1_19_4.class);
                     wrapper.send(Protocol1_19_3To1_19_4.class);
                     wrapper.cancel();
                     PacketWrapper bundleEnd = wrapper.create(ClientboundPackets1_19_4.BUNDLE_DELIMITER);
                     bundleEnd.send(Protocol1_19_3To1_19_4.class);
                     playerVehicleTracker.setVehicleId(-1);
                  }
               }
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_19_3.SET_PASSENGERS, new PacketHandlers() {
         @Override
         protected void register() {
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT_ARRAY_PRIMITIVE);
            this.handler(wrapper -> {
               ProtocolStorables1_19_4 storables = wrapper.user().storables(EntityPacketRewriter1_19_4.this.protocol);
               PlayerVehicleTracker playerVehicleTracker = storables.playerVehicleTracker();
               int clientEntityId = wrapper.user().<EntityTracker>getEntityTracker(EntityPacketRewriter1_19_4.this.protocol).clientEntityId();
               int vehicleId = wrapper.get(Types.VAR_INT, 0);
               if (playerVehicleTracker.getVehicleId() == vehicleId) {
                  playerVehicleTracker.setVehicleId(-1);
               }

               int[] passengerIds = wrapper.get(Types.VAR_INT_ARRAY_PRIMITIVE, 0);

               for (int passengerId : passengerIds) {
                  if (passengerId == clientEntityId) {
                     playerVehicleTracker.setVehicleId(vehicleId);
                     break;
                  }
               }
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_19_3.TELEPORT_ENTITY, new PacketHandlers() {
         @Override
         protected void register() {
            this.handler(wrapper -> {
               int entityId = wrapper.read(Types.VAR_INT);
               int clientEntityId = wrapper.user().<EntityTracker>getEntityTracker(EntityPacketRewriter1_19_4.this.protocol).clientEntityId();
               if (entityId != clientEntityId) {
                  wrapper.write(Types.VAR_INT, entityId);
               } else {
                  wrapper.setPacketType(ClientboundPackets1_19_4.PLAYER_POSITION);
                  wrapper.passthrough(Types.DOUBLE);
                  wrapper.passthrough(Types.DOUBLE);
                  wrapper.passthrough(Types.DOUBLE);
                  wrapper.write(Types.FLOAT, wrapper.read(Types.BYTE).byteValue() * 360.0F / 256.0F);
                  wrapper.write(Types.FLOAT, wrapper.read(Types.BYTE).byteValue() * 360.0F / 256.0F);
                  wrapper.read(Types.BOOLEAN);
                  wrapper.write(Types.BYTE, (byte)0);
                  wrapper.write(Types.VAR_INT, -1);
               }
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_19_3.ANIMATE, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.handler(wrapper -> {
               short action = wrapper.read(Types.UNSIGNED_BYTE);
               if (action != 1) {
                  wrapper.write(Types.UNSIGNED_BYTE, action);
               } else {
                  wrapper.setPacketType(ClientboundPackets1_19_4.HURT_ANIMATION);
                  wrapper.write(Types.FLOAT, 0.0F);
               }
            });
         }
      });
      this.protocol
         .registerClientbound(
            ClientboundPackets1_19_3.RESPAWN,
            new PacketHandlers() {
               @Override
               public void register() {
                  this.map(Types.STRING);
                  this.map(Types.STRING);
                  this.handler(EntityPacketRewriter1_19_4.this.worldDataTrackerHandlerByKey());
                  this.handler(
                     wrapper -> wrapper.user()
                        .<ProtocolStorables1_19_4>storables(EntityPacketRewriter1_19_4.this.protocol)
                        .setPlayerVehicleTracker(new PlayerVehicleTracker())
                  );
               }
            }
         );
      this.protocol.registerClientbound(ClientboundPackets1_19_3.ENTITY_EVENT, wrapper -> {
         int entityId = wrapper.read(Types.INT);
         byte event = wrapper.read(Types.BYTE);
         int damageType = this.damageTypeFromEntityEvent(event);
         if (damageType != -1) {
            wrapper.setPacketType(ClientboundPackets1_19_4.DAMAGE_EVENT);
            wrapper.write(Types.VAR_INT, entityId);
            wrapper.write(Types.VAR_INT, damageType);
            wrapper.write(Types.OPTIONAL_VAR_INT, null);
            wrapper.write(Types.OPTIONAL_VAR_INT, null);
            wrapper.write(Types.BOOLEAN, false);
         } else {
            wrapper.write(Types.INT, entityId);
            wrapper.write(Types.BYTE, event);
         }
      });
      this.registerSetEntityData(ClientboundPackets1_19_3.SET_ENTITY_DATA, Types1_19_3.ENTITY_DATA_LIST, Types1_19_4.ENTITY_DATA_LIST);
   }

   private int damageTypeFromEntityEvent(byte entityEvent) {
      return switch (entityEvent) {
         case 2, 44 -> 16;
         case 33 -> 36;
         case 36 -> 5;
         case 37 -> 27;
         case 57 -> 15;
         default -> -1;
      };
   }

   @Override
   protected void registerRewrites() {
      this.filter().mapDataType(typeId -> Types1_19_4.ENTITY_DATA_TYPES.byId(typeId >= 14 ? typeId + 1 : typeId));
      this.registerEntityDataTypeHandler(
         Types1_19_4.ENTITY_DATA_TYPES.itemType,
         Types1_19_4.ENTITY_DATA_TYPES.blockStateType,
         Types1_19_4.ENTITY_DATA_TYPES.optionalBlockStateType,
         Types1_19_4.ENTITY_DATA_TYPES.particleType,
         null
      );
      this.registerBlockStateHandler(EntityTypes1_19_4.ABSTRACT_MINECART, 11);
      this.filter().type(EntityTypes1_19_4.BOAT).index(11).handler((event, data) -> {
         int boatType = data.<Integer>value();
         if (boatType > 4) {
            data.setValue(boatType + 1);
         }
      });
      this.filter().type(EntityTypes1_19_4.ABSTRACT_HORSE).removeIndex(18);
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes1_19_4.getTypeFromId(type);
   }
}
