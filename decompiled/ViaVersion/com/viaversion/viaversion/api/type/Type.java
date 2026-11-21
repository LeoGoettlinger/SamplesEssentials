package com.viaversion.viaversion.api.type;

import com.viaversion.viaversion.api.minecraft.codec.Ops;

public abstract class Type<T> implements ByteBufReader<T>, ByteBufWriter<T>, CodecWriter<T> {
   private final Class<? super T> outputClass;
   private final String typeName;

   protected Type(Class<? super T> outputClass) {
      this(null, outputClass);
   }

   protected Type(String typeName, Class<? super T> outputClass) {
      this.outputClass = outputClass;
      this.typeName = typeName;
   }

   public Class<? super T> getOutputClass() {
      return this.outputClass;
   }

   public String getTypeName() {
      return this.typeName != null && !this.typeName.isEmpty() ? this.typeName : this.getClass().getName();
   }

   @Override
   public void write(Ops ops, T value) {
      throw new UnsupportedOperationException("Write operation not supported for type: " + this.getTypeName());
   }

   public Class<? extends Type> getBaseClass() {
      return (Class<? extends Type>)this.getClass();
   }

   @Override
   public String toString() {
      return this.getTypeName();
   }
}
