package org.enginehub.piston;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nullable;
import org.enginehub.piston.inject.Key;

public interface CommandValue {
   ImmutableList<String> asStrings();

   default String asString() {
      ImmutableList<String> values = this.asStrings();
      Preconditions.checkState(values.size() > 0, "No value present");
      Preconditions.checkState(values.size() == 1, "Too many values present");
      return (String)values.get(0);
   }

   default <T> ImmutableList<T> asMultiple(Class<T> key) {
      return this.asMultiple(Key.of(key));
   }

   <T> ImmutableList<T> asMultiple(Key<T> var1);

   @Nullable
   default <T> T asSingle(Class<T> key) {
      return this.asSingle(Key.of(key));
   }

   @Nullable
   default <T> T asSingle(Key<T> key) {
      ImmutableList<T> values = this.asMultiple(key);
      if (values.size() == 0 && this.asStrings().size() > 0) {
         return null;
      } else {
         Preconditions.checkState(values.size() > 0, "No value present");
         Preconditions.checkState(values.size() == 1, "Too many values present");
         return (T)values.get(0);
      }
   }
}
