package de.tr7zw.nbtapi;

import de.tr7zw.nbtapi.iface.NBTHandler;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import de.tr7zw.nbtapi.utils.CheckUtil;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import de.tr7zw.nbtapi.utils.PathUtil;
import de.tr7zw.nbtapi.utils.UUIDUtil;
import de.tr7zw.nbtapi.utils.nmsmappings.Forge1710Mappings;
import de.tr7zw.nbtapi.utils.nmsmappings.ReflectionMethod;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bukkit.inventory.ItemStack;

public class NBTCompound implements ReadWriteNBT {
   private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
   private final Lock readLock = this.readWriteLock.readLock();
   private final Lock writeLock = this.readWriteLock.writeLock();
   private String compundName;
   private NBTCompound parent;
   private final boolean readOnly;
   private Object readOnlyCache;

   protected NBTCompound(NBTCompound owner, String name) {
      this(owner, name, false);
   }

   protected NBTCompound(NBTCompound owner, String name, boolean readOnly) {
      this.compundName = name;
      this.parent = owner;
      this.readOnly = readOnly;
   }

   protected Lock getReadLock() {
      return this.readLock;
   }

   protected Lock getWriteLock() {
      return this.writeLock;
   }

   protected void saveCompound() {
      if (this.parent != null) {
         this.parent.saveCompound();
      }
   }

   protected void setResolvedObject(Object object) {
      if (this.isClosed()) {
         throw new NbtApiException("Tried using closed NBT data!");
      } else {
         if (this.readOnly) {
            this.readOnlyCache = object;
         }
      }
   }

   protected void setClosed() {
      if (this.parent != null) {
         this.parent.setClosed();
      }
   }

   protected boolean isClosed() {
      return this.parent != null ? this.parent.isClosed() : false;
   }

   protected boolean isReadOnly() {
      return this.readOnly;
   }

   protected Object getResolvedObject() {
      if (this.isClosed()) {
         throw new NbtApiException("Tried using closed NBT data!");
      } else if (this.readOnlyCache != null) {
         return this.readOnlyCache;
      } else {
         Object rootnbttag = this.getCompound();
         if (rootnbttag instanceof Optional) {
            rootnbttag = ((Optional)rootnbttag).orElse(null);
         }

         if (rootnbttag == null) {
            return null;
         } else if (!NBTReflectionUtil.validCompound(this)) {
            throw new NbtApiException("The Compound wasn't able to be linked back to the root!");
         } else {
            Object workingtag = NBTReflectionUtil.getToCompount(rootnbttag, this);
            if (this.readOnly) {
               this.readOnlyCache = workingtag;
            }

            return workingtag;
         }
      }
   }

   public String getName() {
      return this.compundName;
   }

   public Object getCompound() {
      return this.parent.getCompound();
   }

   protected void setCompound(Object compound) {
      this.parent.setCompound(compound);
   }

   public NBTCompound getParent() {
      return this.parent;
   }

   public void mergeCompound(NBTCompound comp) {
      if (comp != null) {
         try {
            this.writeLock.lock();
            NBTReflectionUtil.mergeOtherNBTCompound(this, comp);
            this.saveCompound();
         } finally {
            this.writeLock.unlock();
         }
      }
   }

   @Override
   public void mergeCompound(ReadableNBT comp) {
      if (comp instanceof NBTCompound) {
         this.mergeCompound((NBTCompound)comp);
      } else {
         throw new NbtApiException("Unknown NBT object: " + comp);
      }
   }

   @Override
   public void setString(String key, String value) {
      try {
         this.writeLock.lock();
         NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_STRING, key, value);
         this.saveCompound();
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public String getString(String key) {
      String var2;
      try {
         this.readLock.lock();
         var2 = (String)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_STRING, key);
      } finally {
         this.readLock.unlock();
      }

      return var2;
   }

   @Override
   public void setInteger(String key, Integer value) {
      try {
         this.writeLock.lock();
         NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_INT, key, value);
         this.saveCompound();
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public Integer getInteger(String key) {
      Integer var2;
      try {
         this.readLock.lock();
         var2 = (Integer)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_INT, key);
      } finally {
         this.readLock.unlock();
      }

      return var2;
   }

   @Override
   public void setDouble(String key, Double value) {
      try {
         this.writeLock.lock();
         NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_DOUBLE, key, value);
         this.saveCompound();
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public Double getDouble(String key) {
      Double var2;
      try {
         this.readLock.lock();
         var2 = (Double)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_DOUBLE, key);
      } finally {
         this.readLock.unlock();
      }

      return var2;
   }

   @Override
   public void setByte(String key, Byte value) {
      try {
         this.writeLock.lock();
         NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_BYTE, key, value);
         this.saveCompound();
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public Byte getByte(String key) {
      Byte var2;
      try {
         this.readLock.lock();
         var2 = (Byte)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_BYTE, key);
      } finally {
         this.readLock.unlock();
      }

      return var2;
   }

   @Override
   public void setShort(String key, Short value) {
      try {
         this.writeLock.lock();
         NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_SHORT, key, value);
         this.saveCompound();
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public Short getShort(String key) {
      Short var2;
      try {
         this.readLock.lock();
         var2 = (Short)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_SHORT, key);
      } finally {
         this.readLock.unlock();
      }

      return var2;
   }

   @Override
   public void setLong(String key, Long value) {
      try {
         this.writeLock.lock();
         NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_LONG, key, value);
         this.saveCompound();
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public Long getLong(String key) {
      Long var2;
      try {
         this.readLock.lock();
         var2 = (Long)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_LONG, key);
      } finally {
         this.readLock.unlock();
      }

      return var2;
   }

   @Override
   public void setFloat(String key, Float value) {
      try {
         this.writeLock.lock();
         NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_FLOAT, key, value);
         this.saveCompound();
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public Float getFloat(String key) {
      Float var2;
      try {
         this.readLock.lock();
         var2 = (Float)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_FLOAT, key);
      } finally {
         this.readLock.unlock();
      }

      return var2;
   }

   @Override
   public void setByteArray(String key, byte[] value) {
      try {
         this.writeLock.lock();
         NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_BYTEARRAY, key, value);
         this.saveCompound();
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public byte[] getByteArray(String key) {
      byte[] var2;
      try {
         this.readLock.lock();
         var2 = (byte[])NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_BYTEARRAY, key);
      } finally {
         this.readLock.unlock();
      }

      return var2;
   }

   @Override
   public void setIntArray(String key, int[] value) {
      try {
         this.writeLock.lock();
         NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_INTARRAY, key, value);
         this.saveCompound();
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public int[] getIntArray(String key) {
      int[] var2;
      try {
         this.readLock.lock();
         var2 = (int[])NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_INTARRAY, key);
      } finally {
         this.readLock.unlock();
      }

      return var2;
   }

   @Override
   public void setLongArray(String key, long[] value) {
      CheckUtil.assertAvailable(MinecraftVersion.MC1_16_R1);

      try {
         this.writeLock.lock();
         NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_LONGARRAY, key, value);
         this.saveCompound();
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public long[] getLongArray(String key) {
      CheckUtil.assertAvailable(MinecraftVersion.MC1_16_R1);

      long[] var2;
      try {
         this.readLock.lock();
         var2 = (long[])NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_LONGARRAY, key);
      } finally {
         this.readLock.unlock();
      }

      return var2;
   }

   @Override
   public void setBoolean(String key, Boolean value) {
      try {
         this.writeLock.lock();
         NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_BOOLEAN, key, value);
         this.saveCompound();
      } finally {
         this.writeLock.unlock();
      }
   }

   protected void set(String key, Object val) {
      NBTReflectionUtil.set(this, key, val);
      this.saveCompound();
   }

   @Override
   public Boolean getBoolean(String key) {
      Boolean var2;
      try {
         this.readLock.lock();
         var2 = (Boolean)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_BOOLEAN, key);
      } finally {
         this.readLock.unlock();
      }

      return var2;
   }

   @Deprecated
   public void setObject(String key, Object value) {
      try {
         this.writeLock.lock();
         NBTReflectionUtil.setObject(this, key, value);
         this.saveCompound();
      } finally {
         this.writeLock.unlock();
      }
   }

   @Deprecated
   public <T> T getObject(String key, Class<T> type) {
      Object var3;
      try {
         this.readLock.lock();
         var3 = NBTReflectionUtil.getObject(this, key, type);
      } finally {
         this.readLock.unlock();
      }

      return (T)var3;
   }

   @Override
   public void setItemStack(String key, ItemStack item) {
      try {
         this.writeLock.lock();
         this.removeKey(key);
         this.addCompound(key).mergeCompound((NBTCompound)NBTItem.convertItemtoNBT(item));
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public ItemStack getItemStack(String key) {
      Object var3;
      try {
         this.readLock.lock();
         NBTCompound comp = this.getCompound(key);
         if (comp != null) {
            return NBTItem.convertNBTtoItem(comp);
         }

         var3 = null;
      } finally {
         this.readLock.unlock();
      }

      return (ItemStack)var3;
   }

   @Override
   public void setItemStackArray(String key, ItemStack[] items) {
      try {
         this.writeLock.lock();
         this.removeKey(key);
         this.addCompound(key).mergeCompound((NBTCompound)NBTItem.convertItemArraytoNBT(items));
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public ItemStack[] getItemStackArray(String key) {
      Object var3;
      try {
         this.readLock.lock();
         NBTCompound comp = this.getCompound(key);
         if (comp != null) {
            return NBTItem.convertNBTtoItemArray(comp);
         }

         var3 = null;
      } finally {
         this.readLock.unlock();
      }

      return (ItemStack[])var3;
   }

   @Override
   public void setUUID(String key, UUID value) {
      try {
         this.writeLock.lock();
         if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R4)) {
            this.setIntArray(key, UUIDUtil.uuidToIntArray(value));
         } else if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R1)) {
            NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_UUID, key, value);
         } else {
            this.setString(key, value.toString());
         }

         this.saveCompound();
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public UUID getUUID(String key) {
      Object var4;
      try {
         this.readLock.lock();
         NBTType type = this.getType(key);
         if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R4) && type == NBTType.NBTTagIntArray) {
            return UUIDUtil.uuidFromIntArray(this.getIntArray(key));
         }

         if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R1) && type == NBTType.NBTTagIntArray) {
            return (UUID)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_UUID, key);
         }

         if (type != NBTType.NBTTagString) {
            return null;
         }

         try {
            return UUID.fromString(this.getString(key));
         } catch (IllegalArgumentException var8) {
            var4 = null;
         }
      } finally {
         this.readLock.unlock();
      }

      return (UUID)var4;
   }

   @Deprecated
   public Boolean hasKey(String key) {
      return this.hasTag(key);
   }

   @Override
   public boolean hasTag(String key) {
      boolean var3;
      try {
         this.readLock.lock();
         Boolean b = (Boolean)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_HAS_KEY, key);
         if (b != null) {
            return b;
         }

         var3 = false;
      } finally {
         this.readLock.unlock();
      }

      return var3;
   }

   @Override
   public void removeKey(String key) {
      try {
         this.writeLock.lock();
         NBTReflectionUtil.remove(this, key);
         this.saveCompound();
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public Set<String> getKeys() {
      HashSet var1;
      try {
         this.readLock.lock();
         var1 = new HashSet<>(NBTReflectionUtil.getKeys(this));
      } finally {
         this.readLock.unlock();
      }

      return var1;
   }

   public NBTCompound addCompound(String name) {
      NBTCompound comp;
      try {
         this.writeLock.lock();
         if (this.getType(name) != NBTType.NBTTagCompound) {
            NBTReflectionUtil.addNBTTagCompound(this, name);
            comp = this.getCompound(name);
            if (comp == null) {
               throw new NbtApiException("Error while adding Compound, got null!");
            }

            this.saveCompound();
            return comp;
         }

         comp = this.getCompound(name);
      } finally {
         this.writeLock.unlock();
      }

      return comp;
   }

   public NBTCompound getCompound(String name) {
      NBTCompound var3;
      try {
         this.readLock.lock();
         if (this.getType(name) != NBTType.NBTTagCompound) {
            return null;
         }

         NBTCompound next = new NBTCompound(this, name, this.readOnly);
         if (!NBTReflectionUtil.validCompound(next)) {
            return null;
         }

         var3 = next;
      } finally {
         this.readLock.unlock();
      }

      return var3;
   }

   public NBTCompound getOrCreateCompound(String name) {
      return this.addCompound(name);
   }

   public NBTList<String> getStringList(String name) {
      NBTList var3;
      try {
         this.writeLock.lock();
         NBTList<String> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagString, String.class);
         this.saveCompound();
         var3 = list;
      } finally {
         this.writeLock.unlock();
      }

      return var3;
   }

   public NBTList<Integer> getIntegerList(String name) {
      NBTList var3;
      try {
         this.writeLock.lock();
         NBTList<Integer> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagInt, Integer.class);
         this.saveCompound();
         var3 = list;
      } finally {
         this.writeLock.unlock();
      }

      return var3;
   }

   public NBTList<int[]> getIntArrayList(String name) {
      NBTList var3;
      try {
         this.writeLock.lock();
         NBTList<int[]> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagIntArray, int[].class);
         this.saveCompound();
         var3 = list;
      } finally {
         this.writeLock.unlock();
      }

      return var3;
   }

   public NBTList<UUID> getUUIDList(String name) {
      NBTList var3;
      try {
         this.writeLock.lock();
         NBTList<UUID> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagIntArray, UUID.class);
         this.saveCompound();
         var3 = list;
      } finally {
         this.writeLock.unlock();
      }

      return var3;
   }

   public NBTList<Float> getFloatList(String name) {
      NBTList var3;
      try {
         this.writeLock.lock();
         NBTList<Float> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagFloat, Float.class);
         this.saveCompound();
         var3 = list;
      } finally {
         this.writeLock.unlock();
      }

      return var3;
   }

   public NBTList<Double> getDoubleList(String name) {
      NBTList var3;
      try {
         this.writeLock.lock();
         NBTList<Double> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagDouble, Double.class);
         this.saveCompound();
         var3 = list;
      } finally {
         this.writeLock.unlock();
      }

      return var3;
   }

   public NBTList<Long> getLongList(String name) {
      NBTList var3;
      try {
         this.writeLock.lock();
         NBTList<Long> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagLong, Long.class);
         this.saveCompound();
         var3 = list;
      } finally {
         this.writeLock.unlock();
      }

      return var3;
   }

   @Override
   public NBTType getListType(String name) {
      Object var2;
      try {
         this.readLock.lock();
         if (this.getType(name) == NBTType.NBTTagList) {
            return NBTReflectionUtil.getListType(this, name);
         }

         var2 = null;
      } finally {
         this.readLock.unlock();
      }

      return (NBTType)var2;
   }

   public NBTCompoundList getCompoundList(String name) {
      NBTCompoundList var3;
      try {
         this.writeLock.lock();
         NBTCompoundList list = (NBTCompoundList)NBTReflectionUtil.<NBTListCompound>getList(this, name, NBTType.NBTTagCompound, NBTListCompound.class);
         this.saveCompound();
         var3 = list;
      } finally {
         this.writeLock.unlock();
      }

      return var3;
   }

   @Override
   public <T> T getOrDefault(String key, T defaultValue) {
      if (defaultValue == null) {
         throw new NullPointerException("Default type in getOrDefault can't be null!");
      } else if (!this.hasTag(key)) {
         return defaultValue;
      } else {
         Class<?> clazz = defaultValue.getClass();
         if (clazz == Boolean.class || clazz == boolean.class) {
            return (T)this.getBoolean(key);
         } else if (clazz == Byte.class || clazz == byte.class) {
            return (T)this.getByte(key);
         } else if (clazz == Short.class || clazz == short.class) {
            return (T)this.getShort(key);
         } else if (clazz == Integer.class || clazz == int.class) {
            return (T)this.getInteger(key);
         } else if (clazz == Long.class || clazz == long.class) {
            return (T)this.getLong(key);
         } else if (clazz == Float.class || clazz == float.class) {
            return (T)this.getFloat(key);
         } else if (clazz == Double.class || clazz == double.class) {
            return (T)this.getDouble(key);
         } else if (clazz == byte[].class) {
            return (T)this.getByteArray(key);
         } else if (clazz == int[].class) {
            return (T)this.getIntArray(key);
         } else if (clazz == long[].class) {
            return (T)this.getLongArray(key);
         } else if (clazz == String.class) {
            return (T)this.getString(key);
         } else if (clazz == UUID.class) {
            UUID uuid = this.getUUID(key);
            return (T)(uuid == null ? defaultValue : uuid);
         } else if (clazz.isEnum()) {
            Object obj = this.getEnum(key, defaultValue.getClass());
            return (T)(obj == null ? defaultValue : obj);
         } else {
            throw new NbtApiException("Unsupported type for getOrDefault: " + clazz.getName());
         }
      }
   }

   @Override
   public <T> T getOrNull(String key, Class<?> type) {
      if (type == null) {
         throw new NullPointerException("Default type in getOrNull can't be null!");
      } else if (!this.hasTag(key)) {
         return null;
      } else if (type == Boolean.class || type == boolean.class) {
         return (T)this.getBoolean(key);
      } else if (type == Byte.class || type == byte.class) {
         return (T)this.getByte(key);
      } else if (type == Short.class || type == short.class) {
         return (T)this.getShort(key);
      } else if (type == Integer.class || type == int.class) {
         return (T)this.getInteger(key);
      } else if (type == Long.class || type == long.class) {
         return (T)this.getLong(key);
      } else if (type == Float.class || type == float.class) {
         return (T)this.getFloat(key);
      } else if (type == Double.class || type == double.class) {
         return (T)this.getDouble(key);
      } else if (type == byte[].class) {
         return (T)this.getByteArray(key);
      } else if (type == int[].class) {
         return (T)this.getIntArray(key);
      } else if (type == long[].class) {
         return (T)this.getLongArray(key);
      } else if (type == String.class) {
         return (T)this.getString(key);
      } else if (type == UUID.class) {
         return (T)this.getUUID(key);
      } else if (type.isEnum()) {
         return this.getEnum(key, (Class<T>)type);
      } else {
         throw new NbtApiException("Unsupported type for getOrNull: " + type.getName());
      }
   }

   @Override
   public <T> T resolveOrNull(String key, Class<?> type) {
      List<PathUtil.PathSegment> keys = PathUtil.splitPath(key);
      NBTCompound tag = this;

      for (int i = 0; i < keys.size() - 1; i++) {
         PathUtil.PathSegment segment = keys.get(i);
         if (!segment.hasIndex()) {
            tag = tag.getCompound(segment.getPath());
            if (tag == null) {
               return null;
            }
         } else if (tag.getType(segment.getPath()) == NBTType.NBTTagList && tag.getListType(segment.getPath()) == NBTType.NBTTagCompound) {
            NBTCompoundList list = tag.getCompoundList(segment.getPath());
            if (segment.getIndex() >= 0) {
               tag = list.get(segment.getIndex());
            } else {
               tag = list.get(list.size() + segment.getIndex());
            }
         }
      }

      PathUtil.PathSegment segment = keys.get(keys.size() - 1);
      return !segment.hasIndex() ? tag.getOrNull(segment.getPath(), type) : this.getIndexedValue(tag, segment, (Class<T>)type);
   }

   @Override
   public <T> T resolveOrDefault(String key, T defaultValue) {
      List<PathUtil.PathSegment> keys = PathUtil.splitPath(key);
      NBTCompound tag = this;

      for (int i = 0; i < keys.size() - 1; i++) {
         PathUtil.PathSegment segment = keys.get(i);
         if (!segment.hasIndex()) {
            tag = tag.getCompound(segment.getPath());
            if (tag == null) {
               return defaultValue;
            }
         } else if (tag.getType(segment.getPath()) == NBTType.NBTTagList && tag.getListType(segment.getPath()) == NBTType.NBTTagCompound) {
            NBTCompoundList list = tag.getCompoundList(segment.getPath());
            if (segment.getIndex() >= 0) {
               tag = list.get(segment.getIndex());
            } else {
               tag = list.get(list.size() + segment.getIndex());
            }
         }
      }

      PathUtil.PathSegment segment = keys.get(keys.size() - 1);
      return !segment.hasIndex() ? tag.getOrDefault(segment.getPath(), defaultValue) : this.getIndexedValue(tag, segment, (Class<T>)defaultValue.getClass());
   }

   private <T> T getIndexedValue(NBTCompound comp, PathUtil.PathSegment segment, Class<T> type) {
      if (type == String.class) {
         if (comp.getType(segment.getPath()) != NBTType.NBTTagList || comp.getListType(segment.getPath()) != NBTType.NBTTagString) {
            throw new NbtApiException("No fitting list/array found for " + segment.getPath() + " of type " + type);
         } else if (segment.getIndex() >= 0) {
            return (T)comp.getStringList(segment.getPath()).get(segment.getIndex());
         } else {
            List<String> list = comp.getStringList(segment.getPath());
            return (T)list.get(list.size() + segment.getIndex());
         }
      } else if (type == int.class || type == Integer.class) {
         if (comp.getType(segment.getPath()) == NBTType.NBTTagList && comp.getListType(segment.getPath()) == NBTType.NBTTagInt) {
            if (segment.getIndex() >= 0) {
               return (T)comp.getIntegerList(segment.getPath()).get(segment.getIndex());
            } else {
               List<Integer> list = comp.getIntegerList(segment.getPath());
               return (T)list.get(list.size() + segment.getIndex());
            }
         } else {
            if (comp.getType(segment.getPath()) == NBTType.NBTTagIntArray) {
               if (segment.getIndex() >= 0) {
                  int[] array = comp.getIntArray(segment.getPath());
                  if (array != null) {
                     return (T)array[segment.getIndex()];
                  }
               } else {
                  int[] array = comp.getIntArray(segment.getPath());
                  if (array != null) {
                     return (T)array[array.length + segment.getIndex()];
                  }
               }
            }

            throw new NbtApiException("No fitting list/array found for " + segment.getPath() + " of type " + type);
         }
      } else if (type == long.class || type == Long.class) {
         if (comp.getType(segment.getPath()) == NBTType.NBTTagList && comp.getListType(segment.getPath()) == NBTType.NBTTagLong) {
            if (segment.getIndex() >= 0) {
               return (T)comp.getLongList(segment.getPath()).get(segment.getIndex());
            } else {
               List<Long> list = comp.getLongList(segment.getPath());
               return (T)list.get(list.size() + segment.getIndex());
            }
         } else {
            if (comp.getType(segment.getPath()) == NBTType.NBTTagLongArray) {
               if (segment.getIndex() >= 0) {
                  long[] array = comp.getLongArray(segment.getPath());
                  if (array != null) {
                     return (T)array[segment.getIndex()];
                  }
               } else {
                  long[] array = comp.getLongArray(segment.getPath());
                  if (array != null) {
                     return (T)array[array.length + segment.getIndex()];
                  }
               }
            }

            throw new NbtApiException("No fitting list/array found for " + segment.getPath() + " of type " + type);
         }
      } else if (type == float.class || type == Float.class) {
         if (comp.getType(segment.getPath()) != NBTType.NBTTagList || comp.getListType(segment.getPath()) != NBTType.NBTTagFloat) {
            throw new NbtApiException("No fitting list/array found for " + segment.getPath() + " of type " + type);
         } else if (segment.getIndex() >= 0) {
            return (T)comp.getFloatList(segment.getPath()).get(segment.getIndex());
         } else {
            List<Float> list = comp.getFloatList(segment.getPath());
            return (T)list.get(list.size() + segment.getIndex());
         }
      } else if (type == double.class || type == Double.class) {
         if (comp.getType(segment.getPath()) != NBTType.NBTTagList || comp.getListType(segment.getPath()) != NBTType.NBTTagDouble) {
            throw new NbtApiException("No fitting list/array found for " + segment.getPath() + " of type " + type);
         } else if (segment.getIndex() >= 0) {
            return (T)comp.getDoubleList(segment.getPath()).get(segment.getIndex());
         } else {
            List<Double> list = comp.getDoubleList(segment.getPath());
            return (T)list.get(list.size() + segment.getIndex());
         }
      } else if (type == int[].class) {
         if (comp.getType(segment.getPath()) != NBTType.NBTTagList || comp.getListType(segment.getPath()) != NBTType.NBTTagIntArray) {
            throw new NbtApiException("No fitting list/array found for " + segment.getPath() + " of type " + type);
         } else if (segment.getIndex() >= 0) {
            return (T)comp.getIntArrayList(segment.getPath()).get(segment.getIndex());
         } else {
            List<int[]> list = comp.getIntArrayList(segment.getPath());
            return (T)list.get(list.size() + segment.getIndex());
         }
      } else if (type != byte.class && type != Byte.class) {
         throw new NbtApiException("Unable to get indexed value for type " + type);
      } else {
         if (comp.getType(segment.getPath()) == NBTType.NBTTagByteArray) {
            if (segment.getIndex() >= 0) {
               byte[] array = comp.getByteArray(segment.getPath());
               if (array != null) {
                  return (T)array[segment.getIndex()];
               }
            } else {
               byte[] array = comp.getByteArray(segment.getPath());
               if (array != null) {
                  return (T)array[array.length + segment.getIndex()];
               }
            }
         }

         throw new NbtApiException("No fitting list/array found for " + segment.getPath() + " of type " + type);
      }
   }

   @Override
   public ReadWriteNBT resolveCompound(String key) {
      List<PathUtil.PathSegment> keys = PathUtil.splitPath(key);
      NBTCompound tag = this;

      for (int i = 0; i < keys.size(); i++) {
         PathUtil.PathSegment segment = keys.get(i);
         if (!segment.hasIndex()) {
            tag = tag.getCompound(segment.getPath());
            if (tag == null) {
               return null;
            }
         } else if (tag.getType(segment.getPath()) == NBTType.NBTTagList && tag.getListType(segment.getPath()) == NBTType.NBTTagCompound) {
            NBTCompoundList list = tag.getCompoundList(segment.getPath());
            if (segment.getIndex() >= 0) {
               tag = list.get(segment.getIndex());
            } else {
               tag = list.get(list.size() + segment.getIndex());
            }
         }
      }

      return tag;
   }

   @Override
   public ReadWriteNBT resolveOrCreateCompound(String key) {
      List<PathUtil.PathSegment> keys = PathUtil.splitPath(key);
      NBTCompound tag = this;

      for (int i = 0; i < keys.size(); i++) {
         PathUtil.PathSegment segment = keys.get(i);
         if (!segment.hasIndex()) {
            tag = tag.getOrCreateCompound(segment.getPath());
            if (tag == null) {
               return null;
            }
         } else if (tag.getType(segment.getPath()) == NBTType.NBTTagList && tag.getListType(segment.getPath()) == NBTType.NBTTagCompound) {
            NBTCompoundList list = tag.getCompoundList(segment.getPath());
            if (segment.getIndex() >= 0) {
               tag = list.get(segment.getIndex());
            } else {
               tag = list.get(list.size() + segment.getIndex());
            }
         }
      }

      return tag;
   }

   @Override
   public <E extends Enum<?>> void setEnum(String key, E value) {
      if (value == null) {
         this.removeKey(key);
      } else {
         this.setString(key, value.name());
      }
   }

   @Override
   public <E extends Enum<E>> E getEnum(String key, Class<E> type) {
      if (key != null && type != null) {
         String name = this.getString(key);
         if (name == null) {
            return null;
         } else {
            try {
               return Enum.valueOf(type, name);
            } catch (IllegalArgumentException var5) {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   @Override
   public NBTType getType(String name) {
      Object var3;
      try {
         this.readLock.lock();
         if (MinecraftVersion.getVersion() == MinecraftVersion.MC1_7_R4) {
            Object nbtbase = NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET, name);
            if (nbtbase != null) {
               return NBTType.valueOf((Byte)ReflectionMethod.COMPOUND_OWN_TYPE_LEGACY.run(nbtbase));
            }

            return null;
         }

         if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R4)) {
            Object o = NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_TYPE, name);
            if (o != null) {
               return NBTType.valueOf((Byte)o);
            }

            return null;
         }

         Object o = NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET, name);
         if (o != null) {
            return NBTType.fromName((String)ReflectionMethod.TAGTYPE_GET_NAME.run(ReflectionMethod.TAGTYPE_OWN_TYPE.run(o)));
         }

         var3 = null;
      } finally {
         this.readLock.unlock();
      }

      return (NBTType)var3;
   }

   @Override
   public void writeCompound(OutputStream stream) {
      try {
         this.writeLock.lock();
         NBTReflectionUtil.writeApiNBT(this, stream);
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public <T> T get(String key, NBTHandler<T> handler) {
      return handler.get(this, key);
   }

   @Override
   public <T> void set(String key, T value, NBTHandler<T> handler) {
      handler.set(this, key, value);
   }

   @Override
   public String toString() {
      return this.asNBTString();
   }

   @Deprecated
   public String toString(String key) {
      return this.asNBTString();
   }

   @Override
   public void clearNBT() {
      for (String key : this.getKeys()) {
         this.removeKey(key);
      }
   }

   @Deprecated
   public String asNBTString() {
      String var2;
      try {
         this.readLock.lock();
         Object comp = this.getResolvedObject();
         if (comp == null) {
            return "{}";
         }

         if (!MinecraftVersion.isForgePresent() || MinecraftVersion.getVersion() != MinecraftVersion.MC1_7_R4) {
            return comp.toString();
         }

         var2 = Forge1710Mappings.toString(comp);
      } finally {
         this.readLock.unlock();
      }

      return var2;
   }

   @Override
   public int hashCode() {
      return this.toString().hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         if (obj instanceof NBTCompound) {
            NBTCompound other = (NBTCompound)obj;
            if (this.getKeys().equals(other.getKeys())) {
               for (String key : this.getKeys()) {
                  if (!isEqual(this, other, key)) {
                     return false;
                  }
               }

               return true;
            }
         }

         return false;
      }
   }

   public NBTCompound extractDifference(ReadableNBT other) {
      if (this == other) {
         return new NBTContainer();
      } else if (other instanceof NBTCompound) {
         return saveDiff(new NBTContainer(), this, (NBTCompound)other);
      } else {
         throw new NbtApiException("Unknown NBT object: " + other);
      }
   }

   private static NBTCompound saveDiff(NBTCompound saveTo, NBTCompound compA, NBTCompound compB) {
      for (String key : compA.getKeys()) {
         saveDiff(saveTo, compA, compB, key);
      }

      return saveTo;
   }

   private static void saveDiff(NBTCompound saveTo, NBTCompound compA, NBTCompound compB, String key) {
      boolean typeMismatch = compA.getType(key) != compB.getType(key);
      switch (compA.getType(key)) {
         case NBTTagByte:
            if (typeMismatch || !isEqual(compA, compB, key)) {
               saveTo.setByte(key, compA.getByte(key));
            }

            return;
         case NBTTagByteArray:
            if (typeMismatch || !isEqual(compA, compB, key)) {
               saveTo.setByteArray(key, compA.getByteArray(key));
            }

            return;
         case NBTTagCompound:
            NBTCompound tmp1 = compA.getCompound(key);
            if (tmp1 == null) {
               return;
            } else {
               if (typeMismatch) {
                  saveTo.addCompound(key).mergeCompound(tmp1);
               } else {
                  NBTCompound tmp2 = compB.getCompound(key);
                  if (tmp2 == null) {
                     saveTo.addCompound(key).mergeCompound(tmp1);
                     return;
                  }

                  NBTCompound tmpDiff = tmp1.extractDifference(tmp2);
                  if (!tmpDiff.getKeys().isEmpty()) {
                     saveTo.addCompound(key).mergeCompound(tmpDiff);
                  }
               }

               return;
            }
         case NBTTagDouble:
            if (typeMismatch || !isEqual(compA, compB, key)) {
               saveTo.setDouble(key, compA.getDouble(key));
            }

            return;
         case NBTTagEnd:
            return;
         case NBTTagFloat:
            if (typeMismatch || !isEqual(compA, compB, key)) {
               saveTo.setFloat(key, compA.getFloat(key));
            }

            return;
         case NBTTagInt:
            if (typeMismatch || !isEqual(compA, compB, key)) {
               saveTo.setInteger(key, compA.getInteger(key));
            }

            return;
         case NBTTagIntArray:
            if (typeMismatch || !isEqual(compA, compB, key)) {
               saveTo.setIntArray(key, compA.getIntArray(key));
            }

            return;
         case NBTTagList:
            if (typeMismatch || !isEqual(compA, compB, key)) {
               saveTo.set(key, NBTReflectionUtil.getEntry(compA, key));
            }

            return;
         case NBTTagLong:
            if (typeMismatch || !isEqual(compA, compB, key)) {
               saveTo.setLong(key, compA.getLong(key));
            }

            return;
         case NBTTagShort:
            if (typeMismatch || !isEqual(compA, compB, key)) {
               saveTo.setShort(key, compA.getShort(key));
            }

            return;
         case NBTTagString:
            if (typeMismatch || !isEqual(compA, compB, key)) {
               saveTo.setString(key, compA.getString(key));
            }

            return;
         case NBTTagLongArray:
            if (typeMismatch || !isEqual(compA, compB, key)) {
               saveTo.setLongArray(key, compA.getLongArray(key));
            }

            return;
      }
   }

   private static boolean isEqual(NBTCompound compA, NBTCompound compB, String key) {
      if (compA.getType(key) != compB.getType(key)) {
         return false;
      } else {
         switch (compA.getType(key)) {
            case NBTTagByte:
               return compA.getByte(key).equals(compB.getByte(key));
            case NBTTagByteArray:
               return Arrays.equals(compA.getByteArray(key), compB.getByteArray(key));
            case NBTTagCompound:
               NBTCompound tmp = compA.getCompound(key);
               return tmp != null && tmp.equals(compB.getCompound(key));
            case NBTTagDouble:
               return compA.getDouble(key).equals(compB.getDouble(key));
            case NBTTagEnd:
               return true;
            case NBTTagFloat:
               return compA.getFloat(key).equals(compB.getFloat(key));
            case NBTTagInt:
               return compA.getInteger(key).equals(compB.getInteger(key));
            case NBTTagIntArray:
               return Arrays.equals(compA.getIntArray(key), compB.getIntArray(key));
            case NBTTagList:
               return NBTReflectionUtil.getEntry(compA, key).toString().equals(NBTReflectionUtil.getEntry(compB, key).toString());
            case NBTTagLong:
               return compA.getLong(key).equals(compB.getLong(key));
            case NBTTagShort:
               return compA.getShort(key).equals(compB.getShort(key));
            case NBTTagString:
               return compA.getString(key).equals(compB.getString(key));
            case NBTTagLongArray:
               return Arrays.equals(compA.getLongArray(key), compB.getLongArray(key));
            default:
               return false;
         }
      }
   }
}
