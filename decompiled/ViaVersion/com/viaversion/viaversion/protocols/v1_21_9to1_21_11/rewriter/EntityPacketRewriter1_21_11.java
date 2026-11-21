package com.viaversion.viaversion.protocols.v1_21_9to1_21_11.rewriter;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_11;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.Protocol1_21_9To1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.storage.ProtocolStorables1_21_11;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandlerEvent;

public final class EntityPacketRewriter1_21_11 extends EntityRewriter<ClientboundPacket1_21_9, Protocol1_21_9To1_21_11> {
   public EntityPacketRewriter1_21_11(Protocol1_21_9To1_21_11 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      this.protocol.registerServerbound(ServerboundPackets1_21_6.PLAYER_ACTION, wrapper -> {
         int action = wrapper.passthrough(Types.VAR_INT);
         if (action == 7) {
            wrapper.cancel();
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_21_9.HORSE_SCREEN_OPEN, ClientboundPackets1_21_11.MOUNT_SCREEN_OPEN);
   }

   @Override
   protected void registerRewrites() {
      EntityDataTypes1_21_11 entityDataTypes = this.protocol.mappedTypes().entityDataTypes();
      this.dataTypeMapper().added(entityDataTypes.zombieNautilusVariantType).register();
      this.registerEntityDataTypeHandler(
         entityDataTypes.itemType,
         entityDataTypes.blockStateType,
         entityDataTypes.optionalBlockStateType,
         entityDataTypes.particleType,
         entityDataTypes.particlesType,
         entityDataTypes.componentType,
         entityDataTypes.optionalComponentType
      );
      this.filter().type(EntityTypes1_21_11.WOLF).index(21).handler(this::relativeToAbsoluteTicks);
      this.filter().type(EntityTypes1_21_11.BEE).index(18).handler(this::relativeToAbsoluteTicks);
      this.filter().type(EntityTypes1_21_11.AVATAR).index(15).handler((event, data) -> {
         byte arm = data.<Byte>value();
         data.setTypeAndValue(entityDataTypes.humanoidArmType, Integer.valueOf(arm));
      });
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes1_21_11.getTypeFromId(type);
   }

   private void relativeToAbsoluteTicks(EntityDataHandlerEvent event, EntityData data) {
      ProtocolStorables1_21_11 storables = event.user().storables(this.protocol);
      long currentGameTime = storables.gameTimeStorage().gameTime();
      int remainingAngerTime = data.<Integer>value();
      data.setTypeAndValue(this.protocol.mappedTypes().entityDataTypes().longType, currentGameTime + remainingAngerTime);
   }
}
