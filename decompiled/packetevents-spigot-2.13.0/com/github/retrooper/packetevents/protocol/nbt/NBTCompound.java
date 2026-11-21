package com.github.retrooper.packetevents.protocol.nbt;

import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import com.github.retrooper.packetevents.protocol.util.NbtDecoder;
import com.github.retrooper.packetevents.protocol.util.NbtEncoder;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class NBTCompound extends NBT {
   protected final Map<String, NBT> tags = new LinkedHashMap<>();

   @Override
   public NBTType<NBTCompound> getType() {
      return NBTType.COMPOUND;
   }

   public boolean isEmpty() {
      return this.tags.isEmpty();
   }

   public boolean contains(String key) {
      return this.tags.containsKey(key);
   }

   public Set<String> getTagNames() {
      return Collections.unmodifiableSet(this.tags.keySet());
   }

   public Map<String, NBT> getTags() {
      return Collections.unmodifiableMap(this.tags);
   }

   public int size() {
      return this.tags.size();
   }

   public NBT getTagOrThrow(String key) throws NbtCodecException {
      NBT tag = this.getTagOrNull(key);
      if (tag == null) {
         throw new NbtCodecException("Tag " + key + " doesn't exist");
      } else {
         return tag;
      }
   }

   @Nullable
   public NBT getTagOrNull(String key) {
      return this.tags.get(key);
   }

   public <T extends NBT> T getTagOfTypeOrThrow(String key, Class<T> type) throws NbtCodecException {
      NBT tag = this.getTagOrThrow(key);
      if (type.isInstance(tag)) {
         return (T)tag;
      } else {
         throw new NbtCodecException(MessageFormat.format("NBT {0} has unexpected type, expected {1}, but got {2}", key, type, tag.getClass()));
      }
   }

   @Nullable
   public <T extends NBT> T getTagOfTypeOrNull(String key, Class<T> type) {
      NBT tag = this.getTagOrNull(key);
      return (T)(type.isInstance(tag) ? tag : null);
   }

   public <T extends NBT> NBTList<T> getTagListOfTypeOrThrow(String key, Class<T> type) throws NbtCodecException {
      NBTList<? extends NBT> list = this.getTagOfTypeOrThrow(key, NBTList.class);
      if (!type.isAssignableFrom(list.getTagsType().getNBTClass())) {
         throw new NbtCodecException(
            MessageFormat.format("NBTList {0} tags type has unexpected type, expected {1}, but got {2}", key, type, list.getTagsType().getNBTClass())
         );
      } else {
         return (NBTList<T>)list;
      }
   }

   @Nullable
   public <T extends NBT> NBTList<T> getTagListOfTypeOrNull(String key, Class<T> type) {
      NBTList<? extends NBT> list = this.getTagOfTypeOrNull(key, NBTList.class);
      return (NBTList<T>)(list != null && type.isAssignableFrom(list.getTagsType().getNBTClass()) ? list : null);
   }

   public NBTCompound getCompoundTagOrThrow(String key) {
      return this.getTagOfTypeOrThrow(key, NBTCompound.class);
   }

   @Nullable
   public NBTCompound getCompoundTagOrNull(String key) {
      return this.getTagOfTypeOrNull(key, NBTCompound.class);
   }

   public Number getNumberTagValueOrThrow(String key) {
      return this.getNumberTagOrThrow(key).getAsNumber();
   }

   @Nullable
   public Number getNumberTagValueOrNull(String key) {
      return this.getNumberTagValueOrDefault(key, null);
   }

   @Contract("_, !null -> !null")
   @Nullable
   public Number getNumberTagValueOrDefault(String key, @Nullable Number number) {
      NBTNumber tag = this.getNumberTagOrNull(key);
      return tag != null ? tag.getAsNumber() : number;
   }

   public NBTNumber getNumberTagOrThrow(String key) {
      return this.getTagOfTypeOrThrow(key, NBTNumber.class);
   }

   @Nullable
   public NBTNumber getNumberTagOrNull(String key) {
      return this.getTagOfTypeOrNull(key, NBTNumber.class);
   }

   public NBTString getStringTagOrThrow(String key) {
      return this.getTagOfTypeOrThrow(key, NBTString.class);
   }

   @Nullable
   public NBTString getStringTagOrNull(String key) {
      return this.getTagOfTypeOrNull(key, NBTString.class);
   }

   public NBTList<NBTCompound> getCompoundListTagOrThrow(String key) {
      return this.getTagListOfTypeOrThrow(key, NBTCompound.class);
   }

   @Nullable
   public NBTList<NBTCompound> getCompoundListTagOrNull(String key) {
      return this.getTagListOfTypeOrNull(key, NBTCompound.class);
   }

   public NBTList<NBTNumber> getNumberTagListTagOrThrow(String key) {
      return this.getTagListOfTypeOrThrow(key, NBTNumber.class);
   }

   @Nullable
   public NBTList<NBTNumber> getNumberListTagOrNull(String key) {
      return this.getTagListOfTypeOrNull(key, NBTNumber.class);
   }

   public NBTList<NBTString> getStringListTagOrThrow(String key) {
      return this.getTagListOfTypeOrThrow(key, NBTString.class);
   }

   @Nullable
   public NBTList<NBTString> getStringListTagOrNull(String key) {
      return this.getTagListOfTypeOrNull(key, NBTString.class);
   }

   public String getStringTagValueOrThrow(String key) {
      return this.getStringTagOrThrow(key).getValue();
   }

   @Nullable
   public String getStringTagValueOrNull(String key) {
      NBT tag = this.getTagOrNull(key);
      return tag instanceof NBTString ? ((NBTString)tag).getValue() : null;
   }

   public String getStringTagValueOrDefault(String key, String defaultValue) {
      NBT tag = this.getTagOrNull(key);
      return tag instanceof NBTString ? ((NBTString)tag).getValue() : defaultValue;
   }

   public NBT removeTag(String key) {
      return this.tags.remove(key);
   }

   @Nullable
   public <T extends NBT> T removeTagAndReturnIfType(String key, Class<T> type) {
      NBT tag = this.removeTag(key);
      return (T)(type.isInstance(tag) ? tag : null);
   }

   @Nullable
   public <T extends NBT> NBTList<T> removeTagAndReturnIfListType(String key, Class<T> type) {
      NBTList<?> list = this.removeTagAndReturnIfType(key, NBTList.class);
      return (NBTList<T>)(list != null && type.isAssignableFrom(list.getTagsType().getNBTClass()) ? list : null);
   }

   public void setTag(String key, @Nullable NBT tag) {
      if (tag != null) {
         this.tags.put(key, tag);
      } else {
         this.tags.remove(key);
      }
   }

   public NBTCompound copy() {
      NBTCompound clone = new NBTCompound();

      for (Entry<String, NBT> entry : this.tags.entrySet()) {
         clone.setTag(entry.getKey(), entry.getValue().copy());
      }

      return clone;
   }

   public boolean getBoolean(String string) {
      return this.getBooleanOr(string, false);
   }

   public boolean getBooleanOr(String string, boolean defaultValue) {
      NBTNumber nbtByte = this.getTagOfTypeOrNull(string, NBTNumber.class);
      return nbtByte != null ? nbtByte.getAsByte() != 0 : defaultValue;
   }

   public boolean getBooleanOrThrow(String string) {
      return this.getTagOfTypeOrThrow(string, NBTNumber.class).getAsByte() != 0;
   }

   @Contract("_, _, !null, _ -> !null")
   @Nullable
   public <T> T getOr(String key, NbtDecoder<T> decoder, @Nullable T def, PacketWrapper<?> wrapper) {
      NBT tag = this.getTagOrNull(key);
      return tag != null ? decoder.decode(tag, wrapper) : def;
   }

   @Nullable
   public <T> T getOrSupply(String key, NbtDecoder<T> decoder, Supplier<T> def, PacketWrapper<?> wrapper) {
      NBT tag = this.getTagOrNull(key);
      return tag != null ? decoder.decode(tag, wrapper) : def.get();
   }

   @Nullable
   public <T> T getOrNull(String key, NbtDecoder<T> decoder, PacketWrapper<?> wrapper) {
      return this.getOr(key, decoder, null, wrapper);
   }

   public <T> T getOrThrow(String key, NbtDecoder<T> decoder, PacketWrapper<?> wrapper) {
      return decoder.decode(this.getTagOrThrow(key), wrapper);
   }

   @Contract("_, _, !null, _ -> !null")
   @Nullable
   public <T> List<T> getListOr(String key, NbtDecoder<T> decoder, @Nullable List<T> def, PacketWrapper<?> wrapper) {
      NBT tag = this.getTagOrNull(key);
      if (!(tag instanceof NBTList)) {
         if (tag != null) {
            List<T> list = new ArrayList<>(1);
            list.add(decoder.decode(tag, wrapper));
            return list;
         } else {
            return def;
         }
      } else {
         List<? extends NBT> tags = ((NBTList)tag).unwrapTags();
         List<T> list = new ArrayList<>(tags.size());

         for (NBT element : tags) {
            list.add(decoder.decode(element, wrapper));
         }

         return list;
      }
   }

   @Nullable
   public <T> List<T> getListOrNull(String key, NbtDecoder<T> decoder, PacketWrapper<?> wrapper) {
      return this.getListOr(key, decoder, null, wrapper);
   }

   public <T> List<T> getListOrEmpty(String key, NbtDecoder<T> decoder, PacketWrapper<?> wrapper) {
      return this.getListOr(key, decoder, Collections.emptyList(), wrapper);
   }

   public <T> List<T> getListOrThrow(String key, NbtDecoder<T> decoder, PacketWrapper<?> wrapper) {
      List<T> list = this.getListOrNull(key, decoder, wrapper);
      if (list == null) {
         throw new IllegalStateException(MessageFormat.format("NBT {0} does not exist", key));
      } else {
         return list;
      }
   }

   public <T> void set(String key, T value, NbtEncoder<T> encoder, PacketWrapper<?> wrapper) {
      this.setTag(key, encoder.encode(wrapper, value));
   }

   public <T> void setList(String key, List<T> value, NbtEncoder<T> encoder, PacketWrapper<?> wrapper) {
      if (value.isEmpty()) {
         this.setTag(key, new NBTList<>(NBTType.END, 0));
      } else {
         int size = value.size();
         List<NBT> tags = new ArrayList<>(size);

         for (T elem : value) {
            tags.add(encoder.encode(wrapper, elem));
         }

         NBTList<?> list = new NBTList<>(NBTList.getCommonTagType(tags), size);

         for (NBT tag : tags) {
            list.addTagOrWrap(tag);
         }

         this.setTag(key, list);
      }
   }

   public <T> void setCompactList(String key, List<T> value, NbtEncoder<T> encoder, PacketWrapper<?> wrapper) {
      if (value.size() == 1) {
         this.set(key, value.get(0), encoder, wrapper);
      } else {
         this.setList(key, value, encoder, wrapper);
      }
   }

   @Override
   public boolean equals(@Nullable Object other) {
      if (other instanceof NBTCompound) {
         return this.isEmpty() && ((NBTCompound)other).isEmpty() ? true : this.tags.equals(((NBTCompound)other).tags);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.tags.hashCode();
   }

   @Override
   public String toString() {
      return "Compound{" + this.tags + "}";
   }
}
