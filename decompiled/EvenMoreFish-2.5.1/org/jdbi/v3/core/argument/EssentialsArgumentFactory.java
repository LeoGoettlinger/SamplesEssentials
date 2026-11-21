package org.jdbi.v3.core.argument;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.UUID;

class EssentialsArgumentFactory extends DelegatingArgumentFactory {
   EssentialsArgumentFactory() {
      this.register(BigDecimal.class, 2, PreparedStatement::setBigDecimal);
      this.register(byte[].class, -3, PreparedStatement::setBytes);
      this.register(String.class, 12, PreparedStatement::setString);
      this.register(UUID.class, 12, PreparedStatement::setObject);
   }
}
