package com.fastasyncworldedit.core.util;

import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.storage.InvalidFormatException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

public final class NbtUtils {
   private NbtUtils() {
   }

   public static <T extends LinTag> T getChildTag(LinCompoundTag tag, String key, LinTagType expected) throws InvalidFormatException {
      LinTag childTag = tag.value().get(key);
      if (childTag == null) {
         throw new InvalidFormatException("Missing a \"" + key + "\" tag");
      } else if (childTag.type().id() != expected.id()) {
         throw new InvalidFormatException(key + " tag is not of tag type " + expected);
      } else {
         return (T)childTag;
      }
   }

   public static int getInt(LinCompoundTag tag, String key) throws InvalidFormatException {
      LinTag childTag = tag.value().get(key);
      if (childTag == null) {
         throw new InvalidFormatException("Missing a \"" + key + "\" tag");
      } else {
         LinTagType<?> type = childTag.type();
         if (type == LinTagType.intTag()) {
            return ((LinIntTag)childTag).value();
         } else if (type == LinTagType.byteTag()) {
            return ((LinByteTag)childTag).value();
         } else if (type == LinTagType.shortTag()) {
            return ((LinShortTag)childTag).value();
         } else {
            throw new InvalidFormatException(key + " tag is not of int, short or byte tag type.");
         }
      }
   }

   public static Map<String, LinTag<?>> getLinCompoundTagValues(LinCompoundTag tag) {
      Map<String, LinTag<?>> value = new HashMap<>();
      value.putAll(tag.value());
      return value;
   }

   public static UUID uuid(FaweCompoundTag compoundTag) {
      LinCompoundTag linTag = compoundTag.linTag();
      LinIntArrayTag uuidTag = linTag.findTag("UUID", LinTagType.intArrayTag());
      if (uuidTag != null) {
         int[] arr = uuidTag.value();
         return new UUID((long)arr[0] << 32 | arr[1] & 4294967295L, (long)arr[2] << 32 | arr[3] & 4294967295L);
      } else {
         LinLongTag uuidMostTag = linTag.findTag("UUIDMost", LinTagType.longTag());
         if (uuidMostTag != null) {
            return new UUID(uuidMostTag.valueAsLong(), linTag.getTag("UUIDLeast", LinTagType.longTag()).valueAsLong());
         } else {
            LinLongTag uuidMostTagx = linTag.findTag("WorldUUIDMost", LinTagType.longTag());
            if (uuidMostTagx != null) {
               return new UUID(uuidMostTagx.valueAsLong(), linTag.getTag("WorldUUIDLeast", LinTagType.longTag()).valueAsLong());
            } else {
               LinLongTag uuidMostTagxx = linTag.findTag("PersistentIDMSB", LinTagType.longTag());
               if (uuidMostTagxx != null) {
                  return new UUID(uuidMostTagxx.valueAsLong(), linTag.getTag("PersistentIDLSB", LinTagType.longTag()).valueAsLong());
               } else {
                  throw new IllegalArgumentException("no uuid present");
               }
            }
         }
      }
   }

   @Nonnull
   public static LinCompoundTag withPosition(@Nonnull LinCompoundTag tag, int x, int y, int z) {
      return tag.toBuilder().putInt("x", x).putInt("y", y).putInt("z", z).build();
   }

   @Nonnull
   public static FaweCompoundTag withPosition(@Nonnull FaweCompoundTag tag, int x, int y, int z) {
      return FaweCompoundTag.of(withPosition(tag.linTag(), x, y, z));
   }

   @Nonnull
   public static LinCompoundTag withEntityInfo(@Nonnull LinCompoundTag tag, @Nonnull Entity entity) {
      LinCompoundTag.Builder builder = tag.toBuilder().putString("Id", entity.getState().getType().id());
      LinListTag<LinDoubleTag> pos = tag.findListTag("Pos", LinTagType.doubleTag());
      if (pos != null) {
         Location loc = entity.getLocation();
         LinListTag<LinDoubleTag> newPos = LinListTag.builder(LinTagType.doubleTag())
            .add(LinDoubleTag.of(loc.x()))
            .add(LinDoubleTag.of(loc.y()))
            .add(LinDoubleTag.of(loc.z()))
            .build();
         builder.put("Pos", newPos);
      }

      return builder.build();
   }

   public static void addUUIDToMap(Map<String, LinTag<?>> map, UUID uuid) {
      int[] uuidArray = new int[]{
         (int)(uuid.getMostSignificantBits() >> 32),
         (int)uuid.getMostSignificantBits(),
         (int)(uuid.getLeastSignificantBits() >> 32),
         (int)uuid.getLeastSignificantBits()
      };
      map.put("UUID", LinIntArrayTag.of(uuidArray));
      map.put("UUIDMost", LinLongTag.of(uuid.getMostSignificantBits()));
      map.put("UUIDLeast", LinLongTag.of(uuid.getLeastSignificantBits()));
      map.put("WorldUUIDMost", LinLongTag.of(uuid.getMostSignificantBits()));
      map.put("WorldUUIDLeast", LinLongTag.of(uuid.getLeastSignificantBits()));
      map.put("PersistentIDMSB", LinLongTag.of(uuid.getMostSignificantBits()));
      map.put("PersistentIDLSB", LinLongTag.of(uuid.getLeastSignificantBits()));
   }

   public static Vector3 entityPosition(FaweCompoundTag compoundTag) {
      LinListTag<LinDoubleTag> pos = compoundTag.linTag().getListTag("Pos", LinTagType.doubleTag());
      double x = pos.get(0).valueAsDouble();
      double y = pos.get(1).valueAsDouble();
      double z = pos.get(2).valueAsDouble();
      return Vector3.at(x, y, z);
   }
}
