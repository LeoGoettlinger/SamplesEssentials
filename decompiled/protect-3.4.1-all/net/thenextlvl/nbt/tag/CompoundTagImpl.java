package net.thenextlvl.nbt.tag;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import net.thenextlvl.nbt.NBTOutputStream;
import org.jspecify.annotations.NullUnmarked;

final class CompoundTagImpl extends ValueTagImpl<Map<String, Tag>> implements CompoundTag {
   public CompoundTagImpl(Map<String, Tag> value) {
      super(Collections.unmodifiableMap(new LinkedHashMap<>(value)));
   }

   public CompoundTagImpl() {
      super(Collections.emptyMap());
   }

   @Override
   public boolean isCompound() {
      return true;
   }

   @Override
   public CompoundTag getAsCompound() {
      return this;
   }

   @Override
   public byte getTypeId() {
      return 10;
   }

   @Override
   public void forEach(BiConsumer<? super String, ? super Tag> action) {
      this.value.forEach(action);
   }

   @Override
   public Set<Entry<String, Tag>> entrySet() {
      return this.value.entrySet();
   }

   @Override
   public Set<String> keySet() {
      return this.value.keySet();
   }

   @Override
   public boolean isEmpty() {
      return this.value.isEmpty();
   }

   @Override
   public int size() {
      return this.value.size();
   }

   @Override
   public boolean containsKey(String property) {
      return this.value.containsKey(property);
   }

   @NullUnmarked
   @Override
   public <T extends Tag> T get(String property) {
      return (T)this.value.get(property);
   }

   @Override
   public <E extends Tag> ListTag<E> getAsList(String tag) {
      return this.<Tag>get(tag).getAsList();
   }

   @Override
   public CompoundTag getAsCompound(String tag) {
      return this.<Tag>get(tag).getAsCompound();
   }

   @NullUnmarked
   @Override
   public <T extends Tag> T getOrDefault(String tag, T defaultValue) {
      return (T)this.value.getOrDefault(tag, defaultValue);
   }

   @Override
   public <T extends Tag> Optional<T> optional(String tag) {
      return Optional.ofNullable(this.get(tag)).map(value -> (T)value);
   }

   @Override
   public CompoundTag.Builder toBuilder() {
      return new CompoundTagImpl.Builder().putAll(this);
   }

   @Override
   public void write(NBTOutputStream outputStream) throws IOException {
      for (Entry<String, Tag> entry : this.entrySet()) {
         outputStream.writeTag(entry.getKey(), entry.getValue());
      }

      EscapeTagImpl.INSTANCE.write(outputStream);
   }

   public static CompoundTag.Builder builder() {
      return new CompoundTagImpl.Builder();
   }

   @Override
   public String toString() {
      if (this.isEmpty()) {
         return "{}";
      } else {
         StringBuilder builder = new StringBuilder("{");
         Iterator<Entry<String, Tag>> iterator = this.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<String, Tag> entry = iterator.next();
            builder.append(entry.getKey()).append(':').append(entry.getValue());
            if (iterator.hasNext()) {
               builder.append(",");
            }
         }

         return builder.append('}').toString();
      }
   }

   public static final class Builder implements CompoundTag.Builder {
      private final Map<String, Tag> values = new LinkedHashMap<>();

      public CompoundTagImpl.Builder put(String name, boolean value) {
         return this.put(name, ByteTag.of(value));
      }

      public CompoundTagImpl.Builder put(String name, byte... array) {
         return this.put(name, new ByteArrayTagImpl(array));
      }

      public CompoundTagImpl.Builder put(String name, int... array) {
         return this.put(name, new IntArrayTagImpl(array));
      }

      public CompoundTagImpl.Builder put(String name, long... array) {
         return this.put(name, new LongArrayTagImpl(array));
      }

      public CompoundTagImpl.Builder put(String name, Number number) {
         return this.put(name, NumberTag.of(number));
      }

      public CompoundTagImpl.Builder put(String name, String value) {
         return this.put(name, new StringTagImpl(value));
      }

      public CompoundTagImpl.Builder put(String name, Tag tag) {
         this.values.put(name, tag);
         return this;
      }

      public CompoundTagImpl.Builder putAll(CompoundTag tag) {
         this.values.putAll(tag.getValue());
         return this;
      }

      @Override
      public boolean isEmpty() {
         return this.values.isEmpty();
      }

      @Override
      public int size() {
         return this.values.size();
      }

      @Override
      public CompoundTag build() {
         return new CompoundTagImpl(this.values);
      }
   }
}
