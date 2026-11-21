package com.viaversion.viabackwards.api.rewriters;

import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Types;

public class SoundRewriter<C extends ClientboundPacketType> extends com.viaversion.viaversion.rewriter.SoundRewriter<C> {
   public SoundRewriter(AbstractProtocol<C, ?, ?, ?> protocol) {
      super(protocol);
   }

   public void registerNamedSound(C packetType) {
      if (this.protocol.getMappingData() != null && !Mappings.isFullIdentity(this.protocol.getMappingData().getFullSoundMappings())) {
         this.protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.STRING);
            this.getNamedSoundHandler().handle(wrapper);
         });
      }
   }

   public void registerStopSound(C packetType) {
      if (this.protocol.getMappingData() != null && !Mappings.isFullIdentity(this.protocol.getMappingData().getFullSoundMappings())) {
         this.protocol.registerClientbound(packetType, wrapper -> {
            byte flags = (Byte)wrapper.passthrough(Types.BYTE);
            if ((flags & 1) != 0) {
               wrapper.passthrough(Types.VAR_INT);
            }

            if ((flags & 2) != 0) {
               String soundId = (String)wrapper.read(Types.STRING);
               String mappedId = this.protocol.getMappingData().getFullSoundMappings().mappedIdentifier(soundId);
               if (mappedId == null) {
                  wrapper.write(Types.STRING, soundId);
               } else {
                  if (!mappedId.isEmpty()) {
                     wrapper.write(Types.STRING, mappedId);
                  } else {
                     wrapper.cancel();
                  }
               }
            }
         });
      }
   }

   public PacketHandler getNamedSoundHandler() {
      return wrapper -> {
         String soundId = (String)wrapper.get(Types.STRING, 0);
         String mappedId = this.protocol.getMappingData().getFullSoundMappings().mappedIdentifier(soundId);
         if (mappedId != null) {
            if (!mappedId.isEmpty()) {
               wrapper.set(Types.STRING, 0, mappedId);
            } else {
               wrapper.cancel();
            }
         }
      };
   }
}
