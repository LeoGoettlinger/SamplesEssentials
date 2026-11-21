package com.viaversion.nbt.conversion;

import com.viaversion.nbt.conversion.converter.ByteArrayTagConverter;
import com.viaversion.nbt.conversion.converter.ByteTagConverter;
import com.viaversion.nbt.conversion.converter.CompoundTagConverter;
import com.viaversion.nbt.conversion.converter.DoubleTagConverter;
import com.viaversion.nbt.conversion.converter.FloatTagConverter;
import com.viaversion.nbt.conversion.converter.IntArrayTagConverter;
import com.viaversion.nbt.conversion.converter.IntTagConverter;
import com.viaversion.nbt.conversion.converter.ListTagConverter;
import com.viaversion.nbt.conversion.converter.LongArrayTagConverter;
import com.viaversion.nbt.conversion.converter.LongTagConverter;
import com.viaversion.nbt.conversion.converter.ShortTagConverter;
import com.viaversion.nbt.conversion.converter.StringTagConverter;
import com.viaversion.nbt.io.TagRegistry;
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
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public final class ConverterRegistry {
   private static final Int2ObjectMap<TagConverter<? extends Tag, ?>> TAG_TO_CONVERTER = new Int2ObjectOpenHashMap<>();
   private static final Map<Class<?>, TagConverter<? extends Tag, ?>> TYPE_TO_CONVERTER = new HashMap<>();

   public static <T extends Tag, V> void register(Class<T> tag, Class<? extends V> type, TagConverter<T, V> converter) {
      int tagId = TagRegistry.getIdFor(tag);
      if (tagId == -1) {
         throw new IllegalArgumentException("Tag " + tag.getName() + " is not a registered tag.");
      } else if (TAG_TO_CONVERTER.containsKey(tagId)) {
         throw new IllegalArgumentException("Type conversion to tag " + tag.getName() + " is already registered.");
      } else if (TYPE_TO_CONVERTER.containsKey(type)) {
         throw new IllegalArgumentException("Tag conversion to type " + type.getName() + " is already registered.");
      } else {
         TAG_TO_CONVERTER.put(tagId, converter);
         TYPE_TO_CONVERTER.put(type, converter);
      }
   }

   public static <T extends Tag, V> void unregister(Class<T> tag, Class<V> type) {
      TAG_TO_CONVERTER.remove(TagRegistry.getIdFor(tag));
      TYPE_TO_CONVERTER.remove(type);
   }

   @Nullable
   public static <T extends Tag, V> V convertToValue(@Nullable T tag) throws ConversionException {
      if (tag != null && tag.getValue() != null) {
         TagConverter<T, ? extends V> converter = (TagConverter<T, ? extends V>)TAG_TO_CONVERTER.get(tag.getTagId());
         if (converter == null) {
            throw new ConversionException("Tag type " + tag.getClass().getName() + " has no converter.");
         } else {
            return (V)converter.convert(tag);
         }
      } else {
         return null;
      }
   }

   @Nullable
   public static <V, T extends Tag> T convertToTag(@Nullable V value) throws ConversionException {
      if (value == null) {
         return null;
      } else {
         Class<?> valueClass = value.getClass();
         TagConverter<T, ? super V> converter = (TagConverter<T, ? super V>)TYPE_TO_CONVERTER.get(valueClass);
         if (converter == null) {
            for (Class<?> interfaceClass : valueClass.getInterfaces()) {
               converter = (TagConverter<T, ? super V>)TYPE_TO_CONVERTER.get(interfaceClass);
               if (converter != null) {
                  break;
               }
            }

            if (converter == null) {
               throw new ConversionException("Value type " + valueClass.getName() + " has no converter.");
            }
         }

         return converter.convert(value);
      }
   }

   static {
      register(ByteTag.class, Byte.class, new ByteTagConverter());
      register(ShortTag.class, Short.class, new ShortTagConverter());
      register(IntTag.class, Integer.class, new IntTagConverter());
      register(LongTag.class, Long.class, new LongTagConverter());
      register(FloatTag.class, Float.class, new FloatTagConverter());
      register(DoubleTag.class, Double.class, new DoubleTagConverter());
      register(ByteArrayTag.class, byte[].class, new ByteArrayTagConverter());
      register(StringTag.class, String.class, new StringTagConverter());
      register(ListTag.class, List.class, new ListTagConverter());
      register(CompoundTag.class, Map.class, new CompoundTagConverter());
      register(IntArrayTag.class, int[].class, new IntArrayTagConverter());
      register(LongArrayTag.class, long[].class, new LongArrayTagConverter());
   }
}
