package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.type.Types;

public class AttributeRewriter<C extends ClientboundPacketType> {
   private final Protocol<C, ?, ?, ?> protocol;

   public AttributeRewriter(Protocol<C, ?, ?, ?> protocol) {
      this.protocol = protocol;
   }

   public void register1_21(C packetType) {
      if (this.protocol.getMappingData() != null && !Mappings.isIntIdIdentity(this.protocol.getMappingData().getAttributeMappings())) {
         this.protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT);
            int size = wrapper.passthrough(Types.VAR_INT);
            int newSize = size;

            for (int i = 0; i < size; i++) {
               int attributeId = wrapper.read(Types.VAR_INT);
               int mappedId = this.protocol.getMappingData().getNewAttributeId(attributeId);
               if (mappedId == -1) {
                  newSize--;
                  wrapper.read(Types.DOUBLE);
                  int modifierSize = wrapper.read(Types.VAR_INT);

                  for (int j = 0; j < modifierSize; j++) {
                     wrapper.read(Types.STRING);
                     wrapper.read(Types.DOUBLE);
                     wrapper.read(Types.BYTE);
                  }
               } else {
                  wrapper.write(Types.VAR_INT, mappedId);
                  wrapper.passthrough(Types.DOUBLE);
                  int modifierSize = wrapper.passthrough(Types.VAR_INT);

                  for (int j = 0; j < modifierSize; j++) {
                     wrapper.passthrough(Types.STRING);
                     wrapper.passthrough(Types.DOUBLE);
                     wrapper.passthrough(Types.BYTE);
                  }
               }
            }

            if (size != newSize) {
               wrapper.set(Types.VAR_INT, 1, newSize);
            }
         });
      }
   }
}
