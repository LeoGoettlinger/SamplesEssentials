package com.viaversion.viaversion.api.type.types.misc;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.util.IdHolder;
import io.netty.buffer.ByteBuf;

public abstract class DynamicType<T extends IdHolder> extends Type<T> {
   protected final Int2ObjectMap<DynamicType.DataReader<T>> readers;

   protected DynamicType(Int2ObjectMap<DynamicType.DataReader<T>> readers, Class<T> outputClass) {
      super(outputClass.getSimpleName(), outputClass);
      this.readers = readers;
   }

   protected DynamicType(Class<T> outputClass) {
      this(new Int2ObjectOpenHashMap<>(), outputClass);
   }

   public DynamicType<T>.DataFiller filler(Protocol<?, ?, ?, ?> protocol) {
      return this.filler(protocol, true);
   }

   public DynamicType<T>.DataFiller filler(Protocol<?, ?, ?, ?> protocol, boolean useMappedNames) {
      return new DynamicType.DataFiller(protocol, useMappedNames);
   }

   protected void readData(ByteBuf buffer, T value) {
      DynamicType.DataReader<T> reader = this.readers.get(value.id());
      if (reader != null) {
         reader.read(buffer, value);
      }
   }

   public DynamicType<T>.RawDataFiller rawFiller() {
      return new DynamicType.RawDataFiller();
   }

   protected abstract FullMappings mappings(Protocol<?, ?, ?, ?> var1);

   public final class DataFiller {
      private final FullMappings mappings;
      private final boolean useMappedNames;

      private DataFiller(Protocol<?, ?, ?, ?> protocol, boolean useMappedNames) {
         this.mappings = DynamicType.this.mappings(protocol);
         Preconditions.checkNotNull(this.mappings, "Mappings for %s are null", new Object[]{protocol.getClass()});
         this.useMappedNames = useMappedNames;
      }

      public DynamicType<T>.DataFiller reader(String identifier, DynamicType.DataReader<T> reader) {
         DynamicType.this.readers.put(this.useMappedNames ? this.mappings.mappedId(identifier) : this.mappings.id(identifier), reader);
         return this;
      }
   }

   @FunctionalInterface
   public interface DataReader<T> {
      void read(ByteBuf var1, T var2);
   }

   public final class RawDataFiller {
      public DynamicType<T>.RawDataFiller reader(int id, DynamicType.DataReader<T> reader) {
         DynamicType.this.readers.put(id, reader);
         return this;
      }
   }
}
