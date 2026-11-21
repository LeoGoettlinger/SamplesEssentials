package org.jdbi.v3.core.argument;

import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.qualifier.NVarchar;

@NVarchar
class NVarcharArgumentFactory extends AbstractArgumentFactory<String> {
   NVarcharArgumentFactory() {
      super(-9);
   }

   protected Argument build(String value, ConfigRegistry config) {
      return (pos, stmt, ctx) -> stmt.setNString(pos, value);
   }
}
