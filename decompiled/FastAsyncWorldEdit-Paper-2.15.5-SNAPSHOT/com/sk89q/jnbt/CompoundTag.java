package com.sk89q.jnbt;

import com.fastasyncworldedit.core.jnbt.NumberTag;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
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
import org.enginehub.linbus.tree.LinNumberTag;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

@Deprecated
public class CompoundTag extends Tag<Object, LinCompoundTag> {
   public CompoundTag(Map<String, Tag<?, ?>> value) {
      this(LinCompoundTag.of(Maps.transformValues(value, Tag::toLinTag)));
   }

   public CompoundTag(LinCompoundTag tag) {
      super(tag);
   }

   public boolean containsKey(String key) {
      return this.linTag.value().containsKey(key);
   }

   public Map<String, Tag<?, ?>> getValue() {
      return ImmutableMap.copyOf(Maps.transformValues(this.linTag.value(), tag -> LinBusConverter.toJnbtTag(tag)));
   }

   public CompoundTag setValue(Map<String, Tag<?, ?>> value) {
      return new CompoundTag(value);
   }

   public CompoundTagBuilder createBuilder() {
      return new CompoundTagBuilder(this.linTag);
   }

   public byte[] getByteArray(String key) {
      LinByteArrayTag tag = this.linTag.findTag(key, LinTagType.byteArrayTag());
      return tag == null ? new byte[0] : tag.value();
   }

   public byte getByte(String key) {
      LinByteTag tag = this.linTag.findTag(key, LinTagType.byteTag());
      return tag == null ? 0 : tag.value();
   }

   public double getDouble(String key) {
      LinDoubleTag tag = this.linTag.findTag(key, LinTagType.doubleTag());
      return tag == null ? 0.0 : tag.value();
   }

   public double asDouble(String key) {
      LinTag<?> tag = this.linTag.value().get(key);
      if (tag instanceof LinNumberTag<?> numberTag) {
         Number value = numberTag.value();
         return value.doubleValue();
      } else {
         return 0.0;
      }
   }

   public float getFloat(String key) {
      LinFloatTag tag = this.linTag.findTag(key, LinTagType.floatTag());
      return tag == null ? 0.0F : tag.value();
   }

   public int[] getIntArray(String key) {
      LinIntArrayTag tag = this.linTag.findTag(key, LinTagType.intArrayTag());
      return tag == null ? new int[0] : tag.value();
   }

   public int getInt(String key) {
      LinIntTag tag = this.linTag.findTag(key, LinTagType.intTag());
      return tag == null ? 0 : tag.value();
   }

   public int asInt(String key) {
      LinTag<?> tag = this.linTag.value().get(key);
      if (tag instanceof LinNumberTag<?> numberTag) {
         Number value = numberTag.value();
         return value.intValue();
      } else {
         return 0;
      }
   }

   public List<? extends Tag<?, ?>> getList(String key) {
      return this.getListTag(key).getValue();
   }

   public <EV, E extends LinTag<EV>> ListTag<EV, E> getListTag(String key) {
      LinListTag<E> tag = this.linTag.findTag(key, LinTagType.listTag());
      if (tag == null) {
         LinTagType<E> endGenerically = (LinTagType<E>)LinTagType.endTag();
         return new ListTag<>(LinListTag.empty(endGenerically));
      } else {
         return new ListTag<>(tag);
      }
   }

   public <T extends Tag<?, ?>> List<T> getList(String key, Class<T> listType) {
      ListTag<?, ?> listTag = this.getListTag(key);
      return (List<T>)(listTag.getType().equals(listType) ? listTag.getValue() : Collections.emptyList());
   }

   public long[] getLongArray(String key) {
      LinLongArrayTag tag = this.linTag.findTag(key, LinTagType.longArrayTag());
      return tag == null ? new long[0] : tag.value();
   }

   public long getLong(String key) {
      LinLongTag tag = this.linTag.findTag(key, LinTagType.longTag());
      return tag == null ? 0L : tag.value();
   }

   public long asLong(String key) {
      LinTag<?> tag = this.linTag.value().get(key);
      if (tag instanceof LinNumberTag<?> numberTag) {
         Number value = numberTag.value();
         return value.longValue();
      } else {
         return 0L;
      }
   }

   public short getShort(String key) {
      LinShortTag tag = this.linTag.findTag(key, LinTagType.shortTag());
      return tag == null ? 0 : tag.value();
   }

   public String getString(String key) {
      LinStringTag tag = this.linTag.findTag(key, LinTagType.stringTag());
      return tag == null ? "" : tag.value();
   }

   public UUID getUUID() {
      UUID uuid;
      if (this.containsKey("UUID")) {
         int[] arr = this.getIntArray("UUID");
         uuid = new UUID((long)arr[0] << 32 | arr[1] & 4294967295L, (long)arr[2] << 32 | arr[3] & 4294967295L);
      } else if (this.containsKey("UUIDMost")) {
         uuid = new UUID(this.getLong("UUIDMost"), this.getLong("UUIDLeast"));
      } else if (this.containsKey("WorldUUIDMost")) {
         uuid = new UUID(this.getLong("WorldUUIDMost"), this.getLong("WorldUUIDLeast"));
      } else {
         if (!this.containsKey("PersistentIDMSB")) {
            return null;
         }

         uuid = new UUID(this.getLong("PersistentIDMSB"), this.getLong("PersistentIDLSB"));
      }

      return uuid;
   }

   public Vector3 getEntityPosition() {
      List<? extends Tag<?, ?>> posTags = this.getList("Pos");
      double x = ((NumberTag)posTags.get(0)).getValue().doubleValue();
      double y = ((NumberTag)posTags.get(1)).getValue().doubleValue();
      double z = ((NumberTag)posTags.get(2)).getValue().doubleValue();
      return Vector3.at(x, y, z);
   }

   public Location getEntityLocation(Extent extent) {
      List<? extends Tag<?, ?>> rotTag = this.getList("Rotation");
      float yaw = ((NumberTag)rotTag.get(0)).getValue().floatValue();
      float pitch = ((NumberTag)rotTag.get(1)).getValue().floatValue();
      return new Location(extent, this.getEntityPosition(), yaw, pitch);
   }

   @Override
   public int getTypeCode() {
      return 10;
   }

   public Map<String, Object> toRaw() {
      HashMap<String, Object> raw = new HashMap<>();
      if (this.getValue().isEmpty()) {
         return raw;
      } else {
         for (Entry<String, Tag<?, ?>> entry : this.getValue().entrySet()) {
            raw.put(entry.getKey(), entry.getValue().toRaw());
         }

         return raw;
      }
   }
}
