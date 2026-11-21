package com.viaversion.viaversion.rewriter.entitydata;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.minecraft.entitydata.types.AbstractEntityDataTypes;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectFunction;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntArraySet;
import com.viaversion.viaversion.libs.fastutil.ints.IntComparators;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

public record EntityDataFilter(@Nullable EntityType type, boolean filterFamily, @Nullable EntityDataType dataType, int index, EntityDataHandler handler) {
   public EntityDataFilter(@Nullable EntityType type, boolean filterFamily, @Nullable EntityDataType dataType, int index, EntityDataHandler handler) {
      Preconditions.checkNotNull(handler, "EntityDataHandler cannot be null");
      this.type = type;
      this.filterFamily = filterFamily;
      this.dataType = dataType;
      this.index = index;
      this.handler = handler;
   }

   public EntityDataFilter(EntityDataHandler handler) {
      this(null, true, null, -1, handler);
   }

   public boolean isFiltered(@Nullable EntityType type, EntityData entityData) {
      return (this.index == -1 || entityData.id() == this.index)
         && (this.type == null || this.matchesType(type))
         && (this.dataType == null || entityData.dataType() == this.dataType);
   }

   private boolean matchesType(EntityType type) {
      if (type == null) {
         return false;
      } else {
         return this.filterFamily ? type.isOrHasParent(this.type) : this.type == type;
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         EntityDataFilter that = (EntityDataFilter)o;
         if (this.index != that.index) {
            return false;
         } else if (this.filterFamily != that.filterFamily) {
            return false;
         } else if (!this.handler.equals(that.handler)) {
            return false;
         } else {
            return !Objects.equals(this.dataType, that.dataType) ? false : Objects.equals(this.type, that.type);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.handler.hashCode();
      result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
      result = 31 * result + (this.dataType != null ? this.dataType.hashCode() : 0);
      result = 31 * result + this.index;
      return 31 * result + (this.filterFamily ? 1 : 0);
   }

   @Override
   public String toString() {
      return "EntityDataFilter{type="
         + this.type
         + ", filterFamily="
         + this.filterFamily
         + ", dataType="
         + this.dataType
         + ", index="
         + this.index
         + ", handler="
         + this.handler
         + "}";
   }

   public static final class Builder {
      private final EntityRewriter<?, ?> rewriter;
      private EntityType type;
      private EntityDataType dataType;
      private int index = -1;
      private boolean filterFamily;
      private EntityDataHandler handler;

      public Builder(EntityRewriter<?, ?> rewriter) {
         this.rewriter = rewriter;
      }

      public EntityDataFilter.Builder dataType(EntityDataType dataType) {
         Preconditions.checkArgument(this.dataType == null);
         this.dataType = dataType;
         return this;
      }

      public EntityDataFilter.Builder type(EntityType type) {
         Preconditions.checkArgument(this.type == null);
         this.type = type;
         this.filterFamily = true;
         return this;
      }

      public EntityDataFilter.Builder exactType(EntityType type) {
         Preconditions.checkArgument(this.type == null);
         this.type = type;
         this.filterFamily = false;
         return this;
      }

      public EntityDataFilter.Builder index(int index) {
         Preconditions.checkArgument(this.index == -1);
         this.index = index;
         return this;
      }

      public EntityDataFilter.Builder handlerNoRegister(EntityDataHandler handler) {
         Preconditions.checkArgument(this.handler == null);
         this.handler = handler;
         return this;
      }

      public void handler(EntityDataHandler handler) {
         Preconditions.checkArgument(this.handler == null);
         this.handler = handler;
         this.register();
      }

      public void mapDataType(Int2ObjectFunction<EntityDataType> updateFunction) {
         this.handler((event, data) -> {
            EntityDataType mappedType = updateFunction.apply(data.dataType().typeId());
            if (mappedType != null) {
               data.setDataType(mappedType);
            } else {
               event.cancel();
            }
         });
      }

      public void cancel(int index) {
         this.index = index;
         this.handler((event, data) -> event.cancel());
      }

      public void toIndex(int newIndex) {
         Preconditions.checkArgument(this.index != -1);
         this.handler((event, data) -> event.setIndex(newIndex));
      }

      public void addIndex(int index) {
         Preconditions.checkArgument(this.index == -1);
         this.handler((event, data) -> {
            if (event.index() >= index) {
               event.setIndex(event.index() + 1);
            }
         });
      }

      public void removeIndex(int index) {
         Preconditions.checkArgument(this.index == -1);
         this.handler((event, data) -> {
            int dataIndex = event.index();
            if (dataIndex == index) {
               event.cancel();
            } else if (dataIndex > index) {
               event.setIndex(dataIndex - 1);
            }
         });
      }

      public void register() {
         this.rewriter.registerFilter(this.build());
      }

      public EntityDataFilter build() {
         Preconditions.checkNotNull(this.handler, "EntityDataHandler cannot be null");
         return new EntityDataFilter(this.type, this.filterFamily, this.dataType, this.index, this.handler);
      }
   }

   public static final class DataTypeMapper {
      private final IntList addedTypeIds = new IntArrayList();
      private final IntList removedTypeIds = new IntArrayList();
      private final IntSet skippedTypeIds = new IntArraySet();
      private final EntityRewriter<?, ?> rewriter;
      private final AbstractEntityDataTypes mappedDataTypes;

      public DataTypeMapper(EntityRewriter<?, ?> rewriter) {
         this.rewriter = rewriter;
         this.mappedDataTypes = rewriter.protocol().mappedTypes().entityDataTypes();
      }

      public EntityDataFilter.DataTypeMapper added(EntityDataType dataType) {
         Preconditions.checkArgument(dataType.getClass() == this.mappedDataTypes.getDataTypeClass());
         this.addedTypeIds.add(dataType.typeId());
         return this;
      }

      public EntityDataFilter.DataTypeMapper removed(EntityDataType dataType) {
         Preconditions.checkArgument(dataType.getClass() == this.rewriter.protocol().types().entityDataTypes().getDataTypeClass());
         this.removedTypeIds.add(dataType.typeId());
         return this;
      }

      public EntityDataFilter.DataTypeMapper skip(EntityDataType dataType) {
         Preconditions.checkArgument(dataType.getClass() == this.rewriter.protocol().types().entityDataTypes().getDataTypeClass());
         this.skippedTypeIds.add(dataType.typeId());
         return this;
      }

      public void register() {
         if (this.addedTypeIds.isEmpty() && this.removedTypeIds.isEmpty() && this.skippedTypeIds.isEmpty()) {
            this.rewriter.filter().handler((event, data) -> {
               EntityDataType dataType = this.mappedDataTypes.byId(data.dataType().typeId());
               data.setDataType(dataType);
            });
         } else {
            this.addedTypeIds.sort(IntComparators.NATURAL_COMPARATOR);
            this.removedTypeIds.sort(IntComparators.OPPOSITE_COMPARATOR);
            this.rewriter.registerFilter(new EntityDataFilter((event, data) -> {
               EntityDataType mappedType = this.mappedType(data.dataType());
               if (mappedType != null) {
                  data.setDataType(mappedType);
               } else if (event.entityType() == null) {
                  event.cancel();
               }
            }));
         }
      }

      @Nullable
      private EntityDataType mappedType(EntityDataType dataType) {
         if (!this.skippedTypeIds.contains(dataType.typeId()) && !this.shouldCancel(dataType)) {
            int mappedId = this.mappedTypeId(dataType.typeId());
            return this.mappedDataTypes.byId(mappedId);
         } else {
            return null;
         }
      }

      private boolean shouldCancel(EntityDataType dataType) {
         return !this.removedTypeIds.isEmpty() && this.removedTypeIds.contains(dataType.typeId());
      }

      private int mappedTypeId(int typeId) {
         for (int removedTypeId : this.removedTypeIds) {
            if (typeId > removedTypeId) {
               typeId--;
            }
         }

         for (int addedTypeId : this.addedTypeIds) {
            if (typeId >= addedTypeId) {
               typeId++;
            }
         }

         return typeId;
      }
   }
}
