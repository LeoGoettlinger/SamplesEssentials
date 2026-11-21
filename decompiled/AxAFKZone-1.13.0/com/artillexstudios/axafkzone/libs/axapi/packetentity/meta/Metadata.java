package com.artillexstudios.axafkzone.libs.axapi.packetentity.meta;

import com.artillexstudios.axafkzone.libs.axapi.packetentity.meta.serializer.Accessors;
import com.artillexstudios.axafkzone.libs.axapi.packetentity.meta.serializer.EntityDataAccessor;
import com.artillexstudios.axafkzone.libs.axapi.packetentity.meta.serializer.EntityDataSerializer;
import com.artillexstudios.axafkzone.libs.axapi.packetentity.meta.serializer.EntityDataSerializers;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;

public final class Metadata {
   private final Int2ObjectMap<Metadata.DataItem<?>> items = new Int2ObjectOpenHashMap();
   private volatile boolean dirty = false;

   public <T> void define(EntityDataAccessor<T> accessor, T value) {
      this.createDataItem(accessor, value);
   }

   public <T> void createDataItem(EntityDataAccessor<T> accessor, T value) {
      Metadata.DataItem<T> dataItem = new Metadata.DataItem<>(accessor.id(), accessor.serializers(), value);
      this.items.put(accessor.id(), dataItem);
   }

   public void markNotDirty() {
      this.dirty = false;
      this.items.forEach((k, v) -> v.dirty = false);
   }

   private <T> Metadata.DataItem<T> getItem(EntityDataAccessor<T> key) {
      return (Metadata.DataItem<T>)this.items.get(key.id());
   }

   public <T> T get(EntityDataAccessor<T> data) {
      return this.getItem(data).getValue();
   }

   public <T> void set(EntityDataAccessor<T> key, T value) {
      this.set(key, value, false);
   }

   public <T> void setToDefault(EntityDataAccessor<T> key) {
      this.getItem(key).setToDefault();
   }

   public <T> void set(EntityDataAccessor<T> key, T value, boolean force) {
      Metadata.DataItem<T> datawatcher_item = this.getItem(key);
      if (force || ObjectUtils.notEqual(value, datawatcher_item.getValue())) {
         datawatcher_item.setValue(value);
         datawatcher_item.setDirty(true);
         this.dirty = true;
      }
   }

   public List<Metadata.DataItem<?>> getNonDefaultValues() {
      List<Metadata.DataItem<?>> list = null;
      ObjectIterator var2 = this.items.values().iterator();

      while (var2.hasNext()) {
         Metadata.DataItem<?> next = (Metadata.DataItem<?>)var2.next();
         if (!next.isSetToDefault()) {
            if (list == null) {
               list = new ArrayList<>();
            }

            list.add(next.copy());
         }
      }

      return list;
   }

   public List<Metadata.DataItem<?>> packForNameUpdate() {
      List<Metadata.DataItem<?>> list = null;
      ObjectIterator var2 = this.items.values().iterator();

      while (var2.hasNext()) {
         Metadata.DataItem<?> next = (Metadata.DataItem<?>)var2.next();
         if (next.serializer() == EntityDataSerializers.COMPONENT && next.id() == Accessors.TEXT_COMPONENT.id()) {
            list = new ArrayList<>(1);
            list.add(next.copy());
            break;
         }
      }

      return list;
   }

   public boolean isDirty() {
      return this.dirty;
   }

   public boolean isEmpty() {
      return this.items.isEmpty();
   }

   public List<Metadata.DataItem<?>> getAll() {
      List<Metadata.DataItem<?>> list = null;
      ObjectIterator var2 = this.items.values().iterator();

      while (var2.hasNext()) {
         Metadata.DataItem<?> next = (Metadata.DataItem<?>)var2.next();
         if (list == null) {
            list = new ArrayList<>();
         }

         list.add(next.copy());
      }

      return list;
   }

   public List<Metadata.DataItem<?>> packDirty() {
      if (!this.dirty) {
         return null;
      } else {
         List<Metadata.DataItem<?>> list = null;
         ObjectIterator var2 = this.items.values().iterator();

         while (var2.hasNext()) {
            Metadata.DataItem<?> next = (Metadata.DataItem<?>)var2.next();
            if (next.isDirty()) {
               next.setDirty(false);
               if (list == null) {
                  list = new ArrayList<>();
               }

               list.add(next.copy());
            }
         }

         this.dirty = false;
         return list;
      }
   }

   public void reset() {
      this.items.forEach((key, item) -> {
         item.setToDefault();
         item.setDirty(true);
      });
   }

   public void resetSilently() {
      this.items.forEach((key, item) -> item.setToDefault());
   }

   public static class DataItem<T> {
      final int id;
      final EntityDataSerializer<T> serializer;
      final T original;
      volatile T value;
      private volatile boolean dirty;

      public DataItem(int id, EntityDataSerializer<T> serializer, T value) {
         this.id = id;
         this.serializer = serializer;
         this.value = value;
         this.original = value;
         this.dirty = true;
      }

      public EntityDataSerializer<T> serializer() {
         return this.serializer;
      }

      public int id() {
         return this.id;
      }

      public T getValue() {
         return this.value;
      }

      public void setValue(T value) {
         this.value = value;
      }

      public boolean isDirty() {
         return this.dirty;
      }

      public void setDirty(boolean dirty) {
         this.dirty = dirty;
      }

      public boolean isSetToDefault() {
         return this.original.equals(this.value);
      }

      public void setToDefault() {
         this.setValue(this.original);
      }

      public Metadata.DataItem<T> copy() {
         return new Metadata.DataItem<>(this.id, this.serializer, this.value);
      }

      @Override
      public String toString() {
         return "DataItem{id="
            + this.id
            + ", serializer="
            + this.serializer
            + ", original="
            + this.original
            + ", value="
            + this.value
            + ", dirty="
            + this.dirty
            + "}";
      }
   }
}
