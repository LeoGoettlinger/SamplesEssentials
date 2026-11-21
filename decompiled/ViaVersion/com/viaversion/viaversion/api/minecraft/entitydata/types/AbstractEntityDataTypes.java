package com.viaversion.viaversion.api.minecraft.entitydata.types;

import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.type.Type;

public abstract class AbstractEntityDataTypes implements EntityDataTypes {
   private final EntityDataType[] values;

   protected AbstractEntityDataTypes(int values) {
      this.values = new EntityDataType[values];
   }

   @Override
   public EntityDataType byId(int id) {
      return this.values[id];
   }

   @Override
   public EntityDataType[] values() {
      return this.values;
   }

   public Class<? extends EntityDataType> getDataTypeClass() {
      return (Class<? extends EntityDataType>)this.byId(0).getClass();
   }

   protected EntityDataType add(int typeId, Type<?> type) {
      if (this.values[typeId] != null) {
         throw new IllegalArgumentException("Entity data type ID " + typeId + " is already registered as " + this.values[typeId]);
      } else {
         EntityDataType dataType = EntityDataType.create(typeId, type);
         this.values[typeId] = dataType;
         return dataType;
      }
   }
}
