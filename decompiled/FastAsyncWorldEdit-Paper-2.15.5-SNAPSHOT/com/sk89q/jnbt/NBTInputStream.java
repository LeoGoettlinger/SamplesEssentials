package com.sk89q.jnbt;

import com.fastasyncworldedit.core.jnbt.streamer.StreamDelegate;
import com.fastasyncworldedit.core.jnbt.streamer.ValueReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import org.jetbrains.annotations.ApiStatus.Internal;

@Deprecated(
   forRemoval = true
)
public final class NBTInputStream implements Closeable {
   private final DataInputStream is;
   private byte[] buf;

   public NBTInputStream(InputStream is) {
      this.is = new DataInputStream(is);
   }

   public NBTInputStream(DataInputStream dis) {
      this.is = dis;
   }

   public void mark(int mark) {
      this.is.mark(mark);
   }

   public void reset() throws IOException {
      this.is.reset();
   }

   public NamedTag readNamedTag() throws IOException {
      return this.readNamedTag(0);
   }

   private NamedTag readNamedTag(int depth) throws IOException {
      int type = this.is.readByte() & 255;
      return new NamedTag(this.readNamedTagName(type), this.readTagPayload(type, depth));
   }

   public Tag readTag() throws IOException {
      int type = this.is.readByte();
      return this.readTagPayload(type, 0);
   }

   public void readNamedTagLazy(StreamDelegate scope) throws IOException {
      try {
         int type = this.is.readByte();
         if (type == 0) {
            return;
         }

         StreamDelegate child = scope.get(this.is);
         if (child != null) {
            child.acceptRoot(this, type, 0);
         } else {
            this.readTagPayloadLazy(type, 0);
         }
      } catch (Throwable var4) {
         var4.printStackTrace();
      }
   }

   public String readNamedTagName(int type) throws IOException {
      if (type != 0) {
         int nameLength = this.is.readShort() & '\uffff';
         byte[] nameBytes = new byte[nameLength];
         this.is.readFully(nameBytes);
         return new String(nameBytes, NBTConstants.CHARSET);
      } else {
         return "";
      }
   }

   public void readTagPayloadLazy(int type, int depth) throws IOException {
      switch (type) {
         case 0:
            return;
         case 1:
            this.is.skipBytes(1);
            return;
         case 2:
            this.is.skipBytes(2);
            return;
         case 3:
            this.is.skipBytes(4);
            return;
         case 4:
            this.is.skipBytes(8);
            return;
         case 5:
            this.is.skipBytes(4);
            return;
         case 6:
            this.is.skipBytes(8);
            return;
         case 7:
            this.is.skipBytes(this.is.readInt());
            return;
         case 8: {
            int length = this.is.readShort() & '\uffff';
            this.is.skipBytes(length);
            return;
         }
         case 9: {
            int childType = this.is.readByte();
            int length = this.is.readInt();

            for (int i = 0; i < length; i++) {
               this.readTagPayloadLazy(childType, depth + 1);
            }

            return;
         }
         case 10:
            depth++;

            while (true) {
               int childType = this.is.readByte();
               if (childType == 0) {
                  return;
               }

               this.is.skipBytes(this.is.readShort() & '\uffff');
               this.readTagPayloadLazy(childType, depth + 1);
            }
         case 11:
            this.is.skipBytes(this.is.readInt() << 2);
            return;
         case 12:
            this.is.skipBytes(this.is.readInt() << 3);
            return;
         default:
            throw new IOException("Invalid tag type: " + type + ".");
      }
   }

   public void readTagPayloadLazy(int type, int depth, StreamDelegate scope) throws IOException {
      switch (type) {
         case 0:
            return;
         case 1:
            ValueReader valuexxxxxx = scope.getValueReader();
            if (valuexxxxxx == null) {
               valuexxxxxx = scope.getElemReader();
            }

            if (valuexxxxxx != null) {
               valuexxxxxx.applyInt(0, this.is.readByte());
            } else {
               this.is.skipBytes(1);
            }

            return;
         case 2:
            ValueReader valuexxxxx = scope.getValueReader();
            if (valuexxxxx == null) {
               valuexxxxx = scope.getElemReader();
            }

            if (valuexxxxx != null) {
               valuexxxxx.applyInt(0, this.is.readShort());
            } else {
               this.is.skipBytes(2);
            }

            return;
         case 3:
            ValueReader valuexxxx = scope.getValueReader();
            if (valuexxxx == null) {
               valuexxxx = scope.getElemReader();
            }

            if (valuexxxx != null) {
               valuexxxx.applyInt(0, this.is.readInt());
            } else {
               this.is.skipBytes(4);
            }

            return;
         case 4:
            ValueReader valuexxx = scope.getValueReader();
            if (valuexxx == null) {
               valuexxx = scope.getElemReader();
            }

            if (valuexxx != null) {
               valuexxx.applyLong(0, this.is.readLong());
            } else {
               this.is.skipBytes(8);
            }

            return;
         case 5:
            ValueReader valuexx = scope.getValueReader();
            if (valuexx == null) {
               valuexx = scope.getElemReader();
            }

            if (valuexx != null) {
               valuexx.applyFloat(0, this.is.readFloat());
            } else {
               this.is.skipBytes(4);
            }

            return;
         case 6:
            ValueReader valuex = scope.getValueReader();
            if (valuex == null) {
               valuex = scope.getElemReader();
            }

            if (valuex != null) {
               valuex.applyDouble(0, this.is.readDouble());
            } else {
               this.is.skipBytes(8);
            }

            return;
         case 7:
            int lengthxxx = this.is.readInt();
            scope.acceptInfo(lengthxxx, 1);
            if (scope.acceptLazy(lengthxxx, this)) {
               return;
            } else {
               ValueReader valueReaderxx = scope.getValueReader();
               if (valueReaderxx != null) {
                  byte[] arr = new byte[lengthxxx];
                  this.is.readFully(arr);
                  valueReaderxx.apply(0, arr);
                  return;
               } else {
                  valueReaderxx = scope.getElemReader();
                  if (valueReaderxx == null) {
                     this.is.skipBytes(lengthxxx);
                     return;
                  }

                  int i = 0;
                  DataInputStream dis = this.is;
                  if (lengthxxx > 1024) {
                     if (this.buf == null) {
                        this.buf = new byte[1024];
                     }

                     for (int left = lengthxxx; left > 1024; left -= 1024) {
                        dis.readFully(this.buf);

                        for (byte b : this.buf) {
                           valueReaderxx.applyInt(i++, b & 255);
                        }
                     }
                  }

                  while (i < lengthxxx) {
                     valueReaderxx.applyInt(i, dis.read());
                     i++;
                  }

                  return;
               }
            }
         case 8:
            ValueReader value = scope.getValueReader();
            if (value == null) {
               value = scope.getElemReader();
            }

            int length = this.is.readShort() & '\uffff';
            if (value != null) {
               byte[] bytes = new byte[length];
               this.is.readFully(bytes);
               value.apply(0, new String(bytes, NBTConstants.CHARSET));
            } else {
               this.is.skipBytes(length);
            }

            return;
         case 9: {
            int childTypex = this.is.readByte();
            int lengthxx = this.is.readInt();
            scope.acceptInfo(lengthxx, childTypex);
            ValueReader valueReader = scope.getValueReader();
            if (valueReader != null) {
               List<Object> tagList = this.readListRaw(depth, childTypex, lengthxx);
               valueReader.apply(0, tagList);
               return;
            } else {
               valueReader = scope.getElemReader();
               if (valueReader != null) {
                  for (int i = 0; i < lengthxx; i++) {
                     valueReader.apply(i, this.readTagPayloadRaw(childTypex, depth + 1));
                  }

                  return;
               }

               StreamDelegate child = scope.get0();
               if (child == null) {
                  for (int i = 0; i < lengthxx; i++) {
                     this.readTagPayloadLazy(childTypex, depth + 1);
                  }
               } else {
                  for (int i = 0; i < lengthxx; i++) {
                     this.readTagPayloadLazy(childTypex, depth + 1, child);
                  }
               }

               return;
            }
         }
         case 10: {
            scope.acceptInfo(-1, 1);
            ValueReader valueReader = scope.getValueReader();
            if (valueReader != null) {
               valueReader.apply(0, this.readTagPayloadRaw(type, depth));
               return;
            } else {
               valueReader = scope.getElemReader();
               if (valueReader == null) {
                  while (true) {
                     int childType = this.is.readByte();
                     if (childType == 0) {
                        return;
                     }

                     StreamDelegate child = scope.get(this.is);
                     if (child == null) {
                        this.readTagPayloadLazy(childType, depth + 1);
                     } else {
                        this.readTagPayloadLazy(childType, depth + 1, child);
                     }
                  }
               } else {
                  int i = 0;

                  while (true) {
                     int childTypex = this.is.readByte();
                     if (childTypex == 0) {
                        return;
                     }

                     String key = this.readNamedTagName(childTypex);
                     Object valuexxxxxxx = this.readTagPayloadRaw(childTypex, depth + 1);
                     SimpleEntry<String, Object> entry = new SimpleEntry<>(key, valuexxxxxxx);
                     valueReader.apply(i, entry);
                     i++;
                  }
               }
            }
         }
         case 11:
            int lengthx = this.is.readInt();
            scope.acceptInfo(lengthx, 3);
            if (scope.acceptLazy(lengthx, this)) {
               return;
            } else {
               ValueReader valueReaderx = scope.getValueReader();
               if (valueReaderx != null) {
                  valueReaderx.apply(0, this.readIntArrayRaw(lengthx));
                  return;
               } else {
                  valueReaderx = scope.getElemReader();
                  if (valueReaderx == null) {
                     this.is.skipBytes(lengthx << 2);
                     return;
                  }

                  for (int i = 0; i < lengthx; i++) {
                     valueReaderx.applyInt(i, this.is.readInt());
                  }

                  return;
               }
            }
         case 12: {
            int length = this.is.readInt();
            scope.acceptInfo(length, 4);
            if (scope.acceptLazy(length, this)) {
               return;
            } else {
               ValueReader valueReader = scope.getValueReader();
               if (valueReader != null) {
                  valueReader.apply(0, this.readLongArrayRaw(length));
                  return;
               } else {
                  valueReader = scope.getElemReader();
                  if (valueReader == null) {
                     this.is.skipBytes(length << 3);
                     return;
                  }

                  for (int i = 0; i < length; i++) {
                     valueReader.applyLong(i, this.is.readLong());
                  }

                  return;
               }
            }
         }
         default:
            throw new IOException("Invalid tag type: " + type + ".");
      }
   }

   private List<Object> readListRaw(int depth, int childType, int length) throws IOException {
      List<Object> tagList = new ArrayList<>(length);

      for (int i = 0; i < length; i++) {
         Object tag = this.readTagPayloadRaw(childType, depth + 1);
         if (tag == null) {
            throw new IOException("TAG_End not permitted in a list.");
         }

         tagList.add(tag);
      }

      return tagList;
   }

   public static int getSize(int type) {
      switch (type) {
         case 0:
         case 1:
         default:
            return 1;
         case 2:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
            return 2;
         case 3:
         case 5:
            return 4;
         case 4:
         case 6:
            return 8;
      }
   }

   public Object readTagPayloadRaw(int type, int depth) throws IOException {
      switch (type) {
         case 0:
            if (depth == 0) {
               throw new IOException("TAG_End found without a TAG_Compound/TAG_List tag preceding it.");
            }

            return null;
         case 1:
            return this.is.readByte();
         case 2:
            return this.is.readShort();
         case 3:
            return this.is.readInt();
         case 4:
            return this.is.readLong();
         case 5:
            return this.is.readFloat();
         case 6:
            return this.is.readDouble();
         case 7: {
            int length = this.is.readInt();
            byte[] bytes = new byte[length];
            this.is.readFully(bytes);
            return bytes;
         }
         case 8: {
            int length = this.is.readShort() & '\uffff';
            byte[] bytes = new byte[length];
            this.is.readFully(bytes);
            return new String(bytes, NBTConstants.CHARSET);
         }
         case 9: {
            int childType = this.is.readByte();
            int length = this.is.readInt();
            return this.readListRaw(depth, childType, length);
         }
         case 10:
            Map<String, Object> tagMap = new HashMap<>();

            while (true) {
               int childTypex = this.is.readByte();
               if (childTypex == 0) {
                  return tagMap;
               }

               String name = this.readNamedTagName(childTypex);
               Object value = this.readTagPayloadRaw(childTypex, depth + 1);
               tagMap.put(name, value);
            }
         case 11: {
            int length = this.is.readInt();
            return this.readIntArrayRaw(length);
         }
         case 12: {
            int length = this.is.readInt();
            return this.readLongArrayRaw(length);
         }
         default:
            throw new IOException("Invalid tag type: " + type + ".");
      }
   }

   private int[] readIntArrayRaw(int length) throws IOException {
      int[] data = new int[length];
      if (this.buf == null) {
         this.buf = new byte[1024];
      }

      int index = 0;

      while (length > 0) {
         int toRead = Math.min(length << 2, this.buf.length);
         this.is.readFully(this.buf, 0, toRead);

         for (int i = 0; i < toRead; index++) {
            data[index] = ((this.buf[i] & 255) << 24) + ((this.buf[i + 1] & 255) << 16) + ((this.buf[i + 2] & 255) << 8) + (this.buf[i + 3] & 255);
            i += 4;
         }

         length -= toRead;
      }

      return data;
   }

   private long[] readLongArrayRaw(int length) throws IOException {
      long[] data = new long[length];
      if (this.buf == null) {
         this.buf = new byte[1024];
      }

      int index = 0;

      while (length > 0) {
         int toRead = Math.min(length << 3, this.buf.length);
         this.is.readFully(this.buf, 0, toRead);

         for (int i = 0; i < toRead; index++) {
            data[index] = (long)this.buf[i] << 56
               | (long)(this.buf[i + 1] & 255) << 48
               | (long)(this.buf[i + 2] & 255) << 40
               | (long)(this.buf[i + 3] & 255) << 32
               | (long)(this.buf[i + 4] & 255) << 24
               | (this.buf[i + 5] & 255) << 16
               | (this.buf[i + 6] & 255) << 8
               | this.buf[i + 7] & 255;
            i += 8;
         }

         length -= toRead;
      }

      return data;
   }

   public Tag readTagPayload(int type, int depth) throws IOException {
      switch (type) {
         case 0:
            if (depth == 0) {
               throw new IOException("TAG_End found without a TAG_Compound/TAG_List tag preceding it.");
            }

            return new EndTag();
         case 1:
            return new ByteTag(this.is.readByte());
         case 2:
            return new ShortTag(this.is.readShort());
         case 3:
            return new IntTag(this.is.readInt());
         case 4:
            return new LongTag(this.is.readLong());
         case 5:
            return new FloatTag(this.is.readFloat());
         case 6:
            return new DoubleTag(this.is.readDouble());
         case 7: {
            int length = this.is.readInt();
            byte[] bytes = new byte[length];
            this.is.readFully(bytes);
            return new ByteArrayTag(bytes);
         }
         case 8: {
            int length = this.is.readShort() & '\uffff';
            byte[] bytes = new byte[length];
            this.is.readFully(bytes);
            return new StringTag(new String(bytes, NBTConstants.CHARSET));
         }
         case 9: {
            int childType = this.is.readByte();
            int length = this.is.readInt();
            List<Tag> tagList = new ArrayList<>();

            for (int i = 0; i < length; i++) {
               Tag tag = this.readTagPayload(childType, depth + 1);
               if (tag instanceof EndTag) {
                  throw new IOException("TAG_End not permitted in a list.");
               }

               tagList.add(tag);
            }

            return new ListTag<>(NBTUtils.getTypeClass(childType), tagList);
         }
         case 10:
            Map<String, Tag<?, ?>> tagMap = new HashMap<>();

            while (true) {
               NamedTag namedTag = this.readNamedTag(depth + 1);
               Tag tag = namedTag.getTag();
               if (tag instanceof EndTag) {
                  return new CompoundTag(tagMap);
               }

               tagMap.put(namedTag.getName(), tag);
            }
         case 11: {
            int length = this.is.readInt();
            int[] data = new int[length];

            for (int i = 0; i < length; i++) {
               data[i] = this.is.readInt();
            }

            return new IntArrayTag(data);
         }
         case 12: {
            int length = this.is.readInt();
            long[] longData = new long[length];

            for (int i = 0; i < length; i++) {
               longData[i] = this.is.readLong();
            }

            return new LongArrayTag(longData);
         }
         default:
            throw new IOException("Invalid tag type: " + type + ".");
      }
   }

   @Override
   public void close() throws IOException {
      this.is.close();
   }

   public Iterator<CompoundTag> toIterator() {
      return new Iterator<CompoundTag>() {
         private CompoundTag last = this.read();

         public CompoundTag read() {
            try {
               return (CompoundTag)NBTInputStream.this.readTag();
            } catch (Exception var3) {
               try {
                  NBTInputStream.this.is.close();
               } catch (IOException var2) {
                  var2.printStackTrace();
               }

               return null;
            }
         }

         @Override
         public boolean hasNext() {
            return this.last != null || (this.last = this.read()) != null;
         }

         public CompoundTag next() {
            CompoundTag tmp = this.last;
            if (tmp == null) {
               tmp = this.read();
            }

            this.last = null;
            return tmp;
         }

         @Override
         public void remove() {
            throw new IllegalArgumentException("CANNOT REMOVE");
         }
      };
   }

   @Internal
   public DataInputStream dataInputStream() {
      return this.is;
   }
}
