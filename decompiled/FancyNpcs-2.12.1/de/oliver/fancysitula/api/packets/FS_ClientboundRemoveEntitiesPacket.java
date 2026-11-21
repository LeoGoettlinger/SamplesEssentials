package de.oliver.fancysitula.api.packets;

import java.util.List;

public abstract class FS_ClientboundRemoveEntitiesPacket extends FS_ClientboundPacket {
   protected List<Integer> entityIds;

   public FS_ClientboundRemoveEntitiesPacket(List<Integer> entityIds) {
      this.entityIds = entityIds;
   }

   public List<Integer> getEntityIds() {
      return this.entityIds;
   }

   public void setEntityIds(List<Integer> entityIds) {
      this.entityIds = entityIds;
   }
}
