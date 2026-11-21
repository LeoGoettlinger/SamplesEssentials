package com.viaversion.viabackwards.protocol.v1_14to1_13_2.rewriter;

import com.viaversion.viabackwards.protocol.v1_14to1_13_2.Protocol1_14To1_13_2;
import com.viaversion.viabackwards.protocol.v1_14to1_13_2.storage.EntityPositionStorage1_14;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.rewriter.RewriterBase;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ClientboundPackets1_14;

public class SoundPacketRewriter1_14 extends RewriterBase<Protocol1_14To1_13_2> {
   public SoundPacketRewriter1_14(Protocol1_14To1_13_2 protocol) {
      super(protocol);
   }

   protected void registerPackets() {
      ((Protocol1_14To1_13_2)this.protocol).registerClientbound(ClientboundPackets1_14.SOUND_ENTITY, null, wrapper -> {
         wrapper.cancel();
         int soundId = (Integer)wrapper.read(Types.VAR_INT);
         int newId = ((Protocol1_14To1_13_2)this.protocol).getMappingData().getSoundMappings().getNewId(soundId);
         if (newId != -1) {
            int category = (Integer)wrapper.read(Types.VAR_INT);
            int entityId = (Integer)wrapper.read(Types.VAR_INT);
            TrackedEntity storedEntity = wrapper.user().getEntityTracker(this.protocol).entity(entityId);
            EntityPositionStorage1_14 entityStorage;
            if (storedEntity != null && (entityStorage = (EntityPositionStorage1_14)storedEntity.get(EntityPositionStorage1_14.class)) != null) {
               float volume = (Float)wrapper.read(Types.FLOAT);
               float pitch = (Float)wrapper.read(Types.FLOAT);
               int x = (int)(entityStorage.x() * 8.0);
               int y = (int)(entityStorage.y() * 8.0);
               int z = (int)(entityStorage.z() * 8.0);
               PacketWrapper soundPacket = wrapper.create(ClientboundPackets1_13.SOUND);
               soundPacket.write(Types.VAR_INT, newId);
               soundPacket.write(Types.VAR_INT, category);
               soundPacket.write(Types.INT, x);
               soundPacket.write(Types.INT, y);
               soundPacket.write(Types.INT, z);
               soundPacket.write(Types.FLOAT, volume);
               soundPacket.write(Types.FLOAT, pitch);
               soundPacket.send(Protocol1_14To1_13_2.class);
            } else {
               ((Protocol1_14To1_13_2)this.protocol).getLogger().warning("Untracked entity with id " + entityId);
            }
         }
      });
   }
}
