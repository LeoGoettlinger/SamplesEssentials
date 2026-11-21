package com.viaversion.viaversion.protocols.v1_15_2to1_16.rewriter;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.WorldIdentifiers;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.api.type.types.version.Types1_16;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packet.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.Protocol1_15_2To1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.data.AttributeMappings1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.data.DimensionRegistries1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ServerboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.storage.ProtocolStorables1_16;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.UUIDUtil;

public class EntityPacketRewriter1_16 extends EntityRewriter<ClientboundPackets1_15, Protocol1_15_2To1_16> {
   private final PacketHandler DIMENSION_HANDLER = wrapper -> {
      WorldIdentifiers map = Via.getConfig().get1_16WorldNamesMap();
      WorldIdentifiers userMap = wrapper.user().get(WorldIdentifiers.class);
      if (userMap != null) {
         map = userMap;
      }

      int dimension = wrapper.read(Types.INT);
      String dimensionName;
      String outputName;
      switch (dimension) {
         case -1:
            dimensionName = "minecraft:the_nether";
            outputName = map.nether();
            break;
         case 0:
            dimensionName = "minecraft:overworld";
            outputName = map.overworld();
            break;
         case 1:
            dimensionName = "minecraft:the_end";
            outputName = map.end();
            break;
         default:
            this.protocol.getLogger().warning("Invalid dimension id: " + dimension);
            dimensionName = "minecraft:overworld";
            outputName = map.overworld();
      }

      wrapper.write(Types.STRING, dimensionName);
      wrapper.write(Types.STRING, outputName);
      this.trackWorld(wrapper.user(), outputName);
   };

   public EntityPacketRewriter1_16(Protocol1_15_2To1_16 protocol) {
      super(protocol);
   }

   @Override
   protected void registerPackets() {
      this.protocol.registerClientbound(ClientboundPackets1_15.ADD_GLOBAL_ENTITY, ClientboundPackets1_16.ADD_ENTITY, wrapper -> {
         int entityId = wrapper.passthrough(Types.VAR_INT);
         byte type = wrapper.read(Types.BYTE);
         if (type != 1) {
            wrapper.cancel();
         } else {
            wrapper.user().<EntityTracker>getEntityTracker(this.protocol).addEntity(entityId, EntityTypes1_16.LIGHTNING_BOLT);
            wrapper.write(Types.UUID, UUIDUtil.randomUUID());
            wrapper.write(Types.VAR_INT, EntityTypes1_16.LIGHTNING_BOLT.getId());
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.write(Types.BYTE, (byte)0);
            wrapper.write(Types.BYTE, (byte)0);
            wrapper.write(Types.INT, 0);
            wrapper.write(Types.SHORT, (short)0);
            wrapper.write(Types.SHORT, (short)0);
            wrapper.write(Types.SHORT, (short)0);
         }
      });
      this.registerTracker(ClientboundPackets1_15.ADD_PLAYER, EntityTypes1_16.PLAYER);
      this.registerSetEntityData(ClientboundPackets1_15.SET_ENTITY_DATA, Types1_14.ENTITY_DATA_LIST, Types1_16.ENTITY_DATA_LIST);
      this.protocol.registerClientbound(ClientboundPackets1_15.RESPAWN, new PacketHandlers() {
         @Override
         public void register() {
            this.handler(EntityPacketRewriter1_16.this.DIMENSION_HANDLER);
            this.map(Types.LONG);
            this.map(Types.UNSIGNED_BYTE);
            this.handler(wrapper -> {
               wrapper.write(Types.BYTE, (byte)-1);
               boolean keepAttributes = wrapper.user().getProtocolInfo().serverProtocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_15);
               String levelType = wrapper.read(Types.STRING);
               wrapper.write(Types.BOOLEAN, false);
               wrapper.write(Types.BOOLEAN, levelType.equals("flat"));
               wrapper.write(Types.BOOLEAN, keepAttributes);
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_15.LOGIN, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.INT);
            this.map(Types.UNSIGNED_BYTE);
            this.handler(wrapper -> {
               wrapper.write(Types.BYTE, (byte)-1);
               wrapper.write(Types.STRING_ARRAY, DimensionRegistries1_16.getWorldNames());
               wrapper.write(Types.NAMED_COMPOUND_TAG, DimensionRegistries1_16.getDimensionsTag());
            });
            this.handler(EntityPacketRewriter1_16.this.DIMENSION_HANDLER);
            this.map(Types.LONG);
            this.map(Types.UNSIGNED_BYTE);
            this.handler(EntityPacketRewriter1_16.this.playerTrackerHandler());
            this.handler(wrapper -> {
               String type = wrapper.read(Types.STRING);
               wrapper.passthrough(Types.VAR_INT);
               wrapper.passthrough(Types.BOOLEAN);
               wrapper.passthrough(Types.BOOLEAN);
               wrapper.write(Types.BOOLEAN, false);
               wrapper.write(Types.BOOLEAN, type.equals("flat"));
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_15.UPDATE_ATTRIBUTES, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int size = wrapper.passthrough(Types.INT);
         int actualSize = size;

         for (int i = 0; i < size; i++) {
            String key = wrapper.read(Types.STRING);
            String attributeIdentifier = (String)AttributeMappings1_16.attributeIdentifierMappings().get(key);
            if (attributeIdentifier == null) {
               attributeIdentifier = Key.namespaced(key);
               if (!Key.isValid(attributeIdentifier)) {
                  if (Via.getConfig().logOtherConversionWarnings()) {
                     this.protocol.getLogger().warning("Invalid attribute: " + key);
                  }

                  actualSize--;
                  wrapper.read(Types.DOUBLE);
                  int modifierSize = wrapper.read(Types.VAR_INT);

                  for (int j = 0; j < modifierSize; j++) {
                     wrapper.read(Types.UUID);
                     wrapper.read(Types.DOUBLE);
                     wrapper.read(Types.BYTE);
                  }
                  continue;
               }
            }

            wrapper.write(Types.STRING, attributeIdentifier);
            wrapper.passthrough(Types.DOUBLE);
            int modifierSize = wrapper.passthrough(Types.VAR_INT);

            for (int j = 0; j < modifierSize; j++) {
               wrapper.passthrough(Types.UUID);
               wrapper.passthrough(Types.DOUBLE);
               wrapper.passthrough(Types.BYTE);
            }
         }

         if (size != actualSize) {
            wrapper.set(Types.INT, 0, actualSize);
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_16.SWING, wrapper -> {
         if (Via.getConfig().cancelSwingInInventory()) {
            ProtocolStorables1_16 storables = wrapper.user().storables(this.protocol);
            if (storables.inventoryTracker().isInventoryOpen()) {
               wrapper.cancel();
            }
         }
      });
   }

   @Override
   protected void registerRewrites() {
      this.filter().mapDataType(Types1_16.ENTITY_DATA_TYPES::byId);
      this.registerEntityDataTypeHandler(
         Types1_16.ENTITY_DATA_TYPES.itemType, Types1_16.ENTITY_DATA_TYPES.optionalBlockStateType, Types1_16.ENTITY_DATA_TYPES.particleType
      );
      this.registerBlockStateHandler(EntityTypes1_16.ABSTRACT_MINECART, 10);
      this.filter().type(EntityTypes1_16.ABSTRACT_ARROW).removeIndex(8);
      this.filter().type(EntityTypes1_16.WOLF).index(16).handler((event, data) -> {
         byte mask = data.<Byte>value();
         int angerTime = (mask & 2) != 0 ? Integer.MAX_VALUE : 0;
         event.createExtraData(new EntityData(20, Types1_16.ENTITY_DATA_TYPES.varIntType, angerTime));
      });
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes1_16.getTypeFromId(type);
   }
}
