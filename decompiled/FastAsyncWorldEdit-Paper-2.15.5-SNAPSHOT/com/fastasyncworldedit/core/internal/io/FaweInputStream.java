package com.fastasyncworldedit.core.internal.io;

import com.fastasyncworldedit.core.util.IOUtil;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NamedTag;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FaweInputStream extends DataInputStream {
   private final InputStream parent;
   private NBTInputStream nbtIn;

   public FaweInputStream(InputStream parent) {
      super(parent);
      this.parent = parent;
   }

   public InputStream getParent() {
      return this.parent;
   }

   public int readMedium() throws IOException {
      return (this.read() << 16) + (this.read() << 8) + this.read();
   }

   public void skipFully(int num) throws IOException {
      long skipped = this.skip(num);

      while (skipped != num) {
         skipped += this.skip(num - skipped);
      }
   }

   public NamedTag readNBT() throws IOException {
      if (this.nbtIn == null) {
         this.nbtIn = new NBTInputStream(this.parent);
      }

      return this.nbtIn.readNamedTag();
   }

   public Object readPrimitive(Class<?> clazz) throws IOException {
      if (clazz == long.class || clazz == Long.class) {
         return this.readLong();
      } else if (clazz == double.class || clazz == Double.class) {
         return this.readDouble();
      } else if (clazz == float.class || clazz == Float.class) {
         return this.readFloat();
      } else if (clazz == int.class || clazz == Integer.class) {
         return this.readInt();
      } else if (clazz == short.class || clazz == Short.class) {
         return this.readShort();
      } else if (clazz == char.class || clazz == Character.class) {
         return this.readChar();
      } else if (clazz == byte.class || clazz == Byte.class) {
         return this.readByte();
      } else if (clazz != boolean.class && clazz != Boolean.class) {
         throw new UnsupportedOperationException("Unknown class " + clazz);
      } else {
         return this.readBoolean();
      }
   }

   public final int readVarInt() throws IOException {
      return IOUtil.readVarInt(this);
   }

   @Override
   public void close() throws IOException {
      if (this.nbtIn != null) {
         this.nbtIn.close();
      }

      this.parent.close();
   }
}
