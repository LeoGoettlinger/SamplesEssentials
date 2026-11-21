package com.viaversion.nbt.tag;

import com.viaversion.nbt.io.TagRegistry;
import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.stringified.SNBT;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.jetbrains.annotations.Nullable;

public final class CompoundTag implements Tag, Iterable<Entry<String, Tag>> {
   public static final int ID = 10;
   private static final Map<String, Tag> EMPTY = Collections.emptyMap();
   private Map<String, Tag> value;

   public CompoundTag() {
      this.value = EMPTY;
   }

   public CompoundTag(Map<String, Tag> value) {
      this.setValue(value);
   }

   public CompoundTag(LinkedHashMap<String, Tag> value) {
      this.setValue(value);
   }

   public static CompoundTag read(DataInput in, TagLimiter tagLimiter, int nestingLevel) throws IOException {
      tagLimiter.checkLevel(nestingLevel);
      int newNestingLevel = nestingLevel + 1;
      CompoundTag compoundTag = new CompoundTag();
      Map<String, Tag> value = null;

      while (true) {
         tagLimiter.countByte();
         int id = in.readByte();
         if (id == 0) {
            return compoundTag;
         }

         String name = in.readUTF();
         tagLimiter.countBytes(2 * name.length());

         Tag tag;
         try {
            tag = TagRegistry.read(id, in, tagLimiter, newNestingLevel);
         } catch (IllegalArgumentException var10) {
            throw new IOException("Failed to create tag.", var10);
         }

         if (value == null) {
            value = compoundTag.valueOrCreate();
         }

         value.put(name, tag);
      }
   }

   private Map<String, Tag> valueOrCreate() {
      if (this.value == EMPTY) {
         this.value = new LinkedHashMap<>(8);
      }

      return this.value;
   }

   public Map<String, Tag> getValue() {
      return this.valueOrCreate();
   }

   @Override
   public String asRawString() {
      return this.value.toString();
   }

   public void setValue(Map<String, Tag> value) {
      this.setValue(new LinkedHashMap<>(value));
   }

   public void setValue(LinkedHashMap<String, Tag> value) {
      if (!value.isEmpty()) {
         for (Entry<String, Tag> entry : value.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
               throw new IllegalArgumentException("key and value cannot be null");
            }
         }
      }

      this.value = value;
   }

   public boolean isEmpty() {
      return this.value.isEmpty();
   }

   public boolean contains(String tagName) {
      return this.value.containsKey(tagName);
   }

   @Nullable
   public Tag get(String tagName) {
      return this.value.get(tagName);
   }

   @Nullable
   public <T extends Tag> T getUnchecked(String tagName) {
      return (T)this.value.get(tagName);
   }

   @Nullable
   public StringTag getStringTag(String tagName) {
      Tag tag = this.value.get(tagName);
      return tag instanceof StringTag ? (StringTag)tag : null;
   }

   @Nullable
   public CompoundTag getCompoundTag(String tagName) {
      Tag tag = this.value.get(tagName);
      return tag instanceof CompoundTag ? (CompoundTag)tag : null;
   }

   @Nullable
   public ListTag<?> getListTag(String tagName) {
      Tag tag = this.value.get(tagName);
      return tag instanceof ListTag ? (ListTag)tag : null;
   }

   @Nullable
   public <T extends Tag> ListTag<T> getListTag(String tagName, Class<T> type) {
      Tag tag = this.value.get(tagName);
      if (!(tag instanceof ListTag)) {
         return null;
      } else {
         Class<? extends Tag> elementType = ((ListTag)tag).getElementType();
         return elementType != type && elementType != null ? null : (ListTag)tag;
      }
   }

   @Nullable
   public ListTag<? extends NumberTag> getNumberListTag(String tagName) {
      Tag tag = this.value.get(tagName);
      if (!(tag instanceof ListTag)) {
         return null;
      } else {
         Class<? extends Tag> elementType = ((ListTag)tag).getElementType();
         return elementType != null && !NumberTag.class.isAssignableFrom(elementType) ? null : (ListTag)tag;
      }
   }

   @Nullable
   public IntTag getIntTag(String tagName) {
      Tag tag = this.value.get(tagName);
      return tag instanceof IntTag ? (IntTag)tag : null;
   }

   @Nullable
   public LongTag getLongTag(String tagName) {
      Tag tag = this.value.get(tagName);
      return tag instanceof LongTag ? (LongTag)tag : null;
   }

   @Nullable
   public ShortTag getShortTag(String tagName) {
      Tag tag = this.value.get(tagName);
      return tag instanceof ShortTag ? (ShortTag)tag : null;
   }

   @Nullable
   public ByteTag getByteTag(String tagName) {
      Tag tag = this.value.get(tagName);
      return tag instanceof ByteTag ? (ByteTag)tag : null;
   }

   @Nullable
   public FloatTag getFloatTag(String tagName) {
      Tag tag = this.value.get(tagName);
      return tag instanceof FloatTag ? (FloatTag)tag : null;
   }

   @Nullable
   public DoubleTag getDoubleTag(String tagName) {
      Tag tag = this.value.get(tagName);
      return tag instanceof DoubleTag ? (DoubleTag)tag : null;
   }

   @Nullable
   public NumberTag getNumberTag(String tagName) {
      Tag tag = this.value.get(tagName);
      return tag instanceof NumberTag ? (NumberTag)tag : null;
   }

   @Nullable
   public ByteArrayTag getByteArrayTag(String tagName) {
      Tag tag = this.value.get(tagName);
      return tag instanceof ByteArrayTag ? (ByteArrayTag)tag : null;
   }

   @Nullable
   public IntArrayTag getIntArrayTag(String tagName) {
      Tag tag = this.value.get(tagName);
      return tag instanceof IntArrayTag ? (IntArrayTag)tag : null;
   }

   @Nullable
   public LongArrayTag getLongArrayTag(String tagName) {
      Tag tag = this.value.get(tagName);
      return tag instanceof LongArrayTag ? (LongArrayTag)tag : null;
   }

   @Nullable
   public String getString(String tagName) {
      return this.getString(tagName, null);
   }

   @Nullable
   public String getString(String tagName, @Nullable String def) {
      Tag tag = this.value.get(tagName);
      return tag instanceof StringTag ? ((StringTag)tag).getValue() : def;
   }

   public int getInt(String tagName) {
      return this.getInt(tagName, 0);
   }

   public int getInt(String tagName, int def) {
      Tag tag = this.value.get(tagName);
      return tag instanceof NumberTag ? ((NumberTag)tag).asInt() : def;
   }

   public long getLong(String tagName) {
      return this.getLong(tagName, 0L);
   }

   public long getLong(String tagName, long def) {
      Tag tag = this.value.get(tagName);
      return tag instanceof NumberTag ? ((NumberTag)tag).asLong() : def;
   }

   public short getShort(String tagName) {
      return this.getShort(tagName, (short)0);
   }

   public short getShort(String tagName, short def) {
      Tag tag = this.value.get(tagName);
      return tag instanceof NumberTag ? ((NumberTag)tag).asShort() : def;
   }

   public byte getByte(String tagName) {
      return this.getByte(tagName, (byte)0);
   }

   public byte getByte(String tagName, byte def) {
      Tag tag = this.value.get(tagName);
      return tag instanceof NumberTag ? ((NumberTag)tag).asByte() : def;
   }

   public float getFloat(String tagName) {
      return this.getFloat(tagName, 0.0F);
   }

   public float getFloat(String tagName, float def) {
      Tag tag = this.value.get(tagName);
      return tag instanceof NumberTag ? ((NumberTag)tag).asFloat() : def;
   }

   public double getDouble(String tagName) {
      return this.getDouble(tagName, 0.0);
   }

   public double getDouble(String tagName, double def) {
      Tag tag = this.value.get(tagName);
      return tag instanceof NumberTag ? ((NumberTag)tag).asDouble() : def;
   }

   public boolean getBoolean(String tagName) {
      return this.getBoolean(tagName, false);
   }

   public boolean getBoolean(String tagName, boolean def) {
      Tag tag = this.value.get(tagName);
      return tag instanceof NumberTag ? ((NumberTag)tag).asBoolean() : def;
   }

   public int getIntStrict(String tagName) {
      return this.getIntStrict(tagName, 0);
   }

   public int getIntStrict(String tagName, int def) {
      Tag tag = this.value.get(tagName);
      return tag instanceof IntTag ? ((IntTag)tag).asInt() : def;
   }

   public long getLongStrict(String tagName) {
      return this.getLongStrict(tagName, 0L);
   }

   public long getLongStrict(String tagName, long def) {
      Tag tag = this.value.get(tagName);
      return tag instanceof LongTag ? ((LongTag)tag).asLong() : def;
   }

   public short getShortStrict(String tagName) {
      return this.getShortStrict(tagName, (short)0);
   }

   public short getShortStrict(String tagName, short def) {
      Tag tag = this.value.get(tagName);
      return tag instanceof ShortTag ? ((ShortTag)tag).asShort() : def;
   }

   public byte getByteStrict(String tagName) {
      return this.getByteStrict(tagName, (byte)0);
   }

   public byte getByteStrict(String tagName, byte def) {
      Tag tag = this.value.get(tagName);
      return tag instanceof ByteTag ? ((ByteTag)tag).asByte() : def;
   }

   public float getFloatStrict(String tagName) {
      return this.getFloatStrict(tagName, 0.0F);
   }

   public float getFloatStrict(String tagName, float def) {
      Tag tag = this.value.get(tagName);
      return tag instanceof FloatTag ? ((FloatTag)tag).asFloat() : def;
   }

   public double getDoubleStrict(String tagName) {
      return this.getDoubleStrict(tagName, 0.0);
   }

   public double getDoubleStrict(String tagName, double def) {
      Tag tag = this.value.get(tagName);
      return tag instanceof DoubleTag ? ((DoubleTag)tag).asDouble() : def;
   }

   @Nullable
   public Tag put(String tagName, Tag tag) {
      if (tag == null) {
         throw new IllegalArgumentException("Cannot add a null tag");
      } else if (tag == this) {
         throw new IllegalArgumentException("Cannot add a tag to itself");
      } else {
         return this.valueOrCreate().put(tagName, tag);
      }
   }

   public void putString(String tagName, String value) {
      this.valueOrCreate().put(tagName, new StringTag(value));
   }

   public void putByte(String tagName, byte value) {
      this.valueOrCreate().put(tagName, new ByteTag(value));
   }

   public void putInt(String tagName, int value) {
      this.valueOrCreate().put(tagName, new IntTag(value));
   }

   public void putShort(String tagName, short value) {
      this.valueOrCreate().put(tagName, new ShortTag(value));
   }

   public void putLong(String tagName, long value) {
      this.valueOrCreate().put(tagName, new LongTag(value));
   }

   public void putFloat(String tagName, float value) {
      this.valueOrCreate().put(tagName, new FloatTag(value));
   }

   public void putDouble(String tagName, double value) {
      this.valueOrCreate().put(tagName, new DoubleTag(value));
   }

   public void putBoolean(String tagName, boolean value) {
      this.valueOrCreate().put(tagName, new ByteTag((byte)(value ? 1 : 0)));
   }

   public void putAll(CompoundTag compoundTag) {
      if (!compoundTag.value.isEmpty()) {
         this.valueOrCreate().putAll(compoundTag.value);
      }
   }

   @Nullable
   public Tag remove(String tagName) {
      return this.value == EMPTY ? null : this.value.remove(tagName);
   }

   @Nullable
   public <T extends Tag> T removeUnchecked(String tagName) {
      return (T)(this.value == EMPTY ? null : this.value.remove(tagName));
   }

   public Set<String> keySet() {
      return this.value.keySet();
   }

   public Collection<Tag> values() {
      return this.value.values();
   }

   public Set<Entry<String, Tag>> entrySet() {
      return this.value.entrySet();
   }

   public int size() {
      return this.value.size();
   }

   public void clear() {
      if (this.value != EMPTY) {
         this.value.clear();
      }
   }

   @Override
   public Iterator<Entry<String, Tag>> iterator() {
      return this.value.entrySet().iterator();
   }

   @Override
   public void write(DataOutput out) throws IOException {
      for (Entry<String, Tag> entry : this.value.entrySet()) {
         Tag tag = entry.getValue();
         out.writeByte(tag.getTagId());
         out.writeUTF(entry.getKey());
         tag.write(out);
      }

      out.writeByte(0);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CompoundTag tags = (CompoundTag)o;
         return this.value.equals(tags.value);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.value.hashCode();
   }

   public CompoundTag copy() {
      if (this.value.isEmpty()) {
         return new CompoundTag();
      } else {
         LinkedHashMap<String, Tag> newMap = new LinkedHashMap<>(this.value.size());

         for (Entry<String, Tag> entry : this.value.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue().copy());
         }

         return new CompoundTag(newMap);
      }
   }

   @Override
   public int getTagId() {
      return 10;
   }

   @Override
   public String toString() {
      return SNBT.serialize(this);
   }
}
