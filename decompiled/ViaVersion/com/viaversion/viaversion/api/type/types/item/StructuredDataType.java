package com.viaversion.viaversion.api.type.types.item;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StructuredDataType extends Type<StructuredData<?>> implements StructuredDataTypeBase {
   private StructuredDataKey<?>[] types;

   public StructuredDataType() {
      super(StructuredData.class);
   }

   @Override
   public void write(ByteBuf buffer, StructuredData<?> object) {
      Types.VAR_INT.writePrimitive(buffer, object.id());
      object.write(buffer);
   }

   @Override
   public StructuredData<?> read(ByteBuf buffer) {
      Preconditions.checkNotNull(this.types, "StructuredDataType has not been initialized");
      int id = Types.VAR_INT.readPrimitive(buffer);
      StructuredDataKey<?> key = this.key(id);
      if (key == null) {
         throw new IllegalArgumentException("No data component serializer found for id " + id);
      } else {
         return this.readData(buffer, key, id);
      }
   }

   @Nullable
   @Override
   public StructuredDataKey<?> key(int id) {
      return id >= 0 && id < this.types.length ? this.types[id] : null;
   }

   public void write(Ops ops, StructuredData<?> data) {
      if (data.isPresent() && ops.context().isSupported(data.key())) {
         this.writeGeneric(ops, data);
      }
   }

   private <V> void writeGeneric(Ops ops, StructuredData<V> data) {
      data.key().type().write(ops, data.value());
   }

   <T> StructuredData<T> readData(ByteBuf buffer, StructuredDataKey<T> key, int id) {
      return StructuredData.of(key, key.type().read(buffer), id);
   }

   public StructuredDataType.DataFiller filler(Protocol<?, ?, ?, ?> protocol) {
      StructuredDataType.DataFiller filler = new StructuredDataType.DataFiller(protocol);
      if (protocol.mappedTypes() != null) {
         filler.add(protocol.mappedTypes().structuredDataKeys().keys());
      }

      return filler;
   }

   public final class DataFiller {
      private final FullMappings mappings;

      private DataFiller(Protocol<?, ?, ?, ?> protocol) {
         this.mappings = protocol.getMappingData().getDataComponentSerializerMappings();
         Preconditions.checkArgument(this.mappings != null, "No mappings found for protocol %s", new Object[]{protocol.getClass()});
         Preconditions.checkArgument(StructuredDataType.this.types == null, "StructuredDataType has already been initialized");
         StructuredDataType.this.types = new StructuredDataKey[this.mappings.mappedSize()];
      }

      public StructuredDataType.DataFiller add(StructuredDataKey<?> key) {
         int id = this.mappings.mappedId(key.identifier());
         Preconditions.checkArgument(id != -1, "No mapped id found for %s", new Object[]{key.identifier()});
         Preconditions.checkArgument(StructuredDataType.this.types[id] == null, "Data component serializer already exists for id %s", new Object[]{id});
         StructuredDataType.this.types[id] = key;
         return this;
      }

      public StructuredDataType.DataFiller add(StructuredDataKey<?>... keys) {
         for (StructuredDataKey<?> key : keys) {
            this.add(key);
         }

         return this;
      }

      public StructuredDataType.DataFiller add(Collection<StructuredDataKey<?>> keys) {
         for (StructuredDataKey<?> key : keys) {
            this.add(key);
         }

         return this;
      }
   }
}
