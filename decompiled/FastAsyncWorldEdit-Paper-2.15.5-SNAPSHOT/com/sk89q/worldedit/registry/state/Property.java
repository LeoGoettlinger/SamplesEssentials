package com.sk89q.worldedit.registry.state;

import com.fastasyncworldedit.core.limit.PropertyRemap;
import com.fastasyncworldedit.core.registry.state.PropertyKey;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public interface Property<T> {
   String getName();

   List<T> getValues();

   @Nullable
   T getValueFor(String string) throws IllegalArgumentException;

   default int getIndex(T value) {
      return this.getValues().indexOf(value);
   }

   default int getIndexFor(CharSequence value) throws IllegalArgumentException {
      return this.getIndex(this.getValueFor(value.toString()));
   }

   default PropertyKey getKey() {
      return PropertyKey.getOrCreate(this.getName());
   }

   default PropertyRemap<T> getRemap(Object from, Object to) {
      Objects.requireNonNull(from);
      Objects.requireNonNull(to);
      return new PropertyRemap<>(this, (T)from, (T)to);
   }
}
