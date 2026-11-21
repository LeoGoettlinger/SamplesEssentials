package com.viaversion.viaversion.libs.mcstructs.converter.impl.v1_20_3;

import com.viaversion.nbt.tag.ByteArrayTag;
import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.DoubleTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.LongArrayTag;
import com.viaversion.nbt.tag.LongTag;
import com.viaversion.nbt.tag.MixedListTag;
import com.viaversion.nbt.tag.NumberArrayTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.libs.mcstructs.converter.DataConverter;
import com.viaversion.viaversion.libs.mcstructs.converter.model.Result;
import com.viaversion.viaversion.libs.mcstructs.snbt.SNbt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public class NbtConverter_v1_20_3 implements DataConverter<Tag> {
   public static final NbtConverter_v1_20_3 INSTANCE = new NbtConverter_v1_20_3();
   private Consumer<String> stringConsumer;
   private final SNbt<CompoundTag> sNbt;

   @Nullable
   @Override
   public Consumer<String> currentConsumer() {
      return this.stringConsumer;
   }

   @Override
   public void setCurrentConsumer(Consumer<String> consumer) {
      this.stringConsumer = consumer;
   }

   @Override
   public DataConverter<Tag> forkIfDefault() {
      return this == INSTANCE ? new NbtConverter_v1_20_3() : this;
   }

   public NbtConverter_v1_20_3() {
      this(SNbt.V1_14);
   }

   protected NbtConverter_v1_20_3(SNbt<CompoundTag> sNbt) {
      this.sNbt = sNbt;
   }

   public <N> N convertTo(DataConverter<N> to, @Nullable Tag element) {
      if (to == this) {
         return (N)element;
      } else if (element == null) {
         return to.empty();
      } else if (element.getTagId() == 0) {
         return to.empty();
      } else if (element instanceof ByteTag) {
         return to.createByte(((ByteTag)element).getValue());
      } else if (element instanceof ShortTag) {
         return to.createShort(((ShortTag)element).getValue());
      } else if (element instanceof IntTag) {
         return to.createInt(((IntTag)element).getValue());
      } else if (element instanceof LongTag) {
         return to.createLong(((LongTag)element).getValue());
      } else if (element instanceof FloatTag) {
         return to.createFloat(((FloatTag)element).getValue());
      } else if (element instanceof DoubleTag) {
         return to.createDouble(((DoubleTag)element).getValue());
      } else if (element instanceof ByteArrayTag) {
         return to.createByteArray(((ByteArrayTag)element).getValue());
      } else if (element instanceof StringTag) {
         return to.createString(((StringTag)element).getValue());
      } else if (element instanceof ListTag) {
         return this.convertList(to, element);
      } else if (element instanceof CompoundTag) {
         return this.convertMap(to, element);
      } else if (element instanceof IntArrayTag) {
         return to.createIntArray(((IntArrayTag)element).getValue());
      } else if (element instanceof LongArrayTag) {
         return to.createLongArray(((LongArrayTag)element).getValue());
      } else {
         throw new IllegalArgumentException("Unknown Nbt type: " + element);
      }
   }

   public Tag createBoolean(boolean value) {
      return new ByteTag(value);
   }

   public Result<Boolean> asBoolean(Tag element) {
      return this.asNumber(element).map(number -> number.byteValue() != 0);
   }

   public Tag createNumber(Number number) {
      return new DoubleTag(number.doubleValue());
   }

   public Result<Number> asNumber(Tag element) {
      return !(element instanceof NumberTag) ? Result.unexpected(element, NumberTag.class) : Result.success(((NumberTag)element).getValue());
   }

   public Tag createByte(byte value) {
      return new ByteTag(value);
   }

   public Tag createShort(short value) {
      return new ShortTag(value);
   }

   public Tag createInt(int value) {
      return new IntTag(value);
   }

   public Tag createLong(long value) {
      return new LongTag(value);
   }

   public Tag createFloat(float value) {
      return new FloatTag(value);
   }

   public Tag createDouble(double value) {
      return new DoubleTag(value);
   }

   public Tag createString(String value) {
      return new StringTag(value);
   }

   public Result<String> asString(Tag element) {
      return !(element instanceof StringTag) ? Result.unexpected(element, StringTag.class) : Result.success(((StringTag)element).getValue());
   }

   public Result<Tag> mergeList(@Nullable Tag list, List<Tag> values) {
      if (list == null) {
         list = new ListTag();
      }

      if (list instanceof ByteArrayTag && values.stream().allMatch(tagx -> tagx instanceof ByteTag)) {
         ByteArrayTag tag = (ByteArrayTag)list;
         byte[] bytes = Arrays.copyOf(tag.getValue(), tag.length() + values.size());

         for (int i = 0; i < values.size(); i++) {
            bytes[tag.length() + i] = ((NumberTag)values.get(i)).asByte();
         }

         return Result.success(new ByteArrayTag(bytes));
      } else if (list instanceof IntArrayTag && values.stream().allMatch(tagx -> tagx instanceof IntTag)) {
         IntArrayTag tag = (IntArrayTag)list;
         int[] ints = Arrays.copyOf(tag.getValue(), tag.length() + values.size());

         for (int i = 0; i < values.size(); i++) {
            ints[tag.length() + i] = ((NumberTag)values.get(i)).asInt();
         }

         return Result.success(new IntArrayTag(ints));
      } else if (list instanceof LongArrayTag && values.stream().allMatch(tagx -> tagx instanceof LongTag)) {
         LongArrayTag tag = (LongArrayTag)list;
         long[] longs = Arrays.copyOf(tag.getValue(), tag.length() + values.size());

         for (int i = 0; i < values.size(); i++) {
            longs[tag.length() + i] = ((NumberTag)values.get(i)).asLong();
         }

         return Result.success(new LongArrayTag(longs));
      } else {
         Result<List<Tag>> listResult = this.asList(list);
         if (listResult.isError()) {
            return listResult.mapError();
         } else {
            List<Tag> elements = new ArrayList<>(listResult.get());
            elements.addAll(values);
            Tag listType = null;

            for (Tag value : elements) {
               if (listType == null) {
                  listType = value;
               } else if (!value.equals(listType)) {
                  listType = new MixedListTag();
               }
            }

            if (listType == null) {
               return Result.success(new ListTag());
            } else if (listType instanceof ByteTag) {
               byte[] bytes = new byte[elements.size()];

               for (int i = 0; i < elements.size(); i++) {
                  bytes[i] = ((ByteTag)elements.get(i)).asByte();
               }

               return Result.success(new ByteArrayTag(bytes));
            } else if (listType instanceof IntTag) {
               int[] ints = new int[elements.size()];

               for (int i = 0; i < elements.size(); i++) {
                  ints[i] = ((IntTag)elements.get(i)).asInt();
               }

               return Result.success(new IntArrayTag(ints));
            } else if (listType instanceof LongTag) {
               long[] longs = new long[elements.size()];

               for (int i = 0; i < elements.size(); i++) {
                  longs[i] = ((LongTag)elements.get(i)).asLong();
               }

               return Result.success(new LongArrayTag(longs));
            } else if (!(listType instanceof MixedListTag)) {
               return Result.success(new ListTag<>(elements));
            } else {
               ListTag<CompoundTag> listTag = new ListTag<>();

               for (Tag tag : elements) {
                  boolean isMarker = tag instanceof CompoundTag && ((CompoundTag)tag).size() == 1 && ((CompoundTag)tag).contains("");
                  if (tag instanceof CompoundTag && !isMarker) {
                     listTag.add((CompoundTag)tag);
                  } else {
                     CompoundTag wrappingTag = new CompoundTag();
                     wrappingTag.put("", tag);
                     listTag.add(wrappingTag);
                  }
               }

               return Result.success(listTag);
            }
         }
      }
   }

   public Result<List<Tag>> asList(Tag element) {
      if (!(element instanceof ListTag)) {
         return element instanceof NumberArrayTag
            ? Result.success(((NumberArrayTag)element).toListTag().getValue())
            : Result.unexpected(element, ListTag.class, NumberArrayTag.class);
      } else {
         ListTag<?> listTag = (ListTag<?>)element;
         List<Tag> list = new ArrayList<>(listTag.size());
         if (CompoundTag.class == listTag.getElementType()) {
            for (Tag tag : listTag) {
               CompoundTag compound = (CompoundTag)tag;
               Tag wrapped = compound.size() == 1 ? compound.get("") : null;
               if (wrapped != null) {
                  list.add(wrapped);
               } else {
                  list.add(tag);
               }
            }
         } else {
            list.addAll((Collection<? extends Tag>)listTag.getValue());
         }

         return Result.success(list);
      }
   }

   public Tag createUnsafeMap(Map<Tag, Tag> values) {
      CompoundTag compound = new CompoundTag();

      for (Entry<Tag, Tag> entry : values.entrySet()) {
         String key;
         if (entry.getKey() instanceof StringTag) {
            key = ((StringTag)entry.getKey()).getValue();
         } else {
            key = this.sNbt.trySerialize(entry.getKey());
         }

         compound.put(key, entry.getValue());
      }

      return compound;
   }

   public Result<Tag> mergeMap(@Nullable Tag map, Map<Tag, Tag> values) {
      if (map == null) {
         map = new CompoundTag();
      }

      if (!(map instanceof CompoundTag)) {
         return Result.unexpected(map, CompoundTag.class);
      } else {
         CompoundTag compound = (CompoundTag)map;

         for (Entry<Tag, Tag> entry : values.entrySet()) {
            if (!(entry.getKey() instanceof StringTag)) {
               return Result.error("Map key is not a string tag");
            }

            compound.put(((StringTag)entry.getKey()).getValue(), entry.getValue());
         }

         return Result.success(compound);
      }
   }

   public Result<Map<Tag, Tag>> asMap(Tag element) {
      if (!(element instanceof CompoundTag)) {
         return Result.unexpected(element, CompoundTag.class);
      } else {
         CompoundTag compound = (CompoundTag)element;
         Map<Tag, Tag> map = new HashMap<>();

         for (Entry<String, Tag> entry : compound) {
            map.put(this.createString(entry.getKey()), entry.getValue());
         }

         return Result.success(map);
      }
   }

   public Result<Map<String, Tag>> asStringTypeMap(Tag element) {
      if (!(element instanceof CompoundTag)) {
         return Result.unexpected(element, CompoundTag.class);
      } else {
         CompoundTag compound = (CompoundTag)element;
         Map<String, Tag> map = new HashMap<>();

         for (Entry<String, Tag> entry : compound) {
            map.put(entry.getKey(), entry.getValue());
         }

         return Result.success(map);
      }
   }

   public Tag createByteArray(byte[] value) {
      return new ByteArrayTag(value);
   }

   public Result<byte[]> asByteArray(Tag element) {
      if (element instanceof ByteArrayTag) {
         return Result.success(((ByteArrayTag)element).getValue());
      } else if (element instanceof IntArrayTag) {
         IntArrayTag intArrayTag = (IntArrayTag)element;
         byte[] bytes = new byte[intArrayTag.length()];

         for (int i = 0; i < intArrayTag.length(); i++) {
            bytes[i] = (byte)intArrayTag.get(i);
         }

         return Result.success(bytes);
      } else if (element instanceof LongArrayTag) {
         LongArrayTag longArrayTag = (LongArrayTag)element;
         byte[] bytes = new byte[longArrayTag.length()];

         for (int i = 0; i < longArrayTag.length(); i++) {
            bytes[i] = (byte)longArrayTag.get(i);
         }

         return Result.success(bytes);
      } else if (!(element instanceof ListTag)) {
         return Result.unexpected(element, ByteArrayTag.class, IntArrayTag.class, LongArrayTag.class, ListTag.class);
      } else {
         List<Tag> list = this.asList(element).get();
         if (!list.stream().allMatch(tag -> tag instanceof NumberTag)) {
            return Result.error("List tag does not contain only number tags");
         } else {
            byte[] bytes = new byte[list.size()];

            for (int i = 0; i < list.size(); i++) {
               bytes[i] = ((NumberTag)list.get(i)).asByte();
            }

            return Result.success(bytes);
         }
      }
   }

   public Tag createIntArray(int[] value) {
      return new IntArrayTag(value);
   }

   public Result<int[]> asIntArray(Tag element) {
      if (element instanceof ByteArrayTag) {
         ByteArrayTag byteArrayTag = (ByteArrayTag)element;
         int[] ints = new int[byteArrayTag.length()];

         for (int i = 0; i < byteArrayTag.length(); i++) {
            ints[i] = byteArrayTag.get(i);
         }

         return Result.success(ints);
      } else if (element instanceof IntArrayTag) {
         return Result.success(((IntArrayTag)element).getValue());
      } else if (element instanceof LongArrayTag) {
         LongArrayTag longArrayTag = (LongArrayTag)element;
         int[] ints = new int[longArrayTag.length()];

         for (int i = 0; i < longArrayTag.length(); i++) {
            ints[i] = (int)longArrayTag.get(i);
         }

         return Result.success(ints);
      } else if (!(element instanceof ListTag)) {
         return Result.unexpected(element, ByteArrayTag.class, IntArrayTag.class, LongArrayTag.class, ListTag.class);
      } else {
         List<Tag> list = this.asList(element).get();
         if (!list.stream().allMatch(tag -> tag instanceof NumberTag)) {
            return Result.error("List tag does not contain only number tags");
         } else {
            int[] ints = new int[list.size()];

            for (int i = 0; i < list.size(); i++) {
               ints[i] = ((NumberTag)list.get(i)).asInt();
            }

            return Result.success(ints);
         }
      }
   }

   public Tag createLongArray(long[] value) {
      return new LongArrayTag(value);
   }

   public Result<long[]> asLongArray(Tag element) {
      if (element instanceof ByteArrayTag) {
         ByteArrayTag byteArrayTag = (ByteArrayTag)element;
         long[] longs = new long[byteArrayTag.length()];

         for (int i = 0; i < byteArrayTag.length(); i++) {
            longs[i] = byteArrayTag.get(i);
         }

         return Result.success(longs);
      } else if (element instanceof IntArrayTag) {
         IntArrayTag intArrayTag = (IntArrayTag)element;
         long[] longs = new long[intArrayTag.length()];

         for (int i = 0; i < intArrayTag.length(); i++) {
            longs[i] = intArrayTag.get(i);
         }

         return Result.success(longs);
      } else if (element instanceof LongArrayTag) {
         return Result.success(((LongArrayTag)element).getValue());
      } else if (!(element instanceof ListTag)) {
         return Result.unexpected(element, ByteArrayTag.class, IntArrayTag.class, LongArrayTag.class, ListTag.class);
      } else {
         List<Tag> list = this.asList(element).get();
         if (!list.stream().allMatch(tag -> tag instanceof NumberTag)) {
            return Result.error("List tag does not contain only number tags");
         } else {
            long[] longs = new long[list.size()];

            for (int i = 0; i < list.size(); i++) {
               longs[i] = ((NumberTag)list.get(i)).asLong();
            }

            return Result.success(longs);
         }
      }
   }
}
