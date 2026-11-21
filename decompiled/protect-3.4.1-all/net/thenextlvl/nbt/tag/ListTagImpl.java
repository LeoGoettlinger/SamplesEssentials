package net.thenextlvl.nbt.tag;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.thenextlvl.nbt.NBTOutputStream;
import org.jspecify.annotations.Nullable;

final class ListTagImpl<T extends Tag> extends ValueTagImpl<List<T>> implements ListTag<T> {
   private final byte contentTypeId;

   private ListTagImpl(List<T> value, byte contentTypeId) {
      super(Collections.unmodifiableList(new LinkedList<>(value)));
      this.contentTypeId = contentTypeId;
   }

   public ListTagImpl(byte contentTypeId) {
      super(Collections.emptyList());
      this.contentTypeId = contentTypeId;
   }

   @Override
   public byte getContentTypeId() {
      return this.contentTypeId;
   }

   @Override
   public boolean isList() {
      return true;
   }

   public ListTagImpl<T> getAsList() {
      return this;
   }

   @Override
   public byte getTypeId() {
      return 9;
   }

   @Override
   public int size() {
      return this.value.size();
   }

   public T get(int index) {
      return this.value.get(index);
   }

   @Override
   public T set(int i, T t) {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Override
   public void add(int i, T t) {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Override
   public T remove(int i) {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Override
   public int indexOf(Object o) {
      return this.value.indexOf(o);
   }

   @Override
   public int lastIndexOf(Object o) {
      return this.value.lastIndexOf(o);
   }

   @Override
   public ListIterator<T> listIterator() {
      return this.value.listIterator();
   }

   @Override
   public ListIterator<T> listIterator(int i) {
      return this.value.listIterator(i);
   }

   @Override
   public List<T> subList(int i, int i1) {
      return this.value.subList(i, i1);
   }

   @Override
   public boolean isEmpty() {
      return this.value.isEmpty();
   }

   @Override
   public boolean contains(Object o) {
      return this.value.contains(o);
   }

   @Override
   public Iterator<T> iterator() {
      return this.value.iterator();
   }

   @Override
   public Object[] toArray() {
      return this.value.toArray();
   }

   @Override
   public <V> V[] toArray(V[] vs) {
      return this.value.toArray(vs);
   }

   @Override
   public boolean add(T t) {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Override
   public boolean remove(Object o) {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Override
   public boolean containsAll(Collection<?> collection) {
      return new HashSet<>(this.value).containsAll(collection);
   }

   @Override
   public boolean addAll(Collection<? extends T> collection) {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Override
   public boolean addAll(int i, Collection<? extends T> collection) {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Override
   public boolean removeAll(Collection<?> collection) {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Override
   public boolean retainAll(Collection<?> collection) {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Override
   public void clear() {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (o == null || this.getClass() != o.getClass()) {
         return false;
      } else if (!super.equals(o)) {
         return false;
      } else {
         ListTagImpl<?> listTag = (ListTagImpl<?>)o;
         return this.contentTypeId == listTag.contentTypeId;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.contentTypeId);
   }

   @Override
   public void write(NBTOutputStream outputStream) throws IOException {
      outputStream.writeByte(this.contentTypeId);
      outputStream.writeInt(this.value.size());

      for (T tag : this.value) {
         tag.write(outputStream);
      }
   }

   @Override
   public String toString() {
      if (this.value.isEmpty()) {
         return "[]";
      } else {
         StringBuilder builder = new StringBuilder("[");

         for (int i = 0; i < this.value.size(); i++) {
            if (i > 0) {
               builder.append(",");
            }

            builder.append(this.value.get(i));
         }

         return builder.append(']').toString();
      }
   }

   public static final class Builder<T extends Tag> implements ListTag.Builder<T> {
      private final List<T> value = new LinkedList<>();
      @Nullable
      private Byte contentTypeId;

      @Override
      public ListTag.Builder<T> contentType(byte contentTypeId) throws IllegalStateException {
         if (this.contentTypeId != null && !this.value.isEmpty()) {
            throw new IllegalStateException("Content type cannot be changed after adding values");
         } else {
            this.contentTypeId = contentTypeId;
            return this;
         }
      }

      @Override
      public ListTag.Builder<T> add(T tag) throws IllegalArgumentException {
         return this.addLast(tag);
      }

      @Override
      public ListTag.Builder<T> add(int index, T tag) throws IllegalArgumentException {
         this.checkOrReplaceContentType(tag);
         this.value.add(index, tag);
         return this;
      }

      @Override
      public ListTag.Builder<T> addFirst(T tag) throws IllegalArgumentException {
         return this.add(0, tag);
      }

      @Override
      public ListTag.Builder<T> addLast(T tag) throws IllegalArgumentException {
         return this.add(this.value.size(), tag);
      }

      @Override
      public ListTag.Builder<T> addAll(Iterable<? extends T> iterable) throws IllegalArgumentException {
         iterable.forEach(this::add);
         return this;
      }

      @Override
      public ListTag.Builder<T> replaceAll(UnaryOperator<T> operator) throws IllegalArgumentException {
         ListIterator<T> li = this.value.listIterator();

         while (li.hasNext()) {
            this.set(li.nextIndex(), operator.apply(li.next()));
         }

         return this;
      }

      @Override
      public ListTag.Builder<T> set(int index, T tag) throws IllegalArgumentException {
         this.checkOrReplaceContentType(tag);
         this.value.set(index, tag);
         return this;
      }

      @Override
      public ListTag.Builder<T> remove(T tag) {
         this.value.remove(tag);
         return this;
      }

      @Override
      public ListTag.Builder<T> removeIf(Predicate<? super T> filter) {
         this.value.removeIf(filter);
         return this;
      }

      @Override
      public ListTag.Builder<T> removeAll(Iterable<? extends T> iterable) throws IllegalArgumentException {
         iterable.forEach(this.value::remove);
         return this;
      }

      @Override
      public ListTag.Builder<T> clear() {
         this.value.clear();
         return this;
      }

      @Override
      public boolean isEmpty() {
         return this.value.isEmpty();
      }

      @Override
      public int size() {
         return this.value.size();
      }

      @Override
      public ListTag<T> build() {
         if (this.contentTypeId == null) {
            throw new IllegalStateException("Empty ListTag must have a content type");
         } else {
            return new ListTagImpl<>(this.value, this.contentTypeId);
         }
      }

      private void checkOrReplaceContentType(T tag) {
         if (this.contentTypeId == null) {
            this.contentTypeId = tag.getTypeId();
         } else if (this.contentTypeId != tag.getTypeId()) {
            throw new IllegalArgumentException("ListTag content type mismatch");
         }
      }
   }
}
