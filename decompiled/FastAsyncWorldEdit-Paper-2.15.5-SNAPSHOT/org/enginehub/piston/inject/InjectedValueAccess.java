package org.enginehub.piston.inject;

import java.util.Optional;

public interface InjectedValueAccess {
   InjectedValueAccess EMPTY = EmptyInjectedValueAccess.INSTANCE;

   default <T> Optional<T> injectedValue(Key<T> key) {
      return this.injectedValue(key, this);
   }

   <T> Optional<T> injectedValue(Key<T> var1, InjectedValueAccess var2);
}
