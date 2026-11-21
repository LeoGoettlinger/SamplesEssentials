package org.jdbi.v3.core.codec;

import java.util.function.Function;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.meta.Beta;

@Beta
public interface Codec<T> {
   default ColumnMapper<T> getColumnMapper() {
      throw new UnsupportedOperationException("getColumnMapper");
   }

   default ColumnMapper<T> getColumnMapper(ConfigRegistry configRegistry) {
      return this.getColumnMapper();
   }

   default Function<T, Argument> getArgumentFunction() {
      throw new UnsupportedOperationException("getArgumentFunction");
   }

   default Function<T, Argument> getArgumentFunction(ConfigRegistry configRegistry) {
      return this.getArgumentFunction();
   }
}
