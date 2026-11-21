package org.skriptlang.skript.util;

import java.util.Collection;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public interface Registry<T> extends Iterable<T> {
   Collection<T> elements();

   @NotNull
   @Override
   default Iterator<T> iterator() {
      return this.elements().iterator();
   }
}
