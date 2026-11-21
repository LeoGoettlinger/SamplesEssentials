package com.sk89q.jnbt;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Function;
import org.enginehub.linbus.tree.LinByteArrayTag;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongArrayTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

@Deprecated(
   forRemoval = true
)
public class LinBusConverter {
   private static final BiMap<Class<? extends Tag>, LinTagType<?>> TAG_TYPES = new com.google.common.collect.ImmutableBiMap.Builder()
      .put(ByteArrayTag.class, LinTagType.byteArrayTag())
      .put(ByteTag.class, LinTagType.byteTag())
      .put(CompoundTag.class, LinTagType.compoundTag())
      .put(DoubleTag.class, LinTagType.doubleTag())
      .put(EndTag.class, LinTagType.endTag())
      .put(FloatTag.class, LinTagType.floatTag())
      .put(IntArrayTag.class, LinTagType.intArrayTag())
      .put(IntTag.class, LinTagType.intTag())
      .put(ListTag.class, LinTagType.listTag())
      .put(LongArrayTag.class, LinTagType.longArrayTag())
      .put(LongTag.class, LinTagType.longTag())
      .put(ShortTag.class, LinTagType.shortTag())
      .put(StringTag.class, LinTagType.stringTag())
      .build();
   private static final Map<LinTagType<?>, Function<LinTag, Tag>> CONVERSION;

   public static LinTagType<?> getAdventureType(Class<? extends Tag> type) {
      return Objects.requireNonNull((LinTagType<?>)TAG_TYPES.get(type), () -> "Missing entry for " + type);
   }

   public static Class<? extends Tag> getJNBTType(LinTagType<?> type) {
      return Objects.requireNonNull((Class<? extends Tag>)TAG_TYPES.inverse().get(type), () -> "Missing entry for " + type);
   }

   public static <V, LT extends LinTag<? extends V>> Tag<V, LT> toJnbtTag(LT tag) {
      return (Tag<V, LT>)(switch (tag.type().id()) {
         case BYTE_ARRAY -> new ByteArrayTag((LinByteArrayTag)tag);
         case BYTE -> new ByteTag((LinByteTag)tag);
         case COMPOUND -> new CompoundTag((LinCompoundTag)tag);
         case DOUBLE -> new DoubleTag((LinDoubleTag)tag);
         case END -> new EndTag();
         case FLOAT -> new FloatTag((LinFloatTag)tag);
         case INT_ARRAY -> new IntArrayTag((LinIntArrayTag)tag);
         case INT -> new IntTag((LinIntTag)tag);
         case LIST -> new ListTag((LinListTag)tag);
         case LONG_ARRAY -> new LongArrayTag((LinLongArrayTag)tag);
         case LONG -> new LongTag((LinLongTag)tag);
         case SHORT -> new ShortTag((LinShortTag)tag);
         case STRING -> new StringTag((LinStringTag)tag);
      });
   }

   private LinBusConverter() {
   }

   public static Tag fromLinBus(LinTag other) {
      if (other == null) {
         return null;
      } else {
         Function<LinTag, Tag> conversion = CONVERSION.get(other.type());
         if (conversion == null) {
            throw new IllegalArgumentException("Can't convert other of type " + other.getClass().getCanonicalName());
         } else {
            return conversion.apply(other);
         }
      }
   }

   static {
      Builder<LinTagType<?>, Function<LinTag, Tag>> conversion = ImmutableMap.builder();

      for (Entry<Class<? extends Tag>, LinTagType<?>> tag : TAG_TYPES.entrySet()) {
         Constructor<?>[] constructors = tag.getKey().getConstructors();

         for (Constructor<?> c : constructors) {
            if (c.getParameterCount() == 1 && LinTag.class.isAssignableFrom(c.getParameterTypes()[0])) {
               conversion.put(tag.getValue(), (Function<LinTag, Tag>)linTag -> {
                  try {
                     return (Tag)c.newInstance(linTag);
                  } catch (IllegalAccessException | InstantiationException var3x) {
                     throw new IllegalStateException(var3x);
                  } catch (InvocationTargetException var4) {
                     throw (RuntimeException)var4.getCause();
                  }
               });
               break;
            }
         }
      }

      CONVERSION = conversion.build();
   }
}
