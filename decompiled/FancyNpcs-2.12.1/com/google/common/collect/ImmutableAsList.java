package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible(
   serializable = true,
   emulated = true
)
abstract class ImmutableAsList<E> extends ImmutableList<E> {
   abstract ImmutableCollection<E> delegateCollection();

   @Override
   public boolean contains(@CheckForNull Object target) {
      return this.delegateCollection().contains(target);
   }

   @Override
   public int size() {
      return this.delegateCollection().size();
   }

   @Override
   public boolean isEmpty() {
      return this.delegateCollection().isEmpty();
   }

   @Override
   boolean isPartialView() {
      return this.delegateCollection().isPartialView();
   }

   @GwtIncompatible
   @J2ktIncompatible
   private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("Use SerializedForm");
   }

   @GwtIncompatible
   @J2ktIncompatible
   @Override
   Object writeReplace() {
      return new ImmutableAsList.SerializedForm(this.delegateCollection());
   }

   @GwtIncompatible
   @J2ktIncompatible
   static class SerializedForm implements Serializable {
      final ImmutableCollection<?> collection;
      private static final long serialVersionUID = 0L;

      SerializedForm(ImmutableCollection<?> collection) {
         this.collection = collection;
      }

      Object readResolve() {
         return this.collection.asList();
      }
   }
}
