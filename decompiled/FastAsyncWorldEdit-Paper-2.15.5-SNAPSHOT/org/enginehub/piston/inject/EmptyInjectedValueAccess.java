package org.enginehub.piston.inject;

import java.util.Optional;

enum EmptyInjectedValueAccess implements InjectedValueAccess {
   INSTANCE;

   @Override
   public <T> Optional<T> injectedValue(Key<T> key, InjectedValueAccess context) {
      return Optional.empty();
   }
}
