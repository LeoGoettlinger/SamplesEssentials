package com.donutorders.libs.mysql.cj.jdbc;

import com.donutorders.libs.mysql.cj.PingTarget;
import com.donutorders.libs.mysql.cj.Query;
import com.donutorders.libs.mysql.cj.exceptions.ExceptionInterceptor;
import com.donutorders.libs.mysql.cj.jdbc.result.ResultSetInternalMethods;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Statement;

public interface JdbcStatement extends Statement, Query {
   int MAX_ROWS = 50000000;

   void enableStreamingResults() throws SQLException;

   void disableStreamingResults() throws SQLException;

   void setLocalInfileInputStream(InputStream var1);

   InputStream getLocalInfileInputStream();

   void setPingTarget(PingTarget var1);

   ExceptionInterceptor getExceptionInterceptor();

   void removeOpenResultSet(ResultSetInternalMethods var1);

   int getOpenResultSetCount();

   void setHoldResultsOpenOverClose(boolean var1);

   Query getQuery();

   void setAttribute(String var1, Object var2);

   void clearAttributes();

   ResultSetInternalMethods getResultSetInternal();
}
