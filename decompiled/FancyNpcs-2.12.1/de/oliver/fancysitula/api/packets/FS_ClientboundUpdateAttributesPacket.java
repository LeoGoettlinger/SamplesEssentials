package de.oliver.fancysitula.api.packets;

import java.util.List;

public abstract class FS_ClientboundUpdateAttributesPacket extends FS_ClientboundPacket {
   protected int entityId;
   protected List<FS_ClientboundUpdateAttributesPacket.AttributeSnapshot> attributes;

   public FS_ClientboundUpdateAttributesPacket(int entityId, List<FS_ClientboundUpdateAttributesPacket.AttributeSnapshot> attributes) {
      this.entityId = entityId;
      this.attributes = attributes;
   }

   public int getEntityId() {
      return this.entityId;
   }

   public void setEntityId(int entityId) {
      this.entityId = entityId;
   }

   public List<FS_ClientboundUpdateAttributesPacket.AttributeSnapshot> getAttributes() {
      return this.attributes;
   }

   public void setAttributes(List<FS_ClientboundUpdateAttributesPacket.AttributeSnapshot> attributes) {
      this.attributes = attributes;
   }

   public record AttributeSnapshot(String attributeId, double baseValue) {
   }
}
