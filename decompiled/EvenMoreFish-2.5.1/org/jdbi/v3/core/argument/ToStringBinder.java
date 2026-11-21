package org.jdbi.v3.core.argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.jdbi.v3.core.argument.internal.StatementBinder;

class ToStringBinder<T> implements StatementBinder<T> {
   private final StatementBinder<String> stringSetter;

   ToStringBinder(StatementBinder<String> stringSetter) {
      this.stringSetter = stringSetter;
   }

   @Override
   public void bind(PreparedStatement p, int index, T value) throws SQLException {
      this.stringSetter.bind(p, index, String.valueOf(value));
   }
}
