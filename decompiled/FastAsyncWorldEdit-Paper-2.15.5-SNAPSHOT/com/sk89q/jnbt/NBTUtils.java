package com.sk89q.jnbt;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.world.storage.InvalidFormatException;
import java.util.Map;
import java.util.UUID;

@Deprecated
public final class NBTUtils {
   private NBTUtils() {
   }

   public static String getTypeName(Class<? extends Tag<?, ?>> clazz) {
      if (clazz.equals(ByteArrayTag.class)) {
         return "TAG_Byte_Array";
      } else if (clazz.equals(ByteTag.class)) {
         return "TAG_Byte";
      } else if (clazz.equals(CompoundTag.class)) {
         return "TAG_Compound";
      } else if (clazz.equals(DoubleTag.class)) {
         return "TAG_Double";
      } else if (clazz.equals(EndTag.class)) {
         return "TAG_End";
      } else if (clazz.equals(FloatTag.class)) {
         return "TAG_Float";
      } else if (clazz.equals(IntTag.class)) {
         return "TAG_Int";
      } else if (clazz.equals(ListTag.class)) {
         return "TAG_List";
      } else if (clazz.equals(LongTag.class)) {
         return "TAG_Long";
      } else if (clazz.equals(ShortTag.class)) {
         return "TAG_Short";
      } else if (clazz.equals(StringTag.class)) {
         return "TAG_String";
      } else if (clazz.equals(IntArrayTag.class)) {
         return "TAG_Int_Array";
      } else if (clazz.equals(LongArrayTag.class)) {
         return "TAG_Long_Array";
      } else {
         throw new IllegalArgumentException("Invalid tag class (" + clazz.getName() + ").");
      }
   }

   public static int getTypeCode(Class<? extends Tag<?, ?>> clazz) {
      if (clazz == ByteArrayTag.class) {
         return 7;
      } else if (clazz == ByteTag.class) {
         return 1;
      } else if (clazz == CompoundTag.class) {
         return 10;
      } else if (clazz == DoubleTag.class) {
         return 6;
      } else if (clazz == EndTag.class) {
         return 0;
      } else if (clazz == FloatTag.class) {
         return 5;
      } else if (clazz == IntArrayTag.class) {
         return 11;
      } else if (clazz == IntTag.class) {
         return 3;
      } else if (clazz.equals(ListTag.class)) {
         return 9;
      } else if (clazz == LongArrayTag.class) {
         return 12;
      } else if (clazz == LongTag.class) {
         return 4;
      } else if (clazz == ShortTag.class) {
         return 2;
      } else if (clazz == StringTag.class) {
         return 8;
      } else {
         throw new IllegalArgumentException("Invalid tag class (" + clazz.getName() + ")");
      }
   }

   public static Class<? extends Tag<?, ?>> getTypeClass(int type) {
      return NBTConstants.getClassFromType(type);
   }

   public static Vector3 toVector(ListTag<?, ?> listTag) {
      Preconditions.checkNotNull(listTag);
      return Vector3.at(listTag.asDouble(0), listTag.asDouble(1), listTag.asDouble(2));
   }

   public static <T extends Tag<?, ?>> T getChildTag(Map<String, Tag<?, ?>> items, String key, Class<T> expected) throws InvalidFormatException {
      if (!items.containsKey(key)) {
         throw new InvalidFormatException("Missing a \"" + key + "\" tag");
      } else {
         Tag<?, ?> tag = items.get(key);
         if (!expected.isInstance(tag)) {
            throw new InvalidFormatException(key + " tag is not of tag type " + expected.getName());
         } else {
            return expected.cast(tag);
         }
      }
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.2"
   )
   public static void addUUIDToMap(Map<String, Tag<?, ?>> map, UUID uuid) {
      int[] uuidArray = new int[]{
         (int)(uuid.getMostSignificantBits() >> 32),
         (int)uuid.getMostSignificantBits(),
         (int)(uuid.getLeastSignificantBits() >> 32),
         (int)uuid.getLeastSignificantBits()
      };
      map.put("UUID", new IntArrayTag(uuidArray));
      map.put("UUIDMost", new LongTag(uuid.getMostSignificantBits()));
      map.put("UUIDLeast", new LongTag(uuid.getLeastSignificantBits()));
      map.put("WorldUUIDMost", new LongTag(uuid.getMostSignificantBits()));
      map.put("WorldUUIDLeast", new LongTag(uuid.getLeastSignificantBits()));
      map.put("PersistentIDMSB", new LongTag(uuid.getMostSignificantBits()));
      map.put("PersistentIDLSB", new LongTag(uuid.getLeastSignificantBits()));
   }
}
