package org.lushplugins.lushrewards.libraries.mysql.cj;

public interface CancelQueryTask {
   boolean cancel();

   Throwable getCaughtWhileCancelling();

   void setCaughtWhileCancelling(Throwable var1);

   Query getQueryToCancel();

   void setQueryToCancel(Query var1);
}
