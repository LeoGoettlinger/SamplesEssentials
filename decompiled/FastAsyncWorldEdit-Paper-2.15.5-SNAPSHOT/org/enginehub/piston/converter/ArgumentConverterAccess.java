package org.enginehub.piston.converter;

import java.util.Optional;
import java.util.Set;
import org.enginehub.piston.inject.Key;

public interface ArgumentConverterAccess {
   ArgumentConverterAccess EMPTY = EmptyArgumentConverterAccess.INSTANCE;

   Set<Key<?>> keySet();

   <T> Optional<ArgumentConverter<T>> getConverter(Key<T> var1);
}
