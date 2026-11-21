package de.oliver.fancysitula.api.packets;

import java.util.List;

public abstract class FS_ClientboundSetEntityDataPacket extends FS_ClientboundPacket {
   protected int entityId;
   protected List<FS_ClientboundSetEntityDataPacket.EntityData> entityData;

   public FS_ClientboundSetEntityDataPacket(int entityId, List<FS_ClientboundSetEntityDataPacket.EntityData> entityData) {
      this.entityId = entityId;
      this.entityData = entityData;
   }

   public int getEntityId() {
      return this.entityId;
   }

   public void setEntityId(int entityId) {
      this.entityId = entityId;
   }

   public List<FS_ClientboundSetEntityDataPacket.EntityData> getEntityData() {
      return this.entityData;
   }

   public void setEntityData(List<FS_ClientboundSetEntityDataPacket.EntityData> entityData) {
      this.entityData = entityData;
   }

   public static class EntityData {
      private FS_ClientboundSetEntityDataPacket.EntityDataAccessor accessor;
      private Object value;

      public EntityData(FS_ClientboundSetEntityDataPacket.EntityDataAccessor accessor, Object value) {
         this.accessor = accessor;
         this.value = value;
      }

      public FS_ClientboundSetEntityDataPacket.EntityDataAccessor getAccessor() {
         return this.accessor;
      }

      public void setAccessor(FS_ClientboundSetEntityDataPacket.EntityDataAccessor accessor) {
         this.accessor = accessor;
      }

      public Object getValue() {
         return this.value;
      }

      public void setValue(Object value) {
         this.value = value;
      }
   }

   public record EntityDataAccessor(String entityClassName, String accessorFieldName) {
   }
}
