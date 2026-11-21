package com.viaversion.viabackwards.api.rewriters;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_14;

public abstract class EntityRewriter<C extends ClientboundPacketType, T extends BackwardsProtocol<C, ?, ?, ?>> extends EntityRewriterBase<C, T> {
   protected EntityRewriter(T protocol) {
      this(protocol, Types1_14.ENTITY_DATA_TYPES.optionalComponentType, Types1_14.ENTITY_DATA_TYPES.booleanType);
   }

   protected EntityRewriter(T protocol, EntityDataType displayType, EntityDataType displayVisibilityType) {
      super(protocol, displayType, 2, displayVisibilityType, 3);
   }

   public PacketHandler getSpawnTrackerWithDataHandler() {
      return wrapper -> {
         int entityId = (Integer)wrapper.get(Types.VAR_INT, 0);
         int typeId = (Integer)wrapper.get(Types.VAR_INT, 1);
         TrackedEntity entity = this.trackAndRewrite(wrapper, typeId, entityId);
         if (entity.entityType() == this.fallingBlockType()) {
            int blockState = (Integer)wrapper.get(Types.INT, 0);
            wrapper.set(Types.INT, 0, ((BackwardsProtocol)this.protocol).getMappingData().getNewBlockStateId(blockState));
         }
      };
   }

   public void trackSpawnWithData1_19(PacketWrapper wrapper, int data) {
      if (((BackwardsProtocol)this.protocol).getMappingData() != null) {
         int entityId = (Integer)wrapper.get(Types.VAR_INT, 0);
         int typeId = (Integer)wrapper.get(Types.VAR_INT, 1);
         TrackedEntity entity = this.trackAndRewrite(wrapper, typeId, entityId);
         if (entity.entityType() == this.fallingBlockType()) {
            wrapper.set(Types.VAR_INT, 2, ((BackwardsProtocol)this.protocol).getMappingData().getNewBlockStateId(data));
         }
      }
   }
}
