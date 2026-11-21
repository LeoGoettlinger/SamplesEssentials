package com.viaversion.viaversion.libs.mcstructs.converter;

import com.viaversion.viaversion.libs.mcstructs.converter.codec.Codec;
import com.viaversion.viaversion.libs.mcstructs.converter.model.Result;
import java.util.Objects;
import lombok.Generated;

public class SerializedData<T> {
   private final T data;
   private final DataConverter<T> converter;

   public <S> Result<S> convert(DataConverter<S> converter) {
      return this.converter == converter ? Result.success(this.data) : Result.success(this.converter.convertTo(converter, this.data));
   }

   public <O> Result<O> deserialize(Codec<O> codec) {
      return codec.deserialize(this.converter, this.data);
   }

   @Override
   public String toString() {
      return "SerializedData{" + this.data + "}";
   }

   @Override
   public boolean equals(Object o) {
      if (o != null && this.getClass() == o.getClass()) {
         SerializedData<?> that = (SerializedData<?>)o;
         return Objects.equals(this.data, that.data);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.data);
   }

   @Generated
   public T getData() {
      return this.data;
   }

   @Generated
   public DataConverter<T> getConverter() {
      return this.converter;
   }

   @Generated
   public SerializedData(T data, DataConverter<T> converter) {
      this.data = data;
      this.converter = converter;
   }
}
