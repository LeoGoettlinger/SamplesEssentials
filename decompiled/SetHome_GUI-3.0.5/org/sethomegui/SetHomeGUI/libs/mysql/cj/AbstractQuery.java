package org.sethomegui.SetHomeGUI.libs.mysql.cj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.RuntimeProperty;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJTimeoutException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.OperationCancelledException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Message;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntityFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Resultset;

public abstract class AbstractQuery implements Query {
   static int statementCounter = 1;
   public NativeSession session = null;
   protected int statementId;
   protected RuntimeProperty<Integer> maxAllowedPacket;
   protected String charEncoding = null;
   protected Lock cancelTimeoutLock = new ReentrantLock();
   private Query.CancelStatus cancelStatus = Query.CancelStatus.NOT_CANCELED;
   protected long timeoutInMillis = 0L;
   protected List<Object> batchedArgs;
   protected Resultset.Type resultSetType = Resultset.Type.FORWARD_ONLY;
   protected int fetchSize = 0;
   protected final AtomicBoolean statementExecuting = new AtomicBoolean(false);
   protected String currentDb = null;
   protected boolean clearWarningsCalled = false;
   private long executeTime = -1L;
   protected QueryAttributesBindings queryAttributesBindings;
   protected final Lock lock = new ReentrantLock();

   public AbstractQuery(NativeSession sess) {
      statementCounter++;
      this.session = sess;
      this.maxAllowedPacket = sess.getPropertySet().getIntegerProperty(PropertyKey.maxAllowedPacket);
      this.charEncoding = sess.getPropertySet().getStringProperty(PropertyKey.characterEncoding).getValue();
      this.queryAttributesBindings = new NativeQueryAttributesBindings(sess);
   }

   @Override
   public int getId() {
      return this.statementId;
   }

   @Override
   public void setCancelStatus(Query.CancelStatus cs) {
      this.cancelStatus = cs;
   }

   @Override
   public long getExecuteTime() {
      return this.executeTime;
   }

   @Override
   public void setExecuteTime(long executeTime) {
      this.executeTime = executeTime;
   }

   @Override
   public void checkCancelTimeout() {
      this.cancelTimeoutLock.lock();

      try {
         if (this.cancelStatus != Query.CancelStatus.NOT_CANCELED) {
            CJException cause = (CJException)(this.cancelStatus == Query.CancelStatus.CANCELED_BY_TIMEOUT
               ? new CJTimeoutException()
               : new OperationCancelledException());
            this.resetCancelledState();
            throw cause;
         }
      } finally {
         this.cancelTimeoutLock.unlock();
      }
   }

   @Override
   public void resetCancelledState() {
      this.cancelTimeoutLock.lock();

      try {
         this.cancelStatus = Query.CancelStatus.NOT_CANCELED;
      } finally {
         this.cancelTimeoutLock.unlock();
      }
   }

   @Override
   public <T extends Resultset, M extends Message> ProtocolEntityFactory<T, M> getResultSetFactory() {
      return null;
   }

   public NativeSession getSession() {
      return this.session;
   }

   @Override
   public Lock getCancelTimeoutLock() {
      return this.cancelTimeoutLock;
   }

   @Override
   public void closeQuery() {
      this.queryAttributesBindings = null;
      this.session = null;
   }

   @Override
   public void addBatch(Object batch) {
      if (this.batchedArgs == null) {
         this.batchedArgs = new ArrayList<>();
      }

      this.batchedArgs.add(batch);
   }

   @Override
   public List<Object> getBatchedArgs() {
      return this.batchedArgs == null ? null : Collections.unmodifiableList(this.batchedArgs);
   }

   @Override
   public void clearBatchedArgs() {
      if (this.batchedArgs != null) {
         this.batchedArgs.clear();
      }
   }

   @Override
   public QueryAttributesBindings getQueryAttributesBindings() {
      return this.queryAttributesBindings;
   }

   @Override
   public int getResultFetchSize() {
      return this.fetchSize;
   }

   @Override
   public void setResultFetchSize(int fetchSize) {
      this.fetchSize = fetchSize;
   }

   @Override
   public Resultset.Type getResultType() {
      return this.resultSetType;
   }

   @Override
   public void setResultType(Resultset.Type resultSetType) {
      this.resultSetType = resultSetType;
   }

   @Override
   public long getTimeoutInMillis() {
      return this.timeoutInMillis;
   }

   @Override
   public void setTimeoutInMillis(long timeoutInMillis) {
      this.timeoutInMillis = timeoutInMillis;
   }

   @Override
   public CancelQueryTask startQueryTimer(Query stmtToCancel, long timeout) {
      if (this.session.getPropertySet().getBooleanProperty(PropertyKey.enableQueryTimeouts).getValue() && timeout != 0L) {
         CancelQueryTaskImpl timeoutTask = new CancelQueryTaskImpl(stmtToCancel);
         this.session.getCancelTimer().schedule(timeoutTask, timeout);
         return timeoutTask;
      } else {
         return null;
      }
   }

   @Override
   public void stopQueryTimer(CancelQueryTask timeoutTask, boolean rethrowCancelReason, boolean checkCancelTimeout) {
      if (timeoutTask != null) {
         timeoutTask.cancel();
         if (rethrowCancelReason && timeoutTask.getCaughtWhileCancelling() != null) {
            Throwable t = timeoutTask.getCaughtWhileCancelling();
            throw ExceptionFactory.createException(t.getMessage(), t);
         }

         if (this.session != null) {
            this.session.getCancelTimer().purge();
         }

         if (checkCancelTimeout) {
            this.checkCancelTimeout();
         }
      }
   }

   @Override
   public AtomicBoolean getStatementExecuting() {
      return this.statementExecuting;
   }

   @Override
   public String getCurrentDatabase() {
      return this.currentDb;
   }

   @Override
   public void setCurrentDatabase(String currentDb) {
      this.currentDb = currentDb;
   }

   @Override
   public boolean isClearWarningsCalled() {
      return this.clearWarningsCalled;
   }

   @Override
   public void setClearWarningsCalled(boolean clearWarningsCalled) {
      this.clearWarningsCalled = clearWarningsCalled;
   }

   @Override
   public void statementBegins() {
      this.clearWarningsCalled = false;
      this.statementExecuting.set(true);
   }
}
