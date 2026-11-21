package org.jdbi.v3.core.array;

import java.lang.reflect.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Stream;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.internal.IterableLike;
import org.jdbi.v3.core.statement.StatementContext;

class SqlArrayArgument<T> implements Argument {
   private final String typeName;
   private final Object[] array;

   SqlArrayArgument(SqlArrayType<T> arrayType, Object newArray) {
      this.typeName = arrayType.getTypeName();
      Stream<T> stream = (Stream<T>)IterableLike.stream(newArray);
      this.array = stream.map(arrayType::convertArrayElement).toArray(n -> (Object[])Array.newInstance(arrayType.getArrayElementClass(), n));
   }

   @Override
   public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
      SqlArrayArgumentStrategy argumentStyle = ctx.getSqlArrayArgumentStrategy();
      switch (argumentStyle) {
         case SQL_ARRAY:
            java.sql.Array sqlArray = statement.getConnection().createArrayOf(this.typeName, this.array);
            ctx.addCleanable(sqlArray::free);
            statement.setArray(position, sqlArray);
            break;
         case OBJECT_ARRAY:
            statement.setObject(position, this.array);
      }
   }

   @Override
   public String toString() {
      return this.typeName + "[] - " + Arrays.toString(this.array);
   }
}
