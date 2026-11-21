package com.sk89q.jnbt;

import com.fastasyncworldedit.core.internal.io.LittleEndianOutputStream;
import com.google.common.base.Preconditions;
import java.io.Closeable;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map.Entry;

@Deprecated(
   forRemoval = true
)
public final class NBTOutputStream extends OutputStream implements Closeable, DataOutput {
   private final DataOutput os;

   public NBTOutputStream(OutputStream os) {
      this((DataOutput)(os instanceof DataOutput ? (DataOutput)os : new DataOutputStream(os)));
   }

   public NBTOutputStream(DataOutput os) {
      this.os = os;
   }

   public NBTOutputStream(DataOutputStream os) {
      this.os = os;
   }

   public NBTOutputStream(OutputStream os, boolean littleEndian) throws IOException {
      this((OutputStream)(littleEndian ? new LittleEndianOutputStream(os) : os));
   }

   public DataOutput getOutputStream() {
      return this.os;
   }

   public void writeNamedTag(String name, Tag<?, ?> tag) throws IOException {
      Preconditions.checkNotNull(name);
      Preconditions.checkNotNull(tag);
      int type = tag.getTypeCode();
      this.writeNamedTagName(name, type);
      if (type == 0) {
         throw new IOException("Named TAG_End not permitted.");
      } else {
         this.writeTagPayload(tag);
      }
   }

   public void writeNamedTag(String name, String value) throws IOException {
      Preconditions.checkNotNull(name);
      Preconditions.checkNotNull(value);
      int type = 8;
      this.writeNamedTagName(name, type);
      byte[] bytes = value.getBytes(NBTConstants.CHARSET);
      this.os.writeShort(bytes.length);
      this.os.write(bytes);
   }

   public void writeNamedTag(String name, int value) throws IOException {
      Preconditions.checkNotNull(name);
      int type = 3;
      this.writeNamedTagName(name, type);
      this.os.writeInt(value);
   }

   public void writeNamedTag(String name, byte value) throws IOException {
      Preconditions.checkNotNull(name);
      int type = 1;
      this.writeNamedTagName(name, type);
      this.os.writeByte(value);
   }

   public void writeNamedTag(String name, short value) throws IOException {
      Preconditions.checkNotNull(name);
      int type = 2;
      this.writeNamedTagName(name, type);
      this.os.writeShort(value);
   }

   public void writeNamedTag(String name, long value) throws IOException {
      Preconditions.checkNotNull(name);
      int type = 4;
      this.writeNamedTagName(name, type);
      this.os.writeLong(value);
   }

   public void writeNamedTag(String name, byte[] bytes) throws IOException {
      Preconditions.checkNotNull(name);
      int type = 7;
      this.writeNamedTagName(name, type);
      this.os.writeInt(bytes.length);
      this.os.write(bytes);
   }

   public void writeNamedTag(String name, int[] data) throws IOException {
      Preconditions.checkNotNull(name);
      int type = 11;
      this.writeNamedTagName(name, type);
      this.os.writeInt(data.length);

      for (int aData : data) {
         this.os.writeInt(aData);
      }
   }

   public void writeNamedEmptyList(String name) throws IOException {
      this.writeNamedEmptyList(name, 10);
   }

   private void writeNamedEmptyList(String name, int type) throws IOException {
      this.writeNamedTagName(name, 9);
      this.os.writeByte(type);
      this.os.writeInt(0);
   }

   public void writeNamedTagName(String name, int type) throws IOException {
      this.os.writeByte(type);
      this.os.writeUTF(name);
   }

   public void writeLazyCompoundTag(String name, NBTOutputStream.LazyWrite next) throws IOException {
      byte[] nameBytes = name.getBytes(NBTConstants.CHARSET);
      this.os.writeByte(10);
      this.os.writeShort(nameBytes.length);
      this.os.write(nameBytes);
      next.write(this);
      this.os.writeByte(0);
   }

   public void writeTag(Tag tag) throws IOException {
      int type = tag.getTypeCode();
      this.os.writeByte(type);
      this.writeTagPayload(tag);
   }

   public void writeEndTag() throws IOException {
      this.os.writeByte(0);
   }

   public void writeTagPayload(Tag tag) throws IOException {
      int type = tag.getTypeCode();
      switch (type) {
         case 0:
            this.writeEndTagPayload((EndTag)tag);
            break;
         case 1:
            this.writeByteTagPayload((ByteTag)tag);
            break;
         case 2:
            this.writeShortTagPayload((ShortTag)tag);
            break;
         case 3:
            this.writeIntTagPayload((IntTag)tag);
            break;
         case 4:
            this.writeLongTagPayload((LongTag)tag);
            break;
         case 5:
            this.writeFloatTagPayload((FloatTag)tag);
            break;
         case 6:
            this.writeDoubleTagPayload((DoubleTag)tag);
            break;
         case 7:
            this.writeByteArrayTagPayload((ByteArrayTag)tag);
            break;
         case 8:
            this.writeStringTagPayload((StringTag)tag);
            break;
         case 9:
            this.writeListTagPayload((ListTag)tag);
            break;
         case 10:
            this.writeCompoundTagPayload((CompoundTag)tag);
            break;
         case 11:
            this.writeIntArrayTagPayload((IntArrayTag)tag);
            break;
         case 12:
            this.writeLongArrayTagPayload((LongArrayTag)tag);
            break;
         default:
            throw new IOException("Invalid tag type: " + type + ".");
      }
   }

   private void writeByteTagPayload(ByteTag tag) throws IOException {
      this.os.writeByte(tag.getValue());
   }

   private void writeByteArrayTagPayload(ByteArrayTag tag) throws IOException {
      byte[] bytes = tag.getValue();
      this.os.writeInt(bytes.length);
      this.os.write(bytes);
   }

   private void writeCompoundTagPayload(CompoundTag tag) throws IOException {
      for (Entry<String, Tag<?, ?>> entry : tag.getValue().entrySet()) {
         this.writeNamedTag(entry.getKey(), entry.getValue());
      }

      this.os.writeByte(0);
   }

   private void writeListTagPayload(ListTag tag) throws IOException {
      Class<? extends Tag<?, ?>> clazz = tag.getType();
      if (clazz == null) {
         clazz = CompoundTag.class;
      }

      List<Tag> tags = tag.getValue();
      int size = tags.size();
      if (!tags.isEmpty()) {
         Tag tag0 = tags.get(0);
         this.os.writeByte(tag0.getTypeCode());
      } else {
         this.os.writeByte(NBTUtils.getTypeCode(clazz));
      }

      this.os.writeInt(size);

      for (Tag tag1 : tags) {
         this.writeTagPayload(tag1);
      }
   }

   private void writeStringTagPayload(StringTag tag) throws IOException {
      byte[] bytes = tag.getValue().getBytes(NBTConstants.CHARSET);
      this.os.writeShort(bytes.length);
      this.os.write(bytes);
   }

   private void writeDoubleTagPayload(DoubleTag tag) throws IOException {
      this.os.writeDouble(tag.getValue());
   }

   private void writeFloatTagPayload(FloatTag tag) throws IOException {
      this.os.writeFloat(tag.getValue());
   }

   private void writeLongTagPayload(LongTag tag) throws IOException {
      this.os.writeLong(tag.getValue());
   }

   private void writeIntTagPayload(IntTag tag) throws IOException {
      this.os.writeInt(tag.getValue());
   }

   private void writeShortTagPayload(ShortTag tag) throws IOException {
      this.os.writeShort(tag.getValue());
   }

   private void writeEndTagPayload(EndTag tag) {
   }

   private void writeIntArrayTagPayload(IntArrayTag tag) throws IOException {
      int[] data = tag.getValue();
      this.os.writeInt(data.length);

      for (int aData : data) {
         this.os.writeInt(aData);
      }
   }

   private void writeLongArrayTagPayload(LongArrayTag tag) throws IOException {
      long[] data = tag.getValue();
      this.os.writeInt(data.length);

      for (long aData : data) {
         this.os.writeLong(aData);
      }
   }

   @Override
   public void close() throws IOException {
      if (this.os instanceof Closeable) {
         ((Closeable)this.os).close();
      }
   }

   @Override
   public void write(int b) throws IOException {
      this.os.write(b);
   }

   @Override
   public void write(byte[] b) throws IOException {
      this.os.write(b);
   }

   @Override
   public void write(byte[] b, int off, int len) throws IOException {
      this.os.write(b, off, len);
   }

   @Override
   public void writeBoolean(boolean v) throws IOException {
      this.os.writeBoolean(v);
   }

   @Override
   public void writeByte(int v) throws IOException {
      this.os.writeByte(v);
   }

   @Override
   public void writeShort(int v) throws IOException {
      this.os.writeShort(v);
   }

   @Override
   public void writeChar(int v) throws IOException {
      this.os.writeChar(v);
   }

   @Override
   public void writeInt(int v) throws IOException {
      this.os.writeInt(v);
   }

   @Override
   public void writeLong(long v) throws IOException {
      this.os.writeLong(v);
   }

   @Override
   public void writeFloat(float v) throws IOException {
      this.os.writeFloat(v);
   }

   @Override
   public void writeDouble(double v) throws IOException {
      this.os.writeDouble(v);
   }

   @Override
   public void writeBytes(String s) throws IOException {
      this.os.writeBytes(s);
   }

   @Override
   public void writeChars(String s) throws IOException {
      this.os.writeChars(s);
   }

   @Override
   public void writeUTF(String s) throws IOException {
      this.os.writeUTF(s);
   }

   @Override
   public void flush() throws IOException {
      if (this.os instanceof Flushable) {
         ((Flushable)this.os).flush();
      }
   }

   public interface LazyWrite {
      void write(NBTOutputStream out) throws IOException;
   }
}
