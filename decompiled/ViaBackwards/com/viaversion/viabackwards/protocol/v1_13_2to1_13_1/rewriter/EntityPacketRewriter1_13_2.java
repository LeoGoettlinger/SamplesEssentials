package com.viaversion.viabackwards.protocol.v1_13_2to1_13_1.rewriter;

import com.viaversion.viabackwards.protocol.v1_13_2to1_13_1.Protocol1_13_2To1_13_1;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.api.type.types.version.Types1_13_2;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import java.util.List;

public class EntityPacketRewriter1_13_2 {
   public static void register(Protocol1_13_2To1_13_1 protocol) {
      protocol.registerClientbound(ClientboundPackets1_13.ADD_MOB, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UUID);
            this.map(Types.VAR_INT);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.SHORT);
            this.map(Types.SHORT);
            this.map(Types.SHORT);
            this.map(Types1_13_2.ENTITY_DATA_LIST, Types1_13.ENTITY_DATA_LIST);
            this.handler(EntityPacketRewriter1_13_2::updateEntityData);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_13.ADD_PLAYER, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UUID);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types1_13_2.ENTITY_DATA_LIST, Types1_13.ENTITY_DATA_LIST);
            this.handler(EntityPacketRewriter1_13_2::updateEntityData);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_13.SET_ENTITY_DATA, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types1_13_2.ENTITY_DATA_LIST, Types1_13.ENTITY_DATA_LIST);
            this.handler(EntityPacketRewriter1_13_2::updateEntityData);
         }
      });
   }

   private static void updateEntityData(PacketWrapper wrapper) {
      for (EntityData data : (List)wrapper.get(Types1_13.ENTITY_DATA_LIST, 0)) {
         EntityDataType dataType = Types1_13.ENTITY_DATA_TYPES.byId(data.dataType().typeId());
         data.setDataType(dataType);
         if (dataType == Types1_13.ENTITY_DATA_TYPES.particleType) {
            Particle particle = (Particle)data.value();
            if (particle.id() == 27) {
               Item item = (Item)particle.getArgument(0).getValue();
               particle.set(0, Types.ITEM1_13, item);
            }
         }
      }
   }
}
