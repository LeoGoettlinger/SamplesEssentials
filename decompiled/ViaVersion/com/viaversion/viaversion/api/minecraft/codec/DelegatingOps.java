package com.viaversion.viaversion.api.minecraft.codec;

import com.viaversion.viaversion.api.type.Type;
import java.util.function.Consumer;

public class DelegatingOps implements Ops {
   private final Ops delegate;

   public DelegatingOps(Ops delegate) {
      this.delegate = delegate;
   }

   @Override
   public CodecContext context() {
      return this.delegate.context();
   }

   @Override
   public void writeByte(byte b) {
      this.delegate.writeByte(b);
   }

   @Override
   public void writeBytes(byte[] array) {
      this.delegate.writeBytes(array);
   }

   @Override
   public void writeBoolean(boolean b) {
      this.delegate.writeBoolean(b);
   }

   @Override
   public void writeShort(short s) {
      this.delegate.writeShort(s);
   }

   @Override
   public void writeString(CharSequence sequence) {
      this.delegate.writeString(sequence);
   }

   @Override
   public void writeInt(int i) {
      this.delegate.writeInt(i);
   }

   @Override
   public void writeLong(long l) {
      this.delegate.writeLong(l);
   }

   @Override
   public void writeFloat(float f) {
      this.delegate.writeFloat(f);
   }

   @Override
   public void writeDouble(double d) {
      this.delegate.writeDouble(d);
   }

   @Override
   public void writeInts(int[] array) {
      this.delegate.writeInts(array);
   }

   @Override
   public void writeLongs(long[] array) {
      this.delegate.writeLongs(array);
   }

   @Override
   public void writeList(Consumer<Ops.ListSerializer> consumer) {
      this.delegate.writeList(consumer);
   }

   @Override
   public void writeMap(Consumer<Ops.MapSerializer> consumer) {
      this.delegate.writeMap(consumer);
   }

   @Override
   public <V> void write(Type<V> type, V value) {
      this.delegate.write(type, value);
   }
}
