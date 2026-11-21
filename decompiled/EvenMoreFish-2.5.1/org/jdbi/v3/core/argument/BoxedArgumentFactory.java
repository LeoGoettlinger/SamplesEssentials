package org.jdbi.v3.core.argument;

import java.sql.PreparedStatement;

class BoxedArgumentFactory extends DelegatingArgumentFactory {
   BoxedArgumentFactory() {
      this.register(Boolean.class, 16, PreparedStatement::setBoolean);
      this.register(Byte.class, -6, PreparedStatement::setByte);
      this.register(Character.class, 1, new ToStringBinder<>(PreparedStatement::setString));
      this.register(Short.class, 5, PreparedStatement::setShort);
      this.register(Integer.class, 4, PreparedStatement::setInt);
      this.register(Long.class, -5, PreparedStatement::setLong);
      this.register(Float.class, 6, PreparedStatement::setFloat);
      this.register(Double.class, 8, PreparedStatement::setDouble);
   }
}
