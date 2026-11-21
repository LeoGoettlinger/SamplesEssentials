package org.lushplugins.lushrewards.libraries.mysql.cj.jdbc;

import java.sql.SQLException;
import java.sql.Savepoint;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.CJException;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionInterceptor;
import org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.exceptions.SQLError;
import org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.StringUtils;

public class MysqlSavepoint implements Savepoint {
   private String savepointName;
   private ExceptionInterceptor exceptionInterceptor;

   MysqlSavepoint(ExceptionInterceptor exceptionInterceptor) throws SQLException {
      this(StringUtils.getUniqueSavepointId(), exceptionInterceptor);
   }

   MysqlSavepoint(String name, ExceptionInterceptor exceptionInterceptor) throws SQLException {
      if (name != null && name.length() != 0) {
         this.savepointName = name;
         this.exceptionInterceptor = exceptionInterceptor;
      } else {
         throw SQLError.createSQLException(Messages.getString("MysqlSavepoint.0"), "S1009", exceptionInterceptor);
      }
   }

   @Override
   public int getSavepointId() throws SQLException {
      try {
         throw SQLError.createSQLException(Messages.getString("MysqlSavepoint.1"), "S1C00", this.exceptionInterceptor);
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.exceptionInterceptor);
      }
   }

   @Override
   public String getSavepointName() throws SQLException {
      try {
         return this.savepointName;
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.exceptionInterceptor);
      }
   }
}
