package com.sk89q.jnbt;

import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.tree.LinByteArrayTag;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinNumberTag;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

@Deprecated
public final class ListTag<EV, E extends LinTag<EV>> extends Tag<Object, LinListTag<E>> {
   public ListTag(Class<? extends Tag<EV, E>> type, List<? extends Tag<EV, E>> value) {
      this(LinListTag.of(LinTagType.fromId(LinTagId.fromId(NBTUtils.getTypeCode(type))), value.stream().map(Tag::toLinTag).collect(Collectors.toList())));
   }

   public ListTag(LinListTag<E> tag) {
      super(tag);
   }

   public Class<? extends Tag<EV, E>> getType() {
      return (Class<? extends Tag<EV, E>>)NBTUtils.getTypeClass(this.linTag.elementType().id().id());
   }

   public List<? extends Tag<EV, E>> getValue() {
      return this.linTag.value().stream().map(LinBusConverter::toJnbtTag).toList();
   }

   public ListTag<EV, E> setValue(List<? extends Tag<EV, E>> list) {
      return new ListTag<>(this.getType(), list);
   }

   private <T> T accessIfExists(int index, Supplier<T> defaultValue, IntFunction<T> accessor) {
      return index >= this.linTag.value().size() ? defaultValue.get() : accessor.apply(index);
   }

   private <T, LT extends LinTag<T>> T extractViaValue(int index, Class<LT> requiredType, Supplier<T> defaultValue) {
      if (index >= this.linTag.value().size()) {
         return defaultValue.get();
      } else {
         E value = this.linTag.get(index);
         return (T)(!requiredType.isInstance(value) ? defaultValue.get() : value.value());
      }
   }

   @Nullable
   public Tag<EV, E> getIfExists(int index) {
      return this.accessIfExists(index, () -> null, i -> LinBusConverter.toJnbtTag(this.linTag.get(i)));
   }

   public byte[] getByteArray(int index) {
      return this.extractViaValue(index, LinByteArrayTag.class, () -> new byte[0]);
   }

   public byte getByte(int index) {
      return this.extractViaValue(index, LinByteTag.class, () -> (byte)0);
   }

   public double getDouble(int index) {
      return this.extractViaValue(index, LinDoubleTag.class, () -> 0.0);
   }

   public double asDouble(int index) {
      return this.accessIfExists(index, () -> 0.0, i -> this.linTag.get(i) instanceof LinNumberTag<?> tag ? tag.value().doubleValue() : 0.0);
   }

   public float getFloat(int index) {
      return this.extractViaValue(index, LinFloatTag.class, () -> 0.0F);
   }

   public int[] getIntArray(int index) {
      return this.extractViaValue(index, LinIntArrayTag.class, () -> new int[0]);
   }

   public int getInt(int index) {
      return this.extractViaValue(index, LinIntTag.class, () -> 0);
   }

   public int asInt(int index) {
      return this.accessIfExists(index, () -> 0, i -> this.linTag.get(i) instanceof LinNumberTag<?> tag ? tag.value().intValue() : 0);
   }

   public List<? extends Tag<?, ?>> getList(int index) {
      return this.getListTag(index).getValue();
   }

   public ListTag<?, ?> getListTag(int index) {
      return new ListTag(this.extractViaValue(index, LinListTag.class, () -> LinListTag.empty((LinTagType<E>)LinTagType.endTag())));
   }

   public <T extends Tag<?, ?>> List<T> getList(int index, Class<T> listType) {
      ListTag<?, ?> listTag = this.getListTag(index);
      return (List<T>)(listTag.getType().equals(listType) ? listTag.getValue() : Collections.emptyList());
   }

   public long getLong(int index) {
      return this.extractViaValue(index, LinLongTag.class, () -> 0L);
   }

   public long asLong(int index) {
      return this.accessIfExists(index, () -> 0L, i -> this.linTag.get(i) instanceof LinNumberTag<?> tag ? tag.value().longValue() : 0L);
   }

   public short getShort(int index) {
      return this.extractViaValue(index, LinShortTag.class, () -> (short)0);
   }

   public String getString(int index) {
      return this.extractViaValue(index, LinStringTag.class, () -> "");
   }

   @Override
   public int getTypeCode() {
      return 9;
   }
}
