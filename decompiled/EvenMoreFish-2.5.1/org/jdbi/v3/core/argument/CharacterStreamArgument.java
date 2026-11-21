package org.jdbi.v3.core.argument;

import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.jdbi.v3.core.statement.StatementContext;

public class CharacterStreamArgument implements Argument {
   private final Reader value;
   private final int length;

   public CharacterStreamArgument(Reader reader, int length) {
      this.value = reader;
      this.length = length;
   }

   @Override
   public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
      statement.setCharacterStream(position, this.value, this.length);
   }

   @Override
   public String toString() {
      return "<stream object cannot be read for toString() calls>";
   }
}
