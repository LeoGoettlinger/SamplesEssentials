package com.viaversion.viaversion.api.minecraft.entitydata;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.type.Type;

public interface EntityDataType {
   Type type();

   int typeId();

   static EntityDataType create(int typeId, Type<?> type) {
      return new EntityDataType.EntityDataTypeImpl(typeId, type);
   }

   public static final class EntityDataTypeImpl implements EntityDataType {
      private final int typeId;
      private final Type<?> type;

      EntityDataTypeImpl(int typeId, Type<?> type) {
         Preconditions.checkNotNull(type);
         this.typeId = typeId;
         this.type = type;
      }

      @Override
      public int typeId() {
         return this.typeId;
      }

      @Override
      public Type<?> type() {
         return this.type;
      }

      @Override
      public String toString() {
         return "EntityDataTypeImpl{typeId=" + this.typeId + ", type=" + this.type + "}";
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            EntityDataType.EntityDataTypeImpl dataType = (EntityDataType.EntityDataTypeImpl)o;
            return this.typeId != dataType.typeId ? false : this.type.equals(dataType.type);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.typeId;
         return 31 * result + this.type.hashCode();
      }
   }
}
