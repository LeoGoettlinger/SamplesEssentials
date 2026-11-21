package com.github.retrooper.packetevents.protocol.world.attributes;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticEnvironmentAttribute<T> extends AbstractMappedEntity implements EnvironmentAttribute<T> {
   private final boolean isSynced;
   @Nullable
   private final AttributeType<T> attributeType;
   @Nullable
   private final T defaultValue;

   @Internal
   public StaticEnvironmentAttribute(@Nullable TypesBuilderData data, boolean isSynced, @Nullable AttributeType<T> attributeType, @Nullable T defaultValue) {
      super(data);
      this.isSynced = isSynced;
      this.attributeType = attributeType;
      this.defaultValue = defaultValue;
   }

   @Override
   public boolean isSynced() {
      return this.isSynced;
   }

   @Override
   public AttributeType<T> getType() {
      if (this.attributeType == null) {
         throw new UnsupportedOperationException();
      } else {
         return this.attributeType;
      }
   }

   @Override
   public T getDefaultValue() {
      if (this.defaultValue == null) {
         throw new UnsupportedOperationException();
      } else {
         return this.defaultValue;
      }
   }
}
