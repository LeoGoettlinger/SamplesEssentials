package com.viaversion.viaversion.protocols.v1_8to1_9.rewriter;

import com.google.common.collect.ImmutableList;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.EulerAngle;
import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_9;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_8;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.data.EntityDataIndex1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.EntityTracker1_9;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandlerEvent;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.Triple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

public class EntityPacketRewriter1_9 extends EntityRewriter<ClientboundPackets1_8, Protocol1_8To1_9> {
   public static final ValueTransformer<Byte, Short> toNewShort = new ValueTransformer<Byte, Short>(Types.SHORT) {
      public Short transform(PacketWrapper wrapper, Byte inputValue) {
         return (short)(inputValue * 128);
      }
   };

   public EntityPacketRewriter1_9(Protocol1_8To1_9 protocol) {
      super(protocol);
   }

   @Override
   protected void registerPackets() {
      this.protocol.registerClientbound(ClientboundPackets1_8.SET_ENTITY_LINK, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.INT);
            this.map(Types.INT);
            this.handler(wrapper -> {
               short leashState = wrapper.read(Types.UNSIGNED_BYTE);
               EntityTracker1_9 tracker = wrapper.user().getEntityTracker(EntityPacketRewriter1_9.this.protocol);
               if (leashState == 0) {
                  int passenger = wrapper.get(Types.INT, 0);
                  int vehicle = wrapper.get(Types.INT, 1);
                  wrapper.cancel();
                  PacketWrapper passengerPacket = wrapper.create(ClientboundPackets1_9.SET_PASSENGERS);
                  if (vehicle == -1) {
                     if (!tracker.getVehicleMap().containsKey(passenger)) {
                        return;
                     }

                     passengerPacket.write(Types.VAR_INT, tracker.getVehicleMap().remove(passenger));
                     passengerPacket.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[0]);
                  } else {
                     passengerPacket.write(Types.VAR_INT, vehicle);
                     passengerPacket.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[]{passenger});
                     tracker.getVehicleMap().put(passenger, vehicle);
                  }

                  passengerPacket.send(Protocol1_8To1_9.class);
               }
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_8.TELEPORT_ENTITY, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.INT, SpawnPacketRewriter1_9.toNewDouble);
            this.map(Types.INT, SpawnPacketRewriter1_9.toNewDouble);
            this.map(Types.INT, SpawnPacketRewriter1_9.toNewDouble);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.BOOLEAN);
            this.handler(wrapper -> {
               int entityID = wrapper.get(Types.VAR_INT, 0);
               if (Via.getConfig().isHologramPatch()) {
                  EntityTracker1_9 tracker = wrapper.user().getEntityTracker(EntityPacketRewriter1_9.this.protocol);
                  if (tracker.getKnownHolograms().contains(entityID)) {
                     Double newValue = wrapper.get(Types.DOUBLE, 1);
                     newValue = newValue + Via.getConfig().getHologramYOffset();
                     wrapper.set(Types.DOUBLE, 1, newValue);
                  }
               }
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_8.MOVE_ENTITY_POS_ROT, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.BYTE, EntityPacketRewriter1_9.toNewShort);
            this.map(Types.BYTE, EntityPacketRewriter1_9.toNewShort);
            this.map(Types.BYTE, EntityPacketRewriter1_9.toNewShort);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.BOOLEAN);
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_8.MOVE_ENTITY_POS, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.BYTE, EntityPacketRewriter1_9.toNewShort);
            this.map(Types.BYTE, EntityPacketRewriter1_9.toNewShort);
            this.map(Types.BYTE, EntityPacketRewriter1_9.toNewShort);
            this.map(Types.BOOLEAN);
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_8.SET_EQUIPPED_ITEM, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.SHORT, new ValueTransformer<Short, Integer>(Types.VAR_INT) {
               public Integer transform(PacketWrapper wrapper, Short slot) {
                  int entityId = wrapper.get(Types.VAR_INT, 0);
                  int receiverId = wrapper.user().<EntityTracker>getEntityTracker(EntityPacketRewriter1_9.this.protocol).clientEntityId();
                  if (slot >= 0 && slot <= 4 && (entityId != receiverId || slot <= 3)) {
                     return entityId == receiverId ? slot.intValue() + 2 : slot > 0 ? slot.intValue() + 1 : slot.intValue();
                  } else {
                     wrapper.cancel();
                     return 0;
                  }
               }
            });
            this.map(Types.ITEM1_8);
            this.handler(wrapper -> {
               Item stack = wrapper.get(Types.ITEM1_8, 0);
               EntityPacketRewriter1_9.this.protocol.getItemRewriter().handleItemToClient(wrapper.user(), stack);
            });
            this.handler(wrapper -> {
               EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(EntityPacketRewriter1_9.this.protocol);
               int entityID = wrapper.get(Types.VAR_INT, 0);
               Item stack = wrapper.get(Types.ITEM1_8, 0);
               if (stack != null && Protocol1_8To1_9.isSword(stack.identifier())) {
                  entityTracker.getValidBlocking().add(entityID);
               } else {
                  entityTracker.getValidBlocking().remove(entityID);
               }
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_8.SET_ENTITY_DATA, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.ENTITY_DATA_LIST1_8, Types.ENTITY_DATA_LIST1_9);
            this.handler(wrapper -> {
               List<EntityData> entityDataList = wrapper.get(Types.ENTITY_DATA_LIST1_9, 0);
               int entityId = wrapper.get(Types.VAR_INT, 0);
               EntityTracker1_9 tracker = wrapper.user().getEntityTracker(EntityPacketRewriter1_9.this.protocol);
               if (tracker.hasEntity(entityId)) {
                  EntityPacketRewriter1_9.this.handleEntityData(entityId, entityDataList, wrapper.user());
               } else {
                  wrapper.cancel();
               }
            });
            this.handler(wrapper -> {
               List<EntityData> entityDataList = wrapper.get(Types.ENTITY_DATA_LIST1_9, 0);
               int entityID = wrapper.get(Types.VAR_INT, 0);
               EntityTracker1_9 tracker = wrapper.user().getEntityTracker(EntityPacketRewriter1_9.this.protocol);
               tracker.handleEntityData(entityID, entityDataList);
            });
            this.handler(wrapper -> {
               List<EntityData> entityDataList = wrapper.get(Types.ENTITY_DATA_LIST1_9, 0);
               if (entityDataList.isEmpty()) {
                  wrapper.cancel();
               }
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_8.UPDATE_MOB_EFFECT, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.VAR_INT);
            this.handler(wrapper -> {
               boolean showParticles = wrapper.read(Types.BOOLEAN);
               boolean newEffect = Via.getConfig().isNewEffectIndicator();
               wrapper.write(Types.BYTE, (byte)(showParticles ? (newEffect ? 2 : 1) : 0));
            });
         }
      });
      this.protocol.cancelClientbound(ClientboundPackets1_8.UPDATE_ENTITY_NBT);
      this.protocol.registerClientbound(ClientboundPackets1_8.PLAYER_COMBAT, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.handler(wrapper -> {
               if (wrapper.get(Types.VAR_INT, 0) == 2) {
                  wrapper.passthrough(Types.VAR_INT);
                  wrapper.passthrough(Types.INT);
                  Protocol1_8To1_9.STRING_TO_JSON.write(wrapper, wrapper.read(Types.STRING));
               }
            });
         }
      });
      this.protocol
         .registerClientbound(
            ClientboundPackets1_8.UPDATE_ATTRIBUTES,
            new PacketHandlers() {
               @Override
               public void register() {
                  this.map(Types.VAR_INT);
                  this.handler(
                     wrapper -> {
                        EntityTracker1_9 tracker = wrapper.user().getEntityTracker(EntityPacketRewriter1_9.this.protocol);
                        if (wrapper.get(Types.VAR_INT, 0) == tracker.getProvidedEntityId()) {
                           int propertiesToRead = wrapper.read(Types.INT);
                           Map<String, Pair<Double, List<Triple<UUID, Double, Byte>>>> properties = new HashMap<>(propertiesToRead);

                           for (int i = 0; i < propertiesToRead; i++) {
                              String key = wrapper.read(Types.STRING);
                              Double value = wrapper.read(Types.DOUBLE);
                              int modifiersToRead = wrapper.read(Types.VAR_INT);
                              List<Triple<UUID, Double, Byte>> modifiers = new ArrayList<>(modifiersToRead);

                              for (int j = 0; j < modifiersToRead; j++) {
                                 modifiers.add(new Triple<>(wrapper.read(Types.UUID), wrapper.read(Types.DOUBLE), wrapper.read(Types.BYTE)));
                              }

                              properties.put(key, new Pair<>(value, modifiers));
                           }

                           properties.put(
                              "generic.attackSpeed",
                              new Pair<>(
                                 20.0,
                                 ImmutableList.of(
                                    new Triple<>(UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3"), 0.0, (byte)0),
                                    new Triple<>(UUID.fromString("AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3"), 0.0, (byte)2),
                                    new Triple<>(UUID.fromString("55FCED67-E92A-486E-9800-B47F202C4386"), 0.0, (byte)2)
                                 )
                              )
                           );
                           wrapper.write(Types.INT, properties.size());

                           for (Entry<String, Pair<Double, List<Triple<UUID, Double, Byte>>>> entry : properties.entrySet()) {
                              wrapper.write(Types.STRING, entry.getKey());
                              wrapper.write(Types.DOUBLE, entry.getValue().key());
                              wrapper.write(Types.VAR_INT, entry.getValue().value().size());

                              for (Triple<UUID, Double, Byte> modifier : entry.getValue().value()) {
                                 wrapper.write(Types.UUID, modifier.first());
                                 wrapper.write(Types.DOUBLE, modifier.second());
                                 wrapper.write(Types.BYTE, modifier.third());
                              }
                           }
                        }
                     }
                  );
               }
            }
         );
      this.protocol.registerClientbound(ClientboundPackets1_8.ANIMATE, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UNSIGNED_BYTE);
            this.handler(wrapper -> {
               if (wrapper.get(Types.UNSIGNED_BYTE, 0) == 3) {
                  wrapper.cancel();
               }
            });
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_9.PLAYER_COMMAND, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.handler(wrapper -> {
               int action = wrapper.get(Types.VAR_INT, 1);
               if (action == 6 || action == 8) {
                  wrapper.cancel();
               }

               if (action == 7) {
                  wrapper.set(Types.VAR_INT, 1, 6);
               }
            });
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_9.INTERACT, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.handler(wrapper -> {
               int type = wrapper.get(Types.VAR_INT, 1);
               if (type == 2) {
                  wrapper.passthrough(Types.FLOAT);
                  wrapper.passthrough(Types.FLOAT);
                  wrapper.passthrough(Types.FLOAT);
               }

               if (type == 0 || type == 2) {
                  int hand = wrapper.read(Types.VAR_INT);
                  if (hand == 1) {
                     wrapper.cancel();
                  }
               }
            });
         }
      });
   }

   @Override
   protected void registerRewrites() {
      this.filter().handler(this::handleEntityData);
   }

   private void handleEntityData(EntityDataHandlerEvent event, EntityData data) {
      EntityType type = event.entityType();
      EntityDataIndex1_9 dataIndex = EntityDataIndex1_9.searchIndex(type, data.id());
      if (dataIndex == null) {
         event.cancel();
      } else if (dataIndex.getNewType() == null) {
         event.cancel();
      } else {
         data.setId(dataIndex.getNewIndex());
         data.setDataTypeUnsafe(dataIndex.getNewType());
         Object value = data.getValue();
         switch (dataIndex.getNewType()) {
            case BYTE:
               if (dataIndex.getOldType() == EntityDataTypes1_8.BYTE) {
                  data.setValue(value);
               }

               if (dataIndex.getOldType() == EntityDataTypes1_8.INT) {
                  data.setValue(((Integer)value).byteValue());
               }

               if (dataIndex == EntityDataIndex1_9.ENTITY_STATUS && type == EntityTypes1_9.EntityType.PLAYER) {
                  byte val = 0;
                  if (((Byte)value & 16) == 16) {
                     val = 1;
                  }

                  int newIndex = EntityDataIndex1_9.PLAYER_HAND.getNewIndex();
                  EntityDataType dataType = EntityDataIndex1_9.PLAYER_HAND.getNewType();
                  event.createExtraData(new EntityData(newIndex, dataType, val));
               }
               break;
            case OPTIONAL_UUID:
               String owner = (String)value;
               UUID toWrite = null;
               if (!owner.isEmpty()) {
                  try {
                     toWrite = UUID.fromString(owner);
                  } catch (Exception var9) {
                  }
               }

               data.setValue(toWrite);
               break;
            case VAR_INT:
               if (dataIndex.getOldType() == EntityDataTypes1_8.BYTE) {
                  data.setValue(((Byte)value).intValue());
               }

               if (dataIndex.getOldType() == EntityDataTypes1_8.SHORT) {
                  data.setValue(((Short)value).intValue());
               }

               if (dataIndex.getOldType() == EntityDataTypes1_8.INT) {
                  data.setValue(value);
               }
               break;
            case FLOAT:
            case STRING:
               data.setValue(value);
               break;
            case BOOLEAN:
               if (dataIndex == EntityDataIndex1_9.ABSTRACT_AGEABLE_AGE) {
                  data.setValue((Byte)value < 0);
               } else {
                  data.setValue((Byte)value != 0);
               }
               break;
            case ITEM:
               data.setValue(value);
               this.protocol.getItemRewriter().handleItemToClient(event.user(), (Item)data.getValue());
               break;
            case BLOCK_POSITION:
               Vector vector = (Vector)value;
               data.setValue(vector);
               break;
            case ROTATIONS:
               EulerAngle angle = (EulerAngle)value;
               data.setValue(angle);
               break;
            case COMPONENT:
               String text = (String)value;
               data.setValue(ComponentUtil.convertJsonOrEmpty(text, SerializerVersion.V1_8, SerializerVersion.V1_9));
               break;
            case OPTIONAL_BLOCK_STATE:
               data.setValue(((Number)value).intValue());
               break;
            default:
               throw new RuntimeException("Unhandled EntityDataType: " + dataIndex.getNewType());
         }
      }
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes1_9.EntityType.findById(type);
   }

   @Override
   public EntityType objectTypeFromId(int type, int data) {
      return EntityTypes1_9.ObjectType.getEntityType(type, data);
   }
}
