package org.jdbi.v3.core.argument;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.jdbi.v3.core.statement.StatementContext;

public class InputStreamArgument implements Argument {
   private final InputStream value;
   private final int length;
   private final boolean ascii;

   public InputStreamArgument(InputStream stream, int length, boolean ascii) {
      this.value = stream;
      this.length = length;
      this.ascii = ascii;
   }

   @Override
   public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
      if (this.ascii) {
         if (this.value == null) {
            statement.setNull(position, -1);
         } else {
            statement.setAsciiStream(position, this.value, this.length);
         }
      } else if (this.value == null) {
         statement.setNull(position, -4);
      } else {
         statement.setBinaryStream(position, this.value, this.length);
      }
   }

   @Override
   public String toString() {
      return "<stream object cannot be read for toString() calls>";
   }
}
