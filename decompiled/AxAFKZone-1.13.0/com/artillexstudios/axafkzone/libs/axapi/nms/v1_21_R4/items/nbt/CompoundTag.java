package com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.nbt;

import com.artillexstudios.axafkzone.libs.axapi.items.nbt.Tag;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class CompoundTag implements com.artillexstudios.axafkzone.libs.axapi.items.nbt.CompoundTag {
   private final NBTTagCompound parent;

   public CompoundTag(NBTTagCompound tag) {
      this.parent = tag;
   }

   @Override
   public void put(String key, Tag tag) {
      this.parent.a(key, (NBTBase)(tag.getParent() instanceof NBTTagCompound compoundTag ? compoundTag : (NBTTagList)tag.getParent()));
   }

   @Override
   public void putByte(String key, byte value) {
      this.parent.a(key, value);
   }

   @Override
   public void putShort(String key, short value) {
      this.parent.a(key, value);
   }

   @Override
   public void putInt(String key, int value) {
      this.parent.a(key, value);
   }

   @Override
   public void putLong(String key, long value) {
      this.parent.a(key, value);
   }

   @Override
   public void putUUID(String key, UUID value) {
      if (this.parent.b(key + "Most") && this.parent.b(key + "Least")) {
         this.parent.r(key + "Most");
         this.parent.r(key + "Least");
      }

      this.putIntArray(key, uuidToIntArray(value));
   }

   @Override
   public UUID getUUID(String key) {
      return this.contains(key + "Most") && this.contains(key + "Least")
         ? new UUID(this.getLong(key + "Most"), this.getLong(key + "Least"))
         : (this.contains(key) ? uuidFromIntArray(this.getIntArray(key)) : null);
   }

   @Override
   public boolean containsUUID(String key) {
      return this.contains(key + "Least") && this.contains(key + "Most") || this.contains(key);
   }

   @Override
   public void putFloat(String key, float value) {
      this.parent.a(key, value);
   }

   @Override
   public void putDouble(String key, double value) {
      this.parent.a(key, value);
   }

   @Override
   public void putString(String key, String value) {
      this.parent.a(key, value);
   }

   @Override
   public void putByteArray(String key, byte[] value) {
      this.parent.a(key, value);
   }

   @Override
   public void putByteArray(String key, List<Byte> value) {
      byte[] bytes = new byte[value.size()];
      int i = 0;

      for (Byte b : value) {
         bytes[i] = b;
         i++;
      }

      this.parent.a(key, bytes);
   }

   @Override
   public void putIntArray(String key, int[] value) {
      this.parent.a(key, value);
   }

   @Override
   public void putIntArray(String key, List<Integer> value) {
      int[] ints = new int[value.size()];
      int i = 0;

      for (Integer integer : value) {
         ints[i] = integer;
         i++;
      }

      this.parent.a(key, ints);
   }

   @Override
   public void putLongArray(String key, long[] value) {
      this.parent.a(key, value);
   }

   @Override
   public void putLongArray(String key, List<Long> value) {
      long[] longs = new long[value.size()];
      int i = 0;

      for (Long l : value) {
         longs[i] = l;
         i++;
      }

      this.parent.a(key, longs);
   }

   @Override
   public void putBoolean(String key, boolean value) {
      this.parent.a(key, value);
   }

   @Override
   public boolean contains(String key) {
      return this.parent.b(key);
   }

   @Override
   public Byte getByte(String key) {
      return (Byte)this.parent.c(key).orElse(null);
   }

   @Override
   public Short getShort(String key) {
      return (Short)this.parent.d(key).orElse(null);
   }

   @Override
   public Integer getInt(String key) {
      return (Integer)this.parent.e(key).orElse(null);
   }

   @Override
   public Long getLong(String key) {
      return (Long)this.parent.f(key).orElse(null);
   }

   @Override
   public Float getFloat(String key) {
      return (Float)this.parent.g(key).orElse(null);
   }

   @Override
   public Double getDouble(String key) {
      return (Double)this.parent.h(key).orElse(null);
   }

   @Override
   public String getString(String key) {
      return (String)this.parent.i(key).orElse(null);
   }

   @Override
   public byte[] getByteArray(String key) {
      return (byte[])this.parent.j(key).orElse(null);
   }

   @Override
   public int[] getIntArray(String key) {
      return (int[])this.parent.k(key).orElse(null);
   }

   @Override
   public long[] getLongArray(String key) {
      return (long[])this.parent.l(key).orElse(null);
   }

   @Override
   public com.artillexstudios.axafkzone.libs.axapi.items.nbt.CompoundTag getCompound(String key) {
      return this.parent.m(key).map(CompoundTag::new).orElse(null);
   }

   public ListTag getList(String key) {
      return this.parent.o(key).map(ListTag::new).orElse(null);
   }

   @Override
   public Boolean getBoolean(String key) {
      return (Boolean)this.parent.q(key).orElse(null);
   }

   @Override
   public void remove(String key) {
      this.parent.r(key);
   }

   @Override
   public boolean isEmpty() {
      return this.parent.j();
   }

   @Override
   public Set<String> getAllKeys() {
      return this.parent.e();
   }

   public NBTTagCompound getParent() {
      return this.parent;
   }

   public static UUID uuidFromIntArray(int[] array) {
      return new UUID((long)array[0] << 32 | array[1] & 4294967295L, (long)array[2] << 32 | array[3] & 4294967295L);
   }

   public static int[] uuidToIntArray(UUID uuid) {
      long l = uuid.getMostSignificantBits();
      long m = uuid.getLeastSignificantBits();
      return leastMostToIntArray(l, m);
   }

   private static int[] leastMostToIntArray(long uuidMost, long uuidLeast) {
      return new int[]{(int)(uuidMost >> 32), (int)uuidMost, (int)(uuidLeast >> 32), (int)uuidLeast};
   }
}
