package org.jdbi.v3.core.argument;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

class SqlTimeArgumentFactory extends DelegatingArgumentFactory {
   SqlTimeArgumentFactory() {
      this.register(Date.class, 93, (p, i, v) -> p.setTimestamp(i, new Timestamp(v.getTime())));
      this.register(java.sql.Date.class, 91, PreparedStatement::setDate);
      this.register(Time.class, 92, PreparedStatement::setTime);
      this.register(Timestamp.class, 93, PreparedStatement::setTimestamp);
   }
}
