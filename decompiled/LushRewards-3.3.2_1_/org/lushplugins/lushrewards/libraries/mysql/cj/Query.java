package org.lushplugins.lushrewards.libraries.mysql.cj;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Message;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntityFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Resultset;

public interface Query {
   int getId();

   void setCancelStatus(Query.CancelStatus var1);

   void checkCancelTimeout();

   <T extends Resultset, M extends Message> ProtocolEntityFactory<T, M> getResultSetFactory();

   Session getSession();

   Object getCancelTimeoutMutex();

   void resetCancelledState();

   void closeQuery();

   void addBatch(Object var1);

   List<Object> getBatchedArgs();

   void clearBatchedArgs();

   QueryAttributesBindings getQueryAttributesBindings();

   int getResultFetchSize();

   void setResultFetchSize(int var1);

   Resultset.Type getResultType();

   void setResultType(Resultset.Type var1);

   long getTimeoutInMillis();

   void setTimeoutInMillis(long var1);

   void setExecuteTime(long var1);

   long getExecuteTime();

   CancelQueryTask startQueryTimer(Query var1, long var2);

   AtomicBoolean getStatementExecuting();

   String getCurrentDatabase();

   void setCurrentDatabase(String var1);

   boolean isClearWarningsCalled();

   void setClearWarningsCalled(boolean var1);

   void statementBegins();

   void stopQueryTimer(CancelQueryTask var1, boolean var2, boolean var3);

   public static enum CancelStatus {
      NOT_CANCELED,
      CANCELED_BY_USER,
      CANCELED_BY_TIMEOUT;
   }
}
