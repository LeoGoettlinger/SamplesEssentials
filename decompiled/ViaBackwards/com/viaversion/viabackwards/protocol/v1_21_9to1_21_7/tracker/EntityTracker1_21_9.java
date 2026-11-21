package com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.tracker;

import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.Protocol1_21_9To1_21_7;
import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.storage.MannequinData;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class EntityTracker1_21_9 extends EntityTrackerBase {
   public EntityTracker1_21_9(UserConnection connection, @Nullable EntityType playerType) {
      super(connection, playerType);
   }

   @Nullable
   public TrackedEntity removeEntity(int id) {
      TrackedEntity entity = super.removeEntity(id);
      if (entity != null) {
         this.sendRemovePacket(entity);
      }

      return entity;
   }

   public void sendRemovePacket(TrackedEntity entity) {
      MannequinData trackedEntityId = (MannequinData)entity.get(MannequinData.class);
      if (trackedEntityId != null) {
         PacketWrapper playerInfoRemove = PacketWrapper.create(ClientboundPackets1_21_6.PLAYER_INFO_REMOVE, this.user());
         playerInfoRemove.write(Types.UUID_ARRAY, new UUID[]{trackedEntityId.uuid()});
         playerInfoRemove.send(Protocol1_21_9To1_21_7.class);
         PacketWrapper removePlayerTeam = PacketWrapper.create(ClientboundPackets1_21_6.SET_PLAYER_TEAM, this.user());
         removePlayerTeam.write(Types.STRING, trackedEntityId.name());
         removePlayerTeam.write(Types.BYTE, (byte)1);
         removePlayerTeam.send(Protocol1_21_9To1_21_7.class);
      }
   }
}
