package com.fastasyncworldedit.core.jnbt.streamer;

import com.sk89q.jnbt.NBTConstants;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import java.io.DataInputStream;
import java.io.IOException;
import org.apache.logging.log4j.Logger;

public class StreamDelegate {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final byte[][] ZERO_KEYS = new byte[0][];
   private static final StreamDelegate[] ZERO_VALUES = new StreamDelegate[0];
   private byte[] buffer;
   private byte[][] keys = ZERO_KEYS;
   private StreamDelegate[] values = ZERO_VALUES;
   private LazyReader lazyReader;
   private ValueReader elemReader;
   private InfoReader infoReader;
   private ValueReader valueReader;

   public StreamDelegate addAndGetParent(String name) {
      this.add(name);
      return this;
   }

   public StreamDelegate add() {
      return this.add("");
   }

   public StreamDelegate add(String name) {
      return this.add(name, new StreamDelegate());
   }

   private StreamDelegate add(String name, StreamDelegate scope) {
      if (this.valueReader != null) {
         LOGGER.warn("Scope {} | {} may not run, as the stream is only read once, and a value reader is already set", name, scope);
      }

      byte[] bytes = name.getBytes(NBTConstants.CHARSET);
      int maxSize = bytes.length;
      byte[][] tmpKeys = new byte[this.keys.length + 1][];
      StreamDelegate[] tmpValues = new StreamDelegate[this.keys.length + 1];
      tmpKeys[this.keys.length] = bytes;
      tmpValues[this.keys.length] = scope;

      int i;
      for (i = 0; i < this.keys.length; i++) {
         byte[] key = this.keys[i];
         if (key.length >= bytes.length) {
            tmpKeys[i] = bytes;
            tmpValues[i] = scope;
            break;
         }

         tmpKeys[i] = key;
         tmpValues[i] = this.values[i];
         maxSize = Math.max(maxSize, key.length);
      }

      while (i < this.keys.length) {
         byte[] key = this.keys[i];
         tmpKeys[i + 1] = key;
         tmpValues[i + 1] = this.values[i];
         maxSize = Math.max(maxSize, key.length);
         i++;
      }

      this.keys = tmpKeys;
      this.values = tmpValues;
      if (this.buffer == null || this.buffer.length < maxSize) {
         this.buffer = new byte[maxSize];
      }

      return scope;
   }

   public StreamDelegate get0() {
      return this.keys.length > 0 && this.keys[0].length == 0 ? this.values[0] : null;
   }

   public StreamDelegate get(DataInputStream is) throws IOException {
      int nameLength = is.readShort() & '\uffff';
      if (nameLength == 0 && this.keys.length > 0 && this.keys[0].length == 0) {
         return this.values[0];
      } else if (nameLength > this.buffer.length) {
         is.skipBytes(nameLength);
         return null;
      } else {
         label90: {
            int index;
            label89: {
               index = 0;
               switch (this.keys.length) {
                  case 0:
                     break label90;
                  case 1:
                     break label89;
               }

               while (true) {
                  if (index < this.keys.length) {
                     byte[] key = this.keys[index];
                     if (key.length < nameLength) {
                        index++;
                        continue;
                     }

                     if (key.length != nameLength) {
                        break label90;
                     }
                  }

                  if (index != this.keys.length - 1) {
                     int max;
                     for (max = index + 1; max < this.keys.length; max++) {
                        byte[] keyx = this.keys[max];
                        if (keyx.length != nameLength) {
                           break;
                        }
                     }

                     if (index != max) {
                        is.readFully(this.buffer, 0, nameLength);

                        label56:
                        for (int i = index; i < max; i++) {
                           byte[] keyx = this.keys[i];

                           for (int j = 0; j < nameLength; j++) {
                              if (this.buffer[j] != keyx[j]) {
                                 continue label56;
                              }
                           }

                           return this.values[i];
                        }

                        return null;
                     }
                  }
                  break;
               }
            }

            byte[] keyx = this.keys[index];
            if (keyx.length == nameLength) {
               int i = 0;

               while (true) {
                  if (nameLength <= 0) {
                     return this.values[index];
                  }

                  byte b = is.readByte();
                  this.buffer[i] = b;
                  if (b != keyx[i]) {
                     nameLength--;
                     break;
                  }

                  nameLength--;
                  i++;
               }
            }
         }

         is.skipBytes(nameLength);
         return null;
      }
   }

   public StreamDelegate withLong(LongValueReader valueReader) {
      return this.withElem(valueReader);
   }

   public StreamDelegate withInt(IntValueReader valueReader) {
      return this.withElem(valueReader);
   }

   public StreamDelegate withValue(ValueReader valueReader) {
      if (this.keys.length != 0) {
         LOGGER.warn("Reader {} may not run, as the stream is only read once, and a value reader is already set", valueReader);
      }

      this.valueReader = valueReader;
      return this;
   }

   public StreamDelegate withStream(LazyReader lazyReader) {
      this.lazyReader = lazyReader;
      return this;
   }

   public StreamDelegate withElem(ValueReader elemReader) {
      this.elemReader = elemReader;
      return this;
   }

   public StreamDelegate withInfo(InfoReader infoReader) {
      this.infoReader = infoReader;
      return this;
   }

   public void acceptRoot(NBTInputStream is, int type, int depth) throws IOException {
      if (this.lazyReader != null) {
         this.lazyReader.apply(0, is);
      } else if (this.elemReader != null) {
         Object raw = is.readTagPayloadRaw(type, depth);
         this.elemReader.apply(0, raw);
      } else if (this.valueReader != null) {
         Object raw = is.readTagPayloadRaw(type, depth);
         this.valueReader.apply(0, raw);
      } else {
         is.readTagPayloadLazy(type, depth + 1, this);
      }
   }

   public ValueReader getValueReader() {
      return this.valueReader;
   }

   public ValueReader getElemReader() {
      return this.elemReader;
   }

   public void acceptInfo(int length, int type) throws IOException {
      if (this.infoReader != null) {
         this.infoReader.apply(length, type);
      }
   }

   public boolean acceptLazy(int length, NBTInputStream is) throws IOException {
      if (this.lazyReader != null) {
         this.lazyReader.apply(length, is);
         return true;
      } else {
         return false;
      }
   }
}
