package com.viaversion.viaversion.api.minecraft.entitydata;

import com.google.common.base.Preconditions;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class EntityData {
   private int id;
   private EntityDataType dataType;
   private Object value;

   public EntityData(int id, EntityDataType dataType, @Nullable Object value) {
      this.id = id;
      this.dataType = dataType;
      this.value = this.checkValue(dataType, value);
   }

   public int id() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public EntityDataType dataType() {
      return this.dataType;
   }

   public void setDataType(EntityDataType dataType) {
      this.checkValue(dataType, this.value);
      this.dataType = dataType;
   }

   @Nullable
   public <T> T value() {
      return (T)this.value;
   }

   @Nullable
   public Object getValue() {
      return this.value;
   }

   public void setValue(@Nullable Object value) {
      this.value = this.checkValue(this.dataType, value);
   }

   public void setTypeAndValue(EntityDataType dataType, @Nullable Object value) {
      this.value = this.checkValue(dataType, value);
      this.dataType = dataType;
   }

   private Object checkValue(EntityDataType dataType, @Nullable Object value) {
      Preconditions.checkNotNull(dataType);
      if (value != null && !dataType.type().getOutputClass().isAssignableFrom(value.getClass())) {
         throw new IllegalArgumentException(
            "Entity data value and dataType are incompatible. Type=" + dataType + ", value=" + value + " (" + value.getClass().getSimpleName() + ")"
         );
      } else {
         return value;
      }
   }

   @Deprecated
   public void setDataTypeUnsafe(EntityDataType type) {
      this.dataType = type;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         EntityData entityData = (EntityData)o;
         if (this.id != entityData.id) {
            return false;
         } else {
            return this.dataType != entityData.dataType ? false : Objects.equals(this.value, entityData.value);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.id;
      result = 31 * result + this.dataType.hashCode();
      return 31 * result + (this.value != null ? this.value.hashCode() : 0);
   }

   @Override
   public String toString() {
      return "EntityData{id=" + this.id + ", dataType=" + this.dataType + ", value=" + this.value + "}";
   }
}
