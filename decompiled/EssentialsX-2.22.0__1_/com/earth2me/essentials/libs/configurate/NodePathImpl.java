package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

final class NodePathImpl implements NodePath {
   static final NodePath EMPTY = new NodePathImpl(new Object[0], false);
   private final Object[] arr;

   NodePathImpl(final Object[] arr, final boolean copy) {
      Objects.requireNonNull(arr);
      this.arr = copy ? Arrays.copyOf(arr, arr.length) : arr;
   }

   @Override
   public Object get(final int i) {
      return this.arr[i];
   }

   @Override
   public int size() {
      return this.arr.length;
   }

   @Override
   public NodePath withAppendedChild(final Object childKey) {
      Objects.requireNonNull(childKey, "childKey");
      Object[] arr = this.arr;
      if (arr.length == 0) {
         return new NodePathImpl(new Object[]{childKey}, false);
      } else {
         Object[] childPath = Arrays.copyOf(arr, arr.length + 1);
         childPath[childPath.length - 1] = childKey;
         return new NodePathImpl(childPath, false);
      }
   }

   @Override
   public NodePath with(final int index, final Object value) throws IndexOutOfBoundsException {
      Objects.requireNonNull(value, "value");
      Object[] arr = this.arr;
      if (index >= 0 && index < arr.length) {
         Object[] newPath = Arrays.copyOf(arr, arr.length);
         newPath[index] = value;
         return new NodePathImpl(newPath, false);
      } else {
         throw new IndexOutOfBoundsException("Index " + index + " is not within limit of [0," + arr.length + ")");
      }
   }

   @Override
   public NodePath plus(final NodePath other) {
      Objects.requireNonNull(other, "other");
      if (this.arr.length == 0) {
         return other;
      } else if (other.size() == 0) {
         return this;
      } else {
         Object[] otherArr = other instanceof NodePathImpl ? ((NodePathImpl)other).arr : other.array();
         Object[] result = new Object[this.arr.length + otherArr.length];
         System.arraycopy(this.arr, 0, result, 0, this.arr.length);
         System.arraycopy(otherArr, 0, result, this.arr.length, otherArr.length);
         return new NodePathImpl(result, false);
      }
   }

   @Override
   public Object[] array() {
      return Arrays.copyOf(this.arr, this.arr.length);
   }

   @Override
   public Iterator<Object> iterator() {
      return Arrays.asList(this.arr).iterator();
   }

   @Override
   public NodePath copy() {
      return new NodePathImpl(this.arr, true);
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (other != null && this.getClass() == other.getClass()) {
         NodePathImpl that = (NodePathImpl)other;
         return Arrays.equals(this.arr, that.arr);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(this.arr);
   }

   @Override
   public String toString() {
      return Arrays.toString(this.arr);
   }
}
