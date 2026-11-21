package org.jdbi.v3.core.internal;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jdbi.v3.core.generic.GenericTypes;

public class IterableLike {
   private IterableLike() {
      throw new UtilityClassException();
   }

   public static boolean isIterable(Object maybeIterable) {
      return maybeIterable instanceof Iterator || maybeIterable instanceof Iterable || maybeIterable != null && maybeIterable.getClass().isArray();
   }

   public static Iterator<Object> of(Object iterable) {
      if (iterable == null) {
         throw new IllegalArgumentException("can't iterate null");
      } else if (iterable instanceof Iterator) {
         return (Iterator<Object>)iterable;
      } else if (iterable instanceof Iterable) {
         return ((Iterable)iterable).iterator();
      } else if (iterable instanceof Stream) {
         return ((Stream)iterable).iterator();
      } else {
         Class<?> klass = iterable.getClass();
         if (!klass.isArray()) {
            throw new IllegalArgumentException(getTypeWarning(klass));
         } else {
            return (Iterator<Object>)(klass.getComponentType().isPrimitive()
               ? new IterableLike.PrimitiveArrayIterator(iterable)
               : Arrays.<Object>asList((Object[])iterable).iterator());
         }
      }
   }

   public static Optional<Type> elementTypeOf(Object iterable) {
      return elementTypeOf((Type)iterable.getClass());
   }

   public static Optional<Type> elementTypeOf(Type type) {
      Class<?> rawClass = GenericTypes.getErasedType(type);
      if (rawClass.isArray()) {
         return Optional.of(rawClass.getComponentType());
      } else if (Iterable.class.isAssignableFrom(rawClass)) {
         return GenericTypes.findGenericParameter(type, Iterable.class);
      } else {
         return Iterator.class.isAssignableFrom(rawClass) ? GenericTypes.findGenericParameter(type, Iterator.class) : Optional.empty();
      }
   }

   public static Stream<Object> stream(Object iterable) {
      return StreamSupport.stream(Spliterators.spliteratorUnknownSize(of(iterable), 16), false);
   }

   public static Iterable<Object> iterable(Object iterable) {
      return () -> of(iterable);
   }

   public static List<Object> toList(Object iterable) {
      List<Object> result = new ArrayList<>();
      of(iterable).forEachRemaining(result::add);
      return result;
   }

   private static String getTypeWarning(final Class<?> type) {
      return "argument must be one of the following: Stream, Iterable, Iterator, or an array/varargs (primitive or complex type); was "
         + type.getName()
         + " instead";
   }

   static class PrimitiveArrayIterator implements Iterator<Object> {
      private int index = 0;
      private final int size;
      private final Object arr;

      PrimitiveArrayIterator(final Object obj) {
         this.size = Array.getLength(obj);
         this.arr = obj;
      }

      @Override
      public boolean hasNext() {
         return this.index < this.size;
      }

      @Override
      public Object next() {
         if (this.hasNext()) {
            return Array.get(this.arr, this.index++);
         } else {
            throw new NoSuchElementException("only " + this.size + " elements available");
         }
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }
}
