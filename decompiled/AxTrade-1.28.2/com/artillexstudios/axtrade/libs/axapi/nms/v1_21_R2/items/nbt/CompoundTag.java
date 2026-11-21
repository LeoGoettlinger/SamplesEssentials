package com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R2.items.nbt;

import com.artillexstudios.axtrade.libs.axapi.items.nbt.Tag;
import com.artillexstudios.axtrade.libs.axapi.reflection.FieldAccessor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.ReportedException;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTNumber;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagType;

public class CompoundTag implements com.artillexstudios.axtrade.libs.axapi.items.nbt.CompoundTag {
   private static final FieldAccessor accessor = FieldAccessor.builder().withClass(NBTTagCompound.class).withField("x").build();
   private final NBTTagCompound parent;
   private final Map<String, NBTBase> tags;

   public CompoundTag(NBTTagCompound tag) {
      this.parent = tag;
      this.tags = accessor.getUnchecked(tag);
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
      if (this.parent.e(key + "Most") && this.parent.e(key + "Least")) {
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
      return this.parent.e(key);
   }

   @Override
   public Byte getByte(String key) {
      try {
         if (this.parent.b(key, 99)) {
            return ((NBTNumber)this.tags.get(key)).i();
         }
      } catch (ClassCastException var3) {
      }

      return null;
   }

   @Override
   public Short getShort(String key) {
      try {
         if (this.parent.b(key, 99)) {
            return ((NBTNumber)this.tags.get(key)).h();
         }
      } catch (ClassCastException var3) {
      }

      return null;
   }

   @Override
   public Integer getInt(String key) {
      try {
         if (this.parent.b(key, 99)) {
            return ((NBTNumber)this.tags.get(key)).g();
         }
      } catch (ClassCastException var3) {
      }

      return null;
   }

   @Override
   public Long getLong(String key) {
      try {
         if (this.parent.b(key, 99)) {
            return ((NBTNumber)this.tags.get(key)).f();
         }
      } catch (ClassCastException var3) {
      }

      return null;
   }

   @Override
   public Float getFloat(String key) {
      try {
         if (this.parent.b(key, 99)) {
            return ((NBTNumber)this.tags.get(key)).k();
         }
      } catch (ClassCastException var3) {
      }

      return null;
   }

   @Override
   public Double getDouble(String key) {
      try {
         if (this.parent.b(key, 99)) {
            return ((NBTNumber)this.tags.get(key)).j();
         }
      } catch (ClassCastException var3) {
      }

      return null;
   }

   @Override
   public String getString(String key) {
      try {
         if (this.parent.b(key, 8)) {
            return this.tags.get(key).u_();
         }
      } catch (ClassCastException var3) {
      }

      return null;
   }

   @Override
   public byte[] getByteArray(String key) {
      try {
         return this.parent.b(key, 7) ? ((NBTTagByteArray)this.tags.get(key)).e() : null;
      } catch (ClassCastException var3) {
         throw new ReportedException(this.createReport(key, NBTTagByteArray.a, var3));
      }
   }

   @Override
   public int[] getIntArray(String key) {
      try {
         return this.parent.b(key, 11) ? ((NBTTagIntArray)this.tags.get(key)).g() : null;
      } catch (ClassCastException var3) {
         throw new ReportedException(this.createReport(key, NBTTagIntArray.a, var3));
      }
   }

   @Override
   public long[] getLongArray(String key) {
      try {
         return this.parent.b(key, 12) ? ((NBTTagLongArray)this.tags.get(key)).g() : null;
      } catch (ClassCastException var3) {
         throw new ReportedException(this.createReport(key, NBTTagLongArray.a, var3));
      }
   }

   public CompoundTag getCompound(String key) {
      try {
         return this.parent.b(key, 10) ? new CompoundTag((NBTTagCompound)this.tags.get(key)) : null;
      } catch (ClassCastException var3) {
         throw new ReportedException(this.createReport(key, NBTTagCompound.b, var3));
      }
   }

   public ListTag getList(String key) {
      try {
         if (this.parent.d(key) == 9) {
            NBTTagList listTag = (NBTTagList)this.tags.get(key);
            return !listTag.isEmpty() && listTag.f() != 10 ? null : new ListTag(listTag);
         } else {
            return null;
         }
      } catch (ClassCastException var3) {
         throw new ReportedException(this.createReport(key, NBTTagList.a, var3));
      }
   }

   @Override
   public Boolean getBoolean(String key) {
      Byte value = this.getByte(key);
      return value == null ? null : value != 0;
   }

   @Override
   public void remove(String key) {
      this.parent.r(key);
   }

   @Override
   public boolean isEmpty() {
      return this.parent.g();
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

   private CrashReport createReport(String tagName, NBTTagType<?> type, ClassCastException exception) {
      CrashReport crashReport = CrashReport.a(exception, "Reading NBT data");
      CrashReportSystemDetails crashReportCategory = crashReport.a("Corrupt NBT tag", 1);
      crashReportCategory.a("Tag type found", () -> this.tags.get(tagName).c().a());
      crashReportCategory.a("Tag type expected", type::a);
      crashReportCategory.a("Tag name", tagName);
      return crashReport;
   }
}
