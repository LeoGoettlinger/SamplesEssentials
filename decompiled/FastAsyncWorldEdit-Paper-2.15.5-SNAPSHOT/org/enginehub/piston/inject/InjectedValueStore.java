package org.enginehub.piston.inject;

import org.enginehub.piston.util.ValueProvider;

public interface InjectedValueStore extends InjectedValueAccess {
   <T> void injectValue(Key<T> var1, ValueProvider<InjectedValueAccess, T> var2);
}
