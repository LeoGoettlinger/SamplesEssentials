package com.earth2me.essentials.libs.configurate.transformation;

import com.earth2me.essentials.libs.configurate.NodePath;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

final class MutableNodePath implements NodePath {
   @LazyInit
   Object[] arr;

   @Override
   public Object get(final int i) {
      return this.arr[i];
   }

   @Override
   public int size() {
      return this.arr.length;
   }

   @Override
   public Object[] array() {
      return Arrays.copyOf(this.arr, this.arr.length);
   }

   @Override
   public NodePath withAppendedChild(final Object childKey) {
      return this.copy().withAppendedChild(childKey);
   }

   @Override
   public NodePath with(final int index, final Object value) throws IndexOutOfBoundsException {
      return this.copy().with(index, value);
   }

   @Override
   public NodePath plus(final NodePath other) {
      Objects.requireNonNull(other, "other");
      Object[] otherArr = other.array();
      Object[] target = Arrays.copyOf(this.arr, this.arr.length + otherArr.length);
      System.arraycopy(otherArr, 0, target, this.arr.length, otherArr.length);
      return NodePath.of(target);
   }

   @Override
   public Iterator<Object> iterator() {
      return Arrays.asList(this.arr).iterator();
   }

   @Override
   public NodePath copy() {
      return NodePath.path(this.arr);
   }
}
