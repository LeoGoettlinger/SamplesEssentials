package org.jdbi.v3.core.statement;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.result.ResultBearing;
import org.jdbi.v3.core.result.ResultSetScanner;
import org.jdbi.v3.core.result.internal.ResultSetSupplier;

public final class MetaData extends BaseStatement<MetaData> implements ResultBearing {
   private final MetaData.MetaDataValueProvider<?> metaDataFunction;

   public MetaData(Handle handle, MetaData.MetaDataValueProvider<?> metaDataFunction) {
      super(handle);
      this.metaDataFunction = metaDataFunction;
   }

   @Override
   public <R> R scanResultSet(ResultSetScanner<R> resultSetScanner) {
      return ResultBearing.of(ResultSetSupplier.closingContext(this::execute, this.getContext()), this.getContext()).scanResultSet(resultSetScanner);
   }

   public <R> R execute() {
      try {
         Connection connection = this.getHandle().getConnection();
         return (R)this.metaDataFunction.provideValue(connection.getMetaData());
      } catch (SQLException var2) {
         throw new UnableToRetrieveMetaDataException(var2, this.getContext());
      }
   }

   @FunctionalInterface
   public interface MetaDataResultSetProvider extends MetaData.MetaDataValueProvider<ResultSet> {
      ResultSet provideValue(DatabaseMetaData databaseMetaData) throws SQLException;
   }

   @FunctionalInterface
   public interface MetaDataValueProvider<T> {
      T provideValue(DatabaseMetaData databaseMetaData) throws SQLException;
   }
}
