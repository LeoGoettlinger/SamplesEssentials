package ch.njol.yggdrasil;

import ch.njol.util.coll.CollectionUtils;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.lang.invoke.StringConcatFactory;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class DefaultYggdrasilInputStream extends YggdrasilInputStream {
   private final InputStream in;
   private final short version;
   private final List<String> readShortStrings = new ArrayList<>();

   public DefaultYggdrasilInputStream(Yggdrasil yggdrasil, InputStream in) throws IOException {
      super(yggdrasil);
      this.in = in;
      if (this.readInt() != 1499948800) {
         throw new StreamCorruptedException("Not an Yggdrasil stream");
      } else {
         this.version = this.readShort();
         if (this.version <= 0 || this.version > 1) {
            throw new StreamCorruptedException("Input was saved using a later version of Yggdrasil");
         }
      }
   }

   private int read() throws IOException {
      int b = this.in.read();
      if (b < 0) {
         throw new EOFException();
      } else {
         return b;
      }
   }

   private void readFully(byte[] buf) throws IOException {
      this.readFully(buf, buf.length);
   }

   private void readFully(byte[] buf, int startLength) throws IOException {
      int offset = 0;
      int length = startLength;

      while (length > 0) {
         int n = this.in.read(buf, offset, length);
         if (n < 0) {
            throw new EOFException("Expected " + startLength + " bytes, but could only read " + (startLength - length));
         }

         offset += n;
         length -= n;
      }
   }

   private String readShortString() throws IOException {
      int length = this.read();
      if (length == (Tag.T_REFERENCE.tag & 255)) {
         int i = this.version <= 1 ? this.readInt() : this.readUnsignedInt();
         if (i >= 0 && i <= this.readShortStrings.size()) {
            return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.readShortStrings.get(i));
         } else {
            throw new StreamCorruptedException("Invalid short string reference " + i);
         }
      } else {
         byte[] d = new byte[length];
         this.readFully(d);
         String s = new String(d, StandardCharsets.UTF_8);
         if (length > 4) {
            this.readShortStrings.add(s);
         }

         return s;
      }
   }

   @Override
   protected Tag readTag() throws IOException {
      int t = this.read();
      Tag tag = Tag.byID(t);
      if (tag == null) {
         throw new StreamCorruptedException("Invalid tag 0x" + Integer.toHexString(t));
      } else {
         return tag;
      }
   }

   private byte readByte() throws IOException {
      return (byte)this.read();
   }

   private short readShort() throws IOException {
      return (short)(this.read() << 8 | this.read());
   }

   private short readUnsignedShort() throws IOException {
      int b = this.read();
      return (b & 128) != 0 ? (short)(b & -129) : (short)(b << 8 | this.read());
   }

   private int readInt() throws IOException {
      return this.read() << 24 | this.read() << 16 | this.read() << 8 | this.read();
   }

   private int readUnsignedInt() throws IOException {
      int b = this.read();
      return (b & 128) != 0 ? (b & -129) << 8 | this.read() : b << 24 | this.read() << 16 | this.read() << 8 | this.read();
   }

   private long readLong() throws IOException {
      return (long)this.in.read() << 56
         | (long)this.in.read() << 48
         | (long)this.in.read() << 40
         | (long)this.in.read() << 32
         | (long)this.in.read() << 24
         | (long)this.in.read() << 16
         | (long)this.in.read() << 8
         | this.read();
   }

   private float readFloat() throws IOException {
      return Float.intBitsToFloat(this.readInt());
   }

   private double readDouble() throws IOException {
      return Double.longBitsToDouble(this.readLong());
   }

   private char readChar() throws IOException {
      return (char)this.readShort();
   }

   private boolean readBoolean() throws IOException {
      int r = this.read();
      if (r == 0) {
         return false;
      } else if (r == 1) {
         return true;
      } else {
         throw new StreamCorruptedException("Invalid boolean value " + r);
      }
   }

   @Override
   protected Object readPrimitive(Tag type) throws IOException {
      switch (type) {
         case T_BYTE:
            return this.readByte();
         case T_SHORT:
            return this.readShort();
         case T_INT:
            return this.readInt();
         case T_LONG:
            return this.readLong();
         case T_FLOAT:
            return this.readFloat();
         case T_DOUBLE:
            return this.readDouble();
         case T_CHAR:
            return this.readChar();
         case T_BOOLEAN:
            return this.readBoolean();
         default:
            throw new YggdrasilException("Internal error; " + type);
      }
   }

   @Override
   protected Object readPrimitive_(Tag type) throws IOException {
      return this.readPrimitive(type);
   }

   @Override
   protected String readString() throws IOException {
      int length = this.readUnsignedInt();
      byte[] d = new byte[length];
      this.readFully(d);
      return new String(d, StandardCharsets.UTF_8);
   }

   @Override
   protected Class<?> readArrayComponentType() throws IOException {
      return this.readClass();
   }

   @Override
   protected int readArrayLength() throws IOException {
      return this.readUnsignedInt();
   }

   @Override
   protected Class<?> readEnumType() throws IOException {
      return this.yggdrasil.getClass(this.readShortString());
   }

   @Override
   protected String readEnumID() throws IOException {
      return this.readShortString();
   }

   @Override
   protected Class<?> readClass() throws IOException {
      label38: {
         int dimensions = 0;

         Tag type;
         while ((type = this.readTag()) == Tag.T_ARRAY) {
            dimensions++;
         }

         Class clazz;

         clazz = switch (type) {
            case T_BYTE, T_SHORT, T_INT, T_LONG, T_FLOAT, T_DOUBLE, T_CHAR, T_BOOLEAN, T_BOOLEAN_OBJ, T_BYTE_OBJ, T_CHAR_OBJ, T_DOUBLE_OBJ, T_FLOAT_OBJ, T_INT_OBJ, T_LONG_OBJ, T_SHORT_OBJ, T_CLASS, T_STRING -> {
               assert type.type != null;

               yield type.type;

               while (dimensions-- > 0) {
                  clazz = CollectionUtils.arrayType(clazz);
               }

               return clazz;
            }
            case T_OBJECT, T_ENUM -> this.yggdrasil.getClass(this.readShortString());
            case T_NULL, T_REFERENCE -> throw new StreamCorruptedException("unexpected tag " + type);
            default -> throw new YggdrasilException("Internal error; " + type);
         };
      }
   }

   @Override
   protected int readReference() throws IOException {
      return this.readUnsignedInt();
   }

   @Override
   protected Class<?> readObjectType() throws IOException {
      return this.yggdrasil.getClass(this.readShortString());
   }

   @Override
   protected short readNumFields() throws IOException {
      return this.readUnsignedShort();
   }

   @Override
   protected String readFieldID() throws IOException {
      return this.readShortString();
   }

   @Override
   public void close() throws IOException {
      try {
         this.read();
         throw new StreamCorruptedException("Stream still has data, at least " + (1 + this.in.available()) + " bytes remain");
      } catch (EOFException var5) {
      } finally {
         this.in.close();
      }
   }
}
