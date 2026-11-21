package com.viaversion.viabackwards.protocol.v1_16to1_15_2.storage;

import com.viaversion.viabackwards.protocol.v1_16to1_15_2.Protocol1_16To1_15_2;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packet.ClientboundPackets1_15;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

public final class PlayerAttributesStorage {
   private final Map<String, PlayerAttributesStorage.Attribute> attributes = new HashMap<>();

   public void sendAttributes(UserConnection connection, int entityId) {
      PacketWrapper updateAttributes = PacketWrapper.create(ClientboundPackets1_15.UPDATE_ATTRIBUTES, connection);
      updateAttributes.write(Types.VAR_INT, entityId);
      updateAttributes.write(Types.INT, this.attributes.size());

      for (Entry<String, PlayerAttributesStorage.Attribute> attributeEntry : this.attributes.entrySet()) {
         PlayerAttributesStorage.Attribute attribute = attributeEntry.getValue();
         updateAttributes.write(Types.STRING, attributeEntry.getKey());
         updateAttributes.write(Types.DOUBLE, attribute.value());
         updateAttributes.write(Types.VAR_INT, attribute.modifiers().length);

         for (PlayerAttributesStorage.AttributeModifier modifier : attribute.modifiers()) {
            updateAttributes.write(Types.UUID, modifier.uuid());
            updateAttributes.write(Types.DOUBLE, modifier.amount());
            updateAttributes.write(Types.BYTE, modifier.operation());
         }
      }

      updateAttributes.send(Protocol1_16To1_15_2.class);
   }

   public void clearAttributes() {
      this.attributes.clear();
   }

   public void addAttribute(String key, PlayerAttributesStorage.Attribute attribute) {
      this.attributes.put(key, attribute);
   }

   public record Attribute(double value, PlayerAttributesStorage.AttributeModifier[] modifiers) {
   }

   public record AttributeModifier(UUID uuid, double amount, byte operation) {
   }
}
