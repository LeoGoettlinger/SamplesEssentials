package com.fastasyncworldedit.core.internal.io;

import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.Tag;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FaweOutputStream extends DataOutputStream {
   private final OutputStream parent;
   private NBTOutputStream nbtOut;

   public FaweOutputStream(OutputStream parent) {
      super(parent);
      this.parent = parent;
   }

   public OutputStream getParent() {
      return this.parent;
   }

   @Override
   public void write(final int b) throws IOException {
      this.out.write(b);
      this.written++;
   }

   public void write(int b, int amount) throws IOException {
      for (int i = 0; i < amount; i++) {
         this.write(b);
      }
   }

   public void writeShort(short s) throws IOException {
      this.write((byte)(s >>> 8));
      this.write((byte)s);
   }

   public void writeMedium(int m) throws IOException {
      this.write((byte)(m >>> 16));
      this.write((byte)(m >>> 8));
      this.write((byte)m);
   }

   public void writeVarInt(int i) throws IOException {
      while ((i & -128) != 0) {
         this.writeByte(i & 127 | 128);
         i >>>= 7;
      }

      this.writeByte(i);
   }

   public void write(long[] data) throws IOException {
      this.writeVarInt(data.length);

      for (long datum : data) {
         this.writeLong(datum);
      }
   }

   public void writeNBT(String name, Tag tag) throws IOException {
      if (this.nbtOut == null) {
         this.nbtOut = new NBTOutputStream(this.parent);
      }

      this.nbtOut.writeNamedTag(name, tag);
   }

   public void writePrimitive(Object value) throws IOException {
      Class<? extends Object> clazz = (Class<? extends Object>)value.getClass();
      if (clazz == long.class || clazz == Long.class) {
         this.writeLong((Long)value);
      } else if (clazz == double.class || clazz == Double.class) {
         this.writeDouble((Double)value);
      } else if (clazz == float.class || clazz == Float.class) {
         this.writeFloat((Float)value);
      } else if (clazz == int.class || clazz == Integer.class) {
         this.writeInt((Integer)value);
      } else if (clazz == short.class || clazz == Short.class) {
         this.writeShort((Short)value);
      } else if (clazz == char.class || clazz == Character.class) {
         this.writeChar((Character)value);
      } else if (clazz != byte.class && clazz != Byte.class) {
         if (clazz != boolean.class && clazz != Boolean.class) {
            throw new UnsupportedOperationException("Unknown class " + clazz);
         }

         this.writeBoolean((Boolean)value);
      } else {
         this.writeByte((Byte)value);
      }
   }

   @Override
   public void close() throws IOException {
      if (this.nbtOut != null) {
         this.nbtOut.close();
      }

      this.parent.close();
   }
}
